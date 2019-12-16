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
