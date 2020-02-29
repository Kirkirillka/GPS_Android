package de.tu_ilmenau.gpstracker.mvp;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;

import de.tu_ilmenau.gpstracker.Config;
import de.tu_ilmenau.gpstracker.model.SpeedTestTotalResult;

/**
 * this is state storage to change data in different threads
 */
public class GPSTrackerModel {
    public static MutableLiveData<Location> location = new MutableLiveData<Location>();
    public static MutableLiveData<String> provider = new MutableLiveData<>();

    public static MutableLiveData<SpeedTestTotalResult> speed = new MutableLiveData<SpeedTestTotalResult>();
    public static MutableLiveData<Boolean> isSpeedTestEnabled = new MutableLiveData<Boolean>();
    public static MutableLiveData<String> speedTestIPAddress = new MutableLiveData<String>();

    public static MutableLiveData<String> brokerIPAddress = new MutableLiveData<>();

    public static MutableLiveData<Boolean> isPushingServiceRunning = new MutableLiveData<>();
    public static MutableLiveData<Integer> timeout = new MutableLiveData<>();


    public GPSTrackerModel(){
        provider.setValue(Config.DEFAULT_LOCATION_PROIDER);
    }
}
