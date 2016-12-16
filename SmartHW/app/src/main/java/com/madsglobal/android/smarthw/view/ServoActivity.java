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
import android.widget.SeekBar;
import android.widget.TextView;

import com.madsglobal.android.smarthw.InterfaceType.InterfaceConfigAndStatus;
import com.madsglobal.android.smarthw.R;
import com.madsglobal.android.smarthw.ShsActivity;
import com.madsglobal.android.smarthw.ShsService;
import com.madsglobal.android.smarthw.common.SHSLifeCycleHandler;
import com.madsglobal.android.smarthw.common.Utility;
import com.madsglobal.android.smarthw.error.GattError;
import com.madsglobal.android.smarthw.setting.ServoSettingActivity;

public class ServoActivity extends AppCompatActivity {
    final String TAG = "ServoActivity";
    TextView[] servoPercentages = new TextView[ShsService.SERVO_CHANNEL];
    SeekBar[] servoSeekBar = new SeekBar[ShsService.SERVO_CHANNEL];
    int[] servoRetainValue = new int[ShsService.SERVO_CHANNEL];
    AlertDialog alertDialog;

    private final BroadcastReceiver errorBroadcastReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (ShsService.BROADCAST_ERROR.equals(action)) {
                final int error = intent.getIntExtra(ShsService.EXTRA_ERROR_DATA, 0);
                showErrorMessage(error);
            }
        }
    };

    private void showErrorMessage(final int error) {
        Intent intent = new Intent(ServoActivity.this, ShsService.class);
        stopService(intent);
        String message = "" + GattError.parse(error);
        Utility.setConnected(false);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.remote_exception_disconnect).setMessage(message);
        alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                Intent intent = new Intent(ServoActivity.this, ShsActivity.class);
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
        setContentView(R.layout.activity_servo);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        servoSeekBar[0] = (SeekBar) findViewById(R.id.servo0_seekBar);
        servoSeekBar[1] = (SeekBar) findViewById(R.id.servo1_seekBar);
        servoSeekBar[2] = (SeekBar) findViewById(R.id.servo2_seekBar);
        servoSeekBar[3] = (SeekBar) findViewById(R.id.servo3_seekBar);


        servoPercentages[0] = (TextView) findViewById(R.id.servo0_percentage);
        servoPercentages[1] = (TextView) findViewById(R.id.servo1_percentage);
        servoPercentages[2] = (TextView) findViewById(R.id.servo2_percentage);
        servoPercentages[3] = (TextView) findViewById(R.id.servo3_percentage);
        final Intent intent = new Intent(ShsService.BROADCAST_INTERFACE_STATUS_CHANGE);
        servoSeekBar[0].setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        String percentageString = progress + "%";
                        servoPercentages[0].setText(percentageString);
                        if (servoRetainValue[0] != progress) {
                            servoRetainValue[0] = progress;
                            InterfaceConfigAndStatus.getInstance().setServoPercentage(0, (byte) progress);
                            intent.putExtra(ShsService.BROADCAST_FUNCTIONID, (byte)ShsService.FUNCTION_ID_SERVO_0);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );
        servoSeekBar[1].setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        String percentageString = progress + "%";
                        servoPercentages[1].setText(percentageString);
                        if (servoRetainValue[1] != progress) {
                            servoRetainValue[1] = progress;
                            InterfaceConfigAndStatus.getInstance().setServoPercentage(1, (byte) progress);
                            intent.putExtra(ShsService.BROADCAST_FUNCTIONID,(byte)(ShsService.FUNCTION_ID_SERVO_0+1));
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        }

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );
        servoSeekBar[2].setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        String percentageString = progress + "%";
                        servoPercentages[2].setText(percentageString);
                        if (servoRetainValue[2] != progress) {
                            servoRetainValue[2] = progress;
                            InterfaceConfigAndStatus.getInstance().setServoPercentage(2, (byte) progress);
                            intent.putExtra(ShsService.BROADCAST_FUNCTIONID,(byte)(ShsService.FUNCTION_ID_SERVO_0+2));
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );

        servoSeekBar[3].setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        String percentageString = progress + "%";
                        servoPercentages[3].setText(percentageString);
                        if (servoRetainValue[3] != progress) {
                            servoRetainValue[3] = progress;
                            InterfaceConfigAndStatus.getInstance().setServoPercentage(3, (byte) progress);
                            intent.putExtra(ShsService.BROADCAST_FUNCTIONID, (byte)(ShsService.FUNCTION_ID_SERVO_0+3));
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );


        for (int i = 0; i < ShsService.SERVO_CHANNEL; i++) {
            setServoPercentage(i);
        }
    }

    private void setServoPercentage(int index) {
        if (InterfaceConfigAndStatus.getInstance().getServoConfig()[index].isEnabled()) {
            int percentage = (int) InterfaceConfigAndStatus.getInstance().getServoPercentages()[index];
            String percentageString;
            if (percentage != 0) {
                percentageString = percentage + "%";
            } else {
                percentageString = "0%";
            }
            servoRetainValue[index] = percentage;
            servoSeekBar[index].setProgress(percentage);
            servoPercentages[index].setText(percentageString);
        } else {
            servoSeekBar[index].setProgress(0);
            servoPercentages[index].setText("0%");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(errorBroadcastReciever);

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (int i = 0; i < ShsService.SERVO_CHANNEL; i++) {
            setServoPercentage(i);
        }
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ShsService.BROADCAST_ERROR);
        broadcastManager.registerReceiver(errorBroadcastReciever, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!SHSLifeCycleHandler.isApplicationInForeground()) {
            Utility.stopService(this);
            Utility.showNotification(this, getString(R.string.shs_disconnected));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pwm_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.pwm_setting) {
            Intent intent = new Intent(ServoActivity.this, ServoSettingActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
