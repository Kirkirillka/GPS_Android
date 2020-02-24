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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
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
import de.tu_ilmenau.gpstracker.database.BufferValue;
import de.tu_ilmenau.gpstracker.database.SqliteBuffer;
import de.tu_ilmenau.gpstracker.dbModel.ClientDeviceMessage;
import fr.bmartel.protocol.http.constants.HttpMethod;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class MqttClientWrapper {
    private static Logger LOG = LoggerFactory.getLogger(MqttClientWrapper.class);

    private static Map<String, MqttClientWrapper> connections = new HashMap<>();

    private MqttClient client;
    Context context;
    private SqliteBuffer buffer;
    private boolean httpSender;


    String serverIp = "10.48.226.193";//TODO add ip address and port
    final String port = "1883";
    final String protocol = "tcp";

    final int QoS = 2;

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
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        } else {
            clientWrapper.setContext(context);
        }
        return clientWrapper;
    }

    public void setHttpSender(boolean httpSender) {
        this.httpSender = httpSender;
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
                LOG.info("onServiceConnected");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                try {
                    disconnect();
                } catch (Exception e) {
                    Toast.makeText(context, "Something went wrong!" + e.getMessage(), Toast.LENGTH_LONG).show();
                    LOG.error(e.getMessage());
                }//                Log.d(LOG_TAG, "onServiceDisconnected");
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
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    public void publish(ClientDeviceMessage clientMessage) throws JsonProcessingException {
        if (!isConnected()) {
            LOG.info("MQTT", "connection closed");
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
                    internalPublish(val.getValue());
                    ids.add(val.getId());
                    LOG.info("pushed:  " + payload);
                }
                buffer.delete(ids);
            }
            internalPublish(payload);
            LOG.info("payload:  " + payload);
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
        if (httpSender) {
            URL url = new URL(this.serverIp + Config.HTTP_POST_URL);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod(HttpMethod.POST_REQUEST);
            OutputStreamWriter out = new OutputStreamWriter(
                    httpCon.getOutputStream());
            out.write(payload);
            out.close();
            httpCon.getInputStream();
        } else {
            byte[] encodedPayload = new byte[0];
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setQos(this.QoS);
            client.publish(subscriptionTopic, message);
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
