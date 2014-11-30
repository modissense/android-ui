package gr.modissense.ui.poi;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.github.kevinsawicki.wishlist.Toaster;
import com.squareup.otto.Bus;

import gr.modissense.ModisSenseApplication;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.R;
import gr.modissense.core.ModiResult;
import gr.modissense.core.Poi;
import gr.modissense.ui.view.CreateOrUpdateCallback;
import gr.modissense.util.Ln;
import gr.modissense.util.SafeAsyncTask;

import javax.inject.Inject;

public class PoiEditDialog extends DialogFragment implements DialogInterface.OnClickListener {
    private static final int DELETE_DIALOG = 0;
    protected Poi point;
    protected EditText nameText;
    protected EditText descriptionText;
    protected EditText keywordsText;
    protected CheckBox publicityCheckBox;
    protected Handler handler = new Handler();
    @Inject
    protected ModisSenseServiceProvider service;
    @Inject
    protected Bus BUS;
    private int mStackLevel = 0;
    private CreateOrUpdateCallback callback;

    public PoiEditDialog() {
        ModisSenseApplication.getInstance().inject(this);
    }

    public void init(Poi point, CreateOrUpdateCallback callback) {
        this.point = point;
        this.callback = callback;
    }

    public Dialog onCreateDialog(Bundle SavedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = inflater.inflate(R.layout.poi_add, null);
        builder.setView(dialogView);
        builder.setTitle("Edit POI");

        nameText = (EditText) dialogView.findViewById(R.id.poiadd_name);
        nameText.setText(point.getName());
        descriptionText = (EditText) dialogView.findViewById(R.id.poiadd_description);
        descriptionText.setText(point.getDescription());
        keywordsText = (EditText) dialogView.findViewById(R.id.poiadd_keywords);
        keywordsText.setText(point.keywordsToString());
        publicityCheckBox = (CheckBox) dialogView.findViewById(R.id.poiadd_publicity);
        publicityCheckBox.setChecked(point.isPublicity());

        builder.setPositiveButton("Update", this);
        if(point.isMine()) {
            builder.setNeutralButton("Delete", this);
        }
        builder.setNegativeButton("Cancel", this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            final Poi poi = new Poi();
            poi.setId(point.getId());
            poi.setName(nameText.getText().toString());
            poi.setDescription(descriptionText.getText().toString());
            poi.keywordsFromString(keywordsText.getText().toString());
            poi.setPublicity(publicityCheckBox.isChecked());
            poi.setX(point.getX());
            poi.setY(point.getY());

            System.out.println(poi);

            new SafeAsyncTask<Boolean>() {
                public Boolean call() throws Exception {
                    ModiResult res = service.getService(PoiEditDialog.this.getActivity()).updatePoi(poi);
                    Ln.d("Result is :" + res);
                    if (res != null) {
                        return res.isResult();
                    }
                    return false;
                }

                @Override
                protected void onException(Exception e) throws RuntimeException {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;

                    String message = cause.getMessage();

                    Toaster.showLong(getActivity(), message);
                }

                @Override
                public void onSuccess(Boolean success) {
                    if (success) {
                        Toaster.showLong(getActivity(), "POI Added");
                        //BUS.post(ModisSenseApplication.getInstance().postMapEvent());
                        if(callback != null) {
                            callback.onCreateOrUpdateData(poi);
                        }
                        PoiEditDialog.this.dismiss();
                    } else {
                        Toaster.showLong(getActivity(), "Unable to add POI");
                    }

                }

                @Override
                protected void onFinally() throws RuntimeException {

                }
            }.execute();


        } else if (which == DialogInterface.BUTTON_NEUTRAL) {
            createDialog(DELETE_DIALOG);
        } else {
            dismiss();
        }
    }

    private void createDialog(int dialog) {
        switch (dialog) {
            case DELETE_DIALOG:
                DialogFragment newFragment = ConfirmDialogFragment.newInstance(R.string.dlg_remove_poi, getActivity(), point);
                mStackLevel++;
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                newFragment.show(ft, "dialog");
                break;
        }

    }

    public static class ConfirmDialogFragment extends DialogFragment {
        @Inject
        protected ModisSenseServiceProvider service;
        @Inject
        protected Bus BUS;

        public static ConfirmDialogFragment newInstance(int title, Activity ctx, Poi poi) {
            ConfirmDialogFragment frag = new ConfirmDialogFragment();
            frag.init(ctx, poi);
            ModisSenseApplication.getInstance().inject(frag);
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        private Activity ctx;
        private Poi poi;

        public ConfirmDialogFragment(){

        }

        public void init(Activity context, Poi poi) {
            this.ctx = context;
            this.poi = poi;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");
            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.icon)
                    .setTitle(title)
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new SafeAsyncTask<Boolean>() {
                                public Boolean call() throws Exception {
                                    ModiResult res = service.getService(ctx).deletePoi(poi);
                                    if (res != null) {
                                        return res.isResult();
                                    }
                                    return false;
                                }

                                @Override
                                protected void onException(Exception e) throws RuntimeException {
                                    Throwable cause = e.getCause() != null ? e.getCause() : e;

                                    String message = cause.getMessage();

                                    Toaster.showLong((Activity) ctx, message);
                                }

                                @Override
                                public void onSuccess(Boolean success) {
                                    if (success) {
                                        BUS.post(ModisSenseApplication.getInstance().postMapEvent());
                                        Toaster.showLong((Activity) ctx, "POI Deleted");
                                    } else {
                                        Toaster.showLong((Activity) ctx, "Unable to remove POI");
                                    }

                                }

                                @Override
                                protected void onFinally() throws RuntimeException {

                                }
                            }.execute();
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
        }
    }
}
