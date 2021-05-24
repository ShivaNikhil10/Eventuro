package com.dsce.eventmanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddEvent extends AppCompatActivity
{

    String UserID;
    String Date;
    String Time;
    String Title;
    String EventDetails;
    String Location;
    String Duration;
    String PDF_url;
    int Year,Month,Day,Hour,Minute;

    String fileName;

    int flag;
    Uri PDFUri;

    ArrayList<String> userList;
    ArrayList<String> members;

    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;
    ProgressBar progressBar;

    LinearLayout checkboxList;
    ScrollView svCheckbox;

    EditText editTextTitle;
    EditText editTextLocation;
    EditText editTextDetails;
    EditText editTextHours;
    EditText editTextMinutes;
    EditText editTextDate;
    EditText editTextTime;
    EditText editTextFile;

    CheckBox checkBoxListArray[];
    CheckBox selectAll;

    Button btnCancel;
    Button btnCreate;
    Button btnRefresh;
    ImageButton btnTime;
    ImageButton btnDate;
    ImageButton btnFileAdd;
    ImageButton btnFileDelete;


    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        mFirebaseAuth = FirebaseAuth.getInstance();
        editTextTitle = findViewById(R.id.etEventTitle);
        editTextDetails = findViewById(R.id.etEventDetails);
        editTextLocation = findViewById(R.id.etEventLocation);
        editTextHours = findViewById(R.id.etDurationHours);
        editTextMinutes = findViewById(R.id.etDurationMinutes);
        editTextDate = findViewById(R.id.etDate);
        editTextTime = findViewById(R.id.etTime);
        editTextFile = findViewById(R.id.etFile);
        btnCancel = findViewById(R.id.btnEventCancel);
        btnCreate = findViewById(R.id.btnEventCreate);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnTime = findViewById(R.id.btnTimeSelect);
        btnDate = findViewById(R.id.btnDateSelect);
        btnFileAdd = findViewById(R.id.ibFileAdd);
        btnFileDelete = findViewById(R.id.ibFileDelete);
        svCheckbox = findViewById(R.id.svCheckbox);
        checkboxList = findViewById(R.id.checkbocList);
        progressBar=findViewById(R.id.AddEventLoadProgressBar);
        userList = new ArrayList<String>();
        members = new ArrayList<String>();

        db = FirebaseFirestore.getInstance();
        storage=FirebaseStorage.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        final CollectionReference userRef = db.collection("Users");

        flag = 0;

        //retrieves list of all users and displays as a checkbox list
        progressBar.setVisibility(View.VISIBLE);
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
                    checkBoxListArray=new CheckBox[userList.size()];
                    //loop sets checkbox for each user
                    for(int i=0;i<userList.size();i++)
                    {

                        CheckBox checkBox=new CheckBox(getApplicationContext());
                        checkBox.setText(userList.get(i));
                        checkBoxListArray[i]=checkBox;
                        checkBox.setTextColor(Color.rgb(255, 255, 255));
                        checkBox.setButtonTintList(ColorStateList.valueOf(Color.rgb(51, 181, 229)));
                        //automatically ticks current user so he can't share the event to himself
                        if(userList.get(i).contentEquals(mFirebaseAuth.getCurrentUser().getEmail()))
                        {
                            checkBox.setChecked(true);
                            checkBox.setEnabled(false);
                        }
                        //adds checkbox to the view
                        checkboxList.addView(checkBox);
                        //handles ticking and unticking of a checkbox
                        //adds all ticked members to a list,which is used later to share the event(adding the event under the user in the database)
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                        {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                            {
                                if(isChecked)
                                {
                                    members.add(buttonView.getText().toString());
                                }
                                else if(!isChecked)
                                {
                                    members.remove(members.indexOf(buttonView.getText().toString()));
                                }
                            }
                        });
                    }

                }
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(AddEvent.this,"Error:"+e,Toast.LENGTH_SHORT).show();
            }
        })
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });



        //this.setContentView(checkboxList);

//        UserID = mFirebaseAuth.getUid();
  //      Title = editTextTitle.getText().toString().trim();
 //       EventDetails = editTextDetails.getText().toString().trim();
 //       Location = editTextLocation.getText().toString().trim();
 //       Duration = editTextHours.getText().toString().trim() + " hour/s "+editTextMinutes.getText().toString().trim() + " minutes";

        //clears all input boxes,checklist
        btnRefresh.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                editTextTitle.getText().clear();
                editTextDetails.getText().clear();
                editTextLocation.getText().clear();
                editTextHours.getText().clear();
                editTextMinutes.getText().clear();
                editTextTime.getText().clear();
                editTextDate.getText().clear();
                btnFileAdd.setImageResource(R.drawable.ic_add_black_24dp);
                editTextFile.getText().clear();
                for(int i=0;i<userList.size();i++)
                {
                    checkBoxListArray[i].setChecked(false);
                }
                selectAll.setChecked(false);
            }
                //Toast.makeText(AddEvent.this,"List:\n"+members,Toast.LENGTH_SHORT).show();

        });

        //time picker to select time
        btnTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);

                timePickerDialog = new TimePickerDialog(AddEvent.this, new TimePickerDialog.OnTimeSetListener()
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

                        Hour = hourOfDay;
                        Minute = minute;
                        Time = hourOfDay+":"+minute+" "+AmPm;
                        //Toast.makeText(AddEvent.this,"Time set =" + Hour + " : " + Minute + " " + AmPm,Toast.LENGTH_SHORT).show();
                        editTextTime.setText(Time);
                    }
                },currentHour,currentMinute,false);

                timePickerDialog.show();

            }
        });

        //date picker to select date
        btnDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Calendar calendar = Calendar.getInstance();
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentYear = calendar.get(Calendar.YEAR);

                datePickerDialog = new DatePickerDialog(AddEvent.this, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int mYear, int mMonth, int mDay)
                    {
                        Date = mDay+"/"+(mMonth+1)+"/"+mYear;
                        Year = mYear;
                        Month = mMonth+1;
                        Day = mDay;
                        //Toast.makeText(AddEvent.this,Date,Toast.LENGTH_SHORT).show();
                        editTextDate.setText(Date);
                    }
                },currentYear,currentMonth,currentDay);
                datePickerDialog.show();
            }
        });

        //redirects to home page
        btnCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i =new Intent(AddEvent.this,HomeActivity.class);
                startActivity(i);
            }
        });

        //selects file from phone storage
        btnFileAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                //checks for permission to access permission,if granted,user is directed to select a file. if not granted,permission is requested
                if(ContextCompat.checkSelfPermission(AddEvent.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {
                    selectPDF();
                    btnFileAdd.setImageResource(R.drawable.ic_edit_white_24dp);
                }
                else
                {
                    //requesting permission to access phone storage
                    ActivityCompat.requestPermissions(AddEvent.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},7);
                }


            }
        });

        //deselects the previously selected file
        btnFileDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                btnFileAdd.setImageResource(R.drawable.ic_add_black_24dp);
                editTextFile.getText().clear();
                PDFUri=null;
            }
        });

        //creates the event
        btnCreate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserID = mFirebaseAuth.getCurrentUser().getEmail();
                Title = editTextTitle.getText().toString().trim();
                EventDetails = editTextDetails.getText().toString().trim();
                Location = editTextLocation.getText().toString().trim();
                Duration = editTextHours.getText().toString().trim() + " hours "+editTextMinutes.getText().toString().trim() + " minutes";
                btnCreate.setEnabled(false);


                //sequence of if conditions to check if all required fields have been filled
                if(Title.isEmpty())
                {
                    editTextTitle.setError("Please enter Title.");
                    editTextTitle.requestFocus();
                }

                else if(EventDetails.isEmpty())
                {
                    editTextDetails.setError("Please enter Details.");
                    editTextDetails.requestFocus();
                }

                else if(Location.isEmpty())
                {
                    editTextLocation.setError("Please enter Location.");
                    editTextLocation.requestFocus();
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

                else if(editTextDate.getText().toString().trim().isEmpty())
                {
                    editTextDate.setError("Please select Date by pressing calendar icon above.");
                    editTextDate.requestFocus();
                }

                else if(editTextTime.getText().toString().trim().isEmpty())
                {
                    editTextTime.setError("Please select Time by pressing clock icon above.");
                    editTextTime.requestFocus();
                }

                else
                {
                    Toast.makeText(AddEvent.this,"Adding event...",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.VISIBLE);
                    //first uploads the pdf selected
                    if(PDFUri!=null)
                    {
                        StorageReference storageReference=storage.getReference();

                        storageReference.child("Uploads").child(fileName).putFile(PDFUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                                {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                    {
                                        Toast.makeText(AddEvent.this,"Upload Successful!",Toast.LENGTH_SHORT).show();

                                        //gets the download link for the selected file
                                        taskSnapshot.getStorage().getDownloadUrl()
                                                .addOnSuccessListener(new OnSuccessListener<Uri>()
                                                {
                                                    @Override
                                                    public void onSuccess(Uri uri)
                                                    {
                                                        PDF_url=uri.toString();

                                                        Event event = new Event(UserID,Title,EventDetails,Location,Duration,Date,Time,Year,Month,Day,Hour,Minute,members,PDF_url,fileName);

                                                        //adding event for current user
                                                        userRef.document(UserID).collection("Events").add(event).addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                                                        {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference)
                                                            {
                                                                Toast.makeText(AddEvent.this,"Event added Successfully!",Toast.LENGTH_SHORT).show();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener()
                                                        {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e)
                                                            {
                                                                Toast.makeText(AddEvent.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                                                flag=3;
                                                            }
                                                        });

                                                        //adding events for event members.(asynchronous task)
                                                        for(int i=0;i<members.size();i++)
                                                        {
                                                            userRef.document(members.get(i)).collection("Events").add(event).addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                                                            {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference)
                                                                {

                                                                }
                                                            }).addOnFailureListener(new OnFailureListener()
                                                            {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e)
                                                                {
                                                                    Toast.makeText(AddEvent.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                                                    //flag=2;
                                                                }
                                                            });
                                                        }

                                                        Intent i =new Intent(AddEvent.this,HomeActivity.class);
                                                        startActivity(i);

                                                    }
                                                });

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Toast.makeText(AddEvent.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                    }
                    //directly adds event for current user and event members without uploading file
                    //same procedure as above
                    else
                    {
                        Event event = new Event(UserID,Title,EventDetails,Location,Duration,Date,Time,Year,Month,Day,Hour,Minute,members,PDF_url,fileName);

                        userRef.document(UserID).collection("Events").add(event).addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                        {
                            @Override
                            public void onSuccess(DocumentReference documentReference)
                            {
                                Toast.makeText(AddEvent.this,"Event added Successfully!",Toast.LENGTH_SHORT).show();
                                //flag =1;
                            }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(AddEvent.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                flag=3;
                            }
                        });

                        for(int i=0;i<members.size();i++)
                        {
                            userRef.document(members.get(i)).collection("Events").add(event).addOnSuccessListener(new OnSuccessListener<DocumentReference>()
                            {
                                @Override
                                public void onSuccess(DocumentReference documentReference)
                                {

                                }
                            }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(AddEvent.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        Intent i =new Intent(AddEvent.this,HomeActivity.class);
                        startActivity(i);
                    }
                }
            }
        });
    }

    //directs user to select a file
    void selectPDF()
    {
        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,9);
    }

    //requests storage access permission from the user
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
            Toast.makeText(AddEvent.this,"Please grant permission...",Toast.LENGTH_SHORT).show();
        }
    }

    //handles uri and filename once file has been selected by user
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==9 && resultCode==RESULT_OK && data!=null)
        {

            PDFUri=data.getData();
            String path=PDFUri.getPath();
            int cut=path.lastIndexOf('/');
            fileName = path.substring(cut+1);
            editTextFile.setText(fileName);

        }
        else
        {
            Toast.makeText(AddEvent.this,"Please Select A File.",Toast.LENGTH_SHORT).show();
        }
    }
}
