/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.cache.provider;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Kimi Liu
 * @version 6.2.6
 * @since JDK 1.8+
 */
public class MySQLHitting extends AbstractHitting {

    public MySQLHitting(Map<String, Object> context) {
        super(context);
    }

    public MySQLHitting(String url, String username, String password) {
        super(url, username, password);
    }

    @Override
    protected Supplier<JdbcOperations> jdbcOperationsSupplier(Map<String, Object> context) {
        return () -> {
            try {
                DruidDataSource druidDataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(context);
                druidDataSource.init();
                JdbcTemplate template = new JdbcTemplate(druidDataSource);
                template.execute("CREATE TABLE IF NOT EXISTS hi_cache_rate(" +
                        "id BIGINT     PRIMARY KEY AUTO_INCREMENT," +
                        "pattern       VARCHAR(64) NOT NULL UNIQUE," +
                        "hit_count     BIGINT      NOT NULL     DEFAULT 0," +
                        "require_count BIGINT      NOT NULL     DEFAULT 0," +
                        "version       BIGINT      NOT NULL     DEFAULT 0)");

                return template;
            } catch (Exception e) {
                throw new InstrumentException(e);
            }
        };
    }

    @Override
    protected Stream<DataDO> transferResults(List<Map<String, Object>> mapResults) {
        return mapResults.stream().map(result -> {
            DataDO dataDO = new DataDO();
            dataDO.setRequireCount((Long) result.get("require_count"));
            dataDO.setHitCount((Long) result.get("hit_count"));
            dataDO.setPattern((String) result.get("pattern"));
            dataDO.setVersion((Long) result.get("version"));

            return dataDO;
        });
    }

}
