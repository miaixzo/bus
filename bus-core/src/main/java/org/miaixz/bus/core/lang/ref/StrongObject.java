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
