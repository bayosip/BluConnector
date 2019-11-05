package inc.osips.bleproject.view.custom_views;

import android.content.Context;
import android.widget.TextView;

import com.skydoves.colorpickerview.AlphaTileView;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.flag.FlagView;

import inc.osips.bleproject.R;

public class CustomColorFlag extends FlagView {

    private TextView textView;
    private AlphaTileView alphaTileView;

    public CustomColorFlag(Context context, int layout) {
        super(context, layout);
        textView = findViewById(R.id.flag_color_code);
        alphaTileView = findViewById(R.id.flag_color_layout);
    }

    @Override
    public void onRefresh(ColorEnvelope colorEnvelope) {
        textView.setText("#" + colorEnvelope.getHexCode().substring(2));
        alphaTileView.setPaintColor(colorEnvelope.getColor());
    }
}
