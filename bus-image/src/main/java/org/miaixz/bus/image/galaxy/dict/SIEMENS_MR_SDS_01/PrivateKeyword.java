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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_SDS_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.SiemensMRSDSSequence:
            return "SiemensMRSDSSequence";
        case PrivateTag.UsedPatientWeight:
            return "UsedPatientWeight";
        case PrivateTag.SARWholeBody:
            return "SARWholeBody";
        case PrivateTag.MRProtocol:
            return "MRProtocol";
        case PrivateTag.SliceArrayConcatenations:
            return "SliceArrayConcatenations";
        case PrivateTag.RelTablePosition:
            return "RelTablePosition";
        case PrivateTag.CoilForGradient:
            return "CoilForGradient";
        case PrivateTag.LongModelName:
            return "LongModelName";
        case PrivateTag.GradientMode:
            return "GradientMode";
        case PrivateTag.PATModeText:
            return "PATModeText";
        case PrivateTag.SWCorrectionFactor:
            return "SWCorrectionFactor";
        case PrivateTag.RFPowerErrorIndicator:
            return "RFPowerErrorIndicator";
        case PrivateTag.PositivePCSDirections:
            return "PositivePCSDirections";
        case PrivateTag.ProtocolChangeHistory:
            return "ProtocolChangeHistory";
        case PrivateTag.DataFileName:
            return "DataFileName";
        case PrivateTag.Stimlim:
            return "Stimlim";
        case PrivateTag.MRProtocolVersion:
            return "MRProtocolVersion";
        case PrivateTag.PhaseGradientAmplitude:
            return "PhaseGradientAmplitude";
        case PrivateTag.ReadoutOS:
            return "ReadoutOS";
        case PrivateTag.tpulsmax:
            return "tpulsmax";
        case PrivateTag.NumberOfPrescans:
            return "NumberOfPrescans";
        case PrivateTag.MeasurementIndex:
            return "MeasurementIndex";
        case PrivateTag.dBdtThreshold:
            return "dBdtThreshold";
        case PrivateTag.SelectionGradientAmplitude:
            return "SelectionGradientAmplitude";
        case PrivateTag.RFSWDMostCriticalAspect:
            return "RFSWDMostCriticalAspect";
        case PrivateTag.MRPhoenixProtocol:
            return "MRPhoenixProtocol";
        case PrivateTag.CoilString:
            return "CoilString";
        case PrivateTag.SliceResolution:
            return "SliceResolution";
        case PrivateTag.Stimmaxonline:
            return "Stimmaxonline";
        case PrivateTag.OperationModeFlag:
            return "OperationModeFlag";
        case PrivateTag.AutoAlignMatrix:
            return "AutoAlignMatrix";
        case PrivateTag.CoilTuningReflection:
            return "CoilTuningReflection";
        case PrivateTag.RepresentativeImage:
            return "RepresentativeImage";
        case PrivateTag.SequenceFileOwner:
            return "SequenceFileOwner";
        case PrivateTag.RFWatchdogMask:
            return "RFWatchdogMask";
        case PrivateTag.PostProcProtocol:
            return "PostProcProtocol";
        case PrivateTag.TablePositionOrigin:
            return "TablePositionOrigin";
        case PrivateTag.MiscSequenceParam:
            return "MiscSequenceParam";
        case PrivateTag.Isocentered:
            return "Isocentered";
        case PrivateTag.CoilID:
            return "CoilID";
        case PrivateTag.PatReinPattern:
            return "PatReinPattern";
        case PrivateTag.SED:
            return "SED";
        case PrivateTag.SARMostCriticalAspect:
            return "SARMostCriticalAspect";
        case PrivateTag.StimmOnMode:
            return "StimmOnMode";
        case PrivateTag.GradientDelayTime:
            return "GradientDelayTime";
        case PrivateTag.ReadoutGradientAmplitude:
            return "ReadoutGradientAmplitude";
        case PrivateTag.AbsTablePosition:
            return "AbsTablePosition";
        case PrivateTag.RFSWDOperationMode:
            return "RFSWDOperationMode";
        case PrivateTag.CoilForGradient2:
            return "CoilForGradient2";
        case PrivateTag.StimFactor:
            return "StimFactor";
        case PrivateTag.Stimmaxgesnormonline:
            return "Stimmaxgesnormonline";
        case PrivateTag.dBdtmax:
            return "dBdtmax";
        case PrivateTag.TransmitterCalibration:
            return "TransmitterCalibration";
        case PrivateTag.MREVAProtocol:
            return "MREVAProtocol";
        case PrivateTag.dBdtLimit:
            return "dBdtLimit";
        case PrivateTag.VFModelInfo:
            return "VFModelInfo";
        case PrivateTag.PhaseSliceOversampling:
            return "PhaseSliceOversampling";
        case PrivateTag.VFSettings:
            return "VFSettings";
        case PrivateTag.AutoAlignData:
            return "AutoAlignData";
        case PrivateTag.FMRIModelParameters:
            return "FMRIModelParameters";
        case PrivateTag.FMRIModelInfo:
            return "FMRIModelInfo";
        case PrivateTag.FMRIExternalParameters:
            return "FMRIExternalParameters";
        case PrivateTag.FMRIExternalInfo:
            return "FMRIExternalInfo";
        case PrivateTag.B1RMS:
            return "B1RMS";
        case PrivateTag.B1RMSSupervision:
            return "B1RMSSupervision";
        case PrivateTag.TalesReferencePower:
            return "TalesReferencePower";
        case PrivateTag.SafetyStandard:
            return "SafetyStandard";
        case PrivateTag.DICOMImageFlavor:
            return "DICOMImageFlavor";
        case PrivateTag.DICOMAcquisitionContrast:
            return "DICOMAcquisitionContrast";
        case PrivateTag.RFEchoTrainLength4MF:
            return "RFEchoTrainLength4MF";
        case PrivateTag.GradientEchoTrainLength4MF:
            return "GradientEchoTrainLength4MF";
        case PrivateTag.VersionInfo:
            return "VersionInfo";
        case PrivateTag.Laterality4MF:
            return "Laterality4MF";
        }
        return "";
    }

}
