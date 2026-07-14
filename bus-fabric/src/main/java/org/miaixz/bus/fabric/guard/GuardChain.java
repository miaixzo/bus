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
package org.miaixz.bus.fabric.guard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Message;

/**
 * Immutable ordered guard rule chain.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class GuardChain implements GuardRule {

    /**
     * Rule name.
     */
    private static final String NAME = "chain";

    /**
     * Ordered guard rules.
     */
    private final List<GuardRule> rules;

    /**
     * Creates a guard chain.
     *
     * @param rules guard rules
     */
    private GuardChain(final List<GuardRule> rules) {
        this.rules = List.copyOf(validateRules(rules));
    }

    /**
     * Creates an empty guard chain.
     *
     * @return empty guard chain
     */
    public static GuardChain empty() {
        return new GuardChain(List.of());
    }

    /**
     * Creates a guard chain from rules.
     *
     * @param rules guard rules
     * @return guard chain
     */
    public static GuardChain of(final List<GuardRule> rules) {
        return new GuardChain(rules);
    }

    /**
     * Creates a guard chain from rules.
     *
     * @param rules guard rules
     * @return guard chain
     */
    public static GuardChain of(final GuardRule... rules) {
        return new GuardChain(
                Arrays.asList(Assert.notNull(rules, () -> new ValidateException("Guard rules must not be null"))));
    }

    /**
     * Returns rule name.
     *
     * @return rule name
     */
    @Override
    public String name() {
        return NAME;
    }

    /**
     * Runs all rules in order until rejection.
     *
     * @param message message
     * @return guard result
     */
    @Override
    public GuardResult check(final Message message) {
        final Message checkedMessage = Assert.notNull(message, () -> new ValidateException("Message must not be null"));
        for (final GuardRule rule : rules) {
            final GuardResult result = Assert
                    .notNull(rule.check(checkedMessage), () -> new ValidateException("Guard result must not be null"));
            if (!result.passed()) {
                return result;
            }
        }
        return GuardResult.pass();
    }

    /**
     * Returns a new chain with an appended rule.
     *
     * @param rule guard rule
     * @return new guard chain
     */
    public GuardChain add(final GuardRule rule) {
        validateRule(rule);
        final ArrayList<GuardRule> copy = new ArrayList<>(rules);
        copy.add(rule);
        return new GuardChain(copy);
    }

    /**
     * Returns rules snapshot.
     *
     * @return rules
     */
    public List<GuardRule> rules() {
        return List.copyOf(rules);
    }

    /**
     * Validates rule list.
     *
     * @param rules rules
     * @return rules
     */
    private static List<GuardRule> validateRules(final List<GuardRule> rules) {
        final List<GuardRule> checkedRules = Assert
                .notNull(rules, () -> new ValidateException("Guard rules must not be null"));
        for (final GuardRule rule : checkedRules) {
            validateRule(rule);
        }
        return checkedRules;
    }

    /**
     * Validates one rule.
     *
     * @param rule rule
     */
    private static void validateRule(final GuardRule rule) {
        Assert.notNull(rule, () -> new ValidateException("Guard rule must not be null"));
    }

}
