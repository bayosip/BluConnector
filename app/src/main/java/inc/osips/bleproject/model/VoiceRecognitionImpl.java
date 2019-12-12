package inc.osips.bleproject.model;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.Locale;
import inc.osips.bleproject.interfaces.VoiceRecognition;
import inc.osips.bleproject.utilities.GeneralUtil;

public abstract class VoiceRecognitionImpl  implements VoiceRecognition{

    String TAG = VoiceRecognitionImpl.class.getSimpleName();
    private Intent voiceIntent;
    protected SpeechRecognizer sr;

    @Override
    public void speechInputCall(){
        try {
            voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            if(voiceIntent!=null)
            sr.startListening(voiceIntent);
            else GeneralUtil.message( "cannot start recognizer");
        } catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, e.toString());
            GeneralUtil.message( "Speech recognition not available");
        }
    }


    @Override
    public void stopListening() {
        if (sr != null) {
            sr.stopListening();
            sr.cancel();
            sr.destroy();
        }
        sr = null;
    }


    @Override
    public abstract void processInstructions(String Commands);

    @Override
    public void restartListeningService() {
        stopListening();
        speechInputCall();
    }
}
