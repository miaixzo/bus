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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_DLR_01;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateElementDictionary extends ElementDictionary {

    public static final String PrivateCreator = "";

    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

        case PrivateTag._0019_xxA0_:
        case PrivateTag._0019_xxA1_:
            return VR.DS;
        case PrivateTag.MeasurementMode:
        case PrivateTag.ImageType:
        case PrivateTag.SoftwareVersion:
        case PrivateTag.MPMCode:
        case PrivateTag.Latitude:
        case PrivateTag.Sensitivity:
        case PrivateTag.EDR:
        case PrivateTag.LFix:
        case PrivateTag.SFix:
        case PrivateTag.PresetMode:
        case PrivateTag.Region:
        case PrivateTag.Subregion:
        case PrivateTag.Orientation:
        case PrivateTag.MarkOnFilm:
        case PrivateTag.RotationOnDRC:
        case PrivateTag.ReaderType:
        case PrivateTag.SubModality:
        case PrivateTag.ReaderSerialNumber:
        case PrivateTag.CassetteScale:
        case PrivateTag.CassetteMatrix:
        case PrivateTag.CassetteSubmatrix:
        case PrivateTag.Barcode:
        case PrivateTag.ContrastType:
        case PrivateTag.RotationAmount:
        case PrivateTag.RotationCenter:
        case PrivateTag.DensityShift:
        case PrivateTag.FrequencyEnhancement:
        case PrivateTag.FrequencyType:
        case PrivateTag.KernelLength:
        case PrivateTag.PLASource:
        case PrivateTag.PLADestination:
        case PrivateTag.UIDOriginalImage:
        case PrivateTag._0019_xx76_:
        case PrivateTag.PLAOfSecondaryDestination:
        case PrivateTag.FilmFormat:
        case PrivateTag.FilmSize:
        case PrivateTag.FullFilmFormat:
            return VR.LO;
        case PrivateTag.ReaderHeader:
            return VR.LT;
        case PrivateTag.KernelMode:
        case PrivateTag.ConvolutionMode:
            return VR.UL;
        case PrivateTag.FrequencyRank:
        case PrivateTag.NumberOfHardcopies:
            return VR.US;
        }
        return VR.UN;
    }
}
