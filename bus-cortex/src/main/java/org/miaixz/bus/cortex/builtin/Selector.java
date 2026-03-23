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
package org.miaixz.bus.cortex.builtin;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Label selector for filtering entries by metadata.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Selector {

    /**
     * Comparison operator for label selector expressions.
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    public enum Op {
        /** Key equals one of the given values. */
        EQ,
        /** Key does not equal any of the given values. */
        NEQ,
        /** Key value is contained in the given set. */
        IN,
        /** Key value is not contained in the given set. */
        NOTIN
    }

    /** Metadata key to match against. */
    private String key;
    /** Comparison operator to apply. */
    private Op op;
    /** Set of values used in the comparison. */
    private List<String> values;

    /**
     * Creates an equality selector.
     *
     * @param key   metadata key
     * @param value expected value
     * @return selector
     */
    public static Selector eq(String key, String value) {
        Selector s = new Selector();
        s.key = key;
        s.op = Op.EQ;
        s.values = List.of(value);
        return s;
    }

    /**
     * Creates an IN selector.
     *
     * @param key    metadata key
     * @param values allowed values
     * @return selector
     */
    public static Selector in(String key, List<String> values) {
        Selector s = new Selector();
        s.key = key;
        s.op = Op.IN;
        s.values = values;
        return s;
    }

}
