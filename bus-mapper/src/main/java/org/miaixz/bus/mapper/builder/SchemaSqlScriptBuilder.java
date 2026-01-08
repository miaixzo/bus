/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import org.miaixz.bus.mapper.Order;
import org.miaixz.bus.mapper.annotation.SqlWrapper;
import org.miaixz.bus.mapper.parsing.SqlScript;
import org.miaixz.bus.mapper.parsing.SqlScriptWrapper;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Supports SQL extension by processing {@link SqlWrapper} annotations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SchemaSqlScriptBuilder implements SqlScriptWrapper {

    /**
     * Wraps an SQL script by applying annotations from the interface, method, and parameters.
     *
     * @param context   The provider context.
     * @param tableMeta The entity table information.
     * @param sqlScript The SQL script to be wrapped.
     * @return The wrapped SQL script.
     */
    @Override
    public SqlScript wrap(ProviderContext context, TableMeta tableMeta, SqlScript sqlScript) {
        Class<?> mapperType = context.getMapperType();
        Method mapperMethod = context.getMapperMethod();
        // Interface annotations
        List<SchemaSqlBuilder> wrappers = parseAnnotations(mapperType, ElementType.TYPE, mapperType.getAnnotations());
        // Method annotations
        wrappers.addAll(parseAnnotations(mapperMethod, ElementType.METHOD, mapperMethod.getAnnotations()));
        // Parameter annotations
        Parameter[] parameters = mapperMethod.getParameters();
        Annotation[][] parameterAnnotations = mapperMethod.getParameterAnnotations();
        for (int i = 0; i < parameters.length; i++) {
            wrappers.addAll(parseAnnotations(parameters[i], ElementType.PARAMETER, parameterAnnotations[i]));
        }
        // Deduplicate and sort
        wrappers = wrappers.stream().distinct().sorted(Comparator.comparing(f -> ((Order) f).order()).reversed())
                .collect(Collectors.toList());
        for (SqlScriptWrapper wrapper : wrappers) {
            sqlScript = wrapper.wrap(context, tableMeta, sqlScript);
        }
        return sqlScript;
    }

    /**
     * Instantiates a {@link SchemaSqlBuilder} object.
     *
     * @param instanceClass The class to instantiate.
     * @param target        The target object.
     * @param type          The element type.
     * @param annotations   The array of annotations.
     * @param <T>           The generic type.
     * @return The instantiated object.
     */
    public <T> T newInstance(Class<T> instanceClass, Object target, ElementType type, Annotation[] annotations) {
        try {
            return instanceClass.getConstructor(Object.class, ElementType.class, Annotation[].class)
                    .newInstance(target, type, annotations);
        } catch (Exception e) {
            throw new RuntimeException("instance [ " + instanceClass + " ] error", e);
        }
    }

    /**
     * Parses {@link SchemaSqlBuilder} instances from an object's annotations.
     *
     * @param target      The target object (class, method, or parameter).
     * @param type        The element type (TYPE, METHOD, PARAMETER).
     * @param annotations The array of annotations.
     * @return A list of {@link SchemaSqlBuilder} instances.
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
