package de.tu_ilmenau.gpstracker.dbModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Coordinate {
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
