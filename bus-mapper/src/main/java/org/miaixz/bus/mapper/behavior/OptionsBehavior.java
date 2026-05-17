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
package org.miaixz.bus.mapper.behavior;

import java.util.EnumSet;
import java.util.Optional;

import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.Charter.Group;

/**
 * Behavior capability options exposed by a database dialect.
 *
 * <p>
 * This contract declares the mapper behavior types supported by a dialect and provides common lookup helpers for
 * callers that need to gate SQL generation by database capability.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface OptionsBehavior extends UpsertBehavior {

    /**
     * Returns all operation types supported by this dialect.
     *
     * @return supported operation types
     */
    default EnumSet<Behavior> types() {
        Behavior type = getUpsertType();
        return type == null || type == Behavior.NONE ? EnumSet.noneOf(Behavior.class) : EnumSet.of(type);
    }

    /**
     * Tests whether the dialect supports the specified operation type.
     *
     * @param type the operation type
     * @return {@code true} when supported
     */
    default boolean supports(Behavior type) {
        return types().contains(type);
    }

    /**
     * Returns the operation type for a mutually exclusive group.
     *
     * @param group the operation group
     * @return the operation type for the group
     */
    default Optional<Behavior> type(Group group) {
        return types().stream().filter(type -> type.group() == group).findFirst();
    }

}
