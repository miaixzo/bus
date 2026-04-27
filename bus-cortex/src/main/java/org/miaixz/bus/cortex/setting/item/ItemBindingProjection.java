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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Binding-focused helpers for item app/profile scope and extension payloads.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ItemBindingProjection {

    /**
     * Prevents utility class instantiation.
     */
    private ItemBindingProjection() {

    }

    /**
     * Returns normalized application identifiers from one item.
     *
     * @param item setting item
     * @return normalized application identifiers or {@code null}
     */
    public static List<String> normalizedAppIds(Item item) {
        return item == null ? null : normalizeIds(item.getApp_ids(), false);
    }

    /**
     * Returns normalized profile identifiers from one item.
     *
     * @param item setting item
     * @return normalized profile identifiers or {@code null}
     */
    public static List<String> normalizedProfileIds(Item item) {
        return item == null ? null : normalizeIds(item.getProfile_ids(), true);
    }

    /**
     * Returns the first normalized profile identifier from one item.
     *
     * @param item setting item
     * @return first profile identifier or {@code null}
     */
    public static String firstProfileId(Item item) {
        List<String> profiles = normalizedProfileIds(item);
        return profiles == null || profiles.isEmpty() ? null : profiles.getFirst();
    }

    /**
     * Normalizes and writes application identifiers into one item.
     *
     * @param item   setting item
     * @param appIds raw application identifiers
     */
    public static void normalizeAppIdsInto(Item item, List<String> appIds) {
        if (item == null) {
            return;
        }
        item.setApp_ids(normalizeIds(appIds, false));
    }

    /**
     * Normalizes and writes one application identifier into one item.
     *
     * @param item  setting item
     * @param appId raw application identifier
     */
    public static void normalizeAppIdsInto(Item item, String appId) {
        normalizeAppIdsInto(item, appId == null ? null : List.of(appId));
    }

    /**
     * Normalizes and writes profile identifiers into one item.
     *
     * @param item       setting item
     * @param profileIds raw profile identifiers
     */
    public static void normalizeProfileIdsInto(Item item, List<String> profileIds) {
        if (item == null) {
            return;
        }
        item.setProfile_ids(normalizeIds(profileIds, true));
    }

    /**
     * Normalizes and writes one profile identifier into one item.
     *
     * @param item      setting item
     * @param profileId raw profile identifier
     */
    public static void normalizeProfileIdsInto(Item item, String profileId) {
        normalizeProfileIdsInto(item, profileId == null ? null : List.of(profileId));
    }

    /**
     * Copies extension metadata into one item through JSON normalization.
     *
     * @param item      setting item
     * @param extension extension metadata
     */
    public static void copyExtensionInto(Item item, Map<String, Object> extension) {
        if (item == null) {
            return;
        }
        item.setExtension(extension == null ? null : JsonKit.toMap(JsonKit.toJsonString(extension)));
    }

    /**
     * Returns whether an item is shared across all applications.
     *
     * @param item setting item
     * @return {@code true} when the item has no application binding
     */
    public static boolean isSharedAcrossApps(Item item) {
        List<String> appIds = normalizedAppIds(item);
        return appIds == null || appIds.isEmpty();
    }

    /**
     * Returns whether an item is available to the supplied application.
     *
     * @param item  setting item
     * @param appId application identifier
     * @return {@code true} when the item is shared or bound to the application
     */
    public static boolean bindsToApp(Item item, String appId) {
        if (isSharedAcrossApps(item)) {
            return true;
        }
        if (StringKit.isEmpty(appId)) {
            return false;
        }
        return normalizedAppIds(item).contains(appId);
    }

    /**
     * Returns whether an item matches the supplied profile binding.
     *
     * @param item      setting item
     * @param profileId profile identifier
     * @return {@code true} when the item is shared or bound to the profile
     */
    public static boolean matchesProfileBinding(Item item, String profileId) {
        if (StringKit.isEmpty(profileId)) {
            return true;
        }
        List<String> profiles = normalizedProfileIds(item);
        if (profiles == null || profiles.isEmpty()) {
            return true;
        }
        return profiles.contains(normalizeProfile(profileId));
    }

    /**
     * Normalizes a list of identifiers.
     *
     * @param values    raw identifier values
     * @param lowerCase whether identifiers should be lower-cased
     * @return normalized identifiers or {@code null}
     */
    private static List<String> normalizeIds(List<String> values, boolean lowerCase) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            appendNormalized(normalized, value, lowerCase);
        }
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Normalizes one profile identifier.
     *
     * @param profileId raw profile identifier
     * @return normalized profile identifier
     */
    private static String normalizeProfile(String profileId) {
        return profileId == null ? null : profileId.trim().toLowerCase();
    }

    /**
     * Normalizes one application identifier.
     *
     * @param value raw application identifier
     * @return normalized application identifier
     */
    private static String normalizeAppId(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.length() >= 2 && normalized.startsWith("\"") && normalized.endsWith("\"")) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        return normalized;
    }

    /**
     * Appends a normalized identifier when it is non-empty and not already present.
     *
     * @param target    normalized identifier list
     * @param value     raw identifier value
     * @param lowerCase whether identifiers should be lower-cased
     */
    private static void appendNormalized(List<String> target, String value, boolean lowerCase) {
        String normalized = lowerCase ? normalizeProfile(value) : normalizeAppId(value);
        if (StringKit.isEmpty(normalized) || target.contains(normalized)) {
            return;
        }
        target.add(normalized);
    }

}
