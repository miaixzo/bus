/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.Sequence;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public final class ICCProfile {

    public static boolean isPresentIn(Attributes attrs) {
        return attrs.containsValue(Tag.ICCProfile) || attrs.containsValue(Tag.OpticalPathSequence);
    }

    public static ColorSpaceFactory colorSpaceFactoryOf(Attributes attrs) {
        byte[] b = attrs.getSafeBytes(Tag.ICCProfile);
        if (b == null) {
            Sequence opticalPathSequence = attrs.getSequence(Tag.OpticalPathSequence);
            if (opticalPathSequence != null && !opticalPathSequence.isEmpty()) {
                if (opticalPathSequence.size() > 1) {
                    return frameIndex -> getColorSpace(attrs, opticalPathSequence, frameIndex);
                }
                b = opticalPathSequence.get(0).getSafeBytes(Tag.ICCProfile);
            }
        }
        if (b == null) {
            return frameIndex -> Optional.empty();
        }
        Optional<ColorSpace> cs = Optional.of(new ICC_ColorSpace(ICC_Profile.getInstance(b)));
        return frameIndex -> cs;
    }

    private static Optional<ColorSpace> getColorSpace(Attributes attrs, Sequence opticalPathSequence, int frameIndex) {
        Attributes functionGroup = attrs.getFunctionGroup(Tag.OpticalPathIdentificationSequence, frameIndex);
        if (functionGroup != null) {
            String opticalPathID = functionGroup.getString(Tag.OpticalPathIdentifier);
            if (opticalPathID != null) {
                Optional<Attributes> match = opticalPathSequence.stream()
                        .filter(item -> opticalPathID.equals(item.getString(Tag.OpticalPathIdentifier))).findFirst();
                if (match.isPresent()) {
                    byte[] b = match.get().getSafeBytes(Tag.ICCProfile);
                    if (b != null)
                        return Optional.of(new ICC_ColorSpace(ICC_Profile.getInstance(b)));
                }
            }
        }
        return Optional.empty();
    }

    public enum Option {
        none {
            @Override
            protected BufferedImage convertColor(BufferedImage bi) {
                return isCS_sRGB(bi) ? bi : BufferedImages.convertColor(bi, CM_sRGB);
            }
        },
        no {
            @Override
            protected BufferedImage convertColor(BufferedImage bi) {
                return isCS_sRGB(bi) ? bi : BufferedImages.replaceColorModel(bi, CM_sRGB);
            }
        },
        yes {
            @Override
            protected BufferedImage convertColor(BufferedImage bi) {
                return isCS_sRGB(bi) ? BufferedImages.replaceColorModel(bi, srgb.colorModel) : bi;
            }
        },
        srgb("sRGB.icc") {
            @Override
            protected BufferedImage convertColor(BufferedImage bi) {
                return isCS_sRGB(bi) ? BufferedImages.replaceColorModel(bi, srgb.colorModel)
                        : BufferedImages.convertColor(bi, srgb.colorModel);
            }
        },
        adobergb("adobeRGB.icc"), rommrgb("rommRGB.icc");

        private static final ColorModel CM_sRGB = ColorModelFactory.createRGBColorModel(8, DataBuffer.TYPE_BYTE,
                ColorSpace.getInstance(ColorSpace.CS_sRGB));
        private final ColorModel colorModel;

        Option() {
            colorModel = null;
        }

        Option(String fileName) {
            try (InputStream is = ICCProfile.class.getResourceAsStream(fileName)) {
                colorModel = ColorModelFactory.createRGBColorModel(8, DataBuffer.TYPE_BYTE,
                        new ICC_ColorSpace(ICC_Profile.getInstance(is)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static boolean isCS_sRGB(BufferedImage bi) {
            return bi.getColorModel().getColorSpace().isCS_sRGB();
        }

        private static BufferedImage toRGB(BufferedImage bi, ColorModel cm) {
            return cm instanceof PaletteColorModel ? BufferedImages.convertPalettetoRGB(bi, null)
                    : cm.getColorSpace().getType() == ColorSpace.TYPE_YCbCr ? BufferedImages.convertYBRtoRGB(bi, null)
                            : bi;
        }

        public BufferedImage adjust(BufferedImage bi) {
            ColorModel cm = bi.getColorModel();
            return cm.getNumColorComponents() == 3 ? convertColor(toRGB(bi, cm)) : bi;
        }

        protected BufferedImage convertColor(BufferedImage bi) {
            return BufferedImages.convertColor(bi, colorModel);
        }
    }

    @FunctionalInterface
    public interface ColorSpaceFactory {
        Optional<ColorSpace> getColorSpace(int frameIndex);
    }

}
