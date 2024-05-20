/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.convert.stringer;

import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.toolkit.IoKit;

import java.io.Reader;
import java.sql.Clob;
import java.util.function.Function;

/**
 * Clob转String
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClobStringer implements Function<Object, String> {

    /**
     * 单例
     */
    public static ClobStringer INSTANCE = new ClobStringer();

    /**
     * Clob字段值转字符串
     *
     * @param clob {@link java.sql.Clob}
     * @return 字符串
     */
    private static String toString(final Clob clob) {
        Reader reader = null;
        try {
            reader = clob.getCharacterStream();
            return IoKit.read(reader);
        } catch (final java.sql.SQLException e) {
            throw new ConvertException(e);
        } finally {
            IoKit.closeQuietly(reader);
        }
    }

    @Override
    public String apply(final Object o) {
        return toString((Clob) o);
    }

}
