[![](https://jitpack.io/v/bayosip/BluConnector.svg)](https://jitpack.io/#bayosip/BluConnector)

# BluConnector
Client side IoT wireless communication using either BLE or WiFi P2P (WiFi Direct).

This library facilitate;
* Device Scanning,
* Device Connection and
* Text-based Communication

with remote devices either using WiFi P2P (WiFi Direct)or Bluetooth Low Energy (BLE), establishing client-side bi-direction communication.

The example app uses the "iot_wireless_comms" library to scan and connect to remote devices via either BLE or WiFi (depending on which button is clicked) and has buttons that send user-defined text based instructions or HEX color codes (via color Picker) to connected devices, the app also capable of sending voice commands to remote devices, leveraging speech recognition.

## Usage

### Dependency

Add `jitpack.io` to your root `build.gradle` at the end of repositories:

``` 
allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }

```

Include the library in your `build.gradle`

```
dependencies {
            implementation 'com.github.bayosip:BluConnector:Tag'
    }
```

### Scanning For Devices

To scan for nearby BLE slave devices or other WiFi Devices, call DeviceScannerFactory with an Activity so it can check if your device is Bluetooth compatible or the Bluetooth is in fact is on on your device. 
then call the builder of your desired valid communication protocol, using the `Constants`:

```
DeviceScannerFactory factory = DeviceScannerFactory.withActivity(activity);
DeviceScannerFactory.Builder builder = null;
        try { 
            builder = factory.getRemoteDeviceBuilderScannerOfType(Constants.BLE);// Bluetooth Low Energy
            //or
            builder = factory.getRemoteDeviceBuilderScannerOfType(Constants.P2P);// Wifi Peer2Peer/Wifi Direct
        } catch (IoTCommException e) {
            Log.e(TAG, "ScannerPresenter: ", e);
            e.printStackTrace();
        }
```
The library has a default `ScanCallback` class for scanning for BLE devices, and for WiFi devices a default`WifiP2pManager.ActionListener` & `WifiP2pManager.PeerListListener`, (only a custom `PeerListListener` can be used)  with a default scan time of 6 seconds, although these can be changed by the user with the builder:

#### BLE
```
    builder.setmScanCallback(callBack)
        .setScanTime(10000)//time in microseconds
```
to only scan for cetain BLE device with a known UUID `builder.setDeviceUniqueID("xxxxx-xxxx-xxxx-xxxx-xxx")`

#### WiFi
```
builder.setmP2PPeerListListener(p2pListener)
                .setScanTime(10000)//time in ms
```
Also to only scan for a WiFi device with a known IP address `builder.setDeviceUniqueID("xxx.xxx.xxx.xxx")`
then call `build()` method to build either the BLE or WiFi Device scanner:

`WirelessDeviceConnectionScanner scanner = builder.build()`

*To start scanning for devices call `scanner.onStart()`
*To stop scanning for devices call `scanner.onStop()`
*To check if scanner is still scanning for devices call `scanner.isScanning` it returns a boolean value.


Depending on what type of device one is scanning for, we listen to the intent broadcast from the scanner object,
the general intent actions to listen for are `WirelessDeviceConnectionScanner.DEVICE_DISCOVERED` and `WirelessDeviceConnectionScanner.SCANNING_STOPPED`.

For WiFi device scanning, there are additional intent actions that are broadcast by Android including;
*`WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION` to check the state of WiFi setting on android device (On/Off)
*`WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION` when the list of wifi devices available changes.

when `WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION` action broadcast is received, simply call `scanner.showDiscoveredDevices()`,
in order to request and get the list of wifi p2p enabled devices discovered. then call `scanner.onStop()` to stop scanning.

An example of how your broadcast receiver will look like is:

```
private final BroadcastReceiver commsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                switch (action) {
                    case WirelessDeviceConnectionScanner.SCANNING_STOPPED:
                        //Do something
                        break;
                    case WirelessDeviceConnectionScanner.DEVICE_DISCOVERED:
                        Parcelable device = intent.getParcelableExtra(Constants.DEVICE_DATA);
                        if(device instanceof BluetoothDevice){
                            BluetoothDevice ble = (BluetoothDevice)device;
                            //Do something
                        }else if (device instanceof WifiP2pDevice){
                            WifiP2pDevice p2pDevice = (WifiP2pDevice)device;
                            // Do something
                        }
                        break;
                    case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                        switch (state){
                            case WifiP2pManager.WIFI_P2P_STATE_ENABLED:
                                //Wifi is enabled
                                break;
                            case WifiP2pManager.WIFI_P2P_STATE_DISABLED:
                                //Wifi has been turned off
                                break;
                             default:
                                 break;
                        }
                        break;
                    case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                        if (scanner !=null) {
                            scanner.showDiscoveredDevices();
                            scanner.onStop();
                        }
                        break;
                }
            }
        }
```
### Connecting to Devices

Similarly, to connect to either BLE or WiFi devices, there is a factory to create a connection object;

`DeviceConnectionFactory factory = DeviceConnectionFactory.withContext(context)`

This factory is responsible for creating builders for the different types of connections (BLE & P2P), builders are based on
selected connection types.

```
DeviceConnectionFactory.Builder builder = factory.getDeviceConnectionBuilder(type, device);//Bluetooth Low Energy, Peer2Peer device object
buildr.build();
the builder requires as arguement the first device data (for BLE especially) to connect to.
```
Other builder options available include:
* `builder.setDeviceUniqueID(String UUID_IP);` this function take a value that is either a Service
UUID of a BLE device to be connected or an IP address to have a P2P connection with.
* `builder.setConnectionTimeOut(int timeOut);` this option is for WiFi P2P connection attempt, one can set the maximum timeout 
for a connection attempt (in milliseconds), the default time is 3000ms.

This library has two bound service classes, one for each type connection. These services are responsible for managing the connection
to devices and also communication with connected devices.
The service class are:
* `BleGattService.class` for BLE communication
* `P2pDataTransferService.class` for WiFi P2p Communication.

To connect to either of these bound service:
```try {
            DeviceConnectionFactory.Builder builder = factory.getDeviceConnectionBuilder(type, device);
            if (device instanceof BluetoothDevice && type.equalsIgnoreCase(Constants.BLE)) {
                Log.i("Connection type", builder.getDeviceType());
                //this.deviceAddr = ((BluetoothDevice) device).getAddress();
                this.deviceName = device.getName();//requires permision BLUETOOTH_CONNECT
                intent = new Intent(activity, BleGattService.class);
                deviceConnector = builder.build();
                bindBleService();
            } else if (device instanceof WifiP2pDevice && type.equalsIgnoreCase(Constants.P2P)) {
                Log.i("Connection", builder.getDeviceType());
                deviceConnector = builder.build();

                intent = new Intent(activity, P2pDataTransferService.class);
                bindBleService();
                this.deviceName = ((WifiP2pDevice) device).deviceName;
            }
        } catch (IoTCommException e) {
            Log.e(TAG, "Service Connection error", e);
            e.printStackTrace();
        }
```

For BLE, once an activity/context is bound to the BleGattService class, one can connect to more devices simultaneously by calling:
`deviceConnector.connectAnotherDeviceSimultaneously(device, serviceUUID);\\service UUID is nullable`

To receive updates from both service classes requires a registered broadcast receiver, that listens to the following actions:

```
private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Action Received");
            final String action = intent.getAction();
            assert action != null;
            String Deviceaddr = intent.getStringExtra(Constants.DEVICE_ADDRESS)
            Log.w(TAG, action);
            switch (action) {
                case Constants.BLE_ACTION_CONNECTED:
                case Constants.P2P_ACTION_CONNECTED:
                    // No need to do anything here. Service discovery is started by the service.
                    Log.i(TAG, "Connected to GATT server.");
                    break;
                case Constants.BLE_ACTION_DISCONNECTED:
                case Constants.P2P_ACTION_DISCONNECTED:
                    Log.i(TAG, "Service Disconnected");
                    break;
                case Constants.ACTION_BLE_SERVICES_DISCOVERED:
                    Log.w("BLE", "services discovered");

                    if (serviceMap.get(count) != null && !serviceMap.get(count)) {
                        listOfRemoteServices.clear();
                        listOfRemoteServices.addAll(Objects
                                .requireNonNull(intent
                                        .getStringArrayListExtra(Constants.SERVICE_UUID)));
                        viewInterface.getUUIDFromPopUp(listOfRemoteServices);
                        Log.w("BLE", "All services found : " + listOfRemoteServices.get(0));
                    }
                    break;
                case Constants.BLE_ACTION_DATA_AVAILABLE:
                    Log.w("DATA", "" + intent.getStringExtra(Constants.BLE_EXTRA_DATA));
                    // This is called after a Notify completes
                    break;
                case Constants.BLE_ACTION_RAW_DATA_AVAILABLE:
                    Log.w("DATA", "" + Arrays.toString(intent.getByteArrayExtra(Constants.BLE_EXTRA_DATA_RAW)));
                    // This is called after a Notify completes
                    break;
                case Constants.ACTION_BLE_CHARX_DATA_CHANGE:
                    Log.i("DATA", "" + intent.getStringExtra(Constants.BLE_EXTRA_DATA_RAW));
                    // This is called after a Notify completes
                    break;
                case Constants.ACTION_BLE_CHARX_DATA_CHANGE_RAW:
                    Log.i("DATA", "onReceive: ->" +
                            Arrays.toString(intent.getByteArrayExtra(inc.osips.iot_wireless_communication
                                    .wireless_comms_module.remote_comms.utility.Constants.BLE_EXTRA_DATA_RAW)));
                case Constants.P2P_ACTION_DATA_AVAILABLE:
                    String s = intent.getStringExtra(Constants.P2P_EXTRA_DATA);
                    if (s != null) {
                        Log.w("DATA", s);
                        // This is called after a Notify completes
                    }
                    break;
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    if (type.equalsIgnoreCase(Constants.P2P)) {
                        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                        switch (state) {
                            case WifiP2pManager.WIFI_P2P_STATE_ENABLED:
                                GeneralUtil.message("Wifi is Ok");
                                break;
                            case WifiP2pManager.WIFI_P2P_STATE_DISABLED:
                                Log.i("Wifi has been turned off, " +
                                        "Please turn on to use this feature");
                                break;
                        }
                    }
                    break;
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                    if (deviceConnector != null)
                        deviceConnector.connectToDeviceWithDeviceInfoFrom(intent);
                    break;

                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                    break;
                case DeviceConnectionFactory.DEVICE_CONNECTION_SERVICE_STOPPED:
                case DeviceConnectionFactory.FAILED_DEVICE_CONNECTION:
                    Log.i("DATA", "Connection Failed)
                    break;
            }
        }

        @Override
        public IBinder peekService(Context myContext, Intent service) {
            return super.peekService(myContext, service);
        }
    };
```

With BLE, when connecting to multiple devices, Use a Map to keep track of devices and their available service UUID:
`final Map deviceServiceMap = HashMap<String, List<String>>();`







