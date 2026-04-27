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

import java.util.List;

import org.miaixz.bus.cortex.Nature;
import org.miaixz.bus.cortex.Type;

import lombok.Getter;
import lombok.Setter;

/**
 * Version-domain release record independent from registry assets.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class VersionRecord extends Nature {

    /**
     * Release version identifier.
     */
    private String version;
    /**
     * Release track key.
     */
    private String track;
    /**
     * Lifecycle status of this release version.
     */
    private VersionStatus versionStatus;
    /**
     * Human-readable release title.
     */
    private String title;
    /**
     * Human-readable release description.
     */
    private String description;
    /**
     * Artifacts attached to this release.
     */
    private List<ReleaseArtifact> artifacts;
    /**
     * JSON metadata for release-specific details.
     */
    private String metadata;
    /**
     * Creation timestamp in epoch milliseconds.
     */
    private Long created;
    /**
     * Publication timestamp in epoch milliseconds.
     */
    private Long published;

    /**
     * Creates a version record with the Cortex VERSION type.
     */
    public VersionRecord() {
        setType(Type.VERSION.key());
    }

}
