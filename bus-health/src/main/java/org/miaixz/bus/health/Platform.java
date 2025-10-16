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
 * The main entry point for system information, providing platform-specific implementations of {@link OperatingSystem}
 * and {@link HardwareAbstractionLayer}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Platform {

    /**
     * Singleton instance.
     */
    public static final Platform INSTANCE = new Platform();

    /**
     * Error message for unsupported operating systems.
     */
    private static final String NOT_SUPPORTED = "Operating system not supported: ";

    /**
     * The current operating system platform, represented by the OS enum, initialized via JNA's Platform.getOSType().
     */
    private static final OS CURRENT_PLATFORM = OS.getValue(getOSType());

    /**
     * Caches the OperatingSystem instance using Memoizer to avoid repeated creation and improve performance.
     */
    private final Supplier<OperatingSystem> os = Memoizer.memoize(this::createOperatingSystem);

    /**
     * Caches the HardwareAbstractionLayer instance using Memoizer to avoid repeated creation and improve performance.
     */
    private final Supplier<HardwareAbstractionLayer> hardware = Memoizer.memoize(this::createHardware);

    /**
     * Private constructor to prevent external instantiation.
     */
    private Platform() {
    }

    /**
     * Gets the current operating system type.
     *
     * @return The JNA platform type constant.
     */
    public static int getOSType() {
        return com.sun.jna.Platform.getOSType();
    }

    /**
     * Checks if the operating system is macOS.
     *
     * @return {@code true} if the operating system is macOS, {@code false} otherwise.
     */
    public static boolean isMac() {
        return com.sun.jna.Platform.isMac();
    }

    /**
     * Checks if the operating system is Android.
     *
     * @return {@code true} if the operating system is Android, {@code false} otherwise.
     */
    public static boolean isAndroid() {
        return com.sun.jna.Platform.isAndroid();
    }

    /**
     * Checks if the operating system is Linux.
     *
     * @return {@code true} if the operating system is Linux, {@code false} otherwise.
     */
    public static boolean isLinux() {
        return com.sun.jna.Platform.isLinux();
    }

    /**
     * Checks if the operating system is AIX.
     *
     * @return {@code true} if the operating system is AIX, {@code false} otherwise.
     */
    public static boolean isAIX() {
        return com.sun.jna.Platform.isAIX();
    }

    /**
     * Checks if the operating system is Windows CE.
     *
     * @return {@code true} if the operating system is Windows CE, {@code false} otherwise.
     */
    public static boolean isWindowsCE() {
        return com.sun.jna.Platform.isWindowsCE();
    }

    /**
     * Checks if the operating system is Windows.
     *
     * @return {@code true} if the operating system is Windows, {@code false} otherwise.
     */
    public static boolean isWindows() {
        return com.sun.jna.Platform.isWindows();
    }

    /**
     * Checks if the operating system is Solaris.
     *
     * @return {@code true} if the operating system is Solaris, {@code false} otherwise.
     */
    public static boolean isSolaris() {
        return com.sun.jna.Platform.isSolaris();
    }

    /**
     * Checks if the operating system is FreeBSD.
     *
     * @return {@code true} if the operating system is FreeBSD, {@code false} otherwise.
     */
    public static boolean isFreeBSD() {
        return com.sun.jna.Platform.isFreeBSD();
    }

    /**
     * Checks if the operating system is OpenBSD.
     *
     * @return {@code true} if the operating system is OpenBSD, {@code false} otherwise.
     */
    public static boolean isOpenBSD() {
        return com.sun.jna.Platform.isOpenBSD();
    }

    /**
     * Checks if the operating system is NetBSD.
     *
     * @return {@code true} if the operating system is NetBSD, {@code false} otherwise.
     */
    public static boolean isNetBSD() {
        return com.sun.jna.Platform.isNetBSD();
    }

    /**
     * Checks if the operating system is GNU.
     *
     * @return {@code true} if the operating system is GNU, {@code false} otherwise.
     */
    public static boolean isGNU() {
        return com.sun.jna.Platform.isGNU();
    }

    /**
     * Checks if the operating system is kFreeBSD.
     *
     * @return {@code true} if the operating system is kFreeBSD, {@code false} otherwise.
     */
    public static boolean isKFreeBSD() {
        return com.sun.jna.Platform.iskFreeBSD();
    }

    /**
     * Checks if X11 is supported.
     *
     * @return {@code true} if X11 is supported, {@code false} otherwise.
     */
    public static boolean isX11() {
        return com.sun.jna.Platform.isX11();
    }

    /**
     * Checks if runtime execution is supported.
     *
     * @return {@code true} if runtime execution is supported, {@code false} otherwise.
     */
    public static boolean hasRuntimeExec() {
        return com.sun.jna.Platform.hasRuntimeExec();
    }

    /**
     * Checks if the platform is 64-bit.
     *
     * @return {@code true} if the platform is 64-bit, {@code false} otherwise.
     */
    public static boolean is64Bit() {
        return com.sun.jna.Platform.is64Bit();
    }

    /**
     * Checks if the CPU is Intel.
     *
     * @return {@code true} if the CPU is Intel, {@code false} otherwise.
     */
    public static boolean isIntel() {
        return com.sun.jna.Platform.isIntel();
    }

    /**
     * Checks if the CPU is PowerPC.
     *
     * @return {@code true} if the CPU is PowerPC, {@code false} otherwise.
     */
    public static boolean isPPC() {
        return com.sun.jna.Platform.isPPC();
    }

    /**
     * Checks if the CPU is ARM.
     *
     * @return {@code true} if the CPU is ARM, {@code false} otherwise.
     */
    public static boolean isARM() {
        return com.sun.jna.Platform.isARM();
    }

    /**
     * Checks if the CPU is SPARC.
     *
     * @return {@code true} if the CPU is SPARC, {@code false} otherwise.
     */
    public static boolean isSPARC() {
        return com.sun.jna.Platform.isSPARC();
    }

    /**
     * Checks if the CPU is MIPS.
     *
     * @return {@code true} if the CPU is MIPS, {@code false} otherwise.
     */
    public static boolean isMIPS() {
        return com.sun.jna.Platform.isMIPS();
    }

    /**
     * Gets a system property, returning a default value on failure.
     *
     * @param name         The name of the property.
     * @param defaultValue The default value.
     * @return The property value or the default value.
     */
    public static String get(String name, String defaultValue) {
        return ObjectKit.defaultIfNull(get(name, false), defaultValue);
    }

    /**
     * Gets a system property, returning null on failure.
     *
     * @param name  The name of the property.
     * @param quiet Whether to suppress errors.
     * @return The property value or null.
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
     * Gets a system property, returning null on failure.
     *
     * @param key The key of the property.
     * @return The property value or null.
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * Gets a boolean system property.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value.
     * @return The boolean value.
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
     * Gets an integer system property.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value.
     * @return The integer value.
     */
    public static int getInt(String key, int defaultValue) {
        return Convert.toInt(get(key), defaultValue);
    }

    /**
     * Gets a long system property.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value.
     * @return The long value.
     */
    public static long getLong(String key, long defaultValue) {
        return Convert.toLong(get(key), defaultValue);
    }

    /**
     * Gets all system properties.
     *
     * @return The list of system properties.
     */
    public static Properties props() {
        return Keys.getProps();
    }

    /**
     * Gets the current process PID.
     *
     * @return The process ID.
     */
    public static long getCurrentPID() {
        return Long.parseLong(getRuntimeMXBean().getName().split(Symbol.AT)[0]);
    }

    /**
     * Gets the class loading system properties.
     *
     * @return The ClassLoadingMXBean.
     */
    public static ClassLoadingMXBean getClassLoadingMXBean() {
        return ManagementFactory.getClassLoadingMXBean();
    }

    /**
     * Gets the memory system properties.
     *
     * @return The MemoryMXBean.
     */
    public static MemoryMXBean getMemoryMXBean() {
        return ManagementFactory.getMemoryMXBean();
    }

    /**
     * Gets the thread system properties.
     *
     * @return The ThreadMXBean.
     */
    public static ThreadMXBean getThreadMXBean() {
        return ManagementFactory.getThreadMXBean();
    }

    /**
     * Gets the runtime system properties.
     *
     * @return The RuntimeMXBean.
     */
    public static RuntimeMXBean getRuntimeMXBean() {
        return ManagementFactory.getRuntimeMXBean();
    }

    /**
     * Gets the compilation system properties, which may be null.
     *
     * @return The CompilationMXBean or null.
     */
    public static CompilationMXBean getCompilationMXBean() {
        return ManagementFactory.getCompilationMXBean();
    }

    /**
     * Gets the operating system related properties.
     *
     * @return The OperatingSystemMXBean.
     */
    public static OperatingSystemMXBean getOperatingSystemMXBean() {
        return ManagementFactory.getOperatingSystemMXBean();
    }

    /**
     * Gets the list of memory pools.
     *
     * @return The list of MemoryPoolMXBeans.
     */
    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
        return ManagementFactory.getMemoryPoolMXBeans();
    }

    /**
     * Gets the list of memory managers.
     *
     * @return The list of MemoryManagerMXBeans.
     */
    public static List<MemoryManagerMXBean> getMemoryManagerMXBeans() {
        return ManagementFactory.getMemoryManagerMXBeans();
    }

    /**
     * Gets the list of garbage collectors.
     *
     * @return The list of GarbageCollectorMXBeans.
     */
    public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
        return ManagementFactory.getGarbageCollectorMXBeans();
    }

    /**
     * Gets the OS enum value for the current platform.
     *
     * @return The current platform.
     */
    public static OS getCurrentPlatform() {
        return CURRENT_PLATFORM;
    }

    /**
     * Generates a native library resource prefix based on the current operating system.
     *
     * @return The path prefix.
     */
    public static String getNativeLibraryResourcePrefix() {
        return getNativeLibraryResourcePrefix(
                getOSType(),
                System.getProperty("os.arch"),
                System.getProperty("os.name"));
    }

    /**
     * Generates a native library resource prefix based on the operating system type, architecture, and name.
     *
     * @param osType The operating system type (from JNA Platform.getOSType()).
     * @param arch   The system architecture (from os.arch).
     * @param name   The system name (from os.name).
     * @return The path prefix.
     */
    public static String getNativeLibraryResourcePrefix(int osType, String arch, String name) {
        // Normalize architecture name
        arch = arch.toLowerCase().trim();
        arch = switch (arch) {
            case "powerpc" -> "ppc";
            case "powerpc64" -> "ppc64";
            case "i386" -> "x86";
            case "x86_64", "amd64" -> "x86_64";
            default -> arch;
        };

        // Generate prefix based on OS type
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
     * Creates a platform-specific OperatingSystem instance.
     *
     * @return The OperatingSystem instance.
     * @throws UnsupportedOperationException if the platform is not supported.
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
     * Creates a platform-specific HardwareAbstractionLayer instance.
     *
     * @return The HardwareAbstractionLayer instance.
     * @throws UnsupportedOperationException if the platform is not supported.
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
     * Gets the platform-specific OperatingSystem instance.
     *
     * @return The OperatingSystem instance.
     */
    public OperatingSystem getOperatingSystem() {
        return os.get();
    }

    /**
     * Gets the platform-specific HardwareAbstractionLayer instance.
     *
     * @return The HardwareAbstractionLayer instance.
     */
    public HardwareAbstractionLayer getHardware() {
        return hardware.get();
    }

    /**
     * Enum of supported operating systems, consistent with JNA platform type constants.
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
         * Gets the OS enum value from the JNA platform type.
         *
         * @param osType The value from JNA Platform.getOSType().
         * @return The corresponding OS enum value.
         */
        public static OS getValue(int osType) {
            if (osType < 0 || osType >= UNKNOWN.ordinal()) {
                return UNKNOWN;
            }
            return values()[osType];
        }

        /**
         * Gets the platform name from the JNA platform type.
         *
         * @param osType The value from JNA Platform.getOSType().
         * @return The platform name.
         */
        public static String getName(int osType) {
            return getValue(osType).getName();
        }

        /**
         * Gets the operating system platform name.
         *
         * @return The platform name.
         */
        public String getName() {
            return name;
        }
    }

}
