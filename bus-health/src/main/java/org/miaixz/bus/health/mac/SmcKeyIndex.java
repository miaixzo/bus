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
package org.miaixz.bus.health.mac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.DoublePredicate;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.logger.Logger;

/**
 * Connection-independent logic for locating macOS SMC sensor keys by index.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public final class SmcKeyIndex {

    /**
     * Apple Silicon GPU cluster temperature key pattern.
     */
    private static final Pattern GPU_TEMPERATURE_KEY = Pattern.compile("^Tg\\d[\\dA-Za-z]$");

    /**
     * Fan current-speed key pattern.
     */
    private static final Pattern FAN_SPEED_KEY = Pattern.compile("^F\\dAc$");

    /**
     * SMC keys are exactly four characters.
     */
    private static final int KEY_LENGTH = 4;

    /**
     * Maximum fan count supported by the SMC key naming scheme.
     */
    public static final int MAX_FANS = 10;

    /**
     * Maximum plausible SMC key count.
     */
    private static final int MAX_KEY_COUNT = 65_536;

    /**
     * Maximum number of keys scanned forward from a located prefix.
     */
    private static final int MAX_SCAN = 256;

    /**
     * Creates a new SmcKeyIndex instance.
     */
    private SmcKeyIndex() {
        // No initialization required.
    }

    /**
     * Locates keys sharing a prefix by binary-searching the sorted SMC key index and scanning forward.
     *
     * @param keyCount   The number of keys in the index.
     * @param keyAtIndex Looks up the key name at an index, returning {@code null} if that read fails.
     * @param prefix     The key prefix to locate.
     * @param mask       Additional test each candidate key must pass.
     * @return Matching keys in index order, {@code null} if the index could not be read reliably.
     */
    public static List<String> findKeys(
            int keyCount,
            IntFunction<String> keyAtIndex,
            String prefix,
            Predicate<String> mask) {
        if (keyCount <= 0 || keyCount > MAX_KEY_COUNT) {
            Logger.debug(false, "Health", "Implausible SMC key count {}; skipping key discovery.", keyCount);
            return null;
        }
        boolean[] readFailed = new boolean[1];
        IntFunction<String> tracked = i -> {
            String key = keyAtIndex.apply(i);
            if (key == null) {
                readFailed[0] = true;
            }
            return key;
        };
        int start = lowerBound(keyCount, tracked, prefix);
        if (start < 0) {
            return null;
        }
        Set<String> found = new LinkedHashSet<>();
        int limit = Math.min(keyCount, start + MAX_SCAN);
        for (int i = start; i < limit; i++) {
            String key = tracked.apply(i);
            if (key == null) {
                key = tracked.apply(i);
            }
            if (key == null) {
                Logger.debug(false, "Health", "Could not read SMC key at index {}; continuing the scan.", i);
            } else {
                if (!key.startsWith(prefix)) {
                    break;
                }
                if (mask.test(key)) {
                    found.add(key);
                }
            }
        }
        if (found.isEmpty() && readFailed[0]) {
            Logger.debug(
                    false,
                    "Health",
                    "No SMC keys matched '{}' and at least one read failed; skipping key discovery.",
                    prefix);
            return null;
        }
        return Collections.unmodifiableList(new ArrayList<>(found));
    }

    /**
     * Binary-searches for the first index whose key sorts at or after the prefix.
     *
     * @param keyCount   The number of keys in the index.
     * @param keyAtIndex Looks up the key name at an index.
     * @param prefix     The prefix to locate.
     * @return The first matching index, or {@code -1} if the index could not be read.
     */
    private static int lowerBound(int keyCount, IntFunction<String> keyAtIndex, String prefix) {
        int lo = 0;
        int hi = keyCount;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            String key = probe(keyAtIndex, mid, keyCount);
            if (key == null) {
                return -1;
            }
            if (key.compareTo(prefix) > 0) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }

    /**
     * Reads the key at an index, retrying nearby indices if that read fails.
     *
     * @param keyAtIndex Looks up the key name at an index.
     * @param index      The index to read.
     * @param keyCount   The number of keys in the index.
     * @return A key name, or {@code null} if nothing nearby could be read.
     */
    private static String probe(IntFunction<String> keyAtIndex, int index, int keyCount) {
        String key = keyAtIndex.apply(index);
        if (key != null) {
            return key;
        }
        for (int delta = 1; delta <= 4; delta++) {
            if (index - delta >= 0) {
                key = keyAtIndex.apply(index - delta);
                if (key != null) {
                    return key;
                }
            }
            if (index + delta < keyCount) {
                key = keyAtIndex.apply(index + delta);
                if (key != null) {
                    return key;
                }
            }
        }
        return null;
    }

    /**
     * Tests whether a key names an Apple Silicon GPU cluster temperature sensor.
     *
     * @param key The four-character SMC key.
     * @return {@code true} if the key matches the GPU temperature naming convention.
     */
    public static boolean isGpuTemperatureKey(String key) {
        return key != null && GPU_TEMPERATURE_KEY.matcher(key).matches();
    }

    /**
     * Tests whether a key names a fan current-speed sensor.
     *
     * @param key The four-character SMC key.
     * @return {@code true} if the key matches the fan speed naming convention.
     */
    public static boolean isFanSpeedKey(String key) {
        return key != null && FAN_SPEED_KEY.matcher(key).matches();
    }

    /**
     * Builds fan current-speed keys from a fan count.
     *
     * @param fanCount The number of fans reported by {@code FNum}.
     * @return Fan speed keys, clamped to {@link #MAX_FANS}.
     */
    public static List<String> fanSpeedKeys(long fanCount) {
        int fans = (int) Math.max(0, Math.min(MAX_FANS, fanCount));
        if (fans < fanCount) {
            Logger.warn(false, "Health", "Ignoring an implausible SMC fan count of {}; using {}.", fanCount, fans);
        }
        List<String> keys = new ArrayList<>(fans);
        for (int i = 0; i < fans; i++) {
            keys.add(String.format(Locale.ROOT, "F%dAc", i));
        }
        return Collections.unmodifiableList(keys);
    }

    /**
     * Reconciles fan keys found in the key index against the count reported by {@code FNum}.
     *
     * @param discovered The discovered keys, or {@code null} if the index could not be read.
     * @param fanCount   The fan count from {@code FNum}.
     * @return The keys to read, or {@code null} if the answer is not yet known.
     */
    public static List<String> reconcileFanKeys(List<String> discovered, long fanCount) {
        if (discovered != null && !discovered.isEmpty()) {
            if (fanCount > 0 && discovered.size() != fanCount) {
                Logger.debug(
                        false,
                        "Health",
                        "Found {} fan speed keys {} but FNum reports {} fans; using the discovered keys.",
                        discovered.size(),
                        discovered,
                        fanCount);
            }
            return discovered;
        }
        if (fanCount > 0) {
            List<String> keys = fanSpeedKeys(fanCount);
            Logger.debug(
                    false,
                    "Health",
                    "Fan speed key discovery found none; using the {} keys FNum implies: {}",
                    keys.size(),
                    keys);
            return keys;
        }
        if (discovered == null) {
            Logger.debug(false, "Health", "Neither the SMC key index nor FNum could be read; deferring the fan count.");
            return null;
        }
        return Collections.emptyList();
    }

    /**
     * Parses a user-supplied comma-separated SMC key list.
     *
     * @param csv The configured value.
     * @return Parsed keys, never {@code null}.
     */
    public static List<String> parseConfiguredKeys(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> keys = new ArrayList<>();
        for (String token : csv.split(",")) {
            String key = token.trim();
            if (key.length() == KEY_LENGTH) {
                keys.add(key);
            } else if (!key.isEmpty()) {
                Logger.warn(
                        false,
                        "Health",
                        "Ignoring configured SMC key '{}': keys are exactly {} characters.",
                        key,
                        KEY_LENGTH);
            }
        }
        return Collections.unmodifiableList(keys);
    }

    /**
     * Returns the first plausible reading among the given keys.
     *
     * @param keys        The keys to read.
     * @param reader      Reads a key and returns the value.
     * @param isPlausible Tests whether a reading is usable.
     * @param description Sensor description for log messages.
     * @return The first plausible reading, or {@code 0} if none were plausible.
     */
    public static double firstPlausible(
            List<String> keys,
            ToDoubleFunction<String> reader,
            DoublePredicate isPlausible,
            String description) {
        for (String key : keys) {
            double value = reader.applyAsDouble(key);
            if (isPlausible.test(value)) {
                return value;
            }
            if (value != 0d) {
                Logger.debug(false, "Health", "Ignoring implausible {} {} from SMC key {}.", description, value, key);
            }
        }
        return 0d;
    }

    /**
     * Returns the highest plausible temperature among the given keys.
     *
     * @param keys        The keys to read.
     * @param reader      Reads a key and returns the temperature in Celsius.
     * @param isPlausible Tests whether a reading is usable.
     * @return The highest plausible reading, or {@code 0} if none were plausible.
     */
    public static double maxPlausible(List<String> keys, ToDoubleFunction<String> reader, DoublePredicate isPlausible) {
        double max = 0d;
        for (String key : keys) {
            double value = reader.applyAsDouble(key);
            if (isPlausible.test(value) && value > max) {
                max = value;
            }
        }
        if (max == 0d && !keys.isEmpty()) {
            Logger.debug(
                    false,
                    "Health",
                    "No plausible temperature among SMC keys {}; sensors are likely idle-gated.",
                    keys);
        }
        return max;
    }

}
