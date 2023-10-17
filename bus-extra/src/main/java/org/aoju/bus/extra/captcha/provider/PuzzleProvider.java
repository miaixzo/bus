/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.extra.captcha.provider;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.extra.captcha.strategy.CodeStrategy;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 滑动验证码
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PuzzleProvider extends AbstractProvider {

    public PuzzleProvider(int width, int height, int codeCount, int interfereCount) {
        super(width, height, codeCount, interfereCount);
    }

    public PuzzleProvider(int width, int height, CodeStrategy generator, int interfereCount) {
        super(width, height, generator, interfereCount);
    }

    @Override
    protected Image createImage(String code) {
        return null;
    }

    @Override
    public String get() {
        return null;

    }

    @Override
    public boolean verify(String inputCode) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < inputCode.length(); i++) {
            char c = inputCode.charAt(i);
            if (c >= Symbol.C_ZERO && c <= Symbol.C_NINE) {
                list.add(Integer.valueOf(String.valueOf(c)));
            }
        }
        int sum = 0;
        for (Integer data : list) {
            sum += data;
        }
        double avg = sum * 1.0 / list.size();
        double sum2 = 0.0;
        for (Integer data : list) {
            sum2 += Math.pow(data - avg, 2);
        }
        double stddev = sum2 / list.size();
        return stddev != 0;
    }

}
