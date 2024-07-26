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
package org.miaixz.bus.image.galaxy.dict.elscint;

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

        case PrivateTag.TamarSourceAE:
        case PrivateTag.TamarGridTokenAE:
        case PrivateTag.TamarOriginalStoringAE:
            return VR.AE;
        case PrivateTag.TamarExcludeTagsList:
            return VR.AT;
        case PrivateTag._00E1_xx14_:
        case PrivateTag.PresentationHorizontalInvert:
        case PrivateTag.PresentationVerticalInvert:
        case PrivateTag._00E1_xx31_:
        case PrivateTag._00E1_xx3F_:
        case PrivateTag._00E1_xx60_:
        case PrivateTag._00E1_xx62_:
        case PrivateTag.PhantomType:
        case PrivateTag.ReferenceType:
        case PrivateTag.ReferenceLevel:
        case PrivateTag.AcquisitionType:
        case PrivateTag.FocalSpotResolution:
        case PrivateTag.ConcurrentSlicesGeneration:
        case PrivateTag.AngularSamplingDensity:
        case PrivateTag._01F1_xx0B_:
        case PrivateTag.ImageViewConvention:
        case PrivateTag._01F1_xx36_:
        case PrivateTag._01F1_xx40_:
        case PrivateTag.SCCTEquivalent:
        case PrivateTag.TamarCompressionType:
        case PrivateTag.TamarStudyStatus:
        case PrivateTag._07A1_xx2B_:
        case PrivateTag.TamarStudyBodyPart:
        case PrivateTag._07A1_xx47_:
        case PrivateTag.TamarStudyPublished:
        case PrivateTag.TamarStudyHasBookmark:
        case PrivateTag._07A1_xx88_:
        case PrivateTag._07A1_xx98_:
        case PrivateTag._07A1_xx9F_:
        case PrivateTag.TamarIsHcffHeader:
        case PrivateTag.TamarStudyHasStickyNote:
        case PrivateTag.TamarMpSavePr:
        case PrivateTag.TamarOriginalPrModality:
        case PrivateTag.TamarOriginalCurveType:
        case PrivateTag._07A3_xx65_:
        case PrivateTag._07A3_xx8F_:
        case PrivateTag.TamarIsDocType:
        case PrivateTag.TamarStudyHasKeyImage:
        case PrivateTag._07A3_xx9F_:
        case PrivateTag.TamarKeySeriesIndication:
        case PrivateTag.TamarStudyHasKeySeries:
        case PrivateTag.TamarGridTokenSpeed:
        case PrivateTag.TamarGridTokenSecurity:
        case PrivateTag.TamarGridTokenTunneled:
        case PrivateTag.TamarNestedTextLineVisibility:
        case PrivateTag.TamarNestedTextDefaultLocation:
        case PrivateTag.TamarNestedTypeOfData:
        case PrivateTag._07A3_xxF2_:
        case PrivateTag.TamarReferringPhysiciansStudyRead:
        case PrivateTag._07A5_xx63_:
        case PrivateTag._07A5_xxC8_:
        case PrivateTag.TextOverlayFlag:
            return VR.CS;
        case PrivateTag._07A1_xx96_:
            return VR.DA;
        case PrivateTag.DLPTotal:
        case PrivateTag.PresentationRelativeCenter:
        case PrivateTag.PresentationRelativePart:
        case PrivateTag._00E1_xx2A_:
        case PrivateTag.TotalDoseSavings:
        case PrivateTag._00E1_xx41_:
        case PrivateTag.AcquisitionDuration:
        case PrivateTag._00E1_xxC4_:
        case PrivateTag.ReconstructionArc:
        case PrivateTag._01F1_xx06_:
        case PrivateTag.TableVelocity:
        case PrivateTag.AcquisitionLength:
        case PrivateTag.ScannerRelativeCenter:
        case PrivateTag.RotationAngle:
        case PrivateTag.Pitch:
        case PrivateTag.RotationTime:
        case PrivateTag.TableIncrement:
        case PrivateTag.CycleTime:
        case PrivateTag._01F1_xx37_:
        case PrivateTag._01F1_xx49_:
        case PrivateTag.keV:
        case PrivateTag._01F9_xx08_:
        case PrivateTag._01F9_xx09_:
        case PrivateTag.RelativeTablePosition:
        case PrivateTag.RelativeTableHeight:
        case PrivateTag.SurviewLength:
        case PrivateTag.BatchNumber:
        case PrivateTag.BatchSize:
        case PrivateTag.BatchSliceNumber:
        case PrivateTag._07A1_xx08_:
        case PrivateTag.TamarPanSideDetails:
        case PrivateTag._07A3_xx92_:
        case PrivateTag._07A3_xx93_:
            return VR.DS;
        case PrivateTag.TamarStudyCreationDate:
        case PrivateTag._07A3_xxFA_:
        case PrivateTag._07A3_xxFB_:
        case PrivateTag.TamarReportsUpdateDate:
            return VR.DT;
        case PrivateTag._01F1_xx0E_:
        case PrivateTag._01F1_xx46_:
        case PrivateTag._01F3_xx03_:
        case PrivateTag._01F3_xx04_:
        case PrivateTag._01F3_xx13_:
        case PrivateTag._01F3_xx14_:
        case PrivateTag._01F3_xx16_:
        case PrivateTag._01F3_xx17_:
        case PrivateTag._01F3_xx19_:
        case PrivateTag._07A1_xx12_:
        case PrivateTag._07A1_xx16_:
        case PrivateTag._07A1_xx19_:
        case PrivateTag._07A1_xx1C_:
        case PrivateTag._5001_xx19_:
        case PrivateTag._5003_xx19_:
        case PrivateTag._5005_xx19_:
        case PrivateTag._6001_xx30_:
            return VR.FL;
        case PrivateTag._00E1_xx05_:
        case PrivateTag._00E1_xx06_:
        case PrivateTag._00E1_xx07_:
        case PrivateTag._00E1_xx3E_:
        case PrivateTag._00E1_xx43_:
        case PrivateTag._00E1_xx6A_:
        case PrivateTag._00E1_xx6B_:
        case PrivateTag._00E1_xxCF_:
        case PrivateTag._01E1_xx34_:
        case PrivateTag._01F1_xx45_:
        case PrivateTag._01F3_xx24_:
        case PrivateTag.iDoseLevel:
        case PrivateTag.SpectralLevel:
        case PrivateTag.AdaptiveFilter:
        case PrivateTag.ReconIncrement:
        case PrivateTag.TamarStudyPriority:
        case PrivateTag.TamarExcludeTagsSize:
        case PrivateTag.TamarExcludeTagsTotalSize:
        case PrivateTag._07A3_xx09_:
        case PrivateTag._07A3_xx64_:
        case PrivateTag._07A3_xx66_:
        case PrivateTag._07A5_xx59_:
        case PrivateTag._07A5_xxAE_:
            return VR.IS;
        case PrivateTag._00E1_xx42_:
        case PrivateTag.ProtocolFileName:
        case PrivateTag.PatientDataModificationDate:
        case PrivateTag._00E1_xxA0_:
        case PrivateTag._01F1_xx38_:
        case PrivateTag._01F1_xx39_:
        case PrivateTag._01F1_xx43_:
        case PrivateTag._01F1_xx4E_:
        case PrivateTag.SPFilter:
        case PrivateTag.TamarSoftwareVersion:
        case PrivateTag.ProtectionFlag:
        case PrivateTag.TamarSourceImageType:
        case PrivateTag._07A1_xxC3_:
        case PrivateTag._07A1_xxD0_:
        case PrivateTag.TamarExeSoftwareVersion:
        case PrivateTag.TamarOriginalCurveDesc:
        case PrivateTag.TamarGridTokenTargetGridName:
        case PrivateTag.TamarGridTokenNextHost:
        case PrivateTag.TamarGridTokenNextHostPort:
        case PrivateTag.TamarGridTokenVersion:
        case PrivateTag._07A3_xxE3_:
        case PrivateTag._07A3_xxF5_:
        case PrivateTag._07A5_xx00_:
        case PrivateTag.TamarStudyInstitutionName:
        case PrivateTag._6001_xx90_:
            return VR.LO;
        case PrivateTag.ReferenceSBIType:
        case PrivateTag.BurnedSpectralAnnotations:
        case PrivateTag._07A1_xx45_:
        case PrivateTag._07A1_xx87_:
        case PrivateTag.TamarNondicomAnnotations:
            return VR.LT;
        case PrivateTag._00E1_xx18_:
        case PrivateTag._00E3_xx00_:
        case PrivateTag._00E3_xx18_:
        case PrivateTag._00E3_xx1F_:
        case PrivateTag._01E1_xx18_:
        case PrivateTag._01F7_xx10_:
        case PrivateTag.TamarCompressedPixelData:
        case PrivateTag._7FDF_xxF0_:
            return VR.OB;
        case PrivateTag.OffsetListStructure:
        case PrivateTag._01E1_xx41_:
        case PrivateTag._01F1_xx44_:
        case PrivateTag._01F7_xx11_:
        case PrivateTag._01F7_xx13_:
        case PrivateTag._01F7_xx14_:
        case PrivateTag._01F7_xx15_:
        case PrivateTag._01F7_xx16_:
        case PrivateTag._01F7_xx17_:
        case PrivateTag._01F7_xx18_:
        case PrivateTag._01F7_xx19_:
        case PrivateTag._01F7_xx1A_:
        case PrivateTag._01F7_xx1B_:
        case PrivateTag._01F7_xx1C_:
        case PrivateTag._01F7_xx1E_:
        case PrivateTag._01F7_xx1F_:
        case PrivateTag._01F7_xx23_:
        case PrivateTag._01F7_xx25_:
        case PrivateTag._01F7_xx26_:
        case PrivateTag._01F7_xx27_:
        case PrivateTag._01F7_xx28_:
        case PrivateTag._01F7_xx29_:
        case PrivateTag._01F7_xx2B_:
        case PrivateTag._01F7_xx2C_:
        case PrivateTag._01F7_xx2D_:
        case PrivateTag._01F7_xx2E_:
        case PrivateTag._01F7_xx30_:
        case PrivateTag._01F7_xx31_:
        case PrivateTag._01F7_xx5C_:
        case PrivateTag._01F7_xx70_:
        case PrivateTag._01F7_xx73_:
        case PrivateTag._01F7_xx74_:
        case PrivateTag._01F7_xx75_:
        case PrivateTag._01F7_xx7F_:
        case PrivateTag._07A1_xx09_:
            return VR.OW;
        case PrivateTag.ImageLabel:
        case PrivateTag.SeriesLabel:
        case PrivateTag.PatientLanguage:
        case PrivateTag._01F1_xx42_:
        case PrivateTag._01F1_xx47_:
        case PrivateTag._01F1_xx4A_:
        case PrivateTag._01F1_xx4B_:
        case PrivateTag._01F1_xx4C_:
        case PrivateTag._01F1_xx4D_:
        case PrivateTag._01F1_xx53_:
        case PrivateTag._01F3_xx18_:
        case PrivateTag.HeadBody:
        case PrivateTag.ImplementationVersion:
        case PrivateTag.SurviewDirection:
        case PrivateTag.ImageViewType:
        case PrivateTag._07A1_xx42_:
        case PrivateTag._07A1_xx4A_:
        case PrivateTag._07A1_xx70_:
        case PrivateTag._07A1_xx71_:
        case PrivateTag._07A1_xx97_:
        case PrivateTag._07A3_xx13_:
        case PrivateTag.TamarOrderStatus:
        case PrivateTag._07A3_xx31_:
        case PrivateTag.TamarStudyAge:
        case PrivateTag.TamarStudyHasMammoCad:
        case PrivateTag._7FDF_xxFF_:
            return VR.SH;
        case PrivateTag.TamarNestedTextLocationX:
        case PrivateTag.TamarNestedTextLocationY:
            return VR.SL;
        case PrivateTag._00E1_xx39_:
        case PrivateTag.ReferenceSequence:
        case PrivateTag._01F3_xx01_:
        case PrivateTag.PSSequence:
        case PrivateTag._07A1_xx18_:
        case PrivateTag.TamarNondicomAnnotationsSequence:
        case PrivateTag._07A3_xx63_:
        case PrivateTag._07A3_xx80_:
        case PrivateTag.TamarGridTokenLoadSequence:
        case PrivateTag.TamarGridTokenQuerySequence:
        case PrivateTag.TamarGridTokenArchiveSequence:
        case PrivateTag.TamarGridTokenLocationSequence:
        case PrivateTag.TamarOriginalNestedElementsSequence:
            return VR.SQ;
        case PrivateTag._01F3_xx02_:
        case PrivateTag._01F3_xx12_:
            return VR.SS;
        case PrivateTag._01E1_xx21_:
        case PrivateTag.SBIVersion:
        case PrivateTag._07A1_xx76_:
        case PrivateTag._07A1_xx8C_:
        case PrivateTag._07A1_xx94_:
        case PrivateTag.TamarProcedureCode:
        case PrivateTag.TamarPatientLocation:
        case PrivateTag.TamarReadingPhysicianLastName:
        case PrivateTag.TamarReadingPhysicianFirstName:
        case PrivateTag._07A3_xx1B_:
        case PrivateTag.TamarSigningPhysicianLastName:
        case PrivateTag.TamarSigningPhysicianFirstName:
        case PrivateTag.TamarSigningPhysicianID:
        case PrivateTag._07A3_xx1F_:
        case PrivateTag._07A3_xx22_:
        case PrivateTag.TamarMiscString5:
        case PrivateTag._07A3xx5C_:
        case PrivateTag._5005_xx10_:
            return VR.ST;
        case PrivateTag._00E1_xx30_:
        case PrivateTag._00E1_xxC2_:
        case PrivateTag._01E1_xx40_:
        case PrivateTag._01F7_xx22_:
            return VR.UI;
        case PrivateTag.NumberOfSeriesInStudy:
        case PrivateTag.NumberOfImagesInStudy:
        case PrivateTag.LastUpdateTime:
        case PrivateTag.LastUpdateDate:
        case PrivateTag._07A1_xx13_:
        case PrivateTag.TamarTranslateFlags:
            return VR.UL;
        case PrivateTag.DataDictionaryVersion:
        case PrivateTag._00E1_xx32_:
        case PrivateTag._00E1_xxEB_:
        case PrivateTag._00E1_xxEC_:
        case PrivateTag.EdgeEnhancementWeight:
        case PrivateTag._01F1_xx30_:
        case PrivateTag.DetectorsLayers:
        case PrivateTag._01F3_xx15_:
        case PrivateTag._01F3_xx23_:
        case PrivateTag._07A1_xx07_:
        case PrivateTag._07A1_xx0C_:
        case PrivateTag._07A1_xx3D_:
        case PrivateTag.TamarSiteID:
        case PrivateTag._07A1_xx56_:
        case PrivateTag._07A3_xx53_:
        case PrivateTag._07A3_xx54_:
        case PrivateTag._07A3_xxB4_:
        case PrivateTag.TamarNestedElementNumber:
        case PrivateTag._5001_xx70_:
        case PrivateTag._5001_xx71_:
            return VR.US;
        }
        return VR.UN;
    }
}
