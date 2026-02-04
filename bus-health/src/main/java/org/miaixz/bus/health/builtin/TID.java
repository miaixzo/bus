/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
