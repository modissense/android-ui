package gr.modissense.ui.poi;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.github.kevinsawicki.wishlist.Toaster;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

import gr.modissense.ModisSenseApplication;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.R;
import gr.modissense.core.Poi;
import gr.modissense.ui.BaseMapFragment;
import gr.modissense.util.Ln;
import gr.modissense.util.SafeAsyncTask;

public class PoiSelectDialog extends DialogFragment implements DialogInterface.OnClickListener {
    protected LatLng point;
    protected EditText nameText;
    protected EditText descriptionText;
    protected EditText keywordsText;
    protected CheckBox publicityCheckBox;
    protected Handler handler = new Handler();
    @Inject
    protected ModisSenseServiceProvider service;
    private BaseMapFragment mapFragment;

    public PoiSelectDialog() {
        ModisSenseApplication.getInstance().inject(this);
    }

    public void init(LatLng point, BaseMapFragment mapFragment) {
        this.point = point;
        this.mapFragment = mapFragment;

    }

    public Dialog onCreateDialog(Bundle SavedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = inflater.inflate(R.layout.poi_add, null);
        builder.setView(dialogView);
        builder.setTitle("Add POI");

        nameText = (EditText) dialogView.findViewById(R.id.poiadd_name);
        descriptionText = (EditText) dialogView.findViewById(R.id.poiadd_description);
        keywordsText = (EditText) dialogView.findViewById(R.id.poiadd_keywords);
        publicityCheckBox = (CheckBox) dialogView.findViewById(R.id.poiadd_publicity);

        builder.setPositiveButton("OK", this);

        builder.setNegativeButton("Cancel", this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            final Poi poi = new Poi();
            poi.setName(nameText.getText().toString());
            poi.setDescription(descriptionText.getText().toString());
            poi.keywordsFromString(keywordsText.getText().toString());
            poi.setPublicity(publicityCheckBox.isChecked());
            poi.setX(point.latitude);
            poi.setY(point.longitude);
            System.out.println(poi);

            new SafeAsyncTask<Poi>() {
                public Poi call() throws Exception {
                    Poi res = service.getService(PoiSelectDialog.this.getActivity()).addPoi(poi);
                    Ln.d("Result is :" + res);
                    return poi;
                }

                @Override
                protected void onException(Exception e) throws RuntimeException {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;

                    String message = cause.getMessage();

                    Toaster.showLong(getActivity(), message);
                }

                @Override
                public void onSuccess(Poi success) {
                    if (success != null) {
                        Toaster.showLong(getActivity(), "POI Added");
                        mapFragment.onCreateOrUpdateData(success);
                        PoiSelectDialog.this.dismiss();
                    } else {
                        Toaster.showLong(getActivity(), "Unable to add POI");
                    }

                }

                @Override
                protected void onFinally() throws RuntimeException {

                }
            }.execute();


        } else {
            dismiss();
        }
    }

}
