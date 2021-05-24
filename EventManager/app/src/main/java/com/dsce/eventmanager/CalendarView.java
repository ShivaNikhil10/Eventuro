package com.dsce.eventmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarView extends AppCompatActivity
{
    ImageButton btnLogout;
    ImageButton btnAddEvent;
    ImageButton btnHome;
    TextView textViewTodaysDate;
    ProgressBar progressBar;
    FirebaseAuth mFirebaseAuth;
    RecyclerView recyclerView;
    EventAdapter adapter;
    List<Event> eventList;
    android.widget.CalendarView calendarView;

    String date;

    FirebaseFirestore db;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view);

        btnAddEvent =findViewById(R.id.ibNewEventCalendarView);
        btnHome=findViewById(R.id.imageButtonHome);
        btnLogout=findViewById(R.id.imageButtonLogoutCalendarView);
        textViewTodaysDate=findViewById(R.id.etSelectedDate);
        calendarView=findViewById(R.id.cvCalendar);
        progressBar=findViewById(R.id.CalendarEventLoadProgressBar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        eventList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.listEventsDateSelected);

        db = FirebaseFirestore.getInstance();
        final CollectionReference userRef = db.collection("Users");

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        /*
        This segment of code is to display today's events by default when the Calendar View page is opened.
        */
        eventList.clear();                  //clears the event list which is to be displayed, before reading the events required from the database.
        progressBar.setVisibility(View.VISIBLE);
        Calendar calendar = Calendar.getInstance();
        final int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        final int currentMonth = calendar.get(Calendar.MONTH)+1;
        final int currentYear = calendar.get(Calendar.YEAR);
        textViewTodaysDate.setText(currentDay+"/"+currentMonth+"/"+currentYear);                    //sets the 'today's date' text view to the current date,by default
        //reading today's events from the database
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
                            for (DocumentSnapshot d:list)
                            {
                                Event event = d.toObject(Event.class);  //store the event read from database into a variable
                                eventList.add(event);                   //adds the events for the current day to the event list
                            }

                            adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(CalendarView.this,"No Events Today.",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(CalendarView.this,"Read Failed.\n"+e,Toast.LENGTH_SHORT).show();
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

        //passes the event list to be displayed in the card view
        adapter = new EventAdapter(this,eventList);
        recyclerView.setAdapter(adapter);

        //Takes user to the Add Event page on clicking the 'New Event' button
        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CalendarView.this,AddEvent.class);
                startActivity(i);
            }
        });

        //Takes user to the Home page on clicking the 'Homet' button
        btnHome.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(CalendarView.this,HomeActivity.class);
                startActivity(i);
            }
        });

        //Logs the user out after asking for confirmation.
        btnLogout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //confirms if the user wants to logout
                AlertDialog.Builder builder = new AlertDialog.Builder(CalendarView.this);
                builder.setTitle("Are you sure you want to Logout?");

                //Logs the user out
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        FirebaseAuth.getInstance().signOut();
                        Intent intToMain = new Intent(CalendarView.this, MainActivity.class);
                        startActivity(intToMain);
                    }
                })
                //Returns user to calendar view if the user does not wish to log out.
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

        //Takes the selected date by the user, and fetches and displays events on the selected date
        calendarView.setOnDateChangeListener(new android.widget.CalendarView.OnDateChangeListener()
        {
            @Override
            public void onSelectedDayChange(@NonNull android.widget.CalendarView view, int year, int month, int dayOfMonth)
            {
                date = dayOfMonth + "/" + (month+1) + "/" + year;                   //gets the selected date
                textViewTodaysDate.setText(date);                                   //sets the 'selected date' text view to the selected date.
                fetchSelectedDayEventList(dayOfMonth,month+1,year);          //fetches the events on the selected date and displays them
            }
        });

    }

    //fetches events on the selected day from the database
    void fetchSelectedDayEventList(int Day,int Month, int Year)
    {
        eventList.clear();
        progressBar.setVisibility(View.VISIBLE);
        //reading required data from database
        db.collection("Users").document(mFirebaseAuth.getCurrentUser().getEmail()).collection("Events")
                .whereEqualTo("year",Year)
                .whereEqualTo("month",Month)
                .whereEqualTo("day",Day)
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

                            for (DocumentSnapshot d:list)
                            {
                                Event event = d.toObject(Event.class);  //store the event read from database into a variable
                                eventList.add(event);                   //adds the events for the current day to the event list
                            }

                            adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            textViewTodaysDate.setText("There are no events for you on "+date);     //If there are no events, appropriate message is displayed to the user
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(CalendarView.this,"Read Failed.\n"+e,Toast.LENGTH_SHORT).show();
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

}
