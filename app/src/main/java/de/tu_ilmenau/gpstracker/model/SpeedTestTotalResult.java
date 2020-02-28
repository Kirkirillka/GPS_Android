package de.tu_ilmenau.gpstracker.model;

public class SpeedTestTotalResult {
    private double upSpeed;
    private double downSpeed;

    private boolean upSpeedReady;
    private boolean downSpeedReady;

    public double getUpSpeed() {
        return Math.floor(upSpeed) ;
    }

    public void setUpSpeed(double upSpeed) {
        this.upSpeed = upSpeed;
    }

    public boolean isFinish() {
        return upSpeedReady | downSpeedReady;
    }

    public void setUpSpeedReady(boolean finish) {
        this.upSpeedReady = finish;
    }

    public boolean isUplinkReady(){
        return upSpeedReady;
    }

    public void setDownSpeedReady(boolean finish) {
        this.downSpeedReady = finish;
    }

    public boolean isDownlinkReady(){
        return downSpeedReady;
    }

    public double getDownSpeed() {
        return Math.floor(downSpeed);
    }

    public void setDownSpeed(double downSpeed) {
        this.downSpeed = downSpeed;
    }
}