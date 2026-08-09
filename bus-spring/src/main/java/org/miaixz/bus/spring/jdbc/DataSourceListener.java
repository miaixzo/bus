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

import javax.sql.DataSource;

import org.springframework.core.Ordered;

/**
 * Observes successful changes to datasource routes owned by one dynamic datasource.
 *
 * @author Kimi Liu
 */
public interface DataSourceListener extends Ordered {

    /**
     * Handles a datasource route after it has been registered successfully.
     *
     * @param key        routing key
     * @param dataSource registered datasource
     */
    default void onAdded(String key, DataSource dataSource) {
        // No action required.
    }

    /**
     * Handles a datasource route after it has been removed successfully.
     *
     * @param key        routing key
     * @param dataSource removed datasource
     */
    default void onRemoved(String key, DataSource dataSource) {
        // No action required.
    }

    /**
     * Returns the listener order.
     *
     * @return lowest precedence unless an implementation declares otherwise
     */
    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
