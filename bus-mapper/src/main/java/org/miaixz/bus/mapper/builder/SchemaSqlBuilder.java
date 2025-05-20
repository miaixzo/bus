/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.mapper.builder;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Objects;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.miaixz.bus.mapper.parsing.SqlScriptWrapper;

/**
 * 注解方式的 {@link SqlScriptWrapper}，提供基于注解的 SQL 脚本包装功能
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class SchemaSqlBuilder implements SqlScriptWrapper {

    /**
     * 注解应用的目标元素类型（类、方法、参数等）
     */
    protected final ElementType type;

    /**
     * 注解的目标对象（类、方法或参数）
     */
    protected final Object target;

    /**
     * 注解数组
     */
    protected final Annotation[] annotations;

    /**
     * 构造函数，初始化注解包装器
     *
     * @param target      目标对象
     * @param type        元素类型
     * @param annotations 注解数组
     */
    public SchemaSqlBuilder(Object target, ElementType type, Annotation[] annotations) {
        this.type = type;
        this.target = target;
        this.annotations = annotations;
    }

    /**
     * 获取注解的目标元素类型
     *
     * @return 元素类型
     */
    public ElementType getType() {
        return type;
    }

    /**
     * 获取注解的目标对象
     *
     * @return 目标对象
     */
    public Object getTarget() {
        return target;
    }

    /**
     * 获取注解数组
     *
     * @return 注解数组
     */
    public Annotation[] getAnnotations() {
        return annotations;
    }

    /**
     * 获取参数名称
     *
     * @param parameter 参数对象
     * @return 参数名称
     */
    public String getParameterName(Parameter parameter) {
        // 优先使用 @Param 注解指定的值
        for (Annotation a : annotations) {
            if (a.annotationType() == Param.class) {
                return ((Param) a).value();
            }
        }
        Executable executable = parameter.getDeclaringExecutable();
        // 只有一个参数时，只能使用默认名称
        if (executable.getParameterCount() == 1) {
            return DynamicContext.PARAMETER_OBJECT_KEY;
        }
        // 参数名
        String name = parameter.getName();
        if (!name.startsWith("arg")) {
            return name;
        }
        // 获取参数顺序号
        int index = 0;
        Parameter[] parameters = executable.getParameters();
        for (; index < parameters.length; index++) {
            if (parameters[index] == parameter) {
                break;
            }
        }
        // 如果方法不是默认名，就直接使用该名称
        if (!name.equals("arg" + index)) {
            return name;
        } else {
            return ParamNameResolver.GENERIC_NAME_PREFIX + (index + 1);
        }
    }

    /**
     * 判断两个对象是否相等
     *
     * @param o 比较对象
     * @return true 表示相等，false 表示不相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SchemaSqlBuilder that = (SchemaSqlBuilder) o;
        return type == that.type && target.equals(that.target);
    }

    /**
     * 计算对象的哈希值
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, target);
    }

}