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
package org.miaixz.bus.http.plugin.httpz;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.DateKit;

import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HTTP状态
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HttpzState {

    private static final int MAX_ERROR_MSG_COUNT = 100;
    protected static AtomicInteger reqTotalCount = new AtomicInteger(0);
    protected static AtomicInteger reqFailureCount = new AtomicInteger(0);
    protected static AtomicInteger reqExceptionCount = new AtomicInteger(0);
    protected static Date startTime = new Date();
    protected static Date lastAccessTime;
    protected static LinkedBlockingDeque<String> errorMsgs = new LinkedBlockingDeque<>(MAX_ERROR_MSG_COUNT);
    private static boolean isStop = false;

    public static void stopStat() {
        HttpzState.isStop = true;
    }

    public static int getReqTotalCount() {
        return reqTotalCount.get();
    }

    public static int getReqFailureCount() {
        return reqFailureCount.get();
    }

    public static int getReqExceptionCount() {
        return reqExceptionCount.get();
    }

    public static Date getStartTime() {
        return startTime;
    }

    public static Date getLastAccessTime() {
        return lastAccessTime;
    }

    public static LinkedBlockingDeque<String> getErrorMsgs() {
        return errorMsgs;
    }

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
            errorMsg.append(DateKit.format(new Date(), Fields.NORM_DATETIME))
                    .append(Symbol.HT)
                    .append(url)
                    .append(Symbol.HT)
                    .append(e.getClass().getName())
                    .append(Symbol.HT)
                    .append(e.getMessage());
            errorMsgs.add(errorMsg.toString());
        }
    }

    protected static void onReqSuccess() {
        if (isStop) {
            return;
        }
        lastAccessTime = new Date();
        reqTotalCount.incrementAndGet();
    }

}
