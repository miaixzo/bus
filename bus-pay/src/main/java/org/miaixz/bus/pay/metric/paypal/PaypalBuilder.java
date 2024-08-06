/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.pay.metric.paypal;

import java.util.concurrent.Callable;

import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.pay.magic.Callback;

/**
 * AccessToken
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PaypalBuilder {

    /**
     * 在遇到异常时尝试重试
     *
     * @param retryLimit    重试次数
     * @param retryCallable 重试回调
     * @param <V>           泛型
     * @return V 结果
     */
    public static <V extends Callback> V retryOnException(int retryLimit, Callable<V> retryCallable) {
        V v = null;
        for (int i = 0; i < retryLimit; i++) {
            try {
                v = retryCallable.call();
            } catch (Exception e) {
                Logger.warn("retry on " + (i + 1) + " times v = " + (v == null ? null : v.getJson()), e);
            }
            if (null != v && v.matching())
                break;
            Logger.error("retry on " + (i + 1) + " times but not matching v = " + (v == null ? null : v.getJson()));
        }
        return v;
    }

    /**
     * 在遇到异常时尝试重试
     *
     * @param retryLimit    重试次数
     * @param sleepMillis   每次重试之后休眠的时间
     * @param retryCallable 重试回调
     * @param <V>           泛型
     * @return V 结果
     */
    public static <V extends Callback> V retryOnException(int retryLimit, long sleepMillis, Callable<V> retryCallable) {
        V v = null;
        for (int i = 0; i < retryLimit; i++) {
            try {
                v = retryCallable.call();
            } catch (Exception e) {
                Logger.warn("retry on " + (i + 1) + " times v = " + (v == null ? null : v.getJson()), e);
            }
            if (null != v && v.matching()) {
                break;
            }
            Logger.error("retry on " + (i + 1) + " times but not matching v = " + (v == null ? null : v.getJson()));
            if (sleepMillis > 0) {
                ThreadKit.sleep(sleepMillis);
            }
        }
        return v;
    }

}
