/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.handler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread-bound SQL rewrite context for one MyBatis executor invocation.
 *
 * <p>
 * Mapper handlers use this context to pass request-scoped SQL rewrites from executor-level interception to
 * statement-level interception without mutating the shared {@code MappedStatement}.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class SqlRewriteContext {

    /**
     * Stack of SQL rewrite scopes for nested executor calls on the current thread.
     */
    private static final ThreadLocal<Deque<Map<String, String>>> SCOPES = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * Prevents instantiation.
     */
    private SqlRewriteContext() {
        // No initialization required.
    }

    /**
     * Opens a new SQL rewrite scope for the current executor invocation.
     */
    static void open() {
        SCOPES.get().push(new HashMap<>());
    }

    /**
     * Stores the final SQL for a mapped statement in the current scope.
     *
     * @param statementId the mapped statement id
     * @param sql         the final SQL
     */
    static void put(String statementId, String sql) {
        if (statementId == null || sql == null) {
            return;
        }
        Deque<Map<String, String>> scopes = SCOPES.get();
        if (scopes.isEmpty()) {
            scopes.push(new HashMap<>());
        }
        scopes.peek().put(statementId, sql);
    }

    /**
     * Reads the final SQL for a mapped statement from the nearest active scope.
     *
     * @param statementId the mapped statement id
     * @return the final SQL, or {@code null} when no rewrite exists
     */
    static String get(String statementId) {
        if (statementId == null) {
            return null;
        }
        for (Map<String, String> scope : SCOPES.get()) {
            String sql = scope.get(statementId);
            if (sql != null) {
                return sql;
            }
        }
        return null;
    }

    /**
     * Closes the current SQL rewrite scope.
     */
    static void close() {
        Deque<Map<String, String>> scopes = SCOPES.get();
        if (!scopes.isEmpty()) {
            scopes.pop();
        }
        if (scopes.isEmpty()) {
            SCOPES.remove();
        }
    }

}
