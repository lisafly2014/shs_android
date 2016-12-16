package com.madsglobal.android.smarthw.InterfaceType;

/**
 * Created by Ximei on 12/13/2016.
 */

public class AinConfiguration {
    boolean enabled;
    byte rangeValue;
    byte rateVale;

    public AinConfiguration(boolean enabled, byte rangeValue, byte rateVale) {
        this.enabled = enabled;
        this.rangeValue = rangeValue;
        this.rateVale = rateVale;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public byte getRangeValue() {
        return rangeValue;
    }

    public void setRangeValue(byte rangeValue) {
        this.rangeValue = rangeValue;
    }

    public byte getRateVale() {
        return rateVale;
    }

    public void setRateVale(byte rateVale) {
        this.rateVale = rateVale;
    }

    @Override
    public String toString() {
        return "AinConfiguration{" +
                "enabled=" + enabled +
                ", rangeValue=" + rangeValue +
                ", rateVale=" + rateVale +
                '}';
    }
}
