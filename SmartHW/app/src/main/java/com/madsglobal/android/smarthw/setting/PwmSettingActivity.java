package com.madsglobal.android.smarthw.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.madsglobal.android.smarthw.InterfaceType.InterfaceConfigAndStatus;
import com.madsglobal.android.smarthw.R;
import com.madsglobal.android.smarthw.ShsActivity;
import com.madsglobal.android.smarthw.ShsService;
import com.madsglobal.android.smarthw.common.SHSLifeCycleHandler;
import com.madsglobal.android.smarthw.common.Utility;
import com.madsglobal.android.smarthw.error.GattError;

public class PwmSettingActivity extends AppCompatActivity {
    public final String TAG = "PwmSettingActivity";

    String[] pwmChannel = {" 0 ", " 1 ", " 2 ", " 3 "};
    String[] pwmPin = {" 0", " 1", " 2", " 3", " 4", " 5", " 6", " 7",
            " 8", " 9", "10", "11", "12", "13", "14", "15",
            "16", "17", "18", "19", "20", "21", "22", "23",
            "24", "25", "26", "27", "28", "29", "30", "31"};
    String[] pwmDrive = {"S0S1", "H0S1", "S0H1", "H0H1", "D0S1", "D0H1", "S0D1", "H0D1"};
    int currentChannel, currentPwmIndex, currentDriveValue;
    Spinner channel_spinner, pin_spinner,drive_spinner;
    boolean isLocked;
    Switch enableSwitch;
    Button pwmSave;
    ImageView pwmLock;
    private boolean isPwmSpinnerInitial = true;
    SeekBar seekBar;
    TextView dutyCycleValue;
    AlertDialog alertDialog;

    private final BroadcastReceiver msgBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ShsService.BROADCAST_CONFIG_ERROR)) {
                String message = intent.getStringExtra(ShsService.BROADCAST_CONFIG_ERROR_MSG);
                notifyMessage(message);
            }else if(ShsService.BROADCAST_ERROR.equals(action)) {
                final int error = intent.getIntExtra(ShsService.EXTRA_ERROR_DATA, 0);
                showErrorMessage(error);
            }
        }
    };

    private void showErrorMessage(final int error) {
        Intent intent = new Intent(PwmSettingActivity.this, ShsService.class);
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
                Intent intent = new Intent(PwmSettingActivity.this, ShsActivity.class);
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
        setContentView(R.layout.activity_pwm_setting);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isLocked = preferences.getBoolean("deviceLocked", true);

        enableSwitch = (Switch) findViewById(R.id.pwm_switch);
        pwmSave = (Button) findViewById(R.id.pwm_bt_save);
        pwmLock = (ImageView) findViewById(R.id.pwm_lock_icon);

        seekBar =(SeekBar)findViewById(R.id.pwm_setting_dutyCycle_seekBar);
        dutyCycleValue = (TextView) findViewById(R.id.pwm_dutyCycle_value);

        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        String dutyCycleString = progress + "%";
                        dutyCycleValue.setText(dutyCycleString);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );

        channel_spinner = (Spinner) findViewById(R.id.pwm_setting_channel);
        ArrayAdapter<String> pwmChannelAdapter = new ArrayAdapter<>(this, R.layout.pwm_spinner_channel, R.id.pwm_spinner_text, pwmChannel);
        channel_spinner.setAdapter(pwmChannelAdapter);

        currentChannel = preferences.getInt("pwmChannelSpinner", 0);
        channel_spinner.setSelection(currentChannel);

        pin_spinner = (Spinner) findViewById(R.id.pwm_setting_pin);
        ArrayAdapter<String>pwmPinAdapter = new ArrayAdapter<>(this, R.layout.pwm_spinner_pin, R.id.pwm_pin_text, pwmPin);
        pin_spinner.setAdapter(pwmPinAdapter);
        currentPwmIndex = InterfaceConfigAndStatus.getInstance().getPwmConfig()[currentChannel].getPinIndex();
        pin_spinner.setSelection(currentPwmIndex);

        drive_spinner = (Spinner) findViewById(R.id.pwm_setting_drive);
        ArrayAdapter<String> pwmDriveAdapter = new ArrayAdapter<>(this, R.layout.pwm_spinner_drive, R.id.pwm_drive_text, pwmDrive);
        drive_spinner.setAdapter(pwmDriveAdapter);
        currentDriveValue = InterfaceConfigAndStatus.getInstance().getPwmConfig()[currentChannel].getDriveValue();
        drive_spinner.setSelection(currentDriveValue);

        if (InterfaceConfigAndStatus.getInstance().getPwmConfig()[currentChannel].isEnabled()) {
            enableSwitch.setChecked(true);
            int dutyCycle = InterfaceConfigAndStatus.getInstance().getPwmConfig()[currentChannel].getDutyCycle();
            seekBar.setProgress(dutyCycle);

        } else {
            enableSwitch.setChecked(false);
            seekBar.setProgress(0);
        }

        channel_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isPwmSpinnerInitial) {
                    isPwmSpinnerInitial = false;
                } else {
                    currentChannel = position;
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putInt("pwmChannelSpinner", currentChannel);
                    edit.apply();

                    if (InterfaceConfigAndStatus.getInstance().getPwmConfig()[currentChannel].isEnabled()) {
                        enableSwitch.setChecked(true);
                        int pinIndex = InterfaceConfigAndStatus.getInstance().getPwmConfig()[currentChannel].getPinIndex();
                        int driveIndex = InterfaceConfigAndStatus.getInstance().getPwmConfig()[currentChannel].getDriveValue();
                        int pwmDutyCycleValue = InterfaceConfigAndStatus.getInstance().getPwmConfig()[currentChannel].getDutyCycle();
                        pin_spinner.setSelection(pinIndex);
                        drive_spinner.setSelection(driveIndex);
                        seekBar.setProgress(pwmDutyCycleValue);
                    } else {
                        enableSwitch.setChecked(false);
                        pin_spinner.setSelection(0);
                        drive_spinner.setSelection(0);
                        seekBar.setProgress(0);
                    }
                }

        }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        pin_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isPwmSpinnerInitial) {
                    currentPwmIndex = position;
                    if (enableSwitch.isChecked()) {
                        pin_spinner.setSelection(currentPwmIndex);
                    } else {
                        pin_spinner.setSelection(0);
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        drive_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isPwmSpinnerInitial) {
                    currentDriveValue = position;
                    if (enableSwitch.isChecked()) {
                        drive_spinner.setSelection(currentDriveValue);
                    } else {
                        drive_spinner.setSelection(0);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i(TAG, "pwm output switch is on.");
                } else {
                    Log.i(TAG, "pwm output switch is off");
                }
            }
        });

        isLocked =preferences.getBoolean("deviceLocked", true);
        if (isLocked) {
            pwmLock.setImageResource(R.drawable.ticked);
        } else {
            pwmLock.setImageResource(R.drawable.unticked);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ShsService.BROADCAST_CONFIG_ERROR);
        intentFilter.addAction(ShsService.BROADCAST_ERROR);
        broadcastManager.registerReceiver(msgBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(msgBroadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!SHSLifeCycleHandler.isApplicationInForeground()) {
            Utility.stopService(this);
            Utility.showNotification(this,getString(R.string.shs_disconnected));
        }
    }

    public void clickSavePwmButton(View view) {
        if (InterfaceConfigAndStatus.getInstance().getPwmConfig()[currentChannel].isEnabled()) {
            if (enableSwitch.isChecked()) {
                if ((InterfaceConfigAndStatus.getInstance().getPwmConfig()[currentChannel].getPinIndex() == currentPwmIndex)&&
                        (InterfaceConfigAndStatus.getInstance().getPwmConfig()[currentChannel].getDriveValue() == currentDriveValue)&&
                        (InterfaceConfigAndStatus.getInstance().getPwmConfig()[currentChannel].getDutyCycle()) == seekBar.getProgress()) {
                    new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(R.string.config_unchanged)
                            .setPositiveButton(R.string.ok, null).show();
                } else {

                    byte[] value = {ShsService.MANAGE_ID_INTERFACE_ADD,(byte)(ShsService.FUNCTION_ID_PWM_0+currentChannel),
                                    (byte) currentPwmIndex, (byte) currentDriveValue, (byte) seekBar.getProgress()};
                    sendPwmSettingBroadcast(value);
                }
            } else {
                //delete pin configuration
                Log.i(TAG, "delete pwm configuration");
                byte[] value = {ShsService.MANAGE_ID_INTERFACE_DELETE,(byte)(ShsService.FUNCTION_ID_PWM_0+currentChannel)};
                pin_spinner.setSelection(0);
                drive_spinner.setSelection(0);
                seekBar.setProgress(0);
                sendPwmSettingBroadcast(value);
            }
        } else {
            if (enableSwitch.isChecked()) {
                Log.i(TAG, "add new pwm configuration");
                //add new pin configuration
                byte[] value = {ShsService.MANAGE_ID_INTERFACE_ADD,(byte)(ShsService.FUNCTION_ID_PWM_0+currentChannel),
                                (byte) currentPwmIndex, (byte) currentDriveValue,(byte) seekBar.getProgress()};
                 sendPwmSettingBroadcast(value);
            } else {
                Log.i(TAG, "pwm not configured");
                new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(R.string.none_configured)
                        .setPositiveButton(R.string.ok, null).show();
            }
        }

    }

    public void clickLockAndSavePwmButton(View view) {
        isLocked = !isLocked;
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("deviceLocked", isLocked);
        edit.apply();
        if (isLocked) {
            Log.i(TAG, "After clicking save,lock device configuration");
            pwmLock.setImageResource(R.drawable.ticked);
        } else {
            Log.i(TAG, "After clicking save,do not lock device configuration");
            pwmLock.setImageResource(R.drawable.unticked);
        }
    }

    private void sendPwmSettingBroadcast(byte[] value) {
        Intent intent = new Intent(ShsService.BROADCAST_CONFIG_UPDATE);
        intent.putExtra(ShsService.BROADCAST_CONFIG_DATA, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void notifyMessage(String message) {
        new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(message)
                .setPositiveButton(R.string.ok, null).show();
    }
}
