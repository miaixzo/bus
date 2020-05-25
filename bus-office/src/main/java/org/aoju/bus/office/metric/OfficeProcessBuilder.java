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
package org.aoju.bus.office.metric;

import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.process.ProcessManager;

import java.io.File;

/**
 * 这个类包含{@link OfficeProcess}的配置.
 *
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
public class OfficeProcessBuilder {

    private File officeHome;
    private File workingDir;
    private ProcessManager processManager;
    private String[] runAsArgs;
    private File templateProfileDir;
    private boolean killExistingProcess = Builder.DEFAULT_KILL_EXISTING_PROCESS;

    /**
     * 使用默认值创建配置.
     */
    public OfficeProcessBuilder() {
        this(null, null, null);
    }

    /**
     * 使用指定的值创建配置.
     *
     * @param officeHome     Office安装的主目录.
     * @param workingDir     要设置为office的工作目录.
     * @param processManager 用于处理创建的流程的流程管理器.
     */
    public OfficeProcessBuilder(
            final File officeHome, final File workingDir, final ProcessManager processManager) {
        this.officeHome = officeHome == null ? Builder.getDefaultOfficeHome() : officeHome;
        this.workingDir =
                workingDir == null ? new File(System.getProperty("java.io.tmpdir")) : workingDir;
        this.processManager =
                processManager == null ? Builder.findBestProcessManager() : processManager;
    }

    /**
     * 获取office主目录(office安装目录).
     *
     * @return office主目录的文件实例.
     */
    public File getOfficeHome() {
        return officeHome;
    }

    /**
     * 设置office主目录
     *
     * @param officeHome 要设置的新主目录.
     */
    public void setOfficeHome(final File officeHome) {
        this.officeHome = officeHome;
    }

    /**
     * 获取处理office进程(检索PID、终止进程)时使用的{@link ProcessManager}实现.
     *
     * @return 所提供的流程管理器.
     */
    public ProcessManager getProcessManager() {
        return processManager;
    }

    /**
     * 将{@code ProcessManager}实现设置为与此office流程一起使用.
     *
     * @param processManager 要使用的流程管理器.
     */
    public void setProcessManager(final ProcessManager processManager) {
        this.processManager = processManager;
    }

    /**
     * 获取将与unix命令一起使用的sudo参数.
     *
     * @return sudo的参数.
     */
    public String[] getRunAsArgs() {
        return ArrayUtils.clone(runAsArgs);
    }

    /**
     * 设置将与unix命令一起使用的sudo参数
     *
     * @param runAsArgs unix操作系统的sudo参数
     */
    public void setRunAsArgs(final String... runAsArgs) {
        this.runAsArgs = ArrayUtils.clone(runAsArgs);
    }

    /**
     * 获取将在其中创建临时office配置文件目录的目录。每个启动的office进程都会创建一个office配置文件目录
     * 默认:  系统临时目录<code>java.io.tmpdir</code>系统属性确定
     *
     * @return 工作目录.
     */
    public File getWorkingDir() {
        return workingDir;
    }

    /**
     * 设置将在其中创建临时office配置文件目录的目录。每个启动的office进程都会创建一个office配置文件目录
     *
     * @param workingDir 工作目录
     */
    public void setWorkingDir(final File workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * 获取要复制到要创建的临时office配置文件目录的目录
     *
     * @return 模板配置文件目录
     */
    public File getTemplateProfileDir() {
        return templateProfileDir;
    }

    /**
     * 设置要复制到要创建的临时office配置文件目录的目录
     *
     * @param templateProfileDir 模板配置文件目录
     */
    public void setTemplateProfileDir(final File templateProfileDir) {
        this.templateProfileDir = templateProfileDir;
    }

    /**
     * 获取在为相同连接字符串启动新office进程时是否终止现有的office进程
     * 默认: true
     *
     * @return {@code true} 杀死现有进程，{@code false}否则忽略
     */
    public boolean isKillExistingProcess() {
        return killExistingProcess;
    }

    /**
     * 设置在为相同连接字符串启动新office进程时是否终止现有的office进程
     *
     * @param killExistingProcess {@code true} 杀死现有进程，{@code false}否则忽略
     */
    public void setKillExistingProcess(final boolean killExistingProcess) {
        this.killExistingProcess = killExistingProcess;
    }

}
