/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.metric.Association;
import org.miaixz.bus.logger.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * 服务器信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Node {

    private final String aet;
    private final String hostname;
    private final Integer port;
    private final boolean validate;

    public Node(String aet) {
        this(aet, null, null);
    }

    public Node(String aet, Integer port) {
        this(aet, null, port);
    }

    public Node(String aet, String hostname, Integer port) {
        this(aet, hostname, port, false);
    }

    public Node(String aet, String hostname, Integer port, boolean validate) {
        if (!StringKit.hasText(aet)) {
            throw new IllegalArgumentException("Missing AETitle");
        }
        if (aet.length() > Normal._16) {
            throw new IllegalArgumentException("AETitle has more than 16 characters");
        }
        if (null != port && (port < 1 || port > 65535)) {
            throw new IllegalArgumentException("Port is out of bound");
        }
        this.aet = aet;
        this.hostname = hostname;
        this.port = port;
        this.validate = validate;
    }

    public static String convertToIP(String hostname) {
        try {
            return InetAddress.getByName(hostname).getHostAddress();
        } catch (UnknownHostException e) {
            Logger.error("Cannot resolve hostname", e);
        }
        return StringKit.hasText(hostname) ? hostname : Http.HOST_IPV4;
    }

    public static Node buildLocalDicomNode(Association as) {
        String ip = null;
        InetAddress address = as.getSocket().getLocalAddress();
        if (null != address) {
            ip = address.getHostAddress();
        }
        return new Node(as.getLocalAET(), ip, as.getSocket().getLocalPort());
    }

    public static Node buildRemoteDicomNode(Association as) {
        String ip = null;
        InetAddress address = as.getSocket().getInetAddress();
        if (null != address) {
            ip = address.getHostAddress();
        }
        return new Node(as.getRemoteAET(), ip, as.getSocket().getPort());
    }

    public boolean equalsHostname(String anotherHostname) {
        if (Objects.equals(hostname, anotherHostname)) {
            return true;
        }
        return convertToIP(hostname).equals(convertToIP(anotherHostname));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return aet.equals(node.aet) &&
                Objects.equals(hostname, node.hostname) &&
                Objects.equals(port, node.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aet, hostname, port);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("Host=");
        buf.append(hostname);
        buf.append(" AET=");
        buf.append(aet);
        buf.append(" Port=");
        buf.append(port);
        return buf.toString();
    }

}
