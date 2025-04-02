#!/bin/bash
# -----------------------------------------------------------------
# run_all_schemes.sh
# Runs all 14 predefined schemes + 25+ manual configs on input/*.txt.
# Logs full metrics, flags, and parameters to summary/metrics_summary.csv.
# Also logs any failures to summary/failure_log.txt.
# At the end, it prints a summary of failures and calls the asymptotic
# analysis Python script (asymptotic_analysis.py) to generate plots
# comparing candidate models (O(1), O(log n), O(n), O(n^2)).
# -----------------------------------------------------------------

# Define directories and file paths.
INPUT_DIR="input"                               # Directory containing input files.
OUTPUT_DIR="output"                             # Directory for output files.
SUMMARY_DIR="summary"                           # Directory for summary files.
SUMMARY_FILE="${SUMMARY_DIR}/metrics_summary.csv"  # CSV file to store metrics.
FAILURE_LOG="${SUMMARY_DIR}/failure_log.txt"    # File to log any failures.
DEBUG_FLAG=""                                   # Flag for enabling debug mode.

# Enable debug mode if passed.
if [[ "$1" == "--debug" ]]; then
  DEBUG_FLAG="--debug"
fi

# Create output and summary directories if they do not exist.
mkdir -p "$OUTPUT_DIR"
mkdir -p "$SUMMARY_DIR"

# Write the CSV header including the new 'input_size' column.
echo "run_type,input_file,input_size,CLI_Flags,hashing_method,mod_value,bucket_size,strategy,c1,c2,collisions,primary_collisions,secondary_collisions,comparisons,insertions,load_factor,execution_time_sec,memory_usage_bytes" > "$SUMMARY_FILE"

# Log the start of the failure log.
echo "Failures from run_all_schemes.sh" > "$FAILURE_LOG"
echo "Timestamp: $(date)" >> "$FAILURE_LOG"
echo "-----------------------------------------" >> "$FAILURE_LOG"

# Function: extract_metrics
# Purpose: Extracts performance metrics from an output file and appends a CSV row.
# It also calculates the input size (number of lines) for asymptotic analysis.
extract_metrics() {
  local run_type="$1"         # Type of run (e.g., scheme_1, manual_div113_linear_b1, etc.)
  local input_file="$2"       # Basename of the input file.
  local output_file="$3"      # Path to the output file.
  local cli_flags="$4"        # CLI flags string used in the run.

  # Check if the output file exists; if not, log an error.
  if [[ ! -f "$output_file" ]]; then
    echo "[ERROR] Output file not found: $output_file" >> "$FAILURE_LOG"
    return 1
  fi

  # Build the full path to the input file to compute input size.
  local full_input_path="${INPUT_DIR}/${input_file}"
  local input_size="NA"       # Default value if file is missing.
  if [[ -f "$full_input_path" ]]; then
    input_size=$(wc -l < "$full_input_path" | tr -d ' ')
  else
    echo "[WARNING] Input file not found for size calculation: $full_input_path" >> "$FAILURE_LOG"
  fi

  # Extract parameters from CLI flags using regex.
  local hashing_method=$(echo "$cli_flags" | grep -oP '(?<=--hashing )\S+')
  local mod_value=$(echo "$cli_flags" | grep -oP '(?<=--mod )\S*')
  local bucket_size=$(echo "$cli_flags" | grep -oP '(?<=--bucket )\S*')
  local strategy=$(echo "$cli_flags" | grep -oP '(?<=--strategy )\S+')
  local c1=$(echo "$cli_flags" | grep -oP '(?<=--c1 )\S*')
  local c2=$(echo "$cli_flags" | grep -oP '(?<=--c2 )\S*')

  # Use awk to extract metrics from the output file.
  awk -v run="$run_type" \
      -v input="$input_file" \
      -v in_size="$input_size" \
      -v flags="$cli_flags" \
      -v hash="$hashing_method" \
      -v mod="$mod_value" \
      -v bucket="$bucket_size" \
      -v strat="$strategy" \
      -v C1="$c1" \
      -v C2="$c2" '
    BEGIN {
      FS=": "; OFS=","
      collisions=primary=secondary=comparisons=insertions=lf=et=mem="NA"
    }
    /# of primary collisions:/ {
      match($0, /# of primary collisions: *([0-9]+), *secondary collisions: *([0-9]+), *total collisions: *([0-9]+)/, m)
      if (m[1] && m[2] && m[3]) {
        primary = m[1]
        secondary = m[2]
        collisions = m[3]
      }
    }
    /# of collisions:/ {
      match($0, /# of collisions: *([0-9]+)/, m)
      if (m[1]) {
        collisions = m[1]
        primary = "NA"
        secondary = "NA"
      }
    }
    /# of comparisons:/ {
      match($0, /# of comparisons: *([0-9]+), *records inserted: *([0-9]+), *load factor: *([0-9.]+)/, m)
      if (m[1] && m[2] && m[3]) {
        comparisons = m[1]
        insertions  = m[2]
        lf          = m[3]
      }
    }
    /Execution Time:/ {
      gsub(" seconds", "", $2); et = $2
    }
    /Memory Usage:/ {
      gsub(" bytes", "", $2); mem = $2
    }
    END {
      print run, input, in_size, flags, hash, mod, bucket, strat, C1, C2, collisions, primary, secondary, comparisons, insertions, lf, et, mem
    }
  ' "$output_file" >> "$SUMMARY_FILE"
}

# Process each input file in the input directory.
for INPUT_FILE in "$INPUT_DIR"/*.txt; do
  BASENAME=$(basename "$INPUT_FILE")
  BASENAME_NOEXT="${BASENAME%.txt}"
  echo "Processing $BASENAME"

  # --- Schemes 1 to 14 ---
  for SCHEME in {1..14}; do
    OUTPUT_FILE="${OUTPUT_DIR}/scheme_${SCHEME}_${BASENAME_NOEXT}.txt"
    case $SCHEME in
      1)  FLAGS="--hashing division --mod 120 --bucket 1 --strategy linear" ;;
      2)  FLAGS="--hashing division --mod 120 --bucket 1 --strategy quadratic --c1 0.5 --c2 0.5" ;;
      3)  FLAGS="--hashing division --mod 120 --bucket 1 --strategy chaining" ;;
      4)  FLAGS="--hashing division --mod 127 --bucket 1 --strategy linear" ;;
      5)  FLAGS="--hashing division --mod 127 --bucket 1 --strategy quadratic --c1 0.5 --c2 0.5" ;;
      6)  FLAGS="--hashing division --mod 127 --bucket 1 --strategy chaining" ;;
      7)  FLAGS="--hashing division --mod 113 --bucket 1 --strategy linear" ;;
      8)  FLAGS="--hashing division --mod 113 --bucket 1 --strategy quadratic --c1 0.5 --c2 0.5" ;;
      9)  FLAGS="--hashing division --mod 113 --bucket 1 --strategy chaining" ;;
      10) FLAGS="--hashing division --mod 41 --bucket 3 --strategy linear" ;;
      11) FLAGS="--hashing division --mod 41 --bucket 3 --strategy quadratic --c1 0.5 --c2 0.5" ;;
      12) FLAGS="--hashing custom --bucket 1 --strategy linear" ;;
      13) FLAGS="--hashing custom --bucket 1 --strategy quadratic --c1 0.5 --c2 0.5" ;;
      14) FLAGS="--hashing custom --bucket 1 --strategy chaining" ;;
    esac

    if ! mvn compile exec:java \
      -Dexec.mainClass="edu.jhu.algos.Main" \
      -Dexec.args="$FLAGS --input $INPUT_FILE --output $OUTPUT_FILE $DEBUG_FLAG"; then
        echo "[FAILED MVN] $FLAGS --input $INPUT_FILE --output $OUTPUT_FILE" >> "$FAILURE_LOG"
    fi

    if ! extract_metrics "scheme_${SCHEME}" "$BASENAME" "$OUTPUT_FILE" "$FLAGS"; then
      echo "[FAILED METRICS] scheme_${SCHEME} → $OUTPUT_FILE" >> "$FAILURE_LOG"
    fi
  done

  # --- Manual Division Configurations ---
  for MOD in 113 127 41; do
    for BUCKET in 1 3; do
      FLAGS="--hashing division --mod $MOD --bucket $BUCKET --strategy linear"
      SUFFIX="manual_div${MOD}_linear_b${BUCKET}"
      OUT="${OUTPUT_DIR}/${SUFFIX}_${BASENAME_NOEXT}.txt"

      if ! mvn compile exec:java \
        -Dexec.mainClass="edu.jhu.algos.Main" \
        -Dexec.args="$FLAGS --input $INPUT_FILE --output $OUT $DEBUG_FLAG"; then
          echo "[FAILED MVN] $FLAGS --input $INPUT_FILE --output $OUT" >> "$FAILURE_LOG"
      fi
      if ! extract_metrics "$SUFFIX" "$BASENAME" "$OUT" "$FLAGS"; then
        echo "[FAILED METRICS] $SUFFIX → $OUT" >> "$FAILURE_LOG"
      fi

      for C1 in 0.5 1.0 1.5; do
        for C2 in 0.5 0.75 0.25; do
          FLAGS="--hashing division --mod $MOD --bucket $BUCKET --strategy quadratic --c1 $C1 --c2 $C2"
          SUFFIX="manual_div${MOD}_quadratic_c${C1//./}_${C2//./}_b${BUCKET}"
          OUT="${OUTPUT_DIR}/${SUFFIX}_${BASENAME_NOEXT}.txt"

          if ! mvn compile exec:java \
            -Dexec.mainClass="edu.jhu.algos.Main" \
            -Dexec.args="$FLAGS --input $INPUT_FILE --output $OUT $DEBUG_FLAG"; then
              echo "[FAILED MVN] $FLAGS --input $INPUT_FILE --output $OUT" >> "$FAILURE_LOG"
          fi
          if ! extract_metrics "$SUFFIX" "$BASENAME" "$OUT" "$FLAGS"; then
            echo "[FAILED METRICS] $SUFFIX → $OUT" >> "$FAILURE_LOG"
          fi
        done
      done
    done
  done

  # --- Manual Custom (Fibonacci) Configurations ---
  for BUCKET in 1 3; do
    for STRATEGY in linear chaining; do
      FLAGS="--hashing custom --bucket $BUCKET --strategy $STRATEGY"
      SUFFIX="manual_custom_${STRATEGY}_b${BUCKET}"
      OUT="${OUTPUT_DIR}/${SUFFIX}_${BASENAME_NOEXT}.txt"

      if ! mvn compile exec:java \
        -Dexec.mainClass="edu.jhu.algos.Main" \
        -Dexec.args="$FLAGS --input $INPUT_FILE --output $OUT $DEBUG_FLAG"; then
          echo "[FAILED MVN] $FLAGS --input $INPUT_FILE --output $OUT" >> "$FAILURE_LOG"
      fi
      if ! extract_metrics "$SUFFIX" "$BASENAME" "$OUT" "$FLAGS"; then
        echo "[FAILED METRICS] $SUFFIX → $OUT" >> "$FAILURE_LOG"
      fi
    done

    for C1 in 0.5 1.0 1.5; do
      for C2 in 0.5 1.0 0.25; do
        FLAGS="--hashing custom --bucket $BUCKET --strategy quadratic --c1 $C1 --c2 $C2"
        SUFFIX="manual_custom_quadratic_c${C1//./}_${C2//./}_b${BUCKET}"
        OUT="${OUTPUT_DIR}/${SUFFIX}_${BASENAME_NOEXT}.txt"

        if ! mvn compile exec:java \
          -Dexec.mainClass="edu.jhu.algos.Main" \
          -Dexec.args="$FLAGS --input $INPUT_FILE --output $OUT $DEBUG_FLAG"; then
            echo "[FAILED MVN] $FLAGS --input $INPUT_FILE --output $OUT" >> "$FAILURE_LOG"
        fi
        if ! extract_metrics "$SUFFIX" "$BASENAME" "$OUT" "$FLAGS"; then
          echo "[FAILED METRICS] $SUFFIX → $OUT" >> "$FAILURE_LOG"
        fi
      done
    done
  done
done

# Log completion of experiments.
echo "All experiments complete. Summary written to: $SUMMARY_FILE"

# -----------------------------------------------------------------
# Print a Summary of Failures (if any)
# -----------------------------------------------------------------
echo "-----------------------------------------"
echo "Failure Summary:"
if [ -s "$FAILURE_LOG" ]; then
  # Print the contents of the failure log if it is not empty.
  cat "$FAILURE_LOG"
else
  echo "No failures encountered."
fi

# -----------------------------------------------------------------
# Run Asymptotic Analysis
# -----------------------------------------------------------------
echo "Running asymptotic analysis..."
# Call the Python analysis script that compares candidate models (O(1), O(log n), O(n), O(n^2)).
python3 asymptotic_analysis.py
echo "Asymptotic analysis complete. Check asymptotic_comparison.png and residuals_plot_<model>.png for details."
