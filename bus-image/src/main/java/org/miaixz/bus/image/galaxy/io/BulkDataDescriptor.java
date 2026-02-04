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
