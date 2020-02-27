package de.tu_ilmenau.gpstracker;

import android.Manifest;
import android.location.LocationManager;

public class Config {
    public static final String HTTP_POST_URL = "/message/new";
    public static String LOC_MANAGER = LocationManager.GPS_PROVIDER;
    /**
     * Permissions that need to be explicitly requested from end user.
     */
    public static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    public final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    /*Location updating config*/
    public static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.1f; // 10 cm
    public static final long MIN_TIME_BW_UPDATES = 1000 * 2; // 2 sec

    /*Mqqt settings*/
    public static final String MQTT_PORT = "1883";
    public static final String MQTT_PROTOCOL =  "tcp";
    public static final int MQQTT_QOS = 2;
    public static final String MQTT_TOPIC = "/messages/";
    public static final String MQTT_USER = "user";
    public static final String MQTT_PASSWORD = "password";

    /*Speed test config*/
    public final static String DOWNLOAD_URL = "ftp://speedtest:speedtest@192.168.10.1/1MB.zip";
    public final static String UPLOAD_URL = "ftp://speedtest:speedtest@192.168.10.1/upload";
}
