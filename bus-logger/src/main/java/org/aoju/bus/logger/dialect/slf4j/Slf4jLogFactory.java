package org.aoju.bus.logger.dialect.slf4j;

import org.aoju.bus.logger.Log;
import org.aoju.bus.logger.LogFactory;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.status.Status;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * <a href="http://www.slf4j.org/">SLF4J</a> log.<br>
 * 同样无缝支持 <a href="http://logback.qos.ch/">LogBack</a>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class Slf4jLogFactory extends LogFactory {

    public Slf4jLogFactory() {
        this(false);
    }

    /**
     * 构造
     *
     * @param failIfNOP 如果未找到桥接包是否报错
     */
    public Slf4jLogFactory(boolean failIfNOP) {
        super("Slf4j");
        checkLogExist(LoggerFactory.class);
        if (false == failIfNOP) {
            return;
        }

        // SFL4J writes it error messages to System.err. Capture them so that the user does not see such a message on
        // the console during automatic detection.
        final StringBuilder buf = new StringBuilder();
        final PrintStream err = System.err;
        try {
            System.setErr(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    buf.append((char) b);
                }
            }, true, "UTF-8"));
            OutputStreamAppender s = new OutputStreamAppender();
            s.setEncoder(new Encoder() {
                @Override
                public byte[] headerBytes() {
                    return new byte[0];
                }

                @Override
                public byte[] encode(Object event) {
                    return new byte[0];
                }

                @Override
                public byte[] footerBytes() {
                    return new byte[0];
                }

                @Override
                public Context getContext() {
                    return null;
                }

                @Override
                public void setContext(Context context) {

                }

                @Override
                public void addStatus(Status status) {

                }

                @Override
                public void addInfo(String msg) {

                }

                @Override
                public void addInfo(String msg, Throwable ex) {

                }

                @Override
                public void addWarn(String msg) {

                }

                @Override
                public void addWarn(String msg, Throwable ex) {

                }

                @Override
                public void addError(String msg) {

                }

                @Override
                public void addError(String msg, Throwable ex) {

                }

                @Override
                public void start() {

                }

                @Override
                public void stop() {

                }

                @Override
                public boolean isStarted() {
                    return false;
                }
            });
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }

        try {
            if (LoggerFactory.getILoggerFactory() instanceof NOPLoggerFactory) {
                throw new NoClassDefFoundError(buf.toString());
            } else {
                err.print(buf);
                err.flush();
            }
        } finally {
            System.setErr(err);
        }
    }

    @Override
    public Log createLog(String name) {
        return new Slf4J(name);
    }

    @Override
    public Log createLog(Class<?> clazz) {
        return new Slf4J(clazz);
    }

}
