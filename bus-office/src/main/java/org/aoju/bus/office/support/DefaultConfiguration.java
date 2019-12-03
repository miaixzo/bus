/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.office.support;

import org.aoju.bus.office.Builder;
import org.aoju.bus.office.Manager;
import org.aoju.bus.office.magic.UnoUrl;
import org.aoju.bus.office.process.LinuxProcessManager;
import org.aoju.bus.office.process.PureJavaProcessManager;

import java.io.File;

/**
 * @author Kimi Liu
 * @version 5.3.1
 * @since JDK 1.8+
 */
public class DefaultConfiguration {

    public static final long DEFAULT_RETRY_TIMEOUT = 120000L;

    private File officeHome = Builder.getDefaultOfficeHome();
    private ConnectionProtocol connectionProtocol = ConnectionProtocol.SOCKET;
    private int[] portNumbers = new int[]{2002};
    private String[] pipeNames = new String[]{"office"};
    private String[] runAsArgs = null;
    private File templateProfileDir = null;
    private File workDir = new File(System.getProperty("java.io.tmpdir"));
    private long taskQueueTimeout = 30000L;
    private long taskExecutionTimeout = 120000L;
    private int maxTasksPerProcess = 200;
    private long retryTimeout = DEFAULT_RETRY_TIMEOUT;

    private org.aoju.bus.office.process.ProcessManager processManager = null;

    public DefaultConfiguration setOfficeHome(String officeHome) throws NullPointerException, IllegalArgumentException {
        checkArgumentNotNull("officeHome", officeHome);
        return setOfficeHome(new File(officeHome));
    }

    public DefaultConfiguration setOfficeHome(File officeHome) throws NullPointerException, IllegalArgumentException {
        checkArgumentNotNull("officeHome", officeHome);
        checkArgument("officeHome", officeHome.isDirectory(), "must exist and be a directory");
        this.officeHome = officeHome;
        return this;
    }

    public DefaultConfiguration setConnectionProtocol(ConnectionProtocol connectionProtocol) throws NullPointerException {
        checkArgumentNotNull("connectionProtocol", connectionProtocol);
        this.connectionProtocol = connectionProtocol;
        return this;
    }

    public DefaultConfiguration setPortNumber(int portNumber) {
        this.portNumbers = new int[]{portNumber};
        return this;
    }

    public DefaultConfiguration setPortNumbers(int... portNumbers) throws NullPointerException, IllegalArgumentException {
        checkArgumentNotNull("portNumbers", portNumbers);
        checkArgument("portNumbers", portNumbers.length > 0, "must not be empty");
        this.portNumbers = portNumbers;
        return this;
    }

    public DefaultConfiguration setPipeName(String pipeName) throws NullPointerException {
        checkArgumentNotNull("pipeName", pipeName);
        this.pipeNames = new String[]{pipeName};
        return this;
    }

    public DefaultConfiguration setPipeNames(String... pipeNames) throws NullPointerException, IllegalArgumentException {
        checkArgumentNotNull("pipeNames", pipeNames);
        checkArgument("pipeNames", pipeNames.length > 0, "must not be empty");
        this.pipeNames = pipeNames;
        return this;
    }

    public DefaultConfiguration setRunAsArgs(String... runAsArgs) {
        this.runAsArgs = runAsArgs;
        return this;
    }

    public DefaultConfiguration setTemplateProfileDir(File templateProfileDir) throws IllegalArgumentException {
        if (templateProfileDir != null) {
            checkArgument("templateProfileDir", templateProfileDir.isDirectory(), "must exist and be a directory");
        }
        this.templateProfileDir = templateProfileDir;
        return this;
    }

    /**
     * Sets the directory where temporary office profiles will be created.
     * <p>
     * Defaults to the system temporary directory as specified by the <code>java.io.tmpdir</code> system property.
     *
     * @param workDir dir
     * @return the config
     */
    public DefaultConfiguration setWorkDir(File workDir) {
        checkArgumentNotNull("workDir", workDir);
        this.workDir = workDir;
        return this;
    }

    public DefaultConfiguration setTaskQueueTimeout(long taskQueueTimeout) {
        this.taskQueueTimeout = taskQueueTimeout;
        return this;
    }

    public DefaultConfiguration setTaskExecutionTimeout(long taskExecutionTimeout) {
        this.taskExecutionTimeout = taskExecutionTimeout;
        return this;
    }

    public DefaultConfiguration setMaxTasksPerProcess(int maxTasksPerProcess) {
        this.maxTasksPerProcess = maxTasksPerProcess;
        return this;
    }

    /**
     * Provide a specific {@link org.aoju.bus.office.process.ProcessManager} implementation
     * <p>
     * The default is to use {@link LinuxProcessManager}
     * on Linux and {@link PureJavaProcessManager} on other platforms.
     *
     * @param processManager the {@link LinuxProcessManager}
     * @return the config
     * @throws NullPointerException exception
     */
    public DefaultConfiguration setProcessManager(org.aoju.bus.office.process.ProcessManager processManager) throws NullPointerException {
        checkArgumentNotNull("processManager", processManager);
        this.processManager = processManager;
        return this;
    }

    /**
     * Retry timeout set in milliseconds. Used for retrying office process calls.
     * If not set, it defaults to 2 minutes
     *
     * @param retryTimeout in milliseconds
     * @return the config
     */
    public DefaultConfiguration setRetryTimeout(long retryTimeout) {
        this.retryTimeout = retryTimeout;
        return this;
    }

    public Manager buildOfficeManager() throws IllegalStateException {
        if (officeHome == null) {
            throw new IllegalStateException("officeHome not set and could not be auto-detected");
        } else if (!officeHome.isDirectory()) {
            throw new IllegalStateException("officeHome doesn't exist or is not a directory: " + officeHome);
        } else if (!Builder.getOfficeExecutable(officeHome).isFile()) {
            throw new IllegalStateException("invalid officeHome: it doesn't contain soffice.bin: " + officeHome);
        }
        if (templateProfileDir != null && !isValidProfileDir(templateProfileDir)) {
            throw new IllegalStateException("templateProfileDir doesn't appear to contain a user profile: " + templateProfileDir);
        }
        if (!workDir.isDirectory()) {
            throw new IllegalStateException("workDir doesn't exist or is not a directory: " + workDir);
        }

        if (processManager == null) {
            processManager = findBestProcessManager();
        }

        int numInstances = connectionProtocol == ConnectionProtocol.PIPE ? pipeNames.length : portNumbers.length;
        UnoUrl[] unoUrls = new UnoUrl[numInstances];
        for (int i = 0; i < numInstances; i++) {
            unoUrls[i] = (connectionProtocol == ConnectionProtocol.PIPE) ? UnoUrl.pipe(pipeNames[i]) : UnoUrl.socket(portNumbers[i]);
        }
        return new ProcessManager(officeHome, unoUrls, runAsArgs, templateProfileDir, workDir, retryTimeout, taskQueueTimeout, taskExecutionTimeout, maxTasksPerProcess, processManager);
    }

    private org.aoju.bus.office.process.ProcessManager findBestProcessManager() {
        if (Builder.isLinux()) {
            LinuxProcessManager processManager = new LinuxProcessManager();
            if (runAsArgs != null) {
                processManager.setRunAsArgs(runAsArgs);
            }
            return processManager;
        } else {
            return new PureJavaProcessManager();
        }
    }

    private void checkArgumentNotNull(String argName, Object argValue) throws NullPointerException {
        if (argValue == null) {
            throw new NullPointerException(argName + " must not be null");
        }
    }

    private void checkArgument(String argName, boolean condition, String message) throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException(argName + " " + message);
        }
    }

    private boolean isValidProfileDir(File profileDir) {
        return new File(profileDir, "user").isDirectory();
    }

}
