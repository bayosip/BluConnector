# BluConnector
Client side IoT wireless communication using either BLE or WiFi P2P (WiFi Direct).

This library facilitate;
* Device Scanning,
* Device Connection and
* Text-based Communication

with remote devices either using WiFi P2P (WiFi Direct)or Bluetooth Low Energy (BLE), establishing client-side bi-direction communication.

The example app uses the "iot_wireless_comms" library to scan and connect to remote devices via either BLE or WiFi (depending on which button is clicked) and has buttons that send user-defined text based instructions or HEX color codes (via color Picker) to connected devices, the app also capable of sending voice commands to remote devices, leveraging speech recognition.

##Usage

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

###Scanning For Devices

To scan for nearby BLE slave devices or other WiFi Devices, call DeviceScannerFactory with an Activity so it can check if your device is Bluetooth compatible or the Bluetooth is in fact is on on your device. 
then call the builder of your desired valid communication protocol, using the `Constants`:

```
DeviceScannerFactory factory = DeviceScannerFactory.withActivity(viewInterface.getCurrentActivity());
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

####BLE
```
	builder.setmScanCallback(callBack)
		.setScanTime(10000)//time in microseconds
```
to only scan for cetain BLE device with a known UUID `builder.setDeviceUniqueID("xxxxx-xxxx-xxxx-xxxx-xxx")`

####WiFi
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


Depending on what type of device one is scanning for we listen to the intent broadcast from the scanner object,
the general intent actions to listen for are `WirelessDeviceConnectionScanner.DEVICE_DISCOVERED` and `WirelessDeviceConnectionScanner.SCANNING_STOPPED`.

For WiFi device scanning, there are additional intent actions that are broadcasted by Android including;
*`WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION` to check the state of WiFi setting on android device (On/Off)
*`WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION` when the list of wifi devices available changes.