/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.validate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.validate.annotation.*;
import org.aoju.bus.validate.validators.Checker;
import org.aoju.bus.validate.validators.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 被校验对象
 * <p>
 * 注意: 当被校验对象为null时,无法获取到对象的Class,所以不会执行对象的Class上标记的任何校验注解
 * </P>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Validated extends Provider {

    /**
     * 校验者信息
     */
    private List<Property> list;
    /**
     * 被校验属性值
     */
    private Object object;
    /**
     * 被校验属性名称
     */
    private String field;
    /**
     * 校验者上下文
     */
    private Context context;

    /**
     * 被校验对象
     * <p>
     * 内部使用一个默认的校验器上下文
     * </P>
     *
     * @param object 被校验的原始对象
     */
    public Validated(Object object) {
        this.object = object;
        this.context = resolve(Context.newInstance(), new Annotation[0]);
        this.list = new ArrayList<>();
    }

    /**
     * 被校验对象
     *
     * @param object        被校验的原始对象
     * @param parentContext 父级校验上下文,当前校验环境会继承所有父级上下文信息,除了是否校验对象内部的属性
     */
    public Validated(Object object, Context parentContext) {
        this.object = object;
        this.context = resolve(parentContext, new Annotation[0]);
        this.list = new ArrayList<>();
    }

    /**
     * 被校验对象
     * <p>
     * 内部使用一个默认的校验器上下文
     * </P>
     *
     * @param object      被校验的原始对象
     * @param annotations 被校验对象上的所有注解
     */
    public Validated(Object object, Annotation[] annotations) {
        this.object = object;
        this.context = resolve(Context.newInstance(), annotations);
        this.list = resolve(annotations);
    }

    /**
     * 被校验对象
     *
     * @param object      被校验的原始对象
     * @param annotations 被校验对象上的所有注解
     * @param context     父级校验上下文,当前校验环境会继承所有父级上下文信息,除了是否校验对象内部的属性
     */
    public Validated(Object object, Annotation[] annotations, Context context) {
        this.object = object;
        this.context = resolve(context, annotations);
        this.list = resolve(annotations);
    }

    /**
     * 被校验对象
     *
     * @param object      被校验的原始对象
     * @param annotations 被校验对象上的所有注解
     * @param context     父级校验上下文,当前校验环境会继承所有父级上下文信息,除了是否校验对象内部的属性
     * @param field       属性信息
     */
    public Validated(Object object, Annotation[] annotations, Context context, String field) {
        this.field = field;
        this.object = object;
        this.context = resolve(context, annotations);
        this.list = resolve(annotations);
    }

    /**
     * 根据对象注解解析校验器
     *
     * @param annotations 注解信息
     * @return the object
     */
    private List<Property> resolve(Annotation[] annotations) {
        List<Property> list = new ArrayList<>();
        for (Annotation annotation : annotations) {
            if (isAnnotation(annotation)) {
                Property property = build(annotation, this.object);
                list.add(property);
            }
        }
        if (ObjectKit.isNotEmpty(this.object)) {
            Class<?> clazz = this.object.getClass();
            List<Annotation> clazzAnnotations = getAnnotation(clazz);
            for (Annotation annotation : clazzAnnotations) {
                Property property = build(annotation, this.object);
                list.add(property);
            }
        }
        return list;
    }

    /**
     * 根据对象注解配置校验上下文
     *
     * @param context     上下文
     * @param annotations 注解信息
     * @return the object
     */
    private Context resolve(Context context, Annotation[] annotations) {
        if (ObjectKit.isNotEmpty(this.object)) {
            Class<?> clazz = this.object.getClass();
            Inside inside = clazz.getAnnotation(Inside.class);
            if (ObjectKit.isNotEmpty(inside)) {
                context.setInside(true);
            }
        }
        for (Annotation annotation : annotations) {
            if (annotation instanceof Valid) {
                context.setInside(((Valid) annotation).inside());
                context.setField(((Valid) annotation).value());
                context.setSkip(((Valid) annotation).skip());
            } else if (annotation instanceof Group) {
                context.addGroups(((Group) annotation).value());
            } else if (annotation instanceof ValidEx) {
                context.setException(((ValidEx) annotation).value());
            } else if (annotation instanceof Inside) {
                context.setInside(true);
            }
        }
        return context;
    }

    /**
     * 执行校验
     * 如果校验环境设置了快速失败的属性为true,那么一旦出现校验失败,则会抛出异常
     *
     * @return 校验结果收集器
     */
    public Collector access() {
        Collector collector = new Collector(this);
        Checker checker = context.getChecker();
        for (Property p : this.list) {
            Collector result = checker.object(this, p);
            collector.collect(result);
        }
        if (context.isInside()) {
            Collector result = checker.inside(this);
            collector.collect(result);
        }
        return collector;
    }

    /**
     * 创建校验器属性对象
     *
     * @param annotation 注解
     * @param object     对象
     * @return 校验器属性对象
     */
    public Property build(Annotation annotation, Object object) {
        Assert.isTrue(isAnnotation(annotation), "尝试从非校验注解上获取信息:" + annotation);
        Class<? extends Annotation> annotationType = annotation.annotationType();
        try {
            String[] groups = (String[]) annotationType.getMethod(Builder.GROUP).invoke(annotation);
            String errmsg = (String) annotationType.getMethod(Builder.ERRMSG).invoke(annotation);
            String errcode = (String) annotationType.getMethod(Builder.ERRCODE).invoke(annotation);
            String name = (String) annotationType.getMethod(Builder.FIELD).invoke(annotation);
            this.field = Builder.DEFAULT_FIELD.equals(name) ? this.field : name;
            Property property = new Property();
            property.setAnnotation(annotation);
            property.setErrmsg(errmsg);
            property.setGroup(groups);
            property.setField(this.field);
            property.setErrcode(errcode);
            property.addParam(Builder.FIELD, this.field);

            if (ObjectKit.isNotEmpty(object) && object.getClass().isArray()) {
                property.addParam(Builder.VAL, Arrays.toString((Object[]) object));
            } else {
                property.addParam(Builder.VAL, String.valueOf(object));
            }

            Method[] declaredMethods = annotationType.getDeclaredMethods();
            for (Method m : declaredMethods) {
                Filler filler = m.getAnnotation(Filler.class);
                if (ObjectKit.isNotEmpty(filler)) {
                    Class<?> returnType = m.getReturnType();
                    Object invoke = m.invoke(annotation);
                    if (returnType.isArray()) {
                        property.addParam(filler.value(), Arrays.toString((Object[]) invoke));
                    } else {
                        property.addParam(filler.value(), invoke);
                    }
                }
            }
            Annotation[] parentAnnos = annotationType.getAnnotations();
            for (Annotation anno : parentAnnos) {
                if (isAnnotation(anno)) {
                    property.addParentProperty(build(anno, object));
                } else if (anno instanceof Array) {
                    property.setArray(true);
                } else if (anno instanceof Complex) {
                    property.setClazz(((Complex) anno).clazz());
                    property.setName(((Complex) anno).value());
                } else if (anno instanceof ValidEx) {
                    property.setException(((ValidEx) anno).value());
                }
            }
            if (ObjectKit.isEmpty(property.getClazz()) || StringKit.isEmpty(property.getName())) {
                throw new InternalException("非法的校验注解,没有使用Complex元注解表示校验器:" + annotationType.getName());
            }
            return property;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new InternalException("非法的校验注解,没有定义通用的校验属性:" + annotationType.getName(), e);
        }
    }

}
