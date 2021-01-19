/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.builtin.hardware;

import org.aoju.bus.core.annotation.Immutable;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Memoize;

import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 此类表示计算机*系统的整个中央处理单元(CPU)，其中可能包含一个
 * 或多个物理程序包(插槽)，一个或多个物理处理器(核心)和一个或
 * 多个逻辑处理器(操作系统看到的内容，可能包括超线程内核)
 *
 * @author Kimi Liu
 * @version 6.1.8
 * @since JDK 1.8+
 */
@ThreadSafe
public interface CentralProcessor {

    /**
     * CPU的标识符字符串，包括名称，供应商，步进，型号和系列信息(也称为CPU的签名)
     *
     * @return 封装CPU标识符*信息的{@link ProcessorIdentifier}对象
     */
    ProcessorIdentifier getProcessorIdentifier();

    /**
     * 该CPU上逻辑处理器的最大频率(Hz)
     *
     * @return 最大频率或-1，如果未知
     */
    long getMaxFreq();

    /**
     * 尝试返回此CPU上逻辑处理器的当前频率(以Hz为单位)
     * 在Windows上，根据最大频率的百分比返回估算值
     * 在具有64个以上逻辑处理器的Windows系统上
     * 只能返回数组第一部分中当前处理器组的频率。
     *
     * @return 系统上每个逻辑处理器的处理器频率数组
     * 使用{@link #getLogicalProcessors()} 将这些频率与物理包和处理器相关联
     */
    long[] getCurrentFreq();

    /**
     * 返回CPU逻辑处理器的数组。数组将按照NUMA节点号
     * 然后是处理器号的*顺序进行排序。此顺序与提供按
     * 处理器结果的其他方法一致
     *
     * @return 逻辑处理器阵列
     */
    List<LogicalProcessor> getLogicalProcessors();

    /**
     * 通过计算来自{@link #getSystemCpuLoadTicks()}的滴答声与来自先
     * 前调用的用户提供的值之间的滴答声，返回整个系统的“最近cpu使用情况”
     *
     * @param oldTicks 先前对*{@link #getSystemCpuLoadTicks()}的调用的滴答数组
     * @return CPU负载介于0和1之间(100 ％)
     */
    double getSystemCpuLoadBetweenTicks(long[] oldTicks);

    /**
     * 获取系统范围的CPU负载滴答计数器。返回具有七个元素的数组
     * 表示在User(0)，Nice(1)，System(2)，Idle(3)IOwait(4)，IOwait(IRQ)(5)
     * 软件中断/DPC中花费的毫秒数(SoftIRQ(6)或Steal(7)状态。使用 {@link CentralProcessor.TickType#getIndex()}
     * 来检索适当的索引。通过测量一个时间间隔内的滴答之间的差异，可以计算该间隔内的CPU负载。
     * <p> 请注意，虽然滴答计数器以毫秒为单位，但它们可能会与(取决于平台的)时钟滴答一起以更大的增量前进
     * 。例如，默认情况下，Windows时钟滴答是1/64秒(约15或16 *毫秒)，而Linux滴答则取决于发行和配置
     * 但通常是1/100秒(10毫秒)
     * <p> Windows上没有Nice和IOWait信息，而macOS上没有IOwait和IRQ信息，因此这些刻度始终为零。
     * <p> 要使用此方法计算总体空闲时间，请同时包括空闲和IOWait滴答。同样，IRQ，SoftIRQ和Steal ticks应该添加到
     * System值以得到总数。系统滴答声还包括执行其他*虚拟主机(窃取)的时间
     *
     * @return 7个长值组成的数组，表示在用户，Nice，System，Idle，IOwait，IRQ，SoftIRQ和窃取状态中花费的时间
     */
    long[] getSystemCpuLoadTicks();

    /**
     * 返回指定元素数量的系统负载平均值，最多3个元素，表示1、5和15分钟
     * 系统负载平均值是排队到可用处理器的可运行实体的数量和在可用处理器上
     * 运行的可运行实体的数量在一段时间内的平均值的总和。负载平均值的计算
     * 方法是特定于操作系统的，但通常是一个与时间相关的阻尼平均值
     * 如果负载平均值不可用，则返回一个负值。此方法的设计目的是提供有关
     * 系统负载的提示，并且可以频繁地查询平均负载在某些平台(例如Windows)
     * 上可能不可用，其中实现此方法的成本很高
     *
     * @param nelem 返回的元素数
     * @return 一个阵列的系统负载平均为1、5和15分钟，阵列的大小由nelem指定;如果不可用，则为负值
     */
    double[] getSystemLoadAverage(int nelem);

    /**
     * 返回所有逻辑处理器的“最近cpu使用量”，方法是在用户提供的前一个调用的值之间
     * 对来自{@link #getProcessorCpuLoadTicks()}的节拍进行计数
     *
     * @param oldTicks 前一个调用{@link #getProcessorCpuLoadTicks()}的tick数组
     * @return 每个逻辑处理器的CPU负载在0到1(100 %)之间的数组
     */
    double[] getProcessorCpuLoadBetweenTicks(long[][] oldTicks);

    /**
     * Get Processor CPU Load tick counters. Returns a two dimensional array, with
     * {@link #getLogicalProcessorCount()} arrays, each containing seven elements
     * representing milliseconds spent in User (0), Nice (1), System (2), Idle (3),
     * IOwait (4), Hardware interrupts (IRQ) (5), Software interrupts/DPC (SoftIRQ)
     * (6), or Steal (7) states. Use
     * {@link CentralProcessor.TickType#getIndex()} to retrieve the
     * appropriate index. By measuring the difference between ticks across a time
     * interval, CPU load over that interval may be calculated.
     * <p>
     * Note that while tick counters are in units of milliseconds, they may advance
     * in larger increments along with (platform dependent) clock ticks. For
     * example, by default Windows clock ticks are 1/64 of a second (about 15 or 16
     * milliseconds) and Linux ticks are distribution and configuration dependent
     * but usually 1/100 of a second (10 milliseconds).
     * <p>
     * Nice and IOwait per processor information is not available on Windows, and
     * IOwait and IRQ information is not available on macOS, so these ticks will
     * always be zero.
     * <p>
     * To calculate overall Idle time using this method, include both Idle and
     * IOWait ticks. Similarly, IRQ, SoftIRQ and Steal ticks should be added to the
     * System value to get the total. System ticks also include time executing other
     * virtual hosts (steal).
     *
     * @return A 2D array of logicalProcessorCount x 7 long values representing time
     * spent in User, Nice, System, Idle, IOwait, IRQ, SoftIRQ, and Steal
     * states.
     */
    long[][] getProcessorCpuLoadTicks();

    /**
     * Get the number of logical CPUs available for processing. This value may be
     * higher than physical CPUs if hyperthreading is enabled.
     *
     * @return The number of logical CPUs available.
     */
    int getLogicalProcessorCount();

    /**
     * Get the number of physical CPUs/cores available for processing.
     *
     * @return The number of physical CPUs available.
     */
    int getPhysicalProcessorCount();

    /**
     * Get the number of packages/sockets in the system. A single package may
     * contain multiple cores.
     *
     * @return The number of physical packages available.
     */
    int getPhysicalPackageCount();

    /**
     * Get the number of context switches which have occurred
     *
     * @return The number of context switches
     */
    long getContextSwitches();

    /**
     * Get the number of interrupts which have occurred
     *
     * @return The number of interrupts
     */
    long getInterrupts();

    /**
     * Index of CPU tick counters in the {@link #getSystemCpuLoadTicks()} and
     * {@link #getProcessorCpuLoadTicks()} arrays.
     */
    enum TickType {
        /**
         * CPU utilization that occurred while executing at the user level
         * (application).
         */
        USER(0),
        /**
         * CPU utilization that occurred while executing at the user level with nice
         * priority.
         */
        NICE(1),
        /**
         * CPU utilization that occurred while executing at the system level (kernel).
         */
        SYSTEM(2),
        /**
         * Time that the CPU or CPUs were idle and the system did not have an
         * outstanding disk I/O request.
         */
        IDLE(3),
        /**
         * Time that the CPU or CPUs were idle during which the system had an
         * outstanding disk I/O request.
         */
        IOWAIT(4),
        /**
         * Time that the CPU used to service hardware IRQs
         */
        IRQ(5),
        /**
         * Time that the CPU used to service soft IRQs
         */
        SOFTIRQ(6),
        /**
         * Time which the hypervisor dedicated for other guests in the system. Only
         * supported on Linux and AIX
         */
        STEAL(7);

        private int index;

        TickType(int value) {
            this.index = value;
        }

        /**
         * @return The integer index of this ENUM in the processor tick arrays, which
         * matches the output of Linux /proc/cpuinfo
         */
        public int getIndex() {
            return index;
        }
    }

    /**
     * A class representing a Logical Processor and its replationship to physical
     * processors, physical packages, and logical groupings such as NUMA Nodes and
     * Processor groups, useful for identifying processor topology.
     */
    @Immutable
    class LogicalProcessor {
        private final int processorNumber;
        private final int physicalProcessorNumber;
        private final int physicalPackageNumber;
        private final int numaNode;
        private final int processorGroup;

        /**
         * @param processorNumber         the Processor number
         * @param physicalProcessorNumber the core number
         * @param physicalPackageNumber   the package/socket number
         */
        public LogicalProcessor(int processorNumber, int physicalProcessorNumber, int physicalPackageNumber) {
            this(processorNumber, physicalProcessorNumber, physicalPackageNumber, 0, 0);
        }

        /**
         * @param processorNumber         the Processor number
         * @param physicalProcessorNumber the core number
         * @param physicalPackageNumber   the package/socket number
         * @param numaNode                the NUMA node number
         */
        public LogicalProcessor(int processorNumber, int physicalProcessorNumber, int physicalPackageNumber,
                                int numaNode) {
            this(processorNumber, physicalProcessorNumber, physicalPackageNumber, numaNode, 0);
        }

        /**
         * @param processorNumber         the Processor number
         * @param physicalProcessorNumber the core number
         * @param physicalPackageNumber   the package/socket number
         * @param numaNode                the NUMA node number
         * @param processorGroup          the Processor Group number
         */
        public LogicalProcessor(int processorNumber, int physicalProcessorNumber, int physicalPackageNumber,
                                int numaNode, int processorGroup) {
            this.processorNumber = processorNumber;
            this.physicalProcessorNumber = physicalProcessorNumber;
            this.physicalPackageNumber = physicalPackageNumber;
            this.numaNode = numaNode;
            this.processorGroup = processorGroup;
        }

        /**
         * The Logical Processor number as seen by the Operating System. Used for
         * assigning process affinity and reporting CPU usage and other statistics.
         *
         * @return the processorNumber
         */
        public int getProcessorNumber() {
            return processorNumber;
        }

        /**
         * The physical processor (core) id number assigned to this logical processor.
         * Hyperthreaded logical processors which share the same physical processor will
         * have the same number.
         *
         * @return the physicalProcessorNumber
         */
        public int getPhysicalProcessorNumber() {
            return physicalProcessorNumber;
        }

        /**
         * The physical package (socket) id number assigned to this logical processor.
         * Multicore CPU packages may have multiple physical processors which share the
         * same number.
         *
         * @return the physicalPackageNumber
         */
        public int getPhysicalPackageNumber() {
            return physicalPackageNumber;
        }

        /**
         * The NUMA node. If the operating system supports Non-Uniform Memory Access
         * this identifies the node number. Set to 0 if the operating system does not
         * support NUMA. Not supported on macOS or FreeBSD.
         *
         * @return the NUMA Node number
         */
        public int getNumaNode() {
            return numaNode;
        }

        /**
         * The Processor Group. Only applies to Windows systems with more than 64
         * logical processors. Set to 0 for other operating systems or Windows systems
         * with 64 or fewer logical processors.
         *
         * @return the processorGroup
         */
        public int getProcessorGroup() {
            return processorGroup;
        }

        @Override
        public String toString() {
            return "LogicalProcessor [processorNumber=" + processorNumber + ", coreNumber=" + physicalProcessorNumber
                    + ", packageNumber=" + physicalPackageNumber + ", numaNode=" + numaNode + ", processorGroup="
                    + processorGroup + "]";
        }

    }

    /**
     * A class encapsulating ghe CPU's identifier strings ,including name, vendor,
     * stepping, model, and family information (also called the signature of a CPU)
     */
    @Immutable
    final class ProcessorIdentifier {

        // Provided in constructor
        private final String cpuVendor;
        private final String cpuName;
        private final String cpuFamily;
        private final String cpuModel;
        private final String cpuStepping;
        private final String processorID;
        private final String cpuIdentifier;
        private final boolean cpu64bit;
        private final long cpuVendorFreq;

        private final Supplier<String> microArchictecture = Memoize.memoize(this::queryMicroarchitecture);


        public ProcessorIdentifier(String cpuVendor, String cpuName, String cpuFamily, String cpuModel,
                                   String cpuStepping, String processorID, boolean cpu64bit) {
            this(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit, -1L);
        }

        public ProcessorIdentifier(String cpuVendor, String cpuName, String cpuFamily, String cpuModel,
                                   String cpuStepping, String processorID, boolean cpu64bit, long vendorFreq) {
            this.cpuVendor = cpuVendor;
            this.cpuName = cpuName;
            this.cpuFamily = cpuFamily;
            this.cpuModel = cpuModel;
            this.cpuStepping = cpuStepping;
            this.processorID = processorID;
            this.cpu64bit = cpu64bit;

            // Build Identifier
            StringBuilder sb = new StringBuilder();
            if (cpuVendor.contentEquals("GenuineIntel")) {
                sb.append(cpu64bit ? "Intel64" : "x86");
            } else {
                sb.append(cpuVendor);
            }
            sb.append(" Family ").append(cpuFamily);
            sb.append(" Model ").append(cpuModel);
            sb.append(" Stepping ").append(cpuStepping);
            this.cpuIdentifier = sb.toString();

            if (vendorFreq > 0) {
                this.cpuVendorFreq = vendorFreq;
            } else {
                // Parse Freq from name string
                Pattern pattern = Pattern.compile("@ (.*)$");
                Matcher matcher = pattern.matcher(cpuName);
                if (matcher.find()) {
                    String unit = matcher.group(1);
                    this.cpuVendorFreq = Builder.parseHertz(unit);
                } else {
                    this.cpuVendorFreq = -1L;
                }
            }
        }

        /**
         * Processor vendor.
         *
         * @return vendor string.
         */
        public String getVendor() {
            return cpuVendor;
        }

        /**
         * Name, eg. Intel(R) Core(TM)2 Duo CPU T7300 @ 2.00GHz
         *
         * @return Processor name.
         */
        public String getName() {
            return cpuName;
        }

        /**
         * Gets the family. For non-Intel/AMD processors, returns the comparable value,
         * such as the Architecture.
         *
         * @return the family
         */
        public String getFamily() {
            return cpuFamily;
        }

        /**
         * Gets the model. For non-Intel/AMD processors, returns the comparable value,
         * such as the Partnum.
         *
         * @return the model
         */
        public String getModel() {
            return cpuModel;
        }

        /**
         * Gets the stepping. For non-Intel/AMD processors, returns the comparable
         * value, such as the rnpn composite of Variant and Revision.
         *
         * @return the stepping
         */
        public String getStepping() {
            return cpuStepping;
        }

        /**
         * Gets the Processor ID. This is a hexidecimal string representing an 8-byte
         * value, normally obtained using the CPUID opcode with the EAX register set to
         * 1. The first four bytes are the resulting contents of the EAX register, which
         * is the Processor signature, represented in human-readable form by
         * {@link #getIdentifier()} . The remaining four bytes are the contents of the
         * EDX register, containing feature flags.
         * <p>
         * For processors that do not support the CPUID opcode this field is populated
         * with a comparable hex string. For example, ARM Processors will fill the first
         * 32 bytes with the MIDR.
         * <p>
         * NOTE: The order of returned bytes is platform and software dependent. Values
         * may be in either Big Endian or Little Endian order.
         *
         * @return A string representing the Processor ID
         */
        public String getProcessorID() {
            return processorID;
        }

        /**
         * Identifier, eg. x86 Family 6 Model 15 Stepping 10. For non-Intel/AMD
         * processors, this string is populated with comparable values.
         *
         * @return Processor identifier.
         */
        public String getIdentifier() {
            return cpuIdentifier;
        }

        /**
         * Is CPU 64bit?
         *
         * @return True if cpu is 64bit.
         */
        public boolean isCpu64bit() {
            return cpu64bit;
        }

        /**
         * Vendor frequency (in Hz), eg. for processor named Intel(R) Core(TM)2 Duo CPU
         * T7300 @ 2.00GHz the vendor frequency is 2000000000.
         *
         * @return Processor frequency or -1 if unknown.
         */
        public long getVendorFreq() {
            return cpuVendorFreq;
        }

        /**
         * Returns the processor's microarchitecture, if known.
         *
         * @return A string containing the microarchitecture if known.
         * {@link Normal#UNKNOWN} otherwise.
         */
        public String getMicroarchitecture() {
            return microArchictecture.get();
        }

        private String queryMicroarchitecture() {
            String arch = null;
            Properties archProps = Builder.readProperties(Builder.BUS_HEALTH_ARCH_PROPERTIES);
            // Intel is default, no prefix
            StringBuilder sb = new StringBuilder();
            // AMD and ARM properties have prefix
            String ucVendor = this.cpuVendor.toUpperCase();
            if (ucVendor.contains("AMD")) {
                sb.append("amd.");
            } else if (ucVendor.contains("ARM")) {
                sb.append("arm.");
            } else if (ucVendor.contains("IBM")) {
                // Directly parse the name to POWER#
                int powerIdx = this.cpuName.indexOf("_POWER");
                if (powerIdx > 0) {
                    arch = this.cpuName.substring(powerIdx + 1);
                }
            } else if (ucVendor.contains("APPLE")) {
                sb.append("apple.");
            }
            if (StringKit.isBlank(arch) && !sb.toString().equals("arm.")) {
                // Append family
                sb.append(this.cpuFamily);
                arch = archProps.getProperty(sb.toString());
            }

            if (StringKit.isBlank(arch)) {
                // Append model
                sb.append(Symbol.C_DOT).append(this.cpuModel);
                arch = archProps.getProperty(sb.toString());
            }

            if (StringKit.isBlank(arch)) {
                // Append stepping
                sb.append(Symbol.C_DOT).append(this.cpuStepping);
                arch = archProps.getProperty(sb.toString());
            }

            return StringKit.isBlank(arch) ? Normal.UNKNOWN : arch;
        }

        @Override
        public String toString() {
            return getIdentifier();
        }
    }

}