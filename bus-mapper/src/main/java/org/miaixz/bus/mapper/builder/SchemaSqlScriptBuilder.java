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
package org.miaixz.bus.mapper.builder;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.mapper.ORDER;
import org.miaixz.bus.mapper.annotation.SqlWrapper;
import org.miaixz.bus.mapper.parsing.SqlScript;
import org.miaixz.bus.mapper.parsing.SqlScriptWrapper;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * 通过 {@link SqlWrapper} 注解支持对 SQL 的扩展
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SchemaSqlScriptBuilder implements SqlScriptWrapper {

    /**
     * 对 SQL 脚本进行包装，应用接口、方法和参数上的注解
     *
     * @param context   提供者上下文
     * @param entity    实体表信息
     * @param sqlScript SQL 脚本
     * @return 包装后的 SQL 脚本
     */
    @Override
    public SqlScript wrap(ProviderContext context, TableMeta entity, SqlScript sqlScript) {
        Class<?> mapperType = context.getMapperType();
        Method mapperMethod = context.getMapperMethod();
        // 接口注解
        List<SchemaSqlBuilder> wrappers = parseAnnotations(mapperType, ElementType.TYPE, mapperType.getAnnotations());
        // 方法注解
        wrappers.addAll(parseAnnotations(mapperMethod, ElementType.METHOD, mapperMethod.getAnnotations()));
        // 参数注解
        Parameter[] parameters = mapperMethod.getParameters();
        Annotation[][] parameterAnnotations = mapperMethod.getParameterAnnotations();
        for (int i = 0; i < parameters.length; i++) {
            wrappers.addAll(parseAnnotations(parameters[i], ElementType.PARAMETER, parameterAnnotations[i]));
        }
        // 去重，排序
        wrappers = wrappers.stream().distinct().sorted(Comparator.comparing(f -> ((ORDER) f).order()).reversed())
                .collect(Collectors.toList());
        for (SqlScriptWrapper wrapper : wrappers) {
            sqlScript = wrapper.wrap(context, entity, sqlScript);
        }
        return sqlScript;
    }

    /**
     * 实例化 {@link SchemaSqlBuilder} 对象
     *
     * @param instanceClass 实例类
     * @param target        目标对象
     * @param type          元素类型
     * @param annotations   注解数组
     * @param <T>           泛型
     * @return 实例化的对象
     */
    public <T> T newInstance(Class<T> instanceClass, Object target, ElementType type, Annotation[] annotations) {
        try {
            return instanceClass.getConstructor(Object.class, ElementType.class, Annotation[].class).newInstance(target,
                    type, annotations);
        } catch (Exception e) {
            throw new RuntimeException("instance [ " + instanceClass + " ] error", e);
        }
    }

    /**
     * 解析对象上的 {@link SchemaSqlBuilder} 实例
     *
     * @param target      目标对象（类、方法或参数）
     * @param type        元素类型（TYPE, METHOD, PARAMETER）
     * @param annotations 注解数组
     * @return {@link SchemaSqlBuilder} 实例列表
     */
    protected List<SchemaSqlBuilder> parseAnnotations(Object target, ElementType type, Annotation[] annotations) {
        List<Class<? extends SchemaSqlBuilder>> classes = new ArrayList<>();
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType == SqlWrapper.class) {
                classes.addAll(Arrays.asList(((SqlWrapper) annotation).value()));
            } else if (annotationType.isAnnotationPresent(SqlWrapper.class)) {
                SqlWrapper annotationTypeAnnotation = annotationType.getAnnotation(SqlWrapper.class);
                classes.addAll(Arrays.asList(annotationTypeAnnotation.value()));
            }
        }
        return classes.stream().map(c -> (SchemaSqlBuilder) newInstance(c, target, type, annotations))
                .collect(Collectors.toList());
    }

}