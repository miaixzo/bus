package org.aoju.bus.office.process;

import java.io.IOException;

/**
 * 提供管理正在运行的流程所需的服务.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface ProcessManager {

    /**
     * 查找具有指定命令行的运行进程的PID.
     *
     * @param query 用于查找带有要pid的进程的查询.
     * @return 如果没有找到，则使用pid;
     * 如果没有找到，则使用{@link org.aoju.bus.office.Builder#PID_NOT_FOUND};
     * 如果没有找到，则使用{@link org.aoju.bus.office.Builder#PID_UNKNOWN}
     * @throws IOException 如果IO错误发生.
     */
    long find(ProcessQuery query) throws IOException;

    /**
     * 终止指定的进程
     *
     * @param process 进程信息.
     * @param pid     进程对应pid.
     * @throws IOException 如果IO错误发生.
     */
    void kill(Process process, long pid) throws IOException;

}
