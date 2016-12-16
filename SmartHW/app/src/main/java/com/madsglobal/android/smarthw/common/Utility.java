package com.madsglobal.android.smarthw.common;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;

import com.madsglobal.android.smarthw.R;
import com.madsglobal.android.smarthw.ShsActivity;
import com.madsglobal.android.smarthw.ShsService;

/**
 * Created by Ximei on 6/23/2016.
 */
public class Utility {
    public static final int NOTIFICATION_ID = 304;
    private static boolean connected = false;

    public static void showNotification(Context context, String message) {
        Intent intent = new Intent(context, ShsActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setTicker(message)
                .setContentTitle(context.getString(R.string.shs))
                .setContentText(message)
                .setSmallIcon(R.drawable.shs_notification)
                .setAutoCancel(true);
        builder.setContentIntent(pIntent);
        final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    public static boolean isConnected() {
        return connected;
    }

    public static void setConnected(boolean mConnected) {
        connected = mConnected;
    }

    public static void stopService(Context context){
        Utility.setConnected(false);
        Intent stopIntent = new Intent(ShsService.BROADCAST_STOP_SERVICE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(stopIntent);
    }
}
