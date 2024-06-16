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
package org.miaixz.bus.cache.provider;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqliteHitting extends AbstractHitting {

    public SqliteHitting(Map<String, Object> context) {
        super(context);
    }

    public SqliteHitting(String url, String username, String password) {
        super(url, username, password);
    }

    @Override
    protected Supplier<JdbcOperations> jdbcOperationsSupplier(Map<String, Object> context) {
        return () -> {
            SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
            dataSource.setDriverClassName("org.sqlite.JDBC");
            dataSource.setUrl((String) context.get("url"));

            JdbcTemplate template = new JdbcTemplate(dataSource);
            template.execute("CREATE TABLE IF NOT EXISTS hi_cache_rate(" +
                    "id BIGINT     IDENTITY PRIMARY KEY," +
                    "pattern       VARCHAR(64) NOT NULL UNIQUE," +
                    "hit_count     BIGINT      NOT NULL     DEFAULT 0," +
                    "require_count BIGINT      NOT NULL     DEFAULT 0," +
                    "version       BIGINT      NOT NULL     DEFAULT 0)");

            return template;
        };
    }

    @Override
    protected Stream<DataDO> transferResults(List<Map<String, Object>> mapResults) {
        return mapResults.stream().map(result -> {
            DataDO dataDO = new DataDO();
            dataDO.setHitCount((Integer) result.get("hit_count"));
            dataDO.setPattern((String) result.get("pattern"));
            dataDO.setRequireCount((Integer) result.get("require_count"));
            dataDO.setVersion((Integer) result.get("version"));

            return dataDO;
        });
    }

}
