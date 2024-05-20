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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.NoSuchException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.validate.*;
import org.miaixz.bus.validate.magic.annotation.Inside;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 校验检查器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Checker {

    /**
     * 根据指定的校验器校验对象
     *
     * @param verified 被校验对象
     * @param property 校验器属性
     * @return 校验结果
     * @throws ValidateException 如果校验环境的fast设置为true, 则校验失败时立刻抛出该异常
     */
    public Collector object(Verified verified, Property property)
            throws ValidateException {
        Collector collector = new Collector(verified);
        Context context = verified.getContext();

        if (Provider.isGroup(property.getGroup(), context.getGroup())) {
            collector.collect(doObject(verified, property));
        }
        List<Property> list = property.getList();
        for (Property p : list) {
            collector.collect(doObject(verified, p));
        }
        return collector;
    }

    /**
     * 校验对象内部的所有字段
     *
     * @param verified 被校验对象
     * @return 校验结果
     */
    public Collector inside(Verified verified) {
        Collector collector = new Collector(verified);
        try {
            Object object = verified.getObject();
            if (ObjectKit.isNotEmpty(object)) {
                Field[] fields = FieldKit.getFields(object.getClass());
                for (Field field : fields) {
                    Object value = FieldKit.getFieldValue(object, field);
                    Annotation[] annotations = field.getDeclaredAnnotations();

                    String[] xFields = verified.getContext().getField();
                    String[] xSkip = null == verified.getContext().getSkip() ? null : verified.getContext().getSkip();

                    // 过滤当前需跳过的属性
                    if (ArrayKit.isNotEmpty(xSkip)
                            && Arrays.asList(xSkip).contains(field.getName())) {
                        continue;
                    }
                    // 过滤当前需要校验的属性
                    if (ArrayKit.isNotEmpty(xFields)
                            && !Arrays.asList(xFields).contains(field.getName())) {
                        continue;
                    }
                    // 属性校验开始
                    verified.getContext().setInside(false);
                    verified = new Verified(value, annotations, verified.getContext(), field.getName());

                    if (null != value && Provider.isCollection(value)
                            && hasInside(annotations)) {
                        collector.collect(doCollectionInside(verified));
                    } else if (null != value && Provider.isArray(value)
                            && hasInside(annotations)) {
                        collector.collect(doArrayInside(verified));
                    }
                    if (verified.getList().isEmpty()) {
                        continue;
                    }
                    collector.collect(verified.access());
                }
            } else {
                Logger.debug("当前被校验的对象为null, 忽略校验对象内部字段: {}", verified);
            }
        } catch (InternalException e) {
            throw new InternalException("无法校验指定字段", e);
        }
        return collector;
    }

    /**
     * 根据校验器属性校验对象
     *
     * @param verified 被校验的对象
     * @param property 校验器属性
     * @return 校验结果
     */
    private Collector doObject(Verified verified, Property property) {
        Matcher matcher = (Matcher) Registry.getInstance().require(property.getName(), property.getClazz());
        if (ObjectKit.isEmpty(matcher)) {
            throw new NoSuchException(String.format("无法找到指定的校验器, name:%s, class:%s",
                    property.getName(),
                    null == property.getClazz() ? Normal.NULL : property.getClazz().getName()));
        }
        Object validatedTarget = verified.getObject();
        if (ObjectKit.isNotEmpty(validatedTarget) && property.isArray() && Provider.isArray(validatedTarget)) {
            return doArrayObject(verified, property);
        } else if (ObjectKit.isNotEmpty(validatedTarget) && property.isArray() && Provider.isCollection(validatedTarget)) {
            return doCollection(verified, property);
        } else {
            boolean result = matcher.on(validatedTarget, property.getAnnotation(), verified.getContext());
            if (!result && verified.getContext().isFast()) {
                throw Provider.resolve(property, verified.getContext());
            }
            return new Collector(verified, property, result);
        }
    }

    /**
     * 校验集合对象元素
     *
     * @param verified 被校验对象
     * @param property 校验器属性
     * @return 校验结果
     */
    private Collector doCollection(Verified verified, Property property) {
        Collector collector = new Collector(verified);
        Collection<?> collection = (Collection<?>) verified.getObject();
        for (Object item : collection) {
            Verified itemTarget = new Verified(item, new Annotation[]{property.getAnnotation()},
                    verified.getContext());
            Collector checked = itemTarget.access();
            collector.collect(checked);
        }

        return collector;
    }

    /**
     * 校验数组对象元素
     *
     * @param verified 被校验对象
     * @param property 校验器属性
     * @return 校验结果
     */
    private Collector doArrayObject(Verified verified, Property property) {
        Collector collector = new Collector(verified);
        Object[] array = (Object[]) verified.getObject();
        for (int i = 0; i < array.length; i++) {
            Verified itemTarget = new Verified(array[i],
                    new Annotation[]{property.getAnnotation()}, verified.getContext());
            Collector checked = itemTarget.access();
            collector.collect(checked);
        }
        return collector;
    }

    /**
     * 校验数组对象元素
     *
     * @param verified 被校验对象
     * @return 校验结果
     */
    private Collector doArrayInside(Verified verified) {
        Collector collector = new Collector(verified);
        Object[] array = (Object[]) verified.getObject();
        for (Object object : array) {
            collector.collect(inside(new Verified(object, verified.getContext())));
        }
        return collector;
    }

    /**
     * 校验集合对象元素
     *
     * @param verified 被校验对象
     * @return 校验结果
     */
    private Collector doCollectionInside(Verified verified) {
        Collector collector = new Collector(verified);
        Collection<?> collection = (Collection<?>) verified.getObject();
        for (Object item : collection) {
            collector.collect(inside(new Verified(item, verified.getContext())));
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
