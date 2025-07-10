package thesis.utils;

public class BitToolkit {
    private BitToolkit() {}

    /**
     * Fills all the lowers bits starting from the most significant bit at 1
     * The authors of this method are Edon Gashi and Kadri Sylejmani
     * source: https://github.com/edongashi/itc-2019
     * @param value The value of which its lower bits are to be filled
     * @return All the lower bits at 1
     */
    public static int fillLowerBits(int value) {
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        return value;
    }

    /**
     * Finds the most significant bit in a value.
     * The authors of this method are Edon Gashi and Kadri Sylejmani
     * source: https://github.com/edongashi/itc-2019
     * @param value The value of which the most significant bit is to be found
     * @return Only the most significant bit
     */
    public static int mostSignificantBit(int value) {
        value = fillLowerBits(value);

        return value & ~(value >> 1);
    }

    public static int shiftLeft(int value, int bits) {
        int valueBits = Integer.highestOneBit(value);

        return value << (bits - valueBits);
    }
}
