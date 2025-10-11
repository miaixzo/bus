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
package org.miaixz.bus.pager.dialect.auto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.pager.dialect.AutoDialect;
import org.miaixz.bus.pager.dialect.AbstractAutoDialect;
import org.miaixz.bus.pager.dialect.AbstractPaging;

/**
 * Default auto-dialect implementation that iterates through all registered {@link AbstractAutoDialect} implementations
 * to find a matching one. If no specific match is found, it falls back to the {@link Early} dialect.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Defalut implements AutoDialect<String> {

    /**
     * A list of registered {@link AbstractAutoDialect} implementations. These are initialized statically, and only
     * those with available dependencies will be added.
     */
    private static final List<AbstractAutoDialect> AUTO_DIALECTS = new ArrayList<>();

    static {
        // When created, initialize all implementations. If a dependent connection pool does not exist,
        // it will not be successfully added here. Therefore, the content here will not be extensive,
        // and it will not iterate multiple times during execution.
        try {
            AUTO_DIALECTS.add(new Hikari());
        } catch (Exception ignore) {
        }
        try {
            AUTO_DIALECTS.add(new Druid());
        } catch (Exception ignore) {
        }
    }

    /**
     * A concurrent map to store the mapping between JDBC URLs (dialect keys) and their corresponding
     * {@link AbstractAutoDialect} instances.
     */
    private Map<String, AbstractAutoDialect> urlMap = new ConcurrentHashMap<>();

    /**
     * Allows manual registration of additional {@link AbstractAutoDialect} implementations. This is generally not
     * necessary as common ones are auto-detected.
     *
     * @param autoDialect the {@link AbstractAutoDialect} instance to register
     */
    public static void registerAutoDialect(AbstractAutoDialect autoDialect) {
        AUTO_DIALECTS.add(autoDialect);
    }

    /**
     * Extracts the dialect key (JDBC URL) by iterating through registered auto-dialects. If a match is found, it's
     * cached and returned. Otherwise, it falls back to {@link Early#DEFAULT}.
     *
     * @param ms         the MappedStatement being executed
     * @param dataSource the DataSource associated with the MappedStatement
     * @param properties the configuration properties
     * @return the dialect key (JDBC URL) for the detected dialect
     */
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
        // If no match is found, use the default method
        return Early.DEFAULT.extractDialectKey(ms, dataSource, properties);
    }

    /**
     * Extracts and returns the appropriate {@link AbstractPaging} dialect based on the dialect key. It uses the cached
     * auto-dialect if available, otherwise falls back to {@link Early#DEFAULT}.
     *
     * @param dialectKey the dialect key (JDBC URL)
     * @param ms         the MappedStatement being executed
     * @param dataSource the DataSource associated with the MappedStatement
     * @param properties the configuration properties
     * @return an instance of {@link AbstractPaging} representing the determined dialect
     */
    @Override
    public AbstractPaging extractDialect(
            String dialectKey,
            MappedStatement ms,
            DataSource dataSource,
            Properties properties) {
        if (dialectKey != null && urlMap.containsKey(dialectKey)) {
            return urlMap.get(dialectKey).extractDialect(dialectKey, ms, dataSource, properties);
        }
        // If no match is found, use the default method
        return Early.DEFAULT.extractDialect(dialectKey, ms, dataSource, properties);
    }

}
