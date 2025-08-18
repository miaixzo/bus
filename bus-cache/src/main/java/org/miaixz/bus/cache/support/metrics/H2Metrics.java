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
package org.miaixz.bus.cache.support.metrics;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.miaixz.bus.core.xyz.StringKit;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import jakarta.annotation.PreDestroy;

/**
 * H2数据库缓存命中率统计实现
 * <p>
 * 基于H2内存数据库实现的缓存命中率统计，使用单连接数据源和JdbcTemplate进行数据库操作。 自动创建缓存统计表，支持并发更新和乐观锁机制。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class H2Metrics extends AbstractMetrics {

    /**
     * 构造方法
     *
     * @param context 上下文参数
     */
    public H2Metrics(Map<String, Object> context) {
        super(context);
    }

    /**
     * 构造方法
     *
     * @param url      数据库URL
     * @param username 用户名
     * @param password 密码
     */
    public H2Metrics(String url, String username, String password) {
        super(url, username, password);
    }

    /**
     * 创建JdbcOperations并初始化数据库
     * <p>
     * 创建H2数据库连接，初始化JdbcTemplate，并创建缓存统计表
     * </p>
     *
     * @param context 上下文参数
     * @return 初始化完成的JdbcOperations对象
     */
    @Override
    protected Supplier<JdbcOperations> jdbcOperationsSupplier(Map<String, Object> context) {
        return () -> {
            SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl(StringKit.toString(context.get("url")));
            dataSource.setUsername(StringKit.toString(context.get("username")));
            dataSource.setPassword(StringKit.toString(context.get("password")));
            JdbcTemplate template = new JdbcTemplate(dataSource);
            template.execute("CREATE TABLE IF NOT EXISTS t_cache_rate(" + "id BIGINT     IDENTITY PRIMARY KEY,"
                    + "pattern       VARCHAR(64) NOT NULL UNIQUE," + "hit_count     BIGINT      NOT NULL     DEFAULT 0,"
                    + "require_count BIGINT      NOT NULL     DEFAULT 0,"
                    + "version       BIGINT      NOT NULL     DEFAULT 0)");
            return template;
        };
    }

    /**
     * 将数据库查询结果转换为DataDO流
     * <p>
     * 将查询结果Map转换为DataDO对象流，便于后续处理
     * </p>
     *
     * @param mapResults 数据库查询结果
     * @return DataDO流
     */
    @Override
    protected Stream<DataDO> transferResults(List<Map<String, Object>> mapResults) {
        return mapResults.stream().map((map) -> {
            AbstractMetrics.DataDO dataDO = new AbstractMetrics.DataDO();
            dataDO.setPattern((String) map.get("PATTERN"));
            dataDO.setHitCount((long) map.get("HIT_COUNT"));
            dataDO.setRequireCount((long) map.get("REQUIRE_COUNT"));
            dataDO.setVersion((long) map.get("VERSION"));
            return dataDO;
        });
    }

    /**
     * 销毁方法
     * <p>
     * 使用@PreDestroy注解，在Bean销毁时调用父类的tearDown方法
     * </p>
     */
    @PreDestroy
    public void tearDown() {
        super.tearDown();
    }

}