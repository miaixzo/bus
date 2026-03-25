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
package org.miaixz.bus.core.bean.path.node;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A simple factory for creating {@link Node} instances based on a given expression string. It determines the type of
 * node (empty, range, list, or name) from the expression.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NodeFactory {

    /**
     * Creates a {@link Node} instance based on the provided expression string. It checks for specific delimiters to
     * determine the node type:
     * <ul>
     * <li>If the expression is empty, an {@link EmptyNode} is returned.</li>
     * <li>If the expression contains a colon ({@code :}), a {@link RangeNode} is created.</li>
     * <li>If the expression contains a comma ({@code ,}), a {@link ListNode} is created.</li>
     * <li>Otherwise, a {@link NameNode} is created.</li>
     * </ul>
     *
     * @param expression The expression string representing the node.
     * @return A {@link Node} instance corresponding to the expression.
     */
    public static Node createNode(final String expression) {
        if (StringKit.isEmpty(expression)) {
            return EmptyNode.INSTANCE;
        }

        if (StringKit.contains(expression, Symbol.C_COLON)) {
            return new RangeNode(expression);
        }

        if (StringKit.contains(expression, Symbol.C_COMMA)) {
            return new ListNode(expression);
        }

        return new NameNode(expression);
    }

}
