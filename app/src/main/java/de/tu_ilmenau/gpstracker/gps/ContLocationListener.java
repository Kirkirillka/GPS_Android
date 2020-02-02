package de.tu_ilmenau.gpstracker.gps;

import android.annotation.SuppressLint;

import android.location.Location;
import android.location.LocationListener;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import de.tu_ilmenau.gpstracker.mqtt.MqttClientWrapper;

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
//                downSpeed = SpeedTester.test();
                wifiInfo = wifiManager.getConnectionInfo();
                clientWrapper.publish(MessageBuilder.buildMessage(loc, wifiInfo, deviceId, downSpeed, upSpeed));
            } catch (Exception e) {
                e.printStackTrace();
                downSpeed = 0;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }

    }
}