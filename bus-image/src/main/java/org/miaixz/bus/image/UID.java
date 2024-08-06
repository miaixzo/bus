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
package org.miaixz.bus.image;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.UIDVisitor;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum UID {

    Verification("1.2.840.10008.1.1", "Verification SOP Class"),
    ImplicitVRLittleEndian("1.2.840.10008.1.2", "Implicit VR Little Endian"),
    ExplicitVRLittleEndian("1.2.840.10008.1.2.1", "Explicit VR Little Endian"),
    EncapsulatedUncompressedExplicitVRLittleEndian("1.2.840.10008.1.2.1.98",
            "Encapsulated Uncompressed Explicit VR Little Endian"),
    DeflatedExplicitVRLittleEndian("1.2.840.10008.1.2.1.99", "Deflated Explicit VR Little Endian"),
    ExplicitVRBigEndian("1.2.840.10008.1.2.2", "Explicit VR Big Endian (Retired)"),
    JPEGBaseline8Bit("1.2.840.10008.1.2.4.50", "JPEG Baseline (Process 1)"),
    JPEGExtended12Bit("1.2.840.10008.1.2.4.51", "JPEG Extended (Process 2 & 4)"),
    JPEGExtended35("1.2.840.10008.1.2.4.52", "JPEG Extended (Process 3 & 5) (Retired)"),
    JPEGSpectralSelectionNonHierarchical68("1.2.840.10008.1.2.4.53", "JPEG Spectral Selection"),
    JPEGSpectralSelectionNonHierarchical79("1.2.840.10008.1.2.4.54", "JPEG Spectral Selection"),
    JPEGFullProgressionNonHierarchical1012("1.2.840.10008.1.2.4.55", "JPEG Full Progression"),
    JPEGFullProgressionNonHierarchical1113("1.2.840.10008.1.2.4.56", "JPEG Full Progression"),
    JPEGLossless("1.2.840.10008.1.2.4.57", "JPEG Lossless"),
    JPEGLosslessNonHierarchical15("1.2.840.10008.1.2.4.58", "JPEG Lossless"),
    JPEGExtendedHierarchical1618("1.2.840.10008.1.2.4.59", "JPEG Extended"),
    JPEGExtendedHierarchical1719("1.2.840.10008.1.2.4.60", "JPEG Extended"),
    JPEGSpectralSelectionHierarchical2022("1.2.840.10008.1.2.4.61", "JPEG Spectral Selection"),
    JPEGSpectralSelectionHierarchical2123("1.2.840.10008.1.2.4.62", "JPEG Spectral Selection"),
    JPEGFullProgressionHierarchical2426("1.2.840.10008.1.2.4.63", "JPEG Full Progression"),
    JPEGFullProgressionHierarchical2527("1.2.840.10008.1.2.4.64", "JPEG Full Progression"),
    JPEGLosslessHierarchical28("1.2.840.10008.1.2.4.65", "JPEG Lossless"),
    JPEGLosslessHierarchical29("1.2.840.10008.1.2.4.66", "JPEG Lossless"),
    JPEGLosslessSV1("1.2.840.10008.1.2.4.70", "JPEG Lossless"),
    JPEGLSLossless("1.2.840.10008.1.2.4.80", "JPEG-LS Lossless Image Compression"),
    JPEGLSNearLossless("1.2.840.10008.1.2.4.81", "JPEG-LS Lossy (Near-Lossless) Image Compression"),
    JPEG2000Lossless("1.2.840.10008.1.2.4.90", "JPEG 2000 Image Compression (Lossless Only)"),
    JPEG2000("1.2.840.10008.1.2.4.91", "JPEG 2000 Image Compression"),
    JPEG2000MCLossless("1.2.840.10008.1.2.4.92", "JPEG 2000 Part 2 Multi-component Image Compression (Lossless Only)"),
    JPEG2000MC("1.2.840.10008.1.2.4.93", "JPEG 2000 Part 2 Multi-component Image Compression"),
    JPIPReferenced("1.2.840.10008.1.2.4.94", "JPIP Referenced"),
    JPIPReferencedDeflate("1.2.840.10008.1.2.4.95", "JPIP Referenced Deflate"),
    MPEG2MPML("1.2.840.10008.1.2.4.100", "MPEG2 Main Profile / Main Level"),
    MPEG2MPMLF("1.2.840.10008.1.2.4.100.1", "Fragmentable MPEG2 Main Profile / Main Level"),
    MPEG2MPHL("1.2.840.10008.1.2.4.101", "MPEG2 Main Profile / High Level"),
    MPEG2MPHLF("1.2.840.10008.1.2.4.101.1", "Fragmentable MPEG2 Main Profile / High Level"),
    MPEG4HP41("1.2.840.10008.1.2.4.102", "MPEG-4 AVC/H.264 High Profile / Level 4.1"),
    MPEG4HP41F("1.2.840.10008.1.2.4.102.1", "Fragmentable MPEG-4 AVC/H.264 High Profile / Level 4.1"),
    MPEG4HP41BD("1.2.840.10008.1.2.4.103", "MPEG-4 AVC/H.264 BD-compatible High Profile / Level 4.1"),
    MPEG4HP41BDF("1.2.840.10008.1.2.4.103.1", "Fragmentable MPEG-4 AVC/H.264 BD-compatible High Profile / Level 4.1"),
    MPEG4HP422D("1.2.840.10008.1.2.4.104", "MPEG-4 AVC/H.264 High Profile / Level 4.2 For 2D Video"),
    MPEG4HP422DF("1.2.840.10008.1.2.4.104.1", "Fragmentable MPEG-4 AVC/H.264 High Profile / Level 4.2 For 2D Video"),
    MPEG4HP423D("1.2.840.10008.1.2.4.105", "MPEG-4 AVC/H.264 High Profile / Level 4.2 For 3D Video"),
    MPEG4HP423DF("1.2.840.10008.1.2.4.105.1", "Fragmentable MPEG-4 AVC/H.264 High Profile / Level 4.2 For 3D Video"),
    MPEG4HP42STEREO("1.2.840.10008.1.2.4.106", "MPEG-4 AVC/H.264 Stereo High Profile / Level 4.2"),
    MPEG4HP42STEREOF("1.2.840.10008.1.2.4.106.1", "Fragmentable MPEG-4 AVC/H.264 Stereo High Profile / Level 4.2"),
    HEVCMP51("1.2.840.10008.1.2.4.107", "HEVC/H.265 Main Profile / Level 5.1"),
    HEVCM10P51("1.2.840.10008.1.2.4.108", "HEVC/H.265 Main 10 Profile / Level 5.1"),
    HTJ2KLossless("1.2.840.10008.1.2.4.201", "High-Throughput JPEG 2000 Image Compression (Lossless Only)"),
    HTJ2KLosslessRPCL("1.2.840.10008.1.2.4.202",
            "High-Throughput JPEG 2000 with RPCL Options Image Compression (Lossless Only)"),
    HTJ2K("1.2.840.10008.1.2.4.203", "High-Throughput JPEG 2000 Image Compression"),
    JPIPHTJ2KReferenced("1.2.840.10008.1.2.4.204", "JPIP HTJ2K Referenced"),
    JPIPHTJ2KReferencedDeflate("1.2.840.10008.1.2.4.205", "JPIP HTJ2K Referenced Deflate"),
    RLELossless("1.2.840.10008.1.2.5", "RLE Lossless"),
    RFC2557MIMEEncapsulation("1.2.840.10008.1.2.6.1", "RFC 2557 MIME encapsulation (Retired)"),
    XMLEncoding("1.2.840.10008.1.2.6.2", "XML Encoding (Retired)"),
    SMPTEST211020UncompressedProgressiveActiveVideo("1.2.840.10008.1.2.7.1",
            "SMPTE ST 2110-20 Uncompressed Progressive Active Video"),
    SMPTEST211020UncompressedInterlacedActiveVideo("1.2.840.10008.1.2.7.2",
            "SMPTE ST 2110-20 Uncompressed Interlaced Active Video"),
    SMPTEST211030PCMDigitalAudio("1.2.840.10008.1.2.7.3", "SMPTE ST 2110-30 PCM Digital Audio"),
    MediaStorageDirectoryStorage("1.2.840.10008.1.3.10", "Media Storage Directory Storage"),
    HotIronPalette("1.2.840.10008.1.5.1", "Hot Iron Color Palette SOP Instance"),
    PETPalette("1.2.840.10008.1.5.2", "PET Color Palette SOP Instance"),
    HotMetalBluePalette("1.2.840.10008.1.5.3", "Hot Metal Blue Color Palette SOP Instance"),
    PET20StepPalette("1.2.840.10008.1.5.4", "PET 20 Step Color Palette SOP Instance"),
    SpringPalette("1.2.840.10008.1.5.5", "Spring Color Palette SOP Instance"),
    SummerPalette("1.2.840.10008.1.5.6", "Summer Color Palette SOP Instance"),
    FallPalette("1.2.840.10008.1.5.7", "Fall Color Palette SOP Instance"),
    WinterPalette("1.2.840.10008.1.5.8", "Winter Color Palette SOP Instance"),
    BasicStudyContentNotification("1.2.840.10008.1.9", "Basic Study Content Notification SOP Class (Retired)"),
    Papyrus3ImplicitVRLittleEndian("1.2.840.10008.1.20", "Papyrus 3 Implicit VR Little Endian (Retired)"),
    StorageCommitmentPushModel("1.2.840.10008.1.20.1", "Storage Commitment Push Model SOP Class"),
    StorageCommitmentPushModelInstance("1.2.840.10008.1.20.1.1", "Storage Commitment Push Model SOP Instance"),
    StorageCommitmentPullModel("1.2.840.10008.1.20.2", "Storage Commitment Pull Model SOP Class (Retired)"),
    StorageCommitmentPullModelInstance("1.2.840.10008.1.20.2.1",
            "Storage Commitment Pull Model SOP Instance (Retired)"),
    ProceduralEventLogging("1.2.840.10008.1.40", "Procedural Event Logging SOP Class"),
    ProceduralEventLoggingInstance("1.2.840.10008.1.40.1", "Procedural Event Logging SOP Instance"),
    SubstanceAdministrationLogging("1.2.840.10008.1.42", "Substance Administration Logging SOP Class"),
    SubstanceAdministrationLoggingInstance("1.2.840.10008.1.42.1", "Substance Administration Logging SOP Instance"),
    DCMUID("1.2.840.10008.2.6.1", "DICOM UID Registry"), DCM("1.2.840.10008.2.16.4", "DICOM Controlled Terminology"),
    MA("1.2.840.10008.2.16.5", "Adult Mouse Anatomy Ontology"), UBERON("1.2.840.10008.2.16.6", "Uberon Ontology"),
    ITIS_TSN("1.2.840.10008.2.16.7", "Integrated Taxonomic Information System (ITIS) Taxonomic Serial Number (TSN)"),
    MGI("1.2.840.10008.2.16.8", "Mouse Genome Initiative (MGI)"),
    PUBCHEM_CID("1.2.840.10008.2.16.9", "PubChem Compound CID"), DC("1.2.840.10008.2.16.10", "Dublin Core"),
    NYUMCCG("1.2.840.10008.2.16.11", "New York University Melanoma Clinical Cooperative Group"),
    MAYONRISBSASRG("1.2.840.10008.2.16.12",
            "Mayo Clinic Non-radiological Images Specific Body Structure Anatomical Surface Region Guide"),
    IBSI("1.2.840.10008.2.16.13", "Image Biomarker Standardisation Initiative"),
    RO("1.2.840.10008.2.16.14", "Radiomics Ontology"), RADELEMENT("1.2.840.10008.2.16.15", "RadElement"),
    I11("1.2.840.10008.2.16.16", "ICD-11"),
    UNS("1.2.840.10008.2.16.17", "Unified numbering system (UNS) for metals and alloys"),
    RRID("1.2.840.10008.2.16.18", "Research Resource Identification"),
    DICOMApplicationContext("1.2.840.10008.3.1.1.1", "DICOM Application Context Name"),
    DetachedPatientManagement("1.2.840.10008.3.1.2.1.1", "Detached Patient Management SOP Class (Retired)"),
    DetachedPatientManagementMeta("1.2.840.10008.3.1.2.1.4", "Detached Patient Management Meta SOP Class (Retired)"),
    DetachedVisitManagement("1.2.840.10008.3.1.2.2.1", "Detached Visit Management SOP Class (Retired)"),
    DetachedStudyManagement("1.2.840.10008.3.1.2.3.1", "Detached Study Management SOP Class (Retired)"),
    StudyComponentManagement("1.2.840.10008.3.1.2.3.2", "Study Component Management SOP Class (Retired)"),
    ModalityPerformedProcedureStep("1.2.840.10008.3.1.2.3.3", "Modality Performed Procedure Step SOP Class"),
    ModalityPerformedProcedureStepRetrieve("1.2.840.10008.3.1.2.3.4",
            "Modality Performed Procedure Step Retrieve SOP Class"),
    ModalityPerformedProcedureStepNotification("1.2.840.10008.3.1.2.3.5",
            "Modality Performed Procedure Step Notification SOP Class"),
    DetachedResultsManagement("1.2.840.10008.3.1.2.5.1", "Detached Results Management SOP Class (Retired)"),
    DetachedResultsManagementMeta("1.2.840.10008.3.1.2.5.4", "Detached Results Management Meta SOP Class (Retired)"),
    DetachedStudyManagementMeta("1.2.840.10008.3.1.2.5.5", "Detached Study Management Meta SOP Class (Retired)"),
    DetachedInterpretationManagement("1.2.840.10008.3.1.2.6.1",
            "Detached Interpretation Management SOP Class (Retired)"),
    Storage("1.2.840.10008.4.2", "Storage Service Class"),
    BasicFilmSession("1.2.840.10008.5.1.1.1", "Basic Film Session SOP Class"),
    BasicFilmBox("1.2.840.10008.5.1.1.2", "Basic Film Box SOP Class"),
    BasicGrayscaleImageBox("1.2.840.10008.5.1.1.4", "Basic Grayscale Image Box SOP Class"),
    BasicColorImageBox("1.2.840.10008.5.1.1.4.1", "Basic Color Image Box SOP Class"),
    ReferencedImageBox("1.2.840.10008.5.1.1.4.2", "Referenced Image Box SOP Class (Retired)"),
    BasicGrayscalePrintManagementMeta("1.2.840.10008.5.1.1.9", "Basic Grayscale Print Management Meta SOP Class"),
    ReferencedGrayscalePrintManagementMeta("1.2.840.10008.5.1.1.9.1",
            "Referenced Grayscale Print Management Meta SOP Class (Retired)"),
    PrintJob("1.2.840.10008.5.1.1.14", "Print Job SOP Class"),
    BasicAnnotationBox("1.2.840.10008.5.1.1.15", "Basic Annotation Box SOP Class"),
    Printer("1.2.840.10008.5.1.1.16", "Printer SOP Class"),
    PrinterConfigurationRetrieval("1.2.840.10008.5.1.1.16.376", "Printer Configuration Retrieval SOP Class"),
    PrinterInstance("1.2.840.10008.5.1.1.17", "Printer SOP Instance"),
    PrinterConfigurationRetrievalInstance("1.2.840.10008.5.1.1.17.376", "Printer Configuration Retrieval SOP Instance"),
    BasicColorPrintManagementMeta("1.2.840.10008.5.1.1.18", "Basic Color Print Management Meta SOP Class"),
    ReferencedColorPrintManagementMeta("1.2.840.10008.5.1.1.18.1",
            "Referenced Color Print Management Meta SOP Class (Retired)"),
    VOILUTBox("1.2.840.10008.5.1.1.22", "VOI LUT Box SOP Class"),
    PresentationLUT("1.2.840.10008.5.1.1.23", "Presentation LUT SOP Class"),
    ImageOverlayBox("1.2.840.10008.5.1.1.24", "Image Overlay Box SOP Class (Retired)"),
    BasicPrintImageOverlayBox("1.2.840.10008.5.1.1.24.1", "Basic Print Image Overlay Box SOP Class (Retired)"),
    PrintQueueInstance("1.2.840.10008.5.1.1.25", "Print Queue SOP Instance (Retired)"),
    PrintQueueManagement("1.2.840.10008.5.1.1.26", "Print Queue Management SOP Class (Retired)"),
    StoredPrintStorage("1.2.840.10008.5.1.1.27", "Stored Print Storage SOP Class (Retired)"),
    HardcopyGrayscaleImageStorage("1.2.840.10008.5.1.1.29", "Hardcopy Grayscale Image Storage SOP Class (Retired)"),
    HardcopyColorImageStorage("1.2.840.10008.5.1.1.30", "Hardcopy Color Image Storage SOP Class (Retired)"),
    PullPrintRequest("1.2.840.10008.5.1.1.31", "Pull Print Request SOP Class (Retired)"),
    PullStoredPrintManagementMeta("1.2.840.10008.5.1.1.32", "Pull Stored Print Management Meta SOP Class (Retired)"),
    MediaCreationManagement("1.2.840.10008.5.1.1.33", "Media Creation Management SOP Class UID"),
    DisplaySystem("1.2.840.10008.5.1.1.40", "Display System SOP Class"),
    DisplaySystemInstance("1.2.840.10008.5.1.1.40.1", "Display System SOP Instance"),
    ComputedRadiographyImageStorage("1.2.840.10008.5.1.4.1.1.1", "Computed Radiography Image Storage"),
    DigitalXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.1.1",
            "Digital X-Ray Image Storage - For Presentation"),
    DigitalXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.1.1.1",
            "Digital X-Ray Image Storage - For Processing"),
    DigitalMammographyXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.1.2",
            "Digital Mammography X-Ray Image Storage - For Presentation"),
    DigitalMammographyXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.1.2.1",
            "Digital Mammography X-Ray Image Storage - For Processing"),
    DigitalIntraOralXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.1.3",
            "Digital Intra-Oral X-Ray Image Storage - For Presentation"),
    DigitalIntraOralXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.1.3.1",
            "Digital Intra-Oral X-Ray Image Storage - For Processing"),
    CTImageStorage("1.2.840.10008.5.1.4.1.1.2", "CT Image Storage"),
    EnhancedCTImageStorage("1.2.840.10008.5.1.4.1.1.2.1", "Enhanced CT Image Storage"),
    LegacyConvertedEnhancedCTImageStorage("1.2.840.10008.5.1.4.1.1.2.2", "Legacy Converted Enhanced CT Image Storage"),
    UltrasoundMultiFrameImageStorageRetired("1.2.840.10008.5.1.4.1.1.3",
            "Ultrasound Multi-frame Image Storage (Retired)"),
    UltrasoundMultiFrameImageStorage("1.2.840.10008.5.1.4.1.1.3.1", "Ultrasound Multi-frame Image Storage"),
    MRImageStorage("1.2.840.10008.5.1.4.1.1.4", "MR Image Storage"),
    EnhancedMRImageStorage("1.2.840.10008.5.1.4.1.1.4.1", "Enhanced MR Image Storage"),
    MRSpectroscopyStorage("1.2.840.10008.5.1.4.1.1.4.2", "MR Spectroscopy Storage"),
    EnhancedMRColorImageStorage("1.2.840.10008.5.1.4.1.1.4.3", "Enhanced MR Color Image Storage"),
    LegacyConvertedEnhancedMRImageStorage("1.2.840.10008.5.1.4.1.1.4.4", "Legacy Converted Enhanced MR Image Storage"),
    NuclearMedicineImageStorageRetired("1.2.840.10008.5.1.4.1.1.5", "Nuclear Medicine Image Storage (Retired)"),
    UltrasoundImageStorageRetired("1.2.840.10008.5.1.4.1.1.6", "Ultrasound Image Storage (Retired)"),
    UltrasoundImageStorage("1.2.840.10008.5.1.4.1.1.6.1", "Ultrasound Image Storage"),
    EnhancedUSVolumeStorage("1.2.840.10008.5.1.4.1.1.6.2", "Enhanced US Volume Storage"),
    PhotoacousticImageStorage("1.2.840.10008.5.1.4.1.1.6.3", "Photoacoustic Image Storage"),
    SecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7", "Secondary Capture Image Storage"),
    MultiFrameSingleBitSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.1",
            "Multi-frame Single Bit Secondary Capture Image Storage"),
    MultiFrameGrayscaleByteSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.2",
            "Multi-frame Grayscale Byte Secondary Capture Image Storage"),
    MultiFrameGrayscaleWordSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.3",
            "Multi-frame Grayscale Word Secondary Capture Image Storage"),
    MultiFrameTrueColorSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.4",
            "Multi-frame True Color Secondary Capture Image Storage"),
    StandaloneOverlayStorage("1.2.840.10008.5.1.4.1.1.8", "Standalone Overlay Storage (Retired)"),
    StandaloneCurveStorage("1.2.840.10008.5.1.4.1.1.9", "Standalone Curve Storage (Retired)"),
    WaveformStorageTrial("1.2.840.10008.5.1.4.1.1.9.1", "Waveform Storage - Trial (Retired)"),
    TwelveLeadECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.1", "12-lead ECG Waveform Storage"),
    GeneralECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.2", "General ECG Waveform Storage"),
    AmbulatoryECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.3", "Ambulatory ECG Waveform Storage"),
    General32bitECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.4", "General 32-bit ECG Waveform Storage"),
    HemodynamicWaveformStorage("1.2.840.10008.5.1.4.1.1.9.2.1", "Hemodynamic Waveform Storage"),
    CardiacElectrophysiologyWaveformStorage("1.2.840.10008.5.1.4.1.1.9.3.1",
            "Cardiac Electrophysiology Waveform Storage"),
    BasicVoiceAudioWaveformStorage("1.2.840.10008.5.1.4.1.1.9.4.1", "Basic Voice Audio Waveform Storage"),
    GeneralAudioWaveformStorage("1.2.840.10008.5.1.4.1.1.9.4.2", "General Audio Waveform Storage"),
    ArterialPulseWaveformStorage("1.2.840.10008.5.1.4.1.1.9.5.1", "Arterial Pulse Waveform Storage"),
    RespiratoryWaveformStorage("1.2.840.10008.5.1.4.1.1.9.6.1", "Respiratory Waveform Storage"),
    MultichannelRespiratoryWaveformStorage("1.2.840.10008.5.1.4.1.1.9.6.2",
            "Multi-channel Respiratory Waveform Storage"),
    RoutineScalpElectroencephalogramWaveformStorage("1.2.840.10008.5.1.4.1.1.9.7.1",
            "Routine Scalp Electroencephalogram Waveform Storage"),
    ElectromyogramWaveformStorage("1.2.840.10008.5.1.4.1.1.9.7.2", "Electromyogram Waveform Storage"),
    ElectrooculogramWaveformStorage("1.2.840.10008.5.1.4.1.1.9.7.3", "Electrooculogram Waveform Storage"),
    SleepElectroencephalogramWaveformStorage("1.2.840.10008.5.1.4.1.1.9.7.4",
            "Sleep Electroencephalogram Waveform Storage"),
    BodyPositionWaveformStorage("1.2.840.10008.5.1.4.1.1.9.8.1", "Body Position Waveform Storage"),
    StandaloneModalityLUTStorage("1.2.840.10008.5.1.4.1.1.10", "Standalone Modality LUT Storage (Retired)"),
    StandaloneVOILUTStorage("1.2.840.10008.5.1.4.1.1.11", "Standalone VOI LUT Storage (Retired)"),
    GrayscaleSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.1",
            "Grayscale Softcopy Presentation State Storage"),
    ColorSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.2", "Color Softcopy Presentation State Storage"),
    PseudoColorSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.3",
            "Pseudo-Color Softcopy Presentation State Storage"),
    BlendingSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.4",
            "Blending Softcopy Presentation State Storage"),
    XAXRFGrayscaleSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.5",
            "XA/XRF Grayscale Softcopy Presentation State Storage"),
    GrayscalePlanarMPRVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.6",
            "Grayscale Planar MPR Volumetric Presentation State Storage"),
    CompositingPlanarMPRVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.7",
            "Compositing Planar MPR Volumetric Presentation State Storage"),
    AdvancedBlendingPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.8",
            "Advanced Blending Presentation State Storage"),
    VolumeRenderingVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.9",
            "Volume Rendering Volumetric Presentation State Storage"),
    SegmentedVolumeRenderingVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.10",
            "Segmented Volume Rendering Volumetric Presentation State Storage"),
    MultipleVolumeRenderingVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.11",
            "Multiple Volume Rendering Volumetric Presentation State Storage"),
    VariableModalityLUTSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.12",
            "Variable Modality LUT Softcopy Presentation State Storage"),
    XRayAngiographicImageStorage("1.2.840.10008.5.1.4.1.1.12.1", "X-Ray Angiographic Image Storage"),
    EnhancedXAImageStorage("1.2.840.10008.5.1.4.1.1.12.1.1", "Enhanced XA Image Storage"),
    XRayRadiofluoroscopicImageStorage("1.2.840.10008.5.1.4.1.1.12.2", "X-Ray Radiofluoroscopic Image Storage"),
    EnhancedXRFImageStorage("1.2.840.10008.5.1.4.1.1.12.2.1", "Enhanced XRF Image Storage"),
    XRayAngiographicBiPlaneImageStorage("1.2.840.10008.5.1.4.1.1.12.3",
            "X-Ray Angiographic Bi-Plane Image Storage (Retired)"),
    ZeissOPTFile("1.2.840.10008.5.1.4.1.1.12.77", "Zeiss OPT File (Retired)"),
    XRay3DAngiographicImageStorage("1.2.840.10008.5.1.4.1.1.13.1.1", "X-Ray 3D Angiographic Image Storage"),
    XRay3DCraniofacialImageStorage("1.2.840.10008.5.1.4.1.1.13.1.2", "X-Ray 3D Craniofacial Image Storage"),
    BreastTomosynthesisImageStorage("1.2.840.10008.5.1.4.1.1.13.1.3", "Breast Tomosynthesis Image Storage"),
    BreastProjectionXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.13.1.4",
            "Breast Projection X-Ray Image Storage - For Presentation"),
    BreastProjectionXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.13.1.5",
            "Breast Projection X-Ray Image Storage - For Processing"),
    IntravascularOpticalCoherenceTomographyImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.14.1",
            "Intravascular Optical Coherence Tomography Image Storage - For Presentation"),
    IntravascularOpticalCoherenceTomographyImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.14.2",
            "Intravascular Optical Coherence Tomography Image Storage - For Processing"),
    NuclearMedicineImageStorage("1.2.840.10008.5.1.4.1.1.20", "Nuclear Medicine Image Storage"),
    ParametricMapStorage("1.2.840.10008.5.1.4.1.1.30", "Parametric Map Storage"),
    MRImageStorageZeroPadded("1.2.840.10008.5.1.4.1.1.40", "MR Image Storage Zero Padded (Retired)"),
    RawDataStorage("1.2.840.10008.5.1.4.1.1.66", "Raw Data Storage"),
    SpatialRegistrationStorage("1.2.840.10008.5.1.4.1.1.66.1", "Spatial Registration Storage"),
    SpatialFiducialsStorage("1.2.840.10008.5.1.4.1.1.66.2", "Spatial Fiducials Storage"),
    DeformableSpatialRegistrationStorage("1.2.840.10008.5.1.4.1.1.66.3", "Deformable Spatial Registration Storage"),
    SegmentationStorage("1.2.840.10008.5.1.4.1.1.66.4", "Segmentation Storage"),
    SurfaceSegmentationStorage("1.2.840.10008.5.1.4.1.1.66.5", "Surface Segmentation Storage"),
    TractographyResultsStorage("1.2.840.10008.5.1.4.1.1.66.6", "Tractography Results Storage"),
    RealWorldValueMappingStorage("1.2.840.10008.5.1.4.1.1.67", "Real World Value Mapping Storage"),
    SurfaceScanMeshStorage("1.2.840.10008.5.1.4.1.1.68.1", "Surface Scan Mesh Storage"),
    SurfaceScanPointCloudStorage("1.2.840.10008.5.1.4.1.1.68.2", "Surface Scan Point Cloud Storage"),
    VLImageStorageTrial("1.2.840.10008.5.1.4.1.1.77.1", "VL Image Storage - Trial (Retired)"),
    VLMultiFrameImageStorageTrial("1.2.840.10008.5.1.4.1.1.77.2", "VL Multi-frame Image Storage - Trial (Retired)"),
    VLEndoscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.1", "VL Endoscopic Image Storage"),
    VideoEndoscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.1.1", "Video Endoscopic Image Storage"),
    VLMicroscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.2", "VL Microscopic Image Storage"),
    VideoMicroscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.2.1", "Video Microscopic Image Storage"),
    VLSlideCoordinatesMicroscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.3",
            "VL Slide-Coordinates Microscopic Image Storage"),
    VLPhotographicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.4", "VL Photographic Image Storage"),
    VideoPhotographicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.4.1", "Video Photographic Image Storage"),
    OphthalmicPhotography8BitImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.1",
            "Ophthalmic Photography 8 Bit Image Storage"),
    OphthalmicPhotography16BitImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.2",
            "Ophthalmic Photography 16 Bit Image Storage"),
    StereometricRelationshipStorage("1.2.840.10008.5.1.4.1.1.77.1.5.3", "Stereometric Relationship Storage"),
    OphthalmicTomographyImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.4", "Ophthalmic Tomography Image Storage"),
    WideFieldOphthalmicPhotographyStereographicProjectionImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.5",
            "Wide Field Ophthalmic Photography Stereographic Projection Image Storage"),
    WideFieldOphthalmicPhotography3DCoordinatesImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.6",
            "Wide Field Ophthalmic Photography 3D Coordinates Image Storage"),
    OphthalmicOpticalCoherenceTomographyEnFaceImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.7",
            "Ophthalmic Optical Coherence Tomography En Face Image Storage"),
    OphthalmicOpticalCoherenceTomographyBscanVolumeAnalysisStorage("1.2.840.10008.5.1.4.1.1.77.1.5.8",
            "Ophthalmic Optical Coherence Tomography B-scan Volume Analysis Storage"),
    VLWholeSlideMicroscopyImageStorage("1.2.840.10008.5.1.4.1.1.77.1.6", "VL Whole Slide Microscopy Image Storage"),
    DermoscopicPhotographyImageStorage("1.2.840.10008.5.1.4.1.1.77.1.7", "Dermoscopic Photography Image Storage"),
    ConfocalMicroscopyImageStorage("1.2.840.10008.5.1.4.1.1.77.1.8", "Confocal Microscopy Image Storage"),
    ConfocalMicroscopyTiledPyramidalImageStorage("1.2.840.10008.5.1.4.1.1.77.1.9",
            "Confocal Microscopy Tiled Pyramidal Image Storage"),
    LensometryMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.1", "Lensometry Measurements Storage"),
    AutorefractionMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.2", "Autorefraction Measurements Storage"),
    KeratometryMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.3", "Keratometry Measurements Storage"),
    SubjectiveRefractionMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.4",
            "Subjective Refraction Measurements Storage"),
    VisualAcuityMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.5", "Visual Acuity Measurements Storage"),
    SpectaclePrescriptionReportStorage("1.2.840.10008.5.1.4.1.1.78.6", "Spectacle Prescription Report Storage"),
    OphthalmicAxialMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.7", "Ophthalmic Axial Measurements Storage"),
    IntraocularLensCalculationsStorage("1.2.840.10008.5.1.4.1.1.78.8", "Intraocular Lens Calculations Storage"),
    MacularGridThicknessAndVolumeReportStorage("1.2.840.10008.5.1.4.1.1.79.1",
            "Macular Grid Thickness and Volume Report Storage"),
    OphthalmicVisualFieldStaticPerimetryMeasurementsStorage("1.2.840.10008.5.1.4.1.1.80.1",
            "Ophthalmic Visual Field Static Perimetry Measurements Storage"),
    OphthalmicThicknessMapStorage("1.2.840.10008.5.1.4.1.1.81.1", "Ophthalmic Thickness Map Storage"),
    CornealTopographyMapStorage("1.2.840.10008.5.1.4.1.1.82.1", "Corneal Topography Map Storage"),
    TextSRStorageTrial("1.2.840.10008.5.1.4.1.1.88.1", "Text SR Storage - Trial (Retired)"),
    AudioSRStorageTrial("1.2.840.10008.5.1.4.1.1.88.2", "Audio SR Storage - Trial (Retired)"),
    DetailSRStorageTrial("1.2.840.10008.5.1.4.1.1.88.3", "Detail SR Storage - Trial (Retired)"),
    ComprehensiveSRStorageTrial("1.2.840.10008.5.1.4.1.1.88.4", "Comprehensive SR Storage - Trial (Retired)"),
    BasicTextSRStorage("1.2.840.10008.5.1.4.1.1.88.11", "Basic Text SR Storage"),
    EnhancedSRStorage("1.2.840.10008.5.1.4.1.1.88.22", "Enhanced SR Storage"),
    ComprehensiveSRStorage("1.2.840.10008.5.1.4.1.1.88.33", "Comprehensive SR Storage"),
    Comprehensive3DSRStorage("1.2.840.10008.5.1.4.1.1.88.34", "Comprehensive 3D SR Storage"),
    ExtensibleSRStorage("1.2.840.10008.5.1.4.1.1.88.35", "Extensible SR Storage"),
    ProcedureLogStorage("1.2.840.10008.5.1.4.1.1.88.40", "Procedure Log Storage"),
    MammographyCADSRStorage("1.2.840.10008.5.1.4.1.1.88.50", "Mammography CAD SR Storage"),
    KeyObjectSelectionDocumentStorage("1.2.840.10008.5.1.4.1.1.88.59", "Key Object Selection Document Storage"),
    ChestCADSRStorage("1.2.840.10008.5.1.4.1.1.88.65", "Chest CAD SR Storage"),
    XRayRadiationDoseSRStorage("1.2.840.10008.5.1.4.1.1.88.67", "X-Ray Radiation Dose SR Storage"),
    RadiopharmaceuticalRadiationDoseSRStorage("1.2.840.10008.5.1.4.1.1.88.68",
            "Radiopharmaceutical Radiation Dose SR Storage"),
    ColonCADSRStorage("1.2.840.10008.5.1.4.1.1.88.69", "Colon CAD SR Storage"),
    ImplantationPlanSRStorage("1.2.840.10008.5.1.4.1.1.88.70", "Implantation Plan SR Storage"),
    AcquisitionContextSRStorage("1.2.840.10008.5.1.4.1.1.88.71", "Acquisition Context SR Storage"),
    SimplifiedAdultEchoSRStorage("1.2.840.10008.5.1.4.1.1.88.72", "Simplified Adult Echo SR Storage"),
    PatientRadiationDoseSRStorage("1.2.840.10008.5.1.4.1.1.88.73", "Patient Radiation Dose SR Storage"),
    PlannedImagingAgentAdministrationSRStorage("1.2.840.10008.5.1.4.1.1.88.74",
            "Planned Imaging Agent Administration SR Storage"),
    PerformedImagingAgentAdministrationSRStorage("1.2.840.10008.5.1.4.1.1.88.75",
            "Performed Imaging Agent Administration SR Storage"),
    EnhancedXRayRadiationDoseSRStorage("1.2.840.10008.5.1.4.1.1.88.76", "Enhanced X-Ray Radiation Dose SR Storage"),
    ContentAssessmentResultsStorage("1.2.840.10008.5.1.4.1.1.90.1", "Content Assessment Results Storage"),
    MicroscopyBulkSimpleAnnotationsStorage("1.2.840.10008.5.1.4.1.1.91.1",
            "Microscopy Bulk Simple Annotations Storage"),
    EncapsulatedPDFStorage("1.2.840.10008.5.1.4.1.1.104.1", "Encapsulated PDF Storage"),
    EncapsulatedCDAStorage("1.2.840.10008.5.1.4.1.1.104.2", "Encapsulated CDA Storage"),
    EncapsulatedSTLStorage("1.2.840.10008.5.1.4.1.1.104.3", "Encapsulated STL Storage"),
    EncapsulatedOBJStorage("1.2.840.10008.5.1.4.1.1.104.4", "Encapsulated OBJ Storage"),
    EncapsulatedMTLStorage("1.2.840.10008.5.1.4.1.1.104.5", "Encapsulated MTL Storage"),
    PositronEmissionTomographyImageStorage("1.2.840.10008.5.1.4.1.1.128", "Positron Emission Tomography Image Storage"),
    LegacyConvertedEnhancedPETImageStorage("1.2.840.10008.5.1.4.1.1.128.1",
            "Legacy Converted Enhanced PET Image Storage"),
    StandalonePETCurveStorage("1.2.840.10008.5.1.4.1.1.129", "Standalone PET Curve Storage (Retired)"),
    EnhancedPETImageStorage("1.2.840.10008.5.1.4.1.1.130", "Enhanced PET Image Storage"),
    BasicStructuredDisplayStorage("1.2.840.10008.5.1.4.1.1.131", "Basic Structured Display Storage"),
    CTDefinedProcedureProtocolStorage("1.2.840.10008.5.1.4.1.1.200.1", "CT Defined Procedure Protocol Storage"),
    CTPerformedProcedureProtocolStorage("1.2.840.10008.5.1.4.1.1.200.2", "CT Performed Procedure Protocol Storage"),
    ProtocolApprovalStorage("1.2.840.10008.5.1.4.1.1.200.3", "Protocol Approval Storage"),
    ProtocolApprovalInformationModelFind("1.2.840.10008.5.1.4.1.1.200.4", "Protocol Approval Information Model - FIND"),
    ProtocolApprovalInformationModelMove("1.2.840.10008.5.1.4.1.1.200.5", "Protocol Approval Information Model - MOVE"),
    ProtocolApprovalInformationModelGet("1.2.840.10008.5.1.4.1.1.200.6", "Protocol Approval Information Model - GET"),
    XADefinedProcedureProtocolStorage("1.2.840.10008.5.1.4.1.1.200.7", "XA Defined Procedure Protocol Storage"),
    XAPerformedProcedureProtocolStorage("1.2.840.10008.5.1.4.1.1.200.8", "XA Performed Procedure Protocol Storage"),
    InventoryStorage("1.2.840.10008.5.1.4.1.1.201.1", "Inventory Storage"),
    InventoryFind("1.2.840.10008.5.1.4.1.1.201.2", "Inventory - FIND"),
    InventoryMove("1.2.840.10008.5.1.4.1.1.201.3", "Inventory - MOVE"),
    InventoryGet("1.2.840.10008.5.1.4.1.1.201.4", "Inventory - GET"),
    InventoryCreation("1.2.840.10008.5.1.4.1.1.201.5", "Inventory Creation"),
    RepositoryQuery("1.2.840.10008.5.1.4.1.1.201.6", "Repository Query"),
    StorageManagementInstance("1.2.840.10008.5.1.4.1.1.201.1.1", "Storage Management SOP Instance"),
    RTImageStorage("1.2.840.10008.5.1.4.1.1.481.1", "RT Image Storage"),
    RTDoseStorage("1.2.840.10008.5.1.4.1.1.481.2", "RT Dose Storage"),
    RTStructureSetStorage("1.2.840.10008.5.1.4.1.1.481.3", "RT Structure Set Storage"),
    RTBeamsTreatmentRecordStorage("1.2.840.10008.5.1.4.1.1.481.4", "RT Beams Treatment Record Storage"),
    RTPlanStorage("1.2.840.10008.5.1.4.1.1.481.5", "RT Plan Storage"),
    RTBrachyTreatmentRecordStorage("1.2.840.10008.5.1.4.1.1.481.6", "RT Brachy Treatment Record Storage"),
    RTTreatmentSummaryRecordStorage("1.2.840.10008.5.1.4.1.1.481.7", "RT Treatment Summary Record Storage"),
    RTIonPlanStorage("1.2.840.10008.5.1.4.1.1.481.8", "RT Ion Plan Storage"),
    RTIonBeamsTreatmentRecordStorage("1.2.840.10008.5.1.4.1.1.481.9", "RT Ion Beams Treatment Record Storage"),
    RTPhysicianIntentStorage("1.2.840.10008.5.1.4.1.1.481.10", "RT Physician Intent Storage"),
    RTSegmentAnnotationStorage("1.2.840.10008.5.1.4.1.1.481.11", "RT Segment Annotation Storage"),
    RTRadiationSetStorage("1.2.840.10008.5.1.4.1.1.481.12", "RT Radiation Set Storage"),
    CArmPhotonElectronRadiationStorage("1.2.840.10008.5.1.4.1.1.481.13", "C-Arm Photon-Electron Radiation Storage"),
    TomotherapeuticRadiationStorage("1.2.840.10008.5.1.4.1.1.481.14", "Tomotherapeutic Radiation Storage"),
    RoboticArmRadiationStorage("1.2.840.10008.5.1.4.1.1.481.15", "Robotic-Arm Radiation Storage"),
    RTRadiationRecordSetStorage("1.2.840.10008.5.1.4.1.1.481.16", "RT Radiation Record Set Storage"),
    RTRadiationSalvageRecordStorage("1.2.840.10008.5.1.4.1.1.481.17", "RT Radiation Salvage Record Storage"),
    TomotherapeuticRadiationRecordStorage("1.2.840.10008.5.1.4.1.1.481.18", "Tomotherapeutic Radiation Record Storage"),
    CArmPhotonElectronRadiationRecordStorage("1.2.840.10008.5.1.4.1.1.481.19",
            "C-Arm Photon-Electron Radiation Record Storage"),
    RoboticRadiationRecordStorage("1.2.840.10008.5.1.4.1.1.481.20", "Robotic Radiation Record Storage"),
    RTRadiationSetDeliveryInstructionStorage("1.2.840.10008.5.1.4.1.1.481.21",
            "RT Radiation Set Delivery Instruction Storage"),
    RTTreatmentPreparationStorage("1.2.840.10008.5.1.4.1.1.481.22", "RT Treatment Preparation Storage"),
    EnhancedRTImageStorage("1.2.840.10008.5.1.4.1.1.481.23", "Enhanced RT Image Storage"),
    EnhancedContinuousRTImageStorage("1.2.840.10008.5.1.4.1.1.481.24", "Enhanced Continuous RT Image Storage"),
    RTPatientPositionAcquisitionInstructionStorage("1.2.840.10008.5.1.4.1.1.481.25",
            "RT Patient Position Acquisition Instruction Storage"),
    DICOSCTImageStorage("1.2.840.10008.5.1.4.1.1.501.1", "DICOS CT Image Storage"),
    DICOSDigitalXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.501.2.1",
            "DICOS Digital X-Ray Image Storage - For Presentation"),
    DICOSDigitalXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.501.2.2",
            "DICOS Digital X-Ray Image Storage - For Processing"),
    DICOSThreatDetectionReportStorage("1.2.840.10008.5.1.4.1.1.501.3", "DICOS Threat Detection Report Storage"),
    DICOS2DAITStorage("1.2.840.10008.5.1.4.1.1.501.4", "DICOS 2D AIT Storage"),
    DICOS3DAITStorage("1.2.840.10008.5.1.4.1.1.501.5", "DICOS 3D AIT Storage"),
    DICOSQuadrupoleResonanceStorage("1.2.840.10008.5.1.4.1.1.501.6", "DICOS Quadrupole Resonance (QR) Storage"),
    EddyCurrentImageStorage("1.2.840.10008.5.1.4.1.1.601.1", "Eddy Current Image Storage"),
    EddyCurrentMultiFrameImageStorage("1.2.840.10008.5.1.4.1.1.601.2", "Eddy Current Multi-frame Image Storage"),
    PatientRootQueryRetrieveInformationModelFind("1.2.840.10008.5.1.4.1.2.1.1",
            "Patient Root Query/Retrieve Information Model - FIND"),
    PatientRootQueryRetrieveInformationModelMove("1.2.840.10008.5.1.4.1.2.1.2",
            "Patient Root Query/Retrieve Information Model - MOVE"),
    PatientRootQueryRetrieveInformationModelGet("1.2.840.10008.5.1.4.1.2.1.3",
            "Patient Root Query/Retrieve Information Model - GET"),
    StudyRootQueryRetrieveInformationModelFind("1.2.840.10008.5.1.4.1.2.2.1",
            "Study Root Query/Retrieve Information Model - FIND"),
    StudyRootQueryRetrieveInformationModelMove("1.2.840.10008.5.1.4.1.2.2.2",
            "Study Root Query/Retrieve Information Model - MOVE"),
    StudyRootQueryRetrieveInformationModelGet("1.2.840.10008.5.1.4.1.2.2.3",
            "Study Root Query/Retrieve Information Model - GET"),
    PatientStudyOnlyQueryRetrieveInformationModelFind("1.2.840.10008.5.1.4.1.2.3.1",
            "Patient/Study Only Query/Retrieve Information Model - FIND (Retired)"),
    PatientStudyOnlyQueryRetrieveInformationModelMove("1.2.840.10008.5.1.4.1.2.3.2",
            "Patient/Study Only Query/Retrieve Information Model - MOVE (Retired)"),
    PatientStudyOnlyQueryRetrieveInformationModelGet("1.2.840.10008.5.1.4.1.2.3.3",
            "Patient/Study Only Query/Retrieve Information Model - GET (Retired)"),
    CompositeInstanceRootRetrieveMove("1.2.840.10008.5.1.4.1.2.4.2", "Composite Instance Root Retrieve - MOVE"),
    CompositeInstanceRootRetrieveGet("1.2.840.10008.5.1.4.1.2.4.3", "Composite Instance Root Retrieve - GET"),
    CompositeInstanceRetrieveWithoutBulkDataGet("1.2.840.10008.5.1.4.1.2.5.3",
            "Composite Instance Retrieve Without Bulk Data - GET"),
    DefinedProcedureProtocolInformationModelFind("1.2.840.10008.5.1.4.20.1",
            "Defined Procedure Protocol Information Model - FIND"),
    DefinedProcedureProtocolInformationModelMove("1.2.840.10008.5.1.4.20.2",
            "Defined Procedure Protocol Information Model - MOVE"),
    DefinedProcedureProtocolInformationModelGet("1.2.840.10008.5.1.4.20.3",
            "Defined Procedure Protocol Information Model - GET"),
    ModalityWorklistInformationModelFind("1.2.840.10008.5.1.4.31", "Modality Worklist Information Model - FIND"),
    GeneralPurposeWorklistManagementMeta("1.2.840.10008.5.1.4.32",
            "General Purpose Worklist Management Meta SOP Class (Retired)"),
    GeneralPurposeWorklistInformationModelFind("1.2.840.10008.5.1.4.32.1",
            "General Purpose Worklist Information Model - FIND (Retired)"),
    GeneralPurposeScheduledProcedureStep("1.2.840.10008.5.1.4.32.2",
            "General Purpose Scheduled Procedure Step SOP Class (Retired)"),
    GeneralPurposePerformedProcedureStep("1.2.840.10008.5.1.4.32.3",
            "General Purpose Performed Procedure Step SOP Class (Retired)"),
    InstanceAvailabilityNotification("1.2.840.10008.5.1.4.33", "Instance Availability Notification SOP Class"),
    RTBeamsDeliveryInstructionStorageTrial("1.2.840.10008.5.1.4.34.1",
            "RT Beams Delivery Instruction Storage - Trial (Retired)"),
    RTConventionalMachineVerificationTrial("1.2.840.10008.5.1.4.34.2",
            "RT Conventional Machine Verification - Trial (Retired)"),
    RTIonMachineVerificationTrial("1.2.840.10008.5.1.4.34.3", "RT Ion Machine Verification - Trial (Retired)"),
    UnifiedWorklistAndProcedureStepTrial("1.2.840.10008.5.1.4.34.4",
            "Unified Worklist and Procedure Step Service Class - Trial (Retired)"),
    UnifiedProcedureStepPushTrial("1.2.840.10008.5.1.4.34.4.1",
            "Unified Procedure Step - Push SOP Class - Trial (Retired)"),
    UnifiedProcedureStepWatchTrial("1.2.840.10008.5.1.4.34.4.2",
            "Unified Procedure Step - Watch SOP Class - Trial (Retired)"),
    UnifiedProcedureStepPullTrial("1.2.840.10008.5.1.4.34.4.3",
            "Unified Procedure Step - Pull SOP Class - Trial (Retired)"),
    UnifiedProcedureStepEventTrial("1.2.840.10008.5.1.4.34.4.4",
            "Unified Procedure Step - Event SOP Class - Trial (Retired)"),
    UPSGlobalSubscriptionInstance("1.2.840.10008.5.1.4.34.5", "UPS Global Subscription SOP Instance"),
    UPSFilteredGlobalSubscriptionInstance("1.2.840.10008.5.1.4.34.5.1",
            "UPS Filtered Global Subscription SOP Instance"),
    UnifiedWorklistAndProcedureStep("1.2.840.10008.5.1.4.34.6", "Unified Worklist and Procedure Step Service Class"),
    UnifiedProcedureStepPush("1.2.840.10008.5.1.4.34.6.1", "Unified Procedure Step - Push SOP Class"),
    UnifiedProcedureStepWatch("1.2.840.10008.5.1.4.34.6.2", "Unified Procedure Step - Watch SOP Class"),
    UnifiedProcedureStepPull("1.2.840.10008.5.1.4.34.6.3", "Unified Procedure Step - Pull SOP Class"),
    UnifiedProcedureStepEvent("1.2.840.10008.5.1.4.34.6.4", "Unified Procedure Step - Event SOP Class"),
    UnifiedProcedureStepQuery("1.2.840.10008.5.1.4.34.6.5", "Unified Procedure Step - Query SOP Class"),
    RTBeamsDeliveryInstructionStorage("1.2.840.10008.5.1.4.34.7", "RT Beams Delivery Instruction Storage"),
    RTConventionalMachineVerification("1.2.840.10008.5.1.4.34.8", "RT Conventional Machine Verification"),
    RTIonMachineVerification("1.2.840.10008.5.1.4.34.9", "RT Ion Machine Verification"),
    RTBrachyApplicationSetupDeliveryInstructionStorage("1.2.840.10008.5.1.4.34.10",
            "RT Brachy Application Setup Delivery Instruction Storage"),
    GeneralRelevantPatientInformationQuery("1.2.840.10008.5.1.4.37.1", "General Relevant Patient Information Query"),
    BreastImagingRelevantPatientInformationQuery("1.2.840.10008.5.1.4.37.2",
            "Breast Imaging Relevant Patient Information Query"),
    CardiacRelevantPatientInformationQuery("1.2.840.10008.5.1.4.37.3", "Cardiac Relevant Patient Information Query"),
    HangingProtocolStorage("1.2.840.10008.5.1.4.38.1", "Hanging Protocol Storage"),
    HangingProtocolInformationModelFind("1.2.840.10008.5.1.4.38.2", "Hanging Protocol Information Model - FIND"),
    HangingProtocolInformationModelMove("1.2.840.10008.5.1.4.38.3", "Hanging Protocol Information Model - MOVE"),
    HangingProtocolInformationModelGet("1.2.840.10008.5.1.4.38.4", "Hanging Protocol Information Model - GET"),
    ColorPaletteStorage("1.2.840.10008.5.1.4.39.1", "Color Palette Storage"),
    ColorPaletteQueryRetrieveInformationModelFind("1.2.840.10008.5.1.4.39.2",
            "Color Palette Query/Retrieve Information Model - FIND"),
    ColorPaletteQueryRetrieveInformationModelMove("1.2.840.10008.5.1.4.39.3",
            "Color Palette Query/Retrieve Information Model - MOVE"),
    ColorPaletteQueryRetrieveInformationModelGet("1.2.840.10008.5.1.4.39.4",
            "Color Palette Query/Retrieve Information Model - GET"),
    ProductCharacteristicsQuery("1.2.840.10008.5.1.4.41", "Product Characteristics Query SOP Class"),
    SubstanceApprovalQuery("1.2.840.10008.5.1.4.42", "Substance Approval Query SOP Class"),
    GenericImplantTemplateStorage("1.2.840.10008.5.1.4.43.1", "Generic Implant Template Storage"),
    GenericImplantTemplateInformationModelFind("1.2.840.10008.5.1.4.43.2",
            "Generic Implant Template Information Model - FIND"),
    GenericImplantTemplateInformationModelMove("1.2.840.10008.5.1.4.43.3",
            "Generic Implant Template Information Model - MOVE"),
    GenericImplantTemplateInformationModelGet("1.2.840.10008.5.1.4.43.4",
            "Generic Implant Template Information Model - GET"),
    ImplantAssemblyTemplateStorage("1.2.840.10008.5.1.4.44.1", "Implant Assembly Template Storage"),
    ImplantAssemblyTemplateInformationModelFind("1.2.840.10008.5.1.4.44.2",
            "Implant Assembly Template Information Model - FIND"),
    ImplantAssemblyTemplateInformationModelMove("1.2.840.10008.5.1.4.44.3",
            "Implant Assembly Template Information Model - MOVE"),
    ImplantAssemblyTemplateInformationModelGet("1.2.840.10008.5.1.4.44.4",
            "Implant Assembly Template Information Model - GET"),
    ImplantTemplateGroupStorage("1.2.840.10008.5.1.4.45.1", "Implant Template Group Storage"),
    ImplantTemplateGroupInformationModelFind("1.2.840.10008.5.1.4.45.2",
            "Implant Template Group Information Model - FIND"),
    ImplantTemplateGroupInformationModelMove("1.2.840.10008.5.1.4.45.3",
            "Implant Template Group Information Model - MOVE"),
    ImplantTemplateGroupInformationModelGet("1.2.840.10008.5.1.4.45.4",
            "Implant Template Group Information Model - GET"),
    NativeDICOMModel("1.2.840.10008.7.1.1", "Native DICOM Model"),
    AbstractMultiDimensionalImageModel("1.2.840.10008.7.1.2", "Abstract Multi-Dimensional Image Model"),
    DICOMContentMappingResource("1.2.840.10008.8.1.1", "DICOM Content Mapping Resource"),
    VideoEndoscopicImageRealTimeCommunication("1.2.840.10008.10.1", "Video Endoscopic Image Real-Time Communication"),
    VideoPhotographicImageRealTimeCommunication("1.2.840.10008.10.2",
            "Video Photographic Image Real-Time Communication"),
    AudioWaveformRealTimeCommunication("1.2.840.10008.10.3", "Audio Waveform Real-Time Communication"),
    RenditionSelectionDocumentRealTimeCommunication("1.2.840.10008.10.4",
            "Rendition Selection Document Real-Time Communication"),
    dicomDeviceName("1.2.840.10008.15.0.3.1", "dicomDeviceName"),
    dicomDescription("1.2.840.10008.15.0.3.2", "dicomDescription"),
    dicomManufacturer("1.2.840.10008.15.0.3.3", "dicomManufacturer"),
    dicomManufacturerModelName("1.2.840.10008.15.0.3.4", "dicomManufacturerModelName"),
    dicomSoftwareVersion("1.2.840.10008.15.0.3.5", "dicomSoftwareVersion"),
    dicomVendorData("1.2.840.10008.15.0.3.6", "dicomVendorData"),
    dicomAETitle("1.2.840.10008.15.0.3.7", "dicomAETitle"),
    dicomNetworkConnectionReference("1.2.840.10008.15.0.3.8", "dicomNetworkConnectionReference"),
    dicomApplicationCluster("1.2.840.10008.15.0.3.9", "dicomApplicationCluster"),
    dicomAssociationInitiator("1.2.840.10008.15.0.3.10", "dicomAssociationInitiator"),
    dicomAssociationAcceptor("1.2.840.10008.15.0.3.11", "dicomAssociationAcceptor"),
    dicomHostname("1.2.840.10008.15.0.3.12", "dicomHostname"), dicomPort("1.2.840.10008.15.0.3.13", "dicomPort"),
    dicomSOPClass("1.2.840.10008.15.0.3.14", "dicomSOPClass"),
    dicomTransferRole("1.2.840.10008.15.0.3.15", "dicomTransferRole"),
    dicomTransferSyntax("1.2.840.10008.15.0.3.16", "dicomTransferSyntax"),
    dicomPrimaryDeviceType("1.2.840.10008.15.0.3.17", "dicomPrimaryDeviceType"),
    dicomRelatedDeviceReference("1.2.840.10008.15.0.3.18", "dicomRelatedDeviceReference"),
    dicomPreferredCalledAETitle("1.2.840.10008.15.0.3.19", "dicomPreferredCalledAETitle"),
    dicomTLSCyphersuite("1.2.840.10008.15.0.3.20", "dicomTLSCyphersuite"),
    dicomAuthorizedNodeCertificateReference("1.2.840.10008.15.0.3.21", "dicomAuthorizedNodeCertificateReference"),
    dicomThisNodeCertificateReference("1.2.840.10008.15.0.3.22", "dicomThisNodeCertificateReference"),
    dicomInstalled("1.2.840.10008.15.0.3.23", "dicomInstalled"),
    dicomStationName("1.2.840.10008.15.0.3.24", "dicomStationName"),
    dicomDeviceSerialNumber("1.2.840.10008.15.0.3.25", "dicomDeviceSerialNumber"),
    dicomInstitutionName("1.2.840.10008.15.0.3.26", "dicomInstitutionName"),
    dicomInstitutionAddress("1.2.840.10008.15.0.3.27", "dicomInstitutionAddress"),
    dicomInstitutionDepartmentName("1.2.840.10008.15.0.3.28", "dicomInstitutionDepartmentName"),
    dicomIssuerOfPatientID("1.2.840.10008.15.0.3.29", "dicomIssuerOfPatientID"),
    dicomPreferredCallingAETitle("1.2.840.10008.15.0.3.30", "dicomPreferredCallingAETitle"),
    dicomSupportedCharacterSet("1.2.840.10008.15.0.3.31", "dicomSupportedCharacterSet"),
    dicomConfigurationRoot("1.2.840.10008.15.0.4.1", "dicomConfigurationRoot"),
    dicomDevicesRoot("1.2.840.10008.15.0.4.2", "dicomDevicesRoot"),
    dicomUniqueAETitlesRegistryRoot("1.2.840.10008.15.0.4.3", "dicomUniqueAETitlesRegistryRoot"),
    dicomDevice("1.2.840.10008.15.0.4.4", "dicomDevice"), dicomNetworkAE("1.2.840.10008.15.0.4.5", "dicomNetworkAE"),
    dicomNetworkConnection("1.2.840.10008.15.0.4.6", "dicomNetworkConnection"),
    dicomUniqueAETitle("1.2.840.10008.15.0.4.7", "dicomUniqueAETitle"),
    dicomTransferCapability("1.2.840.10008.15.0.4.8", "dicomTransferCapability"),
    UTC("1.2.840.10008.15.1.1", "Universal Coordinated Time"),
    PrivateEncapsulatedGenozipStorage("1.2.40.0.13.1.5.1.4.1.1.104.1", "Private Encapsulated Genozip Storage"),
    PrivateEncapsulatedBzip2VCFStorage("1.2.40.0.13.1.5.1.4.1.1.104.2", "Private Encapsulated Bzip2 VCF Storage"),
    PrivateEncapsulatedBzip2DocumentStorage("1.2.40.0.13.1.5.1.4.1.1.104.3",
            "Private Encapsulated Bzip2 Document Storage"),
    PrivateAgfaBasicAttributePresentationState("1.2.124.113532.3500.7",
            "Private Agfa Basic Attribute Presentation State"),
    PrivateAgfaArrivalTransaction("1.2.124.113532.3500.8.1", "Private Agfa Arrival Transaction"),
    PrivateAgfaDictationTransaction("1.2.124.113532.3500.8.2", "Private Agfa Dictation Transaction"),
    PrivateAgfaReportTranscriptionTransaction("1.2.124.113532.3500.8.3",
            "Private Agfa Report Transcription Transaction"),
    PrivateAgfaReportApprovalTransaction("1.2.124.113532.3500.8.4", "Private Agfa Report Approval Transaction"),
    PrivateTomTecAnnotationStorage("1.2.276.0.48.5.1.4.1.1.7", "Private TomTec Annotation Storage"),
    PrivateToshibaUSImageStorage("1.2.392.200036.9116.7.8.1.1.1", "Private Toshiba US Image Storage"),
    PrivateFujiCRImageStorage("1.2.392.200036.9125.1.1.2", "Private Fuji CR Image Storage"),
    PrivateGECollageStorage("1.2.528.1.1001.5.1.1.1", "Private GE Collage Storage"),
    PrivateERADPracticeBuilderReportTextStorage("1.2.826.0.1.3680043.293.1.0.1",
            "Private ERAD Practice Builder Report Text Storage"),
    PrivateERADPracticeBuilderReportDictationStorage("1.2.826.0.1.3680043.293.1.0.2",
            "Private ERAD Practice Builder Report Dictation Storage"),
    PrivatePhilipsHPLive3D01Storage("1.2.840.113543.6.6.1.3.10001", "Private Philips HP Live 3D 01 Storage"),
    PrivatePhilipsHPLive3D02Storage("1.2.840.113543.6.6.1.3.10002", "Private Philips HP Live 3D 02 Storage"),
    PrivateGE3DModelStorage("1.2.840.113619.4.26", "Private GE 3D Model Storage"),
    PrivateGEDicomCTImageInfoObject("1.2.840.113619.4.3", "Private GE Dicom CT Image Info Object"),
    PrivateGEDicomDisplayImageInfoObject("1.2.840.113619.4.4", "Private GE Dicom Display Image Info Object"),
    PrivateGEDicomMRImageInfoObject("1.2.840.113619.4.2", "Private GE Dicom MR Image Info Object"),
    PrivateGEeNTEGRAProtocolOrNMGenieStorage("1.2.840.113619.4.27", "Private GE eNTEGRA Protocol or NM Genie Storage"),
    PrivateGEPETRawDataStorage("1.2.840.113619.4.30", "Private GE PET Raw Data Storage"),
    PrivateGERTPlanStorage("1.2.840.113619.4.5.249", "Private GE RT Plan Storage"),
    PrivatePixelMedLegacyConvertedEnhancedCTImageStorage("1.3.6.1.4.1.5962.301.1",
            "Private PixelMed Legacy Converted Enhanced CT Image Storage"),
    PrivatePixelMedLegacyConvertedEnhancedMRImageStorage("1.3.6.1.4.1.5962.301.2",
            "Private PixelMed Legacy Converted Enhanced MR Image Storage"),
    PrivatePixelMedLegacyConvertedEnhancedPETImageStorage("1.3.6.1.4.1.5962.301.3",
            "Private PixelMed Legacy Converted Enhanced PET Image Storage"),
    PrivatePixelMedFloatingPointImageStorage("1.3.6.1.4.1.5962.301.9", "Private PixelMed Floating Point Image Storage"),
    PrivateSiemensCSANonImageStorage("1.3.12.2.1107.5.9.1", "Private Siemens CSA Non Image Storage"),
    PrivateSiemensCTMRVolumeStorage("1.3.12.2.1107.5.99.3.10", "Private Siemens CT MR Volume Storage"),
    PrivateSiemensAXFrameSetsStorage("1.3.12.2.1107.5.99.3.11", "Private Siemens AX Frame Sets Storage"),
    PrivatePhilipsSpecialisedXAStorage("1.3.46.670589.2.3.1.1", "Private Philips Specialised XA Storage"),
    PrivatePhilipsCXImageStorage("1.3.46.670589.2.4.1.1", "Private Philips CX Image Storage"),
    PrivatePhilips3DPresentationStateStorage("1.3.46.670589.2.5.1.1", "Private Philips 3D Presentation State Storage"),
    PrivatePhilipsVRMLStorage("1.3.46.670589.2.8.1.1", "Private Philips VRML Storage"),
    PrivatePhilipsVolumeSetStorage("1.3.46.670589.2.11.1.1", "Private Philips Volume Set Storage"),
    PrivatePhilipsVolumeStorageRetired("1.3.46.670589.5.0.1", "Private Philips Volume Storage (Retired)"),
    PrivatePhilipsVolumeStorage("1.3.46.670589.5.0.1.1", "Private Philips Volume Storage"),
    PrivatePhilips3DObjectStorageRetired("1.3.46.670589.5.0.2", "Private Philips 3D Object Storage (Retired)"),
    PrivatePhilips3DObjectStorage("1.3.46.670589.5.0.2.1", "Private Philips 3D Object Storage"),
    PrivatePhilipsSurfaceStorageRetired("1.3.46.670589.5.0.3", "Private Philips Surface Storage (Retired)"),
    PrivatePhilipsSurfaceStorage("1.3.46.670589.5.0.3.1", "Private Philips Surface Storage"),
    PrivatePhilipsCompositeObjectStorage("1.3.46.670589.5.0.4", "Private Philips Composite Object Storage"),
    PrivatePhilipsMRCardioProfileStorage("1.3.46.670589.5.0.7", "Private Philips MR Cardio Profile Storage"),
    PrivatePhilipsMRCardioStorageRetired("1.3.46.670589.5.0.8", "Private Philips MR Cardio Storage (Retired)"),
    PrivatePhilipsMRCardioStorage("1.3.46.670589.5.0.8.1", "Private Philips MR Cardio Storage"),
    PrivatePhilipsCTSyntheticImageStorage("1.3.46.670589.5.0.9", "Private Philips CT Synthetic Image Storage"),
    PrivatePhilipsMRSyntheticImageStorage("1.3.46.670589.5.0.10", "Private Philips MR Synthetic Image Storage"),
    PrivatePhilipsMRCardioAnalysisStorageRetired("1.3.46.670589.5.0.11",
            "Private Philips MR Cardio Analysis Storage (Retired)"),
    PrivatePhilipsMRCardioAnalysisStorage("1.3.46.670589.5.0.11.1", "Private Philips MR Cardio Analysis Storage"),
    PrivatePhilipsCXSyntheticImageStorage("1.3.46.670589.5.0.12", "Private Philips CX Synthetic Image Storage"),
    PrivatePhilipsPerfusionStorage("1.3.46.670589.5.0.13", "Private Philips Perfusion Storage"),
    PrivatePhilipsPerfusionImageStorage("1.3.46.670589.5.0.14", "Private Philips Perfusion Image Storage"),
    PrivatePhilipsXRayMFStorage("1.3.46.670589.7.8.1618510091", "Private Philips X-Ray MF Storage"),
    PrivatePhilipsLiveRunStorage("1.3.46.670589.7.8.1618510092", "Private Philips Live Run Storage"),
    PrivatePhilipsRunStorage("1.3.46.670589.7.8.16185100129", "Private Philips Run Storage"),
    PrivatePhilipsReconstructionStorage("1.3.46.670589.7.8.16185100130", "Private Philips Reconstruction Storage"),
    PrivatePhilipsMRSpectrumStorage("1.3.46.670589.11.0.0.12.1", "Private Philips MR Spectrum Storage"),
    PrivatePhilipsMRSeriesDataStorage("1.3.46.670589.11.0.0.12.2", "Private Philips MR Series Data Storage"),
    PrivatePhilipsMRColorImageStorage("1.3.46.670589.11.0.0.12.3", "Private Philips MR Color Image Storage"),
    PrivatePhilipsMRExamcardStorage("1.3.46.670589.11.0.0.12.4", "Private Philips MR Examcard Storage"),
    PrivatePMODMultiFrameImageStorage("2.16.840.1.114033.5.1.4.1.1.130", "Private PMOD Multi-frame Image Storage");

    public static final Pattern PATTERN = Pattern.compile("[012]((\\.0)|(\\.[1-9]\\d*))+");
    public static final Charset ASCII = StandardCharsets.US_ASCII;
    /**
     * UID root for UUIDs (Universally Unique Identifiers) generated in accordance with Rec. ITU-T X.667 | ISO/IEC
     * 9834-8.
     *
     * @see <a href="http://www.oid-info.com/get/2.25">OID repository {joint-iso-itu-t(2) uuid(25)}</a>
     */
    private static final String UUID_ROOT = "2.25";
    public static String root = UUID_ROOT;

    public final String uid;
    public final String desc;

    /**
     * 构造
     *
     * @param uid  编码
     * @param desc 描述
     */
    UID(String uid, String desc) {
        this.uid = uid;
        this.desc = desc;
    }

    /**
     * get uid
     *
     * @param uid uid
     * @return this
     */
    public static UID from(String uid) {
        for (UID u : UID.values()) {
            if (u.uid.equals(uid)) {
                return u;
            }
        }
        return null;
    }

    /**
     * get desc for uid
     *
     * @param uid uid
     * @return the string
     */
    public static String nameOf(String uid) {
        for (UID u : UID.values()) {
            if (Objects.equals(u.uid, uid)) {
                return u.desc;
            }
        }
        return "?";
    }

    public static String forName(String keyword) {
        try {
            return (String) UID.class.getField(keyword).get(null);
        } catch (Exception var2) {
            throw new IllegalArgumentException(keyword);
        }
    }

    public static String toUID(String uid) {
        uid = uid.trim();
        return (uid.equals("*") || Character.isDigit(uid.charAt(0))) ? uid : forName(uid);
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
        cs[rootlen] = '.';
        uuidStr.getChars(0, uuidlen, cs, rootlen + 1);
        return new String(cs);
    }

    public static String[] toUIDs(String s) {
        if (s.equals("*")) {
            return new String[] { "*" };
        }

        String[] uids = StringKit.splitToArray(s, ",");
        for (int i = 0; i < uids.length; i++) {
            uids[i] = toUID(uids[i]);
        }
        return uids;
    }

    private static String[] findUIDs(String regex) {
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

    public static String remapUID(String uid) {
        return nameBasedUID(uid.getBytes(ASCII), root);
    }

    public static String remapUID(String uid, String root) {
        checkRoot(root);
        return nameBasedUID(uid.getBytes(ASCII), root);
    }

    public static int remapUID(Attributes attrs, Map<String, String> uidMap) {
        return remapUIDs(attrs, uidMap, null);
    }

    /**
     * Replaces UIDs in <code>Attributes</code> according specified mapping.
     *
     * @param attrs    Attributes object which UIDs will be replaced
     * @param uidMap   Specified mapping
     * @param modified Attributes object to collect overwritten non-empty attributes with original values or null
     * @return number of replaced UIDs
     */
    private static int remapUIDs(Attributes attrs, Map<String, String> uidMap, Attributes modified) {
        UIDVisitor UIDVisitor = new UIDVisitor(uidMap, modified);
        try {
            attrs.accept(UIDVisitor, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return UIDVisitor.replaced;
    }

    private static String randomUID(String root) {
        return toUID(root, UUID.randomUUID());
    }

    private static String nameBasedUID(byte[] name, String root) {
        return toUID(root, UUID.nameUUIDFromBytes(name));
    }

    public static StringBuilder promptTo(String uid, StringBuilder sb) {
        return sb.append(uid).append(" - ").append(nameOf(uid));
    }

    public static String createUID() {
        return randomUID(root);
    }

    public static String createUID(String root) {
        checkRoot(root);
        return randomUID(root);
    }

    public static String createNameBasedUID(byte[] name) {
        return nameBasedUID(name, root);
    }

    private static String createNameBasedUID(byte[] name, String root) {
        checkRoot(root);
        return nameBasedUID(name, root);
    }

    public static String createUIDIfNull(String uid) {
        return uid == null ? randomUID(root) : uid;
    }

    private static String createUIDIfNull(String uid, String root) {
        checkRoot(root);
        return uid == null ? randomUID(root) : uid;
    }

    private static boolean isValid1(String uid) {
        return uid.length() <= 64 && PATTERN.matcher(uid).matches();
    }

    private static final String getRoot() {
        return root;
    }

    private static final void setRoot(String root) {
        checkRoot(root);
        UID.root = root;
    }

    private static void checkRoot(String root) {
        if (root.length() > 24)
            throw new IllegalArgumentException("root length > 24");
        if (!isValid1(root))
            throw new IllegalArgumentException(root);
    }

    public String getUid() {
        return uid;
    }

    public String getDesc() {
        return desc;
    }

}
