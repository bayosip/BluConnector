package inc.osips.bleproject.interfaces;

/**
 * Created by BABY v2.0 on 1/20/2017.
 */

public interface WirelessConnectionScanner {

    boolean isScanning();
    void onStart();
    void onStop();
    void showDiscoveredDevices();
}
