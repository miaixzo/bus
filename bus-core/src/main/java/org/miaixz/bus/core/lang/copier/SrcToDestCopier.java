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
package org.miaixz.bus.core.lang.copier;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.Predicate;

/**
 * Abstract copier class that defines the basic structure for copying an object of type {@code T} from a source to a
 * destination. Implementations of this abstract class will provide the concrete copying logic within the
 * {@link #copy()} method.
 *
 * @param <T> The type of the object being copied.
 * @param <C> The type of the copier itself, used for fluent programming (method chaining).
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class SrcToDestCopier<T, C extends SrcToDestCopier<T, C>> implements Copier<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852251585211L;

    /**
     * The source object from which data will be copied.
     */
    protected T src;
    /**
     * The target object to which data will be copied.
     */
    protected T target;
    /**
     * A predicate used to filter which parts of the source should be copied. If the predicate evaluates to
     * {@code false} for a given element, that element will not be copied.
     */
    protected Predicate<T> copyPredicate;

    /**
     * Retrieves the source object of the copier.
     *
     * @return The source object.
     */
    public T getSrc() {
        return src;
    }

    /**
     * Sets the source object for the copier.
     *
     * @param src The source object.
     * @return This copier instance, allowing for method chaining.
     */
    public C setSrc(final T src) {
        this.src = src;
        return (C) this;
    }

    /**
     * Retrieves the target object of the copier.
     *
     * @return The target object.
     */
    public T getTarget() {
        return target;
    }

    /**
     * Sets the target object for the copier.
     *
     * @param target The target object.
     * @return This copier instance, allowing for method chaining.
     */
    public C setTarget(final T target) {
        this.target = target;
        return (C) this;
    }

    /**
     * Retrieves the copy predicate (filter) used by this copier.
     *
     * @return The {@link Predicate} used for filtering copy operations.
     */
    public Predicate<T> getCopyPredicate() {
        return copyPredicate;
    }

    /**
     * Sets the copy predicate (filter) for this copier. This predicate can be used to selectively copy elements from
     * the source to the target.
     *
     * @param copyPredicate The {@link Predicate} to set.
     * @return This copier instance, allowing for method chaining.
     */
    public C setCopyPredicate(final Predicate<T> copyPredicate) {
        this.copyPredicate = copyPredicate;
        return (C) this;
    }

}
