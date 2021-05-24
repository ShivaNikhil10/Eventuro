package com.dsce.eventmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.CaseMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder>
{

    Context context;
    List<Event> eventList;
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore db;
    FirebaseStorage storage;
    String EventID;
    String sharedMembersID;
    String deleteMember;
    Event event;
    CollectionReference userRef;
    int k,pos;

    public EventAdapter(Context context, List<Event> eventList)
    {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view =inflater.inflate(R.layout.list_layout,null);
        EventViewHolder holder = new EventViewHolder(view);
        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, final int position)
    {
        event = eventList.get(holder.getAdapterPosition());
        pos=holder.getAdapterPosition();
        holder.textViewTitle.setText(event.getTitle());
        holder.textViewTime.setText(event.getTime());
        holder.textViewDate.setText(event.getDate());
        holder.textViewDuration.setText(event.getDuration());
        holder.textViewLocation.setText(event.getLocation());
        holder.textViewDetails.setText(event.getEventDetails());

    }

    @Override
    public int getItemCount()
    {
        return eventList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder
    {

        TextView textViewTitle,textViewDate,textViewTime,textViewLocation,textViewDuration,textViewDetails;
        ImageButton buttonDelete,buttonEdit;
        CardView parentLayout;

        public EventViewHolder(@NonNull View itemView)
        {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.textviewTitle);
            textViewDate = itemView.findViewById(R.id.textViewDateDisplay);
            textViewTime = itemView.findViewById(R.id.textViewTimeDisplay);
            textViewLocation = itemView.findViewById(R.id.textViewLocationDisplay);
            textViewDuration = itemView.findViewById(R.id.textviewDurationDisplay);
            textViewDetails = itemView.findViewById(R.id.textViewEventDetails);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            buttonEdit = itemView.findViewById(R.id.ibEdit);
            buttonDelete = itemView.findViewById(R.id.ibDelete);

            buttonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pos=getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION)
                    {
                        Event clickedEvent=eventList.get(pos);
                        //Toast.makeText(context,"Position:"+pos+"\nTitle:"+clickedEvent.Title,Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context,EventDetailsActivity.class);
                        intent.putExtra("Title",clickedEvent.getTitle());
                        intent.putExtra("Time",clickedEvent.getTime());
                        intent.putExtra("Date",clickedEvent.getDate());
                        intent.putExtra("Duration",clickedEvent.getDuration());
                        intent.putExtra("Location",clickedEvent.getLocation());
                        intent.putExtra("EventDetails",clickedEvent.getEventDetails());
                        context.startActivity(intent);
                    }
                }
            });

            buttonDelete.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    pos=getAdapterPosition();

                    if(pos != RecyclerView.NO_POSITION)
                    {
                        final Event clickedEvent=eventList.get(pos);

                        final String UserID=mFirebaseAuth.getCurrentUser().getEmail();
                        if(clickedEvent.Admin.equals(UserID))
                        {
                            userRef = db.collection("Users");

                            userRef.document(UserID).collection("Events")
                                    .whereEqualTo("title",clickedEvent.Title)
                                    .whereEqualTo("time",clickedEvent.Time)
                                    .whereEqualTo("duration",clickedEvent.Duration)
                                    .whereEqualTo("location",clickedEvent.Location)
                                    .whereEqualTo("eventDetails",clickedEvent.EventDetails)
                                    .whereEqualTo("date",clickedEvent.Date)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                                    {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                                        {
                                            if(!queryDocumentSnapshots.isEmpty())
                                            {
                                                List <DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                                                for (DocumentSnapshot d:documents)
                                                {
                                                    EventID = d.getId();

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                    builder.setTitle("Are you sure you want to delete Event?");

                                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                                    {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which)
                                                        {
                                                            Toast.makeText(context,"Deleting Event...",Toast.LENGTH_SHORT).show();

                                                            userRef.document(UserID).collection("Events").document(EventID)
                                                                    .delete()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                    {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if(task.isSuccessful())
                                                                            {

                                                                                if(clickedEvent.PDF_URL!=null)
                                                                                {
                                                                                    StorageReference storageReference = storage.getReferenceFromUrl(clickedEvent.PDF_URL);
                                                                                    storageReference.delete()
                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>()
                                                                                            {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid)
                                                                                                {
                                                                                                    if(clickedEvent.EventMembers.size()>0)
                                                                                                    {
                                                                                                        k=0;
                                                                                                        deleteSharedEvents(clickedEvent.EventMembers.get(k));
                                                                                                    }
                                                                                                }
                                                                                            })
                                                                                            .addOnFailureListener(new OnFailureListener()
                                                                                            {
                                                                                                @Override
                                                                                                public void onFailure(@NonNull Exception e)
                                                                                                {
                                                                                                    Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            });
                                                                                }
                                                                                else
                                                                                {
                                                                                    if(clickedEvent.EventMembers.size()>0)
                                                                                    {
                                                                                        k=0;
                                                                                        deleteSharedEvents(clickedEvent.EventMembers.get(k));
                                                                                    }
                                                                                }
                                                                                Toast.makeText(context,"Event Deleted.",Toast.LENGTH_SHORT).show();
                                                                                Intent i =new Intent(context,HomeActivity.class);
                                                                                context.startActivity(i);
                                                                            }
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener()
                                                                    {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e)
                                                                        {
                                                                            Toast.makeText(context,"Error: "+ e,Toast.LENGTH_SHORT).show();
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
                                            }
                                            else
                                            {
                                                Toast.makeText(context,"No events.",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            Toast.makeText(context,"Error: "+e,Toast.LENGTH_SHORT).show();
                                        }
                                    });


                        }
                        else
                        {
                            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                            alertDialog.setTitle("Cannot Delete Event");
                            alertDialog.setMessage("User does not have Admin Privileges.");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                            //Toast.makeText(context,"User does not have Admin Priveleges.",Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            });

        }
    }

    //recursive function to delete shared events for event members
    //has to be recursive as it is an asynchronous task,cannot be in a loop
    void deleteSharedEvents(String deletemember)
    {

        deleteMember=event.EventMembers.get(k);
        userRef.document(event.EventMembers.get(k)).collection("Events")
                .whereEqualTo("title",event.Title)
                .whereEqualTo("time",event.Time)
                .whereEqualTo("duration",event.Duration)
                .whereEqualTo("location",event.Location)
                .whereEqualTo("eventDetails",event.EventDetails)
                .whereEqualTo("date",event.Date)
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
                                sharedMembersID = d.getId();
                            }

                            userRef.document(deleteMember).collection("Events").document(sharedMembersID).delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            k++;
                                            if(k<event.EventMembers.size())
                                            {
                                                deleteSharedEvents(event.EventMembers.get(k));
                                            }
                                        }
                                    });

                        }

                    }
                });

    }

}
