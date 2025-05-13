/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.*;

import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.health.builtin.*;
import org.miaixz.bus.health.builtin.hardware.*;
import org.miaixz.bus.health.builtin.software.FileSystem;
import org.miaixz.bus.health.builtin.software.OSFileStore;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.OperatingSystem;

/**
 * 服务器信息收集
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class Provider implements org.miaixz.bus.core.Provider {

    public static void main(String[] args) {
        System.out.println(Platform.INSTANCE.getOperatingSystem().toString());
    }

    public static List<Inet4Address> getLocalIp4() throws SocketException {
        List<Inet4Address> addresses = new ArrayList<>(1);
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        if (e == null) {
            return addresses;
        }
        while (e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            if (!isValidInterface(n)) {
                continue;
            }
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {
                InetAddress i = (InetAddress) ee.nextElement();
                if (isValidAddress(i)) {
                    addresses.add((Inet4Address) i);
                }
            }
        }
        return addresses;
    }

    /**
     * 过滤回环网卡、点对点网卡、非活动网卡、虚拟网卡并要求网卡名字是eth或ens开头
     *
     * @param ni 网卡
     * @return 如果满足要求则true，否则false
     */
    private static boolean isValidInterface(NetworkInterface ni) throws SocketException {
        return !ni.isLoopback() && !ni.isPointToPoint() && ni.isUp() && !ni.isVirtual()
                && (ni.getName().startsWith("eth") || ni.getName().startsWith("ens"));
    }

    /**
     * 判断是否是IPv4，并且内网地址并过滤回环地址.
     */
    private static boolean isValidAddress(InetAddress address) {
        return address instanceof Inet4Address && address.isSiteLocalAddress() && !address.isLoopbackAddress();
    }

    /**
     * 获取硬件抽象层信息
     *
     * @return {@link HardwareAbstractionLayer}
     */
    public HardwareAbstractionLayer getHardware() {
        return Platform.INSTANCE.getHardware();
    }

    /**
     * 操作系统
     *
     * @return {@link OperatingSystem}
     */
    public OperatingSystem getOperatingSystem() {
        return Platform.INSTANCE.getOperatingSystem();
    }

    /**
     * 中央处理器
     *
     * @return {@link CentralProcessor}
     */
    public CentralProcessor getProcessor() {
        return getHardware().getProcessor();
    }

    /**
     * 获取计算机系统信息
     *
     * @return {@link ComputerSystem}
     */
    public ComputerSystem getComputerSystem() {
        return getHardware().getComputerSystem();
    }

    /**
     * 获取系统信息
     *
     * @return {@link Host}
     */
    public Host getHost() {
        long rxBytesBegin = 0;
        long txBytesBegin = 0;
        long rxPacketsBegin = 0;
        long txPacketsBegin = 0;
        long rxBytesEnd = 0;
        long txBytesEnd = 0;
        long rxPacketsEnd = 0;
        long txPacketsEnd = 0;
        HardwareAbstractionLayer hal = getHardware();
        List<NetworkIF> listBegin = hal.getNetworkIFs();
        for (NetworkIF net : listBegin) {
            rxBytesBegin += net.getBytesRecv();
            txBytesBegin += net.getBytesSent();
            rxPacketsBegin += net.getPacketsRecv();
            txPacketsBegin += net.getPacketsSent();
        }

        // 暂停3秒以计算平均值
        ThreadKit.sleep(3000);

        List<NetworkIF> listEnd = hal.getNetworkIFs();
        for (NetworkIF net : listEnd) {
            rxBytesEnd += net.getBytesRecv();
            txBytesEnd += net.getBytesSent();
            rxPacketsEnd += net.getPacketsRecv();
            txPacketsEnd += net.getPacketsSent();
        }
        InetAddress inetAddress = NetKit.getLocalhostV4();
        return Host.builder().name(inetAddress.getHostName()).ip(inetAddress.getHostAddress())
                .os(Keys.get(Keys.OS_NAME)).osArch(Keys.get(Keys.OS_ARCH)).userDir(Keys.get(Keys.USER_DIR))
                .rxBytesPerSecond(formatDouble((rxBytesEnd - rxBytesBegin) / 3.0 / 1024))
                .txBytesPerSecond(formatDouble((txBytesEnd - txBytesBegin) / 3.0 / 1024))
                .rxPacketsPerSecond(formatDouble((rxPacketsEnd - rxPacketsBegin) / 3.0))
                .txPacketsPerSecond(formatDouble((txPacketsEnd - txPacketsBegin) / 3.0)).build();
    }

    /**
     * 获取所有磁盘使用率信息并添加到结果映射。
     *
     * @param type 类型标识
     * @param map  结果映射
     */
    public void appendAllDisk(String type, Map<String, Object> map) {
        List<Disk> diskList = getDisk();
        if (CollKit.isNotEmpty(diskList)) {
            long usableSpace = 0;
            long totalSpace = 0;
            for (Disk disk : diskList) {
                usableSpace += disk.getUsedSpace();
                totalSpace += disk.getTotalSpace();
            }
            double usedSize = (totalSpace - usableSpace);
            map.put(type, formatDouble((usedSize / totalSpace) * 100));
        }
    }

    /**
     * 获取指定数量的进程列表（按 CPU 使用率降序排序）。
     *
     * @param limitNumber 限制返回的进程数量
     * @param type        类型标识
     * @param map         结果映射
     */
    public void appendProcessList(Integer limitNumber, String type, Map<String, Object> map) {
        List<OSProcess> processList = getOperatingSystem().getProcesses(null, OperatingSystem.ProcessSorting.CPU_DESC,
                limitNumber);
        List<Map<String, Object>> processMapList = new ArrayList<>();
        for (OSProcess process : processList) {
            Map<String, Object> processMap = new HashMap<>(5);
            processMap.put("name", process.getName());
            processMap.put("pid", process.getProcessID());
            processMap.put(TID.CPU, formatDouble(process.getProcessCpuLoadCumulative()));
            processMapList.add(processMap);
        }
        map.put(type, processMapList);
    }

    /**
     * 获取硬件信息并添加到结果映射。
     *
     * @param type 类型标识
     * @param map  结果映射
     */
    public void appendHardware(String type, Map<String, Object> map) {
        Map<String, Object> hardwareMap = new HashMap<>();
        hardwareMap.put(TID.MEMORY, getHardware().getMemory());
        hardwareMap.put("sensors", getHardware().getSensors());
        hardwareMap.put("logicalVolumeGroups", getHardware().getLogicalVolumeGroups());
        hardwareMap.put("soundCards", getHardware().getSoundCards());
        hardwareMap.put("graphicsCards", getHardware().getGraphicsCards());
        hardwareMap.put("displays", getHardware().getDisplays());
        hardwareMap.put("diskStores", getHardware().getDiskStores());
        hardwareMap.put("powerSources", getHardware().getPowerSources());
        hardwareMap.put("networkIFs", getHardware().getNetworkIFs());
        map.put(type, hardwareMap);
    }

    /**
     * 根据类型添加系统或硬件信息到结果映射。
     *
     * @param type 类型标识
     * @param map  结果映射
     */
    public void append(String type, Map<String, Object> map) {
        switch (type.toLowerCase()) {
        case TID.HOST:
            map.put(type, getHost());
            break;
        case TID.CPU:
            map.put(type, getCpu());
            break;
        case TID.DISK:
            map.put(type, getDisk());
            break;
        case TID.JVM:
            map.put(type, getJvm());
            break;
        case TID.MEMORY:
            map.put(type, getMemory());
            break;
        case TID.ALL_DISK:
            appendAllDisk(type, map);
            break;
        case TID.PROCESS:
            appendProcessList(10, type, map);
            break;
        case TID.SYSTEM:
            map.put(type, getComputerSystem());
            break;
        case TID.PROCESSOR:
            map.put(type, getProcessor());
            break;
        case TID.HARDWARE:
            appendHardware(type, map);
            break;
        case TID.POWERSOURCES:
            map.put(type, getPowerSourceInfo());
            break;
        case TID.NETWORKIFS:
            map.put(type, getNetworkInfo());
            break;
        default:
            break;
        }
        map.put("timestamp", DateKit.current());
    }

    /**
     * 获取系统所有支持的监控信息。
     *
     * @return 包含所有类型信息的映射
     */
    public Map<String, Object> getAll() {
        return get(TID.ALL_TID);
    }

    /**
     * 获取单个类型的系统或硬件信息。
     *
     * @param type 类型标识
     * @return 包含指定类型信息的映射
     */
    public Map<String, Object> getSingle(String type) {
        Map<String, Object> map = new HashMap<>(1);
        append(type, map);
        return map;
    }

    /**
     * 根据类型列表获取系统或硬件信息。
     *
     * @param list 类型列表
     * @return 包含指定类型信息的映射
     */
    public Map<String, Object> get(List<String> list) {
        Map<String, Object> map = new HashMap<>(5);
        for (String type : list) {
            append(type, map);
        }
        return map;
    }

    /**
     * 获取指定进程的信息。
     *
     * @param pid 进程 ID
     * @return 包含指定进程信息的映射
     */
    public Map<String, Object> getProcessById(int pid) {
        OSProcess process = getOperatingSystem().getProcess(pid);
        if (process == null) {
            return new HashMap<>();
        }
        Map<String, Object> processMap = new HashMap<>(5);
        processMap.put("name", process.getName());
        processMap.put("pid", process.getProcessID());
        processMap.put(TID.CPU, formatDouble(process.getProcessCpuLoadCumulative()));
        processMap.put(TID.MEMORY, process.getResidentSetSize());
        processMap.put("state", process.getState().toString());
        return processMap;
    }

    /**
     * 获取 CPU 信息
     *
     * @return {@link Cpu}
     */
    public Cpu getCpu() {
        CentralProcessor centralProcessor = getProcessor();
        long[] prevTicks = centralProcessor.getSystemCpuLoadTicks();
        ThreadKit.sleep(600);
        long[] ticks = centralProcessor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()]
                - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()]
                - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()]
                - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()]
                - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long ioWait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()]
                - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()]
                - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + sys + idle + ioWait + irq + softirq + steal;
        return Cpu.builder().physicalCores(centralProcessor.getPhysicalProcessorCount())
                .logicalCores(centralProcessor.getLogicalProcessorCount())
                .systemUsage(formatDouble(sys * 100.0 / totalCpu)).userUsage(formatDouble(user * 100.0 / totalCpu))
                .ioWait(formatDouble(ioWait * 100.0 / totalCpu))
                .totalUsage(formatDouble((totalCpu - idle) * 100.0 / totalCpu)).build();
    }

    /**
     * 获取内存使用信息
     *
     * @return {@link Memory}
     */
    public Memory getMemory() {
        GlobalMemory globalMemory = getHardware().getMemory();
        long totalByte = globalMemory.getTotal();
        long availableByte = globalMemory.getAvailable();
        return Memory.builder().total(formatByte(totalByte)).used(formatByte(totalByte - availableByte))
                .free(formatByte(availableByte)).usage(formatDouble((totalByte - availableByte) * 100.0 / totalByte))
                .build();
    }

    /**
     * 获取 JVM 信息
     *
     * @return {@link Jvm}
     */
    public Jvm getJvm() {
        Runtime runtime = Runtime.getRuntime();
        long jvmTotalMemoryByte = runtime.totalMemory();
        long freeMemoryByte = runtime.freeMemory();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Jvm.builder().jdkVersion(Keys.get(Keys.JAVA_VERSION)).jdkHome(Keys.get(Keys.JAVA_HOME))
                .jdkName(runtimeMXBean.getVmName()).totalMemory(jvmTotalMemoryByte).maxMemory(runtime.maxMemory())
                .freeMemory(freeMemoryByte).usedMemory(jvmTotalMemoryByte - freeMemoryByte)
                .usagePercent(formatDouble((jvmTotalMemoryByte - freeMemoryByte) * 100.0 / jvmTotalMemoryByte))
                .startTime(runtimeMXBean.getStartTime()).uptime(runtimeMXBean.getUptime()).build();
    }

    /**
     * 获取磁盘使用信息
     *
     * @return List of {@link Disk}
     */
    public List<Disk> getDisk() {
        OperatingSystem operatingSystem = getOperatingSystem();
        FileSystem fileSystem = operatingSystem.getFileSystem();
        List<Disk> disks = new ArrayList<>();
        Iterable<OSFileStore> fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray) {
            long total = fs.getTotalSpace();
            long usable = fs.getUsableSpace();
            disks.add(Disk.builder().deviceName(fs.getName()).volumeName(fs.getVolume()).label(fs.getLabel())
                    .logicalVolumeName(fs.getLogicalVolume()).mountPoint(fs.getMount()).description(fs.getDescription())
                    .mountOptions(fs.getOptions()).filesystemType(fs.getType()).uuid(fs.getUUID()).totalSpace(total)
                    .usedSpace(total - usable).freeSpace(usable)
                    .usagePercent(total > 0 ? formatDouble((total - usable) * 100.0 / total) : 0.0).build());
        }
        return disks;
    }

    /**
     * 获取当前系统的网络接口信息。
     *
     * @return 包含网络接口信息的映射
     */
    public Map<String, Object> getNetworkInfo() {
        Map<String, Object> map = new HashMap<>(1);
        map.put("networkIFs", getHardware().getNetworkIFs());
        return map;
    }

    /**
     * 获取当前系统的电源信息。
     *
     * @return 包含电源信息的映射
     */
    public Map<String, Object> getPowerSourceInfo() {
        Map<String, Object> map = new HashMap<>(1);
        map.put("powerSources", getHardware().getPowerSources());
        return map;
    }

    /**
     * 格式化字节为人类可读的单位（KB, MB, GB, TB）
     *
     * @param byteNumber 字节数
     * @return 格式化后的字符串
     */
    public String formatByte(long byteNumber) {
        double FORMAT = 1024.0;
        double kbNumber = byteNumber / FORMAT;
        if (kbNumber < FORMAT) {
            return formatDecimal("#.##KB", kbNumber);
        }
        double mbNumber = kbNumber / FORMAT;
        if (mbNumber < FORMAT) {
            return formatDecimal("#.##MB", mbNumber);
        }
        double gbNumber = mbNumber / FORMAT;
        if (gbNumber < FORMAT) {
            return formatDecimal("#.##GB", gbNumber);
        }
        return formatDecimal("#.##TB", gbNumber / FORMAT);
    }

    /**
     * 格式化数字为指定模式
     *
     * @param pattern 格式模式
     * @param number  数字
     * @return 格式化后的字符串
     */
    public String formatDecimal(String pattern, double number) {
        return new DecimalFormat(pattern).format(number);
    }

    /**
     * 格式化双精度浮点数，保留两位小数
     *
     * @param value 输入值
     * @return 格式化后的双精度值
     */
    public double formatDouble(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     *
     * @return the {@link int}
     */
    @Override
    public Object type() {
        return Platform.INSTANCE.getOSType();
    }

}