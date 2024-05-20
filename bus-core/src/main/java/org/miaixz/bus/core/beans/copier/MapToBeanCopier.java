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
package org.miaixz.bus.core.beans.copier;

import org.miaixz.bus.core.beans.PropDesc;
import org.miaixz.bus.core.center.map.CaseInsensitiveMap;
import org.miaixz.bus.core.center.map.MapWrapper;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.toolkit.BeanKit;
import org.miaixz.bus.core.toolkit.TypeKit;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Map属性拷贝到Bean中的拷贝器
 *
 * @param <T> 目标Bean类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapToBeanCopier<T> extends AbstractCopier<Map<?, ?>, T> {

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
    public MapToBeanCopier(final Map<?, ?> source, final T target, final Type targetType, final CopyOptions copyOptions) {
        super(source, target, copyOptions);
        // 针对MapWrapper特殊处理，提供的Map包装了忽略大小写的Map，则默认转Bean的时候也忽略大小写，如JSONObject
        if (source instanceof MapWrapper) {
            final Map<?, ?> raw = ((MapWrapper<?, ?>) source).getRaw();
            if (raw instanceof CaseInsensitiveMap) {
                copyOptions.setIgnoreCase(true);
            }
        }

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

        this.source.forEach((sKey, sValue) -> {
            if (null == sKey) {
                return;
            }

            // 编辑键值对
            final MutableEntry<String, Object> entry = copyOptions.editField(sKey.toString(), sValue);
            if (null == entry) {
                return;
            }
            final String sFieldName = entry.getKey();
            // 对key做转换，转换后为null的跳过
            if (null == sFieldName) {
                return;
            }

            // 检查目标字段可写性
            // 目标字段检查放在键值对编辑之后，因为键可能被编辑修改
            final PropDesc tDesc = this.copyOptions.findPropDesc(targetPropDescMap, sFieldName);
            if (null == tDesc || !tDesc.isWritable(this.copyOptions.transientSupport)) {
                // 字段不可写，跳过之
                return;
            }

            Object newValue = entry.getValue();
            // 检查目标是否过滤属性
            if (!copyOptions.testPropertyFilter(tDesc.getField(), newValue)) {
                return;
            }

            // 获取目标字段真实类型并转换源值
            final Type fieldType = TypeKit.getActualType(this.targetType, tDesc.getFieldType());
            newValue = this.copyOptions.convertField(fieldType, newValue);

            // 目标赋值
            tDesc.setValue(this.target, newValue, copyOptions.ignoreNullValue, copyOptions.ignoreError, copyOptions.override);
        });
        return this.target;
    }

}
