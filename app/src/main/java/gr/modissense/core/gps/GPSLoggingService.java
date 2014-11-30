package gr.modissense.core.gps;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import gr.modissense.ModisSenseApplication;
import gr.modissense.util.Ln;

import javax.inject.Inject;

public class GPSLoggingService extends Service {
    private final IBinder mBinder = new GPSLoggingBinder();
    @Inject
    GPSLoggingTaskQueue gpsLoggingTaskQueue;
    @Inject
    Bus bus;
    private PositionProvider.PositionListener positionListener = new PositionProvider.PositionListener() {
        private Location lastLoggedLocation = null;

        @Override
        public void onPositionUpdate(Location location) {
            Ln.d("     Location:" + location);
            Ln.d("Last Location:" + lastLoggedLocation);
            if (location != null) {
                Ln.d("Location:" + location);
                if (lastLoggedLocation != null) {
                Ln.d("Distance:" + location.distanceTo(lastLoggedLocation));
                    if (location.distanceTo(lastLoggedLocation) > 14.0f) {
                        Ln.d("Distance is >14");
                        lastLoggedLocation = location;
                        gpsLoggingTaskQueue.add(new GPSLogItem(location));
                    }
                }else{
                    lastLoggedLocation = location;
                    gpsLoggingTaskQueue.add(new GPSLogItem(location));
                }
                //StatusActivity.addMessage(getString(R.string.status_location_update));
                //clientController.setNewLocation(Protocol.createLocationMessage(location));

            }
        }

    };
    private PositionProvider positionProvider;
    private String provider = "gps";
    private int interval = 180;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        ModisSenseApplication.getInstance().inject(this);
        bus.register(this);
        //StatusActivity.addMessage(getString(R.string.status_service_create));
        Ln.d("************************************************CREATE SERVICE");
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.acquire();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final SharedPreferences prefs = this.getSharedPreferences("gr.modissense", Context.MODE_PRIVATE);
        boolean isc = prefs.getBoolean("gpsoff",false);

            Ln.d("************************************************START SERVICE");
            positionProvider = new PositionProvider(this, provider, interval * 1000, positionListener);
        if(!isc) {
            positionProvider.startUpdates();
        }
            return START_STICKY;

    }

    @Subscribe
    public void onStopEvent(GpsStopEvent evt){
        System.out.println("***************************STOPPING");
        positionProvider.stopUpdates();
    }

    @Subscribe
    public void onStartEvent(GpsStartEvent evt){
        System.out.println("***************************STARTING");
        positionProvider.startUpdates();
    }

    @Override
    public void onDestroy() {
        bus.unregister(this);
        if (positionProvider != null) {
            positionProvider.stopUpdates();
        }
        wakeLock.release();
    }

    public class GPSLoggingBinder extends Binder {
        public GPSLoggingService getService() {
            return GPSLoggingService.this;
        }
    }
}
