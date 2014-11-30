package gr.modissense.ui.blog;


import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.Toaster;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import gr.modissense.ModisSenseApplication;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.R;
import gr.modissense.core.BlogItem;
import gr.modissense.core.Blogs;
import gr.modissense.ui.view.ThrowableLoader;

public class BlogMapFragment extends SupportMapFragment implements LoaderManager.LoaderCallbacks<List<BlogItem>>, LocationListener {
    private static final String FORCE_REFRESH = "forceRefresh";
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    @Inject
    protected ModisSenseServiceProvider serviceProvider;
    @Inject
    protected Bus BUS;
    protected List<BlogItem> items = Collections.emptyList();
    protected Map<Marker, BlogItem> markerToBlogItem = new HashMap<Marker, BlogItem>();
    private String searchParams = "2013-10-02";
    private LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModisSenseApplication.getInstance().inject(this);
        setHasOptionsMenu(true);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);


    }

    @Override
    public void onResume() {
        super.onResume();
        BUS.register(this);
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(getActivity());
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            initializeMap();
        } else {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.

    }

    @Override
    public void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        BUS.unregister(this);
    }

    @Subscribe
    public void updateSearchParams(String poiSearchParams) {
        this.searchParams = poiSearchParams;
        forceRefresh();

    }

    //    @Override
//    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
//        inflater.inflate(R.menu.poi, optionsMenu);
//    }
    @Override
    public void onCreateOptionsMenu(Menu optionsMenu, MenuInflater inflater) {
        inflater.inflate(R.menu.blogmap, optionsMenu);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    private void initializeMap() {
        getLoaderManager().initLoader(0, null, this);
        getMap().setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker marker) {

                // Getting view from the layout file info_window_layout
                View v = getActivity().getLayoutInflater().inflate(R.layout.poi_view, null);

                TextView name = (TextView) v.findViewById(R.id.poi_name);
                TextView description = (TextView) v.findViewById(R.id.poi_description);


                TextView interst = (TextView) v.findViewById(R.id.poi_interest);





                final BlogItem poi = markerToBlogItem.get(marker);
                name.setText(poi.getName());
                description.setText(poi.getComment());
                interst.setVisibility(View.GONE);//setText("Interest: " + poi.getInterest());
                v.findViewById(R.id.ratingBar).setVisibility(View.GONE);
                // Returning the view containing InfoWindow contents
                return v;

            }

        });
        getMap().setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
//                ModiBlogItemAddDialog addDialog = new ModiBlogItemAddDialog(point);
//                ModisSenseApplication.getInstance().inject(addDialog);
//                addDialog.show(getActivity().getSupportFragmentManager(), "dlg_add_poi");

            }
        });
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
    public void onLoadFinished(Loader<List<BlogItem>> loader, List<BlogItem> data) {
        // getActivity().setSupportProgressBarIndeterminateVisibility(false);

        Exception exception = getException(loader);
        if (exception != null) {
            showError(getErrorMessage(exception));
            //showList();
            return;
        }
        this.items = data;
        getMap().clear();

        markerToBlogItem.clear();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        PolylineOptions rectOptions = new PolylineOptions();


// Get back the mutable Polygon

        for (BlogItem poi : this.items) {
            Marker option = addMarker(poi);
            builder.include(new LatLng(poi.getLat(), poi.getLon()));
            markerToBlogItem.put(option, poi);
            rectOptions.add(new LatLng(poi.getLat(),poi.getLon()));
        }
        getMap().getUiSettings().setMyLocationButtonEnabled(true);
        getMap().setMyLocationEnabled(true);
        if (this.items.size() == 0) {
            if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
                LatLng latLng = new LatLng(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude(), locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 5);
                getMap().animateCamera(cameraUpdate);
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, MIN_DISTANCE, this);
            }
        } else {
            /*if (!searchParams.isNearest()) {
                PolygonOptions rectOptions = new PolygonOptions()
                        .add(searchParams.getLocation1().toLatLng(), searchParams.getLocation2().toLatLng());

// Get back the mutable Polygon
                Polygon polygon = getMap().addPolygon(rectOptions);

            }*/
            rectOptions.color(Color.BLUE);
            rectOptions.width(7);
            Polyline polygon = getMap().addPolyline(rectOptions);
           CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), 100);
           getMap().moveCamera(cu);
        }

    }

    @Override
    public void onLoaderReset(Loader<List<BlogItem>> loader) {

    }

    /**
     * Show exception in a Toast
     *
     * @param message
     */
    protected void showError(final int message) {
        Toaster.showLong(getActivity(), message);
    }

    /**
     * Get exception from loader if it provides one by being a
     * {@link ThrowableLoader}
     *
     * @param loader
     * @return exception or null if none provided
     */
    protected Exception getException(final Loader<List<BlogItem>> loader) {
        if (loader instanceof ThrowableLoader)
            return ((ThrowableLoader<List<BlogItem>>) loader).clearException();
        else
            return null;
    }

    int getErrorMessage(Exception e) {
        return R.string.error_loading_pois;
    }

    public Marker addMarker(BlogItem poi) {

        MarkerOptions marker = new MarkerOptions().position(new LatLng(poi.getLat(), poi.getLon())).title(poi.getName()).icon(BitmapDescriptorFactory.fromBitmap(new com.google.maps.android.ui.IconGenerator(getActivity()).makeIcon(String.valueOf(poi.getIndex()))));
        return getMap().addMarker(marker);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isUsable())
            return false;
        switch (item.getItemId()) {
            case R.id.list:
                ((BlogDetailsActivity) getActivity()).navigateToList();
                return true;
            case R.id.refresh:
                forceRefresh();
                return true;
            case R.id.logout:
               // logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
////        if (!isUsable())
////            return false;
//        switch (item.getItemId()) {
//            case R.id.addpoi:
//                startActivity(new Intent(getActivity(), ModiBlogItemSearchActivity.class).putExtra(ModiBlogItemSearchActivity.SEARCH_PARAMS, createSearchParamsFromViewPort()));
//                return true;
//            case R.id.nearest:
//                searchParams.setNearestLat(getMap().getCameraPosition().target.latitude);
//                searchParams.setNearestLon(getMap().getCameraPosition().target.longitude);
//                searchParams.setNearest(true);
//                ModisSenseApplication.getInstance().setBlogItemSearchParams(searchParams);
//                BUS.post(ModisSenseApplication.getInstance().postSearchParams());
//                return true;
//            default:
//                return false;
//        }
//    }

    protected boolean isUsable() {
        return getActivity() != null;
    }

//    protected void forceRefresh() {
//        Bundle bundle = new Bundle();
//        bundle.putBoolean(FORCE_REFRESH, true);
//        refresh(bundle);
//    }

    private void refresh(final Bundle args) {
        if (!isUsable())
            return;

        //getActivity().setSupportProgressBarIndeterminateVisibility(true);

        getLoaderManager().restartLoader(0, args, this);
    }

    @Override
    public void onLocationChanged(Location location) {
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
//        getMap().animateCamera(cameraUpdate);
//
//        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    protected void forceRefresh() {
        ((BlogDetailsActivity) getActivity()).forceRefresh();
    }

    @Subscribe
    public void onBlogRefreshed(Blogs theBlog){
        if (!isUsable())
            return;
        Bundle bundle = new Bundle();
        bundle.putBoolean(FORCE_REFRESH, true);
//        getActionBarActivity().setSupportProgressBarIndeterminateVisibility(true);
        getLoaderManager().restartLoader(0, bundle, this);
    }

}
