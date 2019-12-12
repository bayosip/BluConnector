package inc.osips.bleproject.interfaces;

import android.content.Context;

import inc.osips.bleproject.model.remote_comms.Devices;

public interface OnDiscoveredDevicesClickListener {

    void selectDeviceToConnectTo(Devices device);
    Context getListenerContext();

}
