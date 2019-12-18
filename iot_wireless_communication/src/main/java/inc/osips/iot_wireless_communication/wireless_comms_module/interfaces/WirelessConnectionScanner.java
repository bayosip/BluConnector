package inc.osips.iot_wireless_communication.wireless_comms_module.interfaces;

/**
 * Created by BABY v2.0 on 1/20/2017.
 */

public interface WirelessConnectionScanner {

    boolean isScanning();
    void onStart();
    void onStop();
    void showDiscoveredDevices();
}
