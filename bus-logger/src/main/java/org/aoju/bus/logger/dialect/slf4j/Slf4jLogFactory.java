/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.logger.dialect.slf4j;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.status.Status;
import org.aoju.bus.logger.Log;
import org.aoju.bus.logger.LogFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * <a href="http://www.slf4j.org/">SLF4J</a> log.<br>
 * 同样无缝支持 <a href="http://logback.qos.ch/">LogBack</a>
 *
 * @author Kimi Liu
 * @version 3.0.5
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
