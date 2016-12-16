package com.madsglobal.android.smarthw.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.madsglobal.android.smarthw.InterfaceType.InterfaceConfigAndStatus;
import com.madsglobal.android.smarthw.R;
import com.madsglobal.android.smarthw.ShsActivity;
import com.madsglobal.android.smarthw.ShsService;
import com.madsglobal.android.smarthw.common.SHSLifeCycleHandler;
import com.madsglobal.android.smarthw.common.Utility;
import com.madsglobal.android.smarthw.error.GattError;
import com.madsglobal.android.smarthw.setting.AinSettingActivity;

public class AinActivity extends AppCompatActivity {
    final String TAG = "AinActivity";
    ProgressBar[] progressBars = new ProgressBar[6];
    TextView[] progressValues = new TextView[6];
    int progressValue;
    AlertDialog alertDialog;

    private final BroadcastReceiver AinBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ShsService.BROADCAST_AIN_STATUS_UPDATE)) {
                int pinIndex = intent.getIntExtra(ShsService.BROADCAST_PINNUMBER, 0);
                int value = intent.getIntExtra(ShsService.BROADCAST_AIN_DATA, 0);
                getAnalogPinStatus(pinIndex,value);
            }else if(ShsService.BROADCAST_ERROR.equals(action)) {
                final int error = intent.getIntExtra(ShsService.EXTRA_ERROR_DATA, 0);
                showErrorMessage(error);
            }
        }
    };

    private void showErrorMessage(final int error) {
        Intent intent = new Intent(AinActivity.this, ShsService.class);
        stopService(intent);
        String message =""+ GattError.parse(error) ;
        Utility.setConnected(false);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.remote_exception_disconnect).setMessage(message);
        alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                Intent intent = new Intent(AinActivity.this, ShsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ain);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        progressBars[0] = (ProgressBar) findViewById(R.id.ain_progressBar1);
        progressValues[0] = (TextView) findViewById(R.id.ain1_value);

        progressBars[1] = (ProgressBar) findViewById(R.id.ain_progressBar2);
        progressValues[1] = (TextView) findViewById(R.id.ain2_value);

        progressBars[2] = (ProgressBar) findViewById(R.id.ain_progressBar3);
        progressValues[2] = (TextView) findViewById(R.id.ain3_value);

        progressBars[3] = (ProgressBar) findViewById(R.id.ain_progressBar4);
        progressValues[3] = (TextView) findViewById(R.id.ain4_value);

        progressBars[4] = (ProgressBar) findViewById(R.id.ain_progressBar5);
        progressValues[4] = (TextView) findViewById(R.id.ain5_value);

        progressBars[5] = (ProgressBar) findViewById(R.id.ain_progressBar6);
        progressValues[5] = (TextView) findViewById(R.id.ain6_value);

        for (int i = 0; i < 6; i++) {
            getAnalogPinStatus(i, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (int i = 0; i < 6; i++) {
            getAnalogPinStatus(i, 0);
        }
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter(ShsService.BROADCAST_AIN_STATUS_UPDATE);
        intentFilter.addAction(ShsService.BROADCAST_AIN_STATUS_UPDATE);
        intentFilter.addAction(ShsService.BROADCAST_ERROR);
        broadcastManager.registerReceiver(AinBroadcastReceiver, intentFilter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(AinBroadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!SHSLifeCycleHandler.isApplicationInForeground()) {
            Utility.stopService(this);
            Utility.showNotification(this,getString(R.string.shs_disconnected));
        }
    }

    private void getAnalogPinStatus(int pinIndex, int value) {
        if (InterfaceConfigAndStatus.getInstance().getAinConfig()[pinIndex].isEnabled()) {
            value = InterfaceConfigAndStatus.getInstance().getAinCurrentStatus()[pinIndex];
            progressValue = value;
            Log.i(TAG,"progress: "+progressValue);
        } else {
            progressValue = 0;
        }
        progressBars[pinIndex].setProgress(progressValue);
        progressValues[pinIndex].setText(String.valueOf(value));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ani_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.ain_setting){
            Intent intent = new Intent(this, AinSettingActivity.class);
            startActivity(intent);
        }
          return super.onOptionsItemSelected(item);
    }
}
