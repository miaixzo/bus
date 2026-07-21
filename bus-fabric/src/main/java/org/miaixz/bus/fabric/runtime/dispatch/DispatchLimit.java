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
package org.miaixz.bus.fabric.runtime.dispatch;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable global and per-key dispatch limits.
 *
 * @param max    global maximum running tasks
 * @param perKey per-key maximum running tasks
 * @author Kimi Liu
 * @since Java 21+
 */
public record DispatchLimit(int max, int perKey) {

    /**
     * Creates a dispatch limit.
     */
    public DispatchLimit {
        if (max <= Normal._0) {
            throw new ValidateException("Dispatch max must be greater than zero");
        }
        if (perKey <= Normal._0 || perKey > max) {
            throw new ValidateException("Dispatch per-key limit must be between one and max");
        }
    }

    /**
     * Returns default limits.
     *
     * @return default limits
     */
    public static DispatchLimit defaults() {
        return Instances.get(
                DispatchLimit.class.getName() + Symbol.DOT + "defaults",
                () -> new DispatchLimit(Normal._64, Normal._5));
    }

    /**
     * Creates dispatch limits.
     *
     * @param max    global maximum
     * @param perKey per-key maximum
     * @return dispatch limit
     */
    public static DispatchLimit of(final int max, final int perKey) {
        return new DispatchLimit(max, perKey);
    }

    /**
     * Returns the global running limit.
     *
     * @return global running limit
     */
    @Override
    public int max() {
        return max;
    }

    /**
     * Returns the per-key running limit.
     *
     * @return per-key running limit
     */
    @Override
    public int perKey() {
        return perKey;
    }

    /**
     * Returns whether another handle may be promoted.
     *
     * @param runningTotal  total running handles
     * @param runningForKey running handles for the same key
     * @return true when promotion is allowed
     */
    public boolean canPromote(final int runningTotal, final int runningForKey) {
        if (runningTotal < Normal._0 || runningForKey < Normal._0) {
            throw new ValidateException("Running counts must not be negative");
        }
        return runningTotal < max && runningForKey < perKey;
    }

}
