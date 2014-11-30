package gr.modissense.ui.gps;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import gr.modissense.ModisSenseApplication;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.R;
import gr.modissense.authenticator.LogoutService;
import gr.modissense.core.gps.GPSLogItem;
import gr.modissense.core.gps.GPSLoggingTaskQueue;
import gr.modissense.ui.view.ItemListFragment;
import gr.modissense.ui.view.ThrowableLoader;

import javax.inject.Inject;

import java.util.Collections;
import java.util.List;

public class GPSFragment extends ItemListFragment<GPSLogItem> {

    @Inject
    protected ModisSenseServiceProvider serviceProvider;
    @Inject
    protected GPSLoggingTaskQueue queue;
    @Inject
    protected LogoutService logoutService;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModisSenseApplication.getInstance().inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_gps_traces);


    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);

        getListAdapter()
                .addHeader(activity.getLayoutInflater()
                        .inflate(R.layout.gps_list_item_labels, null));
    }

    @Override
    protected LogoutService getLogoutService() {
        return logoutService;
    }

    @Override
    public void onDestroyView() {
        setListAdapter(null);

        super.onDestroyView();
    }

    @Override
    public Loader<List<GPSLogItem>> onCreateLoader(int id, Bundle args) {
        final List<GPSLogItem> initialItems = items;
        return new ThrowableLoader<List<GPSLogItem>>(getActivity(), items) {

            @Override
            public List<GPSLogItem> loadData() throws Exception {
                if (getActivity() != null) {
                    return queue.getItemsLogged();

                } else {
                    return Collections.emptyList();
                }
            }
        };
    }

    @Override
    protected SingleTypeAdapter<GPSLogItem> createAdapter(List<GPSLogItem> items) {
        return new GPSAdapter(getActivity().getLayoutInflater(), items);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        //startActivityForResult(new Intent(getActivity(), PoiViewActivity.class).putExtra(PoiViewActivity.VIEW_PARAMS, (Poi) l.getItemAtPosition(position)), 1001);
    }

//    @Override
//    protected boolean onListItemLongClick(AdapterView<?> parent, View view, int position, long id) {
////        Poi poi = (Poi) parent.getItemAtPosition(position);
////        PoiEditDialog addDialog = new PoiEditDialog(poi);
////        ModisSenseApplication.getInstance().inject(addDialog);
////        addDialog.show(getSherlockActivity().getSupportFragmentManager(), "dlg_edit_poi");
//        return true;
//    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_loading_pois;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED)
            return;
        forceRefresh();
    }
}
