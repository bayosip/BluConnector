package inc.osips.bleproject.view.fragments.home_fragments;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import inc.osips.bleproject.view.activities.Home;

public class BaseFragment extends Fragment {

    protected Home activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Home) context;
    }
}
