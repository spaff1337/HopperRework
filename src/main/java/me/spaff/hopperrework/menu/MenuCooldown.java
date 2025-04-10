package me.spaff.hopperrework.menu;

public class MenuCooldown {
    private long nowMilliseconds;
    private int delayMilliseconds;

    public void setOnCooldown(int delayMilliseconds) {
        this.delayMilliseconds = delayMilliseconds;
        this.nowMilliseconds = System.currentTimeMillis();
    }

    public boolean isOnCooldown() {
        return !((System.currentTimeMillis() - nowMilliseconds) >= delayMilliseconds);
    }
}
