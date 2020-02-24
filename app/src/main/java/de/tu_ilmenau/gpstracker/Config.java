package de.tu_ilmenau.gpstracker;

import android.location.LocationManager;

public class Config {
    public static final String HTTP_POST_URL = "/message/new";
    public static String LOC_MANAGER = LocationManager.GPS_PROVIDER;
    public final static String DOWNLOAD_URL = "ftp://speedtest:speedtest@192.168.10.1/1MB.zip";
    public final static String UPLOAD_URL = "ftp://speedtest:speedtest@192.168.10.1/upload";
}
