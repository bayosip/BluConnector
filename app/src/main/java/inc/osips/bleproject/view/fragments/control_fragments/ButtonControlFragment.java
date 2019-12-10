package inc.osips.bleproject.view.fragments.control_fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import inc.osips.bleproject.interfaces.ControlFragmentListener;
import inc.osips.bleproject.R;
import inc.osips.bleproject.model.utilities.GeneralUtil;
import inc.osips.bleproject.view.activities.ControllerActivity;
import inc.osips.bleproject.view.custom_views.CustomColorFlag;

public class ButtonControlFragment extends Fragment {

    private ImageButton colorWheel, buttonOnOff, buttonDark, buttonBright, buttonBack, buttonNext;
    private ControlFragmentListener fragListner;
    private Context context;
    private String instruct;

    private static final String TAG = ControllerActivity.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manual_control, container, false);
        initialiseWidget(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.context = context;
            fragListner = (ControlFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString());
        }

    }

    private void createColorPickerDialog() {
        ColorPickerDialog.Builder colorDialog = new ColorPickerDialog.Builder(this.context, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("BLE ColorPicker")
                .setPreferenceName("BLEColorPickerDialog")
                .setPositiveButton(getString(R.string.confirm),
                        new ColorEnvelopeListener() {
                            @Override
                            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                instruct = envelope.getHexCode();
                                Log.i(TAG, instruct);
                                String hexCode = "#" + envelope.getHexCode().substring(2);
                                fragListner.sendInstructions(hexCode);
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                .setView(R.layout.alert_view)
                .attachAlphaSlideBar(false) // default is true. If false, do not show the AlphaSlideBar.
                .attachBrightnessSlideBar(true); // default is true. If false, do not show the BrightnessSlideBar.

        ColorPickerView colorPickerView = colorDialog.getColorPickerView();
        colorPickerView.setFlagView(new CustomColorFlag(this.context, R.layout.colour_flag_layout));
        colorDialog.show();
    }

    private void initialiseWidget(View v) {
        colorWheel = v.findViewById(R.id.buttonColorWheel);
        colorWheel.setOnClickListener(OnClick);
        buttonOnOff = v.findViewById(R.id.buttonOnoff);
        buttonOnOff.setOnClickListener(OnClick);
        buttonDark = v.findViewById(R.id.buttonDark);
        buttonDark.setOnClickListener(OnClick);
        buttonBack = v.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(OnClick);
        buttonBright = v.findViewById(R.id.buttonBright);
        buttonBright.setOnClickListener(OnClick);
        buttonNext = v.findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(OnClick);

    }

    Button.OnClickListener OnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            instruct = "";
            switch (v.getId()) {
                case R.id.buttonColorWheel:
                    fragListner.getFragmentactivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                createColorPickerDialog();
                            }catch (Exception e){
                                GeneralUtil.message("Color Picker Unavailable");
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                default:
                    switch (v.getId()) {
                        case R.id.buttonOnoff:
                            instruct = "On/Off";
                            break;
                        case R.id.buttonDark:
                            instruct = "Dim";
                            break;
                        case R.id.buttonBright:
                            instruct = "Bright";
                            break;
                        case R.id.buttonBack:
                            instruct = "Back";
                            break;
                        case R.id.buttonNext:
                            instruct = "Next";
                            break;
                    }
                    fragListner.sendInstructions(instruct.toLowerCase());
            }
        }
    };
}
