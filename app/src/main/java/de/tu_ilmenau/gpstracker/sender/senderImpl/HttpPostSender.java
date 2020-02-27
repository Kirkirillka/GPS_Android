package de.tu_ilmenau.gpstracker.sender.senderImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

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

    public void publish(ClientDeviceMessage clientMessage) throws IOException {
        ObjectMapper mapper = new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setDateFormat(new StdDateFormat().withColonInTimeZone(true))
                .setTimeZone(TimeZone.getDefault());
        String payload = mapper.writeValueAsString(clientMessage);
        try {
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
            internalPublish(payload, serverIp);
            LOG.info("payload:  " + payload);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            buffer.insertValue(payload);
        }
    }

    private void internalPublish(String payload, String serverIp) throws IOException {
        URL url = new URL(String.format("http://%s", serverIp + Config.HTTP_POST_URL));
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod(HttpMethod.POST_REQUEST);
        httpCon.setRequestProperty("Content-Type", "application/json");
        httpCon.setRequestProperty("Accept", "application/json");
        OutputStreamWriter out = new OutputStreamWriter(
                httpCon.getOutputStream());
        out.write(payload);
        out.close();
        httpCon.getInputStream();
    }

    @Override
    public void disconnect() {

    }
}
