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
package org.miaixz.bus.fabric.network.dns.server;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Policy;

/**
 * Fabric policy carrying DNS server options without starting the server by default.
 *
 * @author Kimi Liu
 */
public final class DnsServerPolicy implements Policy {

    /**
     * Typed option containing DNS server startup options.
     */
    public static final Options.Key<DnsServerPolicy> OPTION = Options.key("dns.server.policy", DnsServerPolicy.class);

    /**
     * DNS server startup options.
     */
    private final DnsServerOptions options;

    /**
     * Creates a DNS server policy.
     *
     * @param options DNS server startup options
     */
    public DnsServerPolicy(final DnsServerOptions options) {
        this.options = Assert.notNull(options, () -> new ValidateException("DNS server options must not be null"));
    }

    /**
     * Creates a DNS server policy.
     *
     * @param options DNS server startup options
     * @return DNS server policy
     */
    public static DnsServerPolicy of(final DnsServerOptions options) {
        return new DnsServerPolicy(options);
    }

    /**
     * Returns DNS server startup options.
     *
     * @return DNS server options
     */
    public DnsServerOptions options() {
        return options;
    }

    /**
     * Resolves the DNS server policy from an option snapshot.
     *
     * @param options option snapshot
     * @return configured DNS server policy, or {@code null}
     */
    public static DnsServerPolicy resolve(final Options options) {
        return Assert.notNull(options, () -> new ValidateException("Options must not be null")).get(OPTION);
    }

    /**
     * Writes this policy into options.
     *
     * @param options immutable option source
     * @return option snapshot containing this policy
     */
    @Override
    public Options from(final Options options) {
        return Assert.notNull(options, () -> new ValidateException("Options must not be null")).with(OPTION, this);
    }

}
