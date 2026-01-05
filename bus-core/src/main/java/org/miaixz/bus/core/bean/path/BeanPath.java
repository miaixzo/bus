/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.bean.path;

import java.util.Iterator;

import org.miaixz.bus.core.bean.path.node.Node;
import org.miaixz.bus.core.bean.path.node.NodeFactory;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A bean path expression resolver, used to get or set property values in multi-level nested beans. The expression is
 * divided into two parts:
 * <ol>
 * <li>Dot notation (e.g., {@code user.name}), used to access properties of a bean or values in a map.</li>
 * <li>Bracket notation (e.g., {@code users[0]}), used to access elements in a collection or array by their index.</li>
 * </ol>
 *
 * @param <T> The type of the bean being traversed.
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanPath<T> implements Iterator<BeanPath<T>> {

    /**
     * An array of characters that act as delimiters in the expression.
     */
    private static final char[] EXP_CHARS = { Symbol.C_DOT, Symbol.C_BRACKET_LEFT, Symbol.C_BRACKET_RIGHT };

    /**
     * The current node (part) of the expression.
     */
    private final Node node;
    /**
     * The remaining part of the expression (the child path).
     */
    private final String child;
    /**
     * The factory for creating, getting, and setting bean values.
     */
    private final NodeBeanFactory<T> beanFactory;

    /**
     * Constructs a new {@code BeanPath} by parsing the given expression.
     *
     * @param expression  The path expression (e.g., "user.name", "users[0]").
     * @param beanFactory The factory for creating, getting, and setting bean values.
     */
    public BeanPath(final String expression, final NodeBeanFactory<T> beanFactory) {
        this.beanFactory = beanFactory;
        final int length = expression.length();
        final StringBuilder builder = new StringBuilder();

        char c;
        boolean isBracketStart = false; // Flag for '['
        boolean isInQuotes = false; // Flag for being inside single quotes

        for (int i = 0; i < length; i++) {
            c = expression.charAt(i);
            if ('\'' == c) {
                // Toggle quote mode
                isInQuotes = !isInQuotes;
                continue;
            }

            if (!isInQuotes && ArrayKit.contains(EXP_CHARS, c)) {
                // Handle delimiter characters
                if (Symbol.C_BRACKET_RIGHT == c) {
                    // End of a bracketed index
                    if (!isBracketStart) {
                        throw new IllegalArgumentException(StringKit
                                .format("Bad expression '{}':{}, found ']' but no preceding '['", expression, i));
                    }
                    isBracketStart = false;
                } else {
                    if (isBracketStart) {
                        // Found a new delimiter before the current bracket was closed
                        throw new IllegalArgumentException(StringKit
                                .format("Bad expression '{}':{}, found '[' but no closing ']'", expression, i));
                    } else if (Symbol.C_BRACKET_LEFT == c) {
                        // Start of a bracketed index
                        isBracketStart = true;
                    }
                }
                if (!builder.isEmpty()) {
                    this.node = NodeFactory.createNode(builder.toString());
                    // The rest of the string is the child path.
                    // For '[' keep it as part of the child, e.g., for "name[0]".
                    this.child = StringKit.nullIfEmpty(expression.substring(c == Symbol.C_BRACKET_LEFT ? i : i + 1));
                    return;
                }
            } else {
                // Append non-delimiter characters
                builder.append(c);
            }
        }

        // Handle the last node in the expression
        if (isBracketStart) {
            throw new IllegalArgumentException(StringKit
                    .format("Bad expression '{}':{}, found '[' but no closing ']' at the end", expression, length - 1));
        } else {
            this.node = NodeFactory.createNode(builder.toString());
            this.child = null;
        }
    }

    /**
     * Creates a new {@code BeanPath} instance with a default bean factory.
     *
     * @param expression The path expression.
     * @return a new {@code BeanPath} instance.
     */
    public static BeanPath<Object> of(final String expression) {
        return new BeanPath<>(expression, DefaultNodeBeanFactory.INSTANCE);
    }

    /**
     * Creates a new {@code BeanPath} instance with a custom bean factory.
     *
     * @param expression  The path expression.
     * @param beanFactory The factory for creating, getting, and setting bean values.
     * @param <T>         The type of the bean.
     * @return a new {@code BeanPath} instance.
     */
    public static <T> BeanPath<T> of(final String expression, final NodeBeanFactory<T> beanFactory) {
        return new BeanPath<>(expression, beanFactory);
    }

    /**
     * Gets the current node of the expression.
     *
     * @return The current {@link Node}.
     */
    public Node getNode() {
        return this.node;
    }

    /**
     * Gets the remaining (child) part of the expression.
     *
     * @return The child expression string, or null if this is the last node.
     */
    public String getChild() {
        return this.child;
    }

    /**
     * Returns true if the iteration has more elements.
     *
     * @return true if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return null != this.child;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element
     */
    @Override
    public BeanPath<T> next() {
        return new BeanPath<>(this.child, this.beanFactory);
    }

    /**
     * Recursively gets the value corresponding to this path from the given bean.
     *
     * @param bean The root bean object.
     * @return The value at the specified path, or null if not found.
     */
    public Object getValue(final T bean) {
        final Object value = beanFactory.getValue(bean, this);
        if (null == value) {
            return null;
        }
        if (!hasNext()) {
            return value;
        }
        return next().getValue((T) value);
    }

    /**
     * Recursively sets a value for this path in the given bean. If intermediate nodes (beans, collections, etc.) are
     * null, they will be created automatically.
     *
     * @param bean  The root bean object.
     * @param value The value to set at the path.
     * @return The modified bean.
     */
    public Object setValue(T bean, final Object value) {
        if (!hasNext()) {
            // Reached the end of the path, set the value on the current bean.
            return beanFactory.setValue(bean, value, this);
        }

        final BeanPath<T> childBeanPath = next();
        Object subBean = beanFactory.getValue(bean, this);
        if (null == subBean) {
            // Create intermediate objects if they don't exist.
            subBean = beanFactory.of(bean, this);
            beanFactory.setValue(bean, subBean, this);
            // Re-get the value in case the setValue method modified it.
            subBean = beanFactory.getValue(bean, this);
        }
        // Recurse to the next level.
        final Object newSubBean = childBeanPath.setValue((T) subBean, value);
        if (newSubBean != subBean) {
            // If setting a value in a sub-object returned a new instance (e.g., for an array),
            // update the reference in the parent.
            beanFactory.setValue(bean, newSubBean, this);
        }
        return bean;
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "BeanPath{" + "node=" + node + ", child='" + child + '\'' + '}';
    }

}
