/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.validate.magic;

import lombok.Data;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.text.replacer.HighMultiReplacer;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.MapKit;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 校验注解所包含的通用属性
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
public class Property {

    private boolean array = false;
    /**
     * 错误码
     */
    private String errcode;
    /**
     * 错误提示信息
     */
    private String errmsg;
    /**
     * 错误属性名称
     */
    private String field;

    private String name;
    /**
     * 校验组信息
     */
    private String[] group;
    /**
     * 当前注解
     */
    private Annotation annotation;
    /**
     * 校验类
     */
    private Class<?> clazz;
    /**
     * 异常信息
     */
    private Class<? extends ValidateException> exception;
    /**
     * 校验参数
     */
    private Map<String, Object> param;
    /**
     * 校验属性信息
     */
    private List<Property> list;

    public Property() {
        this.list = new ArrayList<>();
        this.param = new HashMap<>();
    }

    /**
     * 添加父级校验注解属性
     *
     * @param property 属性
     */
    public void addParentProperty(Property property) {
        if (CollKit.isEmpty(this.list)) {
            this.list = new ArrayList<>();
        }
        this.list.add(property);
    }

    /**
     * 添加错误信息的字符串插值参数
     *
     * @param name  插值名称
     * @param value 插值
     */
    public void addParam(String name, Object value) {
        if (MapKit.isEmpty(this.param)) {
            this.param = new HashMap<>();
        }
        if (this.param.containsKey(name)) {
            throw new IllegalArgumentException("当前异常信息格式化参数已经存在:" + name);
        }
        this.param.put(name, value);
    }

    /**
     * 获取字符串插值后的验证信息
     *
     * @return the string
     */
    public String getFormatted() {
        StringBuilder text = new StringBuilder();
        HighMultiReplacer.of(this.param).replace(this.errmsg, 0, text);
        return text.toString();
    }

}
