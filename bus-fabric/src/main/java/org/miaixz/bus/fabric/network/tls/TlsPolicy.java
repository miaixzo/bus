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
package org.miaixz.bus.fabric.network.tls;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Policy;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;

/**
 * Immutable policy pairing the TLS context with its handshake settings.
 *
 * @param context  TLS engine context
 * @param settings immutable handshake settings
 * @author Kimi Liu
 */
public record TlsPolicy(TlsContext context, TlsSettings settings) implements Policy {

    /**
     * Typed option for a complete generic TLS policy.
     * <p>
     * Absence and explicit null both use {@link #defaults()}.
     */
    public static final Options.Key<TlsPolicy> OPTION = Options.key("tls.policy", TlsPolicy.class);

    /**
     * Validates the complete TLS configuration pair.
     */
    public TlsPolicy {
        context = Assert.notNull(context, () -> new ValidateException("TLS context must not be null"));
        settings = Assert.notNull(settings, () -> new ValidateException("TLS settings must not be null"));
    }

    /**
     * Creates a TLS policy.
     *
     * @param context  TLS engine context
     * @param settings immutable handshake settings
     * @return validated TLS policy
     */
    public static TlsPolicy of(final TlsContext context, final TlsSettings settings) {
        return new TlsPolicy(context, settings);
    }

    /**
     * Creates a policy using process defaults.
     *
     * @return default TLS policy
     */
    public static TlsPolicy defaults() {
        return Instances.get(
                TlsPolicy.class.getName() + ".defaults",
                () -> new TlsPolicy(TlsContext.defaults(), TlsSettings.defaults()));
    }

    /**
     * Resolves the configured TLS policy.
     *
     * @param options immutable option source
     * @return configured policy, or the shared default policy when absent or explicitly null
     */
    public static TlsPolicy resolve(final Options options) {
        final TlsPolicy configured = Assert.notNull(options, () -> new ValidateException("Options must not be null"))
                .get(OPTION);
        return configured == null ? defaults() : configured;
    }

    /**
     * Writes this complete TLS policy into its network-owned typed option.
     *
     * @param options immutable option source
     * @return option snapshot containing this TLS policy
     */
    @Override
    public Options from(final Options options) {
        return Assert.notNull(options, () -> new ValidateException("Options must not be null")).with(OPTION, this);
    }

}
