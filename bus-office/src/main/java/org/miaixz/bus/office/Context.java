/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.office;

import org.apache.poi.openxml4j.util.ZipSecureFile;

/**
 * Base interface for all office context interfaces.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Context {

    /**
     * Sets the minimum inflation ratio during decompression. To avoid `Zip Bomb` attacks, POI sets a minimum
     * compression ratio, which is:
     * 
     * <pre>
     * compressed size / uncompressed size
     * </pre>
     * 
     * The default value in POI is 0.01 (i.e., minimum compression to 1%). If the compression ratio of a file in the
     * document is less than this value, an error will be reported. If there are indeed files with high compression
     * ratios in the document, this global method can be used to customize the ratio to avoid errors.
     *
     * @param ratio The minimum ratio of the uncompressed file size to the original file size. A value less than or
     *              equal to 0 disables the check.
     */
    public static void setMinInflateRatio(final double ratio) {
        ZipSecureFile.setMinInflateRatio(ratio);
    }

    /**
     * Sets the maximum file size for a single entry in a Zip file. The default is 4GB, which is the maximum for 32-bit
     * zip format.
     *
     * @param maxEntrySize The maximum file size for a single Zip entry. Must be greater than 0.
     */
    public static void setMaxEntrySize(final long maxEntrySize) {
        ZipSecureFile.setMaxEntrySize(maxEntrySize);
    }

    /**
     * Sets the maximum number of characters for text before decompression. An exception is thrown if this limit is
     * exceeded.
     *
     * @param maxTextSize The maximum number of characters for text.
     * @throws IllegalArgumentException if {@code maxTextSize} is negative.
     */
    public static void setMaxTextSize(final long maxTextSize) {
        ZipSecureFile.setMaxTextSize(maxTextSize);
    }

}
