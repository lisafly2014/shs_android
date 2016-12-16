package com.madsglobal.android.smarthw.common;

/**
 * Created by Ximei on 7/12/2016.
 */
public class Constants {
    // SHS Interface type
    public static final int SHS_DIN = 0; //digitalInput
    public static final int SHS_DOUT = 1; //digitalOutput
    public static final int SHS_ANI = 2; //analogueInput
    public static final int SHS_PWM = 3;
    public static final int SHS_SERVO = 4;
    public static final int SHS_SPI = 5;
    public static final int SHS_I2C = 6;
    public static final int SHS_UART = 7;
    public static final int SHS_UC5 = 8;
    public static final int SHS_QUAD = 9;

    //SHS reponse type
    public static final int RESPONSE_SUCCESS = 1;
    public static final int RESPONSE_FAIL = 2;
    public static final int RESPONSE_UNKNOWN = 3;
    public static final int RESPONSE_LOCKED = 4;
    public static final int RESPONSE_CLASH = 5;
    public static final int RESPONSE_NOT_CONFIGURED = 6;
    public static final int RESPONSE_WRONG_LENGTH = 7;

    //SHS COMMAND ID
    public static final int MANAGE_ID_DEVICE_RESPONSE = 3;
    public static final int MANAGE_ID_DEVICE_RESET = 4;
    public static final int MANAGE_ID_DEVICE_SET_NAME = 5;
    public static final int MANAGE_ID_DEVICE_READ_INFO = 6;
    public static final int MANAGE_ID_DEVICE_GET_INTERFACES = 7;
    public static final int MANAGE_ID_DEVICE_CLEAR_INTERFACES = 8;
    public static final int MANAGE_ID_DEVICE_STORE_CONFIG = 9;
    public static final int MANAGE_ID_DEVICE_LOCK = 10;
    public static final int MANAGE_ID_DEVICE_UNLOCKED = 11;

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
    public static final int FUNCTION_ID_PWM_1 = 113;
    public static final int FUNCTION_ID_PWM_2 = 114;
    public static final int FUNCTION_ID_PWM_3 = 115;


    public static final int FUNCTION_ID_SERVO_0 = 116;
    public static final int FUNCTION_ID_SERVO_1 = 117;
    public static final int FUNCTION_ID_SERVO_2 = 118;
    public static final int FUNCTION_ID_SERVO_3 = 119;

    public static final int FUNCTION_ID_UART = 120;
    public static final int FUNCTION_ID_UART_STATUS = 121;

    public static final int FUNCTION_ID_SPI = 122;
    public static final int FUNCTION_ID_I2C = 123;
    public static final int FUNCTION_ID_RC5 = 124;
    public static final int FUNCTION_ID_QUAD = 125;
    public static final int FUNCTION_ID_TEMP_INT = 126;
    public static final int FUNCTION_ID_MAX = 127;
}
