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
 * Metadata-based entry matching and filtering utility.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MetadataMatcher {

    /**
     * Creates a new MetadataMatcher.
     */
    private MetadataMatcher() {

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
     * Returns true if the metadata satisfies the given selector.
     *
     * @param metadata metadata map to check
     * @param selector selector expression
     * @return true if matched
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
     * Returns true if the metadata satisfies all exact-label and advanced selector constraints.
     *
     * @param metadata  metadata map to check
     * @param labels    exact labels
     * @param selectors advanced selectors
     * @return true if matched
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
     * @return true if matched
     */
    public static boolean matches(Assets assets, Map<String, String> labels, List<Selector> selectors) {
        return assets != null && matches(assets.getLabels(), labels, selectors);
    }

    /**
     * Filters a list of entries by all given selectors.
     *
     * @param entries   entries to filter
     * @param selectors selectors to apply
     * @param <T>       asset type
     * @return filtered list
     */
    public static <T extends Assets> List<T> filter(List<T> entries, List<Selector> selectors) {
        if (selectors == null || selectors.isEmpty()) {
            return entries;
        }
        return entries.stream().filter(e -> selectors.stream().allMatch(s -> matchSelector(e, s)))
                .collect(Collectors.toList());
    }

    /**
     * Filters a list of entries by exact labels and advanced selectors.
     *
     * @param entries   entries to filter
     * @param labels    exact labels
     * @param selectors advanced selectors
     * @param <T>       asset type
     * @return filtered list
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
