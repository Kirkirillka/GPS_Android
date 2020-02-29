package de.tu_ilmenau.gpstracker.mvp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import de.tu_ilmenau.gpstracker.Config;
import de.tu_ilmenau.gpstracker.database.SQLiteRepository;
import de.tu_ilmenau.gpstracker.model.SpeedTestTotalResult;
import de.tu_ilmenau.gpstracker.tasks.SenderCaller;
import de.tu_ilmenau.gpstracker.sender.senderImpl.HttpPostSender;
import de.tu_ilmenau.gpstracker.sender.Sender;

import static de.tu_ilmenau.gpstracker.Config.MIN_DISTANCE_CHANGE_FOR_UPDATES;
import static de.tu_ilmenau.gpstracker.Config.MIN_TIME_BW_UPDATES;

/**
 * Main processing class to interact with model and view, handle processes
 */
public class GPSTrackerPresenter extends ViewModel {

    private GPSTrackerActivity view;
    private GPSTrackerModel model;

    private LocationManager locationManager;
    private String deviceId;

    private SQLiteRepository buffer;


    public GPSTrackerPresenter(GPSTrackerActivity view, String deviceId) {
        this.view = view;
        this.model = new GPSTrackerModel();
        this.deviceId = deviceId;
        this.buffer = new SQLiteRepository(view);

        // start services
        registerGPS();
    }

    @SuppressLint("MissingPermission")
    private boolean registerGPS() {

        if (view == null) {
            return false;
        };

        locationManager = (LocationManager) view.getSystemService(view.LOCATION_SERVICE);

        String chosenLocationProvider = model.provider.getValue();

        if (locationManager.getLastKnownLocation(chosenLocationProvider) == null) {
            chosenLocationProvider = LocationManager.NETWORK_PROVIDER;
            if (locationManager.getLastKnownLocation(chosenLocationProvider) == null) {
                chosenLocationProvider = LocationManager.PASSIVE_PROVIDER;
            }
        }

        // update current provider
        model.provider.postValue(chosenLocationProvider);

        // Register updates
        locationManager.requestLocationUpdates(Config.DEFAULT_LOCATION_PROIDER,
                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        setLocation(location);
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
                });

        return true;
    }

    public boolean toggleServiceDispatching() {
        boolean isPushingContinuousWorking = model.isPushingServiceRunning.getValue();
    };

    public boolean setLocation(Location location){

        Log.d("LOCATION", "New location updates");

        model.location.postValue(location);

        return true;
    }

    public boolean setSpeed(SpeedTestTotalResult speed){
        model.speed.postValue(speed);

        return true;
    }

    public boolean pushLocation() {

        WifiManager wifiManager = (WifiManager) view.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return false;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // Just use HTTP
        String brokerAddress = model.speedTestIPAddress.getValue();

        Sender sender = new HttpPostSender(brokerAddress, buffer);

        if (wifiInfo == null) {
            return false;
        }
        new SenderCaller(wifiInfo, sender, deviceId).execute();

        return true;
    }

    public boolean setBrokerIPAddress(String address) {


        model.brokerIPAddress.postValue(address);

        return true;
    }

    public boolean setSpeedTestIPAddress(String address) {

        model.speedTestIPAddress.postValue(address);

        return true;
    }

    public void viewIsReady() {

        // Register updates        in Model on View

        // update view in case if the last location changed
        model.location.observe(view, new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                view.updateLocation(location);
            }
        });

        // update view in case if the last speed rate measurement changed
        model.speed.observe(view, new Observer<SpeedTestTotalResult>() {
            @Override
            public void onChanged(SpeedTestTotalResult speed) {
                view.updateSpeedRate(speed);
            }
        });
    }
}
