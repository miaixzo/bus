/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 17+
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
