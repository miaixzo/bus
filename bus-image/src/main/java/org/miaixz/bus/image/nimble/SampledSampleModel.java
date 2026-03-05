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

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.SampleModel;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class SampledSampleModel extends SampleModel {

    private final ColorSubsampling subsampling;

    public SampledSampleModel(int w, int h, ColorSubsampling subsampling) {
        super(DataBuffer.TYPE_BYTE, w, h, 3);
        this.subsampling = subsampling;
    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        return new SampledSampleModel(w, h, subsampling);
    }

    @Override
    public DataBuffer createDataBuffer() {
        return new DataBufferByte(subsampling.frameLength(width, height));
    }

    @Override
    public SampleModel createSubsetSampleModel(int[] bands) {
        if (bands.length != 3 || bands[0] != 0 || bands[1] != 1 || bands[2] != 2)
            throw new UnsupportedOperationException();

        return this;
    }

    @Override
    public Object getDataElements(int x, int y, Object object, DataBuffer data) {
        byte[] ret;
        if ((object instanceof byte[]) && ((byte[]) object).length == 3)
            ret = (byte[]) object;
        else
            ret = new byte[3];
        DataBufferByte dbb = (DataBufferByte) data;
        byte[] ba = dbb.getData();
        int iy = subsampling.indexOfY(x, y, width);
        int ibr = subsampling.indexOfBR(x, y, width);
        ret[0] = ba[iy];
        ret[1] = ba[ibr];
        ret[2] = ba[ibr + 1];
        return ret;
    }

    @Override
    public int getNumDataElements() {
        return 3;
    }

    @Override
    public int getSample(int x, int y, int b, DataBuffer data) {
        return ((byte[]) getDataElements(x, y, null, data))[b];
    }

    @Override
    public int[] getSampleSize() {
        return new int[] { 8, 8, 8 };
    }

    @Override
    public int getSampleSize(int band) {
        return 8;
    }

    @Override
    public void setDataElements(int x, int y, Object object, DataBuffer data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSample(int x, int y, int b, int s, DataBuffer data) {
        throw new UnsupportedOperationException();
    }

}
