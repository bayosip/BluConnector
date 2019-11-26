package inc.osips.bleproject.interfaces;

import android.content.Intent;
import android.content.ServiceConnection;

public interface WirelessDeviceConnector {

    boolean isConnected();
    ServiceConnection getServiceConnection();
    void connectToDeviceWithDeviceInfoFrom(Intent intent);
    void sendInstructionsToDevice(String instuctions);
}
