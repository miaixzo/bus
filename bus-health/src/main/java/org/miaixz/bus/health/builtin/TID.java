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
package org.miaixz.bus.health.builtin;

import java.util.Arrays;
import java.util.List;

/**
 * Health status monitoring type identifier constants class.
 * <p>
 * Defines the Type Identifiers for system and hardware information monitoring.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class TID {

    /**
     * Host information
     */
    public static final String HOST = "host";

    /**
     * CPU information
     */
    public static final String CPU = "cpu";

    /**
     * Disk information
     */
    public static final String DISK = "disk";

    /**
     * JVM virtual machine information
     */
    public static final String JVM = "jvm";

    /**
     * Memory information
     */
    public static final String MEMORY = "memory";

    /**
     * All disk usage information
     */
    public static final String ALL_DISK = "alldisk";

    /**
     * System process information
     */
    public static final String PROCESS = "process";

    /**
     * System information
     */
    public static final String SYSTEM = "system";

    /**
     * Processor information
     */
    public static final String PROCESSOR = "processor";

    /**
     * Hardware information
     */
    public static final String HARDWARE = "hardware";

    /**
     * Liveness status
     */
    public static final String LIVENESS = "liveness";

    /**
     * Readiness status
     */
    public static final String READINESS = "readiness";

    /**
     * Power sources information
     */
    public static final String POWERSOURCES = "powerSources";

    /**
     * Network interfaces information
     */
    public static final String NETWORKIFS = "networkIFs";

    /**
     * All monitoring types
     */
    public static final String ALL = "all";

    /**
     * List of all monitoring types
     */
    public static final List<String> ALL_TID = Arrays.asList(
            HOST,
            CPU,
            DISK,
            JVM,
            MEMORY,
            ALL_DISK,
            PROCESS,
            SYSTEM,
            PROCESSOR,
            HARDWARE,
            LIVENESS,
            READINESS,
            POWERSOURCES,
            NETWORKIFS);

}
