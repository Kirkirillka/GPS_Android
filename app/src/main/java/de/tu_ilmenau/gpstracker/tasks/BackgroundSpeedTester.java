package de.tu_ilmenau.gpstracker.tasks;

import android.annotation.SuppressLint;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import de.tu_ilmenau.gpstracker.Config;
import de.tu_ilmenau.gpstracker.model.SpeedTestTempResult;
import de.tu_ilmenau.gpstracker.model.SpeedTestTotalResult;
import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;
import fr.bmartel.speedtest.utils.SpeedTestUtils;

/**
 * Background speed tester
 */
public class BackgroundSpeedTester extends AsyncTask<Void, Void, Void> {
    static Logger LOG = LoggerFactory.getLogger(BackgroundSpeedTester.class);
    protected SpeedTestTotalResult totalResult = new SpeedTestTotalResult();
    protected WifiInfo wifiInfo;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected Void doInBackground(Void... params) {
        speedTest();
        return null;
    }

    protected void speedTest() {
        SpeedTestSocket speedTestSocket = new SpeedTestSocket();
        final SpeedTestTempResult result = new SpeedTestTempResult();
        totalResult = new SpeedTestTotalResult();
        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

            @Override
            public void onCompletion(SpeedTestReport report) {
                // called when download/upload is complete
                synchronized (result) {
                    result.setSpeed(report.getTransferRateBit());
                    result.setFinish(true);
                }
                LOG.info("[COMPLETED] rate in bit/s   : " + report.getTransferRateBit());
            }

            @Override
            public void onError(SpeedTestError speedTestError, String errorMessage) {
                synchronized (result) {
                    result.setSpeed(BigDecimal.ZERO);
                    result.setFinish(true);
                }
                LOG.info("[FAILED] load or download: " + errorMessage);
                // called when a download/upload error occur

            }

            @Override
            public void onProgress(float percent, SpeedTestReport report) {
                // called to notify download/upload progress
                LOG.debug("[PROGRESS] progress : " + percent + "%");
            }
        });
        LOG.info("download start");
        speedTestSocket.setSocketTimeout(5 * 1000);
        speedTestSocket.startDownload(Config.DOWNLOAD_URL);
        while (!result.isFinish()) {
            synchronized (result) {

            }
        }
        totalResult.setDownSpeed(result.getSpeed().doubleValue() / 1024.0); // Kbit/s
        result.setFinish(false);
        String fileName = SpeedTestUtils.generateFileName() + ".txt";
        LOG.info("upload start");
        speedTestSocket.startUpload(Config.UPLOAD_URL + "/" + fileName, 1000000);
        while (!result.isFinish()) {
            synchronized (result) {

            }
        }
        totalResult.setUpSpeed(result.getSpeed().doubleValue() / 1024.0); // Kbit/s
    }

    @Override
    protected void onPostExecute(Void result) {
    }
}
