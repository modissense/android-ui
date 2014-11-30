package gr.modissense.ui.poi;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.*;

import butterknife.InjectView;

import com.github.kevinsawicki.wishlist.Toaster;
import com.squareup.otto.Bus;

import gr.modissense.ModisSenseApplication;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.R;
import gr.modissense.core.ModiUserInfo;
import gr.modissense.core.PoiSearchParams;
import gr.modissense.ui.ModisSenseActivity;
import gr.modissense.ui.view.MultiChoice;
import gr.modissense.ui.view.TriToggleButton;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PoiSearchActivity extends ModisSenseActivity {
    public static final String SEARCH_PARAMS = "search";
    protected PoiSearchParams searchParams;
    @InjectView(R.id.dateStart)
    protected TextView dateStart;
    @InjectView(R.id.dateEnd)
    protected TextView dateEnd;
    @InjectView(R.id.keywords)
    protected TextView keywords;
    @InjectView(R.id.friends)
    protected TextView friends;
    @InjectView(R.id.seekBarValue)
    protected TextView seekBarText;
    @InjectView(R.id.seekBar)
    protected SeekBar seekBar;


    @InjectView(R.id.searchButton)
    protected TextView searchButton;
    @InjectView(R.id.sortToggleButton)
    protected TriToggleButton sortButton;
    @Inject
    protected Bus BUS;
    @Inject
    ModisSenseServiceProvider serviceProvider;
    protected Date start;
    protected Date end;
    private ModiUserInfo userInfo;
    private MultiChoice<ModiUserInfo.UserExpanded> multiChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        setTitle("Search");
        if (getIntent() != null && getIntent().getExtras() != null) {
            searchParams = (PoiSearchParams) getIntent().getExtras().getSerializable(SEARCH_PARAMS);
        }
        dateStart.setClickable(true);

        dateStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerStartDialog(v);
            }
        });
        dateStart.setLongClickable(true);
        dateStart.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dateStart.setText("");
                return true;
            }
        });

        dateEnd.setClickable(true);
        dateEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerEndDialog(v);
            }
        });
        dateEnd.setLongClickable(true);
        dateEnd.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dateEnd.setText("");
                return true;
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        ModiUserInfo allFriends = ModisSenseApplication.getInstance().friends;
        List<ModiUserInfo.UserExpanded> allFriendsToUse = new ArrayList<ModiUserInfo.UserExpanded>();
        if(allFriends != null){
            allFriendsToUse = allFriends.getAllFriends();
        }
        else{
            Toaster.showLong(this,"Friends list not loaded, You neeed to refresh your account info");
        }
        multiChoice = new MultiChoice<ModiUserInfo.UserExpanded>(PoiSearchActivity.this
                ,allFriendsToUse, searchParams.getFriendsSet()) {
            @Override
            protected void onOk() {
                searchParams.setFriendsSet(multiChoice.getSelection());
                StringBuilder sb = new StringBuilder();
                for(ModiUserInfo.UserExpanded u : multiChoice.getSelection()){
                    sb.append(u.getName()).append(",");
                }
                friends.setText(sb.toString());
            }

        };
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                multiChoice.showDialog("Select Friends");
            }
        });
        seekBarText.setText("Number of results: " + searchParams.getNumberOfResults());
        seekBar.setProgress(searchParams.getNumberOfResults());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i==0){
                    seekBar.setProgress(1);
                    return;
                }
                searchParams.setNumberOfResults(i);
                seekBarText.setText("Number of results: " + searchParams.getNumberOfResults());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchParams.setKeywords(keywords.getText().toString());
                searchParams.setFriends(friends.getText().toString());
                switch (sortButton.getState()) {
                    case 0:
                        searchParams.setSort("");
                        break;
                    case 1:
                        searchParams.setSort("interest");
                        break;
                    case 2:
                        searchParams.setSort("hotness");
                        break;
                    default:
                        break;
                }

                ModisSenseApplication.getInstance().getMapEvent().setSearchParams(searchParams);
                BUS.post(ModisSenseApplication.getInstance().postMapEvent());
                //postSearchParams();
                PoiSearchActivity.this.finish();
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        BUS.register(this);


    }

    @Override
    protected void onPause() {
        super.onPause();

        BUS.unregister(this);
    }

    public void showDatePickerStartDialog(View v) {
        DialogFragment newFragment = new DatePickerFragmentStart();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showDatePickerEndDialog(View v) {
        DialogFragment newFragment = new DatePickerFragmentEnd();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragmentStart extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        private EditText dateStart;
        private PoiSearchParams searchParams;

        public void init(EditText dateStart, PoiSearchParams searchParams){
            this.dateStart = dateStart;
            this.searchParams = searchParams;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog dlg = new DatePickerDialog(getActivity(), this, year, month, day);
            dlg.setCancelable(true);
            this.setCancelable(true);
            return dlg;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            dateStart.setText(day + "/" + (month + 1) + "/" + year);
            searchParams.setDateStart(new Date(view.getCalendarView().getDate()));
        }
    }

    public static class DatePickerFragmentEnd extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        private EditText dateEnd;
        private PoiSearchParams searchParams;

        public void init(EditText dateStart, PoiSearchParams searchParams){
            this.dateEnd = dateStart;
            this.searchParams = searchParams;
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog dlg = new DatePickerDialog(getActivity(), this, year, month, day);
            dlg.setCancelable(true);
            this.setCancelable(true);
            dlg.setOnCancelListener(null);

            return dlg;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            dateEnd.setText(day + "/" + (month + 1) + "/" + year);
            searchParams.setDateEnd(new Date(view.getCalendarView().getDate()));
        }
    }


}
