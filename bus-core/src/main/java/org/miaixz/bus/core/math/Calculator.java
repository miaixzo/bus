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
 * A utility class for evaluating mathematical expressions.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Calculator {

    /**
     * The stack for the postfix expression.
     */
    private final Stack<String> postfixStack = new Stack<>();
    /**
     * Operator priorities, indexed by the operator's ASCII value minus 40.
     */
    private final int[] operatPriority = new int[] { 0, 3, 2, 1, -1, 1, 0, 2 };

    /**
     * Calculates the value of a given mathematical expression.
     *
     * @param expression The expression to evaluate.
     * @return The result of the calculation.
     */
    public static double conversion(final String expression) {
        return (new Calculator()).calculate(expression);
    }

    /**
     * Transforms the expression by changing the sign of negative numbers. For example, -2+-1*(-3E-2)-(-1) is converted
     * to ~2+~1*(~3E~2)-(~1).
     *
     * @param expression The expression to transform.
     * @return The transformed string.
     */
    private static String transform(String expression) {
        expression = StringKit.cleanBlank(expression);
        expression = StringKit.removeSuffix(expression, Symbol.EQUAL);
        final char[] arr = expression.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == Symbol.C_MINUS) {
                if (i == 0) {
                    arr[i] = Symbol.C_TILDE;
                } else {
                    final char c = arr[i - 1];
                    if (c == Symbol.C_PLUS || c == Symbol.C_MINUS || c == Symbol.C_STAR || c == '/'
                            || c == Symbol.C_PARENTHESE_LEFT || c == 'E' || c == 'e') {
                        arr[i] = Symbol.C_TILDE;
                    }
                }
            } else if (CharKit.equals(arr[i], Character.toLowerCase(Symbol.C_X), true)) {
                // Convert 'x' to '*'.
                arr[i] = Symbol.C_STAR;
            }
        }
        if (arr[0] == Symbol.C_TILDE && (arr.length > 1 && arr[1] == Symbol.C_PARENTHESE_LEFT)) {
            arr[0] = Symbol.C_MINUS;
            return "0" + new String(arr);
        } else {
            return new String(arr);
        }
    }

    /**
     * Calculates the result of the given expression. For example: 5+12*(3+5)/7
     *
     * @param expression The expression to calculate.
     * @return The result of the calculation.
     */
    public double calculate(final String expression) {
        prepare(transform(expression));

        final Stack<String> resultStack = new Stack<>();
        Collections.reverse(postfixStack); // Reverse the postfix stack.
        String firstValue, secondValue, currentOp; // The first value, second value, and operator for the calculation.
        while (!postfixStack.isEmpty()) {
            currentOp = postfixStack.pop();
            if (!isOperator(currentOp.charAt(0))) { // If it's not an operator, push it onto the operand stack.
                currentOp = currentOp.replace(Symbol.TILDE, Symbol.MINUS);
                resultStack.push(currentOp);
            } else { // If it's an operator, pop two values from the operand stack for calculation.
                secondValue = resultStack.pop();
                firstValue = resultStack.pop();

                // Change the negative sign marker back to a minus sign.
                firstValue = firstValue.replace(Symbol.TILDE, Symbol.MINUS);
                secondValue = secondValue.replace(Symbol.TILDE, Symbol.MINUS);

                final BigDecimal tempResult = calculate(firstValue, secondValue, currentOp.charAt(0));
                resultStack.push(tempResult.toString());
            }
        }

        // If there are multiple numbers in the result stack, it may be due to an omitted multiplication operator, e.g.,
        // (1+2)3.
        return MathKit.mul(resultStack.toArray(new String[0])).doubleValue();
    }

    /**
     * Prepares the data by converting the infix expression to a postfix stack.
     *
     * @param expression The expression to prepare.
     */
    private void prepare(final String expression) {
        final Stack<Character> opStack = new Stack<>();
        opStack.push(Symbol.C_COMMA); // Push a comma onto the bottom of the operator stack, as it has the lowest
                                      // priority.
        final char[] arr = expression.toCharArray();
        int currentIndex = 0; // The current character's position.
        int count = 0; // The length of the substring between the last and current operators, used to extract the
                       // number.
        char currentOp, peekOp; // The current operator and the operator at the top of the stack.
        for (int i = 0; i < arr.length; i++) {
            currentOp = arr[i];
            if (isOperator(currentOp)) { // If the current character is an operator.
                if (count > 0) {
                    postfixStack.push(new String(arr, currentIndex, count)); // Extract the number between the two
                                                                             // operators.
                }
                peekOp = opStack.peek();
                if (currentOp == ')') { // If a right parenthesis is found, pop operators to the postfix stack until a
                                        // left parenthesis is found.
                    while (opStack.peek() != Symbol.C_PARENTHESE_LEFT) {
                        postfixStack.push(String.valueOf(opStack.pop()));
                    }
                    opStack.pop();
                } else {
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
        if (count > 1 || (count == 1 && !isOperator(arr[currentIndex]))) { // If the last character is not a parenthesis
                                                                           // or other operator, add it to the postfix
                                                                           // stack.
            postfixStack.push(new String(arr, currentIndex, count));
        }

        while (opStack.peek() != Symbol.C_COMMA) {
            postfixStack.push(String.valueOf(opStack.pop())); // Add the remaining operators from the operator stack to
                                                              // the postfix stack.
        }
    }

    /**
     * Checks if a character is an arithmetic operator.
     *
     * @param c The character to check.
     * @return {@code true} if it is an arithmetic operator, {@code false} otherwise.
     */
    private boolean isOperator(final char c) {
        return c == Symbol.C_PLUS || c == Symbol.C_MINUS || c == Symbol.C_STAR || c == '/'
                || c == Symbol.C_PARENTHESE_LEFT || c == ')' || c == Symbol.C_PERCENT;
    }

    /**
     * Compares the priority of two operators using their ASCII values minus 40 as an index.
     *
     * @param cur  The current operator.
     * @param peek The operator at the top of the stack.
     * @return {@code true} if the priority of peek is greater than or equal to cur, {@code false} otherwise.
     */
    private boolean compare(char cur, char peek) { // Returns true if peek has higher or equal priority than cur.
        final int offset = 40;
        if (cur == Symbol.C_PERCENT) {
            // The '%' operator has the highest priority.
            cur = 47;
        }
        if (peek == Symbol.C_PERCENT) {
            // The '%' operator has the highest priority.
            peek = 47;
        }

        return operatPriority[peek - offset] >= operatPriority[cur - offset];
    }

    /**
     * Performs a calculation based on the given arithmetic operator.
     *
     * @param firstValue  The first value.
     * @param secondValue The second value.
     * @param currentOp   The arithmetic operator, supporting only '+', '-', '*', '/', and '%'.
     * @return The result of the calculation.
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
