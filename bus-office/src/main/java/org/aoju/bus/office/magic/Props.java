/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.office.magic;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.WrappedTargetException;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.util.Optional;

/**
 * 实用程序函数，使office属性更易于使用.
 *
 * @author Kimi Liu
 * @version 6.1.1
 * @since JDK 1.8+
 */
public final class Props {

    /**
     * 从指定对象的属性中获取属性值.
     *
     * @param obj      获取属性的对象.
     * @param propName 要获取的属性名.
     * @return 包含属性值的可选属性。如果无法检索属性，可选属性将为空.
     * @throws InstrumentException 如果发生UNO异常。UNO异常将导致 {@link InstrumentException}.
     */
    public static Optional<Object> getProperty(final Object obj, final String propName) {
        return getProperty(Lo.qi(XPropertySet.class, obj), propName);
    }

    /**
     * 从指定的属性获取属性值.
     *
     * @param props    获取属性的{@link XPropertySet}.
     * @param propName 要获取的属性名.
     * @return 包含属性值的可选属性。如果无法检索属性，可选属性将为空.
     * @throws InstrumentException 如果发生UNO异常。UNO异常将导致 {@link InstrumentException}.
     */
    public static Optional<Object> getProperty(final XPropertySet props, final String propName) {
        try {
            return Optional.ofNullable(props.getPropertyValue(propName));
        } catch (UnknownPropertyException | WrappedTargetException ex) {
            throw new InstrumentException(ex.getMessage(), ex);
        }
    }

    /**
     * 使用指定的属性名和值创建{@link PropertyValue}和单个属性的数组.
     *
     * @param name  属性名.
     * @param value 属性值.
     * @return 大小为1的数组.
     */
    public static PropertyValue[] makeProperties(final String name, final Object value) {
        final PropertyValue[] props = new PropertyValue[]{new PropertyValue()};
        props[0].Name = name;
        props[0].Value = value;
        return props;
    }

    /**
     * 使用指定的属性名和值创建一个包含两个属性的{@link PropertyValue}数组.
     *
     * @param name1  第一个属性名.
     * @param value1 第一个属性值.
     * @param name2  第二个属性名.
     * @param value2 第二个属性值.
     * @return 大小为2的数组.
     */
    public static PropertyValue[] makeProperties(
            final String name1, final Object value1, final String name2, final Object value2) {
        final PropertyValue[] props = new PropertyValue[]{new PropertyValue(), new PropertyValue()};
        props[0].Name = name1;
        props[0].Value = value1;
        props[1].Name = name2;
        props[1].Value = value2;
        return props;
    }

    /**
     * 使用指定的属性名和值创建{@link PropertyValue}的属性数组.
     *
     * @param names  属性名.
     * @param values 属性值.
     * @return 属性数组.
     */
    public static PropertyValue[] makeProperties(final String[] names, final Object[] values) {
        if (names.length != values.length) {
            throw new IllegalArgumentException("Mismatch in lengths of names and values");
        }

        final PropertyValue[] props = new PropertyValue[names.length];
        for (int i = 0; i < names.length; i++) {
            props[i] = new PropertyValue();
            props[i].Name = names[i];
            props[i].Value = values[i];
        }
        return props;
    }

}
