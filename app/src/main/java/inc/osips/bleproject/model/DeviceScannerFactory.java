package inc.osips.bleproject.model;

import android.text.TextUtils;

import inc.osips.bleproject.interfaces.PresenterInterface;
import inc.osips.bleproject.interfaces.WirelessConnectionScanner;
import inc.osips.bleproject.model.ble_comms.BLE_Scanner;
import inc.osips.bleproject.model.utilities.Constants;
import inc.osips.bleproject.model.wifi_comms.Wifi_Scanner;

public class DeviceScannerFactory {

    private PresenterInterface anInterface;

    public DeviceScannerFactory(PresenterInterface anInterface) {
        this.anInterface = anInterface;
    }

    public WirelessConnectionScanner getRemoteConnectionScannerOfType(String connectionType){

        if(TextUtils.isEmpty(connectionType))return null;

        else if (connectionType.equals(Constants.BLE)){
            return new BLE_Scanner(anInterface);
        }

        else if (connectionType.equals(Constants.WIFI)){
            return new Wifi_Scanner(anInterface);
        }

        else return null;
    }
}
