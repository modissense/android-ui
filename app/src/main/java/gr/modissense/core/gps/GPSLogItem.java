package gr.modissense.core.gps;


import android.accounts.AccountsException;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import com.google.gson.annotations.Expose;
import com.squareup.tape.Task;
import gr.modissense.ModisSenseApplication;
import gr.modissense.ModisSenseServiceProvider;
import gr.modissense.core.ModiResult;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;

public class GPSLogItem implements Task<GPSLogItem.Callback> {
    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());
    @Inject
    ModisSenseServiceProvider modisSenseServiceProvider;
    @Expose
    private double lat;
    @Expose
    private double lng;
    @Expose
    private double bearing;
    @Expose
    private long timestamp;
    @Expose
    private float accuracy;
    @Expose
    private double altitude;
    @Expose
    private String provider;
    @Expose
    private float speed;

    public GPSLogItem() {
    }

    public GPSLogItem(Location location) {
        this.lat = location.getLatitude();
        this.lng = location.getLongitude();
        this.accuracy = location.getAccuracy();
        this.altitude = location.getAltitude();
        this.bearing = location.getBearing();
        this.provider = location.getProvider();
        this.speed = location.getSpeed();
        this.timestamp = location.getTime();
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void execute(final Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ModiResult result = modisSenseServiceProvider.getService(null).logGPSTrace(GPSLogItem.this);
                    System.out.println("result is " + result);
                    if(result!=null && result.isResult()){
                        callback.onSuccess("null");
                    }
                    else{
                        callback.onFailure();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    callback.onFailure();
                }
            }
        }).start();
    }


    public interface Callback {
        void onSuccess(String url);

        void onFailure();
    }
}
