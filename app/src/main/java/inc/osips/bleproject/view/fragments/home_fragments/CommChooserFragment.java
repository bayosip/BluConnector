package inc.osips.bleproject.view.fragments.home_fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import at.markushi.ui.CircleButton;
import inc.osips.bleproject.R;
import inc.osips.bleproject.utilities.Constants;
import inc.osips.bleproject.utilities.GeneralUtil;

public class CommChooserFragment extends BaseFragment {

    public static CommChooserFragment getInstance(){
        return new CommChooserFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_comms_chooser, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initiateWidget(view);
    }

    public void initiateWidget(View v){
        CircleButton selectWIFI, selectBLE;
        selectBLE = v.findViewById(R.id.buttonSelectBLE);
        selectWIFI = v.findViewById(R.id.buttonSelectWIFI);

        selectWIFI.setOnClickListener(listener);
        selectBLE.setOnClickListener(listener);
    }

    private Button.OnClickListener listener =  new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String commsType = null;
            Intent intent = new Intent(activity, DeviceScannerFragment.class);
            switch (view.getId()){
                case R.id.buttonSelectBLE:
                    commsType = Constants.BLE;
                    break;
                case R.id.buttonSelectWIFI:
                    commsType = Constants.WIFI;
                    break;
            }
            activity.changeFragmentToScannerFrag(commsType);
        }
    };
}
