package com.madsglobal.android.smarthw;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.madsglobal.android.smarthw.InterfaceType.AinConfiguration;
import com.madsglobal.android.smarthw.InterfaceType.DinConfiguration;
import com.madsglobal.android.smarthw.InterfaceType.DoutConfiguration;
import com.madsglobal.android.smarthw.InterfaceType.InterfaceConfigAndStatus;
import com.madsglobal.android.smarthw.InterfaceType.PwmConfiguration;
import com.madsglobal.android.smarthw.InterfaceType.ServoConfiguration;
import com.madsglobal.android.smarthw.error.GattError;
import com.madsglobal.android.smarthw.exception.DeviceDisconnectedException;
import com.madsglobal.android.smarthw.exception.ShsException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

public class ShsService extends IntentService {
    private static final String TAG = "ShsService";
    /**
     * The address of the hardware to update.
     */
    public static final String EXTRA_HARDWARE_ADDRESS = "com.madsglobal.android.shs.extra.HARDWARE_ADDRESS";
    public static final String EXTRA_HARDWARE_NAME = "com.madsglobal.android.shs.extra.HARDWARE_NAME";
    private String mHardwareAddress;
    private String mHardwareName;

    /*
   *Broadcast shs connction status
   */
    public static final String BROADCAST_SHS_CONNECTION_STATUS = "com.madsglobal.android.shs.broadcast.shs_connection_status";
    public static final String EXTRA_SHS_CONNECTION_STATUS = "com.madsglobal.android.shs.extra.shs_connection_status";

    /**
     * Broadcast inputPin status message
     */
    public static final String BROADCAST_DIN_STATUS_UPDATE = "com.madsglobal.android.shs.broadcast.din_status_update";

    /**
     * Broadcast output/pwm/servo status change message
     */
    public static final String BROADCAST_INTERFACE_STATUS_CHANGE = "com.madsglobal.android.shs.broadcast.interface_status_change";

    public static final String BROADCAST_FUNCTIONID ="com.madsglobal.android.shs.broadcast.functionid";
    /*
    *Broadcast analog input pin status message
     */
    public static final String BROADCAST_AIN_STATUS_UPDATE = "com.madsglobal.android.shs.broadcast.ain_status_update";
    public static final String BROADCAST_AIN_DATA = "com.madsglobal.android.shs.broadcast.ain_data";
    /*
   * Broadcast Pin configuration error message
   */
    public static final String BROADCAST_CONFIG_ERROR = "com.madsglobal.anroid.shs.broadcast.broadcast_configuration_error";
    public static String BROADCAST_CONFIG_ERROR_MSG = "com.madsglobal.android.shs.broadcast.configuration_error_message";


    /*
    *Broadcast Pin configuration message
    */
    public static final String BROADCAST_CONFIG_UPDATE = "com.madsglobal.android.shs.broadcast.config_update";
    public static final String BROADCAST_CONFIG_DATA = "com.madsglobal.android.shs.broadcast.config_data";

    public static final String BROADCAST_PINNUMBER = "com.madsglobal.android.shs.broadcast_pinnumber";
     public static final String BROADCAST_STOP_SERVICE = "com.madsglobal.android.shs.broadcast_stop_service";

    public static byte[] updatedConfig;
    public static int functionID;
    private static int configCommand;
    private static byte[] mDataBuffer;

    /**
     * UUIDs used by the SHS
     */
    public static final UUID SHS_SERVICE_UUID = new UUID(0x485300005a4e6c61L, 0x626f6c475344414dL);
    private static final UUID SHS_SET_SEND_UUID = new UUID(0x485300015a4e6c61L, 0x626f6c475344414dL);  //shs set send UUID
    private static final UUID SHS_GET_RECEIVE_UUID = new UUID(0x485300025a4e6c61L, 0x626f6c475344414dL); //shs get receive UUID
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = new UUID(0x0000290200001000L, 0x800000805f9b34fbL);

    private static final int NOTIFICATIONS = 1;
    private static final int INDICATIONS = 2;
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    /**
     * The current connection state. If its value is > 0 than an error has occurred. Error number is a negative value of mConnectionState
     */
    private int mConnectionState;
    private final static int STATE_DISCONNECTED = 0;
    private final static int STATE_CONNECTING = -1;
    private final static int STATE_CONNECTED = -2;
    private final static int STATE_CONNECTED_AND_READY = -3; // indicates that services were discovered
    private final static int STATE_DISCONNECTING = -4;
    private final static int STATE_CLOSED = -5;

    /**
     * Lock used in synchronization purposes
     */
    private final Object mLock = new Object();
//    private Thread shsServiceThread;
    private static boolean isServiceStopped;
    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothGatt btGatt;

    public static final int PIN_COUNT = 32;
    public static final int ANALOGUE_INPUT_PIN_NUMBER = 6;
    public static final int PWM_CHANNEL = 4;
    public static final int SERVO_CHANNEL = 4;
    private static DinConfiguration[] inputPinConfiguration = new DinConfiguration[PIN_COUNT];
    private static byte[] inputPinStatus = new byte[PIN_COUNT];
    private static DoutConfiguration[] outputPinConfiguration = new DoutConfiguration[PIN_COUNT];
    private static byte[] outputPinStatus = new byte[PIN_COUNT];
    private static AinConfiguration[] analogPinConfiguration = new AinConfiguration[ANALOGUE_INPUT_PIN_NUMBER];
    private static int[] analogPinStatus = new int[ANALOGUE_INPUT_PIN_NUMBER];

    private static PwmConfiguration[] pwmConfiguration = new PwmConfiguration[PWM_CHANNEL];
    private static byte[] pwmDutyCycle = new byte[PWM_CHANNEL];

    private static ServoConfiguration[] servoConfiguration = new ServoConfiguration[SERVO_CHANNEL];
    private static byte[] servoPercentages = new byte[SERVO_CHANNEL];

    private static ArrayList<Byte> readCmdArray;
    private static int readCount;
    private static boolean finishedGettingConfiguration;
    private static boolean finishedGettingStatus;
    private static boolean isLocked;
//    private long mStartTime;
//    private long endTime;

    /**
     * The number of the last error that has occurred or 0 if there was no error
     */
    private int mError;
    /**
     * Flag set when we got confirmation from the device that notifications are enabled.
     */
    private boolean mNotificationsEnabled;
    /**
     * Flag set when we got confirmation from the device that Service Changed indications are enabled.
     */
    private boolean mServiceChangedIndicationsEnabled;

    /**
     * An extra field to send the progress or error information in the DFU notification. The value may contain:
     * <ul>
     * <li>Value 0 - 100 - percentage progress value</li>
     * <li>An error code with {@link #ERROR_MASK} if initialization error occurred</li>
     * <li>An error code with {@link #ERROR_REMOTE_MASK} if remote SHS target returned an error</li>
     * <li>An error code with {@link #ERROR_CONNECTION_MASK} if connection error occurred (f.e. GATT error (133) or Internal GATT Error (129))</li>
     * </ul>
     * To check if error occurred use:<br />
     * {@code boolean error = progressValue >= MyService.ERROR_MASK;}
     */

    /**
     * The broadcast error message contains the following extras:
     * <ul>
     * <li>{@link #EXTRA_ERROR_DATA} - the error number. Use {@link GattError#parse(int)} to get String representation</li>
     * <li>{@link #EXTRA_HARDWARE_ADDRESS} - the target device address</li>
     * </ul>
     */
    public static final String BROADCAST_ERROR = "com.madsglobal.android.shs.broadcast.BROADCAST_ERROR";
    /**
     * The type of the error. This extra contains information about that kind of error has occurred. Connection state errors and other errors may share the same numbers.
     * For example, the {@link BluetoothGattCallback#onCharacteristicWrite(BluetoothGatt, BluetoothGattCharacteristic, int)} method may return a status code 8 (GATT INSUF AUTHORIZATION),
     * while the status code 8 returned by {@link BluetoothGattCallback#onConnectionStateChange(BluetoothGatt, int, int)} is a GATT CONN TIMEOUT error.
     */
    public static final String EXTRA_ERROR_TYPE = "com.madsglobal.android.shs.extra.EXTRA_ERROR_TYPE";

    /**
     * Error message
     */
    public static final String EXTRA_ERROR_DATA = "com.madsglobal.android.shs.extra.EXTRA_ERROR_DATA";

    public static final int ERROR_TYPE_OTHER = 0;
    public static final int ERROR_TYPE_COMMUNICATION_STATE = 1;
    public static final int ERROR_TYPE_COMMUNICATION = 2;

    /**
     * If this bit is set than the progress value indicates an error. Use {@link GattError#parse(int)} to obtain error name.
     */
    public static final int ERROR_MASK = 0x1000;
    public static final int ERROR_DEVICE_DISCONNECTED = ERROR_MASK; // | 0x00;
    /**
     * Error thrown then {@code gatt.discoverServices();} returns false.
     */
    public static final int ERROR_SERVICE_DISCOVERY_NOT_STARTED = ERROR_MASK | 0x01;
    /**
     * Thrown when the service discovery has finished but the SHS service has not been found. The device does not support SHS.
     */
    public static final int ERROR_SERVICE_NOT_FOUND = ERROR_MASK | 0x02;
    /**
     * Thrown when the required SHS service has been found but at least one of the SHS characteristics is absent.
     */
    public static final int ERROR_CHARACTERISTICS_NOT_FOUND = ERROR_MASK | 0x03;
    /**
     * Thrown when the the Bluetooth adapter is disabled.
     */
    public static final int ERROR_BLUETOOTH_DISABLED = ERROR_MASK | 0x04;
    public static final int ERROR_SERVICE_THREAD_EXCEPTION = ERROR_MASK | 0x05;
    public static final int ERROR_DEVICE_EXCEPTION_DISCONNECTED = ERROR_MASK | 0x06;
    /**
     * The flag set when one of {@link android.bluetooth.BluetoothGattCallback} methods was called with status other than {@link android.bluetooth.BluetoothGatt#GATT_SUCCESS}.
     */
    public static final int ERROR_CONNECTION_MASK = 0x2000;
    /**
     * The flag set when the {@link android.bluetooth.BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)} method was called with
     * status other than {@link android.bluetooth.BluetoothGatt#GATT_SUCCESS}.
     */
    public static final int ERROR_CONNECTION_STATE_MASK = 0x4000;

     //SHS reponse type
    public static final int RESPONSE_SUCCESS = 1;
//    public static final int RESPONSE_FAIL = 2;
//    public static final int RESPONSE_UNKNOWN = 3;
    public static final int RESPONSE_LOCKED = 4;
    public static final int RESPONSE_CLASH = 5;
//    public static final int RESPONSE_NOT_CONFIGURED = 6;
    public static final int RESPONSE_WRONG_LENGTH = 7;

    //SHS COMMAND ID
    public static final int MANAGE_ID_DEVICE_RESPONSE = 3;
//    public static final int MANAGE_ID_DEVICE_RESET = 4;
//    public static final int MANAGE_ID_DEVICE_SET_NAME = 5;
//    public static final int MANAGE_ID_DEVICE_READ_INFO = 6;
    public static final int MANAGE_ID_DEVICE_GET_INTERFACES = 7;
//    public static final int MANAGE_ID_DEVICE_CLEAR_INTERFACES = 8;
    public static final int MANAGE_ID_DEVICE_STORE_CONFIG = 9;
    public static final int MANAGE_ID_DEVICE_LOCK = 10;
    public static final int MANAGE_ID_DEVICE_UNLOCKED = 11;
    public static final int MANAGE_ID_UPDATE_STATUS = 150;

    public static final int MANAGE_ID_INTERFACE_ADD = 20;
    public static final int MANAGE_ID_INTERFACE_DELETE = 21;
    public static final int MANAGE_ID_INTERFACE_GET = 22;
    public static final int MANAGE_ID_INTERFACE_READ = 23;

    /*SHS FUNCTION ID*/
    //Digital Input ID
    public static final int FUNCTION_ID_DIN_0 = 32;
    public static final int FUNCTION_ID_DIN_31 = 63;

    //Digital Output ID
    public static final int FUNCTION_ID_DOUT_0 = 64;
    public static final int FUNCTION_ID_DOUT_31 = 95;

    //Analogue Input ID
    public static final int FUNCTION_ID_AIN_0 = 96;
    public static final int FUNCTION_ID_AIN_5 = 101;

    //PWM ID
    public static final int FUNCTION_ID_PWM_0 = 112;

    public static final int FUNCTION_ID_PWM_3 = 115;


    public static final int FUNCTION_ID_SERVO_0 = 116;

    public static final int FUNCTION_ID_SERVO_3 = 119;



    public ShsService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        final IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        manager.registerReceiver(mConnectionStateBroadcastReceiver, filter);

        final IntentFilter pinFilter = new IntentFilter();
        pinFilter.addAction(BROADCAST_INTERFACE_STATUS_CHANGE);
        pinFilter.addAction(BROADCAST_CONFIG_UPDATE);
        pinFilter.addAction(BROADCAST_STOP_SERVICE);

        manager.registerReceiver(mShsPinStatusReceiver, pinFilter);
    }

    /**
     * Initializes bluetooth adapter
     *
     * @return <code>true</code> if initialization was successful
     */
    private boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            loge("Unable to initialize BluetoothManager.");
            return false;
        }

        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            loge("Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.unregisterReceiver(mConnectionStateBroadcastReceiver);
        manager.unregisterReceiver(mShsPinStatusReceiver);
    }

    /**
     * receive broadcast because output pin status change or pin configuration update
     */
    private final BroadcastReceiver mShsPinStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BROADCAST_INTERFACE_STATUS_CHANGE:
                    byte number = intent.getByteExtra(BROADCAST_FUNCTIONID, (byte)0);

                    if (number>= FUNCTION_ID_DOUT_0 && number <= FUNCTION_ID_DOUT_31){
                        byte[] doutValue = {number, outputPinStatus[number -FUNCTION_ID_DOUT_0]};
                        updateInterfaceStatus(doutValue);
                    }else if(number>= FUNCTION_ID_PWM_0 && number <= FUNCTION_ID_PWM_3){
                        byte[] pwmValue = {number, pwmDutyCycle[number -FUNCTION_ID_PWM_0 ]};
                        updateInterfaceStatus(pwmValue);
                    }else if(number>= FUNCTION_ID_SERVO_0 && number <= FUNCTION_ID_SERVO_3){
                        byte[] servoValue = {number, servoPercentages[number - FUNCTION_ID_SERVO_0 ]};
                        updateInterfaceStatus(servoValue);
                    }
                    break;
                case BROADCAST_CONFIG_UPDATE:
                    mDataBuffer = intent.getByteArrayExtra(BROADCAST_CONFIG_DATA);
                    updatedConfig = new byte[mDataBuffer.length-2];
                    if(mDataBuffer[0] ==MANAGE_ID_INTERFACE_ADD){
                        logi("mDatabuffer: "+mDataBuffer[2]);

                        System.arraycopy(mDataBuffer,2,updatedConfig,0,mDataBuffer.length-2);
                    }
                    functionID = mDataBuffer[1];
                    sendDeviceConfigurationCommand(MANAGE_ID_DEVICE_UNLOCKED);
                    break;
                case BROADCAST_STOP_SERVICE:
                    isServiceStopped = true;
                    synchronized (mLock) {
                        mLock.notifyAll();
                    }
                    break;
            }
        }
    };

    /**
     * When bluetooth connection status change
     */
    private final BroadcastReceiver mConnectionStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            // Obtain the device and check it this is the one that we are connected to
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (!device.getAddress().equals(mHardwareAddress))
                return;


            final String action = intent.getAction();
            logi("Action received: " + action);
            logi("external connection disconnected");

            mConnectionState = STATE_DISCONNECTED;
            // Notify waiting thread
            synchronized (mLock) {
                mLock.notifyAll();
            }
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // Check whether an error occurred
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    logi("Connected to GATT server");
                    mConnectionState = STATE_CONNECTED;

					/*
                     *  The onConnectionStateChange callback is called just after establishing connection and before sending Encryption Request BLE event in case of a paired device.
					 *  In that case and when the Service Changed CCCD is enabled we will get the notification after initializing the encryption, about 1600 milliseconds later.
					 *  If we discover services right after connecting, the onServicesDiscovered callback will be called immediately, before receiving the notification and the following
					 *  service discovery and we may end up with old, application's services instead.
					 *
					 *  NOTE: We are doing this to avoid the hack with calling the hidden gatt.refresh() method, at least for bonded devices.
					 */
                    if (gatt.getDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
                        try {
                            synchronized (this) {
                                logd("Waiting 1600 ms for a possible Service Changed indication...");
                                wait(1600);

                                // After 1.6s the services are already discovered so the following gatt.discoverServices() finishes almost immediately.

                                // NOTE: This also works with shorted waiting time. The gatt.discoverServices() must be called after the indication is received which is
                                // about 600ms after establishing connection. Values 600 - 1600ms should be OK.
                            }
                        } catch (InterruptedException e) {
                            // Do nothing
                        }
                    }

                    // Attempts to discover services after successful connection.
                    final boolean success = gatt.discoverServices();
                    logi("Attempting to start service discovery... " + (success ? "succeed" : "failed"));

                    if (!success) {
                        mError = ERROR_SERVICE_DISCOVERY_NOT_STARTED;
                        // Notify waiting thread
                        synchronized (mLock) {
                            mLock.notifyAll();
                        }
                    } else {
                        // Just return here, lock will be notified when service discovery finishes
                        return;
                    }
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    logi("Disconnected from GATT server");
                    mConnectionState = STATE_DISCONNECTED;
                    if (!isServiceStopped) {
                        mError = ERROR_DEVICE_EXCEPTION_DISCONNECTED;
                        sendErrorBroadcast(mError);
                        // Notify waiting thread
                        synchronized (mLock) {
                            mLock.notifyAll();
                        }
                    } else {
                        logi("Exit SHS");
                    }
                }
            } else {
                loge("Connection state change error: " + status + " newState: " + newState);
                if (newState == BluetoothGatt.STATE_DISCONNECTED)
                    mConnectionState = STATE_DISCONNECTED;

                mError = ERROR_CONNECTION_STATE_MASK | status;
                if (mError != 0) {
                    int error = mError;
                    loge("Device has disconnected");
                    // Connection state errors and other Bluetooth GATT callbacks share the same error numbers. Therefore we are using bit masks to identify the type.
                    if ((error & ERROR_CONNECTION_STATE_MASK) > 0) {
                        error &= ~ERROR_CONNECTION_STATE_MASK;
                        loge(String.format("Error (0x%02X): %s", error, GattError.parseConnectionError(error)));
                    } else {
                        error &= ~ERROR_CONNECTION_MASK;
                        loge(String.format("Error (0x%02X): %s", error, GattError.parseConnectionError(error)));
                    }
                    sendErrorBroadcast(mError);
                    // Notify waiting thread
                    synchronized (mLock) {
                        mLock.notifyAll();
                    }
                }
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                logi("Services discovered");
                mConnectionState = STATE_CONNECTED_AND_READY;
            } else {
                loge("Service discovery error: " + status);
                mError = ERROR_CONNECTION_MASK | status;
            }

            // Notify waiting thread
            synchronized (mLock) {
                mLock.notifyAll();
            }
        }

        @Override
        public void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                loge("Descriptor write error: " + status);
                mError = ERROR_CONNECTION_MASK | status;
                // Notify waiting thread
                synchronized (mLock) {
                    mLock.notifyAll();
                }
            } else {
                mNotificationsEnabled = true;
                logi("CCCD enabled!");
                synchronized (mLock) {
                    mLock.notifyAll();
                }
            }
        }

        @Override
        public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (SHS_SET_SEND_UUID.equals(characteristic.getUuid())) {
                    if (finishedGettingConfiguration) {
                        if (!finishedGettingStatus) {
                            if(readCount < readCmdArray.size()){
                                readCount++;
                                ReadPinStatus();
                            }else{
                                finishedGettingStatus = true;
                                logi("finished getting status");
                            }
                        } else {
                               switch (configCommand) {
                                case MANAGE_ID_DEVICE_UNLOCKED:
                                    logi("send unlock device command");
//                                    endTime = SystemClock.elapsedRealtime();
//                                    logi("send unlock time has taken " + (endTime - mStartTime) + " ms");
                                    sendDeviceConfigurationCommand();
                                    break;
                                case MANAGE_ID_INTERFACE_ADD:
                                case MANAGE_ID_INTERFACE_DELETE:
                                    logi("send update device configuration command");
                                    sendDeviceConfigurationCommand(MANAGE_ID_DEVICE_STORE_CONFIG);
                                    break;
                                case MANAGE_ID_DEVICE_STORE_CONFIG:
                                    logi("send device store config command");
                                    if(isLocked){
                                        sendDeviceConfigurationCommand(MANAGE_ID_DEVICE_LOCK);
                                    }
                                    break;
                                case MANAGE_ID_DEVICE_LOCK:
                                    logi("send lock device command");
                                    break;
                                default:
                                    loge("update interface status");

                            }

                        }
                    }

                } else {
                    mError = ERROR_CHARACTERISTICS_NOT_FOUND;
                    synchronized (mLock) {
                        mLock.notifyAll();
                    }
                }
            } else {
                loge("Characteristic write error: " + status);
                mError = ERROR_CONNECTION_MASK | status;
                synchronized (mLock) {
                    mLock.notifyAll();
                }
            }
        }

        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            if (data[0] == MANAGE_ID_INTERFACE_GET) {
                //  int ID = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
                readCmdArray.add((data[1]));
                if (data[1] >= FUNCTION_ID_DIN_0 && data[1] <= FUNCTION_ID_DIN_31) {
                    int dinIndex = data[1] - FUNCTION_ID_DIN_0;
                    inputPinConfiguration[dinIndex].setEnabled(true);
                    inputPinConfiguration[dinIndex].setPullValue(data[2]);

//                    logi(" "+ inputPinConfiguration[dinIndex]);
                } else if (data[1] >= FUNCTION_ID_DOUT_0 && data[1] <= FUNCTION_ID_DOUT_31) {
                    readCmdArray.add(data[1]);
                    int doutIndex =  data[1] - FUNCTION_ID_DOUT_0;
                    int pullValue = data[2] & 0x03;
                    int driveValue = (data[2] & 0x1C) >> 2;
                    outputPinConfiguration[doutIndex].setEnabled(true);
                    outputPinConfiguration[doutIndex].setPullValue((byte)pullValue);
                    outputPinConfiguration[doutIndex].setPullValue((byte)driveValue);
                    outputPinConfiguration[doutIndex].setDefaultValue(data[3]);

//                    logi(" "+ outputPinConfiguration[doutIndex]);
                } else if (data[1] >= FUNCTION_ID_AIN_0 && data[1] <= FUNCTION_ID_AIN_5) {
                    readCmdArray.add(data[1]);
                    int ainIndex = data[1] - FUNCTION_ID_AIN_0;
                    int rangeValue =data[2]&0x03;
                    int rateValue = (data[2] & 0x1C)>> 2;
                    analogPinConfiguration[ainIndex].setEnabled(true);
                    analogPinConfiguration[ainIndex].setRangeValue((byte)rangeValue);
                    analogPinConfiguration[ainIndex].setRateVale((byte)rateValue);
//                    logi(" "+ analogPinConfiguration[ainIndex]);
                } else if (data[1] >= FUNCTION_ID_PWM_0 && data[1] <= FUNCTION_ID_PWM_3) {
                    readCmdArray.add(data[1]);
                    int pwmIndex = data[1] - FUNCTION_ID_PWM_0;
                    pwmConfiguration[pwmIndex].setEnabled(true);
                    pwmConfiguration[pwmIndex].setPinIndex(data[2]); //pwm channel
                    pwmConfiguration[pwmIndex].setDriveValue(data[3]);//pwm drive
                    pwmConfiguration[pwmIndex].setDutyCycle(data[4]); //pwm dutyCycle

//                    logi(" " + pwmConfiguration[pwmIndex]);
                } else if (data[1] >= FUNCTION_ID_SERVO_0 && data[1] <= FUNCTION_ID_SERVO_3) {
                    readCmdArray.add(data[1]);
                    int servoIndex = data[1] - FUNCTION_ID_SERVO_0;
                    servoConfiguration[servoIndex].setEnabled(true);
                    servoConfiguration[servoIndex].setPinIndex(data[2]); //servo channel
                    servoConfiguration[servoIndex].setDriveValue(data[3]); //servo drive
                    servoConfiguration[servoIndex].setPercentage(data[4]); //servo percentage
//                    logi(" "+servoConfiguration[servoIndex]);
                }
            } else if (data[0] == MANAGE_ID_DEVICE_RESPONSE) {
               if (data[1] == MANAGE_ID_DEVICE_GET_INTERFACES) {
                    finishedGettingConfiguration = true;
                    logi("Finished getting configuration");
                    ReadPinStatus();
                    sendSHSStatusBroadcast(true);
                } else if (data[1] == MANAGE_ID_INTERFACE_ADD) {
                    if (data[2] == RESPONSE_SUCCESS) {
                        if (functionID >= FUNCTION_ID_DIN_0 && functionID <= FUNCTION_ID_DIN_31) {
                            logi("Update input pin configuration");
                            int dinIndex = functionID - FUNCTION_ID_DIN_0;
                            if(!inputPinConfiguration[dinIndex].isEnabled()){
                                inputPinConfiguration[dinIndex].setEnabled(true);
                                inputPinStatus[dinIndex] = 0;
                            }
                            inputPinConfiguration[dinIndex].setPullValue(updatedConfig[0]);
                            logi("inputPinStatus:"+inputPinConfiguration[dinIndex] );
                            logi("inputPinStatus:"+inputPinStatus[dinIndex] );
                        } else if (functionID >= FUNCTION_ID_DOUT_0 && functionID <= FUNCTION_ID_DOUT_31) {
                            logi("Update output pin configuration");
                            int doutIndex = functionID - FUNCTION_ID_DOUT_0;
                            int pullValue = updatedConfig[0] & 0x0c;
                            int driveValue = (updatedConfig[0] &0x1C) >> 2;
                            if(!outputPinConfiguration[doutIndex].isEnabled()){
                                outputPinConfiguration[doutIndex].setEnabled(true);
                                outputPinStatus[doutIndex] = updatedConfig[1];
                            }

                            outputPinConfiguration[doutIndex].setPullValue((byte)pullValue);
                            outputPinConfiguration[doutIndex].setDriveValue((byte)driveValue);
                            outputPinConfiguration[doutIndex].setDefaultValue(updatedConfig[1]);

                        } else if (functionID >= FUNCTION_ID_AIN_0 && functionID <= FUNCTION_ID_AIN_5) {
                            logi("Update analog input pin configuration");
                            int aniIndex = functionID - FUNCTION_ID_AIN_0;
                            int rangeValue = updatedConfig[0] & 0x03;
                            int rateValue = (updatedConfig[0] & 0x0C) >> 2;
                            if(!analogPinConfiguration[aniIndex].isEnabled()){
                                analogPinConfiguration[aniIndex].setEnabled(true);
                                analogPinStatus[aniIndex] =  updatedConfig[0];
                            }
                            analogPinConfiguration[aniIndex].setRangeValue((byte)rangeValue);
                            analogPinConfiguration[aniIndex].setRateVale((byte)rateValue);

                        } else if (functionID >= FUNCTION_ID_PWM_0 && functionID <= FUNCTION_ID_PWM_3) {
                            logi("Update pwm configuration");
                            int pwmIndex = functionID - FUNCTION_ID_PWM_0;
                            if(!pwmConfiguration[pwmIndex].isEnabled()){
                                pwmConfiguration[pwmIndex].setEnabled(true);
                                pwmDutyCycle[pwmIndex] = updatedConfig[2];
                            }
                            pwmConfiguration[pwmIndex].setPinIndex(updatedConfig[0]);
                            pwmConfiguration[pwmIndex].setDriveValue(updatedConfig[1]);
                            pwmConfiguration[pwmIndex].setDutyCycle(updatedConfig[2]);
                        } else if (functionID >= FUNCTION_ID_SERVO_0 && functionID <= FUNCTION_ID_SERVO_3) {
                            logi("Update servo configuration");
                            int servoIndex = functionID - FUNCTION_ID_SERVO_0;
                            if(!servoConfiguration[servoIndex].isEnabled()){
                                servoConfiguration[servoIndex].setEnabled(true);
                                servoPercentages[servoIndex] = updatedConfig[2];
                            }

                            servoConfiguration[servoIndex].setPinIndex(updatedConfig[0]);
                            servoConfiguration[servoIndex].setDriveValue(updatedConfig[1]);
                            servoConfiguration[servoIndex].setPercentage(updatedConfig[2]);
                        }
                       // sendDeviceConfigurationCommand(MANAGE_ID_DEVICE_STORE_CONFIG);
                    } else if (data[2] == RESPONSE_WRONG_LENGTH) {
                        sendPinSettingErrorBroadcast("Command length worong during update pin");
                    } else if (data[2] == RESPONSE_LOCKED) {
                        sendPinSettingErrorBroadcast("Device locked during update pin");
                    } else if (data[2] == RESPONSE_CLASH) {
                        sendPinSettingErrorBroadcast("Pin clash during update pin");
                    }
                } else if (data[1] == MANAGE_ID_INTERFACE_DELETE) {
                    if (data[2] == RESPONSE_SUCCESS) {
                        if (functionID >= FUNCTION_ID_DIN_0 && functionID <= FUNCTION_ID_DIN_31) {
                            logi("Delete input pin configuration");
                            int dinIndex = functionID - FUNCTION_ID_DIN_0;
                            inputPinConfiguration[dinIndex].setEnabled(false);
                            inputPinConfiguration[dinIndex].setPullValue((byte) 0);
                            inputPinStatus[dinIndex] =(byte)0;
                            logi("inputPinStatus:"+inputPinConfiguration[dinIndex] );
                            logi("inputPinStatus:"+inputPinStatus[dinIndex] );
                        } else if (functionID >= FUNCTION_ID_DOUT_0 && functionID <= FUNCTION_ID_DOUT_31) {
                            int doutIndex = functionID - FUNCTION_ID_DOUT_0;
                            logi("Delete output pin configuration " + doutIndex);
                            outputPinConfiguration[doutIndex].setEnabled(false);
                            outputPinConfiguration[doutIndex].setDriveValue((byte)0);
                            outputPinConfiguration[doutIndex].setPullValue((byte)0);
                            outputPinConfiguration[doutIndex].setDefaultValue((byte) 0);
                            outputPinStatus[doutIndex] = (byte) 0;
                        } else if (functionID >= FUNCTION_ID_AIN_0 && functionID <= FUNCTION_ID_AIN_5) {
                            logi("Delete analog input pin configuration");
                            int aniIndex = functionID - FUNCTION_ID_AIN_0;
                            analogPinConfiguration[aniIndex].setEnabled(false);
                            analogPinConfiguration[aniIndex].setRangeValue((byte) 0);
                            analogPinConfiguration[aniIndex].setRateVale((byte)0);
                            analogPinStatus[aniIndex] = (byte) 0;
                        } else if (functionID >= FUNCTION_ID_PWM_0 && functionID <= FUNCTION_ID_PWM_3) {
                            logi("Delete pwm configuration");
                            int pwmIndex = functionID - FUNCTION_ID_PWM_0;
                            pwmConfiguration[pwmIndex].setEnabled(false);
                            pwmConfiguration[pwmIndex].setPinIndex((byte) 0);
                            pwmConfiguration[pwmIndex].setDriveValue((byte) 0);
                            pwmConfiguration[pwmIndex].setDutyCycle((byte) 0);
                            pwmDutyCycle[pwmIndex] = 0;
                        } else if (functionID >= FUNCTION_ID_SERVO_0 && functionID <= FUNCTION_ID_SERVO_3) {
                            int servoIndex = functionID - FUNCTION_ID_SERVO_0;
                            logi("Delete servo configuration");
                            servoConfiguration[servoIndex].setEnabled(false);
                            servoConfiguration[servoIndex].setPinIndex((byte) 0);
                            servoConfiguration[servoIndex].setDriveValue((byte) 0);
                            servoConfiguration[servoIndex].setPercentage((byte) 0);
                            servoPercentages[servoIndex] = 0;
                        }
                    } else if (data[2] == RESPONSE_WRONG_LENGTH) {
                        sendPinSettingErrorBroadcast("Command length worong during delete pin");
                    } else if (data[2] == RESPONSE_LOCKED) {
                        sendPinSettingErrorBroadcast("Device locked during delete pin");
                    } else if (data[2] == RESPONSE_CLASH) {
                        sendPinSettingErrorBroadcast("Pin clash during delete pin");
                    }
                } else if (data[1] == MANAGE_ID_DEVICE_UNLOCKED) {
                    if (data[2] == RESPONSE_SUCCESS) {
                        logi("Unlock devce successfully.");
                    }
                } else if (data[1] == MANAGE_ID_DEVICE_LOCK) {
                    if (data[2] == RESPONSE_SUCCESS) {
                        logi("Lock device successfully");
                    } else {
                        sendPinSettingErrorBroadcast("Lock device fail.");
                    }
                } else if (data[1] == MANAGE_ID_DEVICE_STORE_CONFIG) {
                    if (data[2] == RESPONSE_SUCCESS) {
                        logi("Store device configuration successfully");
                    } else {
                        sendPinSettingErrorBroadcast("Store device configuration fail");
                    }
                }
            } else if (data[0] >= FUNCTION_ID_DIN_0 && data[0] <= FUNCTION_ID_DIN_31) {
                int pinIndex = data[0] - FUNCTION_ID_DIN_0;
                inputPinStatus[pinIndex] = data[1];
                updateInputPinStatus(pinIndex);
            } else if (data[0] >= FUNCTION_ID_DOUT_0 && data[0] <= FUNCTION_ID_DOUT_31) {
                outputPinStatus[data[0] - FUNCTION_ID_DOUT_0] = data[1];
            } else if (data[0] >= FUNCTION_ID_AIN_0 && data[0] <= FUNCTION_ID_AIN_5) {
                int sampleMSB = data[1] & 0xFF;
                int sampleLSB = data[2] & 0xFF;
                int pinIndex = data[0] - FUNCTION_ID_AIN_0;
                int value = (sampleMSB << 8) + sampleLSB;
                analogPinStatus[pinIndex] = value;
                updateAinPinStatus(pinIndex, value);
            } else if (data[0] >= FUNCTION_ID_PWM_0 && data[0] <= FUNCTION_ID_PWM_3) {
                pwmDutyCycle[data[0] - FUNCTION_ID_PWM_0] = data[1];
            } else if (data[0] >= FUNCTION_ID_SERVO_0 && data[0] <= FUNCTION_ID_SERVO_3) {
                servoPercentages[data[0] - FUNCTION_ID_SERVO_0] = data[1];
            }
        }
    };


    /**
     * After get interface configuration and read pin status, sendSHSStatusBroadcast to send out(#STATUS_CONFIGURATION_READY) to refresh the main UI
     */
    private void sendSHSStatusBroadcast(boolean connectionStatus) {
        logi("sendSHSStatusBroadcast");
        final Intent intent = new Intent(BROADCAST_SHS_CONNECTION_STATUS);

        intent.putExtra(EXTRA_SHS_CONNECTION_STATUS, connectionStatus);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Since sending error pin configuration command to the device side, get error message feedback from the device. Broadcast the error message to UI to display
     *
     * @param msg --  Error message
     */
    private void sendPinSettingErrorBroadcast(String msg) {
        logi("sendPinSettingBroadcast");
        final Intent intent = new Intent(BROADCAST_CONFIG_ERROR);
        intent.putExtra(BROADCAST_CONFIG_ERROR_MSG, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private void updateInterfaceStatus(byte[] value) {
        configCommand = MANAGE_ID_UPDATE_STATUS;
        final BluetoothGattCharacteristic sendCharacteristic = btGatt.getService(SHS_SERVICE_UUID).getCharacteristic(SHS_SET_SEND_UUID);
        sendCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        sendCharacteristic.setValue(value);
        btGatt.writeCharacteristic(sendCharacteristic);

    }

    /**
     * When updating interface configuration, send different command to finish the whole configure process
     *
     * @param command command could be:
     *                <ul>
     *                <li>{@link #MANAGE_ID_DEVICE_UNLOCKED}</li>
     *                <li>{@link #MANAGE_ID_DEVICE_STORE_CONFIG}</li>
     *                <li>{@link #MANAGE_ID_DEVICE_LOCK}</li>
     *                </ul>
     *                </p>
     */
    private void sendDeviceConfigurationCommand(int command) {
        logi("sendDeviceConfigurationCommand");
        configCommand = command;
        byte[] value = {(byte) command};

        final BluetoothGattCharacteristic sendCharacteristic = btGatt.getService(SHS_SERVICE_UUID).getCharacteristic(SHS_SET_SEND_UUID);
        sendCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        sendCharacteristic.setValue(value);

//        mStartTime = SystemClock.elapsedRealtime();
        btGatt.writeCharacteristic(sendCharacteristic);
    }

    private void sendDeviceConfigurationCommand() {
         configCommand = mDataBuffer[0];
        final BluetoothGattCharacteristic sendCharacteristic = btGatt.getService(SHS_SERVICE_UUID).getCharacteristic(SHS_SET_SEND_UUID);
        sendCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
         sendCharacteristic.setValue(mDataBuffer);

//        mStartTime = SystemClock.elapsedRealtime();
        btGatt.writeCharacteristic(sendCharacteristic);
    }

    /**
     * Broadcast UI to update input pin status
     *
     * @param pinNumber -- input pin number
     */
    private void updateInputPinStatus(int pinNumber) {
        final Intent intent = new Intent(BROADCAST_DIN_STATUS_UPDATE);
        intent.putExtra(BROADCAST_PINNUMBER, pinNumber);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Broadcast UI to update analog pin status
     *
     * @param pinNumber -- pin number
     * @param value     -- corresponding ani pin value
     */
    private void updateAinPinStatus(int pinNumber, int value) {
        // logi("updateAinPinStatus");
        final Intent intent = new Intent(BROADCAST_AIN_STATUS_UPDATE);
        intent.putExtra(BROADCAST_PINNUMBER, pinNumber);
        intent.putExtra(BROADCAST_AIN_DATA, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    /**
     * Connects to the BLE device with given address. This method is SYNCHRONOUS, it wait until the connection status change from {@link #STATE_CONNECTING} to {@link #STATE_CONNECTED_AND_READY} or an
     * error occurs. This method returns <code>null</code> if Bluetooth adapter is disabled.
     *
     * @param address the device address
     * @return the GATT device or <code>null</code> if Bluetooth adapter is disabled.
     */
    private BluetoothGatt connect(final String address) {
        if (!mBluetoothAdapter.isEnabled())
            return null;

        mConnectionState = STATE_CONNECTING;

        logi("Connecting to the device...");
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        final BluetoothGatt gatt = device.connectGatt(this, false, mGattCallback);

        // We have to wait until the device is connected and services are discovered
        // Connection error may occur as well.
        try {
            synchronized (mLock) {
                while ((mConnectionState == STATE_CONNECTING || mConnectionState == STATE_CONNECTED) && mError == 0)
                    mLock.wait();
            }
        } catch (final InterruptedException e) {
            loge("Sleeping interrupted", e);
        }
        return gatt;
    }

    /**
     * Disconnects from the device and cleans local variables in case of error. This method is SYNCHRONOUS and wait until the disconnecting process will be completed.
     *
     * @param gatt  the GATT device to be disconnected
      */
    private void terminateConnection(final BluetoothGatt gatt) {
        if (mConnectionState != STATE_DISCONNECTED) {
            // Disconnect from the device
            disconnect(gatt);
        }
        // Close the device
        refreshDeviceCache(gatt, true);
        close(gatt);
    }

    /**
     * Disconnects from the device. This is SYNCHRONOUS method and waits until the callback returns new state. Terminates immediately if device is already disconnected. Do not call this method
     * directly, use {@link #terminateConnection(android.bluetooth.BluetoothGatt)} instead.
     *
     * @param gatt the GATT device that has to be disconnected
     */
    private void disconnect(final BluetoothGatt gatt) {
        if (mConnectionState == STATE_DISCONNECTED)
            return;

        mConnectionState = STATE_DISCONNECTING;

        logi("Disconnecting from the device...");
        gatt.disconnect();

        // We have to wait until device gets disconnected or an error occur
        waitUntilDisconnected();

    }

    /**
     * Wait until the connection state will change to {@link #STATE_DISCONNECTED} or until an error occurs.
     */
    private void waitUntilDisconnected() {
        try {
            synchronized (mLock) {
                while (mConnectionState != STATE_DISCONNECTED && mError == 0)
                    mLock.wait();
            }
        } catch (final InterruptedException e) {
            loge("Sleeping interrupted", e);
        }
    }

    /**
     * Closes the GATT device and cleans up.
     *
     * @param gatt the GATT device to be closed
     */
    private void close(final BluetoothGatt gatt) {
        logi("Cleaning up...");
        gatt.close();
        mConnectionState = STATE_CLOSED;
    }

    /**
     * Clears the device cache. After uploading new firmware the DFU target will have other services than before.
     *
     * @param gatt  the GATT device to be refreshed
     * @param force <code>true</code> to force the refresh
     */
    private void refreshDeviceCache(final BluetoothGatt gatt, final boolean force) {
        /*
         * If the device is bonded this is up to the Service Changed characteristic to notify Android that the services has changed.
		 * There is no need for this trick in that case.
		 * If not bonded, the Android should not keep the services cached when the Service Changed characteristic is present in the target device database.
		 * However, due to the Android bug (still exists in Android 5.0.1), it is keeping them anyway and the only way to clear services is by using this hidden refresh method.
		 */
        if (force) {
            /*
             * There is a refresh() method in BluetoothGatt class but for now it's hidden. We will call it using reflections.
			 */
            try {
                final Method refresh = gatt.getClass().getMethod("refresh");
                if (refresh != null) {
                    final boolean success = (Boolean) refresh.invoke(gatt);
                    logi("Refreshing result: " + success);
                }
            } catch (Exception e) {
                loge("An exception occurred while refreshing device", e);
            }
        }
    }

    /**
     * initialize interface configuration
     */
    private void initInterfaceConfiguration() {
        for (int j = 0; j < PIN_COUNT; j++) {
            inputPinConfiguration[j] = new DinConfiguration(false, (byte) 0);
            inputPinStatus[j] = 0;
        }


        for (int k = 0; k < PIN_COUNT; k++) {
            outputPinConfiguration[k] = new DoutConfiguration(false, (byte) 0,(byte)0, (byte) 0); //enabled,pull,drive,default value
            outputPinStatus[k] = 0;
        }

        for (int i = 0; i < 6; i++) {
            analogPinConfiguration[i] = new AinConfiguration(false,(byte)0, (byte)0);   //enabled,range,rate value
            analogPinStatus[i]= 0;
        }

        for (int p = 0; p < PWM_CHANNEL; p++) {
            pwmConfiguration[p] = new PwmConfiguration(false, (byte) 0, (byte) 0, (byte) 0);
            pwmDutyCycle[p] = 0;
        }

        for (int q = 0; q < SERVO_CHANNEL; q++) {
            servoConfiguration[q] = new ServoConfiguration(false, (byte) 0, (byte) 0, (byte) 0);
            servoPercentages[q] = 0;
        }

        InterfaceConfigAndStatus.getInstance().setDinConfig(inputPinConfiguration);
        InterfaceConfigAndStatus.getInstance().setDinCurrentStatus(inputPinStatus);

        InterfaceConfigAndStatus.getInstance().setDoutConfig(outputPinConfiguration);
        InterfaceConfigAndStatus.getInstance().setDoutCurrentStatus(outputPinStatus);

        InterfaceConfigAndStatus.getInstance().setAinConfig(analogPinConfiguration);
        InterfaceConfigAndStatus.getInstance().setAinCurrentStatus(analogPinStatus);

        InterfaceConfigAndStatus.getInstance().setPwmConfig(pwmConfiguration);
        InterfaceConfigAndStatus.getInstance().setPwmCurrentDutyCycle(pwmDutyCycle);

        InterfaceConfigAndStatus.getInstance().setServoConfig(servoConfiguration);
        InterfaceConfigAndStatus.getInstance().setServoPercentages(servoPercentages);
    }

    /**
     * Enables or disables the notifications for given characteristic. This method is SYNCHRONOUS and wait until the
     * {@link android.bluetooth.BluetoothGattCallback#onDescriptorWrite(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattDescriptor, int)} will be called or the connection state will change from {@link #STATE_CONNECTED_AND_READY}. If
     * connection state will change, or an error will occur, an exception will be thrown.
     *
     * @param gatt           the GATT device
     * @param characteristic the characteristic to enable or disable notifications for
     * @param type           {@link #NOTIFICATIONS} or {@link #INDICATIONS}
     * @throws DeviceDisconnectedException
     * @throws ShsException
     */
    private void enableCCCD(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int type) throws DeviceDisconnectedException, ShsException {
        final String debugString = type == NOTIFICATIONS ? "notifications" : "indications";
        if (mConnectionState != STATE_CONNECTED_AND_READY)
            throw new DeviceDisconnectedException("Unable to set " + debugString + " state", mConnectionState);
        mError = 0;
        if ((type == NOTIFICATIONS && mNotificationsEnabled) || (type == INDICATIONS && mServiceChangedIndicationsEnabled))
            return;

        logi("Enabling " + debugString + " for " + characteristic.getUuid());

        // enable notifications locally
        gatt.setCharacteristicNotification(characteristic, true);

        // enable notifications on the device
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        descriptor.setValue(type == NOTIFICATIONS ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        logi("gatt.writeDescriptor(" + descriptor.getUuid() + (type == NOTIFICATIONS ? ", value=0x01-00)" : ", value=0x02-00)"));
        gatt.writeDescriptor(descriptor);

        // We have to wait until device receives a response or an error occur
        try {
            synchronized (mLock) {
                while ((((type == NOTIFICATIONS && !mNotificationsEnabled) || (type == INDICATIONS && !mServiceChangedIndicationsEnabled))
                        && mConnectionState == STATE_CONNECTED_AND_READY && mError == 0))
                    mLock.wait();
            }
        } catch (final InterruptedException e) {
            loge("Sleeping interrupted", e);
        }
        if (mError != 0)
            throw new ShsException("Unable to set " + debugString + " state", mError);
        if (mConnectionState != STATE_CONNECTED_AND_READY)
            throw new DeviceDisconnectedException("Unable to set " + debugString + " state", mConnectionState);
    }

    /**
     * get device configuration
     *
     */
    private void getDeviceConfiguration() {
        logi("getDeviceConfiguration");
        final byte[] data = {MANAGE_ID_DEVICE_GET_INTERFACES};
        final BluetoothGattCharacteristic sendCharacteristic = btGatt.getService(SHS_SERVICE_UUID).getCharacteristic(SHS_SET_SEND_UUID);
        sendCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        sendCharacteristic.setValue(data);
        btGatt.writeCharacteristic(sendCharacteristic);
    }

    /**
     * After get device default configuration, read inputPin status
     *
     */
    private void ReadPinStatus(){
        if(readCount < readCmdArray.size()){
            byte[] data = {MANAGE_ID_INTERFACE_READ, readCmdArray.get(readCount)};
            final BluetoothGattCharacteristic sendCharacteristic = btGatt.getService(SHS_SERVICE_UUID).getCharacteristic(SHS_SET_SEND_UUID);
            sendCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            sendCharacteristic.setValue(data);
            btGatt.writeCharacteristic(sendCharacteristic);
        }
    }

      @Override
    protected void onHandleIntent(final Intent intent) {
          final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
          isLocked = preferences.getBoolean("deviceLocked", true);
          // Read input parameters
          final String hardwareAddress = intent.getStringExtra(EXTRA_HARDWARE_ADDRESS);
          final String deviceName = intent.getStringExtra(EXTRA_HARDWARE_NAME);
          mHardwareAddress = hardwareAddress;
          mHardwareName = deviceName;
          mConnectionState = STATE_DISCONNECTED;
          mError = 0;
          mNotificationsEnabled = false;

          btGatt = null;
          readCmdArray=new ArrayList<Byte>();
          readCount = 0;
          finishedGettingConfiguration = false;
          finishedGettingStatus = false;

          isServiceStopped = false;

          initInterfaceConfiguration();

          /*connect to the hardware. */
          btGatt = connect(hardwareAddress);


        if (btGatt == null) {
            loge("Bluetooth adapter disabled");
            sendErrorBroadcast(ERROR_BLUETOOTH_DISABLED);
            return;
        }
        if (mError > 0) { // error occurred
            final int error = mError & ~ERROR_CONNECTION_STATE_MASK;
            loge(String.format("Connection failed (0x%02X): %s", error, GattError.parseConnectionError(error)));
            terminateConnection(btGatt);
            return;
        }
        // We have connected to SHS device and services are discoverer
        final BluetoothGattService shsService = btGatt.getService(SHS_SERVICE_UUID); // there was a case when the service was null. I don't know why
        if (shsService == null) {
            loge("SHS service does not exists on the device");
            terminateConnection(btGatt);
            return;
        }
        logi("SHS service found.");
        final BluetoothGattCharacteristic shsGetReceiveCharacteristic = shsService.getCharacteristic(SHS_GET_RECEIVE_UUID);

        final BluetoothGattCharacteristic shsSetSendCharacteristic = shsService.getCharacteristic(SHS_SET_SEND_UUID);
        if (shsGetReceiveCharacteristic == null || shsSetSendCharacteristic == null) {
            loge("SHS characteristics not found in the SHS service");
            terminateConnection(btGatt);
            return;
        }
        logi("shs send characteristic and receive characteristic found.");
         try {
            // Enable notifications
            enableCCCD(btGatt, shsGetReceiveCharacteristic, NOTIFICATIONS);

            getDeviceConfiguration();

            // We have to wait until device receives a response or an error occur
            try {
                synchronized (mLock) {
                    while (!isServiceStopped && mConnectionState == STATE_CONNECTED_AND_READY && mError == 0)
                        mLock.wait();
                }
                logi("isServiceStopped: "+isServiceStopped+",mConnectionState: "+mConnectionState+",mError: "+mError);
                if (mError != 0) {
                    logi("mError!=0");
                    terminateConnection(btGatt);
                } else {
                    logi("mError =0");
                    disconnect(btGatt);
                }

            } catch (final InterruptedException e) {
                loge("Shs exit exception.", e);
            }

        } catch (final DeviceDisconnectedException e) {
            loge("Device has disconnected");
            loge(e.getMessage());
            close(btGatt);
            sendErrorBroadcast(ERROR_DEVICE_DISCONNECTED);

        } catch (final ShsException e) {
            int error = e.getErrorNumber();
            // Connection state errors and other Bluetooth GATT callbacks share the same error numbers. Therefore we are using bit masks to identify the type.
            if ((error & ERROR_CONNECTION_STATE_MASK) > 0) {
                error &= ~ERROR_CONNECTION_STATE_MASK;
                loge(String.format("Error (0x%02X): %s", error, GattError.parseConnectionError(error)));
            } else {
                error &= ~ERROR_CONNECTION_MASK;
                loge(String.format("Error (0x%02X): %s", error, GattError.parseConnectionError(error)));
            }
            loge(e.getMessage());
            terminateConnection(btGatt);
        }

    }

    private void sendErrorBroadcast(final int error) {
        final Intent broadcast = new Intent(BROADCAST_ERROR);
        if ((error & ERROR_CONNECTION_MASK) > 0) {
            broadcast.putExtra(EXTRA_ERROR_DATA, error & ~ERROR_CONNECTION_MASK);
            broadcast.putExtra(EXTRA_ERROR_TYPE, ERROR_TYPE_COMMUNICATION);
        } else if ((error & ERROR_CONNECTION_STATE_MASK) > 0) {
            broadcast.putExtra(EXTRA_ERROR_DATA, error & ~ERROR_CONNECTION_STATE_MASK);
            broadcast.putExtra(EXTRA_ERROR_TYPE, ERROR_TYPE_COMMUNICATION_STATE);
        } else {
            broadcast.putExtra(EXTRA_ERROR_DATA, error);
            broadcast.putExtra(EXTRA_ERROR_TYPE, ERROR_TYPE_OTHER);
        }
        broadcast.putExtra(EXTRA_HARDWARE_ADDRESS, mHardwareAddress);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }


    private void loge(final String message) {
        if (BuildConfig.DEBUG)
            Log.e(TAG, message);
    }


    private void loge(final String message, final Throwable e) {
        if (BuildConfig.DEBUG)
            Log.e(TAG, message, e);
    }

    private void logw(final String message) {
        if (BuildConfig.DEBUG)
            Log.w(TAG, message);
    }

    private void logi(final String message) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, message);
    }

    private void logd(final String message) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, message);
    }
/*
    private String parse(final byte[] data) {
        if (data == null){
            logi("data is null,check!!");
            return "";
        }

        final int length = data.length;
        if (length == 0)
            return "";

        final char[] out = new char[length * 3 - 1];
        for (int j = 0; j < length; j++) {
            int v = data[j] & 0xFF;
            out[j * 3] = HEX_ARRAY[v >>> 4];
            out[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            if (j != length - 1)
                out[j * 3 + 2] = '-';
        }
        return new String("pase data" + out);
    }
    */
}
