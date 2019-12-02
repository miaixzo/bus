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

import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.NoConnectException;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.Context;
import org.aoju.bus.office.magic.UnoUrl;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @author Kimi Liu
 * @version 5.3.0
 * @since JDK 1.8+
 */
public class Connection implements Context {

    private static AtomicInteger bridgeIndex = new AtomicInteger();

    private final UnoUrl unoUrl;
    private final List<ConnectionListener> connectionEventListeners = new ArrayList<ConnectionListener>();
    private final Logger logger = Logger.getLogger(getClass().getName());
    private XComponent bridgeComponent;
    private XMultiComponentFactory serviceManager;
    private XComponentContext componentContext;
    private volatile boolean connected = false;
    private XEventListener bridgeListener = new XEventListener() {
        public void disposing(EventObject event) {
            if (connected) {
                connected = false;
                logger.info(String.format("disconnected: '%s'", unoUrl));
                ConnectionEvent connectionEvent = new ConnectionEvent(Connection.this);
                for (ConnectionListener listener : connectionEventListeners) {
                    listener.disconnected(connectionEvent);
                }
            }
        }
    };

    public Connection(UnoUrl unoUrl) {
        this.unoUrl = unoUrl;
    }

    public void addConnectionEventListener(ConnectionListener connectionEventListener) {
        connectionEventListeners.add(connectionEventListener);
    }

    public void connect() throws ConnectException {
        logger.fine(String.format("connecting with connectString '%s'", unoUrl));
        try {
            XComponentContext localContext = Bootstrap.createInitialComponentContext(null);
            XMultiComponentFactory localServiceManager = localContext.getServiceManager();
            XConnector connector = Builder.cast(XConnector.class, localServiceManager.createInstanceWithContext("com.sun.star.connection.Connector", localContext));
            XConnection connection = connector.connect(unoUrl.getConnectString());
            XBridgeFactory bridgeFactory = Builder.cast(XBridgeFactory.class, localServiceManager.createInstanceWithContext("com.sun.star.bridge.BridgeFactory", localContext));
            String bridgeName = "jodconverter_" + bridgeIndex.getAndIncrement();
            XBridge bridge = bridgeFactory.createBridge(bridgeName, "urp", connection, null);
            bridgeComponent = Builder.cast(XComponent.class, bridge);
            bridgeComponent.addEventListener(bridgeListener);
            serviceManager = Builder.cast(XMultiComponentFactory.class, bridge.getInstance("StarOffice.ServiceManager"));
            XPropertySet properties = Builder.cast(XPropertySet.class, serviceManager);
            componentContext = Builder.cast(XComponentContext.class, properties.getPropertyValue("DefaultContext"));
            connected = true;
            logger.info(String.format("connected: '%s'", unoUrl));
            ConnectionEvent connectionEvent = new ConnectionEvent(this);
            for (ConnectionListener listener : connectionEventListeners) {
                listener.connected(connectionEvent);
            }
        } catch (NoConnectException connectException) {
            throw new ConnectException(String.format("connection failed: '%s'; %s", unoUrl, connectException.getMessage()));
        } catch (Exception exception) {
            throw new InstrumentException("connection failed: " + unoUrl, exception);
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public synchronized void disconnect() {
        logger.fine(String.format("disconnecting: '%s'", unoUrl));
        bridgeComponent.dispose();
    }

    public Object getService(String serviceName) {
        try {
            return serviceManager.createInstanceWithContext(serviceName, componentContext);
        } catch (Exception exception) {
            throw new InstrumentException(String.format("failed to obtain service '%s'", serviceName), exception);
        }
    }

}
