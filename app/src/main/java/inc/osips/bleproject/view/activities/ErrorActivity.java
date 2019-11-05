package inc.osips.bleproject.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import inc.osips.bleproject.R;
import inc.osips.bleproject.model.utilities.GeneralUtil;

public class ErrorActivity extends AppCompatActivity {

    Button restartApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        restartApp= findViewById(R.id.buttonRestartApp);

        restartApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GeneralUtil.transitionActivity(ErrorActivity.this, BLE_ScannerActivity.class);
            }
        });
    }
}
