/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.io;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.miaixz.bus.core.io.stream.BOMInputStream;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * A Reader that handles streams with a Byte Order Mark (BOM). If the stream does not have a BOM or the encoding is
 * unrecognized, it defaults to UTF-8.
 *
 * <p>
 * BOM definitions:
 * <ul>
 * <li>00 00 FE FF = UTF-32, big-endian</li>
 * <li>FF FE 00 00 = UTF-32, little-endian</li>
 * <li>EF BB BF = UTF-8</li>
 * <li>FE FF = UTF-16, big-endian</li>
 * <li>FF FE = UTF-16, little-endian</li>
 * </ul>
 *
 * <p>
 * Usage example:
 * 
 * <pre>
 * 
 * FileInputStream fis = new FileInputStream(file);
 * BomReader uin = new BomReader(fis);
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BomReader extends ReaderWrapper {

    /**
     * Constructs a new {@code BomReader} with the given {@link InputStream}.
     *
     * @param in The input stream.
     */
    public BomReader(final InputStream in) {
        super(initReader(in));
    }

    /**
     * Initializes an {@link InputStreamReader} by converting the given {@link InputStream} to a {@link BOMInputStream}.
     *
     * @param in The {@link InputStream} to initialize.
     * @return An initialized {@link InputStreamReader}.
     * @throws InternalException if the encoding is not supported.
     */
    private static InputStreamReader initReader(final InputStream in) {
        Assert.notNull(in, "InputStream must be not null!");
        final BOMInputStream bin = (in instanceof BOMInputStream) ? (BOMInputStream) in : new BOMInputStream(in);
        try {
            return new InputStreamReader(bin, bin.getCharset());
        } catch (final UnsupportedEncodingException e) {
            throw new InternalException(e);
        }
    }

}
