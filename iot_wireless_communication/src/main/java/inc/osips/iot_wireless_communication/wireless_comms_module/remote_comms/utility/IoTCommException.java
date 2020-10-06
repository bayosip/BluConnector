package inc.osips.iot_wireless_communication.wireless_comms_module.remote_comms.utility;

public class IoTCommException extends Exception {

    String connectionType;

    public IoTCommException(String message, String connectionType) {
        super(message);
        this.connectionType = connectionType;
    }

    public String getConnectionType() {
        return connectionType;
    }
}
