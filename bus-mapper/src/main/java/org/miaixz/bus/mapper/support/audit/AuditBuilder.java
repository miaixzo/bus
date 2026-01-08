/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.mapper.support.audit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.core.lang.annotation.Ignore;

/**
 * Audit log builder.
 *
 * <p>
 * Responsible for building audit records and processing audit-related logic. This class handles:
 * </p>
 * <ul>
 * <li>Audit record creation and management</li>
 * <li>Result processing and statistics</li>
 * <li>Ignore annotation detection and caching</li>
 * <li>Audit logging strategy implementation</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AuditBuilder {

    /**
     * Cache whether Mapper methods should ignore audit
     */
    private final Map<String, Boolean> ignoreCache = new ConcurrentHashMap<>();

    /**
     * Audit configuration
     */
    private final AuditConfig config;

    /**
     * Constructor.
     *
     * @param config the audit configuration
     */
    public AuditBuilder(AuditConfig config) {
        this.config = config;
    }

    /**
     * Start audit record (UPDATE, INSERT, DELETE).
     *
     * @param mappedStatement Mapper statement
     * @param parameter       Parameters
     * @return created audit record
     */
    public AuditRecord before(MappedStatement mappedStatement, Object parameter) {
        AuditRecord record = AuditRecord.of().sqlId(mappedStatement.getId())
                .sqlType(mappedStatement.getSqlCommandType().name())
                .parameter(config.isLogParameters() ? parameter : null).context(new HashMap<>(AuditContext.get()))
                .start();

        AuditContext.setRecord(record);
        return record;
    }

    /**
     * Start audit record (SELECT).
     *
     * @param mappedStatement Mapper statement
     * @param parameter       Parameters
     * @param boundSql        Bound SQL
     * @return created audit record
     */
    public AuditRecord before(MappedStatement mappedStatement, Object parameter, BoundSql boundSql) {
        AuditRecord record = AuditRecord.of().sqlId(mappedStatement.getId())
                .sqlType(mappedStatement.getSqlCommandType().name()).sql(boundSql.getSql())
                .parameter(config.isLogParameters() ? parameter : null).context(new HashMap<>(AuditContext.get()))
                .start();

        AuditContext.setRecord(record);
        return record;
    }

    /**
     * End audit record.
     *
     * @param result    Execution result
     * @param exception Exception information (if any)
     */
    public void after(Object result, Throwable exception) {
        AuditRecord record = AuditContext.getRecord();
        if (record == null) {
            return;
        }

        try {
            // Record end time
            record.end();

            // Record result
            if (exception != null) {
                record.failure(exception);
            } else {
                record.success();
                if (config.isLogResults()) {
                    recordResult(record, result);
                }
            }

            // Output audit log
            logAudit(record);

        } finally {
            // Clean up context
            AuditContext.removeRecord();
        }
    }

    /**
     * Clear audit cache.
     */
    public void clear() {
        ignoreCache.clear();
    }

    /**
     * Determine whether to ignore audit.
     *
     * @param mappedStatement Mapper statement
     * @return true to ignore, false not to ignore
     */
    public boolean shouldIgnoreAudit(MappedStatement mappedStatement) {
        String id = mappedStatement.getId();

        // Get from cache
        Boolean cached = ignoreCache.get(id);
        if (cached != null) {
            return cached;
        }

        // Check if method or class has @Ignore annotation
        try {
            String className = id.substring(0, id.lastIndexOf('.'));
            String methodName = id.substring(id.lastIndexOf('.') + 1);

            Class<?> mapperClass = Class.forName(className);

            // Check class level annotation
            if (mapperClass.isAnnotationPresent(Ignore.class)) {
                ignoreCache.put(id, true);
                return true;
            }

            // Check method level annotation
            for (var method : mapperClass.getMethods()) {
                if (method.getName().equals(methodName) && method.isAnnotationPresent(Ignore.class)) {
                    ignoreCache.put(id, true);
                    return true;
                }
            }

            // No annotation
            ignoreCache.put(id, false);
            return false;

        } catch (Exception e) {
            // If check fails, default to not ignoring
            return false;
        }
    }

    /**
     * Record execution result.
     *
     * @param record Audit record
     * @param result Execution result
     */
    private void recordResult(AuditRecord record, Object result) {
        if (result instanceof Integer) {
            // UPDATE, INSERT, DELETE return affected rows
            record.affectedRows((Integer) result);
        } else if (result instanceof List) {
            // SELECT returns result list
            record.resultCount(((List<?>) result).size());
        } else if (result instanceof Object[]) {
            // Handle array result
            Object[] arr = (Object[]) result;
            if (arr.length > 0 && arr[0] instanceof List) {
                record.resultCount(((List<?>) arr[0]).size());
            }
        }
    }

    /**
     * Output audit log.
     *
     * @param record Audit record
     */
    private void logAudit(AuditRecord record) {
        AuditProvider provider = config.getProvider();
        if (provider == null) {
            return;
        }

        // Determine whether logging is needed
        boolean shouldLog = config.isLogAllSql() || config.isSlowSql(record.getElapsedTime()) || !record.isSuccess();

        if (!shouldLog) {
            return;
        }

        // Log based on different situations
        if (!record.isSuccess()) {
            provider.logFailure(record);
        } else if (config.isSlowSql(record.getElapsedTime())) {
            provider.logSlowSql(record);
        } else {
            provider.log(record);
        }

        // Console print
        if (config.isPrintConsole()) {
            System.out.println(record);
        }
    }

}
