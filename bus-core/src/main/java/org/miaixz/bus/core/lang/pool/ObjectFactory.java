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
package org.miaixz.bus.core.lang.pool;

/**
 * Interface for an object factory, providing methods for custom object creation, validation, and destruction. This
 * interface is inspired by:
 * <a href="https://github.com/DanielYWoo/fast-object-pool/">https://github.com/DanielYWoo/fast-object-pool/</a>
 *
 * @param <T> the type of object managed by this factory
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ObjectFactory<T> {

    /**
     * Creates a new instance of the object.
     *
     * @return the newly created object
     */
    T of();

    /**
     * Validates the usability of an object. This is typically used before borrowing an object from the pool or before
     * returning an object to the pool.
     *
     * @param t the object to be validated
     * @return {@code true} if the object is valid and usable, {@code false} otherwise
     */
    boolean validate(T t);

    /**
     * Destroys an object. This method defines the logic for disposing of an object when it is no longer needed or found
     * to be invalid.
     *
     * @param t the object to be destroyed
     */
    void destroy(T t);

}
