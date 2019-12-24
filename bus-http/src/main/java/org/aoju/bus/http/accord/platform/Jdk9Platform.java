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

import org.aoju.bus.http.Internal;
import org.aoju.bus.http.Protocol;
import org.aoju.bus.logger.Logger;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * OpenJDK 9+.
 */
final class Jdk9Platform extends Platform {

    final Method setProtocolMethod;
    final Method getProtocolMethod;

    Jdk9Platform(Method setProtocolMethod, Method getProtocolMethod) {
        this.setProtocolMethod = setProtocolMethod;
        this.getProtocolMethod = getProtocolMethod;
    }

    public static Jdk9Platform buildIfSupported() {
        try {
            Method setProtocolMethod =
                    SSLParameters.class.getMethod("setApplicationProtocols", String[].class);
            Method getProtocolMethod = SSLSocket.class.getMethod("getApplicationProtocol");

            return new Jdk9Platform(setProtocolMethod, getProtocolMethod);
        } catch (NoSuchMethodException ignored) {
            Logger.error(ignored);
        }

        return null;
    }

    @Override
    public void configureTlsExtensions(SSLSocket sslSocket, String hostname,
                                       List<Protocol> protocols) {
        try {
            SSLParameters sslParameters = sslSocket.getSSLParameters();

            List<String> names = alpnProtocolNames(protocols);

            setProtocolMethod.invoke(sslParameters,
                    new Object[]{names.toArray(new String[names.size()])});

            sslSocket.setSSLParameters(sslParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw Internal.assertionError("unable to set ssl parameters", e);
        }
    }

    @Override
    public String getSelectedProtocol(SSLSocket socket) {
        try {
            String protocol = (String) getProtocolMethod.invoke(socket);
            // SSLSocket.getApplicationProtocol 返回 "" 如果应用程序协议值不被使用
            // 没有指定SSLParameters.setApplicationProtocols时观察到的
            if (protocol == null || protocol.equals("")) {
                return null;
            }

            return protocol;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw Internal.assertionError("unable to get selected protocols", e);
        }
    }

    @Override
    public X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
        throw new UnsupportedOperationException(
                "clientBuilder.sslSocketFactory(SSLSocketFactory) not supported on JDK 9+");
    }

}
