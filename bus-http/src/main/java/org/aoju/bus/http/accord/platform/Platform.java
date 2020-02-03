/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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

import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.http.Protocol;
import org.aoju.bus.http.secure.BasicCertificateChainCleaner;
import org.aoju.bus.http.secure.BasicTrustRootIndex;
import org.aoju.bus.http.secure.CertificateChainCleaner;
import org.aoju.bus.http.secure.TrustRootIndex;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.logger.level.Level;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * 访问特定于平台的特性.
 * 服务器名称指示(SNI)
 * 支持Android 2.3+
 * 支持Android 4.0+.
 * 支持Android 5.0+
 * 支持 OpenJDK 7+
 * 支持 OpenJDK 7 and 8 (via the JettyALPN-boot library)
 * 支持OpenJDK 9  SSLParameters和SSLSocket特性
 * 升级到Android 2.3+和OpenJDK 7+。没有用于恢复用于
 * 创建{@link SSLSocketFactory}的trustmanager的公共api
 * 支持Android 6.0+ {@code NetworkSecurityPolicy}
 *
 * @author Kimi Liu
 * @version 5.5.5
 * @since JDK 1.8+
 */
public class Platform {

    public static final int INFO = 4;
    public static final int WARN = 5;
    private static final Platform PLATFORM = findPlatform();

    public static Platform get() {
        return PLATFORM;
    }

    public static List<String> alpnProtocolNames(List<Protocol> protocols) {
        List<String> names = new ArrayList<>(protocols.size());
        for (int i = 0, size = protocols.size(); i < size; i++) {
            Protocol protocol = protocols.get(i);
            if (protocol == Protocol.HTTP_1_0) {
                // No HTTP/1.0 for ALPN
                continue;
            }
            names.add(protocol.toString());
        }
        return names;
    }

    /**
     * 尝试将主机运行时与有能力的平台实现匹配
     *
     * @return 平台信息
     */
    private static Platform findPlatform() {
        Platform android = AndroidPlatform.buildIfSupported();

        if (android != null) {
            return android;
        }

        Platform jdk9 = Jdk9Platform.buildIfSupported();

        if (jdk9 != null) {
            return jdk9;
        }

        Platform jdkWithJettyBoot = JdkWithJettyBootPlatform.buildIfSupported();

        if (jdkWithJettyBoot != null) {
            return jdkWithJettyBoot;
        }

        // 可能是像OpenJDK和Oracle JDK
        return new Platform();
    }

    /**
     * 返回以8位长度为前缀的协议名的连接
     *
     * @param protocols 协议信息
     * @return 8位长度为前缀的协议名的连接
     */
    static byte[] concatLengthPrefixed(List<Protocol> protocols) {
        Buffer result = new Buffer();
        for (int i = 0, size = protocols.size(); i < size; i++) {
            Protocol protocol = protocols.get(i);
            if (protocol == Protocol.HTTP_1_0) {
                // No HTTP/1.0 for ALPN.
                continue;
            }
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

        // 没有找到我们想要的地方。作为最后的尝试，请尝试查找委托上的值
        if (!fieldName.equals("delegate")) {
            Object delegate = readFieldOrNull(instance, Object.class, "delegate");
            if (delegate != null) return readFieldOrNull(delegate, fieldType, fieldName);
        }

        return null;
    }

    /**
     * 自定义头文件中使用的前缀
     *
     * @return 前缀
     */
    public String getPrefix() {
        return "Httpd";
    }

    /**
     * 管理哪些X509证书可用于对安全套接字的远程端进行身份验证。
     * 决策可能基于可信的证书颁发机构、证书撤销列表、在线状态检查或其他方法
     *
     * @param sslSocketFactory 安全套接字工厂
     * @return 信任证书管理器
     */
    protected X509TrustManager trustManager(SSLSocketFactory sslSocketFactory) {
        // 尝试从OpenJDK套接字工厂获取信任管理器。为了支持Robolectric，我们尝试在所有平台上都这样做，
        // Robolectric混合了来自Android和Oracle JDK的类。注意，我们不支持HTTP/2或其他Robolectric上的好特性
        try {
            // 创建SSLContext对象，并使用我们指定的信任证书管理器初始化
            Class<?> sslContextClass = Class.forName("sun.security.ssl.SSLContextImpl");
            Object context = readFieldOrNull(sslSocketFactory, sslContextClass, "context");
            if (context == null) return null;
            return readFieldOrNull(context, X509TrustManager.class, "trustManager");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * 在{@code sslSocket}上为{@code route}配置TLS扩展
     *
     * @param sslSocket 套接字信息
     * @param hostname  客户端握手不为空;服务器端握手为空.
     * @param protocols 服务协议
     */
    public void configureTlsExtensions(SSLSocket sslSocket, String hostname,
                                       List<Protocol> protocols) {
    }

    /**
     * 在TLS握手后调用，以释放由{@link #configureTlsExtensions}分配的资源
     *
     * @param sslSocket 安全套接字
     */
    public void afterHandshake(SSLSocket sslSocket) {

    }

    /**
     * 返回协商的协议，如果没有协商协议，则返回null
     *
     * @param socket 套接字
     * @return 协议
     */
    public String getSelectedProtocol(SSLSocket socket) {
        return null;
    }

    public void connectSocket(Socket socket, InetSocketAddress address, int connectTimeout)
            throws IOException {
        socket.connect(address, connectTimeout);
    }

    public boolean isCleartextTrafficPermitted(String hostname) {
        return true;
    }

    /**
     * 返回一个对象，该对象持有在执行此方法时创建的堆栈跟踪。
     * 用于{@link java.io.Closeable}与{@link #logCloseableLeak(String, Object)}
     *
     * @param closer 闭合器
     * @return 返回一个对象
     */
    public Object getStackTraceForCloseable(String closer) {
        if (Logger.get().isEnabled(Level.DEBUG)) {
            return new Throwable(closer);
        }
        return null;
    }

    public void logCloseableLeak(String message, Object stackTrace) {
        if (stackTrace == null) {
            message += " To see where this was allocated, set the Httpd logger level to FINE: "
                    + "Logger.getLogger(Httpd.class.getName()).setLevel(Level.DEBUG);";
        }
        Logger.warn(message, stackTrace);
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
                // JDK 1.7(公共版本)只支持带命名协议的> TLSv1
                return SSLContext.getInstance("TLSv1.2");
            } catch (NoSuchAlgorithmException e) {
                Logger.error(e);
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
