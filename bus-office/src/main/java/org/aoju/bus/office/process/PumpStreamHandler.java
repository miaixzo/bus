package org.aoju.bus.office.process;

/**
 * 将子过程的标准输出和错误复制到给定的pumpers.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class PumpStreamHandler {

    private final StreamPumper outputPumper;
    private final StreamPumper errorPumper;

    /**
     * 构造一个新的 {@code PumpStreamHandler}.
     *
     * @param outputPumper 输出流 {@code StreamPumper}.
     * @param errorPumper  错误信息 {@code StreamPumper}.
     */
    public PumpStreamHandler(final StreamPumper outputPumper, final StreamPumper errorPumper) {
        this.outputPumper = outputPumper;
        this.errorPumper = errorPumper;
    }

    /**
     * 获取输出 {@code StreamPumper}.
     *
     * @return 输出 pumper.
     */
    public StreamPumper getOutputPumper() {
        return outputPumper;
    }

    /**
     * 获取错误 {@code StreamPumper}.
     *
     * @return 输出错误 pumper.
     */
    public StreamPumper getErrorPumper() {
        return errorPumper;
    }

    /**
     * 启动 pumpers.
     */
    public void start() {
        outputPumper.start();
        errorPumper.start();
    }

    /**
     * 停止 pumpers.
     */
    public void stop() {
        try {
            outputPumper.join();
        } catch (InterruptedException e) {
            // ignore
        }
        try {
            errorPumper.join();
        } catch (InterruptedException e) {
            // ignore
        }
    }

}
