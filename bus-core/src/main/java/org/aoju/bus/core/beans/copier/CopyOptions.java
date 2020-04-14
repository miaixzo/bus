/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
package org.aoju.bus.core.beans.copier;

import org.aoju.bus.core.utils.MapUtils;

import java.util.Map;

/**
 * 属性拷贝选项
 * 包括：
 * 1、限制的类或接口,必须为目标对象的实现接口或父类,用于限制拷贝的属性,
 * 例如一个类我只想复制其父类的一些属性,就可以将editable设置为父类
 * 2、是否忽略空值,当源对象的值为null时,true: 忽略,false: 注入
 * 3、忽略的属性列表,设置一个属性列表,不拷贝这些属性值
 *
 * @author Kimi Liu
 * @version 5.8.6
 * @since JDK 1.8+
 */
public class CopyOptions {

    /**
     * 限制的类或接口,必须为目标对象的实现接口或父类,用于限制拷贝的属性,例如一个类我只想复制其父类的一些属性,就可以将editable设置为父类
     */
    protected Class<?> editable;
    /**
     * 是否忽略空值,当源对象的值为null时,true: 忽略而不注入此值,false: 注入null
     */
    protected boolean ignoreNullValue;
    /**
     * 忽略的目标对象中属性列表,设置一个属性列表,不拷贝这些属性值
     */
    protected String[] ignoreProperties;
    /**
     * 是否忽略字段注入错误
     */
    protected boolean ignoreError;
    /**
     * 是否忽略字段大小写
     */
    protected boolean ignoreCase;
    /**
     * 拷贝属性的字段映射,用于不同的属性之前拷贝做对应表用
     */
    protected Map<String, String> fieldMapping;

    /**
     * 构造拷贝选项
     */
    public CopyOptions() {
    }

    /**
     * 构造拷贝选项
     *
     * @param editable         限制的类或接口,必须为目标对象的实现接口或父类,用于限制拷贝的属性
     * @param ignoreNullValue  是否忽略空值,当源对象的值为null时,true: 忽略而不注入此值,false: 注入null
     * @param ignoreProperties 忽略的目标对象中属性列表,设置一个属性列表,不拷贝这些属性值
     */
    public CopyOptions(Class<?> editable, boolean ignoreNullValue, String... ignoreProperties) {
        this.editable = editable;
        this.ignoreNullValue = ignoreNullValue;
        this.ignoreProperties = ignoreProperties;
    }

    /**
     * 创建拷贝选项
     *
     * @return 拷贝选项
     */
    public static CopyOptions create() {
        return new CopyOptions();
    }

    /**
     * 创建拷贝选项
     *
     * @param editable         限制的类或接口,必须为目标对象的实现接口或父类,用于限制拷贝的属性
     * @param ignoreNullValue  是否忽略空值,当源对象的值为null时,true: 忽略而不注入此值,false: 注入null
     * @param ignoreProperties 忽略的属性列表,设置一个属性列表,不拷贝这些属性值
     * @return 拷贝选项
     */
    public static CopyOptions create(Class<?> editable, boolean ignoreNullValue, String... ignoreProperties) {
        return new CopyOptions(editable, ignoreNullValue, ignoreProperties);
    }

    /**
     * 设置限制的类或接口,必须为目标对象的实现接口或父类,用于限制拷贝的属性
     *
     * @param editable 限制的类或接口
     * @return CopyOptions
     */
    public CopyOptions setEditable(Class<?> editable) {
        this.editable = editable;
        return this;
    }

    /**
     * 设置是否忽略空值,当源对象的值为null时,true: 忽略而不注入此值,false: 注入null
     *
     * @param ignoreNullVall 是否忽略空值,当源对象的值为null时,true: 忽略而不注入此值,false: 注入null
     * @return CopyOptions
     */
    public CopyOptions setIgnoreNullValue(boolean ignoreNullVall) {
        this.ignoreNullValue = ignoreNullVall;
        return this;
    }

    /**
     * 设置忽略的目标对象中属性列表,设置一个属性列表,不拷贝这些属性值
     *
     * @param ignoreProperties 忽略的目标对象中属性列表,设置一个属性列表,不拷贝这些属性值
     * @return CopyOptions
     */
    public CopyOptions setIgnoreProperties(String... ignoreProperties) {
        this.ignoreProperties = ignoreProperties;
        return this;
    }

    /**
     * 设置是否忽略字段的注入错误
     *
     * @param ignoreError 是否忽略注入错误
     * @return CopyOptions
     */
    public CopyOptions setIgnoreError(boolean ignoreError) {
        this.ignoreError = ignoreError;
        return this;
    }

    /**
     * 设置是否忽略字段的注入错误
     *
     * @param ignoreCase 是否忽略大小写
     * @return CopyOptions
     */
    public CopyOptions setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        return this;
    }

    /**
     * 设置拷贝属性的字段映射,用于不同的属性之前拷贝做对应表用
     *
     * @param fieldMapping 拷贝属性的字段映射,用于不同的属性之前拷贝做对应表用
     * @return CopyOptions
     */
    public CopyOptions setFieldMapping(Map<String, String> fieldMapping) {
        this.fieldMapping = fieldMapping;
        return this;
    }

    /**
     * 获取反转之后的映射
     *
     * @return 反转映射
     */
    protected Map<String, String> getReversedMapping() {
        return (null != this.fieldMapping) ? MapUtils.reverse(this.fieldMapping) : null;
    }

}
