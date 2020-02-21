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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tu_ilmenau.gpstracker.Config;
import de.tu_ilmenau.gpstracker.database.SqliteBuffer;
import de.tu_ilmenau.gpstracker.gps.ContLocationListener;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;


@SuppressLint("Registered")
public class MqttClientService extends Service {

    private Logger LOG = LoggerFactory.getLogger(MqttClientService.class);

    private LocationListener locationListener;
    private MqttClientWrapper clientWrapper;
    private LocationManager locationManager;
    private SqliteBuffer buffer;

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
            buffer = new SqliteBuffer(this);
            clientWrapper = new MqttClientWrapper(getApplicationContext(), ip, buffer);
            clientWrapper.connect();
            int timeoutVal = intent.getIntExtra("timeout", 0);
            String deviceId = intent.getStringExtra("device");
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            locationListener = new ContLocationListener(clientWrapper, deviceId, wifiManager);
            locationManager.requestLocationUpdates(Config.LOC_MANAGER,
                    timeoutVal * 1000, 0, locationListener);
        } catch (MqttException e) {
            LOG.error(e.getMessage());
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
                LOG.error(e.getMessage());
            }
        }
        LOG.debug("service stopped...");
    }

}
