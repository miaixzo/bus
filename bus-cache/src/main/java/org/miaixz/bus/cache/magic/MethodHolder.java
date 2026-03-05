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
package org.miaixz.bus.cache.magic;

import lombok.Getter;
import lombok.Setter;

/**
 * A container for metadata about a method's return type.
 * <p>
 * This class stores information such as the return type, the generic type of a collection if applicable, and a flag
 * indicating whether the return type is a collection. It uses Lombok annotations to simplify the creation of getter and
 * setter methods.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class MethodHolder {

    /**
     * The generic type of the collection if the return type is a collection.
     */
    private Class<?> innerReturnType;

    /**
     * The return type of the method.
     */
    private Class<?> returnType;

    /**
     * A flag indicating whether the return type is a collection.
     */
    private boolean collection;

    /**
     * Constructs a new {@code MethodHolder}.
     *
     * @param collection {@code true} if the method's return type is a collection, otherwise {@code false}.
     */
    public MethodHolder(boolean collection) {
        this.collection = collection;
    }

    /**
     * Checks if the method's return type is a collection.
     *
     * @return {@code true} if the return type is a collection, otherwise {@code false}.
     */
    public boolean isCollection() {
        return collection;
    }

    /**
     * Gets the return type of the method.
     *
     * @return The method's return type.
     */
    public Class<?> getReturnType() {
        return returnType;
    }

    /**
     * Sets the return type of the method.
     *
     * @param returnType The method's return type.
     */
    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    /**
     * Gets the inner return type, which is the element type for collections.
     *
     * @return The inner return type, or {@code null} if not applicable.
     */
    public Class<?> getInnerReturnType() {
        return innerReturnType;
    }

    /**
     * Sets the inner return type, which is the element type for collections.
     *
     * @param innerReturnType The inner return type.
     */
    public void setInnerReturnType(Class<?> innerReturnType) {
        this.innerReturnType = innerReturnType;
    }

}
