package inc.osips.bleproject.model;

import android.os.Parcelable;
import android.text.TextUtils;

import inc.osips.bleproject.interfaces.ControllerViewInterface;
import inc.osips.bleproject.interfaces.WirelessDeviceConnector;
import inc.osips.bleproject.model.ble_comms.BLE_Scanner;
import inc.osips.bleproject.model.ble_comms.BleConnection;
import inc.osips.bleproject.model.utilities.Constants;
import inc.osips.bleproject.model.wifi_comms.Wifi_Scanner;

public class DeviceConnectionFactory {

    private ControllerViewInterface viewInterface;

    public DeviceConnectionFactory(ControllerViewInterface viewInterface) {
        this.viewInterface = viewInterface;
    }

    public WirelessDeviceConnector establishConnectionWithDeviceOf(String connectionType, Parcelable device){

        if(TextUtils.isEmpty(connectionType))return null;

        else if (connectionType.equals(Constants.BLE)){
            return new BleConnection(viewInterface, device);
        }

        else if (connectionType.equals(Constants.WIFI)){
            return new BleConnection(viewInterface, device);
        }

        else return null;
    }
}
