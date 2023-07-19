package inc.osips.iot_wireless_communication.wireless_comms_module.interfaces;

import androidx.annotation.Nullable;

/**
 * Created by BABY v2.0 on 1/20/2017.
 */

public interface WirelessDeviceConnectionScanner {

    final String DEVICE_DISCOVERED = "Wireless_Device_Discovered";
    final String SCANNING_STOPPED = "device_scanning_stopped";

    boolean isScanning();
    void onStart(@Nullable String deviceName);
    void onStop();
    void showDiscoveredDevices();
}
