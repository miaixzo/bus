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
package org.miaixz.bus.office.csv;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.wrapper.SimpleWrapper;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * CSV parser, used for parsing CSV files.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CsvTokener extends SimpleWrapper<Reader> implements Closeable {

    /**
     * The position in the Reader (index of the character parsed to).
     */
    private long index;
    /**
     * The previous character.
     */
    private int prev;
    /**
     * Whether to use the previous character.
     */
    private boolean usePrev;

    /**
     * Constructor.
     *
     * @param reader The {@link Reader}.
     */
    public CsvTokener(final Reader reader) {
        super(IoKit.toBuffered(reader));
    }

    /**
     * Reads the next character and records the position.
     *
     * @return The next character.
     */
    public int next() {
        if (this.usePrev) {
            this.usePrev = false;
        } else {
            try {
                this.prev = this.raw.read();
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }
        this.index++;
        return this.prev;
    }

    /**
     * Moves the mark back one character.
     *
     * @throws IllegalStateException Throws this exception if back() is called multiple times.
     */
    public void back() throws IllegalStateException {
        if (this.usePrev || this.index <= 0) {
            throw new IllegalStateException("Stepping back two steps is not supported");
        }
        this.index--;
        this.usePrev = true;
    }

    /**
     * Gets the current position.
     *
     * @return The position.
     */
    public long getIndex() {
        return this.index;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        IoKit.nullSafeClose(this.raw);
    }

}
