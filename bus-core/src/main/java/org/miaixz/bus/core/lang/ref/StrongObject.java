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
package org.miaixz.bus.core.lang.ref;

import java.util.Objects;

import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * A strong reference object that wraps an object of type {@code T}. Unlike soft or weak references, a strong reference
 * prevents the garbage collector from reclaiming the object as long as the strong reference itself is reachable. This
 * class implements the {@link Ref} interface to provide a consistent reference API.
 *
 * @param <T> The type of the object held by this strong reference.
 * @author Kimi Liu
 * @since Java 17+
 */
public class StrongObject<T> implements Ref<T> {

    /**
     * The strongly referenced object.
     */
    private final T object;

    /**
     * Constructs a new strong reference to the given object.
     *
     * @param object The object to be strongly referenced.
     */
    public StrongObject(final T object) {
        this.object = object;
    }

    /**
     * Returns the referenced object. This will never return {@code null} unless the original object was {@code null}.
     *
     * @return The referenced object.
     */
    @Override
    public T get() {
        return this.object;
    }

    /**
     * Returns the hash code for this strong reference, which is the hash code of the referenced object.
     *
     * @return The hash code of this strong reference.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(this.object);
    }

    /**
     * Compares this strong reference to the specified object. The result is {@code true} if and only if the argument is
     * a {@code StrongObject} and the objects referenced by both strong references are {@code equals()}.
     *
     * @param other The object to compare with.
     * @return {@code true} if the given object refers to the same object as this strong reference, {@code false}
     *         otherwise.
     */
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof StrongObject) {
            return ObjectKit.equals(((StrongObject<?>) other).get(), get());
        }
        return false;
    }

}
