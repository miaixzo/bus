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

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor.PhysicalProcessor;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor.ProcessorCache;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor.TickType;
import org.miaixz.bus.health.builtin.hardware.ComputerSystem;
import org.miaixz.bus.health.builtin.hardware.Display;
import org.miaixz.bus.health.builtin.hardware.GlobalMemory;
import org.miaixz.bus.health.builtin.hardware.GraphicsCard;
import org.miaixz.bus.health.builtin.hardware.HWDiskStore;
import org.miaixz.bus.health.builtin.hardware.HWPartition;
import org.miaixz.bus.health.builtin.hardware.LogicalVolumeGroup;
import org.miaixz.bus.health.builtin.hardware.NetworkIF;
import org.miaixz.bus.health.builtin.hardware.PhysicalMemory;
import org.miaixz.bus.health.builtin.hardware.PowerSource;
import org.miaixz.bus.health.builtin.hardware.Printer;
import org.miaixz.bus.health.builtin.hardware.Sensors;
import org.miaixz.bus.health.builtin.hardware.SoundCard;
import org.miaixz.bus.health.builtin.hardware.UsbDevice;
import org.miaixz.bus.health.builtin.hardware.VirtualMemory;
import org.miaixz.bus.health.builtin.software.ApplicationInfo;
import org.miaixz.bus.health.builtin.software.FileSystem;
import org.miaixz.bus.health.builtin.software.InternetProtocolStats;
import org.miaixz.bus.health.builtin.software.NetworkParams;
import org.miaixz.bus.health.builtin.software.OSFileStore;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.OSService;
import org.miaixz.bus.health.builtin.software.OSSession;
import org.miaixz.bus.health.builtin.software.OperatingSystem;
import org.miaixz.bus.health.builtin.software.OperatingSystem.ProcessFiltering;
import org.miaixz.bus.health.builtin.software.OperatingSystem.ProcessSorting;

/**
 * Static helper methods for printing bus-health system information.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Reporter {

    /**
     * Creates a new Reporter instance.
     */
    private Reporter() {
        // No initialization required.
    }

    /**
     * Handles the print operating system operation.
     *
     * @param lines the lines
     * @param os    the os
     */
    public static void printOperatingSystem(List<String> lines, final OperatingSystem os) {
        lines.add(String.valueOf(os));
        lines.add("Booted: " + Instant.ofEpochSecond(os.getSystemBootTime()));
        lines.add("Uptime: " + Formats.formatElapsedSecs(os.getSystemUptime()));
        lines.add("Running with" + (os.isElevated() ? "" : "out") + " elevated permissions.");
        lines.add("Sessions:");
        for (OSSession s : os.getSessions()) {
            lines.add(" " + s.toString());
        }
    }

    /**
     * Handles the print installed apps operation.
     *
     * @param lines                 the lines
     * @param installedApplications the installed applications
     */
    public static void printInstalledApps(List<String> lines, List<ApplicationInfo> installedApplications) {
        lines.add("Apps: ");
        for (int i = 0; i < 5 && i < installedApplications.size(); i++) {
            lines.add(" " + installedApplications.get(i).toString());
        }
    }

    /**
     * Handles the print computer system operation.
     *
     * @param lines          the lines
     * @param computerSystem the computer system
     */
    public static void printComputerSystem(List<String> lines, final ComputerSystem computerSystem) {
        lines.add("System: " + computerSystem.toString());
        lines.add(" Firmware: " + computerSystem.getFirmware().toString());
        lines.add(" Baseboard: " + computerSystem.getBaseboard().toString());
    }

    /**
     * Handles the print processor operation.
     *
     * @param lines     the lines
     * @param processor the processor
     */
    public static void printProcessor(List<String> lines, CentralProcessor processor) {
        lines.add(processor.toString());

        Map<Integer, Integer> efficiencyCount = new HashMap<>();
        int maxEfficiency = 0;
        for (PhysicalProcessor cpu : processor.getPhysicalProcessors()) {
            int eff = cpu.getEfficiency();
            efficiencyCount.merge(eff, 1, Integer::sum);
            if (eff > maxEfficiency) {
                maxEfficiency = eff;
            }
        }
        lines.add(" Topology:");
        lines.add(
                String.format(
                        Locale.ROOT,
                        "  %7s %4s %4s %4s %4s %4s",
                        "LogProc",
                        "P/E",
                        "Proc",
                        "Pkg",
                        "NUMA",
                        "PGrp"));
        for (PhysicalProcessor cpu : processor.getPhysicalProcessors()) {
            lines.add(
                    String.format(
                            Locale.ROOT,
                            "  %7s %4s %4d %4s %4d %4d",
                            processor.getLogicalProcessors().stream()
                                    .filter(p -> p.getPhysicalProcessorNumber() == cpu.getPhysicalProcessorNumber())
                                    .filter(p -> p.getPhysicalPackageNumber() == cpu.getPhysicalPackageNumber())
                                    .map(p -> Integer.toString(p.getProcessorNumber()))
                                    .collect(Collectors.joining(",")),
                            cpu.getEfficiency() == maxEfficiency ? "P" : "E",
                            cpu.getPhysicalProcessorNumber(),
                            cpu.getPhysicalPackageNumber(),
                            processor.getLogicalProcessors().stream()
                                    .filter(p -> p.getPhysicalProcessorNumber() == cpu.getPhysicalProcessorNumber())
                                    .filter(p -> p.getPhysicalPackageNumber() == cpu.getPhysicalPackageNumber())
                                    .mapToInt(p -> p.getNumaNode()).findFirst().orElse(0),
                            processor.getLogicalProcessors().stream()
                                    .filter(p -> p.getPhysicalProcessorNumber() == cpu.getPhysicalProcessorNumber())
                                    .filter(p -> p.getPhysicalPackageNumber() == cpu.getPhysicalPackageNumber())
                                    .mapToInt(p -> p.getProcessorGroup()).findFirst().orElse(0)));
        }
        List<ProcessorCache> caches = processor.getProcessorCaches();
        if (!caches.isEmpty()) {
            lines.add(" Caches:");
        }
        for (int i = 0; i < caches.size(); i++) {
            ProcessorCache cache = caches.get(i);
            boolean perCore = cache.getLevel() < 3;
            boolean pCore = perCore && i < caches.size() - 1 && cache.getLevel() == caches.get(i + 1).getLevel()
                    && cache.getType() == caches.get(i + 1).getType();
            boolean eCore = perCore && i > 0 && cache.getLevel() == caches.get(i - 1).getLevel()
                    && cache.getType() == caches.get(i - 1).getType();
            StringBuilder sb = new StringBuilder("  ").append(cache);
            if (perCore) {
                sb.append(" (per ");
                if (pCore) {
                    sb.append("P-");
                } else if (eCore) {
                    sb.append("E-");
                }
                sb.append("core)");
            }
            lines.add(sb.toString());
        }
    }

    /**
     * Handles the print memory operation.
     *
     * @param lines  the lines
     * @param memory the memory
     */
    public static void printMemory(List<String> lines, GlobalMemory memory) {
        lines.add("Physical Memory: \n " + memory.toString());
        VirtualMemory vm = memory.getVirtualMemory();
        lines.add("Virtual Memory: \n " + vm.toString());
        List<PhysicalMemory> pmList = memory.getPhysicalMemory();
        if (!pmList.isEmpty()) {
            lines.add("Physical Memory: ");
            for (PhysicalMemory pm : pmList) {
                lines.add(" " + pm.toString());
            }
        }
    }

    /**
     * Handles the print cpu operation.
     *
     * @param lines     the lines
     * @param processor the processor
     */
    public static void printCpu(List<String> lines, CentralProcessor processor) {
        lines.add("Context Switches/Interrupts: " + processor.getContextSwitches() + " / " + processor.getInterrupts());

        long[] prevTicks = processor.getSystemCpuLoadTicks();
        long[][] prevProcTicks = processor.getProcessorCpuLoadTicks();
        lines.add("CPU, IOWait, and IRQ ticks @ 0 sec:" + Arrays.toString(prevTicks));
        ThreadKit.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        lines.add("CPU, IOWait, and IRQ ticks @ 1 sec:" + Arrays.toString(ticks));
        long user = ticks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long nice = ticks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long sys = ticks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long idle = ticks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long iowait = ticks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long irq = ticks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softirq = ticks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = ticks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;

        lines.add(
                String.format(
                        Locale.ROOT,
                        "User: %.1f%% Nice: %.1f%% System: %.1f%% Idle: %.1f%% IOwait: %.1f%% IRQ: %.1f%% SoftIRQ: %.1f%% Steal: %.1f%%",
                        100d * user / totalCpu,
                        100d * nice / totalCpu,
                        100d * sys / totalCpu,
                        100d * idle / totalCpu,
                        100d * iowait / totalCpu,
                        100d * irq / totalCpu,
                        100d * softirq / totalCpu,
                        100d * steal / totalCpu));
        lines.add(
                String.format(
                        Locale.ROOT,
                        "CPU load: %.1f%%",
                        processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100));
        double[] loadAverage = processor.getSystemLoadAverage(3);
        lines.add(
                "CPU load averages:"
                        + (loadAverage[0] < 0 ? " N/A" : String.format(Locale.ROOT, " %.2f", loadAverage[0]))
                        + (loadAverage[1] < 0 ? " N/A" : String.format(Locale.ROOT, " %.2f", loadAverage[1]))
                        + (loadAverage[2] < 0 ? " N/A" : String.format(Locale.ROOT, " %.2f", loadAverage[2])));
        StringBuilder procCpu = new StringBuilder("CPU load per processor:");
        double[] load = processor.getProcessorCpuLoadBetweenTicks(prevProcTicks);
        for (double avg : load) {
            procCpu.append(String.format(Locale.ROOT, " %.1f%%", avg * 100));
        }
        lines.add(procCpu.toString());
        long freq = processor.getProcessorIdentifier().getVendorFreq();
        if (freq > 0) {
            lines.add("Vendor Frequency: " + Formats.formatHertz(freq));
        }
        freq = processor.getMaxFreq();
        if (freq > 0) {
            lines.add("Max Frequency: " + Formats.formatHertz(freq));
        }
        long[] freqs = processor.getCurrentFreq();
        if (freqs[0] > 0) {
            StringBuilder sb = new StringBuilder("Current Frequencies: ");
            for (int i = 0; i < freqs.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(Formats.formatHertz(freqs[i]));
            }
            lines.add(sb.toString());
        }
        if (!processor.getFeatureFlags().isEmpty()) {
            lines.add("CPU Features:");
            for (String features : processor.getFeatureFlags()) {
                lines.add("  " + features);
            }
        }
    }

    /**
     * Handles the print processes operation.
     *
     * @param lines  the lines
     * @param os     the os
     * @param memory the memory
     */
    public static void printProcesses(List<String> lines, OperatingSystem os, GlobalMemory memory) {
        OSProcess myProc = os.getProcess(os.getProcessId());
        if (myProc == null) {
            return;
        }
        lines.add(
                "My PID: " + myProc.getProcessID() + " with affinity " + Long.toBinaryString(myProc.getAffinityMask()));
        lines.add("My TID: " + os.getThreadId() + " with details " + os.getCurrentThread());
        lines.add("Processes: " + os.getProcessCount() + ", Threads: " + os.getThreadCount());
        List<OSProcess> procs = os.getProcesses(ProcessFiltering.ALL_PROCESSES, ProcessSorting.CPU_DESC, 5);
        lines.add("   PID  %CPU %MEM       VSZ       RSS   Private Name");
        for (int i = 0; i < procs.size(); i++) {
            OSProcess p = procs.get(i);
            lines.add(
                    String.format(
                            Locale.ROOT,
                            " %5d %5.1f %4.1f %9s %9s %9s %s",
                            p.getProcessID(),
                            100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime(),
                            100d * p.getResidentMemory() / memory.getTotal(),
                            Formats.formatBytes(p.getVirtualSize()),
                            Formats.formatBytes(p.getResidentMemory()),
                            Formats.formatBytes(p.getPrivateResidentMemory()),
                            p.getName()));
        }
        OSProcess p = os.getProcess(os.getProcessId());
        if (p == null) {
            return;
        }
        lines.add("Current process arguments: ");
        for (String s : p.getArguments()) {
            lines.add("  " + s);
        }
        lines.add("Current process environment: ");
        for (Entry<String, String> e : p.getEnvironmentVariables().entrySet()) {
            lines.add("  " + e.getKey() + "=" + e.getValue());
        }
    }

    /**
     * Handles the print services operation.
     *
     * @param lines the lines
     * @param os    the os
     */
    public static void printServices(List<String> lines, OperatingSystem os) {
        lines.add("Services: ");
        lines.add("   PID   State   Name");
        int i = 0;
        for (OSService s : os.getServices()) {
            if (s.getState().equals(OSService.State.RUNNING) && i++ < 5) {
                lines.add(String.format(Locale.ROOT, " %5d  %7s  %s", s.getProcessID(), s.getState(), s.getName()));
            }
        }
        i = 0;
        for (OSService s : os.getServices()) {
            if (s.getState().equals(OSService.State.STOPPED) && i++ < 5) {
                lines.add(String.format(Locale.ROOT, " %5d  %7s  %s", s.getProcessID(), s.getState(), s.getName()));
            }
        }
    }

    /**
     * Handles the print sensors operation.
     *
     * @param lines   the lines
     * @param sensors the sensors
     */
    public static void printSensors(List<String> lines, Sensors sensors) {
        lines.add("Sensors: " + sensors.toString());
    }

    /**
     * Handles the print power sources operation.
     *
     * @param lines the lines
     * @param list  the list
     */
    public static void printPowerSources(List<String> lines, List<PowerSource> list) {
        StringBuilder sb = new StringBuilder("Power Sources: ");
        if (list.isEmpty()) {
            sb.append("Unknown");
        }
        for (PowerSource powerSource : list) {
            sb.append("\n ").append(powerSource.toString());
        }
        lines.add(sb.toString());
    }

    /**
     * Handles the print disks operation.
     *
     * @param lines the lines
     * @param list  the list
     */
    public static void printDisks(List<String> lines, List<HWDiskStore> list) {
        lines.add("Disks:");
        for (HWDiskStore disk : list) {
            lines.add(" " + disk.toString());
            for (HWPartition part : disk.getPartitions()) {
                lines.add(" |-- " + part.toString());
            }
        }
    }

    /**
     * Handles the print l vgroups operation.
     *
     * @param lines the lines
     * @param list  the list
     */
    public static void printLVgroups(List<String> lines, List<LogicalVolumeGroup> list) {
        if (!list.isEmpty()) {
            lines.add("Logical Volume Groups:");
            for (LogicalVolumeGroup lvg : list) {
                lines.add(" " + lvg.toString());
            }
        }
    }

    /**
     * Handles the print file system operation.
     *
     * @param lines      the lines
     * @param fileSystem the file system
     */
    public static void printFileSystem(List<String> lines, FileSystem fileSystem) {
        lines.add("File System:");
        lines.add(
                String.format(
                        Locale.ROOT,
                        " File Descriptors: %d/%d",
                        fileSystem.getOpenFileDescriptors(),
                        fileSystem.getMaxFileDescriptors()));
        for (OSFileStore fs : fileSystem.getFileStores()) {
            long usable = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            lines.add(
                    String.format(
                            Locale.ROOT,
                            " %s (%s) [%s] %s of %s free (%.1f%%), %s of %s files free (%.1f%%) is %s "
                                    + (fs.getLogicalVolume() != null && fs.getLogicalVolume().length() > 0 ? "[%s]"
                                            : "%s")
                                    + " and is mounted at %s",
                            fs.getName(),
                            fs.getDescription().isEmpty() ? "file system" : fs.getDescription(),
                            fs.getType(),
                            Formats.formatBytes(usable),
                            Formats.formatBytes(fs.getTotalSpace()),
                            100d * usable / total,
                            Formats.formatValue(fs.getFreeInodes(), ""),
                            Formats.formatValue(fs.getTotalInodes(), ""),
                            100d * fs.getFreeInodes() / fs.getTotalInodes(),
                            fs.getVolume(),
                            fs.getLogicalVolume(),
                            fs.getMount()));
        }
    }

    /**
     * Handles the print network interfaces operation.
     *
     * @param lines the lines
     * @param list  the list
     */
    public static void printNetworkInterfaces(List<String> lines, List<NetworkIF> list) {
        StringBuilder sb = new StringBuilder("Network Interfaces:");
        if (list.isEmpty()) {
            sb.append(" Unknown");
        } else {
            for (NetworkIF net : list) {
                sb.append("\n ").append(net.toString());
            }
        }
        lines.add(sb.toString());
    }

    /**
     * Handles the print network parameters operation.
     *
     * @param lines         the lines
     * @param networkParams the network params
     */
    public static void printNetworkParameters(List<String> lines, NetworkParams networkParams) {
        lines.add("Network parameters:\n " + networkParams.toString());
    }

    /**
     * Handles the print internet protocol stats operation.
     *
     * @param lines the lines
     * @param ip    the ip
     */
    public static void printInternetProtocolStats(List<String> lines, InternetProtocolStats ip) {
        lines.add("Internet Protocol statistics:");
        lines.add(" TCPv4: " + ip.getTCPv4Stats());
        lines.add(" TCPv6: " + ip.getTCPv6Stats());
        lines.add(" UDPv4: " + ip.getUDPv4Stats());
        lines.add(" UDPv6: " + ip.getUDPv6Stats());
    }

    /**
     * Handles the print displays operation.
     *
     * @param lines the lines
     * @param list  the list
     */
    public static void printDisplays(List<String> lines, List<Display> list) {
        lines.add("Displays:");
        int i = 0;
        for (Display display : list) {
            lines.add(" Display " + i + ":");
            lines.add(String.valueOf(display));
            i++;
        }
    }

    /**
     * Handles the print usb devices operation.
     *
     * @param lines the lines
     * @param list  the list
     */
    public static void printUsbDevices(List<String> lines, List<UsbDevice> list) {
        lines.add("USB Devices:");
        for (UsbDevice usbDevice : list) {
            lines.add(String.valueOf(usbDevice));
        }
    }

    /**
     * Handles the print sound cards operation.
     *
     * @param lines the lines
     * @param list  the list
     */
    public static void printSoundCards(List<String> lines, List<SoundCard> list) {
        lines.add("Sound Cards:");
        for (SoundCard card : list) {
            lines.add(" " + String.valueOf(card));
        }
    }

    /**
     * Handles the print graphics cards operation.
     *
     * @param lines the lines
     * @param list  the list
     */
    public static void printGraphicsCards(List<String> lines, List<GraphicsCard> list) {
        lines.add("Graphics Cards:");
        if (list.isEmpty()) {
            lines.add(" None detected.");
        } else {
            for (GraphicsCard card : list) {
                lines.add(" " + String.valueOf(card));
            }
        }
    }

    /**
     * Handles the print printers operation.
     *
     * @param lines the lines
     * @param list  the list
     */
    public static void printPrinters(List<String> lines, List<Printer> list) {
        lines.add("Printers:");
        if (list.isEmpty()) {
            lines.add(" None detected.");
        } else {
            for (Printer printer : list) {
                lines.add(" " + String.valueOf(printer));
            }
        }
    }

}
