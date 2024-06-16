/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.linux;

import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.health.Config;

import java.io.File;

/**
 * Provides constants for paths in the {@code /dev} filesystem on Linux.
 * If the user desires to configure a custom {@code /dev} path, it must be declared in the configuration file or
 * updated in the {@link Config} class prior to initializing this class.
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
            throw new Config.PropertyException(Config._UTIL_DEV_PATH, "The path does not exist");
        }
        return devPath;
    }

}
