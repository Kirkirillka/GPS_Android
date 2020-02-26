package de.tu_ilmenau.gpstracker.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.tu_ilmenau.gpstracker.Config;
import de.tu_ilmenau.gpstracker.database.SqliteBuffer;
import de.tu_ilmenau.gpstracker.gps.BackgroundSpeedTester;
import de.tu_ilmenau.gpstracker.gps.MessageBuilder;
import de.tu_ilmenau.gpstracker.model.ClientDeviceMessage;
import de.tu_ilmenau.gpstracker.model.SpeedTestTotalResult;
import de.tu_ilmenau.gpstracker.sender.ClientService;
import de.tu_ilmenau.gpstracker.sender.ClientWrapper;
import de.tu_ilmenau.gpstracker.sender.HttpPostSender;
import de.tu_ilmenau.gpstracker.utils.Utils;
import de.tu_ilmenau.gpstracker.view.MainActivity;

import static de.tu_ilmenau.gpstracker.Config.MIN_DISTANCE_CHANGE_FOR_UPDATES;
import static de.tu_ilmenau.gpstracker.Config.MIN_TIME_BW_UPDATES;

/**
 * Main processing class to interact with model and view, handle processes
 */
public class GpsLocationViewModel extends ViewModel {
    public MutableLiveData<Location> locationStorage = new MutableLiveData<Location>();
    public MutableLiveData<SpeedTestTotalResult> speedStorage = new MutableLiveData<SpeedTestTotalResult>();
    private String ipV4;
    private LocationManager locationManager = null;
    private LocationListener locationListener = null;
    private MainActivity mainActivity;
    private String deviceId;
    private SqliteBuffer buffer;
    private boolean httpPost = true;

    public void init(MainActivity mainActivity, String deviceId) {
        this.mainActivity = mainActivity;
        this.deviceId = deviceId;
        this.buffer = new SqliteBuffer(mainActivity);
        locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new CustomLocationListener();
        chooseBestLocationManager();
    }

    @SuppressLint("MissingPermission")
    private void chooseBestLocationManager() {
        if (locationManager.getLastKnownLocation(Config.LOC_MANAGER) == null) {
            Config.LOC_MANAGER = LocationManager.NETWORK_PROVIDER;
            if (locationManager.getLastKnownLocation(Config.LOC_MANAGER) == null) {
                Config.LOC_MANAGER = LocationManager.PASSIVE_PROVIDER;
            }
        }
        locationManager.requestLocationUpdates(Config.LOC_MANAGER,
                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
    }


    public void pushLocation() {
        new BackgroundSpeedTesterAsyncCaller().execute();
    }

    public void stopService() {
        final Intent intent = new Intent(mainActivity, ClientService.class);
        mainActivity.stopService(intent);
    }

    public void startService(int timeoutVal) {
        final Intent intent = new Intent(mainActivity, ClientService.class);
        intent.putExtra("IP", ipV4);
        intent.getStringExtra("IP");
        intent.putExtra("device", deviceId);
        intent.putExtra("timeout", timeoutVal);
        mainActivity.startService(intent);
    }

    public void setIpV4(String ipV4) {
        this.ipV4 = ipV4;
    }

    /**
     * Listener class to get coordinates
     */
    private class CustomLocationListener implements LocationListener {
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


    @SuppressLint("NewApi")
    private class BackgroundSpeedTesterAsyncCaller extends BackgroundSpeedTester {

        private Location loc;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... params) {

            try {
                speedTest();
                loc = locationManager.getLastKnownLocation(Config.LOC_MANAGER);
                WifiManager wifiManager = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiInfo = wifiManager.getConnectionInfo();
                ClientDeviceMessage message = MessageBuilder.buildMessage(loc, wifiInfo, deviceId, totalResult);
                if (httpPost) {
                    HttpPostSender sender = new HttpPostSender(ipV4, buffer);
                    sender.publish(message);
//                    clientWrapper.publish(message);
                    locationStorage.postValue(loc);
                    speedStorage.postValue(totalResult);
                } else {
                    bufferLocation(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private void bufferLocation(ClientDeviceMessage message) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                String payload = mapper.writeValueAsString(message);
                buffer.insertValue(payload);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (wifiInfo == null) {
                Utils.alertBox("Network error", "Network connection is off!", mainActivity);
                return;
            }
        }

    }


}
