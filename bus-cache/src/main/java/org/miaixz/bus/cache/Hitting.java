/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.cache;

import org.miaixz.bus.core.lang.Symbol;

import java.util.Map;

/**
 * 缓存命中策略
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Hitting {

    void reqIncr(String pattern, int count);

    void hitIncr(String pattern, int count);

    Map<String, HittingDO> getHitting();

    void reset(String pattern);

    void resetAll();

    default String summaryName() {
        return "zh".equalsIgnoreCase(System.getProperty("user.language")) ? "全局" : "summary";
    }

    class HittingDO {

        private long hit;

        private long required;

        private String rate;

        private HittingDO(long hit, long required, String rate) {
            this.hit = hit;
            this.required = required;
            this.rate = rate;
        }

        public static HittingDO newInstance(long hit, long required) {
            double rate = (required == 0 ? 0.0 : hit * 100.0 / required);
            String rateStr = String.format("%.1f%s", rate, Symbol.PERCENT);

            return new HittingDO(hit, required, rateStr);
        }

        public static HittingDO mergeShootingDO(HittingDO do1, HittingDO do2) {
            long hit = do1.getHit() + do2.getHit();
            long required = do1.getRequired() + do2.getRequired();

            return newInstance(hit, required);
        }

        public long getHit() {
            return hit;
        }

        public long getRequired() {
            return required;
        }

        public String getRate() {
            return rate;
        }
    }

}
