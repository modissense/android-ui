package gr.modissense.ui.blog;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.github.kevinsawicki.wishlist.Toaster;

import java.util.Calendar;
import java.util.StringTokenizer;

import javax.inject.Inject;

import gr.modissense.ModisSenseApplication;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.R;
import gr.modissense.core.Blog;
import gr.modissense.core.ModiResult;
import gr.modissense.core.VisitItem;
import gr.modissense.ui.view.CreateOrUpdateCallback;
import gr.modissense.util.Ln;
import gr.modissense.util.SafeAsyncTask;

public class VisitUpdateDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private Blog blog;
    protected VisitItem visitItem;
    protected EditText commentsText;
    protected EditText poiIdText;
    protected EditText dateText;
    protected EditText arrivedText;
    protected EditText offText;
    protected EditText seqText;
    protected CheckBox publicityCheckBox;
    protected Handler handler = new Handler();
    @Inject
    protected ModisSenseServiceProvider service;
    private CreateOrUpdateCallback mapFragment;

    public VisitUpdateDialog(){
        ModisSenseApplication.getInstance().inject(this);
    }

    public void init(VisitItem visitItem, CreateOrUpdateCallback mapFragment, Blog blog) {
        this.visitItem = visitItem;
        this.mapFragment = mapFragment;
        this.blog = blog;

    }

    public Dialog onCreateDialog(Bundle SavedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = inflater.inflate(R.layout.visit_update, null);
        builder.setView(dialogView);
        builder.setTitle("Update Blog");

        poiIdText = (EditText) dialogView.findViewById(R.id.visitadd_poi);
        poiIdText.setText(visitItem.getPoiId());
        commentsText = (EditText) dialogView.findViewById(R.id.visitadd_comments);
        commentsText.setText(visitItem.getComments());
        dateText = (EditText) dialogView.findViewById(R.id.visitadd_date);
        dateText.setText(visitItem.getDate());
        dateText.setClickable(true);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragmentDate newFragment = new DatePickerFragmentDate();
                newFragment.setDateText(dateText);
                newFragment.show(VisitUpdateDialog.this.getFragmentManager(), "datePicker");
            }
        });
        arrivedText = (EditText) dialogView.findViewById(R.id.visitadd_arrived);
        arrivedText.setText(visitItem.getArrived());
        arrivedText.setClickable(true);
        arrivedText.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                TimePickerFragmentArrive newFragment = new TimePickerFragmentArrive();
                newFragment.setArrivedText(arrivedText);
                newFragment.show(VisitUpdateDialog.this.getFragmentManager(), "timePickerArrive");
            }
        });
        offText = (EditText) dialogView.findViewById(R.id.visitadd_off);
        offText.setText(visitItem.getOff());
        offText.setClickable(true);
        offText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerFragmentOff newFragment = new TimePickerFragmentOff();
                newFragment.setOffText(offText);
                newFragment.show(VisitUpdateDialog.this.getFragmentManager(), "timePickerOff");
            }
        });
        seqText = (EditText) dialogView.findViewById(R.id.visitadd_seq);
        seqText.setText(String.valueOf(visitItem.getSeqNum()));
        publicityCheckBox = (CheckBox) dialogView.findViewById(R.id.visitadd_publicity);
        publicityCheckBox.setChecked(visitItem.isPublicity());

        builder.setPositiveButton("OK", this);

        builder.setNegativeButton("Cancel", this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            final VisitItem poi = new VisitItem();
            poi.setPoiId(poiIdText.getText().toString());
            if (!"".equals(arrivedText.getText().toString()))
                poi.setArrived(dateText.getText().toString() + " " + arrivedText.getText().toString());
            poi.setComments(commentsText.getText().toString());
            if (!"".equals(offText.getText().toString()))
                poi.setOff(dateText.getText().toString() + " " + offText.getText().toString());
            poi.setPublicity(publicityCheckBox.isChecked());
            poi.setDate(dateText.getText().toString());
            poi.setSeqNum(Integer.parseInt(seqText.getText().toString()));
            System.out.println(poi);

            new SafeAsyncTask<Boolean>() {
                public Boolean call() throws Exception {
                    ModiResult res = service.getService(VisitUpdateDialog.this.getActivity()).updateVisitItem(poi);
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
                    if (success) {
                        Toaster.showLong(getActivity(), "POI Added");
                        mapFragment.onCreateOrUpdateData(null);
                        VisitUpdateDialog.this.dismiss();
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


    public static class DatePickerFragmentDate extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        private EditText dateText;

        public void setDateText(EditText dateText) {
            this.dateText = dateText;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            dateText.setText(year + "-" + (month + 1) + "-" + day);
        }
    }

    private static int[] getHours(String hourText){
        int[] res = new int[]{0,0};
        if(hourText == null){
            return res;
        }
        StringTokenizer str = new StringTokenizer(hourText,":");
        if(str.countTokens()>=2){
            try{
                res[0] = Integer.parseInt(str.nextToken());
            }catch (NumberFormatException e){
                res[0]=0;
            }
            try{
                res[1] = Integer.parseInt(str.nextToken());
            }catch (NumberFormatException e){
                res[1]=0;
            }
        }
        return res;
    }

    public static class TimePickerFragmentArrive extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        private EditText arrivedText;

        public void setArrivedText(EditText arrivedText) {
            this.arrivedText = arrivedText;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int[] existing = getHours(arrivedText.getText().toString());
            // Create a new instance of DatePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, existing[0], existing[1], true);

        }


        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i2) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            String hour = (i < 10) ? ("0" + i) : "" + i;
            String minutes = (i2 < 10) ? ("0" + i2) : "" + i2;
            arrivedText.setText(hour + ":" + minutes + ":00");
        }
    }

    public static class TimePickerFragmentOff extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        private EditText offText;

        public void setOffText(EditText offText) {
            this.offText = offText;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int[] existing = getHours(offText.getText().toString());
            // Create a new instance of DatePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, existing[0], existing[1], true);

        }


        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i2) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            String hour = (i < 10) ? ("0" + i) : "" + i;
            String minutes = (i2 < 10) ? ("0" + i2) : "" + i2;
            offText.setText(hour + ":" + minutes + ":00");
        }
    }
}
