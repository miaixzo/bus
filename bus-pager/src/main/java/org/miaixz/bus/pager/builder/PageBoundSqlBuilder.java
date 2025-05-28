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
package org.miaixz.bus.pager.builder;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.Builder;

/**
 * BoundSql 拦截器链配置器，负责初始化和管理 SQL 绑定处理器链
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageBoundSqlBuilder {

    /**
     * BoundSql 处理器链
     */
    private BoundSqlBuilder.Chain chain;

    /**
     * 配置 BoundSql 拦截器链，根据属性初始化处理器列表
     *
     * @param properties 配置属性
     */
    public void setProperties(Properties properties) {
        // 初始化 boundSqlInterceptorChain
        String boundSqlInterceptors = properties.getProperty("boundSqlInterceptors");
        if (StringKit.isEmpty(boundSqlInterceptors)) {
            return;
        }

        List<BoundSqlBuilder> handlers = Arrays.stream(boundSqlInterceptors.split("[;|,]"))
                .map(className -> (BoundSqlBuilder) Builder.newInstance(className.trim(), properties)).toList();

        if (!handlers.isEmpty()) {
            chain = new BoundSqlChainBuilder(null, handlers);
        }
    }

    /**
     * 获取 BoundSql 处理器链
     *
     * @return 处理器链实例
     */
    public BoundSqlBuilder.Chain getChain() {
        return chain;
    }

}
