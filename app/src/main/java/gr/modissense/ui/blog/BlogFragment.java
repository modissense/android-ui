package gr.modissense.ui.blog;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.Toaster;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import gr.modissense.ModisSenseApplication;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.R;
import gr.modissense.authenticator.LogoutService;
import gr.modissense.core.BlogItem;
import gr.modissense.core.Blogs;
import gr.modissense.core.ModiResult;
import gr.modissense.core.Poi;
import gr.modissense.core.VisitItem;
import gr.modissense.ui.poi.PoiViewActivity;
import gr.modissense.ui.view.CreateOrUpdateCallback;
import gr.modissense.ui.DialogMapFragment;
import gr.modissense.ui.view.ItemListDnDFragment;
import gr.modissense.ui.view.ThrowableLoader;
import gr.modissense.ui.view.DraggableListView;
import gr.modissense.util.Ln;
import gr.modissense.util.SafeAsyncTask;


public class BlogFragment extends ItemListDnDFragment<BlogItem> implements CreateOrUpdateCallback {

    @Inject
    protected ModisSenseServiceProvider serviceProvider;
    @Inject
    protected LogoutService logoutService;
    @Inject
    protected Bus BUS;
    private String date = "2013-10-02";//new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    private View headerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModisSenseApplication.getInstance().inject(this);

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
//        if (ModisSenseApplication.getInstance().getBlogSearchParams() != null) {
//            if (!ModisSenseApplication.getInstance().getBlogSearchParams().equals(searchParams)) {
//                forceRefresh();
//            }
//        }
    }

    @Override
    protected void onDrop(int from, int to) {
        if (((BlogDetailsActivity) getActivity()).getTheBlog() != null) {
            ((TextView) headerView.findViewById(R.id.blog_header_date)).setText(((BlogDetailsActivity) getActivity()).getTheBlog().getDate());
            ((TextView) headerView.findViewById(R.id.blog_header_desc)).setText(((BlogDetailsActivity) getActivity()).getTheBlog().getBlog().getDescription());
        }
        final BlogItem fromItem = (BlogItem)getListAdapter().getItem(from);
        final BlogItem toItem = (BlogItem)getListAdapter().getItem(to);
        new SafeAsyncTask<Boolean>() {
            public Boolean call() throws Exception {
                ModiResult res = serviceProvider.getService(BlogFragment.this.getActivity()).reorderVisitItem(((BlogDetailsActivity) getActivity()).getTheBlog().getDate(),fromItem.getNo(),toItem.getNo(),fromItem.getComment());
                Ln.d("Result is :" + res);
                if (res == null)
                    return false;
                return res.isResult();
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                Throwable cause = e.getCause() != null ? e.getCause() : e;

                String message = cause.getMessage();

                Toaster.showLong(getActivity(), message);
            }

            @Override
            public void onSuccess(Boolean success) {
                System.out.println("RESULT: "+success);
                if (success) {
                    Toaster.showLong(getActivity(), "POI Added");
                    BlogFragment.this.forceRefresh();
                } else {
                    Toaster.showLong(getActivity(), "Unable to add POI");
                }

            }

            @Override
            protected void onFinally() throws RuntimeException {

            }
        }.execute();
        //forceRefresh();
    }

    @Override
    protected void onRemove(int which) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(R.string.no_blogs);


    }

    @Override
    protected void configureList(Activity activity, DraggableListView listView) {
        super.configureList(activity, listView);

        listView.setFastScrollEnabled(true);
        listView.setDividerHeight(0);
        headerView = activity.getLayoutInflater()
                .inflate(R.layout.blog_list_item_labels, null);
        getListAdapter()
                .addHeader(headerView);
    }

    @Override
    public void onLoadFinished(Loader<List<BlogItem>> loader, List<BlogItem> items) {
        super.onLoadFinished(loader, items);
        if (((BlogDetailsActivity) getActivity()).getTheBlog() != null) {
            ((TextView) headerView.findViewById(R.id.blog_header_date)).setText(((BlogDetailsActivity) getActivity()).getTheBlog().getDate());
            ((TextView) headerView.findViewById(R.id.blog_header_desc)).setText(((BlogDetailsActivity) getActivity()).getTheBlog().getBlog().getDescription());
        }
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
    public Loader<List<BlogItem>> onCreateLoader(int id, Bundle args) {
        final List<BlogItem> initialItems = items;
        return new ThrowableLoader<List<BlogItem>>(getActivity(), items) {

            @Override
            public List<BlogItem> loadData() throws Exception {
                if (getActivity() != null && ((BlogDetailsActivity) getActivity()).getTheBlog() !=null) {
                    return ((BlogDetailsActivity) getActivity()).getTheBlog().getBlog().toItems(((BlogDetailsActivity) getActivity()).getTheBlog().getDate());
                }
                return Collections.emptyList();
            }
        };
    }

    @Override
    protected SingleTypeAdapter<BlogItem> createAdapter(List<BlogItem> items) {
        return new BlogAdapter(getActivity().getLayoutInflater(), items);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        BlogItem selected = (BlogItem) l.getItemAtPosition(position);
        VisitUpdateDialog visitAddDialog = new VisitUpdateDialog();
        visitAddDialog.init(selected.toVisitItem(),new CreateOrUpdateCallback() {
            @Override
            public void onCreateOrUpdateData(Poi data) {
                forceRefresh();
            }
        }, ((BlogDetailsActivity) getActivity()).getTheBlog().getBlog());
        ModisSenseApplication.getInstance().inject(visitAddDialog);
        visitAddDialog.show(getActivity().getSupportFragmentManager(), "dlg_edit_visit");
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
        return R.string.error_loading_pois;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED)
            return;
        forceRefresh();
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(R.menu.blog, optionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isUsable())
            return false;
        switch (item.getItemId()) {
            case R.id.map:
                navigateToMap();
                return true;
            case R.id.add:
                addItem();
                return true;
            case R.id.refresh:
                forceRefresh();
                return true;
            case R.id.logout:
                logout();
                return true;
            case R.id.sharefb:
                new SafeAsyncTask<ModiResult>() {

                    @Override
                    public ModiResult call() throws Exception {
                        return serviceProvider.getService(getActivity()).postOnFacebook(((BlogDetailsActivity) getActivity()).getTheBlog().getBlog().getDate());
                    }

                    @Override
                    protected void onSuccess(ModiResult poiFound) throws Exception {
                        Ln.d(poiFound);
                    }
                }.execute();

//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(((BlogDetailsActivity) getActivity()).getTheBlog().getBlog().getMapLink()));
//                startActivity(browserIntent);
                return true;
            case R.id.sharetwitter:
                new SafeAsyncTask<ModiResult>() {

                    @Override
                    public ModiResult call() throws Exception {
                        return serviceProvider.getService(getActivity()).postOnTwitter(((BlogDetailsActivity) getActivity()).getTheBlog().getBlog().getDate());
                    }

                    @Override
                    protected void onSuccess(ModiResult poiFound) throws Exception {
                        Ln.d(poiFound);
                    }
                }.execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void navigateToMap() {
        ((BlogDetailsActivity) getActivity()).navigateToMap();
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
        mapFragment.dismiss();
        mapFragment = null;
        VisitItem item = new VisitItem();
        item.setPoiId(data.getId());
        item.setDate(((BlogDetailsActivity) getActivity()).getTheBlog().getDate());
        //TODO:add seq_num in the end

        int max = 1000;
        for(int i=0;i<getListAdapter().getWrappedAdapter().getCount();i++){
            BlogItem item1 =getListAdapter().getWrappedAdapter().getItem(i);
            if(item1.getNo()>=max){
                max = item1.getNo()+1000;
            }
        }
        item.setSeqNum(max);
        VisitAddDialog visitAddDialog = new VisitAddDialog();
        visitAddDialog.init(data, item,new CreateOrUpdateCallback() {
            @Override
            public void onCreateOrUpdateData(Poi data) {
                forceRefresh();
            }
        }, ((BlogDetailsActivity) getActivity()).getTheBlog().getBlog());
        ModisSenseApplication.getInstance().inject(visitAddDialog);
        visitAddDialog.show(getActivity().getSupportFragmentManager(), "dlg_add_visit");
    }

    @Override
    protected void forceRefresh() {
        ((BlogDetailsActivity) getActivity()).forceRefresh();
    }

    @Subscribe
    public void onBlogRefreshed(Blogs theBlog){
        if (!isUsable())
            return;
        Bundle bundle = new Bundle();
        bundle.putBoolean(FORCE_REFRESH, true);
        getActionBarActivity().setSupportProgressBarIndeterminateVisibility(true);

        getLoaderManager().restartLoader(0, bundle, this);
    }
}
