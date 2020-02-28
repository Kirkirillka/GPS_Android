package de.tu_ilmenau.gpstracker.storage;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;

import de.tu_ilmenau.gpstracker.model.SpeedTestTotalResult;

/**
 * this is state storage to change data in different threads
 */
public class StateStorage {
    public static MutableLiveData<Location> locationStorage = new MutableLiveData<Location>();
    public static MutableLiveData<SpeedTestTotalResult> speedStorage = new MutableLiveData<SpeedTestTotalResult>();
    public static MutableLiveData<Boolean> speedFlag = new MutableLiveData<Boolean>();
    public static MutableLiveData<String> speedIpAddr = new MutableLiveData<String>();

}
