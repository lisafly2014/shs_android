package com.madsglobal.android.smarthw.InterfaceType;

/**
 * Created by Ximei on 7/29/2016.
 */
public class PwmConfiguration {
    boolean enabled;
    byte pinIndex;
    byte driveValue;
    byte dutyCycle;

    public PwmConfiguration(boolean enabled, byte pinIndex, byte driveValue, byte dutyCycle) {
        this.enabled = enabled;
        this.pinIndex = pinIndex;
        this.driveValue = driveValue;
        this.dutyCycle = dutyCycle;
    }

    public byte getDriveValue() {
        return driveValue;
    }

    public void setDriveValue(byte driveValue) {
        this.driveValue = driveValue;
    }

    public byte getDutyCycle() {
        return dutyCycle;
    }

    public void setDutyCycle(byte dutyCycle) {
        this.dutyCycle = dutyCycle;
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

    @Override
    public String toString() {
        return "PwmConfiguration{" +
                "driveValue=" + driveValue +
                ", enabled=" + enabled +
                ", pinIndex=" + pinIndex +
                ", dutyCycle=" + dutyCycle +
                '}';
    }
}
