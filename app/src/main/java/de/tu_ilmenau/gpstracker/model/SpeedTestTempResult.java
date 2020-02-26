package de.tu_ilmenau.gpstracker.model;

import java.math.BigDecimal;

/**
 * This class contains speed test result
 */
public class SpeedTestTempResult {
    private boolean finish;
    private BigDecimal speed;

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public BigDecimal getSpeed() {
        return speed;
    }

    public void setSpeed(BigDecimal speed) {
        this.speed = speed;
    }
}
