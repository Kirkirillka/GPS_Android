package de.tu_ilmenau.gpstracker.mvp;

import android.location.Location;

import de.tu_ilmenau.gpstracker.model.SpeedTestTotalResult;

public interface GPSTrackerView {

    public void init();

    public void onPushManually();

    public void togglePushing();

    public void resetTimeout();

    public void updateSpeedTestIPAddress();

    public void updateBrokerIPAddress();

    public void updateLocation(Location location);

    public void updateSpeedRate(SpeedTestTotalResult speed);


}
