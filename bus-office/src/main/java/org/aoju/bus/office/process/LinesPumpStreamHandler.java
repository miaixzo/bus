package org.aoju.bus.office.process;

import java.io.InputStream;

/**
 * 将子进程的标准输出和错误复制到行列表中.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class LinesPumpStreamHandler extends PumpStreamHandler {

    /**
     * 构造一个新的{@code lines抽水处理程序}.
     *
     * @param output 输出流.
     * @param error  错误流.
     */
    public LinesPumpStreamHandler(final InputStream output, final InputStream error) {
        super(new LinesStreamPumper(output), new LinesStreamPumper(error));
    }

    @Override
    public LinesStreamPumper getOutputPumper() {
        return (LinesStreamPumper) super.getOutputPumper();
    }

    @Override
    public LinesStreamPumper getErrorPumper() {
        return (LinesStreamPumper) super.getErrorPumper();
    }

}
