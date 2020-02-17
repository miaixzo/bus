/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.cron.pattern.parser;

import org.aoju.bus.core.lang.exception.InstrumentException;

/**
 * 简易值转换器 将给定String值转为int
 *
 * @author Kimi Liu
 * @version 5.6.2
 * @since JDK 1.8+
 */
public class SimpleValueParser implements ValueParser {

    /**
     * 最小值（包括）
     */
    protected int min;
    /**
     * 最大值（包括）
     */
    protected int max;

    public SimpleValueParser(int min, int max) {
        if (min > max) {
            this.min = max;
            this.max = min;
        } else {
            this.min = min;
            this.max = max;
        }
    }

    @Override
    public int parse(String value) throws InstrumentException {
        int i;
        try {
            i = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InstrumentException("Invalid integer value: [{}]", value);
        }
        if (i < min || i > max) {
            throw new InstrumentException("Value [{}] out of range: [{} , {}]", i, min, max);
        }
        return i;
    }

    @Override
    public int getMin() {
        return this.min;
    }

    @Override
    public int getMax() {
        return this.max;
    }

}
