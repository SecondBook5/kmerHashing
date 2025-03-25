package edu.jhu.algos;

import edu.jhu.algos.io.HashFileHandler;

import java.util.*;

/**
 * Main entry point for the Hashing Lab.
 * Supports command-line execution of predefined schemes or fully manual configurations.
 * USAGE:
 *   --scheme <1-14>
 *   OR
 *   --hashing <division|custom> --mod <int> --bucket <1|3> --strategy <linear|quadratic|chaining> [--c1 <float>] [--c2 <float>]
 * Required:
 *   --input <filename>
 *   --output <filename>
 * Optional:
 *   --debug
 */
public class Main {

    public static void main(String[] args) {
        Map<String, String> flags = parseArgs(args);

        System.out.println("DEBUG");
        System.out.println("DEBUG: args = " + Arrays.toString(args));
        System.out.println("DEBUG CLI args: " + Arrays.toString(args));
        System.out.println("DEBUG Parsed flags: " + flags);

        boolean debug = flags.containsKey("--debug");

        // -----------------------------
        // Step 1: Validate --input and --output
        // -----------------------------
        String inputFile = flags.get("--input");
        String outputFile = flags.get("--output");

        if (inputFile == null || outputFile == null) {
            System.err.println("Error: --input and --output are required.");
            printUsage();
            return;
        }

        List<Integer> keys = HashFileHandler.readFile(inputFile);
        if (keys == null || keys.isEmpty()) {
            System.err.println("Error: No valid keys found in input.");
            return;
        }

        // -----------------------------
        // Step 2: Dispatch based on CLI mode
        // -----------------------------
        if (flags.containsKey("--scheme")) {
            // MODE A: Predefined scheme (1–14)
            try {
                int schemeNumber = Integer.parseInt(flags.get("--scheme"));
                HashingDriver.runScheme(schemeNumber, keys, outputFile, debug);
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid number for --scheme.");
            }
            return;
        }

        if (flags.containsKey("--hashing") && flags.containsKey("--strategy")) {
            // MODE B: Fully manual configuration

            String method = flags.get("--hashing").toLowerCase();
            String strategy = flags.get("--strategy").toLowerCase();
            int mod = -1;

            // Mod only applies to division hashing
            if (method.equals("division")) {
                if (!flags.containsKey("--mod")) {
                    System.err.println("Error: --mod is required for division hashing.");
                    return;
                }
                try {
                    mod = Integer.parseInt(flags.get("--mod"));
                } catch (NumberFormatException e) {
                    System.err.println("Error: Invalid value for --mod.");
                    return;
                }
            }

            // Bucket size (1 or 3)
            int bucketSize = 1;
            if (flags.containsKey("--bucket")) {
                try {
                    bucketSize = Integer.parseInt(flags.get("--bucket"));
                    if (bucketSize != 1 && bucketSize != 3) {
                        System.err.println("Error: --bucket must be 1 or 3.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error: Invalid value for --bucket.");
                    return;
                }
            }

            // Optional probing constants
            double c1 = 0.5;
            double c2 = 0.5;
            if (flags.containsKey("--c1")) {
                try {
                    c1 = Double.parseDouble(flags.get("--c1"));
                } catch (NumberFormatException e) {
                    System.err.println("Error: Invalid value for --c1.");
                    return;
                }
            }
            if (flags.containsKey("--c2")) {
                try {
                    c2 = Double.parseDouble(flags.get("--c2"));
                } catch (NumberFormatException e) {
                    System.err.println("Error: Invalid value for --c2.");
                    return;
                }
            }

            // Run manually configured table
            HashingDriver.runManual(method, mod, bucketSize, strategy, c1, c2, keys, outputFile, debug);
            return;
        }

        // -----------------------------
        // Step 3: Invalid usage fallback
        // -----------------------------
        System.err.println("Error: Must specify either --scheme or --hashing + --strategy.");
        printUsage();
    }

    /**
     * Parses CLI arguments into a key-value map.
     * Handles flags both with and without values (e.g., --debug).
     */
    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            String flag = args[i];

            if (flag.equals("--debug")) {
                map.put("--debug", "true");
            } else if (flag.startsWith("--")) {
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    map.put(flag, args[i + 1]);
                    i++;
                } else {
                    map.put(flag, "true"); // Standalone flag without value
                }
            }
        }

        return map;
    }

    /**
     * Prints usage instructions for the program.
     */
    private static void printUsage() {
        System.out.println("USAGE:");
        System.out.println("  java -jar Book_AJ_Lab2.jar --scheme <1–14> --input <file> --output <file> [--debug]");
        System.out.println("OR");
        System.out.println("  java -jar Book_AJ_Lab2.jar --hashing <division|custom> --mod <int> --bucket <1|3> " +
                "--strategy <linear|quadratic|chaining> [--c1 <float>] [--c2 <float>] --input <file> --output <file> [--debug]");
    }
}
