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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.LambdaKit;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.health.builtin.*;
import org.miaixz.bus.health.builtin.hardware.*;
import org.miaixz.bus.health.builtin.software.FileSystem;
import org.miaixz.bus.health.builtin.software.OSFileStore;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.OperatingSystem;

/**
 * Collects local operating-system, hardware, process, JVM, and runtime health information.
 *
 * @author Kimi Liu
 */
public class Collector {

    /**
     * Process response retaining the established public names independently of the operating-system model.
     *
     * @param name   process display name
     * @param pid    operating-system process identity
     * @param cpu    cumulative CPU usage
     * @param memory resident memory, absent from the compact process list
     * @param state  process state, absent from the compact process list
     * @author Kimi Liu
     */
    public record ProcessSummary(String name, int pid, double cpu, Long memory, String state) {

    }

    /**
     * Network address names retained by the health contract when hardware getter capitalization differs.
     *
     * @param mtu      interface maximum transmission unit
     * @param ipv4addr interface IPv4 addresses
     * @param ipv6addr interface IPv6 addresses
     * @author Kimi Liu
     */
    public record NetworkAddresses(long mtu, String[] ipv4addr, String[] ipv6addr) {

    }

    /**
     * Video memory projection retaining the established capitalized response member.
     *
     * @param VRam video memory capacity in bytes
     * @author Kimi Liu
     */
    public record VideoMemory(long VRam) {

    }

    /**
     * Creates a new Collector instance.
     */
    public Collector() {
        // No initialization required.
    }

    /**
     * Retrieves a list of local IPv4 addresses.
     *
     * @return A list of {@link Inet4Address}.
     * @throws SocketException if a socket error occurs.
     */
    public static List<Inet4Address> getLocalIp4() throws SocketException {
        List<Inet4Address> addresses = new ArrayList<>(1);
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        if (e == null) {
            return addresses;
        }
        while (e.hasMoreElements()) {
            NetworkInterface n = e.nextElement();
            if (!isValidInterface(n)) {
                continue;
            }
            Enumeration<InetAddress> ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {
                InetAddress i = ee.nextElement();
                if (isValidAddress(i)) {
                    addresses.add((Inet4Address) i);
                }
            }
        }
        return addresses;
    }

    /**
     * Filters out loopback, point-to-point, inactive, and virtual network interfaces, and requires the interface name
     * to start with "eth" or "ens".
     *
     * @param ni The network interface.
     * @return {@code true} if the interface meets the criteria, {@code false} otherwise.
     * @throws SocketException if a socket error occurs.
     */
    private static boolean isValidInterface(NetworkInterface ni) throws SocketException {
        return !ni.isLoopback() && !ni.isPointToPoint() && ni.isUp() && !ni.isVirtual()
                && (ni.getName().startsWith("eth") || ni.getName().startsWith("ens"));
    }

    /**
     * Checks if an address is an IPv4, site-local, and not a loopback address.
     *
     * @param address The address to check.
     * @return {@code true} if the address is valid, {@code false} otherwise.
     */
    private static boolean isValidAddress(InetAddress address) {
        return address instanceof Inet4Address && address.isSiteLocalAddress() && !address.isLoopbackAddress();
    }

    /**
     * Gets the hardware abstraction layer information.
     *
     * @return The {@link HardwareAbstractionLayer}.
     */
    public HardwareAbstractionLayer getHardware() {
        return Platform.INSTANCE.getHardware();
    }

    /**
     * Gets the operating system information.
     *
     * @return The {@link OperatingSystem}.
     */
    public OperatingSystem getOperatingSystem() {
        return Platform.INSTANCE.getOperatingSystem();
    }

    /**
     * Gets the central processor information.
     *
     * @return The {@link CentralProcessor}.
     */
    public CentralProcessor getProcessor() {
        return getHardware().getProcessor();
    }

    /**
     * Gets the computer system information.
     *
     * @return The {@link ComputerSystem}.
     */
    public ComputerSystem getComputerSystem() {
        return getHardware().getComputerSystem();
    }

    /**
     * Gets the system host information.
     *
     * @return A {@link Host} object.
     */
    public Host getHost() {
        long rxBytesBegin = 0;
        long txBytesBegin = 0;
        long rxPacketsBegin = 0;
        long txPacketsBegin = 0;
        HardwareAbstractionLayer hal = getHardware();
        List<NetworkIF> listBegin = hal.getNetworkIFs();
        for (NetworkIF net : listBegin) {
            rxBytesBegin += net.getBytesRecv();
            txBytesBegin += net.getBytesSent();
            rxPacketsBegin += net.getPacketsRecv();
            txPacketsBegin += net.getPacketsSent();
        }

        // Pause for 3 seconds to calculate the average
        ThreadKit.sleep(3000);

        long rxBytesEnd = 0;
        long txBytesEnd = 0;
        long rxPacketsEnd = 0;
        long txPacketsEnd = 0;
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
                .rxBytesPerSecond(Formats.round((rxBytesEnd - rxBytesBegin) / 3.0 / 1024))
                .txBytesPerSecond(Formats.round((txBytesEnd - txBytesBegin) / 3.0 / 1024))
                .rxPacketsPerSecond(Formats.round((rxPacketsEnd - rxPacketsBegin) / 3.0))
                .txPacketsPerSecond(Formats.round((txPacketsEnd - txPacketsBegin) / 3.0)).build();
    }

    /**
     * Appends all disk usage information to the result map.
     *
     * @param type The type identifier.
     * @param map  The result map.
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
            map.put(type, Formats.round((usedSize / totalSpace) * 100));
        }
    }

    /**
     * Gets a list of processes sorted by CPU usage in descending order.
     *
     * @param limitNumber The maximum number of processes to return.
     * @param type        The type identifier.
     * @param map         The result map.
     */
    public void appendProcessList(Integer limitNumber, String type, Map<String, Object> map) {
        List<OSProcess> processList = getOperatingSystem()
                .getProcesses(null, OperatingSystem.ProcessSorting.CPU_DESC, limitNumber);
        List<Map<String, Object>> processMapList = new ArrayList<>();
        for (OSProcess process : processList) {
            Map<String, Object> processMap = BeanKit.beanToMap(
                    new ProcessSummary(process.getName(), process.getProcessID(),
                            Formats.round(process.getProcessCpuLoadCumulative()), null, null),
                    false,
                    true);
            processMapList.add(processMap);
        }
        map.put(type, processMapList);
    }

    /**
     * Appends hardware information to the result map.
     *
     * @param type The type identifier.
     * @param map  The result map.
     */
    public void appendHardware(String type, Map<String, Object> map) {
        HardwareAbstractionLayer hardware = getHardware();
        Map<String, Object> hardwareMap = new HashMap<>();
        hardwareMap.put(TID.MEMORY, memoryDetails(hardware.getMemory()));
        hardwareMap.put(
                LambdaKit.getFieldName(HardwareAbstractionLayer::getSensors),
                sensorDetails(hardware.getSensors()));
        hardwareMap.put(
                LambdaKit.getFieldName(HardwareAbstractionLayer::getLogicalVolumeGroups),
                hardware.getLogicalVolumeGroups().stream().map(Collector::volumeGroupDetails).toList());
        hardwareMap.put(
                LambdaKit.getFieldName(HardwareAbstractionLayer::getSoundCards),
                hardware.getSoundCards().stream().map(Collector::soundCardDetails).toList());
        hardwareMap.put(
                LambdaKit.getFieldName(HardwareAbstractionLayer::getBluetoothDevices),
                hardware.getBluetoothDevices().stream().map(Collector::bluetoothDetails).toList());
        hardwareMap.put(
                LambdaKit.getFieldName(HardwareAbstractionLayer::getGraphicsCards),
                hardware.getGraphicsCards().stream().map(Collector::graphicsDetails).toList());
        hardwareMap.put(
                LambdaKit.getFieldName(HardwareAbstractionLayer::getDisplays),
                hardware.getDisplays().stream().map(Collector::displayDetails).toList());
        hardwareMap.put(
                LambdaKit.getFieldName(HardwareAbstractionLayer::getDiskStores),
                hardware.getDiskStores().stream().map(Collector::diskDetails).toList());
        hardwareMap.put(
                LambdaKit.getFieldName(HardwareAbstractionLayer::getPowerSources),
                hardware.getPowerSources().stream().map(Collector::powerSourceDetails).toList());
        hardwareMap.put(
                LambdaKit.<HardwareAbstractionLayer, List<NetworkIF>>getFieldName(
                        HardwareAbstractionLayer::getNetworkIFs),
                hardware.getNetworkIFs().stream().map(Collector::networkDetails).toList());
        map.put(type, hardwareMap);
    }

    /**
     * Reads physical and virtual memory through the existing memory interface.
     *
     * @param value hardware measurement source
     * @return the hardware detail values for the health response
     */
    private static Map<String, Object> memoryDetails(GlobalMemory value) {
        Map<String, Object> details = BeanKit
                .beanToMap(value, GlobalMemory::getTotal, GlobalMemory::getAvailable, GlobalMemory::getPageSize);
        details.put(
                LambdaKit.getFieldName(GlobalMemory::getVirtualMemory),
                virtualMemoryDetails(value.getVirtualMemory()));
        details.put(
                LambdaKit.getFieldName(GlobalMemory::getPhysicalMemory),
                value.getPhysicalMemory().stream().map(Collector::physicalMemoryDetails).toList());
        return details;
    }

    /**
     * Reads swap and virtual memory measurements.
     *
     * @param value hardware measurement source
     * @return the hardware detail values for the health response
     */
    private static Map<String, Object> virtualMemoryDetails(VirtualMemory value) {
        Map<String, Object> details = BeanKit.beanToMap(
                value,
                VirtualMemory::getSwapTotal,
                VirtualMemory::getSwapUsed,
                VirtualMemory::getVirtualMax,
                VirtualMemory::getVirtualInUse,
                VirtualMemory::getSwapPagesIn,
                VirtualMemory::getSwapPagesOut);
        return details;
    }

    /**
     * Reads the reported attributes of a physical memory bank.
     *
     * @param value hardware measurement source
     * @return the hardware detail values for the health response
     */
    private static Map<String, Object> physicalMemoryDetails(PhysicalMemory value) {
        Map<String, Object> details = BeanKit.beanToMap(
                value,
                PhysicalMemory::getBankLabel,
                PhysicalMemory::getCapacity,
                PhysicalMemory::getClockSpeed,
                PhysicalMemory::getManufacturer,
                PhysicalMemory::getMemoryType,
                PhysicalMemory::getPartNumber,
                PhysicalMemory::getSerialNumber);
        return details;
    }

    /**
     * Reads CPU temperature, voltage, and fan speeds.
     *
     * @param value hardware measurement source
     * @return the hardware detail values for the health response
     */
    private static Map<String, Object> sensorDetails(Sensors value) {
        Map<String, Object> details = BeanKit.beanToMap(value, Sensors::getFanSpeeds);
        details.put(LambdaKit.getFieldName(Sensors::getCpuTemperature), finiteMeasurement(value.getCpuTemperature()));
        details.put(LambdaKit.getFieldName(Sensors::getCpuVoltage), finiteMeasurement(value.getCpuVoltage()));
        return details;
    }

    /**
     * Reads the volume group name and its physical and logical volume assignments.
     *
     * @param value hardware measurement source
     * @return the hardware detail values for the health response
     */
    private static Map<String, Object> volumeGroupDetails(LogicalVolumeGroup value) {
        Map<String, Object> details = BeanKit.beanToMap(
                value,
                LogicalVolumeGroup::getName,
                LogicalVolumeGroup::getPhysicalVolumes,
                LogicalVolumeGroup::getLogicalVolumes);
        return details;
    }

    /**
     * Reads the sound card name, codec, and driver version.
     *
     * @param value hardware measurement source
     * @return the hardware detail values for the health response
     */
    private static Map<String, Object> soundCardDetails(SoundCard value) {
        Map<String, Object> details = BeanKit
                .beanToMap(value, SoundCard::getName, SoundCard::getCodec, SoundCard::getDriverVersion);
        return details;
    }

    /**
     * Reads the Bluetooth device identity and connection status.
     *
     * @param value hardware measurement source
     * @return the hardware detail values for the health response
     */
    private static Map<String, Object> bluetoothDetails(BluetoothDevice value) {
        Map<String, Object> details = BeanKit.beanToMap(
                value,
                BluetoothDevice::getName,
                BluetoothDevice::getAddress,
                BluetoothDevice::getMajorDeviceClass,
                BluetoothDevice::isConnected,
                BluetoothDevice::isPaired,
                BluetoothDevice::getBatteryLevel,
                BluetoothDevice::getAdapterName);
        return details;
    }

    /**
     * Reads the graphics card identity and video memory capacity.
     *
     * @param value hardware measurement source
     * @return the hardware detail values for the health response
     */
    private static Map<String, Object> graphicsDetails(GraphicsCard value) {
        Map<String, Object> details = BeanKit.beanToMap(
                value,
                GraphicsCard::getName,
                GraphicsCard::getDeviceId,
                GraphicsCard::getVendor,
                GraphicsCard::getVersionInfo);
        details.putAll(BeanKit.beanToMap(new VideoMemory(value.getVRam())));
        return details;
    }

    /**
     * Reads the public display information derived from the EDID.
     *
     * @param value hardware measurement source
     * @return the hardware detail values for the health response
     */
    private static Map<String, Object> displayDetails(Display value) {
        DisplayInfo info = value.getDisplayInfo();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put(LambdaKit.getFieldName(Display::getDisplayInfo), info == null ? null : displayInfoDetails(info));
        return details;
    }

    /**
     * Reads display identity, geometry, and preferred resolution.
     *
     * @param value hardware measurement source
     * @return the hardware detail values for the health response
     */
    private static Map<String, Object> displayInfoDetails(DisplayInfo value) {
        Map<String, Object> details = BeanKit.beanToMap(
                value,
                DisplayInfo::getEdid,
                DisplayInfo::isEdidSynthetic,
                DisplayInfo::getManufacturerID,
                DisplayInfo::getProductID,
                DisplayInfo::getSerialNo,
                DisplayInfo::getWeek,
                DisplayInfo::getYear,
                DisplayInfo::getVersion,
                DisplayInfo::isDigital,
                DisplayInfo::getHcm,
                DisplayInfo::getVcm,
                DisplayInfo::getPreferredResolution,
                DisplayInfo::getModel,
                DisplayInfo::getProductSerialNumber);
        return details;
    }

    /**
     * Reads the physical disk identity, counters, and partitions.
     *
     * @param value hardware measurement source
     * @return the hardware detail values for the health response
     */
    private static Map<String, Object> diskDetails(HWDiskStore value) {
        Map<String, Object> details = BeanKit.beanToMap(
                value,
                HWDiskStore::getName,
                HWDiskStore::getModel,
                HWDiskStore::getSerial,
                HWDiskStore::getSize,
                HWDiskStore::getReads,
                HWDiskStore::getReadBytes,
                HWDiskStore::getWrites,
                HWDiskStore::getWriteBytes,
                HWDiskStore::getCurrentQueueLength,
                HWDiskStore::getTransferTime,
                HWDiskStore::getTimeStamp);
        details.put(
                LambdaKit.getFieldName(HWDiskStore::getPartitions),
                value.getPartitions().stream().map(Collector::partitionDetails).toList());
        return details;
    }

    /**
     * Reads a disk partition identifier, size, and mount point.
     *
     * @param value hardware measurement source
     * @return the hardware detail values for the health response
     */
    private static Map<String, Object> partitionDetails(HWPartition value) {
        Map<String, Object> details = BeanKit.beanToMap(
                value,
                HWPartition::getIdentification,
                HWPartition::getName,
                HWPartition::getType,
                HWPartition::getUuid,
                HWPartition::getLabel,
                HWPartition::getSize,
                HWPartition::getMajor,
                HWPartition::getMinor,
                HWPartition::getMountPoint);
        return details;
    }

    /**
     * Reads battery identity, capacity, charge state, and electrical measurements.
     *
     * @param value hardware measurement source
     * @return the hardware detail values for the health response
     */
    private static Map<String, Object> powerSourceDetails(PowerSource value) {
        Map<String, Object> details = BeanKit.beanToMap(
                value,
                PowerSource::getName,
                PowerSource::getDeviceName,
                PowerSource::isPowerOnLine,
                PowerSource::isCharging,
                PowerSource::isDischarging,
                PowerSource::getCurrentCapacity,
                PowerSource::getMaxCapacity,
                PowerSource::getDesignCapacity,
                PowerSource::getCycleCount,
                PowerSource::getChemistry,
                PowerSource::getSerialNumber,
                PowerSource::getManufacturer);
        details.put(
                LambdaKit.getFieldName(PowerSource::getRemainingCapacityPercent),
                finiteMeasurement(value.getRemainingCapacityPercent()));
        details.put(
                LambdaKit.getFieldName(PowerSource::getTimeRemainingEstimated),
                finiteMeasurement(value.getTimeRemainingEstimated()));
        details.put(
                LambdaKit.getFieldName(PowerSource::getTimeRemainingInstant),
                finiteMeasurement(value.getTimeRemainingInstant()));
        details.put(LambdaKit.getFieldName(PowerSource::getAmperage), finiteMeasurement(value.getAmperage()));
        details.put(
                LambdaKit.getFieldName(PowerSource::getPowerUsageRate),
                finiteMeasurement(value.getPowerUsageRate()));
        details.put(LambdaKit.getFieldName(PowerSource::getVoltage), finiteMeasurement(value.getVoltage()));
        details.put(LambdaKit.getFieldName(PowerSource::getTemperature), finiteMeasurement(value.getTemperature()));
        details.put(
                LambdaKit.getFieldName(PowerSource::getCapacityUnits),
                Objects.toString(value.getCapacityUnits(), null));
        details.put(
                LambdaKit.getFieldName(PowerSource::getManufactureDate),
                Objects.toString(value.getManufactureDate(), null));
        return details;
    }

    /**
     * Reads network interface identity, addresses, and traffic counters.
     *
     * @param value hardware measurement source
     * @return the hardware detail values for the health response
     */
    private static Map<String, Object> networkDetails(NetworkIF value) {
        Map<String, Object> details = BeanKit.beanToMap(
                value,
                NetworkIF::getName,
                NetworkIF::getIndex,
                NetworkIF::getDisplayName,
                NetworkIF::getMacaddr,
                NetworkIF::getSubnetMasks,
                NetworkIF::getPrefixLengths,
                NetworkIF::getBytesRecv,
                NetworkIF::getBytesSent,
                NetworkIF::getPacketsRecv,
                NetworkIF::getPacketsSent,
                NetworkIF::getInErrors,
                NetworkIF::getOutErrors,
                NetworkIF::getInDrops,
                NetworkIF::getCollisions,
                NetworkIF::getSpeed,
                NetworkIF::getTimeStamp,
                NetworkIF::isKnownVmMacAddr);
        details.putAll(
                BeanKit.beanToMap(new NetworkAddresses(value.getMTU(), value.getIPv4addr(), value.getIPv6addr())));
        return details;
    }

    /**
     * Omits non-finite sensor values, which JSON cannot represent as numbers.
     *
     * @param value the reported measurement
     * @return the measurement, or {@code null} if it is unavailable
     */
    private static Double finiteMeasurement(double value) {
        return Double.isFinite(value) ? value : null;
    }

    /**
     * Appends system or hardware information to the result map based on the type.
     *
     * @param type The type identifier.
     * @param map  The result map.
     */
    public void append(String type, Map<String, Object> map) {
        switch (StringKit.toLowerCase(type)) {
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
     * Gets all supported monitoring information for the system.
     *
     * @return A map containing all types of information.
     */
    public Map<String, Object> getAll() {
        return get(TID.ALL_TID);
    }

    /**
     * Gets system or hardware information for a single type.
     *
     * @param type The type identifier.
     * @return A map containing the specified type of information.
     */
    public Map<String, Object> getSingle(String type) {
        Map<String, Object> map = new HashMap<>(1);
        append(type, map);
        return map;
    }

    /**
     * Gets system or hardware information based on a list of types.
     *
     * @param list The list of types.
     * @return A map containing the specified types of information.
     */
    public Map<String, Object> get(List<String> list) {
        Map<String, Object> map = new HashMap<>(5);
        for (String type : list) {
            append(type, map);
        }
        return map;
    }

    /**
     * Gets information for a specific process.
     *
     * @param pid The process ID.
     * @return A map containing information about the specified process.
     */
    public Map<String, Object> getProcessById(int pid) {
        OSProcess process = getOperatingSystem().getProcess(pid);
        if (process == null) {
            return new HashMap<>();
        }
        return BeanKit.beanToMap(
                new ProcessSummary(process.getName(), process.getProcessID(),
                        Formats.round(process.getProcessCpuLoadCumulative()), process.getResidentMemory(),
                        process.getState().toString()));
    }

    /**
     * Gets CPU information.
     *
     * @return A {@link Cpu} object.
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
                .systemUsage(Formats.round(sys * 100.0 / totalCpu)).userUsage(Formats.round(user * 100.0 / totalCpu))
                .ioWait(Formats.round(ioWait * 100.0 / totalCpu))
                .totalUsage(Formats.round((totalCpu - idle) * 100.0 / totalCpu)).build();
    }

    /**
     * Gets memory usage information.
     *
     * @return A {@link Memory} object.
     */
    public Memory getMemory() {
        GlobalMemory globalMemory = getHardware().getMemory();
        long totalByte = globalMemory.getTotal();
        long availableByte = globalMemory.getAvailable();
        return Memory.builder().total(Formats.formatBytes(totalByte))
                .used(Formats.formatBytes(totalByte - availableByte)).free(Formats.formatBytes(availableByte))
                .usage(Formats.round((totalByte - availableByte) * 100.0 / totalByte)).build();
    }

    /**
     * Gets JVM information.
     *
     * @return A {@link Jvm} object.
     */
    public Jvm getJvm() {
        Runtime runtime = Runtime.getRuntime();
        long jvmTotalMemoryByte = runtime.totalMemory();
        long freeMemoryByte = runtime.freeMemory();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Jvm.builder().jdkVersion(Keys.get(Keys.JAVA_VERSION)).jdkHome(Keys.get(Keys.JAVA_HOME))
                .jdkName(runtimeMXBean.getVmName()).totalMemory(jvmTotalMemoryByte).maxMemory(runtime.maxMemory())
                .freeMemory(freeMemoryByte).usedMemory(jvmTotalMemoryByte - freeMemoryByte)
                .usagePercent(Formats.round((jvmTotalMemoryByte - freeMemoryByte) * 100.0 / jvmTotalMemoryByte))
                .startTime(runtimeMXBean.getStartTime()).uptime(runtimeMXBean.getUptime()).build();
    }

    /**
     * Gets disk usage information.
     *
     * @return A list of {@link Disk} objects.
     */
    public List<Disk> getDisk() {
        OperatingSystem operatingSystem = getOperatingSystem();
        FileSystem fileSystem = operatingSystem.getFileSystem();
        List<Disk> disks = new ArrayList<>();
        Iterable<OSFileStore> fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray) {
            long total = fs.getTotalSpace();
            long usable = fs.getUsableSpace();
            disks.add(
                    Disk.builder().deviceName(fs.getName()).volumeName(fs.getVolume()).label(fs.getLabel())
                            .logicalVolumeName(fs.getLogicalVolume()).mountPoint(fs.getMount())
                            .description(fs.getDescription()).mountOptions(fs.getOptions()).filesystemType(fs.getType())
                            .uuid(fs.getUUID()).totalSpace(total).usedSpace(total - usable).freeSpace(usable)
                            .usagePercent(total > 0 ? Formats.round((total - usable) * 100.0 / total) : 0.0).build());
        }
        return disks;
    }

    /**
     * Gets the network interface information for the current system.
     *
     * @return A map containing network interface information.
     */
    public Map<String, Object> getNetworkInfo() {
        Map<String, Object> map = new HashMap<>(1);
        map.put("networkIFs", getHardware().getNetworkIFs());
        return map;
    }

    /**
     * Gets the power source information for the current system.
     *
     * @return A map containing power source information.
     */
    public Map<String, Object> getPowerSourceInfo() {
        Map<String, Object> map = new HashMap<>(1);
        map.put("powerSources", getHardware().getPowerSources());
        return map;
    }

    /**
     * Returns the type of the operating system.
     *
     * @return The OS type from {@link Platform}.
     */
    public Object type() {
        return Platform.INSTANCE.getOSType();
    }

}
