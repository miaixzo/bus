/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.sensitive.strategy;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.sensitive.Context;
import org.aoju.bus.sensitive.provider.StrategyProvider;

/**
 * 银行卡号脱敏
 * 只留前四位和后四位
 * 6227 0383 3938 3938 393 脱敏结果: 6227 **** **** ***8 393
 *
 * @author Kimi Liu
 * @version 3.2.8
 * @since JDK 1.8
 */
public class BandCardStrategy implements StrategyProvider {

    @Override
    public String build(Object object, Context context) {
        if (object == null) {
            return null;
        }
        //Mode mode = this.builder.getMode();

        String bankCard = object.toString();
        return StringUtils.left(bankCard, 4).concat(StringUtils.removeStart(StringUtils.leftPad(StringUtils.right(bankCard, 4), StringUtils.length(bankCard), "*"), "***"));
    }

}
