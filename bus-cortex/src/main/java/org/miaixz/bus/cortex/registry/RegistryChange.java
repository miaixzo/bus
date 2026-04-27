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

import java.util.Map;

import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Change;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Type;

import lombok.Getter;
import lombok.Setter;

/**
 * Post-commit change notification emitted after a registry mutation has been applied.
 *
 * @param <T> changed asset type
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class RegistryChange<T extends Assets> implements Change<T> {

    /**
     * Registry mutation kinds that can produce a change notification.
     */
    public enum Action {
        /**
         * One entry was registered.
         */
        REGISTER,
        /**
         * One entry was updated.
         */
        UPDATE,
        /**
         * One entry was deregistered.
         */
        DEREGISTER
    }

    /**
     * Creates an empty registry change event.
     */
    public RegistryChange() {
    }

    /**
     * Identifier of the affected asset.
     */
    private String id;
    /**
     * Mutation action represented by this event.
     */
    private Action action;
    /**
     * Asset type affected by the mutation.
     */
    private Type type;
    /**
     * Namespace containing the affected asset.
     */
    private String namespace_id;
    /**
     * Application identifier containing the affected asset.
     */
    private String app_id;
    /**
     * Method or operation name of the affected asset, when applicable.
     */
    private String method;
    /**
     * Version label of the affected asset, when applicable.
     */
    private String version;
    /**
     * Runtime instance fingerprint associated with the change, when applicable.
     */
    private String fingerprint;
    /**
     * Optional upstream source identifier attached during forwarding.
     */
    private String source;
    /**
     * Logical event type used by downstream bridge and audit pipelines.
     */
    private String eventType;
    /**
     * Optional error message attached to failed or partially applied changes.
     */
    private String errorMessage;
    /**
     * Normalized asset snapshot after the mutation.
     */
    private T asset;
    /**
     * Asset snapshot before the mutation.
     */
    private T previousAsset;
    /**
     * Runtime instance snapshot associated with API registration changes.
     */
    private Instance instance;
    /**
     * Runtime instance snapshot before the mutation.
     */
    private Instance previousInstance;
    /**
     * Lightweight diff payload describing the concrete change set.
     */
    private Map<String, Object> changeSet;
    /**
     * Ordering sequence assigned to this registry event.
     */
    private long sequence;
    /**
     * Creation time of this registry event in epoch milliseconds.
     */
    private long timestamp;

}
