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
package org.miaixz.bus.cortex.setting.app;

import java.io.Serial;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;

/**
 * Application directory entry.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class App extends Tenant {

    @Serial
    private static final long serialVersionUID = 2558900500001L;

    /**
     * Stable application code.
     */
    private String code;
    /**
     * Display name.
     */
    private String name;
    /**
     * Icon resource address or key.
     */
    private String icon;
    /**
     * Application type (e.g., INTERNAL, THIRD_PARTY, MICRO_APP).
     */
    private String type;
    /**
     * Display sort order.
     */
    private Integer sort;
    /**
     * Optional application description.
     */
    private String description;

    /**
     * Creates an empty application entry.
     */
    public App() {
        // No initialization required.
    }

}
