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
package org.miaixz.bus.cortex.magic.runtime;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Diagnostics snapshot for one runtime component.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class DiagnosticsSnapshot {

    /**
     * Creates an empty diagnostics snapshot.
     */
    public DiagnosticsSnapshot() {
    }

    /**
     * Runtime component name.
     */
    private String component;
    /**
     * Runtime component status.
     */
    private String status;
    /**
     * Component-specific diagnostic metrics.
     */
    private Map<String, Object> metrics;
    /**
     * Last observed error message.
     */
    private String lastError;
    /**
     * Snapshot update timestamp in epoch milliseconds.
     */
    private long updatedAt;

}
