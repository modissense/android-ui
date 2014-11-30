

package gr.modissense.ui;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import gr.modissense.ModisSenseApplication;
import gr.modissense.R;
import gr.modissense.ui.account.AccountFragment;
import gr.modissense.ui.blog.BlogHistoryFragment;
import gr.modissense.ui.gps.GPSFragment;
import gr.modissense.ui.poi.PoiMapFragment;
import gr.modissense.ui.poi.PoiNearestFragment;

/**
 * Pager adapter
 */
public class ModisSensePagerAdapter extends FragmentPagerAdapter {

    private final Resources resources;

    /**
     * Create pager adapter
     *
     * @param resources
     * @param fragmentManager
     */
    public ModisSensePagerAdapter(Resources resources, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.resources = resources;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        switch (position) {
            case 0:
                PoiNearestFragment nearestFragment = new PoiNearestFragment();
                nearestFragment.setArguments(bundle);
                return nearestFragment;
            case 1:
                PoiMapFragment mapFragment = new PoiMapFragment();
                mapFragment.setArguments(bundle);
                return mapFragment;
            case 2:
                BlogHistoryFragment historyFragment = new BlogHistoryFragment();
                historyFragment.setArguments(bundle);
                return historyFragment;
            case 3:
                AccountFragment modiAccountFragment = new AccountFragment();
                modiAccountFragment.setArguments(bundle);
                return modiAccountFragment;
            case 4:
                GPSFragment gpsFragment = new GPSFragment();
                gpsFragment.setArguments(bundle);
                return gpsFragment;
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                if(ModisSenseApplication.getInstance().isNearest()) {
                    return resources.getString(R.string.page_nearest);
                }
                return "Trending";
            case 1:
                return resources.getString(R.string.page_map);
            case 2:
                return resources.getString(R.string.page_blog);
            case 3:
                return resources.getString(R.string.page_account);
            case 4:
                return resources.getString(R.string.page_gps);
            default:
                return null;
        }
    }

    public int getPageIcon(int position) {
        switch (position) {
            case 0:
                return R.drawable.menu_nearme;
            case 1:
                return R.drawable.menu_search;
            case 2:
                return R.drawable.menu_blogs;
            case 3:
                return R.drawable.menu_profile;
            case 4:
                return R.drawable.menu_settings;
            default:
                return 0;
        }
    }
}
