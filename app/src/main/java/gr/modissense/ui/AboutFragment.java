package gr.modissense.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.google.android.gms.common.GooglePlayServicesUtil;
import gr.modissense.R;

public class AboutFragment extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("About");
        setContentView(R.layout.about);
    }


    @Override
    public void onResume() {
        super.onResume();
        TextView tv = (TextView) findViewById(R.id.tv_about);
        tv.setText(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this));

    }
}
