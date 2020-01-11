package de.tu_ilmenau.gpstracker.mqtt;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.eclipse.paho.client.mqttv3.MqttException;

import de.tu_ilmenau.gpstracker.gps.ContLocationListener;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;


@SuppressLint("Registered")
public class MqttClientService extends Service {

    private LocationListener locationListener;
    private MqttClientWrapper clientWrapper;
    private LocationManager locationManager;

    public MqttClientService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int i = super.onStartCommand(intent, flags, startId);
        String ip = intent.getStringExtra("IP");
        try {
            clientWrapper = new MqttClientWrapper(getApplicationContext(), ip);
            clientWrapper.connect();
            int timeoutVal = intent.getIntExtra("timeout", 0);
            String deviceId = intent.getStringExtra("device");
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            locationListener = new ContLocationListener(clientWrapper, deviceId, wifiManager);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    timeoutVal * 1000, 0, locationListener);
        } catch (MqttException e) {
            e.printStackTrace(); //Todo
        }
        return i;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        if (clientWrapper != null && clientWrapper.isConnected()) {
            try {
                clientWrapper.disconnect();
            } catch (MqttException e) {
                e.printStackTrace(); //TODO fixMe
            }
        }
        Log.i(TAG, "onCreate() , service stopped...");

    }

}
