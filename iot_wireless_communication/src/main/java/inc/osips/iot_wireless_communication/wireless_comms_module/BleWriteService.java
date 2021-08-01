package inc.osips.iot_wireless_communication.wireless_comms_module;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.LinkedList;
import java.util.List;

import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms.services.BleGattService;

public class BleWriteService {

    private BluetoothGattService service;
    private final List<BluetoothGattCharacteristic> characteristics = new LinkedList<>();


    public BleWriteService(BluetoothGattService service) {
        this.service = service;
    }

    public BluetoothGattService getService() {
        return service;
    }

    public List<BluetoothGattCharacteristic> getCharacteristics() {
        return characteristics;
    }
}
