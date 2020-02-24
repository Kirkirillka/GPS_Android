package de.tu_ilmenau.gpstracker.storage;

public class LastLocationStorage {
    private String longitude;
    private String latitude;
    private String downSpeed;
    private String upSpeed;
    private boolean changed;
    private static LastLocationStorage instance;

    public static LastLocationStorage getInstance() {
        if (instance == null) {
            synchronized (LastLocationStorage.class) {
                if (instance == null) {
                    instance = new LastLocationStorage();
                }
            }
        }
        return instance;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getDownSpeed() {
        return downSpeed;
    }

    public void setDownSpeed(String downSpeed) {
        this.downSpeed = downSpeed;
    }

    public String getUpSpeed() {
        return upSpeed;
    }

    public void setUpSpeed(String upSpeed) {
        this.upSpeed = upSpeed;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}
