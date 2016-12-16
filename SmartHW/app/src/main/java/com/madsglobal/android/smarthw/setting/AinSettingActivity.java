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

public class AinSettingActivity extends AppCompatActivity {
    public final String TAG = "AnalogInputPinSetting";

    String[] aniArray = {"  1", "  2", "  3", "  4", "  5", "  6"};
    String[] rangeArray = {"VDD", "3.6V", "1.2V"};
    String[] rateArray = {"Disabled", "100ms", "1s", "10s"};
    int currentAniPin, currentRangeValue, currentRateValue;
    Spinner spinner_ani, spinner_range, spinner_rate;
    boolean isLocked;
    Switch enableSwitch;
    Button aniSave;
    ImageView ainLock;
    private boolean isAniSpinnerInitial = true;
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
        Intent intent = new Intent(AinSettingActivity.this, ShsService.class);
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
                Intent intent = new Intent(AinSettingActivity.this, ShsActivity.class);
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
        setContentView(R.layout.activity_ain_setting);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isLocked = preferences.getBoolean("deviceLocked", true);

        enableSwitch = (Switch) findViewById(R.id.ani_switch);
        aniSave = (Button) findViewById(R.id.ani_bt_save);
        ainLock = (ImageView) findViewById(R.id.ani_lock_icon);

        spinner_ani = (Spinner) findViewById(R.id.ani_spinner_pin);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.ani_spinner_pin, R.id.ani_spinner_pin_text, aniArray);
        spinner_ani.setAdapter(adapter);

        currentAniPin = preferences.getInt("analogPinSpinner", 0);
        spinner_ani.setSelection(currentAniPin);

        spinner_range = (Spinner) findViewById(R.id.ani_spinner_range);
        ArrayAdapter<String> range_adapter = new ArrayAdapter<>(this, R.layout.ani_spinner_range, R.id.ani_spinner_range_text, rangeArray);
        spinner_range.setAdapter(range_adapter);

        spinner_rate = (Spinner) findViewById(R.id.ani_spinner_rate);
        ArrayAdapter<String> rate_adapter = new ArrayAdapter<>(this, R.layout.ani_spinner_rate, R.id.ani_spinner_rate_text, rateArray);
        spinner_rate.setAdapter(rate_adapter);

        currentRangeValue = InterfaceConfigAndStatus.getInstance().getAinConfig()[currentAniPin].getRangeValue();
        currentRateValue = InterfaceConfigAndStatus.getInstance().getAinConfig()[currentAniPin].getRateVale();

        if (InterfaceConfigAndStatus.getInstance().getAinConfig()[currentAniPin].isEnabled()) {
            Log.i(TAG, "digital analog pin " + "currentPinRow" + " enabled");

            enableSwitch.setChecked(true);
            spinner_range.setSelection(currentRangeValue);
            spinner_rate.setSelection(currentRateValue);
        } else {
            Log.i(TAG, "digital analog pin " + "currentPinRow" + " disabled");
            enableSwitch.setChecked(false);
            spinner_range.setSelection(0);
            spinner_rate.setSelection(0);
        }


        spinner_ani.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isAniSpinnerInitial) {
                    isAniSpinnerInitial = false;
                } else {
                    currentAniPin = position;
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putInt("analogPinSpinner", currentAniPin);
                    edit.apply();
                    Log.i(TAG, "analogPinSpinner: " + currentAniPin);

                    if (InterfaceConfigAndStatus.getInstance().getAinConfig()[currentAniPin].isEnabled()) {
                        enableSwitch.setChecked(true);

                        int rangeValue = InterfaceConfigAndStatus.getInstance().getAinConfig()[currentAniPin].getRangeValue();
                        int rateValue = InterfaceConfigAndStatus.getInstance().getAinConfig()[currentAniPin].getRateVale();
                        spinner_range.setSelection(rangeValue);
                        spinner_rate.setSelection(rateValue);
                    } else {
                        enableSwitch.setChecked(false);
                        spinner_range.setSelection(0);
                        spinner_rate.setSelection(0);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        spinner_range.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isAniSpinnerInitial) {
                    currentRangeValue = position;
                    if (enableSwitch.isChecked()) {
                        spinner_range.setSelection(currentRangeValue);
                    } else {
                        spinner_range.setSelection(0);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        spinner_rate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isAniSpinnerInitial) {
                    currentRateValue = position;
                    if (enableSwitch.isChecked()) {
                        spinner_rate.setSelection(currentRateValue);
                    } else {
                        spinner_rate.setSelection(0);
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
                    Log.i(TAG, "Analog pin switch is on.");
                } else {
                    Log.i(TAG, "Analog pin switch is off");
                }
            }
        });
        isLocked =preferences.getBoolean("deviceLocked", true);
        if (isLocked) {
            ainLock.setImageResource(R.drawable.ticked);
        } else {
            ainLock.setImageResource(R.drawable.unticked);
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

    public void clickSaveAnalogPinButton(View view) {
        Log.i(TAG, "clickSaveAnalogPinButton");
        if (InterfaceConfigAndStatus.getInstance().getAinConfig()[currentAniPin].isEnabled()) {
            if (enableSwitch.isChecked()) {

                int rangeValue = InterfaceConfigAndStatus.getInstance().getAinConfig()[currentAniPin].getRangeValue();
                int rateValue = InterfaceConfigAndStatus.getInstance().getAinConfig()[currentAniPin].getRateVale();
                Log.i(TAG, "range: " + rangeValue + "rate " + rateValue);
                Log.i(TAG, "currentRate: " + currentRangeValue + " currentRate: " + currentRateValue);
                if ((rangeValue == currentRangeValue) && (rateValue == currentRateValue)) {
                    new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(R.string.config_unchanged)
                            .setPositiveButton(R.string.ok, null).show();
                } else {
                    //update pin configuration
                    Log.i(TAG,"update analog pin configuration");
                    byte[] value = {ShsService.MANAGE_ID_INTERFACE_ADD,(byte)(ShsService.FUNCTION_ID_AIN_0+currentAniPin),
                                    (byte) (currentRangeValue | (currentRateValue << 2))};
                    sendAnalogPinSettingBroadcast(value);
                }
            } else {
                //delete pin configuration
                Log.i(TAG, "delete analog pin configuration");
                byte[] value = {ShsService.MANAGE_ID_INTERFACE_DELETE,(byte)(ShsService.FUNCTION_ID_AIN_0+currentAniPin)};
                spinner_range.setSelection(0);
                spinner_rate.setSelection(0);
                sendAnalogPinSettingBroadcast(value);
            }
        } else {
            if (enableSwitch.isChecked()) {
                Log.i(TAG, "add new analog pin");
                //add new pin configuration
                byte[] value = {ShsService.MANAGE_ID_INTERFACE_ADD,(byte)(ShsService.FUNCTION_ID_AIN_0+currentAniPin),
                                (byte) (currentRangeValue | (currentRateValue << 2))};
                sendAnalogPinSettingBroadcast(value);
            } else {
                Log.i(TAG, "not configured");
                new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(R.string.none_configured)
                        .setPositiveButton(R.string.ok, null).show();
            }
        }

    }

    public void clickLockAndSaveAnalogPinButton(View view) {
        isLocked = !isLocked;
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("deviceLocked", isLocked);
        edit.apply();
        if (isLocked) {
            Log.i(TAG, "After clicking save,lock device configuration");
            ainLock.setImageResource(R.drawable.ticked);
        } else {
            Log.i(TAG, "After clicking save,do not lock device configuration");
            ainLock.setImageResource(R.drawable.unticked);
        }

    }

    private void sendAnalogPinSettingBroadcast(byte[] value) {
        Intent intent = new Intent(ShsService.BROADCAST_CONFIG_UPDATE);
        intent.putExtra(ShsService.BROADCAST_CONFIG_DATA, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void notifyMessage(String message) {
        new AlertDialog.Builder(this).setTitle(R.string.config_error_title).setMessage(message)
                .setPositiveButton(R.string.ok, null).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
