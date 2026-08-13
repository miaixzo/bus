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

import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable ordered composition of authentication boundary rules.
 *
 * @param <T> checked value type
 * @author Kimi Liu
 */
public final class RuleChain<T> implements BoundaryRule<T> {

    /**
     * Immutable rule evaluation order.
     */
    private final List<BoundaryRule<T>> rules;

    /**
     * Creates one immutable rule chain.
     *
     * @param rules ordered non-null rules
     */
    private RuleChain(final List<BoundaryRule<T>> rules) {
        this.rules = List.copyOf(rules);
        this.rules.forEach(rule -> Assert.notNull(rule, () -> new ValidateException("Guard rule must not be null")));
    }

    /**
     * Creates a chain preserving the supplied evaluation order.
     *
     * @param rules ordered rules
     * @param <T>   checked value type
     * @return immutable rule chain
     * @throws ValidateException if the array or an element is null
     */
    @SafeVarargs
    public static <T> RuleChain<T> of(final BoundaryRule<T>... rules) {
        return new RuleChain<>(
                Arrays.asList(Assert.notNull(rules, () -> new ValidateException("Guard rules must not be null"))));
    }

    /**
     * Evaluates each rule in order and stops at the first non-success outcome.
     *
     * @param context non-null authentication context passed unchanged to every rule
     * @param value   checked value passed unchanged to every rule
     * @return the first non-success outcome or a completed outcome
     * @throws ValidateException if a rule returns null
     */
    @Override
    public Outcome<Void> evaluate(final Context context, final T value) {
        for (final BoundaryRule<T> rule : rules) {
            final Outcome<Void> result = rule.evaluate(context, value);
            if (result == null) {
                throw new ValidateException("Authentication rule returned null");
            }
            if (!(result instanceof Outcome.Success<Void>)) {
                return result;
            }
        }
        return Outcome.completed();
    }

}
