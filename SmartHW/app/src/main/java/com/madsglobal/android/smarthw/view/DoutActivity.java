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
import android.view.View;
import android.widget.ImageButton;

import com.madsglobal.android.smarthw.InterfaceType.InterfaceConfigAndStatus;
import com.madsglobal.android.smarthw.R;
import com.madsglobal.android.smarthw.ShsActivity;
import com.madsglobal.android.smarthw.ShsService;
import com.madsglobal.android.smarthw.common.SHSLifeCycleHandler;
import com.madsglobal.android.smarthw.common.Utility;
import com.madsglobal.android.smarthw.error.GattError;
import com.madsglobal.android.smarthw.setting.DoutSettingActivity;

import static com.madsglobal.android.smarthw.ShsService.FUNCTION_ID_DOUT_0;

public class DoutActivity extends AppCompatActivity {
    String TAG="DoutActivity";
    ImageButton[] doutPins=new ImageButton[32];
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
        Intent intent = new Intent(DoutActivity.this, ShsService.class);
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
                Intent intent = new Intent(DoutActivity.this, ShsActivity.class);
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
        setContentView(R.layout.activity_dout);
        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        doutPins[0] =(ImageButton)findViewById(R.id.dout_bt_0);
        doutPins[1] =(ImageButton)findViewById(R.id.dout_bt_1);
        doutPins[2] =(ImageButton)findViewById(R.id.dout_bt_2);
        doutPins[3] =(ImageButton)findViewById(R.id.dout_bt_3);
        doutPins[4] =(ImageButton)findViewById(R.id.dout_bt_4);
        doutPins[5] =(ImageButton)findViewById(R.id.dout_bt_5);
        doutPins[6] =(ImageButton)findViewById(R.id.dout_bt_6);
        doutPins[7] =(ImageButton)findViewById(R.id.dout_bt_7);
        doutPins[8] =(ImageButton)findViewById(R.id.dout_bt_8);
        doutPins[9] =(ImageButton)findViewById(R.id.dout_bt_9);
        doutPins[10] =(ImageButton)findViewById(R.id.dout_bt_10);
        doutPins[11] =(ImageButton)findViewById(R.id.dout_bt_11);
        doutPins[12] =(ImageButton)findViewById(R.id.dout_bt_12);
        doutPins[13] =(ImageButton)findViewById(R.id.dout_bt_13);
        doutPins[14] =(ImageButton)findViewById(R.id.dout_bt_14);
        doutPins[15] =(ImageButton)findViewById(R.id.dout_bt_15);
        doutPins[16] =(ImageButton)findViewById(R.id.dout_bt_16);
        doutPins[17] =(ImageButton)findViewById(R.id.dout_bt_17);
        doutPins[18] =(ImageButton)findViewById(R.id.dout_bt_18);
        doutPins[19] =(ImageButton)findViewById(R.id.dout_bt_19);
        doutPins[20] =(ImageButton)findViewById(R.id.dout_bt_20);
        doutPins[21] =(ImageButton)findViewById(R.id.dout_bt_21);
        doutPins[22] =(ImageButton)findViewById(R.id.dout_bt_22);
        doutPins[23] =(ImageButton)findViewById(R.id.dout_bt_23);
        doutPins[24] =(ImageButton)findViewById(R.id.dout_bt_24);
        doutPins[25] =(ImageButton)findViewById(R.id.dout_bt_25);
        doutPins[26] =(ImageButton)findViewById(R.id.dout_bt_26);
        doutPins[27] =(ImageButton)findViewById(R.id.dout_bt_27);
        doutPins[28] =(ImageButton)findViewById(R.id.dout_bt_28);
        doutPins[29] =(ImageButton)findViewById(R.id.dout_bt_29);
        doutPins[30] =(ImageButton)findViewById(R.id.dout_bt_30);
        doutPins[31] =(ImageButton)findViewById(R.id.dout_bt_31);
        for(int i=0;i<32;i++){
            if(doutPins[i] !=null){
                doutPins[i].setTag(i);
                setButtonImage(i);
            }
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
        for(int i=0;i<32;i++){
            setButtonImage(i);
        }
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ShsService.BROADCAST_ERROR);
        broadcastManager.registerReceiver(errorBroadcastReciever,intentFilter);
    }

    private void setButtonImage(int index){
        if(InterfaceConfigAndStatus.getInstance().getDoutConfig()[index].isEnabled()){

            if(InterfaceConfigAndStatus.getInstance().getDoutCurrentStatus()[index] ==1){
                doutPins[index].setImageResource(R.drawable.checkbox_high);
            }else if(InterfaceConfigAndStatus.getInstance().getDoutCurrentStatus()[index] ==0){
                doutPins[index].setImageResource(R.drawable.checkbox_low);
            }
        }else{
            doutPins[index].setImageResource(R.drawable.uncheckbox_disable);
        }

    }
    @Override
    protected void onStop() {
        super.onStop();
        if (!SHSLifeCycleHandler.isApplicationInForeground()) {
            Utility.stopService(this);
            Utility.showNotification(this,getString(R.string.shs_disconnected));
        }
    }

    public void onClick(View v){
        ImageButton ib =(ImageButton)v;

        int index =((int)ib.getTag());
        byte functionID = (byte)(index + FUNCTION_ID_DOUT_0);
        Intent intent =new Intent();
        intent.setAction(ShsService.BROADCAST_INTERFACE_STATUS_CHANGE);
        if(InterfaceConfigAndStatus.getInstance().getDoutConfig()[index].isEnabled()){
            if(InterfaceConfigAndStatus.getInstance().getDoutCurrentStatus()[index]==1){
                doutPins[index].setImageResource(R.drawable.checkbox_low);
                InterfaceConfigAndStatus.getInstance().setDoutPinStatus(index, (byte)0);
                intent.putExtra(ShsService.BROADCAST_FUNCTIONID,functionID);
            }else if(InterfaceConfigAndStatus.getInstance().getDoutCurrentStatus()[index]==0){
                doutPins[index].setImageResource(R.drawable.checkbox_high);
                InterfaceConfigAndStatus.getInstance().setDoutPinStatus(index, (byte)1);
                intent.putExtra(ShsService.BROADCAST_FUNCTIONID,functionID);
            }
            Log.i(TAG,"send ouput pin broadcast");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dout_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() ==R.id.dout_setting){
            Intent intent =new Intent(DoutActivity.this, DoutSettingActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}
