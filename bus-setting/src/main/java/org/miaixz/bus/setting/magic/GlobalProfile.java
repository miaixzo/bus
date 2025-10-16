/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
