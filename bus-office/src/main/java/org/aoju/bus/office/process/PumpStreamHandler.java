/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.office.process;

/**
 * 将子过程的标准输出和错误复制到给定的pumpers.
 *
 * @author Kimi Liu
 * @version 5.3.6
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
