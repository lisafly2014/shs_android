package com.madsglobal.android.smarthw.InterfaceType;

/**
 * Created by Ximei on 2/05/2016.
 */
public class InterfaceConfigAndStatus {
    private static InterfaceConfigAndStatus instance = new InterfaceConfigAndStatus();
    DinConfiguration[] dinConfig;
    byte[] dinCurrentStatus;

    DoutConfiguration[] doutConfig;
    byte[] doutCurrentStatus;

    AinConfiguration[] ainConfig;
    int[] ainCurrentStatus;

    PwmConfiguration[] pwmConfig;
    byte[] pwmCurrentDutyCycle;

    ServoConfiguration[] servoConfig;
    byte[] servoPercentages;

    public ServoConfiguration[] getServoConfig() {
        return servoConfig;
    }

    public void setServoConfig(ServoConfiguration[] servoConfig) {
        this.servoConfig = servoConfig;
    }

    public byte[] getServoPercentages() {
        return servoPercentages;
    }

    public void setServoPercentages(byte[] servoPercentages) {
        this.servoPercentages = servoPercentages;
    }

    public void setServoPercentage(int i, byte percentage){
        if(servoPercentages == null){
            throw new NullPointerException("SHS:servo percentage Array hasn't instantialised!");
        }else{
            servoPercentages[i] = percentage;
        }
    }



    //Runtime initialization
    //By default ThreadSafe
    public static InterfaceConfigAndStatus getInstance() {
        return instance;
    }

    public DinConfiguration[] getDinConfig() {
        return dinConfig;
    }

    public void setDinConfig(DinConfiguration[] dinConfig) {
        this.dinConfig = dinConfig;
    }

    public byte[] getDinCurrentStatus() {
        return dinCurrentStatus;
    }

    public void setDinCurrentStatus(byte[] dinCurrentStatus) {
        this.dinCurrentStatus = dinCurrentStatus;
    }

    public DoutConfiguration[] getDoutConfig() {
        return doutConfig;
    }

    public void setDoutConfig(DoutConfiguration[] doutConfig) {
        this.doutConfig = doutConfig;
    }

    public byte[] getDoutCurrentStatus() {
        return doutCurrentStatus;
    }

    public void setDoutCurrentStatus(byte[] doutCurrentStatus) {
        this.doutCurrentStatus = doutCurrentStatus;
    }
    public void setDoutPinStatus(int i, byte value) {
        if (this.doutCurrentStatus == null) {
            throw new NullPointerException("SHS:output pin status Array hasn't instantialised!");
        } else {
            this.doutCurrentStatus[i] = value;
        }
    }

    public AinConfiguration[] getAinConfig() {
        return ainConfig;
    }

    public void setAinConfig(AinConfiguration[] ainConfig) {
        this.ainConfig = ainConfig;
    }

    public int[] getAinCurrentStatus() {
        return ainCurrentStatus;
    }

    public void setAinCurrentStatus(int[] ainCurrentStatus) {
        this.ainCurrentStatus = ainCurrentStatus;
    }

    public PwmConfiguration[] getPwmConfig() {
        return pwmConfig;
    }

    public void setPwmConfig(PwmConfiguration[] pwmConfig) {
        this.pwmConfig = pwmConfig;
    }

    public void setPwmCurrentDutyCycle(int i, byte dutyCycle) {
        if (pwmCurrentDutyCycle == null) {
            throw new NullPointerException("SHS:pwm dutyCycle Array hasn't instantialised!");
        } else {
            pwmCurrentDutyCycle[i] = dutyCycle;
        }
    }

    public byte[] getPwmCurrentDutyCycle() {
        return pwmCurrentDutyCycle;
    }

    public void setPwmCurrentDutyCycle(byte[] pwmDutyCycle) {
        this.pwmCurrentDutyCycle = pwmDutyCycle;
    }
}
