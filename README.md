# Saturated Reverse Polish Notation (SRPN) Calculator

## Overview

The **Saturated Reverse Polish Notation (SRPN) Calculator** is a translational software project developed in Java, informed by reverse engineering, disassembly, and decompilation of a legacy C implementation. This project adheres to the theoretical and functional requirements of the original SRPN program, emulating its behaviour with optimisations suitable for Java.

The implementation draws on the **MIT licence for software reuse**, specifically leveraging Hex-Rays IDA Pro for disassembly and decompilation. The core objectives of this project are:

- High-fidelity emulation of the original C-based SRPN functionality.
- Preservation of original operational semantics while making language-specific optimisations.
- Transparent documentation of translational design decisions.

Refer to the accompanying annotated PDF for a detailed analysis of the decompiled C source.

---

## Project structure

```
SRPN-REVERSE-ENGINEERING-AND-TRANSPILATION
├── docs
│   └── SRPN decompilation (annotated).pdf  # Decompilation analysis
├── origin                      # Original program binary
│   └── bin
│       └── srpn
├── src
│   ├── srpn.c                  # Original C implementation (for reference)
│   └── main/java/com/seanrossharvey/srpn
│       ├── App.java            # Entry point
│       ├── SRPN.java           # Core emulation of SRPN logic
│       ├── Stack.java          # LIFO stack implementation
│       └── SRPNRudimentaryTests.java  # Test-driven development class
├── pom.xml                     # Maven project configuration
├── .gitignore                  # Git exclusion rules
└── README.md                   # Project documentation (this file)
```

---

## Features

### Core functionalities

1. **Arithmetic operations:**

   - Support for `+`, `-`, `*`, `/`, `%`, and `^`.
   - Adheres to overflow constraints, clamping results to `Integer.MIN_VALUE` and `Integer.MAX_VALUE`.

2. **Pseudo-random number generation:**

   - Implements predefined pseudo-random number sequences replicating `srand(0)` in the original program.
   - Sequence resets after consuming all predefined values.

3. **Stack operations:**

   - Fully emulates Last-In-First-Out (LIFO) stack behaviour.
   - Provides stack size limits, overflow error messages, and underflow handling.

4. **Octal number support:**

   - Converts valid octal inputs into decimal equivalents before processing.
   - Validates octal syntax and detects invalid inputs.

5. **Operator precedence:**
   - Implements operator precedence rules to evaluate expressions in accordance with the original SRPN behaviour.

### Reverse engineering justification

- Original logic disassembled using **Hex-Rays IDA Pro**.
- Systematically translated from C to Java, taking into account:
  - Syntax and library differences.
  - Functional optimisations without deviating from expected behaviour.
- Absence of exception handling in the original program mirrored to preserve compatibility.

---

## Key components

### SRPN.java

The `SRPN` class serves as the central implementation of the reverse-engineered logic. It encapsulates:

- Token processing: Parsing input tokens to differentiate between numbers, operators, and commands.
- Stack operations: Leveraging the `Stack` class for LIFO behaviour.
- Arithmetic and non-arithmetic commands: Implementing operator precedence, overflow handling, and special commands (`d`, `r`, `=`).

### Stack.java

This class provides an abstraction of a stack data structure, built on `ArrayDeque<Long>` for efficient performance.

**Key methods:**

- `push(Long element)`: Inserts an element onto the stack.
- `pop()`: Removes and returns the top element, throwing an exception if the stack is empty.
- `peek()`: Retrieves the top element without removal.
- `printStack()`: Outputs the entire stack (top to bottom).
- `clear()`: Resets the stack.

### SRPNRudimentaryTests.java

A bespoke, rudimentary test suite implementing **Test-driven development (TDD)** principles. This class ensures:

- Exhaustive validation of input-output mappings.
- Execution of boundary tests for numerical limits, stack overflows, and underflows.
- Error handling replication, including the absence of exception mechanisms for malformed input.

**Notable features:**

- Simulates pseudo-random number functionality with predefined sequences.
- Validates compliance with legacy SRPN outputs.
- Incorporates malformed input to test failure behaviour.

---

## Design decisions

### Language transition

- **Scanner over BufferedReader:** Emulates the C `scanf` function for input processing.
- **ArrayDeque for Stack:** Offers optimised, memory-efficient stack implementation.
- **Java Streams for Validation:** Ensures concise and functional validation logic for number and operator detection.

### Optimisations

- Operator precedence implemented via indexed arrays for computational efficiency.
- Avoidance of external dependencies to align with the standalone nature of the original implementation.
- Comprehensive annotations for maintainability and transparency.

### Constraints

- Fixed stack size of 23 elements to mirror original behaviour.
- Predefined pseudo-random number sequence to replicate original program determinism.

---

## Testing strategy

This project follows **Test-driven development (TDD)** principles, relying on programmatic validation rather than JUnit or external testing frameworks. Testing ensures:

- Compatibility with original SRPN outputs.
- Boundary condition handling, including overflow, underflow, and invalid input.
- Debugging robustness by recreating original error scenarios.

## Dependencies

- **Maven:** Build automation and dependency management.
- **Java 17:** Supports modern language features and compatibility.

---

## Usage

### Compilation

Ensure Maven and Java are installed. Clone the repository and navigate to the root directory:

```bash
mvn compile
```

### Execution

Run the application via the Maven `exec` plugin:

```bash
mvn exec:java -Dexec.mainClass="com.seanrossharvey.srpn.App"
```

### Testing

Invoke the test suite:

```bash
mvn exec:java -Dexec.mainClass="com.seanrossharvey.srpn.SRPNRudimentaryTests"
```

---

## Future work

- Integration of formal testing frameworks (e.g., JUnit) for automated validation.
- Refactoring for multi-threaded environments to enhance performance.
- Extension of operator support and custom stack size configurations.

---

## References

- Hex-Rays IDA Pro Documentation: [https://www.hex-rays.com/ida-pro/](https://www.hex-rays.com/ida-pro/)
- Replit MIT Licence: [https://replit.com/](https://replit.com/)
- Reverse Engineering Techniques: [https://reverseengineering.stackexchange.com/](https://reverseengineering.stackexchange.com/)

---

## Licence

This project is distributed under the **MIT Licence**, permitting reuse, modification, and distribution of the software in accordance with the terms and conditions of the licence.
