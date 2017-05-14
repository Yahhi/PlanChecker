package ru.na_uglu.planchecker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

public class TimerEndReceiver extends BroadcastReceiver {
    Context context;
    String song;
    boolean vibration;
    boolean inform25minutes;
    int taskId;

    public static final int notificationId = 1002;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        vibration = intent.getBooleanExtra("vibration", true);
        song = intent.getStringExtra("song");
        inform25minutes = intent.getBooleanExtra("inform25minutes", true);
        taskId = intent.getIntExtra("taskId", 0);
        Log.i("POMODORO", "timer fired with 25minutes=" + inform25minutes);

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock w1 = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Alarm fired");
        w1.acquire(5000);

        createAppBarNotification(context);

        Intent sendToActivity = new Intent();
        sendToActivity.setAction("ru.na-uglu.planchecker");
        sendToActivity.putExtra("inform25minutes", inform25minutes);
        context.sendBroadcast(sendToActivity);
    }

    private void createAppBarNotification(Context context) {

        Intent resultIntent = new Intent(context, TimerActivity.class);
        resultIntent.putExtra("pomodoroMode", true);
        resultIntent.putExtra("taskId", taskId);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(TimerActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Uri songId = Uri.parse(song);
        mBuilder.setSound(songId);
        mBuilder.setAutoCancel(true);
        mBuilder.setSmallIcon(R.drawable.ic_stats);
        mBuilder.setContentTitle(context.getString(R.string.notification_title));
        if (inform25minutes) {
            mBuilder.setContentText(context.getString(R.string.notification_text_25));
        } else {
            mBuilder.setContentText(context.getString(R.string.notification_text_5));
        }
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationId, mBuilder.build());

        vibrateIfNeeded();
    }

    private void vibrateIfNeeded() {
        if (vibration) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500);
        }
    }
}
