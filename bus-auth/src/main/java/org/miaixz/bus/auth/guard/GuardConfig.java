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
package org.miaixz.bus.auth.guard;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.auth.guard.GuardEvaluator.Mode;
import org.miaixz.bus.auth.guard.GuardEvaluator.Rule;
import org.miaixz.bus.auth.guard.inbound.InboundPolicy;
import org.miaixz.bus.auth.guard.outbound.OutboundPolicy;
import org.miaixz.bus.auth.guard.token.DerivedKeyProvider;
import org.miaixz.bus.auth.guard.token.Hs256TokenProvider;
import org.miaixz.bus.auth.guard.token.ServiceKeyProvider;
import org.miaixz.bus.auth.guard.token.ServiceTokenProvider;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Transport-neutral configuration for service identity and client request checks.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class GuardConfig {

    /**
     * Inbound caller verification settings.
     */
    private final Inbound inbound;

    /**
     * Outbound identity signing settings.
     */
    private final Outbound outbound;

    /**
     * Direct client Bearer authentication settings.
     */
    @Builder.Default
    private final Bearer bearer = new Bearer(true);

    /**
     * Client API key authentication settings.
     */
    @Builder.Default
    private final ApiKey apiKey = new ApiKey(true);

    /**
     * Client request-signature verification settings.
     */
    @Builder.Default
    private final Signing signing = new Signing(true);

    /**
     * Shared signing root encoded as Base64.
     */
    private final String secret;

    /**
     * Effective key rotation period in seconds.
     */
    private final long rotate;

    /**
     * Domain separation label for derived signing keys.
     */
    private final String label;

    /**
     * Creates defaults with both service identity directions disabled.
     */
    public GuardConfig() {
        this(new Inbound(false, Normal.EMPTY, Normal.EMPTY, Integer.parseInt(Symbol.FIVE), List.of()),
                new Outbound(false, Normal.EMPTY, 300, Normal.EMPTY, List.of()), Normal.EMPTY, 86_400L,
                "miaixz-bus-anchor-v1:", new Bearer(true), new ApiKey(true), new Signing(true));
    }

    /**
     * Configures service identity and the independent client request checks.
     *
     * @param inbound  inbound service token settings
     * @param outbound outbound service token settings
     * @param secret   Base64-encoded service token derivation root
     * @param rotate   derived-key rotation period in seconds
     * @param label    domain separation label
     * @param bearer   direct client Bearer authentication settings
     * @param apiKey   client API key authentication settings
     * @param signing  client request-signature verification settings
     */
    public GuardConfig(Inbound inbound, Outbound outbound, String secret, long rotate, String label, Bearer bearer,
            ApiKey apiKey, Signing signing) {
        this.inbound = inbound;
        this.outbound = outbound;
        this.secret = secret;
        this.rotate = rotate;
        this.label = label;
        this.bearer = bearer;
        this.apiKey = apiKey;
        this.signing = signing;
    }

    /**
     * Inbound caller identity settings.
     */
    @Getter
    @Accessors(fluent = true)
    public static class Inbound {

        /**
         * Whether inbound verification is enabled.
         */
        private final Boolean enabled;

        /**
         * Default accepted subject codes, separated by commas.
         */
        private final String subjects;

        /**
         * Required audience for this application.
         */
        private final String audience;

        /**
         * Accepted clock displacement in seconds.
         */
        private final int skew;

        /**
         * Explicit inbound path rules.
         */
        private final List<Incoming> rules;

        /**
         * Configures the inbound service identity options.
         *
         * @param enabled  whether inbound caller verification is enabled
         * @param subjects accepted subject codes separated by commas
         * @param audience required audience for this application
         * @param skew     accepted clock displacement in seconds
         * @param rules    explicit inbound path rules
         */

        public Inbound(Boolean enabled, String subjects, String audience, int skew, List<Incoming> rules) {
            this.enabled = enabled;
            this.subjects = subjects;
            this.audience = audience;
            this.skew = skew;
            this.rules = rules == null ? List.of() : List.copyOf(rules);
        }

    }

    /**
     * Outbound service identity settings.
     */
    @Getter
    @Accessors(fluent = true)
    public static class Outbound {

        /**
         * Whether outbound identity signing is enabled.
         */
        private final Boolean enabled;

        /**
         * Subject attached to issued tokens.
         */
        private final String subject;

        /**
         * Token lifetime in seconds.
         */
        private final int ttl;

        /**
         * Destination of the configured gateway client.
         */
        private final String gateway;

        /**
         * Explicit outbound target rules.
         */
        private final List<Outgoing> rules;

        /**
         * Configures the outbound service identity options.
         *
         * @param enabled whether outbound signing is enabled
         * @param subject subject attached to issued tokens
         * @param ttl     token lifetime in seconds
         * @param gateway destination of the configured gateway client
         * @param rules   explicit outbound target rules
         */

        public Outbound(Boolean enabled, String subject, int ttl, String gateway, List<Outgoing> rules) {
            this.enabled = enabled;
            this.subject = subject;
            this.ttl = ttl;
            this.gateway = gateway;
            this.rules = rules == null ? List.of() : List.copyOf(rules);
        }

    }

    /**
     * Direct client Bearer authentication settings.
     */
    @Getter
    @Accessors(fluent = true)
    public static class Bearer {

        /**
         * Whether direct client Bearer authentication is enabled.
         */
        private final boolean enabled;

        /**
         * Maximum Bearer credential length for V1 request-signing key derivation.
         */
        private final int maxLength;

        /**
         * Configures the Bearer authentication channel.
         *
         * @param enabled whether to accept direct client Bearer authentication
         */
        public Bearer(boolean enabled) {
            this(enabled, Normal._8192);
        }

        /**
         * Configures the Bearer channel and its V1 signing limit.
         *
         * @param enabled   whether to accept direct client Bearer authentication
         * @param maxLength positive maximum number of characters accepted for V1 signing
         */
        public Bearer(boolean enabled, int maxLength) {
            if (maxLength <= 0) {
                throw new IllegalArgumentException("Bearer maximum length must be positive");
            }
            this.enabled = enabled;
            this.maxLength = maxLength;
        }

    }

    /**
     * Client API key authentication settings.
     */
    @Getter
    @Accessors(fluent = true)
    public static class ApiKey {

        /**
         * Whether client API key authentication is enabled.
         */
        private final boolean enabled;

        /**
         * Maximum API key length for V1 request-signing key derivation.
         */
        private final int maxLength;

        /**
         * Configures the API key authentication channel.
         *
         * @param enabled whether to accept client API key authentication
         */
        public ApiKey(boolean enabled) {
            this(enabled, Normal._512);
        }

        /**
         * Configures the API key channel and its V1 signing limit.
         *
         * @param enabled   whether to accept client API key authentication
         * @param maxLength positive maximum number of characters accepted for V1 signing
         */
        public ApiKey(boolean enabled, int maxLength) {
            if (maxLength <= 0) {
                throw new IllegalArgumentException("API key maximum length must be positive");
            }
            this.enabled = enabled;
            this.maxLength = maxLength;
        }

    }

    /**
     * Client request-signature verification settings.
     */
    @Getter
    @Accessors(fluent = true)
    public static class Signing {

        /**
         * Whether the caller should apply an asset's request-signature requirement.
         */
        private final boolean enabled;

        /**
         * Maximum allowed timestamp difference between client and server, in minutes.
         */
        private final int skewMinutes;

        /**
         * Configures request-signature verification with the default timestamp window.
         *
         * @param enabled whether to verify a client signature when required by the asset
         */
        public Signing(boolean enabled) {
            this(enabled, 30);
        }

        /**
         * Configures request-signature verification and timestamp tolerance.
         *
         * @param enabled     whether to verify a client signature when required by the asset
         * @param skewMinutes positive maximum timestamp difference in minutes
         */
        public Signing(boolean enabled, int skewMinutes) {
            if (skewMinutes <= 0) {
                throw new IllegalArgumentException("Signing timestamp tolerance must be positive");
            }
            this.enabled = enabled;
            this.skewMinutes = skewMinutes;
        }

    }

    /**
     * Configures one Servlet or Vortex ingress path.
     *
     * @param path     route path pattern
     * @param mode     inbound identity requirement
     * @param subjects allowed caller codes
     * @param origins  allowed original callers for forwarded requests
     */
    public record Incoming(String path, Mode mode, Set<String> subjects, Set<String> origins) {

        /**
         * Creates an ingress rule with immutable caller and origin sets.
         *
         * @param path     route path pattern
         * @param mode     inbound identity requirement
         * @param subjects permitted caller codes, or {@code null} to use the default
         * @param origins  permitted original callers, or {@code null} for an empty set
         */
        public Incoming {
            subjects = subjects == null ? Set.of() : Set.copyOf(subjects);
            origins = origins == null ? Set.of() : Set.copyOf(origins);
        }

    }

    /**
     * Declares an endpoint that requires an admitted service caller in addition to the token policy.
     *
     * @param path                     exact context-relative request path
     * @param origins                  allowed original callers when forwarded identity is required
     * @param directGetParameter       parameter that permits a direct caller on a GET request
     * @param localReadWithoutIdentity whether that same GET may read from an exclusively loopback listener
     */
    public record Admission(String path, Set<String> origins, String directGetParameter,
            boolean localReadWithoutIdentity) {

        /**
         * Creates an endpoint admission rule with an immutable origin allowlist.
         */
        public Admission {
            if (path == null || !path.startsWith(Symbol.SLASH)) {
                throw new IllegalArgumentException("Service admission requires a context-relative path");
            }
            if (localReadWithoutIdentity && (directGetParameter == null || directGetParameter.isBlank())) {
                throw new IllegalArgumentException("Local read requires a GET selector");
            }
            origins = origins == null ? Set.of() : Set.copyOf(origins);
        }

        /**
         * Evaluates admission using only verified service identity or confirmed listener and peer addresses.
         *
         * @param caller     verified service caller, if any
         * @param directRead whether the request matches the configured GET selector
         * @param local      whether both endpoints belong to the exclusively loopback listener
         * @return whether the request can enter the endpoint
         */
        public boolean admits(GuardEvaluator.Verified caller, boolean directRead, boolean local) {
            if (caller == null) {
                return directRead && localReadWithoutIdentity && local;
            }
            return directRead && caller.origin() == null
                    || CallerValidator.matches(caller, Set.of(), origins, !origins.isEmpty());
        }

    }

    /**
     * Configures an outgoing route and its approved destinations.
     *
     * @param path      upstream request path pattern
     * @param route     optional source gateway path pattern
     * @param mode      outbound identity requirement
     * @param audiences trusted destination codes
     */
    public record Outgoing(String path, String route, Mode mode, Set<String> audiences) {

        /**
         * Creates an outgoing rule with an immutable destination set.
         *
         * @param path      upstream request path pattern
         * @param route     optional source gateway path pattern
         * @param mode      outbound identity requirement
         * @param audiences trusted destination codes, or {@code null} for an empty set
         */
        public Outgoing {
            audiences = audiences == null ? Set.of() : Set.copyOf(audiences);
        }

    }

    /**
     * Reports the configured switch for inbound service token verification.
     *
     * @return whether inbound verification is explicitly enabled
     */
    public boolean inboundEnabled() {
        return Boolean.TRUE.equals(inbound.enabled());
    }

    /**
     * Reports the configured switch for outbound service token issuance.
     *
     * @return whether outbound issuance is explicitly enabled
     */
    public boolean outboundEnabled() {
        return Boolean.TRUE.equals(outbound.enabled());
    }

    /**
     * Reports whether direct client Bearer authentication is enabled.
     *
     * @return whether to use Bearer credentials for client authentication
     */
    public boolean isBearerEnabled() {
        return bearer.enabled();
    }

    /**
     * Reports whether client API key authentication is enabled.
     *
     * @return whether to use API keys for client authentication
     */
    public boolean isApiKeyEnabled() {
        return apiKey.enabled();
    }

    /**
     * Reports whether client request-signature verification is enabled.
     *
     * @return whether to verify client request signatures required by an asset
     */
    public boolean isSigningEnabled() {
        return signing.enabled();
    }

    /**
     * Creates the shared identity evaluator from the two independently configured directions.
     *
     * @return service identity evaluator for the application
     */
    public GuardEvaluator identity() {
        return identity(inboundEnabled(), outboundEnabled());
    }

    /**
     * Creates the default key provider for the bound service identity directions.
     *
     * @return derived keys or an empty provider when both directions are disabled
     */
    public ServiceKeyProvider keyProvider() {
        return defaultKeys(inboundEnabled() || outboundEnabled());
    }

    /**
     * Creates the evaluator with independently selected providers.
     *
     * @param keys   trusted key provider
     * @param tokens cryptographic token provider
     * @return service identity evaluator for the bound directions
     */
    public GuardEvaluator identity(ServiceKeyProvider keys, ServiceTokenProvider tokens) {
        return identity(inboundEnabled(), outboundEnabled(), keys, tokens);
    }

    /**
     * Creates the transport-neutral identity for the directions selected by an adapter.
     *
     * @param in  whether the adapter enables inbound verification
     * @param out whether the adapter enables outbound signing
     * @return shared authentication and issuance component
     */
    public GuardEvaluator identity(boolean in, boolean out) {
        return identity(in, out, defaultKeys(in || out), new Hs256TokenProvider());
    }

    /**
     * Connects effective direction rules to the selected providers.
     *
     * @param in     whether inbound verification is enabled
     * @param out    whether outbound signing is enabled
     * @param keys   trusted key provider
     * @param tokens cryptographic token provider
     * @return evaluator for the effective rules
     */
    private GuardEvaluator identity(boolean in, boolean out, ServiceKeyProvider keys, ServiceTokenProvider tokens) {
        List<Rule> incoming = inbound.rules().stream()
                .map(rule -> new Rule(rule.path(), null, rule.mode(), rule.subjects(), rule.origins())).toList();
        List<Rule> outgoing = outbound.rules().stream()
                .map(rule -> new Rule(rule.path(), rule.route(), rule.mode(), rule.audiences())).toList();
        return new GuardEvaluator(
                new InboundPolicy(in, inbound.audience(), values(inbound.subjects()),
                        Duration.ofSeconds(inbound.skew()), incoming),
                new OutboundPolicy(out, outbound.subject(), Duration.ofSeconds(outbound.ttl()), outgoing), keys,
                tokens);
    }

    /**
     * Derives keys only when a direction requires service identity material.
     *
     * @param enabled whether service identity is enabled
     * @return default local key provider for the effective directions
     */
    private ServiceKeyProvider defaultKeys(boolean enabled) {
        return enabled ? new DerivedKeyProvider(secret, rotate, label)
                : now -> new ServiceKeyProvider.Snapshot(null, List.of());
    }

    /**
     * Reads the default allowed subject codes for inbound rules.
     *
     * @param text comma-separated configuration
     * @return distinct nonempty values
     */
    private static Set<String> values(String text) {
        return Arrays.stream(text.split(Symbol.COMMA)).map(String::strip).filter(value -> !value.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

}
