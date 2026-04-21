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
 * @since Java 21+
 */
public abstract class SrcToDestCopier<T, C extends SrcToDestCopier<T, C>> implements Copier<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852251585211L;

    /**
     * Constructs a source-to-destination copier.
     */
    protected SrcToDestCopier() {
    }

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
