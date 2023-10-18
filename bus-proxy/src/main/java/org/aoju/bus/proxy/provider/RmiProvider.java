/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.proxy.provider;

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.proxy.Provider;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;

/**
 * 通过在RMI注册表中查找来提供对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RmiProvider implements Provider {

    private String host = Http.HOST_LOCAL;
    private int port = Registry.REGISTRY_PORT;
    private RMIClientSocketFactory clientSocketFactory;
    private String name;

    public RmiProvider() {

    }

    public RmiProvider(String name) {
        setName(name);
    }

    public RmiProvider(String host, String name) {
        setHost(host);
        setName(name);
    }

    public RmiProvider(String host, int port, String name) {
        setHost(host);
        setName(name);
        setPort(port);
    }

    public RmiProvider(String host, int port, RMIClientSocketFactory clientSocketFactory, String name) {
        setHost(host);
        setPort(port);
        setClientSocketFactory(clientSocketFactory);
        setName(name);
    }

    public Object getObject() {
        try {
            Registry reg = getRegistry();
            return reg.lookup(name);
        } catch (NotBoundException e) {
            throw new InternalException("Name " + name + " not found in registry at " + host + Symbol.COLON + port + Symbol.DOT,
                    e);
        } catch (RemoteException e) {
            throw new InternalException(
                    "Unable to lookup service named " + name + " in registry at " + host + Symbol.COLON + port + Symbol.DOT, e);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setClientSocketFactory(RMIClientSocketFactory clientSocketFactory) {
        this.clientSocketFactory = clientSocketFactory;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private Registry getRegistry() {
        try {
            if (null != clientSocketFactory) {
                return LocateRegistry.getRegistry(host, port, clientSocketFactory);
            } else {
                return LocateRegistry.getRegistry(host, port);
            }
        } catch (RemoteException e) {
            throw new InternalException("Unable to locate registry at " + host + Symbol.COLON + port + Symbol.DOT, e);
        }
    }

}

