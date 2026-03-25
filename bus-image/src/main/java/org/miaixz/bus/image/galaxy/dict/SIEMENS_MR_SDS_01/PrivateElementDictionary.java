/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_SDS_01;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 21+
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

            case PrivateTag.PhaseSliceOversampling:
            case PrivateTag.B1RMSSupervision:
            case PrivateTag.SafetyStandard:
            case PrivateTag.DICOMImageFlavor:
            case PrivateTag.DICOMAcquisitionContrast:
            case PrivateTag.Laterality4MF:
                return VR.CS;

            case PrivateTag.SARWholeBody:
            case PrivateTag.SliceArrayConcatenations:
            case PrivateTag.SWCorrectionFactor:
            case PrivateTag.RFPowerErrorIndicator:
            case PrivateTag.Stimlim:
            case PrivateTag.PhaseGradientAmplitude:
            case PrivateTag.tpulsmax:
            case PrivateTag.dBdtThreshold:
            case PrivateTag.SelectionGradientAmplitude:
            case PrivateTag.SliceResolution:
            case PrivateTag.Stimmaxonline:
            case PrivateTag.CoilTuningReflection:
            case PrivateTag.SED:
            case PrivateTag.SARMostCriticalAspect:
            case PrivateTag.GradientDelayTime:
            case PrivateTag.ReadoutGradientAmplitude:
            case PrivateTag.StimFactor:
            case PrivateTag.Stimmaxgesnormonline:
            case PrivateTag.dBdtmax:
            case PrivateTag.TransmitterCalibration:
            case PrivateTag.dBdtLimit:
            case PrivateTag.B1RMS:
            case PrivateTag.TalesReferencePower:
                return VR.DS;

            case PrivateTag.MeasurementIndex:
            case PrivateTag.AutoAlignMatrix:
                return VR.FL;

            case PrivateTag.ReadoutOS:
                return VR.FD;

            case PrivateTag.UsedPatientWeight:
            case PrivateTag.RelTablePosition:
            case PrivateTag.MRProtocolVersion:
            case PrivateTag.NumberOfPrescans:
            case PrivateTag.OperationModeFlag:
            case PrivateTag.RFWatchdogMask:
            case PrivateTag.MiscSequenceParam:
            case PrivateTag.CoilID:
            case PrivateTag.StimmOnMode:
            case PrivateTag.AbsTablePosition:
                return VR.IS;

            case PrivateTag.CoilForGradient:
            case PrivateTag.LongModelName:
            case PrivateTag.PATModeText:
            case PrivateTag.DataFileName:
            case PrivateTag.CoilString:
            case PrivateTag.PostProcProtocol:
            case PrivateTag.VersionInfo:
                return VR.LO;

            case PrivateTag.MRProtocol:
            case PrivateTag.MRPhoenixProtocol:
            case PrivateTag.MREVAProtocol:
            case PrivateTag.VFModelInfo:
            case PrivateTag.VFSettings:
                return VR.OB;

            case PrivateTag.GradientMode:
            case PrivateTag.PositivePCSDirections:
            case PrivateTag.RFSWDMostCriticalAspect:
            case PrivateTag.SequenceFileOwner:
            case PrivateTag.CoilForGradient2:
                return VR.SH;

            case PrivateTag.TablePositionOrigin:
                return VR.SL;

            case PrivateTag.SiemensMRSDSSequence:
                return VR.SQ;

            case PrivateTag.RFSWDOperationMode:
                return VR.SS;

            case PrivateTag.PatReinPattern:
                return VR.ST;

            case PrivateTag.RepresentativeImage:
                return VR.UI;

            case PrivateTag.ProtocolChangeHistory:
            case PrivateTag.Isocentered:
            case PrivateTag.RFEchoTrainLength4MF:
            case PrivateTag.GradientEchoTrainLength4MF:
                return VR.US;

            case PrivateTag.AutoAlignData:
            case PrivateTag.FMRIModelParameters:
            case PrivateTag.FMRIModelInfo:
            case PrivateTag.FMRIExternalParameters:
            case PrivateTag.FMRIExternalInfo:
                return VR.UT;
        }
        return VR.UN;
    }

}
