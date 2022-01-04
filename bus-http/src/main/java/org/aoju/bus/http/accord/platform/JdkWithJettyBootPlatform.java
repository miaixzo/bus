/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.http.accord.platform;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.http.Builder;
import org.aoju.bus.http.Protocol;
import org.aoju.bus.logger.Logger;

import javax.net.ssl.SSLSocket;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * OpenJDK 7 or OpenJDK 8 with {@code org.mortbay.jetty.alpn/alpn-boot} 在引导类路径中.
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public class JdkWithJettyBootPlatform extends Platform {

    private final Method putMethod;
    private final Method getMethod;
    private final Method removeMethod;
    private final Class<?> clientProviderClass;
    private final Class<?> serverProviderClass;

    JdkWithJettyBootPlatform(Method putMethod, Method getMethod, Method removeMethod,
                             Class<?> clientProviderClass, Class<?> serverProviderClass) {
        this.putMethod = putMethod;
        this.getMethod = getMethod;
        this.removeMethod = removeMethod;
        this.clientProviderClass = clientProviderClass;
        this.serverProviderClass = serverProviderClass;
    }

    public static Platform buildIfSupported() {
        // 查找Jetty的OpenJDK ALPN扩展
        try {
            String negoClassName = "org.eclipse.jetty.alpn.ALPN";
            Class<?> negoClass = Class.forName(negoClassName);
            Class<?> providerClass = Class.forName(negoClassName + "$Provider");
            Class<?> clientProviderClass = Class.forName(negoClassName + "$ClientProvider");
            Class<?> serverProviderClass = Class.forName(negoClassName + "$ServerProvider");
            Method putMethod = negoClass.getMethod("put", SSLSocket.class, providerClass);
            Method getMethod = negoClass.getMethod(Normal.GET, SSLSocket.class);
            Method removeMethod = negoClass.getMethod("remove", SSLSocket.class);
            return new JdkWithJettyBootPlatform(
                    putMethod, getMethod, removeMethod, clientProviderClass, serverProviderClass);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }

        return null;
    }

    @Override
    public void configureTlsExtensions(
            SSLSocket sslSocket, String hostname, List<Protocol> protocols) {
        List<String> names = alpnProtocolNames(protocols);
        try {
            Object provider = Proxy.newProxyInstance(Platform.class.getClassLoader(),
                    new Class[]{clientProviderClass, serverProviderClass}, new JettyNegoProvider(names));
            putMethod.invoke(null, sslSocket, provider);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw Builder.assertionError("unable to set alpn", e);
        }
    }

    @Override
    public void afterHandshake(SSLSocket sslSocket) {
        try {
            removeMethod.invoke(null, sslSocket);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw Builder.assertionError("unable to remove alpn", e);
        }
    }

    @Override
    public String getSelectedProtocol(SSLSocket socket) {
        try {
            JettyNegoProvider provider =
                    (JettyNegoProvider) Proxy.getInvocationHandler(getMethod.invoke(null, socket));
            if (!provider.unsupported && null == provider.selected) {
                Logger.info("ALPN callback dropped: HTTP/2 is disabled. "
                        + "Is alpn-boot on the boot class path?", null);
                return null;
            }
            return provider.unsupported ? null : provider.selected;
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw Builder.assertionError("unable to get selected protocol", e);
        }
    }

    /**
     * 处理ALPN的ClientProvider和ServerProvider的方法，而不需要在编译时依赖于这些接口
     */
    private static class JettyNegoProvider implements InvocationHandler {

        /**
         * 这个对等点支持的协议.
         */
        private final List<String> protocols;
        /**
         * 当远程对等节点通知不支持ALPN时设置.
         */
        boolean unsupported;
        /**
         * 服务器选择的协议.
         */
        String selected;

        JettyNegoProvider(List<String> protocols) {
            this.protocols = protocols;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();
            if (null == args) {
                args = Normal.EMPTY_STRING_ARRAY;
            }
            if (methodName.equals("supports") && boolean.class == returnType) {
                // ALPN支持
                return true;
            } else if (methodName.equals("unsupported") && void.class == returnType) {
                // 不支持ALPN
                this.unsupported = true;
                return null;
            } else if (methodName.equals("protocols") && args.length == 0) {
                // 客户端广播这些协议
                return protocols;
            } else if ((methodName.equals("selectProtocol") || methodName.equals("select"))
                    && String.class == returnType && args.length == 1 && args[0] instanceof List) {
                List<String> peerProtocols = (List) args[0];
                // 选择同行宣传的第一个已知协议.
                for (int i = 0, size = peerProtocols.size(); i < size; i++) {
                    if (protocols.contains(peerProtocols.get(i))) {
                        return selected = peerProtocols.get(i);
                    }
                }
                return selected = protocols.get(0);
            } else if ((methodName.equals("protocolSelected") || methodName.equals("selected"))
                    && args.length == 1) {
                this.selected = (String) args[0];
                return null;
            } else {
                return method.invoke(this, args);
            }
        }
    }
}
