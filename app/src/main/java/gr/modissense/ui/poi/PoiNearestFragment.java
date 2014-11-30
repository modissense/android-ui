package gr.modissense.ui.poi;


import android.accounts.AccountsException;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import gr.modissense.ModisSenseApplication;
import gr.modissense.R;
import gr.modissense.authenticator.LogoutService;
import gr.modissense.core.MapNearestEvent;
import gr.modissense.core.Poi;
import gr.modissense.core.PoiSearchParams;
import gr.modissense.ui.BaseMapFragment;
import gr.modissense.ui.CarouselActivity;
import gr.modissense.util.Ln;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class PoiNearestFragment extends BaseMapFragment {
    private static final String FORCE_REFRESH = "forceRefresh";
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    @Inject
    protected Bus BUS;
    @Inject
    protected LogoutService logoutService;
    private PoiSearchParams searchParams = null;//PoiSearchParams.initialSearchParams;
    private MenuItem nearestMenuItem;
    private boolean initial = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModisSenseApplication.getInstance().inject(this);
        setHasOptionsMenu(true);
        initial = true;

    }

    @Override
    public void onResume() {

        super.onResume();
        BUS.register(this);
        if (isVisible() && initial) {
            initial = false;
            if (getLocationManager().getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
                LatLng latLng = new LatLng(getLocationManager().getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude(), getLocationManager().getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
                getMap().animateCamera(cameraUpdate);
                onCreateOrUpdateData(null);
                searchParams = createSearchParamsFromViewPort(latLng);
            } else {
                getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, MIN_DISTANCE, this);
                searchParams = createSearchParamsFromViewPort();
            }
        }


    }

    @Override
    protected void initializeMap() {
        super.initializeMap();
        if (searchParams == null || !searchParams.isNearest()) {
            onCreateOrUpdateData(null);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        onCreateOrUpdateData(null);
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
    public void updateSearchParams(MapNearestEvent poiSearchParams) {
        Ln.d("Receiving " + poiSearchParams);
        this.searchParams = poiSearchParams.getNearestParams();
        forceRefresh();


    }

    @Override
    public void onLoadFinished(Loader<List<Poi>> loader, List<Poi> data) {
        super.onLoadFinished(loader, data);
        if(ModisSenseApplication.getInstance().isNearest())
            setPopupTitle("Found " + data.size() + " points of interest");
        else
            setPopupTitle("Found " + data.size() + " trending points");
    }

    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(R.menu.nearest, optionsMenu);
        nearestMenuItem = optionsMenu.findItem(R.id.nearest);
        updateNearestMenu();
    }

    private void updateNearestMenu() {
        if (ModisSenseApplication.getInstance().isNearest()) {
            nearestMenuItem.setIcon(R.drawable.hot);
            ((CarouselActivity)getActivity()).getSupportActionBar().getTabAt(0).setText("Near Me");
        }
        else {
            nearestMenuItem.setIcon(R.drawable.menu_nearme);
            ((CarouselActivity)getActivity()).getSupportActionBar().getTabAt(0).setText("Trending");
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onCreateOrUpdateData(Poi data) {
        ModisSenseApplication.getInstance().getMapEventNearest().setNearestParams(createSearchParamsFromViewPort());
        BUS.post(ModisSenseApplication.getInstance().postMapNearestEvent());
    }

    @Override
    public List<Poi> executeLoader() throws AccountsException, IOException {
        //Ln.d("Loader EXECUTING ------------------------------------->" + searchParams);
        if (searchParams == null || !searchParams.isNearest()) {
            return Collections.emptyList();

        }
        if (searchParams.isNearest() && ModisSenseApplication.getInstance().isNearest()) {
            return serviceProvider.getService(getActivity()).getPois(searchParams.getNearestLat(), searchParams.getNearestLon());
        } else if (searchParams.isNearest() && !ModisSenseApplication.getInstance().isNearest()) {
            return serviceProvider.getService(getActivity()).getTrending(searchParams.getNearestLat(), searchParams.getNearestLon(), searchParams.getLocation1(), searchParams.getLocation2());
        }
        return Collections.emptyList();
    }

    @Override
    public void onLoaderExecutionFinished() {
        ModisSenseApplication.getInstance().getMapEventNearest().setNearestParams(searchParams);
    }

    int getErrorMessage(Exception e) {
        return R.string.error_loading_pois;
    }

    protected PoiSearchParams createSearchParamsFromViewPort() {
        PoiSearchParams result = new PoiSearchParams();
        result.setNearestLat(getMap().getCameraPosition().target.latitude);
        result.setNearestLon(getMap().getCameraPosition().target.longitude);
        result.setNearest(true);
        addParamsFromViewPort(result);
        return result;
    }

    protected PoiSearchParams createSearchParamsFromViewPort(LatLng bounds) {
        PoiSearchParams result = new PoiSearchParams();
        result.setNearestLat(bounds.latitude);
        result.setNearestLon(bounds.longitude);
        result.setNearest(true);
        addParamsFromViewPort(result);
        return result;
    }

    protected void addParamsFromViewPort(PoiSearchParams result) {
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isUsable())
            return false;
        switch (item.getItemId()) {
            case R.id.addpoi:
                startActivity(new Intent(getActivity(), PoiSearchActivity.class).putExtra(PoiSearchActivity.SEARCH_PARAMS, createSearchParamsFromViewPort()));
                return true;
            case R.id.nearest:
                ModisSenseApplication.getInstance().setNearest(!ModisSenseApplication.getInstance().isNearest());
                updateNearestMenu();
                forceRefresh();
                return true;
            case R.id.refresh:
                onCreateOrUpdateData(null);
                return true;
//            case R.id.logout:
//                logout();
//                return true;
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
    public int getMarkerIconStyle() {
        if(ModisSenseApplication.getInstance().isNearest()) {
            return super.getMarkerIconStyle();
        }
        return IconGenerator.STYLE_RED;
    }
}
