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
package org.miaixz.bus.cortex.setting.profile;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.basic.entity.Tenant;

/**
 * Configuration profile directory entry.
 *
 * @author Kimi Liu
 */
@Getter
@Setter
@SuperBuilder
public class Profile extends Tenant {

    /**
     * Stable profile code.
     */
    private String code;
    /**
     * Display name.
     */
    private String name;
    /**
     * Display order.
     */
    private Integer sort;
    /**
     * Whether the profile is built in.
     */
    private Integer builtin;
    /**
     * Whether destructive changes are guarded.
     */
    private Integer guarded;
    /**
     * Approval policy stored as text.
     */
    private String policy;
    /**
     * Whether the creator may approve their own rollout.
     */
    private Integer self;
    /**
     * Optional profile description.
     */
    private String description;

    /**
     * Creates an empty profile.
     */
    public Profile() {
        // No initialization required.
    }
}
