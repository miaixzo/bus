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
package org.miaixz.bus.fabric.network.dns;

import java.net.InetAddress;
import java.util.List;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.NetKit;

/**
 * Adapter that exposes host lookup callbacks as current DNS resolver contracts.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsResolverAdapter implements Resolver {

    /**
     * Caller-supplied lookup function that receives a host name and returns resolved addresses.
     */
    private final Function<String, List<InetAddress>> lookup;

    /**
     * Creates an adapter.
     *
     * @param lookup lookup function
     */
    private DnsResolverAdapter(final Function<String, List<InetAddress>> lookup) {
        this.lookup = Assert.notNull(lookup, () -> new ValidateException("DNS lookup must not be null"));
    }

    /**
     * Wraps a lookup function as a Resolver.
     *
     * @param lookup lookup function
     * @return resolver
     */
    public static Resolver resolver(final Function<String, List<InetAddress>> lookup) {
        return new DnsResolverAdapter(lookup);
    }

    /**
     * Wraps a lookup function as a DnsResolver.
     *
     * @param lookup lookup function
     * @return DNS resolver
     */
    public static DnsResolver dnsResolver(final Function<String, List<InetAddress>> lookup) {
        return DnsResolver.of(resolver(lookup));
    }

    @Override
    public List<InetAddress> resolve(final String host) {
        final String normalized = NetKit.normalizeHost(host, "DNS host");
        final List<InetAddress> addresses = Assert
                .notNull(lookup.apply(normalized), () -> new ValidateException("DNS lookup must not return null"));
        return List.copyOf(addresses);
    }

}
