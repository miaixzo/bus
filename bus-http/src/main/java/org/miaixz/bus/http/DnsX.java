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
package org.miaixz.bus.http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * A Domain Name System (DNS) service that resolves IP addresses for hostnames.
 *
 * <p>
 * Most applications will use the default {@linkplain #SYSTEM system DNS service}. Applications may provide their own
 * implementation to use a different DNS server, to prefer IPv6 addresses over IPv4 addresses, or to force the use of
 * specific known IP addresses for testing or other purposes.
 *
 * @author Kimi Liu
 * @see <a href="https://en.wikipedia.org/wiki/Domain_Name_System">Domain Name System on Wikipedia</a>
 * @since Java 17+
 */
public interface DnsX {

    /**
     * A DNS implementation that uses {@link InetAddress#getAllByName(String)} to ask the underlying operating system to
     * look up IP addresses. Most custom {@link DnsX} implementations should delegate to this instance.
     */
    DnsX SYSTEM = hostname -> {
        if (null == hostname) {
            throw new UnknownHostException("hostname == null");
        }
        try {
            return Arrays.asList(InetAddress.getAllByName(hostname));
        } catch (NullPointerException e) {
            UnknownHostException unknownHostException = new UnknownHostException(
                    "Broken system behaviour for dns lookup of " + hostname);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    };

    /**
     * Returns the IP addresses for {@code hostname}, in the order they should be attempted. If a connection to an
     * address fails, the HTTP client will retry the connection with the next address until a connection is established,
     * the set of IP addresses is exhausted, or a limit is exceeded.
     *
     * @param hostname the hostname to look up.
     * @return a list of IP addresses for the given hostname.
     * @throws UnknownHostException if the host is unknown or cannot be resolved.
     */
    List<InetAddress> lookup(String hostname) throws UnknownHostException;

}
