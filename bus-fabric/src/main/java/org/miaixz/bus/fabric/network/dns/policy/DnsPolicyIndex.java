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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsName;
import org.miaixz.bus.fabric.network.dns.message.DnsQuestion;
import org.miaixz.bus.fabric.network.dns.policy.DnsPolicyRule.Action;

/**
 * Immutable compiled DNS policy index.
 *
 * @author Kimi Liu
 */
public final class DnsPolicyIndex {

    /**
     * Empty policy index.
     */
    private static final DnsPolicyIndex EMPTY = new DnsPolicyIndex(List.of(), Map.of(), List.of(), SuffixTrie.empty(),
            List.of(), List.of(), List.of());

    /**
     * Allow rules evaluated before all blocking rules.
     */
    private final List<DnsPolicyRule> allowRules;

    /**
     * Exact-name rules keyed by normalized owner name.
     */
    private final Map<String, List<DnsPolicyRule>> exactRules;

    /**
     * Wildcard rules with precompiled wildcard patterns.
     */
    private final List<DnsPolicyRule> wildcardRules;

    /**
     * Suffix rule trie.
     */
    private final SuffixTrie suffixRules;

    /**
     * Regex rules with precompiled patterns.
     */
    private final List<DnsPolicyRule> regexRules;

    /**
     * CNAME cloaking rules evaluated after regex rules.
     */
    private final List<DnsPolicyRule> cnameCloakingRules;

    /**
     * Source rules preserved for compatibility APIs.
     */
    private final List<DnsPolicyRule> rules;

    /**
     * Creates a compiled policy index.
     *
     * @param allowRules         allow rules
     * @param exactRules         exact rules
     * @param wildcardRules      wildcard rules
     * @param suffixRules        suffix trie
     * @param regexRules         regex rules
     * @param cnameCloakingRules CNAME cloaking rules
     * @param rules              source rules
     */
    private DnsPolicyIndex(final List<DnsPolicyRule> allowRules, final Map<String, List<DnsPolicyRule>> exactRules,
            final List<DnsPolicyRule> wildcardRules, final SuffixTrie suffixRules, final List<DnsPolicyRule> regexRules,
            final List<DnsPolicyRule> cnameCloakingRules, final List<DnsPolicyRule> rules) {
        this.allowRules = List.copyOf(allowRules);
        this.exactRules = immutableExactRules(exactRules);
        this.wildcardRules = List.copyOf(wildcardRules);
        this.suffixRules = suffixRules;
        this.regexRules = List.copyOf(regexRules);
        this.cnameCloakingRules = List.copyOf(cnameCloakingRules);
        this.rules = List.copyOf(rules);
    }

    /**
     * Returns an empty policy index.
     *
     * @return empty policy index
     */
    public static DnsPolicyIndex empty() {
        return EMPTY;
    }

    /**
     * Compiles policy rules into deterministic hot-path indexes.
     *
     * @param rules source policy rules
     * @return compiled policy index
     */
    public static DnsPolicyIndex compile(final List<DnsPolicyRule> rules) {
        if (rules == null) {
            throw new ValidateException("DNS policy index rules must not be null");
        }
        final ArrayList<DnsPolicyRule> allow = new ArrayList<>();
        final LinkedHashMap<String, List<DnsPolicyRule>> exact = new LinkedHashMap<>();
        final ArrayList<DnsPolicyRule> wildcard = new ArrayList<>();
        final SuffixTrie suffix = new SuffixTrie();
        final ArrayList<DnsPolicyRule> regex = new ArrayList<>();
        final ArrayList<DnsPolicyRule> cname = new ArrayList<>();
        final ArrayList<DnsPolicyRule> copied = new ArrayList<>();
        for (final DnsPolicyRule rule : rules) {
            if (rule == null) {
                throw new ValidateException("DNS policy index rules must not contain null");
            }
            copied.add(rule);
            if (rule.allow()) {
                allow.add(rule);
                continue;
            }
            if (rule.action() == Action.CNAME_CLOAKING) {
                cname.add(rule);
                continue;
            }
            switch (rule.mode()) {
                case EXACT -> exact.computeIfAbsent(rule.name(), ignored -> new ArrayList<>()).add(rule);
                case WILDCARD -> wildcard.add(rule);
                case SUFFIX -> suffix.add(rule);
                case REGEX -> regex.add(rule);
            }
        }
        return new DnsPolicyIndex(allow, exact, wildcard, suffix.immutable(), regex, cname, copied);
    }

    /**
     * Matches a DNS question without context constraints.
     *
     * @param question decoded DNS question
     * @return matched response-producing policy rule, or {@code null}
     */
    public DnsPolicyRule match(final DnsQuestion question) {
        return match(question, null, null, null);
    }

    /**
     * Matches a DNS question with context constraints.
     *
     * @param question      decoded DNS question
     * @param clientAddress client address, or {@code null}
     * @param viewName      selected view name, or {@code null}
     * @param endpoint      endpoint name, or {@code null}
     * @return matched response-producing policy rule, or {@code null}
     */
    public DnsPolicyRule match(
            final DnsQuestion question,
            final InetAddress clientAddress,
            final String viewName,
            final String endpoint) {
        if (question == null) {
            throw new ValidateException("DNS policy index question must not be null");
        }
        final String query = DnsName.normalize(question.name());
        if (matchesAny(allowRules, question, clientAddress, viewName, endpoint)) {
            return null;
        }
        final DnsPolicyRule exact = firstMatching(exactRules.get(query), question, clientAddress, viewName, endpoint);
        if (exact != null) {
            return exact;
        }
        final DnsPolicyRule wildcard = firstMatching(wildcardRules, question, clientAddress, viewName, endpoint);
        if (wildcard != null) {
            return wildcard;
        }
        final DnsPolicyRule suffix = suffixRules.match(question, clientAddress, viewName, endpoint);
        if (suffix != null) {
            return suffix;
        }
        final DnsPolicyRule regex = firstMatching(regexRules, question, clientAddress, viewName, endpoint);
        if (regex != null) {
            return regex;
        }
        return firstMatching(cnameCloakingRules, question, clientAddress, viewName, endpoint);
    }

    /**
     * Returns source rules.
     *
     * @return immutable source rules
     */
    public List<DnsPolicyRule> rules() {
        return rules;
    }

    /**
     * Returns whether any policy rule exists.
     *
     * @return true when the index is empty
     */
    public boolean emptyIndex() {
        return rules.isEmpty();
    }

    /**
     * Returns whether any rule in a list matches.
     *
     * @param rules         candidate rules
     * @param question      decoded DNS question
     * @param clientAddress client address, or {@code null}
     * @param viewName      selected view name, or {@code null}
     * @param endpoint      endpoint name, or {@code null}
     * @return true when a rule matches
     */
    private static boolean matchesAny(
            final List<DnsPolicyRule> rules,
            final DnsQuestion question,
            final InetAddress clientAddress,
            final String viewName,
            final String endpoint) {
        return firstMatching(rules, question, clientAddress, viewName, endpoint) != null;
    }

    /**
     * Returns the first matching rule from a list.
     *
     * @param rules         candidate rules
     * @param question      decoded DNS question
     * @param clientAddress client address, or {@code null}
     * @param viewName      selected view name, or {@code null}
     * @param endpoint      endpoint name, or {@code null}
     * @return matching rule, or {@code null}
     */
    private static DnsPolicyRule firstMatching(
            final List<DnsPolicyRule> rules,
            final DnsQuestion question,
            final InetAddress clientAddress,
            final String viewName,
            final String endpoint) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        for (final DnsPolicyRule rule : rules) {
            if (rule.matches(question, clientAddress, viewName, endpoint)) {
                return rule;
            }
        }
        return null;
    }

    /**
     * Creates an immutable exact-rule map.
     *
     * @param source source exact map
     * @return immutable exact map
     */
    private static Map<String, List<DnsPolicyRule>> immutableExactRules(final Map<String, List<DnsPolicyRule>> source) {
        final HashMap<String, List<DnsPolicyRule>> copied = new HashMap<>();
        for (final Map.Entry<String, List<DnsPolicyRule>> entry : source.entrySet()) {
            copied.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(copied);
    }

    /**
     * Compiled suffix trie.
     *
     * @author Kimi Liu
     */
    private static final class SuffixTrie {

        /**
         * Root suffix node.
         */
        private final SuffixNode root;

        /**
         * Creates an empty suffix trie.
         */
        private SuffixTrie() {
            this(new SuffixNode(Map.of(), List.of()));
        }

        /**
         * Creates a suffix trie.
         *
         * @param root root suffix node
         */
        private SuffixTrie(final SuffixNode root) {
            this.root = root;
        }

        /**
         * Returns an immutable empty suffix trie.
         *
         * @return empty suffix trie
         */
        private static SuffixTrie empty() {
            return new SuffixTrie(new SuffixNode(Map.of(), List.of()));
        }

        /**
         * Adds a suffix rule.
         *
         * @param rule suffix policy rule
         */
        private void add(final DnsPolicyRule rule) {
            mutableRoot().add(labels(rule.name()), labels(rule.name()).length - 1, rule);
        }

        /**
         * Returns an immutable copy of this trie.
         *
         * @return immutable suffix trie
         */
        private SuffixTrie immutable() {
            return new SuffixTrie(root.immutable());
        }

        /**
         * Matches a question against the suffix trie.
         *
         * @param question      decoded DNS question
         * @param clientAddress client address, or {@code null}
         * @param viewName      selected view name, or {@code null}
         * @param endpoint      endpoint name, or {@code null}
         * @return matching suffix rule, or {@code null}
         */
        private DnsPolicyRule match(
                final DnsQuestion question,
                final InetAddress clientAddress,
                final String viewName,
                final String endpoint) {
            final String[] labels = labels(DnsName.normalize(question.name()));
            SuffixNode node = root;
            DnsPolicyRule selected = firstMatching(node.rules, question, clientAddress, viewName, endpoint);
            for (int index = labels.length - 1; index >= 0; index--) {
                node = node.children.get(labels[index]);
                if (node == null) {
                    break;
                }
                final DnsPolicyRule candidate = firstMatching(node.rules, question, clientAddress, viewName, endpoint);
                if (candidate != null) {
                    selected = candidate;
                }
            }
            return selected;
        }

        /**
         * Returns the mutable root node.
         *
         * @return root node
         */
        private SuffixNode mutableRoot() {
            return root;
        }

    }

    /**
     * Suffix trie node.
     *
     * @author Kimi Liu
     */
    private static final class SuffixNode {

        /**
         * Child nodes keyed by one DNS label.
         */
        private final Map<String, SuffixNode> children;

        /**
         * Rules ending at this suffix.
         */
        private final List<DnsPolicyRule> rules;

        /**
         * Creates a suffix node.
         *
         * @param children child nodes
         * @param rules    rules ending at this suffix
         */
        private SuffixNode(final Map<String, SuffixNode> children, final List<DnsPolicyRule> rules) {
            this.children = new LinkedHashMap<>(children);
            this.rules = new ArrayList<>(rules);
        }

        /**
         * Adds a rule at the reversed-label position.
         *
         * @param labels labels in normal order
         * @param index  current label index
         * @param rule   rule to add
         */
        private void add(final String[] labels, final int index, final DnsPolicyRule rule) {
            if (index < 0) {
                rules.add(rule);
                return;
            }
            children.computeIfAbsent(labels[index], ignored -> new SuffixNode(Map.of(), List.of()))
                    .add(labels, index - 1, rule);
        }

        /**
         * Returns an immutable copy of this node.
         *
         * @return immutable node
         */
        private SuffixNode immutable() {
            final LinkedHashMap<String, SuffixNode> immutableChildren = new LinkedHashMap<>();
            for (final Map.Entry<String, SuffixNode> entry : children.entrySet()) {
                immutableChildren.put(entry.getKey(), entry.getValue().immutable());
            }
            return new SuffixNode(Map.copyOf(immutableChildren), List.copyOf(rules));
        }

    }

    /**
     * Splits a normalized domain name into labels.
     *
     * @param name normalized domain name
     * @return labels excluding the root label
     */
    private static String[] labels(final String name) {
        return DnsName.labels(name);
    }

}
