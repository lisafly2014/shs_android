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
import com.madsglobal.android.smarthw.setting.PwmSettingActivity;

public class PwmActivity extends AppCompatActivity {
    TextView[] dutyCycleValues = new TextView[ShsService.PWM_CHANNEL];
    SeekBar[] pwmSeekBar = new SeekBar[ShsService.PWM_CHANNEL];
    int[] pwmRetainValue = new int[ShsService.PWM_CHANNEL];
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
        Intent intent = new Intent(PwmActivity.this, ShsService.class);
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
                Intent intent = new Intent(PwmActivity.this, ShsActivity.class);
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
        setContentView(R.layout.activity_pwm);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        pwmSeekBar[0] = (SeekBar) findViewById(R.id.pwm0_seekBar);
        pwmSeekBar[1] = (SeekBar) findViewById(R.id.pwm1_seekBar);
        pwmSeekBar[2] = (SeekBar) findViewById(R.id.pwm2_seekBar);
        pwmSeekBar[3] = (SeekBar) findViewById(R.id.pwm3_seekBar);

        dutyCycleValues[0] = (TextView) findViewById(R.id.pwm0_dutyCycle);
        dutyCycleValues[1] = (TextView) findViewById(R.id.pwm1_dutyCycle);
        dutyCycleValues[2] = (TextView) findViewById(R.id.pwm2_dutyCycle);
        dutyCycleValues[3] = (TextView) findViewById(R.id.pwm3_dutyCycle);

        final Intent intent = new Intent(ShsService.BROADCAST_INTERFACE_STATUS_CHANGE);
        pwmSeekBar[0].setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        String dutyCycleString = progress + "%";
                        dutyCycleValues[0].setText(dutyCycleString);
                        if (pwmRetainValue[0] != progress) {
                            pwmRetainValue[0] = progress;
                            InterfaceConfigAndStatus.getInstance().setPwmCurrentDutyCycle(0, (byte) progress);
                            intent.putExtra(ShsService.BROADCAST_FUNCTIONID, (byte)ShsService.FUNCTION_ID_PWM_0);
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
        pwmSeekBar[1].setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        String dutyCycleString = progress + "%";
                        dutyCycleValues[1].setText(dutyCycleString);
                        if (pwmRetainValue[1] != progress) {
                            pwmRetainValue[1] = progress;
                            InterfaceConfigAndStatus.getInstance().setPwmCurrentDutyCycle(1, (byte) progress);
                            intent.putExtra(ShsService.BROADCAST_FUNCTIONID, (byte)(ShsService.FUNCTION_ID_PWM_0+1));
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

        pwmSeekBar[2].setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        String dutyCycleString = progress + "%";
                        dutyCycleValues[2].setText(dutyCycleString);
                        if (pwmRetainValue[2] != progress) {
                            pwmRetainValue[2] = progress;
                            InterfaceConfigAndStatus.getInstance().setPwmCurrentDutyCycle(2, (byte) progress);
                            intent.putExtra(ShsService.BROADCAST_FUNCTIONID, (byte)(ShsService.FUNCTION_ID_PWM_0+2));
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

        pwmSeekBar[3].setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        String dutyCycleString = progress + "%";
                        dutyCycleValues[3].setText(dutyCycleString);
                        if (pwmRetainValue[3] != progress) {
                            pwmRetainValue[3] = progress;
                            InterfaceConfigAndStatus.getInstance().setPwmCurrentDutyCycle(3, (byte) progress);
                            intent.putExtra(ShsService.BROADCAST_FUNCTIONID,(byte)(ShsService.FUNCTION_ID_PWM_0+3));
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
        for (int i = 0; i < ShsService.PWM_CHANNEL; i++) {
            setPwmDutyCycle(i);
        }
    }

    void setPwmDutyCycle(int index) {
        if (InterfaceConfigAndStatus.getInstance().getPwmConfig()[index].isEnabled()) {
            int dutyCycle = (int) InterfaceConfigAndStatus.getInstance().getPwmCurrentDutyCycle()[index];
            String dutyCycleString;
            if (dutyCycle != 0) {
                dutyCycleString = dutyCycle + "%";
            } else {
                dutyCycleString = "0%";
            }
            pwmRetainValue[index] = dutyCycle;
            pwmSeekBar[index].setProgress(dutyCycle);
            dutyCycleValues[index].setText(dutyCycleString);
        } else {
            pwmSeekBar[index].setProgress(0);
            dutyCycleValues[index].setText("0%");
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
        for (int i = 0; i < ShsService.PWM_CHANNEL; i++) {
            setPwmDutyCycle(i);
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
            Intent intent = new Intent(PwmActivity.this, PwmSettingActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
