/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
