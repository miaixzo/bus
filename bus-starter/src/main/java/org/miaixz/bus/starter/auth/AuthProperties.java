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
package org.miaixz.bus.starter.auth;

import java.util.List;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import org.miaixz.bus.cache.Options;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Binds the root settings of the Spring Boot authentication integration.
 * <p>
 * Vendor clients use the direct {@code bus.auth.<vendor>} layout. {@link AuthService} binds those entries dynamically
 * from the Vendor identifiers registered in bus-auth, so this class does not duplicate the platform catalog. Optional
 * {@code bus.auth.jwt} settings bind one reusable shared JWT service without exposing key material in diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
@Getter
@ConfigurationProperties(prefix = GeniusBuilder.AUTH)
public class AuthProperties {

    /**
     * Whether the complete authentication integration is enabled.
     */
    private final boolean enabled;

    /**
     * Optional authentication-specific atomic cache backend settings.
     */
    @NestedConfigurationProperty
    private final Options cache;

    /**
     * Optional shared JWT service settings.
     */
    @NestedConfigurationProperty
    private final Jwt jwt;

    /**
     * Creates immutable root authentication settings.
     *
     * @param enabled whether the complete authentication integration is enabled
     * @param cache   optional authentication-specific atomic cache backend settings
     * @param jwt     optional shared JWT service settings
     */
    @ConstructorBinding
    public AuthProperties(@DefaultValue(Normal.TRUE) boolean enabled, Options cache, Jwt jwt) {
        this.enabled = enabled;
        this.cache = cache;
        this.jwt = jwt;
    }

    /**
     * Preserves the source-level constructor used before shared JWT settings were introduced.
     *
     * @param enabled whether the complete authentication integration is enabled
     * @param cache   optional authentication-specific atomic cache backend settings
     */
    public AuthProperties(final boolean enabled, final Options cache) {
        this(enabled, cache, null);
    }

    /**
     * Returns a diagnostic representation without exposing cache credentials.
     *
     * @return safe root authentication setting summary
     */
    @Override
    public String toString() {
        return "AuthProperties[enabled=" + enabled + ", cache=<masked>, jwt=<masked>]";
    }

    /**
     * Carries optional shared HS256 JWT settings from {@code bus.auth.jwt}.
     *
     * @param secret       exact non-empty String key material
     * @param secretPolicy compatibility or strict input-length policy
     * @author Kimi Liu
     */
    public record Jwt(String secret, SecretPolicy secretPolicy) {

        /**
         * Applies the compatibility policy when no explicit policy is configured.
         */
        public Jwt {
            secretPolicy = secretPolicy == null ? SecretPolicy.COMPATIBLE : secretPolicy;
        }

        /**
         * Returns a diagnostic representation without exposing key material.
         *
         * @return masked JWT setting summary
         */
        @Override
        public String toString() {
            return "Jwt[secret=<masked>, secretPolicy=" + secretPolicy + ']';
        }

    }

    /**
     * Controls whether Spring configuration accepts short JWT String key material.
     *
     * @author Kimi Liu
     */
    public enum SecretPolicy {
        /**
         * Accepts every non-empty String and warns once for values shorter than 32 UTF-8 bytes.
         */
        COMPATIBLE,
        /**
         * Rejects values shorter than 32 UTF-8 bytes during context initialization.
         */
        STRICT
    }

    /**
     * Carries the standard client fields bound from one {@code bus.auth.<vendor>} block.
     *
     * @param enabled      whether this Vendor client is enabled
     * @param clientId     public client identifier issued by the external platform
     * @param clientSecret client secret supplied by the protected configuration source
     * @param redirectUri  exact callback URI registered with the external platform
     * @param scopes       ordered explicit scopes, or an empty list for manifest defaults
     * @author Kimi Liu
     */
    public record Client(boolean enabled, String clientId, String clientSecret, String redirectUri,
            List<String> scopes) {

        /**
         * Normalizes the optional scope collection without exposing or transforming secret material.
         */
        public Client {
            scopes = scopes == null ? List.of() : List.copyOf(scopes);
        }

        /**
         * Returns a diagnostic representation that never exposes the client secret.
         *
         * @return safe Vendor client setting summary
         */
        @Override
        public String toString() {
            return "Client[enabled=" + enabled + ", clientId=" + clientId + ", clientSecret=<masked>, redirectUri="
                    + redirectUri + ", scopeCount=" + scopes.size() + "]";
        }

    }

}
