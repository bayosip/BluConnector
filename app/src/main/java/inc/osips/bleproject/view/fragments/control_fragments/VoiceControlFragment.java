package inc.osips.bleproject.view.fragments.control_fragments;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import inc.osips.bleproject.interfaces.ControlFragmentListener;
import inc.osips.bleproject.interfaces.SpeechInitCallBack;
import inc.osips.bleproject.R;
import inc.osips.bleproject.utilities.GeneralUtil;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

//public class VoiceControlFragment extends Fragment implements VCPopUpWindow {
public class VoiceControlFragment extends Fragment implements SpeechInitCallBack {
    private ImageButton buttonSpeak;
    private TextView interpretedText;
    private ControlFragmentListener fragListner;
    private PopupWindow vcPopUp;
    private LayoutInflater layoutInflater;
    private ImageView micImage;
    private static View containerView;
    private ViewGroup vcContainer;
    private static final String TAG = "VoiceControl";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragListner = (ControlFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.voice_control, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initialiseWidgets(view);
        containerView = view;
    }

    private void initialiseWidgets(View v) {
        interpretedText = v.findViewById(R.id.textViewInterpret);
        buttonSpeak = v.findViewById(R.id.buttonSpeak);

        //Pop up intialization;
        layoutInflater = (LayoutInflater) getContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        vcContainer = (ViewGroup) layoutInflater.inflate
                (R.layout.voice_input_pop_up, null);
        vcPopUp = new PopupWindow(vcContainer, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        micImage = vcContainer.findViewById(R.id.imageViewMic);

        buttonSpeak.setOnClickListener(v1 -> {
            fragListner.startListening();
            accessSpeechToText();
        });
    }

    private void accessSpeechToText() {
        Dexter.withActivity(requireActivity())
                .withPermission(Manifest.permission.RECORD_AUDIO)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        initSpeechControlFeature(GeneralUtil.isNetworkConnected(getContext()));
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        token.cancelPermissionRequest();
                    }
                }).check();
    }


    public void closePopUp(){
        if (vcPopUp !=null){
            vcPopUp.dismiss();
        }
    }

    public void showVoiceCommandAsText(String commands) {
        micImage.setImageResource(R.drawable.ic_mic_off);
        interpretedText.setText("You said: " + commands);
        vcPopUp.dismiss();
    }

    @Override
    public void initSpeechControlFeature(boolean yes) {
        if (yes) {
            try {
                vcPopUp.showAtLocation(containerView, Gravity.CENTER, 0, 0);
                // Set an elevation value for popup window
                // Call requires API level 21
                if (Build.VERSION.SDK_INT >= 21) {
                    vcPopUp.setElevation(10.0f);
                }

                micImage.setImageResource(R.drawable.ic_mic_on);
                fragListner.speechInputCall();
                vcContainer.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        vcPopUp.dismiss();
                        fragListner.stopListening();
                        return true;
                    }
                });
                // startActivityForResult(SPT.speechInputCall(), 100);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        } else GeneralUtil.message("Internet Required To Use Feature!");
    }
}
