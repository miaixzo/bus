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
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Message;

/**
 * Immutable ordered guard rule chain.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class GuardChain implements GuardRule {

    /**
     * Immutable rules evaluated in insertion order.
     */
    private final List<GuardRule> rules;

    /**
     * Creates a guard chain after validating and copying all rules.
     *
     * @param rules ordered non-null rule list containing no null elements
     */
    private GuardChain(final List<GuardRule> rules) {
        this.rules = List.copyOf(validateRules(rules));
    }

    /**
     * Creates an empty guard chain.
     *
     * @return immutable chain that always passes non-null messages
     */
    public static GuardChain empty() {
        return new GuardChain(List.of());
    }

    /**
     * Creates a guard chain from rules.
     *
     * @param rules ordered rules copied into the chain
     * @return immutable chain preserving list iteration order
     * @throws ValidateException if the list or one of its elements is {@code null}
     */
    public static GuardChain of(final List<GuardRule> rules) {
        return new GuardChain(rules);
    }

    /**
     * Creates a guard chain from rules.
     *
     * @param rules ordered rules copied into the chain
     * @return immutable chain preserving array order
     * @throws ValidateException if the array or one of its elements is {@code null}
     */
    public static GuardChain of(final GuardRule... rules) {
        return new GuardChain(
                Arrays.asList(Assert.notNull(rules, () -> new ValidateException("Guard rules must not be null"))));
    }

    /**
     * Returns the stable composite-rule name.
     *
     * @return {@link Builder#GUARD_CHAIN_NAME}
     */
    @Override
    public String name() {
        return Builder.GUARD_CHAIN_NAME;
    }

    /**
     * Runs all rules in order until rejection.
     *
     * @param message message passed unchanged to every evaluated rule
     * @return first non-passing result, or a passing result when all rules pass
     * @throws ValidateException if {@code message} is {@code null} or a rule returns {@code null}
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
     * @param rule non-null rule appended after all existing rules
     * @return new immutable chain; this chain remains unchanged
     * @throws ValidateException if {@code rule} is {@code null}
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
     * @return immutable ordered rule list retained by this chain
     */
    public List<GuardRule> rules() {
        return List.copyOf(rules);
    }

    /**
     * Validates rule list.
     *
     * @param rules candidate ordered rule list
     * @return same list reference after validating the list and every element
     * @throws ValidateException if the list or one of its elements is {@code null}
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
     * @param rule candidate guard rule
     * @throws ValidateException if {@code rule} is {@code null}
     */
    private static void validateRule(final GuardRule rule) {
        Assert.notNull(rule, () -> new ValidateException("Guard rule must not be null"));
    }

}
