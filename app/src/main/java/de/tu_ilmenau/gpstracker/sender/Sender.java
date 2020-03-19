package de.tu_ilmenau.gpstracker.sender;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

import de.tu_ilmenau.gpstracker.model.ClientDeviceMessage;

public interface Sender {
    boolean publish(ClientDeviceMessage clientMessage);

    void disconnect() throws MqttException;
}
