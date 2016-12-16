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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.madsglobal.android.smarthw.R;
import com.madsglobal.android.smarthw.ShsActivity;
import com.madsglobal.android.smarthw.ShsService;
import com.madsglobal.android.smarthw.common.SHSLifeCycleHandler;
import com.madsglobal.android.smarthw.common.Utility;
import com.madsglobal.android.smarthw.error.GattError;

public class InterfaceTypeActivity extends AppCompatActivity {
    private ListView listview;
    String[] interfaceType = {
            "Digital Input", "Digital Output", "Analog Input", "PWM", "Servo", "SPI", "I2C", "UART", "RCS", "QUAD"
    };
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
        Intent intent = new Intent(InterfaceTypeActivity.this, ShsService.class);
        stopService(intent);
        String message =""+GattError.parse(error) ;
        Utility.setConnected(false);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.remote_exception_disconnect).setMessage(message);
        alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                Intent intent = new Intent(InterfaceTypeActivity.this, ShsActivity.class);
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
        setContentView(R.layout.activity_interface_type);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.activity_listview, interfaceType);
        listview = (ListView) findViewById(R.id.list);
        if(listview !=null){
            listview.setAdapter(adapter);
        }


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String interfaceName = interfaceType[position];
                Intent intent = null;
                try {
                    if (interfaceName.equals(interfaceType[0])) {
                        intent = new Intent(InterfaceTypeActivity.this, DinActivity.class);
                    } else if (interfaceName.equals(interfaceType[1])) {
                        intent = new Intent(InterfaceTypeActivity.this, DoutActivity.class);
                    } else if (interfaceName.equals(interfaceType[2])) {
                        intent = new Intent(InterfaceTypeActivity.this, AinActivity.class);
                    }else if(interfaceName.equals(interfaceType[3])) {
                        intent = new Intent(InterfaceTypeActivity.this,PwmActivity.class);
                    }else if(interfaceName.equals(interfaceType[4])) {
                        intent = new Intent(InterfaceTypeActivity.this,ServoActivity.class);
                    }
                    startActivity(intent);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }
        });
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
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ShsService.BROADCAST_ERROR);
        broadcastManager.registerReceiver(errorBroadcastReciever,intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!SHSLifeCycleHandler.isApplicationInForeground()) {
            Utility.stopService(this);
            Utility.showNotification(this,getString(R.string.shs_disconnected));
        }
    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return true;
//    }
}
