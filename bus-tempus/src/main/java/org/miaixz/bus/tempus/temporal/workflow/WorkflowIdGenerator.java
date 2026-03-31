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
package org.miaixz.bus.tempus.temporal.workflow;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;

/**
 * Strategy interface for generating Temporal workflow identifiers.
 * <p>
 * Implementations can enforce naming conventions or identifier composition rules for workflows.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface WorkflowIdGenerator {

    /**
     * Generates a workflow identifier with an optional prefix.
     * <p>
     * The default implementation uses the prefix followed by a colon and a generated object identifier. If the prefix
     * is {@code null} or blank, only the object identifier is returned.
     *
     * @param prefix the workflow identifier prefix (typically the workflow type name), may be {@code null}
     * @return the generated workflow identifier
     */
    default String workflowId(String prefix) {
        String normalizedPrefix = normalizeComponent(prefix);
        if (!StringKit.hasText(normalizedPrefix)) {
            return ID.objectId();
        }
        return normalizedPrefix + Symbol.C_COLON + ID.objectId();
    }

    /**
     * Generates a workflow identifier that can be deterministic when a stable key is provided.
     * <p>
     * When {@code stableKey} is present, the identifier is composed from the prefix and stable key ("prefix:stableKey")
     * to support idempotency use-cases.
     *
     * @param prefix    the workflow identifier prefix (typically the workflow type name), may be {@code null}
     * @param stableKey a stable key used to build a deterministic identifier, may be {@code null}
     * @return the workflow identifier
     */
    default String workflowId(String prefix, String stableKey) {
        String normalizedStableKey = normalizeComponent(stableKey);
        if (!StringKit.hasText(normalizedStableKey)) {
            return workflowId(prefix);
        }
        String normalizedPrefix = normalizeComponent(prefix);
        if (!StringKit.hasText(normalizedPrefix)) {
            return normalizedStableKey;
        }
        return normalizedPrefix + Symbol.C_COLON + normalizedStableKey;
    }

    /**
     * Normalizes a workflow identifier component into the restricted character set used by this framework.
     *
     * @param value the raw component value
     * @return the normalized component, or {@code null} when no usable content remains
     */
    private static String normalizeComponent(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("[^A-Za-z0-9._:-]+", "_");
        normalized = normalized.replaceAll("_+", "_");
        if (!StringKit.hasText(normalized)) {
            return null;
        }
        if (normalized.length() <= Normal._128) {
            return normalized;
        }
        return normalized.substring(0, 64) + "_" + Builder.sha256Hex(normalized).substring(0, 16);
    }

}
