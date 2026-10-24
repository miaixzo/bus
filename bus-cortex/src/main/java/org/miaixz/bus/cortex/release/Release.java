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
package org.miaixz.bus.cortex.release;

import jakarta.persistence.Lob;

import lombok.Getter;
import lombok.Setter;

import org.miaixz.bus.cortex.Nature;
import org.miaixz.bus.cortex.Type;

/**
 * Publication record independent from registry assets.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
public class Release extends Nature {

    /**
     * Human-readable release title.
     */
    private String title;

    /**
     * Release track key.
     */
    private String track;

    /**
     * Release version identifier.
     */
    private String version;

    /**
     * JSON metadata for release-specific details.
     */
    @Lob
    private String metadata;

    /**
     * Publication timestamp in epoch milliseconds.
     */
    private Long published;

    /**
     * Human-readable release description.
     */
    @Lob
    private String description;

    /**
     * Creates a version record with the Cortex VERSION type.
     */
    public Release() {
        setType(Type.VERSION.key());
    }

}
