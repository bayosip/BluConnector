package inc.osips.bleproject.interfaces;

import android.os.Bundle;

import java.util.List;

import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.Devices;

public interface ScannerViewInterface extends AppActivity{

    void goToDeviceControlView(final Bundle data);
    void progressFromScan(final List<Devices> devices);
}
