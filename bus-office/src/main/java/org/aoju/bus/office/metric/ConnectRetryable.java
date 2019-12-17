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
package org.aoju.bus.office.metric;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.verbose.LocalConnect;

/**
 * 执行到office进程的连接.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class ConnectRetryable extends AbstractRetryable {

    private final OfficeProcess process;
    private final LocalConnect localConnect;

    /**
     * 为指定的连接创建类的新实例.
     *
     * @param localConnect 要连接的office.
     */
    public ConnectRetryable(final LocalConnect localConnect) {
        this(null, localConnect);
    }

    /**
     * 为指定的进程和连接创建类的新实例.
     *
     * @param process      要检索其退出码的office进程.
     * @param localConnect 要连接的office.
     */
    public ConnectRetryable(final OfficeProcess process, final LocalConnect localConnect) {
        super();

        this.process = process;
        this.localConnect = localConnect;
    }

    @Override
    protected void attempt() throws InstrumentException {
        try {
            localConnect.connect();
        } catch (InstrumentException connectionEx) {
            if (ObjectUtils.isEmpty(process)) {
                throw new InstrumentException(connectionEx);
            }
            final Integer exitCode = process.getExitCode();
            if (ObjectUtils.isEmpty(exitCode)) {
                throw new InstrumentException(connectionEx);
            } else if (exitCode.equals(Integer.valueOf(81))) {
                Logger.warn("Office process died with exit code 81; restarting it");
                process.start(true);
                throw new InstrumentException(connectionEx);
            } else {
                throw new InstrumentException("Office process died with exit code " + exitCode, connectionEx);
            }
        }
    }

}
