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
package org.miaixz.bus.cortex.magic.identity;

import org.miaixz.bus.core.center.function.SupplierX;

import org.miaixz.bus.core.data.id.ID;

/**
 * Pluggable unique ID generator with a built-in {@link ID#objectId()} default.
 * <p>
 * Construct without arguments to use MongoDB-style ObjectId strings out of the box. Pass any {@code SupplierX<String>}
 * to the second constructor to replace the strategy with a custom implementation (e.g. Snowflake, UUID, NanoId).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class IdGenerator {

    /**
     * ID generation strategy; defaults to {@link ID#objectId()}.
     */
    private final SupplierX<String> strategy;

    /**
     * Creates an IdGenerator that produces MongoDB-style ObjectId strings via {@link ID#objectId()}.
     */
    public IdGenerator() {
        this.strategy = ID::objectId;
    }

    /**
     * Creates an IdGenerator with a custom ID generation strategy.
     *
     * @param strategy supplier that returns a new unique string ID on each invocation
     */
    public IdGenerator(SupplierX<String> strategy) {
        this.strategy = strategy;
    }

    /**
     * Generates the next unique identifier using the configured strategy.
     *
     * @return new unique string ID
     */
    public String next() {
        return strategy.get();
    }

}
