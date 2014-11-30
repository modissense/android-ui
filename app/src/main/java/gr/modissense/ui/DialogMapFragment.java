package gr.modissense.ui;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;


import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import gr.modissense.ModisSenseApplication;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.R;
import gr.modissense.core.Poi;
import gr.modissense.ui.poi.PoiAddDialog;
import gr.modissense.ui.view.CreateOrUpdateCallback;
import gr.modissense.ui.view.SingleChoice;
import gr.modissense.util.SafeAsyncTask;

public class DialogMapFragment extends DialogFragment implements LocationListener {
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    @Inject
    protected ModisSenseServiceProvider serviceProvider;
    private SupportMapFragment fragment;
    private CreateOrUpdateCallback callback;

    public DialogMapFragment() {
        fragment = new SupportMapFragment();
    }

    public void init(LocationManager locationManager) {
        this.locationManager = locationManager;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mapdialog, container, false);
        getDialog().setTitle("Add Or Select POI");
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.mapView, fragment).commit();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModisSenseApplication.getInstance().inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getFragment().getMap().setMyLocationEnabled(true);
        getFragment().getMap().setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                createOrEditPoi(latLng, callback);
            }
        });
        if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
            LatLng latLng = new LatLng(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude(), locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
            getFragment().getMap().animateCamera(cameraUpdate);
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, MIN_DISTANCE, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        getFragment().getMap().animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
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

    protected void createOrEditPoi(final LatLng point, final CreateOrUpdateCallback fragment) {

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
                    addDialog.init(point, fragment);
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
                                addDialog.init(point, fragment);
                                ModisSenseApplication.getInstance().inject(addDialog);
                                addDialog.show(getActivity().getSupportFragmentManager(), "dlg_add_poi");
                            } else {
//                                PoiEditDialog addDialog = new PoiEditDialog(pois.iterator().next(), fragment);
//                                ModisSenseApplication.getInstance().inject(addDialog);
//                                addDialog.show(getActivity().getSupportFragmentManager(), "dlg_edit_poi");
                                fragment.onCreateOrUpdateData(pois.iterator().next());
                            }
                        }

                    };
                    singleChoice.showDialog("Duplicate POIS");

                }
            }
        }.execute();


    }

    public SupportMapFragment getFragment() {
        return fragment;
    }

    public void setCallback(CreateOrUpdateCallback callback) {
        this.callback = callback;
    }

    public CreateOrUpdateCallback getCallback() {
        return callback;
    }
}
