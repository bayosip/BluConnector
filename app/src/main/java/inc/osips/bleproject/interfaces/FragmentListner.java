package inc.osips.bleproject.interfaces;

import android.app.Activity;

public interface FragmentListner extends AppActivity {

    void sendInstructions (String instruct);//function takes string command and passes
                                                    //it to the gat service
    void speechInputCall();
    void stopListening();
    void startListening();
    Activity getFragmentactivity();
}
