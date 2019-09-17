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
package org.aoju.bus.spring.servlet;


import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author Kimi Liu
 * @version 3.5.0
 * @since JDK 1.8
 */
public class BodyCacheHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private PrintWriter writer = new PrintWriter(byteArrayOutputStream);

    BodyCacheHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
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
                TeeOutputStream write = new TeeOutputStream(BodyCacheHttpServletResponseWrapper.super.getOutputStream(), byteArrayOutputStream);
                write.write(b);
            }
        };
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new ServletPrintWriter(super.getWriter(), writer);
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

    class TeeOutputStream
            extends OutputStream {
        private OutputStream output1;
        private OutputStream output2;

        /**
         * Base constructor.
         *
         * @param output1 the output stream that is wrapped.
         * @param output2 a secondary stream that anything written to output1 is also written to.
         */
        public TeeOutputStream(OutputStream output1, OutputStream output2) {
            this.output1 = output1;
            this.output2 = output2;
        }

        public void write(byte[] buf)
                throws IOException {
            this.output1.write(buf);
            this.output2.write(buf);
        }

        public void write(byte[] buf, int off, int len)
                throws IOException {
            this.output1.write(buf, off, len);
            this.output2.write(buf, off, len);
        }

        public void write(int b)
                throws IOException {
            this.output1.write(b);
            this.output2.write(b);
        }

        public void flush()
                throws IOException {
            this.output1.flush();
            this.output2.flush();
        }

        public void close()
                throws IOException {
            this.output1.close();
            this.output2.close();
        }
    }

}
