package com.example.replypad;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

public class MyService extends Service {
    //変数
    NotificationChannel channel;
    NotificationManager manager;
    public static final String KEY = "REMOTE_INPUT";

    public MyService() {}

    @Override
    public void onCreate() {
        super.onCreate();

        channel = new NotificationChannel("REPLY",
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        RemoteInput remoteInput = new RemoteInput.Builder(KEY).setLabel("右のアイコンをタップして送信").build();

        Intent replyIntent = new Intent(this, MyReceiver.class);

        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(this, 101,
                replyIntent, PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_add,
                "開く", replyPendingIntent).addRemoteInput(remoteInput).build();

        Notification notification = new NotificationCompat.Builder(this, "REPLY")
                .setSmallIcon(R.drawable.ic_add)
                .setContentTitle("入力した内容をメモへ追加します")
                .addAction(action)
                .setSilent(true)
                .setPriority(2)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}