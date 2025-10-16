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
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Objects;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.miaixz.bus.mapper.parsing.SqlScriptWrapper;

/**
 * An annotation-based {@link SqlScriptWrapper} that provides SQL script wrapping functionality based on annotations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class SchemaSqlBuilder implements SqlScriptWrapper {

    /**
     * The target element type for the annotation (e.g., class, method, parameter).
     */
    protected final ElementType type;

    /**
     * The target object of the annotation (e.g., class, method, or parameter).
     */
    protected final Object target;

    /**
     * The array of annotations.
     */
    protected final Annotation[] annotations;

    /**
     * Constructs an annotation wrapper.
     *
     * @param target      The target object.
     * @param type        The element type.
     * @param annotations The array of annotations.
     */
    public SchemaSqlBuilder(Object target, ElementType type, Annotation[] annotations) {
        this.type = type;
        this.target = target;
        this.annotations = annotations;
    }

    /**
     * Gets the target element type of the annotation.
     *
     * @return The element type.
     */
    public ElementType getType() {
        return type;
    }

    /**
     * Gets the target object of the annotation.
     *
     * @return The target object.
     */
    public Object getTarget() {
        return target;
    }

    /**
     * Gets the array of annotations.
     *
     * @return The array of annotations.
     */
    public Annotation[] getAnnotations() {
        return annotations;
    }

    /**
     * Gets the name of a parameter.
     *
     * @param parameter The parameter object.
     * @return The name of the parameter.
     */
    public String getParameterName(Parameter parameter) {
        // Prioritize the value specified by the @Param annotation.
        for (Annotation a : annotations) {
            if (a.annotationType() == Param.class) {
                return ((Param) a).value();
            }
        }
        Executable executable = parameter.getDeclaringExecutable();
        // When there is only one parameter, use the default name.
        if (executable.getParameterCount() == 1) {
            return DynamicContext.PARAMETER_OBJECT_KEY;
        }
        // Get the parameter name.
        String name = parameter.getName();
        if (!name.startsWith("arg")) {
            return name;
        }
        // Get the parameter's sequential index.
        int index = 0;
        Parameter[] parameters = executable.getParameters();
        for (; index < parameters.length; index++) {
            if (parameters[index] == parameter) {
                break;
            }
        }
        // If the name is not the default "argX", use it directly.
        if (!name.equals("arg" + index)) {
            return name;
        } else {
            return ParamNameResolver.GENERIC_NAME_PREFIX + (index + 1);
        }
    }

    /**
     * Checks if two objects are equal.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
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
     * Computes the hash code of the object.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, target);
    }

}
