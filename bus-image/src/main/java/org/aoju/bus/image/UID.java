/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.image;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.ByteKit;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.Sequence;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.data.Value;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * UID信息
 *
 * @author Kimi Liu
 * @version 6.0.0
 * @since JDK 1.8+
 */
public class UID {

    /**
     * Verification SOP Class
     */
    public static final String VerificationSOPClass = "1.2.840.10008.1.1";
    /**
     * Implicit VR Little Endian
     */
    public static final String ImplicitVRLittleEndian = "1.2.840.10008.1.2";
    /**
     * Explicit VR Little Endian
     */
    public static final String ExplicitVRLittleEndian = "1.2.840.10008.1.2.1";
    /**
     * Deflated Explicit VR Little Endian
     */
    public static final String DeflatedExplicitVRLittleEndian = "1.2.840.10008.1.2.1.99";
    /**
     * Explicit VR Big Endian (Retired)
     */
    public static final String ExplicitVRBigEndianRetired = "1.2.840.10008.1.2.2";
    /**
     * JPEG Baseline (Process 1)
     */
    public static final String JPEGBaseline1 = "1.2.840.10008.1.2.4.50";
    /**
     * JPEG Extended (Process 2/4)
     */
    public static final String JPEGExtended24 = "1.2.840.10008.1.2.4.51";
    /**
     * JPEG Extended (Process 3/5) (Retired)
     */
    public static final String JPEGExtended35Retired = "1.2.840.10008.1.2.4.52";
    /**
     * JPEG Spectral Selection, Non-Hierarchical (Process 6/8) (Retired)
     */
    public static final String JPEGSpectralSelectionNonHierarchical68Retired = "1.2.840.10008.1.2.4.53";
    /**
     * JPEG Spectral Selection, Non-Hierarchical (Process 7/9) (Retired)
     */
    public static final String JPEGSpectralSelectionNonHierarchical79Retired = "1.2.840.10008.1.2.4.54";
    /**
     * JPEG Full Progression, Non-Hierarchical (Process 10/12) (Retired)
     */
    public static final String JPEGFullProgressionNonHierarchical1012Retired = "1.2.840.10008.1.2.4.55";
    /**
     * JPEG Full Progression, Non-Hierarchical (Process 11/13) (Retired)
     */
    public static final String JPEGFullProgressionNonHierarchical1113Retired = "1.2.840.10008.1.2.4.56";
    /**
     * JPEG Lossless, Non-Hierarchical (Process 14)
     */
    public static final String JPEGLosslessNonHierarchical14 = "1.2.840.10008.1.2.4.57";
    /**
     * JPEG Lossless, Non-Hierarchical (Process 15) (Retired)
     */
    public static final String JPEGLosslessNonHierarchical15Retired = "1.2.840.10008.1.2.4.58";
    /**
     * JPEG Extended, Hierarchical (Process 16/18) (Retired)
     */
    public static final String JPEGExtendedHierarchical1618Retired = "1.2.840.10008.1.2.4.59";
    /**
     * JPEG Extended, Hierarchical (Process 17/19) (Retired)
     */
    public static final String JPEGExtendedHierarchical1719Retired = "1.2.840.10008.1.2.4.60";
    /**
     * JPEG Spectral Selection, Hierarchical (Process 20/22) (Retired)
     */
    public static final String JPEGSpectralSelectionHierarchical2022Retired = "1.2.840.10008.1.2.4.61";
    /**
     * JPEG Spectral Selection, Hierarchical (Process 21/23) (Retired)
     */
    public static final String JPEGSpectralSelectionHierarchical2123Retired = "1.2.840.10008.1.2.4.62";
    /**
     * JPEG Full Progression, Hierarchical (Process 24/26) (Retired)
     */
    public static final String JPEGFullProgressionHierarchical2426Retired = "1.2.840.10008.1.2.4.63";
    /**
     * JPEG Full Progression, Hierarchical (Process 25/27) (Retired)
     */
    public static final String JPEGFullProgressionHierarchical2527Retired = "1.2.840.10008.1.2.4.64";
    /**
     * JPEG Lossless, Hierarchical (Process 28) (Retired)
     */
    public static final String JPEGLosslessHierarchical28Retired = "1.2.840.10008.1.2.4.65";
    /**
     * JPEG Lossless, Hierarchical (Process 29) (Retired)
     */
    public static final String JPEGLosslessHierarchical29Retired = "1.2.840.10008.1.2.4.66";
    /**
     * JPEG Lossless, Non-Hierarchical, First-Order Prediction (Process 14 [Selection Value 1])
     */
    public static final String JPEGLossless = "1.2.840.10008.1.2.4.70";
    /**
     * JPEG-LS Lossless Image Compression
     */
    public static final String JPEGLSLossless = "1.2.840.10008.1.2.4.80";
    /**
     * JPEG-LS Lossy (Near-Lossless) Image Compression
     */
    public static final String JPEGLSLossyNearLossless = "1.2.840.10008.1.2.4.81";
    /**
     * JPEG 2000 Image Compression (Lossless Only)
     */
    public static final String JPEG2000LosslessOnly = "1.2.840.10008.1.2.4.90";
    /**
     * JPEG 2000 Image Compression
     */
    public static final String JPEG2000 = "1.2.840.10008.1.2.4.91";
    /**
     * JPEG 2000 Part 2 Multi-component Image Compression (Lossless Only)
     */
    public static final String JPEG2000Part2MultiComponentLosslessOnly = "1.2.840.10008.1.2.4.92";
    /**
     * JPEG 2000 Part 2 Multi-component Image Compression
     */
    public static final String JPEG2000Part2MultiComponent = "1.2.840.10008.1.2.4.93";
    /**
     * JPIP Referenced
     */
    public static final String JPIPReferenced = "1.2.840.10008.1.2.4.94";
    /**
     * JPIP Referenced Deflate
     */
    public static final String JPIPReferencedDeflate = "1.2.840.10008.1.2.4.95";
    /**
     * MPEG2 Main Profile / Main Level
     */
    public static final String MPEG2 = "1.2.840.10008.1.2.4.100";
    /**
     * MPEG2 Main Profile / High Level
     */
    public static final String MPEG2MainProfileHighLevel = "1.2.840.10008.1.2.4.101";
    /**
     * MPEG-4 AVC/H.264 High Profile / Level 4.1
     */
    public static final String MPEG4AVCH264HighProfileLevel41 = "1.2.840.10008.1.2.4.102";
    /**
     * MPEG-4 AVC/H.264 BD-compatible High Profile / Level 4.1
     */
    public static final String MPEG4AVCH264BDCompatibleHighProfileLevel41 = "1.2.840.10008.1.2.4.103";
    /**
     * MPEG-4 AVC/H.264 High Profile / Level 4.2 For 2D Video
     */
    public static final String MPEG4AVCH264HighProfileLevel42For2DVideo = "1.2.840.10008.1.2.4.104";
    /**
     * MPEG-4 AVC/H.264 High Profile / Level 4.2 For 3D Video
     */
    public static final String MPEG4AVCH264HighProfileLevel42For3DVideo = "1.2.840.10008.1.2.4.105";
    /**
     * MPEG-4 AVC/H.264 Stereo High Profile / Level 4.2
     */
    public static final String MPEG4AVCH264StereoHighProfileLevel42 = "1.2.840.10008.1.2.4.106";
    /**
     * HEVC/H.265 Main Profile / Level 5.1
     */
    public static final String HEVCH265MainProfileLevel51 = "1.2.840.10008.1.2.4.107";
    /**
     * HEVC/H.265 Main 10 Profile / Level 5.1
     */
    public static final String HEVCH265Main10ProfileLevel51 = "1.2.840.10008.1.2.4.108";
    /**
     * RLE Lossless
     */
    public static final String RLELossless = "1.2.840.10008.1.2.5";
    /**
     * RFC 2557 MIME encapsulation (Retired)
     */
    public static final String RFC2557MIMEEncapsulationRetired = "1.2.840.10008.1.2.6.1";
    /**
     * XML Encoding (Retired)
     */
    public static final String XMLEncodingRetired = "1.2.840.10008.1.2.6.2";
    /**
     * SMPTE ST 2110-20 Uncompressed Progressive Active Video
     */
    public static final String SMPTEST211020UncompressedProgressiveActiveVideo = "1.2.840.10008.1.2.7.1";
    /**
     * SMPTE ST 2110-20 Uncompressed Interlaced Active Video
     */
    public static final String SMPTEST211020UncompressedInterlacedActiveVideo = "1.2.840.10008.1.2.7.2";
    /**
     * SMPTE ST 2110-30 PCM Digital Audio
     */
    public static final String SMPTEST211030PCMDigitalAudio = "1.2.840.10008.1.2.7.3";
    /**
     * Media Storage Directory Storage
     */
    public static final String MediaStorageDirectoryStorage = "1.2.840.10008.1.3.10";
    /**
     * Talairach Brain Atlas Frame of Reference
     */
    public static final String TalairachBrainAtlasFrameOfReference = "1.2.840.10008.1.4.1.1";
    /**
     * SPM2 T1 Frame of Reference
     */
    public static final String SPM2T1FrameOfReference = "1.2.840.10008.1.4.1.2";
    /**
     * SPM2 T2 Frame of Reference
     */
    public static final String SPM2T2FrameOfReference = "1.2.840.10008.1.4.1.3";
    /**
     * SPM2 PD Frame of Reference
     */
    public static final String SPM2PDFrameOfReference = "1.2.840.10008.1.4.1.4";
    /**
     * SPM2 EPI Frame of Reference
     */
    public static final String SPM2EPIFrameOfReference = "1.2.840.10008.1.4.1.5";
    /**
     * SPM2 FIL T1 Frame of Reference
     */
    public static final String SPM2FILT1FrameOfReference = "1.2.840.10008.1.4.1.6";
    /**
     * SPM2 PET Frame of Reference
     */
    public static final String SPM2PETFrameOfReference = "1.2.840.10008.1.4.1.7";
    /**
     * SPM2 TRANSM Frame of Reference
     */
    public static final String SPM2TRANSMFrameOfReference = "1.2.840.10008.1.4.1.8";
    /**
     * SPM2 SPECT Frame of Reference
     */
    public static final String SPM2SPECTFrameOfReference = "1.2.840.10008.1.4.1.9";
    /**
     * SPM2 GRAY Frame of Reference
     */
    public static final String SPM2GRAYFrameOfReference = "1.2.840.10008.1.4.1.10";
    /**
     * SPM2 WHITE Frame of Reference
     */
    public static final String SPM2WHITEFrameOfReference = "1.2.840.10008.1.4.1.11";
    /**
     * SPM2 CSF Frame of Reference
     */
    public static final String SPM2CSFFrameOfReference = "1.2.840.10008.1.4.1.12";
    /**
     * SPM2 BRAINMASK Frame of Reference
     */
    public static final String SPM2BRAINMASKFrameOfReference = "1.2.840.10008.1.4.1.13";
    /**
     * SPM2 AVG305T1 Frame of Reference
     */
    public static final String SPM2AVG305T1FrameOfReference = "1.2.840.10008.1.4.1.14";
    /**
     * SPM2 AVG152T1 Frame of Reference
     */
    public static final String SPM2AVG152T1FrameOfReference = "1.2.840.10008.1.4.1.15";
    /**
     * SPM2 AVG152T2 Frame of Reference
     */
    public static final String SPM2AVG152T2FrameOfReference = "1.2.840.10008.1.4.1.16";
    /**
     * SPM2 AVG152PD Frame of Reference
     */
    public static final String SPM2AVG152PDFrameOfReference = "1.2.840.10008.1.4.1.17";
    /**
     * SPM2 SINGLESUBJT1 Frame of Reference
     */
    public static final String SPM2SINGLESUBJT1FrameOfReference = "1.2.840.10008.1.4.1.18";
    /**
     * ICBM 452 T1 Frame of Reference
     */
    public static final String ICBM452T1FrameOfReference = "1.2.840.10008.1.4.2.1";
    /**
     * ICBM Single Subject MRI Frame of Reference
     */
    public static final String ICBMSingleSubjectMRIFrameOfReference = "1.2.840.10008.1.4.2.2";
    /**
     * IEC 61217 Fixed Coordinate System Frame of Reference
     */
    public static final String IEC61217FixedCoordinateSystemFrameOfReference = "1.2.840.10008.1.4.3.1";
    /**
     * Hot Iron Color Palette SOP Instance
     */
    public static final String HotIronColorPaletteSOPInstance = "1.2.840.10008.1.5.1";
    /**
     * PET Color Palette SOP Instance
     */
    public static final String PETColorPaletteSOPInstance = "1.2.840.10008.1.5.2";
    /**
     * Hot Metal Blue Color Palette SOP Instance
     */
    public static final String HotMetalBlueColorPaletteSOPInstance = "1.2.840.10008.1.5.3";
    /**
     * PET 20 Step Color Palette SOP Instance
     */
    public static final String PET20StepColorPaletteSOPInstance = "1.2.840.10008.1.5.4";
    /**
     * Spring Color Palette SOP Instance
     */
    public static final String SpringColorPaletteSOPInstance = "1.2.840.10008.1.5.5";
    /**
     * Summer Color Palette SOP Instance
     */
    public static final String SummerColorPaletteSOPInstance = "1.2.840.10008.1.5.6";
    /**
     * Fall Color Palette SOP Instance
     */
    public static final String FallColorPaletteSOPInstance = "1.2.840.10008.1.5.7";
    /**
     * Winter Color Palette SOP Instance
     */
    public static final String WinterColorPaletteSOPInstance = "1.2.840.10008.1.5.8";
    /**
     * Basic Study Content Notification SOP Class (Retired)
     */
    public static final String BasicStudyContentNotificationSOPClassRetired = "1.2.840.10008.1.9";
    /**
     * Papyrus 3 Implicit VR Little Endian (Retired)
     */
    public static final String Papyrus3ImplicitVRLittleEndianRetired = "1.2.840.10008.1.20";
    /**
     * Storage Commitment Push Model SOP Class
     */
    public static final String StorageCommitmentPushModelSOPClass = "1.2.840.10008.1.20.1";
    /**
     * Storage Commitment Push Model SOP Instance
     */
    public static final String StorageCommitmentPushModelSOPInstance = "1.2.840.10008.1.20.1.1";
    /**
     * Storage Commitment Pull Model SOP Class (Retired)
     */
    public static final String StorageCommitmentPullModelSOPClassRetired = "1.2.840.10008.1.20.2";
    /**
     * Storage Commitment Pull Model SOP Instance (Retired)
     */
    public static final String StorageCommitmentPullModelSOPInstanceRetired = "1.2.840.10008.1.20.2.1";
    /**
     * Procedural Event Logging SOP Class
     */
    public static final String ProceduralEventLoggingSOPClass = "1.2.840.10008.1.40";
    /**
     * Procedural Event Logging SOP Instance
     */
    public static final String ProceduralEventLoggingSOPInstance = "1.2.840.10008.1.40.1";
    /**
     * Substance Administration Logging SOP Class
     */
    public static final String SubstanceAdministrationLoggingSOPClass = "1.2.840.10008.1.42";
    /**
     * Substance Administration Logging SOP Instance
     */
    public static final String SubstanceAdministrationLoggingSOPInstance = "1.2.840.10008.1.42.1";
    /**
     * DICOM UID Registry
     */
    public static final String DICOMUIDRegistry = "1.2.840.10008.2.6.1";
    /**
     * DICOM Controlled Terminology
     */
    public static final String DICOMControlledTerminology = "1.2.840.10008.2.16.4";
    /**
     * Adult Mouse Anatomy Ontology
     */
    public static final String AdultMouseAnatomyOntology = "1.2.840.10008.2.16.5";
    /**
     * Uberon Ontology
     */
    public static final String UberonOntology = "1.2.840.10008.2.16.6";
    /**
     * Integrated Taxonomic Information System (ITIS) Taxonomic Serial Number (TSN)
     */
    public static final String IntegratedTaxonomicInformationSystemITISTaxonomicSerialNumberTSN = "1.2.840.10008.2.16.7";
    /**
     * Mouse Genome Initiative (MGI)
     */
    public static final String MouseGenomeInitiativeMGI = "1.2.840.10008.2.16.8";
    /**
     * PubChem Compound CID
     */
    public static final String PubChemCompoundCID = "1.2.840.10008.2.16.9";
    /**
     * ICD-11
     */
    public static final String ICD11 = "1.2.840.10008.2.16.10";
    /**
     * New York University Melanoma Clinical Cooperative Group
     */
    public static final String NewYorkUniversityMelanomaClinicalCooperativeGroup = "1.2.840.10008.2.16.11";
    /**
     * Mayo Clinic Non-radiological Images Specific Body Structure Anatomical Surface Region Guide
     */
    public static final String MayoClinicNonRadiologicalImagesSpecificBodyStructureAnatomicalSurfaceRegionGuide = "1.2.840.10008.2.16.12";
    /**
     * Image Biomarker Standardisation Initiative
     */
    public static final String ImageBiomarkerStandardisationInitiative = "1.2.840.10008.2.16.13";
    /**
     * Radiomics Ontology
     */
    public static final String RadiomicsOntology = "1.2.840.10008.2.16.14";
    /**
     * DICOM Application Context Name
     */
    public static final String DICOMApplicationContextName = "1.2.840.10008.3.1.1.1";
    /**
     * Detached Patient Management SOP Class (Retired)
     */
    public static final String DetachedPatientManagementSOPClassRetired = "1.2.840.10008.3.1.2.1.1";
    /**
     * Detached Patient Management Meta SOP Class (Retired)
     */
    public static final String DetachedPatientManagementMetaSOPClassRetired = "1.2.840.10008.3.1.2.1.4";
    /**
     * Detached Visit Management SOP Class (Retired)
     */
    public static final String DetachedVisitManagementSOPClassRetired = "1.2.840.10008.3.1.2.2.1";
    /**
     * Detached Study Management SOP Class (Retired)
     */
    public static final String DetachedStudyManagementSOPClassRetired = "1.2.840.10008.3.1.2.3.1";
    /**
     * Study Component Management SOP Class (Retired)
     */
    public static final String StudyComponentManagementSOPClassRetired = "1.2.840.10008.3.1.2.3.2";
    /**
     * Modality Performed Procedure Step SOP Class
     */
    public static final String ModalityPerformedProcedureStepSOPClass = "1.2.840.10008.3.1.2.3.3";
    /**
     * Modality Performed Procedure Step Retrieve SOP Class
     */
    public static final String ModalityPerformedProcedureStepRetrieveSOPClass = "1.2.840.10008.3.1.2.3.4";
    /**
     * Modality Performed Procedure Step Notification SOP Class
     */
    public static final String ModalityPerformedProcedureStepNotificationSOPClass = "1.2.840.10008.3.1.2.3.5";
    /**
     * Detached Results Management SOP Class (Retired)
     */
    public static final String DetachedResultsManagementSOPClassRetired = "1.2.840.10008.3.1.2.5.1";
    /**
     * Detached Results Management Meta SOP Class (Retired)
     */
    public static final String DetachedResultsManagementMetaSOPClassRetired = "1.2.840.10008.3.1.2.5.4";
    /**
     * Detached Study Management Meta SOP Class (Retired)
     */
    public static final String DetachedStudyManagementMetaSOPClassRetired = "1.2.840.10008.3.1.2.5.5";
    /**
     * Detached Interpretation Management SOP Class (Retired)
     */
    public static final String DetachedInterpretationManagementSOPClassRetired = "1.2.840.10008.3.1.2.6.1";
    /**
     * Storage Service Class
     */
    public static final String StorageServiceClass = "1.2.840.10008.4.2";
    /**
     * Basic Film Session SOP Class
     */
    public static final String BasicFilmSessionSOPClass = "1.2.840.10008.5.1.1.1";
    /**
     * Basic Film Box SOP Class
     */
    public static final String BasicFilmBoxSOPClass = "1.2.840.10008.5.1.1.2";
    /**
     * Basic Grayscale Image Box SOP Class
     */
    public static final String BasicGrayscaleImageBoxSOPClass = "1.2.840.10008.5.1.1.4";
    /**
     * Basic Color Image Box SOP Class
     */
    public static final String BasicColorImageBoxSOPClass = "1.2.840.10008.5.1.1.4.1";
    /**
     * Referenced Image Box SOP Class (Retired)
     */
    public static final String ReferencedImageBoxSOPClassRetired = "1.2.840.10008.5.1.1.4.2";
    /**
     * Basic Grayscale Print Management Meta SOP Class
     */
    public static final String BasicGrayscalePrintManagementMetaSOPClass = "1.2.840.10008.5.1.1.9";
    /**
     * Referenced Grayscale Print Management Meta SOP Class (Retired)
     */
    public static final String ReferencedGrayscalePrintManagementMetaSOPClassRetired = "1.2.840.10008.5.1.1.9.1";
    /**
     * Print Job SOP Class
     */
    public static final String PrintJobSOPClass = "1.2.840.10008.5.1.1.14";
    /**
     * Basic Annotation Box SOP Class
     */
    public static final String BasicAnnotationBoxSOPClass = "1.2.840.10008.5.1.1.15";
    /**
     * Printer SOP Class
     */
    public static final String PrinterSOPClass = "1.2.840.10008.5.1.1.16";
    /**
     * Printer Configuration Retrieval SOP Class
     */
    public static final String PrinterConfigurationRetrievalSOPClass = "1.2.840.10008.5.1.1.16.376";
    /**
     * Printer SOP Instance
     */
    public static final String PrinterSOPInstance = "1.2.840.10008.5.1.1.17";
    /**
     * Printer Configuration Retrieval SOP Instance
     */
    public static final String PrinterConfigurationRetrievalSOPInstance = "1.2.840.10008.5.1.1.17.376";
    /**
     * Basic Color Print Management Meta SOP Class
     */
    public static final String BasicColorPrintManagementMetaSOPClass = "1.2.840.10008.5.1.1.18";
    /**
     * Referenced Color Print Management Meta SOP Class (Retired)
     */
    public static final String ReferencedColorPrintManagementMetaSOPClassRetired = "1.2.840.10008.5.1.1.18.1";
    /**
     * VOI LUT Box SOP Class
     */
    public static final String VOILUTBoxSOPClass = "1.2.840.10008.5.1.1.22";
    /**
     * Presentation LUT SOP Class
     */
    public static final String PresentationLUTSOPClass = "1.2.840.10008.5.1.1.23";
    /**
     * Image Overlay Box SOP Class (Retired)
     */
    public static final String ImageOverlayBoxSOPClassRetired = "1.2.840.10008.5.1.1.24";
    /**
     * Basic Print Image Overlay Box SOP Class (Retired)
     */
    public static final String BasicPrintImageOverlayBoxSOPClassRetired = "1.2.840.10008.5.1.1.24.1";
    /**
     * Print Queue SOP Instance (Retired)
     */
    public static final String PrintQueueSOPInstanceRetired = "1.2.840.10008.5.1.1.25";
    /**
     * Print Queue Management SOP Class (Retired)
     */
    public static final String PrintQueueManagementSOPClassRetired = "1.2.840.10008.5.1.1.26";
    /**
     * Stored Print Storage SOP Class (Retired)
     */
    public static final String StoredPrintStorageSOPClassRetired = "1.2.840.10008.5.1.1.27";
    /**
     * Hardcopy Grayscale Image Storage SOP Class (Retired)
     */
    public static final String HardcopyGrayscaleImageStorageSOPClassRetired = "1.2.840.10008.5.1.1.29";
    /**
     * Hardcopy Color Image Storage SOP Class (Retired)
     */
    public static final String HardcopyColorImageStorageSOPClassRetired = "1.2.840.10008.5.1.1.30";
    /**
     * Pull Print Request SOP Class (Retired)
     */
    public static final String PullPrintRequestSOPClassRetired = "1.2.840.10008.5.1.1.31";
    /**
     * Pull Stored Print Management Meta SOP Class (Retired)
     */
    public static final String PullStoredPrintManagementMetaSOPClassRetired = "1.2.840.10008.5.1.1.32";
    /**
     * Media Creation Management SOP Class UID
     */
    public static final String MediaCreationManagementSOPClassUID = "1.2.840.10008.5.1.1.33";
    /**
     * Display System SOP Class
     */
    public static final String DisplaySystemSOPClass = "1.2.840.10008.5.1.1.40";
    /**
     * Display System SOP Instance
     */
    public static final String DisplaySystemSOPInstance = "1.2.840.10008.5.1.1.40.1";
    /**
     * Computed Radiography Image Storage
     */
    public static final String ComputedRadiographyImageStorage = "1.2.840.10008.5.1.4.1.1.1";
    /**
     * Digital X-Ray Image Storage - For Presentation
     */
    public static final String DigitalXRayImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.1.1";
    /**
     * Digital X-Ray Image Storage - For Processing
     */
    public static final String DigitalXRayImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.1.1.1";
    /**
     * Digital Mammography X-Ray Image Storage - For Presentation
     */
    public static final String DigitalMammographyXRayImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.1.2";
    /**
     * Digital Mammography X-Ray Image Storage - For Processing
     */
    public static final String DigitalMammographyXRayImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.1.2.1";
    /**
     * Digital Intra-Oral X-Ray Image Storage - For Presentation
     */
    public static final String DigitalIntraOralXRayImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.1.3";
    /**
     * Digital Intra-Oral X-Ray Image Storage - For Processing
     */
    public static final String DigitalIntraOralXRayImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.1.3.1";
    /**
     * CT Image Storage
     */
    public static final String CTImageStorage = "1.2.840.10008.5.1.4.1.1.2";
    /**
     * Enhanced CT Image Storage
     */
    public static final String EnhancedCTImageStorage = "1.2.840.10008.5.1.4.1.1.2.1";
    /**
     * Legacy Converted Enhanced CT Image Storage
     */
    public static final String LegacyConvertedEnhancedCTImageStorage = "1.2.840.10008.5.1.4.1.1.2.2";
    /**
     * Ultrasound Multi-frame Image Storage (Retired)
     */
    public static final String UltrasoundMultiFrameImageStorageRetired = "1.2.840.10008.5.1.4.1.1.3";
    /**
     * Ultrasound Multi-frame Image Storage
     */
    public static final String UltrasoundMultiFrameImageStorage = "1.2.840.10008.5.1.4.1.1.3.1";
    /**
     * MR Image Storage
     */
    public static final String MRImageStorage = "1.2.840.10008.5.1.4.1.1.4";
    /**
     * Enhanced MR Image Storage
     */
    public static final String EnhancedMRImageStorage = "1.2.840.10008.5.1.4.1.1.4.1";
    /**
     * MR Spectroscopy Storage
     */
    public static final String MRSpectroscopyStorage = "1.2.840.10008.5.1.4.1.1.4.2";
    /**
     * Enhanced MR Color Image Storage
     */
    public static final String EnhancedMRColorImageStorage = "1.2.840.10008.5.1.4.1.1.4.3";
    /**
     * Legacy Converted Enhanced MR Image Storage
     */
    public static final String LegacyConvertedEnhancedMRImageStorage = "1.2.840.10008.5.1.4.1.1.4.4";
    /**
     * Nuclear Medicine Image Storage (Retired)
     */
    public static final String NuclearMedicineImageStorageRetired = "1.2.840.10008.5.1.4.1.1.5";
    /**
     * Ultrasound Image Storage (Retired)
     */
    public static final String UltrasoundImageStorageRetired = "1.2.840.10008.5.1.4.1.1.6";
    /**
     * Ultrasound Image Storage
     */
    public static final String UltrasoundImageStorage = "1.2.840.10008.5.1.4.1.1.6.1";
    /**
     * Enhanced US Volume Storage
     */
    public static final String EnhancedUSVolumeStorage = "1.2.840.10008.5.1.4.1.1.6.2";
    /**
     * Secondary Capture Image Storage
     */
    public static final String SecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7";
    /**
     * Multi-frame Single Bit Secondary Capture Image Storage
     */
    public static final String MultiFrameSingleBitSecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7.1";
    /**
     * Multi-frame Grayscale Byte Secondary Capture Image Storage
     */
    public static final String MultiFrameGrayscaleByteSecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7.2";
    /**
     * Multi-frame Grayscale Word Secondary Capture Image Storage
     */
    public static final String MultiFrameGrayscaleWordSecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7.3";
    /**
     * Multi-frame True Color Secondary Capture Image Storage
     */
    public static final String MultiFrameTrueColorSecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7.4";
    /**
     * Standalone Overlay Storage (Retired)
     */
    public static final String StandaloneOverlayStorageRetired = "1.2.840.10008.5.1.4.1.1.8";
    /**
     * Standalone Curve Storage (Retired)
     */
    public static final String StandaloneCurveStorageRetired = "1.2.840.10008.5.1.4.1.1.9";
    /**
     * Waveform Storage - Trial (Retired)
     */
    public static final String WaveformStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.9.1";
    /**
     * 12-lead ECG Waveform Storage
     */
    public static final String TwelveLeadECGWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.1.1";
    /**
     * General ECG Waveform Storage
     */
    public static final String GeneralECGWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.1.2";
    /**
     * Ambulatory ECG Waveform Storage
     */
    public static final String AmbulatoryECGWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.1.3";
    /**
     * Hemodynamic Waveform Storage
     */
    public static final String HemodynamicWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.2.1";
    /**
     * Cardiac Electrophysiology Waveform Storage
     */
    public static final String CardiacElectrophysiologyWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.3.1";
    /**
     * Basic Voice Audio Waveform Storage
     */
    public static final String BasicVoiceAudioWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.4.1";
    /**
     * General Audio Waveform Storage
     */
    public static final String GeneralAudioWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.4.2";
    /**
     * Arterial Pulse Waveform Storage
     */
    public static final String ArterialPulseWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.5.1";
    /**
     * Respiratory Waveform Storage
     */
    public static final String RespiratoryWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.6.1";
    /**
     * Standalone Modality LUT Storage (Retired)
     */
    public static final String StandaloneModalityLUTStorageRetired = "1.2.840.10008.5.1.4.1.1.10";
    /**
     * Standalone VOI LUT Storage (Retired)
     */
    public static final String StandaloneVOILUTStorageRetired = "1.2.840.10008.5.1.4.1.1.11";
    /**
     * Grayscale Softcopy Presentation State Storage
     */
    public static final String GrayscaleSoftcopyPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.1";
    /**
     * Color Softcopy Presentation State Storage
     */
    public static final String ColorSoftcopyPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.2";
    /**
     * Pseudo-Color Softcopy Presentation State Storage
     */
    public static final String PseudoColorSoftcopyPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.3";
    /**
     * Blending Softcopy Presentation State Storage
     */
    public static final String BlendingSoftcopyPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.4";
    /**
     * XA/XRF Grayscale Softcopy Presentation State Storage
     */
    public static final String XAXRFGrayscaleSoftcopyPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.5";
    /**
     * Grayscale Planar MPR Volumetric Presentation State Storage
     */
    public static final String GrayscalePlanarMPRVolumetricPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.6";
    /**
     * Compositing Planar MPR Volumetric Presentation State Storage
     */
    public static final String CompositingPlanarMPRVolumetricPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.7";
    /**
     * Advanced Blending Presentation State Storage
     */
    public static final String AdvancedBlendingPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.8";
    /**
     * Volume Rendering Volumetric Presentation State Storage
     */
    public static final String VolumeRenderingVolumetricPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.9";
    /**
     * Segmented Volume Rendering Volumetric Presentation State Storage
     */
    public static final String SegmentedVolumeRenderingVolumetricPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.10";
    /**
     * Multiple Volume Rendering Volumetric Presentation State Storage
     */
    public static final String MultipleVolumeRenderingVolumetricPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.11";
    /**
     * X-Ray Angiographic Image Storage
     */
    public static final String XRayAngiographicImageStorage = "1.2.840.10008.5.1.4.1.1.12.1";
    /**
     * Enhanced XA Image Storage
     */
    public static final String EnhancedXAImageStorage = "1.2.840.10008.5.1.4.1.1.12.1.1";
    /**
     * X-Ray Radiofluoroscopic Image Storage
     */
    public static final String XRayRadiofluoroscopicImageStorage = "1.2.840.10008.5.1.4.1.1.12.2";
    /**
     * Enhanced XRF Image Storage
     */
    public static final String EnhancedXRFImageStorage = "1.2.840.10008.5.1.4.1.1.12.2.1";
    /**
     * X-Ray Angiographic Bi-Plane Image Storage (Retired)
     */
    public static final String XRayAngiographicBiPlaneImageStorageRetired = "1.2.840.10008.5.1.4.1.1.12.3";
    /**
     * Zeiss OPT File (Retired)
     */
    public static final String ZeissOPTFileRetired = "1.2.840.10008.5.1.4.1.1.12.77";
    /**
     * X-Ray 3D Angiographic Image Storage
     */
    public static final String XRay3DAngiographicImageStorage = "1.2.840.10008.5.1.4.1.1.13.1.1";
    /**
     * X-Ray 3D Craniofacial Image Storage
     */
    public static final String XRay3DCraniofacialImageStorage = "1.2.840.10008.5.1.4.1.1.13.1.2";
    /**
     * Breast Tomosynthesis Image Storage
     */
    public static final String BreastTomosynthesisImageStorage = "1.2.840.10008.5.1.4.1.1.13.1.3";
    /**
     * Breast Projection X-Ray Image Storage - For Presentation
     */
    public static final String BreastProjectionXRayImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.13.1.4";
    /**
     * Breast Projection X-Ray Image Storage - For Processing
     */
    public static final String BreastProjectionXRayImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.13.1.5";
    /**
     * Intravascular Optical Coherence Tomography Image Storage - For Presentation
     */
    public static final String IntravascularOpticalCoherenceTomographyImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.14.1";
    /**
     * Intravascular Optical Coherence Tomography Image Storage - For Processing
     */
    public static final String IntravascularOpticalCoherenceTomographyImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.14.2";
    /**
     * Nuclear Medicine Image Storage
     */
    public static final String NuclearMedicineImageStorage = "1.2.840.10008.5.1.4.1.1.20";
    /**
     * Parametric Map Storage
     */
    public static final String ParametricMapStorage = "1.2.840.10008.5.1.4.1.1.30";
    /**
     * MR Image Storage Zero Padded (Retired)
     */
    public static final String MRImageStorageZeroPaddedRetired = "1.2.840.10008.5.1.4.1.1.40";
    /**
     * Raw Data Storage
     */
    public static final String RawDataStorage = "1.2.840.10008.5.1.4.1.1.66";
    /**
     * Spatial Registration Storage
     */
    public static final String SpatialRegistrationStorage = "1.2.840.10008.5.1.4.1.1.66.1";
    /**
     * Spatial Fiducials Storage
     */
    public static final String SpatialFiducialsStorage = "1.2.840.10008.5.1.4.1.1.66.2";
    /**
     * Deformable Spatial Registration Storage
     */
    public static final String DeformableSpatialRegistrationStorage = "1.2.840.10008.5.1.4.1.1.66.3";
    /**
     * Segmentation Storage
     */
    public static final String SegmentationStorage = "1.2.840.10008.5.1.4.1.1.66.4";
    /**
     * Surface Segmentation Storage
     */
    public static final String SurfaceSegmentationStorage = "1.2.840.10008.5.1.4.1.1.66.5";
    /**
     * Tractography Results Storage
     */
    public static final String TractographyResultsStorage = "1.2.840.10008.5.1.4.1.1.66.6";
    /**
     * Real World Value Mapping Storage
     */
    public static final String RealWorldValueMappingStorage = "1.2.840.10008.5.1.4.1.1.67";
    /**
     * Surface Scan Mesh Storage
     */
    public static final String SurfaceScanMeshStorage = "1.2.840.10008.5.1.4.1.1.68.1";
    /**
     * Surface Scan Point Cloud Storage
     */
    public static final String SurfaceScanPointCloudStorage = "1.2.840.10008.5.1.4.1.1.68.2";
    /**
     * VL Image Storage - Trial (Retired)
     */
    public static final String VLImageStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.77.1";
    /**
     * VL Multi-frame Image Storage - Trial (Retired)
     */
    public static final String VLMultiFrameImageStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.77.2";
    /**
     * VL Endoscopic Image Storage
     */
    public static final String VLEndoscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.1";
    /**
     * Video Endoscopic Image Storage
     */
    public static final String VideoEndoscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.1.1";
    /**
     * VL Microscopic Image Storage
     */
    public static final String VLMicroscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.2";
    /**
     * Video Microscopic Image Storage
     */
    public static final String VideoMicroscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.2.1";
    /**
     * VL Slide-Coordinates Microscopic Image Storage
     */
    public static final String VLSlideCoordinatesMicroscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.3";
    /**
     * VL Photographic Image Storage
     */
    public static final String VLPhotographicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.4";
    /**
     * Video Photographic Image Storage
     */
    public static final String VideoPhotographicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.4.1";
    /**
     * Ophthalmic Photography 8 Bit Image Storage
     */
    public static final String OphthalmicPhotography8BitImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.1";
    /**
     * Ophthalmic Photography 16 Bit Image Storage
     */
    public static final String OphthalmicPhotography16BitImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.2";
    /**
     * Stereometric Relationship Storage
     */
    public static final String StereometricRelationshipStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.3";
    /**
     * Ophthalmic Tomography Image Storage
     */
    public static final String OphthalmicTomographyImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.4";
    /**
     * Wide Field Ophthalmic Photography Stereographic Projection Image Storage
     */
    public static final String WideFieldOphthalmicPhotographyStereographicProjectionImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.5";
    /**
     * Wide Field Ophthalmic Photography 3D Coordinates Image Storage
     */
    public static final String WideFieldOphthalmicPhotography3DCoordinatesImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.6";
    /**
     * Ophthalmic Optical Coherence Tomography En Face Image Storage
     */
    public static final String OphthalmicOpticalCoherenceTomographyEnFaceImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.7";
    /**
     * Ophthalmic Optical Coherence Tomography B-scan Volume Analysis Storage
     */
    public static final String OphthalmicOpticalCoherenceTomographyBScanVolumeAnalysisStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.8";
    /**
     * VL Whole Slide Microscopy Image Storage
     */
    public static final String VLWholeSlideMicroscopyImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.6";
    /**
     * Lensometry Measurements Storage
     */
    public static final String LensometryMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.1";
    /**
     * Autorefraction Measurements Storage
     */
    public static final String AutorefractionMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.2";
    /**
     * Keratometry Measurements Storage
     */
    public static final String KeratometryMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.3";
    /**
     * Subjective Refraction Measurements Storage
     */
    public static final String SubjectiveRefractionMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.4";
    /**
     * Visual Acuity Measurements Storage
     */
    public static final String VisualAcuityMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.5";
    /**
     * Spectacle Prescription Report Storage
     */
    public static final String SpectaclePrescriptionReportStorage = "1.2.840.10008.5.1.4.1.1.78.6";
    /**
     * Ophthalmic Axial Measurements Storage
     */
    public static final String OphthalmicAxialMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.7";
    /**
     * Intraocular Lens Calculations Storage
     */
    public static final String IntraocularLensCalculationsStorage = "1.2.840.10008.5.1.4.1.1.78.8";
    /**
     * Macular Grid Thickness and Volume Report Storage
     */
    public static final String MacularGridThicknessAndVolumeReportStorage = "1.2.840.10008.5.1.4.1.1.79.1";
    /**
     * Ophthalmic Visual Field Static Perimetry Measurements Storage
     */
    public static final String OphthalmicVisualFieldStaticPerimetryMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.80.1";
    /**
     * Ophthalmic Thickness Map Storage
     */
    public static final String OphthalmicThicknessMapStorage = "1.2.840.10008.5.1.4.1.1.81.1";
    /**
     * Corneal Topography Map Storage
     */
    public static final String CornealTopographyMapStorage = "1.2.840.10008.5.1.4.1.1.82.1";
    /**
     * Text SR Storage - Trial (Retired)
     */
    public static final String TextSRStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.88.1";
    /**
     * Audio SR Storage - Trial (Retired)
     */
    public static final String AudioSRStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.88.2";
    /**
     * Detail SR Storage - Trial (Retired)
     */
    public static final String DetailSRStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.88.3";
    /**
     * Comprehensive SR Storage - Trial (Retired)
     */
    public static final String ComprehensiveSRStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.88.4";
    /**
     * Basic Text SR Storage
     */
    public static final String BasicTextSRStorage = "1.2.840.10008.5.1.4.1.1.88.11";
    /**
     * Enhanced SR Storage
     */
    public static final String EnhancedSRStorage = "1.2.840.10008.5.1.4.1.1.88.22";
    /**
     * Comprehensive SR Storage
     */
    public static final String ComprehensiveSRStorage = "1.2.840.10008.5.1.4.1.1.88.33";
    /**
     * Comprehensive 3D SR Storage
     */
    public static final String Comprehensive3DSRStorage = "1.2.840.10008.5.1.4.1.1.88.34";
    /**
     * Extensible SR Storage
     */
    public static final String ExtensibleSRStorage = "1.2.840.10008.5.1.4.1.1.88.35";
    /**
     * Procedure Log Storage
     */
    public static final String ProcedureLogStorage = "1.2.840.10008.5.1.4.1.1.88.40";
    /**
     * Mammography CAD SR Storage
     */
    public static final String MammographyCADSRStorage = "1.2.840.10008.5.1.4.1.1.88.50";
    /**
     * Key Object Selection Document Storage
     */
    public static final String KeyObjectSelectionDocumentStorage = "1.2.840.10008.5.1.4.1.1.88.59";
    /**
     * Chest CAD SR Storage
     */
    public static final String ChestCADSRStorage = "1.2.840.10008.5.1.4.1.1.88.65";
    /**
     * X-Ray Radiation Dose SR Storage
     */
    public static final String XRayRadiationDoseSRStorage = "1.2.840.10008.5.1.4.1.1.88.67";
    /**
     * Radiopharmaceutical Radiation Dose SR Storage
     */
    public static final String RadiopharmaceuticalRadiationDoseSRStorage = "1.2.840.10008.5.1.4.1.1.88.68";
    /**
     * Colon CAD SR Storage
     */
    public static final String ColonCADSRStorage = "1.2.840.10008.5.1.4.1.1.88.69";
    /**
     * Implantation Plan SR Storage
     */
    public static final String ImplantationPlanSRStorage = "1.2.840.10008.5.1.4.1.1.88.70";
    /**
     * Acquisition Context SR Storage
     */
    public static final String AcquisitionContextSRStorage = "1.2.840.10008.5.1.4.1.1.88.71";
    /**
     * Simplified Adult Echo SR Storage
     */
    public static final String SimplifiedAdultEchoSRStorage = "1.2.840.10008.5.1.4.1.1.88.72";
    /**
     * Patient Radiation Dose SR Storage
     */
    public static final String PatientRadiationDoseSRStorage = "1.2.840.10008.5.1.4.1.1.88.73";
    /**
     * Planned Imaging Agent Administration SR Storage
     */
    public static final String PlannedImagingAgentAdministrationSRStorage = "1.2.840.10008.5.1.4.1.1.88.74";
    /**
     * Performed Imaging Agent Administration SR Storage
     */
    public static final String PerformedImagingAgentAdministrationSRStorage = "1.2.840.10008.5.1.4.1.1.88.75";
    /**
     * Content Assessment Results Storage
     */
    public static final String ContentAssessmentResultsStorage = "1.2.840.10008.5.1.4.1.1.90.1";
    /**
     * Encapsulated PDF Storage
     */
    public static final String EncapsulatedPDFStorage = "1.2.840.10008.5.1.4.1.1.104.1";
    /**
     * Encapsulated CDA Storage
     */
    public static final String EncapsulatedCDAStorage = "1.2.840.10008.5.1.4.1.1.104.2";
    /**
     * Encapsulated STL Storage
     */
    public static final String EncapsulatedSTLStorage = "1.2.840.10008.5.1.4.1.1.104.3";
    /**
     * Positron Emission Tomography Image Storage
     */
    public static final String PositronEmissionTomographyImageStorage = "1.2.840.10008.5.1.4.1.1.128";
    /**
     * Legacy Converted Enhanced PET Image Storage
     */
    public static final String LegacyConvertedEnhancedPETImageStorage = "1.2.840.10008.5.1.4.1.1.128.1";
    /**
     * Standalone PET Curve Storage (Retired)
     */
    public static final String StandalonePETCurveStorageRetired = "1.2.840.10008.5.1.4.1.1.129";
    /**
     * Enhanced PET Image Storage
     */
    public static final String EnhancedPETImageStorage = "1.2.840.10008.5.1.4.1.1.130";
    /**
     * Basic Structured Display Storage
     */
    public static final String BasicStructuredDisplayStorage = "1.2.840.10008.5.1.4.1.1.131";
    /**
     * CT Defined Procedure Protocol Storage
     */
    public static final String CTDefinedProcedureProtocolStorage = "1.2.840.10008.5.1.4.1.1.200.1";
    /**
     * CT Performed Procedure Protocol Storage
     */
    public static final String CTPerformedProcedureProtocolStorage = "1.2.840.10008.5.1.4.1.1.200.2";
    /**
     * Protocol Approval Storage
     */
    public static final String ProtocolApprovalStorage = "1.2.840.10008.5.1.4.1.1.200.3";
    /**
     * Protocol Approval Information Model - FIND
     */
    public static final String ProtocolApprovalInformationModelFIND = "1.2.840.10008.5.1.4.1.1.200.4";
    /**
     * Protocol Approval Information Model - MOVE
     */
    public static final String ProtocolApprovalInformationModelMOVE = "1.2.840.10008.5.1.4.1.1.200.5";
    /**
     * Protocol Approval Information Model - GET
     */
    public static final String ProtocolApprovalInformationModelGET = "1.2.840.10008.5.1.4.1.1.200.6";
    /**
     * RT Image Storage
     */
    public static final String RTImageStorage = "1.2.840.10008.5.1.4.1.1.481.1";
    /**
     * RT Dose Storage
     */
    public static final String RTDoseStorage = "1.2.840.10008.5.1.4.1.1.481.2";
    /**
     * RT Structure Set Storage
     */
    public static final String RTStructureSetStorage = "1.2.840.10008.5.1.4.1.1.481.3";
    /**
     * RT Beams Treatment Record Storage
     */
    public static final String RTBeamsTreatmentRecordStorage = "1.2.840.10008.5.1.4.1.1.481.4";
    /**
     * RT Plan Storage
     */
    public static final String RTPlanStorage = "1.2.840.10008.5.1.4.1.1.481.5";
    /**
     * RT Brachy Treatment Record Storage
     */
    public static final String RTBrachyTreatmentRecordStorage = "1.2.840.10008.5.1.4.1.1.481.6";
    /**
     * RT Treatment Summary Record Storage
     */
    public static final String RTTreatmentSummaryRecordStorage = "1.2.840.10008.5.1.4.1.1.481.7";
    /**
     * RT Ion Plan Storage
     */
    public static final String RTIonPlanStorage = "1.2.840.10008.5.1.4.1.1.481.8";
    /**
     * RT Ion Beams Treatment Record Storage
     */
    public static final String RTIonBeamsTreatmentRecordStorage = "1.2.840.10008.5.1.4.1.1.481.9";
    /**
     * RT Physician Intent Storage
     */
    public static final String RTPhysicianIntentStorage = "1.2.840.10008.5.1.4.1.1.481.10";
    /**
     * RT Segment Annotation Storage
     */
    public static final String RTSegmentAnnotationStorage = "1.2.840.10008.5.1.4.1.1.481.11";
    /**
     * RT Radiation Set Storage
     */
    public static final String RTRadiationSetStorage = "1.2.840.10008.5.1.4.1.1.481.12";
    /**
     * C-Arm Photon-Electron Radiation Storage
     */
    public static final String CArmPhotonElectronRadiationStorage = "1.2.840.10008.5.1.4.1.1.481.13";
    /**
     * DICOS CT Image Storage
     */
    public static final String DICOSCTImageStorage = "1.2.840.10008.5.1.4.1.1.501.1";
    /**
     * DICOS Digital X-Ray Image Storage - For Presentation
     */
    public static final String DICOSDigitalXRayImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.501.2.1";
    /**
     * DICOS Digital X-Ray Image Storage - For Processing
     */
    public static final String DICOSDigitalXRayImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.501.2.2";
    /**
     * DICOS Threat Detection Report Storage
     */
    public static final String DICOSThreatDetectionReportStorage = "1.2.840.10008.5.1.4.1.1.501.3";
    /**
     * DICOS 2D AIT Storage
     */
    public static final String DICOS2DAITStorage = "1.2.840.10008.5.1.4.1.1.501.4";
    /**
     * DICOS 3D AIT Storage
     */
    public static final String DICOS3DAITStorage = "1.2.840.10008.5.1.4.1.1.501.5";
    /**
     * DICOS Quadrupole Resonance (QR) Storage
     */
    public static final String DICOSQuadrupoleResonanceQRStorage = "1.2.840.10008.5.1.4.1.1.501.6";
    /**
     * Eddy Current Image Storage
     */
    public static final String EddyCurrentImageStorage = "1.2.840.10008.5.1.4.1.1.601.1";
    /**
     * Eddy Current Multi-frame Image Storage
     */
    public static final String EddyCurrentMultiFrameImageStorage = "1.2.840.10008.5.1.4.1.1.601.2";
    /**
     * Patient Root Query/Retrieve Information Model - FIND
     */
    public static final String PatientRootQueryRetrieveInformationModelFIND = "1.2.840.10008.5.1.4.1.2.1.1";
    /**
     * Patient Root Query/Retrieve Information Model - MOVE
     */
    public static final String PatientRootQueryRetrieveInformationModelMOVE = "1.2.840.10008.5.1.4.1.2.1.2";
    /**
     * Patient Root Query/Retrieve Information Model - GET
     */
    public static final String PatientRootQueryRetrieveInformationModelGET = "1.2.840.10008.5.1.4.1.2.1.3";
    /**
     * Study Root Query/Retrieve Information Model - FIND
     */
    public static final String StudyRootQueryRetrieveInformationModelFIND = "1.2.840.10008.5.1.4.1.2.2.1";
    /**
     * Study Root Query/Retrieve Information Model - MOVE
     */
    public static final String StudyRootQueryRetrieveInformationModelMOVE = "1.2.840.10008.5.1.4.1.2.2.2";
    /**
     * Study Root Query/Retrieve Information Model - GET
     */
    public static final String StudyRootQueryRetrieveInformationModelGET = "1.2.840.10008.5.1.4.1.2.2.3";
    /**
     * Patient/Study Only Query/Retrieve Information Model - FIND (Retired)
     */
    public static final String PatientStudyOnlyQueryRetrieveInformationModelFINDRetired = "1.2.840.10008.5.1.4.1.2.3.1";
    /**
     * Patient/Study Only Query/Retrieve Information Model - MOVE (Retired)
     */
    public static final String PatientStudyOnlyQueryRetrieveInformationModelMOVERetired = "1.2.840.10008.5.1.4.1.2.3.2";
    /**
     * Patient/Study Only Query/Retrieve Information Model - GET (Retired)
     */
    public static final String PatientStudyOnlyQueryRetrieveInformationModelGETRetired = "1.2.840.10008.5.1.4.1.2.3.3";
    /**
     * Composite Instance Root Retrieve - MOVE
     */
    public static final String CompositeInstanceRootRetrieveMOVE = "1.2.840.10008.5.1.4.1.2.4.2";
    /**
     * Composite Instance Root Retrieve - GET
     */
    public static final String CompositeInstanceRootRetrieveGET = "1.2.840.10008.5.1.4.1.2.4.3";
    /**
     * Composite Instance Retrieve Without Bulk Data - GET
     */
    public static final String CompositeInstanceRetrieveWithoutBulkDataGET = "1.2.840.10008.5.1.4.1.2.5.3";
    /**
     * Defined Procedure Protocol Information Model - FIND
     */
    public static final String DefinedProcedureProtocolInformationModelFIND = "1.2.840.10008.5.1.4.20.1";
    /**
     * Defined Procedure Protocol Information Model - MOVE
     */
    public static final String DefinedProcedureProtocolInformationModelMOVE = "1.2.840.10008.5.1.4.20.2";
    /**
     * Defined Procedure Protocol Information Model - GET
     */
    public static final String DefinedProcedureProtocolInformationModelGET = "1.2.840.10008.5.1.4.20.3";
    /**
     * Modality Worklist Information Model - FIND
     */
    public static final String ModalityWorklistInformationModelFIND = "1.2.840.10008.5.1.4.31";
    /**
     * General Purpose Worklist Management Meta SOP Class (Retired)
     */
    public static final String GeneralPurposeWorklistManagementMetaSOPClassRetired = "1.2.840.10008.5.1.4.32";
    /**
     * General Purpose Worklist Information Model - FIND (Retired)
     */
    public static final String GeneralPurposeWorklistInformationModelFINDRetired = "1.2.840.10008.5.1.4.32.1";
    /**
     * General Purpose Scheduled Procedure Step SOP Class (Retired)
     */
    public static final String GeneralPurposeScheduledProcedureStepSOPClassRetired = "1.2.840.10008.5.1.4.32.2";
    /**
     * General Purpose Performed Procedure Step SOP Class (Retired)
     */
    public static final String GeneralPurposePerformedProcedureStepSOPClassRetired = "1.2.840.10008.5.1.4.32.3";
    /**
     * Instance Availability Notification SOP Class
     */
    public static final String InstanceAvailabilityNotificationSOPClass = "1.2.840.10008.5.1.4.33";
    /**
     * RT Beams Delivery Instruction Storage - Trial (Retired)
     */
    public static final String RTBeamsDeliveryInstructionStorageTrialRetired = "1.2.840.10008.5.1.4.34.1";
    /**
     * RT Conventional Machine Verification - Trial (Retired)
     */
    public static final String RTConventionalMachineVerificationTrialRetired = "1.2.840.10008.5.1.4.34.2";
    /**
     * RT Ion Machine Verification - Trial (Retired)
     */
    public static final String RTIonMachineVerificationTrialRetired = "1.2.840.10008.5.1.4.34.3";
    /**
     * Unified Worklist and Procedure Step Service Class - Trial (Retired)
     */
    public static final String UnifiedWorklistAndProcedureStepServiceClassTrialRetired = "1.2.840.10008.5.1.4.34.4";
    /**
     * Unified Procedure Step - Push SOP Class - Trial (Retired)
     */
    public static final String UnifiedProcedureStepPushSOPClassTrialRetired = "1.2.840.10008.5.1.4.34.4.1";
    /**
     * Unified Procedure Step - Watch SOP Class - Trial (Retired)
     */
    public static final String UnifiedProcedureStepWatchSOPClassTrialRetired = "1.2.840.10008.5.1.4.34.4.2";
    /**
     * Unified Procedure Step - Pull SOP Class - Trial (Retired)
     */
    public static final String UnifiedProcedureStepPullSOPClassTrialRetired = "1.2.840.10008.5.1.4.34.4.3";
    /**
     * Unified Procedure Step - Event SOP Class - Trial (Retired)
     */
    public static final String UnifiedProcedureStepEventSOPClassTrialRetired = "1.2.840.10008.5.1.4.34.4.4";
    /**
     * UPS Global Subscription SOP Instance
     */
    public static final String UPSGlobalSubscriptionSOPInstance = "1.2.840.10008.5.1.4.34.5";
    /**
     * UPS Filtered Global Subscription SOP Instance
     */
    public static final String UPSFilteredGlobalSubscriptionSOPInstance = "1.2.840.10008.5.1.4.34.5.1";
    /**
     * Unified Worklist and Procedure Step Service Class
     */
    public static final String UnifiedWorklistAndProcedureStepServiceClass = "1.2.840.10008.5.1.4.34.6";
    /**
     * Unified Procedure Step - Push SOP Class
     */
    public static final String UnifiedProcedureStepPushSOPClass = "1.2.840.10008.5.1.4.34.6.1";
    /**
     * Unified Procedure Step - Watch SOP Class
     */
    public static final String UnifiedProcedureStepWatchSOPClass = "1.2.840.10008.5.1.4.34.6.2";
    /**
     * Unified Procedure Step - Pull SOP Class
     */
    public static final String UnifiedProcedureStepPullSOPClass = "1.2.840.10008.5.1.4.34.6.3";
    /**
     * Unified Procedure Step - Event SOP Class
     */
    public static final String UnifiedProcedureStepEventSOPClass = "1.2.840.10008.5.1.4.34.6.4";
    /**
     * RT Beams Delivery Instruction Storage
     */
    public static final String RTBeamsDeliveryInstructionStorage = "1.2.840.10008.5.1.4.34.7";
    /**
     * RT Conventional Machine Verification
     */
    public static final String RTConventionalMachineVerification = "1.2.840.10008.5.1.4.34.8";
    /**
     * RT Ion Machine Verification
     */
    public static final String RTIonMachineVerification = "1.2.840.10008.5.1.4.34.9";
    /**
     * RT Brachy Application Setup Delivery Instruction Storage
     */
    public static final String RTBrachyApplicationSetupDeliveryInstructionStorage = "1.2.840.10008.5.1.4.34.10";
    /**
     * General Relevant Patient Information Query
     */
    public static final String GeneralRelevantPatientInformationQuery = "1.2.840.10008.5.1.4.37.1";
    /**
     * Breast Imaging Relevant Patient Information Query
     */
    public static final String BreastImagingRelevantPatientInformationQuery = "1.2.840.10008.5.1.4.37.2";
    /**
     * Cardiac Relevant Patient Information Query
     */
    public static final String CardiacRelevantPatientInformationQuery = "1.2.840.10008.5.1.4.37.3";
    /**
     * Hanging Protocol Storage
     */
    public static final String HangingProtocolStorage = "1.2.840.10008.5.1.4.38.1";
    /**
     * Hanging Protocol Information Model - FIND
     */
    public static final String HangingProtocolInformationModelFIND = "1.2.840.10008.5.1.4.38.2";
    /**
     * Hanging Protocol Information Model - MOVE
     */
    public static final String HangingProtocolInformationModelMOVE = "1.2.840.10008.5.1.4.38.3";
    /**
     * Hanging Protocol Information Model - GET
     */
    public static final String HangingProtocolInformationModelGET = "1.2.840.10008.5.1.4.38.4";
    /**
     * Color Palette Storage
     */
    public static final String ColorPaletteStorage = "1.2.840.10008.5.1.4.39.1";
    /**
     * Color Palette Query/Retrieve Information Model - FIND
     */
    public static final String ColorPaletteQueryRetrieveInformationModelFIND = "1.2.840.10008.5.1.4.39.2";
    /**
     * Color Palette Query/Retrieve Information Model - MOVE
     */
    public static final String ColorPaletteQueryRetrieveInformationModelMOVE = "1.2.840.10008.5.1.4.39.3";
    /**
     * Color Palette Query/Retrieve Information Model - GET
     */
    public static final String ColorPaletteQueryRetrieveInformationModelGET = "1.2.840.10008.5.1.4.39.4";
    /**
     * Product Characteristics Query SOP Class
     */
    public static final String ProductCharacteristicsQuerySOPClass = "1.2.840.10008.5.1.4.41";
    /**
     * Substance Approval Query SOP Class
     */
    public static final String SubstanceApprovalQuerySOPClass = "1.2.840.10008.5.1.4.42";
    /**
     * Generic Implant Template Storage
     */
    public static final String GenericImplantTemplateStorage = "1.2.840.10008.5.1.4.43.1";
    /**
     * Generic Implant Template Information Model - FIND
     */
    public static final String GenericImplantTemplateInformationModelFIND = "1.2.840.10008.5.1.4.43.2";
    /**
     * Generic Implant Template Information Model - MOVE
     */
    public static final String GenericImplantTemplateInformationModelMOVE = "1.2.840.10008.5.1.4.43.3";
    /**
     * Generic Implant Template Information Model - GET
     */
    public static final String GenericImplantTemplateInformationModelGET = "1.2.840.10008.5.1.4.43.4";
    /**
     * Implant Assembly Template Storage
     */
    public static final String ImplantAssemblyTemplateStorage = "1.2.840.10008.5.1.4.44.1";
    /**
     * Implant Assembly Template Information Model - FIND
     */
    public static final String ImplantAssemblyTemplateInformationModelFIND = "1.2.840.10008.5.1.4.44.2";
    /**
     * Implant Assembly Template Information Model - MOVE
     */
    public static final String ImplantAssemblyTemplateInformationModelMOVE = "1.2.840.10008.5.1.4.44.3";
    /**
     * Implant Assembly Template Information Model - GET
     */
    public static final String ImplantAssemblyTemplateInformationModelGET = "1.2.840.10008.5.1.4.44.4";
    /**
     * Implant Template Group Storage
     */
    public static final String ImplantTemplateGroupStorage = "1.2.840.10008.5.1.4.45.1";
    /**
     * Implant Template Group Information Model - FIND
     */
    public static final String ImplantTemplateGroupInformationModelFIND = "1.2.840.10008.5.1.4.45.2";
    /**
     * Implant Template Group Information Model - MOVE
     */
    public static final String ImplantTemplateGroupInformationModelMOVE = "1.2.840.10008.5.1.4.45.3";
    /**
     * Implant Template Group Information Model - GET
     */
    public static final String ImplantTemplateGroupInformationModelGET = "1.2.840.10008.5.1.4.45.4";
    /**
     * Native DICOM Model
     */
    public static final String NativeDICOMModel = "1.2.840.10008.7.1.1";
    /**
     * Abstract Multi-Dimensional Image Model
     */
    public static final String AbstractMultiDimensionalImageModel = "1.2.840.10008.7.1.2";
    /**
     * DICOM Content Mapping Resource
     */
    public static final String DICOMContentMappingResource = "1.2.840.10008.8.1.1";
    /**
     * Video Endoscopic Image Real-Time Communication
     */
    public static final String VideoEndoscopicImageRealTimeCommunication = "1.2.840.10008.10.1";
    /**
     * Video Photographic Image Real-Time Communication
     */
    public static final String VideoPhotographicImageRealTimeCommunication = "1.2.840.10008.10.2";
    /**
     * Audio Waveform Real-Time Communication
     */
    public static final String AudioWaveformRealTimeCommunication = "1.2.840.10008.10.3";
    /**
     * Rendition Selection Document Real-Time Communication
     */
    public static final String RenditionSelectionDocumentRealTimeCommunication = "1.2.840.10008.10.4";
    /**
     * dicomDeviceName
     */
    public static final String dicomDeviceName = "1.2.840.10008.15.0.3.1";
    /**
     * dicomDescription
     */
    public static final String dicomDescription = "1.2.840.10008.15.0.3.2";
    /**
     * dicomManufacturer
     */
    public static final String dicomManufacturer = "1.2.840.10008.15.0.3.3";
    /**
     * dicomManufacturerModelName
     */
    public static final String dicomManufacturerModelName = "1.2.840.10008.15.0.3.4";
    /**
     * dicomSoftwareVersion
     */
    public static final String dicomSoftwareVersion = "1.2.840.10008.15.0.3.5";
    /**
     * dicomVendorData
     */
    public static final String dicomVendorData = "1.2.840.10008.15.0.3.6";
    /**
     * dicomAETitle
     */
    public static final String dicomAETitle = "1.2.840.10008.15.0.3.7";
    /**
     * dicomNetworkConnectionReference
     */
    public static final String dicomNetworkConnectionReference = "1.2.840.10008.15.0.3.8";
    /**
     * dicomApplicationCluster
     */
    public static final String dicomApplicationCluster = "1.2.840.10008.15.0.3.9";
    /**
     * dicomAssociationInitiator
     */
    public static final String dicomAssociationInitiator = "1.2.840.10008.15.0.3.10";
    /**
     * dicomAssociationAcceptor
     */
    public static final String dicomAssociationAcceptor = "1.2.840.10008.15.0.3.11";
    /**
     * dicomHostname
     */
    public static final String dicomHostname = "1.2.840.10008.15.0.3.12";
    /**
     * dicomPort
     */
    public static final String dicomPort = "1.2.840.10008.15.0.3.13";
    /**
     * dicomSOPClass
     */
    public static final String dicomSOPClass = "1.2.840.10008.15.0.3.14";
    /**
     * dicomTransferRole
     */
    public static final String dicomTransferRole = "1.2.840.10008.15.0.3.15";
    /**
     * dicomTransferSyntax
     */
    public static final String dicomTransferSyntax = "1.2.840.10008.15.0.3.16";
    /**
     * dicomPrimaryDeviceType
     */
    public static final String dicomPrimaryDeviceType = "1.2.840.10008.15.0.3.17";
    /**
     * dicomRelatedDeviceReference
     */
    public static final String dicomRelatedDeviceReference = "1.2.840.10008.15.0.3.18";
    /**
     * dicomPreferredCalledAETitle
     */
    public static final String dicomPreferredCalledAETitle = "1.2.840.10008.15.0.3.19";
    /**
     * dicomTLSCyphersuite
     */
    public static final String dicomTLSCyphersuite = "1.2.840.10008.15.0.3.20";
    /**
     * dicomAuthorizedNodeCertificateReference
     */
    public static final String dicomAuthorizedNodeCertificateReference = "1.2.840.10008.15.0.3.21";
    /**
     * dicomThisNodeCertificateReference
     */
    public static final String dicomThisNodeCertificateReference = "1.2.840.10008.15.0.3.22";
    /**
     * dicomInstalled
     */
    public static final String dicomInstalled = "1.2.840.10008.15.0.3.23";
    /**
     * dicomStationName
     */
    public static final String dicomStationName = "1.2.840.10008.15.0.3.24";
    /**
     * dicomDeviceSerialNumber
     */
    public static final String dicomDeviceSerialNumber = "1.2.840.10008.15.0.3.25";
    /**
     * dicomInstitutionName
     */
    public static final String dicomInstitutionName = "1.2.840.10008.15.0.3.26";
    /**
     * dicomInstitutionAddress
     */
    public static final String dicomInstitutionAddress = "1.2.840.10008.15.0.3.27";
    /**
     * dicomInstitutionDepartmentName
     */
    public static final String dicomInstitutionDepartmentName = "1.2.840.10008.15.0.3.28";
    /**
     * dicomIssuerOfPatientID
     */
    public static final String dicomIssuerOfPatientID = "1.2.840.10008.15.0.3.29";
    /**
     * dicomPreferredCallingAETitle
     */
    public static final String dicomPreferredCallingAETitle = "1.2.840.10008.15.0.3.30";
    /**
     * dicomSupportedCharacterSet
     */
    public static final String dicomSupportedCharacterSet = "1.2.840.10008.15.0.3.31";
    /**
     * dicomConfigurationRoot
     */
    public static final String dicomConfigurationRoot = "1.2.840.10008.15.0.4.1";
    /**
     * dicomDevicesRoot
     */
    public static final String dicomDevicesRoot = "1.2.840.10008.15.0.4.2";
    /**
     * dicomUniqueAETitlesRegistryRoot
     */
    public static final String dicomUniqueAETitlesRegistryRoot = "1.2.840.10008.15.0.4.3";
    /**
     * dicomDevice
     */
    public static final String dicomDevice = "1.2.840.10008.15.0.4.4";
    /**
     * dicomNetworkAE
     */
    public static final String dicomNetworkAE = "1.2.840.10008.15.0.4.5";
    /**
     * dicomNetworkConnection
     */
    public static final String dicomNetworkConnection = "1.2.840.10008.15.0.4.6";
    /**
     * dicomUniqueAETitle
     */
    public static final String dicomUniqueAETitle = "1.2.840.10008.15.0.4.7";
    /**
     * dicomTransferCapability
     */
    public static final String dicomTransferCapability = "1.2.840.10008.15.0.4.8";
    /**
     * Universal Coordinated Time
     */
    public static final String UniversalCoordinatedTime = "1.2.840.10008.15.1.1";
    /**
     * Private Agfa Basic Attribute Presentation State
     */
    public static final String PrivateAgfaBasicAttributePresentationState = "1.2.124.113532.3500.7";
    /**
     * Private Agfa Arrival Transaction
     */
    public static final String PrivateAgfaArrivalTransaction = "1.2.124.113532.3500.8.1";
    /**
     * Private Agfa Dictation Transaction
     */
    public static final String PrivateAgfaDictationTransaction = "1.2.124.113532.3500.8.2";
    /**
     * Private Agfa Report Transcription Transaction
     */
    public static final String PrivateAgfaReportTranscriptionTransaction = "1.2.124.113532.3500.8.3";
    /**
     * Private Agfa Report Approval Transaction
     */
    public static final String PrivateAgfaReportApprovalTransaction = "1.2.124.113532.3500.8.4";
    /**
     * Private TomTec Annotation Storage
     */
    public static final String PrivateTomTecAnnotationStorage = "1.2.276.0.48.5.1.4.1.1.7";
    /**
     * Private Toshiba US Image Storage
     */
    public static final String PrivateToshibaUSImageStorage = "1.2.392.200036.9116.7.8.1.1.1";
    /**
     * Private Fuji CR Image Storage
     */
    public static final String PrivateFujiCRImageStorage = "1.2.392.200036.9125.1.1.2";
    /**
     * Private GE Collage Storage
     */
    public static final String PrivateGECollageStorage = "1.2.528.1.1001.5.1.1.1";
    /**
     * Private ERAD Practice Builder Report Text Storage
     */
    public static final String PrivateERADPracticeBuilderReportTextStorage = "1.2.826.0.1.3680043.293.1.0.1";
    /**
     * Private ERAD Practice Builder Report Dictation Storage
     */
    public static final String PrivateERADPracticeBuilderReportDictationStorage = "1.2.826.0.1.3680043.293.1.0.2";
    /**
     * Private Philips HP Live 3D 01 Storage
     */
    public static final String PrivatePhilipsHPLive3D01Storage = "1.2.840.113543.6.6.1.3.10001";
    /**
     * Private Philips HP Live 3D 02 Storage
     */
    public static final String PrivatePhilipsHPLive3D02Storage = "1.2.840.113543.6.6.1.3.10002";
    /**
     * Private GE 3D Model Storage
     */
    public static final String PrivateGE3DModelStorage = "1.2.840.113619.4.26";
    /**
     * Private GE Dicom CT Image Info Object
     */
    public static final String PrivateGEDicomCTImageInfoObject = "1.2.840.113619.4.3";
    /**
     * Private GE Dicom Display Image Info Object
     */
    public static final String PrivateGEDicomDisplayImageInfoObject = "1.2.840.113619.4.4";
    /**
     * Private GE Dicom MR Image Info Object
     */
    public static final String PrivateGEDicomMRImageInfoObject = "1.2.840.113619.4.2";
    /**
     * Private GE eNTEGRA Protocol or NM Genie Storage
     */
    public static final String PrivateGEeNTEGRAProtocolOrNMGenieStorage = "1.2.840.113619.4.27";
    /**
     * Private GE PET Raw Data Storage
     */
    public static final String PrivateGEPETRawDataStorage = "1.2.840.113619.4.30";
    /**
     * Private GE RT Plan Storage
     */
    public static final String PrivateGERTPlanStorage = "1.2.840.113619.4.5.249";
    /**
     * Private PixelMed Legacy Converted Enhanced CT Image Storage
     */
    public static final String PrivatePixelMedLegacyConvertedEnhancedCTImageStorage = "1.3.6.1.4.1.5962.301.1";
    /**
     * Private PixelMed Legacy Converted Enhanced MR Image Storage
     */
    public static final String PrivatePixelMedLegacyConvertedEnhancedMRImageStorage = "1.3.6.1.4.1.5962.301.2";
    /**
     * Private PixelMed Legacy Converted Enhanced PET Image Storage
     */
    public static final String PrivatePixelMedLegacyConvertedEnhancedPETImageStorage = "1.3.6.1.4.1.5962.301.3";
    /**
     * Private PixelMed Floating Point Image Storage
     */
    public static final String PrivatePixelMedFloatingPointImageStorage = "1.3.6.1.4.1.5962.301.9";
    /**
     * Private Siemens CSA Non Image Storage
     */
    public static final String PrivateSiemensCSANonImageStorage = "1.3.12.2.1107.6.0.0";
    /**
     * Private Siemens CT MR Volume Storage
     */
    public static final String PrivateSiemensCTMRVolumeStorage = "1.3.12.2.1107.5.99.3.10";
    /**
     * Private Siemens AX Frame Sets Storage
     */
    public static final String PrivateSiemensAXFrameSetsStorage = "1.3.12.2.1107.5.99.3.11";
    /**
     * Private Philips Specialised XA Storage
     */
    public static final String PrivatePhilipsSpecialisedXAStorage = "1.3.46.670589.2.3.1.1";
    /**
     * Private Philips CX Image Storage
     */
    public static final String PrivatePhilipsCXImageStorage = "1.3.46.670589.2.4.1.1";
    /**
     * Private Philips 3D Presentation State Storage
     */
    public static final String PrivatePhilips3DPresentationStateStorage = "1.3.46.670589.2.5.1.1";
    /**
     * Private Philips VRML Storage
     */
    public static final String PrivatePhilipsVRMLStorage = "1.3.46.670589.2.8.1.1";
    /**
     * Private Philips Volume Set Storage
     */
    public static final String PrivatePhilipsVolumeSetStorage = "1.3.46.670589.2.11.1.1";
    /**
     * Private Philips Volume Storage (Retired)
     */
    public static final String PrivatePhilipsVolumeStorageRetired = "1.3.46.670589.5.0.1";
    /**
     * Private Philips Volume Storage
     */
    public static final String PrivatePhilipsVolumeStorage = "1.3.46.670589.5.0.1.1";
    /**
     * Private Philips 3D Object Storage (Retired)
     */
    public static final String PrivatePhilips3DObjectStorageRetired = "1.3.46.670589.5.0.2";
    /**
     * Private Philips 3D Object Storage
     */
    public static final String PrivatePhilips3DObjectStorage = "1.3.46.670589.5.0.2.1";
    /**
     * Private Philips Surface Storage (Retired)
     */
    public static final String PrivatePhilipsSurfaceStorageRetired = "1.3.46.670589.5.0.3";
    /**
     * Private Philips Surface Storage
     */
    public static final String PrivatePhilipsSurfaceStorage = "1.3.46.670589.5.0.3.1";
    /**
     * Private Philips Composite Object Storage
     */
    public static final String PrivatePhilipsCompositeObjectStorage = "1.3.46.670589.5.0.4";
    /**
     * Private Philips MR Cardio Profile Storage
     */
    public static final String PrivatePhilipsMRCardioProfileStorage = "1.3.46.670589.5.0.7";
    /**
     * Private Philips MR Cardio Storage (Retired)
     */
    public static final String PrivatePhilipsMRCardioStorageRetired = "1.3.46.670589.6.0.0";
    /**
     * Private Philips MR Cardio Storage
     */
    public static final String PrivatePhilipsMRCardioStorage = "1.3.46.670589.6.0.0.1";
    /**
     * Private Philips CT Synthetic Image Storage
     */
    public static final String PrivatePhilipsCTSyntheticImageStorage = "1.3.46.670589.5.0.9";
    /**
     * Private Philips MR Synthetic Image Storage
     */
    public static final String PrivatePhilipsMRSyntheticImageStorage = "1.3.46.670589.5.0.10";
    /**
     * Private Philips MR Cardio Analysis Storage (Retired)
     */
    public static final String PrivatePhilipsMRCardioAnalysisStorageRetired = "1.3.46.670589.5.0.11";
    /**
     * Private Philips MR Cardio Analysis Storage
     */
    public static final String PrivatePhilipsMRCardioAnalysisStorage = "1.3.46.670589.5.0.11.1";
    /**
     * Private Philips CX Synthetic Image Storage
     */
    public static final String PrivatePhilipsCXSyntheticImageStorage = "1.3.46.670589.5.0.12";
    /**
     * Private Philips Perfusion Storage
     */
    public static final String PrivatePhilipsPerfusionStorage = "1.3.46.670589.5.0.13";
    /**
     * Private Philips Perfusion Image Storage
     */
    public static final String PrivatePhilipsPerfusionImageStorage = "1.3.46.670589.5.0.14";
    /**
     * Private Philips X-Ray MF Storage
     */
    public static final String PrivatePhilipsXRayMFStorage = "1.3.46.670589.7.8.1618510091";
    /**
     * Private Philips Live Run Storage
     */
    public static final String PrivatePhilipsLiveRunStorage = "1.3.46.670589.7.8.1618510092";
    /**
     * Private Philips Run Storage
     */
    public static final String PrivatePhilipsRunStorage = "1.3.46.670589.7.8.16185100129";
    /**
     * Private Philips Reconstruction Storage
     */
    public static final String PrivatePhilipsReconstructionStorage = "1.3.46.670589.7.8.16185100130";
    /**
     * Private Philips MR Spectrum Storage
     */
    public static final String PrivatePhilipsMRSpectrumStorage = "1.3.46.670589.11.0.0.12.1";
    /**
     * Private Philips MR Series Data Storage
     */
    public static final String PrivatePhilipsMRSeriesDataStorage = "1.3.46.670589.11.0.0.12.2";
    /**
     * Private Philips MR Color Image Storage
     */
    public static final String PrivatePhilipsMRColorImageStorage = "1.3.46.670589.11.0.0.12.3";
    /**
     * Private Philips MR Examcard Storage
     */
    public static final String PrivatePhilipsMRExamcardStorage = "1.3.46.670589.11.0.0.12.4";
    /**
     * Private PMOD Multi-frame Image Storage
     */
    public static final String PrivatePMODMultiframeImageStorage = "2.16.840.1.114033.5.1.4.1.1.130";


    private static final Map<String, String> map = new HashMap<>();
    private static final String UUID_ROOT = "2.25";
    private static final Pattern PATTERN = Pattern.compile("[012]((\\.0)|(\\.[1-9]\\d*))+");
    private static final Charset ASCII = StandardCharsets.US_ASCII;
    public static String root = UUID_ROOT;

    static {
        map.put(VerificationSOPClass, "Verification SOP Class");
        map.put(ImplicitVRLittleEndian, "Implicit VR Little Endian");
        map.put(ExplicitVRLittleEndian, "Explicit VR Little Endian");
        map.put(DeflatedExplicitVRLittleEndian, "Deflated Explicit VR Little Endian");
        map.put(ExplicitVRBigEndianRetired, "Explicit VR Big Endian (Retired)");
        map.put(JPEGBaseline1, "JPEG Baseline (Process 1)");
        map.put(JPEGExtended24, "JPEG Extended (Process 2 & 4)");
        map.put(JPEGExtended35Retired, "JPEG Extended (Process 3 & 5) (Retired)");
        map.put(JPEGSpectralSelectionNonHierarchical68Retired, "JPEG Spectral Selection, Non-Hierarchical (Process 6 & 8) (Retired)");
        map.put(JPEGSpectralSelectionNonHierarchical79Retired, "JPEG Spectral Selection, Non-Hierarchical (Process 7 & 9) (Retired)");
        map.put(JPEGFullProgressionNonHierarchical1012Retired, "JPEG Full Progression, Non-Hierarchical (Process 10 & 12) (Retired)");
        map.put(JPEGFullProgressionNonHierarchical1113Retired, "JPEG Full Progression, Non-Hierarchical (Process 11 & 13) (Retired)");
        map.put(JPEGLosslessNonHierarchical14, "JPEG Lossless, Non-Hierarchical (Process 14)");
        map.put(JPEGLosslessNonHierarchical15Retired, "JPEG Lossless, Non-Hierarchical (Process 15) (Retired)");
        map.put(JPEGExtendedHierarchical1618Retired, "JPEG Extended, Hierarchical (Process 16 & 18) (Retired)");
        map.put(JPEGExtendedHierarchical1719Retired, "JPEG Extended, Hierarchical (Process 17 & 19) (Retired)");
        map.put(JPEGSpectralSelectionHierarchical2022Retired, "JPEG Spectral Selection, Hierarchical (Process 20 & 22) (Retired)");
        map.put(JPEGSpectralSelectionHierarchical2123Retired, "JPEG Spectral Selection, Hierarchical (Process 21 & 23) (Retired)");
        map.put(JPEGFullProgressionHierarchical2426Retired, "JPEG Full Progression, Hierarchical (Process 24 & 26) (Retired)");
        map.put(JPEGFullProgressionHierarchical2527Retired, "JPEG Full Progression, Hierarchical (Process 25 & 27) (Retired)");
        map.put(JPEGLosslessHierarchical28Retired, "JPEG Lossless, Hierarchical (Process 28) (Retired)");
        map.put(JPEGLosslessHierarchical29Retired, "JPEG Lossless, Hierarchical (Process 29) (Retired)");
        map.put(JPEGLossless, "JPEG Lossless, Non-Hierarchical, First-Order Prediction (Process 14 [Selection Value 1])");
        map.put(JPEGLSLossless, "JPEG-LS Lossless Image Compression");
        map.put(JPEGLSLossyNearLossless, "JPEG-LS Lossy (Near-Lossless) Image Compression");
        map.put(JPEG2000LosslessOnly, "JPEG 2000 Image Compression (Lossless Only)");
        map.put(JPEG2000, "JPEG 2000 Image Compression");
        map.put(JPEG2000Part2MultiComponentLosslessOnly, "JPEG 2000 Part 2 Multi-component Image Compression (Lossless Only)");
        map.put(JPEG2000Part2MultiComponent, "JPEG 2000 Part 2 Multi-component Image Compression");
        map.put(JPIPReferenced, "JPIP Referenced");
        map.put(JPIPReferencedDeflate, "JPIP Referenced Deflate");
        map.put(MPEG2, "MPEG2 Main Profile / Main Level");
        map.put(MPEG2MainProfileHighLevel, "MPEG2 Main Profile / High Level");
        map.put(MPEG4AVCH264HighProfileLevel41, "MPEG-4 AVC/H.264 High Profile / Level 4.1");
        map.put(MPEG4AVCH264BDCompatibleHighProfileLevel41, "MPEG-4 AVC/H.264 BD-compatible High Profile / Level 4.1");
        map.put(MPEG4AVCH264HighProfileLevel42For2DVideo, "MPEG-4 AVC/H.264 High Profile / Level 4.2 For 2D Video");
        map.put(MPEG4AVCH264HighProfileLevel42For3DVideo, "MPEG-4 AVC/H.264 High Profile / Level 4.2 For 3D Video");
        map.put(MPEG4AVCH264StereoHighProfileLevel42, "MPEG-4 AVC/H.264 Stereo High Profile / Level 4.2");
        map.put(HEVCH265MainProfileLevel51, "HEVC/H.265 Main Profile / Level 5.1");
        map.put(HEVCH265Main10ProfileLevel51, "HEVC/H.265 Main 10 Profile / Level 5.1");
        map.put(RLELossless, "RLE Lossless");
        map.put(RFC2557MIMEEncapsulationRetired, "RFC 2557 MIME encapsulation (Retired)");
        map.put(XMLEncodingRetired, "XML Encoding (Retired)");
        map.put(MediaStorageDirectoryStorage, "Media Storage Directory Storage");
        map.put(TalairachBrainAtlasFrameOfReference, "Talairach Brain Atlas Frame of Reference");
        map.put(SPM2T1FrameOfReference, "SPM2 T1 Frame of Reference");
        map.put(SPM2T2FrameOfReference, "SPM2 T2 Frame of Reference");
        map.put(SPM2PDFrameOfReference, "SPM2 PD Frame of Reference");
        map.put(SPM2EPIFrameOfReference, "SPM2 EPI Frame of Reference");
        map.put(SPM2FILT1FrameOfReference, "SPM2 FIL T1 Frame of Reference");
        map.put(SPM2PETFrameOfReference, "SPM2 PET Frame of Reference");
        map.put(SPM2TRANSMFrameOfReference, "SPM2 TRANSM Frame of Reference");
        map.put(SPM2SPECTFrameOfReference, "SPM2 SPECT Frame of Reference");
        map.put(SPM2GRAYFrameOfReference, "SPM2 GRAY Frame of Reference");
        map.put(SPM2WHITEFrameOfReference, "SPM2 WHITE Frame of Reference");
        map.put(SPM2CSFFrameOfReference, "SPM2 CSF Frame of Reference");
        map.put(SPM2BRAINMASKFrameOfReference, "SPM2 BRAINMASK Frame of Reference");
        map.put(SPM2AVG305T1FrameOfReference, "SPM2 AVG305T1 Frame of Reference");
        map.put(SPM2AVG152T1FrameOfReference, "SPM2 AVG152T1 Frame of Reference");
        map.put(SPM2AVG152T2FrameOfReference, "SPM2 AVG152T2 Frame of Reference");
        map.put(SPM2AVG152PDFrameOfReference, "SPM2 AVG152PD Frame of Reference");
        map.put(SPM2SINGLESUBJT1FrameOfReference, "SPM2 SINGLESUBJT1 Frame of Reference");
        map.put(ICBM452T1FrameOfReference, "ICBM 452 T1 Frame of Reference");
        map.put(ICBMSingleSubjectMRIFrameOfReference, "ICBM Single Subject MRI Frame of Reference");
        map.put(HotIronColorPaletteSOPInstance, "Hot Iron Color Palette SOP Instance");
        map.put(PETColorPaletteSOPInstance, "PET Color Palette SOP Instance");
        map.put(HotMetalBlueColorPaletteSOPInstance, "Hot Metal Blue Color Palette SOP Instance");
        map.put(PET20StepColorPaletteSOPInstance, "PET 20 Step Color Palette SOP Instance");
        map.put(SpringColorPaletteSOPInstance, "Spring Color Palette SOP Instance");
        map.put(SummerColorPaletteSOPInstance, "Summer Color Palette SOP Instance");
        map.put(FallColorPaletteSOPInstance, "Fall Color Palette SOP Instance");
        map.put(WinterColorPaletteSOPInstance, "Winter Color Palette SOP Instance");
        map.put(BasicStudyContentNotificationSOPClassRetired, "Basic Study Content Notification SOP Class (Retired)");
        map.put(Papyrus3ImplicitVRLittleEndianRetired, "Papyrus 3 Implicit VR Little Endian (Retired)");
        map.put(StorageCommitmentPushModelSOPClass, "Storage Commitment Push Model SOP Class");
        map.put(StorageCommitmentPushModelSOPInstance, "Storage Commitment Push Model SOP Instance");
        map.put(StorageCommitmentPullModelSOPClassRetired, "Storage Commitment Pull Model SOP Class (Retired)");
        map.put(StorageCommitmentPullModelSOPInstanceRetired, "Storage Commitment Pull Model SOP Instance (Retired)");
        map.put(ProceduralEventLoggingSOPClass, "Procedural Event Logging SOP Class");
        map.put(ProceduralEventLoggingSOPInstance, "Procedural Event Logging SOP Instance");
        map.put(SubstanceAdministrationLoggingSOPClass, "Substance Administration Logging SOP Class");
        map.put(SubstanceAdministrationLoggingSOPInstance, "Substance Administration Logging SOP Instance");
        map.put(DICOMUIDRegistry, "DICOM UID Registry");
        map.put(DICOMControlledTerminology, "DICOM Controlled Terminology");
        map.put(AdultMouseAnatomyOntology, "Adult Mouse Anatomy Ontology");
        map.put(UberonOntology, "Uberon Ontology");
        map.put(IntegratedTaxonomicInformationSystemITISTaxonomicSerialNumberTSN, "Integrated Taxonomic Information System (ITIS) Taxonomic Serial Number (TSN)");
        map.put(MouseGenomeInitiativeMGI, "Mouse Genome Initiative (MGI)");
        map.put(PubChemCompoundCID, "PubChem Compound CID");
        map.put(ICD11, "ICD-11");
        map.put(NewYorkUniversityMelanomaClinicalCooperativeGroup, "New York University Melanoma Clinical Cooperative Group");
        map.put(MayoClinicNonRadiologicalImagesSpecificBodyStructureAnatomicalSurfaceRegionGuide, "Mayo Clinic Non-radiological Images Specific Body Structure Anatomical Surface Region Guide");
        map.put(ImageBiomarkerStandardisationInitiative, "Image Biomarker Standardisation Initiative");
        map.put(RadiomicsOntology, "Radiomics Ontology");
        map.put(DICOMApplicationContextName, "DICOM Application Context Name");
        map.put(DetachedPatientManagementSOPClassRetired, "Detached Patient Management SOP Class (Retired)");
        map.put(DetachedPatientManagementMetaSOPClassRetired, "Detached Patient Management Meta SOP Class (Retired)");
        map.put(DetachedVisitManagementSOPClassRetired, "Detached Visit Management SOP Class (Retired)");
        map.put(DetachedStudyManagementSOPClassRetired, "Detached Study Management SOP Class (Retired)");
        map.put(StudyComponentManagementSOPClassRetired, "Study Component Management SOP Class (Retired)");
        map.put(ModalityPerformedProcedureStepSOPClass, "Modality Performed Procedure Step SOP Class");
        map.put(ModalityPerformedProcedureStepRetrieveSOPClass, "Modality Performed Procedure Step Retrieve SOP Class");
        map.put(ModalityPerformedProcedureStepNotificationSOPClass, "Modality Performed Procedure Step Notification SOP Class");
        map.put(DetachedResultsManagementSOPClassRetired, "Detached Results Management SOP Class (Retired)");
        map.put(DetachedResultsManagementMetaSOPClassRetired, "Detached Results Management Meta SOP Class (Retired)");
        map.put(DetachedStudyManagementMetaSOPClassRetired, "Detached Study Management Meta SOP Class (Retired)");
        map.put(DetachedInterpretationManagementSOPClassRetired, "Detached Interpretation Management SOP Class (Retired)");
        map.put(StorageServiceClass, "Storage Service Class");
        map.put(BasicFilmSessionSOPClass, "Basic Film Session SOP Class");
        map.put(BasicFilmBoxSOPClass, "Basic Film Box SOP Class");
        map.put(BasicGrayscaleImageBoxSOPClass, "Basic Grayscale Image Box SOP Class");
        map.put(BasicColorImageBoxSOPClass, "Basic Color Image Box SOP Class");
        map.put(ReferencedImageBoxSOPClassRetired, "Referenced Image Box SOP Class (Retired)");
        map.put(BasicGrayscalePrintManagementMetaSOPClass, "Basic Grayscale Print Management Meta SOP Class");
        map.put(ReferencedGrayscalePrintManagementMetaSOPClassRetired, "Referenced Grayscale Print Management Meta SOP Class (Retired)");
        map.put(PrintJobSOPClass, "Print Job SOP Class");
        map.put(BasicAnnotationBoxSOPClass, "Basic Annotation Box SOP Class");
        map.put(PrinterSOPClass, "Printer SOP Class");
        map.put(PrinterConfigurationRetrievalSOPClass, "Printer Configuration Retrieval SOP Class");
        map.put(PrinterSOPInstance, "Printer SOP Instance");
        map.put(PrinterConfigurationRetrievalSOPInstance, "Printer Configuration Retrieval SOP Instance");
        map.put(BasicColorPrintManagementMetaSOPClass, "Basic Color Print Management Meta SOP Class");
        map.put(ReferencedColorPrintManagementMetaSOPClassRetired, "Referenced Color Print Management Meta SOP Class (Retired)");
        map.put(VOILUTBoxSOPClass, "VOI LUT Box SOP Class");
        map.put(PresentationLUTSOPClass, "Presentation LUT SOP Class");
        map.put(ImageOverlayBoxSOPClassRetired, "Image Overlay Box SOP Class (Retired)");
        map.put(BasicPrintImageOverlayBoxSOPClassRetired, "Basic Print Image Overlay Box SOP Class (Retired)");
        map.put(PrintQueueSOPInstanceRetired, "Print Queue SOP Instance (Retired)");
        map.put(PrintQueueManagementSOPClassRetired, "Print Queue Management SOP Class (Retired)");
        map.put(StoredPrintStorageSOPClassRetired, "Stored Print Storage SOP Class (Retired)");
        map.put(HardcopyGrayscaleImageStorageSOPClassRetired, "Hardcopy Grayscale Image Storage SOP Class (Retired)");
        map.put(HardcopyColorImageStorageSOPClassRetired, "Hardcopy Color Image Storage SOP Class (Retired)");
        map.put(PullPrintRequestSOPClassRetired, "Pull Print Request SOP Class (Retired)");
        map.put(PullStoredPrintManagementMetaSOPClassRetired, "Pull Stored Print Management Meta SOP Class (Retired)");
        map.put(MediaCreationManagementSOPClassUID, "Media Creation Management SOP Class UID");
        map.put(DisplaySystemSOPClass, "Display System SOP Class");
        map.put(DisplaySystemSOPInstance, "Display System SOP Instance");
        map.put(ComputedRadiographyImageStorage, "Computed Radiography Image Storage");
        map.put(DigitalXRayImageStorageForPresentation, "Digital X-Ray Image Storage - For Presentation");
        map.put(DigitalXRayImageStorageForProcessing, "Digital X-Ray Image Storage - For Processing");
        map.put(DigitalMammographyXRayImageStorageForPresentation, "Digital Mammography X-Ray Image Storage - For Presentation");
        map.put(DigitalMammographyXRayImageStorageForProcessing, "Digital Mammography X-Ray Image Storage - For Processing");
        map.put(DigitalIntraOralXRayImageStorageForPresentation, "Digital Intra-Oral X-Ray Image Storage - For Presentation");
        map.put(DigitalIntraOralXRayImageStorageForProcessing, "Digital Intra-Oral X-Ray Image Storage - For Processing");
        map.put(CTImageStorage, "CT Image Storage");
        map.put(EnhancedCTImageStorage, "Enhanced CT Image Storage");
        map.put(LegacyConvertedEnhancedCTImageStorage, "Legacy Converted Enhanced CT Image Storage");
        map.put(UltrasoundMultiFrameImageStorageRetired, "Ultrasound Multi-frame Image Storage (Retired)");
        map.put(UltrasoundMultiFrameImageStorage, "Ultrasound Multi-frame Image Storage");
        map.put(MRImageStorage, "MR Image Storage");
        map.put(EnhancedMRImageStorage, "Enhanced MR Image Storage");
        map.put(MRSpectroscopyStorage, "MR Spectroscopy Storage");
        map.put(EnhancedMRColorImageStorage, "Enhanced MR Color Image Storage");
        map.put(LegacyConvertedEnhancedMRImageStorage, "Legacy Converted Enhanced MR Image Storage");
        map.put(NuclearMedicineImageStorageRetired, "Nuclear Medicine Image Storage (Retired)");
        map.put(UltrasoundImageStorageRetired, "Ultrasound Image Storage (Retired)");
        map.put(UltrasoundImageStorage, "Ultrasound Image Storage");
        map.put(EnhancedUSVolumeStorage, "Enhanced US Volume Storage");
        map.put(SecondaryCaptureImageStorage, "Secondary Capture Image Storage");
        map.put(MultiFrameSingleBitSecondaryCaptureImageStorage, "Multi-frame Single Bit Secondary Capture Image Storage");
        map.put(MultiFrameGrayscaleByteSecondaryCaptureImageStorage, "Multi-frame Grayscale Byte Secondary Capture Image Storage");
        map.put(MultiFrameGrayscaleWordSecondaryCaptureImageStorage, "Multi-frame Grayscale Word Secondary Capture Image Storage");
        map.put(MultiFrameTrueColorSecondaryCaptureImageStorage, "Multi-frame True Color Secondary Capture Image Storage");
        map.put(StandaloneOverlayStorageRetired, "Standalone Overlay Storage (Retired)");
        map.put(StandaloneCurveStorageRetired, "Standalone Curve Storage (Retired)");
        map.put(WaveformStorageTrialRetired, "Waveform Storage - Trial (Retired)");
        map.put(TwelveLeadECGWaveformStorage, "12-lead ECG Waveform Storage");
        map.put(GeneralECGWaveformStorage, "General ECG Waveform Storage");
        map.put(AmbulatoryECGWaveformStorage, "Ambulatory ECG Waveform Storage");
        map.put(HemodynamicWaveformStorage, "Hemodynamic Waveform Storage");
        map.put(CardiacElectrophysiologyWaveformStorage, "Cardiac Electrophysiology Waveform Storage");
        map.put(BasicVoiceAudioWaveformStorage, "Basic Voice Audio Waveform Storage");
        map.put(GeneralAudioWaveformStorage, "General Audio Waveform Storage");
        map.put(ArterialPulseWaveformStorage, "Arterial Pulse Waveform Storage");
        map.put(RespiratoryWaveformStorage, "Respiratory Waveform Storage");
        map.put(StandaloneModalityLUTStorageRetired, "Standalone Modality LUT Storage (Retired)");
        map.put(StandaloneVOILUTStorageRetired, "Standalone VOI LUT Storage (Retired)");
        map.put(GrayscaleSoftcopyPresentationStateStorage, "Grayscale Softcopy Presentation State Storage");
        map.put(ColorSoftcopyPresentationStateStorage, "Color Softcopy Presentation State Storage");
        map.put(PseudoColorSoftcopyPresentationStateStorage, "Pseudo-Color Softcopy Presentation State Storage");
        map.put(BlendingSoftcopyPresentationStateStorage, "Blending Softcopy Presentation State Storage");
        map.put(XAXRFGrayscaleSoftcopyPresentationStateStorage, "XA/XRF Grayscale Softcopy Presentation State Storage");
        map.put(GrayscalePlanarMPRVolumetricPresentationStateStorage, "Grayscale Planar MPR Volumetric Presentation State Storage");
        map.put(CompositingPlanarMPRVolumetricPresentationStateStorage, "Compositing Planar MPR Volumetric Presentation State Storage");
        map.put(AdvancedBlendingPresentationStateStorage, "Advanced Blending Presentation State Storage");
        map.put(VolumeRenderingVolumetricPresentationStateStorage, "Volume Rendering Volumetric Presentation State Storage");
        map.put(SegmentedVolumeRenderingVolumetricPresentationStateStorage, "Segmented Volume Rendering Volumetric Presentation State Storage");
        map.put(MultipleVolumeRenderingVolumetricPresentationStateStorage, "Multiple Volume Rendering Volumetric Presentation State Storage");
        map.put(XRayAngiographicImageStorage, "X-Ray Angiographic Image Storage");
        map.put(EnhancedXAImageStorage, "Enhanced XA Image Storage");
        map.put(XRayRadiofluoroscopicImageStorage, "X-Ray Radiofluoroscopic Image Storage");
        map.put(EnhancedXRFImageStorage, "Enhanced XRF Image Storage");
        map.put(XRayAngiographicBiPlaneImageStorageRetired, "X-Ray Angiographic Bi-Plane Image Storage (Retired)");
        map.put(ZeissOPTFileRetired, "Zeiss OPT File (Retired)");
        map.put(XRay3DAngiographicImageStorage, "X-Ray 3D Angiographic Image Storage");
        map.put(XRay3DCraniofacialImageStorage, "X-Ray 3D Craniofacial Image Storage");
        map.put(BreastTomosynthesisImageStorage, "Breast Tomosynthesis Image Storage");
        map.put(BreastProjectionXRayImageStorageForPresentation, "Breast Projection X-Ray Image Storage - For Presentation");
        map.put(BreastProjectionXRayImageStorageForProcessing, "Breast Projection X-Ray Image Storage - For Processing");
        map.put(IntravascularOpticalCoherenceTomographyImageStorageForPresentation, "Intravascular Optical Coherence Tomography Image Storage - For Presentation");
        map.put(IntravascularOpticalCoherenceTomographyImageStorageForProcessing, "Intravascular Optical Coherence Tomography Image Storage - For Processing");
        map.put(NuclearMedicineImageStorage, "Nuclear Medicine Image Storage");
        map.put(ParametricMapStorage, "Parametric Map Storage");
        map.put(MRImageStorageZeroPaddedRetired, "MR Image Storage Zero Padded (Retired)");
        map.put(RawDataStorage, "Raw Data Storage");
        map.put(SpatialRegistrationStorage, "Spatial Registration Storage");
        map.put(SpatialFiducialsStorage, "Spatial Fiducials Storage");
        map.put(DeformableSpatialRegistrationStorage, "Deformable Spatial Registration Storage");
        map.put(SegmentationStorage, "Segmentation Storage");
        map.put(SurfaceSegmentationStorage, "Surface Segmentation Storage");
        map.put(TractographyResultsStorage, "Tractography Results Storage");
        map.put(RealWorldValueMappingStorage, "Real World Value Mapping Storage");
        map.put(SurfaceScanMeshStorage, "Surface Scan Mesh Storage");
        map.put(SurfaceScanPointCloudStorage, "Surface Scan Point Cloud Storage");
        map.put(VLImageStorageTrialRetired, "VL Image Storage - Trial (Retired)");
        map.put(VLMultiFrameImageStorageTrialRetired, "VL Multi-frame Image Storage - Trial (Retired)");
        map.put(VLEndoscopicImageStorage, "VL Endoscopic Image Storage");
        map.put(VideoEndoscopicImageStorage, "Video Endoscopic Image Storage");
        map.put(VLMicroscopicImageStorage, "VL Microscopic Image Storage");
        map.put(VideoMicroscopicImageStorage, "Video Microscopic Image Storage");
        map.put(VLSlideCoordinatesMicroscopicImageStorage, "VL Slide-Coordinates Microscopic Image Storage");
        map.put(VLPhotographicImageStorage, "VL Photographic Image Storage");
        map.put(VideoPhotographicImageStorage, "Video Photographic Image Storage");
        map.put(OphthalmicPhotography8BitImageStorage, "Ophthalmic Photography 8 Bit Image Storage");
        map.put(OphthalmicPhotography16BitImageStorage, "Ophthalmic Photography 16 Bit Image Storage");
        map.put(StereometricRelationshipStorage, "Stereometric Relationship Storage");
        map.put(OphthalmicTomographyImageStorage, "Ophthalmic Tomography Image Storage");
        map.put(WideFieldOphthalmicPhotographyStereographicProjectionImageStorage, "Wide Field Ophthalmic Photography Stereographic Projection Image Storage");
        map.put(WideFieldOphthalmicPhotography3DCoordinatesImageStorage, "Wide Field Ophthalmic Photography 3D Coordinates Image Storage");
        map.put(OphthalmicOpticalCoherenceTomographyEnFaceImageStorage, "Ophthalmic Optical Coherence Tomography En Face Image Storage");
        map.put(OphthalmicOpticalCoherenceTomographyBScanVolumeAnalysisStorage, "Ophthalmic Optical Coherence Tomography B-scan Volume Analysis Storage");
        map.put(VLWholeSlideMicroscopyImageStorage, "VL Whole Slide Microscopy Image Storage");
        map.put(LensometryMeasurementsStorage, "Lensometry Measurements Storage");
        map.put(AutorefractionMeasurementsStorage, "Autorefraction Measurements Storage");
        map.put(KeratometryMeasurementsStorage, "Keratometry Measurements Storage");
        map.put(SubjectiveRefractionMeasurementsStorage, "Subjective Refraction Measurements Storage");
        map.put(VisualAcuityMeasurementsStorage, "Visual Acuity Measurements Storage");
        map.put(SpectaclePrescriptionReportStorage, "Spectacle Prescription Report Storage");
        map.put(OphthalmicAxialMeasurementsStorage, "Ophthalmic Axial Measurements Storage");
        map.put(IntraocularLensCalculationsStorage, "Intraocular Lens Calculations Storage");
        map.put(MacularGridThicknessAndVolumeReportStorage, "Macular Grid Thickness and Volume Report Storage");
        map.put(OphthalmicVisualFieldStaticPerimetryMeasurementsStorage, "Ophthalmic Visual Field Static Perimetry Measurements Storage");
        map.put(OphthalmicThicknessMapStorage, "Ophthalmic Thickness Map Storage");
        map.put(CornealTopographyMapStorage, "Corneal Topography Map Storage");
        map.put(TextSRStorageTrialRetired, "Text SR Storage - Trial (Retired)");
        map.put(AudioSRStorageTrialRetired, "Audio SR Storage - Trial (Retired)");
        map.put(DetailSRStorageTrialRetired, "Detail SR Storage - Trial (Retired)");
        map.put(ComprehensiveSRStorageTrialRetired, "Comprehensive SR Storage - Trial (Retired)");
        map.put(BasicTextSRStorage, "Basic Text SR Storage");
        map.put(EnhancedSRStorage, "Enhanced SR Storage");
        map.put(ComprehensiveSRStorage, "Comprehensive SR Storage");
        map.put(Comprehensive3DSRStorage, "Comprehensive 3D SR Storage");
        map.put(ExtensibleSRStorage, "Extensible SR Storage");
        map.put(ProcedureLogStorage, "Procedure Log Storage");
        map.put(MammographyCADSRStorage, "Mammography CAD SR Storage");
        map.put(KeyObjectSelectionDocumentStorage, "Key Object Selection Document Storage");
        map.put(ChestCADSRStorage, "Chest CAD SR Storage");
        map.put(XRayRadiationDoseSRStorage, "X-Ray Radiation Dose SR Storage");
        map.put(RadiopharmaceuticalRadiationDoseSRStorage, "Radiopharmaceutical Radiation Dose SR Storage");
        map.put(ColonCADSRStorage, "Colon CAD SR Storage");
        map.put(ImplantationPlanSRStorage, "Implantation Plan SR Storage");
        map.put(AcquisitionContextSRStorage, "Acquisition Context SR Storage");
        map.put(SimplifiedAdultEchoSRStorage, "Simplified Adult Echo SR Storage");
        map.put(PatientRadiationDoseSRStorage, "Patient Radiation Dose SR Storage");
        map.put(PlannedImagingAgentAdministrationSRStorage, "Planned Imaging Agent Administration SR Storage");
        map.put(PerformedImagingAgentAdministrationSRStorage, "Performed Imaging Agent Administration SR Storage");
        map.put(ContentAssessmentResultsStorage, "Content Assessment Results Storage");
        map.put(EncapsulatedPDFStorage, "Encapsulated PDF Storage");
        map.put(EncapsulatedCDAStorage, "Encapsulated CDA Storage");
        map.put(EncapsulatedSTLStorage, "Encapsulated STL Storage");
        map.put(PositronEmissionTomographyImageStorage, "Positron Emission Tomography Image Storage");
        map.put(LegacyConvertedEnhancedPETImageStorage, "Legacy Converted Enhanced PET Image Storage");
        map.put(StandalonePETCurveStorageRetired, "Standalone PET Curve Storage (Retired)");
        map.put(EnhancedPETImageStorage, "Enhanced PET Image Storage");
        map.put(BasicStructuredDisplayStorage, "Basic Structured Display Storage");
        map.put(CTDefinedProcedureProtocolStorage, "CT Defined Procedure Protocol Storage");
        map.put(CTPerformedProcedureProtocolStorage, "CT Performed Procedure Protocol Storage");
        map.put(ProtocolApprovalStorage, "Protocol Approval Storage");
        map.put(ProtocolApprovalInformationModelFIND, "Protocol Approval Information Model - FIND");
        map.put(ProtocolApprovalInformationModelMOVE, "Protocol Approval Information Model - MOVE");
        map.put(ProtocolApprovalInformationModelGET, "Protocol Approval Information Model - GET");
        map.put(RTImageStorage, "RT Image Storage");
        map.put(RTDoseStorage, "RT Dose Storage");
        map.put(RTStructureSetStorage, "RT Structure Set Storage");
        map.put(RTBeamsTreatmentRecordStorage, "RT Beams Treatment Record Storage");
        map.put(RTPlanStorage, "RT Plan Storage");
        map.put(RTBrachyTreatmentRecordStorage, "RT Brachy Treatment Record Storage");
        map.put(RTTreatmentSummaryRecordStorage, "RT Treatment Summary Record Storage");
        map.put(RTIonPlanStorage, "RT Ion Plan Storage");
        map.put(RTIonBeamsTreatmentRecordStorage, "RT Ion Beams Treatment Record Storage");
        map.put(RTPhysicianIntentStorage, "RT Physician Intent Storage");
        map.put(RTSegmentAnnotationStorage, "RT Segment Annotation Storage");
        map.put(DICOSCTImageStorage, "DICOS CT Image Storage");
        map.put(DICOSDigitalXRayImageStorageForPresentation, "DICOS Digital X-Ray Image Storage - For Presentation");
        map.put(DICOSDigitalXRayImageStorageForProcessing, "DICOS Digital X-Ray Image Storage - For Processing");
        map.put(DICOSThreatDetectionReportStorage, "DICOS Threat Detection Report Storage");
        map.put(DICOS2DAITStorage, "DICOS 2D AIT Storage");
        map.put(DICOS3DAITStorage, "DICOS 3D AIT Storage");
        map.put(DICOSQuadrupoleResonanceQRStorage, "DICOS Quadrupole Resonance (QR) Storage");
        map.put(EddyCurrentImageStorage, "Eddy Current Image Storage");
        map.put(EddyCurrentMultiFrameImageStorage, "Eddy Current Multi-frame Image Storage");
        map.put(PatientRootQueryRetrieveInformationModelFIND, "Patient Root Query/Retrieve Information Model - FIND");
        map.put(PatientRootQueryRetrieveInformationModelMOVE, "Patient Root Query/Retrieve Information Model - MOVE");
        map.put(PatientRootQueryRetrieveInformationModelGET, "Patient Root Query/Retrieve Information Model - GET");
        map.put(StudyRootQueryRetrieveInformationModelFIND, "Study Root Query/Retrieve Information Model - FIND");
        map.put(StudyRootQueryRetrieveInformationModelMOVE, "Study Root Query/Retrieve Information Model - MOVE");
        map.put(StudyRootQueryRetrieveInformationModelGET, "Study Root Query/Retrieve Information Model - GET");
        map.put(PatientStudyOnlyQueryRetrieveInformationModelFINDRetired, "Patient/Study Only Query/Retrieve Information Model - FIND (Retired)");
        map.put(PatientStudyOnlyQueryRetrieveInformationModelMOVERetired, "Patient/Study Only Query/Retrieve Information Model - MOVE (Retired)");
        map.put(PatientStudyOnlyQueryRetrieveInformationModelGETRetired, "Patient/Study Only Query/Retrieve Information Model - GET (Retired)");
        map.put(CompositeInstanceRootRetrieveMOVE, "Composite Instance Root Retrieve - MOVE");
        map.put(CompositeInstanceRootRetrieveGET, "Composite Instance Root Retrieve - GET");
        map.put(CompositeInstanceRetrieveWithoutBulkDataGET, "Composite Instance Retrieve Without Bulk Data - GET");
        map.put(DefinedProcedureProtocolInformationModelFIND, "Defined Procedure Protocol Information Model - FIND");
        map.put(DefinedProcedureProtocolInformationModelMOVE, "Defined Procedure Protocol Information Model - MOVE");
        map.put(DefinedProcedureProtocolInformationModelGET, "Defined Procedure Protocol Information Model - GET");
        map.put(ModalityWorklistInformationModelFIND, "Modality Worklist Information Model - FIND");
        map.put(GeneralPurposeWorklistManagementMetaSOPClassRetired, "General Purpose Worklist Management Meta SOP Class (Retired)");
        map.put(GeneralPurposeWorklistInformationModelFINDRetired, "General Purpose Worklist Information Model - FIND (Retired)");
        map.put(GeneralPurposeScheduledProcedureStepSOPClassRetired, "General Purpose Scheduled Procedure Step SOP Class (Retired)");
        map.put(GeneralPurposePerformedProcedureStepSOPClassRetired, "General Purpose Performed Procedure Step SOP Class (Retired)");
        map.put(InstanceAvailabilityNotificationSOPClass, "Instance Availability Notification SOP Class");
        map.put(RTBeamsDeliveryInstructionStorageTrialRetired, "RT Beams Delivery Instruction Storage - Trial (Retired)");
        map.put(RTConventionalMachineVerificationTrialRetired, "RT Conventional Machine Verification - Trial (Retired)");
        map.put(RTIonMachineVerificationTrialRetired, "RT Ion Machine Verification - Trial (Retired)");
        map.put(UnifiedWorklistAndProcedureStepServiceClassTrialRetired, "Unified Worklist and Procedure Step Service Class - Trial (Retired)");
        map.put(UnifiedProcedureStepPushSOPClassTrialRetired, "Unified Procedure Step - Push SOP Class - Trial (Retired)");
        map.put(UnifiedProcedureStepWatchSOPClassTrialRetired, "Unified Procedure Step - Watch SOP Class - Trial (Retired)");
        map.put(UnifiedProcedureStepPullSOPClassTrialRetired, "Unified Procedure Step - Pull SOP Class - Trial (Retired)");
        map.put(UnifiedProcedureStepEventSOPClassTrialRetired, "Unified Procedure Step - Event SOP Class - Trial (Retired)");
        map.put(UPSGlobalSubscriptionSOPInstance, "UPS Global Subscription SOP Instance");
        map.put(UPSFilteredGlobalSubscriptionSOPInstance, "UPS Filtered Global Subscription SOP Instance");
        map.put(UnifiedWorklistAndProcedureStepServiceClass, "Unified Worklist and Procedure Step Service Class");
        map.put(UnifiedProcedureStepPushSOPClass, "Unified Procedure Step - Push SOP Class");
        map.put(UnifiedProcedureStepWatchSOPClass, "Unified Procedure Step - Watch SOP Class");
        map.put(UnifiedProcedureStepPullSOPClass, "Unified Procedure Step - Pull SOP Class");
        map.put(UnifiedProcedureStepEventSOPClass, "Unified Procedure Step - Event SOP Class");
        map.put(RTBeamsDeliveryInstructionStorage, "RT Beams Delivery Instruction Storage");
        map.put(RTConventionalMachineVerification, "RT Conventional Machine Verification");
        map.put(RTIonMachineVerification, "RT Ion Machine Verification");
        map.put(RTBrachyApplicationSetupDeliveryInstructionStorage, "RT Brachy Application Setup Delivery Instruction Storage");
        map.put(GeneralRelevantPatientInformationQuery, "General Relevant Patient Information Query");
        map.put(BreastImagingRelevantPatientInformationQuery, "Breast Imaging Relevant Patient Information Query");
        map.put(CardiacRelevantPatientInformationQuery, "Cardiac Relevant Patient Information Query");
        map.put(HangingProtocolStorage, "Hanging Protocol Storage");
        map.put(HangingProtocolInformationModelFIND, "Hanging Protocol Information Model - FIND");
        map.put(HangingProtocolInformationModelMOVE, "Hanging Protocol Information Model - MOVE");
        map.put(HangingProtocolInformationModelGET, "Hanging Protocol Information Model - GET");
        map.put(ColorPaletteStorage, "Color Palette Storage");
        map.put(ColorPaletteQueryRetrieveInformationModelFIND, "Color Palette Query/Retrieve Information Model - FIND");
        map.put(ColorPaletteQueryRetrieveInformationModelMOVE, "Color Palette Query/Retrieve Information Model - MOVE");
        map.put(ColorPaletteQueryRetrieveInformationModelGET, "Color Palette Query/Retrieve Information Model - GET");
        map.put(ProductCharacteristicsQuerySOPClass, "Product Characteristics Query SOP Class");
        map.put(SubstanceApprovalQuerySOPClass, "Substance Approval Query SOP Class");
        map.put(GenericImplantTemplateStorage, "Generic Implant Template Storage");
        map.put(GenericImplantTemplateInformationModelFIND, "Generic Implant Template Information Model - FIND");
        map.put(GenericImplantTemplateInformationModelMOVE, "Generic Implant Template Information Model - MOVE");
        map.put(GenericImplantTemplateInformationModelGET, "Generic Implant Template Information Model - GET");
        map.put(ImplantAssemblyTemplateStorage, "Implant Assembly Template Storage");
        map.put(ImplantAssemblyTemplateInformationModelFIND, "Implant Assembly Template Information Model - FIND");
        map.put(ImplantAssemblyTemplateInformationModelMOVE, "Implant Assembly Template Information Model - MOVE");
        map.put(ImplantAssemblyTemplateInformationModelGET, "Implant Assembly Template Information Model - GET");
        map.put(ImplantTemplateGroupStorage, "Implant Template Group Storage");
        map.put(ImplantTemplateGroupInformationModelFIND, "Implant Template Group Information Model - FIND");
        map.put(ImplantTemplateGroupInformationModelMOVE, "Implant Template Group Information Model - MOVE");
        map.put(ImplantTemplateGroupInformationModelGET, "Implant Template Group Information Model - GET");
        map.put(NativeDICOMModel, "Native DICOM Model");
        map.put(AbstractMultiDimensionalImageModel, "Abstract Multi-Dimensional Image Model");
        map.put(DICOMContentMappingResource, "DICOM Content Mapping Resource");
        map.put(dicomDeviceName, "dicomDeviceName");
        map.put(dicomDescription, "dicomDescription");
        map.put(dicomManufacturer, "dicomManufacturer");
        map.put(dicomManufacturerModelName, "dicomManufacturerModelName");
        map.put(dicomSoftwareVersion, "dicomSoftwareVersion");
        map.put(dicomVendorData, "dicomVendorData");
        map.put(dicomAETitle, "dicomAETitle");
        map.put(dicomNetworkConnectionReference, "dicomNetworkConnectionReference");
        map.put(dicomApplicationCluster, "dicomApplicationCluster");
        map.put(dicomAssociationInitiator, "dicomAssociationInitiator");
        map.put(dicomAssociationAcceptor, "dicomAssociationAcceptor");
        map.put(dicomHostname, "dicomHostname");
        map.put(dicomPort, "dicomPort");
        map.put(dicomSOPClass, "dicomSOPClass");
        map.put(dicomTransferRole, "dicomTransferRole");
        map.put(dicomTransferSyntax, "dicomTransferSyntax");
        map.put(dicomPrimaryDeviceType, "dicomPrimaryDeviceType");
        map.put(dicomRelatedDeviceReference, "dicomRelatedDeviceReference");
        map.put(dicomPreferredCalledAETitle, "dicomPreferredCalledAETitle");
        map.put(dicomTLSCyphersuite, "dicomTLSCyphersuite");
        map.put(dicomAuthorizedNodeCertificateReference, "dicomAuthorizedNodeCertificateReference");
        map.put(dicomThisNodeCertificateReference, "dicomThisNodeCertificateReference");
        map.put(dicomInstalled, "dicomInstalled");
        map.put(dicomStationName, "dicomStationName");
        map.put(dicomDeviceSerialNumber, "dicomDeviceSerialNumber");
        map.put(dicomInstitutionName, "dicomInstitutionName");
        map.put(dicomInstitutionAddress, "dicomInstitutionAddress");
        map.put(dicomInstitutionDepartmentName, "dicomInstitutionDepartmentName");
        map.put(dicomIssuerOfPatientID, "dicomIssuerOfPatientID");
        map.put(dicomPreferredCallingAETitle, "dicomPreferredCallingAETitle");
        map.put(dicomSupportedCharacterSet, "dicomSupportedCharacterSet");
        map.put(dicomConfigurationRoot, "dicomConfigurationRoot");
        map.put(dicomDevicesRoot, "dicomDevicesRoot");
        map.put(dicomUniqueAETitlesRegistryRoot, "dicomUniqueAETitlesRegistryRoot");
        map.put(dicomDevice, "dicomDevice");
        map.put(dicomNetworkAE, "dicomNetworkAE");
        map.put(dicomNetworkConnection, "dicomNetworkConnection");
        map.put(dicomUniqueAETitle, "dicomUniqueAETitle");
        map.put(dicomTransferCapability, "dicomTransferCapability");
        map.put(UniversalCoordinatedTime, "Universal Coordinated Time");
        map.put(PrivateAgfaBasicAttributePresentationState, "Private Agfa Basic Attribute Presentation State");
        map.put(PrivateAgfaArrivalTransaction, "Private Agfa Arrival Transaction");
        map.put(PrivateAgfaDictationTransaction, "Private Agfa Dictation Transaction");
        map.put(PrivateAgfaReportTranscriptionTransaction, "Private Agfa Report Transcription Transaction");
        map.put(PrivateAgfaReportApprovalTransaction, "Private Agfa Report Approval Transaction");
        map.put(PrivateTomTecAnnotationStorage, "Private TomTec Annotation Storage");
        map.put(PrivateToshibaUSImageStorage, "Private Toshiba US Image Storage");
        map.put(PrivateFujiCRImageStorage, "Private Fuji CR Image Storage");
        map.put(PrivateGECollageStorage, "Private GE Collage Storage");
        map.put(PrivateERADPracticeBuilderReportTextStorage, "Private ERAD Practice Builder Report Text Storage");
        map.put(PrivateERADPracticeBuilderReportDictationStorage, "Private ERAD Practice Builder Report Dictation Storage");
        map.put(PrivatePhilipsHPLive3D01Storage, "Private Philips HP Live 3D 01 Storage");
        map.put(PrivatePhilipsHPLive3D02Storage, "Private Philips HP Live 3D 02 Storage");
        map.put(PrivateGE3DModelStorage, "Private GE 3D Model Storage");
        map.put(PrivateGEDicomCTImageInfoObject, "Private GE Dicom CT Image Info Object");
        map.put(PrivateGEDicomDisplayImageInfoObject, "Private GE Dicom Display Image Info Object");
        map.put(PrivateGEDicomMRImageInfoObject, "Private GE Dicom MR Image Info Object");
        map.put(PrivateGEeNTEGRAProtocolOrNMGenieStorage, "Private GE eNTEGRA Protocol or NM Genie Storage");
        map.put(PrivateGEPETRawDataStorage, "Private GE PET Raw Data Storage");
        map.put(PrivateGERTPlanStorage, "Private GE RT Plan Storage");
        map.put(PrivatePixelMedLegacyConvertedEnhancedCTImageStorage, "Private PixelMed Legacy Converted Enhanced CT Image Storage");
        map.put(PrivatePixelMedLegacyConvertedEnhancedMRImageStorage, "Private PixelMed Legacy Converted Enhanced MR Image Storage");
        map.put(PrivatePixelMedLegacyConvertedEnhancedPETImageStorage, "Private PixelMed Legacy Converted Enhanced PET Image Storage");
        map.put(PrivatePixelMedFloatingPointImageStorage, "Private PixelMed Floating Point Image Storage");
        map.put(PrivateSiemensCSANonImageStorage, "Private Siemens CSA Non Image Storage");
        map.put(PrivateSiemensCTMRVolumeStorage, "Private Siemens CT MR Volume Storage");
        map.put(PrivateSiemensAXFrameSetsStorage, "Private Siemens AX Frame Sets Storage");
        map.put(PrivatePhilipsSpecialisedXAStorage, "Private Philips Specialised XA Storage");
        map.put(PrivatePhilipsCXImageStorage, "Private Philips CX Image Storage");
        map.put(PrivatePhilips3DPresentationStateStorage, "Private Philips 3D Presentation State Storage");
        map.put(PrivatePhilipsVRMLStorage, "Private Philips VRML Storage");
        map.put(PrivatePhilipsVolumeSetStorage, "Private Philips Volume Set Storage");
        map.put(PrivatePhilipsVolumeStorageRetired, "Private Philips Volume Storage (Retired)");
        map.put(PrivatePhilipsVolumeStorage, "Private Philips Volume Storage");
        map.put(PrivatePhilips3DObjectStorageRetired, "Private Philips 3D Object Storage (Retired)");
        map.put(PrivatePhilips3DObjectStorage, "Private Philips 3D Object Storage");
        map.put(PrivatePhilipsSurfaceStorageRetired, "Private Philips Surface Storage (Retired)");
        map.put(PrivatePhilipsSurfaceStorage, "Private Philips Surface Storage");
        map.put(PrivatePhilipsCompositeObjectStorage, "Private Philips Composite Object Storage");
        map.put(PrivatePhilipsMRCardioProfileStorage, "Private Philips MR Cardio Profile Storage");
        map.put(PrivatePhilipsMRCardioStorageRetired, "Private Philips MR Cardio Storage (Retired)");
        map.put(PrivatePhilipsMRCardioStorage, "Private Philips MR Cardio Storage");
        map.put(PrivatePhilipsCTSyntheticImageStorage, "Private Philips CT Synthetic Image Storage");
        map.put(PrivatePhilipsMRSyntheticImageStorage, "Private Philips MR Synthetic Image Storage");
        map.put(PrivatePhilipsMRCardioAnalysisStorageRetired, "Private Philips MR Cardio Analysis Storage (Retired)");
        map.put(PrivatePhilipsMRCardioAnalysisStorage, "Private Philips MR Cardio Analysis Storage");
        map.put(PrivatePhilipsCXSyntheticImageStorage, "Private Philips CX Synthetic Image Storage");
        map.put(PrivatePhilipsPerfusionStorage, "Private Philips Perfusion Storage");
        map.put(PrivatePhilipsPerfusionImageStorage, "Private Philips Perfusion Image Storage");
        map.put(PrivatePhilipsXRayMFStorage, "Private Philips X-Ray MF Storage");
        map.put(PrivatePhilipsLiveRunStorage, "Private Philips Live Run Storage");
        map.put(PrivatePhilipsRunStorage, "Private Philips Run Storage");
        map.put(PrivatePhilipsReconstructionStorage, "Private Philips Reconstruction Storage");
        map.put(PrivatePhilipsMRSpectrumStorage, "Private Philips MR Spectrum Storage");
        map.put(PrivatePhilipsMRSeriesDataStorage, "Private Philips MR Series Data Storage");
        map.put(PrivatePhilipsMRColorImageStorage, "Private Philips MR Color Image Storage");
        map.put(PrivatePhilipsMRExamcardStorage, "Private Philips MR Examcard Storage");
        map.put(PrivatePMODMultiframeImageStorage, "Private PMOD Multi-frame Image Storage");
    }

    public static String nameOf(String uid) {
        try {
            return map.get(uid);
        } catch (Exception e) {
        }
        return Symbol.QUESTION_MARK;
    }

    public static String forName(String keyword) {
        try {
            return (String) UID.class.getField(keyword).get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException(keyword);
        }
    }

    public static final String getRoot() {
        return UUID_ROOT;
    }

    public static final void setRoot(String root) {
        checkRoot(root);
        UID.root = root;
    }

    private static void checkRoot(String root) {
        if (root.length() > 24)
            throw new IllegalArgumentException("root length > 24");
        if (!isValid(root))
            throw new IllegalArgumentException(root);
    }

    public static boolean isValid(String uid) {
        return uid.length() <= 64 && PATTERN.matcher(uid).matches();
    }

    public static String createUID() {
        return randomUID(root);
    }

    public static String createNameBasedUID(byte[] name) {
        return nameBasedUID(name, root);
    }

    public static String createNameBasedUID(byte[] name, String root) {
        checkRoot(root);
        return nameBasedUID(name, root);
    }

    public static String createUID(String root) {
        checkRoot(root);
        return randomUID(root);
    }

    public static String createUIDIfNull(String uid) {
        return uid == null ? randomUID(root) : uid;
    }

    public static String createUIDIfNull(String uid, String root) {
        checkRoot(root);
        return uid == null ? randomUID(root) : uid;
    }

    public static String remapUID(String uid) {
        return nameBasedUID(uid.getBytes(ASCII), root);
    }

    public static String remapUID(String uid, String root) {
        checkRoot(root);
        return nameBasedUID(uid.getBytes(ASCII), root);
    }

    private static String randomUID(String root) {
        return toUID(root, UUID.randomUUID());
    }

    private static String nameBasedUID(byte[] name, String root) {
        return toUID(root, UUID.nameUUIDFromBytes(name));
    }

    private static String toUID(String root, UUID uuid) {
        byte[] b17 = new byte[17];
        ByteKit.longToBytesBE(uuid.getMostSignificantBits(), b17, 1);
        ByteKit.longToBytesBE(uuid.getLeastSignificantBits(), b17, 9);
        String uuidStr = new BigInteger(b17).toString();
        int rootlen = root.length();
        int uuidlen = uuidStr.length();
        char[] cs = new char[rootlen + uuidlen + 1];
        root.getChars(0, rootlen, cs, 0);
        cs[rootlen] = Symbol.C_DOT;
        uuidStr.getChars(0, uuidlen, cs, rootlen + 1);
        return new String(cs);
    }

    public static StringBuilder promptTo(String uid, StringBuilder sb) {
        return sb.append(uid).append(" - ").append(UID.nameOf(uid));
    }

    public static String[] findUIDs(String regex) {
        Pattern p = Pattern.compile(regex);
        Field[] fields = UID.class.getFields();
        String[] uids = new String[fields.length];
        int j = 0;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (p.matcher(field.getName()).matches())
                try {
                    uids[j++] = (String) field.get(null);
                } catch (Exception ignore) {
                }
        }
        return Arrays.copyOf(uids, j);
    }

    public static int remapUIDs(Attributes attrs, Map<String, String> uidMap) {
        return remapUIDs(attrs, uidMap, null);
    }

    /**
     * Replaces UIDs in Attributes according specified mapping.
     *
     * @param attrs    Attributes object which UIDs will be replaced
     * @param uidMap   Specified mapping
     * @param modified Attributes object to collect overwritten non-empty
     *                 attributes with original values or true
     * @return number of replaced UIDs
     */
    public static int remapUIDs(Attributes attrs, Map<String, String> uidMap, Attributes modified) {
        Visitor visitor = new Visitor(uidMap, modified);
        try {
            attrs.accept(visitor, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return visitor.replaced;
    }

    private static class Visitor implements Attributes.Visitor {
        private final Map<String, String> uidMap;
        private final Attributes modified;
        private int replaced;
        private int rootSeqTag;

        public Visitor(Map<String, String> uidMap, Attributes modified) {
            this.uidMap = uidMap;
            this.modified = modified;
        }

        @Override
        public boolean visit(Attributes attrs, int tag, VR vr, Object val) {
            if (vr != VR.UI || val == Value.NULL) {
                if (attrs.isRoot())
                    rootSeqTag = vr == VR.SQ ? tag : 0;
                return true;
            }

            String[] ss;
            if (val instanceof byte[]) {
                ss = attrs.getStrings(tag);
                val = ss.length == 1 ? ss[0] : ss;
            }
            if (val instanceof String[]) {
                ss = (String[]) val;
                for (int i = 0, c = 0; i < ss.length; i++) {
                    String uid = uidMap.get(ss[i]);
                    if (uid != null) {
                        if (c++ == 0)
                            modified(attrs, tag, vr, ss.clone());
                        ss[i] = uid;
                        replaced++;
                    }
                }
            } else {
                String uid = uidMap.get(val);
                if (uid != null) {
                    modified(attrs, tag, vr, val);
                    attrs.setString(tag, VR.UI, uid);
                    replaced++;
                }
            }
            return true;
        }

        private void modified(Attributes attrs, int tag, VR vr, Object val) {
            if (modified == null)
                return;

            if (rootSeqTag == 0) {
                modified.setValue(tag, vr, val);
            } else if (!modified.contains(rootSeqTag)) {
                Sequence src = attrs.getRoot().getSequence(rootSeqTag);
                Sequence dst = modified.newSequence(rootSeqTag, src.size());
                for (Attributes item : src) {
                    dst.add(new Attributes(item));
                }
            }
        }

    }

}
