/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.pager.dialect.auto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.pager.AutoDialect;
import org.miaixz.bus.pager.dialect.AbstractAutoDialect;
import org.miaixz.bus.pager.dialect.AbstractPaging;

/**
 * 遍历所有实现，找到匹配的实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Defalut implements AutoDialect<String> {

    private static final List<AbstractAutoDialect> AUTO_DIALECTS = new ArrayList<>();

    static {
        // 创建时，初始化所有实现，当依赖的连接池不存在时，这里不会添加成功，所以理论上这里包含的内容不会多，执行时不会迭代多次
        try {
            AUTO_DIALECTS.add(new Hikari());
        } catch (Exception ignore) {
        }
        try {
            AUTO_DIALECTS.add(new Druid());
        } catch (Exception ignore) {
        }
    }

    private Map<String, AbstractAutoDialect> urlMap = new ConcurrentHashMap<>();

    /**
     * 允许手工添加额外的实现，实际上没有必要
     *
     * @param autoDialect 自动方言
     */
    public static void registerAutoDialect(AbstractAutoDialect autoDialect) {
        AUTO_DIALECTS.add(autoDialect);
    }

    @Override
    public String extractDialectKey(MappedStatement ms, DataSource dataSource, Properties properties) {
        for (AbstractAutoDialect autoDialect : AUTO_DIALECTS) {
            String dialectKey = autoDialect.extractDialectKey(ms, dataSource, properties);
            if (dialectKey != null) {
                if (!urlMap.containsKey(dialectKey)) {
                    urlMap.put(dialectKey, autoDialect);
                }
                return dialectKey;
            }
        }
        // 都不匹配的时候使用默认方式
        return Early.DEFAULT.extractDialectKey(ms, dataSource, properties);
    }

    @Override
    public AbstractPaging extractDialect(String dialectKey, MappedStatement ms, DataSource dataSource,
            Properties properties) {
        if (dialectKey != null && urlMap.containsKey(dialectKey)) {
            return urlMap.get(dialectKey).extractDialect(dialectKey, ms, dataSource, properties);
        }
        // 都不匹配的时候使用默认方式
        return Early.DEFAULT.extractDialect(dialectKey, ms, dataSource, properties);
    }

}
