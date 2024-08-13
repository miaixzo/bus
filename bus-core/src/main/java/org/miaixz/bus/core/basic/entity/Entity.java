/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.basic.entity;

import java.io.Serializable;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.FieldKit;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity 实体
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Entity implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 数据主键
     */
    @Id
    protected String id;

    /**
     * 根据主键属性,判断主键是否值为空
     *
     * @param <T>    对象
     * @param entity 反射对象
     * @param field  属性
     * @return 主键为空, 则返回false；主键有值,返回true
     */
    public <T> boolean isPKNotNull(T entity, String field) {
        if (!FieldKit.hasField(entity.getClass(), field)) {
            return false;
        }
        Object value = FieldKit.getFieldValue(entity, field);
        return null != value && !Normal.EMPTY.equals(value);
    }

    /**
     * 依据对象的属性获取对象值
     *
     * @param <T>    对象
     * @param entity 反射对象
     * @param field  属性数组
     * @return 返回对象属性值
     */
    public <T> Object getValue(T entity, String field) {
        if (FieldKit.hasField(entity.getClass(), field)) {
            Object value = FieldKit.getFieldValue(entity, field);
            return null != value ? value.toString() : null;
        }
        return null;
    }

    /**
     * 依据对象的属性数组和值数组对进行赋值
     *
     * @param <T>    对象
     * @param entity 反射对象
     * @param fields 属性数组
     * @param value  值数组
     */
    public <T> void setValue(T entity, String[] fields, Object[] value) {
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (FieldKit.hasField(entity.getClass(), field)) {
                FieldKit.setFieldValue(entity, field, value[i]);
            }
        }
    }

}
