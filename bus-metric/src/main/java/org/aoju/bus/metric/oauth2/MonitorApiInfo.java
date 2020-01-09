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
package org.aoju.bus.metric.oauth2;

import org.aoju.bus.metric.Context;
import org.aoju.bus.metric.builtin.LimitQueue;
import org.aoju.bus.metric.builtin.Pagable;

import java.io.Serializable;
import java.util.Queue;

/**
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8++
 */
public class MonitorApiInfo implements Pagable, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接口名
     */
    private String name;
    /**
     * 版本号
     */
    private String version;
    /**
     * 访问次数
     */
    private long visitCount;
    /**
     * 出错次数
     */
    private int errorCount;
    /**
     * 平均访问耗时
     */
    private double avgConsumeMilliseconds;
    /**
     * 总耗时
     */
    private long sumConsumeMilliseconds;
    /**
     * 最大耗时
     */
    private long maxConsumeMilliseconds;
    /**
     * 出错信息
     */
    private Queue<String> errors = new LimitQueue<>(Context.getConfig().getMonitorErrorQueueSize());

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(long visitCount) {
        this.visitCount = visitCount;
    }

    public double getAvgConsumeMilliseconds() {
        return avgConsumeMilliseconds;
    }

    public void setAvgConsumeMilliseconds(double avgConsumeMilliseconds) {
        this.avgConsumeMilliseconds = avgConsumeMilliseconds;
    }

    public long getSumConsumeMilliseconds() {
        return sumConsumeMilliseconds;
    }

    public void setSumConsumeMilliseconds(long sumConsumeMilliseconds) {
        this.sumConsumeMilliseconds = sumConsumeMilliseconds;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public long getMaxConsumeMilliseconds() {
        return maxConsumeMilliseconds;
    }

    public void setMaxConsumeMilliseconds(long maxConsumeMilliseconds) {
        this.maxConsumeMilliseconds = maxConsumeMilliseconds;
    }

    public Queue<String> getErrors() {
        return errors;
    }

    public void setErrors(Queue<String> errors) {
        this.errors = errors;
    }

}
