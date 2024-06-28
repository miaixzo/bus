/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.metric;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.galaxy.Material;
import org.miaixz.bus.image.metric.internal.net.TCPHandler;
import org.miaixz.bus.image.metric.internal.net.TCPListener;
import org.miaixz.bus.image.metric.internal.net.UDPHandler;
import org.miaixz.bus.image.metric.internal.net.UDPListener;
import org.miaixz.bus.logger.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Connection implements Serializable {

    public static final int NO_TIMEOUT = 0;
    public static final int SYNCHRONOUS_MODE = 1;
    public static final int NOT_LISTENING = -1;
    public static final int DEF_BACKLOG = 50;
    public static final int DEF_SOCKETDELAY = 50;
    public static final int DEF_BUFFERSIZE = 0;
    public static final int DEF_MAX_PDU_LENGTH = 16378;
    public static final String TLS_RSA_WITH_NULL_SHA = "SSL_RSA_WITH_NULL_SHA";
    public static final String TLS_RSA_WITH_3DES_EDE_CBC_SHA = "SSL_RSA_WITH_3DES_EDE_CBC_SHA";
    // 适应SunJSSE TLS应用程序数据长度16408
    public static final String TLS_RSA_WITH_AES_128_CBC_SHA = "TLS_RSA_WITH_AES_128_CBC_SHA";
    public static final String[] DEFAULT_TLS_PROTOCOLS = {"TLSv1.2", "TLSv1.1", "TLSv1"};
    private static final EnumMap<Protocol, TCPHandler> tcpHandlers = new EnumMap<>(Protocol.class);
    private static final EnumMap<Protocol, UDPHandler> udpHandlers = new EnumMap<>(Protocol.class);

    static {
        registerTCPProtocolHandler(Protocol.DICOM, AdvancedHandler.INSTANCE);
    }

    /**
     * 设备信息
     */
    private Device device;
    /**
     * 网络连接对象的任意名称
     */
    private String commonName;
    /**
     * 主机名的字符串
     */
    private String hostname;
    /**
     * 套接字绑定地址
     */
    private String bindAddress;
    /**
     * 客户端套接字绑定地址
     */
    private String clientBindAddress;
    /**
     * HTTP代理
     */
    private String httpProxy;
    /**
     * TCP端口
     */
    private int port = NOT_LISTENING;
    /**
     * 积压日志
     */
    private int backlog = DEF_BACKLOG;
    /**
     * 链接超时时间
     */
    private int connectTimeout;
    /**
     * 请求超时时间
     */
    private int requestTimeout;
    /**
     * 接受超时时间
     */
    private int acceptTimeout;
    /**
     * 释放超时时间
     */
    private int releaseTimeout;
    /**
     * 响应超时时间
     */
    private int responseTimeout;
    /**
     * 回收超时时间
     */
    private int retrieveTimeout;
    /**
     * 检索超时总计
     */
    private boolean retrieveTimeoutTotal;
    /**
     * 空闲超时
     */
    private int idleTimeout;
    /**
     * 套接字关闭的延迟时间
     */
    private int socketCloseDelay = DEF_SOCKETDELAY;
    /**
     * 发送缓冲区大小
     */
    private int sendBufferSize;
    /**
     * 收到缓冲区大小
     */
    private int receiveBufferSize;
    /**
     * 发送PDU长度
     */
    private int sendPDULength = DEF_MAX_PDU_LENGTH;
    /**
     * 接收PDU长度
     */
    private int receivePDULength = DEF_MAX_PDU_LENGTH;
    /**
     * 执行的最大操作数
     */
    private int maxOpsPerformed = SYNCHRONOUS_MODE;
    /**
     * 调用的最大操作数
     */
    private int maxOpsInvoked = SYNCHRONOUS_MODE;
    private boolean packPDV = true;
    /**
     * TCP无延迟
     */
    private boolean tcpNoDelay = true;
    /**
     * TLS是否需要客户端验证
     */
    private boolean tlsNeedClientAuth = true;
    /**
     * TLS 密码套件
     */
    private String[] tlsCipherSuites = {};
    /**
     * TLS 协议信息
     */
    private String[] tlsProtocols = DEFAULT_TLS_PROTOCOLS;
    /**
     * 忽略的IP地址列表
     */
    private String[] blacklist = {};
    /**
     * 是否安装网络连接
     */
    private Boolean installed;
    /**
     * 协议信息
     */
    private Protocol protocol = Protocol.DICOM;
    /**
     * 黑名单地址
     */
    private transient List<InetAddress> blacklistAddrs;
    /**
     * 主机地址
     */
    private transient InetAddress hostAddr;
    /**
     * 绑定地址
     */
    private transient InetAddress bindAddr;
    /**
     * 客户端绑定地址
     */
    private transient InetAddress clientBindAddr;
    /**
     * 监听器
     */
    private transient volatile SocketListener listener;
    /**
     * 重新绑定需要
     */
    private transient boolean rebindNeeded;

    public Connection() {
    }

    public Connection(String commonName, String hostname) {
        this(commonName, hostname, NOT_LISTENING);
    }

    public Connection(String commonName, String hostname, int port) {
        this.commonName = commonName;
        this.hostname = hostname;
        this.port = port;
    }

    public static TCPHandler registerTCPProtocolHandler(
            Protocol protocol, TCPHandler handler) {
        return tcpHandlers.put(protocol, handler);
    }

    public static TCPHandler unregisterTCPProtocolHandler(
            Protocol protocol) {
        return tcpHandlers.remove(protocol);
    }

    public static UDPHandler registerUDPProtocolHandler(
            Protocol protocol, UDPHandler handler) {
        return udpHandlers.put(protocol, handler);
    }

    public static UDPHandler unregisterUDPProtocolHandler(
            Protocol protocol) {
        return udpHandlers.remove(protocol);
    }

    private static String[] intersect(String[] ss1, String[] ss2) {
        String[] ss = new String[Math.min(ss1.length, ss2.length)];
        int len = 0;
        for (String s1 : ss1)
            for (String s2 : ss2)
                if (s1.equals(s2)) {
                    ss[len++] = s1;
                    break;
                }
        if (len == ss.length)
            return ss;

        String[] dest = new String[len];
        System.arraycopy(ss, 0, dest, 0, len);
        return dest;
    }

    /**
     * 获取此网络连接所属的Device对象
     *
     * @return 设备信息
     */
    public final Device getDevice() {
        return device;
    }

    /**
     * 设置此网络连接所属的设备对象
     *
     * @param device 所属设备对象
     */
    public final void setDevice(Device device) {
        if (null != device && null != this.device)
            throw new IllegalStateException("already owned by " + device);
        this.device = device;
    }

    /**
     * 这是此特定连接的DNS名称
     * 用于获取连接的当前IP地址主机名必须具有足够的资格
     * 对于任何客户端DNS用户而言都是明确的
     *
     * @return 包含主机名的字符串
     */
    public final String getHostname() {
        return hostname;
    }

    /**
     * 这是此特定连接的DNS名称
     * 用于获取连接的当前IP地址，主机名必须具有足够的资格
     * 对于任何客户端DNS用户而言都是明确的
     *
     * @param hostname 包含主机名的字符串
     */
    public final void setHostname(String hostname) {
        if (null != hostname
                ? hostname.equals(this.hostname)
                : null == this.hostname)
            return;

        this.hostname = hostname;
        needRebind();
    }

    /**
     * 监听套接字的绑定地址或{@code null}如果{@code null}，则将
     * 侦听套接字绑定到{@link #getHostname()} 这是默认值
     *
     * @return 连接的绑定地址或{@code null}
     */
    public final String getBindAddress() {
        return bindAddress;
    }

    /**
     * 监听套接字的绑定地址或{@code null} 如果{@code null}，
     * 则将侦听套接字绑定到{@link #getHostname()}
     *
     * @param bindAddress 监听套接字的绑定地址或{@code null}
     */
    public final void setBindAddress(String bindAddress) {
        if (null != bindAddress
                ? bindAddress.equals(this.bindAddress)
                : null == this.bindAddress)
            return;

        this.bindAddress = bindAddress;
        this.bindAddr = null;
        needRebind();
    }

    /**
     * 传出连接的绑定地址，{@code "0.0.0.0"} 或{@code null}
     * 如果{@code "0.0.0.0"}，系统将选择任何本地IP进行传出连接
     * 如果{@code null}，则将传出连接绑定到 {@link #getHostname()}
     *
     * @return 字符串
     */
    public String getClientBindAddress() {
        return clientBindAddress;
    }

    /**
     * 传出连接的绑定地址， {@code "0.0.0.0"}或{@code null}
     * 如果{@code "0.0.0.0"}，系统将选择任何本地IP进行传出*连接
     * 如果{@code null}，则将传出连接绑定到 {@link #getHostname()}
     *
     * @param bindAddress 传出连接的绑定地址或{@code null}
     */
    public void setClientBindAddress(String bindAddress) {
        if (null != bindAddress
                ? bindAddress.equals(this.clientBindAddress)
                : null == this.clientBindAddress)
            return;

        this.clientBindAddress = bindAddress;
        this.clientBindAddr = null;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        if (null == protocol)
            throw new NullPointerException();

        if (this.protocol == protocol)
            return;

        this.protocol = protocol;
        needRebind();
    }

    public boolean isRebindNeeded() {
        return rebindNeeded;
    }

    public void needRebind() {
        this.rebindNeeded = true;
    }

    /**
     * 网络连接对象的任意名称可以是一个有意义的*名称或任何唯一的字符序列
     *
     * @return 包含名称的字符串
     */
    public final String getCommonName() {
        return commonName;
    }

    /**
     * 网络连接对象的任意名称可以是一个有意义的*名称或任何唯一的字符序列
     *
     * @param name 包含名称的字符串
     */
    public final void setCommonName(String name) {
        this.commonName = name;
    }

    /**
     * AE正在侦听的TCP端口，或-1表示仅启动关联的网络连接
     *
     * @return 包含端口号或-1
     */
    public final int getPort() {
        return port;
    }

    /**
     * AE正在侦听的TCP端口，或仅用于启动关联的网络连接
     * 有效的端口值在0到65535之间
     *
     * @param port 端口号或-1
     */
    public final void setPort(int port) {
        if (this.port == port)
            return;

        if ((port <= 0 || port > 0xFFFF) && port != NOT_LISTENING)
            throw new IllegalArgumentException("port out of range:" + port);

        this.port = port;
        needRebind();
    }

    public final String getHttpProxy() {
        return httpProxy;
    }

    public final void setHttpProxy(String proxy) {
        this.httpProxy = proxy;
    }

    public final boolean useHttpProxy() {
        return null != httpProxy;
    }

    public final boolean isServer() {
        return port > 0;
    }

    public final int getBacklog() {
        return backlog;
    }

    public final void setBacklog(int backlog) {
        if (this.backlog == backlog)
            return;

        if (backlog < 1)
            throw new IllegalArgumentException("backlog: " + backlog);

        this.backlog = backlog;
        needRebind();
    }

    public final int getConnectTimeout() {
        return connectTimeout;
    }

    public final void setConnectTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.connectTimeout = timeout;
    }

    /**
     * 接收A-ASSOCIATE-RQ的超时时间，默认为5000
     *
     * @return the int
     */
    public final int getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * 接收A-ASSOCIATE-RQ的超时时间，默认为5000
     *
     * @param timeout 一个包含毫秒的int值
     */
    public final void setRequestTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.requestTimeout = timeout;
    }

    public final int getAcceptTimeout() {
        return acceptTimeout;
    }

    public final void setAcceptTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.acceptTimeout = timeout;
    }


    /**
     * 接收A-RELEASE-RP的超时时间，默认为5000
     *
     * @return 一个包含毫秒的int值
     */
    public final int getReleaseTimeout() {
        return releaseTimeout;
    }

    /**
     * 接收A-RELEASE-RP的超时时间，默认为5000
     *
     * @param timeout 一个包含毫秒的int值
     */
    public final void setReleaseTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.releaseTimeout = timeout;
    }

    /**
     * 发送A-ABORT后，套接字关闭的延迟时间(以毫秒为单位)，默认为50毫秒
     *
     * @return 一个包含毫秒的int值
     */
    public final int getSocketCloseDelay() {
        return socketCloseDelay;
    }

    /**
     * 发送A-ABORT后，套接字关闭的延迟时间(以毫秒为单位)，默认为50毫秒
     *
     * @param delay 一个包含毫秒的int值
     */
    public final void setSocketCloseDelay(int delay) {
        if (delay < 0)
            throw new IllegalArgumentException("delay: " + delay);
        this.socketCloseDelay = delay;
    }

    public final int getResponseTimeout() {
        return responseTimeout;
    }

    public final void setResponseTimeout(int timeout) {
        this.responseTimeout = timeout;
    }

    public final int getRetrieveTimeout() {
        return retrieveTimeout;
    }

    public final void setRetrieveTimeout(int timeout) {
        this.retrieveTimeout = timeout;
    }

    public final boolean isRetrieveTimeoutTotal() {
        return retrieveTimeoutTotal;
    }

    public final void setRetrieveTimeoutTotal(boolean retrieveTimeoutTotal) {
        this.retrieveTimeoutTotal = retrieveTimeoutTotal;
    }

    public final int getIdleTimeout() {
        return idleTimeout;
    }

    public final void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     * 此特定连接上支持的TLS CipherSuite
     * TLS CipherSuites必须使用RFC-2246字符串
     * 表示形式进行描述(例如“ SSL_RSA_WITH_3DES_EDE_CBC_SHA")
     *
     * @return 包含受支持的密码套件的String数组
     */
    public String[] getTlsCipherSuites() {
        return tlsCipherSuites;
    }

    /**
     * 此特定连接上支持的TLS CipherSuite
     * TLS CipherSuites必须使用RFC-2246字符串
     * 表示形式进行描述(例如"SSL_RSA_WITH_3DES_EDE_CBC_SHA")
     *
     * @param tlsCipherSuites 包含受支持的密码套件的String数组
     */
    public void setTlsCipherSuites(String... tlsCipherSuites) {
        if (Arrays.equals(this.tlsCipherSuites, tlsCipherSuites))
            return;

        this.tlsCipherSuites = tlsCipherSuites;
        needRebind();
    }

    public final boolean isTls() {
        return tlsCipherSuites.length > 0;
    }

    public final String[] getTlsProtocols() {
        return tlsProtocols;
    }

    public final void setTlsProtocols(String... tlsProtocols) {
        if (Arrays.equals(this.tlsProtocols, tlsProtocols))
            return;

        this.tlsProtocols = tlsProtocols;
        needRebind();
    }

    public final boolean isTlsNeedClientAuth() {
        return tlsNeedClientAuth;
    }

    public final void setTlsNeedClientAuth(boolean tlsNeedClientAuth) {
        if (this.tlsNeedClientAuth == tlsNeedClientAuth)
            return;

        this.tlsNeedClientAuth = tlsNeedClientAuth;
        needRebind();
    }

    /**
     * 获取以KB为单位的SO_RCVBUF套接字值
     *
     * @return 一个包含缓冲区大小(以KB为单位)
     */
    public final int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    /**
     * 将SO_RCVBUF套接字选项设置为以KB为单位的指定值
     *
     * @param size 一个包含缓冲区大小(以KB为单位)
     */
    public final void setReceiveBufferSize(int size) {
        if (size < 0)
            throw new IllegalArgumentException("size: " + size);
        this.receiveBufferSize = size;
    }

    private void setReceiveBufferSize(Socket s) throws SocketException {
        int size = s.getReceiveBufferSize();
        if (receiveBufferSize == 0) {
            receiveBufferSize = size;
        } else if (receiveBufferSize != size) {
            s.setReceiveBufferSize(receiveBufferSize);
            receiveBufferSize = s.getReceiveBufferSize();
        }
    }

    public void setReceiveBufferSize(ServerSocket ss) throws SocketException {
        int size = ss.getReceiveBufferSize();
        if (receiveBufferSize == 0) {
            receiveBufferSize = size;
        } else if (receiveBufferSize != size) {
            ss.setReceiveBufferSize(receiveBufferSize);
            receiveBufferSize = ss.getReceiveBufferSize();
        }
    }

    public void setReceiveBufferSize(DatagramSocket ds) throws SocketException {
        int size = ds.getReceiveBufferSize();
        if (receiveBufferSize == 0) {
            receiveBufferSize = size;
        } else if (receiveBufferSize != size) {
            ds.setReceiveBufferSize(receiveBufferSize);
            receiveBufferSize = ds.getReceiveBufferSize();
        }
    }

    /**
     * 获取以KB为单位的SO_SNDBUF套接字选项值
     *
     * @return 一个包含缓冲区大小(以KB为单位)
     */
    public final int getSendBufferSize() {
        return sendBufferSize;
    }

    /**
     * 将SO_SNDBUF套接字选项设置为以KB为单位的指定值
     *
     * @param size 一个包含缓冲区大小(以KB为单位)
     */
    public final void setSendBufferSize(int size) {
        if (size < 0)
            throw new IllegalArgumentException("size: " + size);
        this.sendBufferSize = size;
    }

    public final int getSendPDULength() {
        return sendPDULength;
    }

    public final void setSendPDULength(int sendPDULength) {
        this.sendPDULength = sendPDULength;
    }

    public final int getReceivePDULength() {
        return receivePDULength;
    }

    public final void setReceivePDULength(int receivePDULength) {
        this.receivePDULength = receivePDULength;
    }

    public final int getMaxOpsPerformed() {
        return maxOpsPerformed;
    }

    public final void setMaxOpsPerformed(int maxOpsPerformed) {
        this.maxOpsPerformed = maxOpsPerformed;
    }

    public final int getMaxOpsInvoked() {
        return maxOpsInvoked;
    }

    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    public final boolean isPackPDV() {
        return packPDV;
    }

    public final void setPackPDV(boolean packPDV) {
        this.packPDV = packPDV;
    }

    /**
     * 确定此网络连接是否正在将Nagle的算法用作其网络通信的一部分
     *
     * @return boolean如果使用TCP无延迟(禁用Nagle算法)则为true
     */
    public final boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * 设置此网络连接是否应将Nagle的算法*作为其网络通信的一部分
     *
     * @param tcpNoDelay boolean如果应使用TCP无延迟(禁用Nagle算法)则为True
     */
    public final void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    /**
     * 如果网络上安装了网络连接，则为True如果不存在
     * 则将从设备继承有关网络连接*的安装状态的信息
     *
     * @return boolean如果NetworkConnection安装在网络上，则为True
     */
    public boolean isInstalled() {
        return null != device && device.isInstalled()
                && (null == installed || installed.booleanValue());
    }

    public Boolean getInstalled() {
        return installed;
    }

    /**
     * 如果网络上安装了网络连接，则为True如果不存在
     * 则将从设备继承有关网络连接*的安装状态的信息
     *
     * @param installed 如果网络上安装了NetworkConnection，则为True
     */
    public void setInstalled(Boolean installed) {
        if (this.installed == installed)
            return;

        boolean prev = isInstalled();
        this.installed = installed;
        if (isInstalled() != prev)
            needRebind();
    }

    public synchronized void rebind() throws IOException, GeneralSecurityException {
        unbind();
        bind();
    }

    /**
     * 获取我们应忽略的IP地址列表
     * 在使用负载均衡器的环境中很有用。对于来自负载平衡交换机的TCP ping
     * 我们不想剥离新的*线程并尝试协商关联。
     *
     * @return 返回应忽略的IP地址列表
     */
    public final String[] getBlacklist() {
        return blacklist;
    }

    /**
     * 设置一个IP地址列表，我们应从中忽略连接
     * 在使用负载均衡器的环境中很有用对于来自负载平衡交换机的TCP ping
     * 我们不想剥离新的*线程并尝试协商关联
     *
     * @param blacklist IP地址列表，应将其忽略
     */
    public final void setBlacklist(String[] blacklist) {
        this.blacklist = blacklist;
        this.blacklistAddrs = null;
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder(), Normal.EMPTY).toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + Symbol.SPACE;
        Material.appendLine(sb, indent, "Connection[cn: ", commonName);
        Material.appendLine(sb, indent2, "host: ", hostname);
        Material.appendLine(sb, indent2, "port: ", port);
        Material.appendLine(sb, indent2, "ciphers: ", Arrays.toString(tlsCipherSuites));
        Material.appendLine(sb, indent2, "installed: ", getInstalled());
        return sb.append(indent).append(Symbol.C_BRACKET_RIGHT);
    }

    public void setSocketSendOptions(Socket s) throws SocketException {
        int size = s.getSendBufferSize();
        if (sendBufferSize == 0) {
            sendBufferSize = size;
        } else if (sendBufferSize != size) {
            s.setSendBufferSize(sendBufferSize);
            sendBufferSize = s.getSendBufferSize();
        }
        if (s.getTcpNoDelay() != tcpNoDelay) {
            s.setTcpNoDelay(tcpNoDelay);
        }
    }

    private InetAddress hostAddr() throws UnknownHostException {
        if (null == hostAddr && null != hostname)
            hostAddr = InetAddress.getByName(hostname);

        return hostAddr;
    }

    private InetAddress bindAddr() throws UnknownHostException {
        if (null == bindAddress)
            return hostAddr();

        if (null == bindAddr)
            bindAddr = InetAddress.getByName(bindAddress);

        return bindAddr;
    }

    private InetAddress clientBindAddr() throws UnknownHostException {
        if (null == clientBindAddress)
            return hostAddr();

        if (null == clientBindAddr)
            clientBindAddr = InetAddress.getByName(clientBindAddress);

        return clientBindAddr;
    }

    private List<InetAddress> blacklistAddrs() {
        if (null == blacklistAddrs) {
            blacklistAddrs = new ArrayList<InetAddress>(blacklist.length);
            for (String hostname : blacklist)
                try {
                    blacklistAddrs.add(InetAddress.getByName(hostname));
                } catch (UnknownHostException e) {
                    Logger.warn("Failed to lookup InetAddress of " + hostname, e);
                }
        }
        return blacklistAddrs;
    }


    public InetSocketAddress getEndPoint() throws UnknownHostException {
        return new InetSocketAddress(hostAddr(), port);
    }

    public InetSocketAddress getBindPoint() throws UnknownHostException {
        return new InetSocketAddress(bindAddr(), port);
    }

    public InetSocketAddress getClientBindPoint() throws UnknownHostException {
        return new InetSocketAddress(clientBindAddr(), 0);
    }

    private void checkInstalled() {
        if (!isInstalled())
            throw new IllegalStateException("Not installed");
    }

    private void checkCompatible(Connection remoteConn) throws InternalException {
        if (!isCompatible(remoteConn))
            throw new InternalException(remoteConn.toString());
    }

    /**
     * 将此网络连接绑定到TCP端口并启动服务器套接字* accept循环
     *
     * @return the boolean
     * @throws IOException              网络交互是否有问题
     * @throws GeneralSecurityException 异常
     */
    public synchronized boolean bind() throws IOException, GeneralSecurityException {
        if (!(isInstalled() && isServer())) {
            rebindNeeded = false;
            return false;
        }
        if (null == device)
            throw new IllegalStateException("Not attached to Device");
        if (isListening())
            throw new IllegalStateException("Already listening - " + listener);
        if (protocol.isTcp()) {
            TCPHandler handler = tcpHandlers.get(protocol);
            if (null == handler) {
                Logger.info("No TCP Protocol Handler for protocol {}", protocol);
                return false;
            }
            listener = new TCPListener(this, handler);
        } else {
            UDPHandler handler = udpHandlers.get(protocol);
            if (null == handler) {
                Logger.info("No UDP Protocol Handler for protocol {}", protocol);
                return false;
            }
            listener = new UDPListener(this, handler);
        }
        rebindNeeded = false;
        return true;
    }

    public final boolean isListening() {
        return null != listener;
    }

    public boolean isBlackListed(InetAddress ia) {
        return blacklistAddrs().contains(ia);
    }

    public synchronized void unbind() {
        Closeable tmp = listener;
        if (null == tmp)
            return;
        listener = null;
        try {
            tmp.close();
        } catch (Throwable e) {
            Logger.error(e.getMessage());
            // 关闭服务器套接字时忽略错误.
        }
    }

    public Socket connect(Connection remoteConn)
            throws IOException, InternalException, GeneralSecurityException {
        checkInstalled();
        if (!protocol.isTcp())
            throw new IllegalStateException("Not a TCP Connection");
        checkCompatible(remoteConn);
        SocketAddress bindPoint = getClientBindPoint();
        String remoteHostname = remoteConn.getHostname();
        int remotePort = remoteConn.getPort();
        Logger.info("Initiate connection from {} to {}:{}",
                bindPoint, remoteHostname, remotePort);
        Socket s = new Socket();
        Monitoring monitor = null != device
                ? device.getMonitoring()
                : null;
        try {
            s.bind(bindPoint);
            setReceiveBufferSize(s);
            setSocketSendOptions(s);
            String remoteProxy = remoteConn.getHttpProxy();
            if (null != remoteProxy) {
                String userauth = null;
                String[] ss = Material.split(remoteProxy, Symbol.C_AT);
                if (ss.length > 1) {
                    userauth = ss[0];
                    remoteProxy = ss[1];
                }
                ss = Material.split(remoteProxy, Symbol.C_COLON);
                int proxyPort = ss.length > 1 ? Integer.parseInt(ss[1]) : 8080;
                s.connect(new InetSocketAddress(ss[0], proxyPort), connectTimeout);
                try {
                    doProxyHandshake(s, remoteHostname, remotePort, userauth,
                            connectTimeout);
                } catch (IOException e) {
                    IoKit.close(s);
                    throw e;
                }
            } else {
                s.connect(remoteConn.getEndPoint(), connectTimeout);
            }
            if (isTls())
                s = createTLSSocket(s, remoteConn);
            if (null != monitor)
                monitor.onConnectionEstablished(this, remoteConn, s);
            Logger.info("Established connection {}", s);
            return s;
        } catch (GeneralSecurityException e) {
            if (null != monitor)
                monitor.onConnectionFailed(this, remoteConn, s, e);
            IoKit.close(s);
            throw e;
        } catch (IOException e) {
            if (null != monitor)
                monitor.onConnectionFailed(this, remoteConn, s, e);
            IoKit.close(s);
            throw e;
        }
    }

    public DatagramSocket createDatagramSocket() throws IOException {
        checkInstalled();
        if (protocol.isTcp())
            throw new IllegalStateException("Not a UDP Connection");

        DatagramSocket ds = new DatagramSocket(getClientBindPoint());
        int size = ds.getSendBufferSize();
        if (sendBufferSize == 0) {
            sendBufferSize = size;
        } else if (sendBufferSize != size) {
            ds.setSendBufferSize(sendBufferSize);
            sendBufferSize = ds.getSendBufferSize();
        }
        return ds;
    }

    public SocketListener getListener() {
        return listener;
    }

    private void doProxyHandshake(Socket s, String hostname, int port,
                                  String userauth, int connectTimeout) throws IOException {
        StringBuilder request = new StringBuilder(Normal._128);
        request.append("CONNECT ")
                .append(hostname).append(Symbol.C_COLON).append(port)
                .append(" HTTP/1.1\r\nHost: ")
                .append(hostname).append(Symbol.C_COLON).append(port);
        if (null != userauth) {
            byte[] b = userauth.getBytes(Charset.UTF_8);
            char[] base64 = new char[(b.length + 2) / 3 * 4];
            Base64.encode(b, 0, b.length, base64, 0);
            request.append("\r\nProxy-Authorization: basic ")
                    .append(base64);
        }
        request.append("\r\n\r\n");
        OutputStream out = s.getOutputStream();
        out.write(request.toString().getBytes(Charset.US_ASCII));
        out.flush();

        s.setSoTimeout(connectTimeout);
        String response = new HTTPResponse(s).toString();
        s.setSoTimeout(0);
        if (!response.startsWith("HTTP/1.1 2"))
            throw new IOException("Unable to tunnel through " + s
                    + ". Proxy returns \"" + response + '\"');
    }

    private SSLSocket createTLSSocket(Socket s, Connection remoteConn)
            throws GeneralSecurityException, IOException {
        SSLContext sslContext = device.sslContext();
        SSLSocketFactory sf = sslContext.getSocketFactory();
        SSLSocket ssl = (SSLSocket) sf.createSocket(s,
                remoteConn.getHostname(), remoteConn.getPort(), true);
        ssl.setEnabledProtocols(
                intersect(remoteConn.getTlsProtocols(), getTlsProtocols()));
        ssl.setEnabledCipherSuites(
                intersect(remoteConn.tlsCipherSuites, tlsCipherSuites));
        ssl.startHandshake();
        return ssl;
    }

    public void close(Socket s) {
        Logger.info("Close connection {}", s);
        IoKit.close(s);
    }

    public boolean isCompatible(Connection remoteConn) {
        if (remoteConn.protocol != protocol)
            return false;

        if (!protocol.isTcp())
            return true;

        if (!isTls())
            return !remoteConn.isTls();

        return hasCommon(remoteConn.getTlsProtocols(), getTlsProtocols())
                && hasCommon(remoteConn.tlsCipherSuites, tlsCipherSuites);
    }

    private boolean hasCommon(String[] ss1, String[] ss2) {
        for (String s1 : ss1)
            for (String s2 : ss2)
                if (s1.equals(s2))
                    return true;
        return false;
    }

    public boolean equalsRDN(Connection other) {
        return null != commonName
                ? commonName.equals(other.commonName)
                : null == other.commonName
                && hostname.equals(other.hostname)
                && port == other.port
                && protocol == other.protocol;
    }

    public void reconfigure(Connection from) {
        setCommonName(from.commonName);
        setHostname(from.hostname);
        setPort(from.port);
        setBindAddress(from.bindAddress);
        setClientBindAddress(from.clientBindAddress);
        setProtocol(from.protocol);
        setHttpProxy(from.httpProxy);
        setBacklog(from.backlog);
        setConnectTimeout(from.connectTimeout);
        setRequestTimeout(from.requestTimeout);
        setAcceptTimeout(from.acceptTimeout);
        setReleaseTimeout(from.releaseTimeout);
        setResponseTimeout(from.responseTimeout);
        setRetrieveTimeout(from.retrieveTimeout);
        setIdleTimeout(from.idleTimeout);
        setSocketCloseDelay(from.socketCloseDelay);
        setSendBufferSize(from.sendBufferSize);
        setReceiveBufferSize(from.receiveBufferSize);
        setSendPDULength(from.sendPDULength);
        setReceivePDULength(from.receivePDULength);
        setMaxOpsPerformed(from.maxOpsPerformed);
        setMaxOpsInvoked(from.maxOpsInvoked);
        setPackPDV(from.packPDV);
        setTcpNoDelay(from.tcpNoDelay);
        setTlsNeedClientAuth(from.tlsNeedClientAuth);
        setTlsCipherSuites(from.tlsCipherSuites);
        setTlsProtocols(from.tlsProtocols);
        setBlacklist(from.blacklist);
        setInstalled(from.installed);
    }

    private static class HTTPResponse extends ByteArrayOutputStream {

        private final String rsp;

        public HTTPResponse(Socket s) throws IOException {
            super(Normal._64);
            InputStream in = s.getInputStream();
            boolean eol = false;
            int b;
            while ((b = in.read()) != -1) {
                write(b);
                if (b == Symbol.C_LF) {
                    if (eol) {
                        rsp = new String(super.buf, 0, super.count, Charset.US_ASCII);
                        return;
                    }
                    eol = true;
                } else if (b != Symbol.C_CR) {
                    eol = false;
                }
            }
            throw new IOException("Unexpected EOF from " + s);
        }

        @Override
        public String toString() {
            return rsp;
        }
    }

}
