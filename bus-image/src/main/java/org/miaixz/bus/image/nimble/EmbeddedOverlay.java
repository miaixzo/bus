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
                    Logger.info(
                            "Ignore embedded overlay #{} from bit #{} < bits stored: {}",
                            (gg0000 >>> 17) + 1,
                            bitPosition,
                            bitsStored);
                } else {
                    data.add(new EmbeddedOverlay(gg0000, bitPosition));
                }
            }
        }
        return data.isEmpty() ? Collections.emptyList() : data;
    }

}
