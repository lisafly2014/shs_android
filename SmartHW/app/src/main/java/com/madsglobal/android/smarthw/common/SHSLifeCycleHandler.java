package com.madsglobal.android.smarthw.common;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.madsglobal.android.smarthw.ShsService;

/**
 * Created by Ximei on 6/13/2016.
 */
public class SHSLifeCycleHandler implements Application.ActivityLifecycleCallbacks  {
    private static int resumed;
    private static int stopped;
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    // And add this public static function
    public static boolean isApplicationInForeground() {
        return resumed > stopped;
    }

}
