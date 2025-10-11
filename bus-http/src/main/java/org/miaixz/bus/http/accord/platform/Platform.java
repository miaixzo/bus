/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.accord.platform;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.http.secure.BasicCertificateChainCleaner;
import org.miaixz.bus.http.secure.BasicTrustRootIndex;
import org.miaixz.bus.http.secure.CertificateChainCleaner;
import org.miaixz.bus.http.secure.TrustRootIndex;
import org.miaixz.bus.logger.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to platform-specific features.
 * <p>
 * This class abstracts away differences in Java and Android runtimes, providing a consistent API for features like TLS
 * extensions (SNI, ALPN), trust manager extraction, and network security policy checks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Platform {

    private static final Platform PLATFORM = findPlatform();

    /**
     * Returns the platform-specific implementation.
     *
     * @return The current platform.
     */
    public static Platform get() {
        return PLATFORM;
    }

    /**
     * Converts a list of {@link Protocol} objects to a list of their string representations.
     *
     * @param protocols The list of protocols.
     * @return A list of protocol names.
     */
    public static List<String> alpnProtocolNames(List<Protocol> protocols) {
        List<String> names = new ArrayList<>(protocols.size());
        for (int i = 0, size = protocols.size(); i < size; i++) {
            Protocol protocol = protocols.get(i);
            if (protocol == Protocol.HTTP_1_0) {
                continue; // No HTTP/1.0 for ALPN.
            }
            names.add(protocol.toString());
        }
        return names;
    }

    /**
     * Attempts to match the host runtime with a capable platform implementation.
     *
     * @return The platform implementation.
     */
    private static Platform findPlatform() {
        Platform jdk = JdkPlatform.buildIfSupported();
        if (jdk != null) {
            return jdk;
        }
        // Probably an Oracle JDK like OpenJDK.
        return new Platform();
    }

    /**
     * Returns the concatenation of 8-bit, length-prefixed protocol names.
     * http://tools.ietf.org/html/draft-agl-tls-nextprotoneg-04#page-4
     *
     * @param protocols The list of protocols.
     * @return The concatenated, length-prefixed protocol names.
     */
    static byte[] concatLengthPrefixed(List<Protocol> protocols) {
        Buffer result = new Buffer();
        for (int i = 0, size = protocols.size(); i < size; i++) {
            Protocol protocol = protocols.get(i);
            if (protocol == Protocol.HTTP_1_0) {
                continue; // No HTTP/1.0 for ALPN.
            }
            result.writeByte(protocol.toString().length());
            result.writeUtf8(protocol.toString());
        }
        return result.readByteArray();
    }

    /**
     * Reads a field from an object via reflection, returning null if the field is not found or not accessible.
     *
     * @param instance  The object instance.
     * @param fieldType The type of the field.
     * @param fieldName The name of the field.
     * @param <T>       The field type.
     * @return The field value, or null.
     */
    static <T> T readFieldOrNull(Object instance, Class<T> fieldType, String fieldName) {
        for (Class<?> c = instance.getClass(); c != Object.class; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(instance);
                if (value == null || !fieldType.isInstance(value))
                    return null;
                return fieldType.cast(value);
            } catch (NoSuchFieldException ignored) {
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            }
        }

        // Try to find the value on a delegate, as a last resort.
        if (!fieldName.equals("delegate")) {
            Object delegate = readFieldOrNull(instance, Object.class, "delegate");
            if (delegate != null)
                return readFieldOrNull(delegate, fieldType, fieldName);
        }

        return null;
    }

    /**
     * Returns the prefix used in custom headers.
     *
     * @return The prefix string.
     */
    public String getPrefix() {
        return "Httpd";
    }

    /**
     * Manages which X.509 certificates can be used to authenticate the remote end of a secure socket. Decisions may be
     * based on trusted certificate authorities, certificate revocation lists, online status checks, or other methods.
     *
     * @param sslSocketFactory The SSL socket factory.
     * @return The trust manager.
     */
    protected X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
        // Attempt to get the trust manager from an OpenJDK socket factory. This is necessary to support
        // Robolectric, which mixes classes from Android and Oracle JDKs. Note that we don't support HTTP/2
        // or other nice features on Robolectric.
        try {
            // Create an SSLContext object and initialize it with our specified trust manager.
            Class<?> sslContextClass = Class.forName("sun.security.ssl.SSLContextImpl");
            Object context = readFieldOrNull(sslSocketFactory, sslContextClass, "context");
            if (context == null)
                return null;
            return readFieldOrNull(context, X509TrustManager.class, "trustManager");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Configures TLS extensions on the {@code sslSocket} for the given {@code route}.
     *
     * @param sslSocket The SSL socket.
     * @param hostname  The hostname for client-side handshakes; null for server-side.
     * @param protocols The supported protocols.
     */
    public void configureTlsExtensions(SSLSocket sslSocket, String hostname, List<Protocol> protocols) {
    }

    /**
     * Called after the TLS handshake to release any resources allocated by {@link #configureTlsExtensions}.
     *
     * @param sslSocket The SSL socket.
     */
    public void afterHandshake(SSLSocket sslSocket) {
    }

    /**
     * Returns the negotiated protocol, or null if no protocol was negotiated.
     *
     * @param socket The socket.
     * @return The negotiated protocol.
     */
    public String getSelectedProtocol(SSLSocket socket) {
        return null;
    }

    /**
     * Connects the given socket to the specified address.
     *
     * @param socket         The socket to connect.
     * @param address        The address to connect to.
     * @param connectTimeout The connection timeout in milliseconds.
     * @throws IOException if an I/O error occurs.
     */
    public void connectSocket(Socket socket, InetSocketAddress address, int connectTimeout) throws IOException {
        socket.connect(address, connectTimeout);
    }

    /**
     * Returns true if cleartext traffic is permitted for the given hostname.
     *
     * @param hostname The hostname to check.
     * @return {@code true} if cleartext traffic is permitted.
     */
    public boolean isCleartextTrafficPermitted(String hostname) {
        return true;
    }

    /**
     * Returns an object that holds a stack trace created at the time this method was called. Used for debugging
     * {@link java.io.Closeable} leaks with {@link #logCloseableLeak(String, Object)}.
     *
     * @param closer A string describing the closeable resource.
     * @return An object holding the stack trace.
     */
    public Object getStackTraceForCloseable(String closer) {
        if (Logger.isDebugEnabled()) {
            return new Throwable(closer);
        }
        return null;
    }

    /**
     * Logs a message about a leaked closeable resource.
     *
     * @param message    The message to log.
     * @param stackTrace An object holding the stack trace, created by {@link #getStackTraceForCloseable(String)}.
     */
    public void logCloseableLeak(String message, Object stackTrace) {
        if (stackTrace == null) {
            message += " To see where this was allocated, set the Httpd logger level to DEBUG: "
                    + "Logger.getLogger(Httpd.class.getName()).setLevel(Level.DEBUG);";
        }
        Logger.warn(message, stackTrace);
    }

    /**
     * Builds a certificate chain cleaner for the given trust manager.
     *
     * @param trustManager The trust manager.
     * @return A certificate chain cleaner.
     */
    public CertificateChainCleaner buildCertificateChainCleaner(X509TrustManager trustManager) {
        return new BasicCertificateChainCleaner(buildTrustRootIndex(trustManager));
    }

    /**
     * Builds a certificate chain cleaner for the given SSL socket factory.
     *
     * @param sslSocketFactory The SSL socket factory.
     * @return A certificate chain cleaner.
     */
    public CertificateChainCleaner buildCertificateChainCleaner(SSLSocketFactory sslSocketFactory) {
        X509TrustManager trustManager = trustManager(sslSocketFactory);
        if (trustManager == null) {
            throw new IllegalStateException("Unable to extract the trust manager on " + Platform.get()
                    + ", sslSocketFactory is " + sslSocketFactory.getClass());
        }
        return buildCertificateChainCleaner(trustManager);
    }

    /**
     * Builds a trust root index for the given trust manager.
     *
     * @param trustManager The trust manager.
     * @return A trust root index.
     */
    public TrustRootIndex buildTrustRootIndex(X509TrustManager trustManager) {
        return new BasicTrustRootIndex(trustManager.getAcceptedIssuers());
    }

    /**
     * Configures the given SSL socket factory.
     *
     * @param socketFactory The SSL socket factory to configure.
     */
    public void configureSslSocketFactory(SSLSocketFactory socketFactory) {

    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
