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
package org.miaixz.bus.shade.screw.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Represents a pair of a {@link Field} and its corresponding {@link Method} (typically a setter). This class is used
 * internally for mapping operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FieldMethod {

    /**
     * The {@link Field} object.
     */
    private Field field;

    /**
     * The corresponding {@link Method} object, usually a setter.
     */
    private Method method;

    /**
     * Gets the {@link Field} object.
     *
     * @return The {@link Field} object.
     */
    public Field getField() {
        return field;
    }

    /**
     * Sets the {@link Field} object.
     *
     * @param field The {@link Field} to set.
     */
    public void setField(Field field) {
        this.field = field;
    }

    /**
     * Gets the {@link Method} object.
     *
     * @return The {@link Method} object.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Sets the {@link Method} object.
     *
     * @param method The {@link Method} to set.
     */
    public void setMethod(Method method) {
        this.method = method;
    }

}
