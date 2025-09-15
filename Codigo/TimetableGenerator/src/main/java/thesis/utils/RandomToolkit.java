package thesis.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomToolkit {
    private RandomToolkit() {}

    /**
     * Choose a random value present in the list provided
     * @param valueList List of values of which a random value must be chosen
     * @return A random value present in the list or null if the list provided is null
     * @param <T> Type of the values provided
     */
    public static <T> T random(List<T> valueList) {
        if(valueList == null || valueList.isEmpty()) {
            return null;
        }

        return valueList.get(ThreadLocalRandom.current().nextInt(valueList.size()));
    }

    /**
     * Choose a random index from which a value is removed
     * @param valueList List of values of which a random value is removed
     * @param <T> Type of the values provided
     */
    public static <T> void removeRandom(List<T> valueList) {
        valueList.remove(ThreadLocalRandom.current().nextInt(valueList.size()));
    }

    /**
     * Generates a random value between 0 (inclusive) and 1 (exclusive)
     * @return The value generated
     */
    public static float random() {
        return ThreadLocalRandom.current().nextFloat();
    }

    /**
     * Generates a random int value between the max (exclusive) and the min (inclusive).
     * @param maxValue Int value that defines the maximum of the random value generator
     * @param minValue Int value that defines the minimum of the random value generator
     * @return The value generated
     */
    public static int random(int maxValue, int minValue) {
        if(maxValue == minValue) {
            return maxValue;
        }

        return ThreadLocalRandom.current().nextInt(maxValue-minValue) + minValue;
    }

    /**
     * Generates a random int value between the max (exclusive) and zero (inclusive).
     * @param maxValue Int value that defines the maximum of the random value generator
     * @return The value generated
     */
    public static int random(int maxValue) {
        return random(maxValue, 0);
    }

    /**
     * Generates a random long value between the max (exclusive) and the min (inclusive).
     * @param maxValue Long value that defines the maximum of the random value generator
     * @param minValue Long value that defines the minimum of the random value generator
     * @return The value generated
     */
    public static long random(long maxValue, long minValue) {
        if(maxValue == minValue) {
            return maxValue;
        }

        return ThreadLocalRandom.current().nextLong(maxValue-minValue) + minValue;
    }

    /**
     * Generates a random long value between the max (exclusive) and zero (inclusive).
     * @param maxValue Long value that defines the maximum of the random value generator
     * @return The value generated
     */
    public static long random(long maxValue) {
        return random(maxValue, 0);
    }
}
