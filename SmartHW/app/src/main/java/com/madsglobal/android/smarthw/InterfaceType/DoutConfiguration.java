package com.madsglobal.android.smarthw.InterfaceType;

/**
 * Created by Ximei on 27/04/2016.
 */
public class DoutConfiguration {
    boolean enabled;
    byte pullValue;
    byte driveValue;
    byte defaultValue;

    public DoutConfiguration(boolean enabled, byte pullValue, byte driveValue, byte defaultValue) {
        this.enabled = enabled;
        this.pullValue = pullValue;
        this.driveValue = driveValue;
        this.defaultValue = defaultValue;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public byte getPullValue() {
        return pullValue;
    }

    public void setPullValue(byte pullValue) {
        this.pullValue = pullValue;
    }

    public byte getDriveValue() {
        return driveValue;
    }

    public void setDriveValue(byte driveValue) {
        this.driveValue = driveValue;
    }

    public byte getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(byte defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "DoutConfiguration{" +
                "defaultValue=" + defaultValue +
                ", enabled=" + enabled +
                ", pullValue=" + pullValue +
                ", driveValue=" + driveValue +
                '}';
    }
}
