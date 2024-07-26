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
package org.miaixz.bus.image.galaxy.dict.PHILIPS_MR_PART;

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

        case PrivateTag._0019_xx14_:
        case PrivateTag._0019_xx1c_:
        case PrivateTag._0019_xx1d_:
        case PrivateTag._0019_xx28_:
        case PrivateTag._0019_xx8A_:
        case PrivateTag._0019_xx8B_:
        case PrivateTag._0019_xx8C_:
        case PrivateTag._0019_xx8D_:
        case PrivateTag._0019_xx8E_:
        case PrivateTag._0019_xx8F_:
        case PrivateTag._0019_xxF6_:
        case PrivateTag._0019_xxF7_:
        case PrivateTag._0019_xxF8_:
        case PrivateTag._0019_xxF9_:
        case PrivateTag._0019_xxFA_:
        case PrivateTag._0019_xxFB_:
        case PrivateTag._0019_xxa4_:
        case PrivateTag._0021_xx09_:
        case PrivateTag._0021_xx0a_:
        case PrivateTag._0021_xx13_:
            return VR.CS;
        case PrivateTag.FieldOfView:
        case PrivateTag._0019_xx03_:
        case PrivateTag.CCAngulation:
        case PrivateTag.APAngulation:
        case PrivateTag.LRAngulation:
        case PrivateTag.LROffcenter:
        case PrivateTag.CCOffcenter:
        case PrivateTag.APOffcenter:
        case PrivateTag._0019_xx0e_:
        case PrivateTag.SliceFactor:
        case PrivateTag.EchoTimes:
        case PrivateTag.HeartbeatInterval:
        case PrivateTag.RepetitionTimeFFE:
        case PrivateTag.FFEFlipAngle:
        case PrivateTag._0019_xx1e_:
        case PrivateTag._0019_xx21_:
        case PrivateTag.DynamicScanTimeBegin:
        case PrivateTag._0019_xx23_:
        case PrivateTag._0019_xx25_:
        case PrivateTag._0019_xx26_:
        case PrivateTag._0019_xx27_:
        case PrivateTag._0019_xx29_:
        case PrivateTag._0019_xx31_:
        case PrivateTag._0019_xx51_:
        case PrivateTag._0019_xx52_:
        case PrivateTag._0019_xx54_:
        case PrivateTag._0019_xx55_:
        case PrivateTag._0019_xx56_:
        case PrivateTag._0019_xx58_:
        case PrivateTag._0019_xx60_:
        case PrivateTag._0019_xx61_:
        case PrivateTag._0019_xx62_:
        case PrivateTag._0019_xx63_:
        case PrivateTag.RepetitionTimeSE:
        case PrivateTag.RepetitionTimeIR:
        case PrivateTag.InversionDelay:
        case PrivateTag.GateDelay:
        case PrivateTag.GateWidth:
        case PrivateTag.TriggerDelayTime:
        case PrivateTag._0019_xx70_:
        case PrivateTag.ChemicalShift:
        case PrivateTag._0019_xxB4_:
        case PrivateTag._0019_xxB5_:
        case PrivateTag._0019_xxB6_:
        case PrivateTag._0019_xxD3_:
        case PrivateTag._0019_xxa1_:
        case PrivateTag._0019_xxa3_:
        case PrivateTag.TriggerDelayTimes:
        case PrivateTag.ScanPercentage:
        case PrivateTag.PrepulseDelay:
        case PrivateTag.PhaseContrastVelocity:
        case PrivateTag.PatientReferenceID:
        case PrivateTag._0029_xx00_:
        case PrivateTag._0029_xx10_:
        case PrivateTag._0029_xx11_:
        case PrivateTag._0029_xx31_:
        case PrivateTag._0029_xx32_:
        case PrivateTag._0029_xx50_:
        case PrivateTag._0029_xx51_:
        case PrivateTag._0029_xx52_:
        case PrivateTag._0029_xx53_:
            return VR.DS;
        case PrivateTag.StackType:
        case PrivateTag._0019_xx02_:
        case PrivateTag.PatientOrientation1:
        case PrivateTag.PatientOrientation:
        case PrivateTag.SliceOrientation:
        case PrivateTag.NumberOfSlices:
        case PrivateTag.DynamicStudy:
        case PrivateTag.NumberOfScans:
        case PrivateTag._0019_xx24_:
        case PrivateTag.ReconstructionResolution:
        case PrivateTag._0019_xx50_:
        case PrivateTag._0019_xx53_:
        case PrivateTag.NumberOfPhases:
        case PrivateTag.CardiacFrequency:
        case PrivateTag.NumberOfChemicalShifts:
        case PrivateTag.NumberOfRows:
        case PrivateTag.NumberOfSamples:
        case PrivateTag._0019_xx96_:
        case PrivateTag._0019_xxF0_:
        case PrivateTag._0019_xxa0_:
        case PrivateTag._0019_xxc8_:
        case PrivateTag.FoldoverDirectionTransverse:
        case PrivateTag.FoldoverDirectionSagittal:
        case PrivateTag.FoldoverDirectionCoronal:
        case PrivateTag._0019_xxcc_:
        case PrivateTag._0019_xxcd_:
        case PrivateTag._0019_xxce_:
        case PrivateTag.NumberOfEchoes:
        case PrivateTag.ScanResolution:
        case PrivateTag.ArtifactReduction:
        case PrivateTag._0019_xxd5_:
        case PrivateTag._0019_xxd6_:
        case PrivateTag.Halfscan:
        case PrivateTag.EPIFactor:
        case PrivateTag.TurboFactor:
        case PrivateTag._0019_xxdb_:
        case PrivateTag.PercentageOfScanCompleted:
        case PrivateTag.ResonanceFrequency:
        case PrivateTag.ReconstructionNumber:
        case PrivateTag.ImageType:
        case PrivateTag.SliceNumber:
        case PrivateTag.SliceGap:
        case PrivateTag.EchoNumber:
        case PrivateTag.ChemicalShiftNumber:
        case PrivateTag.PhaseNumber:
        case PrivateTag.DynamicScanNumber:
        case PrivateTag.NumberOfRowsInObject:
        case PrivateTag.RowNumber:
        case PrivateTag._0021_xx62_:
            return VR.IS;
        case PrivateTag._0019_xx30_:
        case PrivateTag.MagnetizationTransferContrast:
        case PrivateTag.SpectralPresaturationWithInversionRecovery:
        case PrivateTag._0019_xx97_:
        case PrivateTag.WaterFatShift:
        case PrivateTag._0021_xx06_:
        case PrivateTag._0021_xx08_:
        case PrivateTag._0021_xx0f_:
        case PrivateTag._0029_xx20_:
            return VR.LO;
        case PrivateTag.SliceThickness:
            return VR.LT;
        case PrivateTag._0019_xx40_:
        case PrivateTag._0019_xx57_:
        case PrivateTag._0019_xx59_:
        case PrivateTag._0019_xx66_:
        case PrivateTag._0019_xx67_:
        case PrivateTag._0019_xxD1_:
        case PrivateTag._0021_xx15_:
        case PrivateTag._0029_xx04_:
            return VR.US;
        }
        return VR.UN;
    }
}
