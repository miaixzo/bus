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
package org.miaixz.bus.health.mac.driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Parsing;

/**
 * Derives Apple Silicon CPU frequencies from IOReport CPU performance-state residency.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public final class CpuFrequencyResidency {

    /**
     * The names of states where a core is not executing.
     */
    private static final Set<String> IDLE_STATES = new HashSet<>(Arrays.asList("IDLE", "DOWN", "OFF"));

    /**
     * The CPU channel name pattern.
     */
    private static final Pattern CPU_CHANNEL = Pattern.compile("^(?:DIE_(\\d+)_)?([A-Z])CPU(\\d+)$");

    /**
     * The realized CPU complex channel name pattern.
     */
    private static final Pattern REALIZED_COMPLEX_CHANNEL = Pattern.compile("^(?:DIE_(\\d+)_)?([A-Z])CPM(\\d*)$");

    /**
     * The CPU channel prefixes in ascending performance order.
     */
    private static final String CHANNEL_PREFIX_ORDER = "EMPS";

    /**
     * Prevents instantiation.
     */
    private CpuFrequencyResidency() {
    }

    /**
     * Computes a running core's active weighted frequency.
     *
     * @param stateResidency the ticks spent in each state, in channel order
     * @param table          the frequency table in ascending order
     * @return the frequency in hertz, or {@code 0} if the data cannot be paired
     */
    public static long activeWeightedFrequency(Map<String, Long> stateResidency, long[] table) {
        if (table.length == Normal._0) {
            return Normal._0;
        }
        List<Long> residency = new ArrayList<>(stateResidency.size());
        long observedTicks = Normal._0;
        int firstActive = Normal.__1;
        for (Map.Entry<String, Long> state : stateResidency.entrySet()) {
            if (firstActive < Normal._0 && !IDLE_STATES.contains(state.getKey().toUpperCase(Locale.ROOT))) {
                firstActive = residency.size();
            }
            Long ticks = state.getValue();
            long clamped = ticks == null ? Normal._0 : Math.max(ticks, Normal._0);
            observedTicks += clamped;
            residency.add(clamped);
        }
        if (firstActive < Normal._0) {
            return Normal._0;
        }
        if (residency.size() - firstActive != table.length) {
            int aligned = residency.size() - table.length;
            if (aligned < firstActive) {
                return Normal._0;
            }
            firstActive = aligned;
        }
        long totalTicks = Normal._0;
        double weighted = 0d;
        for (int i = Normal._0; i < table.length; i++) {
            long ticks = residency.get(firstActive + i);
            totalTicks += ticks;
            weighted += (double) ticks * table[i];
        }
        if (totalTicks == Normal._0) {
            return observedTicks == Normal._0 ? Normal._0 : table[Normal._0];
        }
        return (long) (weighted / totalTicks);
    }

    /**
     * Collects realized CPU complex states by core type rank.
     *
     * @param complexStates the complex states keyed by channel name
     * @return the summed state residency keyed by CPU core type rank
     */
    public static Map<Integer, Map<String, Long>> realizedComplexStates(Map<String, Map<String, Long>> complexStates) {
        Map<Integer, Map<String, Long>> byRank = new TreeMap<>();
        for (Map.Entry<String, Map<String, Long>> channel : complexStates.entrySet()) {
            Matcher matcher = REALIZED_COMPLEX_CHANNEL.matcher(channel.getKey());
            if (!matcher.matches()) {
                continue;
            }
            int letter = CHANNEL_PREFIX_ORDER.indexOf(matcher.group(2));
            int rank = letter < Normal._0 ? Normal._4 : letter;
            Map<String, Long> summed = byRank.get(rank);
            if (summed == null) {
                byRank.put(rank, ticksByState(channel.getValue()));
            } else if (summed.keySet().equals(channel.getValue().keySet())) {
                for (Map.Entry<String, Long> state : ticksByState(channel.getValue()).entrySet()) {
                    summed.merge(state.getKey(), state.getValue(), Long::sum);
                }
            } else {
                byRank.put(rank, Collections.emptyMap());
            }
        }
        return Collections.unmodifiableMap(byRank);
    }

    /**
     * Copies a channel's residency in state order.
     *
     * @param states the source states
     * @return the copied states
     */
    private static Map<String, Long> ticksByState(Map<String, Long> states) {
        Map<String, Long> ticksByState = new LinkedHashMap<>();
        for (Map.Entry<String, Long> state : states.entrySet()) {
            Long ticks = state.getValue();
            ticksByState.put(state.getKey(), ticks == null ? Normal._0 : ticks);
        }
        return ticksByState;
    }

    /**
     * Orders CPU core channel names in macOS logical core order.
     *
     * @param channelNames the channel names to order
     * @return the ordered channel names
     */
    public static List<String> orderChannels(Collection<String> channelNames) {
        List<String> ordered = new ArrayList<>(channelNames);
        Collections.sort(ordered, (left, right) -> {
            int bySortKey = Long.compare(sortKey(left), sortKey(right));
            return bySortKey == 0 ? left.compareTo(right) : bySortKey;
        });
        return ordered;
    }

    /**
     * Gets the core type rank from an IOReport channel name.
     *
     * @param channelName the channel name
     * @return the rank, or {@link Normal#_4}
     */
    public static int prefixRank(String channelName) {
        Matcher matcher = CPU_CHANNEL.matcher(channelName);
        if (matcher.matches()) {
            int rank = CHANNEL_PREFIX_ORDER.indexOf(matcher.group(2));
            if (rank >= Normal._0) {
                return rank;
            }
        }
        return Normal._4;
    }

    /**
     * Builds a sortable key for a channel name.
     *
     * @param channelName the channel name
     * @return the sort key
     */
    private static long sortKey(String channelName) {
        Matcher matcher = CPU_CHANNEL.matcher(channelName);
        if (!matcher.matches()) {
            return Long.MAX_VALUE;
        }
        int rank = CHANNEL_PREFIX_ORDER.indexOf(matcher.group(2));
        long die = Parsing.parseIntOrDefault(matcher.group(Normal._1), Normal._0);
        long core = Parsing.parseIntOrDefault(matcher.group(Normal._3), Normal._0);
        return ((long) (rank < Normal._0 ? Normal._4 : rank) << (Normal._2 * Normal._20)) | (die << Normal._20) | core;
    }

    /**
     * Aligns available items to efficiency classes at the highest-performing end.
     *
     * @param itemCount  the number of items available
     * @param classCount the number of efficiency classes
     * @return an item index for each efficiency class, or an empty array when no item exists
     */
    public static int[] alignAtTop(int itemCount, int classCount) {
        if (itemCount == Normal._0) {
            return Normal.EMPTY_INT_ARRAY;
        }
        int[] indices = new int[Math.max(classCount, Normal._1)];
        for (int i = Normal._0; i < indices.length; i++) {
            int index = itemCount - indices.length + i;
            indices[i] = Math.min(Math.max(index, Normal._0), itemCount - Normal._1);
        }
        return indices;
    }

}
