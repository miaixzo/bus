/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.bean.path.node;

import java.util.List;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;

/**
 * Represents a list node in a Bean path expression, supporting patterns like {@code [num0,num1,num2...]} for indices or
 * {@code ['key0','key1']} for keys.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ListNode implements Node {

    /**
     * The list of names or indices within this node.
     */
    final List<String> names;

    /**
     * Constructs a {@code ListNode} by parsing the given expression.
     *
     * @param expression The expression string for the list node (e.g., "num0,num1" or "'key0','key1'").
     */
    public ListNode(final String expression) {
        this.names = CharsBacker.splitTrim(expression, Symbol.COMMA);
    }

    /**
     * Retrieves the names or indices in the list, without removing single quotes.
     *
     * @return An array of names or indices.
     */
    public String[] getNames() {
        return this.names.toArray(new String[0]);
    }

    /**
     * Retrieves the names or indices in the list, with single quotes removed if present.
     *
     * @return An array of unwrapped names or indices.
     */
    public String[] getUnWrappedNames() {
        final String[] unWrappedNames = new String[names.size()];
        for (int i = 0; i < unWrappedNames.length; i++) {
            unWrappedNames[i] = CharsBacker.unWrap(names.get(i), Symbol.C_SINGLE_QUOTE);
        }

        return unWrappedNames;
    }

    /**
     * Returns a string representation of this list node.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        return this.names.toString();
    }

}
