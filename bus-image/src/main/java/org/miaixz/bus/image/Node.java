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
package org.miaixz.bus.image;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.logger.Logger;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a DICOM Application Entity (AE) node, encapsulating its network address information such as AE Title,
 * hostname, and port. This class is immutable.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Node {

    /**
     * The Application Entity Title (AET). Must not be empty and must be 16 characters or less.
     */
    private final String aet;
    /**
     * The hostname or IP address of the node.
     */
    private final String hostname;
    /**
     * The port number for the DICOM service. Must be between 1 and 65535.
     */
    private final Integer port;
    /**
     * A flag indicating whether hostname validation should be performed during connection.
     */
    private final boolean validateHostname;
    /**
     * The unique identifier for the node, typically from a database.
     */
    private final Long id;

    /**
     * Constructs a new Node with only an AE Title. Hostname and port will be null.
     *
     * @param aet The Application Entity Title.
     */
    public Node(String aet) {
        this(aet, null, null);
    }

    /**
     * Constructs a new Node with an AE Title and port. Hostname will be null.
     *
     * @param aet  The Application Entity Title.
     * @param port The port number.
     */
    public Node(String aet, Integer port) {
        this(aet, null, port);
    }

    /**
     * Constructs a new Node with AE Title, hostname, and port. Hostname validation will be disabled by default.
     *
     * @param aet      The Application Entity Title.
     * @param hostname The hostname or IP address.
     * @param port     The port number.
     */
    public Node(String aet, String hostname, Integer port) {
        this(null, aet, hostname, port, false);
    }

    /**
     * Constructs a new Node with an ID, AE Title, hostname, and port. Hostname validation will be disabled by default.
     *
     * @param id       The unique identifier for the node.
     * @param aet      The Application Entity Title.
     * @param hostname The hostname or IP address.
     * @param port     The port number.
     */
    public Node(Long id, String aet, String hostname, Integer port) {
        this(id, aet, hostname, port, false);
    }

    /**
     * The primary constructor for creating a new Node.
     *
     * @param id               The unique identifier for the node. Can be null.
     * @param aet              The Application Entity Title. Must not be empty and must not exceed 16 characters.
     * @param hostname         The hostname or IP address. Can be null.
     * @param port             The port number. Can be null, but if specified, must be within the valid range (1-65535).
     * @param validateHostname A flag indicating whether to validate the hostname.
     * @throws IllegalArgumentException if AET is missing, exceeds 16 characters, or if the port is out of bounds.
     */
    public Node(Long id, String aet, String hostname, Integer port, boolean validateHostname) {
        if (!StringKit.hasText(aet)) {
            throw new IllegalArgumentException("Missing AETitle");
        }
        if (aet.length() > 16) {
            throw new IllegalArgumentException("AETitle has more than 16 characters");
        }
        if (port != null && (port < 1 || port > 65535)) {
            throw new IllegalArgumentException("Port is out of bound");
        }
        this.id = id;
        this.aet = aet.trim();
        this.hostname = hostname;
        this.port = port;
        this.validateHostname = validateHostname;
    }

    /**
     * Converts a given hostname into its IP address representation. If the hostname cannot be resolved, it logs an
     * error and returns the original hostname if it's not empty, otherwise defaults to "127.0.0.1".
     *
     * @param hostname The hostname to convert.
     * @return The resolved IP address as a string, or a fallback value.
     */
    public static String convertToIP(String hostname) {
        try {
            return InetAddress.getByName(hostname).getHostAddress();
        } catch (UnknownHostException e) {
            Logger.error("Cannot resolve hostname", e);
        }
        return StringKit.hasText(hostname) ? hostname : "127.0.0.1";
    }

    /**
     * Builds a {@code Node} instance representing the local peer from a DICOM {@code Association}.
     *
     * @param as The active DICOM association.
     * @return A new {@code Node} object representing the local AE.
     */
    public static Node buildLocalDicomNode(Association as) {
        String ip = null;
        InetAddress address = as.getSocket().getLocalAddress();
        if (address != null) {
            ip = address.getHostAddress();
        }
        return new Node(as.getLocalAET(), ip, as.getSocket().getLocalPort());
    }

    /**
     * Builds a {@code Node} instance representing the remote peer from a DICOM {@code Association}.
     *
     * @param as The active DICOM association.
     * @return A new {@code Node} object representing the remote AE.
     */
    public static Node buildRemoteDicomNode(Association as) {
        String ip = null;
        InetAddress address = as.getSocket().getInetAddress();
        if (address != null) {
            ip = address.getHostAddress();
        }
        return new Node(as.getRemoteAET(), ip, as.getSocket().getPort());
    }

    /**
     * Gets the unique identifier of this node.
     *
     * @return The identifier.
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the AE Title of this node.
     *
     * @return The AE Title.
     */
    public String getAet() {
        return aet;
    }

    /**
     * Gets the hostname of this node.
     *
     * @return The hostname or IP address.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Gets the port number of this node.
     *
     * @return The port number.
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Checks if hostname validation is enabled for this node.
     *
     * @return {@code true} if hostname validation is enabled, {@code false} otherwise.
     */
    public boolean isValidateHostname() {
        return validateHostname;
    }

    /**
     * Compares this node's hostname with another hostname. The comparison is done after attempting to resolve both
     * hostnames to their IP addresses to ensure a canonical match.
     *
     * @param anotherHostname The hostname to compare against.
     * @return {@code true} if the hostnames are considered equal, {@code false} otherwise.
     */
    public boolean equalsHostname(String anotherHostname) {
        if (Objects.equals(hostname, anotherHostname)) {
            return true;
        }
        return convertToIP(hostname).equals(convertToIP(anotherHostname));
    }

    /**
     * Indicates whether some other object is "equal to" this one. Two nodes are considered equal if they have the same
     * AE Title, hostname, and port.
     *
     * @param o The reference object with which to compare.
     * @return {@code true} if this object is the same as the o argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Node node = (Node) o;
        return aet.equals(node.aet) && Objects.equals(hostname, node.hostname) && Objects.equals(port, node.port);
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of hash tables such as those
     * provided by {@link java.util.HashMap}.
     *
     * @return A hash code value for this object, based on AE Title, hostname, and port.
     */
    @Override
    public int hashCode() {
        return Objects.hash(aet, hostname, port);
    }

    /**
     * Returns a string representation of the node.
     *
     * @return A string containing the hostname, AE Title, and port of the node.
     */
    @Override
    public String toString() {
        return "Host=" + hostname + " AET=" + aet + " Port=" + port;
    }

}
