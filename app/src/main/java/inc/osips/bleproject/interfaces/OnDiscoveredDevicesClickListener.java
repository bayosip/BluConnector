package inc.osips.bleproject.interfaces;

import android.content.Context;

import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.Devices;

public interface OnDiscoveredDevicesClickListener {

    void selectDevicesToConnectTo(Devices device);
    Context getListenerContext();

}
