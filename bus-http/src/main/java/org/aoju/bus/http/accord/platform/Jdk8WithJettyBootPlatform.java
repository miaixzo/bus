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
 * OpenJDK 8 with {@code org.mortbay.jetty.alpn:alpn-boot} in the boot class path.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
class Jdk8WithJettyBootPlatform extends Platform {
    private final Method putMethod;
    private final Method getMethod;
    private final Method removeMethod;
    private final Class<?> clientProviderClass;
    private final Class<?> serverProviderClass;

    Jdk8WithJettyBootPlatform(Method putMethod, Method getMethod, Method removeMethod,
                              Class<?> clientProviderClass, Class<?> serverProviderClass) {
        this.putMethod = putMethod;
        this.getMethod = getMethod;
        this.removeMethod = removeMethod;
        this.clientProviderClass = clientProviderClass;
        this.serverProviderClass = serverProviderClass;
    }

    public static Platform buildIfSupported() {
        // Find Jetty's ALPN extension for OpenJDK.
        try {
            String alpnClassName = "org.eclipse.jetty.alpn.ALPN";
            Class<?> alpnClass = Class.forName(alpnClassName, true, null);
            Class<?> providerClass = Class.forName(alpnClassName + "$Provider", true, null);
            Class<?> clientProviderClass = Class.forName(alpnClassName + "$ClientProvider", true, null);
            Class<?> serverProviderClass = Class.forName(alpnClassName + "$ServerProvider", true, null);
            Method putMethod = alpnClass.getMethod("put", SSLSocket.class, providerClass);
            Method getMethod = alpnClass.getMethod("get", SSLSocket.class);
            Method removeMethod = alpnClass.getMethod("remove", SSLSocket.class);
            return new Jdk8WithJettyBootPlatform(
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
            Object alpnProvider = Proxy.newProxyInstance(Platform.class.getClassLoader(),
                    new Class[]{clientProviderClass, serverProviderClass}, new AlpnProvider(names));
            putMethod.invoke(null, sslSocket, alpnProvider);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new AssertionError("failed to set ALPN", e);
        }
    }

    @Override
    public void afterHandshake(SSLSocket sslSocket) {
        try {
            removeMethod.invoke(null, sslSocket);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError("failed to remove ALPN", e);
        }
    }

    @Override
    public String getSelectedProtocol(SSLSocket socket) {
        try {
            AlpnProvider provider =
                    (AlpnProvider) Proxy.getInvocationHandler(getMethod.invoke(null, socket));
            if (!provider.unsupported && provider.selected == null) {
                Logger.info("ALPN callback dropped: HTTP/2 is disabled. "
                        + "Is alpn-boot on the boot class path?", null);
                return null;
            }
            return provider.unsupported ? null : provider.selected;
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new AssertionError("failed to get ALPN selected protocol", e);
        }
    }

    /**
     * Handle the methods of ALPN's ClientProvider and ServerProvider without a compile-time
     * dependency on those interfaces.
     */
    private static class AlpnProvider implements InvocationHandler {
        /**
         * This peer's supported protocols.
         */
        private final List<String> protocols;
        /**
         * Set when remote peer notifies ALPN is unsupported.
         */
        boolean unsupported;
        /**
         * The protocol the server selected.
         */
        String selected;

        AlpnProvider(List<String> protocols) {
            this.protocols = protocols;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();
            if (args == null) {
                args = Builder.EMPTY_STRING_ARRAY;
            }
            if (methodName.equals("supports") && boolean.class == returnType) {
                return true; // ALPN is supported.
            } else if (methodName.equals("unsupported") && void.class == returnType) {
                this.unsupported = true; // Peer doesn't support ALPN.
                return null;
            } else if (methodName.equals("protocols") && args.length == 0) {
                return protocols; // Client advertises these protocols.
            } else if ((methodName.equals("selectProtocol") || methodName.equals("select"))
                    && String.class == returnType && args.length == 1 && args[0] instanceof List) {
                List<?> peerProtocols = (List) args[0];
                // Pick the first known protocol the peer advertises.
                for (int i = 0, size = peerProtocols.size(); i < size; i++) {
                    String protocol = (String) peerProtocols.get(i);
                    if (protocols.contains(protocol)) {
                        return selected = protocol;
                    }
                }
                return selected = protocols.get(0); // On no intersection, try peer's first protocol.
            } else if ((methodName.equals("protocolSelected") || methodName.equals("selected"))
                    && args.length == 1) {
                this.selected = (String) args[0]; // Server selected this protocol.
                return null;
            } else {
                return method.invoke(this, args);
            }
        }
    }

}
