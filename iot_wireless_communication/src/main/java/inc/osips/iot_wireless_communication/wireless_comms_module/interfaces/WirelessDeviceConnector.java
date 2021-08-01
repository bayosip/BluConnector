package inc.osips.iot_wireless_communication.wireless_comms_module.interfaces;

import android.content.Intent;
import android.content.ServiceConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.Devices;

public interface WirelessDeviceConnector {

    String MTU_CHANGE_SUCCESS ="Success";
    String MTU_CHANGE_FAILURE = "Failure";
    boolean isConnected();
    ServiceConnection getServiceConnection();
    void selectServiceUsingUUID (@Nullable String deviceAddress, @NonNull String UUID);
    void connectToDeviceWithDeviceInfoFrom(@NonNull Intent intent);
    void sendInstructionsToRemoteDevice(@Nullable String deviceAddress, @NonNull String instructions);
    void sendInstructionsToRemoteDevice(@Nullable String deviceAddress,
                                        @Nullable UUID charxDescriptor, @NonNull String instructions);
}
