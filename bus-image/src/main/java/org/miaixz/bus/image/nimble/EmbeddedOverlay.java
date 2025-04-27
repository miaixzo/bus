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
package org.miaixz.bus.image.nimble;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.logger.Logger;

/**
 * Represents a pixel embedded overlay in DICOM attributes which is defined by the group offset and the bit position.
 * This type of overlay has been retired in DICOM standard, but it is still used in some old DICOM files.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public record EmbeddedOverlay(int groupOffset, int bitPosition) {

    /**
     * Returns a list of EmbeddedOverlay objects extracted from the given DICOM attributes.
     *
     * @param dcm the DICOM attributes containing the embedded overlays
     * @return a list of EmbeddedOverlay objects
     */
    public static List<EmbeddedOverlay> getEmbeddedOverlay(Attributes dcm) {
        List<EmbeddedOverlay> data = new ArrayList<>();
        int bitsAllocated = dcm.getInt(Tag.BitsAllocated, 8);
        int bitsStored = dcm.getInt(Tag.BitsStored, bitsAllocated);
        for (int i = 0; i < 16; i++) {
            int gg0000 = i << 17;
            if (dcm.getInt(Tag.OverlayBitsAllocated | gg0000, 1) != 1) {
                int bitPosition = dcm.getInt(Tag.OverlayBitPosition | gg0000, 0);
                if (bitPosition < bitsStored) {
                    Logger.info("Ignore embedded overlay #{} from bit #{} < bits stored: {}", (gg0000 >>> 17) + 1,
                            bitPosition, bitsStored);
                } else {
                    data.add(new EmbeddedOverlay(gg0000, bitPosition));
                }
            }
        }
        return data.isEmpty() ? Collections.emptyList() : data;
    }

}
