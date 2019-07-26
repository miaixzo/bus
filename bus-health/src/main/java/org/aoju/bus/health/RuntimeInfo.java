package org.aoju.bus.health;

import org.aoju.bus.core.utils.FileUtils;

/**
 * 运行时信息，包括内存总大小、已用大小、可用大小等
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class RuntimeInfo {

    private Runtime currentRuntime = Runtime.getRuntime();

    /**
     * 获得运行时对象
     *
     * @return {@link Runtime}
     */
    public final Runtime getRuntime() {
        return currentRuntime;
    }

    /**
     * 获得JVM最大可用内存
     *
     * @return 最大可用内存
     */
    public final long getMaxMemory() {
        return currentRuntime.maxMemory();
    }

    /**
     * 获得JVM已分配内存
     *
     * @return 已分配内存
     */
    public final long getTotalMemory() {
        return currentRuntime.totalMemory();
    }

    /**
     * 获得JVM已分配内存中的剩余空间
     *
     * @return 已分配内存中的剩余空间
     */
    public final long getFreeMemory() {
        return currentRuntime.freeMemory();
    }

    /**
     * 获得JVM最大可用内存
     *
     * @return 最大可用内存
     */
    public final long getUsableMemory() {
        return currentRuntime.maxMemory() - currentRuntime.totalMemory() + currentRuntime.freeMemory();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        SystemUtils.append(builder, "Max Memory:    ", FileUtils.readableFileSize(getMaxMemory()));
        SystemUtils.append(builder, "Total Memory:     ", FileUtils.readableFileSize(getTotalMemory()));
        SystemUtils.append(builder, "Free Memory:     ", FileUtils.readableFileSize(getFreeMemory()));
        SystemUtils.append(builder, "Usable Memory:     ", FileUtils.readableFileSize(getUsableMemory()));
        return builder.toString();
    }

}
