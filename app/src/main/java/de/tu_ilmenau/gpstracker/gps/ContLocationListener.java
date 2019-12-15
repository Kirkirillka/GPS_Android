package de.tu_ilmenau.gpstracker.gps;

import android.annotation.SuppressLint;

import android.location.Location;
import android.location.LocationListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;


import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Date;
import java.util.GregorianCalendar;

import de.tu_ilmenau.gpstracker.mqtt.MqttClientWrapper;

public class ContLocationListener implements LocationListener {

    MqttClientWrapper clientWrapper;
    String deviceId;
    WifiManager wifiManager;

    public ContLocationListener(MqttClientWrapper clientWrapper, String deviceId, WifiManager wifiManager) {
        this.wifiManager = wifiManager;
        this.clientWrapper = clientWrapper;
        this.deviceId = deviceId;
    }


    @Override
    public void onLocationChanged(Location loc) {
        @SuppressLint("MissingPermission") WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return;
        }
        GregorianCalendar cal = new GregorianCalendar();
        Date date = new Date();
        cal.setTime(date);
        try {
            clientWrapper.publish(MessgeBuilder.buildMessage(loc, wifiInfo, deviceId));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
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