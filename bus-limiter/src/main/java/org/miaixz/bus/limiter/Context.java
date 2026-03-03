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
package org.miaixz.bus.limiter;

import org.miaixz.bus.core.lang.Normal;

import lombok.*;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

/**
 * Context configuration class for the limiter module. This class holds various settings and properties related to the
 * limiting functionality, such as hotspot cache duration, logging enablement, and user identifier provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Context {

    /**
     * Default duration for hotspot caching in seconds.
     */
    @Builder.Default
    private int seconds = 60;

    /**
     * Flag indicating whether logging is enabled for the limiter.
     */
    @Builder.Default
    private boolean logger = true;

    /**
     * The fully qualified class name of the user identifier provider. This provider is used to determine the current
     * user for limiting purposes.
     */
    @Builder.Default
    private String supplier = Normal.EMPTY;

    /**
     * Extension properties or additional configuration in a string format. This field can be used to store custom,
     * module-specific settings.
     */
    private String extension;

}
