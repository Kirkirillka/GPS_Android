package de.tu_ilmenau.gpstracker.tasks;

import android.annotation.SuppressLint;
import android.location.Location;
import android.net.wifi.WifiInfo;


import de.tu_ilmenau.gpstracker.utils.MessageBuilder;
import de.tu_ilmenau.gpstracker.model.ClientDeviceMessage;
import de.tu_ilmenau.gpstracker.sender.Sender;
import de.tu_ilmenau.gpstracker.mvp.GPSTrackerModel;

public class SenderCaller extends SpeedTester {

    private WifiInfo wifiInfo;
    private Sender sender;
    private String deviceId;

    public SenderCaller(WifiInfo wifiInfo, Sender sender, String deviceId) {
        this.wifiInfo = wifiInfo;
        this.sender = sender;
        this.deviceId = deviceId;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected Void doInBackground(Void... params) {

        try {
            Boolean needTestSpeed = GPSTrackerModel.isSpeedTestEnabled.getValue();
            if (Boolean.TRUE.equals(needTestSpeed)) {
                speedTest();
            }
            // prepare message to send
            ClientDeviceMessage message = MessageBuilder.buildMessage(wifiInfo, deviceId, totalResult);

            // update view-model
            GPSTrackerModel.speed.postValue(totalResult);

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