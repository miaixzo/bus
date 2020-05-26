/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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

import org.aoju.bus.core.beans.BeanDesc;
import org.aoju.bus.core.beans.copier.provider.BeanValueProvider;
import org.aoju.bus.core.beans.copier.provider.MapValueProvider;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.lang.Typed;
import org.aoju.bus.core.lang.copier.Copier;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.*;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * Bean拷贝
 *
 * @param <T> 目标对象类型
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
public class BeanCopier<T> implements Copier<T>, Serializable {

    /**
     * 源对象
     */
    private final Object source;
    /**
     * 目标对象
     */
    private final T dest;
    /**
     * 目标的类型(用于泛型类注入)
     */
    private final Type destType;
    /**
     * 拷贝选项
     */
    private final CopyOptions copyOptions;

    /**
     * 构造
     *
     * @param source      来源对象，可以是Bean或者Map
     * @param dest        目标Bean对象
     * @param destType    目标的泛型类型，用于标注有泛型参数的Bean对象
     * @param copyOptions 拷贝属性选项
     */
    public BeanCopier(Object source, T dest, Type destType, CopyOptions copyOptions) {
        this.source = source;
        this.dest = dest;
        this.destType = destType;
        this.copyOptions = copyOptions;
    }

    /**
     * 创建BeanCopier
     *
     * @param <T>         目标Bean类型
     * @param source      来源对象，可以是Bean或者Map
     * @param dest        目标Bean对象
     * @param copyOptions 拷贝属性选项
     * @return BeanCopier
     */
    public static <T> BeanCopier<T> create(Object source, T dest, CopyOptions copyOptions) {
        return create(source, dest, dest.getClass(), copyOptions);
    }

    /**
     * 创建BeanCopier
     *
     * @param <T>         目标Bean类型
     * @param source      来源对象,可以是Bean或者Map
     * @param dest        目标Bean对象
     * @param destType    目标的泛型类型,用于标注有泛型参数的Bean对象
     * @param copyOptions 拷贝属性选项
     * @return BeanCopier
     */
    public static <T> BeanCopier<T> create(Object source, T dest, Type destType, CopyOptions copyOptions) {
        return new BeanCopier<>(source, dest, destType, copyOptions);
    }

    /**
     * 获取指定字段名对应的映射值
     *
     * @param mapping   反向映射Map
     * @param fieldName 字段名
     * @return 映射值，无对应值返回字段名
     */
    private static String mappingKey(Map<String, String> mapping, String fieldName) {
        if (MapKit.isEmpty(mapping)) {
            return fieldName;
        }
        return ObjectKit.defaultIfNull(mapping.get(fieldName), fieldName);
    }

    @Override
    public T copy() {
        if (null != this.source) {
            if (this.source instanceof ValueProvider) {
                // 目标只支持Bean
                valueProviderToBean((ValueProvider<String>) this.source, this.dest);
            } else if (this.source instanceof Map) {
                if (this.dest instanceof Map) {
                    mapToMap((Map<?, ?>) this.source, (Map<?, ?>) this.dest);
                } else {
                    mapToBean((Map<?, ?>) this.source, this.dest);
                }
            } else {
                if (this.dest instanceof Map) {
                    beanToMap(this.source, (Map<?, ?>) this.dest);
                } else {
                    beanToBean(this.source, this.dest);
                }
            }
        }
        return this.dest;
    }

    /**
     * Bean和Bean之间属性拷贝
     *
     * @param providerBean 来源Bean
     * @param destBean     目标Bean
     */
    private void beanToBean(Object providerBean, Object destBean) {
        valueProviderToBean(new BeanValueProvider(providerBean, this.copyOptions.ignoreCase, this.copyOptions.ignoreError), destBean);
    }

    /**
     * Map转Bean属性拷贝
     *
     * @param map  Map
     * @param bean Bean
     */
    private void mapToBean(Map<?, ?> map, Object bean) {
        valueProviderToBean(
                new MapValueProvider(map, this.copyOptions.ignoreCase, this.copyOptions.ignoreError),
                bean
        );
    }

    /**
     * Map转Map
     *
     * @param source 源Map
     * @param dest   目标Map
     */
    private void mapToMap(Map source, Map dest) {
        if (null != dest && null != source) {
            dest.putAll(source);
        }
    }

    /**
     * 对象转Map
     *
     * @param bean      bean对象
     * @param targetMap 目标的Map
     */
    private void beanToMap(Object bean, Map targetMap) {
        final Collection<BeanDesc.PropDesc> props = BeanKit.getBeanDesc(bean.getClass()).getProps();
        final HashSet<String> ignoreSet = (null != copyOptions.ignoreProperties) ? CollKit.newHashSet(copyOptions.ignoreProperties) : null;
        final CopyOptions copyOptions = this.copyOptions;

        String key;
        Method getter;
        Object value;
        for (BeanDesc.PropDesc prop : props) {
            key = prop.getFieldName();
            // 过滤class属性
            // 得到property对应的getter方法
            getter = prop.getGetter();
            if (null != getter) {
                // 只读取有getter方法的属性
                try {
                    value = getter.invoke(bean);
                } catch (Exception e) {
                    if (copyOptions.ignoreError) {
                        continue;// 忽略反射失败
                    } else {
                        throw new InstrumentException("Get value of [{}] error!", prop.getFieldName());
                    }
                }
                if (CollKit.contains(ignoreSet, key)) {
                    // 目标属性值被忽略或值提供者无此key时跳过
                    continue;
                }
                if (null == value && copyOptions.ignoreNullValue) {
                    continue;// 当允许跳过空时,跳过
                }
                if (bean.equals(value)) {
                    continue;// 值不能为bean本身,防止循环引用
                }
                targetMap.put(mappingKey(copyOptions.fieldMapping, key), value);
            }
        }
    }

    /**
     * 值提供器转Bean
     *
     * @param valueProvider 值提供器
     * @param bean          Bean
     */
    private void valueProviderToBean(ValueProvider<String> valueProvider, Object bean) {
        if (null == valueProvider) {
            return;
        }

        final CopyOptions copyOptions = this.copyOptions;
        Class<?> actualEditable = bean.getClass();
        if (null != copyOptions.editable) {
            // 检查限制类是否为target的父类或接口
            if (false == copyOptions.editable.isInstance(bean)) {
                throw new IllegalArgumentException(StringKit.format("Target class [{}] not assignable to Editable class [{}]", bean.getClass().getName(), copyOptions.editable.getName()));
            }
            actualEditable = copyOptions.editable;
        }
        final HashSet<String> ignoreSet = (null != copyOptions.ignoreProperties) ? CollKit.newHashSet(copyOptions.ignoreProperties) : null;
        final Map<String, String> fieldReverseMapping = copyOptions.getReversedMapping();

        final Collection<BeanDesc.PropDesc> props = BeanKit.getBeanDesc(actualEditable).getProps();
        Field field;
        String fieldName;
        Object value;
        Method setterMethod;
        Class<?> propClass;
        for (BeanDesc.PropDesc prop : props) {
            // 获取值
            field = prop.getField();
            fieldName = prop.getFieldName();
            if (CollKit.contains(ignoreSet, fieldName)) {
                // 目标属性值被忽略或值提供者无此key时跳过
                continue;
            }
            final String providerKey = mappingKey(fieldReverseMapping, fieldName);
            if (false == valueProvider.containsKey(providerKey)) {
                // 无对应值可提供
                continue;
            }
            setterMethod = prop.getSetter();
            if (null == setterMethod && false == BeanKit.isPublic(field)) {
                // Setter方法不存在或者字段为非public跳过
                //5.1.0新增支持public字段注入支持
                continue;
            }

            Type valueType = (null == setterMethod) ? TypeKit.getType(field) : TypeKit.getFirstParamType(setterMethod);
            if (valueType instanceof ParameterizedType) {
                // 参数为泛型参数类型，解析对应泛型类型为真实类型
                ParameterizedType tmp = (ParameterizedType) valueType;
                Type[] actualTypeArguments = tmp.getActualTypeArguments();
                if (TypeKit.hasTypeVeriable(actualTypeArguments)) {
                    // 泛型对象中含有未被转换的泛型变量
                    actualTypeArguments = TypeKit.getActualTypes(this.destType, field.getDeclaringClass(), tmp.getActualTypeArguments());
                    if (ArrayKit.isNotEmpty(actualTypeArguments)) {
                        // 替换泛型变量为实际类型
                        valueType = new Typed(actualTypeArguments, tmp.getOwnerType(), tmp.getRawType());
                    }
                }
            } else if (valueType instanceof TypeVariable) {
                // 参数为泛型，查找其真实类型(适用于泛型方法定义于泛型父类)
                valueType = TypeKit.getActualType(this.destType, field.getDeclaringClass(), valueType);
            }

            value = valueProvider.value(providerKey, valueType);
            if (null == value && copyOptions.ignoreNullValue) {
                continue;// 当允许跳过空时，跳过
            }
            if (bean == value) {
                continue;// 值不能为bean本身，防止循环引用
            }

            try {
                // valueProvider在没有对值做转换且当类型不匹配的时候，执行默认转换
                propClass = prop.getFieldClass();
                if (false == propClass.isInstance(value)) {
                    value = Convert.convert(propClass, value);
                    if (null == value && copyOptions.ignoreNullValue) {
                        continue;// 当允许跳过空时，跳过
                    }
                }

                if (null == setterMethod) {
                    // 直接注入值
                    ReflectKit.setFieldValue(bean, field, value);
                } else {
                    // 执行set方法注入值
                    ReflectKit.invoke(bean, setterMethod, value);
                }
            } catch (Exception e) {
                if (false == copyOptions.ignoreError) {
                    throw new InstrumentException("Inject [{}] error!", prop.getFieldName());
                }
                // 忽略注入失败
            }
        }
    }

}
