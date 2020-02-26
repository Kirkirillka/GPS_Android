package de.tu_ilmenau.gpstracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.tu_ilmenau.gpstracker.utils.ClientDeviceMessageFields;

/**
 * This is message class which contains coordinates
 */
public class Coordinate {
    public Coordinate() {
    }

    @JsonProperty(ClientDeviceMessageFields.LONGITUDE)
    private double longitude;

    @JsonProperty(ClientDeviceMessageFields.LATITUDE)
    private double latitude;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
