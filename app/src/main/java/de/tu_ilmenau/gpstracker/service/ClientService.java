package de.tu_ilmenau.gpstracker.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import de.tu_ilmenau.gpstracker.Config;
import de.tu_ilmenau.gpstracker.database.SqliteBuffer;
import de.tu_ilmenau.gpstracker.listener.ContLocationListener;
import de.tu_ilmenau.gpstracker.sender.Sender;
import de.tu_ilmenau.gpstracker.sender.senderImpl.HttpPostSender;

import static de.tu_ilmenau.gpstracker.Config.MIN_DISTANCE_CHANGE_FOR_UPDATES;


@SuppressLint("Registered")
public class ClientService extends Service {

    private Logger LOG = LoggerFactory.getLogger(ClientService.class);

    private LocationListener locationListener;
    private Sender sender;
    private LocationManager locationManager;
    private SqliteBuffer buffer;

    public ClientService() {
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
        boolean httpUse = intent.getBooleanExtra("httpUse", true);
        try {
            buffer = new SqliteBuffer(this);

            sender = new HttpPostSender(ip, buffer);

            int timeoutVal = intent.getIntExtra("timeout", 0);
            String deviceId = intent.getStringExtra("device");
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            locationListener = new ContLocationListener(sender, deviceId, wifiManager);
            locationManager.requestLocationUpdates(Config.LOC_MANAGER,
                    timeoutVal * 1000, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return i;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        if (sender != null) {
            try {
                sender.disconnect();
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }
        LOG.info("service stopped..." + new Date());
    }

}
