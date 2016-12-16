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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;

import com.madsglobal.android.smarthw.InterfaceType.InterfaceConfigAndStatus;
import com.madsglobal.android.smarthw.R;
import com.madsglobal.android.smarthw.ShsActivity;
import com.madsglobal.android.smarthw.ShsService;
import com.madsglobal.android.smarthw.common.SHSLifeCycleHandler;
import com.madsglobal.android.smarthw.common.Utility;
import com.madsglobal.android.smarthw.error.GattError;

public class DoutSettingActivity extends AppCompatActivity {
    final String TAG = "DoutPinSetting";

    String[] doutArray = {" 0 ", " 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ",
            " 8 ", " 9 ", "10 ", "11 ", "12 ", "13 ", "14 ", "15 ",
            "16 ", "17 ", "18 ", "19 ", "20 ", "21 ", "22 ", "23 ",
            "24 ", "25 ", "26 ", "27 ", "28 ", "29 ", "30 ", "31 "};
    String[] pullArray = {"NoPull", "PullDw", "PullUp"};
    String[] driveArray = {"S0S1", "H0S1", "S0H1", "H0H1", "D0S1", "D0H1", "S0D1", "H0D1"};
    String[] defaultArray = {"Low", "High"};
    int currentDoutPin, currentPullValue, currentDriveValue, currentDefaultValue;

    Spinner spinner_dout, spinner_pull, spinner_drive, spinner_default;
    boolean isLocked;
    Switch enableSwitch;
    Button doutSave;
    ImageView doutLock;

    private boolean isSpinnerInitial = true;
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
        Intent intent = new Intent(DoutSettingActivity.this, ShsService.class);
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
                Intent intent = new Intent(DoutSettingActivity.this, ShsActivity.class);
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
        setContentView(R.layout.activity_dout_setting);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isLocked = preferences.getBoolean("deviceLocked", true);
        enableSwitch = (Switch) findViewById(R.id.dout_switch);

        doutSave = (Button) findViewById(R.id.dout_bt_save);
        doutLock = (ImageView) findViewById(R.id.dout_lock_icon);

        spinner_dout = (Spinner) findViewById(R.id.dout_spinner_pin);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dout_spinner_pin, R.id.dout_spinner_pin_text, doutArray);
        spinner_dout.setAdapter(adapter);

        currentDoutPin = preferences.getInt("outputPinSpinner", 0);
        spinner_dout.setSelection(currentDoutPin);



        spinner_pull = (Spinner) findViewById(R.id.dout_spinner_pull);
        ArrayAdapter<String>  pull_adapter =  new ArrayAdapter<>(this, R.layout.dout_spinner_pull, R.id.dout_spinner_pull_text, pullArray);
        spinner_pull.setAdapter(pull_adapter);

        spinner_drive = (Spinner) findViewById(R.id.dout_spinner_drive);
        ArrayAdapter<String> drive_adapter = new ArrayAdapter<>(this, R.layout.dout_spinner_drive, R.id.dout_spinner_drive_text, driveArray);
        spinner_drive.setAdapter(drive_adapter);

        currentPullValue = InterfaceConfigAndStatus.getInstance().getDoutConfig()[currentDoutPin].getPullValue();
        currentDriveValue = InterfaceConfigAndStatus.getInstance().getDoutConfig()[currentDoutPin].getDriveValue();

        spinner_default = (Spinner) findViewById(R.id.dout_spinner_default);
        ArrayAdapter<String> default_adapter = new ArrayAdapter<>(this, R.layout.dout_spinner_default, R.id.dout_spinner_default_text, defaultArray);
        spinner_default.setAdapter(default_adapter);
        currentDefaultValue = InterfaceConfigAndStatus.getInstance().getDoutConfig()[currentDoutPin].getDefaultValue();

        if (InterfaceConfigAndStatus.getInstance().getDoutConfig()[currentDoutPin].isEnabled()) {
            enableSwitch.setChecked(true);
            spinner_pull.setSelection(currentPullValue);
            spinner_drive.setSelection(currentDriveValue);
            spinner_default.setSelection(currentDefaultValue);
        } else {
            enableSwitch.setChecked(false);
            spinner_pull.setSelection(0);
            spinner_drive.setSelection(0);
            spinner_default.setSelection(0);
        }

        spinner_dout.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSpinnerInitial) {
                    isSpinnerInitial = false;
                } else {
                    currentDoutPin = position;
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putInt("outputPinSpinner", currentDoutPin);
                    edit.apply();
                    Log.i(TAG, "currentOutputPinRow: " + currentDoutPin);

                    if (InterfaceConfigAndStatus.getInstance().getDoutConfig()[currentDoutPin].isEnabled()) {
                        enableSwitch.setChecked(true);

                        int pullValue = InterfaceConfigAndStatus.getInstance().getDoutConfig()[currentDoutPin].getPullValue();
                        int driveValue = InterfaceConfigAndStatus.getInstance().getDoutConfig()[currentDoutPin].getDriveValue();
                        int defaultValue = InterfaceConfigAndStatus.getInstance().getDoutConfig()[currentDoutPin].getDefaultValue();
                        spinner_pull.setSelection(pullValue);
                        spinner_drive.setSelection(driveValue);
                        spinner_default.setSelection(defaultValue);
                    } else {
                        enableSwitch.setChecked(false);
                        spinner_pull.setSelection(0);
                        spinner_drive.setSelection(0);
                        spinner_default.setSelection(0);
                    }

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        spinner_pull.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isSpinnerInitial) {
                    currentPullValue = position;
                    if (enableSwitch.isChecked()) {
                        spinner_pull.setSelection(currentPullValue);
                    } else {
                        spinner_pull.setSelection(0);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        spinner_drive.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isSpinnerInitial) {
                    currentDriveValue = position;
                    if (enableSwitch.isChecked()) {
                        spinner_drive.setSelection(currentDriveValue);
                    } else {
                        spinner_drive.setSelection(0);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_default.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isSpinnerInitial) {
                    currentDefaultValue = position;
                    if (enableSwitch.isChecked()) {
                        spinner_default.setSelection(currentDefaultValue);
                    } else {
                        spinner_default.setSelection(0);
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
                    Log.i(TAG, "Output pin switch is on.");
                } else {
                    Log.i(TAG, "Output pin switch is off");
                 }
            }
        });

        isLocked =preferences.getBoolean("deviceLocked", true);
        if (isLocked) {
            doutLock.setImageResource(R.drawable.ticked);
        } else {
            doutLock.setImageResource(R.drawable.unticked);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void clickSaveOutputPinButton(View view) {
        if (InterfaceConfigAndStatus.getInstance().getDoutConfig()[currentDoutPin].isEnabled()) {
            if (enableSwitch.isChecked()) {
                int pullValue = InterfaceConfigAndStatus.getInstance().getDoutConfig()[currentDoutPin].getPullValue();
                int driveValue = InterfaceConfigAndStatus.getInstance().getDoutConfig()[currentDoutPin].getDriveValue();
                int defaultValue = InterfaceConfigAndStatus.getInstance().getDoutConfig()[currentDoutPin].getDefaultValue();
                 if ((pullValue == currentPullValue) && (driveValue == currentDriveValue) && (defaultValue == currentDefaultValue)) {
                    new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(R.string.config_unchanged)
                            .setPositiveButton(R.string.ok, null).show();
                } else {
                    //update pin configuration
                    byte[] value = {ShsService.MANAGE_ID_INTERFACE_ADD,(byte)(ShsService.FUNCTION_ID_DOUT_0+currentDoutPin),
                                    (byte) (currentPullValue | (currentDriveValue << 2)), (byte) currentDefaultValue};
                    sendOutputPinSettingBroadcast(value);
                }
            } else {
                //delete pin configuration
                Log.i(TAG, "delted pin configuration");
                byte[] value = {ShsService.MANAGE_ID_INTERFACE_DELETE,(byte)(ShsService.FUNCTION_ID_DOUT_0+currentDoutPin)};
                spinner_pull.setSelection(0);
                spinner_drive.setSelection(0);
                spinner_default.setSelection(0);
                sendOutputPinSettingBroadcast(value);
            }
        } else {
            if (enableSwitch.isChecked()) {
                Log.i(TAG, "add new pin");
                //add new pin configuration
                byte[] value = {ShsService.MANAGE_ID_INTERFACE_ADD,(byte)(ShsService.FUNCTION_ID_DOUT_0+currentDoutPin),
                                (byte) (currentPullValue | (currentDriveValue << 2)), (byte) currentDefaultValue};
                sendOutputPinSettingBroadcast(value);
            } else {
                Log.i(TAG, "not configured");
                new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(R.string.none_configured)
                        .setPositiveButton(R.string.ok, null).show();
            }
        }
    }

    public void clickLockAndSaveOutputPinButton(View view) {
        isLocked = !isLocked;
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("deviceLocked", isLocked);
        edit.apply();
        if (isLocked) {
            Log.i(TAG, "After clicking save,lock device configuration");
            doutLock.setImageResource(R.drawable.ticked);
        } else {
            Log.i(TAG, "After clicking save,do not lock device configuration");
            doutLock.setImageResource(R.drawable.unticked);
        }
    }

    private void sendOutputPinSettingBroadcast(byte[] value) {
        Intent intent = new Intent(ShsService.BROADCAST_CONFIG_UPDATE);
        intent.putExtra(ShsService.BROADCAST_CONFIG_DATA, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void notifyMessage(String message) {
        new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(message)
                .setPositiveButton(R.string.ok, null).show();
    }





}
