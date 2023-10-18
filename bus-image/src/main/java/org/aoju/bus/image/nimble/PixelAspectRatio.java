/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.nimble;

import org.aoju.bus.image.Tag;
import org.aoju.bus.image.galaxy.data.Attributes;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PixelAspectRatio {

    public static float forImage(Attributes attrs) {
        return forImage(attrs, Tag.PixelAspectRatio,
                Tag.PixelSpacing,
                Tag.ImagerPixelSpacing,
                Tag.NominalScannedPixelSpacing);
    }

    public static float forPresentationState(Attributes attrs) {
        return forImage(attrs, Tag.PresentationPixelAspectRatio,
                Tag.PresentationPixelSpacing);
    }

    private static float forImage(Attributes attrs, int aspectRatioTag,
                                  int... pixelSpacingTags) {
        int[] ratio = attrs.getInts(aspectRatioTag);
        if (null != ratio && ratio.length == 2
                && ratio[0] > 0 && ratio[1] > 0)
            return (float) ratio[0] / ratio[1];

        for (int pixelSpacingTag : pixelSpacingTags) {
            float[] spaces = attrs.getFloats(pixelSpacingTag);
            if (null != spaces && spaces.length == 2
                    && spaces[0] > 0 && spaces[1] > 0)
                return spaces[0] / spaces[1];
        }
        return 1f;
    }

}
