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
package org.miaixz.bus.cortex.version;

import lombok.Getter;
import lombok.Setter;

/**
 * Artifact attached to one release version.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class ReleaseArtifact {

    /**
     * Creates an empty release artifact.
     */
    public ReleaseArtifact() {

    }

    /**
     * Artifact category, such as binary, manifest, schema, or documentation.
     */
    private String artifactType;
    /**
     * Cortex resource type represented by this artifact.
     */
    private String resourceType;
    /**
     * Identifier of the Cortex resource represented by this artifact.
     */
    private String resourceId;
    /**
     * Integrity checksum for the referenced payload.
     */
    private String checksum;
    /**
     * Storage reference or URL for the artifact payload.
     */
    private String payloadRef;

}
