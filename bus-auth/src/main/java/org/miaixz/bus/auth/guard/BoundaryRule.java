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

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Composable authentication boundary rule.
 *
 * @param <T> checked value type
 * @author Kimi Liu
 */
@FunctionalInterface
public interface BoundaryRule<T> {

    /**
     * Evaluates one value without throwing for a domain rejection.
     *
     * @param context non-null authentication context
     * @param value   value to evaluate
     * @return non-null successful, rejected, or failed outcome
     */
    Outcome<Void> evaluate(Context context, T value);

    /**
     * Evaluates one value and converts non-success outcomes to validation exceptions.
     *
     * @param context non-null authentication context
     * @param value   value to evaluate
     * @throws ValidateException if the rule returns null, rejects the value, or fails
     */
    default void check(final Context context, final T value) {
        final Outcome<Void> result = evaluate(context, value);
        if (result == null) {
            throw new ValidateException("Authentication rule returned null");
        }
        if (result instanceof Outcome.Rejected<Void> rejected) {
            throw new ValidateException(rejected.failure().error());
        }
        if (result instanceof Outcome.Failed<Void> failed) {
            throw new ValidateException(failed.failure().error().getKey(), failed.failure().error().getValue(),
                    failed.cause());
        }
    }

}
