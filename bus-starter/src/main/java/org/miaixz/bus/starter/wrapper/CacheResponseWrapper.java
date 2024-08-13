/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheResponseWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private PrintWriter writer = new PrintWriter(byteArrayOutputStream);

    CacheResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new ServletPrintWriter(super.getWriter(), writer);
    }

    public byte[] getBody() {
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {
                TeeOutputStream write = new TeeOutputStream(CacheResponseWrapper.super.getOutputStream(),
                        byteArrayOutputStream);
                write.write(b);
            }
        };
    }

    private static class ServletPrintWriter extends PrintWriter {

        PrintWriter printWriter;

        ServletPrintWriter(PrintWriter main, PrintWriter printWriter) {
            super(main, true);
            this.printWriter = printWriter;
        }

        @Override
        public void write(char[] buff, int off, int len) {
            super.write(buff, off, len);
            super.flush();
            printWriter.write(buff, off, len);
            printWriter.flush();
        }

        @Override
        public void write(String s, int off, int len) {
            super.write(s, off, len);
            super.flush();
            printWriter.write(s, off, len);
            printWriter.flush();
        }

        @Override
        public void write(int c) {
            super.write(c);
            super.flush();
            printWriter.write(c);
            printWriter.flush();
        }

        @Override
        public void flush() {
            super.flush();
            printWriter.flush();
        }
    }

    class TeeOutputStream extends OutputStream {

        private OutputStream oneOut;
        private OutputStream twoOut;

        /**
         * @param oneOut 被包装的输出流.
         * @param twoOut 任何写入oneOut的内容也会被写入一个次级流.
         */
        public TeeOutputStream(OutputStream oneOut, OutputStream twoOut) {
            this.oneOut = oneOut;
            this.twoOut = twoOut;
        }

        public void write(byte[] buf) throws IOException {
            this.oneOut.write(buf);
            this.twoOut.write(buf);
        }

        public void write(byte[] buf, int off, int len) throws IOException {
            this.oneOut.write(buf, off, len);
            this.twoOut.write(buf, off, len);
        }

        public void write(int b) throws IOException {
            this.oneOut.write(b);
            this.twoOut.write(b);
        }

        public void flush() throws IOException {
            this.oneOut.flush();
            this.twoOut.flush();
        }

        public void close() throws IOException {
            this.oneOut.close();
            this.twoOut.close();
        }
    }

}
