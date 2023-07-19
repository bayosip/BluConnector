package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.ble_comms;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import inc.osips.iot_wireless_communication.wireless_comms_module.interfaces.WirelessDeviceConnectionScanner;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Constants;
import inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility.Util;

public class BLE_Scanner extends ScanCallback implements WirelessDeviceConnectionScanner {

    private long SCAN_TIME = 6000; //default scan time
    private final BluetoothAdapter bleAdapter;
    private final BluetoothLeScanner bluetoothLeScanner;
    private final Context context;

    private final List<ScanFilter> filters=new ArrayList<>();//filters for specified devices
    private ScanCallback mScanCallback = this;
    private boolean scanState = false;
    private static final String TAG = "BLE_Scanner";
    private final List<String> deviceAddresses= new ArrayList<>();
    //private
    private final ScanSettings settings;
    private ParcelUuid uuidParcel = null;
    //UUID uuid;

    public BLE_Scanner(@NonNull Context context, @Nullable ScanCallback mScanCallback,
                       @NonNull String baseUUID, long scanTime){
        if (scanTime >= 0)SCAN_TIME = scanTime;

        this.context = context;
        if (mScanCallback != null)
            this.mScanCallback = mScanCallback;
        final BluetoothManager manager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);

        bleAdapter = manager.getAdapter();
        if (!TextUtils.isEmpty(baseUUID))
            uuidParcel = new ParcelUuid(UUID.fromString(baseUUID));

        bluetoothLeScanner = bleAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder()
                .setReportDelay(SCAN_TIME)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
    }

    @Override
    public boolean isScanning() {
        return scanState;
    }

    /*
    * Start scanning for BLE devices, after checking the device is Bluetooth is on
    * */
    @Override
    public void onStart(@Nullable String deviceName) {
        if (!HW_Compatibility_Checker.checkBluetooth(bleAdapter)) {
            context.sendBroadcast(new Intent(Constants.BLE_ACTION_TURN_ON_BLUETOOTH));
//            HW_Compatibility_Checker.requestUserBluetooth(context);
        }

        if (!checkNecessaryPermissions()) {
            Util.message(context, "Please allow Bluetooth permission to all scanning for devices");
            return;
        }
        if (uuidParcel== null){
            scanForAllBLEDevices();
        }
        else scanForSpecificBLEDevices(deviceName);
    }

    @Override
    public void onStop() {
        if (scanState) {
            scanStop();
            deviceAddresses.clear();
        }
    }


    private boolean checkNecessaryPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        return (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                ( ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED);
        else return (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    /*
    * Scan for BLE with a specific uuid
    * */
    private void scanForSpecificBLEDevices(String deviceName) {
        if (!scanState) {
            ScanFilter myDevice = new ScanFilter.Builder()
                    .setServiceUuid(uuidParcel).build();
            Log.d(TAG, uuidParcel.toString());

            if (myDevice !=null){
                filters.add(myDevice);
            }
            else {
                myDevice = new ScanFilter.Builder()
                        .setDeviceName(deviceName).build();
                filters.add(myDevice);
            }
            //start scan for scan_time, then stop
            new Handler(Looper.getMainLooper()).postDelayed(this::scanStop, SCAN_TIME);

            scanState = true;
           if(checkNecessaryPermissions()) bluetoothLeScanner.startScan(filters, settings, mScanCallback);
        }
        else{
            scanStop();
        }
    }

    /*
    * Scan for ALL BLE devices
    * */
    private void scanForAllBLEDevices(){
        if (!scanState){
            //start scan for scan_time, then stop
            Util.getHandler().postDelayed(this::scanStop, SCAN_TIME);

            scanState = true;
            if(checkNecessaryPermissions())
                bluetoothLeScanner.startScan(null, settings, mScanCallback);
        }
        else{
            scanStop();
        }
    }

    //
    private void scanStop() {
        Util.message(context,"Scanning Stopped!");

        if (scanState) {
            scanState = false;
            deviceAddresses.clear();
            Log.w(TAG, "scanning stopped");
            if(bluetoothLeScanner != null && checkNecessaryPermissions())
                bluetoothLeScanner.stopScan(mScanCallback);

            context.sendBroadcast(new Intent(WirelessDeviceConnectionScanner.SCANNING_STOPPED));
        }
    }

    @Override
    public void showDiscoveredDevices() {}

    @Override
    public void onScanResult(int callbackType, final ScanResult result) {
        Log.i("callbackType", String.valueOf(callbackType));
        Log.i("result", result.toString());
        final int RSSI = result.getRssi();
        BluetoothDevice ble = result.getDevice();
        if (RSSI>=-105 && !deviceAddresses.contains(ble.getAddress())) {
            context.sendBroadcast(new Intent(WirelessDeviceConnectionScanner.DEVICE_DISCOVERED)
                    .putExtra(Constants.DEVICE_DATA, ble));
            deviceAddresses.add(ble.getAddress());
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult sr : results) {
            Log.w("ScanResult - Results", sr.toString());
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e("Scan Failed", "Error Code: " + errorCode);

        if (errorCode == ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED){
            Util.message(context, "Please Disable And Re-enable Bluetooth, or Restart Device");
        }
    }


}
