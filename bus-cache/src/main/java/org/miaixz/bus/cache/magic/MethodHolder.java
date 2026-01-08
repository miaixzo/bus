/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
