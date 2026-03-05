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
package org.miaixz.bus.http.plugin.httpz;

import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.DateKit;

/**
 * A utility class for tracking global statistics and state for the {@code Httpz} client. It provides thread-safe
 * counters for total requests, failures, and exceptions, as well as a log of recent errors.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HttpzState {

    /**
     * The maximum number of error messages to store.
     */
    private static final int MAX_ERROR_MSG_COUNT = 100;
    /**
     * A counter for the total number of requests made.
     */
    protected static AtomicInteger reqTotalCount = new AtomicInteger(0);
    /**
     * A counter for the number of failed requests (e.g., non-2xx responses).
     */
    protected static AtomicInteger reqFailureCount = new AtomicInteger(0);
    /**
     * A counter for the number of requests that resulted in an exception.
     */
    protected static AtomicInteger reqExceptionCount = new AtomicInteger(0);
    /**
     * The time when the statistics tracking started.
     */
    protected static Date startTime = new Date();
    /**
     * The timestamp of the last recorded request activity.
     */
    protected static Date lastAccessTime;
    /**
     * A deque to store the most recent error messages.
     */
    protected static LinkedBlockingDeque<String> errorMsgs = new LinkedBlockingDeque<>(MAX_ERROR_MSG_COUNT);
    /**
     * A flag to stop statistics collection.
     */
    private static volatile boolean isStop = false;

    /**
     * Stops the collection of statistics.
     */
    public static void stopStat() {
        HttpzState.isStop = true;
    }

    /**
     * @return The total number of requests initiated.
     */
    public static int getReqTotalCount() {
        return reqTotalCount.get();
    }

    /**
     * @return The total number of failed requests.
     */
    public static int getReqFailureCount() {
        return reqFailureCount.get();
    }

    /**
     * @return The total number of requests that threw an exception.
     */
    public static int getReqExceptionCount() {
        return reqExceptionCount.get();
    }

    /**
     * @return The start time of the statistics collection.
     */
    public static Date getStartTime() {
        return startTime;
    }

    /**
     * @return The timestamp of the last request activity.
     */
    public static Date getLastAccessTime() {
        return lastAccessTime;
    }

    /**
     * @return A deque containing the most recent error messages.
     */
    public static LinkedBlockingDeque<String> getErrorMsgs() {
        return errorMsgs;
    }

    /**
     * A callback method invoked when a request fails. It increments the relevant counters and logs the error.
     *
     * @param url The URL of the failed request.
     * @param e   The exception that occurred, if any.
     */
    protected static void onReqFailure(String url, Exception e) {
        if (isStop) {
            return;
        }
        lastAccessTime = new Date();
        reqTotalCount.incrementAndGet();
        reqFailureCount.incrementAndGet();
        if (null != e) {
            reqExceptionCount.incrementAndGet();
            if (errorMsgs.size() >= MAX_ERROR_MSG_COUNT) {
                errorMsgs.removeFirst();
            }
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append(DateKit.format(new Date(), Fields.NORM_DATETIME)).append(Symbol.HT).append(url)
                    .append(Symbol.HT).append(e.getClass().getName()).append(Symbol.HT).append(e.getMessage());
            errorMsgs.add(errorMsg.toString());
        }
    }

    /**
     * A callback method invoked when a request succeeds. It increments the total request counter.
     */
    protected static void onReqSuccess() {
        if (isStop) {
            return;
        }
        lastAccessTime = new Date();
        reqTotalCount.incrementAndGet();
    }

}
