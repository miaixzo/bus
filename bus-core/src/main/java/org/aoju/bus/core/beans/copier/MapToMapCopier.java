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

import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.toolkit.TypeKit;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Map属性拷贝到Map中的拷贝器
 *
 * @author Kimi Liu
 * @version 6.5.0
 * @since Java 17+
 */
public class MapToMapCopier extends AbstractCopier<Map, Map> {

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
    public MapToMapCopier(Map source, Map target, Type targetType, CopyOptions copyOptions) {
        super(source, target, copyOptions);
        this.targetType = targetType;
    }

    @Override
    public Map copy() {
        this.source.forEach((sKey, sValue) -> {
            if (null == sKey) {
                return;
            }
            final String sKeyStr = copyOptions.editFieldName(sKey.toString());
            // 对key做转换，转换后为null的跳过
            if (null == sKeyStr) {
                return;
            }

            final Object targetValue = target.get(sKeyStr);
            // 非覆盖模式下，如果目标值存在，则跳过
            if (false == copyOptions.override && null != targetValue) {
                return;
            }

            // 获取目标值真实类型并转换源值
            final Type[] typeArguments = TypeKit.getTypeArguments(this.targetType);
            if (null != typeArguments) {
                sValue = Convert.convertWithCheck(typeArguments[1], sValue, null, this.copyOptions.ignoreError);
                sValue = copyOptions.editFieldValue(sKeyStr, sValue);
            }

            // 目标赋值
            target.put(sKeyStr, sValue);
        });
        return this.target;
    }

}
