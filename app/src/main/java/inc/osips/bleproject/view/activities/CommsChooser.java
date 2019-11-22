package inc.osips.bleproject.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import at.markushi.ui.CircleButton;
import inc.osips.bleproject.R;
import inc.osips.bleproject.model.utilities.Constants;
import inc.osips.bleproject.model.utilities.GeneralUtil;

public class CommsChooser extends AppCompatActivity {


    private CircleButton selectWIFI, selectBLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comms_chooser);
        initiateWidget();
    }

    public void initiateWidget(){
        selectBLE = findViewById(R.id.buttonSelectBLE);
        selectWIFI = findViewById(R.id.buttonSelectWIFI);

        selectWIFI.setOnClickListener(listener);
        selectBLE.setOnClickListener(listener);
    }

    private Button.OnClickListener listener =  new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String commsType = null;
            Intent intent = new Intent(CommsChooser.this, DeviceScannerActivity.class);
            switch (view.getId()){
                case R.id.buttonSelectBLE:
                    commsType = Constants.BLE;
                    break;
                case R.id.buttonSelectWIFI:
                    commsType = Constants.WIFI;
                    break;
            }
            intent.putExtra(Constants.COMM_TYPE, commsType);
            GeneralUtil.transitionActivity(CommsChooser.this, intent);
        }
    };
}
