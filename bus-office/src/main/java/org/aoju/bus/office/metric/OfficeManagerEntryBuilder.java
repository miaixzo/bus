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
package org.aoju.bus.office.metric;

/**
 * 这个接口提供了{@link OfficeProcessEntryManager}的配置
 *
 * @author Kimi Liu
 * @version 5.5.8
 * @since JDK 1.8+
 */
public interface OfficeManagerEntryBuilder {

    /**
     * 获取允许处理任务的最大时间。如果任务的处理时间长于此超时，则此任务将中止并处理下一个任务.
     * 默认:2分钟
     *
     * @return 任务执行超时，以毫秒为单位.
     */
    long getTaskExecutionTimeout();

    /**
     * 设置允许处理任务的最大时间。如果任务的处理时间长于此超时，则此任务将中止并处理下一个任务.
     *
     * @param taskExecutionTimeout 新的任务执行超时.
     */
    void setTaskExecutionTimeout(final long taskExecutionTimeout);

}
