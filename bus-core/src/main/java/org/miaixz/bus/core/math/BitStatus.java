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
package org.miaixz.bus.core.math;

/**
 * A utility class for representing states using bitwise operations. Parameters must be `even` and `greater than or
 * equal to 0`.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BitStatus {

    /**
     * Constructs a new BitStatus. Utility class constructor for static access.
     */
    private BitStatus() {
    }

    /**
     * Adds a status.
     *
     * @param states The original status collection.
     * @param stat   The status to add.
     * @return The new status value.
     */
    public static int add(final int states, final int stat) {
        check(states, stat);
        return states | stat;
    }

    /**
     * Checks if a status is present.
     *
     * @param states The original status collection.
     * @param stat   The status to check for.
     * @return {@code true} if the status is present, {@code false} otherwise.
     */
    public static boolean has(final int states, final int stat) {
        check(states, stat);
        return (states & stat) == stat;
    }

    /**
     * Removes a status.
     *
     * @param states The original status collection.
     * @param stat   The status to remove.
     * @return The new status value.
     */
    public static int remove(final int states, final int stat) {
        check(states, stat);
        if (has(states, stat)) {
            return states ^ stat;
        }
        return states;
    }

    /**
     * Clears all statuses, returning 0.
     *
     * @return 0
     */
    public static int clear() {
        return 0;
    }

    /**
     * Checks the validity of status values.
     * <ul>
     * <li>Must be greater than or equal to 0.</li>
     * <li>Must be an even number.</li>
     * </ul>
     *
     * @param args The status values to check.
     */
    private static void check(final int... args) {
        for (final int arg : args) {
            if (arg < 0) {
                // In bitwise operations, 0 has all bits as 0 in its binary representation and cannot represent any
                // specific state.
                // If 0 were allowed as a legal status value, it would be impossible to distinguish whether 0 represents
                // "no status" or a specific status when checking (e.g., via the has() method).
                // Allowing 0 as a status could also cause confusion with the clear() operation (which returns 0),
                // leading to logical errors.
                throw new IllegalArgumentException(arg + " must be greater than or equal to 0");
            }
            if ((arg & 1) == 1) {
                // Using even numbers as status values ensures that each status occupies only one bit in the binary
                // representation,
                // thus avoiding confusion between different statuses and ensuring the accuracy of bitwise operations.
                throw new IllegalArgumentException(arg + " is not an even number");
            }
        }
    }

}
