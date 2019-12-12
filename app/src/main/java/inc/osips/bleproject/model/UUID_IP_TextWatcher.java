package inc.osips.bleproject.model;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import inc.osips.bleproject.utilities.Constants;

public class UUID_IP_TextWatcher implements TextWatcher {

    private EditText editUuid;
    private String commType;

    public UUID_IP_TextWatcher(EditText editUuid, String commType) {
        this.editUuid = editUuid;
        this.commType = commType;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        String text = editUuid.getText().toString();
        int textLen =  text.length();
        if (commType.equalsIgnoreCase(Constants.BLE)){
            Log.i("Watcher type", Constants.BLE);
            if (!TextUtils.isEmpty(text)&& text.charAt(textLen-1)!='-'&&(textLen == 9 || textLen ==14 || textLen==19 || textLen==24)){
                editUuid.setText(new StringBuilder(text).insert(text.length()-1, "-").toString());
                editUuid.setSelection(editUuid.getText().length());
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }
}
