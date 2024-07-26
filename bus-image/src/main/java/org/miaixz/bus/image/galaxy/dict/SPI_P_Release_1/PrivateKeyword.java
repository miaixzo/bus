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
package org.miaixz.bus.image.galaxy.dict.SPI_P_Release_1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.DataObjectRecognitionCode:
            return "DataObjectRecognitionCode";
        case PrivateTag.ImageDataConsistency:
            return "ImageDataConsistency";
        case PrivateTag._0009_xx08_:
            return "_0009_xx08_";
        case PrivateTag._0009_xx10_:
            return "_0009_xx10_";
        case PrivateTag._0009_xx12_:
            return "_0009_xx12_";
        case PrivateTag.UniqueIdentifier:
            return "UniqueIdentifier";
        case PrivateTag._0009_xx16_:
            return "_0009_xx16_";
        case PrivateTag._0009_xx18_:
            return "_0009_xx18_";
        case PrivateTag._0009_xx21_:
            return "_0009_xx21_";
        case PrivateTag.PACSUniqueIdentifier:
            return "PACSUniqueIdentifier";
        case PrivateTag.ClusterUniqueIdentifier:
            return "ClusterUniqueIdentifier";
        case PrivateTag.SystemUniqueIdentifier:
            return "SystemUniqueIdentifier";
        case PrivateTag._0009_xx39_:
            return "_0009_xx39_";
        case PrivateTag.StudyUniqueIdentifier:
            return "StudyUniqueIdentifier";
        case PrivateTag.SeriesUniqueIdentifier:
            return "SeriesUniqueIdentifier";
        case PrivateTag._0009_xx91_:
            return "_0009_xx91_";
        case PrivateTag._0009_xxA0_:
            return "_0009_xxA0_";
        case PrivateTag._0009_xxF2_:
            return "_0009_xxF2_";
        case PrivateTag._0009_xxF3_:
            return "_0009_xxF3_";
        case PrivateTag._0009_xxF4_:
            return "_0009_xxF4_";
        case PrivateTag._0009_xxF5_:
            return "_0009_xxF5_";
        case PrivateTag._0009_xxF7_:
            return "_0009_xxF7_";
        case PrivateTag.PatientEntryID:
            return "PatientEntryID";
        case PrivateTag._0011_xx20_:
            return "_0011_xx20_";
        case PrivateTag._0011_xx21_:
            return "_0011_xx21_";
        case PrivateTag._0011_xx22_:
            return "_0011_xx22_";
        case PrivateTag._0011_xx30_:
            return "_0011_xx30_";
        case PrivateTag._0011_xx31_:
            return "_0011_xx31_";
        case PrivateTag._0011_xx32_:
            return "_0011_xx32_";
        case PrivateTag._0019_xx00_:
            return "_0019_xx00_";
        case PrivateTag._0019_xx01_:
            return "_0019_xx01_";
        case PrivateTag._0019_xx02_:
            return "_0019_xx02_";
        case PrivateTag.MainsFrequency:
            return "MainsFrequency";
        case PrivateTag.OriginalPixelDataQuality:
            return "OriginalPixelDataQuality";
        case PrivateTag.ECGTriggering:
            return "ECGTriggering";
        case PrivateTag.ECG1Offset:
            return "ECG1Offset";
        case PrivateTag.ECG2Offset1:
            return "ECG2Offset1";
        case PrivateTag.ECG2Offset2:
            return "ECG2Offset2";
        case PrivateTag.VideoScanMode:
            return "VideoScanMode";
        case PrivateTag.VideoLineRate:
            return "VideoLineRate";
        case PrivateTag.XrayTechnique:
            return "XrayTechnique";
        case PrivateTag.ImageIdentifierFromat:
            return "ImageIdentifierFromat";
        case PrivateTag.IrisDiaphragm:
            return "IrisDiaphragm";
        case PrivateTag.Filter:
            return "Filter";
        case PrivateTag.CineParallel:
            return "CineParallel";
        case PrivateTag.CineMaster:
            return "CineMaster";
        case PrivateTag.ExposureChannel:
            return "ExposureChannel";
        case PrivateTag.ExposureChannelFirstImage:
            return "ExposureChannelFirstImage";
        case PrivateTag.ProcessingChannel:
            return "ProcessingChannel";
        case PrivateTag.AcquisitionDelay:
            return "AcquisitionDelay";
        case PrivateTag.RelativeImageTime:
            return "RelativeImageTime";
        case PrivateTag.VideoWhiteCompression:
            return "VideoWhiteCompression";
        case PrivateTag.Angulation:
            return "Angulation";
        case PrivateTag.Rotation:
            return "Rotation";
        case PrivateTag._0021_xx14_:
            return "_0021_xx14_";
        case PrivateTag._0029_xx00_:
            return "_0029_xx00_";
        case PrivateTag.PixelAspectRatio:
            return "PixelAspectRatio";
        case PrivateTag.ProcessedPixelDataQuality:
            return "ProcessedPixelDataQuality";
        case PrivateTag._0029_xx30_:
            return "_0029_xx30_";
        case PrivateTag._0029_xx38_:
            return "_0029_xx38_";
        case PrivateTag._0029_xx60_:
            return "_0029_xx60_";
        case PrivateTag._0029_xx61_:
            return "_0029_xx61_";
        case PrivateTag._0029_xx67_:
            return "_0029_xx67_";
        case PrivateTag.WindowID:
            return "WindowID";
        case PrivateTag.VideoInvertSubtracted:
            return "VideoInvertSubtracted";
        case PrivateTag.VideoInvertNonsubtracted:
            return "VideoInvertNonsubtracted";
        case PrivateTag.WindowSelectStatus:
            return "WindowSelectStatus";
        case PrivateTag.ECGDisplayPrintingID:
            return "ECGDisplayPrintingID";
        case PrivateTag.ECGDisplayPrinting:
            return "ECGDisplayPrinting";
        case PrivateTag.ECGDisplayPrintingEnableStatus:
            return "ECGDisplayPrintingEnableStatus";
        case PrivateTag.ECGDisplayPrintingSelectStatus:
            return "ECGDisplayPrintingSelectStatus";
        case PrivateTag.PhysiologicalDisplayID:
            return "PhysiologicalDisplayID";
        case PrivateTag.PreferredPhysiologicalChannelDisplay:
            return "PreferredPhysiologicalChannelDisplay";
        case PrivateTag.PhysiologicalDisplayEnableStatus:
            return "PhysiologicalDisplayEnableStatus";
        case PrivateTag.PhysiologicalDisplaySelectStatus:
            return "PhysiologicalDisplaySelectStatus";
        case PrivateTag._0029_xx90_:
            return "_0029_xx90_";
        case PrivateTag._0029_xx91_:
            return "_0029_xx91_";
        case PrivateTag._0029_xx9F_:
            return "_0029_xx9F_";
        case PrivateTag._0029_xxA0_:
            return "_0029_xxA0_";
        case PrivateTag._0029_xxA1_:
            return "_0029_xxA1_";
        case PrivateTag._0029_xxAF_:
            return "_0029_xxAF_";
        case PrivateTag._0029_xxB0_:
            return "_0029_xxB0_";
        case PrivateTag._0029_xxB1_:
            return "_0029_xxB1_";
        case PrivateTag._0029_xxBF_:
            return "_0029_xxBF_";
        case PrivateTag.FunctionalShutterID:
            return "FunctionalShutterID";
        case PrivateTag.FieldOfShutter:
            return "FieldOfShutter";
        case PrivateTag.FieldOfShutterRectangle:
            return "FieldOfShutterRectangle";
        case PrivateTag.ShutterEnableStatus:
            return "ShutterEnableStatus";
        case PrivateTag.ShutterSelectStatus:
            return "ShutterSelectStatus";
        case PrivateTag.PixelData:
            return "PixelData";
        }
        return "";
    }

}
