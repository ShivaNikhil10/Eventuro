package com.dsce.eventmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity
{

    ImageButton btnLogout;
    ImageButton btnAddEvent;
    ImageButton btnCalendar;
    TextView textViewToday;
    TextView textViewPastWeek;
    TextView textViewThisWeek;
    TextView textViewNoEvents;
    ProgressBar progressBar;
    FirebaseAuth mFirebaseAuth;
    FirebaseStorage storage;
    RecyclerView recyclerView;
    EventAdapter adapter;
    SwipeRefreshLayout refreshLayout;

    String deleteMember;
    String sharedMemberID;
    List<Event> eventList;

    FirebaseFirestore db;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private void fetchEventList()
    {
        eventList.clear();
        db.collection("Users").document(mFirebaseAuth.getCurrentUser().getEmail()).collection("Events")
                .orderBy("year", Query.Direction.ASCENDING)
                .orderBy("month")
                .orderBy("day")
                .orderBy("hour")
                .orderBy("minute")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                    {
                        if(!queryDocumentSnapshots.isEmpty())
                        {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();


                            for (DocumentSnapshot d:list)
                            {
                                Event event = d.toObject(Event.class);
                                eventList.add(event);
                            }

                            adapter.notifyDataSetChanged();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(HomeActivity.this,"Read Failed.\n"+e,Toast.LENGTH_SHORT).show();
                    }
                });

        //for(int i=0;i<5;i++)
        //{
        //   Event event=new Event("admin","title "+i,"Event details...","Somewhere","a lifetime","Some date","Sometime");
        //  eventList.add(event);
        //}

        adapter = new EventAdapter(this,eventList);
        recyclerView.setAdapter(adapter);

    }

    void fetchTodayEventList()
    {
        progressBar.setVisibility(View.VISIBLE);
        eventList.clear();
        final Calendar calendar = Calendar.getInstance();
        final int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        final int currentMonth = calendar.get(Calendar.MONTH)+1;
        final int currentYear = calendar.get(Calendar.YEAR);
        //Toast.makeText(HomeActivity.this,"hmm",Toast.LENGTH_SHORT).show();
        db.collection("Users").document(mFirebaseAuth.getCurrentUser().getEmail()).collection("Events")
                .whereEqualTo("year",currentYear)
                .whereEqualTo("month",currentMonth)
                .whereEqualTo("day",currentDay)
                .orderBy("hour", Query.Direction.ASCENDING)
                .orderBy("minute")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                    {
                        if(!queryDocumentSnapshots.isEmpty())
                        {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            //Toast.makeText(HomeActivity.this,"i'm in",Toast.LENGTH_SHORT).show();
                            int i=1;
                            for (DocumentSnapshot d:list)
                            {
                                Event event = d.toObject(Event.class);
                                if((event.Hour-1)>=calendar.get(Calendar.HOUR_OF_DAY) && event.Hour!=0)
                                {
                                    createNotificationChannel(i);
                                    //Toast.makeText(HomeActivity.this,"iteration:"+i,Toast.LENGTH_SHORT).show();
                                    setNotification(event.Hour,event.Minute,event.Title,event.EventDetails,i);
                                    i++;
                                }

                                eventList.add(event);
                            }
                            Toast.makeText(HomeActivity.this,"Total number of Events : "+eventList.size(),Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(HomeActivity.this,"No Events Today.",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(HomeActivity.this,"Read Failed.\n"+e,Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });


        adapter = new EventAdapter(this,eventList);
        recyclerView.setAdapter(adapter);

    }

    void fetchThisWeekEventList()
    {
        //setNotification();
        eventList.clear();
        Calendar calendar = Calendar.getInstance();
        final int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        final int currentMonth = calendar.get(Calendar.MONTH)+1;
        final int currentYear = calendar.get(Calendar.YEAR);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("Users").document(mFirebaseAuth.getCurrentUser().getEmail()).collection("Events")
                .whereEqualTo("year",currentYear)
                .whereEqualTo("month",currentMonth)
                .whereGreaterThan("day",currentDay)
                .whereLessThan("day",currentDay+7)
                .orderBy("day", Query.Direction.ASCENDING)
                .orderBy("hour")
                .orderBy("minute")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                    {
                        if(!queryDocumentSnapshots.isEmpty())
                        {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            //Toast.makeText(HomeActivity.this,"i'm in",Toast.LENGTH_SHORT).show();

                            for (DocumentSnapshot d:list)
                            {
                                Event event = d.toObject(Event.class);
                                eventList.add(event);
                            }

                            adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(HomeActivity.this,"No Events This Week.",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(HomeActivity.this,"Read Failed.\n"+e,Toast.LENGTH_SHORT).show();
                    }
                })
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });


        adapter = new EventAdapter(this,eventList);
        recyclerView.setAdapter(adapter);
    }

    void fetchpastWeekEventList()
    {
        eventList.clear();
        Calendar calendar = Calendar.getInstance();
        final int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        final int currentMonth = calendar.get(Calendar.MONTH)+1;
        final int currentYear = calendar.get(Calendar.YEAR);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("Users").document(mFirebaseAuth.getCurrentUser().getEmail()).collection("Events")
                .whereEqualTo("year",currentYear)
                .whereEqualTo("month",currentMonth)
                .whereGreaterThan("day",currentDay-7)
                .whereLessThan("day",currentDay)
                .orderBy("day", Query.Direction.ASCENDING)
                .orderBy("hour")
                .orderBy("minute")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                    {
                        if(!queryDocumentSnapshots.isEmpty())
                        {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot d:list)
                            {
                                Event event = d.toObject(Event.class);
                                eventList.add(event);
                            }

                            adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(HomeActivity.this,"No Events This Week.",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(HomeActivity.this,"Read Failed.\n"+e,Toast.LENGTH_SHORT).show();
                    }
                })
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });


        adapter = new EventAdapter(this,eventList);
        recyclerView.setAdapter(adapter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        textViewPastWeek=findViewById(R.id.tvPast);
        textViewToday=findViewById(R.id.tvToday);
        textViewThisWeek=findViewById(R.id.tvNext);
        textViewNoEvents=findViewById(R.id.tvNoEvents);
        btnLogout = findViewById(R.id.imageButtonLogout);
        btnAddEvent = findViewById(R.id.ibNewEvent);
        progressBar=findViewById(R.id.HomeEventLoadProgressBar);
        mFirebaseAuth = FirebaseAuth.getInstance();
        eventList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.listEvents);
        refreshLayout = findViewById(R.id.refreshLayout);
        btnCalendar =findViewById(R.id.imageButtonCalendar);

        db = FirebaseFirestore.getInstance();
        storage=FirebaseStorage.getInstance();
        final CollectionReference userRef = db.collection("Users");

        Map<String, Object> UserList = new HashMap<>();
        UserList.put("name", mFirebaseAuth.getCurrentUser().getEmail());

        autoDeleteEvents();

        //createNotificationChannel();

        db.collection("UserList").document(mFirebaseAuth.getCurrentUser().getEmail()).set(UserList).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(HomeActivity.this,"Registration Failed.\n"+e,Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });



        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()
            {
                //fetchEventList();
                textViewToday.setTextSize(22);
                textViewToday.setTextColor(Color.WHITE);
                textViewThisWeek.setTextColor(Color.GRAY);
                textViewPastWeek.setTextColor(Color.GRAY);
                textViewPastWeek.setTextSize(18);
                textViewThisWeek.setTextSize(18);
                fetchTodayEventList();
                refreshLayout.setRefreshing(false);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Are you sure you want to Logout?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        FirebaseAuth.getInstance().signOut();
                        Intent intToMain = new Intent(HomeActivity.this, MainActivity.class);
                        startActivity(intToMain);
                        HomeActivity.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                     @Override
                     public void onClick(DialogInterface dialog, int which)
                     {

                     }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });

        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this,AddEvent.class);
                startActivity(i);
            }
        });

        btnCalendar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(HomeActivity.this,CalendarView.class);
                startActivity(i);
            }
        });

        //fetchEventList();
        fetchTodayEventList();
        textViewPastWeek.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                textViewPastWeek.setTextSize(22);
                //textViewPastWeek.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
                textViewPastWeek.setTextColor(Color.WHITE);
                textViewThisWeek.setTextColor(Color.GRAY);
                textViewToday.setTextColor(Color.GRAY);
                textViewToday.setTextSize(18);
                textViewThisWeek.setTextSize(18);
                //textViewPastWeek.setText("Past Week");
                fetchpastWeekEventList();
            }
        });
        textViewToday.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                textViewToday.setTextSize(22);
                textViewToday.setTextColor(Color.WHITE);
                textViewThisWeek.setTextColor(Color.GRAY);
                textViewPastWeek.setTextColor(Color.GRAY);
                textViewPastWeek.setTextSize(18);
                textViewThisWeek.setTextSize(18);
                fetchTodayEventList();
            }
        });
        textViewThisWeek.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                textViewThisWeek.setTextSize(22);
                textViewThisWeek.setTextColor(Color.WHITE);
                textViewPastWeek.setTextColor(Color.GRAY);
                textViewToday.setTextColor(Color.GRAY);
                textViewToday.setTextSize(18);
                textViewPastWeek.setTextSize(18);
                fetchThisWeekEventList();
            }
        });

    }

    void autoDeleteEvents()
    {
        final Calendar calendar = Calendar.getInstance();
        final int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        final int currentMonth = calendar.get(Calendar.MONTH)+1;
        final int currentYear = calendar.get(Calendar.YEAR);
        db.collection("Users").document(mFirebaseAuth.getCurrentUser().getEmail()).collection("Events")
                .whereEqualTo("year",currentYear)
                .whereLessThan("month",currentMonth)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                    {
                        if(!queryDocumentSnapshots.isEmpty())
                        {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot d:list)
                            {
                                final Event DeleteEvent = d.toObject(Event.class);

                                if(DeleteEvent.Admin.equals(mFirebaseAuth.getCurrentUser().getEmail()))
                                {
                                    //Toast.makeText(HomeActivity.this,"Old Events:"+event.Title,Toast.LENGTH_SHORT).show();
                                    String DeleteId=d.getId();
                                    db.collection("Users").document(mFirebaseAuth.getCurrentUser().getEmail()).collection("Events").document(DeleteId)
                                            .delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                            {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    for(int i=0;i<DeleteEvent.EventMembers.size();i++)
                                                    {
                                                        deleteMember=DeleteEvent.EventMembers.get(i);
                                                        db.collection("Users").document(DeleteEvent.EventMembers.get(i)).collection("Events")
                                                                .whereEqualTo("title",DeleteEvent.Title)
                                                                .whereEqualTo("time",DeleteEvent.Time)
                                                                .whereEqualTo("duration",DeleteEvent.Duration)
                                                                .whereEqualTo("location",DeleteEvent.Location)
                                                                .whereEqualTo("eventDetails",DeleteEvent.EventDetails)
                                                                .whereEqualTo("date",DeleteEvent.Date)
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

                                                                                sharedMemberID = d.getId();

                                                                                //Toast.makeText(EventDetailsActivity.this,"set....not added\n"+d.getId()+"\n"+ID,Toast.LENGTH_SHORT).show();
                                                                            }

                                                                            db.collection("Users").document(deleteMember).collection("Events").document(sharedMemberID).delete();

                                                                        }

                                                                    }
                                                                });
                                                    }
                                                    if(DeleteEvent.PDF_URL!=null)
                                                    {
                                                        StorageReference storageReference = storage.getReferenceFromUrl(DeleteEvent.PDF_URL);
                                                        storageReference.delete()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid)
                                                                    {

                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener()
                                                                {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e)
                                                                    {
                                                                        Log.e("Message",e.toString());
                                                                    }
                                                                });
                                                    }
                                                }
                                            });


                                }
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        //Log.e("IndexRequired",e.toString());
                        Toast.makeText(HomeActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                    }
                });

    }

    void createNotificationChannel(int i)
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            CharSequence name="EventReminderChannel";
            String description="Channel for Event Reminder";
            int importance= NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("NotifyEvent"+i,name,importance);
            channel.setDescription(description);
            //Toast.makeText(HomeActivity.this," notification set with id:"+i,Toast.LENGTH_SHORT).show();
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


    }

    void setNotification(int hour,int minute,String title,String text,int notificationID)
    {
        String notificationText;
        if(hour>12)
        {
            notificationText="Event starts at "+(hour-12)+":"+minute+" PM";
        }
        else if(hour==12)
        {
            notificationText="Event starts at "+hour+":"+minute+" PM";
        }
        else
        {
            notificationText="Event starts at "+hour+":"+minute+" AM";
        }

        Intent intent = new Intent(HomeActivity.this,AlarmReciver.class);
        intent.putExtra("text",notificationText);
        intent.putExtra("title",title);
        intent.putExtra("id",notificationID);
        PendingIntent pendingIntent =PendingIntent.getBroadcast(HomeActivity.this,notificationID,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager =(AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar currentTimeCalendar=Calendar.getInstance();
        Calendar startTime=Calendar.getInstance();
        //startTime.set(Calendar.HOUR_OF_DAY,currentTimeCalendar.get(Calendar.HOUR_OF_DAY));
        //startTime.set(Calendar.MINUTE,currentTimeCalendar.get(Calendar.MINUTE)+1);
        if(hour==0)
            hour=1;
        startTime.set(Calendar.HOUR_OF_DAY,hour-1);
        startTime.set(Calendar.MINUTE,minute);
        startTime.set(Calendar.SECOND,0);
        //startTime.set(Calendar.YEAR,Calendar.MONTH,Calendar.DAY);
        long alarmTime=startTime.getTimeInMillis();
        //Toast.makeText(HomeActivity.this,title+" notification set with id:"+notificationID,Toast.LENGTH_SHORT).show();
        long currentTime =System.currentTimeMillis();
        long tenSeconds = 1000*10;
        alarmManager.set(AlarmManager.RTC_WAKEUP,alarmTime,pendingIntent);
    }

}
