/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.office.metric;

/**
 * 这个类提供了{@link AbstractOfficePoolManager}的配置
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since JDK 1.8+
 */
public interface OfficeManagerPoolBuilder extends OfficeManagerBuilder {

    /**
     * 获取转换队列中任务的最大生存时间。如果等待时间长于此超时，则任务将从队列中删除
     * 默认:30秒
     *
     * @return 任务队列超时，以毫秒为单位
     */
    long getTaskQueueTimeout();

    /**
     * 设置转换队列中任务的最大生存时间。如果等待时间长于此超时，则任务将从队列中删除
     *
     * @param taskQueueTimeout 任务队列超时，以毫秒为单位
     */
    void setTaskQueueTimeout(final long taskQueueTimeout);

}
