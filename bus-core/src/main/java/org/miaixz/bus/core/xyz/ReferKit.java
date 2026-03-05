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
