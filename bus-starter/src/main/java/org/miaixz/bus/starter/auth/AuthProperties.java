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

import org.miaixz.bus.auth.guard.GuardConfig;
import org.miaixz.bus.cache.Options;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Binds the root settings of the Spring Boot authentication integration.
 * <p>
 * Vendor clients use the direct {@code bus.auth.<vendor>} layout. {@link AuthService} binds those entries dynamically
 * from the Vendor identifiers registered in bus-auth, so this class does not duplicate the platform catalog. Optional
 * {@code bus.auth.jwt} settings bind one reusable shared JWT service. {@code bus.auth.guard} defines the service token
 * policy independently of the JWT service.
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
     * Service identity configuration used by the Auth Guard.
     */
    @NestedConfigurationProperty
    private final GuardConfig guard;

    /**
     * Creates immutable root authentication settings.
     *
     * @param enabled whether the complete authentication integration is enabled
     * @param cache   optional authentication-specific atomic cache backend settings
     * @param jwt     optional shared JWT service settings
     * @param guard   optional request and service identity settings from {@code bus.auth.guard}
     */
    @ConstructorBinding
    public AuthProperties(@DefaultValue(Normal.TRUE) boolean enabled, Options cache, Jwt jwt, Guard guard) {
        this.enabled = enabled;
        this.cache = cache;
        this.jwt = jwt;
        this.guard = guard == null ? new GuardConfig() : guard.config();
    }

    /**
     * Creates root authentication settings with the default service identity configuration.
     *
     * @param enabled whether the complete authentication integration is enabled
     * @param cache   optional authentication-specific atomic cache backend settings
     * @param jwt     optional shared JWT service settings
     */
    public AuthProperties(boolean enabled, Options cache, Jwt jwt) {
        this(enabled, cache, jwt, null);
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
        return "AuthProperties[enabled=" + enabled + ", cache=<masked>, jwt=<masked>, guard=<masked>]";
    }

    /**
     * Binds Spring property defaults and converts them to the transport-neutral Guard configuration.
     *
     * @param inbound  inbound caller verification settings
     * @param outbound outbound signing settings
     * @param secret   Base64-encoded key derivation root
     * @param rotate   key rotation period in seconds
     * @param label    signing key derivation label
     * @param bearer   direct client Bearer authentication setting
     * @param apiKey   client API key authentication setting
     * @param signing  client request-signature verification setting
     */
    public record Guard(@DefaultValue Inbound inbound, @DefaultValue Outbound outbound,
            @DefaultValue(Normal.EMPTY) String secret, @DefaultValue("86400") long rotate,
            @DefaultValue("miaixz-bus-anchor-v1:") String label, @DefaultValue Bearer bearer,
            @DefaultValue ApiKey apiKey, @DefaultValue Signing signing) {

        /**
         * Binds the configured switches and defaults absent client checks to enabled.
         */
        @ConstructorBinding
        public Guard {
            bearer = bearer == null ? new Bearer(true, null) : bearer;
            apiKey = apiKey == null ? new ApiKey(true, null) : apiKey;
            signing = signing == null ? new Signing(true, null) : signing;
        }

        /**
         * Creates service identity settings with the default client checks.
         *
         * @param inbound  inbound service token settings
         * @param outbound outbound service token settings
         * @param secret   Base64-encoded key derivation root
         * @param rotate   key rotation period in seconds
         * @param label    signing key derivation label
         */
        public Guard(Inbound inbound, Outbound outbound, String secret, long rotate, String label) {
            this(inbound, outbound, secret, rotate, label, null, null, null);
        }

        /**
         * Returns the configuration used by the Auth Guard beans.
         *
         * @return transport-neutral Guard settings
         */
        private GuardConfig config() {
            return new GuardConfig(
                    new GuardConfig.Inbound(inbound.enabled(), inbound.subjects(), inbound.audience(), inbound.skew(),
                            inbound.rules()),
                    new GuardConfig.Outbound(outbound.enabled(), outbound.subject(), outbound.ttl(), outbound.gateway(),
                            outbound.rules()),
                    secret, rotate, label,
                    bearer.maxLength() == null ? new GuardConfig.Bearer(bearer.enabled())
                            : new GuardConfig.Bearer(bearer.enabled(), bearer.maxLength()),
                    apiKey.maxLength() == null ? new GuardConfig.ApiKey(apiKey.enabled())
                            : new GuardConfig.ApiKey(apiKey.enabled(), apiKey.maxLength()),
                    signing.skewMinutes() == null ? new GuardConfig.Signing(signing.enabled())
                            : new GuardConfig.Signing(signing.enabled(), signing.skewMinutes()));
        }

        /**
         * Returns a diagnostic representation with key material masked.
         *
         * @return safe service identity setting summary
         */
        @Override
        public String toString() {
            return "Guard[secret=<masked>]";
        }

        /**
         * Binds the global verification switch and ingress rules.
         *
         * @param enabled  whether inbound verification is enabled
         * @param subjects accepted caller codes separated by commas
         * @param audience required audience of received tokens
         * @param skew     permitted clock displacement in seconds
         * @param rules    inbound path rules
         */
        public record Inbound(Boolean enabled, @DefaultValue(Normal.EMPTY) String subjects,
                @DefaultValue(Normal.EMPTY) String audience, @DefaultValue(Symbol.FIVE) int skew,
                List<GuardConfig.Incoming> rules) {
        }

        /**
         * Binds the global signing switch and next-hop rules.
         *
         * @param enabled whether outbound issuance is enabled
         * @param subject subject assigned to issued tokens
         * @param ttl     maximum lifetime of an issued token in seconds
         * @param gateway audience of the configured gateway client
         * @param rules   outbound target rules
         */
        public record Outbound(Boolean enabled, @DefaultValue(Normal.EMPTY) String subject,
                @DefaultValue("300") int ttl, @DefaultValue(Normal.EMPTY) String gateway,
                List<GuardConfig.Outgoing> rules) {
        }

        /**
         * Binds direct client Bearer authentication settings.
         *
         * @param enabled   whether the Bearer authentication channel is enabled
         * @param maxLength maximum credential length for V1 request signing
         */
        public record Bearer(@DefaultValue(Normal.TRUE) boolean enabled, Integer maxLength) {
        }

        /**
         * Binds client API key authentication settings.
         *
         * @param enabled   whether the API key authentication channel is enabled
         * @param maxLength maximum credential length for V1 request signing
         */
        public record ApiKey(@DefaultValue(Normal.TRUE) boolean enabled, Integer maxLength) {
        }

        /**
         * Binds client request-signature verification settings.
         *
         * @param enabled     whether request-signature verification is enabled
         * @param skewMinutes maximum accepted client timestamp difference in minutes
         */
        public record Signing(@DefaultValue(Normal.TRUE) boolean enabled, Integer skewMinutes) {
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
     * Carries optional shared HS256 JWT settings from {@code bus.auth.jwt}.
     *
     * @param secret       exact non-empty String key material
     * @param secretPolicy compatibility or strict input-length policy
     * @author Kimi Liu
     */
    public record Jwt(String secret, SecretPolicy secretPolicy) {

        /**
         * Applies the compatibility policy when no explicit policy is configured.
         *
         * @param secret       exact non-empty String key material
         * @param secretPolicy compatibility or strict input-length policy
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
         *
         * @param enabled      whether this Vendor client is enabled
         * @param clientId     public client identifier issued by the external platform
         * @param clientSecret client secret supplied by the protected configuration source
         * @param redirectUri  exact callback URI registered with the external platform
         * @param scopes       ordered explicit scopes, or an empty list for manifest defaults
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
