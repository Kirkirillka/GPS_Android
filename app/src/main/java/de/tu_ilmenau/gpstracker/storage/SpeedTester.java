package de.tu_ilmenau.gpstracker.storage;

import java.net.HttpURLConnection;
import java.net.URL;

public class SpeedTester {

    private static final String TEST_IMG = "http://hdwpro.com/wp-content/uploads/2016/12/Awesome-HD-Pic.jpg";

    public static double test() {
        try {
            long startTime = System.currentTimeMillis();
            URL url = new URL(TEST_IMG);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            final long contentLength = connection.getContentLength();
            long endTime = System.currentTimeMillis();
            double kilobits = contentLength / 1024 * 8; //kilobits
            double seconds = (endTime - startTime) / 1000.0;
            return Math.round(kilobits / seconds * 100.0) / 100.0;  //kilobits-per-second (Kbps)
        } catch (Exception io) {
            io.printStackTrace();
            return 0.0;
        }
    }

}
