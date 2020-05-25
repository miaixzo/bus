/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.extra.captcha.strategy;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.RandomUtils;
import org.aoju.bus.core.utils.StringUtils;

/**
 * 数字计算验证码生成器
 *
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
public class MathStrategy implements CodeStrategy {

    /**
     * 参与计算数字最大长度
     */
    private final int numberLength;

    /**
     * 构造
     */
    public MathStrategy() {
        this(2);
    }

    /**
     * 构造
     *
     * @param numberLength 参与计算最大数字位数
     */
    public MathStrategy(int numberLength) {
        this.numberLength = numberLength;
    }

    @Override
    public String generate() {
        final int limit = getLimit();
        String number1 = Integer.toString(RandomUtils.randomInt(limit));
        String number2 = Integer.toString(RandomUtils.randomInt(limit));
        number1 = StringUtils.padAfter(number1, this.numberLength, Symbol.C_SPACE);
        number2 = StringUtils.padAfter(number2, this.numberLength, Symbol.C_SPACE);

        return StringUtils.builder()
                .append(number1)
                .append(RandomUtils.randomChar(Symbol.PLUS + Symbol.HYPHEN + Symbol.STAR))
                .append(number2)
                .append('=').toString();
    }

    @Override
    public boolean verify(String code, String userInputCode) {
        int result;
        try {
            result = Integer.parseInt(userInputCode);
        } catch (NumberFormatException e) {
            return false;
        }

        final int a = Integer.parseInt(StringUtils.sub(code, 0, this.numberLength).trim());
        final char operator = code.charAt(this.numberLength);
        final int b = Integer.parseInt(StringUtils.sub(code, this.numberLength + 1, this.numberLength + 1 + this.numberLength).trim());

        switch (operator) {
            case Symbol.C_PLUS:
                return (a + b) == result;
            case Symbol.C_HYPHEN:
                return (a - b) == result;
            case Symbol.C_STAR:
                return (a * b) == result;
            default:
                return false;
        }
    }

    /**
     * 获取长度验证码
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
        return Integer.parseInt(Symbol.C_ONE + StringUtils.repeat(Symbol.C_ZERO, this.numberLength));
    }

}
