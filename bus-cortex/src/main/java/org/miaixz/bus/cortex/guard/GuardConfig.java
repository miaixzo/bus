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
package org.miaixz.bus.cortex.guard;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.experimental.Accessors;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.cortex.guard.GuardEvaluator.Mode;
import org.miaixz.bus.cortex.guard.GuardEvaluator.Rule;
import org.miaixz.bus.cortex.guard.inbound.InboundPolicy;
import org.miaixz.bus.cortex.guard.outbound.OutboundPolicy;
import org.miaixz.bus.cortex.guard.token.DerivedKeyProvider;
import org.miaixz.bus.cortex.guard.token.Hs256TokenProvider;
import org.miaixz.bus.cortex.guard.token.ServiceKeyProvider;
import org.miaixz.bus.cortex.guard.token.ServiceTokenProvider;

/**
 * Transport-neutral configuration for service identity.
 *
 * @author Kimi Liu
 */
@Getter
@Accessors(fluent = true)
public class GuardConfig {

    /**
     * Default rotation period in seconds, expressed as a property value.
     */
    protected static final String DEFAULT_ROTATE = "86400";

    /**
     * Default token lifetime in seconds, expressed as a property value.
     */
    protected static final String DEFAULT_TTL = "300";

    /**
     * Inbound caller verification settings.
     */
    private final Inbound inbound;

    /**
     * Outbound identity signing settings.
     */
    private final Outbound outbound;

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
                new Outbound(false, Normal.EMPTY, Integer.parseInt(DEFAULT_TTL), Normal.EMPTY, List.of()), Normal.EMPTY,
                Long.parseLong(DEFAULT_ROTATE), GuardEvaluator.DEFAULT_LABEL);
    }

    /**
     * Configures the service identity directions and default key rotation.
     *
     * @param inbound  inbound caller verification settings
     * @param outbound outbound signing settings
     * @param secret   shared Base64-encoded signing root
     * @param rotate   derived-key rotation period in seconds
     * @param label    domain separation label
     */

    public GuardConfig(Inbound inbound, Outbound outbound, String secret, long rotate, String label) {
        this.inbound = inbound;
        this.outbound = outbound;
        this.secret = secret;
        this.rotate = rotate;
        this.label = label;
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
     * Creates the shared identity evaluator from the two independently configured directions.
     *
     * @return service identity evaluator for the application
     */
    public GuardEvaluator identity() {
        return identity(Boolean.TRUE.equals(inbound.enabled()), Boolean.TRUE.equals(outbound.enabled()));
    }

    /**
     * Creates the default key provider for the bound service identity directions.
     *
     * @return derived keys or an empty provider when both directions are disabled
     */
    public ServiceKeyProvider keyProvider() {
        return defaultKeys(Boolean.TRUE.equals(inbound.enabled()) || Boolean.TRUE.equals(outbound.enabled()));
    }

    /**
     * Creates the evaluator with independently selected providers.
     *
     * @param keys   trusted key provider
     * @param tokens cryptographic token provider
     * @return service identity evaluator for the bound directions
     */
    public GuardEvaluator identity(ServiceKeyProvider keys, ServiceTokenProvider tokens) {
        return identity(Boolean.TRUE.equals(inbound.enabled()), Boolean.TRUE.equals(outbound.enabled()), keys, tokens);
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
