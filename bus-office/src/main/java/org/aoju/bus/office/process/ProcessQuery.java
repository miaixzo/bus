package org.aoju.bus.office.process;

/**
 * 包含用于查询正在运行的进程所需的信息.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class ProcessQuery {

    private final String command;
    private final String argument;

    /**
     * 使用给定的命令和参数构造一个新实例.
     *
     * @param command  处理命令.
     * @param argument 过程参数.
     */
    public ProcessQuery(final String command, final String argument) {
        super();
        this.command = command;
        this.argument = argument;
    }

    /**
     * 获取要查询的流程的参数.
     *
     * @return 过程参数.
     */
    public String getArgument() {
        return argument;
    }

    /**
     * 获取要查询的进程的命令.
     *
     * @return 处理命令.
     */
    public String getCommand() {
        return command;
    }

}
