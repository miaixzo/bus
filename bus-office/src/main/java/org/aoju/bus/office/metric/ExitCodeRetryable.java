package org.aoju.bus.office.metric;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.Expense;

/**
 * 获取office进程的退出码值.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class ExitCodeRetryable extends AbstractRetryable {

    private final Expense process;
    private int exitCode;

    /**
     * 为指定的进程创建类的新实例.
     *
     * @param process 要检索其退出码的进程.
     */
    public ExitCodeRetryable(final Expense process) {
        super();
        this.process = process;
    }

    @Override
    protected void attempt() throws InstrumentException {
        final Integer code = process.getExitCode();
        if (code == null) {
            throw new InstrumentException();
        }
        exitCode = code.intValue();
    }

    /**
     * 进程的退出码.
     *
     * @return 进程的退出值。值0表示正常终止.
     */
    public int getExitCode() {
        return exitCode;
    }

}
