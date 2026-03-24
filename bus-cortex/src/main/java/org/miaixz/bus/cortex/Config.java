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
package org.miaixz.bus.cortex;

import java.util.function.Consumer;

/**
 * Configuration center contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Config {

    /**
     * Returns the latest published content for the given group and data ID.
     *
     * @param group  config group name
     * @param dataId config data identifier
     * @return current config content, or {@code null} if absent
     */
    String get(String group, String dataId);

    /**
     * Returns config content after applying client-specific gray routing rules.
     *
     * @param group    config group name
     * @param dataId   config data identifier
     * @param clientIp client IP used for gray-rule selection
     * @return resolved config content, or {@code null} if absent
     */
    String get(String group, String dataId, String clientIp);

    /**
     * Publishes new content for the given group and data ID.
     *
     * @param group   config group name
     * @param dataId  config data identifier
     * @param content config content to publish
     */
    void publish(String group, String dataId, String content);

    /**
     * Rolls back the config entry to a historical version.
     *
     * @param group   config group name
     * @param dataId  config data identifier
     * @param version historical version to restore
     */
    void rollback(String group, String dataId, long version);

    /**
     * Subscribes to config changes and returns a watch identifier.
     *
     * @param group    config group name
     * @param dataId   config data identifier
     * @param listener listener invoked with updated content
     * @return watch identifier used to cancel the subscription
     */
    String watch(String group, String dataId, Consumer<String> listener);

    /**
     * Cancels a previously registered config watch.
     *
     * @param watchId watch identifier returned by {@link #watch(String, String, Consumer)}
     */
    void unwatch(String watchId);

}
