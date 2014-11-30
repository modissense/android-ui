package gr.modissense.ui.poi;



import android.app.AlertDialog;
import android.app.Dialog;


import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import butterknife.InjectView;
import com.github.kevinsawicki.wishlist.Toaster;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.otto.Bus;

import com.squareup.picasso.Picasso;
import gr.modissense.ModisSenseApplication;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.R;
import gr.modissense.core.ModiResult;
import gr.modissense.core.Poi;
import gr.modissense.ui.ModisSenseActivity;
import gr.modissense.ui.view.CreateOrUpdateCallback;
import gr.modissense.util.SafeAsyncTask;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

public class PoiViewActivity
        extends ModisSenseActivity implements CreateOrUpdateCallback {
    public static final String VIEW_PARAMS = "view_poi";
    private static final int DELETE_DIALOG = 0;
    protected Poi poi;
    @InjectView(R.id.poi_name)
    protected TextView name;
    @InjectView(R.id.poi_description)
    protected TextView description;
    @InjectView(R.id.poi_visits)
    protected TextView poiVisits;
    @InjectView(R.id.poi_address)
    protected TextView addressView;
    @InjectView(R.id.poi_comments)
    protected TextView commentsView;

    @InjectView(R.id.poi_icon)
    protected ImageView iconView;
    @InjectView(R.id.poi_interest)
    protected RatingBar ratingBar;



    @InjectView(R.id.friend_name)
    protected TextView friendName;
    @InjectView(R.id.friend_description)
    protected TextView friendDescription;
    @InjectView(R.id.friend_visits)
    protected TextView friendVisits;
    @InjectView(R.id.friend_address)
    protected TextView frienddAddressView;


    @InjectView(R.id.friend_icon)
    protected ImageView friendIconView;
    @InjectView(R.id.friend_interest)
    protected RatingBar friendRatingBar;

    @Inject
    protected ModisSenseServiceProvider service;
    @Inject
    protected Bus BUS;
    private int mStackLevel = 0;
    private SupportMapFragment fragment;
    private IconGenerator mIconGenerator;
    private MenuItem deleteMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_view_activity);
        setTitle("Σημείο Ενδιαφέροντος");
        ModisSenseApplication.getInstance().inject(this);

        if (getIntent() != null && getIntent().getExtras() != null) {
            poi = (Poi) getIntent().getExtras().getSerializable(VIEW_PARAMS);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        fragment = new SupportMapFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.mapView, fragment).commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mIconGenerator = new IconGenerator(this);
        mIconGenerator.setStyle(IconGenerator.STYLE_BLUE);
        if (getIntent() != null && getIntent().getExtras() != null) {
            poi = (Poi) getIntent().getExtras().getSerializable(VIEW_PARAMS);
        }
        doRender();
    }



    private void doRender(){
        setTitle(poi.getName());
        Bitmap icon = mIconGenerator.makeIcon(poi.getName());
        LatLng latLng = poi.getPosition();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        fragment.getMap().animateCamera(cameraUpdate);
        fragment.getMap().addMarker(new MarkerOptions().position(poi.getPosition()).title(poi.getName()).icon(BitmapDescriptorFactory.fromBitmap(icon)));
        name.setText(poi.getName());
        Geocoder geoCoder = new Geocoder(this);
        List<Address> matches = null;
        try {
            matches = geoCoder.getFromLocation(poi.getPosition().latitude, poi.getPosition().longitude, 1);

        } catch (IOException e) {
            //e.printStackTrace();
        }
        if(matches != null ){
            Address bestMatch = (matches.isEmpty() ? null : matches.get(0));
            if(bestMatch != null){
                StringBuilder b = new StringBuilder();
                for(int i =0; i<bestMatch.getMaxAddressLineIndex();i++){
                    if(i!=0){
                        b.append(", ");
                    }
                    b.append(bestMatch.getAddressLine(i));
                }
                addressView.setText(b.toString());
            }
        }
        else {
            addressView.setText("Δεν υπάρχει διαθέσιμη διεύθυνση");
        }
        if(poi.getDescription() == null || "null".equals(poi.getDescription())){
            description.setText("Δεν υπάρχει διαθέσιμη περιγραφή");
        }
        else{
            description.setText(poi.getDescription());
        }
        poiVisits.setText(poi.getHotness() +" Επισκέψεις");
        commentsView.setText(poi.getNumberOfComments()+" Σχόλια");
        if(poi.getImage() != null) {
            Picasso.with(ModisSenseApplication.getInstance())
                    .load(poi.getImage())
                    .placeholder(R.drawable.gravatar_icon)
                    .into(iconView);
        }
        ratingBar.setRating((float) ((poi.getInterest()*10f)/2));
        friendName.setText("Η γνώμη των φίλων σου");
        if(poi.getPersonalized() != null){
            if(poi.getPersonalized().getComment() != null){
                Picasso.with(ModisSenseApplication.getInstance())
                        .load(poi.getPersonalized().getComment().getUserPicture())
                        .placeholder(R.drawable.gravatar_icon)
                        .into(friendIconView);
                friendDescription.setText(poi.getPersonalized().getComment().getUser());
                frienddAddressView.setText(poi.getPersonalized().getComment().getText());
            }
            friendVisits.setText(poi.getPersonalized().getHotness()+" Επισκέψεις Φίλων");
            friendRatingBar.setRating((float) ((poi.getPersonalized().getInterest()*10f)/2));
        }
        updateDeleteMenu();
    }

    private void updateDeleteMenu(){
        if(poi != null && poi.isMine()){
            if(deleteMenuItem != null){
                deleteMenuItem.setVisible(true);
                return;
            }
        }
        if(deleteMenuItem != null){
            deleteMenuItem.setVisible(false);
        }

    }
    private void createDialog(int dialog) {
        switch (dialog) {
            case DELETE_DIALOG:
                DialogFragment newFragment = ConfirmDialogFragment.newInstance(R.string.dlg_remove_poi);
                mStackLevel++;
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                newFragment.show(ft, "dialog");
                break;
        }

    }

    public void doPositiveClick() {
        new SafeAsyncTask<Boolean>() {
            public Boolean call() throws Exception {
                ModiResult res = service.getService(PoiViewActivity.this).deletePoi(poi);
                if (res != null) {
                    return res.isResult();
                }
                return false;
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                Throwable cause = e.getCause() != null ? e.getCause() : e;

                String message = cause.getMessage();

                Toaster.showLong(PoiViewActivity.this, message);
            }

            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    BUS.post(ModisSenseApplication.getInstance().postMapEvent());
                    Toaster.showLong(PoiViewActivity.this, "POI Deleted");
                } else {
                    Toaster.showLong(PoiViewActivity.this, "Unable to remove POI");
                }
                PoiViewActivity.this.finish();
            }

            @Override
            protected void onFinally() throws RuntimeException {

            }
        }.execute();
    }

    public void doNegativeClick() {

    }

    public Poi getPoi() {
        return poi;
    }

    @Override
    public void onCreateOrUpdateData(Poi data) {
        this.poi = data;
        getIntent().getExtras().putSerializable(VIEW_PARAMS, this.poi);
        doRender();
        //TODO: post required map event

    }

    public static class ConfirmDialogFragment extends DialogFragment {

        public static ConfirmDialogFragment newInstance(int title) {
            ConfirmDialogFragment frag = new ConfirmDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }



        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");
            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.icon)
                    .setTitle(title)
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ((PoiViewActivity) getActivity()).doPositiveClick();
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ((PoiViewActivity) getActivity()).doNegativeClick();
                        }
                    })
                    .create();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.poi_menu, menu);
        deleteMenuItem = menu.findItem(R.id.delete);
        updateDeleteMenu();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.edit:
                PoiEditDialog addDialog = new PoiEditDialog();
                addDialog.init(poi, this);
                ModisSenseApplication.getInstance().inject(addDialog);
                addDialog.show(this.getSupportFragmentManager(), "dlg_edit_poi");
                break;
            case R.id.delete:
                createDialog(DELETE_DIALOG);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
