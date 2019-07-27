package org.aoju.bus.cache.provider;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class H2Provider extends AbstractProvider {

    public H2Provider() {
        this(System.getProperty("user.home") + "/.H2/cache");
    }

    public H2Provider(String dbPath) {
        super(dbPath, Collections.emptyMap());
    }

    @Override
    protected Supplier<JdbcOperations> jdbcOperationsSupplier(String dbPath, Map<String, Object> context) {
        return () -> {
            SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl(String.format("jdbc:h2:%s;AUTO_SERVER=TRUE;AUTO_RECONNECT=TRUE;AUTO_SERVER=TRUE", dbPath));
            dataSource.setUsername("cache");
            dataSource.setPassword("cache");

            JdbcTemplate template = new JdbcTemplate(dataSource);
            template.execute("CREATE TABLE IF NOT EXISTS hi_hit_rate(" +
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
            AbstractProvider.DataDO dataDO = new AbstractProvider.DataDO();
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
