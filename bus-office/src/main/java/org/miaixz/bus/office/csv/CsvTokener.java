/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
