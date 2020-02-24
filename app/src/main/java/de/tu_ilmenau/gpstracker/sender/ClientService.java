package de.tu_ilmenau.gpstracker.sender;

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
import de.tu_ilmenau.gpstracker.gps.ContLocationListener;


@SuppressLint("Registered")
public class ClientService extends Service {

    private Logger LOG = LoggerFactory.getLogger(ClientService.class);

    private LocationListener locationListener;
    private ClientWrapper clientWrapper;
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
        try {
            buffer = new SqliteBuffer(this);
            clientWrapper = new ClientWrapper(getApplicationContext(), ip, buffer);
            clientWrapper.connect();
            int timeoutVal = intent.getIntExtra("timeout", 0);
            String deviceId = intent.getStringExtra("device");
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            locationListener = new ContLocationListener(clientWrapper, deviceId, wifiManager);
            locationManager.requestLocationUpdates(Config.LOC_MANAGER,
                    timeoutVal * 1000, 0, locationListener);
        } catch (Exception e) {
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
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }
        LOG.info("service stopped..." + new Date());
    }

}
