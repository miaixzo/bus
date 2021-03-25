/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.health;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.instance.Instances;
import org.aoju.bus.core.lang.*;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.health.builtin.*;
import org.aoju.bus.health.builtin.hardware.*;
import org.aoju.bus.health.builtin.software.OperatingSystem;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.System;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String parsing utility.
 *
 * @author Kimi Liu
 * @version 6.2.2
 * @since JDK 1.8+
 */
@ThreadSafe
public final class Builder {

    public static final String BUS_HEALTH_PROPERTIES = "bus-health.properties";
    public static final String BUS_HEALTH_ARCH_PROPERTIES = "bus-health-arch.properties";
    public static final String BUS_HEALTH_ADDR_PROPERTIES = "bus-health-addr.properties";

    /**
     * The official/approved path for sysfs information. Note: /sys/class/dmi/id
     * symlinks here
     */
    public static final String SYSFS_SERIAL_PATH = "/sys/devices/virtual/dmi/id/";
    /**
     * The Unix Epoch, a default value when WMI DateTime queries return no value.
     */
    public static final OffsetDateTime UNIX_EPOCH = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    private static final String MESSAGE = "{} didn't parse. Returning default. {}";
    /**
     * Used for matching
     */
    private static final Pattern HERTZ_PATTERN = Pattern.compile("(\\d+(.\\d+)?) ?([kMGT]?Hz).*");
    private static final Pattern BYTES_PATTERN = Pattern.compile("(\\d+) ?([kMGT]?B).*");
    /**
     * Pattern for [dd-[hh:[mm:[ss[.sss]]]]]
     */
    private static final Pattern DHMS = Pattern.compile("(?:(\\d+)-)?(?:(\\d+):)??(?:(\\d+):)?(\\d+)(?:\\.(\\d+))?");
    /**
     * Pattern for a UUID
     */
    private static final Pattern UUID_PATTERN = Pattern
            .compile(".*([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}).*");
    /**
     * Pattern for Windows PnPDeviceID vendor and product ID
     */
    private static final Pattern VENDOR_PRODUCT_ID = Pattern
            .compile(".*(?:VID|VEN)_(\\p{XDigit}{4})&(?:PID|DEV)_(\\p{XDigit}{4}).*");
    /**
     * Pattern for Linux lspci machine readable
     */
    private static final Pattern LSPCI_MACHINE_READABLE = Pattern.compile("(.+)\\s\\[(.*?)\\]");
    /**
     * Pattern for Linux lspci memory
     */
    private static final Pattern LSPCI_MEMORY_SIZE = Pattern.compile(".+\\s\\[size=(\\d+)([kKMGT])\\]");
    /**
     * Hertz related variables.
     */
    private static final String HZ = "Hz";
    private static final String KHZ = "kHz";
    private static final String MHZ = "MHz";
    private static final String GHZ = "GHz";
    private static final String THZ = "THz";
    private static final String PHZ = "PHz";
    private static final Map<String, Long> MULTIPLIERS;
    /**
     * DH时间戳是1601时代，本地时间常量要转换为UTC
     */
    private static final long EPOCH_DIFF = 11_644_473_600_000L;
    /**
     * 此时区在指定日期与UTC的偏移量
     */
    private static final int TZ_OFFSET = TimeZone.getDefault().getOffset(System.currentTimeMillis());
    /**
     * 快速十进制求幂:pow(10,y)——> POWERS_OF_10[y]
     */
    private static final long[] POWERS_OF_TEN = {1L, 10L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L,
            100_000_000L, 1_000_000_000L, 10_000_000_000L, 100_000_000_000L, 1_000_000_000_000L, 10_000_000_000_000L,
            100_000_000_000_000L, 1_000_000_000_000_000L, 10_000_000_000_000_000L, 100_000_000_000_000_000L,
            1_000_000_000_000_000_000L};
    /**
     * WMI返回的日期时间格式
     */
    private static final DateTimeFormatter CIM_FORMAT = DateTimeFormatter.ofPattern(Fields.PURE_DATETIME_ICE_PATTERN,
            Locale.US);

    private static final String GLOB_PREFIX = "glob:";
    private static final String REGEX_PREFIX = "regex:";

    /**
     * 硬件信息
     */
    private static final HardwareAbstractionLayer HARDWARE;
    /**
     * 系统信息
     */
    private static final OperatingSystem OS;
    /**
     * 操作系统信息
     */
    private static final Platform PLATFORM;

    static {
        MULTIPLIERS = new HashMap<>();
        MULTIPLIERS.put(HZ, 1L);
        MULTIPLIERS.put(KHZ, 1_000L);
        MULTIPLIERS.put(MHZ, 1_000_000L);
        MULTIPLIERS.put(GHZ, 1_000_000_000L);
        MULTIPLIERS.put(THZ, 1_000_000_000_000L);
        MULTIPLIERS.put(PHZ, 1_000_000_000_000_000L);
        PLATFORM = new Platform();
        HARDWARE = PLATFORM.getHardware();
        OS = PLATFORM.getOperatingSystem();
    }

    private Builder() {

    }

    /**
     * 获取操作系统相关信息，包括系统版本、文件系统、进程等
     *
     * @return 操作系统相关信息
     */
    public static OperatingSystem getOs() {
        return OS;
    }

    /**
     * 获取硬件相关信息，包括内存、硬盘、网络设备、显示器、USB、声卡等
     *
     * @return 硬件相关信息
     */
    public static HardwareAbstractionLayer getHardware() {
        return HARDWARE;
    }

    /**
     * 获取BIOS中计算机相关信息，比如序列号、固件版本等
     *
     * @return 获取BIOS中计算机相关信息
     */
    public static ComputerSystem getSystem() {
        return HARDWARE.getComputerSystem();
    }

    /**
     * 获取内存相关信息，比如总内存、可用内存等
     *
     * @return 内存相关信息
     */
    public static GlobalMemory getMemory() {
        return HARDWARE.getMemory();
    }

    /**
     * 获取CPU(处理器)相关信息，比如CPU负载等
     *
     * @return CPU(处理器)相关信息
     */
    public static CentralProcessor getProcessor() {
        return HARDWARE.getProcessor();
    }


    /**
     * 取得Java Virtual Machine Specification的信息
     *
     * @return <code>JvmSpecInfo</code>对象
     */
    public static JvmSpec getJvmSpecInfo() {
        return Instances.singletion(JvmSpec.class);
    }

    /**
     * 取得Java Virtual Machine Implementation的信息
     *
     * @return <code>JvmInfo</code>对象
     */
    public static Jvm getJvmInfo() {
        return Instances.singletion(Jvm.class);
    }

    /**
     * 取得Java Specification的信息
     *
     * @return <code>JavaSpecInfo</code>对象
     */
    public static JavaSpec getJavaSpecInfo() {
        return Instances.singletion(JavaSpec.class);
    }

    /**
     * 取得Java Implementation的信息
     *
     * @return <code>JavaInfo</code>对象
     */
    public static Java getJavaInfo() {
        return Instances.singletion(Java.class);
    }

    /**
     * 取得当前运行的JRE的信息
     *
     * @return <code>JreInfo</code>对象
     */
    public static JavaRuntime getJavaRuntimeInfo() {
        return Instances.singletion(JavaRuntime.class);
    }

    /**
     * 取得User的信息
     *
     * @return <code>UserInfo</code>对象
     */
    public static User getUserInfo() {
        return Instances.singletion(User.class);
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
                        } else if (null == inetAddress) {
                            /** site-local类型的地址未被发现,先记录候选地址 */
                            inetAddress = inetAddr;
                        }
                    }
                }
            }
            if (null != inetAddress) {
                return inetAddress;
            }
            /**  如果没有发现 non-loopback地址.只能用最次选的方案 */
            inetAddress = InetAddress.getLocalHost();
            if (null == inetAddress) {
                throw new InstrumentException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return inetAddress;
        } catch (Exception e) {
            throw new InstrumentException("Failed to determine LAN address: " + e);
        }
    }

    /**
     * 获取网络相关信息，可能多块网卡
     *
     * @return 网络相关信息
     */
    public static List<NetworkIF> getNetworkIFs() {
        return HARDWARE.getNetworkIFs();
    }

    /**
     * 获取系统CPU 系统使用率、用户使用率、利用率等等 相关信息
     *
     * @return 系统 CPU 使用率 等信息
     */
    public static Cpu getCpuInfo() {
        return getCpuInfo(1000);
    }

    /**
     * 获取系统CPU 系统使用率、用户使用率、利用率等等 相关信息
     *
     * @param waitingTime 设置等待时间
     * @return 系统 CPU 使用率 等信息
     */
    public static Cpu getCpuInfo(long waitingTime) {
        return getCpuInfo(getProcessor(), waitingTime);
    }

    /**
     * 获取系统CPU 系统使用率、用户使用率、利用率等等 相关信息
     *
     * @param processor   {@link CentralProcessor}
     * @param waitingTime 设置等待时间
     * @return 系统 CPU 使用率 等信息
     */
    private static Cpu getCpuInfo(CentralProcessor processor, long waitingTime) {
        Cpu cpu = new Cpu();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        sleep(waitingTime);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softIrq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long ioWait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = Math.max(user + nice + cSys + idle + ioWait + irq + softIrq + steal, 0);
        final DecimalFormat format = new DecimalFormat("#.00");
        cpu.setCpuNum(processor.getLogicalProcessorCount());
        cpu.setToTal(totalCpu);
        cpu.setSys(Double.parseDouble(format.format(cSys <= 0 ? 0 : (100d * cSys / totalCpu))));
        cpu.setUsed(Double.parseDouble(format.format(user <= 0 ? 0 : (100d * user / totalCpu))));
        if (totalCpu == 0) {
            cpu.setWait(0);
        } else {
            cpu.setWait(Double.parseDouble(format.format(100d * ioWait / totalCpu)));
        }
        cpu.setFree(Double.parseDouble(format.format(idle <= 0 ? 0 : (100d * idle / totalCpu))));
        cpu.setCpuModel(processor.toString());
        return cpu;
    }

    /**
     * 获取传感器相关信息，例如CPU温度、风扇转速等，传感器可能有多个
     *
     * @return 传感器相关信息
     */
    public static Sensors getSensors() {
        return HARDWARE.getSensors();
    }

    /**
     * 获取磁盘相关信息，可能有多个磁盘(包括可移动磁盘等)
     *
     * @return 磁盘相关信息
     */
    public static List<HWDiskStore> getDiskStores() {
        return HARDWARE.getDiskStores();
    }

    /**
     * Sleeps for the specified number of milliseconds.
     *
     * @param ms How long to sleep
     */
    public static void sleep(long ms) {
        try {
            Logger.trace("Sleeping for {} ms", ms);
            Thread.sleep(ms);
        } catch (InterruptedException e) { // NOSONAR squid:S2142
            Logger.warn("Interrupted while sleeping for {} ms: {}", ms, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Gets a map containing current working directory info
     *
     * @param pid a process ID, optional
     * @return a map of process IDs to their current working directory. If
     * {@code pid} is a negative number, all processes are returned;
     * otherwise the map may contain only a single element for {@code pid}
     */
    public static Map<Integer, String> getCwdMap(int pid) {
        List<String> lsof = Executor.runNative("lsof -F n -d cwd" + (pid < 0 ? "" : " -p " + pid));
        Map<Integer, String> cwdMap = new HashMap<>();
        Integer key = -1;
        for (String line : lsof) {
            if (line.isEmpty()) {
                continue;
            }
            switch (line.charAt(0)) {
                case 'p':
                    key = Builder.parseIntOrDefault(line.substring(1), -1);
                    break;
                case 'n':
                    cwdMap.put(key, line.substring(1));
                    break;
                case 'f':
                    // ignore the 'cwd' file descriptor
                default:
                    break;
            }
        }
        return cwdMap;
    }

    /**
     * Gets current working directory info
     *
     * @param pid a process ID
     * @return the current working directory for that process.
     */
    public static String getCwd(int pid) {
        List<String> lsof = Executor.runNative("lsof -F n -d cwd -p " + pid);
        for (String line : lsof) {
            if (!line.isEmpty() && line.charAt(0) == 'n') {
                return line.substring(1).trim();
            }
        }
        return Normal.EMPTY;
    }

    /**
     * Read a file and return the long value contained therein. Intended primarily
     * for Linux /sys filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise zero
     */
    public static long getLongFromFile(String filename) {
        if (Logger.get().isDebug()) {
            Logger.debug("Reading file {}", filename);
        }
        List<String> read = FileKit.readLines(filename);
        if (!read.isEmpty()) {
            if (Logger.get().isTrace()) {
                Logger.trace("Read {}", read.get(0));
            }
            return Builder.parseLongOrDefault(read.get(0), 0L);
        }
        return 0L;
    }

    /**
     * Convert unsigned int to signed long.
     *
     * @param x Signed int representing an unsigned integer
     * @return long value of x unsigned
     */
    public static long getUnsignedInt(int x) {
        return x & 0x00000000ffffffffL;
    }

    /**
     * Read a file and return the int value contained therein. Intended primarily
     * for Linux /sys filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise zero
     */
    public static int getIntFromFile(String filename) {
        if (Logger.get().isDebug()) {
            Logger.debug("Reading file {}", filename);
        }
        try {
            List<String> read = FileKit.readLines(filename);
            if (!read.isEmpty()) {
                if (Logger.get().isTrace()) {
                    Logger.trace("Read {}", read.get(0));
                }
                return Integer.parseInt(read.get(0));
            }
        } catch (NumberFormatException ex) {
            Logger.warn("Unable to read value from {}. {}", filename, ex.getMessage());
        }
        return 0;
    }

    /**
     * Read a file and return the String value contained therein. Intended primarily
     * for Linux /sys filesystem
     *
     * @param filename The file to read
     * @return The value contained in the file, if any; otherwise empty string
     */
    public static String getStringFromFile(String filename) {
        if (Logger.get().isDebug()) {
            Logger.debug("Reading file {}", filename);
        }
        List<String> read = FileKit.readLines(filename);
        if (!read.isEmpty()) {
            if (Logger.get().isTrace()) {
                Logger.trace("Read {}", read.get(0));
            }
            return read.get(0);
        }
        return Normal.EMPTY;
    }

    /**
     * Read a file and return a map of string keys to string values contained
     * therein. Intended primarily for Linux {@code /proc/[pid]} files to provide
     * more detailed or accurate information not available in the API.
     *
     * @param filename  The file to read
     * @param separator Character(s) in each line of the file that separate the key and
     *                  the value.
     * @return The map contained in the file, delimited by the separator, with the
     * value whitespace trimmed. If keys and values are not parsed, an empty
     * map is returned.
     */
    public static Map<String, String> getKeyValueMapFromFile(String filename, String separator) {
        Map<String, String> map = new HashMap<>();
        if (Logger.get().isDebug()) {
            Logger.debug("Reading file {}", filename);
        }
        List<String> lines = FileKit.readLines(filename);
        for (String line : lines) {
            String[] parts = line.split(separator);
            if (parts.length == 2) {
                map.put(parts[0], parts[1].trim());
            }
        }
        return map;
    }

    /**
     * Gets the Manufacturer ID from (up to) 3 5-bit characters in bytes 8 and 9
     *
     * @param edid The EDID byte array
     * @return The manufacturer ID
     */
    public static String getManufacturerID(byte[] edid) {
        // Bytes 8-9 are manufacturer ID in 3 5-bit characters.
        String temp = String
                .format("%8s%8s", Integer.toBinaryString(edid[8] & 0xFF), Integer.toBinaryString(edid[9] & 0xFF))
                .replace(Symbol.C_SPACE, '0');
        Logger.debug("Manufacurer ID: {}", temp);
        return String.format("%s%s%s", (char) (64 + Integer.parseInt(temp.substring(1, 6), 2)),
                (char) (64 + Integer.parseInt(temp.substring(7, 11), 2)),
                (char) (64 + Integer.parseInt(temp.substring(12, 16), 2))).replace(Symbol.AT, Normal.EMPTY);
    }

    /**
     * Gets the Product ID, bytes 10 and 11
     *
     * @param edid The EDID byte array
     * @return The product ID
     */
    public static String getProductID(byte[] edid) {
        // Bytes 10-11 are product ID expressed in hex characters
        return Integer.toHexString(
                ByteBuffer.wrap(Arrays.copyOfRange(edid, 10, 12)).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xffff);
    }

    /**
     * Gets the Serial number, bytes 12-15
     *
     * @param edid The EDID byte array
     * @return If all 4 bytes represent alphanumeric characters, a 4-character
     * string, otherwise a hex string.
     */
    public static String getSerialNo(byte[] edid) {
        // Bytes 12-15 are Serial number (last 4 characters)
        if (Logger.get().isDebug()) {
            Logger.debug("Serial number: {}", Arrays.toString(Arrays.copyOfRange(edid, 12, 16)));
        }
        return String.format("%s%s%s%s", getAlphaNumericOrHex(edid[15]), getAlphaNumericOrHex(edid[14]),
                getAlphaNumericOrHex(edid[13]), getAlphaNumericOrHex(edid[12]));
    }

    private static String getAlphaNumericOrHex(byte b) {
        return Character.isLetterOrDigit((char) b) ? String.format("%s", (char) b) : String.format("%02X", b);
    }

    /**
     * Return the week of year of manufacture
     *
     * @param edid The EDID byte array
     * @return The week of year
     */
    public static byte getWeek(byte[] edid) {
        // Byte 16 is manufacture week
        return edid[16];
    }

    /**
     * Return the year of manufacture
     *
     * @param edid The EDID byte array
     * @return The year of manufacture
     */
    public static int getYear(byte[] edid) {
        // Byte 17 is manufacture year-1990
        byte temp = edid[17];
        Logger.debug("Year-1990: {}", temp);
        return temp + 1990;
    }

    /**
     * Return the EDID version
     *
     * @param edid The EDID byte array
     * @return The EDID version
     */
    public static String getVersion(byte[] edid) {
        // Bytes 18-19 are EDID version
        return edid[18] + Symbol.DOT + edid[19];
    }

    /**
     * Test if this EDID is a digital monitor based on byte 20
     *
     * @param edid The EDID byte array
     * @return True if the EDID represents a digital monitor, false otherwise
     */
    public static boolean isDigital(byte[] edid) {
        // Byte 20 is Video input params
        return 1 == (edid[20] & 0xff) >> 7;
    }

    /**
     * Get monitor width in cm
     *
     * @param edid The EDID byte array
     * @return Monitor width in cm
     */
    public static int getHcm(byte[] edid) {
        // Byte 21 is horizontal size in cm
        return edid[21];
    }

    /**
     * Get monitor height in cm
     *
     * @param edid The EDID byte array
     * @return Monitor height in cm
     */
    public static int getVcm(byte[] edid) {
        // Byte 22 is vertical size in cm
        return edid[22];
    }

    /**
     * Get the VESA descriptors
     *
     * @param edid The EDID byte array
     * @return A 2D array with four 18-byte elements representing VESA descriptors
     */
    public static byte[][] getDescriptors(byte[] edid) {
        byte[][] desc = new byte[4][18];
        for (int i = 0; i < desc.length; i++) {
            System.arraycopy(edid, 54 + 18 * i, desc[i], 0, 18);
        }
        return desc;
    }

    /**
     * Get the VESA descriptor type
     *
     * @param desc An 18-byte VESA descriptor
     * @return An integer representing the first four bytes of the VESA descriptor
     */
    public static int getDescriptorType(byte[] desc) {
        return ByteBuffer.wrap(Arrays.copyOfRange(desc, 0, 4)).getInt();
    }

    /**
     * Parse a detailed timing descriptor
     *
     * @param desc An 18-byte VESA descriptor
     * @return A string describing part of the detailed timing descriptor
     */
    public static String getTimingDescriptor(byte[] desc) {
        int clock = ByteBuffer.wrap(Arrays.copyOfRange(desc, 0, 2)).order(ByteOrder.LITTLE_ENDIAN).getShort() / 100;
        int hActive = (desc[2] & 0xff) + ((desc[4] & 0xf0) << 4);
        int vActive = (desc[5] & 0xff) + ((desc[7] & 0xf0) << 4);
        return String.format("Clock %dMHz, Active Pixels %dx%d ", clock, hActive, vActive);
    }

    /**
     * Parse descriptor range limits
     *
     * @param desc An 18-byte VESA descriptor
     * @return A string describing some of the range limits
     */
    public static String getDescriptorRangeLimits(byte[] desc) {
        return String.format("Field Rate %d-%d Hz vertical, %d-%d Hz horizontal, Max clock: %d MHz", desc[5], desc[6],
                desc[7], desc[8], desc[9] * 10);
    }

    /**
     * Parse descriptor text
     *
     * @param desc An 18-byte VESA descriptor
     * @return Plain text starting at the 4th byte
     */
    public static String getDescriptorText(byte[] desc) {
        return new String(Arrays.copyOfRange(desc, 4, 18), Charset.US_ASCII).trim();
    }

    /**
     * Read a configuration file from the class path and return its properties
     *
     * @param fileName The filename
     * @return A {@link java.util.Properties} object containing the properties.
     */
    public static java.util.Properties readProperties(String fileName) {
        return org.aoju.bus.setting.magic.Properties.getProp(Symbol.SLASH + Normal.META_DATA_INF + "/healthy/" + fileName, Builder.class);
    }

    /**
     * Parse an EDID byte array into user-readable information
     *
     * @param edid An EDID byte array
     * @return User-readable text represented by the EDID
     */
    public static String toString(byte[] edid) {
        StringBuilder sb = new StringBuilder();
        sb.append("  Manuf. ID=").append(getManufacturerID(edid));
        sb.append(", Product ID=").append(getProductID(edid));
        sb.append(", ").append(isDigital(edid) ? "Digital" : "Analog");
        sb.append(", Serial=").append(getSerialNo(edid));
        sb.append(", ManufDate=").append(getWeek(edid) * 12 / 52 + 1).append(Symbol.C_SLASH)
                .append(getYear(edid));
        sb.append(", EDID v").append(getVersion(edid));
        int hSize = getHcm(edid);
        int vSize = getVcm(edid);
        sb.append(String.format("%n  %d x %d cm (%.1f x %.1f in)", hSize, vSize, hSize / 2.54, vSize / 2.54));
        byte[][] desc = getDescriptors(edid);
        for (byte[] b : desc) {
            switch (getDescriptorType(b)) {
                case 0xff:
                    sb.append("\n  Serial Number: ").append(getDescriptorText(b));
                    break;
                case 0xfe:
                    sb.append("\n  Unspecified Text: ").append(getDescriptorText(b));
                    break;
                case 0xfd:
                    sb.append("\n  Range Limits: ").append(getDescriptorRangeLimits(b));
                    break;
                case 0xfc:
                    sb.append("\n  Monitor Name: ").append(getDescriptorText(b));
                    break;
                case 0xfb:
                    sb.append("\n  White Point Data: ").append(byteArrayToHexString(b));
                    break;
                case 0xfa:
                    sb.append("\n  Standard Timing ID: ").append(byteArrayToHexString(b));
                    break;
                default:
                    if (getDescriptorType(b) <= 0x0f && getDescriptorType(b) >= 0x00) {
                        sb.append("\n  Manufacturer Data: ").append(byteArrayToHexString(b));
                    } else {
                        sb.append("\n  Preferred Timing: ").append(getTimingDescriptor(b));
                    }
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Parse hertz from a string, eg. "2.00MHz" is 2000000L.
     *
     * @param hertz Hertz size.
     * @return {@link java.lang.Long} Hertz value or -1 if not parseable.
     */
    public static long parseHertz(String hertz) {
        Matcher matcher = HERTZ_PATTERN.matcher(hertz.trim());
        if (matcher.find() && matcher.groupCount() == 3) {
            // Regexp enforces #(.#) format so no test for NFE required
            double value = Double.valueOf(matcher.group(1)) * MULTIPLIERS.getOrDefault(matcher.group(3), -1L);
            if (value >= 0d) {
                return (long) value;
            }
        }
        return -1L;
    }

    /**
     * Parse the last element of a space-delimited string to a value
     *
     * @param s The string to parse
     * @param i Default integer if not parsable
     * @return value or the given default if not parsable
     */
    public static int parseLastInt(String s, int i) {
        try {
            String ls = parseLastString(s);
            if (ls.toLowerCase().startsWith("0x")) {
                return Integer.decode(ls);
            } else {
                return Integer.parseInt(ls);
            }
        } catch (NumberFormatException e) {
            Logger.trace(MESSAGE, s, e);
            return i;
        }
    }

    /**
     * Parse the last element of a space-delimited string to a value
     *
     * @param s  The string to parse
     * @param li Default long integer if not parsable
     * @return value or the given default if not parsable
     */
    public static long parseLastLong(String s, long li) {
        try {
            String ls = parseLastString(s);
            if (ls.toLowerCase().startsWith("0x")) {
                return Long.decode(ls);
            } else {
                return Long.parseLong(ls);
            }
        } catch (NumberFormatException e) {
            Logger.trace(MESSAGE, s, e);
            return li;
        }
    }

    /**
     * Parse the last element of a space-delimited string to a value
     *
     * @param s The string to parse
     * @param d Default double if not parsable
     * @return value or the given default if not parsable
     */
    public static double parseLastDouble(String s, double d) {
        try {
            return Double.parseDouble(parseLastString(s));
        } catch (NumberFormatException e) {
            Logger.trace(MESSAGE, s, e);
            return d;
        }
    }

    /**
     * Parse the last element of a space-delimited string to a string
     *
     * @param s The string to parse
     * @return last space-delimited element
     */
    public static String parseLastString(String s) {
        String[] ss = RegEx.SPACES.split(s);
        // guaranteed at least one element
        return ss[ss.length - 1];
    }

    /**
     * Parse a byte aray into a string of hexadecimal digits including leading zeros
     *
     * @param bytes The byte array to represent
     * @return A string of hex characters corresponding to the bytes. The string is
     * upper case.
     */
    public static String byteArrayToHexString(byte[] bytes) {
        // Solution copied from https://stackoverflow.com/questions/9655181
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = Normal.DIGITS_16_UPPER[v >>> 4];
            hexChars[j * 2 + 1] = Normal.DIGITS_16_UPPER[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Parse a string of hexadecimal digits into a byte array
     *
     * @param digits The string to be parsed
     * @return a byte array with each pair of characters converted to a byte, or
     * empty array if the string is not valid hex
     */
    public static byte[] hexStringToByteArray(String digits) {
        int len = digits.length();
        // Check if string is valid hex
        if (!RegEx.VALID_HEX.matcher(digits).matches() || (len & 0x1) != 0) {
            Logger.warn("Invalid hexadecimal string: {}", digits);
            return new byte[0];
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) (Character.digit(digits.charAt(i), 16) << 4
                    | Character.digit(digits.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Parse a human readable ASCII string into a byte array, truncating or padding
     * with zeros (if necessary) so the array has the specified length.
     *
     * @param text   The string to be parsed
     * @param length Length of the returned array.
     * @return A byte array of specified length, with each of the first length
     * characters converted to a byte. If length is longer than the provided
     * string length, will be filled with zeroes.
     */
    public static byte[] asciiStringToByteArray(String text, int length) {
        return Arrays.copyOf(text.getBytes(Charset.US_ASCII), length);
    }

    /**
     * Convert a long value to a byte array using Big Endian, truncating or padding
     * with zeros (if necessary) so the array has the specified length.
     *
     * @param value     The value to be converted
     * @param valueSize Number of bytes representing the value
     * @param length    Number of bytes to return
     * @return A byte array of specified length representing the long in the first
     * valueSize bytes
     */
    public static byte[] longToByteArray(long value, int valueSize, int length) {
        long val = value;
        // Convert the long to 8-byte BE representation
        byte[] b = new byte[8];
        for (int i = 7; i >= 0 && val != 0L; i--) {
            b[i] = (byte) val;
            val >>>= 8;
        }
        // Then copy the rightmost valueSize bytes
        // e.g., for an integer we want rightmost 4 bytes
        return Arrays.copyOfRange(b, 8 - valueSize, 8 + length - valueSize);
    }

    /**
     * Convert a string to an integer representation.
     *
     * @param str  A human readable ASCII string
     * @param size Number of characters to convert to the long. May not exceed 8.
     * @return An integer representing the string where each character is treated as
     * a byte
     */
    public static long strToLong(String str, int size) {
        return byteArrayToLong(str.getBytes(Charset.US_ASCII), size);
    }

    /**
     * Convert a byte array to its (long) integer representation assuming big endian
     * ordering.
     *
     * @param bytes An array of bytes no smaller than the size to be converted
     * @param size  Number of bytes to convert to the long. May not exceed 8.
     * @return A long integer representing the byte array
     */
    public static long byteArrayToLong(byte[] bytes, int size) {
        return byteArrayToLong(bytes, size, true);
    }

    /**
     * Convert a byte array to its (long) integer representation in the specified
     * endianness.
     *
     * @param bytes     An array of bytes no smaller than the size to be converted
     * @param size      Number of bytes to convert to the long. May not exceed 8.
     * @param bigEndian True to parse big-endian, false to parse little-endian
     * @return An long integer representing the byte array
     */
    public static long byteArrayToLong(byte[] bytes, int size, boolean bigEndian) {
        if (size > 8) {
            throw new IllegalArgumentException("Can't convert more than 8 bytes.");
        }
        if (size > bytes.length) {
            throw new IllegalArgumentException("Size can't be larger than array length.");
        }
        long total = 0L;
        for (int i = 0; i < size; i++) {
            if (bigEndian) {
                total = total << 8 | bytes[i] & 0xff;
            } else {
                total = total << 8 | bytes[size - i - 1] & 0xff;
            }
        }
        return total;
    }


    /**
     * Convert a byte array to its floating point representation.
     *
     * @param bytes  An array of bytes no smaller than the size to be converted
     * @param size   Number of bytes to convert to the float. May not exceed 8.
     * @param fpBits Number of bits representing the decimal
     * @return A float; the integer portion representing the byte array as an
     * integer shifted by the bits specified in fpBits; with the remaining
     * bits used as a decimal
     */
    public static float byteArrayToFloat(byte[] bytes, int size, int fpBits) {
        return byteArrayToLong(bytes, size) / (float) (1 << fpBits);
    }

    /**
     * Convert an unsigned integer to a long value. The method assumes that all bits
     * in the specified integer value are 'data' bits, including the
     * most-significant bit which Java normally considers a sign bit. The method
     * must be used only when it is certain that the integer value represents an
     * unsigned integer, for example when the integer is returned by JNA library in
     * a structure which holds unsigned integers.
     *
     * @param unsignedValue The unsigned integer value to convert.
     * @return The unsigned integer value widened to a long.
     */
    public static long unsignedIntToLong(int unsignedValue) {
        // use standard Java widening conversion to long which does
        // sign-extension,
        // then drop any copies of the sign bit, to prevent the value being
        // considered a negative one by Java if it is set
        long longValue = unsignedValue;
        return longValue & 0xffff_ffffL;
    }

    /**
     * Parses a string of hex digits to a string where each pair of hex digits
     * represents an ASCII character
     *
     * @param hexString A sequence of hex digits
     * @return The corresponding string if valid hex; otherwise the original
     * hexString
     */
    public static String hexStringToString(String hexString) {
        // Odd length strings won't parse, return
        if (hexString.length() % 2 > 0) {
            return hexString;
        }
        int charAsInt;
        StringBuilder sb = new StringBuilder();
        try {
            for (int pos = 0; pos < hexString.length(); pos += 2) {
                charAsInt = Integer.parseInt(hexString.substring(pos, pos + 2), 16);
                if (charAsInt < 32 || charAsInt > 127) {
                    return hexString;
                }
                sb.append((char) charAsInt);
            }
        } catch (NumberFormatException e) {
            Logger.trace(MESSAGE, hexString, e);
            // Hex failed to parse, just return the existing string
            return hexString;
        }
        return sb.toString();
    }

    /**
     * Attempts to parse a string to an int. If it fails, returns the default
     *
     * @param s          The string to parse
     * @param defaultInt The value to return if parsing fails
     * @return The parsed int, or the default if parsing failed
     */
    public static int parseIntOrDefault(String s, int defaultInt) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Logger.trace(MESSAGE, s, e);
            return defaultInt;
        }
    }

    /**
     * Attempts to parse a string to a long. If it fails, returns the default
     *
     * @param s           The string to parse
     * @param defaultLong The value to return if parsing fails
     * @return The parsed long, or the default if parsing failed
     */
    public static long parseLongOrDefault(String s, long defaultLong) {
        try {
            return new BigInteger(s).longValue();
        } catch (NumberFormatException e) {
            Logger.trace(MESSAGE, s, e);
            return defaultLong;
        }
    }

    /**
     * Attempts to parse a string to a double. If it fails, returns the default
     *
     * @param s             The string to parse
     * @param defaultDouble The value to return if parsing fails
     * @return The parsed double, or the default if parsing failed
     */
    public static double parseDoubleOrDefault(String s, double defaultDouble) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            Logger.trace(MESSAGE, s, e);
            return defaultDouble;
        }
    }

    /**
     * Attempts to parse a string of the form [DD-[hh:]]mm:ss[.ddd] to a number of
     * milliseconds. If it fails, returns the default.
     *
     * @param s           The string to parse
     * @param defaultLong The value to return if parsing fails
     * @return The parsed number of seconds, or the default if parsing fails
     */
    public static long parseDHMSOrDefault(String s, long defaultLong) {
        Matcher m = DHMS.matcher(s);
        if (m.matches()) {
            long milliseconds = 0L;
            if (null != m.group(1)) {
                milliseconds += parseLongOrDefault(m.group(1), 0L) * 86_400_000L;
            }
            if (null != m.group(2)) {
                milliseconds += parseLongOrDefault(m.group(2), 0L) * 3_600_000L;
            }
            if (null != m.group(3)) {
                milliseconds += parseLongOrDefault(m.group(3), 0L) * 60_000L;
            }
            milliseconds += parseLongOrDefault(m.group(4), 0L) * 1000L;
            milliseconds += (long) (1000 * parseDoubleOrDefault("0." + m.group(5), 0d));
            return milliseconds;
        }
        return defaultLong;
    }

    /**
     * Attempts to parse a UUID. If it fails, returns the default.
     *
     * @param s          The string to parse
     * @param defaultStr The value to return if parsing fails
     * @return The parsed UUID, or the default if parsing fails
     */
    public static String parseUuidOrDefault(String s, String defaultStr) {
        Matcher m = UUID_PATTERN.matcher(s.toLowerCase());
        if (m.matches()) {
            return m.group(1);
        }
        return defaultStr;
    }

    /**
     * Parses a string key = 'value' (string)
     *
     * @param line The entire string
     * @return the value contained between single tick marks
     */
    public static String getSingleQuoteStringValue(String line) {
        return getStringBetween(line, Symbol.C_SINGLE_QUOTE);
    }

    /**
     * Gets a value between two characters having multiple same characters between
     * them. <b>Examples : </b>
     * <ul>
     * <li>"name = 'James Gosling's Java'" returns "James Gosling's Java"</li>
     * <li>"pci.name = 'Realtek AC'97 Audio Device'" returns "Realtek AC'97 Audio
     * Device"</li>
     * </ul>
     *
     * @param line The "key-value" pair line.
     * @param c    The Trailing And Leading characters of the string line
     * @return : The value having the characters between them.
     */
    public static String getStringBetween(String line, char c) {
        int firstOcc = line.indexOf(c);
        if (firstOcc < 0) {
            return Normal.EMPTY;
        }
        return line.substring(firstOcc + 1, line.lastIndexOf(c)).trim();
    }

    /**
     * Parses a string such as "10.12.2" or "key = 1 (0x1) (int)" to find the
     * integer value of the first set of one or more consecutive digits
     *
     * @param line The entire string
     * @return the value of first integer if any; 0 otherwise
     */
    public static int getFirstIntValue(String line) {
        return getNthIntValue(line, 1);
    }

    /**
     * Parses a string such as "10.12.2" or "key = 1 (0x1) (int)" to find the
     * integer value of the nth set of one or more consecutive digits
     *
     * @param line The entire string
     * @param n    Which set of integers to return
     * @return the value of nth integer if any; 0 otherwise
     */
    public static int getNthIntValue(String line, int n) {
        // Split the string by non-digits,
        String[] split = RegEx.NOT_NUMBERS.split(RegEx.WITH_NOT_NUMBERS.matcher(line).replaceFirst(Normal.EMPTY));
        if (split.length >= n) {
            return parseIntOrDefault(split[n - 1], 0);
        }
        return 0;
    }

    /**
     * Removes all matching sub strings from the string. More efficient than regexp.
     *
     * @param original source String to remove from
     * @param toRemove the sub string to be removed
     * @return The string with all matching substrings removed
     */
    public static String removeMatchingString(final String original, final String toRemove) {
        if (null == original || original.isEmpty() || null == toRemove || toRemove.isEmpty()) {
            return original;
        }

        int matchIndex = original.indexOf(toRemove, 0);
        if (matchIndex == -1) {
            return original;
        }

        StringBuilder buffer = new StringBuilder(original.length() - toRemove.length());
        int currIndex = 0;
        do {
            buffer.append(original.substring(currIndex, matchIndex));
            currIndex = matchIndex + toRemove.length();
            matchIndex = original.indexOf(toRemove, currIndex);
        } while (matchIndex != -1);

        buffer.append(original.substring(currIndex));
        return buffer.toString();
    }

    /**
     * Parses a delimited string to an array of longs. Optimized for processing
     * predictable-length arrays such as outputs of reliably formatted Linux proc or
     * sys filesystem, minimizing new object creation. Users should perform other
     * sanity checks of data.
     * <p>
     * As a special case, non-numeric fields (such as UUIDs in OpenVZ) at the end of
     * the list are ignored. Values greater than the max long value return the max
     * long value.
     * <p>
     * The indices parameters are referenced assuming the length as specified, and
     * leading characters are ignored. For example, if the string is "foo 12 34 5"
     * and the length is 3, then index 0 is 12, index 1 is 34, and index 2 is 5.
     *
     * @param s         The string to parse
     * @param indices   An array indicating which indexes should be populated in the final
     *                  array; other values will be skipped. This idex is zero-referenced
     *                  assuming the rightmost delimited fields of the string contain the
     *                  array.
     * @param length    The total number of elements in the string array. It is
     *                  permissible for the string to have more elements than this;
     *                  leading elements will be ignored. This should be calculated once
     *                  per text format by {@link #countStringToLongArray}.
     * @param delimiter The character to delimit by.
     * @return If successful, an array of parsed longs. If parsing errors occurred,
     * will be an array of zeros.
     */
    public static long[] parseStringToLongArray(String s, int[] indices, int length, char delimiter) {
        long[] parsed = new long[indices.length];
        // Iterate from right-to-left of String
        // Fill right to left of result array using index array
        int charIndex = s.length();
        int parsedIndex = indices.length - 1;
        int stringIndex = length - 1;

        int power = 0;
        int c;
        boolean delimCurrent = false;
        boolean numeric = true;
        boolean numberFound = false; // ignore nonnumeric at end
        boolean dashSeen = false; // to flag uuids as nonnumeric
        while (--charIndex > 0 && parsedIndex >= 0) {
            c = s.charAt(charIndex);
            if (c == delimiter) {
                // first parseable number?
                if (!numberFound && numeric) {
                    numberFound = true;
                }
                if (!delimCurrent) {
                    if (numberFound && indices[parsedIndex] == stringIndex--) {
                        parsedIndex--;
                    }
                    delimCurrent = true;
                    power = 0;
                    dashSeen = false;
                    numeric = true;
                }
            } else if (indices[parsedIndex] != stringIndex || c == Symbol.C_PLUS || !numeric) {
                // Doesn't impact parsing, ignore
                delimCurrent = false;
            } else if (c >= '0' && c <= '9' && !dashSeen) {
                if (power > 18 || power == 17 && c == '9' && parsed[parsedIndex] > 223_372_036_854_775_807L) {
                    parsed[parsedIndex] = Long.MAX_VALUE;
                } else {
                    parsed[parsedIndex] += (c - '0') * Builder.POWERS_OF_TEN[power++];
                }
                delimCurrent = false;
            } else if (c == Symbol.C_HYPHEN) {
                parsed[parsedIndex] *= -1L;
                delimCurrent = false;
                dashSeen = true;
            } else {
                // Flag as nonnumeric and continue unless we've seen a numeric
                // error on everything else
                if (numberFound) {
                    if (!noLog(s)) {
                        Logger.error("Illegal character parsing string '{}' to long array: {}", s, s.charAt(charIndex));
                    }
                    return new long[indices.length];
                }
                parsed[parsedIndex] = 0;
                numeric = false;
            }
        }
        if (parsedIndex > 0) {
            if (!noLog(s)) {
                Logger.error("Not enough fields in string '{}' parsing to long array: {}", s,
                        indices.length - parsedIndex);
            }
            return new long[indices.length];
        }
        return parsed;
    }

    /**
     * Test whether to log this message
     *
     * @param s The string to log
     * @return True if the string begins with {@code NOLOG}
     */
    private static boolean noLog(String s) {
        return s.startsWith("NOLOG: ");
    }

    /**
     * Parses a delimited string to count elements of an array of longs. Intended to
     * be called once to calculate the {@code length} field for
     * {@link #parseStringToLongArray}.
     * <p>
     * As a special case, non-numeric fields (such as UUIDs in OpenVZ) at the end of
     * the list are ignored.
     *
     * @param s         The string to parse
     * @param delimiter The character to delimit by
     * @return The number of parsable long values which follow the last unparsable
     * value.
     */
    public static int countStringToLongArray(String s, char delimiter) {
        // Iterate from right-to-left of String
        // Fill right to left of result array using index array
        int charIndex = s.length();
        int numbers = 0;

        int c;
        boolean delimCurrent = false;
        boolean numeric = true;
        boolean dashSeen = false; // to flag uuids as nonnumeric
        while (--charIndex > 0) {
            c = s.charAt(charIndex);
            if (c == delimiter) {
                if (!delimCurrent) {
                    if (numeric) {
                        numbers++;
                    }
                    delimCurrent = true;
                    dashSeen = false;
                    numeric = true;
                }
            } else if (c == Symbol.C_PLUS || !numeric) {
                // Doesn't impact parsing, ignore
                delimCurrent = false;
            } else if (c >= '0' && c <= '9' && !dashSeen) {
                delimCurrent = false;
            } else if (c == Symbol.C_HYPHEN) {
                delimCurrent = false;
                dashSeen = true;
            } else {
                // we found non-digit or delimiter. If not last field, exit
                if (numbers > 0) {
                    return numbers;
                }
                // Else flag as nonnumeric and continue
                numeric = false;
            }
        }
        // We got to beginning of string with only numbers, count start as a delimiter
        // and exit
        return numbers + 1;
    }

    /**
     * Get a String in a line of text between two marker strings
     *
     * @param text   Text to search for match
     * @param before Start matching after this text
     * @param after  End matching before this text
     * @return Text between the strings before and after, or empty string if either
     * marker does not exist
     */
    public static String getTextBetweenStrings(String text, String before, String after) {

        String result = Normal.EMPTY;

        if (text.indexOf(before) >= 0 && text.indexOf(after) >= 0) {
            result = text.substring(text.indexOf(before) + before.length(), text.length());
            result = result.substring(0, result.indexOf(after));
        }
        return result;
    }

    /**
     * Convert a long representing filetime (100-ns since 1601 epoch) to ms since
     * 1970 epoch
     *
     * @param filetime A 64-bit value equivalent to FILETIME
     * @param local    True if converting from a local filetime (PDH counter); false if
     *                 already UTC (WMI PerfRawData classes)
     * @return Equivalent milliseconds since the epoch
     */
    public static long filetimeToUtcMs(long filetime, boolean local) {
        return filetime / 10_000L - EPOCH_DIFF - (local ? TZ_OFFSET : 0L);
    }

    /**
     * Parse a date in MM-DD-YYYY or MM/DD/YYYY to YYYY-MM-DD
     *
     * @param dateString The date in MM DD YYYY format
     * @return The date in ISO YYYY-MM-DD format if parseable, or the original
     * string
     */
    public static String parseMmDdYyyyToYyyyMmDD(String dateString) {
        try {
            // Date is MM-DD-YYYY, convert to YYYY-MM-DD
            return String.format("%s-%s-%s", dateString.substring(6, 10), dateString.substring(0, 2),
                    dateString.substring(3, 5));
        } catch (StringIndexOutOfBoundsException e) {
            return dateString;
        }
    }

    /**
     * Converts a string in CIM Date Format, as returned by WMI for DateTime types,
     * into a {@link java.time.OffsetDateTime}.
     *
     * @param cimDateTime A non-null DateTime String in CIM date format, e.g.,
     *                    <code>20160513072950.782000-420</code>
     * @return The parsed {@link java.time.OffsetDateTime} if the string is
     * parsable, otherwise {@link Builder#UNIX_EPOCH}.
     */
    public static OffsetDateTime parseCimDateTimeToOffset(String cimDateTime) {
        // Keep first 22 characters: digits, decimal, and + or - sign
        // But alter last 3 characters from a minute offset to hh:mm
        try {
            // From WMI as 20160513072950.782000-420,
            int tzInMinutes = Integer.parseInt(cimDateTime.substring(22));
            // modified to 20160513072950.782000-07:00 which can be parsed
            LocalTime offsetAsLocalTime = LocalTime.MIDNIGHT.plusMinutes(tzInMinutes);
            return OffsetDateTime.parse(
                    cimDateTime.substring(0, 22) + offsetAsLocalTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
                    Builder.CIM_FORMAT);
        } catch (IndexOutOfBoundsException // if cimDate not 22+ chars
                | NumberFormatException // if TZ minutes doesn't parse
                | DateTimeParseException e) {
            Logger.trace("Unable to parse {} to CIM DateTime.", cimDateTime);
            return Builder.UNIX_EPOCH;
        }
    }

    /**
     * Checks if a file path equals or starts with an prefix in the given list
     *
     * @param prefixList A list of path prefixes
     * @param path       a string path to check
     * @return true if the path exactly equals, or starts with one of the strings in
     * prefixList
     */
    public static boolean filePathStartsWith(List<String> prefixList, String path) {
        for (String match : prefixList) {
            if (path.equals(match) || path.startsWith(match + Symbol.SLASH)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses a string such as "4096 MB" to its long. Used to parse macOS and *nix
     * memory chip sizes. Although the units given are decimal they must parse to
     * binary units.
     *
     * @param size A string of memory sizes like "4096 MB"
     * @return the size parsed to a long
     */
    public static long parseDecimalMemorySizeToBinary(String size) {
        String[] mem = RegEx.SPACES.split(size);
        if (mem.length < 2) {
            // If no spaces, use regexp
            Matcher matcher = BYTES_PATTERN.matcher(size.trim());
            if (matcher.find() && matcher.groupCount() == 2) {
                mem = new String[2];
                mem[0] = matcher.group(1);
                mem[1] = matcher.group(2);
            }
        }
        long capacity = Builder.parseLongOrDefault(mem[0], 0L);
        if (mem.length == 2 && mem[1].length() > 1) {
            switch (mem[1].charAt(0)) {
                case 'T':
                    capacity <<= 40;
                    break;
                case 'G':
                    capacity <<= 30;
                    break;
                case 'M':
                    capacity <<= 20;
                    break;
                case 'K':
                case 'k':
                    capacity <<= 10;
                    break;
                default:
                    break;
            }
        }
        return capacity;
    }

    /**
     * Parse a Windows PnPDeviceID to get the vendor ID and product ID.
     *
     * @param pnpDeviceId The PnPDeviceID
     * @return A {@link Pair} where the first element is the vendor ID and second
     * element is the product ID, if parsing was successful, or {@code null}
     * otherwise
     */
    public static Pair<String, String> parsePnPDeviceIdToVendorProductId(String pnpDeviceId) {
        Matcher m = VENDOR_PRODUCT_ID.matcher(pnpDeviceId);
        if (m.matches()) {
            String vendorId = "0x" + m.group(1).toLowerCase();
            String productId = "0x" + m.group(2).toLowerCase();
            return Pair.of(vendorId, productId);
        }
        return null;
    }

    /**
     * Parse a Linux lshw resources string to calculate the memory size
     *
     * @param resources A string containing one or more elements of the form
     *                  {@code memory:b00000000-bffffffff}
     * @return The number of bytes consumed by the memory in the {@code resources}
     * string
     */
    public static long parseLshwResourceString(String resources) {
        long bytes = 0L;
        // First split by whitespace
        String[] resourceArray = RegEx.SPACES.split(resources);
        for (String r : resourceArray) {
            // Remove prefix
            if (r.startsWith("memory:")) {
                // Split to low and high
                String[] mem = r.substring(7).split(Symbol.HYPHEN);
                if (mem.length == 2) {
                    try {
                        // Parse the hex strings
                        bytes += Long.parseLong(mem[1], 16) - Long.parseLong(mem[0], 16) + 1;
                    } catch (NumberFormatException e) {
                        Logger.trace(MESSAGE, r, e);
                    }
                }
            }
        }
        return bytes;
    }

    /**
     * Parse a Linux lspci machine readble line to its name and id
     *
     * @param line A string in the form Foo [bar]
     * @return A pair separating the String before the square brackets and within
     * them if found, null otherwise
     */
    public static Pair<String, String> parseLspciMachineReadable(String line) {
        Matcher matcher = LSPCI_MACHINE_READABLE.matcher(line);
        if (matcher.matches()) {
            return Pair.of(matcher.group(1), matcher.group(2));
        }
        return null;
    }

    /**
     * Parse a Linux lspci line containing memory size
     *
     * @param line A string in the form Foo [size=256M]
     * @return A the memory size in bytes
     */
    public static long parseLspciMemorySize(String line) {
        Matcher matcher = LSPCI_MEMORY_SIZE.matcher(line);
        if (matcher.matches()) {
            return parseDecimalMemorySizeToBinary(matcher.group(1) + Symbol.SPACE + matcher.group(2) + "B");
        }
        return 0;
    }

    /**
     * Parse a space-delimited list of integers which include hyphenated ranges to a
     * list of just the integers. For example, 0 1 4-7 parses to a list containing
     * 0, 1, 4, 5, 6, and 7.
     *
     * @param str A string containing space-delimited integers or ranges of integers
     *            with a hyphen
     * @return A list of integers representing the provided range(s).
     */
    public static List<Integer> parseHyphenatedIntList(String str) {
        List<Integer> result = new ArrayList<>();
        for (String s : RegEx.SPACES.split(str)) {
            if (s.contains(Symbol.HYPHEN)) {
                int first = getFirstIntValue(s);
                int last = getNthIntValue(s, 2);
                for (int i = first; i <= last; i++) {
                    result.add(i);
                }
            } else {
                int only = Builder.parseIntOrDefault(s, -1);
                if (only >= 0) {
                    result.add(only);
                }
            }
        }
        return result;
    }

    /**
     * Parse an integer in big endian IP format to its component bytes representing
     * an IPv4 address
     *
     * @param ip The address as an integer
     * @return The address as an array of four bytes
     */
    public static byte[] parseIntToIP(int ip) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ip).array();
    }

    /**
     * Parse an integer array in big endian IP format to its component bytes
     * representing an IPv6 address
     *
     * @param ip6 The address as an integer array
     * @return The address as an array of sizteen bytes
     */
    public static byte[] parseIntArrayToIP(int[] ip6) {
        ByteBuffer bb = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        for (int i : ip6) {
            bb.putInt(i);
        }
        return bb.array();
    }

    /**
     * TCP network addresses and ports are in big endian format by definition. The
     * order of the two bytes in the 16-bit unsigned short port value must be
     * reversed
     *
     * @param port The port number in big endian order
     * @return The port number
     * @see <a href=
     * "https://docs.microsoft.com/en-us/windows/win32/api/winsock/nf-winsock-ntohs">ntohs</a>
     */
    public static int bigEndian16ToLittleEndian(int port) {
        // 20480 = 0x5000 should be 0x0050 = 80
        // 47873 = 0xBB01 should be 0x01BB = 443
        return port >> 8 & 0xff | port << 8 & 0xff00;
    }

    /**
     * 输出到<code>StringBuilder</code>
     *
     * @param builder <code>StringBuilder</code>对象
     * @param caption 标题
     * @param value   值
     */
    public static void append(StringBuilder builder, String caption, Object value) {
        builder.append(caption).append(StringKit.nullToDefault(Convert.toString(value), "[n/a]")).append("\n");
    }

    /**
     * Parse an integer array to an IPv4 or IPv6 as appropriate.
     * <p>
     * Intended for use on Utmp structures's {@code ut_addr_v6} element.
     *
     * @param utAddrV6 An array of 4 integers representing an IPv6 address. IPv4 address
     *                 uses just utAddrV6[0]
     * @return A string representation of the IP address.
     */
    public static String parseUtAddrV6toIP(int[] utAddrV6) {
        if (utAddrV6.length != 4) {
            throw new IllegalArgumentException("ut_addr_v6 must have exactly 4 elements");
        }
        // IPv4 has only first element
        if (utAddrV6[1] == 0 && utAddrV6[2] == 0 && utAddrV6[3] == 0) {
            // Special case for all 0's
            if (utAddrV6[0] == 0) {
                return "::";
            }
            // Parse using InetAddress
            byte[] ipv4 = ByteBuffer.allocate(4).putInt(utAddrV6[0]).array();
            try {
                return InetAddress.getByAddress(ipv4).getHostAddress();
            } catch (UnknownHostException e) {
                // Shouldn't happen with length 4 or 16
                return Normal.UNKNOWN;
            }
        }
        // Parse all 16 bytes
        byte[] ipv6 = ByteBuffer.allocate(16).putInt(utAddrV6[0]).putInt(utAddrV6[1]).putInt(utAddrV6[2])
                .putInt(utAddrV6[3]).array();
        try {
            return InetAddress.getByAddress(ipv6).getHostAddress()
                    .replaceAll("((?:(?:^|:)0+\\b){2,}):?(?!\\S*\\b\\1:0+\\b)(\\S*)", "::$2");
        } catch (UnknownHostException e) {
            // Shouldn't happen with length 4 or 16
            return Normal.UNKNOWN;
        }
    }

    /**
     * Gets open files
     *
     * @param pid The process ID
     * @return the number of open files.
     */
    public static long getOpenFiles(int pid) {
        int openFiles = Executor.runNative("lsof -p " + pid).size();
        return openFiles > 0 ? openFiles - 1L : 0L;
    }

    /**
     * Tests if a String matches another String with a wildcard pattern.
     *
     * @param text    The String to test
     * @param pattern The String containing a wildcard pattern where ? represents a
     *                single character and * represents any number of characters. If the
     *                first character of the pattern is a carat (^) the test is
     *                performed against the remaining characters and the result of the
     *                test is the opposite.
     * @return True if the String matches or if the first character is ^ and the
     * remainder of the String does not match.
     */
    public static boolean wildcardMatch(String text, String pattern) {
        if (pattern.length() > 0 && pattern.charAt(0) == Symbol.C_CARET) {
            return !wildcardMatch(text, pattern.substring(1));
        }
        return text.matches(pattern.replace(Symbol.QUESTION_MARK, Symbol.DOT + Symbol.QUESTION_MARK).replace(Symbol.STAR, Symbol.DOT + Symbol.STAR + Symbol.QUESTION_MARK));
    }

    /**
     * Parses a string of hex digits to an int value.
     *
     * @param hexString    A sequence of hex digits
     * @param defaultValue default value to return if parsefails
     * @return The corresponding int value
     */
    public static int hexStringToInt(String hexString, int defaultValue) {
        if (null != hexString) {
            try {
                if (hexString.startsWith("0x")) {
                    return new BigInteger(hexString.substring(2), 16).intValue();
                } else {
                    return new BigInteger(hexString, 16).intValue();
                }
            } catch (NumberFormatException e) {
                Logger.trace(MESSAGE, hexString, e);
            }
        }
        // Hex failed to parse, just return the default long
        return defaultValue;
    }

    /**
     * Parses a string of hex digits to long value.
     *
     * @param hexString    A sequence of hex digits
     * @param defaultValue default value to return if parsefails
     * @return The corresponding long value
     */
    public static long hexStringToLong(String hexString, long defaultValue) {
        if (null != hexString) {
            try {
                if (hexString.startsWith("0x")) {
                    return new BigInteger(hexString.substring(2), 16).longValue();
                } else {
                    return new BigInteger(hexString, 16).longValue();
                }
            } catch (NumberFormatException e) {
                Logger.trace(MESSAGE, hexString, e);
            }
        }
        // Hex failed to parse, just return the default long
        return defaultValue;
    }

    /**
     * Parses a String "....foo" to "foo"
     *
     * @param dotPrefixedStr A string with possibly leading dots
     * @return The string without the dots
     */
    public static String removeLeadingDots(String dotPrefixedStr) {
        int pos = 0;
        while (pos < dotPrefixedStr.length() && dotPrefixedStr.charAt(pos) == Symbol.C_DOT) {
            pos++;
        }
        return pos < dotPrefixedStr.length() ? dotPrefixedStr.substring(pos) : Normal.EMPTY;
    }

    /**
     * Reads the target of a symbolic link
     *
     * @param file The file to read
     * @return The symlink name, or null if the read failed
     */
    public static String readSymlinkTarget(File file) {
        try {
            return Files.readSymbolicLink(Paths.get(file.getAbsolutePath())).toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Rounds a floating point number to the nearest integer
     *
     * @param x the floating point number
     * @return the integer
     */
    public static int roundToInt(double x) {
        return (int) Math.round(x);
    }

    /**
     * Evaluates if file store (identified by {@code path} and {@code volume})
     * should be excluded or not based on configuration
     * {@code pathIncludes, pathExcludes, volumeIncludes, volumeExcludes}.
     * <p>
     * Inclusion has priority over exclusion. If no exclusion/inclusion pattern is
     * specified, then filestore is not excluded.
     *
     * @param path           Mountpoint of filestore.
     * @param volume         Filestore volume.
     * @param pathIncludes   List of patterns for path inclusions.
     * @param pathExcludes   List of patterns for path exclusions.
     * @param volumeIncludes List of patterns for volume inclusions.
     * @param volumeExcludes List of patterns for volume exclusions.
     * @return {@code true} if file store should be excluded or {@code false}
     * otherwise.
     */
    public static boolean isFileStoreExcluded(String path, String volume,
                                              List<PathMatcher> pathIncludes,
                                              List<PathMatcher> pathExcludes,
                                              List<PathMatcher> volumeIncludes,
                                              List<PathMatcher> volumeExcludes) {
        Path p = Paths.get(path);
        Path v = Paths.get(volume);
        if (matches(p, pathIncludes) || matches(v, volumeIncludes)) {
            return false;
        }
        return matches(p, pathExcludes) || matches(v, volumeExcludes);
    }

    /**
     * Load from config and parse file system include/exclude line.
     *
     * @param configPropertyName The config property containing the line to be parsed.
     * @return List of PathMatchers to be used to match filestore volume and path.
     */
    public static List<PathMatcher> loadAndParseFileSystemConfig(String configPropertyName) {
        String config = Config.get(configPropertyName, Normal.EMPTY);
        return parseFileSystemConfig(config);
    }

    /**
     * Parse file system include/exclude line.
     *
     * @param config The config line to be parsed.
     * @return List of PathMatchers to be used to match filestore volume and path.
     */
    public static List<PathMatcher> parseFileSystemConfig(String config) {
        FileSystem fs = FileSystems.getDefault();
        List<PathMatcher> patterns = new ArrayList<>();
        for (String item : config.split(",")) {
            if (item.length() > 0) {
                // Using glob: prefix as the defult unless user has specified glob or regex. See
                // https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-
                if (!(item.startsWith(GLOB_PREFIX) || item.startsWith(REGEX_PREFIX))) {
                    item = GLOB_PREFIX + item;
                }
                patterns.add(fs.getPathMatcher(item));
            }
        }
        return patterns;
    }

    /**
     * Checks if {@code text} matches any of @param patterns}.
     *
     * @param text     The text to be matched.
     * @param patterns List of patterns.
     * @return {@code true} if given text matches at least one glob pattern or
     * {@code false} otherwise.
     * @see <a href="https://en.wikipedia.org/wiki/Glob_(programming)">Wikipedia -
     * glob (programming)</a>
     */
    public static boolean matches(Path text, List<PathMatcher> patterns) {
        for (PathMatcher pattern : patterns) {
            if (pattern.matches(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses a string like "53G" or "54.904 M" to its long value.
     *
     * @param count A count with a multiplyer like "4096 M"
     * @return the count parsed to a long
     */
    public static long parseMultipliedToLongs(String count) {
        Matcher matcher = Pattern.compile("(\\d+(.\\d+)?)[\\s]?([kKMGT])?").matcher(count.trim());
        String[] mem;
        if (matcher.find() && matcher.groupCount() == 3) {
            mem = new String[2];
            mem[0] = matcher.group(1);
            mem[1] = matcher.group(3);
        } else {
            mem = new String[]{count};
        }

        double number = parseDoubleOrDefault(mem[0], 0L);
        if (mem.length == 2 && null != mem[1] && mem[1].length() >= 1) {
            switch ((mem[1].charAt(0))) {
                case 'T':
                    number *= 1_000_000_000_000L;
                    break;
                case 'G':
                    number *= 1_000_000_000L;
                    break;
                case 'M':
                    number *= 1_000_000L;
                    break;
                case 'K':
                case 'k':
                    number *= 1_000L;
                    break;
                default:
            }
        }
        return (long) number;
    }

}
