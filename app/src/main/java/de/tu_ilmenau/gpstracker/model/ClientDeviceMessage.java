package de.tu_ilmenau.gpstracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

import de.tu_ilmenau.gpstracker.utils.ClientDeviceMessageFields;

/**
 * This is main message which contains coordinates, payload, connection
 * upstream and downstream speed, wifi connection properties
 */
public class ClientDeviceMessage extends Coordinate {

    @JsonProperty(ClientDeviceMessageFields.TIME)
    private Date time;

    public ClientDeviceMessage() {
    }

    private ClientDeviceMessage(Builder builder) {
        setLongitude(builder.longitude);
        setLatitude(builder.latitude);
        setTime(builder.time);
        setMessageType(builder.messageType);
        setDevice(builder.device);
        setPayload(builder.payload);
    }

    public enum MessageType {
        raw,
        wifi
    }
    @JsonProperty("message_type")
    private MessageType messageType;

    @JsonProperty("device")
    private Device device;


    @JsonProperty("payload")
    private Block payload;


    public static class Block {

        @JsonProperty("info_type")
        private String infoType;

        @JsonProperty("ssid")
        private String ssid;

        @JsonProperty("bssid")
        private String bssid;

        @JsonProperty("signal")
        private Signal signal;

        @JsonProperty("downSpeed")
        private double downSpeed;

        @JsonProperty("upSpeed")
        private double upSpeed;

        public static class Signal {

            @JsonProperty("rssi")
            private int rssi;

            public int getRssi() {
                return rssi;
            }

            public void setRssi(int rssi) {
                this.rssi = rssi;
            }
        }

        public String getInfoType() {
            return infoType;
        }

        public void setInfoType(String infoType) {
            this.infoType = infoType;
        }

        public String getSsid() {
            return ssid;
        }

        public void setSsid(String ssid) {
            this.ssid = ssid;
        }

        public String getBssid() {
            return bssid;
        }

        public void setBssid(String bssid) {
            if (bssid.matches("^([0-9A-Fa-f]{2}[\\.:-]){5}([0-9A-Fa-f]{2})$")) this.bssid = bssid;
        }

        public Signal getSignal() {
            return signal;
        }

        public void setSignal(Signal signal) {
            this.signal = signal;
        }

        public double getDownSpeed() {
            return downSpeed;
        }

        public void setDownSpeed(double downSpeed) {
            this.downSpeed = downSpeed;
        }

        public double getUpSpeed() {
            return upSpeed;
        }

        public void setUpSpeed(double upSpeed) {
            this.upSpeed = upSpeed;
        }
    }


    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Block getPayload() {
        return payload;
    }

    public void setPayload(Block payload) {
        this.payload = payload;
    }

    public static final class Builder {
        private double longitude;
        private double latitude;
        private Date time;
        private MessageType messageType;
        private Device device;
        private Block payload;

        public Builder() {
        }

        public Builder longitude(double val) {
            longitude = val;
            return this;
        }

        public Builder latitude(double val) {
            latitude = val;
            return this;
        }

        public Builder time(Date val) {
            time = val;
            return this;
        }

        public Builder messageType(MessageType val) {
            messageType = val;
            return this;
        }

        public Builder device(Device val) {
            device = val;
            return this;
        }

        public Builder payload(Block val) {
            payload = val;
            return this;
        }

        public ClientDeviceMessage build() {
            return new ClientDeviceMessage(this);
        }
    }
}