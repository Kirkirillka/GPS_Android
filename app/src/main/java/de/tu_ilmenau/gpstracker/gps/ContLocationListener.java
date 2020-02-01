package de.tu_ilmenau.gpstracker.gps;

import android.annotation.SuppressLint;

import android.location.Location;
import android.location.LocationListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import java.math.BigDecimal;

import de.tu_ilmenau.gpstracker.SpeedTestResult;
import de.tu_ilmenau.gpstracker.mqtt.MqttClientWrapper;
import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class ContLocationListener implements LocationListener {

    private MqttClientWrapper clientWrapper;
    private String deviceId;
    private WifiManager wifiManager;
    private Location loc;

    public ContLocationListener(MqttClientWrapper clientWrapper, String deviceId, WifiManager wifiManager) {
        this.wifiManager = wifiManager;
        this.clientWrapper = clientWrapper;
        this.deviceId = deviceId;
    }


    @Override
    public void onLocationChanged(Location loc) {
        this.loc = loc;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            new AsyncCaller().execute();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @SuppressLint("NewApi")
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {
        private double downSpeed = 0.0;
        private double upSpeed = 0.0;
        private WifiInfo wifiInfo;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... params) {

            try {
                speedTest();
//                downSpeed = SpeedTester.test();
                wifiInfo = wifiManager.getConnectionInfo();
                clientWrapper.publish(MessageBuilder.buildMessage(loc, wifiInfo, deviceId, downSpeed, upSpeed));
            } catch (Exception e) {
                e.printStackTrace();
                downSpeed = 0;
            }
            return null;
        }
        private void speedTest() {
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
                    System.out.println("[COMPLETED] rate in octet/s : " + report.getTransferRateOctet());
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
                    System.out.println("[PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                    System.out.println("[PROGRESS] rate in bit/s   : " + report.getTransferRateBit());
                }
            });
            speedTestSocket.startDownload("http://ipv4.ikoula.testdebit.info/1M.iso");
            while (!result.isFinish()) {
                synchronized (result) {

                }
            }
            downSpeed = result.getSpeed().doubleValue() / 1024;
            result.setFinish(false);
            speedTestSocket.startUpload("http://ipv4.ikoula.testdebit.info/", 1000000);
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
}