package de.tu_ilmenau.gpstracker.sender.senderImpl;

import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import de.tu_ilmenau.gpstracker.Config;
import de.tu_ilmenau.gpstracker.database.SqliteBuffer;
import de.tu_ilmenau.gpstracker.model.BufferValue;
import de.tu_ilmenau.gpstracker.model.ClientDeviceMessage;
import de.tu_ilmenau.gpstracker.sender.Sender;
import fr.bmartel.protocol.http.constants.HttpMethod;

public class HttpPostSender implements Sender {
    private static Logger LOG = LoggerFactory.getLogger(HttpPostSender.class);
    private String serverIp;
    private SqliteBuffer buffer;

    public HttpPostSender(String serverIp, SqliteBuffer buffer) {
        this.serverIp = serverIp;
        this.buffer = buffer;
    }

    public boolean publish(ClientDeviceMessage clientMessage) {
        String payload = "";

        ObjectMapper mapper = new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setDateFormat(new StdDateFormat().withColonInTimeZone(true))
                .setTimeZone(TimeZone.getDefault());

        try {
            payload = mapper.writeValueAsString(clientMessage);
            if (buffer.getCount() > 0) {
                List<BufferValue> all = buffer.getAll();
                List<Integer> ids = new ArrayList<>(all.size());
                for (BufferValue val : all) {
                    internalPublish(val.getValue(), serverIp);
                    ids.add(val.getId());
                    LOG.info("pushed:  " + payload);
                }
                buffer.delete(ids);
            }

            boolean isSent = internalPublish(payload, serverIp);
            if (!isSent) {
                LOG.error("Cannot send a message to the provided address. Save to local DB to send the next time.");
                buffer.insertValue(payload);

                return false;
            } else {
                LOG.info("A new message successfully sent");
                LOG.debug("payload:  " + payload);
                return true;
            }
        } catch (JsonProcessingException e) {
            LOG.error("Cannot process the message, wrong format!");
            return false;
        }
    }

    private boolean internalPublish(String payload, String serverIp) {

        try {
            URL url = new URL(String.format("http://%s", serverIp + Config.HTTP_POST_URL));

            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setRequestMethod(HttpMethod.POST_REQUEST);
            httpCon.setRequestProperty("Content-Type", "application/json");
            httpCon.setRequestProperty("Accept", "application/json");
            OutputStreamWriter out = new OutputStreamWriter(
                    httpCon.getOutputStream());
            out.write(payload);
            out.close();
            httpCon.connect();
            httpCon.getResponseMessage();

            return true;
        } catch (IOException e) {
            return false;
        }
    }


    @Override
    public void disconnect() {

    }
}
