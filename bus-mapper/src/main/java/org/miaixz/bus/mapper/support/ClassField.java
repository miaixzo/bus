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
package org.miaixz.bus.mapper.support;

import java.util.function.Predicate;

import org.miaixz.bus.mapper.mapping.MapperColumn;

/**
 * 记录字段对应的类和字段名，用于匹配实体类字段与数据库列的属性。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClassField implements Predicate<MapperColumn> {

    /**
     * 实体类
     */
    private final Class<?> clazz;

    /**
     * 字段名称
     */
    private final String field;

    /**
     * 构造函数，初始化类和字段信息。
     *
     * @param clazz 实体类
     * @param field 字段名称
     */
    public ClassField(Class<?> clazz, String field) {
        this.clazz = clazz;
        this.field = field;
    }

    /**
     * 判断指定列的属性名是否与当前字段名匹配（忽略大小写）。
     *
     * @param column 数据库列信息
     * @return 如果属性名匹配则返回 true，否则返回 false
     */
    @Override
    public boolean test(MapperColumn column) {
        return getField().equalsIgnoreCase(column.property());
    }

    /**
     * 获取实体类。
     *
     * @return 实体类
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * 获取字段名称。
     *
     * @return 字段名称
     */
    public String getField() {
        return field;
    }

}