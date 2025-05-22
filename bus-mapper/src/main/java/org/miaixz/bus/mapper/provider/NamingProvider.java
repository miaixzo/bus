/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.mapper.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Context;
import org.miaixz.bus.mapper.parsing.FieldMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * 提供命名样式的接口，支持通过 SPI 扩展自定义命名规则。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface NamingProvider extends Provider {

    /**
     * 存储命名样式与实现类的映射。
     */
    Map<String, NamingProvider> styleMap = new HashMap() {
        {
            List<NamingProvider> instances = NormalSpiLoader.loadList(false, NamingProvider.class);
            for (NamingProvider instance : instances) {
                put(instance.type(), instance);
            }
        }
    };

    /**
     * 获取默认命名样式处理实例。
     *
     * @return 默认命名样式实现
     */
    static NamingProvider getDefaultStyle() {
        return type(null);
    }

    /**
     * 根据样式名称获取命名样式处理实例。
     *
     * @param style 样式名称，若为空则使用全局配置或默认样式
     * @return 命名样式实现
     * @throws IllegalArgumentException 如果样式名称无效
     */
    static NamingProvider type(String style) {
        if (style == null || style.isEmpty()) {
            style = Context.INSTANCE.getProperty(Args.NAMING_KEY, Args.CAMEL_UNDERLINE_LOWER_CASE);
        }
        if (style == null || style.isEmpty()) {
            style = Args.CAMEL_UNDERLINE_LOWER_CASE;
        }

        if (styleMap.containsKey(style)) {
            return styleMap.get(style);
        } else {
            throw new IllegalArgumentException("illegal style：" + style);
        }
    }

    /**
     * 转换实体类为表名。
     *
     * @param entityClass 实体类
     * @return 对应的表名
     */
    String tableName(Class<?> entityClass);

    /**
     * 转换字段为列名。
     *
     * @param entityTable 实体表信息
     * @param field       实体字段信息
     * @return 对应的列名
     */
    String columnName(TableMeta entityTable, FieldMeta field);

}