package ua.myxazaur.vintagecam.utils;

public class MathUtils {
    private static final double THRESHOLD = 0.001;

    public static double damp(double current, double target, double smoothing, double deltaTime) {
        return current + (target - current) * smoothing * deltaTime;
    }

    public static double stepTowards(double current, double target, double step) {
        double diff = target - current;
        // Fast return if close enough
        if (Math.abs(diff) < THRESHOLD) {
            return target;
        }
        return current + diff * step;
    }


    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp01(double value) {
        return clamp(value, 0.0, 1.0);
    }
}