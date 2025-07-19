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
package org.miaixz.bus.extra.captcha.strategy;

import java.io.Serial;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.math.Calculator;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 数字计算验证码生成策略
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MathStrategy implements CodeStrategy {

    @Serial
    private static final long serialVersionUID = 2852292238303L;

    private static final String operators = "+-*";

    /**
     * 参与计算数字最大长度
     */
    private final int numberLength;
    /**
     * 计算结果是否允许负数
     */
    private final boolean resultHasNegativeNumber;

    /**
     * 构造
     */
    public MathStrategy() {
        this(2, false);
    }

    /**
     * 构造
     *
     * @param numberLength 参与计算最大数字位数
     */
    /**
     * 构造
     *
     * @param numberLength            参与计算最大数字位数
     * @param resultHasNegativeNumber 结果是否允许负数
     */
    public MathStrategy(final int numberLength, final boolean resultHasNegativeNumber) {
        this.numberLength = numberLength;
        this.resultHasNegativeNumber = resultHasNegativeNumber;
    }

    @Override
    public String generate() {
        final int limit = getLimit();
        final char operator = RandomKit.randomChar(operators);
        final int numberInt1;
        final int numberInt2;
        numberInt1 = RandomKit.randomInt(limit);
        // 如果禁止了结果有负数，且计算方式正好计算为减法，需要第二个数小于第一个数
        if (!resultHasNegativeNumber && CharUtil.equals('-', operator, false)) {
            // 如果第一个数为0，第二个数必须为0，随机[0,0)的数字会报错
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

    @Override
    public boolean verify(final String code, final String userInputCode) {
        final int result;
        try {
            result = Integer.parseInt(userInputCode);
        } catch (final NumberFormatException e) {
            // 用户输入非数字
            return false;
        }

        final int calculateResult = (int) Calculator.conversion(code);
        return result == calculateResult;
    }

    /**
     * 获取验证码长度
     *
     * @return 验证码长度
     */
    public int getLength() {
        return this.numberLength * 2 + 2;
    }

    /**
     * 根据长度获取参与计算数字最大值
     *
     * @return 最大值
     */
    private int getLimit() {
        return Integer.parseInt("1" + StringKit.repeat('0', this.numberLength));
    }

}
