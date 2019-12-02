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

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.Manager;
import org.aoju.bus.office.Office;
import org.aoju.bus.office.magic.UnoUrl;

import java.net.ConnectException;

/**
 * {@link Manager} implementation that connects to an external Office process.
 * <p>
 * The external Office process needs to be started manually, e.g. from the command line with
 *
 * <pre>
 * soffice -accept="socket,host=127.0.0.1,port=2002;urp;"
 * </pre>
 * <p>
 * Since this implementation does not manage the Office process, it does not support auto-restarting the process if it exits unexpectedly.
 * <p>
 * It will however auto-reconnect to the external process if the latter is manually restarted.
 * <p>
 * This {@link Manager} implementation basically provides the same behaviour as JODConverter 2.x, including using <em>synchronized</em> blocks for serialising office
 * operations.
 *
 * @author Kimi Liu
 * @version 5.3.0
 * @since JDK 1.8+
 */
class ExternalManager implements Manager {

    private final Connection connection;
    private final boolean connectOnStart;

    /**
     * @param unoUrl
     * @param connectOnStart should a connection be attempted on {@link #start()}? Default is <em>true</em>. If <em>false</em>, a connection will only be attempted the first time an
     *                       {@link Office} is executed.
     */
    public ExternalManager(UnoUrl unoUrl, boolean connectOnStart) {
        connection = new Connection(unoUrl);
        this.connectOnStart = connectOnStart;
    }

    public void start() throws InstrumentException {
        if (connectOnStart) {
            synchronized (connection) {
                connect();
            }
        }
    }

    public void stop() {
        synchronized (connection) {
            if (connection.isConnected()) {
                connection.disconnect();
            }
        }
    }

    public void execute(Office task) throws InstrumentException {
        synchronized (connection) {
            if (!connection.isConnected()) {
                connect();
            }
            task.execute(connection);
        }
    }

    private void connect() {
        try {
            connection.connect();
        } catch (ConnectException connectException) {
            throw new InstrumentException("could not connect to external office process", connectException);
        }
    }

    public boolean isRunning() {
        return connection.isConnected();
    }

}
