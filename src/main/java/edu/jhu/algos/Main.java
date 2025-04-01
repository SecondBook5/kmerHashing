package edu.jhu.algos;

import edu.jhu.algos.io.HashFileHandler;

import java.util.*;

/**
 * Main entry point for the Hashing Lab.
 * Supports command-line execution of predefined schemes, fully manual configurations,
 * or batch mode via --run-all for comparative analysis and optional plot generation.
 */
public class Main {

    public static void main(String[] args) {
        Map<String, String> flags = parseArgs(args);

        boolean debug = flags.containsKey("--debug");
        boolean generatePlots = flags.containsKey("--generate-plots");

        // Debug print of all CLI flags
        if (debug) {
            System.out.println("[DEBUG] Parsed CLI Flags:");
            flags.forEach((k, v) -> System.out.printf("  %s => %s%n", k, v));
        }

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
        // Step 2: Dispatch Mode (Exclusive)
        // -----------------------------

        boolean isRunAll = flags.containsKey("--run-all");
        boolean isSchemeMode = flags.containsKey("--scheme");
        boolean isManualMode = flags.containsKey("--hashing") && flags.containsKey("--strategy");

        int modeCount = (isRunAll ? 1 : 0) + (isSchemeMode ? 1 : 0) + (isManualMode ? 1 : 0);
        if (modeCount != 1) {
            System.err.println("Error: You must specify exactly one mode: --run-all, --scheme, or manual flags.");
            printUsage();
            return;
        }

        // MODE A: Run all 14 schemes
        if (isRunAll) {
            HashingDriver.runAllSchemes(keys, outputFile, debug, generatePlots);
            return;
        }

        // MODE B: Predefined Scheme
        if (isSchemeMode) {
            try {
                int schemeNumber = Integer.parseInt(flags.get("--scheme"));
                if (schemeNumber < 1 || schemeNumber > 14) {
                    System.err.println("Error: --scheme must be between 1 and 14.");
                    return;
                }
                HashingDriver.runScheme(schemeNumber, keys, outputFile, debug, generatePlots);
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid number for --scheme.");
            }
            return;
        }

        // MODE C: Fully Manual
        String method = flags.get("--hashing").toLowerCase();
        String strategy = flags.get("--strategy").toLowerCase();

        int mod = -1;
        if ("division".equals(method)) {
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

        double c1 = 0.5, c2 = 0.5;
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

        HashingDriver.runManual(method, mod, bucketSize, strategy, c1, c2, keys, outputFile, debug);
    }

    /**
     * Parses CLI arguments into a map of flag → value (or true for standalone).
     */
    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String flag = args[i];
            if (flag.equals("--debug") || flag.equals("--run-all") || flag.equals("--generate-plots")) {
                map.put(flag, "true");
            } else if (flag.startsWith("--")) {
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    map.put(flag, args[i + 1]);
                    i++;
                } else {
                    map.put(flag, "true");
                }
            }
        }
        return map;
    }

    /**
     * Prints command-line usage instructions.
     */
    private static void printUsage() {
        System.out.println("USAGE:");
        System.out.println("  java -jar Book_AJ_Lab2.jar --run-all --input <file> --output <basefile> [--debug] [--generate-plots]");
        System.out.println("OR");
        System.out.println("  java -jar Book_AJ_Lab2.jar --scheme <1–14> --input <file> --output <file> [--debug]");
        System.out.println("OR");
        System.out.println("  java -jar Book_AJ_Lab2.jar --hashing <division|custom> --mod <int> --bucket <1|3> " +
                "--strategy <linear|quadratic|chaining> [--c1 <float>] [--c2 <float>] --input <file> --output <file> [--debug]");
    }
}
