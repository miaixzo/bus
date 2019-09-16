/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.utils;

import org.aoju.bus.core.key.ObjectID;

/**
 * 实体类相关工具类
 * 解决问题： 1、快速对实体的常驻字段
 *
 * @author Kimi Liu
 * @version 3.2.8
 * @since JDK 1.8
 */
public class EntityUtils {

    /**
     * 快速将bean的creator、created附上相关值
     *
     * @param <T>    对象
     * @param entity 反射对象
     */
    public static <T> void setCreateInfo(T entity) {
        String id = ObjectID.id();
        String[] fields = {"id", "creator", "created"};
        Object[] value = new Object[]{id, getValue(entity, "x_user_id"), StringUtils.toString(DateUtils.timestamp())};
        setValue(entity, fields, value);
    }

    /**
     * 快速将bean的modifier、modified附上相关值
     *
     * @param <T>    对象
     * @param entity 反射对象
     */
    public static <T> void setUpdatedInfo(T entity) {
        String[] fields = {"modifier", "modified"};
        Object[] value = new Object[]{getValue(entity, "x_user_id"), StringUtils.toString(DateUtils.timestamp())};
        setValue(entity, fields, value);
    }

    public static <T> void setCreatAndUpdatInfo(T entity) {
        setCreateInfo(entity);
        setUpdatedInfo(entity);
    }

    /**
     * 依据对象的属性数组和值数组对进行赋值
     *
     * @param <T>    对象
     * @param entity 反射对象
     * @param fields 属性数组
     * @param value  值数组
     */
    private static <T> void setValue(T entity, String[] fields, Object[] value) {
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (ReflectUtils.hasField(entity, field)) {
                ReflectUtils.invokeSetter(entity, field, value[i]);
            }
        }
    }

    /**
     * 依据对象的属性获取对象值
     *
     * @param <T>    对象
     * @param entity 反射对象
     * @param field  属性数组
     */
    private static <T> Object getValue(T entity, String field) {
        if (ReflectUtils.hasField(entity, field)) {
            Object object = ReflectUtils.invokeGetter(entity, field);
            return object != null ? object.toString() : null;
        }
        return null;
    }

    /**
     * 根据主键属性，判断主键是否值为空
     *
     * @param <T>    对象
     * @param entity 反射对象
     * @param field  属性
     * @return 主键为空，则返回false；主键有值，返回true
     */
    public static <T> boolean isPKNotNull(T entity, String field) {
        if (!ReflectUtils.hasField(entity, field)) {
            return false;
        }
        Object value = ReflectUtils.getFieldValue(entity, field);
        return value != null && !"".equals(value);
    }

}
