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
package org.miaixz.bus.cortex.guard.token;

import java.util.Objects;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.guard.GuardContext;
import org.miaixz.bus.cortex.guard.GuardDecision;
import org.miaixz.bus.cortex.guard.GuardPolicy;
import org.miaixz.bus.cortex.guard.GuardStrategy;

/**
 * Lightweight token-guard strategy that performs presence and transport pre-validation.
 * <p>
 * This strategy intentionally does not verify token signatures or consult persistence. Those responsibilities stay in
 * the runtime authorization provider. Its job is to keep namespace and asset-level token policy definitions in one
 * reusable Cortex model.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TokenGuardStrategy implements GuardStrategy {

    /**
     * Well-known attribute name used to pass a parsed {@link TokenCredential} through {@link GuardContext}.
     */
    public static final String ATTRIBUTE_TOKEN_CREDENTIAL = "tokenCredential";

    /**
     * Token-guard configuration consulted by this strategy.
     */
    private final TokenGuardConfig config;

    /**
     * Creates one strategy instance bound to token-guard configuration.
     *
     * @param config token-guard configuration
     */
    public TokenGuardStrategy(TokenGuardConfig config) {
        this.config = Objects.requireNonNull(config, "Token guard config must not be null");
    }

    /**
     * Evaluates whether the request satisfies basic token-guard requirements.
     *
     * @param context guard evaluation context
     * @return guard decision
     */
    @Override
    public GuardDecision evaluate(GuardContext context) {
        if (!config.isEnabled()) {
            return GuardDecision.allow();
        }
        if (context == null) {
            return GuardDecision.deny("guard.context.missing", "Guard context is required");
        }

        final GuardPolicy policy = context.getPolicy();
        if (policy == null || !policy.tokenBased()) {
            return GuardDecision.allow();
        }

        if (StringKit.isBlank(context.getToken())) {
            return GuardDecision.deny("guard.token.missing", "Token credential is required");
        }

        final Object rawCredential = context.getAttribute(ATTRIBUTE_TOKEN_CREDENTIAL);
        if (rawCredential instanceof TokenCredential credential) {
            if (credential.getTransport() != null && !config.supports(credential.getTransport())) {
                return GuardDecision.deny(
                        "guard.token.transport.unsupported",
                        "Token transport is not allowed: " + credential.getTransport());
            }
        }

        return GuardDecision.allow();
    }

    /**
     * Returns the token-guard configuration used by this strategy.
     *
     * @return token-guard configuration
     */
    public TokenGuardConfig getConfig() {
        return config;
    }

}
