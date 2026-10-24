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
package org.miaixz.bus.cortex.builtin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.cortex.Assets;

/**
 * Matches and filters entries by metadata.
 *
 * @author Kimi Liu
 */
public class MetadataMatcher {

    /**
     * Creates a new MetadataMatcher.
     */
    public MetadataMatcher() {
        // No initialization required.
    }

    /**
     * Returns the effective selector list after merging exact labels and advanced selectors.
     *
     * @param labels    exact labels
     * @param selectors advanced selectors
     * @return merged selector list
     */
    public static List<Selector> selectors(Map<String, String> labels, List<Selector> selectors) {
        List<Selector> merged = new ArrayList<>();
        if (labels != null && !labels.isEmpty()) {
            merged.addAll(Selector.ofExactLabels(new LinkedHashMap<>(labels)));
        }
        if (selectors != null && !selectors.isEmpty()) {
            merged.addAll(selectors);
        }
        return merged;
    }

    /**
     * Returns true if the assets's metadata satisfies the given selector.
     *
     * @param assets   assets to check
     * @param selector label selector
     * @return true if matched
     */
    public static boolean matchSelector(Assets assets, Selector selector) {
        return assets != null && matchSelector(assets.getLabels(), selector);
    }

    /**
     * Evaluates one comparison against a label map. EQ and NEQ compare the first configured value; IN and NOTIN test
     * membership. Null or empty comparison lists make EQ/IN false and NEQ/NOTIN true. A missing actual value makes IN
     * false and NOTIN true.
     *
     * @param metadata label map, or null
     * @param selector expression, or null; a non-null expression requires an operator and non-null first value for
     *                 nonempty EQ/NEQ comparisons
     * @return false for a null map or expression; otherwise the comparison result
     * @throws NullPointerException if an evaluated expression has no operator or has a null first EQ/NEQ value
     */
    public static boolean matchSelector(Map<String, String> metadata, Selector selector) {
        if (metadata == null || selector == null) {
            return false;
        }
        String actual = metadata.get(selector.getKey());
        List<String> values = selector.getValues();
        return switch (selector.getOp()) {
            case EQ -> values != null && !values.isEmpty() && values.get(0).equals(actual);
            case NEQ -> values == null || values.isEmpty() || !values.get(0).equals(actual);
            case IN -> actual != null && values != null && values.contains(actual);
            case NOTIN -> actual == null || values == null || !values.contains(actual);
        };
    }

    /**
     * Tests every effective constraint. An empty constraint set matches even a null label map; a nonempty set rejects
     * null or empty metadata before evaluating selectors.
     *
     * @param metadata  label map, or null
     * @param labels    exact constraints, or null
     * @param selectors advanced constraints, or null
     * @return true when there are no constraints, otherwise whether all constraints match
     * @throws NullPointerException if an evaluated selector violates its comparison preconditions
     */
    public static boolean matches(Map<String, String> metadata, Map<String, String> labels, List<Selector> selectors) {
        List<Selector> merged = selectors(labels, selectors);
        if (merged.isEmpty()) {
            return true;
        }
        if (metadata == null || metadata.isEmpty()) {
            return false;
        }
        return merged.stream().allMatch(selector -> matchSelector(metadata, selector));
    }

    /**
     * Returns true if the asset labels satisfy all exact-label and advanced selector constraints.
     *
     * @param assets    asset to check
     * @param labels    exact labels
     * @param selectors advanced selectors
     * @return false for a null asset; otherwise whether its labels satisfy all effective constraints
     */
    public static boolean matches(Assets assets, Map<String, String> labels, List<Selector> selectors) {
        return assets != null && matches(assets.getLabels(), labels, selectors);
    }

    /**
     * Filters entries against all selectors, preserving matching entries in encounter order. When no selectors are
     * supplied, returns the input list itself without copying or validating it.
     *
     * @param <T>       asset type
     * @param entries   entries to filter; may be null only when no selectors are supplied
     * @param selectors selector expressions, or null for no filtering
     * @return the original list, possibly null, when selectors are absent or empty; otherwise a new mutable filtered
     *         list
     * @throws NullPointerException if entries is null while filtering is required, or a selector violates its
     *                              comparison preconditions
     */
    public static <T extends Assets> List<T> filter(List<T> entries, List<Selector> selectors) {
        if (selectors == null || selectors.isEmpty()) {
            return entries;
        }
        return entries.stream().filter(e -> selectors.stream().allMatch(s -> matchSelector(e, s)))
                .collect(Collectors.toList());
    }

    /**
     * Filters entries by exact labels and advanced selectors. With no effective constraints, returns the original list.
     *
     * @param <T>       asset type
     * @param entries   entries to filter; may be null only when no constraints are supplied
     * @param labels    exact label constraints, or null
     * @param selectors advanced constraints, or null
     * @return the original list, possibly null, when no constraints exist; otherwise a new mutable filtered list
     * @throws NullPointerException if entries is null while filtering is required, or a selector violates its
     *                              comparison preconditions
     */
    public static <T extends Assets> List<T> filter(
            List<T> entries,
            Map<String, String> labels,
            List<Selector> selectors) {
        List<Selector> merged = selectors(labels, selectors);
        if (merged.isEmpty()) {
            return entries;
        }
        return entries.stream().filter(entry -> matches(entry, labels, selectors)).collect(Collectors.toList());
    }

}
