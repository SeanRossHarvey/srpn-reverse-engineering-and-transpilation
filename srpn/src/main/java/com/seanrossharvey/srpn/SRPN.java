package com.seanrossharvey.srpn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * Saturated reverse Polish notation calculator
 *
 * n.b. This implementation draws heavily on the disassembly, decompilation, & reverse engineering of the original
 * program (allowed for by Replit MIT licence of published software and software reuse clause)
 * These operations were performed with Hex-Rays IDA Pro & decompiler
 * This approach was chosen to ensure the closest possible emulation
 * Due to inability of exact portability due to language difference, differing syntax & libraries are used
 * Optimisations are made where possible without deviation or loss of function
 *
 * See repository PDF for a full analysis of the decompiled C (with annotations in the form of
 * comments)
 */
public final class SRPN {
	// region Fields
	// Stack (LIFO)
	private static final Stack stack = new Stack();
	// Operators (explicitly ordered by element position)
	private static final String[] operators = {"-", "+", "*", "/", "%", "^"};
	// endregion

	// region Constructor
	// n.b. 'while' loop overrides 'while' loop found in 'Main.java' (in compliance with instruction to not modify
	// 'Main.java', redundant code is now imparted extraneously via declaration and instantiation of this class)
	// n.b. this constructor conforms to the disassembled 'run_srpn' function more closely than the implementation found
	// in 'Main.java'
	// n.b. no exception handling (try / catch) or graceful exit code were found in the disassembly of the original
	// program at entry point
	// n.b. 'Scanner' has closer functionality to 'scanf' than 'BufferedReader' found in 'Main.java'
	public SRPN() {
		// Comment flag
		var commentFlag = false;
		try (// 'scanf' substitute
		var scanner = new Scanner(System.in)) {
			// While another token is in scanner input, check for comment flag as designated by '#' and process token if not
			// flagged
			while (scanner.hasNext()) {
				var input = scanner.next();
				if (input.equals("#")) {
					// On / off 'switch' for comment based on presence of '#'
					commentFlag = !commentFlag;
				} else if (!commentFlag) {
					processToken(input);
				}
			}
		}
	}
	// endregion

	// region Methods
	/**
	 * Processes a token based on its type.<br><br>
	 * This method processes an input token after determination of type. If a token is identified as a
	 * number or an operator via {@link #isNumber(String)} or {@link #isOperator(String)}, it is treated
	 * as a command and {@link #processCommand(String)} is invoked with the token as an argument. Otherwise, it is
	 * treated as a line and is passed to {@link #processLine(String)} for further processing.<br>
	 * @param input the input token to be processed.
	 */
	public void processToken(String input) {
		if (isNumber(input) || isOperator(input))
			processCommand(input);
		else
			processLine(input);
	}

	/**
	 * Processes a command based on the input string.<br><br>
	 * This method determines the type of command based on the input string and processes it accordingly. If the input
	 * string is not a valid operator or is equal to 'r', it is treated as a number and passed to the
	 * {@link #processNumber(String)}
	 * method for further processing. Otherwise, it is treated as an operator and passed to the
	 * {@link #processOperator(String)}
	 * method.<br>
	 * @param input the input string representing a command.
	 */
	public void processCommand(String input) {
		if ((!isOperator(input) || input.equals("r")))
			processNumber(input);
		else
			processOperator(input);
	}

	/**
	 * Checks if the specified input is a number.<br><br>
	 * Individual characters are permitted. Any input must pass {@link Character#isDigit(char)} testing
	 * (return {@code true}).
	 * @param input the input string to be checked.
	 * @return {@code true} if the input is a number, {@code false} otherwise.
	 */
	public boolean isNumber(String input) {
		if (input.length() <= 1)
			return Character.isDigit(input.charAt(0));

		return IntStream.range((input.charAt(0) == '-') ? 1 : 0, input.length())
				.allMatch(i -> Character.isDigit(input.charAt(i)));
	}

	/**
	 * Checks if the specified input is an octal number.<br><br>
	 * In order to determine whether the input is an octal number, it must have a length equal to or greater than '3',
	 * the first character must be '0', and it must satisfy the pattern '-0' if negative.
	 * @param input the input string to be checked.
	 * @return {@code true} if the input is an octal number, {@code false} otherwise.
	 */
	public boolean isOctal(String input) {
		if (input.length() <= 2)
			return false;
		if (input.charAt(0) == '0')
			return true;
		return input.charAt(0) == '-' && input.charAt(1) == '0';
	}

	/**
	 * Checks if the specified input is a valid octal number.<br><br>
	 * Each character in the input string is checked to ensure that it falls within the valid range of '0' to '7'.
	 * A leading '-' character is allowed to indicate a negative octal number.
	 * @param input the input string to be checked.
	 * @return {@code true} if the input is a valid octal number, {@code false} otherwise.
	 */
	public boolean isValidOctal(String input) {
		return IntStream.range(input.charAt(0) == '-' ? 1 : 0, input.length())
				.allMatch(i -> input.charAt(i) >= '0' && input.charAt(i) <= '7');
	}

	/**
	 * Checks if the specified character is a valid operator character.<br><br>
	 * This method converts a {@link char} to a {@link String} and passes it through to {@link #isOperator(String)}
	 * as an argument in order to determine the validity of an operator.<br>
	 * @param ch the character to be checked.
	 * @return {@code true} if the character is a valid operator character, {@code false} otherwise.
	 * @see #isOperator(String)
	 */
	public boolean isOperatorChar(char ch) {
		return isOperator(Character.toString(ch));
	}

	/**
	 * Checks if the specified input is a valid operator.<br><br>
	 * This method determines whether the given input is a valid operator. The input is considered to be a valid
	 * operator
	 * if it matches one of the following values: 'r', 'd', '=', or any of the operators defined in the
	 * {@link SRPNTests#operators} array.<br>
	 * @param input the input string to be checked.
	 * @return {@code true} if the input is a valid operator, {@code false} otherwise.
	 * @see SRPNTests#operators
	 * @see Arrays#asList(Object[])
	 */
	public boolean isOperator(String input) {
		return input.equals("r") || input.equals("d") || input.equals("=") || Arrays.asList(operators).contains(input);
	}

	/**
	 * Checks if a value is within the range of {@link Integer#MIN_VALUE} and {@link Integer#MAX_VALUE} and returns a
	 * clamped result within this range.<br><br>
	 * @param value the value to be checked against the valid range.
	 * @return the result within the valid range of integers.
	 */
	public double checkLimit(double value) {
		return (value > Integer.MAX_VALUE) ? Integer.MAX_VALUE :
				(value < Integer.MIN_VALUE) ? Integer.MIN_VALUE : value;
	}

	/**
	 * Converts an octal value to its equivalent decimal representation.<br><br>
	 * A simple iteration through each octal digit is performed in order to convert.
	 * @param value the octal value to be converted to decimal.
	 * @return the decimal representation of the octal value.
	 * @throws IllegalArgumentException if the value is negative.
	 */
	public double convertOctalToDecimal(int value) {
		var integer = 0;
		var doubleValue = 0.0;
		// Loop through each octal digit until '0'
		while (value != 0) {
			// Extract right-most digit
			var digit = value % 10;
			// Bitwise left shift operator (effective multiplication by corresponding power of '8')
			// Increment integer to track each iteration
			doubleValue += digit << (3 * integer++);
			// Remove right-most digit for next iteration
			value /= 10;
		}
		return doubleValue;
	}

	/**
	 * Returns the precedence level of a given operator.<br><br>
	 * Operator precedence is based on its position as an element in {@link SRPNTests#operators}.<br>
	 * The lower the precedence level, the higher the priority of the operator. If the operator is not found in the
	 * array,<br>
	 * it is automatically presumed to have the highest possible precedence level with a value of
	 * {@link Integer#MAX_VALUE}.<br>
	 * @param operator the operator for which the precedence level is to be determined.
	 * @return the precedence level of the operator.
	 * @throws IllegalArgumentException if the operator is 'null' or 'empty'.
	 */
	public int operatorPrecedence(String operator) {
		return IntStream.range(0, operators.length)
				.filter(i -> operator.equals(operators[i]))
				.findFirst()
				.orElse(Integer.MAX_VALUE);
	}

	/**
	 * Processes a number input.<br>
	 * <br>
	 * Operation performed depends on input.<br><br>
	 * - There is a hard-coded stack size of '23'. If this is reached, a stack overflow message will be printed.<br>
	 * - If the input is equal to 'r', a pseudorandom number is generated using {@link SRPNTests#random()} and is
	 * pushed onto the stack.
	 * - If the input is an octal number, it is converted to its decimal representation using the
	 * convertOctalToDecimal(int) method. The result is then limit checked (range) before being pushed onto the stack.
	 * <br>
	 * - If the input is a valid numerical input, it is parsed to a double, limit checked (range), and pushed onto the
	 * stack.<br>
	 * @param input the input representing a number.
	 * @see SRPNTests#random()
	 * @see #convertOctalToDecimal(int)
	 * @see #isValidOctal(String)
	 * @see #checkLimit(double)
	 */
	public void processNumber(String input) {
		// Hard coded stack size limit ('23')
		if (stack.size() == 23) {
			System.out.println("Stack overflow.");
			return;
		}
		// Generate a random number and push to stack
		if (input.equals("r")) {
			stack.push((long) random());
			// Octal handling
		} else if (isOctal(input)) {
			if (isValidOctal(input))
				stack.push((long) checkLimit(convertOctalToDecimal(Integer.parseInt(input))));
			// Final case (numerical)
		} else
			stack.push((long) checkLimit(Double.parseDouble(input)));
	}

	/**
	 * Processes the given operator and performs its corresponding operations on the stack.<br><br>
	 * Includes stack print and arithmetic operations.<br>
	 * @param operator The operator to be processed.
	 */
	public void processOperator(String operator) {
		double a, b, topElement;

		switch (operator) {
			// Print stack
			case "d" -> stack.printStack();
			// Print top element of stack
			case "=" -> {
				if (stack.size() == 0)
					System.out.println("Stack empty.");
				else {
					topElement = stack.peek();
					System.out.printf("%d\n", (int) topElement);
				}
			}
			// Numerical case
			default -> {
				// Print out stack underflow error message if stack is less than '1'
				if (stack.size() <= 1) {
					System.out.println("Stack underflow.");
					return;
				}
				// Assignment for binary operation(s) below
				b = stack.pop();
				a = stack.pop();
				// Arithmetic operators
				switch (operator) {
					case "+" -> stack.push((long) checkLimit(a + b));
					case "-" -> stack.push((long) checkLimit(a - b));
					case "*" -> stack.push((long) checkLimit(a * b));
					case "/" -> {
						// Explicit divide by zero error check
						if (b != 0.0) {
							stack.push((long) checkLimit(a / b));
						} else {
							System.out.println("Divide by 0.");
							stack.push((long) a);
							stack.push(0L);
						}
					}
					case "%" -> {
						// Explicit divide by zero check (allows set of all real numbers with zero removed)
						if (a != 0.0) {
							stack.push((long) checkLimit((int) a % (int) b));
						} else {
							System.out.println("Divide by 0.");
							stack.push(0L);
							stack.push((long) b);
						}
					}
					case "^" -> {
						// Explicit negative power check (allows set of positive real numbers with zero removed)
						if (b >= 0.0) {
							stack.push((long) checkLimit(Math.pow(a, b)));
						} else {
							System.out.println("Negative power.");
							stack.push((long) a);
							stack.push((long) b);
						}
					}
					default -> {
					}
				}
			}
		}
	}

	/**
	 * Processes a line of input; parsing and executing commands based on the given line.<br><br>
	 * The input line is analysed on a per-character basis; identifying operators, operands, and commands.<br>
	 * The processed commands are executed according to specific logical rules and predetermined operator precedence.
	 * <br>
	 * @param line The line of input to be processed.
	 */
	public void processLine(String line) {
		// n.b. 'LinkedList' is used in this block to preserve order (fail-safe)
		// Temporary storage before token grouping (character accumulation before tokenisation)
		var source = new LinkedList<String>();
		// Tokens for processing ('space' delimited)
		var tokens = new LinkedList<String>();

		// Iterate through each line character
		for (var ch : line.toCharArray()) {
			// region Custom ported fail-safe behaviour (bypass '__ctype_b_loc' complexity)
			if ((ch == 'r' || ch == 'd' || 	ch == '=') // 'r', 'd', or '='
				|| (!Character.isDigit(ch) && !isOperatorChar(ch))) // Any other character
			{
				// Add new token from current source (cut-off point)
				if(source.size() > 0) {
					tokens.add(String.join("", source));
					source.clear();
				}
				// Add character
				tokens.add(String.valueOf(ch));
				continue;
			}
			// endregion
			if (Character.isLetter(ch) || Character.isDigit(ch)) {
				source.add(String.valueOf(ch));
			} else if (isOperatorChar(ch)) {
				// Negative number flag logic block
				var isNegated = false;
				if (ch == '-' && source.size() == 0) {
					source.add(String.valueOf(ch));
					isNegated = true;
				}
				if (!isNegated) {
					if (source.size() > 0) {
						tokens.add(String.join("", source));
						source.clear();
					}
					tokens.add(String.valueOf(ch));
				}
				// Regular tokenisation
			} else {
				if (source.size() > 0) {
					tokens.add(String.join("", source));
					source.clear();
				}
				tokens.add(String.valueOf(ch));
			}
		}

		// Add source contents to token if source array contains elements
		if (source.size() > 0)
			tokens.add(String.join("", source));

		// Ordered command list
		var commands = new LinkedList<String>();
		// Operator precedence variable
		var currentOperatorPrecedence = -1;
		// Iterate over tokens
		for (var token : tokens) {
			if (isNumber(token)) {
				processCommand(token);
				// 'd' logic
			} else if (token.equals("d")) {
				while (!commands.isEmpty())
					processCommand(commands.removeLast());
				processCommand(token);
			} else {
				// 'r' & '=' logic
				if (token.equals("r") || token.equals("=")) {
					processCommand(token);
					continue;
				}
				if (isOperator(token)) {
					// Operator precedence logic
					var operatorPrecedence = operatorPrecedence(token);
					if (operatorPrecedence >= currentOperatorPrecedence) {
						commands.addLast(token);
						currentOperatorPrecedence = operatorPrecedence;
					} else {
						while (!commands.isEmpty())
							processCommand(commands.removeLast());
						currentOperatorPrecedence = operatorPrecedence;
						commands.addLast(token);
					}
				} else
					System.out.printf("Unrecognised operator or operand \"%s\".%n", token);
			}
		}

		// Process any remaining commands
		while (!commands.isEmpty())
			processCommand(commands.removeLast());
	}

	// 'srand(0)' & 'rand' predefined table of values (ported)
	// This is a mapping of a random seed of '0' which wraps around and resets when maximum random number capacity is
	// reached (per decompilation output)
	private static final List<Integer> numbers = new ArrayList<>(Arrays.asList(
			1804289383, 846930886, 1681692777, 1714636915, 1957747793,
			424238335, 719885386, 1649760492, 596516649, 1189641421,
			1025202362, 1350490027, 783368690, 1102520059, 2044897763,
			1967513926, 1365180540, 1540383426, 304089172, 1303455736,
			35005211, 521595368
	));

	// Track index for 'numbers' by access via 'random' method
	private static int randomCurrentIndex = 0;

	// Return mapped random number
	public static int random() {
		var number = numbers.get(randomCurrentIndex);
		randomCurrentIndex = (randomCurrentIndex + 1) % numbers.size();
		return number;
	}
	// endregion
}

