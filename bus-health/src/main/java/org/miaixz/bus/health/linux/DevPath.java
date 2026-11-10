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
 * Provides constants for paths in the {@code /dev} filesystem on Linux.
 * <p>
 * {@link #DEV} is where device files are read and may point to a mounted host directory. {@link #NODE_ROOT} is the
 * device-name root reported by the kernel, udev, {@code /proc/mounts}, and tools such as {@code pvs}. Returned device
 * names always use {@link #NODE_ROOT}; {@link #toFilePath(String)} converts them to paths below {@link #DEV}.
 * <p>
 * A custom {@code /dev} path must be configured through {@link Builder} before this class is initialized.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class DevPath {

    /**
     * The configured device filesystem location used for file access.
     */
    public static final String DEV = queryDevConfig() + Symbol.SLASH;

    /**
     * The root of device names reported by the kernel and udev.
     */
    public static final String NODE_ROOT = Symbol.SLASH + "dev" + Symbol.SLASH;

    /**
     * The disk-by-UUID directory below {@link #DEV}.
     */
    public static final String DISK_BY_UUID = DEV + "disk/by-uuid";

    /**
     * The device-mapper device-name prefix.
     */
    public static final String DM = NODE_ROOT + "dm";

    /**
     * The loop-device name prefix.
     */
    public static final String LOOP = NODE_ROOT + "loop";

    /**
     * The mapped device-name prefix.
     */
    public static final String MAPPER = NODE_ROOT + "mapper/";

    /**
     * The RAM-disk device-name prefix.
     */
    public static final String RAM = NODE_ROOT + "ram";

    /**
     * Constructs a new DevPath instance.
     */
    public DevPath() {
        // No initialization required.
    }

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

    /**
     * Converts a device name below {@link #NODE_ROOT} to the configured file path below {@link #DEV}.
     *
     * @param nodeName the device name
     * @return the configured file path, or the input when it is not below {@link #NODE_ROOT}
     */
    public static String toFilePath(String nodeName) {
        return reroot(nodeName, NODE_ROOT, DEV);
    }

    /**
     * Converts a configured device file path to the corresponding name below {@link #NODE_ROOT}.
     *
     * @param filePath the configured file path
     * @return the device name, or the input when it is not below {@link #DEV}
     */
    public static String toNodeName(String filePath) {
        return reroot(filePath, DEV, NODE_ROOT);
    }

    /**
     * Replaces one path root with another.
     *
     * @param path the path to convert
     * @param from the source root
     * @param to   the target root
     * @return the converted path, or the input when it is not below the source root
     */
    static String reroot(String path, String from, String to) {
        return path.startsWith(from) ? to + path.substring(from.length()) : path;
    }

}
