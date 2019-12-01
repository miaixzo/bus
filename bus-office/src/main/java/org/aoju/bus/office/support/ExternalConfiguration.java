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

import org.aoju.bus.office.Manager;
import org.aoju.bus.office.magic.UnoUrl;

/**
 * @author Kimi Liu
 * @version 5.2.8
 * @since JDK 1.8+
 */
public class ExternalConfiguration {

    private ConnectionProtocol connectionProtocol = ConnectionProtocol.SOCKET;
    private int portNumber = 2002;
    private String pipeName = "office";
    private boolean connectOnStart = true;

    public ExternalConfiguration setConnectionProtocol(ConnectionProtocol connectionProtocol) {
        this.connectionProtocol = connectionProtocol;
        return this;
    }

    public ExternalConfiguration setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    public ExternalConfiguration setPipeName(String pipeName) {
        this.pipeName = pipeName;
        return this;
    }

    public ExternalConfiguration setConnectOnStart(boolean connectOnStart) {
        this.connectOnStart = connectOnStart;
        return this;
    }

    public Manager buildOfficeManager() {
        UnoUrl unoUrl = connectionProtocol == ConnectionProtocol.SOCKET ? UnoUrl.socket(portNumber) : UnoUrl.pipe(pipeName);
        return new ExternalManager(unoUrl, connectOnStart);
    }

}
