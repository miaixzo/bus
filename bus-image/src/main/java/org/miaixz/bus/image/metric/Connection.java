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
package org.miaixz.bus.image.metric;

import java.io.*;
import java.net.*;
import java.security.GeneralSecurityException;
import java.util.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.net.*;
import org.miaixz.bus.logger.Logger;

/**
 * A DICOM Part 15, Annex H compliant class, <code>NetworkConnection</code> encapsulates the properties associated with
 * a connection to a TCP/IP network. The <i>network connection</i> describes one TCP port on one network device. This
 * can be used for a TCP connection over which a DICOM association can be negotiated with one or more Network AEs. It
 * specifies 8 the hostname and TCP port number. A network connection may support multiple Network AEs. The Network AE
 * selection takes place during association negotiation based on the called and calling AE-titles.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Connection implements Serializable {

    public static final int NO_TIMEOUT = 0;
    public static final int SYNCHRONOUS_MODE = 1;
    public static final int NOT_LISTENING = -1;
    public static final int DEF_BACKLOG = 50;
    public static final int DEF_SOCKETDELAY = 50;
    public static final int DEF_ABORT_TIMEOUT = 1000;
    public static final int DEF_BUFFERSIZE = 0;
    public static final int DEF_MAX_PDU_LENGTH = 16378;
    public static final String TLS_RSA_WITH_NULL_SHA = "SSL_RSA_WITH_NULL_SHA";
    public static final String TLS_RSA_WITH_3DES_EDE_CBC_SHA = "SSL_RSA_WITH_3DES_EDE_CBC_SHA";
    // to fit into SunJSSE TLS Application Data Length 16408
    public static final String TLS_RSA_WITH_AES_128_CBC_SHA = "TLS_RSA_WITH_AES_128_CBC_SHA";
    public static final String[] DEFAULT_TLS_PROTOCOLS = { "TLSv1.2" };
    private static final long serialVersionUID = -1L;
    private static final EnumMap<Protocol, TCPProtocolHandler> tcpHandlers = new EnumMap<>(Protocol.class);
    private static final EnumMap<Protocol, UDPProtocolHandler> udpHandlers = new EnumMap<>(Protocol.class);

    static {
        registerTCPProtocolHandler(Protocol.DICOM, ImageProtocolHandler.INSTANCE);
    }

    private Device device;
    private String commonName;
    private String hostname;
    private String bindAddress;
    private String clientBindAddress;
    private String httpProxy;
    private int port = NOT_LISTENING;
    private int backlog = DEF_BACKLOG;
    private int connectTimeout;
    private int requestTimeout;
    private int acceptTimeout;
    private int releaseTimeout;
    private int sendTimeout;
    private int storeTimeout;
    private int responseTimeout;
    private int retrieveTimeout;
    private boolean retrieveTimeoutTotal;
    private int idleTimeout;
    private int abortTimeout = DEF_ABORT_TIMEOUT;
    private int socketCloseDelay = DEF_SOCKETDELAY;
    private int sendBufferSize;
    private int receiveBufferSize;
    private int sendPDULength = DEF_MAX_PDU_LENGTH;
    private int receivePDULength = DEF_MAX_PDU_LENGTH;
    private int maxOpsPerformed = SYNCHRONOUS_MODE;
    private int maxOpsInvoked = SYNCHRONOUS_MODE;
    private boolean packPDV = true;
    private boolean tcpNoDelay = true;
    private boolean tlsNeedClientAuth = true;
    private String[] tlsCipherSuites = {};
    private String[] tlsProtocols = DEFAULT_TLS_PROTOCOLS;
    private String[] blacklist = {};
    private Boolean installed;
    private Protocol protocol = Protocol.DICOM;
    private EndpointIdentificationAlgorithm tlsEndpointIdentificationAlgorithm;
    private transient List<InetAddress> blacklistAddrs;
    private transient InetAddress hostAddr;
    private transient InetAddress bindAddr;
    private transient InetAddress clientBindAddr;
    private transient volatile Listener listener;
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

    public Connection(Connection from) {
        reconfigure(from);
    }

    public static TCPProtocolHandler registerTCPProtocolHandler(Protocol protocol, TCPProtocolHandler handler) {
        return tcpHandlers.put(protocol, handler);
    }

    public static TCPProtocolHandler unregisterTCPProtocolHandler(Protocol protocol) {
        return tcpHandlers.remove(protocol);
    }

    public static UDPProtocolHandler registerUDPProtocolHandler(Protocol protocol, UDPProtocolHandler handler) {
        return udpHandlers.put(protocol, handler);
    }

    public static UDPProtocolHandler unregisterUDPProtocolHandler(Protocol protocol) {
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
        if (device != null && this.device != null)
            throw new IllegalStateException("already owned by " + device);
        this.device = device;
    }

    /**
     * 这是此特定连接的DNS名称 用于获取连接的当前IP地址主机名必须具有足够的资格 对于任何客户端DNS用户而言都是明确的
     *
     * @return 包含主机名的字符串
     */
    public final String getHostname() {
        return hostname;
    }

    /**
     * 这是此特定连接的DNS名称 用于获取连接的当前IP地址，主机名必须具有足够的资格 对于任何客户端DNS用户而言都是明确的
     *
     * @param hostname 包含主机名的字符串
     */
    public final void setHostname(String hostname) {
        if (Objects.equals(hostname, this.hostname))
            return;

        this.hostname = hostname;
        needRebind();
    }

    /**
     * 监听套接字的绑定地址或{@code null}如果{@code null}，则将 侦听套接字绑定到{@link #getHostname()} 这是默认值
     *
     * @return 连接的绑定地址或{@code null}
     */
    public final String getBindAddress() {
        return bindAddress;
    }

    /**
     * 监听套接字的绑定地址或{@code null} 如果{@code null}， 则将侦听套接字绑定到{@link #getHostname()}
     *
     * @param bindAddress 监听套接字的绑定地址或{@code null}
     */
    public final void setBindAddress(String bindAddress) {
        if (Objects.equals(bindAddress, this.bindAddress))
            return;

        this.bindAddress = bindAddress;
        this.bindAddr = null;
        needRebind();
    }

    /**
     * 传出连接的绑定地址，{@code "0.0.0.0"} 或{@code null} 如果{@code "0.0.0.0"}，系统将选择任何本地IP进行传出连接 如果{@code null}，则将传出连接绑定到
     * {@link #getHostname()}
     *
     * @return 字符串
     */
    public String getClientBindAddress() {
        return clientBindAddress;
    }

    /**
     * 传出连接的绑定地址， {@code "0.0.0.0"}或{@code null} 如果{@code "0.0.0.0"}，系统将选择任何本地IP进行传出*连接 如果{@code null}，则将传出连接绑定到
     * {@link #getHostname()}
     *
     * @param bindAddress 传出连接的绑定地址或{@code null}
     */
    public void setClientBindAddress(String bindAddress) {
        if (Objects.equals(bindAddress, this.clientBindAddress))
            return;

        this.clientBindAddress = bindAddress;
        this.clientBindAddr = null;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        if (protocol == null)
            throw new NullPointerException();

        if (this.protocol == protocol)
            return;

        this.protocol = protocol;
        needRebind();
    }

    public EndpointIdentificationAlgorithm getTlsEndpointIdentificationAlgorithm() {
        return tlsEndpointIdentificationAlgorithm;
    }

    public void setTlsEndpointIdentificationAlgorithm(
            EndpointIdentificationAlgorithm tlsEndpointIdentificationAlgorithm) {
        this.tlsEndpointIdentificationAlgorithm = tlsEndpointIdentificationAlgorithm;
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
     * AE正在侦听的TCP端口，或仅用于启动关联的网络连接 有效的端口值在0到65535之间
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
        return httpProxy != null;
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

    public int getAbortTimeout() {
        return abortTimeout;
    }

    public void setAbortTimeout(int delay) {
        if (delay < 0)
            throw new IllegalArgumentException("delay: " + delay);
        this.abortTimeout = delay;
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

    /**
     * Timeout in ms for sending other DIMSE RQs than C STORE-RQs.
     *
     * @return Timeout in ms or {@code 0} (= no timeout).
     */
    public int getSendTimeout() {
        return sendTimeout;
    }

    /**
     * Timeout in ms for sending other DIMSE RQs than C-STORE RQs.
     *
     * @param timeout Timeout in ms or {@code 0} (= no timeout).
     */
    public void setSendTimeout(int timeout) {
        this.sendTimeout = timeout;
    }

    /**
     * Timeout in ms for sending C-STORE RQs.
     *
     * @return Timeout in ms or {@code 0} (= no timeout).
     */
    public int getStoreTimeout() {
        return storeTimeout;
    }

    /**
     * Timeout in ms for sending C-STORE RQs.
     *
     * @param timeout Timeout in ms or {@code 0} (= no timeout).
     */
    public void setStoreTimeout(int timeout) {
        this.storeTimeout = timeout;
    }

    /**
     * Timeout in ms for receiving other outstanding DIMSE RSPs than C-MOVE or C-GET RSPs.
     *
     * @return Timeout in ms or {@code 0} (= no timeout).
     */
    public final int getResponseTimeout() {
        return responseTimeout;
    }

    /**
     * Timeout in ms for receiving other outstanding DIMSE RSPs than C-MOVE or C-GET RSPs.
     *
     * @param timeout Timeout in ms or {@code 0} (= no timeout).
     */
    public final void setResponseTimeout(int timeout) {
        this.responseTimeout = timeout;
    }

    /**
     * Timeout in ms for receiving outstanding C-MOVE or C-GET RSPs.
     *
     * @return Timeout in ms or {@code 0} (= no timeout).
     */
    public final int getRetrieveTimeout() {
        return retrieveTimeout;
    }

    /**
     * Timeout in ms for receiving outstanding C-MOVE or C-GET RSPs.
     *
     * @param timeout Timeout in ms or {@code 0} (= no timeout).
     */
    public final void setRetrieveTimeout(int timeout) {
        this.retrieveTimeout = timeout;
    }

    /**
     * Indicates if the timer with the specified timeout for outstanding C-GET and C-MOVE RSPs shall be restarted on
     * receive of pending RSPs.
     *
     * @return if {@code false}, restart the timer with the specified timeout for outstanding C-GET and C-MOVE RSPs on
     *         receive of pending RSPs, otherwise not.
     */
    public final boolean isRetrieveTimeoutTotal() {
        return retrieveTimeoutTotal;
    }

    /**
     * Indicates if the timer with the specified timeout for outstanding C-GET and C-MOVE RSPs shall be restarted on
     * receive of pending RSPs.
     *
     * @param total if {@code false}, restart the timer with the specified timeout for outstanding C-GET and C-MOVE RSPs
     *              on receive of pending RSPs, otherwise not.
     */
    public final void setRetrieveTimeoutTotal(boolean total) {
        this.retrieveTimeoutTotal = total;
    }

    /**
     * Timeout in ms for aborting of idle Associations.
     *
     * @return Timeout in ms or {@code 0} (= no timeout).
     */
    public final int getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Timeout in ms for aborting of idle Associations.
     *
     * @param timeout Timeout in ms or {@code 0} (= no timeout).
     */
    public final void setIdleTimeout(int timeout) {
        this.idleTimeout = timeout;
    }

    /**
     * 此特定连接上支持的TLS CipherSuite TLS CipherSuites必须使用RFC-2246字符串 表示形式进行描述(例如“ SSL_RSA_WITH_3DES_EDE_CBC_SHA")
     *
     * @return 包含受支持的密码套件的String数组
     */
    public String[] getTlsCipherSuites() {
        return tlsCipherSuites;
    }

    /**
     * 此特定连接上支持的TLS CipherSuite TLS CipherSuites必须使用RFC-2246字符串 表示形式进行描述(例如"SSL_RSA_WITH_3DES_EDE_CBC_SHA")
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
     * 如果网络上安装了网络连接，则为True如果不存在 则将从设备继承有关网络连接*的安装状态的信息
     *
     * @return boolean如果NetworkConnection安装在网络上，则为True
     */
    public boolean isInstalled() {
        return device != null && device.isInstalled() && (installed == null || installed.booleanValue());
    }

    public Boolean getInstalled() {
        return installed;
    }

    /**
     * 如果网络上安装了网络连接，则为True如果不存在 则将从设备继承有关网络连接*的安装状态的信息
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
     * 获取我们应忽略的IP地址列表 在使用负载均衡器的环境中很有用。对于来自负载平衡交换机的TCP ping 我们不想剥离新的*线程并尝试协商关联。
     *
     * @return 返回应忽略的IP地址列表
     */
    public final String[] getBlacklist() {
        return blacklist;
    }

    /**
     * 设置一个IP地址列表，我们应从中忽略连接 在使用负载均衡器的环境中很有用对于来自负载平衡交换机的TCP ping 我们不想剥离新的*线程并尝试协商关联
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
        Builder.appendLine(sb, indent, "Connection[cn: ", commonName);
        Builder.appendLine(sb, indent2, "host: ", hostname);
        Builder.appendLine(sb, indent2, "port: ", port);
        Builder.appendLine(sb, indent2, "ciphers: ", Arrays.toString(tlsCipherSuites));
        Builder.appendLine(sb, indent2, "installed: ", getInstalled());
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
        if (hostAddr == null && hostname != null)
            hostAddr = InetAddress.getByName(hostname);

        return hostAddr;
    }

    private InetAddress bindAddr() throws UnknownHostException {
        if (bindAddress == null)
            return hostAddr();

        if (bindAddr == null)
            bindAddr = InetAddress.getByName(bindAddress);

        return bindAddr;
    }

    private InetAddress clientBindAddr() throws UnknownHostException {
        if (clientBindAddress == null)
            return hostAddr();

        if (clientBindAddr == null)
            clientBindAddr = InetAddress.getByName(clientBindAddress);

        return clientBindAddr;
    }

    private List<InetAddress> blacklistAddrs() {
        if (blacklistAddrs == null) {
            blacklistAddrs = new ArrayList<>(blacklist.length);
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
        if (device == null)
            throw new IllegalStateException("Not attached to Device");
        if (isListening())
            throw new IllegalStateException("Already listening - " + listener);
        if (protocol.isTCP()) {
            TCPProtocolHandler handler = tcpHandlers.get(protocol);
            if (handler == null) {
                Logger.info("No TCP Protocol Handler for protocol {}", protocol);
                return false;
            }
            listener = new TCPListener(this, handler);
        } else {
            UDPProtocolHandler handler = udpHandlers.get(protocol);
            if (handler == null) {
                Logger.info("No UDP Protocol Handler for protocol {}", protocol);
                return false;
            }
            listener = new UDPListener(this, handler);
        }
        rebindNeeded = false;
        return true;
    }

    public final boolean isListening() {
        return listener != null;
    }

    public boolean isBlackListed(InetAddress ia) {
        return blacklistAddrs().contains(ia);
    }

    public synchronized void unbind() {
        Closeable tmp = listener;
        if (tmp == null)
            return;
        listener = null;
        try {
            tmp.close();
        } catch (Throwable e) {
            Logger.error(e.getMessage());
            // 关闭服务器套接字时忽略错误.
        }
    }

    public Socket connect(Connection remoteConn) throws IOException, InternalException, GeneralSecurityException {
        checkInstalled();
        if (!protocol.isTCP())
            throw new IllegalStateException("Not a TCP Connection");
        checkCompatible(remoteConn);
        SocketAddress bindPoint = getClientBindPoint();
        String remoteHostname = remoteConn.getHostname();
        int remotePort = remoteConn.getPort();
        Logger.info("Initiate connection from {} to {}:{}", bindPoint, remoteHostname, remotePort);
        Socket s = new Socket();
        ConnectionMonitor monitor = device != null ? device.getConnectionMonitor() : null;
        try {
            s.bind(bindPoint);
            setReceiveBufferSize(s);
            setSocketSendOptions(s);
            String remoteProxy = remoteConn.getHttpProxy();
            if (remoteProxy != null) {
                String userauth = null;
                String[] ss = Builder.split(remoteProxy, Symbol.C_AT);
                if (ss.length > 1) {
                    userauth = ss[0];
                    remoteProxy = ss[1];
                }
                ss = Builder.split(remoteProxy, Symbol.C_COLON);
                int proxyPort = ss.length > 1 ? Integer.parseInt(ss[1]) : 8080;
                s.connect(new InetSocketAddress(ss[0], proxyPort), connectTimeout);
                try {
                    doProxyHandshake(s, remoteHostname, remotePort, userauth, connectTimeout);
                } catch (IOException e) {
                    IoKit.close(s);
                    throw e;
                }
            } else {
                s.connect(remoteConn.getEndPoint(), connectTimeout);
            }
            if (isTls())
                s = createTLSSocket(s, remoteConn);
            if (monitor != null)
                monitor.onConnectionEstablished(this, remoteConn, s);
            Logger.info("Established connection {}", s);
            return s;
        } catch (GeneralSecurityException e) {
            if (monitor != null)
                monitor.onConnectionFailed(this, remoteConn, s, e);
            IoKit.close(s);
            throw e;
        } catch (IOException e) {
            if (monitor != null)
                monitor.onConnectionFailed(this, remoteConn, s, e);
            IoKit.close(s);
            throw e;
        }
    }

    public DatagramSocket createDatagramSocket() throws IOException {
        checkInstalled();
        if (protocol.isTCP())
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

    public Listener getListener() {
        return listener;
    }

    private void doProxyHandshake(Socket s, String hostname, int port, String userauth, int connectTimeout)
            throws IOException {
        StringBuilder request = new StringBuilder(Normal._128);
        request.append("CONNECT ").append(hostname).append(Symbol.C_COLON).append(port).append(" HTTP/1.1\r\nHost: ")
                .append(hostname).append(Symbol.C_COLON).append(port);
        if (userauth != null) {
            byte[] b = userauth.getBytes(Charset.UTF_8);
            char[] base64 = new char[(b.length + 2) / 3 * 4];
            Builder.encode(b, 0, b.length, base64, 0);
            request.append("\r\nProxy-Authorization: basic ").append(base64);
        }
        request.append("\r\n\r\n");
        OutputStream out = s.getOutputStream();
        out.write(request.toString().getBytes(Charset.US_ASCII));
        out.flush();

        s.setSoTimeout(connectTimeout);
        String response = new HTTPResponse(s).toString();
        s.setSoTimeout(0);
        if (!response.startsWith("HTTP/1.1 2"))
            throw new IOException("Unable to tunnel through " + s + ". Proxy returns \"" + response + '\"');
    }

    private SSLSocket createTLSSocket(Socket s, Connection remoteConn) throws GeneralSecurityException, IOException {
        SSLContext sslContext = device.sslContext();
        SSLSocketFactory sf = sslContext.getSocketFactory();
        SSLSocket ssl = (SSLSocket) sf.createSocket(s, remoteConn.getHostname(), remoteConn.getPort(), true);
        ssl.setEnabledProtocols(intersect(remoteConn.getTlsProtocols(), getTlsProtocols()));
        ssl.setEnabledCipherSuites(intersect(remoteConn.getTlsCipherSuites(), getTlsCipherSuites()));

        if (tlsEndpointIdentificationAlgorithm != null) {
            SSLParameters parameters = ssl.getSSLParameters();
            parameters.setEndpointIdentificationAlgorithm(tlsEndpointIdentificationAlgorithm.name());
            ssl.setSSLParameters(parameters);
        }
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

        if (!protocol.isTCP())
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
        return commonName != null ? commonName.equals(other.commonName)
                : other.commonName == null && hostname.equals(other.hostname) && port == other.port
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
        setSendTimeout(from.sendTimeout);
        setStoreTimeout(from.storeTimeout);
        setResponseTimeout(from.responseTimeout);
        setRetrieveTimeout(from.retrieveTimeout);
        setIdleTimeout(from.idleTimeout);
        setAbortTimeout(from.abortTimeout);
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
        setTlsEndpointIdentificationAlgorithm(from.tlsEndpointIdentificationAlgorithm);
        setBlacklist(from.blacklist);
        setInstalled(from.installed);
    }

    public enum Protocol {
        DICOM, HL7, HL7_MLLP2, SYSLOG_TLS, SYSLOG_UDP, HTTP;

        public boolean isTCP() {
            return this != SYSLOG_UDP;
        }

        public boolean isHL7() {
            return this == HL7 || this == HL7_MLLP2;
        }

        public boolean isSyslog() {
            return this == SYSLOG_TLS || this == SYSLOG_UDP;
        }
    }

    public enum EndpointIdentificationAlgorithm {
        HTTPS, LDAPS
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
