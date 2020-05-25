/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.office.bridge;

import org.aoju.bus.office.Builder;
import org.aoju.bus.office.metric.AbstractOfficePoolManager;
import org.aoju.bus.office.metric.OfficeManagerPoolBuilder;

import java.io.File;

/**
 * 当不需要任何office实例来执行转换时，
 * 该类提供{@link AbstractOfficePoolManager}的配置.
 *
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
public class OnlineOfficePoolBuilder extends OnlineOfficeEntryBuilder
        implements OfficeManagerPoolBuilder {

    private long taskQueueTimeout = Builder.DEFAULT_TASK_QUEUE_TIMEOUT;
    private File workingDir;

    /**
     * 使用指定的值创建配置.
     *
     * @param workingDir 要设置为office的工作目录.
     */
    public OnlineOfficePoolBuilder(final File workingDir) {
        super();

        this.workingDir = workingDir;
    }

    @Override
    public long getTaskQueueTimeout() {
        return taskQueueTimeout;
    }

    @Override
    public void setTaskQueueTimeout(final long taskQueueTimeout) {
        this.taskQueueTimeout = taskQueueTimeout;
    }

    @Override
    public File getWorkingDir() {
        return workingDir;
    }

    @Override
    public void setWorkingDir(final File workingDir) {
        this.workingDir = workingDir;
    }

}
