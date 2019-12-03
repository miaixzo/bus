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
package org.aoju.bus.office;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.office.magic.UnoUrl;
import org.aoju.bus.office.process.ProcessManager;
import org.aoju.bus.office.support.ProcessQuery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Kimi Liu
 * @version 5.3.1
 * @since JDK 1.8+
 */
public class Process {

    private final File officeHome;
    private final UnoUrl unoUrl;
    private final String[] runAsArgs;
    private final File templateProfileDir;
    private final File instanceProfileDir;
    private final ProcessManager processManager;
    private final Logger logger = Logger.getLogger(getClass().getName());
    private java.lang.Process process;
    private long pid = ProcessManager.PID_UNKNOWN;

    public Process(File officeHome, UnoUrl unoUrl, String[] runAsArgs, File templateProfileDir, File workDir, ProcessManager processManager) {
        this.officeHome = officeHome;
        this.unoUrl = unoUrl;
        this.runAsArgs = runAsArgs;
        this.templateProfileDir = templateProfileDir;
        this.instanceProfileDir = getInstanceProfileDir(workDir, unoUrl);
        this.processManager = processManager;
    }

    public void start() throws IOException {
        start(false);
    }

    public void start(boolean restart) throws IOException {
        ProcessQuery processQuery = new ProcessQuery("soffice.bin", unoUrl.getAcceptString());
        long existingPid = processManager.findPid(processQuery);
        if (!(existingPid == ProcessManager.PID_NOT_FOUND || existingPid == ProcessManager.PID_UNKNOWN)) {
            throw new IllegalStateException(String.format("a process with acceptString '%s' is already running; pid %d",
                    unoUrl.getAcceptString(), existingPid));
        }
        if (!restart) {
            prepareInstanceProfileDir();
        }
        List<String> command = new ArrayList<String>();
        File executable = Builder.getOfficeExecutable(officeHome);
        if (runAsArgs != null) {
            command.addAll(Arrays.asList(runAsArgs));
        }
        command.add(executable.getAbsolutePath());
        command.add("-accept=" + unoUrl.getAcceptString() + ";urp;");
        command.add("-env:UserInstallation=" + Builder.toUrl(instanceProfileDir));
        command.add("-headless");
        command.add("-nocrashreport");
        command.add("-nodefault");
        command.add("-nofirststartwizard");
        command.add("-nolockcheck");
        command.add("-nologo");
        command.add("-norestore");
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        if (Builder.isWindows()) {
            addBasisAndUrePaths(processBuilder);
        }
        logger.info(String.format("starting process with acceptString '%s' and profileDir '%s'", unoUrl, instanceProfileDir));
        process = processBuilder.start();
        pid = processManager.findPid(processQuery);
        if (pid == ProcessManager.PID_NOT_FOUND) {
            throw new IllegalStateException(String.format("process with acceptString '%s' started but its pid could not be found",
                    unoUrl.getAcceptString()));
        }
        logger.info("started process" + (pid != ProcessManager.PID_UNKNOWN ? "; pid = " + pid : ""));
    }

    private File getInstanceProfileDir(File workDir, UnoUrl unoUrl) {
        String dirName = ".jodconverter_" + unoUrl.getAcceptString().replace(',', '_').replace('=', '-');
        return new File(workDir, dirName);
    }

    private void prepareInstanceProfileDir() throws InstrumentException {
        if (instanceProfileDir.exists()) {
            logger.warning(String.format("profile dir '%s' already exists; deleting", instanceProfileDir));
            deleteProfileDir();
        }
        if (templateProfileDir != null) {
            try {
                FileUtils.copy(templateProfileDir, instanceProfileDir, true);
            } catch (InstrumentException ex) {
                throw new InstrumentException("failed to create profileDir", ex);
            }
        }
    }

    public void deleteProfileDir() {
        if (instanceProfileDir != null) {
            try {
                FileUtils.clean(instanceProfileDir);
            } catch (InstrumentException ex) {
                File oldProfileDir = new File(instanceProfileDir.getParentFile(), instanceProfileDir.getName() + ".old." + System.currentTimeMillis());
                if (instanceProfileDir.renameTo(oldProfileDir)) {
                    logger.warning("could not delete profileDir: " + ex.getMessage() + "; renamed it to " + oldProfileDir);
                } else {
                    logger.severe("could not delete profileDir: " + ex.getMessage());
                }
            }
        }
    }

    private void addBasisAndUrePaths(ProcessBuilder processBuilder) throws IOException {
        // see http://wiki.services.openoffice.org/wiki/ODF_Toolkit/Efforts/Three-Layer_OOo
        File basisLink = new File(officeHome, "basis-link");
        if (!basisLink.isFile()) {
            logger.fine("no %OFFICE_HOME%/basis-link found; assuming it's OOo 2.x and we don't need to append URE and Basic paths");
            return;
        }
        String basisLinkText = FileUtils.readFile(basisLink).trim();
        File basisHome = new File(officeHome, basisLinkText);
        File basisProgram = new File(basisHome, "program");
        File ureLink = new File(basisHome, "ure-link");
        String ureLinkText = FileUtils.readFile(ureLink).trim();
        File ureHome = new File(basisHome, ureLinkText);
        File ureBin = new File(ureHome, "bin");
        Map<String, String> environment = processBuilder.environment();
        // Windows environment variables are case insensitive but Java maps are not :-/
        // so let's make sure we modify the existing key
        String pathKey = "PATH";
        for (String key : environment.keySet()) {
            if ("PATH".equalsIgnoreCase(key)) {
                pathKey = key;
            }
        }
        String path = environment.get(pathKey) + ";" + ureBin.getAbsolutePath() + ";" + basisProgram.getAbsolutePath();
        logger.fine(String.format("setting %s to \"%s\"", pathKey, path));
        environment.put(pathKey, path);
    }

    public boolean isRunning() {
        if (process == null) {
            return false;
        }
        return getExitCode() == null;
    }

    public Integer getExitCode() {
        try {
            return process.exitValue();
        } catch (IllegalThreadStateException exception) {
            return null;
        }
    }

    public int getExitCode(long retryInterval, long retryTimeout) throws InstrumentException {
        try {
            ExitCodeRetryable retryable = new ExitCodeRetryable();
            retryable.execute(retryInterval, retryTimeout);
            return retryable.getExitCode();
        } catch (InstrumentException retryTimeoutException) {
            throw retryTimeoutException;
        } catch (Exception exception) {
            throw new InstrumentException("could not get process exit code", exception);
        }
    }

    public int forciblyTerminate(long retryInterval, long retryTimeout) throws IOException, InstrumentException {
        logger.info(String.format("trying to forcibly terminate process: '" + unoUrl + "'" + (pid != ProcessManager.PID_UNKNOWN ? " (pid " + pid + ")" : "")));
        processManager.kill(process, pid);
        return getExitCode(retryInterval, retryTimeout);
    }

    private class ExitCodeRetryable extends Retryable {

        private int exitCode;

        protected void attempt() throws Exception {
            try {
                exitCode = process.exitValue();
            } catch (IllegalThreadStateException illegalThreadStateException) {
                throw new InstrumentException(illegalThreadStateException);
            }
        }

        public int getExitCode() {
            return exitCode;
        }

    }

}
