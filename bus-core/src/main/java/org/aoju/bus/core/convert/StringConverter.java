/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.convert;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.exception.ConvertException;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.XmlKit;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.TimeZone;

/**
 * 字符串转换器
 *
 * @author Kimi Liu
 * @version 6.2.9
 * @since JDK 1.8+
 */
public class StringConverter extends AbstractConverter<String> {

    /**
     * Clob字段值转字符串
     *
     * @param clob {@link Clob}
     * @return 字符串
     */
    private static String clobToString(Clob clob) {
        Reader reader = null;
        try {
            reader = clob.getCharacterStream();
            return IoKit.read(reader);
        } catch (SQLException e) {
            throw new ConvertException(e);
        } finally {
            IoKit.close(reader);
        }
    }

    /**
     * Blob字段值转字符串
     *
     * @param blob {@link Blob}
     * @return 字符串
     */
    private static String blobToString(Blob blob) {
        InputStream in = null;
        try {
            in = blob.getBinaryStream();
            return IoKit.read(in, Charset.UTF_8);
        } catch (SQLException e) {
            throw new ConvertException(e);
        } finally {
            IoKit.close(in);
        }
    }

    @Override
    protected String convertInternal(Object value) {
        if (value instanceof TimeZone) {
            return ((TimeZone) value).getID();
        } else if (value instanceof org.w3c.dom.Node) {
            return XmlKit.toString((org.w3c.dom.Node) value);
        } else if (value instanceof Clob) {
            return clobToString((Clob) value);
        } else if (value instanceof Blob) {
            return blobToString((Blob) value);
        }

        return convertString(value);
    }

}
