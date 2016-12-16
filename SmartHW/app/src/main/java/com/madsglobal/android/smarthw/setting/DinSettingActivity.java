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
import android.widget.Spinner;
import android.widget.Switch;

import com.madsglobal.android.smarthw.InterfaceType.InterfaceConfigAndStatus;
import com.madsglobal.android.smarthw.R;
import com.madsglobal.android.smarthw.ShsActivity;
import com.madsglobal.android.smarthw.ShsService;
import com.madsglobal.android.smarthw.common.SHSLifeCycleHandler;
import com.madsglobal.android.smarthw.common.Utility;
import com.madsglobal.android.smarthw.error.GattError;

public class DinSettingActivity extends AppCompatActivity {
    public final String TAG = "DigitalInputPinSetting";

    String[] dinArray = {"  0  ", "  1  ", "  2  ", "  3  ", "  4  ", "  5  ", "  6  ", "  7  ",
            "  8  ", "  9  ", " 10  ", " 11  ", " 12  ", " 13  ", " 14  ", " 15  ",
            " 16  ", " 17  ", " 18  ", " 19  ", " 20  ", " 21  ", " 22  ", " 23  ",
            " 24  ", " 25  ", " 26  ", " 27  ", " 28  ", " 29  ", " 30 ", " 31 "};

    String[] pullArray = {"NoPull", "PullDw", "PullUp"};
    int currentDinPin, currentPullValue;
    Spinner spinner_din, spinner_pull;
    boolean isLocked;
    Switch enableSwitch;
    Button dinSave;
    ImageView dinLock;

    private boolean isDinSpinnerInitial = true;
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
        Intent intent = new Intent(DinSettingActivity.this, ShsService.class);
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
                Intent intent = new Intent(DinSettingActivity.this, ShsActivity.class);
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
        setContentView(R.layout.activity_din_setting);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        enableSwitch = (Switch) findViewById(R.id.din_switch);
        dinSave = (Button) findViewById(R.id.din_bt_save);
        dinLock = (ImageView) findViewById(R.id.din_lock_icon);

        spinner_din = (Spinner) findViewById(R.id.din_spinner_pin);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.din_spinner_pin, R.id.din_spinner_pin_text, dinArray);
        spinner_din.setAdapter(adapter);

        currentDinPin = preferences.getInt("inputPinSpinner", 0);
        spinner_din.setSelection(currentDinPin);

        spinner_pull = (Spinner) findViewById(R.id.din_spinner_pull);
        ArrayAdapter<String> pull_adapter = new ArrayAdapter<>(this, R.layout.din_spinner_pull, R.id.din_spinner_pull_text, pullArray);
        spinner_pull.setAdapter(pull_adapter);

        currentPullValue = InterfaceConfigAndStatus.getInstance().getDinConfig()[currentDinPin].getPullValue();

        if (InterfaceConfigAndStatus.getInstance().getDinConfig()[currentDinPin].isEnabled()) {
            enableSwitch.setChecked(true);
            spinner_pull.setSelection(currentPullValue);

        } else {
            enableSwitch.setChecked(false);
            spinner_pull.setSelection(0);
        }

        spinner_din.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isDinSpinnerInitial) {
                    isDinSpinnerInitial = false;
                } else {
                    currentDinPin = position;
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putInt("inputPinSpinner", currentDinPin);
                    edit.apply();
                    Log.i(TAG, "currentInputPinRow: " + currentDinPin);

                    if (InterfaceConfigAndStatus.getInstance().getDinConfig()[currentDinPin].isEnabled()) {
                        enableSwitch.setChecked(true);
                        int pullIndex = InterfaceConfigAndStatus.getInstance().getDinConfig()[currentDinPin].getPullValue();
                        spinner_pull.setSelection(pullIndex);
                    } else {
                        enableSwitch.setChecked(false);
                        spinner_pull.setSelection(0);
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
                if (!isDinSpinnerInitial) {
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

        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i(TAG, "Input pin switch is on.");
                    enableSwitch.setChecked(true);
                } else {
                    Log.i(TAG, "Input pin switch is off");
                    enableSwitch.setChecked(false);
                }
            }
        });

        isLocked =preferences.getBoolean("deviceLocked", true);
        if (isLocked) {
            dinLock.setImageResource(R.drawable.ticked);
        } else {
            dinLock.setImageResource(R.drawable.unticked);
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

    public void clickSaveInputPinButton(View view) {
        if (InterfaceConfigAndStatus.getInstance().getDinConfig()[currentDinPin].isEnabled()) {
            if (enableSwitch.isChecked()) {
                int pullValue = (int) (InterfaceConfigAndStatus.getInstance().getDinConfig()[currentDinPin].getPullValue());
                if (pullValue == currentPullValue) {
                    new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(R.string.config_unchanged)
                            .setPositiveButton(R.string.ok, null).show();
                } else {
                    //update pin configuration
                    byte[] value = {ShsService.MANAGE_ID_INTERFACE_ADD,(byte)(ShsService.FUNCTION_ID_DIN_0+currentDinPin),(byte) currentPullValue};
                    sendInputPinSettingBroadcast(value);
                }
            } else {
                //delete pin configuration
                Log.i(TAG, "delete pin configuration");
                byte[] value = {ShsService.MANAGE_ID_INTERFACE_DELETE, (byte)(ShsService.FUNCTION_ID_DIN_0+currentDinPin)};
                spinner_pull.setSelection(0);
                sendInputPinSettingBroadcast(value);
            }
        } else {
            if (enableSwitch.isChecked()) {
                Log.i(TAG, "add new pin");
                //add new pin configuration
                byte[] value = { ShsService.MANAGE_ID_INTERFACE_ADD,(byte)(ShsService.FUNCTION_ID_DIN_0+currentDinPin),(byte) currentPullValue};
                sendInputPinSettingBroadcast(value);
            } else {
                Log.i(TAG, "not configured");
                new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(R.string.none_configured)
                        .setPositiveButton(R.string.ok, null).show();
            }
        }
    }

    public void clickLockAndSaveInputPinButton(View view) {
        isLocked = !isLocked;
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("deviceLocked", isLocked);
        edit.apply();
        if (isLocked) {
            Log.i(TAG, "After clicking save,lock device configuration");
            dinLock.setImageResource(R.drawable.ticked);
        } else {
            Log.i(TAG, "After clicking save,do not lock device configuration");
            dinLock.setImageResource(R.drawable.unticked);
        }
    }

    private void sendInputPinSettingBroadcast(byte[] value) {
        Intent intent = new Intent(ShsService.BROADCAST_CONFIG_UPDATE);
        intent.putExtra(ShsService.BROADCAST_CONFIG_DATA, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void notifyMessage(String message) {
        new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(message)
                .setPositiveButton(R.string.ok, null).show();
    }


}
