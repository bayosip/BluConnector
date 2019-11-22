package inc.osips.bleproject.interfaces;

import android.content.ServiceConnection;

public interface WirelessDeviceConnector {

    boolean isConnected();
    ServiceConnection getConnection();
    void sendInstructionsToDevice(String instuctions);
}
