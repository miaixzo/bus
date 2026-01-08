/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.extra.captcha.strategy;

import java.io.Serial;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.math.Calculator;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Math calculation CAPTCHA generation strategy.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MathStrategy implements CodeStrategy {

    @Serial
    private static final long serialVersionUID = 2852292238303L;

    /**
     * Operators used in the math expression.
     */
    private static final String operators = "+-*";

    /**
     * The maximum length of numbers involved in the calculation.
     */
    private final int numberLength;
    /**
     * Whether the calculation result is allowed to be a negative number.
     */
    private final boolean resultHasNegativeNumber;

    /**
     * Constructs a new {@code MathStrategy} with default settings. Uses a number length of 2 and does not allow
     * negative results.
     */
    public MathStrategy() {
        this(2, false);
    }

    /**
     * Constructs a new {@code MathStrategy} with the specified number length and negative result allowance.
     *
     * @param numberLength            The maximum number of digits for numbers involved in the calculation.
     * @param resultHasNegativeNumber {@code true} if the calculation result can be negative, {@code false} otherwise.
     */
    public MathStrategy(final int numberLength, final boolean resultHasNegativeNumber) {
        this.numberLength = numberLength;
        this.resultHasNegativeNumber = resultHasNegativeNumber;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Generates a random math expression CAPTCHA code with two numbers and an operator (+, -, or *). The numbers are
     * padded with spaces to the specified length for visual consistency.
     * </p>
     *
     * @return a math expression string in the format "number1 operator number2="
     */
    @Override
    public String generate() {
        final int limit = getLimit();
        final char operator = RandomKit.randomChar(operators);
        final int numberInt1;
        final int numberInt2;
        numberInt1 = RandomKit.randomInt(limit);
        // If negative results are forbidden and the operation is subtraction, the second number must be less than the
        // first.
        if (!resultHasNegativeNumber && CharKit.equals(Symbol.C_MINUS, operator, false)) {
            // If the first number is 0, the second number must be 0; generating a random number in [0,0) would cause an
            // error.
            numberInt2 = numberInt1 == 0 ? 0 : RandomKit.randomInt(0, numberInt1);
        } else {
            numberInt2 = RandomKit.randomInt(limit);
        }
        String number1 = Integer.toString(numberInt1);
        String number2 = Integer.toString(numberInt2);

        number1 = StringKit.padAfter(number1, this.numberLength, Symbol.C_SPACE);
        number2 = StringKit.padAfter(number2, this.numberLength, Symbol.C_SPACE);

        return StringKit.builder().append(number1).append(operator).append(number2).append('=').toString();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Verifies the user's mathematical answer by evaluating the expression and comparing it with the user's input.
     * </p>
     *
     * @param code          the generated math expression (e.g., "1 + 2 =")
     * @param userInputCode the user's calculated answer
     * @return {@code true} if the user's answer matches the calculated result, {@code false} otherwise
     */
    @Override
    public boolean verify(final String code, final String userInputCode) {
        final int result;
        try {
            result = Integer.parseInt(userInputCode);
        } catch (final NumberFormatException e) {
            // User input is not a number
            return false;
        }

        final int calculateResult = (int) Calculator.conversion(code);
        return result == calculateResult;
    }

    /**
     * Gets the length of the CAPTCHA code string (including numbers, operator, and equals sign).
     *
     * @return The length of the CAPTCHA code.
     */
    public int getLength() {
        return this.numberLength * 2 + 2;
    }

    /**
     * Gets the upper limit for the numbers involved in the calculation based on {@code numberLength}.
     *
     * @return The maximum value for the numbers.
     */
    private int getLimit() {
        return Integer.parseInt("1" + StringKit.repeat('0', this.numberLength));
    }

}
