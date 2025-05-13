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
 * 全局配置工具类。默认值请参见 {@code META-INF/health/bus.health.properties}。
 * <p>
 * 使用 {@link System#setProperty(String, String)} 设置的 Java 系统属性将覆盖 {@code bus.health.properties} 文件中的值， 但随后可以通过
 * {@link #set(String, Object)} 或 {@link #remove(String)} 进行更改。
 * <p>
 * 如果使用操作配置的方法，此类在多线程环境下不是线程安全的。这些方法旨在在启动时由单一线程使用， 在实例化任何其他 OSHI 类之前。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@NotThreadSafe
public final class Config {

    /**
     * 全局配置文件路径，指定健康相关的属性文件
     */
    public static final String _HEALTH_PROPERTIES = "bus.health.properties";

    /**
     * 系统架构配置文件路径
     */
    public static final String _ARCHITECTURE_PROPERTIES = "bus.health.architecture.properties";

    /**
     * Linux 文件名配置文件路径
     */
    public static final String _LINUX_FILENAME_PROPERTIES = "bus.health.linux.filename.properties";

    /**
     * macOS 版本配置文件路径
     */
    public static final String _MACOS_VERSIONS_PROPERTIES = "bus.health.macos.version.properties";

    /**
     * 虚拟机 MAC 地址配置文件路径
     */
    public static final String _VM_MAC_ADDR_PROPERTIES = "bus.health.vmmacaddr.properties";

    /**
     * 通用配置：进程路径
     */
    public static final String _UTIL_PROC_PATH = "bus.health.proc.path";

    /**
     * 通用配置：系统路径
     */
    public static final String _UTIL_SYS_PATH = "bus.health.sys.path";

    /**
     * 通用配置：设备路径
     */
    public static final String _UTIL_DEV_PATH = "bus.health.dev.path";

    /**
     * 通用配置：WMI 超时时间
     */
    public static final String _UTIL_WMI_TIMEOUT = "bus.health.wmi.timeout";

    /**
     * 通用配置：记忆化器过期时间
     */
    public static final String _UTIL_MEMOIZER_EXPIRATION = "bus.health.memoizer.expiration";

    /**
     * 通用配置：伪文件系统类型
     */
    public static final String _PSEUDO_FILESYSTEM_TYPES = "bus.health.pseudo.filesystem.types";

    /**
     * 通用配置：网络文件系统类型
     */
    public static final String _NETWORK_FILESYSTEM_TYPES = "bus.health.network.filesystem.types";

    /**
     * Linux 配置：是否允许使用 udev
     */
    public static final String _LINUX_ALLOWUDEV = "bus.health.linux.allowudev";

    /**
     * Linux 配置：是否记录 procfs 警告日志
     */
    public static final String _LINUX_PROCFS_LOGWARNING = "bus.health.linux.procfs.logwarning";

    /**
     * Linux 配置：是否记录 mac sysctl 警告日志
     */
    public static final String _MAC_SYSCTL_LOGWARNING = "bus.health.mac.sysctl.logwarning";

    /**
     * Linux 配置：文件系统路径排除列表
     */
    public static final String _LINUX_FS_PATH_EXCLUDES = "bus.health.linux.filesystem.path.excludes";

    /**
     * Linux 配置：文件系统路径包含列表
     */
    public static final String _LINUX_FS_PATH_INCLUDES = "bus.health.linux.filesystem.path.includes";

    /**
     * Linux 配置：文件系统卷排除列表
     */
    public static final String _LINUX_FS_VOLUME_EXCLUDES = "bus.health.linux.filesystem.volume.excludes";

    /**
     * Linux 配置：文件系统卷包含列表
     */
    public static final String _LINUX_FS_VOLUME_INCLUDES = "bus.health.linux.filesystem.volume.includes";

    /**
     * Linux 配置：CPU 温度传感器类型的优先级
     */
    public static final String _LINUX_THERMAL_ZONE_TYPE_PRIORITY = "bus.health.linux.sensors.cpuTemperature.types";

    /**
     * macOS 配置：文件系统路径排除列表
     */
    public static final String _MAC_FS_PATH_EXCLUDES = "bus.health.mac.filesystem.path.excludes";

    /**
     * macOS 配置：文件系统路径包含列表
     */
    public static final String _MAC_FS_PATH_INCLUDES = "bus.health.mac.filesystem.path.includes";

    /**
     * macOS 配置：文件系统卷排除列表
     */
    public static final String _MAC_FS_VOLUME_EXCLUDES = "bus.health.mac.filesystem.volume.excludes";

    /**
     * macOS 配置：文件系统卷包含列表
     */
    public static final String _MAC_FS_VOLUME_INCLUDES = "bus.health.mac.filesystem.volume.includes";

    /**
     * Unix 配置：who 命令路径
     */
    public static final String _UNIX_WHOCOMMAND = "bus.health.unix.whoCommand";

    /**
     * OpenBSD 配置：文件系统路径排除列表
     */
    public static final String _UNIX_OPENBSD_FS_PATH_EXCLUDES = "bus.health.unix.openbsd.filesystem.path.excludes";

    /**
     * OpenBSD 配置：文件系统路径包含列表
     */
    public static final String _UNIX_OPENBSD_FS_PATH_INCLUDES = "bus.health.unix.openbsd.filesystem.path.includes";

    /**
     * OpenBSD 配置：文件系统卷排除列表
     */
    public static final String _UNIX_OPENBSD_FS_VOLUME_EXCLUDES = "bus.health.unix.openbsd.filesystem.volume.excludes";

    /**
     * OpenBSD 配置：文件系统卷包含列表
     */
    public static final String _UNIX_OPENBSD_FS_VOLUME_INCLUDES = "bus.health.unix.openbsd.filesystem.volume.includes";

    /**
     * AIX 配置：文件系统路径排除列表
     */
    public static final String _UNIX_AIX_FS_PATH_EXCLUDES = "bus.health.unix.aix.filesystem.path.excludes";

    /**
     * AIX 配置：文件系统路径包含列表
     */
    public static final String _UNIX_AIX_FS_PATH_INCLUDES = "bus.health.unix.aix.filesystem.path.includes";

    /**
     * AIX 配置：文件系统卷排除列表
     */
    public static final String _UNIX_AIX_FS_VOLUME_EXCLUDES = "bus.health.unix.aix.filesystem.volume.excludes";

    /**
     * AIX 配置：文件系统卷包含列表
     */
    public static final String _UNIX_AIX_FS_VOLUME_INCLUDES = "bus.health.unix.aix.filesystem.volume.includes";

    /**
     * Solaris 配置：是否允许使用 kstat2
     */
    public static final String _UNIX_SOLARIS_ALLOWKSTAT2 = "bus.health.unix.solaris.allowKstat2";

    /**
     * Solaris 配置：文件系统路径排除列表
     */
    public static final String _UNIX_SOLARIS_FS_PATH_EXCLUDES = "bus.health.unix.solaris.filesystem.path.excludes";

    /**
     * Solaris 配置：文件系统路径包含列表
     */
    public static final String _UNIX_SOLARIS_FS_PATH_INCLUDES = "bus.health.unix.solaris.filesystem.path.includes";

    /**
     * Solaris 配置：文件系统卷排除列表
     */
    public static final String _UNIX_SOLARIS_FS_VOLUME_EXCLUDES = "bus.health.unix.solaris.filesystem.volume.excludes";

    /**
     * Solaris 配置：文件系统卷包含列表
     */
    public static final String _UNIX_SOLARIS_FS_VOLUME_INCLUDES = "bus.health.unix.solaris.filesystem.volume.includes";

    /**
     * FreeBSD 配置：文件系统路径排除列表
     */
    public static final String _UNIX_FREEBSD_FS_PATH_EXCLUDES = "bus.health.unix.freebsd.filesystem.path.excludes";

    /**
     * FreeBSD 配置：文件系统路径包含列表
     */
    public static final String _UNIX_FREEBSD_FS_PATH_INCLUDES = "bus.health.unix.freebsd.filesystem.path.includes";

    /**
     * FreeBSD 配置：文件系统卷排除列表
     */
    public static final String _UNIX_FREEBSD_FS_VOLUME_EXCLUDES = "bus.health.unix.freebsd.filesystem.volume.excludes";

    /**
     * FreeBSD 配置：文件系统卷包含列表
     */
    public static final String _UNIX_FREEBSD_FS_VOLUME_INCLUDES = "bus.health.unix.freebsd.filesystem.volume.includes";

    /**
     * Windows 配置：事件日志设置
     */
    public static final String _WINDOWS_EVENTLOG = "bus.health.windows.eventlog";

    /**
     * Windows 配置：是否将进程状态视为挂起
     */
    public static final String _WINDOWS_PROCSTATE_SUSPENDED = "bus.health.windows.procstate.suspended";

    /**
     * Windows 配置：是否使用批处理命令行
     */
    public static final String _WINDOWS_COMMANDLINE_BATCH = "bus.health.windows.commandline.batch";

    /**
     * Windows 配置：HKEY 性能数据设置
     */
    public static final String _WINDOWS_HKEYPERFDATA = "bus.health.windows.hkeyperfdata";

    /**
     * Windows 配置：是否使用旧版系统计数器
     */
    public static final String _WINDOWS_LEGACY_SYSTEM_COUNTERS = "bus.health.windows.legacy.system.counters";

    /**
     * Windows 配置：是否启用负载平均值
     */
    public static final String _WINDOWS_LOADAVERAGE = "bus.health.windows.loadaverage";

    /**
     * Windows 配置：CPU 实用工具设置
     */
    public static final String _WINDOWS_CPU_UTILITY = "bus.health.windows.cpu.utility";

    /**
     * Windows 配置：是否禁用性能磁盘计数器
     */
    public static final String _WINDOWS_PERFDISK_DIABLED = "bus.health.windows.perfdisk.disabled";

    /**
     * Windows 配置：是否禁用性能操作系统计数器
     */
    public static final String _WINDOWS_PERFOS_DIABLED = "bus.health.windows.perfos.disabled";

    /**
     * Windows 配置：是否禁用性能进程计数器
     */
    public static final String _WINDOWS_PERFPROC_DIABLED = "bus.health.windows.perfproc.disabled";

    /**
     * Windows 配置：是否在性能计数器失败时禁用所有计数器
     */
    public static final String _WINDOWS_PERF_DISABLE_ALL_ON_FAILURE = "bus.health.windows.perf.disable.all.on.failure";

    /**
     * 配置属性，延迟初始化
     */
    private static Properties CONFIG;

    /**
     * 获取全局配置属性，延迟加载
     *
     * @return 配置属性
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
     * 获取与指定键关联的属性值。
     *
     * @param key 属性键
     * @return 如果属性存在，返回属性值；否则返回 null
     */
    public static String get(String key) {
        return getConfig().getProperty(key);
    }

    /**
     * 获取与指定键关联的字符串属性值。
     *
     * @param key 属性键
     * @param def 默认值
     * @return 属性值，如果未找到则返回默认值
     */
    public static String get(String key, String def) {
        return getConfig().getProperty(key, def);
    }

    /**
     * 获取与指定键关联的整数属性值。
     *
     * @param key 属性键
     * @param def 默认值
     * @return 属性值，如果未找到则返回默认值
     */
    public static int get(String key, int def) {
        String value = getConfig().getProperty(key);
        return value == null ? def : Parsing.parseIntOrDefault(value, def);
    }

    /**
     * 获取与指定键关联的双精度浮点数属性值。
     *
     * @param key 属性键
     * @param def 默认值
     * @return 属性值，如果未找到则返回默认值
     */
    public static double get(String key, double def) {
        String value = getConfig().getProperty(key);
        return value == null ? def : Parsing.parseDoubleOrDefault(value, def);
    }

    /**
     * 获取与指定键关联的布尔属性值。
     *
     * @param key 属性键
     * @param def 默认值
     * @return 属性值，如果未找到则返回默认值
     */
    public static boolean get(String key, boolean def) {
        String value = getConfig().getProperty(key);
        return value == null ? def : Boolean.parseBoolean(value);
    }

    /**
     * 设置指定属性，覆盖任何现有值。如果给定值为 {@code null}，则移除该属性。
     *
     * @param key 属性键
     * @param val 新值
     */
    public static void set(String key, Object val) {
        if (val == null) {
            getConfig().remove(key);
        } else {
            getConfig().setProperty(key, val.toString());
        }
    }

    /**
     * 将指定属性重置为其默认值。
     *
     * @param key 属性键
     */
    public static void remove(String key) {
        getConfig().remove(key);
    }

    /**
     * 清空配置。
     */
    public static void clear() {
        getConfig().clear();
    }

    /**
     * 将给定的 {@link java.util.Properties} 加载到全局配置中。
     *
     * @param properties 新属性
     */
    public static void load(Properties properties) {
        getConfig().putAll(properties);
    }

    /**
     * 从类路径读取配置文件并返回其属性。
     *
     * @param fileName 文件名
     * @return 包含属性的 {@link java.util.Properties} 对象
     */
    public static Properties readProperties(String fileName) {
        return new Props(Normal.META_INF + "/health/" + fileName);
    }

}