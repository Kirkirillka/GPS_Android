package de.tu_ilmenau.gpstracker.tasks;

import android.annotation.SuppressLint;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tu_ilmenau.gpstracker.Config;
import de.tu_ilmenau.gpstracker.model.SpeedTestTotalResult;
import de.tu_ilmenau.gpstracker.storage.StateStorage;
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

    protected void doUplinkTest(){

        String value = StateStorage.speedIpAddr.getValue();
        SpeedTestSocket speedTestSocket = new SpeedTestSocket();

        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

            @Override
            public void onCompletion(SpeedTestReport report) {
                // called when download/upload is complete
                synchronized (totalResult) {

                    double rate = report.getTransferRateBit().doubleValue() / 8 / 1024;

                    totalResult.setUpSpeed(rate);
                    totalResult.setUpSpeedReady(true);
                }
                LOG.info("[COMPLETED] rate in bit/s   : " + report.getTransferRateBit());
            }

            @Override
            public void onError(SpeedTestError speedTestError, String errorMessage) {
                synchronized (totalResult) {

                    totalResult.setUpSpeed(0);
                    totalResult.setUpSpeedReady(true);
                }
                LOG.info("[FAILED] upload: " + errorMessage);
                // called when a download/upload error occur

            }

            @Override
            public void onProgress(float percent, SpeedTestReport report) {
                // called to notify download/upload progress
                LOG.debug("[PROGRESS] progress : " + percent + "%");
            }
        });

        String fileName = SpeedTestUtils.generateFileName() + ".txt";
        LOG.info("upload start");
        speedTestSocket.startUpload(String.format(Config.UPLOAD_TEMPL, value) + "/" + fileName, 1000000);
    }


    protected void doDownlinkTest(){

        String value = StateStorage.speedIpAddr.getValue();
        SpeedTestSocket speedTestSocket = new SpeedTestSocket();

        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

            @Override
            public void onCompletion(SpeedTestReport report) {
                // called when download/upload is complete
                synchronized (totalResult) {

                    double rate = report.getTransferRateBit().doubleValue() / 8 / 1024;

                    totalResult.setDownSpeed(rate);
                    totalResult.setDownSpeedReady(true);
                }
                LOG.info("[COMPLETED] rate in bit/s   : " + report.getTransferRateBit());
            }

            @Override
            public void onError(SpeedTestError speedTestError, String errorMessage) {
                synchronized (totalResult) {

                    totalResult.setDownSpeed(0);
                    totalResult.setDownSpeedReady(true);
                }
                LOG.info("[FAILED] download: " + errorMessage);
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
        speedTestSocket.startDownload(String.format(Config.DOWNLOAD_URL_TEMPL, value));

    }

    protected void speedTest() {

        doDownlinkTest();

        //what until downlink test is done

        while (!totalResult.isDownlinkReady())
        {
            //wait//
        }

        doUplinkTest();

        //what until uplink test is done

        while (!totalResult.isUplinkReady())
        {
            //wait//
        }
    }

    @Override
    protected void onPostExecute(Void result) {
    }
}
