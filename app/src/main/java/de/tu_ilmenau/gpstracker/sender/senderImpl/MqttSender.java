package de.tu_ilmenau.gpstracker.sender.senderImpl;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import de.tu_ilmenau.gpstracker.Config;
import de.tu_ilmenau.gpstracker.model.BufferValue;
import de.tu_ilmenau.gpstracker.database.SqliteBuffer;
import de.tu_ilmenau.gpstracker.model.ClientDeviceMessage;
import de.tu_ilmenau.gpstracker.sender.Sender;

/**
 * Mqtt client for push data to selecting server
 */
public class MqttSender implements Sender {
    private static Logger LOG = LoggerFactory.getLogger(MqttSender.class);
    private static Map<String, MqttSender> connections = new HashMap<>();

    private MqttClient client;
    private Context context;
    private SqliteBuffer buffer;

    private String serverIp = "10.48.226.193";//TODO add ip address and port
    private final String clientId = "Test";

    protected ServiceConnection serverConn;

    public static MqttSender getInstance(Context context, String serverIp, SqliteBuffer buffer
    ) {
        MqttSender clientWrapper = connections.get(serverIp);
        if (clientWrapper == null) {
            try {
                clientWrapper = new MqttSender(context, serverIp, buffer);
                connections.put(serverIp, clientWrapper);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        } else {
            clientWrapper.setContext(context);
        }
        return clientWrapper;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private MqttSender(Context context, String serverIp, SqliteBuffer buffer) throws MqttException {
        this.context = context;
        this.buffer = buffer;
        this.serverIp = serverIp;
        String clientId = MqttClient.generateClientId();
        String serverUri = String.format("%s://%s:%s", Config.MQTT_PROTOCOL, this.serverIp, Config.MQTT_PORT);
        client = new MqttClient(serverUri, clientId, new MemoryPersistence());
        init();
    }

    public void init() {
        serverConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                connect();
                LOG.info("onServiceConnected");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                try {
                    disconnect();
                } catch (Exception e) {
                    Toast.makeText(context, "Something went wrong!" + e.getMessage(), Toast.LENGTH_LONG).show();
                    LOG.error(e.getMessage());
                }
                LOG.info("onServiceDisconnected");
            }
        };
    }

    public void disconnect() throws MqttException {
        if (client != null) {
            client.disconnect(0);
        }
    }

    public void connect() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        options.setCleanSession(false);
        options.setUserName(Config.MQTT_USER);
        options.setPassword(Config.MQTT_PASSWORD.toCharArray());
        try {
            client.connect(options);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    LOG.info("connection lost" + cause.getMessage());
                    cause.printStackTrace();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("read good");
                    LOG.info("message arrived");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    LOG.info("delivery completed");
                    System.out.println("push good");
                }
            });
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    public void publish(ClientDeviceMessage clientMessage) throws JsonProcessingException {
        if (!isConnected()) {
            LOG.info("MQTT connection closed");
        }
        ObjectMapper mapper = new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setDateFormat(new StdDateFormat().withColonInTimeZone(true))
                .setTimeZone(TimeZone.getDefault());
        String payload = mapper.writeValueAsString(clientMessage);

        try {
            if (buffer.getCount() > 0) {
                List<BufferValue> all = buffer.getAll();
                List<Integer> ids = new ArrayList<>(all.size());
                for (BufferValue val : all) {
                    LOG.info("message sended:  " + val.getValue());
                    internalPublish(val.getValue());
                    ids.add(val.getId());
                }
                buffer.delete(ids);
            }
            internalPublish(payload);
            LOG.info("message sended:  " + payload);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            buffer.insertValue(payload);
            try {
                client.disconnect();
                client.connect();
            } catch (MqttException ex) {
                LOG.error(ex.getMessage());
            }
        }
    }

    private void internalPublish(String payload) throws IOException, MqttException {
        byte[] encodedPayload = new byte[0];
        encodedPayload = payload.getBytes("UTF-8");
        MqttMessage message = new MqttMessage(encodedPayload);
        message.setQos(Config.MQQTT_QOS);
        client.publish(Config.MQTT_TOPIC, message);
    }

    public boolean isConnected() {
        return client.isConnected();
    }


    public class MqttClientService extends Service {

        public MqttClientService() {
            super();
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onStart(Intent intent, int startId) {
            connect();
            super.onStart(intent, startId);
        }

        @Override
        public void onDestroy() {
            try {
                disconnect();
            } catch (MqttException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}
