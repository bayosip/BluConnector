package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.wlan_comms;

import android.content.Intent;
import android.content.ServiceConnection;

import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnector;

public class WLANServiceConnection implements WirelessDeviceConnector {
    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public ServiceConnection getServiceConnection() {
        return null;
    }

    @Override
    public void connectToDeviceWithDeviceInfoFrom(Intent intent) {

    }

    @Override
    public void sendInstructionsToRemoteDevice(String instructions) {

    }
}
