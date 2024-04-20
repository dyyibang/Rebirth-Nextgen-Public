package me.rebirthclient.api.util;

public class MathUtil {
    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }
    public static double clamp(double value, double min, double max) {
        if (value < min) return min;
        return Math.min(value, max);
    }
    public static double square(double input) {
        return input * input;
    }

    public static float random(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }
    public static double random(double min, double max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public static float rad(float angle) {
        return (float) (angle * Math.PI / 180);
    }
}
