package org.aoju.bus.cache.provider;

import com.google.common.base.StandardSystemProperty;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

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
public class SqliteProvider extends AbstractProvider {

    public SqliteProvider() {
        this(StandardSystemProperty.USER_HOME.value() + "/.sqlite.db");
    }

    public SqliteProvider(String dbPath) {
        super(dbPath, Collections.emptyMap());
    }

    @Override
    protected Supplier<JdbcOperations> jdbcOperationsSupplier(String dbPath, Map<String, Object> context) {
        return () -> {
            SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
            dataSource.setDriverClassName("org.sqlite.JDBC");
            dataSource.setUrl(String.format("jdbc:sqlite:%s", dbPath));

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
