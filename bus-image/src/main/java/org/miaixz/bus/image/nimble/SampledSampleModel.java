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
    public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
        byte[] ret;
        if ((obj instanceof byte[]) && ((byte[]) obj).length == 3)
            ret = (byte[]) obj;
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
    public void setDataElements(int x, int y, Object obj, DataBuffer data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSample(int x, int y, int b, int s, DataBuffer data) {
        throw new UnsupportedOperationException();
    }

}
