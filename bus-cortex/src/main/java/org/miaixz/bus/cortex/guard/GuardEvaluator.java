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
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.cortex.guard.inbound.InboundDecision;
import org.miaixz.bus.cortex.guard.inbound.InboundEvaluator;
import org.miaixz.bus.cortex.guard.inbound.InboundEvaluator.Received;
import org.miaixz.bus.cortex.guard.inbound.InboundPolicy;
import org.miaixz.bus.cortex.guard.outbound.OutboundIssuer;
import org.miaixz.bus.cortex.guard.outbound.OutboundPolicy;
import org.miaixz.bus.cortex.guard.token.DerivedKeyProvider;
import org.miaixz.bus.cortex.guard.token.Hs256TokenProvider;
import org.miaixz.bus.cortex.guard.token.ServiceKeyProvider;
import org.miaixz.bus.cortex.guard.token.ServiceTokenProvider;
import org.miaixz.bus.cortex.registry.MetadataCodec;

/**
 * Coordinates inbound service identity, outbound token issuance, and shared HTTP header handling.
 *
 * @author Kimi Liu
 */
public final class GuardEvaluator {

    /**
     * Stable separation label shared by deployments without a custom value.
     */
    public static final String DEFAULT_LABEL = "miaixz-bus-anchor-v1:";

    /**
     * Request attribute holding the verified service identity.
     */
    public static final String VERIFIED_ATTRIBUTE = "org.miaixz.bus.cortex.guard.GuardEvaluator.verified";

    /**
     * Resolves inbound policy and verifies the service token.
     */
    private final InboundEvaluator inbound;

    /**
     * Resolves the next-hop policy and issues the outbound token.
     */
    private final OutboundIssuer outbound;

    /**
     * Whether the shared inbound evaluator is enabled.
     */
    private final boolean inboundEnabled;

    /**
     * Whether the shared outbound issuer is enabled.
     */
    private final boolean outboundEnabled;

    /**
     * Creates a shared identity configured for one or both directions.
     *
     * @param secret   Base64 deployment root
     * @param rotate   rotation period in seconds
     * @param label    derivation label
     * @param inbound  inbound service policy
     * @param outbound outbound service policy
     */
    public GuardEvaluator(String secret, long rotate, String label, InboundPolicy inbound, OutboundPolicy outbound) {
        this(inbound, outbound, inbound.enabled() || outbound.enabled() ? new DerivedKeyProvider(secret, rotate, label)
                : now -> new ServiceKeyProvider.Snapshot(null, List.of()), new Hs256TokenProvider());
    }

    /**
     * Creates an evaluator with independently selected key and token providers.
     *
     * @param inbound  inbound service policy
     * @param outbound outbound service policy
     * @param keys     trusted key source for this application context
     * @param tokens   cryptographic service token provider
     * @throws IllegalStateException if an enabled direction has no active key
     */
    public GuardEvaluator(InboundPolicy inbound, OutboundPolicy outbound, ServiceKeyProvider keys,
            ServiceTokenProvider tokens) {
        this.inbound = new InboundEvaluator(inbound, keys, tokens);
        this.outbound = new OutboundIssuer(outbound, keys, tokens);
        this.inboundEnabled = inbound.enabled();
        this.outboundEnabled = outbound.enabled();
        if (inboundEnabled || outboundEnabled) {
            Instant now = Instant.now();
            ServiceKeyProvider.Snapshot snapshot = Objects.requireNonNull(keys.keys(now));
            if (inboundEnabled && snapshot.verification().stream().noneMatch(key -> key.active(now))
                    || outboundEnabled && (snapshot.signing() == null || !snapshot.signing().active(now)))
                throw new IllegalStateException("Required service identity is unavailable");
        }
    }

    /**
     * Creates an identity with both direction policies inactive.
     *
     * @return identity suitable for an application without configured service trust
     */
    public static GuardEvaluator disabled() {
        return new GuardEvaluator(null, Normal._1, DEFAULT_LABEL,
                new InboundPolicy(false, null, Set.of(), Duration.ZERO, List.of()),
                new OutboundPolicy(false, null, Duration.ZERO, List.of()));
    }

    /**
     * Collects the standard header values needed before a gateway can resolve the effective asset policy. For an
     * unresolved asset, supplied values are retained so its effective requirement can be evaluated after routing.
     *
     * @param path         context-relative ingress path
     * @param fixed        fixed route requirement, or null for configurable paths
     * @param assetPending whether a resolved asset can change the effective identity mode
     * @param read         case-insensitive transport header lookup
     * @return header presence for sanitization and the values needed by the later authentication stage
     */
    public Received receive(String path, Mode fixed, boolean assetPending, FunctionX<String, List<String>> read) {
        if (!inboundEnabled)
            return new Received(false, List.of());
        List<String> values = read.apply(Http.Auth.SERVICE_TOKEN_HEADER);
        if (values == null)
            return new Received(false, List.of());
        boolean retain = assetPending || inbound.mode(path, fixed) != Mode.OFF;
        return new Received(true, retain ? values : List.of());
    }

    /**
     * Reads the configured ingress header and authenticates it against an application-relative path.
     *
     * @param path   context-relative ingress path
     * @param header configured header name, or null for the standard header
     * @param read   case-insensitive transport header lookup
     * @return the effective authentication decision
     */
    public InboundDecision acceptHeaders(String path, String header, FunctionX<String, List<String>> read) {
        if (!inboundEnabled)
            return new InboundDecision(InboundDecision.Status.ACCEPTED, null);
        List<String> values = read.apply(headerName(header));
        return accept(path, values == null ? List.of() : values, null, null);
    }

    /**
     * Identifies the header names an adapter must hide from downstream handlers.
     *
     * @param header configured header name, or null for the standard header
     * @param name   candidate header name
     * @return whether the name is reserved for service identity
     */
    public boolean reservesHeader(String header, String name) {
        return Http.Auth.SERVICE_TOKEN_HEADER.equalsIgnoreCase(name) || headerName(header).equalsIgnoreCase(name);
    }

    /**
     * Removes incoming service identity headers from a mutable transport view.
     *
     * @param header configured header name, or null for the standard header
     * @param names  current names in the transport
     * @param remove transport header removal
     */
    public void clearHeaders(String header, Collection<String> names, Consumer<String> remove) {
        names.stream().filter(candidate -> reservesHeader(header, candidate)).toList().forEach(remove);
    }

    /**
     * Clears any reserved values and writes the credential issued for the next hop, if one was needed.
     *
     * @param header configured header name, or null for the standard header
     * @param token  newly signed next-hop token, or null to leave headers clear
     * @param names  current names in the transport
     * @param remove transport header removal
     * @param set    transport's single-value header writer
     */
    public void forwardHeaders(
            String header,
            String token,
            Collection<String> names,
            ConsumerX<String> remove,
            BiConsumerX<String, String> set) {
        clearHeaders(header, names, remove);
        if (token != null)
            set.accept(headerName(header), token);
    }

    /**
     * Resolves the active service token header name.
     *
     * @param header configured name, or null to use the standard name
     * @return active header name
     */
    private static String headerName(String header) {
        return header == null ? Http.Auth.SERVICE_TOKEN_HEADER : header;
    }

    /**
     * Evaluates configured inbound caller identity before business authorization.
     *
     * @param path   effective context-relative route path
     * @param tokens every instance of the service identity header
     * @param fixed  fixed route boundary, or null for rule-based selection
     * @param asset  policy from the selected asset, or null
     * @return decision and verified identity, if present
     */
    public InboundDecision accept(String path, List<String> tokens, Mode fixed, Asset asset) {
        return inboundEnabled ? inbound.evaluate(path, tokens, fixed, asset)
                : new InboundDecision(InboundDecision.Status.ACCEPTED, null);
    }

    /**
     * Evaluates an outbound target and issues only when required for that target.
     *
     * @param route           original gateway route, or null for a direct client
     * @param audience        trusted destination service code
     * @param path            context-relative destination path
     * @param fixed           fixed channel requirement, or null
     * @param asset           outbound mode declared by the asset, or null
     * @param assetControlled whether only opted-in assets may receive tokens on this route
     * @return newly scoped token or null when the request does not use a service token
     * @throws IllegalStateException when a required token cannot be issued
     */
    public String outbound(
            String route,
            String audience,
            String path,
            Mode fixed,
            Mode asset,
            boolean assetControlled) {
        return outbound(route, audience, path, fixed, asset, assetControlled, null);
    }

    /**
     * Evaluates outbound rules and preserves the subject authenticated to this hop when forwarding.
     *
     * @param route           original gateway route, or null for a direct client
     * @param audience        trusted destination service code
     * @param path            context-relative destination path
     * @param fixed           fixed channel requirement, or null
     * @param asset           outbound mode declared by the asset, or null
     * @param assetControlled whether this route requires an explicit asset opt-in
     * @param caller          service identity already verified at this hop, or null for a direct client
     * @return newly scoped token or null when the request does not use a service token
     * @throws IllegalStateException when a required token cannot be issued
     */
    public String outbound(
            String route,
            String audience,
            String path,
            Mode fixed,
            Mode asset,
            boolean assetControlled,
            Verified caller) {
        return outbound(route, audience, path, fixed, asset, assetControlled, caller, null);
    }

    /**
     * Signs the authenticated caller and selected route together for the destination service.
     *
     * @param route           original gateway route, or null for a direct client
     * @param audience        trusted destination service code
     * @param path            context-relative destination path
     * @param fixed           fixed channel requirement, or null
     * @param asset           outbound mode declared by the asset, or null
     * @param assetControlled whether this route requires an explicit asset opt-in
     * @param caller          service identity verified at this hop, or null for a direct client
     * @param assetId         identifier selected by the forwarding hop, or null when omitted
     * @return compact token or null when the route does not use service identity
     * @throws IllegalStateException when a required token cannot be issued
     */
    public String outbound(
            String route,
            String audience,
            String path,
            Mode fixed,
            Mode asset,
            boolean assetControlled,
            Verified caller,
            String assetId) {
        return outboundEnabled ? outbound.issue(route, audience, path, fixed, asset, assetControlled, caller, assetId)
                : null;
    }

    /**
     * Issues an identity for a previously selected trusted destination. Entry adapters should use
     * {@link #outbound(String, String, String, Mode, Mode, boolean)} to select that destination.
     *
     * @param audience verified destination service code
     * @return compact service token, or null when outbound issuance is disabled
     */
    public String issue(String audience) {
        return outboundEnabled ? outbound.issue(audience) : null;
    }

    /**
     * Reads the service identity policy from a registered asset's metadata.
     *
     * @param metadata registered asset JSON text
     * @return explicit policy, or null when the asset has no identity settings
     */
    public static Asset asset(String metadata) {
        return asset(metadata, true, true);
    }

    /**
     * Reads the registered identity rules only for the selected directions.
     *
     * @param metadata     registered asset metadata
     * @param readInbound  whether to read inbound rules and caller restrictions
     * @param readOutbound whether to read outbound rules
     * @return rules for the selected directions, or null when no selected rules are present
     */
    public static Asset asset(String metadata, boolean readInbound, boolean readOutbound) {
        if (!readInbound && !readOutbound)
            return null;
        var cortex = section(MetadataCodec.root(metadata), MetadataCodec.ROOT);
        var policy = section(cortex, "serviceIdentity");
        if (policy == null)
            return null;
        if (!policy.containsKey("inbound") && !policy.containsKey("outbound"))
            throw new IllegalArgumentException("Asset service identity policy requires a direction");
        var inbound = readInbound ? section(policy, "inbound") : null;
        var outbound = readOutbound ? section(policy, "outbound") : null;
        return inbound == null && outbound == null ? null : new Asset(mode(inbound), subjects(inbound), mode(outbound));
    }

    /**
     * Reports the global verification switch independently of route policy.
     *
     * @return whether inbound service token verification is enabled
     */
    public boolean inboundEnabled() {
        return inboundEnabled;
    }

    /**
     * Reports the global issuance switch independently of destination policy.
     *
     * @return whether outbound service token issuance is enabled
     */
    public boolean outboundEnabled() {
        return outboundEnabled;
    }

    /**
     * Reads an optional object without treating an invalid declaration as an omitted rule.
     *
     * @param parent containing object
     * @param name   field name
     * @return nested object or null when absent
     */
    private static Map<String, Object> section(Map<String, Object> parent, String name) {
        if (parent == null || !parent.containsKey(name))
            return null;
        if (parent.get(name) instanceof Map<?, ?> section)
            return MetadataCodec.object(section);
        throw new IllegalArgumentException("Invalid asset service identity configuration");
    }

    /**
     * Reads the mode from one optional direction object.
     *
     * @param direction registered direction fields
     * @return configured mode or null when the direction is absent
     */
    private static Mode mode(Map<String, Object> direction) {
        if (direction == null)
            return null;
        if (direction.get("mode") instanceof String text)
            return Mode.valueOf(text.toUpperCase(Locale.ROOT));
        throw new IllegalArgumentException("Invalid asset service identity mode");
    }

    /**
     * Reads and validates the asset caller whitelist.
     *
     * @param direction registered inbound fields
     * @return caller codes, or the empty default
     */
    private static Set<String> subjects(Map<String, Object> direction) {
        if (direction == null || !direction.containsKey("subjects"))
            return Set.of();
        if (!(direction.get("subjects") instanceof List<?> values) || values.isEmpty()
                || values.stream().anyMatch(value -> !(value instanceof String code) || code.isBlank()))
            throw new IllegalArgumentException("Invalid asset service identity subjects");
        return Set.copyOf(values.stream().map(String.class::cast).toList());
    }

    /**
     * Registered per-asset overrides.
     *
     * @param inbound  inbound mode
     * @param subjects accepted caller codes
     * @param outbound outbound mode
     */
    public record Asset(Mode inbound, Set<String> subjects, Mode outbound) {

        /**
         * Validates and copies the registered overrides.
         *
         * @param inbound  inbound identity requirement
         * @param subjects accepted caller codes
         * @param outbound outbound identity requirement
         * @throws IllegalArgumentException if the outbound mode is {@link Mode#OPTIONAL}
         */
        public Asset {
            subjects = Set.copyOf(subjects);
            if (outbound == Mode.OPTIONAL)
                throw new IllegalArgumentException("Invalid asset outbound service identity mode");
        }

    }

    /**
     * Service identity requirement for a direction.
     */
    public enum Mode {
        /**
         * Pass through without using a service identity.
         */
        OFF,
        /**
         * Check an inbound identity when supplied.
         */
        OPTIONAL,
        /**
         * Require service identity for the request.
         */
        REQUIRED
    }

    /**
     * Path condition shared by the two service identity directions.
     *
     * @param path    context-relative path or descendant pattern
     * @param route   optional source route for outbound rules
     * @param mode    requested service identity handling
     * @param codes   allowed callers or destinations depending on direction
     * @param origins allowed original callers for forwarded requests
     */
    public record Rule(String path, String route, Mode mode, Set<String> codes, Set<String> origins) {

        /**
         * Segment-based descendant pattern suffix.
         */
        private static final String DESCENDANTS = Symbol.SLASH + Symbol.STAR + Symbol.STAR;

        /**
         * Finds the highest-priority rule for an address.
         *
         * @param rules    applicable rules
         * @param route    original gateway route
         * @param audience trusted destination code for outbound selection
         * @param path     context-relative HTTP path
         * @return matching rule or null if none
         */
        public static Rule match(List<Rule> rules, String route, String audience, String path) {
            return rules.stream().filter(rule -> rule.matches(route, path))
                    .filter(rule -> audience == null || rule.codes().isEmpty() || rule.codes().contains(audience))
                    .max(
                            Comparator.comparingInt((Rule rule) -> priority(rule.route()))
                                    .thenComparingInt(rule -> priority(rule.path())))
                    .orElse(null);
        }

        /**
         * Rejects indistinguishable rule coordinates with conflicting effects.
         *
         * @param rules    rules for one direction
         * @param outbound whether codes designate disjoint targets
         * @return immutable rule list
         * @throws IllegalArgumentException if a rule is invalid for this direction or conflicts with another rule
         */
        public static List<Rule> validateRules(List<Rule> rules, boolean outbound) {
            List<Rule> copy = List.copyOf(rules);
            for (int i = 0; i < copy.size(); i++) {
                Rule rule = copy.get(i);
                if (outbound && (rule.mode() == Mode.OPTIONAL || rule.mode() == Mode.REQUIRED && rule.codes().isEmpty())
                        || !outbound && (rule.path() == null || rule.route() != null))
                    throw new IllegalArgumentException("Invalid service identity rule");
                for (Rule other : copy.subList(i + 1, copy.size())) {
                    if (Objects.equals(rule.path(), other.path()) && Objects.equals(rule.route(), other.route())
                            && !rule.equals(other) && (!outbound || rule.codes().isEmpty() || other.codes().isEmpty()
                                    || rule.codes().stream().anyMatch(other.codes()::contains)))
                        throw new IllegalArgumentException("Conflicting service identity rules");
                }
            }
            return copy;
        }

        /**
         * Validates a rule and takes immutable copies of its caller or destination codes.
         *
         * @param path    context-relative path or descendant pattern
         * @param route   optional source route for outbound rules
         * @param mode    requested service identity handling
         * @param codes   allowed callers or destinations depending on direction
         * @param origins allowed original callers for forwarded requests
         * @throws IllegalArgumentException if a path pattern or code is invalid, or if origins are configured for a
         *                                  mode other than {@link Mode#REQUIRED}
         */
        public Rule {
            Objects.requireNonNull(mode);
            codes = Set.copyOf(codes);
            origins = Set.copyOf(origins);
            validatePattern(path);
            validatePattern(route);
            if (codes.stream().anyMatch(String::isBlank) || origins.stream().anyMatch(String::isBlank)
                    || !origins.isEmpty() && mode != Mode.REQUIRED)
                throw new IllegalArgumentException("Invalid service identity rule codes");
        }

        /**
         * Creates a rule with no original caller restriction.
         *
         * @param path  context-relative path or descendant pattern
         * @param route optional source route for outbound rules
         * @param mode  requested service identity handling
         * @param codes allowed callers or destinations depending on direction
         * @throws IllegalArgumentException if a path pattern or code is invalid
         */
        public Rule(String path, String route, Mode mode, Set<String> codes) {
            this(path, route, mode, codes, Set.of());
        }

        /**
         * Assigns exact paths precedence over descendant patterns and unspecified coordinates.
         *
         * @param pattern rule path or route
         * @return comparison priority
         */
        private static int priority(String pattern) {
            if (pattern == null)
                return 0;
            return pattern.endsWith(DESCENDANTS) ? pattern.length() : Integer.MAX_VALUE;
        }

        /**
         * Checks a path against a configuration pattern at a segment boundary.
         *
         * @param pattern exact path, descendant pattern, or null
         * @param path    context-relative path
         * @return whether this coordinate matches
         */
        private static boolean matchesCoordinate(String pattern, String path) {
            if (pattern == null)
                return true;
            if (path == null)
                return false;
            if (!pattern.endsWith(DESCENDANTS))
                return pattern.equals(path);
            String prefix = pattern.substring(0, pattern.length() - DESCENDANTS.length());
            return path.equals(prefix) || path.startsWith(prefix + Symbol.SLASH);
        }

        /**
         * Validates a supported absolute path pattern.
         *
         * @param pattern configured path or route
         */
        private static void validatePattern(String pattern) {
            if (pattern == null)
                return;
            String base = pattern.endsWith(DESCENDANTS) ? pattern.substring(0, pattern.length() - DESCENDANTS.length())
                    : pattern;
            if (!pattern.startsWith(Symbol.SLASH) || base.contains(Symbol.FORWARDSLASH) || base.contains(Symbol.STAR)
                    || base.contains(Symbol.QUESTION_MARK) || base.contains(Symbol.HASH)
                    || base.contains(Symbol.BACKSLASH) || base.contains(Symbol.PERCENT)
                    || base.contains(Symbol.SEMICOLON) || base.contains(Symbol.DOUBLE_DOT))
                throw new IllegalArgumentException("Invalid service identity path pattern");
        }

        /**
         * Matches the route and target path coordinates.
         *
         * @param actualRoute gateway route when applicable
         * @param actualPath  concrete context-relative path
         * @return whether the rule matches both coordinates
         */
        public boolean matches(String actualRoute, String actualPath) {
            return matchesCoordinate(route, actualRoute) && matchesCoordinate(path, actualPath);
        }

    }

    /**
     * Authenticated service identity.
     *
     * @param subject  validated caller code
     * @param audience validated destination service code
     * @param origin   signed previous-hop caller code, or null for a direct call
     * @param asset    signed selected route identifier, or null when omitted
     */
    public record Verified(String subject, String audience, String origin, String asset) {

        /**
         * Represents an identity authenticated directly by the destination service.
         *
         * @param subject  validated caller code
         * @param audience validated destination service code
         */
        public Verified(String subject, String audience) {
            this(subject, audience, null, null);
        }

        /**
         * Represents an authenticated forwarding hop without an asset assertion.
         *
         * @param subject  validated forwarding service code
         * @param audience validated destination service code
         * @param origin   signed previous-hop caller code
         */
        public Verified(String subject, String audience, String origin) {
            this(subject, audience, origin, null);
        }

    }

}
