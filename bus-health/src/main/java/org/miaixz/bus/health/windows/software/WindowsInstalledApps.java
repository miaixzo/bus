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
package org.miaixz.bus.health.windows.software;

import java.util.List;

import org.miaixz.bus.health.builtin.software.ApplicationInfo;
import org.miaixz.bus.health.windows.driver.registry.InstalledAppsData;

/**
 * The windows installed apps class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WindowsInstalledApps {

    /**
     * Creates a new WindowsInstalledApps instance.
     */
    private WindowsInstalledApps() {
        // No initialization required.
    }

    /**
     * Queries the installed apps.
     *
     * @return the query installed apps result
     */
    public static List<ApplicationInfo> queryInstalledApps() {
        return InstalledAppsData.queryInstalledApps();
    }

}
