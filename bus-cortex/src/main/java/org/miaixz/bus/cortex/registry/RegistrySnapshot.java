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

import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Type;

import lombok.Getter;
import lombok.Setter;

/**
 * Snapshot payload used by vortex full and incremental synchronization.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class RegistrySnapshot {

    /**
     * Creates an empty registry snapshot.
     */
    public RegistrySnapshot() {

    }

    /**
     * Namespace included in this snapshot.
     */
    private String namespace_id;
    /**
     * Type scopes included in this snapshot.
     */
    private List<Type> types = List.of();
    /**
     * Lower modified watermark used for incremental queries.
     */
    private long since;
    /**
     * Upper modified watermark observed while building the snapshot.
     */
    private long watermark;
    /**
     * Snapshot identifier used by bridge and export flows.
     */
    private String snapshotId;
    /**
     * Snapshot checksum used for integrity validation.
     */
    private String checksum;
    /**
     * Snapshot source marker.
     */
    private String source;
    /**
     * Human-readable summary of the snapshot contents.
     */
    private String summary;
    /**
     * Snapshot creation timestamp.
     */
    private long timestamp;
    /**
     * Assets carried by this snapshot.
     */
    private List<Assets> assets = List.of();

}
