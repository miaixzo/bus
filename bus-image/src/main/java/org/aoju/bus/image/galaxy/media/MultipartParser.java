/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.galaxy.media;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class MultipartParser {

    /**
     * 回车符，'\ r'，0x0D，十进制13
     */
    public static final byte CR = 0x0D;
    /**
     * 换行符'\ n'，0x0D，十进制十进制
     */
    public static final byte LF = 0x0A;
    /**
     * 破折号，'-'，0x2D，十进制45
     */
    public static final byte DASH = 0x2D;

    private String boundary;

    private MultipartParser() {
    }

    public MultipartParser(String boundary) {
        this.boundary = boundary;
    }

    public static void parseMultipartRelated(URLConnection urlConnection, InputStream inputStream, Handler handler)
            throws IOException {
        String contentType = urlConnection.getContentType();
        byte[] boundary = getBoundary(contentType, MediaType.MULTIPART_RELATED);
        if (null == boundary) {
            throw new IllegalStateException("Cannot find boundary of multipart");
        }
        MultipartReader multipartReader = new MultipartReader(inputStream, boundary);
        multipartReader.setHeaderEncoding("UTF8");

        int k = 1;
        boolean nextPart = multipartReader.skipFirstBoundary();
        while (nextPart) {
            String headersString = multipartReader.readHeaders();
            handler.bodyPart(multipartReader, k++, getHeaders(headersString));
            nextPart = multipartReader.readBoundary();
        }
    }

    protected static Map<String, String> getHeaders(String header) {
        final Map<String, String> headers = new HashMap<>();
        int length = header.length();
        int start = 0;
        while (true) {
            int end = parseEOF(header, start);
            if (start == end) {
                break;
            }
            StringBuilder buf = new StringBuilder(header.substring(start, end));
            start = end + 2;
            while (start < length) {
                int k = start;
                while (k < length) {
                    char c = header.charAt(k);
                    if (c != Symbol.C_SPACE && c != Symbol.C_HT) {
                        break;
                    }
                    ++k;
                }
                if (k == start) {
                    break;
                }
                end = parseEOF(header, k);
                buf.append(Symbol.SPACE);
                buf.append(header, k, end);
                start = end + 2;
            }

            String field = buf.toString();
            int index = field.indexOf(Symbol.C_COLON);
            if (index == -1) {
                continue;
            }
            String name = field.substring(0, index).trim();
            String value = field.substring(field.indexOf(Symbol.C_COLON) + 1).trim();

            if (headers.containsKey(name)) {
                headers.put(name, headers.get(name) + Symbol.COMMA + value);
            } else {
                headers.put(name, value);
            }
        }

        return headers;
    }

    private static int parseEOF(String header, int pos) {
        int index = pos;
        while (true) {
            int k = header.indexOf(Symbol.C_CR, index);
            if (k == -1 || k + 1 >= header.length()) {
                throw new IllegalStateException("No EOF found in headers");
            }
            if (header.charAt(k + 1) == Symbol.C_LF) {
                return k;
            }
            index = k + 1;
        }
    }

    protected static byte[] getBoundary(String respContentType, String ckeckMultipartType) {
        if (!StringKit.hasText(respContentType)) {
            return null;
        }
        HeaderFieldValues parser = new HeaderFieldValues(respContentType);
        String boundaryStr = parser.getValue("boundary");

        if (null == boundaryStr || (null != ckeckMultipartType && !parser.hasKey(ckeckMultipartType))) {
            return null;
        }
        return boundaryStr.getBytes(Charset.ISO_8859_1);
    }

    public void parse(InputStream in, Handler handler) throws IOException {
        new MultipartInputStream(in, Symbol.MINUS + Symbol.MINUS + boundary).skipAll();
        for (int i = 1; ; i++) {
            int ch1 = in.read();
            int ch2 = in.read();
            if ((ch1 | ch2) < 0)
                throw new EOFException();

            if (ch1 == Symbol.C_MINUS && ch2 == Symbol.C_MINUS)
                break;

            if (ch1 != Symbol.C_CR || ch2 != Symbol.C_LF)
                throw new IOException("missing CR/LF after boundary");

            MultipartInputStream mis = new MultipartInputStream(in, "\r\n--" + boundary);
            handler.bodyPart(mis, i);
            mis.skipAll();
        }
    }

    public enum Separator {
        HEADER(new byte[]{CR, LF, CR, LF}), FIELD(new byte[]{CR, LF}),
        BOUNDARY(new byte[]{CR, LF, DASH, DASH}), STREAM(new byte[]{DASH, DASH});

        private final byte[] type;

        Separator(byte[] type) {
            this.type = type;
        }

        public byte[] getType() {
            return type;
        }

        @Override
        public String toString() {
            return new String(type);
        }
    }

    public interface Handler {

        void bodyPart(MultipartInputStream in, int partNumber) throws IOException;

        void bodyPart(MultipartReader multipartReader, int partNumber, Map<String, String> headers)
                throws IOException;
    }

}
