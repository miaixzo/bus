/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.math;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Stack;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A utility class for evaluating mathematical expressions using the Shunting Yard algorithm.
 * <p>
 * This calculator supports basic arithmetic operators (+, -, *, /, %), parentheses, and scientific notation. It handles
 * operator precedence and converts infix expressions to postfix (Reverse Polish Notation) for evaluation.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Calculator {

    /**
     * The stack used to store the postfix expression (Reverse Polish Notation).
     */
    private final Stack<String> postfixStack = new Stack<>();
    /**
     * Operator priorities, indexed by the operator's ASCII value minus 40.
     * <p>
     * ASCII mapping reference (Offset 40):
     * <ul>
     * <li>40 '(': Priority 0</li>
     * <li>41 ')': Priority 3</li>
     * <li>42 '*': Priority 2</li>
     * <li>43 '+': Priority 1</li>
     * <li>44 ',': Priority -1 (Sentinel)</li>
     * <li>45 '-': Priority 1</li>
     * <li>46 '.': Priority 0</li>
     * <li>47 '/': Priority 2</li>
     * </ul>
     * </p>
     */
    private final int[] operatPriority = new int[] { 0, 3, 2, 1, -1, 1, 0, 2 };

    /**
     * Calculates the value of a given mathematical expression.
     *
     * @param expression The expression to evaluate (e.g., "3 + 4 * 2").
     * @return The result of the calculation as a double.
     */
    public static double conversion(final String expression) {
        return (new Calculator()).calculate(expression);
    }

    /**
     * Transforms the expression by preparing negative numbers and unary operators for parsing.
     * <p>
     * For example, it converts {@code -2+-1*(-3E-2)-(-1)} to {@code ~2+~1*(~3E~2)-(~1)}, where '~' represents a unary
     * minus to distinguish it from the binary subtraction operator.
     * </p>
     *
     * @param expression The expression to transform.
     * @return The transformed string.
     */
    private static String transform(String expression) {
        expression = StringKit.cleanBlank(expression);
        expression = StringKit.removeSuffix(expression, Symbol.EQUAL);
        final char[] arr = expression.toCharArray();

        final StringBuilder out = new StringBuilder(arr.length);
        for (int i = 0; i < arr.length; i++) {
            final char c = arr[i];

            // Treat 'x' or 'X' as multiplication operator '*'
            if (CharKit.equals(c, 'x', true)) {
                out.append('*');
                continue;
            }

            // If it's '+' or '-', determine if it's a sign (scientific notation), a binary operator, or a unary
            // operator
            if (c == '+' || c == '-') {
                // If the previous character written was 'e' or 'E', treat this as a sign for scientific notation
                final int outLen = out.length();
                if (outLen > 0) {
                    final char prevOut = out.charAt(outLen - 1);
                    if (prevOut == 'e' || prevOut == 'E') {
                        // After e/E:
                        // '+' can be safely discarded (1e+3 == 1e3)
                        // '-' must be kept but should not be treated as a binary operator, so replace with '~'
                        // temporarily
                        if (c == '-') {
                            out.append('~');
                        }
                        continue;
                    }
                }

                // Find the previous non-whitespace character in the original string to check for unary context
                int j = i - 1;
                while (j >= 0 && Character.isWhitespace(arr[j]))
                    j--;
                final boolean unaryContext = (j < 0) || isPrevCharOperatorOrLeftParen(arr[j]);

                if (unaryContext) {
                    // Collect consecutive unary + or - (e.g., --+ - -> merge into a single net sign)
                    int k = i;
                    int minusCount = 0;
                    while (k < arr.length && (arr[k] == '+' || arr[k] == '-')) {
                        if (arr[k] == '-')
                            minusCount++;
                        k++;
                    }
                    final boolean netNegative = (minusCount % 2 == 1);
                    if (netNegative) {
                        // Mark unary minus with '~' (compatible with original implementation)
                        out.append('~');
                    }
                    i = k - 1;
                } else {
                    // Binary operator, write + or - directly
                    out.append(c);
                }
                continue;
            }
            // Append other characters (digits, letters, parentheses, e, E, decimal points, etc.) directly
            out.append(c);
        }

        // Special handling: if it starts with "~(", convert it to "0-(" (handled as 0 minus group)
        final String result = out.toString();
        final char[] resArr = result.toCharArray();
        if (resArr.length >= 2 && resArr[0] == '~' && resArr[1] == '(') {
            resArr[0] = '-';
            return "0" + new String(resArr);
        } else {
            return result;
        }
    }

    /**
     * Checks if the character preceding the current position is an operator or a left parenthesis. This is used to
     * determine if a subsequent '+' or '-' is a unary operator.
     *
     * @param c The character to check.
     * @return {@code true} if the character indicates a unary context.
     */
    private static boolean isPrevCharOperatorOrLeftParen(final char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '(';
    }

    /**
     * Calculates the result of the given expression.
     * <p>
     * Example: {@code 5+12*(3+5)/7}
     * </p>
     *
     * @param expression The expression to calculate.
     * @return The result of the calculation.
     */
    public double calculate(final String expression) {
        prepare(transform(expression));

        final Stack<String> resultStack = new Stack<>();
        Collections.reverse(postfixStack); // Reverse the list view to process from start to end (Shunting Yard output)
        String firstValue, secondValue, currentOp;
        while (!postfixStack.isEmpty()) {
            currentOp = postfixStack.pop();
            if (!isOperator(currentOp.charAt(0))) {
                // If it's not an operator, push it onto the operand stack
                currentOp = currentOp.replace(Symbol.TILDE, Symbol.MINUS);
                resultStack.push(currentOp);
            } else {
                // If it's an operator, pop two values from the operand stack for calculation
                secondValue = resultStack.pop();
                firstValue = resultStack.pop();

                // Change the temporary negative sign marker back to a standard minus sign
                firstValue = firstValue.replace(Symbol.TILDE, Symbol.MINUS);
                secondValue = secondValue.replace(Symbol.TILDE, Symbol.MINUS);

                final BigDecimal tempResult = calculate(firstValue, secondValue, currentOp.charAt(0));
                resultStack.push(tempResult.toString());
            }
        }

        // If multiple numbers remain, it may be due to omitted multiplication, e.g., (1+2)3 -> 3 * 3
        return MathKit.mul(resultStack.toArray(new String[0])).doubleValue();
    }

    /**
     * Prepares the data by converting the infix expression to a postfix stack (Shunting Yard Algorithm).
     *
     * @param expression The expression to prepare.
     */
    private void prepare(final String expression) {
        final Stack<Character> opStack = new Stack<>();
        opStack.push(Symbol.C_COMMA); // Push a comma sentinel to the stack bottom (lowest priority)
        final char[] arr = expression.toCharArray();
        int currentIndex = 0; // Start index of the current number substring
        int count = 0; // Length of the current number substring
        char currentOp, peekOp;
        for (int i = 0; i < arr.length; i++) {
            currentOp = arr[i];
            if (isOperator(currentOp)) { // If current char is an operator
                if (count > 0) {
                    // Push the number preceding the operator to the output stack
                    postfixStack.push(new String(arr, currentIndex, count));
                }
                peekOp = opStack.peek();
                if (currentOp == ')') {
                    // Pop operators to output until a left parenthesis is found
                    while (opStack.peek() != Symbol.C_PARENTHESE_LEFT) {
                        postfixStack.push(String.valueOf(opStack.pop()));
                    }
                    opStack.pop(); // Discard the left parenthesis
                } else {
                    // Push operators with higher or equal precedence from opStack to output
                    while (currentOp != Symbol.C_PARENTHESE_LEFT && peekOp != Symbol.C_COMMA
                            && compare(currentOp, peekOp)) {
                        postfixStack.push(String.valueOf(opStack.pop()));
                        peekOp = opStack.peek();
                    }
                    opStack.push(currentOp);
                }
                count = 0;
                currentIndex = i + 1;
            } else {
                count++;
            }
        }
        // Push the last number if exists
        if (count > 1 || (count == 1 && !isOperator(arr[currentIndex]))) {
            postfixStack.push(new String(arr, currentIndex, count));
        }

        // Pop remaining operators
        while (opStack.peek() != Symbol.C_COMMA) {
            postfixStack.push(String.valueOf(opStack.pop()));
        }
    }

    /**
     * Checks if a character is a supported arithmetic operator.
     *
     * @param c The character to check.
     * @return {@code true} if it is an arithmetic operator, {@code false} otherwise.
     */
    private boolean isOperator(final char c) {
        return c == Symbol.C_PLUS || c == Symbol.C_MINUS || c == Symbol.C_STAR || c == '/'
                || c == Symbol.C_PARENTHESE_LEFT || c == ')' || c == Symbol.C_PERCENT;
    }

    /**
     * Compares the priority of two operators.
     *
     * @param cur  The current operator.
     * @param peek The operator at the top of the stack.
     * @return {@code true} if {@code peek} has higher or equal priority than {@code cur}, {@code false} otherwise.
     */
    private boolean compare(char cur, char peek) {
        final int offset = 40;
        if (cur == Symbol.C_PERCENT) {
            // Map '%' to the same priority index as '/' (47)
            cur = 47;
        }
        if (peek == Symbol.C_PERCENT) {
            // The '%' operator has the highest priority.
            peek = 47;
        }

        return operatPriority[peek - offset] >= operatPriority[cur - offset];
    }

    /**
     * Performs a calculation on two values based on the given arithmetic operator.
     *
     * @param firstValue  The first operand.
     * @param secondValue The second operand.
     * @param currentOp   The arithmetic operator (+, -, *, /, %).
     * @return The result of the calculation as a {@link BigDecimal}.
     * @throws IllegalStateException If an unsupported operator is encountered.
     */
    private BigDecimal calculate(final String firstValue, final String secondValue, final char currentOp) {
        return switch (currentOp) {
            case Symbol.C_PLUS -> MathKit.add(firstValue, secondValue);
            case Symbol.C_MINUS -> MathKit.sub(firstValue, secondValue);
            case Symbol.C_STAR -> MathKit.mul(firstValue, secondValue);
            case Symbol.C_SLASH -> MathKit.div(firstValue, secondValue);
            case Symbol.C_PERCENT -> MathKit.toBigDecimal(firstValue).remainder(MathKit.toBigDecimal(secondValue));
            default -> throw new IllegalStateException("Unexpected value: " + currentOp);
        };
    }

}
