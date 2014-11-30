package gr.modissense.core.gps;

import java.util.*;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import gr.modissense.util.Ln;

public class PositionProvider {

    public static final String PROVIDER_MIXED = "mixed";
    public static final long PERIOD_DELTA = 10 * 1000;
    public static final long RETRY_PERIOD = 60 * 1000;
    private final Context context;
    private final Handler handler;
    private final LocationManager locationManager;
    private final long period;
    private final PositionListener listener;

    private final Runnable updateTask = new Runnable() {

        private boolean tryProvider(String provider) {
            Ln.d("Running update task");
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null && new Date().getTime() - location.getTime() <= period + PERIOD_DELTA) {
                listener.onPositionUpdate(location);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void run() {
            if (useFine && tryProvider(LocationManager.GPS_PROVIDER)) {
            } else if (useCoarse && tryProvider(LocationManager.NETWORK_PROVIDER)) {
            } else {
                listener.onPositionUpdate(null);
            }
            handler.postDelayed(this, period);
        }

    };
    private final InternalLocationListener fineLocationListener = new InternalLocationListener();
    private final InternalLocationListener coarseLocationListener = new InternalLocationListener();
    private final InternalLocationListener networkLocationListener = new InternalLocationListener();
    private boolean useFine;
    private boolean useCoarse;

    public PositionProvider(Context context, String type, long period, PositionListener listener) {
        this.context = context;
        handler = new Handler(this.context.getMainLooper());
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.period = period;
        this.listener = listener;

        // Determine providers
        if (type.equals(PROVIDER_MIXED)) {
            useFine = true;
            useCoarse = true;
        } else if (type.equals(LocationManager.GPS_PROVIDER)) {
            useFine = true;
        } else if (type.equals(LocationManager.NETWORK_PROVIDER)) {
            useCoarse = true;
        }
    }

    public static String createLocationMessage(Location l) {
        StringBuilder s = new StringBuilder("$GPRMC,");
        Formatter f = new Formatter(s, Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        calendar.setTimeInMillis(l.getTime());

        f.format("%1$tH%1$tM%1$tS.%1$tL,A,", calendar);

        double lat = l.getLatitude();
        double lon = l.getLongitude();
        f.format("%02d%07.4f,%c,", (int) Math.abs(lat), Math.abs(lat) % 1 * 60, lat < 0 ? 'S' : 'N');
        f.format("%03d%07.4f,%c,", (int) Math.abs(lon), Math.abs(lon) % 1 * 60, lon < 0 ? 'W' : 'E');

        double speed = l.getSpeed() * 1.943844; // speed in knots
        f.format("%.2f,%.2f,", speed, l.getBearing());
        f.format("%1$td%1$tm%1$ty,,", calendar);

        byte checksum = 0;
        for (byte b : s.substring(1).getBytes()) {
            checksum ^= b;
        }
        f.format("*%02x\r\n", (int) checksum);
        f.close();

        return s.toString();
    }

    public void startUpdates() {
        if (useFine) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, period, 0, fineLocationListener);
        }
        if (useCoarse) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, period, 0, coarseLocationListener);
        }

        handler.postDelayed(updateTask, period);
    }

    public void stopUpdates() {
        handler.removeCallbacks(updateTask);
        locationManager.removeUpdates(fineLocationListener);
        locationManager.removeUpdates(coarseLocationListener);
    }

    public interface PositionListener {
        public void onPositionUpdate(Location location);
    }

    private class InternalLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(final String provider, int status, Bundle extras) {
            if (status == LocationProvider.TEMPORARILY_UNAVAILABLE || status == LocationProvider.OUT_OF_SERVICE) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        locationManager.removeUpdates(InternalLocationListener.this);
                        locationManager.requestLocationUpdates(provider, period, 0, InternalLocationListener.this);
                    }
                }, RETRY_PERIOD);
            }
        }

    }

}