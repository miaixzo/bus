/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.accord.platform;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.net.Protocol;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * A platform-specific implementation for OpenJDK 9 and later.
 * <p>
 * This class handles TLS extensions like ALPN using the standard APIs available in modern JDKs.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
final class JdkPlatform extends Platform {

    final Method setProtocolMethod;
    final Method getProtocolMethod;

    JdkPlatform(Method setProtocolMethod, Method getProtocolMethod) {
        this.setProtocolMethod = setProtocolMethod;
        this.getProtocolMethod = getProtocolMethod;
    }

    /**
     * Builds a {@code JdkPlatform} instance if the current runtime is OpenJDK 9 or later.
     *
     * @return A new {@code JdkPlatform} instance, or null if not supported.
     */
    public static JdkPlatform buildIfSupported() {
        try {
            Method setProtocolMethod = SSLParameters.class.getMethod("setApplicationProtocols", String[].class);
            Method getProtocolMethod = SSLSocket.class.getMethod("getApplicationProtocol");

            return new JdkPlatform(setProtocolMethod, getProtocolMethod);
        } catch (NoSuchMethodException ignored) {
            // This is not an OpenJDK 9+ runtime.
        }

        return null;
    }

    @Override
    public void configureTlsExtensions(SSLSocket sslSocket, String hostname, List<Protocol> protocols) {
        try {
            SSLParameters sslParameters = sslSocket.getSSLParameters();

            List<String> names = alpnProtocolNames(protocols);

            setProtocolMethod.invoke(sslParameters, (Object) names.toArray(new String[0]));

            sslSocket.setSSLParameters(sslParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError("failed to set SSL parameters", e);
        }
    }

    @Override
    public String getSelectedProtocol(SSLSocket socket) {
        try {
            String protocol = (String) getProtocolMethod.invoke(socket);

            // SSLSocket.getApplicationProtocol returns "" if no application protocol value is used.
            // This is observed when SSLParameters.setApplicationProtocols is not specified.
            if (protocol == null || Normal.EMPTY.equals(protocol)) {
                return null;
            }

            return protocol;
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof UnsupportedOperationException) {
                // Handle UnsupportedOperationException as it is defined in the getApplicationProtocol API.
                // https://docs.oracle.com/javase/9/docs/api/javax/net/ssl/SSLSocket.html
                return null;
            }

            throw new AssertionError("failed to get ALPN selected protocol", e);
        } catch (IllegalAccessException e) {
            throw new AssertionError("failed to get ALPN selected protocol", e);
        }
    }

    @Override
    public X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
        // Not supported due to access checks on JDK 9+:
        // java.lang.reflect.InaccessibleObjectException: Unable to make member of class
        // sun.security.ssl.SSLSocketFactoryImpl accessible: module java.base does not export
        // sun.security.ssl to unnamed module @xxx
        throw new UnsupportedOperationException(
                "clientBuilder.sslSocketFactory(SSLSocketFactory) not supported on JDK 9+");
    }

}
