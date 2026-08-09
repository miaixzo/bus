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
package org.miaixz.bus.health.linux;

import java.io.File;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.health.Builder;

/**
 * Provides constants for paths in the {@code /dev} filesystem on Linux. If the user desires to configure a custom
 * {@code /dev} path, it must be declared in the configuration file or updated in the {@link Builder} class prior to
 * initializing this class.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public final class DevPath {

    /**
     * Constructs a new DevPath instance.
     */
    public DevPath() {
        // No initialization required.
    }

    /**
     * The /dev filesystem location.
     */
    public static final String DEV = queryDevConfig() + Symbol.SLASH;

    /**
     * The DISK_BY_UUID constant.
     */
    public static final String DISK_BY_UUID = DEV + "disk/by-uuid";

    /**
     * The DM constant.
     */
    public static final String DM = DEV + "dm";

    /**
     * The LOOP constant.
     */
    public static final String LOOP = DEV + "loop";

    /**
     * The MAPPER constant.
     */
    public static final String MAPPER = DEV + "mapper/";

    /**
     * The RAM constant.
     */
    public static final String RAM = DEV + "ram";

    /**
     * Queries the dev config.
     *
     * @return the query dev config result
     */
    private static String queryDevConfig() {
        String devPath = Builder.get(Builder._DEV_PATH, Symbol.SLASH + "dev");
        // Ensure prefix begins with path separator, but doesn't end with one
        devPath = Symbol.C_SLASH + devPath.replaceAll("/$|^/", Normal.EMPTY);
        if (!new File(devPath).exists()) {
            throw new NotFoundException(Builder._DEV_PATH, "The path does not exist");
        }
        return devPath;
    }

}
