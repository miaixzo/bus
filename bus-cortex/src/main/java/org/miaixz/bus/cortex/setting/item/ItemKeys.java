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
package org.miaixz.bus.cortex.setting.item;

import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;

/**
 * Shared key and identifier helpers for the curator.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ItemKeys {

    /**
     * Sequence stream segment used for setting item revisions.
     */
    public static final String SETTING_REVISION_SEQUENCE_SEGMENT = "setting:item:";

    /**
     * Utility class.
     */
    private ItemKeys() {

    }

    /**
     * Builds the stable logical item identifier.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @return stable setting entry identifier
     */
    public static String itemId(String namespace, String group, String data_id) {
        StringBuilder builder = new StringBuilder();
        builder.append(CortexIdentity.namespace(namespace)).append(':').append(nullToEmpty(group)).append(':')
                .append(nullToEmpty(data_id));
        return builder.toString();
    }

    /**
     * Builds the stable logical item identifier.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @return stable setting entry identifier
     */
    public static String id(String namespace, String group, String data_id) {
        return itemId(namespace, group, data_id);
    }

    /**
     * Builds the logical scope used by runtime setting channels.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return profile scoped identifier, or durable item identifier when profile is empty
     */
    public static String profileScope(String namespace, String group, String data_id, String profile) {
        String itemId = itemId(namespace, group, data_id);
        String normalizedProfile = normalizeProfile(profile);
        return normalizedProfile == null ? itemId : itemId + ":" + normalizedProfile;
    }

    /**
     * Builds the logical watch key for a setting item.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return full setting watch key
     */
    public static String watchKey(String namespace, String group, String data_id, String profile) {
        return watchKeyForScope(namespace, group, data_id, profile);
    }

    /**
     * Builds the logical watch key for a setting item scope.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return full setting watch key
     */
    public static String watchKeyForScope(String namespace, String group, String data_id, String profile) {
        return Builder.SETTING_PREFIX + profileScope(namespace, group, data_id, profile);
    }

    /**
     * Builds the cache key used for one runtime overlay entry.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return runtime overlay key
     */
    public static String overlayKey(String namespace, String group, String data_id, String profile) {
        return overlayKeyForScope(namespace, group, data_id, profile);
    }

    /**
     * Builds the cache key used for one runtime overlay scope.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return runtime overlay key
     */
    public static String overlayKeyForScope(String namespace, String group, String data_id, String profile) {
        return Builder.SETTING_PREFIX + "overlay:" + profileScope(namespace, group, data_id, profile);
    }

    /**
     * Builds one logical export key used by external exports.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return export key
     */
    public static String exportKey(String namespace, String group, String data_id, String profile) {
        return exportKeyForScope(namespace, group, data_id, profile);
    }

    /**
     * Builds one logical export key used by external exports for a setting scope.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return export key
     */
    public static String exportKeyForScope(String namespace, String group, String data_id, String profile) {
        return profileScope(namespace, group, data_id, profile);
    }

    /**
     * Builds the cache key prefix for current-state setting entries in one namespace.
     *
     * @param namespace namespace
     * @return current-entry key prefix
     */
    public static String entryPrefix(String namespace) {
        return Builder.SETTING_PREFIX + "entry:" + CortexIdentity.namespace(namespace) + ":";
    }

    /**
     * Builds the cache key for one current-state setting entry.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @return current-entry key
     */
    public static String entryKey(String namespace, String group, String data_id) {
        return entryKeyForScope(namespace, group, data_id, null);
    }

    /**
     * Builds the cache key for one current-state setting entry scope.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return scoped current-entry key
     */
    public static String entryKeyForScope(String namespace, String group, String data_id, String profile) {
        return Builder.SETTING_PREFIX + "entry:" + profileScope(namespace, group, data_id, profile);
    }

    /**
     * Builds the cache key prefix for all current-state scopes under one item coordinate.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @return current-entry item prefix
     */
    public static String entryPrefixForScope(String namespace, String group, String data_id) {
        return Builder.SETTING_PREFIX + "entry:" + itemId(namespace, group, data_id);
    }

    /**
     * Builds the cache key prefix for {@code setting.item.revision} snapshots.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return revision key prefix
     */
    public static String revisionPrefix(String namespace, String group, String data_id, String profile) {
        return revisionPrefixForScope(namespace, group, data_id, profile);
    }

    /**
     * Builds the cache key prefix for {@code setting.item.revision} snapshots in one runtime scope.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @return scoped revision key prefix
     */
    public static String revisionPrefixForScope(String namespace, String group, String data_id, String profile) {
        return Builder.SETTING_PREFIX + "revision:" + profileScope(namespace, group, data_id, profile) + ":";
    }

    /**
     * Builds the cache key for one concrete revision.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @param revision  revision number
     * @return revision cache key
     */
    public static String revisionKey(String namespace, String group, String data_id, String profile, String revision) {
        return revisionKeyForScope(namespace, group, data_id, profile, revision);
    }

    /**
     * Builds the cache key for one concrete revision in a runtime scope.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @param profile   optional profile
     * @param revision  revision number
     * @return scoped revision cache key
     */
    public static String revisionKeyForScope(
            String namespace,
            String group,
            String data_id,
            String profile,
            String revision) {
        return revisionPrefixForScope(namespace, group, data_id, profile) + revision;
    }

    /**
     * Builds the sequence key used to allocate {@code setting.item.revision} numbers.
     *
     * @param namespace namespace
     * @param group     setting group
     * @param data_id   setting data identifier
     * @return sequence key
     */
    public static String revisionSequenceKey(String namespace, String group, String data_id) {
        return Builder.SEQUENCE_PREFIX + SETTING_REVISION_SEQUENCE_SEGMENT + itemId(namespace, group, data_id);
    }

    /**
     * Normalizes null string segments to empty strings before key composition.
     *
     * @param value raw segment value
     * @return non-null key segment
     */
    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * Normalizes a profile segment for setting item keys.
     *
     * @param profile raw profile segment
     * @return normalized profile segment or {@code null}
     */
    private static String normalizeProfile(String profile) {
        if (profile == null || profile.isBlank()) {
            return null;
        }
        return profile.trim().toLowerCase();
    }

}
