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
package org.miaixz.bus.core.lang.mutable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * A mutable {@code Object} wrapper.
 *
 * @param <T> The type of the mutable object.
 * @author Kimi Liu
 * @since Java 17+
 */
public class MutableObject<T> implements Mutable<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852270938613L;

    /**
     * The mutable value.
     */
    private T value;

    /**
     * Constructs a new MutableObject with a {@code null} value.
     */
    public MutableObject() {

    }

    /**
     * Constructs a new MutableObject with the specified value.
     *
     * @param value The initial value.
     */
    public MutableObject(final T value) {
        this.value = value;
    }

    /**
     * Creates a new {@code MutableObject}.
     *
     * @param value The value to wrap.
     * @param <T>   The type of the value.
     * @return A new {@code MutableObject} instance.
     */
    public static <T> MutableObject<T> of(final T value) {
        return new MutableObject<>(value);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Override
    public T get() {
        return this.value;
    }

    /**
     * Set method.
     */
    @Override
    public void set(final T value) {
        this.value = value;
    }

    /**
     * Checks if this object equals another object.
     *
     * @param object the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (this.getClass() == object.getClass()) {
            final MutableObject<?> that = (MutableObject<?>) object;
            return ObjectKit.equals(this.value, that.value);
        }
        return false;
    }

    /**
     * Returns the hash code for this MutableObject.
     *
     * @return The hash code based on the current value.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Returns the string representation of this MutableObject.
     *
     * @return The string representation of the current value, or "null" if the value is null.
     */
    @Override
    public String toString() {
        return value == null ? "null" : value.toString();
    }

}
