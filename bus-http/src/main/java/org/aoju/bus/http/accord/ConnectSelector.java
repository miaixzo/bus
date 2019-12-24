package org.aoju.bus.http.accord;

import org.aoju.bus.http.Internal;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.UnknownServiceException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

/**
 * 处理连接规范回退策略:当安全套接字连接由于握手/协议问题而失败时，
 * 可能会使用不同的协议重试连接。实例是有状态的，应该创建并用于单个连接尝试
 */
public final class ConnectSelector {

    private final List<ConnectSuite> connectSuites;
    private int nextModeIndex;
    private boolean isFallbackPossible;
    private boolean isFallback;

    public ConnectSelector(List<ConnectSuite> connectSuites) {
        this.nextModeIndex = 0;
        this.connectSuites = connectSuites;
    }

    /**
     * 根据{@link SSLSocket} 配置连接到指定的主机的信息{@link ConnectSuite}
     * 返回{@link ConnectSuite}，不会返回{@code null}
     *
     * @param sslSocket ssl套接字
     * @return 套接字连接的配置
     * @throws IOException 如果套接字不支持任何可用的TLS模式
     */
    public ConnectSuite configureSecureSocket(SSLSocket sslSocket) throws IOException {
        ConnectSuite tlsConfiguration = null;
        for (int i = nextModeIndex, size = connectSuites.size(); i < size; i++) {
            ConnectSuite connectSuite = this.connectSuites.get(i);
            if (connectSuite.isCompatible(sslSocket)) {
                tlsConfiguration = connectSuite;
                nextModeIndex = i + 1;
                break;
            }
        }

        if (tlsConfiguration == null) {
            // 这可能是第一次尝试连接，而套接字不支持任何必需的协议
            // 或者可能是重试(但此套接字支持的协议比先前的套接字所建议的少)
            throw new UnknownServiceException(
                    "Unable to find acceptable protocols. isFallback=" + isFallback
                            + ", modes=" + connectSuites
                            + ", supported protocols=" + Arrays.toString(sslSocket.getEnabledProtocols()));
        }

        isFallbackPossible = isFallbackPossible(sslSocket);

        Internal.instance.apply(tlsConfiguration, sslSocket, isFallback);

        return tlsConfiguration;
    }

    /**
     * 报告连接失败。确定下一个要尝试的{@link ConnectSuite}(如果有的话)
     *
     * @param ex 异常信息
     * @return 如果需要使用 {@link #configureSecureSocket(SSLSocket)} 或{@code false}重试连接，
     * 则为{@code true};如果不需要重试连接，则为{@link #configureSecureSocket(SSLSocket)}或{@code false}
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

        // 在Android上，SSLProtocolExceptions可能由TLS_FALLBACK_SCSV失败引起，
        // 这意味着我们在可能不应该重试的时候重试
        return (ex instanceof SSLHandshakeException
                || ex instanceof SSLProtocolException
                || ex instanceof SSLException);
    }

    /**
     * 如果根据提供的{@link SSLSocket}，回退策略中的任何后面的{@link ConnectSuite}
     * 看起来都是可能的，则返回{@code true}。假设具有与提供的套接字相同的功能
     */
    private boolean isFallbackPossible(SSLSocket socket) {
        for (int i = nextModeIndex; i < connectSuites.size(); i++) {
            if (connectSuites.get(i).isCompatible(socket)) {
                return true;
            }
        }
        return false;
    }

}
