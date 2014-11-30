package gr.modissense.ui.gps;

import android.view.LayoutInflater;
import gr.modissense.R;
import gr.modissense.core.gps.GPSLogItem;
import gr.modissense.ui.view.AlternatingColorListAdapter;

import java.util.Date;
import java.util.List;


public class GPSAdapter extends AlternatingColorListAdapter<GPSLogItem> {


    /**
     * @param inflater
     * @param items
     * @param selectable
     */
    public GPSAdapter(LayoutInflater inflater, List<GPSLogItem> items,
                      boolean selectable) {
        super(R.layout.providers_list, inflater, items, selectable);
    }

    /**
     * @param inflater
     * @param items
     */
    public GPSAdapter(LayoutInflater inflater, List<GPSLogItem> items) {
        super(R.layout.poi_list, inflater, items);

    }

    @Override
    protected int[] getChildViewIds() {
        return new int[]{R.id.poiName, R.id.poiXY,
                R.id.poiInterest, R.id.poiHotness, R.id.poiPublic, R.id.poiKeywords, R.id.poiDescription};
    }

    @Override
    protected void update(int position, GPSLogItem item) {
        super.update(position, item);
        setText(0, item.getProvider());
        setText(1, "");
        setText(2, "");
        setText(3, "Acc:" + item.getAccuracy());
        setText(4, "");
        setText(5, "LatLng: " + item.getLat() + " " + item.getLng());
        setText(6, String.valueOf(new Date(item.getTimestamp())));
        //setNumber(R.id.tv_date, item.getCreatedAt());
    }
}
