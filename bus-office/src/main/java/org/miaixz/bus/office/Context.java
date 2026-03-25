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
package org.miaixz.bus.office;

import org.apache.poi.openxml4j.util.ZipSecureFile;

/**
 * Base interface for all office context interfaces.
 *
 * @author Kimi Liu
 * @since Java 21+
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
