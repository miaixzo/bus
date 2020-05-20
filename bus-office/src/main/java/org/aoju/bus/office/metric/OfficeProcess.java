/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
package org.aoju.bus.office.metric;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.health.Platform;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.Expense;
import org.aoju.bus.office.magic.UnoUrl;
import org.aoju.bus.office.process.LinesPumpStreamHandler;
import org.aoju.bus.office.process.ProcessManager;
import org.aoju.bus.office.process.ProcessQuery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 表示正在执行的office程序的实例.
 *
 * @author Kimi Liu
 * @version 5.9.2
 * @since JDK 1.8+
 */
public class OfficeProcess {

    private final UnoUrl unoUrl;
    private final OfficeProcessBuilder config;
    private final File instanceProfileDir;
    private Expense process;
    private OfficeOption descriptor;
    private long pid = Builder.PID_UNKNOWN;

    /**
     * 使用默认配置为指定的URL构造office流程类的新实例.
     *
     * @param unoUrl 为其创建流程的URL.
     */
    public OfficeProcess(final UnoUrl unoUrl) {
        this(unoUrl, new OfficeProcessBuilder());
    }

    /**
     * 使用指定的配置构造office流程类的新实例.
     *
     * @param unoUrl 为其创建流程的URL.
     * @param config 流程配置.
     */
    public OfficeProcess(final UnoUrl unoUrl, final OfficeProcessBuilder config) {

        this.unoUrl = unoUrl;
        this.config = config;
        this.instanceProfileDir = getInstanceProfileDir();
    }

    /**
     * 检查是否已经有一个使用我们想要使用的连接字符串运行的office进程
     * 如果kill开关打开，进程将被终止.
     *
     * @param processQuery 要使用的连接字符串的查询
     * @throws InstrumentException 如果验证失败.
     */
    private void checkForExistingProcess(final ProcessQuery processQuery) throws InstrumentException {
        try {
            // 搜索现有进程，该进程将阻止我们使用相同的连接字符串启动新的office进程.
            final ProcessManager processManager = config.getProcessManager();
            long existingPid = processManager.find(processQuery);

            // 如果Kill开关打开，则使用相同的连接字符串终止任何正在运行的office进程
            if (!(existingPid == Builder.PID_NOT_FOUND || existingPid == Builder.PID_UNKNOWN)
                    && config.isKillExistingProcess()) {
                Logger.warn("A process with acceptString '{}' is already running; pid {}",
                        processQuery.getArgument(),
                        existingPid);
                processManager.kill(null, existingPid);
                waitForProcessToDie();
                existingPid = processManager.find(processQuery);
            }

            if (existingPid != Builder.PID_NOT_FOUND && existingPid != Builder.PID_UNKNOWN) {
                throw new InstrumentException(
                        String.format("A process with acceptString '%s' is already running; pid %d",
                                processQuery.getArgument(), existingPid));
            }

        } catch (IOException ioEx) {
            throw new InstrumentException(
                    String.format("Unable to check if there is already an existing process with acceptString '%s'",
                            processQuery.getArgument()),
                    ioEx);
        }
    }

    /**
     * 删除office进程的配置文件目录
     */
    public void deleteInstanceProfileDir() {
        Logger.debug("Deleting instance profile directory '{}'", instanceProfileDir);
        try {
            FileUtils.delete(instanceProfileDir);
        } catch (InstrumentException ioEx) {
            final File oldProfileDir =
                    new File(
                            instanceProfileDir.getParentFile(),
                            instanceProfileDir.getName() + ".old." + System.currentTimeMillis());
            if (instanceProfileDir.renameTo(oldProfileDir)) {
                Logger.warn(
                        "Could not delete profileDir: {}; renamed it to {}", ioEx.getMessage(), oldProfileDir);
            } else {
                Logger.error("Could not delete profileDir: {}", ioEx.getMessage());
            }
        }
    }

    /**
     * 终止office流程实例.
     *
     * @param retryInterval 每次退出码检索尝试之间的间隔.
     * @param retryTimeout  超时之后，我们将不再尝试检索退出码.
     * @return pid信息
     * @throws InstrumentException 如果我们无法获得进程的退出码.
     */
    public int forciblyTerminate(final long retryInterval, final long retryTimeout)
            throws InstrumentException {
        if (ObjectUtils.isEmpty(process)) {
            return 0;
        }

        Logger.info(
                "Trying to forcibly terminate process: '{}'; pid: {}",
                unoUrl.getConnectionParametersAsString(),
                pid == Builder.PID_UNKNOWN ? "NA" : pid);

        try {
            config.getProcessManager().kill(process.getProcess(), pid);
            return getExitCode(retryInterval, retryTimeout);
        } catch (IOException ioEx) {
            throw new InstrumentException("Unable to kill the process with pid: " + pid, ioEx);
        }
    }

    /**
     * 获取office进程的退出码
     *
     * @return 进程的退出值。值0表示正常终止。如果进程尚未终止，则返回{@code null}
     */
    public Integer getExitCode() {
        if (ObjectUtils.isEmpty(process)) {
            return 0;
        }
        return process.getExitCode();
    }

    /**
     * 获取office进程的退出码。我们将尝试获取退出代码，直到成功或达到指定的超时为止
     *
     * @param retryInterval 每次退出码检索尝试之间的间隔
     * @param retryTimeout  超时之后，我们将不再尝试检索退出码
     * @return 进程的退出值。值0表示正常终止
     * @throws InstrumentException 如果无法获得进程的退出码
     */
    public int getExitCode(final long retryInterval, final long retryTimeout)
            throws InstrumentException {
        if (ObjectUtils.isEmpty(process)) {
            return 0;
        }
        try {
            final ExitCodeRetryable retryable = new ExitCodeRetryable(process);
            retryable.execute(retryInterval, retryTimeout);
            return retryable.getExitCode();
        } catch (Exception ex) {
            throw new InstrumentException("Could not get the process exit code", ex);
        }
    }

    /**
     * 获取office进程的配置文件目录
     *
     * @return 概要目录实例
     */
    private File getInstanceProfileDir() {
        return new File(
                config.getWorkingDir(),
                ".converter_"
                        + unoUrl.getConnectionAndParametersAsString().replace(Symbol.C_COMMA, Symbol.C_UNDERLINE).replace(Symbol.C_EQUAL, Symbol.C_HYPHEN));
    }

    /**
     * 获取office进程是否正在运行
     *
     * @return {@code true} 是正在运行的office进程;{@code false 否则}
     */
    public boolean isRunning() {
        return process != null && getExitCode() == null;
    }

    /**
     * 准备office流程的概要目录
     *
     * @throws InstrumentException 如果模板配置文件目录不能复制到新的实例配置文件目录
     */
    private void prepareInstanceProfileDir() throws InstrumentException {
        if (instanceProfileDir.exists()) {
            Logger.warn("Profile dir '{}' already exists; deleting", instanceProfileDir);
            deleteInstanceProfileDir();
        }
        if (config.getTemplateProfileDir() != null) {
            try {
                FileUtils.copyFile(config.getTemplateProfileDir(), instanceProfileDir);
            } catch (InstrumentException ioEx) {
                throw new InstrumentException("Failed to create the instance profile directory", ioEx);
            }
        }
    }

    /**
     * 准备用于启动office流程的ProcessBuilder
     *
     * @param acceptString office进程的连接字符串(accept参数)
     * @return 创建的ProcessBuilder
     */
    private ProcessBuilder prepareProcessBuilder(final String acceptString) {
        final List<String> command = new ArrayList<>();
        final File executable = Builder.getOfficeExecutable(config.getOfficeHome());
        if (config.getRunAsArgs() != null) {
            command.addAll(Arrays.asList(config.getRunAsArgs()));
        }

        final String execPath = executable.getAbsolutePath();
        final String prefix = descriptor.useLongOptionNameGnuStyle() ? Symbol.HYPHEN + Symbol.HYPHEN : Symbol.HYPHEN;
        command.add(execPath);
        command.add(prefix + "accept=" + acceptString);
        command.add(prefix + "headless");
        command.add(prefix + "invisible");
        command.add(prefix + "nocrashreport");
        command.add(prefix + "nodefault");
        command.add(prefix + "nofirststartwizard");
        command.add(prefix + "nolockcheck");
        command.add(prefix + "nologo");
        command.add(prefix + "norestore");
        command.add("-env:UserInstallation=" + Builder.toUrl(instanceProfileDir));
        return new ProcessBuilder(command);
    }

    /**
     * 启动office流程
     *
     * @throws InstrumentException 如果无法启动office进程
     */
    public void start() throws InstrumentException {
        start(false);
    }

    /**
     * 启动office流程
     *
     * @param restart 判断是否需要重新启动,重新启动将假定实例配置文件目录已经创建
     *                如果要重新创建实例配置文件目录,应该将{@code restart}设置为{@code false}
     * @throws InstrumentException 如果无法启动office进程
     */
    public void start(final boolean restart) throws InstrumentException {
        final String acceptString =
                unoUrl.getConnectionAndParametersAsString()
                        + Symbol.SEMICOLON
                        + unoUrl.getProtocolAndParametersAsString()
                        + Symbol.SEMICOLON
                        + unoUrl.getRootOid();

        // 搜索现有进程.
        final ProcessQuery processQuery = new ProcessQuery("soffice", acceptString);
        checkForExistingProcess(processQuery);

        // 仅在第一次启动时准备实例目录
        if (!restart) {
            prepareInstanceProfileDir();
        }

        // 限定office版本
        detectOfficeVersion();

        // 创建用于启动office流程的构建器
        final ProcessBuilder processBuilder = prepareProcessBuilder(acceptString);

        // 启动过程.
        Logger.info("Starting process with acceptString '{}' and profileDir '{}'",
                acceptString,
                instanceProfileDir);
        try {
            process = new Expense(processBuilder.start());
            pid = config.getProcessManager().find(processQuery);
            Logger.info("Started process{}", pid == Builder.PID_UNKNOWN ? Normal.EMPTY : "; pid = " + pid);
        } catch (IOException ioEx) {
            throw new InstrumentException(
                    String.format("An I/O error prevents us to start a process with acceptString '%s'", acceptString),
                    ioEx);
        }

        if (pid == Builder.PID_NOT_FOUND) {
            throw new InstrumentException(
                    String.format("A process with acceptString '%s' started but its pid could not be found",
                            acceptString));
        }
    }

    private void waitForProcessToDie() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void detectOfficeVersion() {
        final List<String> command = new ArrayList<>();
        final File executable = Builder.getOfficeExecutable(config.getOfficeHome());
        if (config.getRunAsArgs() != null) {
            command.addAll(Arrays.asList(config.getRunAsArgs()));
        }

        final String execPath = executable.getAbsolutePath();

        descriptor = OfficeOption.fromExecutablePath(execPath);

        if (Platform.isWindows()) {
            return;
        }

        final String prefix = descriptor.useLongOptionNameGnuStyle() ? Symbol.HYPHEN + Symbol.HYPHEN : Symbol.HYPHEN;

        command.add(execPath);
        command.add(prefix + "invisible");
        command.add(prefix + "help");
        command.add(prefix + "headless");
        command.add(prefix + "nocrashreport");
        command.add(prefix + "nodefault");
        command.add(prefix + "nofirststartwizard");
        command.add(prefix + "nolockcheck");
        command.add(prefix + "nologo");
        command.add(prefix + "norestore");
        command.add("-env:UserInstallation=" + Builder.toUrl(instanceProfileDir));
        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            final Process process = processBuilder.start();
            final LinesPumpStreamHandler handler =
                    new LinesPumpStreamHandler(process.getInputStream(), process.getErrorStream());
            handler.start();
            process.waitFor();
            handler.stop();
            descriptor = OfficeOption.fromHelpOutput(handler.getOutputPumper().getLines());
        } catch (IOException | InterruptedException ioEx) {
            Logger.warn("An I/O error prevents us to determine office version", ioEx);
        }
    }

}
