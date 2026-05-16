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
package org.miaixz.bus.mapper.support.schema;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * JVM-local table lock used during startup schema initialization.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class SchemaInitializationLock {

    /**
     * JVM-local locks by datasource and table identity.
     */
    private static final ConcurrentHashMap<SchemaLockKey, ReentrantLock> LOCKS = new ConcurrentHashMap<>();

    /**
     * Prevents instantiation.
     */
    private SchemaInitializationLock() {

    }

    /**
     * Acquires a JVM-local table lock.
     *
     * @param dataSource the datasource being initialized
     * @param table      the table metadata
     * @return the lock handle
     */
    static AutoCloseable acquire(DataSource dataSource, TableMeta table) {
        SchemaLockKey key = new SchemaLockKey(dataSource, table.catalog(), table.schema(), table.tableName());
        ReentrantLock lock = LOCKS.computeIfAbsent(key, ignored -> new ReentrantLock());
        lock.lock();
        return new LockHandle(key, lock, LOCKS);
    }

    /**
     * JVM-local schema lock handle.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class LockHandle implements AutoCloseable {

        /**
         * Schema lock key.
         */
        private final SchemaLockKey key;

        /**
         * Acquired lock.
         */
        private final ReentrantLock lock;

        /**
         * Lock registry.
         */
        private final ConcurrentMap<SchemaLockKey, ReentrantLock> locks;

        /**
         * Creates a lock handle.
         *
         * @param key   the schema lock key
         * @param lock  the acquired lock
         * @param locks the lock registry
         */
        private LockHandle(SchemaLockKey key, ReentrantLock lock, ConcurrentMap<SchemaLockKey, ReentrantLock> locks) {
            this.key = key;
            this.lock = lock;
            this.locks = locks;
        }

        /**
         * Releases the acquired JVM-local lock.
         */
        @Override
        public void close() {
            lock.unlock();
            if (!lock.isLocked() && !lock.hasQueuedThreads()) {
                locks.remove(key, lock);
            }
        }

    }

    /**
     * JVM-local schema lock key.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class SchemaLockKey {

        /**
         * Datasource compared by identity.
         */
        private final DataSource dataSource;

        /**
         * Normalized catalog.
         */
        private final String catalog;

        /**
         * Normalized schema.
         */
        private final String schema;

        /**
         * Normalized table name.
         */
        private final String tableName;

        /**
         * Creates a schema lock key.
         *
         * @param dataSource the datasource
         * @param catalog    the catalog name
         * @param schema     the schema name
         * @param tableName  the table name
         */
        private SchemaLockKey(DataSource dataSource, String catalog, String schema, String tableName) {
            this.dataSource = dataSource;
            this.catalog = TableSnapshot.normalizeIdentifier(catalog);
            this.schema = TableSnapshot.normalizeIdentifier(schema);
            this.tableName = TableSnapshot.normalizeIdentifier(tableName);
        }

        /**
         * Tests equality using datasource identity and normalized value fields.
         *
         * @param object the object to compare
         * @return {@code true} when both keys identify the same table initialization lock
         */
        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof SchemaLockKey that)) {
                return false;
            }
            return dataSource == that.dataSource && Objects.equals(catalog, that.catalog)
                    && Objects.equals(schema, that.schema) && Objects.equals(tableName, that.tableName);
        }

        /**
         * Returns a hash code based on datasource identity and normalized value fields.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(dataSource), catalog, schema, tableName);
        }

    }

}
