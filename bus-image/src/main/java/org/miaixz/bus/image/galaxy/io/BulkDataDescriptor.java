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
package org.miaixz.bus.image.galaxy.io;

import java.util.List;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.ItemPointer;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * Interface for describing which DICOM attributes should be treated as Bulk Data. Implementations of this interface
 * define the criteria for identifying bulk data elements, which are typically large data items that might be stored
 * externally or handled specially.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface BulkDataDescriptor {

    /**
     * A default {@code BulkDataDescriptor} that identifies standard DICOM bulk data elements. It uses
     * {@link BasicBulkDataDescriptor#isStandardBulkData} to determine if an attribute is bulk data.
     */
    BulkDataDescriptor DEFAULT = (itemPointer, privateCreator, tag, vr, length) -> BasicBulkDataDescriptor
            .isStandardBulkData(itemPointer, tag);

    /**
     * A {@code BulkDataDescriptor} that specifically identifies the Pixel Data (7FE0,0010) attribute as bulk data.
     */
    BulkDataDescriptor PIXELDATA = (itemPointer, privateCreator, tag, vr, length) -> tag == Tag.PixelData;

    /**
     * Determines if a given DICOM attribute should be treated as bulk data.
     * 
     * @param itemPointer    A list of {@link ItemPointer} objects indicating the path to the attribute within nested
     *                       sequences.
     * @param privateCreator The private creator of the attribute, or {@code null} if it's a standard attribute.
     * @param tag            The DICOM tag of the attribute.
     * @param vr             The Value Representation (VR) of the attribute.
     * @param length         The value length of the attribute.
     * @return {@code true} if the attribute is considered bulk data, {@code false} otherwise.
     */
    boolean isBulkData(List<ItemPointer> itemPointer, String privateCreator, int tag, VR vr, int length);

}
