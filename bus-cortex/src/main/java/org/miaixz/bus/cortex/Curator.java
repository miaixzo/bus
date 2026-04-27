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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Curator contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Curator {

    /**
     * Returns the latest published content for the given group and data ID.
     *
     * @param group   setting group name
     * @param data_id setting data identifier
     * @return current setting content, or {@code null} if absent
     */
    String get(String group, String data_id);

    /**
     * Returns setting content after applying client-specific gray routing rules.
     *
     * @param group    setting group name
     * @param data_id  setting data identifier
     * @param clientIp client IP used for gray-rule selection
     * @return resolved setting content, or {@code null} if absent
     */
    String get(String group, String data_id, String clientIp);

    /**
     * Returns the latest published content for one profile.
     *
     * @param group   setting group name
     * @param data_id setting data identifier
     * @param profile {@code setting.profile}
     * @return current setting content, or {@code null} if absent
     */
    default String getProfile(String group, String data_id, String profile) {
        return get(group, data_id);
    }

    /**
     * Returns setting content after applying client-specific gray routing rules and profile matching.
     *
     * @param group    setting group name
     * @param data_id  setting data identifier
     * @param profile  {@code setting.profile}
     * @param clientIp client IP used for gray-rule selection
     * @return resolved setting content, or {@code null} if absent
     */
    default String get(String group, String data_id, String profile, String clientIp) {
        return getProfile(group, data_id, profile);
    }

    /**
     * Publishes new content for the given group and data ID.
     *
     * @param group   setting group name
     * @param data_id setting data identifier
     * @param content setting content to publish
     */
    void publish(String group, String data_id, String content);

    /**
     * Publishes new content for one profile.
     *
     * @param group   setting group name
     * @param data_id setting data identifier
     * @param profile {@code setting.profile}
     * @param content setting content to publish
     */
    default void publish(String group, String data_id, String profile, String content) {
        publish(group, data_id, content);
    }

    /**
     * Rolls back the setting entry to a historical item revision.
     *
     * @param group    setting group name
     * @param data_id  setting data identifier
     * @param revision historical revision to restore
     */
    void rollback(String group, String data_id, String revision);

    /**
     * Rolls back one profile-specific setting entry to a historical item revision.
     *
     * @param group    setting group name
     * @param data_id  setting data identifier
     * @param profile  {@code setting.profile}
     * @param revision historical revision to restore
     */
    default void rollback(String group, String data_id, String profile, String revision) {
        rollback(group, data_id, revision);
    }

    /**
     * Resolves a batch of setting values keyed by logical identifier.
     *
     * @param groupAndDataIds logical identifiers in {@code group:data_id} form
     * @return resolved values
     */
    default Map<String, String> batchGet(List<String> groupAndDataIds) {
        Map<String, String> result = new LinkedHashMap<>();
        if (groupAndDataIds == null) {
            return result;
        }
        for (String item : groupAndDataIds) {
            if (item == null || item.isBlank()) {
                continue;
            }
            String[] parts = item.split(":", 2);
            if (parts.length != 2) {
                continue;
            }
            result.put(item, get(parts[0], parts[1]));
        }
        return result;
    }

    /**
     * Resolves one setting value with fallback content.
     *
     * @param group    setting group name
     * @param data_id  setting data identifier
     * @param fallback fallback content
     * @return resolved setting content or fallback
     */
    default String getOrDefault(String group, String data_id, String fallback) {
        String value = get(group, data_id);
        return value == null ? fallback : value;
    }

    /**
     * Subscribes to setting changes and returns a watch identifier.
     *
     * @param group    setting group name
     * @param data_id  setting data identifier
     * @param listener listener invoked with updated content
     * @return watch identifier used to cancel the subscription
     */
    String watch(String group, String data_id, Consumer<String> listener);

    /**
     * Cancels a previously registered setting watch.
     *
     * @param watch_id watch identifier returned by {@link #watch(String, String, Consumer)}
     */
    void unwatch(String watch_id);

}
