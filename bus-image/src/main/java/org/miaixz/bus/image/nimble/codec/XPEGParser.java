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
package org.miaixz.bus.image.nimble.codec;

import java.io.IOException;

import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.nimble.codec.mp4.MP4FileType;

/**
 * Defines the XPEGParser contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface XPEGParser {

    /**
     * Gets the code stream position.
     *
     * @return the code stream position.
     */
    long getCodeStreamPosition();

    /**
     * Gets the position after app segments.
     *
     * @return the position after app segments.
     */
    long getPositionAfterAPPSegments();

    /**
     * Gets the mp4 file type.
     *
     * @return the mp4 file type.
     */
    MP4FileType getMP4FileType();

    /**
     * Gets the attributes.
     *
     * @param attrs the attrs.
     * @return the attributes.
     */
    Attributes getAttributes(Attributes attrs);

    /**
     * Gets the transfer syntax uid.
     *
     * @param fragmented the fragmented.
     * @return the transfer syntax uid.
     * @throws IOException if the operation cannot be completed.
     */
    String getTransferSyntaxUID(boolean fragmented) throws IOException;

    /**
     * Gets the transfer syntax uid.
     *
     * @return the transfer syntax uid.
     * @throws IOException if the operation cannot be completed.
     */
    default String getTransferSyntaxUID() throws IOException {
        return getTransferSyntaxUID(false);
    }

}
