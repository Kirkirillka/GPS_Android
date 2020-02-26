package de.tu_ilmenau.gpstracker.sender;

import java.io.IOException;

import de.tu_ilmenau.gpstracker.model.ClientDeviceMessage;

public interface Sender {
    void publish(ClientDeviceMessage clientMessage) throws IOException;
}
