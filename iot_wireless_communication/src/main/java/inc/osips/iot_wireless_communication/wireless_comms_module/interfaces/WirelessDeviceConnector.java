package inc.osips.iot_wireless_communication.wireless_comms_module.interfaces;

import android.content.Intent;
import android.content.ServiceConnection;

import androidx.annotation.NonNull;

public interface WirelessDeviceConnector {

    String NO_MORE_SERVICES_AVAILABLE = "No_more_services";
    String MTU_CHANGE_SUCCESS ="Success";
    String MTU_CHANGE_FAILURE = "Failure";
    boolean isConnected();
    ServiceConnection getServiceConnection();
    void selectServiceUsingUUID (@NonNull String UUID);
    void connectToDeviceWithDeviceInfoFrom(@NonNull Intent intent);
    void sendInstructionsToRemoteDevice(@NonNull String instuctions);
}
