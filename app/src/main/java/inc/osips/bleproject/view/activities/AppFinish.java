package inc.osips.bleproject.view.activities;

import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import inc.osips.bleproject.App;
import inc.osips.bleproject.R;
import inc.osips.bleproject.utilities.ServiceUtil;

public class AppFinish extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_finish);
        if (getIntent().getExtras() != null && getIntent().getExtras()
                .getBoolean("EXIT", false)) {
            if(Build.VERSION.SDK_INT >= 21)finishAndRemoveTask();
            else finish();
        }
    }

    @Override
    protected void onDestroy() {
        if(!ServiceUtil.isAppRunning(App.context)){
            ServiceUtil.stopService(App.context);
        }
        super.onDestroy();
    }
}
