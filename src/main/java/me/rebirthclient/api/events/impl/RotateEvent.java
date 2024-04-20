package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;

public class RotateEvent extends Event {
    private float yaw;
    private float pitch;
    public RotateEvent(float yaw, float pitch) {
        super(Stage.Pre);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setRotation(final float yaw, final float pitch) {
        this.setYaw(yaw);
        this.setPitch(pitch);
    }
}
