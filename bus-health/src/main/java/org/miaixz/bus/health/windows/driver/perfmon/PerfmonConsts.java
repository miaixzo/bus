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
package org.miaixz.bus.health.windows.driver.perfmon;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * Constants used in Perfmon driver classes
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class PerfmonConsts {

    /*
     * Instance filter constants used by enums implementing PdhCounterProperty or PdhCounterWildcardProperty
     */
    /**
     * The TOTAL_INSTANCE constant.
     */
    public static final String TOTAL_INSTANCE = "_Total";

    /**
     * The TOTAL_OR_IDLE_INSTANCES constant.
     */
    public static final String TOTAL_OR_IDLE_INSTANCES = "_Total|Idle";

    /**
     * The TOTAL_INSTANCES constant.
     */
    public static final String TOTAL_INSTANCES = "*_Total";

    /**
     * The NOT_TOTAL_INSTANCE constant.
     */
    public static final String NOT_TOTAL_INSTANCE = "^" + TOTAL_INSTANCE;

    /**
     * The NOT_TOTAL_INSTANCES constant.
     */
    public static final String NOT_TOTAL_INSTANCES = "^" + TOTAL_INSTANCES;

    /**
     * Perfmon counter names and corresponding WMI tables
     */
    public static final String MEMORY = "Memory";

    /**
     * The WIN32_PERF_RAW_DATA_PERF_OS_MEMORY constant.
     */
    public static final String WIN32_PERF_RAW_DATA_PERF_OS_MEMORY = "Win32_PerfRawData_PerfOS_Memory";

    /**
     * The PAGING_FILE constant.
     */
    public static final String PAGING_FILE = "Paging File";

    /**
     * The WIN32_PERF_RAW_DATA_PERF_OS_PAGING_FILE constant.
     */
    public static final String WIN32_PERF_RAW_DATA_PERF_OS_PAGING_FILE = "Win32_PerfRawData_PerfOS_PagingFile";

    /**
     * The PHYSICAL_DISK constant.
     */
    public static final String PHYSICAL_DISK = "PhysicalDisk";

    /**
     * The WIN32_PERF_RAW_DATA_PERF_DISK_PHYSICAL_DISK_WHERE_NAME_NOT_TOTAL constant.
     */
    public static final String WIN32_PERF_RAW_DATA_PERF_DISK_PHYSICAL_DISK_WHERE_NAME_NOT_TOTAL = "Win32_PerfRawData_PerfDisk_PhysicalDisk WHERE Name!=\"_Total\"";

    /**
     * The PROCESS constant.
     */
    public static final String PROCESS = "Process";

    /**
     * The WIN32_PERFPROC_PROCESS constant.
     */
    public static final String WIN32_PERFPROC_PROCESS = "Win32_PerfRawData_PerfProc_Process";

    /**
     * The WIN32_PERFPROC_PROCESS_WHERE_NOT_NAME_LIKE_TOTAL constant.
     */
    public static final String WIN32_PERFPROC_PROCESS_WHERE_NOT_NAME_LIKE_TOTAL = WIN32_PERFPROC_PROCESS
            + " WHERE NOT Name LIKE \"%_Total\"";

    /**
     * The WIN32_PERFPROC_PROCESS_WHERE_NAME_TOTAL constant.
     */
    public static final String WIN32_PERFPROC_PROCESS_WHERE_NAME_TOTAL = WIN32_PERFPROC_PROCESS
            + " WHERE Name=\"_Total\"";

    /**
     * The WIN32_PERFPROC_PROCESS_WHERE_IDPROCESS_0 constant.
     */
    public static final String WIN32_PERFPROC_PROCESS_WHERE_IDPROCESS_0 = "Win32_PerfRawData_PerfProc_Process WHERE IDProcess=0";

    /**
     * The THREAD constant.
     */
    public static final String THREAD = "Thread";

    /**
     * The WIN32_PERF_RAW_DATA_PERF_PROC_THREAD constant.
     */
    public static final String WIN32_PERF_RAW_DATA_PERF_PROC_THREAD = "Win32_PerfRawData_PerfProc_Thread";

    /**
     * The WIN32_PERF_RAW_DATA_PERF_PROC_THREAD_WHERE_NOT_NAME_LIKE_TOTAL constant.
     */
    public static final String WIN32_PERF_RAW_DATA_PERF_PROC_THREAD_WHERE_NOT_NAME_LIKE_TOTAL = "Win32_PerfRawData_PerfProc_Thread WHERE NOT Name LIKE \"%_Total\"";

    // For Vista- ... Older systems just have processor #
    /**
     * The PROCESSOR constant.
     */
    public static final String PROCESSOR = "Processor";

    /**
     * The WIN32_PERF_RAW_DATA_PERF_OS_PROCESSOR_WHERE_NAME_NOT_TOTAL constant.
     */
    public static final String WIN32_PERF_RAW_DATA_PERF_OS_PROCESSOR_WHERE_NAME_NOT_TOTAL = "Win32_PerfRawData_PerfOS_Processor WHERE Name!=\"_Total\"";

    /**
     * The WIN32_PERF_RAW_DATA_PERF_OS_PROCESSOR_WHERE_NAME_TOTAL constant.
     */
    public static final String WIN32_PERF_RAW_DATA_PERF_OS_PROCESSOR_WHERE_NAME_TOTAL = "Win32_PerfRawData_PerfOS_Processor WHERE Name=\"_Total\"";

    // For Win7+ ... NAME field includes NUMA nodes
    /**
     * The PROCESSOR_INFORMATION constant.
     */
    public static final String PROCESSOR_INFORMATION = "Processor Information";

    /**
     * The WIN32_PERF_RAW_DATA_COUNTERS_PROCESSOR_INFORMATION_WHERE_NOT_NAME_LIKE_TOTAL constant.
     */
    public static final String WIN32_PERF_RAW_DATA_COUNTERS_PROCESSOR_INFORMATION_WHERE_NOT_NAME_LIKE_TOTAL = "Win32_PerfRawData_Counters_ProcessorInformation WHERE NOT Name LIKE \"%_Total\"";

    /**
     * The WIN32_PERF_FORMATTED_DATA_COUNTERS_PROCESSOR_INFORMATION_WHERE_NOT_NAME_LIKE_TOTAL constant.
     */
    public static final String WIN32_PERF_FORMATTED_DATA_COUNTERS_PROCESSOR_INFORMATION_WHERE_NOT_NAME_LIKE_TOTAL = "Win32_PerfFormattedData_Counters_ProcessorInformation WHERE NOT Name LIKE \"%_Total\"";

    /**
     * The SYSTEM constant.
     */
    public static final String SYSTEM = "System";

    /**
     * The WIN32_PERF_RAW_DATA_PERF_OS_SYSTEM constant.
     */
    public static final String WIN32_PERF_RAW_DATA_PERF_OS_SYSTEM = "Win32_PerfRawData_PerfOS_System";

    /**
     * The GPU_ENGINE constant.
     */
    public static final String GPU_ENGINE = "GPU Engine";

    /**
     * The GPU_ADAPTER_MEMORY constant.
     */
    public static final String GPU_ADAPTER_MEMORY = "GPU Adapter Memory";

    /**
     * Everything in this class is static, never instantiate it
     */
    private PerfmonConsts() {
        throw new AssertionError();
    }

}
