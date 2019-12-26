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
package org.aoju.bus.office.bridge;

import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.UnoUrl;
import org.aoju.bus.office.metric.OfficeConnectEvent;
import org.aoju.bus.office.metric.OfficeConnectEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负责使用给定的UnoUrl管理到office进程的连接.
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public class LocalOfficeBridgeFactory implements LocalOfficeContextAware, XEventListener {

    private static AtomicInteger bridgeIndex = new AtomicInteger();
    private final UnoUrl unoUrl;
    private final List<OfficeConnectEventListener> connectionEventListeners;
    private final AtomicBoolean connected = new AtomicBoolean();
    private Object desktopService;
    private XComponent bridgeComponent;
    private XComponentContext componentContext;
    private XComponentLoader componentLoader;

    /**
     * 为指定的UNO URL构造新连接.
     *
     * @param unoUrl 为其创建连接的URL.
     */
    public LocalOfficeBridgeFactory(final UnoUrl unoUrl) {
        this.unoUrl = unoUrl;
        this.connectionEventListeners = new ArrayList<>();
    }

    /**
     * 将侦听器添加到此连接的连接事件侦听器列表.
     *
     * @param connectionEventListener 当与office进程建立连接和连接丢失时，它将被通知.
     */
    public void addConnectionEventListener(final OfficeConnectEventListener connectionEventListener) {
        connectionEventListeners.add(connectionEventListener);
    }

    /**
     * 建立到office实例的连接
     */
    public void connect() throws InstrumentException {
        synchronized (this) {
            final String connectPart = unoUrl.getConnectionAndParametersAsString();
            Logger.debug("Connecting with connectString '{}'", connectPart);
            try {
                // 创建默认的本地组件上下文
                final XComponentContext localContext = Bootstrap.createInitialComponentContext(null);
                // 初始化服务管理器.
                final XMultiComponentFactory localServiceManager = localContext.getServiceManager();

                // 实例化连接器服务.
                final XConnector connector = Lo.qi(XConnector.class,
                        localServiceManager.createInstanceWithContext(
                                "com.sun.star.connection.Connector", localContext));

                // 仅使用uno-url的连接字符串部分进行连接.
                final XConnection connection = connector.connect(connectPart);

                // 实例化桥接工厂服务.
                final XBridgeFactory bridgeFactory = Lo.qi(XBridgeFactory.class,
                        localServiceManager.createInstanceWithContext(
                                "com.sun.star.bridge.BridgeFactory", localContext));

                // 使用urp协议创建没有实例提供程序的远程桥接.
                final String bridgeName = "converter_" + bridgeIndex.getAndIncrement();
                final XBridge bridge = bridgeFactory.createBridge(
                        bridgeName, unoUrl.getProtocolAndParametersAsString(), connection, null);

                // 查询XComponent接口并将其添加为事件监听器.
                bridgeComponent = Lo.qi(XComponent.class, bridge);
                bridgeComponent.addEventListener(this);

                // 获取远程实例
                final String rootOid = unoUrl.getRootOid();
                final Object bridgeInstance = bridge.getInstance(rootOid);

                if (ObjectUtils.isEmpty(bridgeInstance)) {
                    throw new InstrumentException(
                            "Server didn't provide an instance for '" + rootOid + Symbol.SINGLE_QUOTE, connectPart);
                }

                // 查询其主工厂接口的初始对象.
                final XMultiComponentFactory officeMultiComponentFactory = Lo.qi(XMultiComponentFactory.class, bridgeInstance);

                // 检索XPropertySet接口的组件上下文(尚未从office导出)查询.
                final XPropertySet properties = Lo.qi(XPropertySet.class, officeMultiComponentFactory);

                // 使用来自office服务器的默认上下文查询接口XComponentContext.
                componentContext = Lo.qi(XComponentContext.class, properties.getPropertyValue("DefaultContext"));

                // 现在创建处理应用程序窗口和文档的桌面服务
                // 注意:在这里使用office组件上下文!
                desktopService = officeMultiComponentFactory.createInstanceWithContext(
                        "com.sun.star.frame.Desktop", componentContext);
                componentLoader = Lo.qi(XComponentLoader.class, desktopService);

                if (ObjectUtils.isEmpty(componentLoader)) {
                    throw new InstrumentException("Could not create a desktop service", connectPart);
                }

                connected.set(true);
                Logger.info("Connected: '{}'", connectPart);

                // 通知所有的监听器我们已经接通了
                final OfficeConnectEvent connectionEvent = new OfficeConnectEvent(this);
                connectionEventListeners.stream().forEach(listener -> listener.connected(connectionEvent));

            } catch (InstrumentException connectionEx) {
                throw connectionEx;

            } catch (Exception ex) {
                throw new InstrumentException(
                        String.format("Connection failed: '%s'; %s", connectPart, ex.getMessage()),
                        connectPart,
                        ex);
            }
        }
    }

    /**
     * 关闭连接.
     */
    public void disconnect() {
        synchronized (this) {
            Logger.debug("Disconnecting from '{}'", unoUrl.getConnectionAndParametersAsString());
            bridgeComponent.dispose();
        }
    }

    @Override
    public void disposing(final EventObject eventObject) {
        if (connected.get()) {
            connected.set(false);
            componentContext = null;
            componentLoader = null;
            desktopService = null;
            bridgeComponent = null;

            Logger.info("Disconnected: '{}'", unoUrl.getConnectionAndParametersAsString());

            final OfficeConnectEvent connectionEvent = new OfficeConnectEvent(this);
            connectionEventListeners.stream().forEach(listener -> listener.disconnected(connectionEvent));
        }
    }

    @Override
    public XComponentContext getComponentContext() {
        return componentContext;
    }

    @Override
    public XComponentLoader getComponentLoader() {
        return componentLoader;
    }

    @Override
    public XDesktop getDesktop() {
        return Lo.qi(XDesktop.class, desktopService);
    }

    /**
     * 获取是否连接到office实例.
     *
     * @return {@code true} 连接到office实例 {@code false} 未连接.
     */
    public boolean isConnected() {
        return connected.get();
    }

}
