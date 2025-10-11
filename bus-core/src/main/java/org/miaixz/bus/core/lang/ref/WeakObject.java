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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;

import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * A weak reference object, which is automatically cleared by the garbage collector when it is no longer strongly or
 * softly reachable. This class extends {@link WeakReference} and implements the {@link Ref} interface to provide a
 * consistent reference API.
 *
 * @param <T> The type of the object held by this weak reference.
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeakObject<T> extends WeakReference<T> implements Ref<T> {

    /**
     * The pre-calculated hash code of the referenced object. This is stored to ensure consistent hash code behavior
     * even if the referenced object is cleared.
     */
    private final int hashCode;

    /**
     * Constructs a new weak reference to the given object. The reference is registered with the given queue. The hash
     * code of the object is pre-calculated and stored.
     *
     * @param object The object to be weakly referenced.
     * @param queue  The reference queue with which the weak reference is to be registered.
     */
    public WeakObject(final T object, final ReferenceQueue<? super T> queue) {
        super(object, queue);
        hashCode = Objects.hashCode(object);
    }

    /**
     * Returns the hash code for this weak reference. The hash code is based on the hash code of the referenced object
     * at the time of construction. This ensures that the hash code remains consistent even if the referenced object is
     * cleared.
     *
     * @return The hash code of this weak reference.
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Compares this weak reference to the specified object. The result is {@code true} if and only if the argument is a
     * {@code WeakObject} and the objects referenced by both weak references are {@code equals()}.
     *
     * @param object The object to compare with.
     * @return {@code true} if the given object refers to the same object as this weak reference, {@code false}
     *         otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        } else if (object instanceof WeakObject) {
            return ObjectKit.equals(((WeakObject<?>) object).get(), get());
        }
        return false;
    }

}
