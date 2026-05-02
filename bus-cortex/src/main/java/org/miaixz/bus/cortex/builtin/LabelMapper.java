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

/**
 * Converts between runtime label maps and management-facing label items.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class LabelMapper {

    /**
     * Creates a new mapper.
     */
    private LabelMapper() {

    }

    /**
     * Converts runtime labels into management-facing label items.
     *
     * @param labels runtime labels
     * @return label items preserving encounter order
     */
    public static List<Label> toLabels(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            return List.of();
        }
        List<Label> result = new ArrayList<>(labels.size());
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            Label label = new Label();
            label.setKey(entry.getKey());
            label.setValue(entry.getValue());
            result.add(label);
        }
        return result;
    }

    /**
     * Converts management-facing label items into runtime labels.
     *
     * @param labels label items
     * @return runtime label map preserving encounter order
     */
    public static Map<String, String> toMap(List<Label> labels) {
        if (labels == null || labels.isEmpty()) {
            return null;
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (Label label : labels) {
            if (label == null || label.getKey() == null || label.getKey().isBlank()) {
                continue;
            }
            result.put(label.getKey(), label.getValue());
        }
        return result.isEmpty() ? null : result;
    }

}
