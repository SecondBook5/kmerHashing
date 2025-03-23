package edu.jhu.algos.hashing;

/**
 * Enum representing the configuration of each supported hashing scheme (1–14).
 * Each entry defines:
 * - hashingMethod: "division" or "custom"
 * - modValue: modulo used in division hash (set to -1 for custom)
 * - bucketSize: 1 or 3
 * - strategy: collision resolution strategy
 */
public enum HashingScheme {

    SCHEME_1(1, "division", 120, 1, "linear"),
    SCHEME_2(2, "division", 120, 1, "quadratic"),
    SCHEME_3(3, "division", 120, 1, "chaining"),
    SCHEME_4(4, "division", 127, 1, "linear"),
    SCHEME_5(5, "division", 127, 1, "quadratic"),
    SCHEME_6(6, "division", 127, 1, "chaining"),
    SCHEME_7(7, "division", 113, 1, "linear"),
    SCHEME_8(8, "division", 113, 1, "quadratic"),
    SCHEME_9(9, "division", 113, 1, "chaining"),
    SCHEME_10(10, "division", 41, 3, "linear"),
    SCHEME_11(11, "division", 41, 3, "quadratic"),
    SCHEME_12(12, "custom", -1, 1, "linear"),
    SCHEME_13(13, "custom", -1, 1, "quadratic"),
    SCHEME_14(14, "custom", -1, 1, "chaining");

    public final int schemeNumber;
    public final String hashingMethod;
    public final int modValue;
    public final int bucketSize;
    public final String strategy;

    HashingScheme(int schemeNumber, String hashingMethod, int modValue, int bucketSize, String strategy) {
        this.schemeNumber = schemeNumber;
        this.hashingMethod = hashingMethod;
        this.modValue = modValue;
        this.bucketSize = bucketSize;
        this.strategy = strategy;
    }

    /**
     * Returns the enum entry for a given scheme number.
     * Returns null if the number does not match any known scheme.
     */
    public static HashingScheme fromNumber(int number) {
        for (HashingScheme scheme : values()) {
            if (scheme.schemeNumber == number) return scheme;
        }
        return null;
    }
}
