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
        
            case PrivateTag.Filter:
            case PrivateTag.CineParallel:
            case PrivateTag.CineMaster:
            case PrivateTag.VideoWhiteCompression:
            case PrivateTag.VideoInvertSubtracted:
            case PrivateTag.VideoInvertNonsubtracted:
            case PrivateTag.WindowSelectStatus:
            case PrivateTag.ECGDisplayPrinting:
            case PrivateTag.ECGDisplayPrintingEnableStatus:
            case PrivateTag.ECGDisplayPrintingSelectStatus:
            case PrivateTag.PhysiologicalDisplayEnableStatus:
            case PrivateTag.PhysiologicalDisplaySelectStatus:
            case PrivateTag._0029_xx9F_:
            case PrivateTag._0029_xxAF_:
            case PrivateTag._0029_xxBF_:
            case PrivateTag.ShutterEnableStatus:
            case PrivateTag.ShutterSelectStatus:
                return VR.CS;
            case PrivateTag.ImageIdentifierFromat:
            case PrivateTag.AcquisitionDelay:
            case PrivateTag._0029_xx00_:
            case PrivateTag.PixelAspectRatio:
            case PrivateTag._0029_xx90_:
            case PrivateTag._0029_xxA0_:
            case PrivateTag._0029_xxB0_:
                return VR.DS;
            case PrivateTag.ImageDataConsistency:
            case PrivateTag._0009_xx10_:
            case PrivateTag._0009_xx12_:
            case PrivateTag.UniqueIdentifier:
            case PrivateTag._0009_xx16_:
            case PrivateTag._0009_xx18_:
            case PrivateTag._0019_xx00_:
            case PrivateTag._0019_xx01_:
            case PrivateTag.OriginalPixelDataQuality:
            case PrivateTag.ProcessedPixelDataQuality:
                return VR.LO;
            case PrivateTag.DataObjectRecognitionCode:
            case PrivateTag._0009_xx21_:
            case PrivateTag.PACSUniqueIdentifier:
            case PrivateTag.ClusterUniqueIdentifier:
            case PrivateTag.SystemUniqueIdentifier:
            case PrivateTag._0009_xx39_:
            case PrivateTag.StudyUniqueIdentifier:
            case PrivateTag.SeriesUniqueIdentifier:
            case PrivateTag._0009_xx91_:
            case PrivateTag._0009_xxF2_:
            case PrivateTag._0009_xxF4_:
            case PrivateTag._0009_xxF7_:
            case PrivateTag.PatientEntryID:
            case PrivateTag._0021_xx14_:
            case PrivateTag._0029_xx30_:
            case PrivateTag._0029_xx60_:
            case PrivateTag._0029_xx61_:
            case PrivateTag._0029_xx67_:
            case PrivateTag.WindowID:
            case PrivateTag.ECGDisplayPrintingID:
            case PrivateTag.PhysiologicalDisplayID:
            case PrivateTag.FunctionalShutterID:
            case PrivateTag.FieldOfShutterRectangle:
                return VR.LT;
            case PrivateTag._0009_xx08_:
            case PrivateTag.MainsFrequency:
            case PrivateTag.ECGTriggering:
            case PrivateTag.VideoScanMode:
            case PrivateTag.VideoLineRate:
            case PrivateTag.XrayTechnique:
            case PrivateTag.IrisDiaphragm:
            case PrivateTag.ExposureChannel:
            case PrivateTag.ProcessingChannel:
            case PrivateTag.Angulation:
            case PrivateTag.Rotation:
            case PrivateTag._0029_xx38_:
            case PrivateTag.PreferredPhysiologicalChannelDisplay:
            case PrivateTag._0029_xx91_:
            case PrivateTag._0029_xxA1_:
            case PrivateTag._0029_xxB1_:
            case PrivateTag.FieldOfShutter:
                return VR.US;
        }
        return VR.UN;
    }
}
