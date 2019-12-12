package inc.osips.bleproject.view.fragments.control_fragments;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

import inc.osips.bleproject.utilities.Constants;

public class ControlAdapter extends FragmentStatePagerAdapter {

    private static String[] pageTitles;
    private List<Fragment> fragList;

    static {
        pageTitles = new String[]{Constants.MANUAL_CTRL, Constants.VOICE_CTRL};
    }

    public ControlAdapter(FragmentManager fm, Context context, List<Fragment> fragList) {
        super(fm);
        this.fragList = fragList;
    }

    @Override
    public int getCount() {
        return  fragList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles[position];
    }

    @Override
    public Fragment getItem(int position) {
        return fragList.get(position);
    }
}
