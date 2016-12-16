package com.madsglobal.android.smarthw.common;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.madsglobal.android.smarthw.R;
import com.madsglobal.android.smarthw.ShsActivity;

/**
 * Created by Ximei on 6/23/2016.
 */
public class NotificationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If this activity is the root activity of the task, the app is not running
        if (isTaskRoot()) {

            final Intent startAppIntent = new Intent(this, ShsActivity.class);
            startAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startAppIntent);
        }

        // Now finish, which will drop the user in to the activity that was at the top
        //  of the task stack
        finish();
    }
}


