package edu.jhu.algos.hashing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * InputGenerator generates test files for five input categories.
 * It writes one key per line and logs debug output for collision detection.
 */
public class InputGenerator {

    private static final int TABLE_SIZE = 120;
    private static final String OUTPUT_DIR = "input/";
    private static final Random random = new Random(42); // deterministic seed

    public static void main(String[] args) {
        ensureDirectory(OUTPUT_DIR);

        generateFixedSizeInputs();
        generateForcedCollisions();
        generateEdgeCaseFiles();
        generateInvalidInputFiles();
        generateSpecialConfigs();
    }

    // ------------------ 1. Fixed Size Inputs ------------------

    private static void generateFixedSizeInputs() {
        generateRandomInput("input_36.txt", 36);
        generateRandomInput("input_84.txt", 84);
        generateRandomInput("input_108.txt", 108);
        generateRandomInput("input_120.txt", 120);
        writeToFile("input_empty.txt", Collections.emptyList());
    }

    private static void generateRandomInput(String fileName, int count) {
        Set<Integer> keys = new LinkedHashSet<>();
        while (keys.size() < count) {
            int value = random.nextInt(900_000) + 10000; // random 5–6 digit numbers
            keys.add(value);
        }
        writeToFile(fileName, keys);
    }

    // ------------------ 2. Forced Collisions ------------------

    private static void generateForcedCollisions() {
        generateSmartCollisions("division_collisions_scheme1.txt", new DivisionHashTable(TABLE_SIZE, 1, 120, "linear", false));
        generateSmartCollisions("division_collisions_scheme2.txt", new DivisionHashTable(TABLE_SIZE, 1, 120, "quadratic", false));
        generateSmartCollisions("division_collisions_scheme3.txt", new DivisionHashTable(TABLE_SIZE, 1, 120, "chaining", false));

        generateSmartCollisions("custom_collisions_scheme12.txt", new CustomHashTable(TABLE_SIZE, 1, "linear", false));
        generateSmartCollisions("custom_collisions_scheme13.txt", new CustomHashTable(TABLE_SIZE, 1, "quadratic", false));
        generateSmartCollisions("custom_collisions_scheme14.txt", new CustomHashTable(TABLE_SIZE, 1, "chaining", false));
    }

    private static void generateSmartCollisions(String filename, HashTable table) {
        Map<Integer, List<Integer>> map = new HashMap<>();
        Set<Integer> result = new LinkedHashSet<>();

        for (int key = 1; key < 30000 && result.size() < 60; key++) {
            int h = table.hash(key);
            map.computeIfAbsent(h, k -> new ArrayList<>()).add(key);

            List<Integer> bucket = map.get(h);
            if (bucket.size() == 3 && !result.containsAll(bucket)) {
                result.addAll(bucket);
                System.out.printf("[DEBUG] Collision at index %d: %s%n", h, bucket);
            }
        }

        writeToFile(filename, result);
    }

    // ------------------ 3. Edge Case Files ------------------

    private static void generateEdgeCaseFiles() {
        writeToFile("input_repeated_keys.txt", Collections.nCopies(60, 999));

        Set<Integer> nearFull = new LinkedHashSet<>();
        for (int i = 1000; i < 1119; i++) nearFull.add(i);
        writeToFile("input_near_capacity.txt", nearFull);

        writeToFile("input_boundary_keys.txt", Arrays.asList(
                Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE
        ));

        writeToFile("input_gaps.txt", Arrays.asList(10, 1000, 10000, 25000, 50000, 100000));

        Set<Integer> highLoadWithCollisions = new LinkedHashSet<>();
        for (int i = 1; i <= 110; i++) highLoadWithCollisions.add(i);
        int base = 1000;
        while (highLoadWithCollisions.size() < 115) {
            highLoadWithCollisions.add(base);
            highLoadWithCollisions.add(base + 113);
            base += 500;
        }
        writeToFile("input_highload_collisions.txt", highLoadWithCollisions);
    }

    // ------------------ 4. Invalid Input Files ------------------

    private static void generateInvalidInputFiles() {
        writeRaw("input_non_integer.txt", Arrays.asList("abc", "42", "!", "%%", "NaN", "999"));
        writeRaw("input_corrupt.txt", Arrays.asList(
                "", "     ", "@@@@", "NULL", "42a", "a42", "1-23", "123-", "9999,", ",999", "12.34"
        ));
        writeRaw("input_missing_commas.txt", Arrays.asList(
                "100 200 300", "400 500", "600", "700\t800", "900|1000"
        ));
        writeRaw("input_overflow.txt", Arrays.asList(
                String.valueOf(Long.MAX_VALUE),
                "9223372036854775807",
                "2147483648",
                "-2147483649",
                "-9999999999999999999"
        ));
    }

    private static void writeRaw(String filename, List<String> lines) {
        File file = new File(OUTPUT_DIR + filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) writer.write(line + "\n");
            System.out.printf("✔ Generated %s (%d lines)%n", filename, lines.size());
        } catch (IOException e) {
            System.err.printf("✘ Failed to write %s (%s)%n", filename, e.getMessage());
        }
    }

    // ------------------ 5. Special Stress Configs ------------------

    private static void generateSpecialConfigs() {
        List<Integer> stress = new ArrayList<>();
        int base = 9999;
        for (int i = 0; i < 119; i++) {
            stress.add(base + i * 7919);
        }
        writeToFile("input_probe_stress.txt", stress);
    }

    // ------------------ Utilities ------------------

    private static void writeToFile(String fileName, Collection<Integer> keys) {
        File file = new File(OUTPUT_DIR + fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Integer key : keys) {
                writer.write(String.valueOf(key));
                writer.newLine();
            }
            System.out.printf("✔ Generated %s (%d keys)%n", fileName, keys.size());
        } catch (IOException e) {
            System.err.printf("✘ Failed to write %s (%s)%n", fileName, e.getMessage());
        }
    }

    private static void ensureDirectory(String dirName) {
        File dir = new File(dirName);
        if (!dir.exists() && dir.mkdirs()) {
            System.out.println("Created input directory: " + dirName);
        }
    }
}
