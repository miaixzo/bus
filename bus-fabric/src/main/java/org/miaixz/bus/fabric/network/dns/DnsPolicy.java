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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Policy;

/**
 * Immutable DNS policy selecting the resolver owned by a fabric runtime.
 *
 * @param resolver resolver source whose cache is shared by its runtime-bound view
 * @author Kimi Liu
 */
public record DnsPolicy(DnsResolver resolver) implements Policy {

    /**
     * Typed option containing the DNS policy.
     */
    public static final Options.Key<DnsPolicy> OPTION = Options.key("dns.policy", DnsPolicy.class);

    /**
     * Validates the resolver source.
     */
    public DnsPolicy {
        resolver = Assert.notNull(resolver, () -> new ValidateException("DNS resolver must not be null"));
    }

    /**
     * Creates a policy from one resolver.
     *
     * @param resolver resolver source
     * @return validated DNS policy
     */
    public static DnsPolicy of(final DnsResolver resolver) {
        return new DnsPolicy(resolver);
    }

    /**
     * Returns the shared system resolver policy.
     *
     * @return default DNS policy
     */
    public static DnsPolicy defaults() {
        return new DnsPolicy(DnsResolver.system());
    }

    /**
     * Resolves the configured policy.
     *
     * @param options immutable option source
     * @return configured policy or system default
     */
    public static DnsPolicy resolve(final Options options) {
        final DnsPolicy configured = Assert.notNull(options, () -> new ValidateException("Options must not be null"))
                .get(OPTION);
        return configured == null ? defaults() : configured;
    }

    /**
     * Writes this policy into the immutable options.
     *
     * @param options immutable option source
     * @return option snapshot containing this policy
     */
    @Override
    public Options from(final Options options) {
        return Assert.notNull(options, () -> new ValidateException("Options must not be null")).with(OPTION, this);
    }

}
