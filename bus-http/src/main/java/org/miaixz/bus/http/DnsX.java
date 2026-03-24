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
 * @since Java 21+
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
