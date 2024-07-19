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
package org.miaixz.bus.image.galaxy.dict.GEMS_GDXE_FALCON_04;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {
    
        switch (tag & 0xFFFF00FF) {
                case PrivateTag.ProcessedSeriesUID:
            return "ProcessedSeriesUID";
        case PrivateTag.AcquisitionType:
            return "AcquisitionType";
        case PrivateTag.AcquisitionUID:
            return "AcquisitionUID";
        case PrivateTag.ImageDose:
            return "ImageDose";
        case PrivateTag.StudyDose:
            return "StudyDose";
        case PrivateTag.StudyDAP:
            return "StudyDAP";
        case PrivateTag.NonDigitalExposures:
            return "NonDigitalExposures";
        case PrivateTag.TotalExposures:
            return "TotalExposures";
        case PrivateTag.ROI:
            return "ROI";
        case PrivateTag.PatientSizeString:
            return "PatientSizeString";
        case PrivateTag.SPSUID:
            return "SPSUID";
        case PrivateTag._0011_xx14_:
            return "_0011_xx14_";
        case PrivateTag.DetectorARCGain:
            return "DetectorARCGain";
        case PrivateTag.ProcessingDebugInfo:
            return "ProcessingDebugInfo";
        case PrivateTag.OverrideMode:
            return "OverrideMode";
        case PrivateTag.FilmSpeedSelection:
            return "FilmSpeedSelection";
        case PrivateTag._0011_xx27_:
            return "_0011_xx27_";
        case PrivateTag._0011_xx28_:
            return "_0011_xx28_";
        case PrivateTag._0011_xx29_:
            return "_0011_xx29_";
        case PrivateTag._0011_xx30_:
            return "_0011_xx30_";
        case PrivateTag.DetectedFieldOfView:
            return "DetectedFieldOfView";
        case PrivateTag.AdjustedFieldOfView:
            return "AdjustedFieldOfView";
        case PrivateTag.DetectorExposureIndex:
            return "DetectorExposureIndex";
        case PrivateTag.CompensatedDetectorExposure:
            return "CompensatedDetectorExposure";
        case PrivateTag.UncompensatedDetectorExposure:
            return "UncompensatedDetectorExposure";
        case PrivateTag.MedianAnatomyCountValue:
            return "MedianAnatomyCountValue";
        case PrivateTag.DEILowerAndUpperLimitValues:
            return "DEILowerAndUpperLimitValues";
        case PrivateTag.ShiftVectorForPasting:
            return "ShiftVectorForPasting";
        case PrivateTag.ImageNumberInPasting:
            return "ImageNumberInPasting";
        case PrivateTag.PastingOverlap:
            return "PastingOverlap";
        case PrivateTag.SubImageCollimatorVertices:
            return "SubImageCollimatorVertices";
        case PrivateTag.ViewIP:
            return "ViewIP";
        case PrivateTag.KeystoneCoordinates:
            return "KeystoneCoordinates";
        case PrivateTag.ReceptorType:
            return "ReceptorType";
        case PrivateTag._0011_xx46_:
            return "_0011_xx46_";
        case PrivateTag._0011_xx47_:
            return "_0011_xx47_";
        case PrivateTag._0011_xx59_:
            return "_0011_xx59_";
        case PrivateTag._0011_xx60_:
            return "_0011_xx60_";
        case PrivateTag._0011_xx6D_:
            return "_0011_xx6D_";
        }
        return "";
    }

}
