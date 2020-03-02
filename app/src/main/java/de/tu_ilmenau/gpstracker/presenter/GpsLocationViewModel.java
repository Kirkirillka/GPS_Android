package de.tu_ilmenau.gpstracker.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.lifecycle.ViewModel;

import de.tu_ilmenau.gpstracker.Config;
import de.tu_ilmenau.gpstracker.database.SqliteBuffer;
import de.tu_ilmenau.gpstracker.listener.CustomLocationListener;
import de.tu_ilmenau.gpstracker.tasks.BackgroundSenderCaller;
import de.tu_ilmenau.gpstracker.service.ClientService;
import de.tu_ilmenau.gpstracker.sender.senderImpl.HttpPostSender;
import de.tu_ilmenau.gpstracker.sender.Sender;
import de.tu_ilmenau.gpstracker.utils.Utils;
import de.tu_ilmenau.gpstracker.view.MainActivity;

import static de.tu_ilmenau.gpstracker.Config.MIN_DISTANCE_CHANGE_FOR_UPDATES;
import static de.tu_ilmenau.gpstracker.Config.MIN_TIME_BW_UPDATES;

/**
 * Main processing class to interact with model and view, handle processes
 */
public class GpsLocationViewModel extends ViewModel {
    private String ipV4;
    private String speedTestIpV4;

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
        @SuppressLint("MissingPermission") Location loc = locationManager.getLastKnownLocation(Config.LOC_MANAGER);
        WifiManager wifiManager = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // Just use HTTP
        Sender sender = new HttpPostSender(ipV4, buffer);

        if (wifiInfo == null) {
            Utils.alertBox("Network error", "Network connection is off!", mainActivity);
            return;
        }
        new BackgroundSenderCaller(loc, wifiInfo, sender, deviceId).execute();
    }

    public void stopService() {
        final Intent intent = new Intent(mainActivity, ClientService.class);
        mainActivity.stopService(intent);
    }

    public void startService(int timeoutVal) {
        final Intent intent = new Intent(mainActivity, ClientService.class);
        intent.putExtra("IP", ipV4);
        intent.putExtra("device", deviceId);
        intent.putExtra("httpUse", httpPost);
        intent.putExtra("timeout", timeoutVal);
        mainActivity.startService(intent);
    }

    public void setIpV4(String ipV4) {
        this.ipV4 = ipV4;
    }

    public void setHttpPost(boolean httpPost) {
        this.httpPost = httpPost;
    }
}
