package org.aoju.bus.office;

import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.process.PumpStreamHandler;
import org.aoju.bus.office.process.StreamPumper;

import java.util.Objects;

/**
 * 重定向输出和错误流的进程的包装器类.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class Expense {

    private final Process process;
    private final PumpStreamHandler streamHandler;

    /**
     * 为给定的流程创建一个新的包装器.
     *
     * @param process 为其创建包装器的过程.
     */
    public Expense(final Process process) {
        super();

        Objects.requireNonNull(process, "process must not be null");

        this.process = process;

        streamHandler =
                new PumpStreamHandler(
                        new StreamPumper(process.getInputStream(), (line) -> Logger.info(line)),
                        new StreamPumper(process.getErrorStream(), (line) -> Logger.error(line)));
        streamHandler.start();
    }

    /**
     * 获取此包装器的进程.
     *
     * @return 当前这个进程.
     */
    public Process getProcess() {
        return process;
    }

    /**
     * 获取进程的退出代码.
     *
     * @return 进程的退出码，如果尚未终止则为空.
     */
    public Integer getExitCode() {
        try {
            final int exitValue = process.exitValue();
            streamHandler.stop();
            return exitValue;

        } catch (IllegalThreadStateException ex) {
            Logger.trace("The Office process has not yet terminated.");
            return null;
        }
    }

}
