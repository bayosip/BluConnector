package inc.osips.bleproject.model;

import android.os.Parcelable;

public class Devices {

    private String deviceName;
    private String deviceAddress;
    private int deviceRSSI;
    private Parcelable deviceData;

    public Devices(String deviceName, String deviceAddress, int deviceRSSI, Parcelable deviceData) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.deviceRSSI = deviceRSSI;
        this.deviceData = deviceData;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public int getDeviceRSSI() {
        return deviceRSSI;
    }

    public Parcelable getDeviceData() {
        return deviceData;
    }

}
