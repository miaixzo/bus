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

import com.sun.star.frame.XDesktop;
import com.sun.star.lang.DisposedException;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.Retryable;

import java.net.ConnectException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Kimi Liu
 * @version 5.3.1
 * @since JDK 1.8+
 */
class ManagedProcess {

    private static final Integer EXIT_CODE_NEW_INSTALLATION = Integer.valueOf(81);

    private final ManagedSettings settings;

    private final org.aoju.bus.office.Process process;
    private final Connection connection;
    private final Logger logger = Logger.getLogger(getClass().getName());
    private ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("OfficeProcessThread"));

    public ManagedProcess(ManagedSettings settings) throws InstrumentException {
        this.settings = settings;
        process = new org.aoju.bus.office.Process(settings.getOfficeHome(), settings.getUnoUrl(), settings.getRunAsArgs(), settings.getTemplateProfileDir(), settings.getWorkDir(), settings
                .getProcessManager());
        connection = new Connection(settings.getUnoUrl());
    }

    public Connection getConnection() {
        return connection;
    }

    public void startAndWait() throws InstrumentException {
        Future<?> future = executor.submit(new Runnable() {
            public void run() {
                doStartProcessAndConnect();
            }
        });
        try {
            future.get();
        } catch (Exception exception) {
            throw new InstrumentException("failed to start and connect", exception);
        }
    }

    public void stopAndWait() throws InstrumentException {
        Future<?> future = executor.submit(new Runnable() {
            public void run() {
                doStopProcess();
            }
        });
        try {
            future.get();
        } catch (Exception exception) {
            throw new InstrumentException("failed to start and connect", exception);
        }
    }

    public void restartAndWait() {
        Future<?> future = executor.submit(new Runnable() {
            public void run() {
                doStopProcess();
                doStartProcessAndConnect();
            }
        });
        try {
            future.get();
        } catch (Exception exception) {
            throw new InstrumentException("failed to restart", exception);
        }
    }

    public void restartDueToTaskTimeout() {
        executor.execute(new Runnable() {
            public void run() {
                doTerminateProcess();
                // will cause unexpected disconnection and subsequent restart
            }
        });
    }

    public void restartDueToLostConnection() {
        executor.execute(new Runnable() {
            public void run() {
                try {
                    doEnsureProcessExited();
                    doStartProcessAndConnect();
                } catch (InstrumentException officeException) {
                    logger.log(Level.SEVERE, "could not restart process", officeException);
                }
            }
        });
    }

    private void doStartProcessAndConnect() throws InstrumentException {
        try {
            process.start();
            new Retryable() {
                protected void attempt() throws Exception {
                    try {
                        connection.connect();
                    } catch (ConnectException connectException) {
                        Integer exitCode = process.getExitCode();
                        if (exitCode == null) {
                            // process is running; retry later
                            throw new InstrumentException(connectException);
                        } else if (exitCode.equals(EXIT_CODE_NEW_INSTALLATION)) {
                            // restart and retry later
                            // see http://code.google.com/p/jodconverter/issues/detail?id=84
                            logger.log(Level.WARNING, "office process died with exit code 81; restarting it");
                            process.start(true);
                            throw new InstrumentException(connectException);
                        } else {
                            throw new InstrumentException("office process died with exit code " + exitCode);
                        }
                    }
                }
            }.execute(settings.getRetryInterval(), settings.getRetryTimeout());
        } catch (Exception exception) {
            throw new InstrumentException("could not establish connection", exception);
        }
    }

    private void doStopProcess() {
        try {
            XDesktop desktop = Builder.cast(XDesktop.class, connection.getService(Builder.SERVICE_DESKTOP));
            desktop.terminate();
        } catch (DisposedException disposedException) {
            // expected
        } catch (Exception exception) {
            // in case we can't get hold of the desktop
            doTerminateProcess();
        }
        doEnsureProcessExited();
    }

    private void doEnsureProcessExited() throws InstrumentException {
        try {
            int exitCode = process.getExitCode(settings.getRetryInterval(), settings.getRetryTimeout());
            logger.info("process exited with code " + exitCode);
        } catch (InstrumentException ex) {
            doTerminateProcess();
        }
        process.deleteProfileDir();
    }

    private void doTerminateProcess() throws InstrumentException {
        try {
            int exitCode = process.forciblyTerminate(settings.getRetryInterval(), settings.getRetryTimeout());
            logger.info("process forcibly terminated with code " + exitCode);
        } catch (Exception exception) {
            throw new InstrumentException("could not terminate process", exception);
        }
    }

    boolean isConnected() {
        return connection.isConnected();
    }

}
