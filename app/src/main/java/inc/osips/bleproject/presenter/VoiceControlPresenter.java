package inc.osips.bleproject.presenter;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.SpeechRecognizer;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

import inc.osips.bleproject.interfaces.ControllerViewInterface;
import inc.osips.bleproject.model.VoiceRecognitionImpl;
import inc.osips.bleproject.model.utilities.GeneralUtil;
import inc.osips.bleproject.view.activities.ControllerActivity;

public class VoiceControlPresenter extends VoiceRecognitionImpl implements RecognitionListener {


    protected ControllerViewInterface viewInterface;
    protected ArrayList<String> instructions;
    protected static String TAG;
    protected boolean micCheck =false;
    private static final String GOOGLE_RECOGNITION_SERVICE_NAME =
            "com.google.android.googlequicksearchbox/com.google.android.voicesearch.serviceapi.GoogleRecognitionService";



    static {
        TAG = ControllerActivity.class.getSimpleName();
    }

    public VoiceControlPresenter(ControllerViewInterface viewInterface){
        this.viewInterface = viewInterface;
    }

    public void initSpeech() {
        if(ActivityCompat.checkSelfPermission(viewInterface.getControlContext(),
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            micCheck = true;
            try {

                if (!isSpeechRecognizerAvailable()) {
                    GeneralUtil.message("Cannot reach Recognizer");
                }
                else {
                    sr = SpeechRecognizer.createSpeechRecognizer(viewInterface.getControlContext(),
                            ComponentName.unflattenFromString(GOOGLE_RECOGNITION_SERVICE_NAME));
                    sr.setRecognitionListener(this);
                }
            } catch (NullPointerException e) {
                Log.i(TAG, e.getMessage());
                e.printStackTrace();
                GeneralUtil.message("Failed to Initialise Speech Module");
            }
        }else GeneralUtil.message("Speech Permission Not Granted");
    }

    private  boolean isSpeechRecognizerAvailable() {
        Boolean sIsSpeechRecognizerAvailable = null;
        if (sIsSpeechRecognizerAvailable == null) {
            boolean isRecognitionAvailable =  this.viewInterface.getControlContext().getPackageManager() != null
                    && SpeechRecognizer.isRecognitionAvailable(viewInterface.getControlContext());
            if (isRecognitionAvailable) {
                ServiceConnection connection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {

                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {

                    }
                };
                Intent serviceIntent = new Intent(RecognitionService.SERVICE_INTERFACE);
                ComponentName recognizerServiceComponent = ComponentName.unflattenFromString(GOOGLE_RECOGNITION_SERVICE_NAME);
                if (recognizerServiceComponent == null) {
                    return false;
                }

                serviceIntent.setComponent(recognizerServiceComponent);
                boolean isServiceAvailableToBind = this.viewInterface.getControlContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
                if (isServiceAvailableToBind) {
                    this.viewInterface.getControlContext().unbindService(connection);
                }
                sIsSpeechRecognizerAvailable = isServiceAvailableToBind;
            } else {
                sIsSpeechRecognizerAvailable = false;
            }
        }
        return sIsSpeechRecognizerAvailable;
    }

    //The user has started to speak.
    @Override
    public void onBeginningOfSpeech() {
        System.out.println("Starting to listen");
    }

    @Override
    public void onError(int error) {
        restartListeningService();
    }

    // This method will be executed when voice commands were found
    @Override
    public void onResults(Bundle results) {
        //micImage.setImageResource(R.drawable.ic_mic_off);
        instructions = results.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);
        String command = instructions.get(0).toLowerCase(Locale.getDefault());
        Log.i(TAG, command);
        System.out.println(command);
        processInstructions(command);
    }



    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    @Override
    public void processInstructions(String commands) {
        viewInterface.processInstructions(commands);
    }
}
