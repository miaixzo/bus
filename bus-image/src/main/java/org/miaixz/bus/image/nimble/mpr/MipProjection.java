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
package org.miaixz.bus.image.nimble.mpr;

import java.util.List;
import java.util.Objects;

import org.miaixz.bus.image.nimble.opencv.ImageCV;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;

/**
 * Maximum, minimum and mean intensity projections for image stacks.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum MipProjection {

    /**
     * Constant for the none value.
     */
    NONE,
    /**
     * Constant for the min value.
     */
    MIN,
    /**
     * Constant for the mean value.
     */
    MEAN,
    /**
     * Constant for the max value.
     */
    MAX;

    /**
     * Executes the apply operation.
     *
     * @param sources the sources.
     * @return the operation result.
     */
    public PlanarImage apply(List<PlanarImage> sources) {
        return switch (this) {
            case NONE -> null;
            case MIN -> ImageCV.minStack(validateSources(sources));
            case MEAN -> ImageCV.meanStack(validateSources(sources));
            case MAX -> ImageCV.maxStack(validateSources(sources));
        };
    }

    /**
     * Executes the apply operation.
     *
     * @param projection the projection.
     * @param sources    the sources.
     * @return the operation result.
     */
    public static PlanarImage apply(MipProjection projection, List<PlanarImage> sources) {
        return Objects.requireNonNull(projection, "projection").apply(sources);
    }

    /**
     * Validates the sources.
     *
     * @param sources the sources.
     * @return the operation result.
     */
    private static List<PlanarImage> validateSources(List<PlanarImage> sources) {
        if (Objects.requireNonNull(sources, "sources").isEmpty()) {
            throw new IllegalArgumentException("sources cannot be empty");
        }
        return sources;
    }

}
