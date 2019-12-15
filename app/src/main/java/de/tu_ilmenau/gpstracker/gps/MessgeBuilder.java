package de.tu_ilmenau.gpstracker.gps;

import android.location.Location;
import android.net.wifi.WifiInfo;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import de.tu_ilmenau.gpstracker.dbModel.ClientDeviceMessage;
import de.tu_ilmenau.gpstracker.dbModel.Device;

public class MessgeBuilder {

    public static ClientDeviceMessage buildMessage(Location loc, WifiInfo wifiInfo, String deviceId) {
        GregorianCalendar cal = new GregorianCalendar();
        Date date = new Date();
        cal.setTime(date);
        ClientDeviceMessage.Block payload = new ClientDeviceMessage.Block();
        payload.setBssid(wifiInfo.getBSSID());
        payload.setSsid(wifiInfo.getSSID());
        ClientDeviceMessage.Block.Signal signal = new ClientDeviceMessage.Block.Signal();
        signal.setRssi(wifiInfo.getRssi());
        payload.setSignal(signal);
        payload.setInfoType("test");
        try {
            return new ClientDeviceMessage.Builder().latitude(loc.getLatitude())
                    .longitude(loc.getLongitude()).messageType(ClientDeviceMessage.MessageType.wifi)
                    .time(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal))
                    .device(new Device.Builder().deviceType(Device.DeviceType.handy).id(deviceId).build())
                    .payload(payload)
                    .build();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();//TODO fixme
            return null;
        }
    }
}
