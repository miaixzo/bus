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

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class SampledColorModel extends ColorModel {

    private static final int[] BITS = { 8, 8, 8 };

    private final ColorSubsampling subsampling;

    public SampledColorModel(ColorSpace cspace, ColorSubsampling subsampling) {
        super(24, BITS, cspace, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        this.subsampling = subsampling;
    }

    @Override
    public boolean isCompatibleRaster(Raster raster) {
        return isCompatibleSampleModel(raster.getSampleModel());
    }

    @Override
    public boolean isCompatibleSampleModel(SampleModel sm) {
        return sm instanceof SampledSampleModel;
    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        return new SampledSampleModel(w, h, subsampling);
    }

    @Override
    public int getAlpha(int pixel) {
        return 255;
    }

    @Override
    public int getBlue(int pixel) {
        return pixel & 0xFF;
    }

    @Override
    public int getGreen(int pixel) {
        return pixel & 0xFF00;
    }

    @Override
    public int getRed(int pixel) {
        return pixel & 0xFF0000;
    }

    @Override
    public int getAlpha(Object inData) {
        return 255;
    }

    @Override
    public int getBlue(Object inData) {
        return getRGB(inData) & 0xFF;
    }

    @Override
    public int getGreen(Object inData) {
        return (getRGB(inData) >> 8) & 0xFF;
    }

    @Override
    public int getRed(Object inData) {
        return getRGB(inData) >> 16;
    }

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
