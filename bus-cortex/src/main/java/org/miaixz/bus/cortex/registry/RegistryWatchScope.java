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
package org.miaixz.bus.cortex.registry;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Internal registry watch scope with watch-only controls separated from query criteria.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class RegistryWatchScope {

    /**
     * Creates an empty registry watch scope.
     */
    public RegistryWatchScope() {

    }

    /**
     * Registry query criteria for the watch subscription.
     */
    private RegistryQuery query;
    /**
     * Event types accepted by the subscription.
     */
    private List<String> eventTypes;
    /**
     * Watch source identity.
     */
    private String source;
    /**
     * Caller-supplied request identifier for tracing.
     */
    private String requestId;
    /**
     * Maximum pending event count for the subscription.
     */
    private int maxPending;

}
