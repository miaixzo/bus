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
package org.miaixz.bus.image.galaxy.dict.agfa;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag._0009_xx10_:
            return "_0009_xx10_";
        case PrivateTag._0009_xx11_:
            return "_0009_xx11_";
        case PrivateTag._0009_xx13_:
            return "_0009_xx13_";
        case PrivateTag._0009_xx14_:
            return "_0009_xx14_";
        case PrivateTag._0009_xx15_:
            return "_0009_xx15_";
        case PrivateTag.CassetteDataStream:
            return "CassetteDataStream";
        case PrivateTag.ImageProcessingParameters:
            return "ImageProcessingParameters";
        case PrivateTag.IdentificationData:
            return "IdentificationData";
        case PrivateTag.SensitometryName:
            return "SensitometryName";
        case PrivateTag.WindowLevelList:
            return "WindowLevelList";
        case PrivateTag.DoseMonitoring:
            return "DoseMonitoring";
        case PrivateTag.OtherInfo:
            return "OtherInfo";
        case PrivateTag.ClippedExposureDeviation:
            return "ClippedExposureDeviation";
        case PrivateTag.LogarithmicPLTFullScale:
            return "LogarithmicPLTFullScale";
        case PrivateTag.TotalNumberSeries:
            return "TotalNumberSeries";
        case PrivateTag.SessionNumber:
            return "SessionNumber";
        case PrivateTag.IDStationName:
            return "IDStationName";
        case PrivateTag.NumberOfImagesInStudyToBeTransmitted:
            return "NumberOfImagesInStudyToBeTransmitted";
        case PrivateTag.TotalNumberImages:
            return "TotalNumberImages";
        case PrivateTag.GeometricalTransformations:
            return "GeometricalTransformations";
        case PrivateTag.RoamOrigin:
            return "RoamOrigin";
        case PrivateTag.ZoomFactor:
            return "ZoomFactor";
        case PrivateTag.Status:
            return "Status";
        }
        return "";
    }

}
