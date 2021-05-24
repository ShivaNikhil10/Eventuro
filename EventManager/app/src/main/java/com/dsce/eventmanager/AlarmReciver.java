package com.dsce.eventmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReciver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String Title=intent.getStringExtra("title");
        String Text=intent.getStringExtra("text");
        int id=intent.getIntExtra("id",1);
        //int ids=Integer.parseInt(intent.getStringExtra("id"));
        //Toast.makeText(context,Title+" notification set with id:"+id,Toast.LENGTH_SHORT).show();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"NotifyEvent"+id)
                .setSmallIcon(R.mipmap.notif_icon)
                .setContentTitle(Title)
                .setContentText(Text)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(7,builder.build());
    }
}
