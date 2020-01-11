package de.tu_ilmenau.gpstracker.gps;

import android.annotation.SuppressLint;

import android.location.Location;
import android.location.LocationListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import de.tu_ilmenau.gpstracker.mqtt.MqttClientWrapper;
import de.tu_ilmenau.gpstracker.storage.SpeedTester;

public class ContLocationListener implements LocationListener {

    private MqttClientWrapper clientWrapper;
    private String deviceId;
    private WifiManager wifiManager;
    private Location loc;

    public ContLocationListener(MqttClientWrapper clientWrapper, String deviceId, WifiManager wifiManager) {
        this.wifiManager = wifiManager;
        this.clientWrapper = clientWrapper;
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
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {
        private double speed = 0.0;
        private WifiInfo wifiInfo;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... params) {
            try {
                speed = SpeedTester.test();
                wifiInfo = wifiManager.getConnectionInfo();
                clientWrapper.publish(MessgeBuilder.buildMessage(loc, wifiInfo, deviceId, speed));
            } catch (Exception e) {
                e.printStackTrace();
                speed = 0;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }

    }
}