/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.linux;

import java.io.File;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.health.Config;

/**
 * Provides constants for paths in the {@code /dev} filesystem on Linux. If the user desires to configure a custom
 * {@code /dev} path, it must be declared in the configuration file or updated in the {@link Config} class prior to
 * initializing this class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class DevPath {

    /**
     * The /dev filesystem location.
     */
    public static final String DEV = queryDevConfig() + "/";

    public static final String DISK_BY_UUID = DEV + "disk/by-uuid";
    public static final String DM = DEV + "dm";
    public static final String LOOP = DEV + "loop";
    public static final String MAPPER = DEV + "mapper/";
    public static final String RAM = DEV + "ram";

    private static String queryDevConfig() {
        String devPath = Config.get(Config._UTIL_DEV_PATH, "/dev");
        // Ensure prefix begins with path separator, but doesn't end with one
        devPath = '/' + devPath.replaceAll("/$|^/", Normal.EMPTY);
        if (!new File(devPath).exists()) {
            throw new NotFoundException(Config._UTIL_DEV_PATH, "The path does not exist");
        }
        return devPath;
    }

}
