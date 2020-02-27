package de.tu_ilmenau.gpstracker.listener;

import android.location.Location;
import android.location.LocationListener;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import de.tu_ilmenau.gpstracker.sender.Sender;
import de.tu_ilmenau.gpstracker.tasks.BackgroundSenderCaller;

/**
 * Background service location listener
 */
public class ContLocationListener implements LocationListener {

    private Sender sender;
    private String deviceId;
    private WifiManager wifiManager;

    public ContLocationListener(Sender sender, String deviceId, WifiManager wifiManager) {
        this.wifiManager = wifiManager;
        this.sender = sender;
        this.deviceId = deviceId;
    }


    @Override
    public void onLocationChanged(Location loc) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            new BackgroundSenderCaller(loc, wifiManager.getConnectionInfo(), sender, deviceId).execute();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}