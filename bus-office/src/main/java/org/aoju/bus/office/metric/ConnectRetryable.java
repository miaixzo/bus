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

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.bridge.LocalOfficeBridgeFactory;

/**
 * 执行到office进程的连接.
 *
 * @author Kimi Liu
 * @version 6.1.0
 * @since JDK 1.8+
 */
public class ConnectRetryable extends AbstractRetryable {

    private final OfficeProcess process;
    private final LocalOfficeBridgeFactory localOffice;

    /**
     * 为指定的连接创建类的新实例.
     *
     * @param localOffice 要连接的office.
     */
    public ConnectRetryable(final LocalOfficeBridgeFactory localOffice) {
        this(null, localOffice);
    }

    /**
     * 为指定的进程和连接创建类的新实例.
     *
     * @param process     要检索其退出码的office进程.
     * @param localOffice 要连接的office.
     */
    public ConnectRetryable(final OfficeProcess process, final LocalOfficeBridgeFactory localOffice) {
        super();

        this.process = process;
        this.localOffice = localOffice;
    }

    @Override
    protected void attempt() throws InstrumentException {
        try {
            localOffice.connect();
        } catch (InstrumentException connectionEx) {
            if (ObjectKit.isEmpty(process)) {
                throw new InstrumentException(connectionEx);
            }
            final Integer exitCode = process.getExitCode();
            if (ObjectKit.isEmpty(exitCode)) {
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
