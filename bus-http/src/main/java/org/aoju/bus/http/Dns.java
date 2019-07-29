package org.aoju.bus.http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * A entity name service that resolves IP addresses for host names. Most applications will use the
 * {@linkplain #SYSTEM system DNS service}, which is the default. Some applications may provide
 * their own implementation to use a different DNS server, to prefer IPv6 addresses, to prefer IPv4
 * addresses, or to force a specific known IP address.
 *
 * <p>Implementations of this interface must be safe for concurrent use.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Dns {
    /**
     * A DNS that uses {@link InetAddress#getAllByName} to ask the underlying operating system to
     * lookup IP addresses. Most custom {@link Dns} implementations should delegate to this instance.
     */
    Dns SYSTEM = new Dns() {
        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            if (hostname == null) throw new UnknownHostException("hostname == null");
            try {
                return Arrays.asList(InetAddress.getAllByName(hostname));
            } catch (NullPointerException e) {
                UnknownHostException unknownHostException =
                        new UnknownHostException("Broken system behaviour for dns lookup of " + hostname);
                unknownHostException.initCause(e);
                throw unknownHostException;
            }
        }
    };

    /**
     * Returns the IP addresses of {@code hostname}, in the order they will be attempted by HttpClient. If
     * a connection to an address fails, HttpClient will retry the connection with the next address until
     * either a connection is made, the set of IP addresses is exhausted, or a limit is exceeded.
     */
    List<InetAddress> lookup(String hostname) throws UnknownHostException;
}
