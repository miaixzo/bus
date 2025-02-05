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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_DLR_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.MeasurementMode:
            return "MeasurementMode";
        case PrivateTag.ImageType:
            return "ImageType";
        case PrivateTag.SoftwareVersion:
            return "SoftwareVersion";
        case PrivateTag.MPMCode:
            return "MPMCode";
        case PrivateTag.Latitude:
            return "Latitude";
        case PrivateTag.Sensitivity:
            return "Sensitivity";
        case PrivateTag.EDR:
            return "EDR";
        case PrivateTag.LFix:
            return "LFix";
        case PrivateTag.SFix:
            return "SFix";
        case PrivateTag.PresetMode:
            return "PresetMode";
        case PrivateTag.Region:
            return "Region";
        case PrivateTag.Subregion:
            return "Subregion";
        case PrivateTag.Orientation:
            return "Orientation";
        case PrivateTag.MarkOnFilm:
            return "MarkOnFilm";
        case PrivateTag.RotationOnDRC:
            return "RotationOnDRC";
        case PrivateTag.ReaderType:
            return "ReaderType";
        case PrivateTag.SubModality:
            return "SubModality";
        case PrivateTag.ReaderSerialNumber:
            return "ReaderSerialNumber";
        case PrivateTag.CassetteScale:
            return "CassetteScale";
        case PrivateTag.CassetteMatrix:
            return "CassetteMatrix";
        case PrivateTag.CassetteSubmatrix:
            return "CassetteSubmatrix";
        case PrivateTag.Barcode:
            return "Barcode";
        case PrivateTag.ContrastType:
            return "ContrastType";
        case PrivateTag.RotationAmount:
            return "RotationAmount";
        case PrivateTag.RotationCenter:
            return "RotationCenter";
        case PrivateTag.DensityShift:
            return "DensityShift";
        case PrivateTag.FrequencyRank:
            return "FrequencyRank";
        case PrivateTag.FrequencyEnhancement:
            return "FrequencyEnhancement";
        case PrivateTag.FrequencyType:
            return "FrequencyType";
        case PrivateTag.KernelLength:
            return "KernelLength";
        case PrivateTag.KernelMode:
            return "KernelMode";
        case PrivateTag.ConvolutionMode:
            return "ConvolutionMode";
        case PrivateTag.PLASource:
            return "PLASource";
        case PrivateTag.PLADestination:
            return "PLADestination";
        case PrivateTag.UIDOriginalImage:
            return "UIDOriginalImage";
        case PrivateTag._0019_xx76_:
            return "_0019_xx76_";
        case PrivateTag.ReaderHeader:
            return "ReaderHeader";
        case PrivateTag.PLAOfSecondaryDestination:
            return "PLAOfSecondaryDestination";
        case PrivateTag._0019_xxA0_:
            return "_0019_xxA0_";
        case PrivateTag._0019_xxA1_:
            return "_0019_xxA1_";
        case PrivateTag.NumberOfHardcopies:
            return "NumberOfHardcopies";
        case PrivateTag.FilmFormat:
            return "FilmFormat";
        case PrivateTag.FilmSize:
            return "FilmSize";
        case PrivateTag.FullFilmFormat:
            return "FullFilmFormat";
        }
        return "";
    }

}
