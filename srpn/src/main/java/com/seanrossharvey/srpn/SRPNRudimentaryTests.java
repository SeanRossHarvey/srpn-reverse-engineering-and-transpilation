package com.seanrossharvey.srpn;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.IntStream;

/**
 * Saturated reverse Polish notation calculator
 *
 * n.b. This implementation draws heavily on the disassembly, decompilation, & reverse engineering of the original
 * program (allowed for by Replit MIT licence of published software and software reuse clause in assignment brief)
 * These operations were performed with Hex-Rays IDA Pro & decompiler
 * This approach was chosen to ensure the closest possible emulation
 * Due to inability of exact portability due to language difference, differing syntax & libraries are used
 * Optimisations are made where possible without deviation or loss of function
 *
 * See the submitted PDF or Word document for a full analysis of the decompiled C (with annotations in the form of
 * comments)
 */
public final class SRPNRudimentaryTests {
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

	// n.b. not a robust / traditional unit test
	public record RudimentaryTest(List<String> testCases){};
	public SRPNRudimentaryTests() {
		// n.b. all expected outputs in the below test cases originate directly from the original SRPN program (exact)
		var testCases = List.of(
				// Tests 1
				new RudimentaryTest(List.of(
						"10", "", // Input and expected output (line-by-line)
						"2", "",
						"+", "",
						"=", "12" // Normal
				)),
				new RudimentaryTest(List.of(
						"11", "",
						"3", "",
						"-", "",
						"=", "8" // Normal
				)),
				new RudimentaryTest(List.of(
						"9", "",
						"4", "",
						"*", "",
						"=", "36" // Normal
				)),
				new RudimentaryTest(List.of(
						"11", "",
						"3", "",
						"/", "",
						"=", "3" // Normal
				)),
				new RudimentaryTest(List.of(
						"11", "",
						"3", "",
						"%", "",
						"=", "2" // Normal
				)),
				// Tests 2
				new RudimentaryTest(List.of(
						"3", "",
						"3", "",
						"*", "",
						"4", "",
						"4", "",
						"*", "",
						"+", "",
						"=", "25" // Normal
				)),
				new RudimentaryTest(List.of(
						"1234", "",
						"2345", "",
						"3456", "",
						"d", "1234\n2345\n3456",
						"+", "",
						"d", "1234\n5801",
						"+", "",
						"d", "7035",
						"=", "7035" // Normal
				)),
				// Tests 3
				new RudimentaryTest(List.of(
						"2147483647", "",
						"1", "",
						"+", "",
						"=", "2147483647" // Boundary
				)),
				new RudimentaryTest(List.of(
						"-2147483647", "",
						"1", "",
						"-", "",
						"=", "-2147483648",
						"20", "",
						"-", "",
						"=", "-2147483648" // Boundary
				)),
				new RudimentaryTest(List.of(
						"100000", "",
						"0", "",
						"-", "",
						"d", "100000",
						"*", "Stack underflow.",
						"=", "100000" // Boundary
				)),
				// Tests 4
				new RudimentaryTest(List.of(
						"1", "",
						"+", "Stack underflow." // Boundary
				)),
				new RudimentaryTest(List.of(
						"10", "",
						"5", "",
						"-5", "",
						"+", "",
						"/", "Divide by 0." // Boundary
				)),
				new RudimentaryTest(List.of(
						"11+1+1+d", "Stack underflow.\n13" // Boundary
				)),
				new RudimentaryTest(List.of(
						"# This i s a comment #", "",
						"1 2 + # And so i s t h i s #", "",
						"d", "3" // Normal
				)),
				new RudimentaryTest(List.of(
						"3 3 ^ 3 ^ 3 ^=", "3" // Boundary
				)),
				new RudimentaryTest(List.of(
						"r r r r r r r r r r r r r r r r r r r r r r d r r r d", """
								1804289383
								846930886
								1681692777
								1714636915
								1957747793
								424238335
								719885386
								1649760492
								596516649
								1189641421
								1025202362
								1350490027
								783368690
								1102520059
								2044897763
								1967513926
								1365180540
								1540383426
								304089172
								1303455736
								35005211
								521595368
								Stack overflow.
								Stack overflow.
								1804289383
								846930886
								1681692777
								1714636915
								1957747793
								424238335
								719885386
								1649760492
								596516649
								1189641421
								1025202362
								1350490027
								783368690
								1102520059
								2044897763
								1967513926
								1365180540
								1540383426
								304089172
								1303455736
								35005211
								521595368
								1804289383""" // Boundary
				)),
				// Custom tests
				// Simple operation with variable whitespace
				new RudimentaryTest(List.of(
						"          10                    6     -               =", "4" // Normal
				)),
				// Double operator
				new RudimentaryTest(List.of(
						"25 6 - - =", "Stack underflow.\n19" // Boundary
				)),
				// Triple operator
				new RudimentaryTest(List.of(
						"38 7 + + + =", "Stack underflow.\nStack underflow.\n45" // Boundary
				)),
				// Sequential numbers (stack precedence)
				new RudimentaryTest(List.of(
						"16 57 83 92 26 72 * =", "1872" // Boundary
				)),
				// Consecutive with no whitespace stack inspection
				new RudimentaryTest(List.of(
						"-10-10-10-10", "",
						"d", "-20"  // Boundary
				)),
				// Consecutive with no whitespace and sign & number variance stack inspection
				new RudimentaryTest(List.of(
						"-10-10+30-10-10", "",
						"d", "-50" // Boundary
				)),
				// Operator first
				new RudimentaryTest(List.of(
						"* 6 8 =", "Stack underflow.\n8" // Boundary
				)),
				// Double operator first
				new RudimentaryTest(List.of(
						"* / 3 9 =", "Stack underflow.\nStack underflow.\n9" // Boundary
				)),
				// Stack interrogation (test empty, test initialisation value, & test sequential random)
				new RudimentaryTest(List.of(
						"= d r = d r = d r = d r = d r =", """
								Stack empty.
								-2147483648
								1804289383
								1804289383
								846930886
								1804289383
								846930886
								1681692777
								1804289383
								846930886
								1681692777
								1714636915
								1804289383
								846930886
								1681692777
								1714636915
								1957747793""" // Boundary
				)),
				// All alphanumeric, all numbers, all characters, & variable whitespace (randomised)
				new RudimentaryTest(List.of(
						"ZDPx   K% =Q~  bkMYL d4*}.E3s`y\"o:5  X2+t Ua;S^6|   qf&C  cvj1 (_[p9,8HWzr {#/  ?gGN   )ei TO@>l  V!A m]n-<IRB' h07   Ju$wF", """
								Unrecognised operator or operand "Z".
								Unrecognised operator or operand "D".
								Unrecognised operator or operand "P".
								Unrecognised operator or operand "x".
								Unrecognised operator or operand "K".
								Stack underflow.
								Stack empty.
								Unrecognised operator or operand "Q".
								Unrecognised operator or operand "~".
								Unrecognised operator or operand "b".
								Unrecognised operator or operand "k".
								Unrecognised operator or operand "M".
								Unrecognised operator or operand "Y".
								Unrecognised operator or operand "L".
								-2147483648
								Unrecognised operator or operand "}".
								Unrecognised operator or operand ".".
								Unrecognised operator or operand "E".
								Unrecognised operator or operand "s".
								Unrecognised operator or operand "`".
								Unrecognised operator or operand "y".
								Unrecognised operator or operand ""\".
								Unrecognised operator or operand "o".
								Unrecognised operator or operand ":".
								Unrecognised operator or operand "X".
								Unrecognised operator or operand "t".
								Unrecognised operator or operand "U".
								Unrecognised operator or operand "a".
								Unrecognised operator or operand ";".
								Unrecognised operator or operand "S".
								Unrecognised operator or operand "|".
								Unrecognised operator or operand "q".
								Unrecognised operator or operand "f".
								Unrecognised operator or operand "&".
								Unrecognised operator or operand "C".
								Unrecognised operator or operand "c".
								Unrecognised operator or operand "v".
								Unrecognised operator or operand "j".
								Unrecognised operator or operand "(".
								Unrecognised operator or operand "_".
								Unrecognised operator or operand "[".
								Unrecognised operator or operand "p".
								Unrecognised operator or operand ",".
								Unrecognised operator or operand "H".
								Unrecognised operator or operand "W".
								Unrecognised operator or operand "z".
								Unrecognised operator or operand "{".
								Unrecognised operator or operand "#".
								Unrecognised operator or operand "?".
								Unrecognised operator or operand "g".
								Unrecognised operator or operand "G".
								Unrecognised operator or operand "N".
								Unrecognised operator or operand ")".
								Unrecognised operator or operand "e".
								Unrecognised operator or operand "i".
								Unrecognised operator or operand "T".
								Unrecognised operator or operand "O".
								Unrecognised operator or operand "@".
								Unrecognised operator or operand ">".
								Unrecognised operator or operand "l".
								Unrecognised operator or operand "V".
								Unrecognised operator or operand "!".
								Unrecognised operator or operand "A".
								Unrecognised operator or operand "m".
								Unrecognised operator or operand "]".
								Unrecognised operator or operand "n".
								Unrecognised operator or operand "<".
								Unrecognised operator or operand "I".
								Unrecognised operator or operand "R".
								Unrecognised operator or operand "B".
								Unrecognised operator or operand "'".
								Unrecognised operator or operand "h".
								Unrecognised operator or operand "J".
								Unrecognised operator or operand "u".
								Unrecognised operator or operand "$".
								Unrecognised operator or operand "w".
								Unrecognised operator or operand "F".""" // Boundary
				)),
				// Text
				new RudimentaryTest(List.of(
						"Lorem ipsum dolor sit amet, consectetur adipiscing elit.", """
								Unrecognised operator or operand "L".
								Unrecognised operator or operand "o".
								Unrecognised operator or operand "e".
								Unrecognised operator or operand "m".
								Unrecognised operator or operand "i".
								Unrecognised operator or operand "p".
								Unrecognised operator or operand "s".
								Unrecognised operator or operand "u".
								Unrecognised operator or operand "m".
								1804289383
								Unrecognised operator or operand "o".
								Unrecognised operator or operand "l".
								Unrecognised operator or operand "o".
								Unrecognised operator or operand "s".
								Unrecognised operator or operand "i".
								Unrecognised operator or operand "t".
								Unrecognised operator or operand "a".
								Unrecognised operator or operand "m".
								Unrecognised operator or operand "e".
								Unrecognised operator or operand "t".
								Unrecognised operator or operand ",".
								Unrecognised operator or operand "c".
								Unrecognised operator or operand "o".
								Unrecognised operator or operand "n".
								Unrecognised operator or operand "s".
								Unrecognised operator or operand "e".
								Unrecognised operator or operand "c".
								Unrecognised operator or operand "t".
								Unrecognised operator or operand "e".
								Unrecognised operator or operand "t".
								Unrecognised operator or operand "u".
								Unrecognised operator or operand "a".
								1804289383
								846930886
								1681692777
								Unrecognised operator or operand "i".
								Unrecognised operator or operand "p".
								Unrecognised operator or operand "i".
								Unrecognised operator or operand "s".
								Unrecognised operator or operand "c".
								Unrecognised operator or operand "i".
								Unrecognised operator or operand "n".
								Unrecognised operator or operand "g".
								Unrecognised operator or operand "e".
								Unrecognised operator or operand "l".
								Unrecognised operator or operand "i".
								Unrecognised operator or operand "t".
								Unrecognised operator or operand ".".""" // Boundary
				)),
				// Consecutive 'rd' x 10; no whitespace
				new RudimentaryTest(List.of(
						"rdrdrdrdrdrdrdrdrdrd", """
								1804289383
								1804289383
								846930886
								1804289383
								846930886
								1681692777
								1804289383
								846930886
								1681692777
								1714636915
								1804289383
								846930886
								1681692777
								1714636915
								1957747793
								1804289383
								846930886
								1681692777
								1714636915
								1957747793
								424238335
								1804289383
								846930886
								1681692777
								1714636915
								1957747793
								424238335
								719885386
								1804289383
								846930886
								1681692777
								1714636915
								1957747793
								424238335
								719885386
								1649760492
								1804289383
								846930886
								1681692777
								1714636915
								1957747793
								424238335
								719885386
								1649760492
								596516649
								1804289383
								846930886
								1681692777
								1714636915
								1957747793
								424238335
								719885386
								1649760492
								596516649
								1189641421""" // Boundary
				)),
				// Garbled with 'r' & 'd'
				new RudimentaryTest(List.of(
						"$r@5 - 8 * 3d!#2 + 6 / $4 + r%^7 - 9d@1 * 2 + 4d#3 - 5r!@6 / $7 + 2d%^8?", """
								Unrecognised operator or operand "$".
								Unrecognised operator or operand "@".
								2147483647
								3
								Unrecognised operator or operand "!".
								Unrecognised operator or operand "#".
								Unrecognised operator or operand "$".
								2147483643
								9
								Unrecognised operator or operand "@".
								2147483643
								11
								4
								Unrecognised operator or operand "#".
								Unrecognised operator or operand "!".
								Unrecognised operator or operand "@".
								Unrecognised operator or operand "$".
								2147483643
								11
								1
								5
								280282136
								2
								Unrecognised operator or operand "?".""" // Boundary
				)),
				// Garbled with 'r' & 'd' (fragmented)
				new RudimentaryTest(List.of(
						"Dr7*$9! - 2 + 3d@5 / r#%@6?", """
								Unrecognised operator or operand "D".
								Unrecognised operator or operand "$".
								Unrecognised operator or operand "!".
								1804289322
								3
								Unrecognised operator or operand "@".
								Unrecognised operator or operand "#".
								Unrecognised operator or operand "@".
								Unrecognised operator or operand "?".""", // Boundary
						"Rd!#6$ * 2 + 9d@3 - r%^7 / 4 + $5?", """
								Unrecognised operator or operand "R".
								1804289322
								0
								4
								Unrecognised operator or operand "!".
								Unrecognised operator or operand "#".
								Unrecognised operator or operand "$".
								1804289322
								0
								26
								9
								Unrecognised operator or operand "@".
								Unrecognised operator or operand "$".
								Unrecognised operator or operand "?".""", // Boundary
						"$2d!7 * 4 - r#@3 + 5 / 9dr$?", """
								Unrecognised operator or operand "$".
								1804289322
								0
								8
								5
								2
								Unrecognised operator or operand "!".
								Unrecognised operator or operand "#".
								Unrecognised operator or operand "@".
								1804289322
								0
								8
								5
								10
								342927383
								9
								Unrecognised operator or operand "$".
								Unrecognised operator or operand "?".""", // Boundary
						"9dr@5 + $3 * r!#2 - 7d%^6 / $4?", """
								1804289322
								0
								8
								5
								10
								342927383
								9
								1957747793
								9
								Unrecognised operator or operand "@".
								Unrecognised operator or operand "$".
								Unrecognised operator or operand "!".
								Unrecognised operator or operand "#".
								1804289322
								0
								8
								5
								10
								342927383
								9
								1957747793
								9
								1272715020
								719885384
								7
								Unrecognised operator or operand "$".
								Unrecognised operator or operand "?".""", // Boundary
						"7 * dr@2 - $4 + 6 / r!#9d$?", """
								1804289322
								0
								8
								5
								10
								342927383
								9
								1957747793
								9
								11697
								28
								Unrecognised operator or operand "@".
								Unrecognised operator or operand "$".
								Unrecognised operator or operand "!".
								Unrecognised operator or operand "#".
								1804289322
								0
								8
								5
								10
								342927383
								9
								1957747793
								9
								11697
								28
								274960082
								596516649
								9
								Unrecognised operator or operand "$".
								Unrecognised operator or operand "?".""" // Boundary
				)),
				// Bonus test - the input below will "crash" both the original program and the emulator
				new RudimentaryTest(List.of(
						"E  BzHf      :pr>  $i<e9     S=D  G)QVO xcw!A0  }t6^R  %/{s_v .hF, Yqam1g'&   Ky@X?UJd2*    " +
								"]u+|5;j CZkTN`3o(7       4lIb\"8  [W#n-MP~L", "" // Error
				))
		);

		var testsPassed = 0;
		var byteArrayOutputStream = new ByteArrayOutputStream();
		var customPrintStream = new PrintStream(byteArrayOutputStream);

		// n.b. each test case ('RudimentaryTest') presumes from program start and resets for every new test case
		// (hence stack clear & random number current index reset)
		for (var i = 0; i < testCases.size(); i++) {
			var testCase = testCases.get(i);

			var print = String.format("%nTest case %d%n", i + 1);
			System.out.print(print);
			System.out.println(Character.toString('-').repeat(10 + (int) Math.log10(i + 1) + 1) + "\n");

			stack.clear(); // Always clear stack between tests
			randomCurrentIndex = 0; // Always clear random number current index between tests

			var originalPrintStream = System.out;

			String input, expectedOutput;
			StringTokenizer stringTokenizer;
			var commentFlag = false;
			var subTestsPassed = 0;

			// Each loop iteration corresponds to an input and an output (line-by-line)
			for(var j = 0; j < testCase.testCases.size(); j += 2) {
				input = replaceEscapeCharacters(testCase.testCases.get(j));
				expectedOutput = replaceEscapeCharacters(testCase.testCases.get(j + 1));

				System.out.printf("\tChecking input '%s' gives output '%s' ...%n", input, expectedOutput);


				stringTokenizer = new StringTokenizer(input);
				try {
					// Get SRPN emulator output using custom print stream
					System.setOut(customPrintStream);
					while (stringTokenizer.hasMoreTokens()) {
						var token = stringTokenizer.nextToken();
						if (token.equals("#")) {
							commentFlag = !commentFlag;
						} else if (!commentFlag) {
							processToken(token);
						}
					}
					System.setOut(originalPrintStream);
				}
				catch(ArithmeticException e) {
					if(testCase.testCases.get(j).equals("E  BzHf      :pr>  $i<e9     S=D  G)QVO xcw!A0  }t6^R  %/{s_v" +
							" " +
							".hF, Yqam1g'&   Ky@X?UJd2*    ]u+|5;j CZkTN`3o(7       4lIb\"8  [W#n-MP~L")) {
						testsPassed++;
						subTestsPassed++;
						System.setOut(originalPrintStream);
						System.out.println("\t\t\u001B[32mProgram has crashed as expected.\u001B[0m");
						System.out.printf("%n%d out of %d tests have passed%n", testsPassed, testCases.size());
						System.setOut(customPrintStream);
						byteArrayOutputStream.reset();
						System.out.flush();
						System.exit(0);
					}
				}

				// Ignore all carriage return escape characters and ignore all end of line new line escape characters
				var actualOutput = replaceEscapeCharacters(
						byteArrayOutputStream.toString()
								.replaceAll("\r", "")
								.replaceAll("[\r\n]+$", "")
				);

				var hasSubTestPassed = expectedOutput.equals(actualOutput);

				System.out.printf("\t\tIs output equal: \n\t\t\t%s%n",
						hasSubTestPassed ? "\u001B[32mtrue\u001B[0m" : "\u001B[31mfalse\u001B[0m");
				System.out.printf("\t\t\t\tExpected output: '%s'%n", expectedOutput);
				System.out.printf("\t\t\t\tActual output: '%s'%n", actualOutput);

				byteArrayOutputStream.reset();

				System.out.flush();

				if(hasSubTestPassed)
					subTestsPassed++;
			}

			if(subTestsPassed == (testCase.testCases.size() / 2))
				testsPassed++;
		}

		System.out.printf("%n%d out of %d tests have passed%n", testsPassed, testCases.size());
	}
	// endregion

	// region Methods
	/**
	 * Replaces any escape characters for a given input string with corresponding escaped representations.
	 * @param input The input string to be processed.
	 * @return The processed string with escape characters replaced with corresponding escaped representations.
	 */
	public static String replaceEscapeCharacters(String input) {
		return input.replace("\\", "\\\\")
				.replace("\n", "\\n")
				.replace("\t", "\\t")
				.replace("\r", "\\r")
				.replace("\b", "\\b")
				.replace("\f", "\\f");
	}

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
	 * {@link SRPNRudimentaryTests#operators} array.<br>
	 * @param input the input string to be checked.
	 * @return {@code true} if the input is a valid operator, {@code false} otherwise.
	 * @see SRPNRudimentaryTests#operators
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
	 * Operator precedence is based on its position as an element in {@link SRPNRudimentaryTests#operators}.<br>
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
	 * - If the input is equal to 'r', a pseudorandom number is generated using {@link SRPNRudimentaryTests#random()} and is
	 * pushed onto the stack.
	 * - If the input is an octal number, it is converted to its decimal representation using the
	 * convertOctalToDecimal(int) method. The result is then limit checked (range) before being pushed onto the stack.
	 * <br>
	 * - If the input is a valid numerical input, it is parsed to a double, limit checked (range), and pushed onto the
	 * stack.<br>
	 * @param input the input representing a number.
	 * @see SRPNRudimentaryTests#random()
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