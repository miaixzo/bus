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
package org.miaixz.bus.fabric.network.dns.policy;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsExtendedError;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.message.DnsQuestion;
import org.miaixz.bus.fabric.network.dns.message.DnsResponseCode;
import org.miaixz.bus.fabric.network.dns.record.DnsRecord;
import org.miaixz.bus.fabric.network.dns.record.DnsRecordType;
import org.miaixz.bus.fabric.network.dns.zone.CidrBlock;

/**
 * Immutable DNS policy rule evaluated before resolution or forwarding.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsPolicyRule {

    /**
     * Rule match mode.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Mode {

        /**
         * Exact-name match.
         */
        EXACT,

        /**
         * Domain suffix match.
         */
        SUFFIX,

        /**
         * Wildcard DNS name match where {@code *} matches exactly one label.
         */
        WILDCARD,

        /**
         * Regular expression match against the normalized absolute query name.
         */
        REGEX

    }

    /**
     * Policy action.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Action {

        /**
         * Allow-list action that stops later block rules.
         */
        ALLOW,

        /**
         * Block-list action that returns the configured response code.
         */
        BLOCK,

        /**
         * Fixed address action returning A or AAAA records by query type.
         */
        FIXED_ADDRESS,

        /**
         * Explicit NXDOMAIN action.
         */
        NXDOMAIN,

        /**
         * Explicit NOERROR/NODATA action.
         */
        NODATA,

        /**
         * CNAME cloaking detection action.
         */
        CNAME_CLOAKING

    }

    /**
     * Match mode.
     */
    private final Mode mode;

    /**
     * Policy action.
     */
    private final Action action;

    /**
     * Canonical DNS name, suffix, wildcard name, or regex expression.
     */
    private final String name;

    /**
     * Compiled wildcard or regular-expression matcher, or {@code null} for direct name modes.
     */
    private final Pattern pattern;

    /**
     * Response code returned when the rule produces a DNS response.
     */
    private final DnsResponseCode responseCode;

    /**
     * Answer records returned when the rule matches.
     */
    private final List<DnsRecord> answers;

    /**
     * EDNS Extended DNS Error metadata.
     */
    private final DnsExtendedError extendedError;

    /**
     * Optional view name constraint.
     */
    private final String viewName;

    /**
     * Optional client CIDR constraints.
     */
    private final List<CidrBlock> clientCidrs;

    /**
     * Optional server endpoint constraint.
     */
    private final String endpoint;

    /**
     * Creates a policy rule.
     *
     * @param mode         match mode
     * @param name         canonical DNS name or suffix
     * @param responseCode response code returned on match
     */
    public DnsPolicyRule(final Mode mode, final String name, final DnsResponseCode responseCode) {
        this(mode, name, inferredAction(responseCode, List.of()), responseCode, List.of(), null, null, List.of(), null);
    }

    /**
     * Creates a policy rule.
     *
     * @param mode         match mode
     * @param name         canonical DNS name or suffix
     * @param responseCode response code returned on match
     * @param answers      answer records returned on match
     */
    public DnsPolicyRule(final Mode mode, final String name, final DnsResponseCode responseCode,
            final List<DnsRecord> answers) {
        this(mode, name, inferredAction(responseCode, answers), responseCode, answers, null, null, List.of(), null);
    }

    /**
     * Creates a policy rule.
     *
     * @param mode          match mode
     * @param name          canonical DNS name, suffix, wildcard, or regex expression
     * @param action        policy action
     * @param responseCode  response code returned on match
     * @param answers       answer records returned on match
     * @param extendedError optional EDNS Extended DNS Error metadata
     * @param viewName      optional view name constraint
     * @param clientCidrs   optional client CIDR constraints
     * @param endpoint      optional endpoint constraint
     */
    public DnsPolicyRule(final Mode mode, final String name, final Action action, final DnsResponseCode responseCode,
            final List<DnsRecord> answers, final DnsExtendedError extendedError, final String viewName,
            final List<CidrBlock> clientCidrs, final String endpoint) {
        if (mode == null) {
            throw new ValidateException("DNS policy mode must not be null");
        }
        if (action == null) {
            throw new ValidateException("DNS policy action must not be null");
        }
        if (responseCode == null) {
            throw new ValidateException("DNS policy response code must not be null");
        }
        this.mode = mode;
        this.action = action;
        this.name = mode == Mode.REGEX ? validateExpression(name) : DnsName.normalize(name);
        this.pattern = compiledPattern(mode, this.name);
        this.responseCode = responseCode;
        this.answers = immutableAnswers(action, responseCode, answers);
        this.extendedError = extendedError;
        this.viewName = normalizeOptional(viewName);
        this.clientCidrs = immutableCidrs(clientCidrs);
        this.endpoint = normalizeOptional(endpoint);
        validateAction();
    }

    /**
     * Creates an exact-name allow rule.
     *
     * @param name DNS name
     * @return policy rule
     */
    public static DnsPolicyRule allowExact(final String name) {
        return new DnsPolicyRule(Mode.EXACT, name, Action.ALLOW, DnsResponseCode.NOERROR, List.of(), null, null,
                List.of(), null);
    }

    /**
     * Creates an exact-name NXDOMAIN block rule.
     *
     * @param name DNS name
     * @return policy rule
     */
    public static DnsPolicyRule blockExact(final String name) {
        return new DnsPolicyRule(Mode.EXACT, name, Action.BLOCK, DnsResponseCode.NXDOMAIN, List.of(),
                DnsExtendedError.blocked("blocked by policy"), null, List.of(), null);
    }

    /**
     * Creates a suffix NXDOMAIN block rule.
     *
     * @param name DNS suffix
     * @return policy rule
     */
    public static DnsPolicyRule blockSuffix(final String name) {
        return new DnsPolicyRule(Mode.SUFFIX, name, Action.BLOCK, DnsResponseCode.NXDOMAIN, List.of(),
                DnsExtendedError.blocked("blocked by policy"), null, List.of(), null);
    }

    /**
     * Creates a wildcard NXDOMAIN block rule.
     *
     * @param name DNS wildcard name using {@code *} as a full label
     * @return policy rule
     */
    public static DnsPolicyRule blockWildcard(final String name) {
        return new DnsPolicyRule(Mode.WILDCARD, name, Action.BLOCK, DnsResponseCode.NXDOMAIN, List.of(),
                DnsExtendedError.blocked("blocked by policy"), null, List.of(), null);
    }

    /**
     * Creates a regular-expression NXDOMAIN block rule.
     *
     * @param expression regular expression matched against the normalized absolute query name
     * @return policy rule
     */
    public static DnsPolicyRule blockRegex(final String expression) {
        return new DnsPolicyRule(Mode.REGEX, expression, Action.BLOCK, DnsResponseCode.NXDOMAIN, List.of(),
                DnsExtendedError.blocked("blocked by policy"), null, List.of(), null);
    }

    /**
     * Creates an exact-name NOERROR/NODATA rule.
     *
     * @param name DNS name
     * @return policy rule
     */
    public static DnsPolicyRule nodataExact(final String name) {
        return new DnsPolicyRule(Mode.EXACT, name, Action.NODATA, DnsResponseCode.NOERROR, List.of(), null, null,
                List.of(), null);
    }

    /**
     * Creates an exact-name fixed-address answer rule.
     *
     * @param name    DNS name
     * @param address IPv4 or IPv6 address returned by the rule
     * @param ttl     unsigned 32-bit TTL
     * @return policy rule
     */
    public static DnsPolicyRule fixedAddressExact(final String name, final InetAddress address, final long ttl) {
        return new DnsPolicyRule(Mode.EXACT, name, Action.FIXED_ADDRESS, DnsResponseCode.NOERROR,
                List.of(addressRecord(name, address, ttl)), null, null, List.of(), null);
    }

    /**
     * Creates a suffix fixed-address answer rule.
     *
     * @param name    DNS suffix
     * @param address IPv4 or IPv6 address returned by the rule
     * @param ttl     unsigned 32-bit TTL
     * @return policy rule
     */
    public static DnsPolicyRule fixedAddressSuffix(final String name, final InetAddress address, final long ttl) {
        return new DnsPolicyRule(Mode.SUFFIX, name, Action.FIXED_ADDRESS, DnsResponseCode.NOERROR,
                List.of(addressRecord(name, address, ttl)), null, null, List.of(), null);
    }

    /**
     * Creates a wildcard fixed-address answer rule.
     *
     * @param name    DNS wildcard name using {@code *} as a full label
     * @param address IPv4 or IPv6 address returned by the rule
     * @param ttl     unsigned 32-bit TTL
     * @return policy rule
     */
    public static DnsPolicyRule fixedAddressWildcard(final String name, final InetAddress address, final long ttl) {
        return new DnsPolicyRule(Mode.WILDCARD, name, Action.FIXED_ADDRESS, DnsResponseCode.NOERROR,
                List.of(addressRecord(name, address, ttl)), null, null, List.of(), null);
    }

    /**
     * Returns whether this rule matches a question.
     *
     * @param question decoded DNS question
     * @return true when the rule matches the question name
     */
    public boolean matches(final DnsQuestion question) {
        return matches(question, null, null, null);
    }

    /**
     * Returns whether this rule matches a full policy context.
     *
     * @param question      decoded DNS question
     * @param clientAddress client address, or {@code null}
     * @param view          selected view name, or {@code null}
     * @param endpoint      server endpoint name, or {@code null}
     * @return true when the rule matches the query and all constraints
     */
    public boolean matches(
            final DnsQuestion question,
            final InetAddress clientAddress,
            final String view,
            final String endpoint) {
        if (question == null) {
            throw new ValidateException("DNS policy question must not be null");
        }
        return contextMatches(clientAddress, view, endpoint)
                && matchesNormalizedName(DnsName.normalize(question.name()));
    }

    /**
     * Returns whether a normalized DNS name matches this rule.
     *
     * @param query normalized DNS query name
     * @return true when the name matches
     */
    boolean matchesNormalizedName(final String query) {
        return switch (mode) {
            case EXACT -> query.equals(name);
            case SUFFIX -> DnsName.inZone(query, name);
            case WILDCARD, REGEX -> pattern.matcher(query).matches();
        };
    }

    /**
     * Returns the policy action.
     *
     * @return policy action
     */
    public Action action() {
        return action;
    }

    /**
     * Returns whether this rule is an allow rule.
     *
     * @return true when action is allow
     */
    public boolean allow() {
        return action == Action.ALLOW;
    }

    /**
     * Returns the rule response code.
     *
     * @return DNS response code
     */
    public DnsResponseCode responseCode() {
        return responseCode;
    }

    /**
     * Returns EDNS Extended DNS Error metadata.
     *
     * @return EDE metadata, or {@code null}
     */
    public DnsExtendedError extendedError() {
        return extendedError;
    }

    /**
     * Returns answer records rewritten to the current query owner.
     *
     * @param question decoded DNS question
     * @return immutable answer records for a matched query
     */
    public List<DnsRecord> answers(final DnsQuestion question) {
        if (question == null) {
            throw new ValidateException("DNS policy answer question must not be null");
        }
        if (answers.isEmpty()) {
            return List.of();
        }
        final ArrayList<DnsRecord> rewritten = new ArrayList<>(answers.size());
        for (final DnsRecord answer : answers) {
            if (includeAnswer(question, answer)) {
                rewritten.add(answer.withName(question.name()));
            }
        }
        return List.copyOf(rewritten);
    }

    /**
     * Returns the match mode.
     *
     * @return rule match mode
     */
    public Mode mode() {
        return mode;
    }

    /**
     * Returns the canonical match name or regex expression.
     *
     * @return canonical DNS name, suffix, wildcard, or regex expression
     */
    public String name() {
        return name;
    }

    /**
     * Returns the optional view constraint.
     *
     * @return view name, or {@code null}
     */
    public String viewName() {
        return viewName;
    }

    /**
     * Returns the optional client CIDR constraints.
     *
     * @return immutable client CIDR constraints
     */
    public List<CidrBlock> clientCidrs() {
        return clientCidrs;
    }

    /**
     * Returns the optional endpoint constraint.
     *
     * @return endpoint name, or {@code null}
     */
    public String endpoint() {
        return endpoint;
    }

    /**
     * Returns the compiled pattern for wildcard or regex rules.
     *
     * @return compiled pattern, or {@code null}
     */
    Pattern pattern() {
        return pattern;
    }

    /**
     * Returns whether all context constraints match.
     *
     * @param clientAddress client address, or {@code null}
     * @param view          selected view name, or {@code null}
     * @param endpoint      endpoint name, or {@code null}
     * @return true when all constraints match
     */
    private boolean contextMatches(final InetAddress clientAddress, final String view, final String endpoint) {
        return viewMatches(view) && endpointMatches(endpoint) && clientMatches(clientAddress);
    }

    /**
     * Returns whether the view constraint matches.
     *
     * @param view selected view name, or {@code null}
     * @return true when the view constraint matches
     */
    private boolean viewMatches(final String view) {
        return viewName == null || viewName.equals(normalizeOptional(view));
    }

    /**
     * Returns whether the endpoint constraint matches.
     *
     * @param endpoint selected endpoint, or {@code null}
     * @return true when the endpoint constraint matches
     */
    private boolean endpointMatches(final String endpoint) {
        return this.endpoint == null || this.endpoint.equals(normalizeOptional(endpoint));
    }

    /**
     * Returns whether the client CIDR constraint matches.
     *
     * @param clientAddress client address, or {@code null}
     * @return true when the CIDR constraint matches
     */
    private boolean clientMatches(final InetAddress clientAddress) {
        if (clientCidrs.isEmpty()) {
            return true;
        }
        for (final CidrBlock cidr : clientCidrs) {
            if (cidr.contains(clientAddress)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether an answer record is applicable to the query type.
     *
     * @param question decoded DNS question
     * @param answer   answer record
     * @return true when the answer should be returned
     */
    private boolean includeAnswer(final DnsQuestion question, final DnsRecord answer) {
        if (action != Action.FIXED_ADDRESS) {
            return true;
        }
        return question.typeCode() == answer.typeCode()
                && (question.typeCode() == DnsRecordType.A.code() || question.typeCode() == DnsRecordType.AAAA.code());
    }

    /**
     * Validates action-specific invariants.
     */
    private void validateAction() {
        if (action == Action.ALLOW && (!answers.isEmpty() || responseCode != DnsResponseCode.NOERROR)) {
            throw new ValidateException("DNS allow policy must not produce answers");
        }
        if (action == Action.NODATA && (!answers.isEmpty() || responseCode != DnsResponseCode.NOERROR)) {
            throw new ValidateException("DNS NODATA policy must be NOERROR without answers");
        }
        if (action == Action.FIXED_ADDRESS && responseCode != DnsResponseCode.NOERROR) {
            throw new ValidateException("DNS fixed IP policy must use NOERROR");
        }
    }

    /**
     * Compiles the pattern used by wildcard and regular-expression modes.
     *
     * @param mode selected match mode
     * @param name normalized name or validated expression
     * @return compiled pattern, or {@code null}
     */
    private static Pattern compiledPattern(final Mode mode, final String name) {
        return switch (mode) {
            case EXACT, SUFFIX -> null;
            case WILDCARD -> wildcardPattern(name);
            case REGEX -> regexPattern(name);
        };
    }

    /**
     * Creates an A or AAAA record from an address literal.
     *
     * @param name    owner name
     * @param address IPv4 or IPv6 address
     * @param ttl     unsigned 32-bit TTL
     * @return address record
     */
    private static DnsRecord addressRecord(final String name, final InetAddress address, final long ttl) {
        if (address == null) {
            throw new ValidateException("DNS policy fixed address must not be null");
        }
        return address.getAddress().length == 4 ? DnsRecord.a(name, address, ttl) : DnsRecord.aaaa(name, address, ttl);
    }

    /**
     * Validates and copies policy answer records.
     *
     * @param action       policy action
     * @param responseCode response code returned on match
     * @param answers      source answer records
     * @return immutable answer records
     */
    private static List<DnsRecord> immutableAnswers(
            final Action action,
            final DnsResponseCode responseCode,
            final List<DnsRecord> answers) {
        if (answers == null) {
            throw new ValidateException("DNS policy answers must not be null");
        }
        if (!answers.isEmpty() && responseCode != DnsResponseCode.NOERROR) {
            throw new ValidateException("DNS policy answers require NOERROR response code");
        }
        for (final DnsRecord answer : answers) {
            if (answer == null) {
                throw new ValidateException("DNS policy answers must not contain null");
            }
            if (action == Action.FIXED_ADDRESS && answer.typeCode() != DnsRecordType.A.code()
                    && answer.typeCode() != DnsRecordType.AAAA.code()) {
                throw new ValidateException("DNS fixed IP policy answers must be A or AAAA");
            }
        }
        return List.copyOf(answers);
    }

    /**
     * Validates and copies client CIDR constraints.
     *
     * @param clientCidrs source CIDR constraints
     * @return immutable CIDR constraints
     */
    private static List<CidrBlock> immutableCidrs(final List<CidrBlock> clientCidrs) {
        if (clientCidrs == null) {
            throw new ValidateException("DNS policy client CIDRs must not be null");
        }
        for (final CidrBlock cidr : clientCidrs) {
            if (cidr == null) {
                throw new ValidateException("DNS policy client CIDRs must not contain null");
            }
        }
        return List.copyOf(clientCidrs);
    }

    /**
     * Compiles a wildcard DNS name where {@code *} matches exactly one label.
     *
     * @param name normalized wildcard DNS name
     * @return compiled wildcard pattern
     */
    private static Pattern wildcardPattern(final String name) {
        final String body = name.substring(0, name.length() - 1);
        final StringBuilder expression = new StringBuilder("^");
        final String[] labels = DnsName.labels(body);
        for (final String label : labels) {
            if (label.contains(Symbol.STAR) && !Symbol.STAR.equals(label)) {
                throw new ValidateException("DNS wildcard policy only allows * as a full label");
            }
            expression.append(Symbol.STAR.equals(label) ? "[^.]+" : Pattern.quote(label)).append("\\.");
        }
        expression.append('$');
        return Pattern.compile(expression.toString());
    }

    /**
     * Validates a regular-expression policy expression.
     *
     * @param expression candidate expression
     * @return validated expression
     */
    private static String validateExpression(final String expression) {
        if (expression == null || expression.isBlank()) {
            throw new ValidateException("DNS policy regex must be non-blank");
        }
        return expression.trim();
    }

    /**
     * Compiles a regular-expression policy matcher.
     *
     * @param expression regular expression
     * @return compiled pattern
     */
    private static Pattern regexPattern(final String expression) {
        try {
            return Pattern.compile(expression);
        } catch (final PatternSyntaxException e) {
            throw new ValidateException("DNS policy regex is invalid", e);
        }
    }

    /**
     * Infers the action used by legacy constructors.
     *
     * @param responseCode response code
     * @param answers      answer records
     * @return inferred action
     */
    private static Action inferredAction(final DnsResponseCode responseCode, final List<DnsRecord> answers) {
        if (responseCode == DnsResponseCode.NOERROR && answers != null && !answers.isEmpty()) {
            return Action.FIXED_ADDRESS;
        }
        if (responseCode == DnsResponseCode.NOERROR) {
            return Action.NODATA;
        }
        if (responseCode == DnsResponseCode.NXDOMAIN) {
            return Action.BLOCK;
        }
        return Action.BLOCK;
    }

    /**
     * Normalizes optional text.
     *
     * @param value source value
     * @return trimmed value, or {@code null}
     */
    private static String normalizeOptional(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

}
