package de.tu_ilmenau.gpstracker.mqtt;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import de.tu_ilmenau.gpstracker.database.BufferValue;
import de.tu_ilmenau.gpstracker.database.SqliteBuffer;
import de.tu_ilmenau.gpstracker.dbModel.ClientDeviceMessage;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class MqttClientWrapper {
    private Logger LOG = LoggerFactory.getLogger(MqttClientWrapper.class);

    private static Map<String, MqttClientWrapper> connections = new HashMap<>();

    private MqttClient client;
    Context context;
    private SqliteBuffer buffer;


    String serverIp = "10.48.226.193";//TODO add ip address and port
    final String port = "1883";
    final String protocol = "tcp";

    final String clientId = "Test";
    final String subscriptionTopic = "/messages/";

    final String username = "user";
    final String password = "password";

    protected ServiceConnection serverConn;

    public static MqttClientWrapper getInstance(Context context, String serverIp, SqliteBuffer buffer
    ) {
        MqttClientWrapper clientWrapper = connections.get(serverIp);
        if (clientWrapper == null) {
            try {
                clientWrapper = new MqttClientWrapper(context, serverIp, buffer);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            clientWrapper.setContext(context);
        }
        return clientWrapper;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public MqttClientWrapper(Context context, String serverIp, SqliteBuffer buffer) throws MqttException {
        this.context = context;
        this.buffer = buffer;
        this.serverIp = serverIp;
        String clientId = MqttClient.generateClientId();
        String serverUri = String.format("%s://%s:%s", protocol, this.serverIp, port);
        client = new MqttClient(serverUri, clientId, new MemoryPersistence());
        init();
//        connect();
    }

    public void init() {
        serverConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                connect();
                LOG.debug("onServiceConnected");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                try {
                    disconnect();
                } catch (MqttException e) {
                    Toast.makeText(context, "Something went wrong!" + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }//                Log.d(LOG_TAG, "onServiceDisconnected");
                LOG.debug("onServiceDisconnected");
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
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        try {
            client.connect(options);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    cause.printStackTrace();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("read good");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("push good");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(ClientDeviceMessage clientMessage) throws JsonProcessingException {
        if (!isConnected()) {
            LOG.debug("MQTT", "connection closed");
        }
        ObjectMapper mapper = new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setDateFormat(new StdDateFormat().withColonInTimeZone(true))
                .setTimeZone(TimeZone.getDefault());
        String payload = mapper.writeValueAsString(clientMessage);
        byte[] encodedPayload = new byte[0];
        try {
            if (buffer.getCount() > 0) {
                List<BufferValue> all = buffer.getAll();
                List<Integer> ids = new ArrayList<>(all.size());
                for (BufferValue val : all) {
                    encodedPayload = val.getValue().getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    client.publish(subscriptionTopic, message);
                    ids.add(val.getId());
                    LOG.debug("pushed:  ", payload);
                }
                buffer.delete(ids);
            }
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(subscriptionTopic, message);
            LOG.debug("payload:  ", payload);
        } catch (UnsupportedEncodingException | MqttException e) {
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

    public boolean isConnected() {
        return client.isConnected();
    }

    public ServiceConnection getServerConn() {
        return serverConn;
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
