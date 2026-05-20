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
package org.miaixz.bus.image.nimble;

import java.util.Arrays;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * Represents the PixelAspectRatio type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PixelAspectRatio {

    /**
     * Constructs a new {@code PixelAspectRatio} instance.
     */
    public PixelAspectRatio() {
        // No initialization required.
    }

    /**
     * Executes the for image operation.
     *
     * @param attrs the attrs.
     * @return the operation result.
     */
    public static float forImage(Attributes attrs) {
        return forImage(
                attrs,
                Tag.PixelAspectRatio,
                Tag.PixelSpacing,
                Tag.ImagerPixelSpacing,
                Tag.NominalScannedPixelSpacing);
    }

    /**
     * Executes the for presentation state operation.
     *
     * @param attrs the attrs.
     * @return the operation result.
     */
    public static float forPresentationState(Attributes attrs) {
        return forImage(attrs, Tag.PresentationPixelAspectRatio, Tag.PresentationPixelSpacing);
    }

    /**
     * Executes the spacing for image operation.
     *
     * @param attrs the attrs.
     * @return the operation result.
     */
    public static double[] spacingForImage(Attributes attrs) {
        return pixelSpacing(attrs, Tag.PixelSpacing, Tag.ImagerPixelSpacing, Tag.NominalScannedPixelSpacing);
    }

    /**
     * Executes the spacing for presentation state operation.
     *
     * @param attrs the attrs.
     * @return the operation result.
     */
    public static double[] spacingForPresentationState(Attributes attrs) {
        return pixelSpacing(attrs, Tag.PresentationPixelSpacing);
    }

    /**
     * Executes the measurements operation.
     *
     * @param attrs       the attrs.
     * @param imageHeight the image height.
     * @return the operation result.
     */
    public static MeasurementsAdapter measurements(Attributes attrs, int imageHeight) {
        return measurements(attrs, 0, 0, false, imageHeight);
    }

    /**
     * Executes the measurements operation.
     *
     * @param attrs       the attrs.
     * @param offsetX     the offset x.
     * @param offsetY     the offset y.
     * @param upYAxis     the up y axis.
     * @param imageHeight the image height.
     * @return the operation result.
     */
    public static MeasurementsAdapter measurements(
            Attributes attrs,
            int offsetX,
            int offsetY,
            boolean upYAxis,
            int imageHeight) {
        double[] spacing = spacingForImage(attrs);
        double calibrationRatio = spacing == null ? 1.0 : spacing[1];
        Unit unit = spacing == null ? Unit.PIXEL : Unit.MILLIMETER;
        return new MeasurementsAdapter(calibrationRatio, offsetX, offsetY, upYAxis, imageHeight, unit);
    }

    /**
     * Executes the for image operation.
     *
     * @param attrs            the attrs.
     * @param aspectRatioTag   the aspect ratio tag.
     * @param pixelSpacingTags the pixel spacing tags.
     * @return the operation result.
     */
    private static float forImage(Attributes attrs, int aspectRatioTag, int... pixelSpacingTags) {
        int[] ratio = attrs.getInts(aspectRatioTag);
        if (ratio != null && ratio.length == 2 && ratio[0] > 0 && ratio[1] > 0)
            return (float) ratio[0] / ratio[1];

        for (int pixelSpacingTag : pixelSpacingTags) {
            float[] spaces = attrs.getFloats(pixelSpacingTag);
            if (spaces != null && spaces.length == 2 && spaces[0] > 0 && spaces[1] > 0)
                return spaces[0] / spaces[1];
        }
        return 1f;
    }

    /**
     * Executes the pixel spacing operation.
     *
     * @param attrs            the attrs.
     * @param pixelSpacingTags the pixel spacing tags.
     * @return the operation result.
     */
    private static double[] pixelSpacing(Attributes attrs, int... pixelSpacingTags) {
        if (attrs == null) {
            return null;
        }
        for (int pixelSpacingTag : pixelSpacingTags) {
            double[] spaces = attrs.getDoubles(pixelSpacingTag);
            if (spaces != null && spaces.length == 2 && spaces[0] > 0 && spaces[1] > 0) {
                return Arrays.copyOf(spaces, spaces.length);
            }
        }
        return null;
    }

}
