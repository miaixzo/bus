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
package org.miaixz.bus.health;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.NotThreadSafe;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.setting.metric.props.Props;

/**
 * Global configuration utility class. Default values can be found in {@code META-INF/health/bus.health.properties}.
 * <p>
 * Configuration is resolved in the following precedence order: programmatic values set with
 * {@link #set(String, Object)}, Java system properties, {@code BUS_HEALTH_*} environment variables, an external
 * properties file, and then the classpath defaults.
 * <p>
 * This class is not thread-safe in a multi-threaded environment if methods that manipulate configuration are used.
 * These methods are intended to be used by a single thread at startup, before any other OSHI classes are instantiated.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@NotThreadSafe
public final class Config {

    /**
     * Constructs a new Config instance.
     */
    public Config() {
        // No initialization required.
    }

    /**
     * Global configuration file path, specifying health-related properties.
     */
    public static final String _HEALTH_PROPERTIES = "bus.health.properties";

    /**
     * System property for an external health configuration file.
     */
    public static final String _HEALTH_PROPERTIES_FILE = "bus.health.properties.file";

    /**
     * Environment variable for an external health configuration file.
     */
    public static final String _HEALTH_PROPERTIES_FILE_ENV = "BUS_HEALTH_PROPERTIES_FILE";

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
     * Linux configuration: whether to probe NFS servers before querying filesystem statistics.
     */
    public static final String _LINUX_FILESYSTEM_CHECKNFS = "bus.health.linux.filesystem.checknfs";

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
    public static final String _LINUX_THERMAL_ZONE_TYPE_PRIORITY = "bus.health.linux.sensors.cputemperature.types";

    /**
     * Linux configuration: priority of hardware monitor sensor names.
     */
    public static final String _LINUX_HWMON_NAME_PRIORITY = "bus.health.linux.sensors.hwmon.names";

    /**
     * Linux configuration: optional command prefix for privileged command execution.
     */
    public static final String _LINUX_PRIVILEGED_PREFIX = "bus.health.linux.privileged.prefix";

    /**
     * Linux configuration: comma-separated allowlist for commands eligible for privileged execution.
     */
    public static final String _LINUX_PRIVILEGED_ALLOWLIST = "bus.health.linux.privileged.allowlist";

    /**
     * Linux configuration: comma-separated allowlist for files eligible for privileged reads.
     */
    public static final String _LINUX_PRIVILEGED_FILE_ALLOWLIST = "bus.health.linux.privileged.file.allowlist";

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
    public static final String _UNIX_WHOCOMMAND = "bus.health.unix.whocommand";

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
    public static final String _UNIX_SOLARIS_ALLOWKSTAT2 = "bus.health.unix.solaris.allowkstat2";

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
    public static final String _WINDOWS_PERFDISK_DISABLED = "bus.health.windows.perfdisk.disabled";

    /**
     * Windows configuration: whether to disable performance OS counters.
     */
    public static final String _WINDOWS_PERFOS_DISABLED = "bus.health.windows.perfos.disabled";

    /**
     * Windows configuration: whether to disable performance process counters.
     */
    public static final String _WINDOWS_PERFPROC_DISABLED = "bus.health.windows.perfproc.disabled";

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
                Logger.debug(true, "Health", "Health configuration loading started: resource={}", _HEALTH_PROPERTIES);
                CONFIG = readProperties(_HEALTH_PROPERTIES);
                loadExternalConfig(CONFIG);
                Logger.info(
                        false,
                        "Health",
                        "Health configuration loaded: resource={}, propertyCount={}",
                        _HEALTH_PROPERTIES,
                        CONFIG.size());
            } catch (Exception e) {
                Logger.error(
                        false,
                        "Health",
                        "Health configuration load failed: resource={}, exception={}",
                        _HEALTH_PROPERTIES,
                        e.getClass().getSimpleName(),
                        e);
            }
        }
        return CONFIG;
    }

    /**
     * Loads external, environment, and system property overrides into the configuration.
     *
     * @param config The configuration properties to update.
     */
    static void loadExternalConfig(Properties config) {
        String externalFile = System.getProperty(_HEALTH_PROPERTIES_FILE);
        if (externalFile == null || externalFile.isEmpty()) {
            externalFile = System.getenv(_HEALTH_PROPERTIES_FILE_ENV);
        }
        if (externalFile != null && !externalFile.isEmpty()) {
            try (InputStream is = new FileInputStream(externalFile)) {
                config.load(is);
            } catch (FileNotFoundException e) {
                Logger.debug(false, "Health", "External health configuration file not found: path={}", externalFile);
            } catch (IOException e) {
                Logger.debug(
                        false,
                        "Health",
                        "External health configuration load failed: path={}, exception={}",
                        externalFile,
                        e.getClass().getSimpleName(),
                        e);
            }
        }
        System.getenv().forEach((key, value) -> {
            if (key.startsWith("BUS_HEALTH_") && !_HEALTH_PROPERTIES_FILE_ENV.equals(key)) {
                config.setProperty(envKeyToProperty(key), value);
            }
        });
        System.getProperties().forEach((key, value) -> {
            String propertyKey = key.toString();
            if (propertyKey.startsWith("bus.health.") && !_HEALTH_PROPERTIES_FILE.equals(propertyKey)) {
                config.setProperty(propertyKey, value.toString());
            }
        });
    }

    /**
     * Converts an environment variable name to a configuration property key.
     *
     * @param envKey The environment variable name.
     * @return The corresponding configuration property key.
     */
    static String envKeyToProperty(String envKey) {
        return envKey.toLowerCase(Locale.ROOT).replace('_', '.');
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
        Logger.debug(
                false,
                "Health",
                "Health configuration merged: propertyCount={}",
                properties == null ? 0 : properties.size());
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
