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

import java.util.Optional;

import org.miaixz.bus.image.nimble.opencv.LookupTableCV;

/**
 * Interface representing DICOM Presentation State lookup table configuration.
 * <p>
 * This interface encapsulates the presentation state parameters defined in DICOM Presentation State (PR) objects, which
 * control how medical images are displayed through lookup table transformations.
 * <p>
 * The presentation state LUT defines:
 * <ul>
 * <li>Custom lookup table data for pixel value transformation
 * <li>Human-readable explanation of the transformation purpose
 * <li>Shape mode indicating the mathematical function used
 * </ul>
 *
 * @see LookupTableCV
 * @see LutShape
 * @author Kimi Liu
 * @since Java 21+
 */
public interface PresentationStateLut {

    /**
     * Retrieves the presentation state lookup table for pixel value transformation.
     * <p>
     * This lookup table, when present, defines a custom mapping from input pixel values to output display values. It is
     * typically used for specialized medical image visualization requirements defined in DICOM Presentation State
     * objects.
     *
     * @return an {@link Optional} containing the lookup table if available, or {@link Optional#empty()} if no custom
     *         LUT is defined
     */
    Optional<LookupTableCV> getPrLut();

    /**
     * Retrieves the human-readable explanation of the presentation state LUT purpose.
     * <p>
     * This explanation provides context about why this particular lookup table transformation is being applied, such as
     * "Chest X-Ray Enhancement" or "Bone Window Display".
     *
     * @return an {@link Optional} containing the LUT explanation if available, or {@link Optional#empty()} if no
     *         explanation is provided
     */
    Optional<String> getPrLutExplanation();

    /**
     * Retrieves the shape mode identifier for the presentation state LUT.
     * <p>
     * The shape mode indicates the mathematical function or transformation type applied by this LUT (e.g., "LINEAR",
     * "SIGMOID"). This corresponds to DICOM tag (2050,0020) LUT Function.
     *
     * @return an {@link Optional} containing the LUT shape mode if available, or {@link Optional#empty()} if no shape
     *         mode is specified
     */
    Optional<String> getPrLutShapeMode();

}
