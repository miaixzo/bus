/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.beans.copier;

import org.aoju.bus.core.beans.PropertyDesc;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.toolkit.BeanKit;
import org.aoju.bus.core.toolkit.TypeKit;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Bean属性拷贝到Map中的拷贝器
 *
 * @author Kimi Liu
 * @version 6.5.0
 * @since Java 17+
 */
public class BeanToMapCopier extends AbstractCopier<Object, Map> {

    /**
     * 目标的Map类型（用于泛型类注入）
     */
    private final Type targetType;

    /**
     * 构造
     *
     * @param source      来源Map
     * @param target      目标Bean对象
     * @param targetType  目标泛型类型
     * @param copyOptions 拷贝选项
     */
    public BeanToMapCopier(Object source, Map target, Type targetType, CopyOptions copyOptions) {
        super(source, target, copyOptions);
        this.targetType = targetType;
    }

    @Override
    public Map copy() {
        Class<?> actualEditable = source.getClass();
        if (null != copyOptions.editable) {
            // 检查限制类是否为target的父类或接口
            Assert.isTrue(copyOptions.editable.isInstance(source),
                    "Source class [{}] not assignable to Editable class [{}]", actualEditable.getName(), copyOptions.editable.getName());
            actualEditable = copyOptions.editable;
        }

        final Map<String, PropertyDesc> sourcePropertyDescMap = BeanKit.getBeanDesc(actualEditable).getPropMap(copyOptions.ignoreCase);
        sourcePropertyDescMap.forEach((sFieldName, sDesc) -> {
            if (null == sFieldName || false == sDesc.isReadable(copyOptions.transientSupport)) {
                // 字段空或不可读，跳过
                return;
            }

            sFieldName = copyOptions.editFieldName(sFieldName);
            // 对key做转换，转换后为null的跳过
            if (null == sFieldName) {
                return;
            }

            // 检查源对象属性是否过滤属性
            Object sValue = sDesc.getValue(this.source);
            if (false == copyOptions.testPropertyFilter(sDesc.getField(), sValue)) {
                return;
            }

            // 获取目标值真实类型并转换源值
            final Type[] typeArguments = TypeKit.getTypeArguments(this.targetType);
            if (null != typeArguments) {
                sValue = this.copyOptions.convertField(typeArguments[1], sValue);
                sValue = copyOptions.editFieldValue(sFieldName, sValue);
            }

            // 目标赋值
            if (null != sValue || false == copyOptions.ignoreNullValue) {
                target.put(sFieldName, sValue);
            }
        });
        return this.target;
    }

}
