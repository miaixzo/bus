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
package org.miaixz.bus.core.bean.copier;

import java.lang.reflect.Type;
import java.util.Map;

import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * Map 到 Map 的属性拷贝器，将源 Map 的键值对复制到目标 Map。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapToMapCopier extends AbstractCopier<Map, Map> {

    /**
     * 目标 Map 的泛型参数类型
     */
    private final Type[] typeArguments;

    /**
     * 构造方法，初始化 Map 到 Map 的拷贝器。
     *
     * @param source      来源 Map
     * @param target      目标 Map 对象
     * @param targetType  目标泛型类型
     * @param copyOptions 拷贝选项，null 时使用默认配置
     */
    public MapToMapCopier(final Map source, final Map target, final Type targetType, final CopyOptions copyOptions) {
        super(source, target, copyOptions);
        this.typeArguments = TypeKit.getTypeArguments(targetType);
    }

    /**
     * 执行 Map 到 Map 的拷贝操作。
     *
     * @return 目标 Map
     */
    @Override
    public Map copy() {
        this.source.forEach((sKey, sValue) -> {
            if (null == sKey) {
                return;
            }

            // 编辑键值对
            final MutableEntry<Object, Object> entry = copyOptions.editField(sKey.toString(), sValue);
            if (null == entry) {
                return;
            }
            sKey = entry.getKey();
            // 键转换后为 null 则跳过
            if (null == sKey) {
                return;
            }
            sValue = entry.getValue();
            // 忽略空值
            if (copyOptions.ignoreNullValue && sValue == null) {
                return;
            }

            final Object targetValue = target.get(sKey);
            // 非覆盖模式下，目标值存在则跳过
            if (!copyOptions.override && null != targetValue) {
                return;
            }

            // 转换源值到目标值的类型
            if (null != typeArguments) {
                sValue = this.copyOptions.convertField(typeArguments[1], sValue);
            }

            // 目标赋值
            target.put(sKey, sValue);
        });
        return this.target;
    }

}