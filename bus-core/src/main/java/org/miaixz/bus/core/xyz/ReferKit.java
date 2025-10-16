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
package org.miaixz.bus.core.xyz;

import java.lang.ref.*;

import org.miaixz.bus.core.lang.ref.Ref;
import org.miaixz.bus.core.lang.ref.ReferenceType;

/**
 * Utility class for {@link java.lang.ref.Reference}. This class provides wrappers for:
 * 
 * <pre>
 * 1. {@link SoftReference}: Collected by the GC when memory is low.
 * 2. {@link WeakReference}: Collected by the GC during its normal cycle.
 * 3. {@link PhantomReference}: Enqueued in a {@link ReferenceQueue} when its referent is detected by the GC.
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReferKit {

    /**
     * Creates a {@link Reference} of a specific type.
     *
     * @param <T>      The type of the referenced object.
     * @param type     The type of the reference.
     * @param referent The object to be referenced.
     * @return The {@link Reference}.
     */
    public static <T> Reference<T> of(final ReferenceType type, final T referent) {
        return of(type, referent, null);
    }

    /**
     * Creates a {@link Reference} of a specific type with a `ReferenceQueue`.
     *
     * @param <T>      The type of the referenced object.
     * @param type     The type of the reference.
     * @param referent The object to be referenced.
     * @param queue    The reference queue.
     * @return The {@link Reference}.
     */
    public static <T> Reference<T> of(final ReferenceType type, final T referent, final ReferenceQueue<T> queue) {
        switch (type) {
            case SOFT:
                return new SoftReference<>(referent, queue);

            case WEAK:
                return new WeakReference<>(referent, queue);

            case PHANTOM:
                return new PhantomReference<>(referent, queue);

            default:
                return null;
        }
    }

    /**
     * Safely gets the referenced object from a `Reference`.
     *
     * @param <T>    The type of the object.
     * @param object The `Reference` object.
     * @return The referenced object, or `null`.
     */
    public static <T> T get(final Reference<T> object) {
        return ObjectKit.apply(object, Reference::get);
    }

    /**
     * Safely gets the referenced object from a `Ref`.
     *
     * @param <T>    The type of the object.
     * @param object The `Ref` object.
     * @return The referenced object, or `null`.
     */
    public static <T> T get(final Ref<T> object) {
        return ObjectKit.apply(object, Ref::get);
    }

}
