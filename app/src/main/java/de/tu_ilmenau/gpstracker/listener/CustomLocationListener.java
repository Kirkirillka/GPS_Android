package de.tu_ilmenau.gpstracker.listener;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Listener class to get coordinates
 */
public class CustomLocationListener implements LocationListener {
    @Override
    public void onLocationChanged(Location loc) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider,
                                int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
}