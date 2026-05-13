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

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;

/**
 * Represents the SampledColorModel type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SampledColorModel extends ColorModel {

    /**
     * The bits value.
     */
    private static final int[] BITS = { 8, 8, 8 };

    /**
     * The subsampling value.
     */
    private final ColorSubsampling subsampling;

    /**
     * Creates a new instance.
     *
     * @param cspace      the cspace.
     * @param subsampling the subsampling.
     */
    public SampledColorModel(ColorSpace cspace, ColorSubsampling subsampling) {
        super(24, BITS, cspace, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        this.subsampling = subsampling;
    }

    /**
     * Determines whether compatible raster.
     *
     * @param raster the raster.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean isCompatibleRaster(Raster raster) {
        return isCompatibleSampleModel(raster.getSampleModel());
    }

    /**
     * Determines whether compatible sample model.
     *
     * @param sm the sm.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean isCompatibleSampleModel(SampleModel sm) {
        return sm instanceof SampledSampleModel;
    }

    /**
     * Creates the compatible sample model.
     *
     * @param w the w.
     * @param h the h.
     * @return the operation result.
     */
    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        return new SampledSampleModel(w, h, subsampling);
    }

    /**
     * Gets the alpha.
     *
     * @param pixel the pixel.
     * @return the alpha.
     */
    @Override
    public int getAlpha(int pixel) {
        return 255;
    }

    /**
     * Gets the blue.
     *
     * @param pixel the pixel.
     * @return the blue.
     */
    @Override
    public int getBlue(int pixel) {
        return pixel & 0xFF;
    }

    /**
     * Gets the green.
     *
     * @param pixel the pixel.
     * @return the green.
     */
    @Override
    public int getGreen(int pixel) {
        return pixel & 0xFF00;
    }

    /**
     * Gets the red.
     *
     * @param pixel the pixel.
     * @return the red.
     */
    @Override
    public int getRed(int pixel) {
        return pixel & 0xFF0000;
    }

    /**
     * Gets the alpha.
     *
     * @param inData the in data.
     * @return the alpha.
     */
    @Override
    public int getAlpha(Object inData) {
        return 255;
    }

    /**
     * Gets the blue.
     *
     * @param inData the in data.
     * @return the blue.
     */
    @Override
    public int getBlue(Object inData) {
        return getRGB(inData) & 0xFF;
    }

    /**
     * Gets the green.
     *
     * @param inData the in data.
     * @return the green.
     */
    @Override
    public int getGreen(Object inData) {
        return (getRGB(inData) >> 8) & 0xFF;
    }

    /**
     * Gets the red.
     *
     * @param inData the in data.
     * @return the red.
     */
    @Override
    public int getRed(Object inData) {
        return getRGB(inData) >> 16;
    }

    /**
     * Gets the rgb.
     *
     * @param inData the in data.
     * @return the rgb.
     */
    @Override
    public int getRGB(Object inData) {
        byte[] ba = (byte[]) inData;
        ColorSpace cs = getColorSpace();
        float[] fba = new float[] { (ba[0] & 0xFF) / 255f, (ba[1] & 0xFF) / 255f, (ba[2] & 0xFF) / 255f };
        float[] rgb = cs.toRGB(fba);
        int ret = (((int) (rgb[0] * 255)) << 16) | (((int) (rgb[1] * 255)) << 8) | (((int) (rgb[2] * 255)));
        return ret;
    }

}
