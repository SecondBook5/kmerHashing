#!/bin/bash

# -----------------------------------------------------------------
# run_all_schemes.sh
# Runs all 14 predefined schemes + 25+ manual configs on input/*.txt
# Logs full metrics + flags + parameters to summary/metrics_summary.csv
# Also logs any failures to summary/failure_log.txt
# -----------------------------------------------------------------

INPUT_DIR="input"
OUTPUT_DIR="output"
SUMMARY_DIR="summary"
SUMMARY_FILE="${SUMMARY_DIR}/metrics_summary.csv"
FAILURE_LOG="${SUMMARY_DIR}/failure_log.txt"
DEBUG_FLAG=""

# Enable debug mode if passed
if [[ "$1" == "--debug" ]]; then
  DEBUG_FLAG="--debug"
fi

mkdir -p "$OUTPUT_DIR"
mkdir -p "$SUMMARY_DIR"

# CSV HEADER
echo "run_type,input_file,CLI_Flags,hashing_method,mod_value,bucket_size,strategy,c1,c2,collisions,primary_collisions,secondary_collisions,comparisons,insertions,load_factor,execution_time_sec,memory_usage_bytes" > "$SUMMARY_FILE"
echo " Failures from run_all_schemes.sh" > "$FAILURE_LOG"
echo "Timestamp: $(date)" >> "$FAILURE_LOG"
echo "-----------------------------------------" >> "$FAILURE_LOG"

# Metric extraction function
extract_metrics() {
  local run_type="$1"
  local input_file="$2"
  local output_file="$3"
  local cli_flags="$4"

  if [[ ! -f "$output_file" ]]; then
    echo "[ERROR] Output file not found: $output_file" >> "$FAILURE_LOG"
    return 1
  fi

  local hashing_method=$(echo "$cli_flags" | grep -oP '(?<=--hashing )\S+')
  local mod_value=$(echo "$cli_flags" | grep -oP '(?<=--mod )\S*')
  local bucket_size=$(echo "$cli_flags" | grep -oP '(?<=--bucket )\S*')
  local strategy=$(echo "$cli_flags" | grep -oP '(?<=--strategy )\S+')
  local c1=$(echo "$cli_flags" | grep -oP '(?<=--c1 )\S*')
  local c2=$(echo "$cli_flags" | grep -oP '(?<=--c2 )\S*')

  awk -v run="$run_type" -v input="$input_file" -v flags="$cli_flags" \
      -v hash="$hashing_method" -v mod="$mod_value" -v bucket="$bucket_size" \
      -v strat="$strategy" -v C1="$c1" -v C2="$c2" '
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
      gsub(" seconds", "", $2); et=$2
    }

    /Memory Usage:/ {
      gsub(" bytes", "", $2); mem=$2
    }

    END {
      print run, input, flags, hash, mod, bucket, strat, C1, C2,
            collisions, primary, secondary, comparisons, insertions, lf, et, mem
    }
  ' "$output_file" >> "$SUMMARY_FILE"
}

# Run schemes and manual configs on each input file
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

  # --- Manual Division ---
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

  # --- Manual Custom (Fibonacci) ---
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

echo "All runs complete. Summary written to: $SUMMARY_FILE"
echo "Generating asymptotic plots..."
python3 plot_metrics_analysis.py

echo "All experiments and plotting complete."
echo "See any failures in: $FAILURE_LOG"
