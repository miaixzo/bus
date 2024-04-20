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
package org.miaixz.bus.core.lang;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 任务类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Job implements Runnable {

    private final Object target;
    private final Method method;
    private final AtomicLong runCount = new AtomicLong();
    private TYPE type;
    private Long fixedDelay;
    private Long fixedRate;
    private Long initialDelay;
    private String cron;
    private Boolean async;
    private Instant startAt;

    public Job(Object target, Method method) {
        this.target = target;
        this.method = method;
    }

    public Job configFixedDelay(long fixedDelay, long initialDelay) {
        this.type = TYPE.FIXED_DELAY;
        this.fixedDelay = fixedDelay;
        this.initialDelay = initialDelay;
        return this;
    }

    public Job configFixedRate(long fixedRate, long initialDelay) {
        this.type = TYPE.FIXED_RATE;
        this.fixedRate = fixedRate;
        this.initialDelay = initialDelay;
        return this;
    }

    public Job configCron(String cron) {
        this.type = TYPE.CRON;
        this.cron = cron;
        return this;
    }

    public Job configOnApplicationStart(boolean async) {
        Job job = this;
        if (null != this.type) {
            job = new Job(target, method);
        }
        job.type = TYPE.ON_APPLICATION_START;
        job.async = async;
        return job;
    }

    @Override
    public void run() {
        try {
            startAt = Instant.now();
            method.invoke(target);
        } catch (Exception e) {
            onException(e);
        } finally {
            onFinally();
        }
    }

    private void onException(Exception e) {
        Console.log("job [{}] execute error", this, e);
    }

    private void onFinally() {
        long count = runCount.incrementAndGet();
        Console.log("job [{}] elapsed time [{}], current rounds [{}]", this,
                Duration.between(startAt, Instant.now()).toMillis(), count);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(target.getClass().getName()).append(Symbol.SHAPE)
                .append(method.getName()).append(Symbol.OR).append(type);
        if (type == TYPE.CRON) {
            sb.append(Symbol.OR).append(cron);
        } else if (type == TYPE.ON_APPLICATION_START) {
            sb.append(Symbol.C_OR).append(async);
        } else {
            if (type == TYPE.FIXED_DELAY) {
                sb.append(Symbol.OR).append(fixedDelay);
            } else if (type == TYPE.FIXED_RATE) {
                sb.append(Symbol.OR).append(fixedRate);
            }
            sb.append(Symbol.C_COMMA).append(initialDelay);
        }
        return sb.toString();
    }

    /**
     * 任务类型
     */
    public enum TYPE {
        /**
         * fixed delay job
         */
        FIXED_DELAY,
        /**
         * fixed rate job
         */
        FIXED_RATE,
        /**
         * cron job
         */
        CRON,
        /**
         * when application start
         */
        ON_APPLICATION_START
    }

}