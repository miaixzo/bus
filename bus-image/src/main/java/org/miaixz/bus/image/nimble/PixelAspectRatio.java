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

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PixelAspectRatio {

    public static float forImage(Attributes attrs) {
        return forImage(
                attrs,
                Tag.PixelAspectRatio,
                Tag.PixelSpacing,
                Tag.ImagerPixelSpacing,
                Tag.NominalScannedPixelSpacing);
    }

    public static float forPresentationState(Attributes attrs) {
        return forImage(attrs, Tag.PresentationPixelAspectRatio, Tag.PresentationPixelSpacing);
    }

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

}
