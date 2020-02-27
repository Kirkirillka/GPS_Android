package de.tu_ilmenau.gpstracker.sender;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

import de.tu_ilmenau.gpstracker.model.ClientDeviceMessage;

public interface Sender {
    void publish(ClientDeviceMessage clientMessage) throws IOException;

    void disconnect() throws MqttException;
}
