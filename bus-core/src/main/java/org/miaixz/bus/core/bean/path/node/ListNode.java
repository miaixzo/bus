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
