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

import java.lang.management.*;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.health.builtin.hardware.HardwareAbstractionLayer;
import org.miaixz.bus.health.builtin.software.OperatingSystem;
import org.miaixz.bus.health.linux.hardware.LinuxHardwareAbstractionLayer;
import org.miaixz.bus.health.linux.software.LinuxOperatingSystem;
import org.miaixz.bus.health.mac.hardware.MacHardwareAbstractionLayer;
import org.miaixz.bus.health.mac.software.MacOperatingSystem;
import org.miaixz.bus.health.unix.platform.aix.hardware.AixHardwareAbstractionLayer;
import org.miaixz.bus.health.unix.platform.aix.software.AixOperatingSystem;
import org.miaixz.bus.health.unix.platform.freebsd.hardware.FreeBsdHardwareAbstractionLayer;
import org.miaixz.bus.health.unix.platform.freebsd.software.FreeBsdOperatingSystem;
import org.miaixz.bus.health.unix.platform.openbsd.hardware.OpenBsdHardwareAbstractionLayer;
import org.miaixz.bus.health.unix.platform.openbsd.software.OpenBsdOperatingSystem;
import org.miaixz.bus.health.unix.platform.solaris.hardware.SolarisHardwareAbstractionLayer;
import org.miaixz.bus.health.unix.platform.solaris.software.SolarisOperatingSystem;
import org.miaixz.bus.health.windows.hardware.WindowsHardwareAbstractionLayer;
import org.miaixz.bus.health.windows.software.WindowsOperatingSystem;

/**
 * 系统信息主入口，提供特定平台的 {@link OperatingSystem} 和 {@link HardwareAbstractionLayer} 实现。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Platform {

    /**
     * 单例实例
     */
    public static final Platform INSTANCE = new Platform();

    /**
     * 不支持的操作系统错误提示消息
     */
    private static final String NOT_SUPPORTED = "Operating system not supported: ";

    /**
     * 当前操作系统平台，使用 OS 枚举表示，通过 JNA Platform.getOSType() 初始化
     */
    private static final OS CURRENT_PLATFORM = OS.getValue(getOSType());

    /**
     * 使用 Memoizer 缓存 OperatingSystem 实例，避免重复创建，提高性能
     */
    private final Supplier<OperatingSystem> os = Memoizer.memoize(this::createOperatingSystem);

    /**
     * 使用 Memoizer 缓存 HardwareAbstractionLayer 实例，避免重复创建，提高性能
     */
    private final Supplier<HardwareAbstractionLayer> hardware = Memoizer.memoize(this::createHardware);

    /**
     * 私有构造函数，防止外部实例化
     */
    private Platform() {
    }

    /**
     * 获取当前操作系统类型
     *
     * @return JNA 平台类型常量
     */
    public static int getOSType() {
        return com.sun.jna.Platform.getOSType();
    }

    /**
     * 判断是否为 macOS
     *
     * @return true 如果是 macOS
     */
    public static boolean isMac() {
        return com.sun.jna.Platform.isMac();
    }

    /**
     * 判断是否为 Android
     *
     * @return true 如果是 Android
     */
    public static boolean isAndroid() {
        return com.sun.jna.Platform.isAndroid();
    }

    /**
     * 判断是否为 Linux
     *
     * @return true 如果是 Linux
     */
    public static boolean isLinux() {
        return com.sun.jna.Platform.isLinux();
    }

    /**
     * 判断是否为 AIX
     *
     * @return true 如果是 AIX
     */
    public static boolean isAIX() {
        return com.sun.jna.Platform.isAIX();
    }

    /**
     * 判断是否为 Windows CE
     *
     * @return true 如果是 Windows CE
     */
    public static boolean isWindowsCE() {
        return com.sun.jna.Platform.isWindowsCE();
    }

    /**
     * 判断是否为 Windows
     *
     * @return true 如果是 Windows
     */
    public static boolean isWindows() {
        return com.sun.jna.Platform.isWindows();
    }

    /**
     * 判断是否为 Solaris
     *
     * @return true 如果是 Solaris
     */
    public static boolean isSolaris() {
        return com.sun.jna.Platform.isSolaris();
    }

    /**
     * 判断是否为 FreeBSD
     *
     * @return true 如果是 FreeBSD
     */
    public static boolean isFreeBSD() {
        return com.sun.jna.Platform.isFreeBSD();
    }

    /**
     * 判断是否为 OpenBSD
     *
     * @return true 如果是 OpenBSD
     */
    public static boolean isOpenBSD() {
        return com.sun.jna.Platform.isOpenBSD();
    }

    /**
     * 判断是否为 NetBSD
     *
     * @return true 如果是 NetBSD
     */
    public static boolean isNetBSD() {
        return com.sun.jna.Platform.isNetBSD();
    }

    /**
     * 判断是否为 GNU
     *
     * @return true 如果是 GNU
     */
    public static boolean isGNU() {
        return com.sun.jna.Platform.isGNU();
    }

    /**
     * 判断是否为 kFreeBSD
     *
     * @return true 如果是 kFreeBSD
     */
    public static boolean isKFreeBSD() {
        return com.sun.jna.Platform.iskFreeBSD();
    }

    /**
     * 判断是否支持 X11
     *
     * @return true 如果支持 X11
     */
    public static boolean isX11() {
        return com.sun.jna.Platform.isX11();
    }

    /**
     * 判断是否支持运行时执行
     *
     * @return true 如果支持
     */
    public static boolean hasRuntimeExec() {
        return com.sun.jna.Platform.hasRuntimeExec();
    }

    /**
     * 判断是否为 64 位平台
     *
     * @return true 如果是 64 位
     */
    public static boolean is64Bit() {
        return com.sun.jna.Platform.is64Bit();
    }

    /**
     * 判断 CPU 是否为 Intel
     *
     * @return true 如果是 Intel
     */
    public static boolean isIntel() {
        return com.sun.jna.Platform.isIntel();
    }

    /**
     * 判断 CPU 是否为 PowerPC
     *
     * @return true 如果是 PowerPC
     */
    public static boolean isPPC() {
        return com.sun.jna.Platform.isPPC();
    }

    /**
     * 判断 CPU 是否为 ARM
     *
     * @return true 如果是 ARM
     */
    public static boolean isARM() {
        return com.sun.jna.Platform.isARM();
    }

    /**
     * 判断 CPU 是否为 SPARC
     *
     * @return true 如果是 SPARC
     */
    public static boolean isSPARC() {
        return com.sun.jna.Platform.isSPARC();
    }

    /**
     * 判断 CPU 是否为 MIPS
     *
     * @return true 如果是 MIPS
     */
    public static boolean isMIPS() {
        return com.sun.jna.Platform.isMIPS();
    }

    /**
     * 获取系统属性，失败时返回默认值。
     *
     * @param name         属性名
     * @param defaultValue 默认值
     * @return 属性值或默认值
     */
    public static String get(String name, String defaultValue) {
        return ObjectKit.defaultIfNull(get(name, false), defaultValue);
    }

    /**
     * 样属性，失败时返回 null。
     *
     * @param name  属性名
     * @param quiet 是否静默模式（不打印错误）
     * @return 属性值或 null
     */
    public static String get(String name, boolean quiet) {
        try {
            return System.getProperty(name);
        } catch (SecurityException e) {
            if (!quiet) {
                throw new InternalException("Failed to retrieve system property: " + name + " {}", e.getMessage());
            }
            return null;
        }
    }

    /**
     * 获取系统属性，失败时返回 null。
     *
     * @param key 键
     * @return 属性值或 null
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * 获取布尔类型系统属性
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 布尔值
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        value = value.trim().toLowerCase();
        if (value.isEmpty()) {
            return true;
        }
        return switch (value) {
        case "true", "yes", Symbol.ONE -> true;
        case "false", "no", Symbol.ZERO -> false;
        default -> defaultValue;
        };
    }

    /**
     * 获取整型系统属性
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 整型值
     */
    public static int getInt(String key, int defaultValue) {
        return Convert.toInt(get(key), defaultValue);
    }

    /**
     * 获取长整型系统属性
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 长整型值
     */
    public static long getLong(String key, long defaultValue) {
        return Convert.toLong(get(key), defaultValue);
    }

    /**
     * 获取所有系统属性
     *
     * @return 系样属性列表
     */
    public static Properties props() {
        return Keys.getProps();
    }

    /**
     * 获取当前进程 PID
     *
     * @return 进程 ID
     */
    public static long getCurrentPID() {
        return Long.parseLong(getRuntimeMXBean().getName().split(Symbol.AT)[0]);
    }

    /**
     * 获取类加载系统属性
     *
     * @return 类加载 MXBean
     */
    public static ClassLoadingMXBean getClassLoadingMXBean() {
        return ManagementFactory.getClassLoadingMXBean();
    }

    /**
     * 获取内存系统属性
     *
     * @return 内存 MXBean
     */
    public static MemoryMXBean getMemoryMXBean() {
        return ManagementFactory.getMemoryMXBean();
    }

    /**
     * 获取线程系统属性
     *
     * @return 线程 MXBean
     */
    public static ThreadMXBean getThreadMXBean() {
        return ManagementFactory.getThreadMXBean();
    }

    /**
     * 获取运行时系统属性
     *
     * @return 运行时 MXBean
     */
    public static RuntimeMXBean getRuntimeMXBean() {
        return ManagementFactory.getRuntimeMXBean();
    }

    /**
     * 获取编译系统属性，可能返回 null。
     *
     * @return 编译 MXBean 或 null
     */
    public static CompilationMXBean getCompilationMXBean() {
        return ManagementFactory.getCompilationMXBean();
    }

    /**
     * 获取操作系统相关属性
     *
     * @return 操作系统 MXBean
     */
    public static OperatingSystemMXBean getOperatingSystemMXBean() {
        return ManagementFactory.getOperatingSystemMXBean();
    }

    /**
     * 获取内存池列表
     *
     * @return 内存池 MXBean 列表
     */
    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
        return ManagementFactory.getMemoryPoolMXBeans();
    }

    /**
     * 获取内存管理器列表
     *
     * @return 内存管理器 MXBean 列表
     */
    public static List<MemoryManagerMXBean> getMemoryManagerMXBeans() {
        return ManagementFactory.getMemoryManagerMXBeans();
    }

    /**
     * 获取垃圾回收器列表
     *
     * @return 垃圾回收器 MXBean 列表
     */
    public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
        return ManagementFactory.getGarbageCollectorMXBeans();
    }

    /**
     * 获取当前平台的 OS 枚举值
     *
     * @return 当前平台
     */
    public static OS getCurrentPlatform() {
        return CURRENT_PLATFORM;
    }

    /**
     * 根据当前操作系统生成本地库资源路径前缀
     *
     * @return 路径前缀
     */
    public static String getNativeLibraryResourcePrefix() {
        return getNativeLibraryResourcePrefix(getOSType(), System.getProperty("os.arch"),
                System.getProperty("os.name"));
    }

    /**
     * 根据操作系统类型、架构和名称生成本地库资源路径前缀
     *
     * @param osType 操作系统类型（JNA Platform.getOSType()）
     * @param arch   系统架构（os.arch）
     * @param name   系统名称（os.name）
     * @return 路径前缀
     */
    public static String getNativeLibraryResourcePrefix(int osType, String arch, String name) {
        // 规范化架构名称
        arch = arch.toLowerCase().trim();
        arch = switch (arch) {
        case "powerpc" -> "ppc";
        case "powerpc64" -> "ppc64";
        case "i386" -> "x86";
        case "x86_64", "amd64" -> "x86_64";
        default -> arch;
        };

        // 根据操作系统类型生成前缀
        switch (osType) {
        case com.sun.jna.Platform.ANDROID:
            return "android-" + (arch.startsWith("arm") ? "arm" : arch);
        case com.sun.jna.Platform.WINDOWS:
            return "win32-" + arch;
        case com.sun.jna.Platform.WINDOWSCE:
            return "w32ce-" + arch;
        case com.sun.jna.Platform.MAC:
            return "macos-" + arch;
        case com.sun.jna.Platform.LINUX:
            return "linux-" + arch;
        case com.sun.jna.Platform.SOLARIS:
            return "sunos-" + arch;
        case com.sun.jna.Platform.FREEBSD:
            return "freebsd-" + arch;
        case com.sun.jna.Platform.OPENBSD:
            return "openbsd-" + arch;
        case com.sun.jna.Platform.NETBSD:
            return "netbsd-" + arch;
        case com.sun.jna.Platform.KFREEBSD:
            return "kfreebsd-" + arch;
        case com.sun.jna.Platform.AIX:
            return "aix-" + arch;
        default:
            String osPrefix = name.toLowerCase().split(Symbol.SPACE)[0];
            return osPrefix + Symbol.MINUS + arch;
        }
    }

    /**
     * 创建特定平台的操作系统实例
     *
     * @return 操作系统实例
     * @throws UnsupportedOperationException 如果平台不受支持
     */
    private OperatingSystem createOperatingSystem() {
        switch (CURRENT_PLATFORM) {
        case WINDOWS:
            return new WindowsOperatingSystem();
        case LINUX:
        case ANDROID:
            return new LinuxOperatingSystem();
        case MACOS:
            return new MacOperatingSystem();
        case SOLARIS:
            return new SolarisOperatingSystem();
        case FREEBSD:
            return new FreeBsdOperatingSystem();
        case AIX:
            return new AixOperatingSystem();
        case OPENBSD:
            return new OpenBsdOperatingSystem();
        default:
            throw new UnsupportedOperationException(NOT_SUPPORTED + CURRENT_PLATFORM.getName());
        }
    }

    /**
     * 创建特定平台的硬件抽象层实例
     *
     * @return 硬件抽象层实例
     * @throws UnsupportedOperationException 如果平台不受支持
     */
    private HardwareAbstractionLayer createHardware() {
        switch (CURRENT_PLATFORM) {
        case WINDOWS:
            return new WindowsHardwareAbstractionLayer();
        case LINUX:
        case ANDROID:
            return new LinuxHardwareAbstractionLayer();
        case MACOS:
            return new MacHardwareAbstractionLayer();
        case SOLARIS:
            return new SolarisHardwareAbstractionLayer();
        case FREEBSD:
            return new FreeBsdHardwareAbstractionLayer();
        case AIX:
            return new AixHardwareAbstractionLayer();
        case OPENBSD:
            return new OpenBsdHardwareAbstractionLayer();
        default:
            throw new UnsupportedOperationException(NOT_SUPPORTED + CURRENT_PLATFORM.getName());
        }
    }

    /**
     * 获取特定平台的操作系统实例
     *
     * @return 操作系统实例
     */
    public OperatingSystem getOperatingSystem() {
        return os.get();
    }

    /**
     * 获取特定平台的硬件抽象层实例
     *
     * @return 硬件抽象层实例
     */
    public HardwareAbstractionLayer getHardware() {
        return hardware.get();
    }

    /**
     * 支持的操作系统枚举，与 JNA 平台类型常量顺序一致。
     */
    public enum OS {

        /**
         * macOS
         */
        MACOS("macOS"),
        /**
         * A flavor of Linux
         */
        LINUX("Linux"),
        /**
         * Microsoft Windows
         */
        WINDOWS("Windows"),
        /**
         * Solaris (SunOS)
         */
        SOLARIS("Solaris"),
        /**
         * FreeBSD
         */
        FREEBSD("FreeBSD"),
        /**
         * OpenBSD
         */
        OPENBSD("OpenBSD"),
        /**
         * Windows Embedded Compact
         */
        WINDOWSCE("Windows CE"),
        /**
         * IBM AIX
         */
        AIX("AIX"),
        /**
         * Android
         */
        ANDROID("Android"),
        /**
         * GNU operating system
         */
        GNU("GNU"),
        /**
         * Debian GNU/kFreeBSD
         */
        KFREEBSD("kFreeBSD"),
        /**
         * NetBSD
         */
        NETBSD("NetBSD"),
        /**
         * An unspecified system
         */
        UNKNOWN("Unknown");

        private final String name;

        OS(String name) {
            this.name = name;
        }

        /**
         * 根据 JNA 平台类型获取 OS 枚举值
         *
         * @param osType JNA Platform.getOSType() 返回值
         * @return 对应的 OS 枚举值
         */
        public static OS getValue(int osType) {
            if (osType < 0 || osType >= UNKNOWN.ordinal()) {
                return UNKNOWN;
            }
            return values()[osType];
        }

        /**
         * 根据 JNA 平台类型获取平台名称
         *
         * @param osType JNA Platform.getOSType() 返回值
         * @return 平台名称
         */
        public static String getName(int osType) {
            return getValue(osType).getName();
        }

        /**
         * 获取操作系统平台名称。
         *
         * @return 平台名称
         */
        public String getName() {
            return name;
        }
    }

}