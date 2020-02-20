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

import com.sun.star.lang.DisposedException;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.thread.NamedThreadFactory;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.bridge.LocalOfficeBridgeFactory;
import org.aoju.bus.office.magic.UnoUrl;

import java.util.concurrent.*;

/**
 * OfficeProcessManager负责管理一个office流程以及到这个office流程的连接(桥接).
 *
 * @author Kimi Liu
 * @version 5.6.2
 * @since JDK 1.8+
 */
public class OfficeProcessManager {

    private final OfficeProcess process;
    private final LocalOfficeBridgeFactory localOffice;
    private final ExecutorService executor;
    private final OfficeProcessManagerBuilder config;

    /**
     * 创建具有指定配置的新管理器.
     *
     * @param unoUrl 为其创建管理器的URL.
     * @param config 管理器的配置.
     */
    public OfficeProcessManager(final UnoUrl unoUrl, final OfficeProcessManagerBuilder config) {
        this.config = config;
        process = new OfficeProcess(unoUrl, config);
        localOffice = new LocalOfficeBridgeFactory(unoUrl);
        executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("OfficeProcessThread"));
    }

    /**
     * 确保进程退出.
     *
     * @param deleteInstanceProfileDir 如果{@code true}，实例配置文件目录将被删除
     *                                 我们并不总是希望在重新启动时删除实例配置文件目录，
     * @throws InstrumentException 如果发生异常
     */
    private void doEnsureProcessExited(final boolean deleteInstanceProfileDir)
            throws InstrumentException {
        try {
            final int exitCode =
                    process.getExitCode(config.getProcessRetryInterval(), config.getProcessTimeout());
            Logger.info("process exited with code {}", exitCode);

        } catch (InstrumentException retryTimeoutEx) {

            Logger.debug("doEnsureProcessExited times out", retryTimeoutEx);
            doTerminateProcess();

        } finally {
            if (deleteInstanceProfileDir) {
                process.deleteInstanceProfileDir();
            }
        }
    }

    /**
     * 启动这个类管理的office进程并连接到该进程
     *
     * @param restart 指示是重新启动还是重新启动
     *                重新启动将假定实例配置文件目录已经创建. 要重新创建实例配置文件目录，
     *                应该将{@code restart}设置为{@code false}
     */
    private void doStartProcessAndConnect(final boolean restart) throws InstrumentException {
        process.start(restart);

        try {
            new ConnectRetryable(process, localOffice)
                    .execute(config.getProcessRetryInterval(), config.getProcessTimeout());

        } catch (Exception ex) {
            if (ex instanceof InstrumentException) {
                throw (InstrumentException) ex;
            }
            throw new InstrumentException("Could not establish connection", ex);
        }
    }

    /**
     * 停止由OfficeProcessManager管理的office进程
     *
     * @param deleteInstanceProfileDir 如果{@code true}，实例配置文件目录将被删除
     *                                 我们并不总是希望在重新启动时删除实例配置文件目录
     */
    private void doStopProcess(final boolean deleteInstanceProfileDir) throws InstrumentException {
        try {
            final boolean terminated = localOffice.getDesktop().terminate();
            // 再一次:尝试终止
            Logger.debug(
                    "The Office Process {}",
                    terminated
                            ? "has been terminated"
                            : "is still running. Someone else prevents termination, e.g. the quickstarter");

        } catch (DisposedException disposedEx) {
            // 预期的，所以忽略它
            Logger.debug("Expected DisposedException catched and ignored in doStopProcess", disposedEx);
        } catch (Exception ex) {
            Logger.debug("Exception catched in doStopProcess", ex);
            doTerminateProcess();

        } finally {
            doEnsureProcessExited(deleteInstanceProfileDir);
        }
    }

    /**
     * 确保进程退出
     *
     * @throws InstrumentException 如果发生异常
     */
    private void doTerminateProcess() throws InstrumentException {
        try {
            final int exitCode =
                    process.forciblyTerminate(config.getProcessRetryInterval(), config.getProcessTimeout());
            Logger.info("process forcibly terminated with code {}", exitCode);

        } catch (Exception ex) {
            throw new InstrumentException("Could not terminate process", ex);
        }
    }

    /**
     * 获取此管理器的连接
     *
     * @return 这个管理器的{@link LocalConnect}
     */
    LocalOfficeBridgeFactory getLocalOffice() {
        return localOffice;
    }

    /**
     * 新启动office进程，并等待连接到重新启动的进程
     *
     * @throws InstrumentException 如果我们不能重新启动office进程
     */
    public void restartAndWait() throws InstrumentException {
        submitAndWait(
                "Restart",
                () -> {
                    // 在重新启动时，不会删除实例配置文件目录，从而加快了office进程的启动。
                    doStopProcess(false);
                    doStartProcessAndConnect(true);

                    return null;
                });
    }

    /**
     * 当连接丢失时，重新启动office进程
     */
    public void restartDueToLostConnection() {
        Logger.info("Executing task 'Restart After Lost Connection'...");
        executor.execute(
                () -> {
                    try {
                        doEnsureProcessExited(true);
                        doStartProcessAndConnect(false);
                    } catch (InstrumentException officeEx) {
                        Logger.error("Could not restart process after connection lost.", officeEx);
                    }
                });
    }

    /**
     * 在执行任务时出现超时时，重新启动office进程
     */
    public void restartDueToTaskTimeout() {
        Logger.info("Executing task 'Restart After Timeout'...");
        executor.execute(
                () -> {
                    try {
                        doTerminateProcess();
                    } catch (InstrumentException officeException) {
                        Logger.error("Could not terminate process after task timeout.", officeException);
                    }
                });
    }

    /**
     * 启动一个office进程，并等待连接到正在运行的进程
     *
     * @throws InstrumentException 如果不能启动并连接到office进程
     */
    public void startAndWait() throws InstrumentException {
        // 将启动任务提交给执行程序并等待
        submitAndWait(
                "Start",
                () -> {
                    doStartProcessAndConnect(false);
                    return null;
                });
    }

    /**
     * 停止office进程并等待该进程停止
     *
     * @throws InstrumentException 如果我们不能停止office程序
     */
    public void stopAndWait() throws InstrumentException {
        // 将停止任务提交给执行程序并等待
        submitAndWait(
                "Stop",
                () -> {
                    doStopProcess(true);
                    return null;
                });
    }

    // 将指定的任务提交给执行程序并等待其完成
    private void submitAndWait(final String taskName, final Callable<Void> task)
            throws InstrumentException {

        Logger.info("Submitting task '{}' and waiting...", taskName);
        final Future<Void> future = executor.submit(task);

        // 等待重启任务完成
        try {
            future.get();
            Logger.debug("Task '{}' executed successfully", taskName);

        } catch (ExecutionException executionEx) {
            Logger.debug("ExecutionException catched in submitAndWait", executionEx);

            // 重新抛出原来的(原因)异常
            if (executionEx.getCause() instanceof InstrumentException) {
                throw (InstrumentException) executionEx.getCause();
            }
            throw new InstrumentException(
                    "Failed to execute task '" + taskName + Symbol.SINGLE_QUOTE, executionEx.getCause());

        } catch (InterruptedException interruptedEx) {
            Thread.currentThread().interrupt();
        }
    }

}
