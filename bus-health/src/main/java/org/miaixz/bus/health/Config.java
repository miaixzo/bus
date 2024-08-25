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
package org.miaixz.bus.health;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.NotThreadSafe;
import org.miaixz.bus.setting.metric.props.Props;

import java.util.Properties;

/**
 * The global configuration utility. See {@code src/main/resources/bus.health.properties} for default values. This class
 * is not thread safe if methods manipulating the configuration are used. These methods are intended for use by a single
 * thread at startup, before instantiation of any other classes. does not guarantee re- reading of any configuration
 * changes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@NotThreadSafe
public final class Config {

    /**
     * Global File
     */
    public static final String _HEALTH_PROPERTIES = "bus.health.linux.filename.properties";
    public static final String _ARCHITECTURE_PROPERTIES = "bus.health.architecture.properties";
    public static final String _LINUX_FILENAME_PROPERTIES = "bus.health.linux.filename.properties";
    public static final String _MACOS_VERSIONS_PROPERTIES = "bus.health.macos.version.properties";
    public static final String _VM_MAC_ADDR_PROPERTIES = "bus.health.vmmacaddr.properties";

    /**
     * Common
     */
    public static final String _UTIL_PROC_PATH = "bus.health.proc.path";
    public static final String _UTIL_SYS_PATH = "bus.health.sys.path";
    public static final String _UTIL_DEV_PATH = "bus.health.dev.path";
    public static final String _UTIL_WMI_TIMEOUT = "bus.health.wmi.timeout";
    public static final String _UTIL_MEMOIZER_EXPIRATION = "bus.health.memoizer.expiration";
    public static final String _PSEUDO_FILESYSTEM_TYPES = "bus.health.pseudo.filesystem.types";
    public static final String _NETWORK_FILESYSTEM_TYPES = "bus.health.network.filesystem.types";

    /**
     * Linux
     */
    public static final String _LINUX_ALLOWUDEV = "bus.health.linux.allowudev";
    public static final String _LINUX_PROCFS_LOGWARNING = "bus.health.linux.procfs.logwarning";
    public static final String _MAC_SYSCTL_LOGWARNING = "bus.health.mac.sysctl.logwarning";
    public static final String _LINUX_FS_PATH_EXCLUDES = "bus.health.linux.filesystem.path.excludes";
    public static final String _LINUX_FS_PATH_INCLUDES = "bus.health.linux.filesystem.path.includes";
    public static final String _LINUX_FS_VOLUME_EXCLUDES = "bus.health.linux.filesystem.volume.excludes";
    public static final String _LINUX_FS_VOLUME_INCLUDES = "bus.health.linux.filesystem.volume.includes";

    /**
     * Mac
     */
    public static final String _MAC_FS_PATH_EXCLUDES = "bus.health.mac.filesystem.path.excludes";
    public static final String _MAC_FS_PATH_INCLUDES = "bus.health.mac.filesystem.path.includes";
    public static final String _MAC_FS_VOLUME_EXCLUDES = "bus.health.mac.filesystem.volume.excludes";
    public static final String _MAC_FS_VOLUME_INCLUDES = "bus.health.mac.filesystem.volume.includes";

    /**
     * Unix
     */
    public static final String _UNIX_WHOCOMMAND = "bus.health.unix.whoCommand";

    /**
     * OpenBSD
     */
    public static final String _UNIX_OPENBSD_FS_PATH_EXCLUDES = "bus.health.unix.openbsd.filesystem.path.excludes";
    public static final String _UNIX_OPENBSD_FS_PATH_INCLUDES = "bus.health.unix.openbsd.filesystem.path.includes";
    public static final String _UNIX_OPENBSD_FS_VOLUME_EXCLUDES = "bus.health.unix.openbsd.filesystem.volume.excludes";
    public static final String _UNIX_OPENBSD_FS_VOLUME_INCLUDES = "bus.health.unix.openbsd.filesystem.volume.includes";

    /**
     * AIX
     */
    public static final String _UNIX_AIX_FS_PATH_EXCLUDES = "bus.health.unix.aix.filesystem.path.excludes";
    public static final String _UNIX_AIX_FS_PATH_INCLUDES = "bus.health.unix.aix.filesystem.path.includes";
    public static final String _UNIX_AIX_FS_VOLUME_EXCLUDES = "bus.health.unix.aix.filesystem.volume.excludes";
    public static final String _UNIX_AIX_FS_VOLUME_INCLUDES = "bus.health.unix.aix.filesystem.volume.includes";

    /**
     * Solaris
     */
    public static final String _UNIX_SOLARIS_ALLOWKSTAT2 = "bus.health.unix.solaris.allowKstat2";
    public static final String _UNIX_SOLARIS_FS_PATH_EXCLUDES = "bus.health.unix.solaris.filesystem.path.excludes";
    public static final String _UNIX_SOLARIS_FS_PATH_INCLUDES = "bus.health.unix.solaris.filesystem.path.includes";
    public static final String _UNIX_SOLARIS_FS_VOLUME_EXCLUDES = "bus.health.unix.solaris.filesystem.volume.excludes";
    public static final String _UNIX_SOLARIS_FS_VOLUME_INCLUDES = "bus.health.unix.solaris.filesystem.volume.includes";

    /**
     * FreeBSD
     */
    public static final String _UNIX_FREEBSD_FS_PATH_EXCLUDES = "bus.health.unix.freebsd.filesystem.path.excludes";
    public static final String _UNIX_FREEBSD_FS_PATH_INCLUDES = "bus.health.unix.freebsd.filesystem.path.includes";
    public static final String _UNIX_FREEBSD_FS_VOLUME_EXCLUDES = "bus.health.unix.freebsd.filesystem.volume.excludes";
    public static final String _UNIX_FREEBSD_FS_VOLUME_INCLUDES = "bus.health.unix.freebsd.filesystem.volume.includes";

    /**
     * Windows
     */
    public static final String _WINDOWS_EVENTLOG = "bus.health.windows.eventlog";
    public static final String _WINDOWS_PROCSTATE_SUSPENDED = "bus.health.windows.procstate.suspended";
    public static final String _WINDOWS_COMMANDLINE_BATCH = "bus.health.windows.commandline.batch";
    public static final String _WINDOWS_HKEYPERFDATA = "bus.health.windows.hkeyperfdata";
    public static final String _WINDOWS_LEGACY_SYSTEM_COUNTERS = "bus.health.windows.legacy.system.counters";
    public static final String _WINDOWS_LOADAVERAGE = "bus.health.windows.loadaverage";
    public static final String _WINDOWS_CPU_UTILITY = "bus.health.windows.cpu.utility";
    public static final String _WINDOWS_PERFDISK_DIABLED = "bus.health.windows.perfdisk.disabled";
    public static final String _WINDOWS_PERFOS_DIABLED = "bus.health.windows.perfos.disabled";
    public static final String _WINDOWS_PERFPROC_DIABLED = "bus.health.windows.perfproc.disabled";

    /**
     * default values
     */
    public static final Properties CONFIG = readProperties(_HEALTH_PROPERTIES);

    /**
     * Get the property associated with the given key.
     *
     * @param key The property key
     * @return The property value if it exists, or null otherwise
     */
    public static String get(String key) {
        return CONFIG.getProperty(key);
    }

    /**
     * Get the {@code String} property associated with the given key.
     *
     * @param key The property key
     * @param def The default value
     * @return The property value or the given default if not found
     */
    public static String get(String key, String def) {
        return CONFIG.getProperty(key, def);
    }

    /**
     * Get the {@code int} property associated with the given key.
     *
     * @param key The property key
     * @param def The default value
     * @return The property value or the given default if not found
     */
    public static int get(String key, int def) {
        String value = CONFIG.getProperty(key);
        return value == null ? def : Parsing.parseIntOrDefault(value, def);
    }

    /**
     * Get the {@code double} property associated with the given key.
     *
     * @param key The property key
     * @param def The default value
     * @return The property value or the given default if not found
     */
    public static double get(String key, double def) {
        String value = CONFIG.getProperty(key);
        return value == null ? def : Parsing.parseDoubleOrDefault(value, def);
    }

    /**
     * Get the {@code boolean} property associated with the given key.
     *
     * @param key The property key
     * @param def The default value
     * @return The property value or the given default if not found
     */
    public static boolean get(String key, boolean def) {
        String value = CONFIG.getProperty(key);
        return value == null ? def : Boolean.parseBoolean(value);
    }

    /**
     * Set the given property, overwriting any existing value. If the given value is {@code null}, the property is
     * removed.
     *
     * @param key The property key
     * @param val The new value
     */
    public static void set(String key, Object val) {
        if (val == null) {
            CONFIG.remove(key);
        } else {
            CONFIG.setProperty(key, val.toString());
        }
    }

    /**
     * Reset the given property to its default value.
     *
     * @param key The property key
     */
    public static void remove(String key) {
        CONFIG.remove(key);
    }

    /**
     * Clear the configuration.
     */
    public static void clear() {
        CONFIG.clear();
    }

    /**
     * Load the given {@link java.util.Properties} into the global configuration.
     *
     * @param properties The new properties
     */
    public static void load(Properties properties) {
        CONFIG.putAll(properties);
    }

    /**
     * Read a configuration file from the class path and return its properties
     *
     * @param fileName The filename
     * @return A {@link java.util.Properties} object containing the properties.
     */
    public static Properties readProperties(String fileName) {
        return new Props(Symbol.SLASH + Normal.META_INF + "/health/" + fileName);
    }

    /**
     * Indicates that a configuration value is invalid.
     */
    public static class PropertyException extends RuntimeException {

        private static final long serialVersionUID = -1L;

        /**
         * @param property The property name
         */
        public PropertyException(String property) {
            super("Invalid property: \"" + property + "\" = " + Config.get(property, null));
        }

        /**
         * @param property The property name
         * @param message  An exception message
         */
        public PropertyException(String property, String message) {
            super("Invalid property \"" + property + "\": " + message);
        }
    }

}
