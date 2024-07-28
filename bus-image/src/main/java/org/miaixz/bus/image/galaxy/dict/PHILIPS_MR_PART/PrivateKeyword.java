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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.FieldOfView:
            return "FieldOfView";
        case PrivateTag.StackType:
            return "StackType";
        case PrivateTag._0019_xx02_:
            return "_0019_xx02_";
        case PrivateTag._0019_xx03_:
            return "_0019_xx03_";
        case PrivateTag.CCAngulation:
            return "CCAngulation";
        case PrivateTag.APAngulation:
            return "APAngulation";
        case PrivateTag.LRAngulation:
            return "LRAngulation";
        case PrivateTag.PatientOrientation1:
            return "PatientOrientation1";
        case PrivateTag.PatientOrientation:
            return "PatientOrientation";
        case PrivateTag.SliceOrientation:
            return "SliceOrientation";
        case PrivateTag.LROffcenter:
            return "LROffcenter";
        case PrivateTag.CCOffcenter:
            return "CCOffcenter";
        case PrivateTag.APOffcenter:
            return "APOffcenter";
        case PrivateTag._0019_xx0e_:
            return "_0019_xx0e_";
        case PrivateTag.NumberOfSlices:
            return "NumberOfSlices";
        case PrivateTag.SliceFactor:
            return "SliceFactor";
        case PrivateTag.EchoTimes:
            return "EchoTimes";
        case PrivateTag._0019_xx14_:
            return "_0019_xx14_";
        case PrivateTag.DynamicStudy:
            return "DynamicStudy";
        case PrivateTag.HeartbeatInterval:
            return "HeartbeatInterval";
        case PrivateTag.RepetitionTimeFFE:
            return "RepetitionTimeFFE";
        case PrivateTag.FFEFlipAngle:
            return "FFEFlipAngle";
        case PrivateTag.NumberOfScans:
            return "NumberOfScans";
        case PrivateTag._0019_xx1c_:
            return "_0019_xx1c_";
        case PrivateTag._0019_xx1d_:
            return "_0019_xx1d_";
        case PrivateTag._0019_xx1e_:
            return "_0019_xx1e_";
        case PrivateTag._0019_xx21_:
            return "_0019_xx21_";
        case PrivateTag.DynamicScanTimeBegin:
            return "DynamicScanTimeBegin";
        case PrivateTag._0019_xx23_:
            return "_0019_xx23_";
        case PrivateTag._0019_xx24_:
            return "_0019_xx24_";
        case PrivateTag._0019_xx25_:
            return "_0019_xx25_";
        case PrivateTag._0019_xx26_:
            return "_0019_xx26_";
        case PrivateTag._0019_xx27_:
            return "_0019_xx27_";
        case PrivateTag._0019_xx28_:
            return "_0019_xx28_";
        case PrivateTag._0019_xx29_:
            return "_0019_xx29_";
        case PrivateTag._0019_xx30_:
            return "_0019_xx30_";
        case PrivateTag._0019_xx31_:
            return "_0019_xx31_";
        case PrivateTag._0019_xx40_:
            return "_0019_xx40_";
        case PrivateTag.ReconstructionResolution:
            return "ReconstructionResolution";
        case PrivateTag._0019_xx50_:
            return "_0019_xx50_";
        case PrivateTag._0019_xx51_:
            return "_0019_xx51_";
        case PrivateTag._0019_xx52_:
            return "_0019_xx52_";
        case PrivateTag._0019_xx53_:
            return "_0019_xx53_";
        case PrivateTag._0019_xx54_:
            return "_0019_xx54_";
        case PrivateTag._0019_xx55_:
            return "_0019_xx55_";
        case PrivateTag._0019_xx56_:
            return "_0019_xx56_";
        case PrivateTag._0019_xx57_:
            return "_0019_xx57_";
        case PrivateTag._0019_xx58_:
            return "_0019_xx58_";
        case PrivateTag._0019_xx59_:
            return "_0019_xx59_";
        case PrivateTag._0019_xx60_:
            return "_0019_xx60_";
        case PrivateTag._0019_xx61_:
            return "_0019_xx61_";
        case PrivateTag._0019_xx62_:
            return "_0019_xx62_";
        case PrivateTag._0019_xx63_:
            return "_0019_xx63_";
        case PrivateTag.RepetitionTimeSE:
            return "RepetitionTimeSE";
        case PrivateTag.RepetitionTimeIR:
            return "RepetitionTimeIR";
        case PrivateTag._0019_xx66_:
            return "_0019_xx66_";
        case PrivateTag._0019_xx67_:
            return "_0019_xx67_";
        case PrivateTag.NumberOfPhases:
            return "NumberOfPhases";
        case PrivateTag.CardiacFrequency:
            return "CardiacFrequency";
        case PrivateTag.InversionDelay:
            return "InversionDelay";
        case PrivateTag.GateDelay:
            return "GateDelay";
        case PrivateTag.GateWidth:
            return "GateWidth";
        case PrivateTag.TriggerDelayTime:
            return "TriggerDelayTime";
        case PrivateTag._0019_xx70_:
            return "_0019_xx70_";
        case PrivateTag.NumberOfChemicalShifts:
            return "NumberOfChemicalShifts";
        case PrivateTag.ChemicalShift:
            return "ChemicalShift";
        case PrivateTag.NumberOfRows:
            return "NumberOfRows";
        case PrivateTag.NumberOfSamples:
            return "NumberOfSamples";
        case PrivateTag._0019_xx8A_:
            return "_0019_xx8A_";
        case PrivateTag._0019_xx8B_:
            return "_0019_xx8B_";
        case PrivateTag._0019_xx8C_:
            return "_0019_xx8C_";
        case PrivateTag._0019_xx8D_:
            return "_0019_xx8D_";
        case PrivateTag._0019_xx8E_:
            return "_0019_xx8E_";
        case PrivateTag._0019_xx8F_:
            return "_0019_xx8F_";
        case PrivateTag.MagnetizationTransferContrast:
            return "MagnetizationTransferContrast";
        case PrivateTag.SpectralPresaturationWithInversionRecovery:
            return "SpectralPresaturationWithInversionRecovery";
        case PrivateTag._0019_xx96_:
            return "_0019_xx96_";
        case PrivateTag._0019_xx97_:
            return "_0019_xx97_";
        case PrivateTag._0019_xxB4_:
            return "_0019_xxB4_";
        case PrivateTag._0019_xxB5_:
            return "_0019_xxB5_";
        case PrivateTag._0019_xxB6_:
            return "_0019_xxB6_";
        case PrivateTag._0019_xxD1_:
            return "_0019_xxD1_";
        case PrivateTag._0019_xxD3_:
            return "_0019_xxD3_";
        case PrivateTag._0019_xxF0_:
            return "_0019_xxF0_";
        case PrivateTag._0019_xxF6_:
            return "_0019_xxF6_";
        case PrivateTag._0019_xxF7_:
            return "_0019_xxF7_";
        case PrivateTag._0019_xxF8_:
            return "_0019_xxF8_";
        case PrivateTag._0019_xxF9_:
            return "_0019_xxF9_";
        case PrivateTag._0019_xxFA_:
            return "_0019_xxFA_";
        case PrivateTag._0019_xxFB_:
            return "_0019_xxFB_";
        case PrivateTag._0019_xxa0_:
            return "_0019_xxa0_";
        case PrivateTag._0019_xxa1_:
            return "_0019_xxa1_";
        case PrivateTag._0019_xxa3_:
            return "_0019_xxa3_";
        case PrivateTag._0019_xxa4_:
            return "_0019_xxa4_";
        case PrivateTag.TriggerDelayTimes:
            return "TriggerDelayTimes";
        case PrivateTag._0019_xxc8_:
            return "_0019_xxc8_";
        case PrivateTag.FoldoverDirectionTransverse:
            return "FoldoverDirectionTransverse";
        case PrivateTag.FoldoverDirectionSagittal:
            return "FoldoverDirectionSagittal";
        case PrivateTag.FoldoverDirectionCoronal:
            return "FoldoverDirectionCoronal";
        case PrivateTag._0019_xxcc_:
            return "_0019_xxcc_";
        case PrivateTag._0019_xxcd_:
            return "_0019_xxcd_";
        case PrivateTag._0019_xxce_:
            return "_0019_xxce_";
        case PrivateTag.NumberOfEchoes:
            return "NumberOfEchoes";
        case PrivateTag.ScanResolution:
            return "ScanResolution";
        case PrivateTag.WaterFatShift:
            return "WaterFatShift";
        case PrivateTag.ArtifactReduction:
            return "ArtifactReduction";
        case PrivateTag._0019_xxd5_:
            return "_0019_xxd5_";
        case PrivateTag._0019_xxd6_:
            return "_0019_xxd6_";
        case PrivateTag.ScanPercentage:
            return "ScanPercentage";
        case PrivateTag.Halfscan:
            return "Halfscan";
        case PrivateTag.EPIFactor:
            return "EPIFactor";
        case PrivateTag.TurboFactor:
            return "TurboFactor";
        case PrivateTag._0019_xxdb_:
            return "_0019_xxdb_";
        case PrivateTag.PercentageOfScanCompleted:
            return "PercentageOfScanCompleted";
        case PrivateTag.PrepulseDelay:
            return "PrepulseDelay";
        case PrivateTag.PhaseContrastVelocity:
            return "PhaseContrastVelocity";
        case PrivateTag.ResonanceFrequency:
            return "ResonanceFrequency";
        case PrivateTag.ReconstructionNumber:
            return "ReconstructionNumber";
        case PrivateTag._0021_xx06_:
            return "_0021_xx06_";
        case PrivateTag._0021_xx08_:
            return "_0021_xx08_";
        case PrivateTag._0021_xx09_:
            return "_0021_xx09_";
        case PrivateTag._0021_xx0a_:
            return "_0021_xx0a_";
        case PrivateTag._0021_xx0f_:
            return "_0021_xx0f_";
        case PrivateTag.ImageType:
            return "ImageType";
        case PrivateTag._0021_xx13_:
            return "_0021_xx13_";
        case PrivateTag._0021_xx15_:
            return "_0021_xx15_";
        case PrivateTag.SliceNumber:
            return "SliceNumber";
        case PrivateTag.SliceGap:
            return "SliceGap";
        case PrivateTag.EchoNumber:
            return "EchoNumber";
        case PrivateTag.PatientReferenceID:
            return "PatientReferenceID";
        case PrivateTag.ChemicalShiftNumber:
            return "ChemicalShiftNumber";
        case PrivateTag.PhaseNumber:
            return "PhaseNumber";
        case PrivateTag.DynamicScanNumber:
            return "DynamicScanNumber";
        case PrivateTag.NumberOfRowsInObject:
            return "NumberOfRowsInObject";
        case PrivateTag.RowNumber:
            return "RowNumber";
        case PrivateTag._0021_xx62_:
            return "_0021_xx62_";
        case PrivateTag._0029_xx00_:
            return "_0029_xx00_";
        case PrivateTag._0029_xx04_:
            return "_0029_xx04_";
        case PrivateTag._0029_xx10_:
            return "_0029_xx10_";
        case PrivateTag._0029_xx11_:
            return "_0029_xx11_";
        case PrivateTag._0029_xx20_:
            return "_0029_xx20_";
        case PrivateTag._0029_xx31_:
            return "_0029_xx31_";
        case PrivateTag._0029_xx32_:
            return "_0029_xx32_";
        case PrivateTag._0029_xx50_:
            return "_0029_xx50_";
        case PrivateTag._0029_xx51_:
            return "_0029_xx51_";
        case PrivateTag._0029_xx52_:
            return "_0029_xx52_";
        case PrivateTag._0029_xx53_:
            return "_0029_xx53_";
        case PrivateTag.SliceThickness:
            return "SliceThickness";
        }
        return "";
    }

}
