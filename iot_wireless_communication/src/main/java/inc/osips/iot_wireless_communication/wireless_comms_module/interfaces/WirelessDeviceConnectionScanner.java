package inc.osips.iot_wireless_communication.wireless_comms_module.interfaces;

/**
 * Created by BABY v2.0 on 1/20/2017.
 */

public interface WirelessDeviceConnectionScanner {

    final String DEVICE_DISCOVERED = "Wireless_Device_Discovered";
    final String SCANNING_STOPPED = "device_scanning_stopped";

    boolean isScanning();
    void onStart();
    void onStop();
    void showDiscoveredDevices();
}
