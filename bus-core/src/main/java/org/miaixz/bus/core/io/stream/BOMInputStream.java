/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.io.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import org.miaixz.bus.core.io.ByteOrderMark;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * An {@link InputStream} wrapper that detects and skips a Byte Order Mark (BOM) from the beginning of the stream. After
 * calling {@code getCharset()}, the detected character set (if a BOM is present) will be returned, and the BOM bytes
 * will be effectively removed from the stream.
 * <p>
 * BOM definitions:
 * <a href="http://www.unicode.org/unicode/faq/utf_bom.html">http://www.unicode.org/unicode/faq/utf_bom.html</a>
 * <ul>
 * <li>00 00 FE FF = UTF-32, big-endian</li>
 * <li>FF FE 00 00 = UTF-32, little-endian</li>
 * <li>EF BB BF = UTF-8</li>
 * <li>FE FF = UTF-16, big-endian</li>
 * <li>FF FE = UTF-16, little-endian</li>
 * </ul>
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * String enc = "UTF-8"; // or NULL to use systemdefault
 * FileInputStream fis = new FileInputStream(file);
 * BOMInputStream uin = new BOMInputStream(fis, enc);
 * enc = uin.getCharset(); // check and skip possible BOM bytes
 * }</pre>
 * <p>
 * Reference: <a href="http://akini.mbnet.fi/java/unicodereader/UnicodeInputStream.java.txt">
 * http://akini.mbnet.fi/java/unicodereader/UnicodeInputStream.java.txt</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BOMInputStream extends InputStream {

    /**
     * The maximum number of bytes to check for a BOM.
     */
    private static final int BOM_SIZE = 4;
    /**
     * The underlying {@link PushbackInputStream} used for reading and pushing back bytes.
     */
    private final PushbackInputStream in;
    /**
     * The default character set to use if no BOM is detected.
     */
    private final String defaultCharset;
    /**
     * A flag indicating whether the BOM detection and charset initialization has been performed.
     */
    private boolean initialized = false;
    /**
     * The detected character set, or the default character set if no BOM is found.
     */
    private String charset;

    /**
     * Constructs a new {@code BOMInputStream} with the given input stream and a default character set of UTF-8.
     *
     * @param in The input stream to wrap.
     */
    public BOMInputStream(final InputStream in) {
        this(in, Charset.DEFAULT_UTF_8);
    }

    /**
     * Constructs a new {@code BOMInputStream} with the given input stream and a specified default character set.
     *
     * @param in             The input stream to wrap.
     * @param defaultCharset The default character set to use if no BOM is detected.
     */
    public BOMInputStream(final InputStream in, final String defaultCharset) {
        this.in = new PushbackInputStream(in, BOM_SIZE);
        this.defaultCharset = defaultCharset;
    }

    /**
     * Returns the default character set used by this stream.
     *
     * @return The default character set.
     */
    public String getDefaultCharset() {
        return this.defaultCharset;
    }

    /**
     * Returns the character set detected from the BOM, or the default character set if no BOM is present. This method
     * triggers the BOM detection and removal if it hasn't been performed yet.
     *
     * @return The detected or default character set.
     * @throws InternalException If an {@link IOException} occurs during BOM detection.
     */
    public String getCharset() {
        if (!this.initialized) {
            try {
                init();
            } catch (final IOException ex) {
                throw new InternalException(ex);
            }
        }
        return this.charset;
    }

    /**
     * Closes the underlying input stream.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        this.initialized = true;
        in.close();
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is returned as an {@code int} in the range
     * {@code 0} to {@code 255}. If no byte is available because the end of the stream has been reached, the value
     * {@code -1} is returned. This method triggers the BOM detection and removal if it hasn't been performed yet.
     *
     * @return The next byte of data, or {@code -1} if the end of the stream is reached.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        this.initialized = true;
        return in.read();
    }

    /**
     * Reads ahead a few bytes and checks for BOM marks. If a BOM is found, its bytes are skipped, and the corresponding
     * character set is determined. Any non-BOM bytes read are pushed back to the stream.
     *
     * @throws IOException If an I/O error occurs during reading or pushing back bytes.
     */
    protected void init() throws IOException {
        if (this.initialized) {
            return;
        }

        final byte[] bom = new byte[BOM_SIZE];
        final int n;
        int unread = 0;
        n = in.read(bom, 0, bom.length);

        for (final ByteOrderMark byteOrderMark : ByteOrderMark.ALL) {
            if (byteOrderMark.test(bom)) {
                this.charset = byteOrderMark.getCharsetName();
                unread = n - byteOrderMark.length();
                break;
            }
        }
        if (0 == unread) {
            // Unicode BOM mark not found, unread all bytes
            this.charset = this.defaultCharset;
            unread = n;
        }

        if (unread > 0) {
            in.unread(bom, (n - unread), unread);
        }

        this.initialized = true;
    }

}
