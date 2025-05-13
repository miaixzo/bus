package org.miaixz.bus.health.builtin;

import java.util.Arrays;
import java.util.List;

/**
 * 健康状态监控类型标识常量类。
 * <p>
 * 定义了系统和硬件信息监控的类型标识符（Type Identifiers）。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class TID {

    /**
     * 主机信息
     */
    public static final String HOST = "host";

    /**
     * CPU 信息
     */
    public static final String CPU = "cpu";

    /**
     * 磁盘信息
     */
    public static final String DISK = "disk";

    /**
     * JVM 虚拟机信息
     */
    public static final String JVM = "jvm";

    /**
     * 内存信息
     */
    public static final String MEMORY = "memory";

    /**
     * 所有磁盘使用率信息
     */
    public static final String ALL_DISK = "alldisk";

    /**
     * 系统进程信息
     */
    public static final String PROCESS = "process";

    /**
     * 系统信息
     */
    public static final String SYSTEM = "system";

    /**
     * 处理器信息
     */
    public static final String PROCESSOR = "processor";

    /**
     * 硬件信息
     */
    public static final String HARDWARE = "hardware";

    /**
     * 存活状态
     */
    public static final String LIVENESS = "liveness";

    /**
     * 就绪状态
     */
    public static final String READINESS = "readiness";

    /**
     * 就绪状态
     */
    public static final String POWERSOURCES = "powerSources";

    /**
     * 就绪状态
     */
    public static final String NETWORKIFS = "networkIFs";

    /**
     * 所有监控类型
     */
    public static final String ALL = "all";

    /**
     * 所有监控类型的列表
     */
    public static final List<String> ALL_TID = Arrays.asList(HOST, CPU, DISK, JVM, MEMORY, ALL_DISK, PROCESS, SYSTEM,
            PROCESSOR, HARDWARE, LIVENESS, READINESS, POWERSOURCES, NETWORKIFS);

}