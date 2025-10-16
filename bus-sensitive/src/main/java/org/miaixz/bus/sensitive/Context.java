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
package org.miaixz.bus.sensitive;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.sensitive.magic.annotation.Shield;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the execution context for a desensitization operation. It holds information about the current object,
 * field, and annotations being processed.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class Context {

    /**
     * The current object being processed.
     */
    private Object currentObject;

    /**
     * The current field being processed.
     */
    private Field currentField;

    /**
     * A list of all fields in the current object's class.
     */
    private List<Field> allFieldList = new ArrayList<>();

    /**
     * The current {@link Shield} annotation being applied.
     */
    private Shield shield;

    /**
     * The class of the current object.
     */
    private Class<?> beanClass;

    /**
     * The entry object, used when processing elements of a collection or array.
     */
    private Object entry;

    /**
     * An extension property for custom data.
     */
    private String extension;

    /**
     * Creates a new {@code Context} instance.
     *
     * @return this new instance.
     */
    public static Context newInstance() {
        return new Context();
    }

    /**
     * Gets the name of the current field being processed.
     *
     * @return The field name.
     */
    public String getCurrentFieldName() {
        return this.currentField.getName();
    }

    /**
     * Gets the value of the current field being processed.
     *
     * @return The field's value.
     */
    public Object getCurrentFieldValue() {
        try {
            return this.currentField.get(this.currentObject);
        } catch (IllegalAccessException e) {
            throw new InternalException(e);
        }
    }

}
