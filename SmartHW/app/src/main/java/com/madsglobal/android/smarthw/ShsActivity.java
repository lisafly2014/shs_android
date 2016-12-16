package com.madsglobal.android.smarthw;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.madsglobal.android.smarthw.common.Utility;
import com.madsglobal.android.smarthw.error.GattError;
import com.madsglobal.android.smarthw.scanner.ScannerFragment;
import com.madsglobal.android.smarthw.view.InterfaceTypeActivity;
import com.madsglobal.android.smarthw.common.SHSLifeCycleHandler;

public class ShsActivity extends AppCompatActivity implements ScannerFragment.OnDeviceSelectedListener {
    private static final String TAG = "ShsActivity";
    private final static long CONFIGURATION_DURATION = 30000;

    private static final int ENABLE_BT_REQ = 0;
    private BluetoothDevice mSelectedDevice;

    private TextView mHardwareName;
    private Button mFindHardware;
    private Button mShowInterface;
    private Button mDisconnectHardware;

    private static String mDeviceName;
    private final Handler mHandler = new Handler();

    private final BroadcastReceiver updateShsBroadcastReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (ShsService.BROADCAST_SHS_CONNECTION_STATUS.equals(action)) {
                mHandler.removeCallbacks(mRunnable);
                final boolean isConnected = intent.getBooleanExtra(ShsService.EXTRA_SHS_CONNECTION_STATUS, false);
                Utility.setConnected(isConnected);
                if(Utility.isConnected()){
                    configUI();
                }
            } else if (ShsService.BROADCAST_ERROR.equals(action)) {
                final int error = intent.getIntExtra(ShsService.EXTRA_ERROR_DATA, 0);
                showErrorMessage(error);
            }
        }

    };

    private void showErrorMessage(final int error) {
        Utility.stopService(this);
        String message = "" + GattError.parse(error);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.remote_exception_disconnect).setMessage(message);
        alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                clearUI();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    private IntentFilter shsIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ShsService.BROADCAST_ERROR);
        intentFilter.addAction(ShsService.BROADCAST_SHS_CONNECTION_STATUS);

        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shs);
        mHardwareName = (TextView) findViewById(R.id.txt_hardwareName);
        mFindHardware = (Button) findViewById(R.id.btn_findHardware);
        mShowInterface = (Button) findViewById(R.id.btn_showInterface);
        mDisconnectHardware = (Button) findViewById(R.id.btn_disconnectHW);

        getApplication().registerActivityLifecycleCallbacks(new SHSLifeCycleHandler());
        isBLESupported();
        if (!isBLEEnabled()) {
            showBLEDialog();
        }
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.getBoolean("deviceLocked", true);
    }

    private void clearUI() {
        mHardwareName.setText("");

        mFindHardware.setText(R.string.find_hardware);
        mFindHardware.setEnabled(true);
        mFindHardware.setVisibility(View.VISIBLE);

        mShowInterface.setEnabled(false);
        mShowInterface.setVisibility(View.INVISIBLE);

        mDisconnectHardware.setEnabled(false);
        mDisconnectHardware.setVisibility(View.INVISIBLE);
    }

    private void configUI() {
        mHardwareName.setText(mDeviceName != null ? mDeviceName : getString(R.string.not_available));

        mFindHardware.setEnabled(false);
        mFindHardware.setVisibility(View.INVISIBLE);

        mShowInterface.setEnabled(true);
        mShowInterface.setVisibility(View.VISIBLE);

        mDisconnectHardware.setEnabled(true);
        mDisconnectHardware.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(updateShsBroadcastReciever);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Utility.isConnected()){
            configUI();
        }else{
            clearUI();
        }
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(updateShsBroadcastReciever, shsIntentFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!SHSLifeCycleHandler.isApplicationInForeground()) {
            resetApplication();
            Utility.showNotification(this,getString(R.string.shs_disconnected));
        }
    }

    /*
    *Click show interface button to show interface type
     */
    public void chooseInterfaceType(View view) {
        Log.i(TAG,"connection status = "+Utility.isConnected());
        if(Utility.isConnected()){
            Intent intent = new Intent(ShsActivity.this, InterfaceTypeActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Callback of FindHardware button on ShsActivity
     */
    public void onFindHwClicked(final View view) {
        if (isBLEEnabled()) {
            showDeviceScanningDialog();
        } else {
            showBLEDialog();
        }
    }

    /**
     * Click Diconnect button to disconnect with the pheripheral
     * @param view
     */
    public void onDisconnectClicked(View view)
    {
        if(Utility.isConnected()){
            resetApplication();
        }else{
            clearUI();
        }
    }

    void resetApplication() {
        Utility.stopService(this);
        clearUI();
    }

    @Override
    public void onDeviceSelected(final BluetoothDevice device, final String name) {
        mSelectedDevice = device;
        Log.i(TAG, "hardware name " + name);
        mDeviceName = name;
        mFindHardware.setText(R.string.hardware_connecting);
        mFindHardware.setEnabled(false);

        mHandler.postDelayed(mRunnable, CONFIGURATION_DURATION);
        Intent service = new Intent(this, ShsService.class);
        service.putExtra(ShsService.EXTRA_HARDWARE_ADDRESS, mSelectedDevice.getAddress());
        service.putExtra(ShsService.EXTRA_HARDWARE_NAME, name);
        startService(service);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            onFire();
        }
    };
    /**
     * Fire on timeout
     */
    private void onFire(){
        resetApplication();
        Utility.showNotification(getApplicationContext(),getString(R.string.shs_time_out));
    }
    @Override
    public void onDialogCanceled() {
        // do nothing
    }


    private void isBLESupported() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast(R.string.no_ble);
            finish();
        }
    }

    private boolean isBLEEnabled() {
        final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = manager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    private void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, ENABLE_BT_REQ);
    }


    private void showDeviceScanningDialog() {
        final ScannerFragment dialog = ScannerFragment.getInstance(null); // Device that is advertising directly does not have the GENERAL_DISCOVERABLE nor LIMITED_DISCOVERABLE flag set.
        dialog.show(getSupportFragmentManager(), "scan_fragment");
    }

    private void showToast(final int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }

    private void showToast(final String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
