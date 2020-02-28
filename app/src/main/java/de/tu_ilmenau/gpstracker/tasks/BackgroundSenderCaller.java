package de.tu_ilmenau.gpstracker.tasks;

import android.annotation.SuppressLint;
import android.location.Location;
import android.net.wifi.WifiInfo;


import de.tu_ilmenau.gpstracker.utils.MessageBuilder;
import de.tu_ilmenau.gpstracker.model.ClientDeviceMessage;
import de.tu_ilmenau.gpstracker.sender.Sender;
import de.tu_ilmenau.gpstracker.storage.StateStorage;

public class BackgroundSenderCaller extends BackgroundSpeedTester {

    private Location location;
    private WifiInfo wifiInfo;
    private Sender sender;
    private String deviceId;

    public BackgroundSenderCaller(Location location, WifiInfo wifiInfo, Sender sender, String deviceId) {
        this.location = location;
        this.wifiInfo = wifiInfo;
        this.sender = sender;
        this.deviceId = deviceId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected Void doInBackground(Void... params) {

        try {
            Boolean needTestSpeed = StateStorage.speedFlag.getValue();
            if (Boolean.TRUE.equals(needTestSpeed)) {
                speedTest();
            }
            // prepare message to send
            ClientDeviceMessage message = MessageBuilder.buildMessage(location, wifiInfo, deviceId, totalResult);
            // update view-model
            StateStorage.speedStorage.postValue(totalResult);
            StateStorage.locationStorage.postValue(location);
            // send message
            sender.publish(message);

        } catch (Exception e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }
}