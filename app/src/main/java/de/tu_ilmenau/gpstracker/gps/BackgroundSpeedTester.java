package de.tu_ilmenau.gpstracker.gps;

import android.annotation.SuppressLint;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;

import java.math.BigDecimal;

import de.tu_ilmenau.gpstracker.Config;
import de.tu_ilmenau.gpstracker.SpeedTestResult;
import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class BackgroundSpeedTester extends AsyncTask<Void, Void, Void> {

    double downSpeed = 0.0;
    double upSpeed = 0.0;
    WifiInfo wifiInfo;

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
        final SpeedTestResult result = new SpeedTestResult();
        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

            @Override
            public void onCompletion(SpeedTestReport report) {
                // called when download/upload is complete
                synchronized (result) {
                    result.setSpeed(report.getTransferRateBit());
                    result.setFinish(true);
                }
                System.out.println("[COMPLETED] rate in bit/s   : " + report.getTransferRateBit());

            }

            @Override
            public void onError(SpeedTestError speedTestError, String errorMessage) {
                synchronized (result) {
                    result.setSpeed(BigDecimal.ZERO);
                    result.setFinish(true);
                }
                // called when a download/upload error occur

            }

            @Override
            public void onProgress(float percent, SpeedTestReport report) {
                // called to notify download/upload progress
                System.out.println("[PROGRESS] progress : " + percent + "%");
            }
        });
        speedTestSocket.startDownload(Config.DOWNLOAD_URL);
        while (!result.isFinish()) {
            synchronized (result) {

            }
        }
        downSpeed = result.getSpeed().doubleValue() / 1024;
        result.setFinish(false);
        speedTestSocket.startUpload(Config.UPLOAD_URL, 1000000);
        while (!result.isFinish()) {
            synchronized (result) {

            }
        }
        upSpeed = result.getSpeed().doubleValue() / 1024;
    }

    @Override
    protected void onPostExecute(Void result) {
    }
}
