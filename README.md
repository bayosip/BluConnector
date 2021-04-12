# BluConnector
Client side IoT wireless communication using either BLE or WiFi P2P (WiFi Direct).

This library facilitate;
* Device Scanning,
* Device Connection and
* Text-based Communication
 
with remote devices either using WiFi P2P (WiFi Direct)or Bluetooth Low Energy (BLE), establishing client-side bi-direction communication.

The example app uses the "iot_wireless_comms" library to scan and connect to remote devices via either BLE or WiFi (depending on which button is clicked) and has buttons that send user-defined text based instructions or HEX color codes (via color Picker) to connected devices, the app also capable of sending voice commands to remote devices, leveraging speech recognition.
