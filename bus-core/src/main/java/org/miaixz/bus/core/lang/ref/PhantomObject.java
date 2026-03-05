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
package org.miaixz.bus.core.lang.ref;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;

/**
 * A phantom reference object. When the garbage collector determines that a phantom reference's referent is phantom
 * reachable, the {@link PhantomReference} object is enqueued on its {@link ReferenceQueue}. The object is not yet
 * finalized. It will be reclaimed only after the {@link ReferenceQueue} is processed.
 *
 * @param <T> The type of the object held by this phantom reference.
 * @author Kimi Liu
 * @since Java 17+
 */
public class PhantomObject<T> extends PhantomReference<T> implements Ref<T> {

    /**
     * The pre-calculated hash code of the referenced object. This is stored to ensure consistent hash code behavior
     * even if the referenced object is cleared.
     */
    private final int hashCode;

    /**
     * Constructs a new phantom reference to the given object. The reference is registered with the given queue. The
     * hash code of the object is pre-calculated and stored.
     *
     * @param object The object to be phantom referenced.
     * @param queue  The reference queue with which the phantom reference is to be registered.
     */
    public PhantomObject(final T object, final ReferenceQueue<? super T> queue) {
        super(object, queue);
        hashCode = Objects.hashCode(object);
    }

    /**
     * Returns the hash code for this phantom reference. The hash code is based on the hash code of the referenced
     * object at the time of construction. This ensures that the hash code remains consistent.
     *
     * @return The hash code of this phantom reference.
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Compares this phantom reference to the specified object. The result is {@code true} if and only if the argument
     * is a {@code PhantomObject}. Note that since {@link PhantomReference#get()} always returns {@code null}, this
     * comparison does not depend on the referenced objects.
     *
     * @param other The object to compare with.
     * @return {@code true} if the given object is a {@code PhantomObject}, {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof PhantomObject) {
            return this.hashCode == ((PhantomObject<?>) other).hashCode;
        }
        return false;
    }

}
