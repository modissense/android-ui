package gr.modissense.ui;

import android.accounts.AccountsException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.github.kevinsawicki.wishlist.Toaster;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.util.*;

import javax.inject.Inject;

import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import gr.modissense.*;
import gr.modissense.core.Poi;
import gr.modissense.ui.poi.PoiAddDialog;
import gr.modissense.ui.poi.PoiEditDialog;
import gr.modissense.ui.poi.PoiViewActivity;
import gr.modissense.ui.view.CreateOrUpdateCallback;
import gr.modissense.ui.view.SingleChoice;
import gr.modissense.ui.view.SlidingUpPanelLayout;
import gr.modissense.ui.view.ThrowableLoader;
import gr.modissense.util.Ln;
import gr.modissense.util.SafeAsyncTask;

public abstract class BaseMapFragment extends SupportMapFragment implements LoaderManager.LoaderCallbacks<List<Poi>>, LocationListener, GoogleMap.OnInfoWindowClickListener, CreateOrUpdateCallback {
    private static final String FORCE_REFRESH = "forceRefresh";
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    @Inject
    protected ModisSenseServiceProvider serviceProvider;
    protected List<Poi> items = Collections.emptyList();
    protected Map<Marker, Poi> markerToPoi = new HashMap<Marker, Poi>();
    private LocationManager locationManager;
    private boolean initial = false;
    private ClusterManager<Poi> mClusterManager;
    private Cluster<Poi> clickedCluster;
    private Poi clickedClusterItem;
    private SlidingUpPanelLayout mSlideLayout;
    private View mSupportMapView;
    private RelativeLayout mRootView;
    private LinearLayout mLayoutListDirections;
    private ImageView mImgIndicator;
    private TextView mTvDistanceDuration;
    private TextView mTvSummary;
    private boolean playServicesAvailable = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mSupportMapView = super.onCreateView(inflater, container, savedInstanceState);
        this.mRootView = (RelativeLayout) inflater.inflate(R.layout.fragment_location_detail_map, container, false);
        this.mSlideLayout = (SlidingUpPanelLayout) mRootView.findViewById(R.id.layout_slidingup);
        this.mSlideLayout.addView(mSupportMapView, 0);
        mRootView.setFocusableInTouchMode(true);
        mRootView.requestFocus();
        mRootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK ) {
                    //Log.i(tag, "onKey Back listener is working!!!");
                    //getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    mSlideLayout.collapsePane();
                    return true;
                } else {
                    return false;
                }
            }
        });
        return mRootView;
    }

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
        this.mTvDistanceDuration =(TextView) mRootView.findViewById(R.id.tv_duration_distance);
        this.mTvSummary =(TextView) mRootView.findViewById(R.id.tv_sumary);
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(getActivity());
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            playServicesAvailable = true;
            if(isVisible()){
                initializeMap();
            }
        } else {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    protected void initializeMap() {
        this.mLayoutListDirections = (LinearLayout) mRootView.findViewById(R.id.list_directions);
        this.mImgIndicator =(ImageView) mRootView.findViewById(R.id.img_indicator);
        this.mSlideLayout = (SlidingUpPanelLayout) mRootView.findViewById(R.id.layout_slidingup);

        this.mSlideLayout.setAnchorPoint(1f);
        this.mSlideLayout.setEnableDragViewTouchEvents(true);
        this.mSlideLayout.setDragView(mRootView.findViewById(R.id.layout_info));
        this.mSlideLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelExpanded(View panel) {
               mImgIndicator.setImageResource(R.drawable.ic_action_expand);

            }

            @Override
            public void onPanelCollapsed(View panel) {
                mImgIndicator.setImageResource(R.drawable.ic_action_collapse);

            }

            @Override
            public void onPanelAnchored(View panel) {

            }
        });
        getLoaderManager().initLoader(0, null, this);
        mClusterManager = new ClusterManager<Poi>(getActivity(), getMap());
        getMap().setOnInfoWindowClickListener(this);
        getMap().setInfoWindowAdapter(mClusterManager.getMarkerManager());
        getMap().setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Poi>() {
            @Override
            public boolean onClusterClick(Cluster<Poi> cluster) {
                clickedCluster = cluster; // remember for use later in the Adapter
                return false;
            }
        });
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Poi>() {
            @Override
            public boolean onClusterItemClick(Poi item) {
                clickedClusterItem = item;
                return false;
            }
        });
        mClusterManager.setRenderer(new PoiRenderer(this.getActivity(),getMap(),mClusterManager, this));
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

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
                RatingBar rating = (RatingBar) v.findViewById(R.id.ratingBar);


                final Poi poi = clickedClusterItem;
                name.setText(poi.getName());
                if(poi.getDescription() == null || "null".equals(poi.getDescription())){
                    description.setText("Δεν υπάρχει διαθέσιμη περιγραφή");
                }
                else {
                    description.setText(poi.getDescription());
                }
                rating.setRating((float) ((poi.getInterest()*10f)/2));
                interst.setText(poi.getHotness()+" επισκέψεις");



                // Returning the view containing InfoWindow contents
                return v;

            }

        });
        getMap().setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
                createOrEditPoi(point);
            }
        });

        getMap().setOnCameraChangeListener(mClusterManager);
    }


    private void setUpInfoForSlidingPanel(){

        mTvDistanceDuration.setText("Near Me");
        mTvSummary.setVisibility(View.GONE);
        mLayoutListDirections.removeAllViews();
            for(Poi poi : items){
                addItemDirections(poi);
            }

    }

    protected void setPopupTitle(String title){
        mTvDistanceDuration.setText(title);
    }

    private void addItemDirections(final Poi poi){

        RelativeLayout mRelativeLayout = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item_information, null);
        TextView mTvDes = (TextView) mRelativeLayout.findViewById(R.id.tv_name);
        //TextView mTvDistance = (TextView) mRelativeLayout.findViewById(R.id.tv_distance);
        ImageView mImgIcon = (ImageView) mRelativeLayout.findViewById(R.id.img_icon);

        //TODO: show image based on hotness and interest
        mTvDes.setText("("+String.valueOf(items.indexOf(poi) + 1)+") "+poi.getName());

        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if(mLayoutListDirections.getChildCount()!=0){
            mLayoutParams.topMargin=(int)2;// ResolutionUtils.convertDpToPixel(mContext, 2);
        }
        mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SafeAsyncTask<Poi>() {

                    @Override
                    public Poi call() throws Exception {
                        return serviceProvider.getService(getActivity()).getPois(poi.getId());
                    }

                    @Override
                    protected void onSuccess(Poi poiFound) throws Exception {
                        super.onSuccess(poiFound);
                        if (poiFound == null) {
                            System.out.println("Poi is null");
                        } else {
                            poiFound.setX(poi.getX());
                            poiFound.setY(poi.getY());
                            poiFound.setId(poi.getId());
                            poiFound.setMine(poi.isMine());
                            Intent intent = new Intent(getActivity(), PoiViewActivity.class);
                            intent.putExtra(PoiViewActivity.VIEW_PARAMS,poiFound);
                            startActivity(intent);
                        }
                    }
                }.execute();

            }
        });
        mLayoutListDirections.addView(mRelativeLayout, mLayoutParams);
    }


    protected void createOrEditPoi(final LatLng point) {
        new SafeAsyncTask<List<Poi>>() {

            @Override
            public List<Poi> call() throws Exception {
                return serviceProvider.getService(getActivity()).getPoisDuplicates(point.latitude, point.longitude);
            }

            @Override
            protected void onSuccess(List<Poi> duplicates) throws Exception {
                super.onSuccess(duplicates);
                if (duplicates == null || duplicates.size() == 0) {
                    PoiAddDialog addDialog = new PoiAddDialog();
                    addDialog.init(point, BaseMapFragment.this);
                    ModisSenseApplication.getInstance().inject(addDialog);
                    addDialog.show(getActivity().getSupportFragmentManager(), "dlg_add_poi");
                } else {
                    final SingleChoice<Poi> singleChoice = new SingleChoice<Poi>(getActivity()
                            , duplicates, new HashSet<Poi>()) {
                        @Override
                        protected void onOk() {
                            Set<Poi> pois = getSelection();
                            if (pois == null || pois.size() == 0) {
                                PoiAddDialog addDialog = new PoiAddDialog();
                                addDialog.init(point, BaseMapFragment.this);
                                ModisSenseApplication.getInstance().inject(addDialog);
                                addDialog.show(getActivity().getSupportFragmentManager(), "dlg_add_poi");
                            } else {
                                PoiEditDialog addDialog = new PoiEditDialog();
                                addDialog.init(pois.iterator().next(), BaseMapFragment.this);
                                ModisSenseApplication.getInstance().inject(addDialog);
                                addDialog.show(getActivity().getSupportFragmentManager(), "dlg_edit_poi");
                            }
                        }

                    };
                    singleChoice.showDialog("Duplicate POIS");
                }
            }
        }.execute();


    }

    public abstract void onCreateOrUpdateData(Poi data);

    public abstract List<Poi> executeLoader() throws AccountsException, IOException;

    public abstract void onLoaderExecutionFinished();

    @Override
    public Loader<List<Poi>> onCreateLoader(int id, Bundle args) {
        final List<Poi> initialItems = items;
        return new ThrowableLoader<List<Poi>>(getActivity(), items) {

            @Override
            public List<Poi> loadData() throws Exception {
                try {
                    if (getActivity() != null) {
                        return executeLoader();
                    } else {
                        Toaster.showLong(getActivity(), "No POIS found for visible map region");
                        return Collections.emptyList();
                    }

                } catch (OperationCanceledException e) {
                    Activity activity = getActivity();
                    if (activity != null)
                        activity.finish();
                    return initialItems;
                } finally {
                    onLoaderExecutionFinished();
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Poi>> loader, List<Poi> data) {
        // getActivity().setSupportProgressBarIndeterminateVisibility(false);
        Ln.d("Loading finished for "+getActivity()+" "+data);
        Exception exception = getException(loader);
        if (exception != null) {
            showError(getErrorMessage(exception));
            //showList();
            return;
        }
        this.items = data;
        if(!playServicesAvailable){
            return;
        }
        getMap().clear();

        markerToPoi.clear();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        mClusterManager.clearItems();
        mClusterManager.addItems(this.items);
        for (Poi poi : this.items) {
            //Marker option = addMarker(poi);
            builder.include(new LatLng(poi.getX(), poi.getY()));
            //markerToPoi.put(option, poi);

        }
        setUpInfoForSlidingPanel();
        getMap().getUiSettings().setMyLocationButtonEnabled(true);
        getMap().setMyLocationEnabled(true);
        if (this.items.size() == 0 && !initial) {
            initial = true;
            if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
                LatLng latLng = new LatLng(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude(), locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
                getMap().animateCamera(cameraUpdate);
            } else {
                //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, MIN_DISTANCE, this);
            }
        }
        else{
            //TODO: zoom to search items?????
        }
        mClusterManager.cluster();


    }




    @Override
    public void onLoaderReset(Loader<List<Poi>> loader) {

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
     * {@link gr.modissense.ui.view.ThrowableLoader}
     *
     * @param loader
     * @return exception or null if none provided
     */
    protected Exception getException(final Loader<List<Poi>> loader) {
        if (loader instanceof ThrowableLoader)
            return ((ThrowableLoader<List<Poi>>) loader).clearException();
        else
            return null;
    }

    int getErrorMessage(Exception e) {
        return R.string.error_loading_pois;
    }

    public Marker addMarker(Poi poi) {
        MarkerOptions marker = new MarkerOptions().position(new LatLng(poi.getX(), poi.getY())).title(poi.getName()).icon(BitmapDescriptorFactory.fromBitmap(new com.google.maps.android.ui.IconGenerator(getActivity()).makeIcon(String.valueOf(this.items.indexOf(poi) + 1))));
        return getMap().addMarker(marker);

    }

    protected boolean isUsable() {
        return getActivity() != null;
    }

    protected void forceRefresh() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(FORCE_REFRESH, true);
        refresh(bundle);
    }

    private void refresh(final Bundle args) {
        if (!isUsable())
            return;
        getLoaderManager().restartLoader(0, args, this);
    }

    @Override
    public void onLocationChanged(Location location) {
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
//        getMap().animateCamera(cameraUpdate);
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

    @Override
    public void onInfoWindowClick(Marker marker) {
        final Poi poi = clickedClusterItem;
        new SafeAsyncTask<Poi>() {

            @Override
            public Poi call() throws Exception {
                return serviceProvider.getService(getActivity()).getPois(poi.getId());
            }

            @Override
            protected void onSuccess(Poi poiFound) throws Exception {
                super.onSuccess(poiFound);
                if (poiFound == null) {
                    System.out.println("Poi is null");
                } else {
                    poiFound.setX(poi.getX());
                    poiFound.setY(poi.getY());
                    poiFound.setId(poi.getId());
                    poiFound.setMine(poi.isMine());
                    Intent intent = new Intent(getActivity(), PoiViewActivity.class);
                    intent.putExtra(PoiViewActivity.VIEW_PARAMS,poiFound);
                    startActivity(intent);
                }
            }
        }.execute();
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }


    private static class PoiRenderer  extends DefaultClusterRenderer<Poi>{
        private final IconGenerator mIconGenerator;
        private final IconGenerator mClusterIconGenerator;
        private final BaseMapFragment mapFragment;
        private final Context context;
        public PoiRenderer(Context context, GoogleMap map, ClusterManager<Poi> clusterManager, BaseMapFragment mapFragment) {
            super(context, map, clusterManager);
            mIconGenerator = new IconGenerator(context);
            mIconGenerator.setStyle(mapFragment.getMarkerIconStyle());
            mClusterIconGenerator = new IconGenerator(context);
            //mClusterIconGenerator.setStyle(mapFragment.getMarkerIconStyle());
            this.context = context;
            this.mapFragment = mapFragment;
        }

        @Override
        protected void onBeforeClusterItemRendered(Poi poi, MarkerOptions markerOptions) {

            Bitmap icon = mIconGenerator.makeIcon(String.valueOf(mapFragment.items.indexOf(poi) + 1));//mIconGenerator.makeIcon("("+String.valueOf(mapFragment.items.indexOf(poi) + 1)+") "+poi.getName());


            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }



        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }



    public int getMarkerIconStyle(){
        return IconGenerator.STYLE_BLUE;
    }


}