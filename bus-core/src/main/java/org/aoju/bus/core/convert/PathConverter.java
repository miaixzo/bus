/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.convert;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 字符串转换器
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class PathConverter extends AbstractConverter<Path> {

    @Override
    protected Path convertInternal(Object value) {
        try {
            if (value instanceof URI) {
                return Paths.get((URI) value);
            }

            if (value instanceof URL) {
                return Paths.get(((URL) value).toURI());
            }

            if (value instanceof File) {
                return ((File) value).toPath();
            }

            return Paths.get(convertToStr(value));
        } catch (Exception e) {
            // Ignore Exception
        }
        return null;
    }

}
