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
