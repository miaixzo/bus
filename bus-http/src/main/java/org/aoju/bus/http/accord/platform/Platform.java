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

import org.aoju.bus.core.io.segment.Buffer;
import org.aoju.bus.http.Client;
import org.aoju.bus.http.Protocol;
import org.aoju.bus.http.secure.BasicCertificateChainCleaner;
import org.aoju.bus.http.secure.BasicTrustRootIndex;
import org.aoju.bus.http.secure.CertificateChainCleaner;
import org.aoju.bus.http.secure.TrustRootIndex;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Access to platform-specific features.
 *
 * <h3>Server name indication (SNI)</h3>
 *
 * <p>Supported on Android 2.3+.
 * <p>
 * Supported on OpenJDK 7+
 *
 * <h3>Session Tickets</h3>
 *
 * <p>Supported on Android 2.3+.
 *
 * <h3>Android Traffic Stats (Socket Tagging)</h3>
 *
 * <p>Supported on Android 4.0+.
 *
 * <h3>ALPN (Application Layer Protocol Negotiation)</h3>
 *
 * <p>Supported on Android 5.0+. The APIs were present in Android 4.4, but that implementation was
 * unstable.
 * Supported on OpenJDK 9 via SSLParameters and SSLSocket features.
 *
 * <h3>Trust Manager Extraction</h3>
 *
 * <p>Supported on Android 2.3+ and OpenJDK 7+. There are no public APIs to recover the trust
 * manager that was used to create an {@link SSLSocketFactory}.
 *
 * <h3>Android Cleartext Permit Detection</h3>
 *
 * <p>Supported on Android 6.0+ via {@code NetworkSecurityPolicy}.
 *
 * @author Kimi Liu
 * @version 5.0.2
 * @since JDK 1.8+
 */
public class Platform {

    public static final int INFO = 4;
    public static final int WARN = 5;
    private static final Platform PLATFORM = findPlatform();
    private static final Logger logger = Logger.getLogger(Client.class.getName());

    public static Platform get() {
        return PLATFORM;
    }

    public static List<String> alpnProtocolNames(List<Protocol> protocols) {
        List<String> names = new ArrayList<>(protocols.size());
        for (int i = 0, size = protocols.size(); i < size; i++) {
            Protocol protocol = protocols.get(i);
            if (protocol == Protocol.HTTP_1_0) continue; // No HTTP/1.0 for ALPN.
            names.add(protocol.toString());
        }
        return names;
    }

    public static boolean isConscryptPreferred() {
        if ("conscrypt".equals(System.getProperty("HttpClient.platform"))) {
            return true;
        }

        String preferredProvider = Security.getProviders()[0].getName();
        return "Conscrypt".equals(preferredProvider);
    }

    private static Platform findPlatform() {

        Platform jdk9 = Jdk9Platform.buildIfSupported();

        if (jdk9 != null) {
            return jdk9;
        }

        Platform jdkWithJettyBoot = JdkWithJettyBootPlatform.buildIfSupported();

        if (jdkWithJettyBoot != null) {
            return jdkWithJettyBoot;
        }

        return new Platform();
    }

    static byte[] concatLengthPrefixed(List<Protocol> protocols) {
        Buffer result = new Buffer();
        for (int i = 0, size = protocols.size(); i < size; i++) {
            Protocol protocol = protocols.get(i);
            if (protocol == Protocol.HTTP_1_0) continue;
            result.writeByte(protocol.toString().length());
            result.writeUtf8(protocol.toString());
        }
        return result.readByteArray();
    }

    static <T> T readFieldOrNull(Object instance, Class<T> fieldType, String fieldName) {
        for (Class<?> c = instance.getClass(); c != Object.class; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(instance);
                if (value == null || !fieldType.isInstance(value)) return null;
                return fieldType.cast(value);
            } catch (NoSuchFieldException ignored) {
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            }
        }

        if (!fieldName.equals("delegate")) {
            Object delegate = readFieldOrNull(instance, Object.class, "delegate");
            if (delegate != null) return readFieldOrNull(delegate, fieldType, fieldName);
        }

        return null;
    }

    public String getPrefix() {
        return "HttpClient";
    }

    protected X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
        try {
            Class<?> sslContextClass = Class.forName("sun.security.ssl.SSLContextImpl");
            Object context = readFieldOrNull(sslSocketFactory, sslContextClass, "context");
            if (context == null) return null;
            return readFieldOrNull(context, X509TrustManager.class, "trustManager");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public void configureTlsExtensions(SSLSocket sslSocket, String hostname,
                                       List<Protocol> protocols) {
    }

    public void afterHandshake(SSLSocket sslSocket) {
    }

    public String getSelectedProtocol(SSLSocket socket) {
        return null;
    }

    public void connectSocket(Socket socket, InetSocketAddress address, int connectTimeout)
            throws IOException {
        socket.connect(address, connectTimeout);
    }

    public void log(int level, String message, Throwable t) {
        Level logLevel = level == WARN ? Level.WARNING : Level.INFO;
        logger.log(logLevel, message, t);
    }

    public boolean isCleartextTrafficPermitted(String hostname) {
        return true;
    }

    public Object getStackTraceForCloseable(String closer) {
        if (logger.isLoggable(Level.FINE)) {
            return new Throwable(closer); // These are expensive to allocate.
        }
        return null;
    }

    public void logCloseableLeak(String message, Object stackTrace) {
        if (stackTrace == null) {
            message += " To see where this was allocated, set the HttpClient logger level to FINE: "
                    + "Logger.getLogger(HttpClient.class.getName()).setLevel(Level.FINE);";
        }
        log(WARN, message, (Throwable) stackTrace);
    }

    public CertificateChainCleaner buildCertificateChainCleaner(X509TrustManager trustManager) {
        return new BasicCertificateChainCleaner(buildTrustRootIndex(trustManager));
    }

    public CertificateChainCleaner buildCertificateChainCleaner(SSLSocketFactory sslSocketFactory) {
        X509TrustManager trustManager = trustManager(sslSocketFactory);

        if (trustManager == null) {
            throw new IllegalStateException("Unable to extract the trust manager on "
                    + Platform.get()
                    + ", sslSocketFactory is "
                    + sslSocketFactory.getClass());
        }

        return buildCertificateChainCleaner(trustManager);
    }

    public SSLContext getSSLContext() {
        String jvmVersion = System.getProperty("java.specification.version");
        if ("1.7".equals(jvmVersion)) {
            try {
                return SSLContext.getInstance("TLSv1.2");
            } catch (NoSuchAlgorithmException e) {
                // fallback to TLS
            }
        }

        try {
            return SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No TLS provider", e);
        }
    }

    public TrustRootIndex buildTrustRootIndex(X509TrustManager trustManager) {
        return new BasicTrustRootIndex(trustManager.getAcceptedIssuers());
    }

    public void configureSslSocketFactory(SSLSocketFactory socketFactory) {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
