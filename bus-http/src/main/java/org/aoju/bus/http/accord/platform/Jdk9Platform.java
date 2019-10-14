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
package org.aoju.bus.http.accord.platform;

import org.aoju.bus.http.Internal;
import org.aoju.bus.http.Protocol;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * OpenJDK 9+.
 *
 * @author Kimi Liu
 * @version 5.0.1
 * @since JDK 1.8+
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
            // pre JDK 9
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
