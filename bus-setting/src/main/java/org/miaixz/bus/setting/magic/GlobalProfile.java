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
package org.miaixz.bus.setting.magic;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.setting.Setting;

/**
 * A global center for managing {@link Profile} configurations. It provides static access to a singleton {@code Profile}
 * instance, allowing different parts of an application to retrieve profile-specific settings.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GlobalProfile {

    /**
     * Private constructor to prevent instantiation.
     */
    private GlobalProfile() {
    }

    /**
     * Sets the active global profile and returns the corresponding {@link Profile} instance. If an instance for this
     * profile name already exists, it is returned; otherwise, a new one is created and cached.
     *
     * @param profile The name of the environment profile (e.g., "dev", "prod").
     * @return The singleton {@link Profile} instance for the given profile name.
     */
    public static Profile setProfile(final String profile) {
        return Instances.get(Profile.class, profile);
    }

    /**
     * Gets a {@link Setting} instance for the specified configuration file name under the currently active global
     * profile.
     *
     * @param settingName The name of the configuration file (e.g., "db.setting").
     * @return The {@link Setting} instance.
     */
    public static Setting getSetting(final String settingName) {
        return Instances.get(Profile.class).getSetting(settingName);
    }

}
