package de.tu_ilmenau.gpstracker.gps;

import android.location.Location;

import java.util.Map;

public class LastLocationStore {
    private static Map<String, Location> locationMap;

    public static Location getLastLocation(String provider) {
       return locationMap.get(provider);
    }

    public static void updateLocation(String provider, Location location) {
        locationMap.put(provider, location);
    }
}
