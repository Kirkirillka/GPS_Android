package de.tu_ilmenau.gpstracker.storage;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SpeedTester {

    private static final String TEST_IMG = "http://icons.iconarchive.com/icons/danleech/simple/128/android-icon.png";

    public static double test() throws IOException {
        long startTime = System.currentTimeMillis();
        URL url = new URL(TEST_IMG);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        final long contentLength = connection.getContentLength();
        long endTime = System.currentTimeMillis();
        double kilobits = contentLength / 1024 * 8; //kilobits
        double seconds = (endTime - startTime) / 1000.0;
        return Math.round(kilobits / seconds * 100.0) / 100.0;  //kilobits-per-second (Kbps)
    }

}
