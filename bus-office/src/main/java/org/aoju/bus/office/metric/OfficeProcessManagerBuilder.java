/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
 * 这个类包含{@link OfficeProcessManager}的配置.
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public class OfficeProcessManagerBuilder extends OfficeProcessBuilder {

    /**
     * 进程超时，以毫秒为单位.
     */
    private long processTimeout = Builder.DEFAULT_PROCESS_TIMEOUT;
    /**
     * 新进程重试间隔(以毫秒为单位)
     */
    private long processRetryInterval = Builder.DEFAULT_PROCESS_RETRY_INTERVAL;

    /**
     * Office流程可以执行的新任务数
     */
    private int maxTasksPerProcess = Builder.DEFAULT_MAX_TASKS_PER_PROCESS;

    /**
     * 禁用OpenGL, {@code false} 禁用OpenGL.
     */
    private boolean disableOpengl = Builder.DEFAULT_DISABLE_OPENGL;

    /**
     * 使用默认值创建配置.
     */
    public OfficeProcessManagerBuilder() {
        super();
    }

    /**
     * 使用指定的值创建配置.
     *
     * @param officeHome     Office安装的主目录.
     * @param workingDir     要设置为office的工作目录.
     * @param processManager 用于处理创建的流程的流程管理器.
     */
    public OfficeProcessManagerBuilder(
            final File officeHome, final File workingDir, final ProcessManager processManager) {
        super(officeHome, workingDir, processManager);
    }

    /**
     * 获取尝试执行office流程调用(启动/终止)的超时时间(以毫秒为单位).
     *
     * @return 进程超时，以毫秒为单位.
     */
    public long getProcessTimeout() {
        return processTimeout;
    }

    /**
     * 获取尝试执行office流程调用(启动/停止)时的超时时间(以毫秒为单位)
     *
     * @param processTimeout 新的进程超时，以毫秒为单位.
     */
    public void setProcessTimeout(final long processTimeout) {
        this.processTimeout = processTimeout;
    }

    /**
     * 获取尝试执行office流程调用(启动/终止)时每次尝试之间的延迟(以毫秒为单位).
     *
     * @return 重试间隔，以毫秒为单位.
     */
    public long getProcessRetryInterval() {
        return processRetryInterval;
    }

    /**
     * 获取尝试执行office流程调用(开始/终止)时，设置每次尝试之间的延迟(以毫秒为单位).
     *
     * @param processRetryInterval 新进程重试间隔(以毫秒为单位).
     */
    public void setProcessRetryInterval(final long processRetryInterval) {
        this.processRetryInterval = processRetryInterval;
    }

    /**
     * 获取尝试执行Office重新启动之前可以执行的最大任务数.
     *
     * @return 一个Office流程可以执行的任务数量.
     */
    public int getMaxTasksPerProcess() {
        return maxTasksPerProcess;
    }

    /**
     * 设置office流程在重新启动之前可以执行的最大任务数.
     *
     * @param maxTasksPerProcess Office流程可以执行的新任务数
     */
    public void setMaxTasksPerProcess(final int maxTasksPerProcess) {
        this.maxTasksPerProcess = maxTasksPerProcess;
    }

    /**
     * 获取启动新office进程时是否必须禁用OpenGL
     *
     * @return {@code true}禁用OpenGL, {@code false}禁用OpenGL
     */
    public boolean isDisableOpengl() {
        return disableOpengl;
    }

    /**
     * 获取启动新office进程时是否必须禁用OpenGL
     * 如果OpenGL已经根据office进程使用的用户配置文件被禁用，
     * 则不会执行任何操作。如果选项更改，则必须重新启动office
     *
     * @param disableOpengl {@code true}禁用OpenGL, {@code false}禁用OpenGL
     */
    public void setDisableOpengl(final boolean disableOpengl) {
        this.disableOpengl = disableOpengl;
    }

}
