package thesis.utils;

public class DoubleToolkit {
    private final static double EPSILON = 1e-9;

    private DoubleToolkit() {}

    /**
     * Performs a comparison between two double values. Due to possible imprecisions the values may have an error even though they should be equal and so this method aims to mitigate that
     * @param a
     * @param b
     * @return true if the values are close enough to be considered equal, false otherwise
     */
    public static boolean isEqual(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }
}
