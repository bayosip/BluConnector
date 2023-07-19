package inc.osips.bleproject.interfaces;

import android.app.Activity;

import java.util.List;

public interface ControlFragmentListener extends AppActivity {

    void sendInstructions (String instruct);//function takes string command and passes
                                                    //it to the gat service
    void speechInputCall();
    void stopListening();
    void startListening();
    void setSelectedServiceUUID ( String uuid);
    void setSelectedAddresses(List<String> addresses);
    void getDeviceAddressFromPopUp();
    Activity getFragmentActivity();
}
