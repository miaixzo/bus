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
package org.miaixz.bus.mapper.support.keysql;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 封装 SqlSource 以实现在插入前生成主键。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GenIdSqlSource implements SqlSource {

    /**
     * 原始 SQL 源
     */
    private final SqlSource sqlSource;

    /**
     * 主键生成器
     */
    private final GenIdKeyGenerator keyGenerator;

    /**
     * 构造函数，初始化 SQL 源和主键生成器。
     *
     * @param sqlSource    原始 SQL 源
     * @param keyGenerator 主键生成器
     */
    public GenIdSqlSource(SqlSource sqlSource, GenIdKeyGenerator keyGenerator) {
        this.sqlSource = sqlSource;
        this.keyGenerator = keyGenerator;
    }

    /**
     * 获取绑定 SQL，并在必要时提前生成主键。
     *
     * @param parameterObject 参数对象
     * @return 绑定后的 SQL 对象
     */
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // 确保初始化时不会漏掉首次主键生成
        keyGenerator.prepare(parameterObject);
        return sqlSource.getBoundSql(parameterObject);
    }

}