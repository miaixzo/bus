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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.cortex.Assets;

/**
 * Metadata-based entry routing and filtering utility.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MetadataRouter {

    /**
     * Creates a new MetadataRouter.
     */
    private MetadataRouter() {

    }

    /**
     * Returns true if the assets's metadata satisfies the given selector.
     *
     * @param assets   assets to check
     * @param selector label selector
     * @return true if matched
     */
    public static boolean matchSelector(Assets assets, Selector selector) {
        Map<String, String> metadata = assets.getLabels();
        if (metadata == null) {
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

}
