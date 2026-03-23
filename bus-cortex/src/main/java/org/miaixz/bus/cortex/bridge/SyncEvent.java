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
package org.miaixz.bus.cortex.bridge;

import org.miaixz.bus.cortex.Assets;

import lombok.Getter;
import lombok.Setter;

/**
 * Sync event emitted when a registry change must be propagated to the gateway.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class SyncEvent {

    /**
     * Action type for a gateway sync event.
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    public enum SyncAction {
        /**
         * A new entry has been registered.
         */
        REGISTER,
        /**
         * An existing entry has been deregistered.
         */
        DEREGISTER,
        /**
         * An existing entry has been updated.
         */
        UPDATE
    }

    /**
     * The action to apply on the gateway side.
     */
    private SyncAction action;
    /**
     * The asset representation of the changed registry entry.
     */
    private Assets asset;
    /**
     * Namespace in which the change occurred.
     */
    private String namespace;
    /**
     * Monotonic sequence number for ordering events.
     */
    private long sequence;

}
