# Book_AJ_Lab2_Hashing

## Overview
This project implements and analyzes 14 hashing schemes using Java. It supports:
- Division-based hashing with linear, quadratic, and chaining strategies
- Fibonacci hashing (Knuth's method) with linear, quadratic, and chaining strategies
- Full CLI-driven execution with predefined or manual configurations
- Metrics tracking (comparisons, collisions, probes, memory, time)
- Output formatting and analysis-ready results

---

## Project Structure
```
Book_AJ_Lab2_Hashing/
├── src/main/java/edu/jhu/algos
│   ├── Main.java                  # CLI entry point
│   ├── HashingDriver.java        # Executes all schemes
│   ├── hashing/                  # Core hashing logic
│   │   ├── HashTable.java        # Abstract base
│   │   ├── DivisionHashTable.java
│   │   ├── CustomHashTable.java
│   │   ├── ProbingStrategy.java
│   │   ├── HashingScheme.java    # Scheme metadata
│   ├── data_structures/          # Chaining helpers
│   │   ├── ChainedNode.java
│   │   ├── LinkedListChain.java
│   │   └── Stack.java
│   ├── io/
│   │   ├── HashFileHandler.java
│   │   └── OutputFormatter.java
│   └── utils/
│       └── PerformanceMetrics.java
├── input/                        # Input .txt files
├── output/                       # Outputs, one per scheme/config
├── run_all_schemes.sh           # Shell script to batch run schemes
├── pom.xml                      # Maven build
```

---

## How to Compile and Run

### Compile with Maven
```
mvn clean package
```

### Run predefined scheme (1–14):
```
java -jar target/Book_AJ_Lab2.jar \
  --scheme 3 \
  --input input/example.txt \
  --output output/scheme_3.txt \
  --debug
```

### Run manual configuration:
```
java -jar target/Book_AJ_Lab2.jar \
  --hashing custom \
  --strategy quadratic \
  --bucket 1 \
  --c1 0.5 --c2 0.5 \
  --input input/my_keys.txt \
  --output output/custom_output.txt \
  --debug
```

---

## Input Format
- One integer per line
- Blank lines ignored
- Non-integer lines skipped with warnings
- Edge cases handled: Integer.MIN_VALUE, empty file, corrupted lines, etc.

---

## Output Format
Each output file includes:
- Echoed input
- Scheme config (method, strategy, mod, bucket size)
- Performance stats:
    - Primary / secondary collisions
    - Probes, comparisons, insertions
    - Load factor
    - Time, memory
- Final table:
    - Grid view (probing)
    - Linked list view (chaining)

Example snippet:
```
original input:
5, 10, 15, 20, 25

scheme 1 (division) - modulo: 120, bucket size: 1, linear

# of primary collisions: 2, secondary collisions: 1, total collisions: 3
# of comparisons: 6, records inserted: 5, load factor: 0.041667

Execution Time: 4.320e-4 seconds
Memory Usage: 1.024e+5 bytes
```

---

## Testing
- All modules tested with JUnit5
- `src/test/java/` contains:
    - Unit tests for every class
    - Edge case validations
    - File output tests
    - Metric accuracy checks
- Output files are auto-deleted after tests

Run tests:
```
mvn test
```

---

## Enhancements
- Full debug logging with `--debug`
- Custom c1/c2 for probing exploration
- Output formatter mirrors to console + file
- Memory pool for chaining with reusable nodes
- Clear modular boundaries for future extensions

---

## Author
Book_AJ
Lab 2 – Hashing Algorithms
Algorithms for Bioinformatics @ JHU

---


