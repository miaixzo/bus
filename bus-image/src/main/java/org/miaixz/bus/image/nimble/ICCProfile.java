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
 * Represents the ICCProfile type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ICCProfile {

    /**
     * Constructs a new ICCProfile instance.
     */
    public ICCProfile() {
        // No initialization required.
    }

    /**
     * Determines whether present in.
     *
     * @param attrs the attrs.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean isPresentIn(Attributes attrs) {
        return attrs.containsValue(Tag.ICCProfile) || attrs.containsValue(Tag.OpticalPathSequence);
    }

    /**
     * Executes the color space factory of operation.
     *
     * @param attrs the attrs.
     * @return the operation result.
     */
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

    /**
     * Gets the color space.
     *
     * @param attrs               the attrs.
     * @param opticalPathSequence the optical path sequence.
     * @param frameIndex          the frame index.
     * @return the color space.
     */
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

    /**
     * Defines the Option values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Option {

        /**
         * The none value.
         */
        none {

            @Override
            protected BufferedImage convertColor(BufferedImage bi) {
                return isCS_sRGB(bi) ? bi : BufferedImages.convertColor(bi, CM_sRGB);
            }
        },
        /**
         * The no value.
         */
        no {

            @Override
            protected BufferedImage convertColor(BufferedImage bi) {
                return isCS_sRGB(bi) ? bi : BufferedImages.replaceColorModel(bi, CM_sRGB);
            }
        },
        /**
         * The yes value.
         */
        yes {

            @Override
            protected BufferedImage convertColor(BufferedImage bi) {
                ColorModel model = srgb.getColorModel() == null ? CM_sRGB : srgb.getColorModel();
                return isCS_sRGB(bi) ? BufferedImages.replaceColorModel(bi, model) : bi;
            }
        },
        /**
         * Constant for the srgb value.
         */
        srgb("sRGB.icc"),
        /**
         * Constant for the adobergb value.
         */
        adobergb("adobeRGB.icc"),
        /**
         * Constant for the rommrgb value.
         */
        rommrgb("rommRGB.icc");

        /**
         * The cm s rgb value.
         */
        private static final ColorModel CM_sRGB = ColorModelFactory
                .createRGBColorModel(8, DataBuffer.TYPE_BYTE, ColorSpace.getInstance(ColorSpace.CS_sRGB));

        /**
         * The color model value.
         */
        private final ColorModel colorModel;

        /**
         * Creates a new instance.
         */
        Option() {
            colorModel = null;
        }

        /**
         * Creates a new instance.
         *
         * @param fileName the file name.
         */
        Option(String fileName) {
            ColorModel model = null;
            try (InputStream is = ICCProfile.class.getResourceAsStream(fileName)) {
                if (is != null) {
                    model = ColorModelFactory.createRGBColorModel(
                            8,
                            DataBuffer.TYPE_BYTE,
                            new ICC_ColorSpace(ICC_Profile.getInstance(is)));
                }
            } catch (IOException | RuntimeException e) {
                model = null;
            }
            colorModel = model;
        }

        /**
         * Determines whether cs s rgb.
         *
         * @param bi the bi.
         * @return true if the condition is met; otherwise false.
         */
        private static boolean isCS_sRGB(BufferedImage bi) {
            return bi.getColorModel().getColorSpace().isCS_sRGB();
        }

        /**
         * Converts this value to rgb.
         *
         * @param bi the bi.
         * @param cm the cm.
         * @return the operation result.
         */
        private static BufferedImage toRGB(BufferedImage bi, ColorModel cm) {
            return cm instanceof PaletteColorModel ? BufferedImages.convertPalettetoRGB(bi, null)
                    : cm.getColorSpace().getType() == ColorSpace.TYPE_YCbCr ? BufferedImages.convertYBRtoRGB(bi, null)
                            : bi;
        }

        /**
         * Gets the color model.
         *
         * @return the color model.
         */
        private ColorModel getColorModel() {
            return colorModel;
        }

        /**
         * Executes the adjust operation.
         *
         * @param bi the bi.
         * @return the operation result.
         */
        public BufferedImage adjust(BufferedImage bi) {
            ColorModel cm = bi.getColorModel();
            return cm.getNumColorComponents() == 3 ? convertColor(toRGB(bi, cm)) : bi;
        }

        /**
         * Executes the convert color operation.
         *
         * @param bi the bi.
         * @return the operation result.
         */
        protected BufferedImage convertColor(BufferedImage bi) {
            return colorModel == null ? none.convertColor(bi) : BufferedImages.convertColor(bi, colorModel);
        }

    }

    /**
     * Defines the ColorSpaceFactory contract.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FunctionalInterface
    public interface ColorSpaceFactory {

        /**
         * Gets the color space.
         *
         * @param frameIndex the frame index.
         * @return the color space.
         */
        Optional<ColorSpace> getColorSpace(int frameIndex);

    }

}
