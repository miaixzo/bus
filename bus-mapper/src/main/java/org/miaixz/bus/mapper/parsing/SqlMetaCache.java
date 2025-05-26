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
package org.miaixz.bus.mapper.parsing;

import java.util.function.Supplier;

import org.apache.ibatis.builder.annotation.ProviderContext;

/**
 * SQL 缓存类，用于延迟生成 SQL 脚本
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlMetaCache {

    /**
     * 空对象实例
     */
    public static final SqlMetaCache NULL = new SqlMetaCache(null, null, null);

    /**
     * 执行方法上下文
     */
    private final ProviderContext providerContext;

    /**
     * 实体类信息
     */
    private final TableMeta tableMeta;

    /**
     * SQL 提供者
     */
    private final Supplier<String> sqlScriptSupplier;

    /**
     * 构造函数，初始化 SQL 缓存
     *
     * @param providerContext   执行方法上下文
     * @param tableMeta         实体类信息
     * @param sqlScriptSupplier SQL 脚本提供者
     */
    public SqlMetaCache(ProviderContext providerContext, TableMeta tableMeta, Supplier<String> sqlScriptSupplier) {
        this.providerContext = providerContext;
        this.tableMeta = tableMeta;
        this.sqlScriptSupplier = sqlScriptSupplier;
    }

    /**
     * 获取 SQL 脚本，延迟到最终生成 SqlSource 时执行
     *
     * @return SQL 脚本
     */
    public String getSqlScript() {
        return sqlScriptSupplier.get();
    }

    /**
     * 获取执行方法上下文
     *
     * @return 执行方法上下文
     */
    public ProviderContext getProviderContext() {
        return providerContext;
    }

    /**
     * 获取实体类信息
     *
     * @return 实体类信息
     */
    public TableMeta getTableMeta() {
        return tableMeta;
    }

}