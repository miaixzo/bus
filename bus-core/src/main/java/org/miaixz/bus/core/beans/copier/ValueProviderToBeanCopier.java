/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.beans.copier;

import org.miaixz.bus.core.beans.PropDesc;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.TypeKit;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * {@link ValueProvider}属性拷贝到Bean中的拷贝器
 *
 * @param <T> 目标Bean类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class ValueProviderToBeanCopier<T> extends AbstractCopier<ValueProvider<String>, T> {

    /**
     * 目标的类型（用于泛型类注入）
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
    public ValueProviderToBeanCopier(final ValueProvider<String> source, final T target, final Type targetType, final CopyOptions copyOptions) {
        super(source, target, copyOptions);
        this.targetType = targetType;
    }

    @Override
    public T copy() {
        Class<?> actualEditable = target.getClass();
        if (null != copyOptions.editable) {
            // 检查限制类是否为target的父类或接口
            Assert.isTrue(copyOptions.editable.isInstance(target),
                    "Target class [{}] not assignable to Editable class [{}]", actualEditable.getName(), copyOptions.editable.getName());
            actualEditable = copyOptions.editable;
        }
        final Map<String, PropDesc> targetPropDescMap = BeanKit.getBeanDesc(actualEditable).getPropMap(copyOptions.ignoreCase);

        targetPropDescMap.forEach((tFieldName, tDesc) -> {
            if (null == tFieldName) {
                return;
            }

            // 检查目标字段可写性
            if (null == tDesc || !tDesc.isWritable(this.copyOptions.transientSupport)) {
                // 字段不可写，跳过之
                return;
            }

            // 获取目标字段真实类型
            final Type fieldType = TypeKit.getActualType(this.targetType, tDesc.getFieldType());
            // 编辑键值对
            final MutableEntry<String, Object> entry = copyOptions.editField(tFieldName, null);
            if (null == entry) {
                return;
            }
            tFieldName = entry.getKey();
            // 对key做转换，转换后为null的跳过
            if (null == tFieldName) {
                return;
            }
            // 无字段内容跳过
            if (!source.containsKey(tFieldName)) {
                return;
            }
            final Object sValue = source.value(tFieldName, fieldType);

            // 检查目标对象属性是否过滤属性
            if (!copyOptions.testPropertyFilter(tDesc.getField(), sValue)) {
                return;
            }

            // 目标赋值
            tDesc.setValue(this.target, sValue, copyOptions.ignoreNullValue, copyOptions.ignoreError, copyOptions.override);
        });
        return this.target;
    }

}
