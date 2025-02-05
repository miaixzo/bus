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
package org.miaixz.bus.http.accord;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.UnknownServiceException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

import org.miaixz.bus.http.metric.Internal;

/**
 * 处理连接规范回退策略:当安全套接字连接由于握手/协议问题而失败时， 可能会使用不同的协议重试连接。实例是有状态的，应该创建并用于单个连接尝试
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConnectionSelector {

    private final List<ConnectionSuite> connectionSuites;
    private int nextModeIndex;
    private boolean isFallbackPossible;
    private boolean isFallback;

    public ConnectionSelector(List<ConnectionSuite> connectionSuites) {
        this.nextModeIndex = 0;
        this.connectionSuites = connectionSuites;
    }

    /**
     * 根据{@link SSLSocket} 配置连接到指定的主机的信息{@link ConnectionSuite} 返回{@link ConnectionSuite}，不会返回{@code null}
     *
     * @param sslSocket ssl套接字
     * @return 套接字连接的配置
     * @throws IOException 如果套接字不支持任何可用的TLS模式
     */
    public ConnectionSuite configureSecureSocket(SSLSocket sslSocket) throws IOException {
        ConnectionSuite tlsConfiguration = null;
        for (int i = nextModeIndex, size = connectionSuites.size(); i < size; i++) {
            ConnectionSuite connectionSuite = connectionSuites.get(i);
            if (connectionSuite.isCompatible(sslSocket)) {
                tlsConfiguration = connectionSuite;
                nextModeIndex = i + 1;
                break;
            }
        }

        if (null == tlsConfiguration) {
            // 这可能是第一次尝试连接，而套接字不支持任何必需的协议
            // 或者可能是重试(但此套接字支持的协议比先前的套接字所建议的少)
            throw new UnknownServiceException(
                    "Unable to find acceptable protocols. isFallback=" + isFallback + ", modes=" + connectionSuites
                            + ", supported protocols=" + Arrays.toString(sslSocket.getEnabledProtocols()));
        }

        isFallbackPossible = isFallbackPossible(sslSocket);

        Internal.instance.apply(tlsConfiguration, sslSocket, isFallback);

        return tlsConfiguration;
    }

    /**
     * 报告连接失败。确定下一个要尝试的{@link ConnectionSuite}(如果有的话)
     *
     * @param ex 异常信息
     * @return 如果需要使用 {@link #configureSecureSocket(SSLSocket)} 或{@code false}重试连接，
     *         则为{@code true};如果不需要重试连接，则为{@link #configureSecureSocket(SSLSocket)}或{@code false}
     */
    public boolean connectionFailed(IOException ex) {
        // 未来使用此策略进行连接的任何尝试都将是一次回退尝试
        isFallback = true;

        if (!isFallbackPossible) {
            return false;
        }

        // 如果有协议问题，不会恢复.
        if (ex instanceof ProtocolException) {
            return false;
        }

        // 如果出现中断或超时(SocketTimeoutException)，则不进行恢复。对于套接字连接超时情况，
        // 我们不会使用不同的ConnectionSpec尝试相同的主机:认为通讯是不可到达的
        if (ex instanceof InterruptedIOException) {
            return false;
        }

        // 查找已知的客户端或协商错误，这些错误不太可能通过再次尝试使用不同的连接规范来修复
        if (ex instanceof SSLHandshakeException) {
            // 如果问题是来自X509TrustManager的一个证书异常，那么不会重试.
            if (ex.getCause() instanceof CertificateException) {
                return false;
            }
        }
        if (ex instanceof SSLPeerUnverifiedException) {
            // 例如，证书未经许可的错误.
            return false;
        }

        // 重试所有其他SSL失败
        return ex instanceof SSLException;
    }

    /**
     * 如果根据提供的{@link SSLSocket}，回退策略中的任何后面的{@link ConnectionSuite} 看起来都是可能的，则返回{@code true}。假设具有与提供的套接字相同的功能
     */
    private boolean isFallbackPossible(SSLSocket socket) {
        for (int i = nextModeIndex; i < connectionSuites.size(); i++) {
            if (connectionSuites.get(i).isCompatible(socket)) {
                return true;
            }
        }
        return false;
    }

}
