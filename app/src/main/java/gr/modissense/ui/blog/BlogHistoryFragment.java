package gr.modissense.ui.blog;


import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.squareup.otto.Bus;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import gr.modissense.ModisSenseApplication;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.R;
import gr.modissense.authenticator.LogoutService;
import gr.modissense.core.Blogs;
import gr.modissense.core.Poi;
import gr.modissense.core.VisitItem;
import gr.modissense.ui.DialogMapFragment;
import gr.modissense.ui.view.CreateOrUpdateCallback;
import gr.modissense.ui.view.ItemListFragment;
import gr.modissense.ui.view.ThrowableLoader;

public class BlogHistoryFragment extends ItemListFragment<Blogs> implements CreateOrUpdateCallback{

    @Inject
    protected ModisSenseServiceProvider serviceProvider;
    @Inject
    protected LogoutService logoutService;
    @Inject
    protected Bus BUS;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModisSenseApplication.getInstance().inject(this);
        forceRefresh();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        BUS.unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BUS.register(this);
        forceRefresh();
//        if (ModisSenseApplication.getInstance().getBlogSearchParams() != null) {
//            if (!ModisSenseApplication.getInstance().getBlogSearchParams().equals(searchParams)) {
//                forceRefresh();
//            }
//        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_blogs);


    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);

        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);

        getListAdapter()
                .addHeader(activity.getLayoutInflater()
                        .inflate(R.layout.blog_history_item_labels, null));
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
    public Loader<List<Blogs>> onCreateLoader(int id, Bundle args) {
        final List<Blogs> initialItems = items;
        return new ThrowableLoader<List<Blogs>>(getActivity(), items) {

            @Override
            public List<Blogs> loadData() throws Exception {
                try {
                    if (getActivity() != null) {
                        List<Blogs> blogs = serviceProvider.getService(getActivity()).getUserBlogs();
                        if (blogs != null && blogs.size() > 0) {
                            Collections.sort(blogs);
                            return blogs;
                        }
                    }
                    return Collections.emptyList();

                } catch (OperationCanceledException e) {
                    Activity activity = getActivity();
                    if (activity != null)
                        activity.finish();
                    return initialItems;
                }
            }
        };
    }

    @Override
    protected SingleTypeAdapter<Blogs> createAdapter(List<Blogs> items) {
        return new BlogHistoryAdapter(getActivity().getLayoutInflater(), items);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        startActivity(new Intent(getActivity(), BlogDetailsActivity.class).putExtra("date", ((Blogs) l.getItemAtPosition(position)).getDate()));
        //startActivityForResult(new Intent(getActivity(), ModiBlogViewActivity.class).putExtra(ModiBlogViewActivity.VIEW_PARAMS, (BlogItem) l.getItemAtPosition(position)), 1001);
    }

    @Override
    protected boolean onListItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        /*BlogItem poi = (BlogItem) parent.getItemAtPosition(position);
        ModiBlogEditDialog addDialog = new ModiBlogEditDialog(poi);
        ModisSenseApplication.getInstance().inject(addDialog);
        addDialog.show(getSherlockActivity().getSupportFragmentManager(), "dlg_edit_poi");*/
        return true;
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.error_loading_blogs;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED)
            return;
        forceRefresh();
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(R.menu.bloghistory, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isUsable())
            return false;
        switch (item.getItemId()) {
            case R.id.add:
                addItem();
                return true;
            case R.id.refresh:
                forceRefresh();
                return true;
            case R.id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    DialogMapFragment mapFragment;
    private void addItem() {
        mapFragment = new DialogMapFragment();
        mapFragment.init((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE));
        mapFragment.setCallback(this);
        mapFragment.show(getFragmentManager(), "MAP");

    }



    @Override
    public void onCreateOrUpdateData(Poi data) {
        System.out.println("********************Received : "+data);
        mapFragment.dismiss();
        mapFragment = null;
        VisitItem item = new VisitItem();
        item.setPoiId(data.getId());
        item.setDate((new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
        item.setSeqNum(1000);
        VisitAddDialog visitAddDialog = new VisitAddDialog();
        visitAddDialog.init(data, item,new CreateOrUpdateCallback() {
            @Override
            public void onCreateOrUpdateData(Poi data) {
                forceRefresh();
            }
        }, null);
        ModisSenseApplication.getInstance().inject(visitAddDialog);
        visitAddDialog.show(getActivity().getSupportFragmentManager(), "dlg_add_visit");
    }

}
