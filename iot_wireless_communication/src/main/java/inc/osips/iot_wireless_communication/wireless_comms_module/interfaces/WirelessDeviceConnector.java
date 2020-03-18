package inc.osips.iot_wireless_communication.wireless_comms_module.interfaces;

import android.content.Intent;
import android.content.ServiceConnection;

public interface WirelessDeviceConnector {

    boolean isConnected();
    ServiceConnection getServiceConnection();
    void connectToDeviceWithDeviceInfoFrom(Intent intent);
    void sendInstructionsToRemoteDevice(String instuctions);
}
