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

import java.util.*;

import org.miaixz.bus.core.net.Specifics;
import org.miaixz.bus.cortex.Builder;

import lombok.Getter;
import lombok.Setter;

/**
 * Token guard configuration shared by asset or namespace-level security policy.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class TokenGuardConfig {

    /**
     * Creates a token-guard configuration with default policy values.
     */
    public TokenGuardConfig() {
    }

    /**
     * Whether token-based guard logic is enabled for the current scope.
     */
    private boolean enabled = true;

    /**
     * Cache key prefix used by token-related material when Cortex stores security state.
     */
    private String cachePrefix = Builder.SECURITY_PREFIX;

    /**
     * Default token lifetime in seconds when no finer-grained override is configured.
     */
    private long expireSeconds = Builder.DEFAULT_TOKEN_EXPIRE_SECONDS;

    /**
     * Refresh threshold in seconds. Tokens close to expiry can be proactively renewed by downstream services.
     */
    private long refreshThresholdSeconds = 25L * 60L;

    /**
     * Whether downstream token services are allowed to auto-refresh credentials near expiry.
     */
    private boolean autoRefresh = true;

    /**
     * Whether the same subject may keep multiple live sessions at the same time.
     */
    private boolean multipleSessions = true;

    /**
     * Optional logical issuer name used by persistence or auditing layers.
     */
    private String issuer;

    /**
     * Optional signing or verification secret for token material.
     */
    private String secretKey;

    /**
     * Allowed transport channels for incoming tokens.
     */
    private List<TokenTransport> transports = new ArrayList<>(List.of(TokenTransport.HEADER));

    /**
     * Candidate header names used to discover tokens, aligned with Vortex request parsing.
     */
    private List<String> headerNames = new ArrayList<>(Arrays.asList(Specifics.TOKEN_KEYS));

    /**
     * Candidate cookie names used to discover tokens.
     */
    private List<String> cookieNames = new ArrayList<>(List.of("access_token", "token"));

    /**
     * Candidate query parameter names used to discover tokens.
     */
    private List<String> queryNames = new ArrayList<>(List.of("access_token", "token"));

    /**
     * Bearer prefix expected in header transport.
     */
    private String bearerPrefix = "Bearer ";

    /**
     * Optional per-scope lifetime override expressed in seconds.
     */
    private Map<String, Long> scopeExpireSeconds = new LinkedHashMap<>();

    /**
     * Returns whether the given transport channel is allowed.
     *
     * @param transport transport to check
     * @return {@code true} when the transport channel is allowed
     */
    public boolean supports(TokenTransport transport) {
        return transport != null && transports != null && transports.contains(transport);
    }

    /**
     * Resolves one effective token lifetime in seconds for the supplied logical scope.
     *
     * @param scope logical scope, role, or scene key
     * @return positive lifetime in seconds
     */
    public long resolveExpireSeconds(String scope) {
        if (scope != null && scopeExpireSeconds != null) {
            Long override = scopeExpireSeconds.get(scope);
            if (override != null && override > 0) {
                return override;
            }
        }
        return expireSeconds;
    }

}
