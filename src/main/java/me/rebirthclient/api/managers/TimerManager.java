package me.rebirthclient.api.managers;

public class TimerManager {

    public float timer = 1;

    public void set(float factor) {
        if (factor < 0.1f) factor = 0.1f;
        timer = factor;
    }

    public void reset() {
        timer = 1;
    }

    public float get() {
        return timer;
    }
}

