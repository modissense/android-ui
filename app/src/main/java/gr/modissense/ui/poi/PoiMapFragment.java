package gr.modissense.ui.poi;

import android.accounts.AccountsException;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import gr.modissense.ModisSenseApplication;
import gr.modissense.R;
import gr.modissense.authenticator.LogoutService;
import gr.modissense.core.MapEvent;
import gr.modissense.core.Poi;
import gr.modissense.core.PoiSearchParams;
import gr.modissense.ui.BaseMapFragment;
import gr.modissense.util.Ln;

public class PoiMapFragment extends BaseMapFragment implements SearchView.OnQueryTextListener {

    private static final String FORCE_REFRESH = "forceRefresh";
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    @Inject
    protected Bus BUS;
    @Inject
    protected LogoutService logoutService;
    private PoiSearchParams searchParams = null;//PoiSearchParams.initialSearchParams;
    private boolean initial = false;
    private SearchView mSearchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModisSenseApplication.getInstance().inject(this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        BUS.register(this);
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

    @Subscribe
    public void updateSearchParams(MapEvent poiSearchParams) {
        this.searchParams = poiSearchParams.getSearchParams();
        forceRefresh();
    }


    @Override
    public void onLoadFinished(Loader<List<Poi>> loader, List<Poi> data) {
        super.onLoadFinished(loader, data);
        setPopupTitle("Found "+data.size()+" points of interest");
    }




    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(R.menu.poi, optionsMenu);
        MenuItem searchItem = optionsMenu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        Ln.d(searchItem);
        Ln.d(searchItem.getActionView());
        setupSearchView(searchItem);
    }

    private void setupSearchView(MenuItem searchItem) {

//        if (isAlwaysExpanded()) {
//            mSearchView.setIconifiedByDefault(false);
//        } else {
            searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        //}

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();

            SearchableInfo info = searchManager.getSearchableInfo(getActivity().getComponentName());
            for (SearchableInfo inf : searchables) {
                if (inf.getSuggestAuthority() != null
                        && inf.getSuggestAuthority().startsWith("applications")) {
                    info = inf;
                }
            }
            ((SearchView) searchItem.getActionView()).setSearchableInfo(info);
        }

        ((SearchView) searchItem.getActionView()).setOnQueryTextListener(this);
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onCreateOrUpdateData(Poi data) {
        ModisSenseApplication.getInstance().getMapEvent().setSearchParams(createSearchParamsFromViewPort());
        BUS.post(ModisSenseApplication.getInstance().postMapEvent());
    }

    @Override
    public List<Poi> executeLoader() throws AccountsException, IOException {
        if (searchParams == null) {
            return Collections.emptyList();
        }
        return serviceProvider.getService(getActivity()).getPois(searchParams);
    }

    @Override
    public void onLoaderExecutionFinished() {
        ModisSenseApplication.getInstance().getMapEvent().setSearchParams(searchParams);
    }

    int getErrorMessage(Exception e) {
        return R.string.error_loading_pois;
    }

    protected PoiSearchParams createSearchParamsFromViewPort() {
        PoiSearchParams result = new PoiSearchParams();
        LatLngBounds curScreen = getMap().getProjection().getVisibleRegion().latLngBounds;

        VisibleRegion visibleRegion = getMap().getProjection().getVisibleRegion();
        LatLng l1 = visibleRegion.latLngBounds.northeast;
        LatLng l2 = visibleRegion.latLngBounds.southwest;
        double lowLat;
        double lowLng;
        double highLat;
        double highLng;

        if (visibleRegion.latLngBounds.northeast.latitude < visibleRegion.latLngBounds.southwest.latitude) {
            lowLat = visibleRegion.latLngBounds.northeast.latitude;
            highLat = visibleRegion.latLngBounds.southwest.latitude;
        } else {
            highLat = visibleRegion.latLngBounds.northeast.latitude;
            lowLat = visibleRegion.latLngBounds.southwest.latitude;
        }
        if (visibleRegion.latLngBounds.northeast.longitude < visibleRegion.latLngBounds.southwest.longitude) {
            lowLng = visibleRegion.latLngBounds.northeast.longitude;
            highLng = visibleRegion.latLngBounds.southwest.longitude;
        } else {
            highLng = visibleRegion.latLngBounds.northeast.longitude;
            lowLng = visibleRegion.latLngBounds.southwest.longitude;
        }
//        result.setLocation1(new gr.modissense.core.Location(lowLat, lowLng));
//        result.setLocation2(new gr.modissense.core.Location(highLat, highLng));
        result.setLocation1(new gr.modissense.core.Location(curScreen.northeast.latitude, curScreen.northeast.longitude));
        result.setLocation2(new gr.modissense.core.Location(curScreen.southwest.latitude, curScreen.southwest.longitude));
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isUsable())
            return false;
        switch (item.getItemId()) {
            case R.id.addpoi:
                startActivity(new Intent(getActivity(), PoiSearchActivity.class).putExtra(PoiSearchActivity.SEARCH_PARAMS, createSearchParamsFromViewPort()));
                return true;
            case R.id.refresh:
                forceRefresh();
                return true;
            default:
                return false;
        }

    }

    protected void logout() {
        logoutService.logout(new Runnable() {
            @Override
            public void run() {
                // Calling a refresh will force the service to look for a logged in user
                // and when it finds none the user will be requested to log in again.
                forceRefresh();
            }
        });
    }


    @Override
    public boolean onQueryTextSubmit(String s) {
        PoiSearchParams tempSearchParams = createSearchParamsFromViewPort();
        tempSearchParams.setKeywords(s);
        ModisSenseApplication.getInstance().getMapEvent().setSearchParams(tempSearchParams);
        BUS.post(ModisSenseApplication.getInstance().postMapEvent());
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }
}