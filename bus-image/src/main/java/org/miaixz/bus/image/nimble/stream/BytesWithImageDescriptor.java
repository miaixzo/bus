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
package org.miaixz.bus.image.nimble.stream;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * Defines the BytesWithImageDescriptor contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface BytesWithImageDescriptor extends ImageReaderDescriptor {

    /**
     * Gets the bytes.
     *
     * @param frame the frame.
     * @return the bytes.
     * @throws IOException if the operation cannot be completed.
     */
    ByteBuffer getBytes(int frame) throws IOException;

    /**
     * Gets the transfer syntax.
     *
     * @return the transfer syntax.
     */
    String getTransferSyntax();

    /**
     * Executes the big endian operation.
     *
     * @return true if the condition is met; otherwise false.
     */
    default boolean bigEndian() {
        return false;
    }

    /**
     * Executes the float pixel data operation.
     *
     * @return true if the condition is met; otherwise false.
     */
    default boolean floatPixelData() {
        return false;
    }

    /**
     * Gets the pixel data vr.
     *
     * @return the pixel data vr.
     */
    VR getPixelDataVR();

    /**
     * Gets the palette color lookup table.
     *
     * @return the palette color lookup table.
     */
    Attributes getPaletteColorLookupTable();

}
