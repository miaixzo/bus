package org.aoju.bus.office.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.Objects;

/**
 * 从输入流中读取所有行.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class StreamPumper extends Thread {

    private final InputStream stream;
    private final LineConsumer consumer;

    /**
     * 为指定的流创建一个新的pumper.
     *
     * @param stream   要从中读取的输入流.
     * @param consumer 从输入流读取行的使用者.
     */
    public StreamPumper(final InputStream stream, final LineConsumer consumer) {
        super();

        Objects.requireNonNull(stream, "stream must not be null");
        Objects.requireNonNull(stream, "consumer must not be null");

        this.stream = stream;
        this.consumer = consumer;
        this.setDaemon(true);
    }

    /**
     * 获取从输入流读取的行的使用者.
     *
     * @return The consumer.
     */
    public LineConsumer getConsumer() {
        return consumer;
    }

    @Override
    public void run() {
        try (BufferedReader bufferedReader =
                     new BufferedReader(Channels.newReader(Channels.newChannel(stream), "UTF-8"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                consumer.consume(line);
            }
        } catch (IOException ex) {
            // ignore errors
        }
    }

    /**
     * 提供一个函数来使用从流中读取的行.
     */
    @FunctionalInterface
    public interface LineConsumer {

        /**
         * 使用从输入流读取的行.
         *
         * @param line 读取行信息.
         */
        void consume(String line);
    }

}
