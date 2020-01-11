package de.tu_ilmenau.gpstracker.gps;

import android.location.Location;
import android.net.wifi.WifiInfo;

import java.util.Date;

import de.tu_ilmenau.gpstracker.dbModel.ClientDeviceMessage;
import de.tu_ilmenau.gpstracker.dbModel.Device;

public class MessageBuilder {

    public static ClientDeviceMessage buildMessage(Location loc, WifiInfo wifiInfo, String deviceId, double speed) {
//        GregorianCalendar cal = new GregorianCalendar();
        Date date = new Date();
//        cal.setTime(date);
        ClientDeviceMessage.Block payload = new ClientDeviceMessage.Block();
        payload.setBssid(wifiInfo.getBSSID());
        payload.setSsid(wifiInfo.getSSID());
        payload.setSpeed(speed);
        ClientDeviceMessage.Block.Signal signal = new ClientDeviceMessage.Block.Signal();
        signal.setRssi(wifiInfo.getRssi());
        payload.setSignal(signal);
        payload.setInfoType("test");
        return new ClientDeviceMessage.Builder().latitude(loc.getLatitude())
                .longitude(loc.getLongitude()).messageType(ClientDeviceMessage.MessageType.wifi)
                .time(date.getTime())
                .device(new Device.Builder().deviceType(Device.DeviceType.handy).id(deviceId).build())
                .payload(payload)
                .build();
       /* try {
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();//TODO fixme
            return null;
        }*/
    }
}
