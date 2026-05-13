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
 * @since Java 21+
 */
public class Connection implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852260616932L;

    /**
     * The no timeout value.
     */
    public static final int NO_TIMEOUT = 0;

    /**
     * The synchronous mode value.
     */
    public static final int SYNCHRONOUS_MODE = 1;

    /**
     * The not listening value.
     */
    public static final int NOT_LISTENING = -1;

    /**
     * The def backlog value.
     */
    public static final int DEF_BACKLOG = 50;

    /**
     * The def socketdelay value.
     */
    public static final int DEF_SOCKETDELAY = 50;

    /**
     * The def abort timeout value.
     */
    public static final int DEF_ABORT_TIMEOUT = 1000;

    /**
     * The def buffersize value.
     */
    public static final int DEF_BUFFERSIZE = 0;

    /**
     * The def max pdu length value.
     */
    public static final int DEF_MAX_PDU_LENGTH = 16378;

    /**
     * The tls rsa with null sha value.
     */
    public static final String TLS_RSA_WITH_NULL_SHA = "SSL_RSA_WITH_NULL_SHA";

    /**
     * The tls rsa with 3 des ede cbc sha value.
     */
    public static final String TLS_RSA_WITH_3DES_EDE_CBC_SHA = "SSL_RSA_WITH_3DES_EDE_CBC_SHA";
    // to fit into SunJSSE TLS Application Data Length 16408
    /**
     * The tls rsa with aes 128 cbc sha value.
     */
    public static final String TLS_RSA_WITH_AES_128_CBC_SHA = "TLS_RSA_WITH_AES_128_CBC_SHA";

    /**
     * The default tls protocols value.
     */
    public static final String[] DEFAULT_TLS_PROTOCOLS = { "TLSv1.2" };

    /**
     * The tcp handlers value.
     */
    private static final EnumMap<Protocol, TCPProtocolHandler> tcpHandlers = new EnumMap<>(Protocol.class);

    /**
     * The udp handlers value.
     */
    private static final EnumMap<Protocol, UDPProtocolHandler> udpHandlers = new EnumMap<>(Protocol.class);

    static {
        registerTCPProtocolHandler(Protocol.DICOM, ImageProtocolHandler.INSTANCE);
    }

    /**
     * The device value.
     */
    private Device device;

    /**
     * The common name value.
     */
    private String commonName;

    /**
     * The hostname value.
     */
    private String hostname;

    /**
     * The bind address value.
     */
    private String bindAddress;

    /**
     * The client bind address value.
     */
    private String clientBindAddress;

    /**
     * The http proxy value.
     */
    private String httpProxy;

    /**
     * The port value.
     */
    private int port = NOT_LISTENING;

    /**
     * The backlog value.
     */
    private int backlog = DEF_BACKLOG;

    /**
     * The connect timeout value.
     */
    private int connectTimeout;

    /**
     * The request timeout value.
     */
    private int requestTimeout;

    /**
     * The accept timeout value.
     */
    private int acceptTimeout;

    /**
     * The release timeout value.
     */
    private int releaseTimeout;

    /**
     * The send timeout value.
     */
    private int sendTimeout;

    /**
     * The store timeout value.
     */
    private int storeTimeout;

    /**
     * The response timeout value.
     */
    private int responseTimeout;

    /**
     * The retrieve timeout value.
     */
    private int retrieveTimeout;

    /**
     * The retrieve timeout total value.
     */
    private boolean retrieveTimeoutTotal;

    /**
     * The idle timeout value.
     */
    private int idleTimeout;

    /**
     * The abort timeout value.
     */
    private int abortTimeout = DEF_ABORT_TIMEOUT;

    /**
     * The socket close delay value.
     */
    private int socketCloseDelay = DEF_SOCKETDELAY;

    /**
     * The send buffer size value.
     */
    private int sendBufferSize;

    /**
     * The receive buffer size value.
     */
    private int receiveBufferSize;

    /**
     * The send pdu length value.
     */
    private int sendPDULength = DEF_MAX_PDU_LENGTH;

    /**
     * The receive pdu length value.
     */
    private int receivePDULength = DEF_MAX_PDU_LENGTH;

    /**
     * The max ops performed value.
     */
    private int maxOpsPerformed = SYNCHRONOUS_MODE;

    /**
     * The max ops invoked value.
     */
    private int maxOpsInvoked = SYNCHRONOUS_MODE;

    /**
     * The pack pdv value.
     */
    private boolean packPDV = true;

    /**
     * The tcp no delay value.
     */
    private boolean tcpNoDelay = true;

    /**
     * The tls need client auth value.
     */
    private boolean tlsNeedClientAuth = true;

    /**
     * The tls cipher suites value.
     */
    private String[] tlsCipherSuites = {};

    /**
     * The tls protocols value.
     */
    private String[] tlsProtocols = DEFAULT_TLS_PROTOCOLS;

    /**
     * The blacklist value.
     */
    private String[] blacklist = {};

    /**
     * The installed value.
     */
    private Boolean installed;

    /**
     * The protocol value.
     */
    private Protocol protocol = Protocol.DICOM;

    /**
     * The tls endpoint identification algorithm value.
     */
    private EndpointIdentificationAlgorithm tlsEndpointIdentificationAlgorithm;

    /**
     * The blacklist addrs value.
     */
    private transient List<InetAddress> blacklistAddrs;

    /**
     * The host addr value.
     */
    private transient InetAddress hostAddr;

    /**
     * The bind addr value.
     */
    private transient InetAddress bindAddr;

    /**
     * The client bind addr value.
     */
    private transient InetAddress clientBindAddr;

    /**
     * The listener value.
     */
    private transient volatile Listener listener;

    /**
     * The rebind needed value.
     */
    private transient boolean rebindNeeded;

    /**
     * Creates a new instance.
     */
    public Connection() {
    }

    /**
     * Creates a new instance.
     *
     * @param commonName the common name.
     * @param hostname   the hostname.
     */
    public Connection(String commonName, String hostname) {
        this(commonName, hostname, NOT_LISTENING);
    }

    /**
     * Creates a new instance.
     *
     * @param commonName the common name.
     * @param hostname   the hostname.
     * @param port       the port.
     */
    public Connection(String commonName, String hostname, int port) {
        this.commonName = commonName;
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * Creates a new instance.
     *
     * @param from the from.
     */
    public Connection(Connection from) {
        reconfigure(from);
    }

    /**
     * Executes the register tcp protocol handler operation.
     *
     * @param protocol the protocol.
     * @param handler  the handler.
     * @return the operation result.
     */
    public static TCPProtocolHandler registerTCPProtocolHandler(Protocol protocol, TCPProtocolHandler handler) {
        return tcpHandlers.put(protocol, handler);
    }

    /**
     * Executes the unregister tcp protocol handler operation.
     *
     * @param protocol the protocol.
     * @return the operation result.
     */
    public static TCPProtocolHandler unregisterTCPProtocolHandler(Protocol protocol) {
        return tcpHandlers.remove(protocol);
    }

    /**
     * Executes the register udp protocol handler operation.
     *
     * @param protocol the protocol.
     * @param handler  the handler.
     * @return the operation result.
     */
    public static UDPProtocolHandler registerUDPProtocolHandler(Protocol protocol, UDPProtocolHandler handler) {
        return udpHandlers.put(protocol, handler);
    }

    /**
     * Executes the unregister udp protocol handler operation.
     *
     * @param protocol the protocol.
     * @return the operation result.
     */
    public static UDPProtocolHandler unregisterUDPProtocolHandler(Protocol protocol) {
        return udpHandlers.remove(protocol);
    }

    /**
     * Executes the intersect operation.
     *
     * @param ss1 the ss1.
     * @param ss2 the ss2.
     * @return the operation result.
     */
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
     * Gets the related value.
     *
     * @return the result.
     */
    public final Device getDevice() {
        return device;
    }

    /**
     * Sets the related value.
     *
     * @param device the device.
     */
    public final void setDevice(Device device) {
        if (device != null && this.device != null)
            throw new IllegalStateException("already owned by " + device);
        this.device = device;
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final String getHostname() {
        return hostname;
    }

    /**
     * Gets the related value.
     *
     * @param hostname the hostname.
     */
    public final void setHostname(String hostname) {
        if (Objects.equals(hostname, this.hostname))
            return;

        this.hostname = hostname;
        needRebind();
    }

    /**
     * Binds the related value.
     *
     * @return the result.
     */
    public final String getBindAddress() {
        return bindAddress;
    }

    /**
     * Binds the related value.
     *
     * @param bindAddress the bind address.
     */
    public final void setBindAddress(String bindAddress) {
        if (Objects.equals(bindAddress, this.bindAddress))
            return;

        this.bindAddress = bindAddress;
        this.bindAddr = null;
        needRebind();
    }

    /**
     * Binds the related value. {@link #getHostname()}
     *
     * @return the result.
     */
    public String getClientBindAddress() {
        return clientBindAddress;
    }

    /**
     * Binds the related value. {@link #getHostname()}
     *
     * @param bindAddress the bind address.
     */
    public void setClientBindAddress(String bindAddress) {
        if (Objects.equals(bindAddress, this.clientBindAddress))
            return;

        this.clientBindAddress = bindAddress;
        this.clientBindAddr = null;
    }

    /**
     * Gets the protocol.
     *
     * @return the protocol.
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * Sets the protocol.
     *
     * @param protocol the protocol.
     */
    public void setProtocol(Protocol protocol) {
        if (protocol == null)
            throw new NullPointerException();

        if (this.protocol == protocol)
            return;

        this.protocol = protocol;
        needRebind();
    }

    /**
     * Gets the tls endpoint identification algorithm.
     *
     * @return the tls endpoint identification algorithm.
     */
    public EndpointIdentificationAlgorithm getTlsEndpointIdentificationAlgorithm() {
        return tlsEndpointIdentificationAlgorithm;
    }

    /**
     * Sets the tls endpoint identification algorithm.
     *
     * @param tlsEndpointIdentificationAlgorithm the tls endpoint identification algorithm.
     */
    public void setTlsEndpointIdentificationAlgorithm(
            EndpointIdentificationAlgorithm tlsEndpointIdentificationAlgorithm) {
        this.tlsEndpointIdentificationAlgorithm = tlsEndpointIdentificationAlgorithm;
    }

    /**
     * Determines whether rebind needed.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isRebindNeeded() {
        return rebindNeeded;
    }

    /**
     * Executes the need rebind operation.
     */
    public void needRebind() {
        this.rebindNeeded = true;
    }

    /**
     * Provides DICOM processing details.
     *
     * @return the result.
     */
    public final String getCommonName() {
        return commonName;
    }

    /**
     * Provides DICOM processing details.
     *
     * @param name the name.
     */
    public final void setCommonName(String name) {
        this.commonName = name;
    }

    /**
     * Provides DICOM processing details.
     *
     * @return the result.
     */
    public final int getPort() {
        return port;
    }

    /**
     * Provides DICOM processing details.
     *
     * @param port the port.
     */
    public final void setPort(int port) {
        if (this.port == port)
            return;

        if ((port <= 0 || port > 0xFFFF) && port != NOT_LISTENING)
            throw new IllegalArgumentException("port out of range:" + port);

        this.port = port;
        needRebind();
    }

    /**
     * Gets the http proxy.
     *
     * @return the http proxy.
     */
    public final String getHttpProxy() {
        return httpProxy;
    }

    /**
     * Sets the http proxy.
     *
     * @param proxy the proxy.
     */
    public final void setHttpProxy(String proxy) {
        this.httpProxy = proxy;
    }

    /**
     * Executes the use http proxy operation.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean useHttpProxy() {
        return httpProxy != null;
    }

    /**
     * Determines whether server.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isServer() {
        return port > 0;
    }

    /**
     * Gets the backlog.
     *
     * @return the backlog.
     */
    public final int getBacklog() {
        return backlog;
    }

    /**
     * Sets the backlog.
     *
     * @param backlog the backlog.
     */
    public final void setBacklog(int backlog) {
        if (this.backlog == backlog)
            return;

        if (backlog < 1)
            throw new IllegalArgumentException("backlog: " + backlog);

        this.backlog = backlog;
        needRebind();
    }

    /**
     * Gets the connect timeout.
     *
     * @return the connect timeout.
     */
    public final int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets the connect timeout.
     *
     * @param timeout the timeout.
     */
    public final void setConnectTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.connectTimeout = timeout;
    }

    /**
     * Provides DICOM processing details.
     *
     * @return the int
     */
    public final int getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Provides DICOM processing details.
     *
     * @param timeout the timeout.
     */
    public final void setRequestTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.requestTimeout = timeout;
    }

    /**
     * Gets the accept timeout.
     *
     * @return the accept timeout.
     */
    public final int getAcceptTimeout() {
        return acceptTimeout;
    }

    /**
     * Sets the accept timeout.
     *
     * @param timeout the timeout.
     */
    public final void setAcceptTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.acceptTimeout = timeout;
    }

    /**
     * Provides DICOM processing details.
     *
     * @return the result.
     */
    public final int getReleaseTimeout() {
        return releaseTimeout;
    }

    /**
     * Provides DICOM processing details.
     *
     * @param timeout the timeout.
     */
    public final void setReleaseTimeout(int timeout) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout: " + timeout);
        this.releaseTimeout = timeout;
    }

    /**
     * Gets the abort timeout.
     *
     * @return the abort timeout.
     */
    public int getAbortTimeout() {
        return abortTimeout;
    }

    /**
     * Sets the abort timeout.
     *
     * @param delay the delay.
     */
    public void setAbortTimeout(int delay) {
        if (delay < 0)
            throw new IllegalArgumentException("delay: " + delay);
        this.abortTimeout = delay;
    }

    /**
     * Sends the related message.
     *
     * @return the result.
     */
    public final int getSocketCloseDelay() {
        return socketCloseDelay;
    }

    /**
     * Sends the related message.
     *
     * @param delay the delay.
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
     * Provides DICOM processing details.
     *
     * @return the result.
     */
    public String[] getTlsCipherSuites() {
        return tlsCipherSuites;
    }

    /**
     * Provides DICOM processing details.
     *
     * @param tlsCipherSuites the tls cipher suites.
     */
    public void setTlsCipherSuites(String... tlsCipherSuites) {
        if (Arrays.equals(this.tlsCipherSuites, tlsCipherSuites))
            return;

        this.tlsCipherSuites = tlsCipherSuites;
        needRebind();
    }

    /**
     * Determines whether tls.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isTls() {
        return tlsCipherSuites.length > 0;
    }

    /**
     * Gets the tls protocols.
     *
     * @return the tls protocols.
     */
    public final String[] getTlsProtocols() {
        return tlsProtocols;
    }

    /**
     * Sets the tls protocols.
     *
     * @param tlsProtocols the tls protocols.
     */
    public final void setTlsProtocols(String... tlsProtocols) {
        if (Arrays.equals(this.tlsProtocols, tlsProtocols))
            return;

        this.tlsProtocols = tlsProtocols;
        needRebind();
    }

    /**
     * Determines whether tls need client auth.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isTlsNeedClientAuth() {
        return tlsNeedClientAuth;
    }

    /**
     * Sets the tls need client auth.
     *
     * @param tlsNeedClientAuth the tls need client auth.
     */
    public final void setTlsNeedClientAuth(boolean tlsNeedClientAuth) {
        if (this.tlsNeedClientAuth == tlsNeedClientAuth)
            return;

        this.tlsNeedClientAuth = tlsNeedClientAuth;
        needRebind();
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    /**
     * Sets the related value.
     *
     * @param size the size.
     */
    public final void setReceiveBufferSize(int size) {
        if (size < 0)
            throw new IllegalArgumentException("size: " + size);
        this.receiveBufferSize = size;
    }

    /**
     * Sets the receive buffer size.
     *
     * @param s the s.
     * @throws SocketException if the operation cannot be completed.
     */
    private void setReceiveBufferSize(Socket s) throws SocketException {
        int size = s.getReceiveBufferSize();
        if (receiveBufferSize == 0) {
            receiveBufferSize = size;
        } else if (receiveBufferSize != size) {
            s.setReceiveBufferSize(receiveBufferSize);
            receiveBufferSize = s.getReceiveBufferSize();
        }
    }

    /**
     * Sets the receive buffer size.
     *
     * @param ss the ss.
     * @throws SocketException if the operation cannot be completed.
     */
    public void setReceiveBufferSize(ServerSocket ss) throws SocketException {
        int size = ss.getReceiveBufferSize();
        if (receiveBufferSize == 0) {
            receiveBufferSize = size;
        } else if (receiveBufferSize != size) {
            ss.setReceiveBufferSize(receiveBufferSize);
            receiveBufferSize = ss.getReceiveBufferSize();
        }
    }

    /**
     * Sets the receive buffer size.
     *
     * @param ds the ds.
     * @throws SocketException if the operation cannot be completed.
     */
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
     * Gets the related value.
     *
     * @return the result.
     */
    public final int getSendBufferSize() {
        return sendBufferSize;
    }

    /**
     * Sets the related value.
     *
     * @param size the size.
     */
    public final void setSendBufferSize(int size) {
        if (size < 0)
            throw new IllegalArgumentException("size: " + size);
        this.sendBufferSize = size;
    }

    /**
     * Gets the send pdu length.
     *
     * @return the send pdu length.
     */
    public final int getSendPDULength() {
        return sendPDULength;
    }

    /**
     * Sets the send pdu length.
     *
     * @param sendPDULength the send pdu length.
     */
    public final void setSendPDULength(int sendPDULength) {
        this.sendPDULength = sendPDULength;
    }

    /**
     * Gets the receive pdu length.
     *
     * @return the receive pdu length.
     */
    public final int getReceivePDULength() {
        return receivePDULength;
    }

    /**
     * Sets the receive pdu length.
     *
     * @param receivePDULength the receive pdu length.
     */
    public final void setReceivePDULength(int receivePDULength) {
        this.receivePDULength = receivePDULength;
    }

    /**
     * Gets the max ops performed.
     *
     * @return the max ops performed.
     */
    public final int getMaxOpsPerformed() {
        return maxOpsPerformed;
    }

    /**
     * Sets the max ops performed.
     *
     * @param maxOpsPerformed the max ops performed.
     */
    public final void setMaxOpsPerformed(int maxOpsPerformed) {
        this.maxOpsPerformed = maxOpsPerformed;
    }

    /**
     * Gets the max ops invoked.
     *
     * @return the max ops invoked.
     */
    public final int getMaxOpsInvoked() {
        return maxOpsInvoked;
    }

    /**
     * Sets the max ops invoked.
     *
     * @param maxOpsInvoked the max ops invoked.
     */
    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    /**
     * Determines whether pack pdv.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isPackPDV() {
        return packPDV;
    }

    /**
     * Sets the pack pdv.
     *
     * @param packPDV the pack pdv.
     */
    public final void setPackPDV(boolean packPDV) {
        this.packPDV = packPDV;
    }

    /**
     * Determines whether the condition is met.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * Sets the related value.
     *
     * @param tcpNoDelay the tcp no delay.
     */
    public final void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    /**
     * Provides DICOM processing details.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isInstalled() {
        return device != null && device.isInstalled() && (installed == null || installed.booleanValue());
    }

    /**
     * Gets the installed.
     *
     * @return the installed.
     */
    public Boolean getInstalled() {
        return installed;
    }

    /**
     * Provides DICOM processing details.
     *
     * @param installed the installed.
     */
    public void setInstalled(Boolean installed) {
        if (this.installed == installed)
            return;

        boolean prev = isInstalled();
        this.installed = installed;
        if (isInstalled() != prev)
            needRebind();
    }

    /**
     * Executes the rebind operation.
     *
     * @throws IOException              if the operation cannot be completed.
     * @throws GeneralSecurityException if the operation cannot be completed.
     */
    public synchronized void rebind() throws IOException, GeneralSecurityException {
        unbind();
        bind();
    }

    /**
     * Gets the related value.
     *
     * @return the result.
     */
    public final String[] getBlacklist() {
        return blacklist;
    }

    /**
     * Sets the related value.
     *
     * @param blacklist the blacklist.
     */
    public final void setBlacklist(String[] blacklist) {
        this.blacklist = blacklist;
        this.blacklistAddrs = null;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return promptTo(new StringBuilder(), Normal.EMPTY).toString();
    }

    /**
     * Executes the prompt to operation.
     *
     * @param sb     the sb.
     * @param indent the indent.
     * @return the operation result.
     */
    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + Symbol.SPACE;
        Builder.appendLine(sb, indent, "Connection[cn: ", commonName);
        Builder.appendLine(sb, indent2, "host: ", hostname);
        Builder.appendLine(sb, indent2, "port: ", port);
        Builder.appendLine(sb, indent2, "ciphers: ", Arrays.toString(tlsCipherSuites));
        Builder.appendLine(sb, indent2, "installed: ", getInstalled());
        return sb.append(indent).append(Symbol.C_BRACKET_RIGHT);
    }

    /**
     * Sets the socket send options.
     *
     * @param s the s.
     * @throws SocketException if the operation cannot be completed.
     */
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

    /**
     * Executes the host addr operation.
     *
     * @return the operation result.
     * @throws UnknownHostException if the operation cannot be completed.
     */
    private InetAddress hostAddr() throws UnknownHostException {
        if (hostAddr == null && hostname != null)
            hostAddr = InetAddress.getByName(hostname);

        return hostAddr;
    }

    /**
     * Executes the bind addr operation.
     *
     * @return the operation result.
     * @throws UnknownHostException if the operation cannot be completed.
     */
    private InetAddress bindAddr() throws UnknownHostException {
        if (bindAddress == null)
            return hostAddr();

        if (bindAddr == null)
            bindAddr = InetAddress.getByName(bindAddress);

        return bindAddr;
    }

    /**
     * Executes the client bind addr operation.
     *
     * @return the operation result.
     * @throws UnknownHostException if the operation cannot be completed.
     */
    private InetAddress clientBindAddr() throws UnknownHostException {
        if (clientBindAddress == null)
            return hostAddr();

        if (clientBindAddr == null)
            clientBindAddr = InetAddress.getByName(clientBindAddress);

        return clientBindAddr;
    }

    /**
     * Executes the blacklist addrs operation.
     *
     * @return the operation result.
     */
    private List<InetAddress> blacklistAddrs() {
        if (blacklistAddrs == null) {
            blacklistAddrs = new ArrayList<>(blacklist.length);
            for (String hostname : blacklist)
                try {
                    blacklistAddrs.add(InetAddress.getByName(hostname));
                } catch (UnknownHostException e) {
                    Logger.warn(false, "Image", "Failed to lookup InetAddress of " + hostname, e);
                }
        }
        return blacklistAddrs;
    }

    /**
     * Gets the end point.
     *
     * @return the end point.
     * @throws UnknownHostException if the operation cannot be completed.
     */
    public InetSocketAddress getEndPoint() throws UnknownHostException {
        return new InetSocketAddress(hostAddr(), port);
    }

    /**
     * Gets the bind point.
     *
     * @return the bind point.
     * @throws UnknownHostException if the operation cannot be completed.
     */
    public InetSocketAddress getBindPoint() throws UnknownHostException {
        return new InetSocketAddress(bindAddr(), port);
    }

    /**
     * Gets the client bind point.
     *
     * @return the client bind point.
     * @throws UnknownHostException if the operation cannot be completed.
     */
    public InetSocketAddress getClientBindPoint() throws UnknownHostException {
        return new InetSocketAddress(clientBindAddr(), 0);
    }

    /**
     * Executes the check installed operation.
     */
    private void checkInstalled() {
        if (!isInstalled())
            throw new IllegalStateException("Not installed");
    }

    /**
     * Executes the check compatible operation.
     *
     * @param remoteConn the remote conn.
     * @throws InternalException if the operation cannot be completed.
     */
    private void checkCompatible(Connection remoteConn) throws InternalException {
        if (!isCompatible(remoteConn))
            throw new InternalException(remoteConn.toString());
    }

    /**
     * Binds the related value.
     *
     * @return the boolean
     * @throws IOException              if the operation cannot be completed.
     * @throws GeneralSecurityException if the operation cannot be completed.
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
                Logger.info(false, "Image", "No TCP Protocol Handler for protocol {}", protocol);
                return false;
            }
            listener = new TCPListener(this, handler);
        } else {
            UDPProtocolHandler handler = udpHandlers.get(protocol);
            if (handler == null) {
                Logger.info(false, "Image", "No UDP Protocol Handler for protocol {}", protocol);
                return false;
            }
            listener = new UDPListener(this, handler);
        }
        rebindNeeded = false;
        return true;
    }

    /**
     * Determines whether listening.
     *
     * @return true if the condition is met; otherwise false.
     */
    public final boolean isListening() {
        return listener != null;
    }

    /**
     * Determines whether black listed.
     *
     * @param ia the ia.
     * @return true if the condition is met; otherwise false.
     */
    public boolean isBlackListed(InetAddress ia) {
        return blacklistAddrs().contains(ia);
    }

    /**
     * Executes the unbind operation.
     */
    public synchronized void unbind() {
        Closeable tmp = listener;
        if (tmp == null)
            return;
        listener = null;
        try {
            tmp.close();
        } catch (Throwable e) {
            Logger.error(
                    false,
                    "Image",
                    e,
                    "Connection unbind close failed: protocol={}, exception={}",
                    protocol,
                    e.getClass().getSimpleName());
            // Ignore errors while closing the server socket.
        }
    }

    /**
     * Executes the connect operation.
     *
     * @param remoteConn the remote conn.
     * @return the operation result.
     * @throws IOException              if the operation cannot be completed.
     * @throws InternalException        if the operation cannot be completed.
     * @throws GeneralSecurityException if the operation cannot be completed.
     */
    public Socket connect(Connection remoteConn) throws IOException, InternalException, GeneralSecurityException {
        checkInstalled();
        if (!protocol.isTCP())
            throw new IllegalStateException("Not a TCP Connection");
        checkCompatible(remoteConn);
        SocketAddress bindPoint = getClientBindPoint();
        String remoteHostname = remoteConn.getHostname();
        int remotePort = remoteConn.getPort();
        Logger.info(true, "Image", "Initiate connection from {} to {}:{}", bindPoint, remoteHostname, remotePort);
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
            Logger.info(true, "Image", "Established connection {}", s);
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

    /**
     * Creates the datagram socket.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Gets the listener.
     *
     * @return the listener.
     */
    public Listener getListener() {
        return listener;
    }

    /**
     * Executes the do proxy handshake operation.
     *
     * @param s              the s.
     * @param hostname       the hostname.
     * @param port           the port.
     * @param userauth       the userauth.
     * @param connectTimeout the connect timeout.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Creates the tls socket.
     *
     * @param s          the s.
     * @param remoteConn the remote conn.
     * @return the operation result.
     * @throws GeneralSecurityException if the operation cannot be completed.
     * @throws IOException              if the operation cannot be completed.
     */
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

    /**
     * Executes the close operation.
     *
     * @param s the s.
     */
    public void close(Socket s) {
        Logger.info(true, "Image", "Close connection {}", s);
        IoKit.close(s);
    }

    /**
     * Determines whether compatible.
     *
     * @param remoteConn the remote conn.
     * @return true if the condition is met; otherwise false.
     */
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

    /**
     * Determines whether common.
     *
     * @param ss1 the ss1.
     * @param ss2 the ss2.
     * @return true if the condition is met; otherwise false.
     */
    private boolean hasCommon(String[] ss1, String[] ss2) {
        for (String s1 : ss1)
            for (String s2 : ss2)
                if (s1.equals(s2))
                    return true;
        return false;
    }

    /**
     * Executes the equals rdn operation.
     *
     * @param other the other.
     * @return true if the condition is met; otherwise false.
     */
    public boolean equalsRDN(Connection other) {
        return commonName != null ? commonName.equals(other.commonName)
                : other.commonName == null && hostname.equals(other.hostname) && port == other.port
                        && protocol == other.protocol;
    }

    /**
     * Executes the reconfigure operation.
     *
     * @param from the from.
     */
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

    /**
     * Defines the Protocol values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Protocol {

        /**
         * Constant for the dicom value.
         */
        DICOM,
        /**
         * Constant for the hl7 value.
         */
        HL7,
        /**
         * Constant for the hl7 mllp2 value.
         */
        HL7_MLLP2,
        /**
         * Constant for the syslog tls value.
         */
        SYSLOG_TLS,
        /**
         * Constant for the syslog udp value.
         */
        SYSLOG_UDP,
        /**
         * Constant for the http value.
         */
        HTTP;

        /**
         * Determines whether tcp.
         *
         * @return true if the condition is met; otherwise false.
         */
        public boolean isTCP() {
            return this != SYSLOG_UDP;
        }

        /**
         * Determines whether hl7.
         *
         * @return true if the condition is met; otherwise false.
         */
        public boolean isHL7() {
            return this == HL7 || this == HL7_MLLP2;
        }

        /**
         * Determines whether syslog.
         *
         * @return true if the condition is met; otherwise false.
         */
        public boolean isSyslog() {
            return this == SYSLOG_TLS || this == SYSLOG_UDP;
        }

    }

    /**
     * Defines the EndpointIdentificationAlgorithm values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum EndpointIdentificationAlgorithm {
        /**
         * Constant for the https value.
         */
        HTTPS,
        /**
         * Constant for the ldaps value.
         */
        LDAPS

    }

    /**
     * Represents the HTTPResponse type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static class HTTPResponse extends ByteArrayOutputStream {

        /**
         * The rsp value.
         */
        private final String rsp;

        /**
         * Creates a new instance.
         *
         * @param s the s.
         * @throws IOException if the operation cannot be completed.
         */
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

        /**
         * Returns the string representation.
         *
         * @return the string representation.
         */
        @Override
        public String toString() {
            return rsp;
        }

    }

}
