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
package org.miaixz.bus.image.nimble.opencv.lut;

/**
 * Default immutable record implementation of the {@link WlPresentation} interface.
 * <p>
 * This record provides a concrete implementation for managing window/level presentation parameters in medical image
 * processing. It encapsulates:
 * <ul>
 * <li>Pixel padding behavior configuration
 * <li>DICOM Presentation State lookup table information
 * </ul>
 * <p>
 * As a record, this class is inherently immutable and thread-safe, making it suitable for concurrent image processing
 * operations.
 * <p>
 * <b>Usage Example:</b>
 *
 * <pre>{@code
 * var prLut = // ... obtain presentation state LUT
 * var presentation = new DefaultWlPresentation(prLut, true);
 *
 * if (presentation.isPixelPadding()) {
 *     // Apply pixel padding during transformation
 * }
 * }</pre>
 *
 * @param presentationState the DICOM presentation state LUT configuration, may be {@code null} if no presentation state
 *                          is available
 * @param pixelPadding      {@code true} to enable pixel padding during image processing, {@code false} to disable it
 * @see WlPresentation
 * @see PresentationStateLut
 * @author Kimi Liu
 * @since Java 21+
 */
public record DefaultWlPresentation(PresentationStateLut presentationState, boolean pixelPadding)
        implements WlPresentation {

    /**
     * Determines whether pixel padding.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean isPixelPadding() {
        return pixelPadding;
    }

    /**
     * Gets the presentation state.
     *
     * @return the presentation state.
     */
    @Override
    public PresentationStateLut getPresentationState() {
        return presentationState;
    }

}
