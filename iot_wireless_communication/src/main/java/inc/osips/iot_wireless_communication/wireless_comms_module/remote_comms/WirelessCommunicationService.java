package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms.services.BleGattService;

public abstract class WirelessCommunicationService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
