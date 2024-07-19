/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.image.nimble;

import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.image.nimble.opencv.ImageCV;
import org.miaixz.bus.image.nimble.opencv.ImageProcessor;
import org.miaixz.bus.image.nimble.opencv.LookupTableCV;
import org.miaixz.bus.image.nimble.opencv.PlanarImage;
import org.miaixz.bus.image.nimble.opencv.lut.PresentationStateLut;
import org.miaixz.bus.image.nimble.opencv.lut.WindLevelParameters;
import org.miaixz.bus.image.nimble.stream.ImageDescriptor;
import org.opencv.core.CvType;

import java.awt.image.DataBuffer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageRendering {

    public static PlanarImage getRawRenderedImage(
            final PlanarImage imageSource, ImageDescriptor desc, ImageReadParam params) {
        PlanarImage img = getImageWithoutEmbeddedOverlay(imageSource, desc);
        ImageAdapter adapter = new ImageAdapter(img, desc);
        return getModalityLutImage(imageSource, adapter, params);
    }

    public static PlanarImage getModalityLutImage(
            PlanarImage img, ImageAdapter adapter, ImageReadParam params) {
        WindLevelParameters p = new WindLevelParameters(adapter, params);
        int datatype = Objects.requireNonNull(img).type();

        if (datatype >= CvType.CV_8U && datatype < CvType.CV_32S) {
            LookupTableCV modalityLookup = adapter.getModalityLookup(p, p.isInverseLut());
            return modalityLookup == null ? img.toImageCV() : modalityLookup.lookup(img.toMat());
        }
        return img;
    }

    public static PlanarImage getDefaultRenderedImage(
            final PlanarImage imageSource,
            ImageDescriptor desc,
            ImageReadParam params,
            int frameIndex) {
        PlanarImage img = getImageWithoutEmbeddedOverlay(imageSource, desc);
        img = getVoiLutImage(img, desc, params);
        return OverlayData.getOverlayImage(imageSource, img, desc, params, frameIndex);
    }

    public static PlanarImage getVoiLutImage(
            final PlanarImage imageSource, ImageDescriptor desc, ImageReadParam params) {
        ImageAdapter adapter = new ImageAdapter(imageSource, desc);
        return getVoiLutImage(imageSource, adapter, params);
    }

    public static PlanarImage getVoiLutImage(
            PlanarImage imageSource, ImageAdapter adapter, ImageReadParam params) {
        WindLevelParameters p = new WindLevelParameters(adapter, params);
        int datatype = Objects.requireNonNull(imageSource).type();

        if (datatype >= CvType.CV_8U && datatype < CvType.CV_32S) {
            return getImageForByteOrShortData(imageSource, adapter, p);
        } else if (datatype >= CvType.CV_32S) {
            return getImageWithFloatOrIntData(imageSource, p, datatype);
        }
        return null;
    }

    private static ImageCV getImageForByteOrShortData(
            PlanarImage imageSource, ImageAdapter adapter, WindLevelParameters p) {
        ImageDescriptor desc = adapter.getImageDescriptor();
        LookupTableCV modalityLookup = adapter.getModalityLookup(p, p.isInverseLut());
        ImageCV imageModalityTransformed =
                modalityLookup == null
                        ? imageSource.toImageCV()
                        : modalityLookup.lookup(imageSource.toMat());

        /**
         * C.11.2.1.2 Window center and window width
         *
         * Theses Attributes shall be used only for Images with Photometric Interpretation (0028,0004) values of
         * MONOCHROME1 and MONOCHROME2. They have no meaning for other Images.
         */
        if ((!p.isAllowWinLevelOnColorImage()
                || MathKit.isEqual(p.getWindow(), 255.0) && MathKit.isEqual(p.getLevel(), 127.5))
                && !desc.getPhotometricInterpretation().isMonochrome()) {
            /*
             * If photometric interpretation is not monochrome do not apply VOILUT. It is necessary for
             * PALETTE_COLOR.
             */
            return imageModalityTransformed;
        }

        PresentationStateLut prDcm = p.getPresentationState();
        Optional<LookupTableCV> prLut = prDcm == null ? Optional.empty() : prDcm.getPrLut();
        LookupTableCV voiLookup = null;
        if (prLut.isEmpty() || p.getLutShape().getLookup() != null) {
            voiLookup = adapter.getVOILookup(p);
        }
        if (prLut.isEmpty()) {
            return voiLookup.lookup(imageModalityTransformed);
        }

        ImageCV imageVoiTransformed =
                voiLookup == null ? imageModalityTransformed : voiLookup.lookup(imageModalityTransformed);
        return prLut.get().lookup(imageVoiTransformed);
    }

    private static ImageCV getImageWithFloatOrIntData(
            PlanarImage imageSource, WindLevelParameters p, int datatype) {
        double low = p.getLevel() - p.getWindow() / 2.0;
        double high = p.getLevel() + p.getWindow() / 2.0;
        double range = high - low;
        if (range < 1.0 && datatype == DataBuffer.TYPE_INT) {
            range = 1.0;
        }
        double slope = 255.0 / range;
        double yint = 255.0 - slope * high;

        return ImageProcessor.rescaleToByte(ImageCV.toMat(imageSource), slope, yint);
    }

    /**
     * For overlays encoded in Overlay Data Element (60xx,3000), Overlay Bits Allocated (60xx,0100) is
     * always 1 and Overlay Bit Position (60xx,0102) is always 0.
     *
     * @param img the image source
     * @return the bit mask for removing the pixel overlay
     * @see <a
     * href="http://dicom.nema.org/medical/dicom/current/output/chtml/part05/chapter_8.html">8.1.2
     * Overlay data encoding of related data elements</a>
     */
    public static PlanarImage getImageWithoutEmbeddedOverlay(PlanarImage img, ImageDescriptor desc) {
        Objects.requireNonNull(img);
        List<EmbeddedOverlay> embeddedOverlays = Objects.requireNonNull(desc).getEmbeddedOverlay();
        if (!embeddedOverlays.isEmpty()) {
            int bitsStored = desc.getBitsStored();
            int bitsAllocated = desc.getBitsAllocated();
            if (bitsStored < desc.getBitsAllocated() && bitsAllocated >= 8 && bitsAllocated <= 16) {
                int highBit = desc.getHighBit();
                int high = highBit + 1;
                int val = (1 << high) - 1;
                if (high > bitsStored) {
                    val -= (1 << (high - bitsStored)) - 1;
                }
                // Set to 0 all bits upper than highBit and if lower than high-bitsStored (=> all bits
                // outside bitStored)
                if (high > bitsStored) {
                    desc.getModalityLUT().adaptWithOverlayBitMask(high - bitsStored);
                }

                // Set to 0 all bits outside bitStored
                return ImageProcessor.bitwiseAnd(img.toMat(), val);
            }
        }
        return img;
    }

}
