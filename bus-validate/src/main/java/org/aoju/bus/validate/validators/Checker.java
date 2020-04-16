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
package org.aoju.bus.validate.validators;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.lang.exception.NoSuchException;
import org.aoju.bus.core.lang.exception.ValidateException;
import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.validate.*;
import org.aoju.bus.validate.annotation.Inside;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 校验检查器
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class Checker {

    /**
     * 根据指定的校验器校验对象
     *
     * @param validated 被校验对象
     * @param property  校验器属性
     * @return 校验结果
     * @throws ValidateException 如果校验环境的fast设置为true, 则校验失败时立刻抛出该异常
     */
    public Collector object(Validated validated, Property property)
            throws ValidateException {
        Collector collector = new Collector(validated);
        Context context = validated.getContext();

        if (Provider.isGroup(property.getGroup(), context.getGroup())) {
            collector.collect(doObject(validated, property));
        }
        List<Property> list = property.getList();
        for (Property p : list) {
            collector.collect(doObject(validated, p));
        }
        return collector;
    }

    /**
     * 校验对象内部的所有字段
     *
     * @param validated 被校验对象
     * @return 校验结果
     */
    public Collector inside(Validated validated) {
        Collector collector = new Collector(validated);
        try {
            Object object = validated.getObject();
            if (ObjectUtils.isNotEmpty(object)) {
                Field[] fields = ClassUtils.getAllFields(object.getClass());
                for (Field field : fields) {
                    Object value = ClassUtils.readField(field, object, true);
                    Annotation[] annotations = field.getDeclaredAnnotations();

                    String[] xFields = validated.getContext().getField();
                    String[] xSkip = validated.getContext().getSkip() == null ? null : validated.getContext().getSkip();

                    // 过滤当前需跳过的属性
                    if (ArrayUtils.isNotEmpty(xSkip)
                            && Arrays.asList(xSkip).contains(field.getName())) {
                        continue;
                    }
                    // 过滤当前需要校验的属性
                    if (ArrayUtils.isNotEmpty(xFields)
                            && !Arrays.asList(xFields).contains(field.getName())) {
                        continue;
                    }
                    // 属性校验开始
                    validated.getContext().setInside(false);
                    validated = new Validated(value, annotations, validated.getContext(), field.getName());

                    if (value != null && Provider.isCollection(value)
                            && hasInside(annotations)) {
                        collector.collect(doCollectionInside(validated));
                    } else if (value != null && Provider.isArray(value)
                            && hasInside(annotations)) {
                        collector.collect(doArrayInside(validated));
                    }
                    if (validated.getList().isEmpty()) {
                        continue;
                    }
                    collector.collect(validated.access());
                }
            } else {
                Logger.debug("当前被校验的对象为null, 忽略校验对象内部字段: {}", validated);
            }
        } catch (IllegalAccessException e) {
            throw new InstrumentException("无法校验指定字段", e);
        }
        return collector;
    }

    /**
     * 根据校验器属性校验对象
     *
     * @param validated 被校验的对象
     * @param property  校验器属性
     * @return 校验结果
     */
    private Collector doObject(Validated validated, Property property) {
        Matcher matcher = (Matcher) Registry.getInstance().require(property.getName(), property.getClazz());
        if (ObjectUtils.isEmpty(matcher)) {
            throw new NoSuchException(String.format("无法找到指定的校验器, name:%s, class:%s",
                    property.getName(),
                    property.getClazz() == null ? Normal.NULL : property.getClazz().getName()));
        }
        Object validatedTarget = validated.getObject();
        if (ObjectUtils.isNotEmpty(validatedTarget) && property.isArray() && Provider.isArray(validatedTarget)) {
            return doArrayObject(validated, property);
        } else if (ObjectUtils.isNotEmpty(validatedTarget) && property.isArray() && Provider.isCollection(validatedTarget)) {
            return doCollection(validated, property);
        } else {
            boolean result = matcher.on(validatedTarget, property.getAnnotation(), validated.getContext());
            if (!result && validated.getContext().isFast()) {
                throw Provider.resolve(property, validated.getContext());
            }
            return new Collector(validated, property, result);
        }
    }

    /**
     * 校验集合对象元素
     *
     * @param validated 被校验对象
     * @param property  校验器属性
     * @return 校验结果
     */
    private Collector doCollection(Validated validated, Property property) {
        Collector collector = new Collector(validated);
        Collection<?> collection = (Collection<?>) validated.getObject();
        for (Object item : collection) {
            Validated itemTarget = new Validated(item, new Annotation[]{property.getAnnotation()},
                    validated.getContext());
            Collector checked = itemTarget.access();
            collector.collect(checked);
        }

        return collector;
    }

    /**
     * 校验数组对象元素
     *
     * @param validated 被校验对象
     * @param property  校验器属性
     * @return 校验结果
     */
    private Collector doArrayObject(Validated validated, Property property) {
        Collector collector = new Collector(validated);
        Object[] array = (Object[]) validated.getObject();
        for (int i = 0; i < array.length; i++) {
            Validated itemTarget = new Validated(array[i],
                    new Annotation[]{property.getAnnotation()}, validated.getContext());
            Collector checked = itemTarget.access();
            collector.collect(checked);
        }
        return collector;
    }

    /**
     * 校验数组对象元素
     *
     * @param validated 被校验对象
     * @return 校验结果
     */
    private Collector doArrayInside(Validated validated) {
        Collector collector = new Collector(validated);
        Object[] array = (Object[]) validated.getObject();
        for (Object object : array) {
            collector.collect(inside(new Validated(object, validated.getContext())));
        }
        return collector;
    }

    /**
     * 校验集合对象元素
     *
     * @param validated 被校验对象
     * @return 校验结果
     */
    private Collector doCollectionInside(Validated validated) {
        Collector collector = new Collector(validated);
        Collection<?> collection = (Collection<?>) validated.getObject();
        for (Object item : collection) {
            collector.collect(inside(new Validated(item, validated.getContext())));
        }
        return collector;
    }

    /**
     * 是否为内部校验注解
     *
     * @param annotations 注解
     * @return 校验结果
     */
    private boolean hasInside(Annotation[] annotations) {
        return Arrays.stream(annotations).anyMatch(an -> an instanceof Inside);
    }

}
