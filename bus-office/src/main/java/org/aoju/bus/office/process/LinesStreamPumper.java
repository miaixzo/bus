package org.aoju.bus.office.process;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 从输入流中读取所有行.
 */
public class LinesStreamPumper extends StreamPumper {

    /**
     * 为指定的流创建一个新的pumper.
     *
     * @param stream 要从中读取的输入流.
     */
    public LinesStreamPumper(final InputStream stream) {
        super(stream, new LinesConsumer());
    }

    /**
     * 读取该pumper从流中读取的行
     *
     * @return 命令输出行.
     */
    public List<String> getLines() {
        return ((LinesConsumer) getConsumer()).lines;
    }

    private static class LinesConsumer implements LineConsumer {

        private final List<String> lines = new ArrayList<>();

        @Override
        public void consume(final String line) {
            lines.add(line);
        }
    }

}
