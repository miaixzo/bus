/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.health;

import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.lang.Singleton;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.health.builtin.*;
import org.aoju.bus.health.builtin.hardware.HardwareAbstractionLayer;
import org.aoju.bus.health.builtin.software.NetworkParams;
import org.aoju.bus.health.builtin.software.OSUser;
import org.aoju.bus.health.builtin.software.OperatingSystem;
import org.aoju.bus.health.linux.hardware.LinuxHardwareAbstractionLayer;
import org.aoju.bus.health.linux.software.LinuxOperatingSystem;
import org.aoju.bus.health.mac.hardware.MacHardwareAbstractionLayer;
import org.aoju.bus.health.mac.software.MacOperatingSystem;
import org.aoju.bus.health.unix.freebsd.hardware.FreeBsdHardwareAbstractionLayer;
import org.aoju.bus.health.unix.freebsd.software.FreeBsdOperatingSystem;
import org.aoju.bus.health.unix.solaris.hardware.SolarisHardwareAbstractionLayer;
import org.aoju.bus.health.unix.solaris.software.SolarisOperatingSystem;
import org.aoju.bus.health.windows.hardware.WindowsHardwareAbstractionLayer;
import org.aoju.bus.health.windows.software.WindowsOperatingSystem;

import java.io.PrintWriter;
import java.lang.management.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * 操作系统信息支持
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class Platform {

    private static final OS OS_CURRENT_PLATFORM;

    static {
        if (com.sun.jna.Platform.isWindows()) {
            OS_CURRENT_PLATFORM = OS.WINDOWS;
        } else if (com.sun.jna.Platform.isLinux()) {
            OS_CURRENT_PLATFORM = OS.LINUX;
        } else if (com.sun.jna.Platform.isMac()) {
            OS_CURRENT_PLATFORM = OS.MACOSX;
        } else if (com.sun.jna.Platform.isSolaris()) {
            OS_CURRENT_PLATFORM = OS.SOLARIS;
        } else if (com.sun.jna.Platform.isFreeBSD()) {
            OS_CURRENT_PLATFORM = OS.FREEBSD;
        } else {
            OS_CURRENT_PLATFORM = OS.UNKNOWN;
        }
    }

    private final Supplier<OperatingSystem> os = Memoize.memoize(this::createOperatingSystem);
    private final Supplier<HardwareAbstractionLayer> hardware = Memoize.memoize(this::createHardware);

    /**
     * Getter for the field <code>currentPlatformEnum</code>.
     *
     * @return Returns the currentPlatformEnum.
     */
    public static Platform.OS getCurrentPlatform() {
        return OS_CURRENT_PLATFORM;
    }

    /**
     * <p>
     * Getter for the field <code>currentPlatformEnum</code>.
     * </p>
     *
     * @return Returns the currentPlatformEnum.
     */
    public static OS getCurrentOs() {
        return OS_CURRENT_PLATFORM;
    }

    public static int getOSType() {
        return com.sun.jna.Platform.getOSType();
    }

    public static boolean isMac() {
        return com.sun.jna.Platform.isMac();
    }

    public static boolean isAndroid() {
        return com.sun.jna.Platform.isAndroid();
    }

    public static boolean isLinux() {
        return com.sun.jna.Platform.isLinux();
    }

    public static boolean isAIX() {
        return com.sun.jna.Platform.isAIX();
    }

    public static boolean isWindowsCE() {
        return com.sun.jna.Platform.isWindowsCE();
    }

    public static boolean isWindows() {
        return com.sun.jna.Platform.isWindows();
    }

    public static boolean isSolaris() {
        return com.sun.jna.Platform.isSolaris();
    }

    public static boolean isFreeBSD() {
        return com.sun.jna.Platform.isFreeBSD();
    }

    public static boolean isOpenBSD() {
        return com.sun.jna.Platform.isOpenBSD();
    }

    public static boolean isNetBSD() {
        return com.sun.jna.Platform.isNetBSD();
    }

    public static boolean isGNU() {
        return com.sun.jna.Platform.isGNU();
    }

    public static boolean iskFreeBSD() {
        return com.sun.jna.Platform.iskFreeBSD();
    }

    public static boolean isX11() {
        return com.sun.jna.Platform.isX11();
    }

    public static boolean hasRuntimeExec() {
        return com.sun.jna.Platform.hasRuntimeExec();
    }

    public static boolean is64Bit() {
        return com.sun.jna.Platform.is64Bit();
    }

    public static boolean isIntel() {
        return com.sun.jna.Platform.isIntel();
    }

    public static boolean isPPC() {
        return com.sun.jna.Platform.isPPC();
    }

    public static boolean isARM() {
        return com.sun.jna.Platform.isARM();
    }

    public static boolean isSPARC() {
        return com.sun.jna.Platform.isSPARC();
    }

    public static boolean isMIPS() {
        return com.sun.jna.Platform.isMIPS();
    }

    /**
     * 根据当前操作系统类型/arch/名称生成一个规范的字符串前缀
     *
     * @return 路径前缀
     */
    public static String getNativeLibraryResourcePrefix() {
        return getNativeLibraryResourcePrefix(getOSType(), System.getProperty("os.arch"), System.getProperty("os.name"));
    }

    /**
     * 根据给定的操作系统类型/arch/名称生成一个规范的字符串前缀。
     *
     * @param osType 从 {@link #getOSType()} 获取
     * @param arch   从 <code>os.arch</code> 获取系统属性
     * @param name   从 <code>os.name</code> 获取系统属性
     * @return the path prefix
     */
    public static String getNativeLibraryResourcePrefix(int osType, String arch, String name) {
        String osPrefix;
        arch = arch.toLowerCase().trim();
        if ("powerpc".equals(arch)) {
            arch = "ppc";
        } else if ("powerpc64".equals(arch)) {
            arch = "ppc64";
        } else if ("i386".equals(arch)) {
            arch = "x86";
        } else if ("x86_64".equals(arch) || "amd64".equals(arch)) {
            arch = "x86-64";
        }
        switch (osType) {
            case com.sun.jna.Platform.ANDROID:
                if (arch.startsWith("arm")) {
                    arch = "arm";
                }
                osPrefix = "android-" + arch;
                break;
            case com.sun.jna.Platform.WINDOWS:
                osPrefix = "win32-" + arch;
                break;
            case com.sun.jna.Platform.WINDOWSCE:
                osPrefix = "w32ce-" + arch;
                break;
            case com.sun.jna.Platform.MAC:
                osPrefix = "darwin";
                break;
            case com.sun.jna.Platform.LINUX:
                osPrefix = "linux-" + arch;
                break;
            case com.sun.jna.Platform.SOLARIS:
                osPrefix = "sunos-" + arch;
                break;
            case com.sun.jna.Platform.FREEBSD:
                osPrefix = "freebsd-" + arch;
                break;
            case com.sun.jna.Platform.OPENBSD:
                osPrefix = "openbsd-" + arch;
                break;
            case com.sun.jna.Platform.NETBSD:
                osPrefix = "netbsd-" + arch;
                break;
            case com.sun.jna.Platform.KFREEBSD:
                osPrefix = "kfreebsd-" + arch;
                break;
            default:
                osPrefix = name.toLowerCase();
                int space = osPrefix.indexOf(Symbol.SPACE);
                if (space != -1) {
                    osPrefix = osPrefix.substring(0, space);
                }
                osPrefix += Symbol.HYPHEN + arch;
                break;
        }
        return osPrefix;
    }

    /**
     * 取得系统属性,如果因为Java安全的限制而失败,则将错误打在Log中,然后返回 <code>null</code>
     *
     * @param name         属性名
     * @param defaultValue 默认值
     * @return 属性值或<code>null</code>
     */
    public static String get(String name, String defaultValue) {
        return StringUtils.nullToDefault(get(name, false), defaultValue);
    }

    /**
     * 取得系统属性,如果因为Java安全的限制而失败,则将错误打在Log中,然后返回 <code>null</code>
     *
     * @param name  属性名
     * @param quiet 安静模式,不将出错信息打在<code>System.err</code>中
     * @return 属性值或<code>null</code>
     */
    public static String get(String name, boolean quiet) {
        try {
            return System.getProperty(name);
        } catch (SecurityException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获得System属性（调用System.getProperty）
     *
     * @param key 键
     * @return 属性值
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * 获得boolean类型值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
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

        if ("true".equals(value) || "yes".equals(value) || Symbol.ONE.equals(value)) {
            return true;
        }

        if ("false".equals(value) || "no".equals(value) || Symbol.ZERO.equals(value)) {
            return false;
        }

        return defaultValue;
    }

    /**
     * 获得int类型值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static long getInt(String key, int defaultValue) {
        return Convert.toInt(get(key), defaultValue);
    }

    /**
     * 获得long类型值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static long getLong(String key, long defaultValue) {
        return Convert.toLong(get(key), defaultValue);
    }

    /**
     * @return 属性列表
     */
    public static Properties props() {
        return System.getProperties();
    }

    /**
     * 获取当前进程 PID
     *
     * @return 当前进程 ID
     */
    public static long getCurrentPID() {
        return Long.parseLong(getRuntimeMXBean().getName().split(Symbol.AT)[0]);
    }

    /**
     * 返回Java虚拟机类加载系统相关属性
     *
     * @return {@link ClassLoadingMXBean}
     */
    public static ClassLoadingMXBean getClassLoadingMXBean() {
        return ManagementFactory.getClassLoadingMXBean();
    }

    /**
     * 返回Java虚拟机内存系统相关属性
     *
     * @return {@link MemoryMXBean}
     */
    public static MemoryMXBean getMemoryMXBean() {
        return ManagementFactory.getMemoryMXBean();
    }

    /**
     * 返回Java虚拟机线程系统相关属性
     *
     * @return {@link ThreadMXBean}
     */
    public static ThreadMXBean getThreadMXBean() {
        return ManagementFactory.getThreadMXBean();
    }

    /**
     * 返回Java虚拟机运行时系统相关属性
     *
     * @return {@link RuntimeMXBean}
     */
    public static RuntimeMXBean getRuntimeMXBean() {
        return ManagementFactory.getRuntimeMXBean();
    }

    /**
     * 返回Java虚拟机编译系统相关属性
     * 如果没有编译系统,则返回<code>null</code>
     *
     * @return a {@link CompilationMXBean} ,如果没有编译系统,则返回<code>null</code>
     */
    public static CompilationMXBean getCompilationMXBean() {
        return ManagementFactory.getCompilationMXBean();
    }

    /**
     * 返回Java虚拟机运行下的操作系统相关信息属性
     *
     * @return {@link OperatingSystemMXBean}
     */
    public static OperatingSystemMXBean getOperatingSystemMXBean() {
        return ManagementFactory.getOperatingSystemMXBean();
    }

    /**
     * Returns a list of {@link MemoryPoolMXBean} objects in the Java virtual machine. The Java virtual machine can have first or more memory pools. It may add or remove memory pools during execution.
     *
     * @return a list of <tt>MemoryPoolMXBean</tt> objects.
     */
    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
        return ManagementFactory.getMemoryPoolMXBeans();
    }

    /**
     * Returns a list of {@link MemoryManagerMXBean} objects in the Java virtual machine. The Java virtual machine can have first or more memory managers. It may add or remove memory managers during
     * execution.
     *
     * @return a list of <tt>MemoryManagerMXBean</tt> objects.
     */
    public static List<MemoryManagerMXBean> getMemoryManagerMXBeans() {
        return ManagementFactory.getMemoryManagerMXBeans();
    }

    /**
     * Returns a list of {@link GarbageCollectorMXBean} objects in the Java virtual machine. The Java virtual machine may have first or more <tt>GarbageCollectorMXBean</tt> objects. It may add or remove
     * <tt>GarbageCollectorMXBean</tt> during execution.
     *
     * @return a list of <tt>GarbageCollectorMXBean</tt> objects.
     */
    public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
        return ManagementFactory.getGarbageCollectorMXBeans();
    }

    /**
     * 取得Java Virtual Machine Specification的信息
     *
     * @return <code>JvmSpecInfo</code>对象
     */
    public static JvmSpec getJvmSpecInfo() {
        return Singleton.get(JvmSpec.class);
    }

    /**
     * 取得Java Virtual Machine Implementation的信息
     *
     * @return <code>JvmInfo</code>对象
     */
    public static Jvm getJvmInfo() {
        return Singleton.get(Jvm.class);
    }

    /**
     * 取得Java Specification的信息
     *
     * @return <code>JavaSpecInfo</code>对象
     */
    public static JavaSpec getJavaSpecInfo() {
        return Singleton.get(JavaSpec.class);
    }

    /**
     * 取得Java Implementation的信息
     *
     * @return <code>JavaInfo</code>对象
     */
    public static Java getJavaInfo() {
        return Singleton.get(Java.class);
    }

    /**
     * 取得当前运行的JRE的信息
     *
     * @return <code>JreInfo</code>对象
     */
    public static JavaRuntime getJavaRuntimeInfo() {
        return Singleton.get(JavaRuntime.class);
    }

    /**
     * 取得OS的信息
     *
     * @return <code>OsInfo</code>对象
     */
    public static OperatingSystem getOsInfo() {
        return Singleton.get(OperatingSystem.class);
    }

    /**
     * 取得User的信息
     *
     * @return <code>UserInfo</code>对象
     */
    public static OSUser getUserInfo() {
        return Singleton.get(OSUser.class);
    }

    /**
     * 取得Host的信息
     *
     * @return <code>HostInfo</code>对象
     */
    public static NetworkParams getHostInfo() {
        return Singleton.get(NetworkParams.class);
    }

    /**
     * 将系统信息输出到<code>System.out</code>中
     */
    public static void dumpSystemInfo() {
        dumpSystemInfo(new PrintWriter(System.out));
    }

    /**
     * 将系统信息输出到指定<code>PrintWriter</code>中
     *
     * @param out <code>PrintWriter</code>输出流
     */
    public static void dumpSystemInfo(PrintWriter out) {
        out.println("--------------");
        out.println(getJvmSpecInfo());
        out.println("--------------");
        out.println(getJvmInfo());
        out.println("--------------");
        out.println(getJavaSpecInfo());
        out.println("--------------");
        out.println(getJavaInfo());
        out.println("--------------");
        out.println(getJavaRuntimeInfo());
        out.println("--------------");
        out.println(getOsInfo());
        out.println("--------------");
        out.println(getUserInfo());
        out.println("--------------");
        out.println(getHostInfo());
        out.flush();
    }

    /**
     * 输出到<code>StringBuilder</code>
     *
     * @param builder <code>StringBuilder</code>对象
     * @param caption 标题
     * @param value   值
     */
    public static void append(StringBuilder builder, String caption, Object value) {
        builder.append(caption).append(StringUtils.nullToDefault(Convert.toString(value), "[n/a]")).append(Symbol.LF);
    }

    /**
     * 取得当前主机信息
     *
     * @return 主机地址信息
     */
    public static InetAddress getLocalAddress() {
        try {
            InetAddress inetAddress = null;
            /** 遍历所有的网络接口 */
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                /** 在所有的网络接口下再遍历IP */
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            /** 如果是site-local地址,就是它了 */
                            return inetAddr;
                        } else if (inetAddress == null) {
                            /** site-local类型的地址未被发现,先记录候选地址 */
                            inetAddress = inetAddr;
                        }
                    }
                }
            }
            if (inetAddress != null) {
                return inetAddress;
            }
            /**  如果没有发现 non-loopback地址.只能用最次选的方案 */
            inetAddress = InetAddress.getLocalHost();
            if (inetAddress == null) {
                throw new InstrumentException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return inetAddress;
        } catch (Exception e) {
            throw new InstrumentException("Failed to determine LAN address: " + e);
        }
    }

    /**
     * Creates a new instance of the appropriate platform-specific
     * {@link  OperatingSystem}.
     *
     * @return A new instance of {@link  OperatingSystem}.
     */
    public OperatingSystem getOperatingSystem() {
        return os.get();
    }

    private OperatingSystem createOperatingSystem() {
        switch (OS_CURRENT_PLATFORM) {

            case WINDOWS:
                return new WindowsOperatingSystem();
            case LINUX:
                return new LinuxOperatingSystem();
            case MACOSX:
                return new MacOperatingSystem();
            case SOLARIS:
                return new SolarisOperatingSystem();
            case FREEBSD:
                return new FreeBsdOperatingSystem();
            default:
                throw new UnsupportedOperationException("Operating system not supported: " + com.sun.jna.Platform.getOSType());
        }
    }

    /**
     * Creates a new instance of the appropriate platform-specific
     * {@link  HardwareAbstractionLayer}.
     *
     * @return A new instance of {@link  HardwareAbstractionLayer}.
     */
    public HardwareAbstractionLayer getHardware() {
        return hardware.get();
    }

    private HardwareAbstractionLayer createHardware() {
        switch (OS_CURRENT_PLATFORM) {

            case WINDOWS:
                return new WindowsHardwareAbstractionLayer();
            case LINUX:
                return new LinuxHardwareAbstractionLayer();
            case MACOSX:
                return new MacHardwareAbstractionLayer();
            case SOLARIS:
                return new SolarisHardwareAbstractionLayer();
            case FREEBSD:
                return new FreeBsdHardwareAbstractionLayer();
            default:
                throw new UnsupportedOperationException("Operating system not supported: " + com.sun.jna.Platform.getOSType());
        }
    }

    /**
     * Enum of supported operating systems.
     */
    public enum OS {
        /**
         * Microsoft Windows
         */
        WINDOWS,
        /**
         * A flavor of Linux
         */
        LINUX,
        /**
         * macOS (OS X)
         */
        MACOSX,
        /**
         * Solaris (SunOS)
         */
        SOLARIS,
        /**
         * FreeBSD
         */
        FREEBSD,
        /**
         * OpenBSD, WindowsCE, or an unspecified system
         */
        UNKNOWN
    }

}
