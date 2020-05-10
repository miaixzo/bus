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
package org.aoju.bus.office.bridge;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.magic.UnoUrl;
import org.aoju.bus.office.metric.*;
import org.aoju.bus.office.process.AbstractProcessManager;
import org.aoju.bus.office.process.ProcessManager;

import java.io.File;
import java.util.Arrays;

/**
 * 默认的{@link OfficeManager}实现，它使用一个office进程池来执行转换任务.
 *
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
public final class LocalOfficePoolManager extends AbstractOfficePoolManager {

    private final UnoUrl[] unoUrls;

    private LocalOfficePoolManager(
            final UnoUrl[] unoUrls, final OfficeProcessManagerPoolBuilder config) {
        super(unoUrls.length, config);
        this.unoUrls = Arrays.copyOf(unoUrls, unoUrls.length);
    }

    /**
     * 创建一个新的生成器实例.
     *
     * @return 新的生成器实例.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 使用默认配置创建一个新的{@link LocalOfficePoolManager}.
     *
     * @return 带有默认配置的 {@link LocalOfficeBridgeFactory}.
     */
    public static LocalOfficePoolManager make() {
        return builder().build();
    }

    /**
     * 使用默认配置创建一个新的{@link LocalOfficePoolManager}.
     * 创建的管理器将是{@link InstalledOfficeHolder} 类的唯一实例.
     * 如果该类已经持有 {@link OfficeManager} 实例， 则现有管理器的所有者负责停止它.
     *
     * @return 带有默认配置的 {@link LocalOfficeBridgeFactory}.
     */
    public static LocalOfficePoolManager install() {
        return builder().install().build();
    }

    @Override
    protected OfficeProcessEntryManager[] createPoolEntries() {

        return Arrays.stream(unoUrls)
                .map(officeUrl -> new OfficeProcessEntryManager(
                        officeUrl, (OfficeProcessManagerPoolBuilder) config))
                .toArray(OfficeProcessEntryManager[]::new);
    }

    /**
     * 用于构造{@link LocalOfficePoolManager}的生成器
     *
     * @see LocalOfficePoolManager
     */
    public static final class Builder extends AbstractOfficeManagerPoolBuilder<Builder> {

        // OfficeProcess
        private String[] pipeNames;
        private int[] portNumbers;
        private File officeHome;
        private ProcessManager processManager;
        private String[] runAsArgs;
        private File templateProfileDir;
        private boolean useDefaultOnInvalidTemplateProfileDir;
        private boolean killExistingProcess = org.aoju.bus.office.Builder.DEFAULT_KILL_EXISTING_PROCESS;

        // OfficeProcessManager
        private long processTimeout = org.aoju.bus.office.Builder.DEFAULT_PROCESS_TIMEOUT;
        private long processRetryInterval = org.aoju.bus.office.Builder.DEFAULT_PROCESS_RETRY_INTERVAL;
        private int maxTasksPerProcess = org.aoju.bus.office.Builder.DEFAULT_MAX_TASKS_PER_PROCESS;
        private boolean disableOpengl = org.aoju.bus.office.Builder.DEFAULT_DISABLE_OPENGL;

        private Builder() {
            super();
        }

        @Override
        public LocalOfficePoolManager build() {
            // 为尚未设置的属性分配默认值.
            if (ObjectUtils.isEmpty(officeHome)) {
                officeHome = org.aoju.bus.office.Builder.getDefaultOfficeHome();
            }

            if (ObjectUtils.isEmpty(workingDir)) {
                workingDir = new File(System.getProperty("java.io.tmpdir"));
            }

            if (ObjectUtils.isEmpty(processManager)) {
                processManager = org.aoju.bus.office.Builder.findBestProcessManager();
            }

            // 验证office目录
            org.aoju.bus.office.Builder.validateOfficeHome(officeHome);
            org.aoju.bus.office.Builder.validateOfficeWorkingDirectory(workingDir);
            if (useDefaultOnInvalidTemplateProfileDir) {
                try {
                    org.aoju.bus.office.Builder.validateOfficeTemplateProfileDirectory(templateProfileDir);
                } catch (IllegalStateException ex) {
                    templateProfileDir = null;
                    Logger.warn("Falling back to default templateProfileDir. Cause: ", ex.getMessage());
                }
            } else {
                org.aoju.bus.office.Builder.validateOfficeTemplateProfileDirectory(templateProfileDir);
            }

            final UnoUrl[] unoUrls = org.aoju.bus.office.Builder.buildOfficeUrls(portNumbers, pipeNames);

            final OfficeProcessManagerPoolBuilder config =
                    new OfficeProcessManagerPoolBuilder(officeHome, workingDir, processManager);
            config.setRunAsArgs(runAsArgs);
            config.setTemplateProfileDir(templateProfileDir);
            config.setKillExistingProcess(killExistingProcess);
            config.setProcessTimeout(processTimeout);
            config.setProcessRetryInterval(processRetryInterval);
            config.setMaxTasksPerProcess(maxTasksPerProcess);
            config.setDisableOpengl(disableOpengl);
            config.setTaskExecutionTimeout(taskExecutionTimeout);
            config.setTaskQueueTimeout(taskQueueTimeout);

            final LocalOfficePoolManager manager = new LocalOfficePoolManager(unoUrls, config);
            if (install) {
                InstalledOfficeHolder.setInstance(manager);
            }
            return manager;
        }

        /**
         * 指定将用于与office通信的管道名称。将为每个管道名称启动一个office实例.
         *
         * @param pipeNames 要使用的管道名称.
         * @return 构造器实例.
         */
        public Builder pipeNames(final String... pipeNames) {
            Assert.isTrue(pipeNames != null && pipeNames.length > 0, "The pipe name list must not be empty");
            this.pipeNames = ArrayUtils.clone(pipeNames);
            return this;
        }

        /**
         * 指定将用于与office通信的端口号。将为每个端口号启动office实例.
         *
         * @param portNumbers 要使用的端口号.
         * @return 构造器实例.
         */
        public Builder portNumbers(final int... portNumbers) {
            Assert.isTrue(portNumbers != null && portNumbers.length > 0, "The port number list must not be empty");
            this.portNumbers = ArrayUtils.clone(portNumbers);
            return this;
        }

        /**
         * 指定office主目录(office安装).
         *
         * @param officeHome 要设置的新主目录.
         * @return 构造器实例.
         */
        public Builder officeHome(final File officeHome) {
            this.officeHome = officeHome;
            return this;
        }

        /**
         * 指定office主目录(office安装).
         *
         * @param officeHome 要设置的新主目录.
         * @return 构造器实例.
         */
        public Builder officeHome(final String officeHome) {
            return StringUtils.isBlank(officeHome) ? this : officeHome(new File(officeHome));
        }

        /**
         * 提供特定的{@link ProcessManager}实现，用于处理office进程(检索PID、终止进程).
         *
         * @param processManager 所提供的流程管理器.
         * @return 构造器实例.
         */
        public Builder processManager(final ProcessManager processManager) {
            Assert.notNull(processManager, "The process manager must not be null");
            this.processManager = processManager;
            return this;
        }

        /**
         * 提供自定义{@link ProcessManager}实现，该实现可能不包括在标准JODConverter发行版中.
         *
         * @param processManagerClass 提供的流程管理器的类型。类必须实现{@code ProcessManager}接口，
         *                            必须在类路径上(或者更具体地说，可以从当前类加载器访问)，
         *                            并且必须有一个默认的公共构造函数.
         * @return 构造器实例.
         * @see ProcessManager
         * @see AbstractProcessManager
         */
        public Builder processManager(final String processManagerClass) {
            try {
                return StringUtils.isBlank(processManagerClass)
                        ? this
                        : processManager((ProcessManager) Class.forName(processManagerClass).newInstance());
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                throw new IllegalArgumentException(
                        "Unable to create a Process manager from the specified class name: "
                                + processManagerClass,
                        ex);
            }
        }

        /**
         * 指定将与unix命令一起使用的sudo参数.
         *
         * @param runAsArgs unix操作系统的sudo参数.
         * @return 构造器实例.
         */
        public Builder runAsArgs(final String... runAsArgs) {
            Assert.isTrue(runAsArgs != null && runAsArgs.length > 0, "The runAs argument list must not be empty");
            this.runAsArgs = ArrayUtils.clone(runAsArgs);
            return this;
        }

        /**
         * 指定要复制到要创建的临时office配置文件目录的目录.
         *
         * @param templateProfileDir 新的模板配置文件目录.
         * @return 构造器实例.
         */
        public Builder templateProfileDir(final File templateProfileDir) {
            this.templateProfileDir = templateProfileDir;
            return this;
        }

        /**
         * 指定要复制到要创建的临时office配置文件目录的目录.
         *
         * @param templateProfileDir 新的模板配置文件目录.
         * @return 构造器实例.
         */
        public Builder templateProfileDir(final String templateProfileDir) {
            return StringUtils.isBlank(templateProfileDir)
                    ? this
                    : templateProfileDir(new File(templateProfileDir));
        }

        /**
         * 指定要复制到要创建的临时office配置文件目录的目录。如果给定的templateProfileDir无效，
         * 则将忽略它并应用默认行为.
         *
         * @param templateProfileDir 新的模板配置文件目录.
         * @return 构造器实例.
         */
        public Builder templateProfileDirOrDefault(final File templateProfileDir) {
            this.useDefaultOnInvalidTemplateProfileDir = true;
            this.templateProfileDir = templateProfileDir;
            return this;
        }

        /**
         * 指定要复制到要创建的临时office配置文件目录的目录。如果给定的templateProfileDir无效，
         * 则将忽略它并应用默认行为.
         *
         * @param templateProfileDir 新的模板配置文件目录.
         * @return 构造器实例.
         */
        public Builder templateProfileDirOrDefault(final String templateProfileDir) {
            return StringUtils.isBlank(templateProfileDir)
                    ? this
                    : templateProfileDirOrDefault(new File(templateProfileDir));
        }

        /**
         * 指定在为相同连接字符串启动新office进程时是否终止现有的office进程.
         * 默认: true
         *
         * @param killExistingProcess 当必须使用相同的连接字符串创建新进程时，
         *                            {@code true}将终止现有进程，否则为{@code false}.
         * @return 构造器实例.
         */
        public Builder killExistingProcess(final boolean killExistingProcess) {
            this.killExistingProcess = killExistingProcess;
            return this;
        }

        /**
         * 指定尝试执行office流程调用(启动/终止)时的超时时间，以毫秒为单位.
         * <p>
         * 默认: 120000毫秒 (2分钟)
         *
         * @param processTimeout 进程超时，以毫秒为单位.
         * @return 构造器实例.
         */
        public Builder processTimeout(final long processTimeout) {
            this.processTimeout = processTimeout;
            return this;
        }

        /**
         * 指定尝试执行office流程调用(开始/终止)时每次尝试之间的延迟(以毫秒为单位).
         * 默认: 250毫秒 (0.25 秒)
         *
         * @param processRetryInterval 重试间隔，以毫秒为单位.
         * @return 构造器实例.
         */
        public Builder processRetryInterval(final long processRetryInterval) {
            this.processRetryInterval = processRetryInterval;
            return this;
        }

        /**
         * 指定office流程在重新启动之前可以执行的最大任务数.
         * <p>
         * 默认: 200
         *
         * @param maxTasksPerProcess office流程可以执行的新最大任务数.
         * @return 构造器实例.
         */
        public Builder maxTasksPerProcess(final int maxTasksPerProcess) {
            this.maxTasksPerProcess = maxTasksPerProcess;
            return this;
        }

        /**
         * 指定在启动新office进程时是否必须禁用OpenGL.
         * 如果OpenGL已经根据office进程使用的用户配置文件被禁用，
         * 则不会执行任何操作。如果更改了选项，则必须重新启动office.
         * 默认: false
         *
         * @param disableOpengl {@code true}禁用OpenGL， {@code false}禁用OpenGL.
         * @return 构造器实例.
         */
        public Builder disableOpengl(final boolean disableOpengl) {
            this.disableOpengl = disableOpengl;
            return this;
        }

    }

}
