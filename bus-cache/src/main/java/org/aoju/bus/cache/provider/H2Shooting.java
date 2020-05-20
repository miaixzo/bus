/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.cache.provider;

import org.aoju.bus.core.utils.StringUtils;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Kimi Liu
 * @version 5.9.1
 * @since JDK 1.8+
 */
public class H2Shooting extends AbstractShooting {

    public H2Shooting(Map<String, Object> context) {
        super(context);
    }

    public H2Shooting(String url, String username, String password) {
        super(url, username, password);
    }

    @Override
    protected Supplier<JdbcOperations> jdbcOperationsSupplier(Map<String, Object> context) {
        return () -> {
            SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl(StringUtils.toString(context.get("url")));
            dataSource.setUsername(StringUtils.toString(context.get("username")));
            dataSource.setPassword(StringUtils.toString(context.get("password")));

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
        return mapResults.stream().map((map) -> {
            AbstractShooting.DataDO dataDO = new AbstractShooting.DataDO();
            dataDO.setPattern((String) map.get("PATTERN"));
            dataDO.setHitCount((long) map.get("HIT_COUNT"));
            dataDO.setRequireCount((long) map.get("REQUIRE_COUNT"));
            dataDO.setVersion((long) map.get("VERSION"));

            return dataDO;
        });
    }

    @PreDestroy
    public void tearDown() {
        super.tearDown();
    }

}
