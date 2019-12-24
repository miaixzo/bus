/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aoju.bus.http.accord.platform;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.http.Internal;
import org.aoju.bus.http.Protocol;

import javax.net.ssl.SSLSocket;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * OpenJDK 7 or OpenJDK 8 with {@code org.mortbay.jetty.alpn/alpn-boot} 在引导类路径中.
 */
class JdkWithJettyBootPlatform extends Platform {

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
            Method getMethod = negoClass.getMethod("get", SSLSocket.class);
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
            throw new InstrumentException("unable to set alpn", e);
        }
    }

    @Override
    public void afterHandshake(SSLSocket sslSocket) {
        try {
            removeMethod.invoke(null, sslSocket);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new InstrumentException("unable to remove alpn", e);
        }
    }

    @Override
    public String getSelectedProtocol(SSLSocket socket) {
        try {
            JettyNegoProvider provider =
                    (JettyNegoProvider) Proxy.getInvocationHandler(getMethod.invoke(null, socket));
            if (!provider.unsupported && provider.selected == null) {
                get().log(INFO, "ALPN callback dropped: HTTP/2 is disabled. class path?", null);
                return null;
            }
            return provider.unsupported ? null : provider.selected;
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw Internal.assertionError("unable to get selected protocol", e);
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
            if (args == null) {
                args = Internal.EMPTY_STRING_ARRAY;
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
