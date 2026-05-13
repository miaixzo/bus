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
 * Interface representing window/level presentation parameters for medical image processing.
 * <p>
 * This interface defines the core presentation state for image display, including pixel padding behavior and
 * presentation state LUT information used in DICOM image transformations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface WlPresentation {

    /**
     * Determines whether pixel padding should be applied during image processing.
     *
     * @return {@code true} if pixel padding is enabled, {@code false} otherwise
     */
    boolean isPixelPadding();

    /**
     * Retrieves the presentation state lookup table configuration.
     *
     * @return the presentation state LUT, or {@code null} if not available
     */
    PresentationStateLut getPresentationState();

}
