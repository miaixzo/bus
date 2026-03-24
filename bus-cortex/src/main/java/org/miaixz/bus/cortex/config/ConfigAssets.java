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
package org.miaixz.bus.cortex.config;

import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Species;

import lombok.Getter;
import lombok.Setter;

/**
 * Persisted configuration assets.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class ConfigAssets extends Assets {

    /**
     * Configuration group to which this entry belongs.
     */
    private String group;
    /**
     * Configuration data identifier within the group.
     */
    private String dataId;
    /**
     * Current configuration content.
     */
    private String content;
    /**
     * Current published version number.
     */
    private long configVersion;
    /**
     * Operator responsible for the latest change.
     */
    private String operator;
    /**
     * Optional gray-release rule associated with this config entry.
     */
    private GrayRule grayRule;

    /**
     * Creates a config assets with type preset to {@link Species#CONFIG}.
     */
    public ConfigAssets() {
        setSpecies(Species.CONFIG);
    }

}
