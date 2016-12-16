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
import android.widget.ImageButton;

import com.madsglobal.android.smarthw.InterfaceType.InterfaceConfigAndStatus;
import com.madsglobal.android.smarthw.R;
import com.madsglobal.android.smarthw.ShsActivity;
import com.madsglobal.android.smarthw.ShsService;
import com.madsglobal.android.smarthw.common.SHSLifeCycleHandler;
import com.madsglobal.android.smarthw.common.Utility;
import com.madsglobal.android.smarthw.error.GattError;
import com.madsglobal.android.smarthw.setting.DinSettingActivity;

public class DinActivity extends AppCompatActivity {
    String TAG = "DinActivity";
    ImageButton[] dinPins = new ImageButton[32];
    AlertDialog alertDialog;

    private final BroadcastReceiver dInBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ShsService.BROADCAST_DIN_STATUS_UPDATE)) {
                int pinIndex = intent.getIntExtra(ShsService.BROADCAST_PINNUMBER, 0);
                setButtonImage(pinIndex);
            }else if(ShsService.BROADCAST_ERROR.equals(action)) {
                final int error = intent.getIntExtra(ShsService.EXTRA_ERROR_DATA, 0);
                showErrorMessage(error);
            }
        }
    };

    private void showErrorMessage(final int error) {
        Intent intent = new Intent(DinActivity.this, ShsService.class);
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
                Intent intent = new Intent(DinActivity.this, ShsActivity.class);
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
        setContentView(R.layout.activity_din);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dinPins[0] = (ImageButton) findViewById(R.id.din_bt_0);
        dinPins[1] = (ImageButton) findViewById(R.id.din_bt_1);
        dinPins[2] = (ImageButton) findViewById(R.id.din_bt_2);
        dinPins[3] = (ImageButton) findViewById(R.id.din_bt_3);
        dinPins[4] = (ImageButton) findViewById(R.id.din_bt_4);
        dinPins[5] = (ImageButton) findViewById(R.id.din_bt_5);
        dinPins[6] = (ImageButton) findViewById(R.id.din_bt_6);
        dinPins[7] = (ImageButton) findViewById(R.id.din_bt_7);
        dinPins[8] = (ImageButton) findViewById(R.id.din_bt_8);
        dinPins[9] = (ImageButton) findViewById(R.id.din_bt_9);
        dinPins[10] = (ImageButton) findViewById(R.id.din_bt_10);
        dinPins[11] = (ImageButton) findViewById(R.id.din_bt_11);
        dinPins[12] = (ImageButton) findViewById(R.id.din_bt_12);
        dinPins[13] = (ImageButton) findViewById(R.id.din_bt_13);
        dinPins[14] = (ImageButton) findViewById(R.id.din_bt_14);
        dinPins[15] = (ImageButton) findViewById(R.id.din_bt_15);
        dinPins[16] = (ImageButton) findViewById(R.id.din_bt_16);
        dinPins[17] = (ImageButton) findViewById(R.id.din_bt_17);
        dinPins[18] = (ImageButton) findViewById(R.id.din_bt_18);
        dinPins[19] = (ImageButton) findViewById(R.id.din_bt_19);
        dinPins[20] = (ImageButton) findViewById(R.id.din_bt_20);
        dinPins[21] = (ImageButton) findViewById(R.id.din_bt_21);
        dinPins[22] = (ImageButton) findViewById(R.id.din_bt_22);
        dinPins[23] = (ImageButton) findViewById(R.id.din_bt_23);
        dinPins[24] = (ImageButton) findViewById(R.id.din_bt_24);
        dinPins[25] = (ImageButton) findViewById(R.id.din_bt_25);
        dinPins[26] = (ImageButton) findViewById(R.id.din_bt_26);
        dinPins[27] = (ImageButton) findViewById(R.id.din_bt_27);
        dinPins[28] = (ImageButton) findViewById(R.id.din_bt_28);
        dinPins[29] = (ImageButton) findViewById(R.id.din_bt_29);
        dinPins[30] = (ImageButton) findViewById(R.id.din_bt_30);
        dinPins[31] = (ImageButton) findViewById(R.id.din_bt_31);
        for (int i = 0; i < 32; i++) {
            setButtonImage(i);
        }
    }

    private void setButtonImage(int index) {
        if (InterfaceConfigAndStatus.getInstance().getDinConfig()[index].isEnabled()) {
            if (InterfaceConfigAndStatus.getInstance().getDinCurrentStatus()[index] == 1) {
                Log.i(TAG,"index: "+index);
                dinPins[index].setImageResource(R.drawable.checkbox_high);
            } else if (InterfaceConfigAndStatus.getInstance().getDinCurrentStatus()[index] == 0) {
                dinPins[index].setImageResource(R.drawable.checkbox_low);
            }
        } else {
            dinPins[index].setImageResource(R.drawable.uncheckbox_disable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (int i = 0; i < 32; i++) {
            setButtonImage(i);
        }
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ShsService.BROADCAST_DIN_STATUS_UPDATE);
        intentFilter.addAction(ShsService.BROADCAST_ERROR);
        broadcastManager.registerReceiver(dInBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(dInBroadcastReceiver);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_din_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() ==R.id.din_setting){
            Intent intent = new Intent(this, DinSettingActivity.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }
}
