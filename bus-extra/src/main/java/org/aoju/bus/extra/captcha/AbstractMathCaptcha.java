/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
package org.aoju.bus.extra.captcha;

import java.util.Random;

/**
 * @author Kimi Liu
 * @version 5.8.6
 * @since JDK 1.8+
 */
public abstract class AbstractMathCaptcha extends AbstractCaptcha {

    /**
     * 生成随机加减验证码
     *
     * @return 验证码字符数组
     */
    @Override
    protected char[] alphas() {
        // 生成随机类
        Random random = new Random();
        char[] cs = new char[4];
        int rand0 = random.nextInt(10);
        if (rand0 == 0) {
            rand0 = 1;
        }
        int rand1 = random.nextInt(10);
        boolean rand2 = random.nextBoolean();
        int rand3 = random.nextInt(10);
        cs[0] = (char) ('0' + rand0);
        cs[1] = (char) ('0' + rand1);
        cs[2] = rand2 ? '+' : '-';
        cs[3] = (char) ('0' + rand3);

        int num1 = rand0 * 10 + rand1;
        int num2 = rand3;
        int result = rand2 ? num1 + num2 : num1 - num2;
        chars = String.valueOf(result);
        return cs;
    }

}
