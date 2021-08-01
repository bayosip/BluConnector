package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.wlan_comms;

import android.content.Intent;
import android.content.ServiceConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

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
    public void selectServiceUsingUUID(@Nullable String deviceAddress, @NonNull String UUID) {
    }

    @Override
    public void connectToDeviceWithDeviceInfoFrom(Intent intent) {
    }

    @Override
    public void sendInstructionsToRemoteDevice(@Nullable String deviceAddress,
                                               @NonNull String instructions) {
    }

    @Override
    public void sendInstructionsToRemoteDevice(@Nullable String deviceAddress,
                                               @Nullable  UUID charxDescriptor, @NonNull String instructions) {

    }
}
