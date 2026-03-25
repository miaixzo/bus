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

import org.miaixz.bus.core.xyz.MathKit;

/**
 * Handles name nodes or index nodes in a Bean path expression, such as:
 * <ul>
 * <li>{@code name} (for property names)</li>
 * <li>{@code 1} (for array or list indices)</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NameNode implements Node {

    /**
     * The name or index represented by this node.
     */
    private final String name;

    /**
     * Constructs a {@code NameNode} with the given name.
     *
     * @param name The name or index of the node.
     */
    public NameNode(final String name) {
        this.name = name;
    }

    /**
     * Retrieves the name or index of this node.
     *
     * @return The name or index of the node.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if this node represents a numeric index.
     *
     * @return {@code true} if the name is a valid integer, {@code false} otherwise.
     */
    public boolean isNumber() {
        return MathKit.isInteger(name);
    }

    /**
     * Returns the string representation of this node, which is its name.
     *
     * @return The name of the node.
     */
    @Override
    public String toString() {
        return this.name;
    }

}
