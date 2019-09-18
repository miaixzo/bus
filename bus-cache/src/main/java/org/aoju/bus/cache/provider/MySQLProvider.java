/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.cache.provider;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Kimi Liu
 * @version 3.5.0
 * @since JDK 1.8
 */
public class MySQLProvider extends AbstractProvider {

    private static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";

    private static final String URL_MYSQL = "jdbc:mysql://${host}:${port}/${database}";
    private static final Pattern pattern = Pattern.compile("\\$\\{(\\w)+}");

    public MySQLProvider(String host, long port, String username, String password) {
        this(host, port,
                System.getProperty("product.name", "unnamed"),
                username, password);
    }

    public MySQLProvider(String host, long port, String database, String username, String password) {
        super(database,
                newHashMap(
                        "host", host,
                        "port", port,
                        "username", username,
                        "password", password
                ));
    }

    private static HashMap<String, Object> newHashMap(Object... keyValues) {
        HashMap<String, Object> map = new HashMap<>(keyValues.length / 2);
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = (String) keyValues[i];
            Object value = keyValues[i + 1];

            map.put(key, value);
        }

        return map;
    }

    private static String format(String template, Map<String, Object> argMap) {
        Matcher matcher = pattern.matcher(template);

        while (matcher.find()) {
            String exp = matcher.group();
            Object value = argMap.get(trim(exp));
            String expStrValue = getStringValue(value);

            template = template.replace(exp, expStrValue);
        }

        return template;
    }

    private static String getStringValue(Object obj) {
        String string;

        if (obj instanceof String) {
            string = (String) obj;
        } else {
            string = String.valueOf(obj);
        }

        return string;
    }

    private static String trim(String string) {
        if (string.startsWith("${"))
            string = string.substring("${".length());

        if (string.endsWith("}"))
            string = string.substring(0, string.length() - "}".length());

        return string;
    }

    @Override
    protected Supplier<JdbcOperations> jdbcOperationsSupplier(String dbPath, Map<String, Object> context) {
        return () -> {
            context.put("database", dbPath);
            SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
            dataSource.setDriverClassName(DRIVER_MYSQL);
            dataSource.setUrl(format(URL_MYSQL, context));
            dataSource.setUsername((String) context.get("username"));
            dataSource.setPassword((String) context.get("password"));

            JdbcTemplate template = new JdbcTemplate(dataSource);
            template.execute("CREATE TABLE IF NOT EXISTS hi_cache_rate(" +
                    "id BIGINT     PRIMARY KEY AUTO_INCREMENT," +
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
            dataDO.setRequireCount((Long) result.get("require_count"));
            dataDO.setHitCount((Long) result.get("hit_count"));
            dataDO.setPattern((String) result.get("pattern"));
            dataDO.setVersion((Long) result.get("version"));

            return dataDO;
        });
    }

}
