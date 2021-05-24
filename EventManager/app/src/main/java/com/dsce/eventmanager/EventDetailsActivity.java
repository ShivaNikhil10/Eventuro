package com.dsce.eventmanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.icu.text.CaseMap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventDetailsActivity extends AppCompatActivity
{
    //Variables for data of event to be updated
    Event updatedSharedEvent,addMemberSharedEvent;
    String Title,Time,Date,Duration,Location,EventDetails;
    String ID;
    String Admin;

    //Variables for updated event data
    String uTitle;
    String uEventDetails;
    String uLocation;
    String uDuration;
    String uHour;
    String uMinute;
    String uDate;
    String uTime;
    String pdf_url;
    String fileName;
    String UserID;

    String updateMember;                //member to be updated
    String addMember;                   //new members added to event
    String deleteMember;                //member whose event needs to be deleted

    int k,j;

    Uri PDFUri;                         //Local path for the PDF

    ArrayList<String> members;          //Members who are already part of the event
    ArrayList<String> eventMembers;     //New members with whom event is shared with
    ArrayList<String> userList;         //List of all users existing in database

    int Year,Month,Day,Hour,Minute;

    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore db;
    EditText editTextTitle;
    EditText editTextLocation;
    EditText editTextDetails;
    EditText editTextHours;
    EditText editTextMinutes;
    EditText editTextDate;
    EditText editTextTime;
    EditText editTextURL;
    EditText editTextFileName;
    TextView textViewTitle;

    LinearLayout checkboxList;

    Button btnCancel;
    Button btnDelete;
    Button btnUpdate;
    ImageButton btnTime;
    ImageButton btnDate;
    ImageButton btnFileAdd;
    ImageButton btnFileDelete;
    ImageButton btnFileDownload;
    ImageButton btnCopyLink;
    ProgressBar progressBar;

    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;

    FirebaseStorage storage;
    CollectionReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        //instanciating firebase objects
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("Users");
        mFirebaseAuth = FirebaseAuth.getInstance();

        editTextTitle = findViewById(R.id.etEventTitle);
        editTextDetails = findViewById(R.id.etEventDetails);
        editTextLocation = findViewById(R.id.etEventLocation);
        editTextHours = findViewById(R.id.etDurationHours);
        editTextMinutes = findViewById(R.id.etDurationMinutes);
        editTextDate = findViewById(R.id.etDate);
        editTextTime = findViewById(R.id.etTime);
        editTextURL=findViewById(R.id.etURL);
        editTextFileName=findViewById(R.id.etFileName);
        textViewTitle = findViewById(R.id.textView);
        btnCancel = findViewById(R.id.btnEventCancel);
        btnDelete = findViewById(R.id.btnEventDelete);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnTime = findViewById(R.id.btnTimeSelect);
        btnDate = findViewById(R.id.btnDateSelect);
        btnFileAdd = findViewById(R.id.ibFileEditAdd);
        btnFileDelete = findViewById(R.id.ibFileEditDelete);
        btnFileDownload =findViewById(R.id.ibDownload);
        btnCopyLink =findViewById(R.id.ibCopyLink);
        progressBar=findViewById(R.id.EventDetailsProgressBar);

        storage=FirebaseStorage.getInstance();

        checkboxList = findViewById(R.id.checkbocList);

        members = new ArrayList<String>();
        eventMembers = new ArrayList<String>();
        userList = new ArrayList<String>();

        getIncomingIntent();                    //Gets data of the event selected from HomeActivity

        progressBar.setVisibility(View.VISIBLE);                //Shows progress bar at top of screen
        //extracts data of selected event from database
        userRef.document(mFirebaseAuth.getCurrentUser().getEmail()).collection("Events")
                .whereEqualTo("title",Title)
                .whereEqualTo("time",Time)
                .whereEqualTo("duration",Duration)
                .whereEqualTo("location",Location)
                .whereEqualTo("eventDetails",EventDetails)
                .whereEqualTo("date",Date)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                    {
                        if(!queryDocumentSnapshots.isEmpty())
                        {
                            List <DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for(DocumentSnapshot d:list)
                            {
                                //assigning data of selected event from database to variables
                                Event event = d.toObject(Event.class);
                                ID = d.getId();
                                Year = event.Year;
                                Month = event.Month;
                                Day = event.Day;
                                Hour=event.Hour;
                                Minute = event.Minute;
                                Admin = event.Admin;
                                fileName = event.FileName;
                                pdf_url=event.PDF_URL;
                                if(pdf_url!=null)
                                {
                                    editTextURL.setText(pdf_url);
                                    editTextFileName.setText(fileName);
                                }
                                members.addAll(event.EventMembers);
                            }

                            db.collection("UserList").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                            {
                                @RequiresApi(api = Build.VERSION_CODES.P)
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                                {
                                    if(!queryDocumentSnapshots.isEmpty())
                                    {
                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                                        for(DocumentSnapshot d:list)
                                        {
                                            userList.add(d.getString("name"));
                                        }
                                        //Dynamically drawing checkboxes for choosing event members
                                        CheckBox checkBoxListArray[] =new CheckBox[userList.size()];
                                        for(int i=0;i<userList.size();i++)
                                        {
                                            CheckBox checkBox=new CheckBox(getApplicationContext());
                                            checkBox.setText(userList.get(i));
                                            checkBox.setTextColor(Color.rgb(255, 255, 255));
                                            checkBox.setButtonTintList(ColorStateList.valueOf(Color.rgb(51, 181, 229)));
                                            checkBoxListArray[i]=checkBox;
                                            //automatically checks and disables unchecking the current user in the list of user checkboxes
                                            //Done to prevent user from selecting themselves. If user selects themselves,a duplicate of event will be created.
                                            if(userList.get(i).contentEquals(mFirebaseAuth.getCurrentUser().getEmail()))
                                            {
                                                checkBox.setChecked(true);
                                                checkBox.setEnabled(false);
                                            }

                                            if(userList.get(i).contentEquals(Admin)){
                                                checkBox.setChecked(true);
                                                checkBox.setEnabled(false);
                                            }
                                            checkboxList.addView(checkBox);
                                            //Toast.makeText(EventDetailsActivity.this,Admin,Toast.LENGTH_SHORT).show();

                                            for(int k=0;k<members.size();k++)
                                            {
                                                //checks users who are already part of the event
                                                if(members.get(k).contentEquals(userList.get(i)))
                                                {
                                                    checkBox.setChecked(true);
                                                    checkBox.setEnabled(false);
                                                    break;
                                                }
                                            }
                                            //adds new members with whom event will be shared
                                            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                                            {
                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                                                {
                                                    if(isChecked)
                                                    {

                                                        eventMembers.add(buttonView.getText().toString());
                                                        {
                                                            members.add(buttonView.getText().toString());
                                                        }
                                                    }
                                                    else if(!isChecked)
                                                    {
                                                        eventMembers.remove(eventMembers.indexOf(buttonView.getText().toString()));
                                                        members.remove(buttonView.getText().toString());
                                                        //Toast.makeText(EventDetailsActivity.this,"members:"+members+"\n\nEventMembers:"+eventMembers,Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }

                                        //Checks members who are already part of the event
                                        for(int j=0;j<userList.size();j++)
                                        {
                                            if(members.contains(userList.get(j)))
                                            {
                                                checkBoxListArray[j].setChecked(true);
                                                checkBoxListArray[j].setEnabled(false);
                                            }
                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(EventDetailsActivity.this,"Error:"+e,Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                        else
                        {
                            Toast.makeText(EventDetailsActivity.this,"No events.",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(EventDetailsActivity.this,"Error:\n"+e,Toast.LENGTH_SHORT).show();
                    }
                })
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                progressBar.setVisibility(View.INVISIBLE);      //makes progress bar invisible as loading data is done
            }
        });

        //to choose updated time of event
        btnTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);

                timePickerDialog = new TimePickerDialog(EventDetailsActivity.this, new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                    {

                        String AmPm;
                        if (hourOfDay >= 12)
                        {
                            AmPm = "PM";
                        }
                        else
                        {
                            AmPm = "AM";
                        }
                        //Sets updated time of event
                        Hour = hourOfDay;
                        Minute = minute;
                        uTime = hourOfDay+":"+minute+" "+AmPm;
                        editTextTime.setText(uTime);
                    }
                },currentHour,currentMinute,false);

                timePickerDialog.show();

            }
        });

        //To choose updated date of event
        btnDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Calendar calendar = Calendar.getInstance();
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentYear = calendar.get(Calendar.YEAR);

                datePickerDialog = new DatePickerDialog(EventDetailsActivity.this, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int mYear, int mMonth, int mDay)
                    {
                        //sets updated date of the event
                        uDate = mDay+"/"+(mMonth+1)+"/"+mYear;
                        Year = mYear;
                        Month = mMonth+1;
                        Day = mDay;
                        editTextDate.setText(uDate);
                    }
                },currentYear,currentMonth,currentDay);
                datePickerDialog.show();
            }
        });

        //opens file URL in browser,thereby downloading the file
        btnFileDownload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if(pdf_url!=null)
                {
                    try
                    {
                        Intent i = new Intent("android.intent.action.MAIN");
                        i.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.MAIN"));
                        i.addCategory("android.intent.category.LAUNCHER");
                        i.setData(Uri.parse(pdf_url));
                        startActivity(i);
                    }
                    catch (ActivityNotFoundException e)
                    {
                        Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(pdf_url));
                        startActivity(i);
                    }
                }
                else
                {
                    Toast.makeText(EventDetailsActivity.this,"No File Previously stored for This Event.",Toast.LENGTH_SHORT).show();
                }

            }
        });

        //copies the file URL to clipboard
        btnCopyLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!editTextURL.getText().toString().isEmpty())
                {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("File URL", editTextURL.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(EventDetailsActivity.this,"Link copied to clipboard.",Toast.LENGTH_SHORT).show();

                }
                else
                {
                    Toast.makeText(EventDetailsActivity.this,"No File Previously stored for This Event.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Cancels update/delete and returns to Home Page
        btnCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i =new Intent(EventDetailsActivity.this,HomeActivity.class);
                startActivity(i);
            }
        });

        //Deletes the event for the user and all members of the event if the current user is the admin
        btnDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //checks if current user is admin
                if(Admin.equals(mFirebaseAuth.getCurrentUser().getEmail()))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailsActivity.this);
                    builder.setTitle("Are you sure you want to delete Event?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Toast.makeText(EventDetailsActivity.this,"Deleting Event...",Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.VISIBLE);            //sets progress bar to show event is being deleted
                            //Deletes event for current user/admin
                            userRef.document(mFirebaseAuth.getCurrentUser().getEmail()).collection("Events").document(ID)
                                    .delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                //Checks if the event has a file to delete
                                                if(pdf_url!=null)
                                                {
                                                    StorageReference storageReference = storage.getReferenceFromUrl(pdf_url);
                                                    //Deletes file from storage
                                                    storageReference.delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>()
                                                            {
                                                                @Override
                                                                public void onSuccess(Void aVoid)
                                                                {
                                                                    //Deletes event for all shared members
                                                                    if(members.size()>0)
                                                                    {
                                                                        k=0;
                                                                        deleteSharedEvents(members.get(k));
                                                                    }
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener()
                                                            {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e)
                                                                {
                                                                    Toast.makeText(EventDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                                else
                                                {
                                                    //Deletes event for all shared members
                                                    if(members.size()>0)
                                                    {
                                                        k=0;
                                                        deleteSharedEvents(members.get(k));
                                                    }
                                                }
                                                progressBar.setVisibility(View.INVISIBLE);          //sets progress bar to invisible to show deletion is complete
                                                Toast.makeText(EventDetailsActivity.this,"Event Deleted",Toast.LENGTH_SHORT).show();
                                                //returns to home page after deleting the event
                                                Intent i =new Intent(EventDetailsActivity.this,HomeActivity.class);
                                                startActivity(i);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            Toast.makeText(EventDetailsActivity.this,"Error: "+ e,Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                //If current user is not the admin
                else
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(EventDetailsActivity.this).create();
                    alertDialog.setTitle("Cannot Delete Event");
                    alertDialog.setMessage("User does not have Admin Privileges.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        });

        //Selects File from phone's Local Storage
        btnFileAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if(ContextCompat.checkSelfPermission(EventDetailsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {
                    selectPDF();
                    btnFileAdd.setImageResource(R.drawable.ic_edit_white_24dp);
                }
                else
                {
                    ActivityCompat.requestPermissions(EventDetailsActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},7);

                }
            }
        });

        //Deletes File from storage if file had een uploaded,or else simple clears the text views
        btnFileDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if(!editTextURL.getText().toString().isEmpty())
                {
                    StorageReference storageReference = storage.getReferenceFromUrl(pdf_url);
                    //Deletes the File from storage
                    storageReference.delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>()
                            {
                                @Override
                                public void onSuccess(Void aVoid)
                                {
                                    Toast.makeText(EventDetailsActivity.this,"File Deleted.",Toast.LENGTH_SHORT).show();
                                    //Clears the text views and resets required values
                                    btnFileAdd.setImageResource(R.drawable.ic_add_black_24dp);
                                    editTextFileName.getText().clear();
                                    editTextURL.getText().clear();
                                    pdf_url=null;
                                    PDFUri=null;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(EventDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        //Updates the event for current user and all event members,if the current user is the admin of the event
        btnUpdate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //checks if current user is the admin of the event
                if(Admin.equals(mFirebaseAuth.getCurrentUser().getEmail()))
                {
                    Toast.makeText(EventDetailsActivity.this,"Updating Event...",Toast.LENGTH_SHORT).show();

                    UserID = mFirebaseAuth.getCurrentUser().getEmail();

                    //Retrieves updated data of the event to write in database
                    uTitle = editTextTitle.getText().toString().trim();
                    uEventDetails = editTextDetails.getText().toString().trim();
                    uLocation = editTextLocation.getText().toString().trim();
                    uHour=editTextHours.getText().toString().trim();
                    uMinute=editTextMinutes.getText().toString().trim();
                    uDuration = editTextHours.getText().toString().trim() + " hours "+editTextMinutes.getText().toString().trim() + " minutes";
                    uDate = editTextDate.getText().toString().trim();
                    uTime = editTextTime.getText().toString().trim();

                    //Checks for empty fields and prompts user to fill them
                    if(uTitle.isEmpty())
                    {
                        editTextTitle.setError("Please enter Title.");
                        editTextTitle.requestFocus();
                    }

                    else if(uLocation.isEmpty())
                    {
                        editTextLocation.setError("Please enter Location.");
                        editTextLocation.requestFocus();
                    }

                    else if(uEventDetails.isEmpty())
                    {
                        editTextDetails.setError("Please enter Details.");
                        editTextDetails.requestFocus();
                    }

                    else if(editTextHours.getText().toString().trim().isEmpty())
                    {
                        editTextHours.setError("Please fill Duration.");
                        editTextHours.requestFocus();
                    }

                    else if(editTextMinutes.getText().toString().trim().isEmpty())
                    {
                        editTextMinutes.setError("Please fill Duration.");
                        editTextMinutes.requestFocus();
                    }

                    else if(uTime.isEmpty())
                    {
                        editTextTime.setError("Please select Time by pressing clock icon above.");
                        editTextTime.requestFocus();
                    }

                    else if(uDate.isEmpty())
                    {
                        editTextDate.setError("Please select date by pressing calendar icon above.");
                        editTextDate.requestFocus();
                    }

                    else
                    {
                        //checks if current user is event admin
                        if(Admin.equals(UserID))
                        {
                            progressBar.setVisibility(View.VISIBLE);
                            //checks if event has a file
                            if(PDFUri!=null)
                            {
                                StorageReference storageReference=storage.getReference();

                                //adding file to database if a file is selected
                                storageReference.child("Uploads").child(fileName).putFile(PDFUri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                                        {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                            {
                                                Toast.makeText(EventDetailsActivity.this,"Upload Successful!",Toast.LENGTH_SHORT).show();
                                                taskSnapshot.getStorage().getDownloadUrl()
                                                        .addOnSuccessListener(new OnSuccessListener<Uri>()
                                                        {
                                                            @Override
                                                            public void onSuccess(Uri uri)
                                                            {
                                                                pdf_url=uri.toString();

                                                                final Event updatedEvent = new Event(UserID,uTitle,uEventDetails,uLocation,uDuration,uDate,uTime,Year,Month,Day,Hour,Minute,members,pdf_url,fileName);

                                                                //Updates event for current user
                                                                userRef.document(UserID).collection("Events").document(ID).set(updatedEvent)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>()
                                                                        {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid)
                                                                            {
                                                                                /*
                                                                                for(int i=0;i<eventMembers.size();i++)
                                                                                {
                                                                                    userRef.document(eventMembers.get(i)).collection("Events").add(updatedEvent);
                                                                                    members.remove(members.indexOf(eventMembers.get(i)));
                                                                                }
                                                                                eventMembers.clear();
                                                                                */

                                                                                //Adds event to newly selected event members
                                                                                if(eventMembers.size()!=0)
                                                                                {
                                                                                    j=0;
                                                                                    addSharedEvents(eventMembers.get(j),updatedEvent);
                                                                                }

                                                                                //Updates event for shared members
                                                                                k=0;
                                                                                if ((members.size()>0))
                                                                                {
                                                                                    updateSharedEvents(members.get(k),updatedEvent);
                                                                                }

                                                                                Toast.makeText(EventDetailsActivity.this,"Event Updated.",Toast.LENGTH_SHORT).show();
                                                                                //Returns to Home page on completion
                                                                                Intent i =new Intent(EventDetailsActivity.this,HomeActivity.class);
                                                                                startActivity(i);
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener()
                                                                        {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e)
                                                                            {
                                                                                Toast.makeText(EventDetailsActivity.this,"Error: "+e,Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });

                                                            }
                                                        });

                                            }
                                        })
                                .addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Toast.makeText(EventDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                                    {
                                        progressBar.setVisibility(View.INVISIBLE);//Sets progress bar to invisible to show that event has been updated
                                    }
                                });
                            }
                            //if the event does not have a file
                            else
                            {
                                final Event updatedEvent = new Event(UserID,uTitle,uEventDetails,uLocation,uDuration,uDate,uTime,Year,Month,Day,Hour,Minute,members,pdf_url,fileName);

                                //Updates event for current user
                                userRef.document(UserID).collection("Events").document(ID).set(updatedEvent)
                                        .addOnSuccessListener(new OnSuccessListener<Void>()
                                        {
                                            @Override
                                            public void onSuccess(Void aVoid)
                                            {
                                                //Adds event to newly shared mails
                                                if(eventMembers.size()!=0)
                                                {
                                                    j=0;
                                                    addSharedEvents(eventMembers.get(j),updatedEvent);
                                                }

                                                //Updates events for members who the event is already shared with
                                                k=0;
                                                if(members.size()>0)
                                                {
                                                    updateSharedEvents(members.get(k), updatedEvent);
                                                }

                                                Toast.makeText(EventDetailsActivity.this,"Event Updated.",Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.INVISIBLE);          //Shows updation has been completed
                                                //Returns to Home page after deletion
                                                Intent i =new Intent(EventDetailsActivity.this,HomeActivity.class);
                                                startActivity(i);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener()
                                        {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                Toast.makeText(EventDetailsActivity.this,"Error: "+e,Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }

                        }
                        else
                        {
                            AlertDialog alertDialog = new AlertDialog.Builder(EventDetailsActivity.this).create();
                            alertDialog.setTitle("Cannot Update Event");
                            alertDialog.setMessage("User does not have Admin Privileges.");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }
                    }
                }

                else
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(EventDetailsActivity.this).create();
                    alertDialog.setTitle("CannotUpdate Event");
                    alertDialog.setMessage("User does not have Admin Privileges.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }


            }
        });

    }

    //Recursively adds events to members with whom event is shared with
    //cannot use for loop as firebase functions are asynch
    void addSharedEvents(String addmember,Event updatedevent)
    {
        addMember=eventMembers.get(j);
        addMemberSharedEvent=updatedevent;
        //Adds event to a shared member
        userRef.document(addMember).collection("Events").add(addMemberSharedEvent)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>()
                {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task)
                    {
                        j++;
                        if(j<eventMembers.size())
                        {
                            addSharedEvents(eventMembers.get(j),addMemberSharedEvent);
                        }
                    }
                });
    }

    //Recursively adds events to shared members
    //cannot use for loop as firebase functions are asynch
    void updateSharedEvents(String updatemember, final Event updatedevent)
    {
        updateMember=members.get(k);
        updatedSharedEvent=updatedevent;
        //To update event for shared members
        userRef.document(updateMember).collection("Events")
                .whereEqualTo("title",Title)
                .whereEqualTo("time",Time)
                .whereEqualTo("duration",Duration)
                .whereEqualTo("location",Location)
                .whereEqualTo("eventDetails",EventDetails)
                .whereEqualTo("date",Date)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                    {
                        if(!queryDocumentSnapshots.isEmpty())
                        {
                            List <DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for(DocumentSnapshot d:list)
                            {
                                //retrieves ID for the event in a event member
                                ID = d.getId();
                            }

                            //Updates event
                            userRef.document(updateMember).collection("Events").document(ID).set(updatedSharedEvent)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            k++;
                                            if(k<members.size())
                                            {
                                                updateSharedEvents(members.get(k),updatedSharedEvent);
                                            }
                                        }
                                    });

                        }

                    }
                });
    }

    //Recursively deletes events for shared members
    //cannot use for loop as firebase functions are asynch
    void deleteSharedEvents(String deletemember)
    {
        deleteMember=members.get(k);

        userRef.document(members.get(k)).collection("Events")
                .whereEqualTo("title",Title)
                .whereEqualTo("time",Time)
                .whereEqualTo("duration",Duration)
                .whereEqualTo("location",Location)
                .whereEqualTo("eventDetails",EventDetails)
                .whereEqualTo("date",Date)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                    {
                        if(!queryDocumentSnapshots.isEmpty())
                        {
                            List <DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for(DocumentSnapshot d:list)
                            {
                                //retrieves ID of event to be deleted
                                ID = d.getId();
                            }

                            //Deleting event for shared members
                            userRef.document(deleteMember).collection("Events").document(ID).delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            k++;
                                            if(k<members.size())
                                            {
                                                deleteSharedEvents(members.get(k));
                                            }
                                        }
                                    });

                        }

                    }
                });

    }

    //retrieves data of selected  event from home page
    private void getIncomingIntent()
    {
        if(getIntent().hasExtra("Title")
            && getIntent().hasExtra("Time")
            && getIntent().hasExtra("Date")
            && getIntent().hasExtra("Duration")
            && getIntent().hasExtra("Location")
            && getIntent().hasExtra("EventDetails"))
        {
            Title = getIntent().getStringExtra("Title");
            Time = getIntent().getStringExtra("Time");
            Date = getIntent().getStringExtra("Date");
            Duration = getIntent().getStringExtra("Duration");
            Location = getIntent().getStringExtra("Location");
            EventDetails = getIntent().getStringExtra("EventDetails");

            setData(Title,Time,Date,Duration,Location,EventDetails);
        }
    }

    //Sets event data to text view
    private void setData(String title,String time,String date,String duration,String location,String eventDetails)
    {
        editTextTitle.setText(title);
        editTextDate.setText(date);
        editTextDetails.setText(eventDetails);
        editTextLocation.setText(location);
        editTextTime.setText(time);
        textViewTitle.setText(title);
        String minutesonly = duration.replaceAll("(^\\d+ hours )","");
        minutesonly= minutesonly.replaceAll("[^0-9]", "");
        String hoursonly = duration.replaceAll("(\\d+ minutes$)","");
        hoursonly= hoursonly.replaceAll("[^0-9]", "");
        editTextHours.setText(hoursonly);
        editTextMinutes.setText(minutesonly);
    }

    //Choose file for upload
    void selectPDF()
    {
        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,9);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==7 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            selectPDF();
        }
        else
        {
            Toast.makeText(EventDetailsActivity.this,"Please grant permission...",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==9 && resultCode==RESULT_OK && data!=null)
        {

            PDFUri=data.getData();
            String path=PDFUri.getPath();
            int cut=path.lastIndexOf('/');
            fileName = path.substring(cut+1);
            editTextFileName.setText(fileName);

        }
        else
        {
            Toast.makeText(EventDetailsActivity.this,"Please Select A File.",Toast.LENGTH_SHORT).show();
        }
    }

}
