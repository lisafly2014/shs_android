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

public class ServoSettingActivity extends AppCompatActivity {
    public final String TAG = "ServoSettingActivity";

    String[] servoChannel = {" 0 ", " 1 ", " 2 ", " 3 "};
    String[] servoPin = {" 0", " 1", " 2", " 3", " 4", " 5", " 6", " 7",
            " 8", " 9", "10", "11", "12", "13", "14", "15",
            "16", "17", "18", "19", "20", "21", "22", "23",
            "24", "25", "26", "27", "28", "29", "30", "31"};
    String[] servoDrive = {"S0S1", "H0S1", "S0H1", "H0H1", "D0S1", "D0H1", "S0D1", "H0D1"};
    int currentChannel, currentServoIndex, currentDriveValue;
    Spinner channel_spinner;
    Spinner pin_spinner;
    Spinner drive_spinner;

    boolean isLocked;
    Switch enableSwitch;
    Button servoSave;
    ImageView servoLock;
    private boolean isServoSpinnerInitial = true;
    SeekBar seekBar;
    TextView percentageValue;
    AlertDialog alertDialog;

    private final BroadcastReceiver msgBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ShsService.BROADCAST_CONFIG_ERROR)) {
                String message = intent.getStringExtra(ShsService.BROADCAST_CONFIG_ERROR_MSG);
                notifyMessage(message);
            } else if (ShsService.BROADCAST_ERROR.equals(action)) {
                final int error = intent.getIntExtra(ShsService.EXTRA_ERROR_DATA, 0);
                showErrorMessage(error);
            }
        }
    };

    private void showErrorMessage(final int error) {
        Intent intent = new Intent(ServoSettingActivity.this, ShsService.class);
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
                Intent intent = new Intent(ServoSettingActivity.this, ShsActivity.class);
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
        setContentView(R.layout.activity_servo_setting);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isLocked = preferences.getBoolean("deviceLocked", true);

        enableSwitch = (Switch) findViewById(R.id.servo_switch);
        servoSave = (Button) findViewById(R.id.servo_bt_save);
        servoLock = (ImageView) findViewById(R.id.servo_lock_icon);


        seekBar = (SeekBar) findViewById(R.id.servo_setting_percentage_seekBar);
        percentageValue = (TextView) findViewById(R.id.servo_percentage_value);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        String pecentageString = progress+ "%";
                        percentageValue.setText(pecentageString);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );

        channel_spinner = (Spinner) findViewById(R.id.servo_setting_channel);
        ArrayAdapter<String> servoChannelAdapter = new ArrayAdapter<>(this, R.layout.servo_spinner_channel, R.id.servo_channel_text, servoChannel);
        channel_spinner.setAdapter(servoChannelAdapter);

        currentChannel = preferences.getInt("servoSpinner", 0);
        channel_spinner.setSelection(currentChannel);

        pin_spinner = (Spinner) findViewById(R.id.servo_setting_pin);
        ArrayAdapter<String> servoPinAdapter = new ArrayAdapter<>(this, R.layout.servo_spinner_pin, R.id.servo_pin_text, servoPin);
        pin_spinner.setAdapter(servoPinAdapter);
        currentServoIndex = InterfaceConfigAndStatus.getInstance().getServoConfig()[currentChannel].getPinIndex();
        pin_spinner.setSelection(currentServoIndex);

        drive_spinner = (Spinner) findViewById(R.id.servo_setting_drive);
        ArrayAdapter<String> servoDriveAdapter = new ArrayAdapter<>(this, R.layout.servo_spinner_drive, R.id.servo_drive_text, servoDrive);
        drive_spinner.setAdapter(servoDriveAdapter);
        currentDriveValue = InterfaceConfigAndStatus.getInstance().getServoConfig()[currentChannel].getDriveValue();
        drive_spinner.setSelection(currentDriveValue);

        if (InterfaceConfigAndStatus.getInstance().getServoConfig()[currentChannel].isEnabled()) {
            enableSwitch.setChecked(true);
            int pinIndex = InterfaceConfigAndStatus.getInstance().getServoConfig()[currentChannel].getPinIndex();
            int driveIndex = InterfaceConfigAndStatus.getInstance().getServoConfig()[currentChannel].getDriveValue();
            int percentage = InterfaceConfigAndStatus.getInstance().getServoConfig()[currentChannel].getPercentage();

            pin_spinner.setSelection(pinIndex);
            drive_spinner.setSelection(driveIndex);
            seekBar.setProgress(percentage);
        } else {
            enableSwitch.setChecked(false);
            pin_spinner.setSelection(0);
            drive_spinner.setSelection(0);
            seekBar.setProgress(0);
        }

        channel_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isServoSpinnerInitial) {
                    isServoSpinnerInitial = false;
                } else {
                    currentChannel = position;
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putInt("servoSpinner", currentChannel);
                    edit.apply();
                    Log.i(TAG, "servoServoRow: " + currentChannel);

                    if (InterfaceConfigAndStatus.getInstance().getServoConfig()[currentChannel].isEnabled()) {
                        enableSwitch.setChecked(true);
                        int percentage = InterfaceConfigAndStatus.getInstance().getServoConfig()[currentChannel].getPercentage();
                        seekBar.setProgress(percentage);

                    } else {
                        enableSwitch.setChecked(false);
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
                if (!isServoSpinnerInitial) {
                    currentServoIndex = position;
                    if (enableSwitch.isChecked()) {
                        pin_spinner.setSelection(currentServoIndex);
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
                if (!isServoSpinnerInitial) {
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
                    Log.i(TAG, "servo output switch is on.");
                } else {
                    Log.i(TAG, "servo output switch is off");
                }
            }
        });

        isLocked =preferences.getBoolean("deviceLocked", true);
        if (isLocked) {
            servoLock.setImageResource(R.drawable.ticked);
        } else {
            servoLock.setImageResource(R.drawable.unticked);
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
            Utility.showNotification(this, getString(R.string.shs_disconnected));
        }
    }

    public void clickSaveServoButton(View view) {
        if (InterfaceConfigAndStatus.getInstance().getServoConfig()[currentChannel].isEnabled()) {
            if (enableSwitch.isChecked()) {
                if ((InterfaceConfigAndStatus.getInstance().getServoConfig()[currentChannel].getPinIndex() == currentServoIndex) &&
                        (InterfaceConfigAndStatus.getInstance().getServoConfig()[currentChannel].getDriveValue() == currentDriveValue) &&
                        (InterfaceConfigAndStatus.getInstance().getServoConfig()[currentChannel].getPercentage()) == seekBar.getProgress()) {
                    new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(R.string.config_unchanged)
                            .setPositiveButton(R.string.ok, null).show();
                } else {
                    byte[] value = {ShsService.MANAGE_ID_INTERFACE_ADD,(byte)(ShsService.FUNCTION_ID_SERVO_0+currentChannel),
                                    (byte) currentServoIndex, (byte) currentDriveValue, (byte) seekBar.getProgress()};
                    sendServoSettingBroadcast(value);
                }
            } else {
                //delete pin configuration
                Log.i(TAG, "delete servo configuration");
                byte[] value = {ShsService.MANAGE_ID_INTERFACE_DELETE,(byte)(ShsService.FUNCTION_ID_SERVO_0+currentChannel)};
                pin_spinner.setSelection(0);
                drive_spinner.setSelection(0);
                seekBar.setProgress(0);
                sendServoSettingBroadcast(value);
            }
        } else {
            if (enableSwitch.isChecked()) {
                Log.i(TAG, "add new servo configuration");
                //add new pin configuration
                byte[] value = {ShsService.MANAGE_ID_INTERFACE_ADD,(byte)(ShsService.FUNCTION_ID_SERVO_0+currentChannel),
                                (byte) currentServoIndex, (byte) currentDriveValue,(byte) seekBar.getProgress()};
                sendServoSettingBroadcast(value);
            } else {
                Log.i(TAG, "servo not configured");
                new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(R.string.none_configured)
                        .setPositiveButton(R.string.ok, null).show();
            }
        }

    }

    public void clickLockAndSaveServoButton(View view) {
        isLocked = !isLocked;
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("deviceLocked", isLocked);
        edit.apply();
        if (isLocked) {
            Log.i(TAG, "After clicking save,lock device configuration");
            servoLock.setImageResource(R.drawable.ticked);
        } else {
            Log.i(TAG, "After clicking save,do not lock device configuration");
            servoLock.setImageResource(R.drawable.unticked);
        }
    }

    private void sendServoSettingBroadcast(byte[] value) {
        Intent intent = new Intent(ShsService.BROADCAST_CONFIG_UPDATE);
        intent.putExtra(ShsService.BROADCAST_CONFIG_DATA, value);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void notifyMessage(String message) {
        new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(message)
                .setPositiveButton(R.string.ok, null).show();
    }
}