package inc.osips.bleproject.interfaces;

/**
 * Created by BABY v2.0 on 1/4/2017.
 */

public interface VoiceRecognition {

    public void speechInputCall();
    public void processInstructions(String command);
    public void restartListeningService(); // This will be executed after a voice command was
                                          // processed to keep the recognition service activated
    public void stopListening();//Stop listening for voice comands
}
