package inc.osips.bleproject.model.ble_comms;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import inc.osips.bleproject.interfaces.PresenterInterface;
import inc.osips.bleproject.interfaces.WirelessConnectionScanner;
import inc.osips.bleproject.model.utilities.GeneralUtil;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BLE_Scanner implements WirelessConnectionScanner {

    private BluetoothAdapter bleAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothDevice bleDevice;
    private ScanSettings settings;
    private List<ScanFilter> filters;

    private boolean scanState = false;
    private String deviceName = "Osi_p BLE-LED Controller";
    private final String baseUUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";

    private ParcelUuid uuidParcel;
    private PresenterInterface pInterface;
    //UUID uuid;

    public BLE_Scanner(PresenterInterface presenter){
        pInterface = presenter;
        // dbAdapter = new DatabaseAdapter(ma.getApplicationContext());
        final BluetoothManager manager = (BluetoothManager) pInterface.getScanningActivity()
                                                        .getSystemService(Context.BLUETOOTH_SERVICE);

        bleAdapter = manager.getAdapter();
        uuidParcel = new ParcelUuid(UUID.fromString(baseUUID));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner = bleAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
        }
    }

    public boolean isScanning() {
        return scanState;
    }

    public void onStart() {
        if (!HW_Compatibility_Checker.checkBluetooth(bleAdapter)) {
            HW_Compatibility_Checker.requestUserBluetooth(pInterface);
        }
        scanForBLEDevices(true);
    }

    public void onStop() {
        if (isScanning())
            scanStop();
    }

    private void scanForBLEDevices(Boolean yes) {
        if (yes && !scanState) {
            ScanFilter myDevice = new ScanFilter.Builder()
                    .setServiceUuid(uuidParcel).build();
            Log.d("Device UUID ", uuidParcel.toString());
            filters = new ArrayList<>();
            if (myDevice !=null){
            filters.add(myDevice);
            }
            else {
                myDevice = new ScanFilter.Builder()
                        .setDeviceName(deviceName).build();
                filters.add(myDevice);
            }
            //start scan for 15s, the stop
            GeneralUtil.getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                   scanStop();
                }
            }, 15000);

            scanState = true;
            if (Build.VERSION.SDK_INT < 21) {
                bleAdapter.startLeScan(pInterface);
            } else {
                bluetoothLeScanner.startScan(filters, settings, pInterface.getScanCallBack());
            }
        }
        else{
            scanStop();

        }
    }

    private void scanStop() {
        GeneralUtil.message("Scanning Stopped!");
        if (scanState) {
                scanState = false;
            if (Build.VERSION.SDK_INT < 21) {
                bleAdapter.stopLeScan(pInterface);
            } else {
                if(bluetoothLeScanner != null)
                    bluetoothLeScanner.stopScan(pInterface.getScanCallBack());
            }
        } else return;
    }


    public BluetoothDevice getBLEDevice (){
        return this.bleDevice;
    }

    @Override
    public void showDiscoveredDevices() {

    }
}
