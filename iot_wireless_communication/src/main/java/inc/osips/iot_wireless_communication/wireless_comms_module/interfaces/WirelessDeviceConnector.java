package inc.osips.iot_wireless_communication.wireless_comms_module.interfaces;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.Devices;

public interface WirelessDeviceConnector {

    String MTU_CHANGE_SUCCESS ="Success";
    String MTU_CHANGE_FAILURE = "Failure";
    boolean isConnected();
    ServiceConnection getServiceConnection();
    void disconnectDevice(@NonNull String address);
    void increaseMessagingByteLimit(@NonNull String address, int size);
    void maxOutMessagingByteLimit(@NonNull String address);
    void enableNotificationsFor(String serviceUuid, String attrId, String descriptor, String deviceAddress);
    void disableNotificationsFor(String serviceUuid, String attrId, String descriptor, String deviceAddress);
    void connectAnotherDeviceSimultaneously(@NonNull Parcelable device, @Nullable String serviceUUID) throws Exception;
    void selectServiceUsingUUID (@NonNull String deviceAddress, @NonNull String UUID);
    void connectToDeviceWithDeviceInfoFrom(@NonNull Intent intent);
    void sendStringInstructionsToRemoteDevice(@Nullable String deviceAddress, @NonNull String instructions);
    void sendStringInstructionsToRemoteDevice(@Nullable String deviceAddress,
                                              @Nullable UUID charxUuid, @NonNull String instructions);
    void sendBytesInstructionsToRemoteDevice(String deviceAddr,
                                             @Nullable UUID charxUuid, byte[] data);
    void sendBytesInstructionsToRemoteDevice(String deviceAddr, byte[] data);
}
