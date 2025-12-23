/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
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

import java.util.Properties;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.NotThreadSafe;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.setting.metric.props.Props;

/**
 * Global configuration utility class. Default values can be found in {@code META-INF/health/bus.health.properties}.
 * <p>
 * Java system properties set using {@link System#setProperty(String, String)} will override values in the
 * {@code bus.health.properties} file, but can subsequently be changed via {@link #set(String, Object)} or
 * {@link #remove(String)}.
 * <p>
 * This class is not thread-safe in a multi-threaded environment if methods that manipulate configuration are used.
 * These methods are intended to be used by a single thread at startup, before any other OSHI classes are instantiated.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@NotThreadSafe
public final class Config {

    /**
     * Global configuration file path, specifying health-related properties.
     */
    public static final String _HEALTH_PROPERTIES = "bus.health.properties";

    /**
     * System architecture configuration file path.
     */
    public static final String _ARCHITECTURE_PROPERTIES = "bus.health.architecture.properties";

    /**
     * Linux filename configuration file path.
     */
    public static final String _LINUX_FILENAME_PROPERTIES = "bus.health.linux.filename.properties";

    /**
     * macOS version configuration file path.
     */
    public static final String _MACOS_VERSIONS_PROPERTIES = "bus.health.macos.version.properties";

    /**
     * Virtual machine MAC address configuration file path.
     */
    public static final String _VM_MAC_ADDR_PROPERTIES = "bus.health.vmmacaddr.properties";

    /**
     * General configuration: process path.
     */
    public static final String _UTIL_PROC_PATH = "bus.health.proc.path";

    /**
     * General configuration: system path.
     */
    public static final String _UTIL_SYS_PATH = "bus.health.sys.path";

    /**
     * General configuration: device path.
     */
    public static final String _UTIL_DEV_PATH = "bus.health.dev.path";

    /**
     * General configuration: WMI timeout.
     */
    public static final String _UTIL_WMI_TIMEOUT = "bus.health.wmi.timeout";

    /**
     * General configuration: memoizer expiration time.
     */
    public static final String _UTIL_MEMOIZER_EXPIRATION = "bus.health.memoizer.expiration";

    /**
     * General configuration: pseudo filesystem types.
     */
    public static final String _PSEUDO_FILESYSTEM_TYPES = "bus.health.pseudo.filesystem.types";

    /**
     * General configuration: network filesystem types.
     */
    public static final String _NETWORK_FILESYSTEM_TYPES = "bus.health.network.filesystem.types";

    /**
     * Linux configuration: whether to allow udev.
     */
    public static final String _LINUX_ALLOWUDEV = "bus.health.linux.allowudev";

    /**
     * Configuration property key: Determines whether Systemd integration is allowed on Linux.
     */
    public static final String _LINUX_ALLOWSYSTEMD = "bus.health.linux.allowsystemd";

    /**
     * Linux configuration: whether to log procfs warnings.
     */
    public static final String _LINUX_PROCFS_LOGWARNING = "bus.health.linux.procfs.logwarning";

    /**
     * Linux configuration: whether to log mac sysctl warnings.
     */
    public static final String _MAC_SYSCTL_LOGWARNING = "bus.health.mac.sysctl.logwarning";

    /**
     * Linux configuration: filesystem path exclusion list.
     */
    public static final String _LINUX_FS_PATH_EXCLUDES = "bus.health.linux.filesystem.path.excludes";

    /**
     * Linux configuration: filesystem path inclusion list.
     */
    public static final String _LINUX_FS_PATH_INCLUDES = "bus.health.linux.filesystem.path.includes";

    /**
     * Linux configuration: filesystem volume exclusion list.
     */
    public static final String _LINUX_FS_VOLUME_EXCLUDES = "bus.health.linux.filesystem.volume.excludes";

    /**
     * Linux configuration: filesystem volume inclusion list.
     */
    public static final String _LINUX_FS_VOLUME_INCLUDES = "bus.health.linux.filesystem.volume.includes";

    /**
     * Linux configuration: priority of CPU temperature sensor types.
     */
    public static final String _LINUX_THERMAL_ZONE_TYPE_PRIORITY = "bus.health.linux.sensors.cpuTemperature.types";

    /**
     * macOS configuration: filesystem path exclusion list.
     */
    public static final String _MAC_FS_PATH_EXCLUDES = "bus.health.mac.filesystem.path.excludes";

    /**
     * macOS configuration: filesystem path inclusion list.
     */
    public static final String _MAC_FS_PATH_INCLUDES = "bus.health.mac.filesystem.path.includes";

    /**
     * macOS configuration: filesystem volume exclusion list.
     */
    public static final String _MAC_FS_VOLUME_EXCLUDES = "bus.health.mac.filesystem.volume.excludes";

    /**
     * macOS configuration: filesystem volume inclusion list.
     */
    public static final String _MAC_FS_VOLUME_INCLUDES = "bus.health.mac.filesystem.volume.includes";

    /**
     * Unix configuration: path to the 'who' command.
     */
    public static final String _UNIX_WHOCOMMAND = "bus.health.unix.whoCommand";

    /**
     * OpenBSD configuration: filesystem path exclusion list.
     */
    public static final String _UNIX_OPENBSD_FS_PATH_EXCLUDES = "bus.health.unix.openbsd.filesystem.path.excludes";

    /**
     * OpenBSD configuration: filesystem path inclusion list.
     */
    public static final String _UNIX_OPENBSD_FS_PATH_INCLUDES = "bus.health.unix.openbsd.filesystem.path.includes";

    /**
     * OpenBSD configuration: filesystem volume exclusion list.
     */
    public static final String _UNIX_OPENBSD_FS_VOLUME_EXCLUDES = "bus.health.unix.openbsd.filesystem.volume.excludes";

    /**
     * OpenBSD configuration: filesystem volume inclusion list.
     */
    public static final String _UNIX_OPENBSD_FS_VOLUME_INCLUDES = "bus.health.unix.openbsd.filesystem.volume.includes";

    /**
     * AIX configuration: filesystem path exclusion list.
     */
    public static final String _UNIX_AIX_FS_PATH_EXCLUDES = "bus.health.unix.aix.filesystem.path.excludes";

    /**
     * AIX configuration: filesystem path inclusion list.
     */
    public static final String _UNIX_AIX_FS_PATH_INCLUDES = "bus.health.unix.aix.filesystem.path.includes";

    /**
     * AIX configuration: filesystem volume exclusion list.
     */
    public static final String _UNIX_AIX_FS_VOLUME_EXCLUDES = "bus.health.unix.aix.filesystem.volume.excludes";

    /**
     * AIX configuration: filesystem volume inclusion list.
     */
    public static final String _UNIX_AIX_FS_VOLUME_INCLUDES = "bus.health.unix.aix.filesystem.volume.includes";

    /**
     * Solaris configuration: whether to allow kstat2.
     */
    public static final String _UNIX_SOLARIS_ALLOWKSTAT2 = "bus.health.unix.solaris.allowKstat2";

    /**
     * Solaris configuration: filesystem path exclusion list.
     */
    public static final String _UNIX_SOLARIS_FS_PATH_EXCLUDES = "bus.health.unix.solaris.filesystem.path.excludes";

    /**
     * Solaris configuration: filesystem path inclusion list.
     */
    public static final String _UNIX_SOLARIS_FS_PATH_INCLUDES = "bus.health.unix.solaris.filesystem.path.includes";

    /**
     * Solaris configuration: filesystem volume exclusion list.
     */
    public static final String _UNIX_SOLARIS_FS_VOLUME_EXCLUDES = "bus.health.unix.solaris.filesystem.volume.excludes";

    /**
     * Solaris configuration: filesystem volume inclusion list.
     */
    public static final String _UNIX_SOLARIS_FS_VOLUME_INCLUDES = "bus.health.unix.solaris.filesystem.volume.includes";

    /**
     * FreeBSD configuration: filesystem path exclusion list.
     */
    public static final String _UNIX_FREEBSD_FS_PATH_EXCLUDES = "bus.health.unix.freebsd.filesystem.path.excludes";

    /**
     * FreeBSD configuration: filesystem path inclusion list.
     */
    public static final String _UNIX_FREEBSD_FS_PATH_INCLUDES = "bus.health.unix.freebsd.filesystem.path.includes";

    /**
     * FreeBSD configuration: filesystem volume exclusion list.
     */
    public static final String _UNIX_FREEBSD_FS_VOLUME_EXCLUDES = "bus.health.unix.freebsd.filesystem.volume.excludes";

    /**
     * FreeBSD configuration: filesystem volume inclusion list.
     */
    public static final String _UNIX_FREEBSD_FS_VOLUME_INCLUDES = "bus.health.unix.freebsd.filesystem.volume.includes";

    /**
     * Windows configuration: event log settings.
     */
    public static final String _WINDOWS_EVENTLOG = "bus.health.windows.eventlog";

    /**
     * Windows configuration: whether to consider process state as suspended.
     */
    public static final String _WINDOWS_PROCSTATE_SUSPENDED = "bus.health.windows.procstate.suspended";

    /**
     * Windows configuration: whether to use batch command line.
     */
    public static final String _WINDOWS_COMMANDLINE_BATCH = "bus.health.windows.commandline.batch";

    /**
     * Windows configuration: HKEY performance data settings.
     */
    public static final String _WINDOWS_HKEYPERFDATA = "bus.health.windows.hkeyperfdata";

    /**
     * Windows configuration: whether to use legacy system counters.
     */
    public static final String _WINDOWS_LEGACY_SYSTEM_COUNTERS = "bus.health.windows.legacy.system.counters";

    /**
     * Windows configuration: whether to enable load average.
     */
    public static final String _WINDOWS_LOADAVERAGE = "bus.health.windows.loadaverage";

    /**
     * Windows configuration: CPU utility settings.
     */
    public static final String _WINDOWS_CPU_UTILITY = "bus.health.windows.cpu.utility";

    /**
     * Windows configuration: whether to disable performance disk counters.
     */
    public static final String _WINDOWS_PERFDISK_DIABLED = "bus.health.windows.perfdisk.disabled";

    /**
     * Windows configuration: whether to disable performance OS counters.
     */
    public static final String _WINDOWS_PERFOS_DIABLED = "bus.health.windows.perfos.disabled";

    /**
     * Windows configuration: whether to disable performance process counters.
     */
    public static final String _WINDOWS_PERFPROC_DIABLED = "bus.health.windows.perfproc.disabled";

    /**
     * Windows configuration: whether to disable all counters on performance counter failure.
     */
    public static final String _WINDOWS_PERF_DISABLE_ALL_ON_FAILURE = "bus.health.windows.perf.disable.all.on.failure";

    /**
     * Configuration properties, lazily initialized.
     */
    private static Properties CONFIG;

    /**
     * Retrieves the global configuration properties, lazily loaded.
     *
     * @return The configuration properties.
     */
    private static synchronized Properties getConfig() {
        if (CONFIG == null) {
            CONFIG = new Properties();
            try {
                CONFIG = readProperties(_HEALTH_PROPERTIES);
                Logger.info("Successfully loaded configuration from {}", _HEALTH_PROPERTIES);
            } catch (Exception e) {
                Logger.error("Failed to load configuration from {}: {}", _HEALTH_PROPERTIES, e.getMessage(), e);
            }
        }
        return CONFIG;
    }

    /**
     * Retrieves the property value associated with the specified key.
     *
     * @param key The property key.
     * @return The property value if it exists; otherwise, {@code null}.
     */
    public static String get(String key) {
        return getConfig().getProperty(key);
    }

    /**
     * Retrieves the string property value associated with the specified key.
     *
     * @param key The property key.
     * @param def The default value.
     * @return The property value, or the default value if not found.
     */
    public static String get(String key, String def) {
        return getConfig().getProperty(key, def);
    }

    /**
     * Retrieves the integer property value associated with the specified key.
     *
     * @param key The property key.
     * @param def The default value.
     * @return The property value, or the default value if not found.
     */
    public static int get(String key, int def) {
        String value = getConfig().getProperty(key);
        return value == null ? def : Parsing.parseIntOrDefault(value, def);
    }

    /**
     * Retrieves the double property value associated with the specified key.
     *
     * @param key The property key.
     * @param def The default value.
     * @return The property value, or the default value if not found.
     */
    public static double get(String key, double def) {
        String value = getConfig().getProperty(key);
        return value == null ? def : Parsing.parseDoubleOrDefault(value, def);
    }

    /**
     * Retrieves the boolean property value associated with the specified key.
     *
     * @param key The property key.
     * @param def The default value.
     * @return The property value, or the default value if not found.
     */
    public static boolean get(String key, boolean def) {
        String value = getConfig().getProperty(key);
        return value == null ? def : Boolean.parseBoolean(value);
    }

    /**
     * Sets the specified property, overriding any existing value. If the given value is {@code null}, the property is
     * removed.
     *
     * @param key The property key.
     * @param val The new value.
     */
    public static void set(String key, Object val) {
        if (val == null) {
            getConfig().remove(key);
        } else {
            getConfig().setProperty(key, val.toString());
        }
    }

    /**
     * Resets the specified property to its default value.
     *
     * @param key The property key.
     */
    public static void remove(String key) {
        getConfig().remove(key);
    }

    /**
     * Clears the configuration.
     */
    public static void clear() {
        getConfig().clear();
    }

    /**
     * Loads the given {@link java.util.Properties} into the global configuration.
     *
     * @param properties The new properties to load.
     */
    public static void load(Properties properties) {
        getConfig().putAll(properties);
    }

    /**
     * Reads a configuration file from the classpath and returns its properties.
     *
     * @param fileName The name of the file.
     * @return A {@link java.util.Properties} object containing the properties.
     */
    public static Properties readProperties(String fileName) {
        return new Props(Normal.META_INF + "/health/" + fileName);
    }

}
