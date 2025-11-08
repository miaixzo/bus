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
package org.miaixz.bus.core.net;

import java.io.IOException;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * An enumeration of network protocols, used for identifying and handling various network protocols. The protocol names
 * correspond to the scheme returned by {@link java.net.URL#getProtocol()} or {@link java.net.URI#getScheme()} (e.g.,
 * http, https), not the specific protocol version (e.g., http/1.1, spdy/3.1). This class is used to differentiate how
 * HTTP messages are constructed and to handle protocol-specific features, supporting the identification and validation
 * of multiple communication protocols.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum Protocol {

    /**
     * Named pipe protocol, used for inter-process communication via shared memory. It is slightly faster than sockets
     * but is limited to processes on the same machine. Java does not support named pipes by default.
     */
    PIPE("pipe"),

    /**
     * A reliable TCP/IP socket connection protocol that provides connection-oriented communication.
     */
    SOCKET("socket"),

    /**
     * Transmission Control Protocol (TCP), providing reliable, connection-oriented communication, widely used in
     * network applications.
     */
    TCP("tcp"),

    /**
     * User Datagram Protocol (UDP), providing connectionless, unreliable communication, suitable for low-latency
     * scenarios.
     */
    UDP("udp"),

    /**
     * Hypertext Transfer Protocol (HTTP), used for transmitting web page data in plain text.
     */
    HTTP("http"),

    /**
     * HTTP/1.0 protocol, an early plain text protocol that does not support persistent connections, compliant with RFC
     * 1945.
     */
    HTTP_1_0("HTTP/1.0"),

    /**
     * HTTP/1.1 protocol, a plain text protocol that supports persistent connections and pipelining, compliant with RFC
     * 7230.
     */
    HTTP_1_1("HTTP/1.1"),

    /**
     * HTTP/2 protocol, a binary framing protocol that supports header compression, request multiplexing, and server
     * push, based on HTTP/1.1 semantics, compliant with RFC 7540.
     */
    HTTP_2("h2"),

    /**
     * HTTP/3 protocol, based on QUIC transport, supporting efficient multiplexing and low latency, compliant with RFC
     * 9000.
     */
    HTTP_3("h3"),

    /**
     * SPDY/3.1 protocol, a binary framing protocol from Chromium that supports header compression and request
     * multiplexing, based on HTTP/1.1 semantics (deprecated).
     */
    SPDY_3("spdy/3.1"),

    /**
     * HTTP/2 cleartext protocol, which does not require an upgrade handshake and requires the client to have prior
     * knowledge that the server supports cleartext HTTP/2, compliant with RFC 7540.
     */
    H2_PRIOR_KNOWLEDGE("h2_prior_knowledge"),

    /**
     * QUIC (Quick UDP Internet Connections) protocol, a secure, multiplexed transport protocol based on UDP that
     * optimizes HTTP/2 semantics, compliant with RFC 9000.
     */
    QUIC("quic"),

    /**
     * SOAP 1.1 protocol, a simple object access protocol based on XML for communication in distributed systems,
     * compliant with W3C standards.
     */
    SOAP_1_1("soap 1.1 protocol"),

    /**
     * SOAP 1.2 protocol, an upgraded version of SOAP with improved performance and compatibility, compliant with W3C
     * standards.
     */
    SOAP_1_2("SOAP 1.2 Protocol"),

    /**
     * WebSocket protocol (cleartext), used for bidirectional real-time communication over TCP, compliant with RFC 6455.
     */
    WS("ws"),

    /**
     * Encrypted WebSocket protocol (secure), providing bidirectional real-time communication over TLS, compliant with
     * RFC 6455.
     */
    WSS("wss"),

    /**
     * Secure Hypertext Transfer Protocol (HTTPS), an encrypted version of HTTP over TLS/SSL, compliant with RFC 2818.
     */
    HTTPS("https"),

    /**
     * Generic SSL protocol, supporting certain versions of SSL encryption (deprecated).
     */
    SSL("ssl"),

    /**
     * SSL 2.0 protocol, an early encryption protocol, deprecated due to security vulnerabilities.
     */
    SSLv2("SSLv2"),

    /**
     * SSL 3.0 protocol, an improved encryption protocol, deprecated due to security vulnerabilities.
     */
    SSLv3("SSLv3"),

    /**
     * Generic TLS protocol, supporting certain versions of TLS encryption for secure communication.
     */
    TLS("tls"),

    /**
     * TLS 1.0 protocol, compliant with RFC 2246, providing secure encrypted communication (gradually being deprecated).
     */
    TLSv1("TLSv1"),

    /**
     * TLS 1.1 protocol, compliant with RFC 4346, with improved security (gradually being deprecated).
     */
    TLSv1_1("TLSv1.1"),

    /**
     * TLS 1.2 protocol, compliant with RFC 5246, with further enhanced security, widely used.
     */
    TLSv1_2("TLSv1.2"),

    /**
     * TLS 1.3 protocol, compliant with RFC 8446, providing higher security and performance, recommended for use.
     */
    TLSv1_3("TLSv1.3"),

    /**
     * DICOM protocol, used for the transmission and storage of medical imaging data, compliant with ISO 12052.
     */
    DICOM("dicom"),

    /**
     * HL7 protocol, a standardized protocol for exchanging medical information, compliant with HL7 international
     * standards.
     */
    HL7("hl7"),

    /**
     * OpenID Connect protocol, an authentication protocol based on OAuth2, compliant with OpenID standards.
     */
    OIDC("OIDC"),

    /**
     * SAML protocol, used for single sign-on and identity federation, compliant with OASIS standards.
     */
    SAML("SAML"),

    /**
     * Lightweight Directory Access Protocol (LDAP), used for accessing directory services, compliant with RFC 4511.
     */
    LDAP("LDAP"),

    /**
     * Message Queue.
     */
    MQ("MQ"),

    /**
     * Advanced Message Queuing Protocol.
     */
    AMQP("AMQP"),

    /**
     * Simple Text Oriented Messaging Protocol.
     */
    STOMP("STOMP"),

    /**
     * Message Queuing Telemetry Transport.
     */
    MQTT("MQTT"),

    /**
     * Message middleware binary protocol.
     */
    OPENWIRE("Openwire"),

    /**
     * Custom binary protocol used by Apache Kafka.
     */
    KAFKA("Kafka"),

    /**
     * Redis Serialization Protocol.
     */
    RESP("RESP"),

    /**
     * Model-Context Protocol.
     */
    MCP("MCP"),

    ;

    /**
     * HTTP prefix, formatted as "http://".
     */
    public static final String HTTP_PREFIX = HTTP.name + Symbol.COLON + Symbol.FORWARDSLASH;

    /**
     * HTTPS prefix, formatted as "https://".
     */
    public static final String HTTPS_PREFIX = HTTPS.name + Symbol.COLON + Symbol.FORWARDSLASH;

    /**
     * WebSocket prefix, formatted as "ws://".
     */
    public static final String WS_PREFIX = WS.name + Symbol.COLON + Symbol.FORWARDSLASH;

    /**
     * Secure WebSocket prefix, formatted as "wss://".
     */
    public static final String WSS_PREFIX = WSS.name + Symbol.COLON + Symbol.FORWARDSLASH;

    /**
     * Local IPv4 address, with the value "127.0.0.1", representing the local loopback address.
     */
    public static final String HOST_IPV4 = "127.0.0.1";

    /**
     * Local hostname, with the value "localhost", used for local host access.
     */
    public static final String HOST_LOCAL = "localhost";

    /**
     * Minimum IPv4 address in string form, with the value "0.0.0.0", typically used to listen on all interfaces.
     */
    public static final String IPV4_STR_MIN = "0.0.0.0";

    /**
     * Maximum IPv4 address in string form, with the value "255.255.255.255", representing the broadcast address.
     */
    public static final String IPV4_STR_MAX = "255.255.255.255";

    /**
     * Maximum IPv4 address in numerical form, with the value 0xffffffffL, representing the highest IP address.
     */
    public static final long IPV4_NUM_MAX = 0xffffffffL;

    /**
     * Maximum unused IPv4 address in string form, with the value "0.255.255.255", representing an unallocated address
     * range.
     */
    public static final String IPV4_UNUSED_STR_MAX = "0.255.255.255";

    /**
     * The name of the protocol, used to identify the protocol type.
     */
    public final String name;

    /**
     * Gets the corresponding protocol enum instance from the protocol name.
     *
     * @param protocol The name of the protocol.
     * @return The corresponding protocol enum instance.
     * @throws IOException If the protocol name is unknown.
     */
    public static Protocol get(String protocol) throws IOException {
        if (StringKit.isEmpty(protocol)) {
            throw new IOException("Protocol cannot be null or empty");
        }
        switch (protocol) {
            case "HTTP/1.0":
                return HTTP_1_0;

            case "HTTP/1.1":
                return HTTP_1_1;

            case "h2_prior_knowledge":
                return H2_PRIOR_KNOWLEDGE;

            case "h2":
                return HTTP_2;

            case "spdy/3.1":
                return SPDY_3;

            case "quic":
                return QUIC;

            case "soap 1.1 protocol":
                return SOAP_1_1;

            case "SOAP 1.2 Protocol":
                return SOAP_1_2;

            default:
                throw new IOException("Unexpected protocol: " + protocol);
        }
    }

    /**
     * Gets the host from a URL.
     *
     * @param url The URL to process.
     * @return The corresponding host.
     */
    public static String getHost(String url) {
        return getHost(url, true);
    }

    /**
     * Gets the host from a URL.
     *
     * @param url      The URL to process.
     * @param withPort Whether to include the port.
     * @return The corresponding host.
     */
    public static String getHost(String url, boolean withPort) {
        if (StringKit.isEmpty(url)) {
            return url;
        }

        // First, remove the protocol part
        String withoutProtocol = url.replaceFirst("^[a-zA-Z]+://", Normal.EMPTY);

        // If the port is needed, return directly
        if (withPort) {
            return withoutProtocol;
        }

        // If the port is not needed, check if a port number exists
        int portIndex = withoutProtocol.indexOf(':');
        if (portIndex != -1) {
            return withoutProtocol.substring(0, portIndex);
        }

        // If there is no port, return directly
        return withoutProtocol;
    }

    /**
     * Gets the port number from an address.
     * <p>
     * This method will attempt to find an explicit port. If not found, it will try to infer the default port for "http"
     * (80) or "httpsS" (BUG) "https" (443). If neither is found, it will throw an exception.
     *
     * @param address The address, in the format "hostname:port" or "protocol://hostname:port".
     * @return The port number.
     * @throws IllegalArgumentException If the address format is invalid or no port (explicit or implicit) can be found.
     */
    public static int getPort(String address) {
        // Pass -1 as the defaultPort to indicate that an exception should be thrown if no port is found
        return getPort(address, -1);
    }

    /**
     * Gets the port number from an address, returning a default value if not found.
     * <p>
     * This method follows this logic: 1. Check for an explicit port (e.g., ":8080"). 2. If no explicit port, check for
     * an implicit protocol port (http=80, https=443). 3. If neither, return the provided {@code defaultPort}. 4. If
     * {@code defaultPort} is -1 and no port is found, throw an exception.
     *
     * @param address     The address, in the format "hostname:port" or "protocol://hostname:port".
     * @param defaultPort The default port to return if no port is specified in the address.
     * @return The port number or the default port.
     * @throws IllegalArgumentException If the address format is invalid and no default port is provided.
     */
    public static int getPort(String address, int defaultPort) {
        if (address == null || address.isEmpty()) {
            if (defaultPort >= 0) {
                return defaultPort;
            }
            throw new IllegalArgumentException("Address cannot be null or empty");
        }

        String hostPortPart = address;
        int implicitPort = -1; // Default implicit port

        // Check for protocol and set implicit port
        int protocolIndex = address.indexOf("://");
        if (protocolIndex >= 0) {
            String protocol = address.substring(0, protocolIndex);
            hostPortPart = address.substring(protocolIndex + 3); // Part after "://"

            if ("http".equalsIgnoreCase(protocol)) {
                implicitPort = PORT._80.getPort();
            } else if ("https".equalsIgnoreCase(protocol)) {
                implicitPort = PORT._443.getPort();
            }
            // You could add more implicit ports here, e.g., "ws" -> 80, "wss" -> 443
        }

        // Find the last colon, which would separate host from port
        int portIndex = hostPortPart.lastIndexOf(Symbol.C_COLON);

        // Ensure it's not a colon in an IPv6 address, e.g., [::1]
        int ipv6EndIndex = hostPortPart.lastIndexOf(Symbol.C_BRACKET_RIGHT);
        if (portIndex > 0 && ipv6EndIndex < portIndex) {
            // This colon is for a port

            String portString = hostPortPart.substring(portIndex + 1);

            // Remove any path, query, or fragment from the port string
            int pathIndex = portString.indexOf(Symbol.C_SLASH);
            if (pathIndex >= 0) {
                portString = portString.substring(0, pathIndex);
            }
            int queryIndex = portString.indexOf(Symbol.C_QUESTION_MARK);
            if (queryIndex >= 0) {
                portString = portString.substring(0, queryIndex);
            }
            int hashIndex = portString.indexOf(Symbol.C_HASH);
            if (hashIndex >= 0) {
                portString = portString.substring(0, hashIndex);
            }

            // If portStr is empty (e.g., "host:/path"), it's invalid, fall through
            if (!portString.isEmpty()) {
                try {
                    return Integer.parseInt(portString);
                } catch (NumberFormatException e) {
                    if (defaultPort >= 0) {
                        return defaultPort;
                    }
                    throw new IllegalArgumentException("Invalid port number in address: " + address);
                }
            }
        }

        // No explicit port found, return implicit port if one was set
        if (implicitPort >= 0) {
            return implicitPort;
        }

        // No explicit or implicit port, return the user's default
        if (defaultPort >= 0) {
            return defaultPort;
        }

        // All options exhausted
        throw new IllegalArgumentException("Port not specified and no default port available for address: " + address);
    }

    /**
     * Checks if a URL is an HTTP protocol, supporting standard prefixes and URL-encoded formats.
     *
     * @param url The URL to validate.
     * @return {@code true} if it is an HTTP protocol, {@code false} otherwise.
     */
    public static boolean isHttp(String url) {
        if (StringKit.isEmpty(url)) {
            return false;
        }
        return url.startsWith(HTTP_PREFIX) || url.startsWith("http%3A%2F%2F");
    }

    /**
     * Checks if a URL is an HTTPS protocol, supporting standard prefixes and URL-encoded formats.
     *
     * @param url The URL to validate.
     * @return {@code true} if it is an HTTPS protocol, {@code false} otherwise.
     */
    public static boolean isHttps(String url) {
        if (StringKit.isEmpty(url)) {
            return false;
        }
        return url.startsWith(HTTPS_PREFIX) || url.startsWith("https%3A%2F%2F");
    }

    /**
     * Checks if a URL points to the local host (domain or IP), supporting IPv4 and localhost.
     *
     * @param url The URL to validate.
     * @return {@code true} if it is a local host, {@code false} otherwise.
     */
    public static boolean isLocalHost(String url) {
        return StringKit.isEmpty(url) || url.contains(HOST_IPV4) || url.contains(HOST_LOCAL);
    }

    /**
     * Checks if a URL is an HTTPS protocol or points to the local host.
     *
     * @param url The URL to validate.
     * @return {@code true} if it is HTTPS or a local host, {@code false} otherwise.
     */
    public static boolean isHttpsOrLocalHost(String url) {
        if (StringKit.isEmpty(url)) {
            return false;
        }
        return isHttps(url) || isLocalHost(url);
    }

    /**
     * Checks if the protocol is TCP-based (i.e., not UDP).
     *
     * @return {@code true} if it is a TCP protocol, {@code false} if it is UDP.
     */
    public boolean isTcp() {
        return this != UDP;
    }

    /**
     * Checks if the protocol is a secure protocol (based on TLS/SSL).
     *
     * @return {@code true} if it is a secure protocol (e.g., HTTPS, WSS, TLS, SSL), {@code false} otherwise.
     */
    public boolean isSecure() {
        return this == HTTPS || this == WSS || this == TLS || this == SSL || this == TLSv1 || this == TLSv1_1
                || this == TLSv1_2 || this == TLSv1_3 || this == SSLv2 || this == SSLv3;
    }

    /**
     * Returns the protocol name, used for ALPN (Application-Layer Protocol Negotiation) protocol identification, such
     * as "http/1.1", "h2", etc.
     *
     * @return The protocol name.
     */
    @Override
    public String toString() {
        return name;
    }

}
