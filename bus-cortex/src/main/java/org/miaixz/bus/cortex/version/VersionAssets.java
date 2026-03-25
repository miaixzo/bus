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

import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Species;

import lombok.Getter;
import lombok.Setter;

/**
 * Versioned artifact definition.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class VersionAssets extends Assets {

    /** Semantic version string (e.g. "1.2.3"). */
    private String semver;
    /** Human-readable changelog for this version. */
    private String changelog;
    /** Artifact coordinates or download URL for this version. */
    private String artifact;
    /** Current release state of this version. */
    private VersionStatus versionStatus;

    /**
     * Creates a VersionAssets with the default species set to VERSION.
     */
    public VersionAssets() {
        setSpecies(Species.VERSION);
    }

}
