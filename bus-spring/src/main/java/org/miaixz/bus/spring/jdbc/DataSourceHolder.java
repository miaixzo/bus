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
package org.miaixz.bus.spring.jdbc;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Owns the default and explicitly selected JDBC routing keys.
 * <p>
 * Routing integrations use the effective key returned by {@link #getKey()}. Consumers may observe that key through a
 * read-only callback, but they do not own or modify this holder.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DataSourceHolder {

    /**
     * Explicit JDBC routing key selected for the current thread.
     */
    private final ThreadLocal<String> currentKey = new ThreadLocal<>();

    /**
     * Application-context primary routing key used when the current thread has no explicit selection.
     */
    private volatile String defaultKey = Normal.DEFAULT;

    /**
     * Creates an application-context-scoped datasource holder.
     */
    public DataSourceHolder() {
        // No initialization required.
    }

    /**
     * Returns the explicitly selected routing key.
     *
     * @return explicitly selected key, or {@code null} when the application default applies
     */
    public String getCurrentKey() {
        return this.currentKey.get();
    }

    /**
     * Returns the effective JDBC routing key.
     *
     * @return explicit routing key or the default routing key
     */
    public String getKey() {
        String key = this.currentKey.get();
        return key == null ? this.defaultKey : key;
    }

    /**
     * Sets the default JDBC routing key.
     *
     * @param key configured primary datasource key
     */
    public void setDefaultKey(String key) {
        String value = StringKit.trim(key);
        if (StringKit.isEmpty(value)) {
            throw new IllegalArgumentException("Default JDBC datasource key must not be blank");
        }
        this.defaultKey = value;
    }

    /**
     * Selects an explicit JDBC routing key.
     *
     * @param key routing key; {@code null} removes the explicit selection
     */
    public void setKey(String key) {
        if (key == null) {
            remove();
        } else {
            this.currentKey.set(key);
        }
    }

    /**
     * Removes the explicit JDBC routing key from the current thread.
     */
    public void remove() {
        this.currentKey.remove();
    }

    /**
     * Opens a lexical routing scope that restores the exact previous thread state when closed.
     *
     * @param key explicit key for this scope
     * @return scope that restores the exact parent state
     */
    public Scope scope(String key) {
        return new Scope(key);
    }

    /**
     * Lexical JDBC routing scope with idempotent parent-state restoration.
     */
    public class Scope implements AutoCloseable {

        /**
         * Datasource key that was active before this scope was opened.
         */
        private final String previous;

        /**
         * Ensures restoration occurs at most once.
         */
        private boolean closed;

        /**
         * Creates a scope that restores the previous datasource key when closed.
         *
         * @param key routing key for the scope; {@code null} clears the explicit selection within the scope
         */
        private Scope(String key) {
            this.previous = currentKey.get();
            setKey(key);
        }

        /**
         * Restores the exact routing key that was active before this scope opened.
         */
        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            setKey(previous);
        }

    }

}
