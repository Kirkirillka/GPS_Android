package de.tu_ilmenau.gpstracker.gps;

import android.annotation.SuppressLint;

import android.location.Location;
import android.location.LocationListener;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import de.tu_ilmenau.gpstracker.model.ClientDeviceMessage;
import de.tu_ilmenau.gpstracker.sender.ClientWrapper;
import de.tu_ilmenau.gpstracker.sender.HttpPostSender;
import de.tu_ilmenau.gpstracker.sender.Sender;
import de.tu_ilmenau.gpstracker.storage.LastLocationStorage;

public class ContLocationListener implements LocationListener {

    private Sender sender;
    private String deviceId;
    private WifiManager wifiManager;
    private Location loc;

    public ContLocationListener(Sender sender, String deviceId, WifiManager wifiManager) {
        this.wifiManager = wifiManager;
        this.sender = sender;
        this.deviceId = deviceId;
    }


    @Override
    public void onLocationChanged(Location loc) {
        this.loc = loc;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            new AsyncCaller().execute();
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

    @SuppressLint("NewApi")
    private class AsyncCaller extends BackgroundSpeedTester {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... params) {

            try {
                speedTest();
                wifiInfo = wifiManager.getConnectionInfo();
                ClientDeviceMessage clientDeviceMessage = MessageBuilder.buildMessage(loc, wifiInfo, deviceId, totalResult);
                sender.publish(clientDeviceMessage);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }

    }
}