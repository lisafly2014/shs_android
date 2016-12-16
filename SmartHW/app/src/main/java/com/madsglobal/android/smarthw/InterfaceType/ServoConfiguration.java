package com.madsglobal.android.smarthw.InterfaceType;

/**
 * Created by Ximei on 7/29/2016.
 */
public class ServoConfiguration {
    boolean enabled;
    byte pinIndex;
    byte driveValue;
    byte percentage;

    public ServoConfiguration(boolean enabled, byte pinIndex, byte driveValue, byte percentage) {
        this.enabled = enabled;
        this.pinIndex = pinIndex;
        this.driveValue = driveValue;
        this.percentage = percentage;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public byte getPinIndex() {
        return pinIndex;
    }

    public void setPinIndex(byte pinIndex) {
        this.pinIndex = pinIndex;
    }

    public byte getDriveValue() {
        return driveValue;
    }

    public void setDriveValue(byte driveValue) {
        this.driveValue = driveValue;
    }


    public byte getPercentage() {
        return percentage;
    }

    public void setPercentage(byte percentage) {
        this.percentage = percentage;
    }



    @Override
    public String toString() {
        return "ServoConfiguration{" +
                " enabled= "+ enabled+
                ", pinIndex=" + pinIndex +
                ", driveValue=" + driveValue+
                ", percentage=" + percentage +
                '}';
    }
}
