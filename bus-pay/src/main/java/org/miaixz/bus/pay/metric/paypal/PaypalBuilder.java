/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.pay.metric.paypal;

import java.util.concurrent.Callable;

import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.pay.magic.Callback;

/**
 * A utility class for PayPal operations, including retry logic for API calls.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PaypalBuilder {

    /**
     * Retries a callable operation upon exception.
     *
     * @param <V>           The return type of the callable, which must extend {@link Callback}.
     * @param retryLimit    The maximum number of retry attempts.
     * @param retryCallable The callable operation to be executed.
     * @return The result of the callable operation, or null if it fails after all retries.
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
     * Retries a callable operation upon exception, with a specified delay between retries.
     *
     * @param <V>           The return type of the callable, which must extend {@link Callback}.
     * @param retryLimit    The maximum number of retry attempts.
     * @param sleepMillis   The time to sleep in milliseconds between retries.
     * @param retryCallable The callable operation to be executed.
     * @return The result of the callable operation, or null if it fails after all retries.
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
