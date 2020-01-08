/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health;

import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.lang.Singleton;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.health.hardware.HardwareLayer;
import org.aoju.bus.health.hardware.linux.LinuxHardwareLayer;
import org.aoju.bus.health.hardware.mac.MacHardwareLayer;
import org.aoju.bus.health.hardware.unix.freebsd.FreeBsdHardwareLayer;
import org.aoju.bus.health.hardware.unix.solaris.SolarisHardwareLayer;
import org.aoju.bus.health.hardware.windows.WindowsHardwareLayer;
import org.aoju.bus.health.software.*;
import org.aoju.bus.health.software.linux.LinuxOS;
import org.aoju.bus.health.software.mac.MacOS;
import org.aoju.bus.health.software.unix.freebsd.FreeBsdOS;
import org.aoju.bus.health.software.unix.solaris.SolarisOS;
import org.aoju.bus.health.software.windows.WindowsOS;

import java.io.PrintWriter;
import java.lang.management.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * System information. This is the main entry point to Oshi.
 * <p>
 * This object provides getters which instantiate the appropriate
 * platform-specific implementations of {@link OperatingSystem}
 * (software) and {@link HardwareLayer} (hardware).
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8+
 */
public class Systemd {

    private static final Platform.OS _PLATFORM;

    static {
        if (com.sun.jna.Platform.isWindows()) {
            _PLATFORM = Platform.OS.WINDOWS;
        } else if (Platform.isLinux()) {
            _PLATFORM = Platform.OS.LINUX;
        } else if (Platform.isMac()) {
            _PLATFORM = Platform.OS.MACOSX;
        } else if (Platform.isSolaris()) {
            _PLATFORM = Platform.OS.SOLARIS;
        } else if (Platform.isFreeBSD()) {
            _PLATFORM = Platform.OS.FREEBSD;
        } else {
            _PLATFORM = Platform.OS.UNKNOWN;
        }
    }

    private final Supplier<OperatingSystem> os = Memoizer.memoize(this::createOperatingSystem);

    private final Supplier<HardwareLayer> hardware = Memoizer.memoize(this::createHardware);

    /**
     * <p>
     * Getter for the field <code>currentPlatformEnum</code>.
     * </p>
     *
     * @return Returns the currentPlatformEnum.
     */
    public static Platform.OS getCurrentPlatform() {
        return _PLATFORM;
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
    public static JvmSpecInfo getJvmSpecInfo() {
        return Singleton.get(JvmSpecInfo.class);
    }

    /**
     * 取得Java Virtual Machine Implementation的信息
     *
     * @return <code>JvmInfo</code>对象
     */
    public static JvmInfo getJvmInfo() {
        return Singleton.get(JvmInfo.class);
    }

    /**
     * 取得Java Specification的信息
     *
     * @return <code>JavaSpecInfo</code>对象
     */
    public static JavaSpecInfo getJavaSpecInfo() {
        return Singleton.get(JavaSpecInfo.class);
    }

    /**
     * 取得Java Implementation的信息
     *
     * @return <code>JavaInfo</code>对象
     */
    public static JavaInfo getJavaInfo() {
        return Singleton.get(JavaInfo.class);
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
     * {@link OperatingSystem}.
     *
     * @return A new instance of {@link OperatingSystem}.
     */
    public OperatingSystem getOperatingSystem() {
        return os.get();
    }

    private OperatingSystem createOperatingSystem() {
        switch (_PLATFORM) {

            case WINDOWS:
                return new WindowsOS();
            case LINUX:
                return new LinuxOS();
            case MACOSX:
                return new MacOS();
            case SOLARIS:
                return new SolarisOS();
            case FREEBSD:
                return new FreeBsdOS();
            default:
                throw new UnsupportedOperationException("Operating system not supported: " + com.sun.jna.Platform.getOSType());
        }
    }

    /**
     * Creates a new instance of the appropriate platform-specific
     * {@link HardwareLayer}.
     *
     * @return A new instance of {@link HardwareLayer}.
     */
    public HardwareLayer getHardware() {
        return hardware.get();
    }

    private HardwareLayer createHardware() {
        switch (_PLATFORM) {

            case WINDOWS:
                return new WindowsHardwareLayer();
            case LINUX:
                return new LinuxHardwareLayer();
            case MACOSX:
                return new MacHardwareLayer();
            case SOLARIS:
                return new SolarisHardwareLayer();
            case FREEBSD:
                return new FreeBsdHardwareLayer();
            default:
                throw new UnsupportedOperationException("Operating system not supported: " + com.sun.jna.Platform.getOSType());
        }
    }

}
