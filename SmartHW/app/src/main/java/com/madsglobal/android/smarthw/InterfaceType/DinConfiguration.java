package com.madsglobal.android.smarthw.InterfaceType;

import static android.R.attr.value;

/**
 * Created by Ximei on 27/04/2016.
 */
public class DinConfiguration {
    boolean enabled;
    byte pullValue;

    public DinConfiguration(boolean enabled, byte pullValue) {
        this.enabled = enabled;
        this.pullValue = pullValue;
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

    @Override
    public String toString() {
        return "DinConfiguration{" +
                "enabled=" + enabled +
                ", pullValue=" + pullValue +
                '}';
    }
}