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

import org.aoju.bus.office.Builder;
import org.aoju.bus.office.process.ProcessManager;

import java.io.File;

/**
 * 当需要office实例来执行转换时，该类保存{@link OfficeProcessEntryManager}的配置
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public class OfficeProcessManagerEntryBuilder extends OfficeProcessManagerBuilder
        implements OfficeManagerEntryBuilder {

    private long taskExecutionTimeout = Builder.DEFAULT_TASK_EXECUTION_TIMEOUT;

    /**
     * 使用默认值创建配置.
     */
    public OfficeProcessManagerEntryBuilder() {
        super();
    }

    /**
     * 使用指定的值创建配置.
     *
     * @param officeHome     office安装的主目录.
     * @param workingDir     要设置为office的工作目录.
     * @param processManager 用于处理创建的流程的流程管理器.
     */
    public OfficeProcessManagerEntryBuilder(
            final File officeHome, final File workingDir, final ProcessManager processManager) {
        super(officeHome, workingDir, processManager);
    }

    @Override
    public long getTaskExecutionTimeout() {
        return taskExecutionTimeout;
    }

    @Override
    public void setTaskExecutionTimeout(final long taskExecutionTimeout) {
        this.taskExecutionTimeout = taskExecutionTimeout;
    }

}
