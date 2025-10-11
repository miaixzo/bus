/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.UIDVisitor;

/**
 * An enumeration of DICOM Unique Identifiers (UIDs).
 *
 * <p>
 * This enum defines various UIDs used in the DICOM standard, including SOP Classes, Transfer Syntaxes, Information
 * Models, and more. Each enum constant holds a UID string and its corresponding description.
 * </p>
 *
 * <p>
 * Additionally, this class provides utility methods for creating, validating, and transforming UIDs. This includes
 * methods for generating UIDs based on UUIDs and for remapping UIDs in DICOM attributes according to a specified
 * mapping.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum UID {

    /**
     * Verification SOP Class.
     */
    Verification("1.2.840.10008.1.1", "Verification SOP Class"),
    /**
     * Implicit VR Little Endian Transfer Syntax.
     */
    ImplicitVRLittleEndian("1.2.840.10008.1.2", "Implicit VR Little Endian"),
    /**
     * Explicit VR Little Endian Transfer Syntax.
     */
    ExplicitVRLittleEndian("1.2.840.10008.1.2.1", "Explicit VR Little Endian"),
    /**
     * Encapsulated Uncompressed Explicit VR Little Endian Transfer Syntax.
     */
    EncapsulatedUncompressedExplicitVRLittleEndian("1.2.840.10008.1.2.1.98",
            "Encapsulated Uncompressed Explicit VR Little Endian"),
    /**
     * Deflated Explicit VR Little Endian Transfer Syntax.
     */
    DeflatedExplicitVRLittleEndian("1.2.840.10008.1.2.1.99", "Deflated Explicit VR Little Endian"),
    /**
     * Explicit VR Big Endian Transfer Syntax (Retired).
     */
    ExplicitVRBigEndian("1.2.840.10008.1.2.2", "Explicit VR Big Endian (Retired)"),
    /**
     * JPEG Baseline (Process 1) Image Compression.
     */
    JPEGBaseline8Bit("1.2.840.10008.1.2.4.50", "JPEG Baseline (Process 1)"),
    /**
     * JPEG Extended (Process 2 &amp; 4) Image Compression.
     */
    JPEGExtended12Bit("1.2.840.10008.1.2.4.51", "JPEG Extended (Process 2 & 4)"),
    /**
     * JPEG Extended (Process 3 &amp; 5) Image Compression (Retired).
     */
    JPEGExtended35("1.2.840.10008.1.2.4.52", "JPEG Extended (Process 3 & 5) (Retired)"),
    /**
     * JPEG Spectral Selection, Non-Hierarchical (Process 6 &amp; 8) Image Compression.
     */
    JPEGSpectralSelectionNonHierarchical68("1.2.840.10008.1.2.4.53", "JPEG Spectral Selection"),
    /**
     * JPEG Spectral Selection, Non-Hierarchical (Process 7 &amp; 9) Image Compression.
     */
    JPEGSpectralSelectionNonHierarchical79("1.2.840.10008.1.2.4.54", "JPEG Spectral Selection"),
    /**
     * JPEG Full Progression, Non-Hierarchical (Process 10 &amp; 12) Image Compression.
     */
    JPEGFullProgressionNonHierarchical1012("1.2.840.10008.1.2.4.55", "JPEG Full Progression"),
    /**
     * JPEG Full Progression, Non-Hierarchical (Process 11 &amp; 13) Image Compression.
     */
    JPEGFullProgressionNonHierarchical1113("1.2.840.10008.1.2.4.56", "JPEG Full Progression"),
    /**
     * JPEG Lossless, Non-Hierarchical (Process 14) Image Compression.
     */
    JPEGLossless("1.2.840.10008.1.2.4.57", "JPEG Lossless"),
    /**
     * JPEG Lossless, Non-Hierarchical, First-Order Prediction (Process 14 [Selection Value 1]) Image Compression.
     */
    JPEGLosslessNonHierarchical15("1.2.840.10008.1.2.4.58", "JPEG Lossless"),
    /**
     * JPEG Extended, Hierarchical (Process 16 &amp; 18) Image Compression.
     */
    JPEGExtendedHierarchical1618("1.2.840.10008.1.2.4.59", "JPEG Extended"),
    /**
     * JPEG Extended, Hierarchical (Process 17 &amp; 19) Image Compression.
     */
    JPEGExtendedHierarchical1719("1.2.840.10008.1.2.4.60", "JPEG Extended"),
    /**
     * JPEG Spectral Selection, Hierarchical (Process 20 &amp; 22) Image Compression.
     */
    JPEGSpectralSelectionHierarchical2022("1.2.840.10008.1.2.4.61", "JPEG Spectral Selection"),
    /**
     * JPEG Spectral Selection, Hierarchical (Process 21 &amp; 23) Image Compression.
     */
    JPEGSpectralSelectionHierarchical2123("1.2.840.10008.1.2.4.62", "JPEG Spectral Selection"),
    /**
     * JPEG Full Progression, Hierarchical (Process 24 &amp; 26) Image Compression.
     */
    JPEGFullProgressionHierarchical2426("1.2.840.10008.1.2.4.63", "JPEG Full Progression"),
    /**
     * JPEG Full Progression, Hierarchical (Process 25 &amp; 27) Image Compression.
     */
    JPEGFullProgressionHierarchical2527("1.2.840.10008.1.2.4.64", "JPEG Full Progression"),
    /**
     * JPEG Lossless, Hierarchical (Process 28) Image Compression.
     */
    JPEGLosslessHierarchical28("1.2.840.10008.1.2.4.65", "JPEG Lossless"),
    /**
     * JPEG Lossless, Hierarchical (Process 29) Image Compression.
     */
    JPEGLosslessHierarchical29("1.2.840.10008.1.2.4.66", "JPEG Lossless"),
    /**
     * JPEG Lossless, Non-Hierarchical, First-Order Prediction (Process 14) Image Compression.
     */
    JPEGLosslessSV1("1.2.840.10008.1.2.4.70", "JPEG Lossless"),
    /**
     * JPEG-LS Lossless Image Compression.
     */
    JPEGLSLossless("1.2.840.10008.1.2.4.80", "JPEG-LS Lossless Image Compression"),
    /**
     * JPEG-LS Lossy (Near-Lossless) Image Compression.
     */
    JPEGLSNearLossless("1.2.840.10008.1.2.4.81", "JPEG-LS Lossy (Near-Lossless) Image Compression"),
    /**
     * JPEG 2000 Image Compression (Lossless Only).
     */
    JPEG2000Lossless("1.2.840.10008.1.2.4.90", "JPEG 2000 Image Compression (Lossless Only)"),
    /**
     * JPEG 2000 Image Compression.
     */
    JPEG2000("1.2.840.10008.1.2.4.91", "JPEG 2000 Image Compression"),
    /**
     * JPEG 2000 Part 2 Multi-component Image Compression (Lossless Only).
     */
    JPEG2000MCLossless("1.2.840.10008.1.2.4.92", "JPEG 2000 Part 2 Multi-component Image Compression (Lossless Only)"),
    /**
     * JPEG 2000 Part 2 Multi-component Image Compression.
     */
    JPEG2000MC("1.2.840.10008.1.2.4.93", "JPEG 2000 Part 2 Multi-component Image Compression"),
    /**
     * JPIP Referenced Transfer Syntax.
     */
    JPIPReferenced("1.2.840.10008.1.2.4.94", "JPIP Referenced"),
    /**
     * JPIP Referenced Deflate Transfer Syntax.
     */
    JPIPReferencedDeflate("1.2.840.10008.1.2.4.95", "JPIP Referenced Deflate"),
    /**
     * MPEG2 Main Profile / Main Level Video Compression.
     */
    MPEG2MPML("1.2.840.10008.1.2.4.100", "MPEG2 Main Profile / Main Level"),
    /**
     * Fragmentable MPEG2 Main Profile / Main Level.
     */
    MPEG2MPMLF("1.2.840.10008.1.2.4.100.1", "Fragmentable MPEG2 Main Profile / Main Level"),
    /**
     * MPEG2 Main Profile / High Level Video Compression.
     */
    MPEG2MPHL("1.2.840.10008.1.2.4.101", "MPEG2 Main Profile / High Level"),
    /**
     * Fragmentable MPEG2 Main Profile / High Level.
     */
    MPEG2MPHLF("1.2.840.10008.1.2.4.101.1", "Fragmentable MPEG2 Main Profile / High Level"),
    /**
     * MPEG-4 AVC/H.264 High Profile / Level 4.1 Video Compression.
     */
    MPEG4HP41("1.2.840.10008.1.2.4.102", "MPEG-4 AVC/H.264 High Profile / Level 4.1"),
    /**
     * Fragmentable MPEG-4 AVC/H.264 High Profile / Level 4.1.
     */
    MPEG4HP41F("1.2.840.10008.1.2.4.102.1", "Fragmentable MPEG-4 AVC/H.264 High Profile / Level 4.1"),
    /**
     * MPEG-4 AVC/H.264 BD-compatible High Profile / Level 4.1.
     */
    MPEG4HP41BD("1.2.840.10008.1.2.4.103", "MPEG-4 AVC/H.264 BD-compatible High Profile / Level 4.1"),
    /**
     * Fragmentable MPEG-4 AVC/H.264 BD-compatible High Profile / Level 4.1.
     */
    MPEG4HP41BDF("1.2.840.10008.1.2.4.103.1", "Fragmentable MPEG-4 AVC/H.264 BD-compatible High Profile / Level 4.1"),
    /**
     * MPEG-4 AVC/H.264 High Profile / Level 4.2 For 2D Video.
     */
    MPEG4HP422D("1.2.840.10008.1.2.4.104", "MPEG-4 AVC/H.264 High Profile / Level 4.2 For 2D Video"),
    /**
     * Fragmentable MPEG-4 AVC/H.264 High Profile / Level 4.2 For 2D Video.
     */
    MPEG4HP422DF("1.2.840.10008.1.2.4.104.1", "Fragmentable MPEG-4 AVC/H.264 High Profile / Level 4.2 For 2D Video"),
    /**
     * MPEG-4 AVC/H.264 High Profile / Level 4.2 For 3D Video.
     */
    MPEG4HP423D("1.2.840.10008.1.2.4.105", "MPEG-4 AVC/H.264 High Profile / Level 4.2 For 3D Video"),
    /**
     * Fragmentable MPEG-4 AVC/H.264 High Profile / Level 4.2 For 3D Video.
     */
    MPEG4HP423DF("1.2.840.10008.1.2.4.105.1", "Fragmentable MPEG-4 AVC/H.264 High Profile / Level 4.2 For 3D Video"),
    /**
     * MPEG-4 AVC/H.264 Stereo High Profile / Level 4.2.
     */
    MPEG4HP42STEREO("1.2.840.10008.1.2.4.106", "MPEG-4 AVC/H.264 Stereo High Profile / Level 4.2"),
    /**
     * Fragmentable MPEG-4 AVC/H.264 Stereo High Profile / Level 4.2.
     */
    MPEG4HP42STEREOF("1.2.840.10008.1.2.4.106.1", "Fragmentable MPEG-4 AVC/H.264 Stereo High Profile / Level 4.2"),
    /**
     * HEVC/H.265 Main Profile / Level 5.1.
     */
    HEVCMP51("1.2.840.10008.1.2.4.107", "HEVC/H.265 Main Profile / Level 5.1"),
    /**
     * HEVC/H.265 Main 10 Profile / Level 5.1.
     */
    HEVCM10P51("1.2.840.10008.1.2.4.108", "HEVC/H.265 Main 10 Profile / Level 5.1"),
    /**
     * High-Throughput JPEG 2000 Image Compression (Lossless Only).
     */
    HTJ2KLossless("1.2.840.10008.1.2.4.201", "High-Throughput JPEG 2000 Image Compression (Lossless Only)"),
    /**
     * High-Throughput JPEG 2000 with RPCL Options Image Compression (Lossless Only).
     */
    HTJ2KLosslessRPCL("1.2.840.10008.1.2.4.202",
            "High-Throughput JPEG 2000 with RPCL Options Image Compression (Lossless Only)"),
    /**
     * High-Throughput JPEG 2000 Image Compression.
     */
    HTJ2K("1.2.840.10008.1.2.4.203", "High-Throughput JPEG 2000 Image Compression"),
    /**
     * JPIP HTJ2K Referenced Transfer Syntax.
     */
    JPIPHTJ2KReferenced("1.2.840.10008.1.2.4.204", "JPIP HTJ2K Referenced"),
    /**
     * JPIP HTJ2K Referenced Deflate Transfer Syntax.
     */
    JPIPHTJ2KReferencedDeflate("1.2.840.10008.1.2.4.205", "JPIP HTJ2K Referenced Deflate"),
    /**
     * RLE Lossless Compression.
     */
    RLELossless("1.2.840.10008.1.2.5", "RLE Lossless"),
    /**
     * RFC 2557 MIME encapsulation (Retired).
     */
    RFC2557MIMEEncapsulation("1.2.840.10008.1.2.6.1", "RFC 2557 MIME encapsulation (Retired)"),
    /**
     * XML Encoding (Retired).
     */
    XMLEncoding("1.2.840.10008.1.2.6.2", "XML Encoding (Retired)"),
    /**
     * SMPTE ST 2110-20 Uncompressed Progressive Active Video.
     */
    SMPTEST211020UncompressedProgressiveActiveVideo("1.2.840.10008.1.2.7.1",
            "SMPTE ST 2110-20 Uncompressed Progressive Active Video"),
    /**
     * SMPTE ST 2110-20 Uncompressed Interlaced Active Video.
     */
    SMPTEST211020UncompressedInterlacedActiveVideo("1.2.840.10008.1.2.7.2",
            "SMPTE ST 2110-20 Uncompressed Interlaced Active Video"),
    /**
     * SMPTE ST 2110-30 PCM Digital Audio.
     */
    SMPTEST211030PCMDigitalAudio("1.2.840.10008.1.2.7.3", "SMPTE ST 2110-30 PCM Digital Audio"),
    /**
     * Media Storage Directory Storage SOP Class.
     */
    MediaStorageDirectoryStorage("1.2.840.10008.1.3.10", "Media Storage Directory Storage"),
    /**
     * Hot Iron Color Palette SOP Instance.
     */
    HotIronPalette("1.2.840.10008.1.5.1", "Hot Iron Color Palette SOP Instance"),
    /**
     * PET Color Palette SOP Instance.
     */
    PETPalette("1.2.840.10008.1.5.2", "PET Color Palette SOP Instance"),
    /**
     * Hot Metal Blue Color Palette SOP Instance.
     */
    HotMetalBluePalette("1.2.840.10008.1.5.3", "Hot Metal Blue Color Palette SOP Instance"),
    /**
     * PET 20 Step Color Palette SOP Instance.
     */
    PET20StepPalette("1.2.840.10008.1.5.4", "PET 20 Step Color Palette SOP Instance"),
    /**
     * Spring Color Palette SOP Instance.
     */
    SpringPalette("1.2.840.10008.1.5.5", "Spring Color Palette SOP Instance"),
    /**
     * Summer Color Palette SOP Instance.
     */
    SummerPalette("1.2.840.10008.1.5.6", "Summer Color Palette SOP Instance"),
    /**
     * Fall Color Palette SOP Instance.
     */
    FallPalette("1.2.840.10008.1.5.7", "Fall Color Palette SOP Instance"),
    /**
     * Winter Color Palette SOP Instance.
     */
    WinterPalette("1.2.840.10008.1.5.8", "Winter Color Palette SOP Instance"),
    /**
     * Basic Study Content Notification SOP Class (Retired).
     */
    BasicStudyContentNotification("1.2.840.10008.1.9", "Basic Study Content Notification SOP Class (Retired)"),
    /**
     * Papyrus 3 Implicit VR Little Endian (Retired).
     */
    Papyrus3ImplicitVRLittleEndian("1.2.840.10008.1.20", "Papyrus 3 Implicit VR Little Endian (Retired)"),
    /**
     * Storage Commitment Push Model SOP Class.
     */
    StorageCommitmentPushModel("1.2.840.10008.1.20.1", "Storage Commitment Push Model SOP Class"),
    /**
     * Storage Commitment Push Model SOP Instance.
     */
    StorageCommitmentPushModelInstance("1.2.840.10008.1.20.1.1", "Storage Commitment Push Model SOP Instance"),
    /**
     * Storage Commitment Pull Model SOP Class (Retired).
     */
    StorageCommitmentPullModel("1.2.840.10008.1.20.2", "Storage Commitment Pull Model SOP Class (Retired)"),
    /**
     * Storage Commitment Pull Model SOP Instance (Retired).
     */
    StorageCommitmentPullModelInstance("1.2.840.10008.1.20.2.1",
            "Storage Commitment Pull Model SOP Instance (Retired)"),
    /**
     * Procedural Event Logging SOP Class.
     */
    ProceduralEventLogging("1.2.840.10008.1.40", "Procedural Event Logging SOP Class"),
    /**
     * Procedural Event Logging SOP Instance.
     */
    ProceduralEventLoggingInstance("1.2.840.10008.1.40.1", "Procedural Event Logging SOP Instance"),
    /**
     * Substance Administration Logging SOP Class.
     */
    SubstanceAdministrationLogging("1.2.840.10008.1.42", "Substance Administration Logging SOP Class"),
    /**
     * Substance Administration Logging SOP Instance.
     */
    SubstanceAdministrationLoggingInstance("1.2.840.10008.1.42.1", "Substance Administration Logging SOP Instance"),
    /**
     * DICOM UID Registry.
     */
    DCMUID("1.2.840.10008.2.6.1", "DICOM UID Registry"),
    /**
     * DICOM Controlled Terminology.
     */
    DCM("1.2.840.10008.2.16.4", "DICOM Controlled Terminology"),
    /**
     * Adult Mouse Anatomy Ontology.
     */
    MA("1.2.840.10008.2.16.5", "Adult Mouse Anatomy Ontology"),
    /**
     * Uberon Ontology.
     */
    UBERON("1.2.840.10008.2.16.6", "Uberon Ontology"),
    /**
     * Integrated Taxonomic Information System (ITIS) Taxonomic Serial Number (TSN).
     */
    ITIS_TSN("1.2.840.10008.2.16.7", "Integrated Taxonomic Information System (ITIS) Taxonomic Serial Number (TSN)"),
    /**
     * Mouse Genome Initiative (MGI).
     */
    MGI("1.2.840.10008.2.16.8", "Mouse Genome Initiative (MGI)"),
    /**
     * PubChem Compound CID.
     */
    PUBCHEM_CID("1.2.840.10008.2.16.9", "PubChem Compound CID"),
    /**
     * Dublin Core.
     */
    DC("1.2.840.10008.2.16.10", "Dublin Core"),
    /**
     * New York University Melanoma Clinical Cooperative Group.
     */
    NYUMCCG("1.2.840.10008.2.16.11", "New York University Melanoma Clinical Cooperative Group"),
    /**
     * Mayo Clinic Non-radiological Images Specific Body Structure Anatomical Surface Region Guide.
     */
    MAYONRISBSASRG("1.2.840.10008.2.16.12",
            "Mayo Clinic Non-radiological Images Specific Body Structure Anatomical Surface Region Guide"),
    /**
     * Image Biomarker Standardisation Initiative.
     */
    IBSI("1.2.840.10008.2.16.13", "Image Biomarker Standardisation Initiative"),
    /**
     * Radiomics Ontology.
     */
    RO("1.2.840.10008.2.16.14", "Radiomics Ontology"),
    /**
     * RadElement.
     */
    RADELEMENT("1.2.840.10008.2.16.15", "RadElement"),
    /**
     * ICD-11.
     */
    I11("1.2.840.10008.2.16.16", "ICD-11"),
    /**
     * Unified numbering system (UNS) for metals and alloys.
     */
    UNS("1.2.840.10008.2.16.17", "Unified numbering system (UNS) for metals and alloys"),
    /**
     * Research Resource Identification.
     */
    RRID("1.2.840.10008.2.16.18", "Research Resource Identification"),
    /**
     * DICOM Application Context Name.
     */
    DICOMApplicationContext("1.2.840.10008.3.1.1.1", "DICOM Application Context Name"),
    /**
     * Detached Patient Management SOP Class (Retired).
     */
    DetachedPatientManagement("1.2.840.10008.3.1.2.1.1", "Detached Patient Management SOP Class (Retired)"),
    /**
     * Detached Patient Management Meta SOP Class (Retired).
     */
    DetachedPatientManagementMeta("1.2.840.10008.3.1.2.1.4", "Detached Patient Management Meta SOP Class (Retired)"),
    /**
     * Detached Visit Management SOP Class (Retired).
     */
    DetachedVisitManagement("1.2.840.10008.3.1.2.2.1", "Detached Visit Management SOP Class (Retired)"),
    /**
     * Detached Study Management SOP Class (Retired).
     */
    DetachedStudyManagement("1.2.840.10008.3.1.2.3.1", "Detached Study Management SOP Class (Retired)"),
    /**
     * Study Component Management SOP Class (Retired).
     */
    StudyComponentManagement("1.2.840.10008.3.1.2.3.2", "Study Component Management SOP Class (Retired)"),
    /**
     * Modality Performed Procedure Step SOP Class.
     */
    ModalityPerformedProcedureStep("1.2.840.10008.3.1.2.3.3", "Modality Performed Procedure Step SOP Class"),
    /**
     * Modality Performed Procedure Step Retrieve SOP Class.
     */
    ModalityPerformedProcedureStepRetrieve("1.2.840.10008.3.1.2.3.4",
            "Modality Performed Procedure Step Retrieve SOP Class"),
    /**
     * Modality Performed Procedure Step Notification SOP Class.
     */
    ModalityPerformedProcedureStepNotification("1.2.840.10008.3.1.2.3.5",
            "Modality Performed Procedure Step Notification SOP Class"),
    /**
     * Detached Results Management SOP Class (Retired).
     */
    DetachedResultsManagement("1.2.840.10008.3.1.2.5.1", "Detached Results Management SOP Class (Retired)"),
    /**
     * Detached Results Management Meta SOP Class (Retired).
     */
    DetachedResultsManagementMeta("1.2.840.10008.3.1.2.5.4", "Detached Results Management Meta SOP Class (Retired)"),
    /**
     * Detached Study Management Meta SOP Class (Retired).
     */
    DetachedStudyManagementMeta("1.2.840.10008.3.1.2.5.5", "Detached Study Management Meta SOP Class (Retired)"),
    /**
     * Detached Interpretation Management SOP Class (Retired).
     */
    DetachedInterpretationManagement("1.2.840.10008.3.1.2.6.1",
            "Detached Interpretation Management SOP Class (Retired)"),
    /**
     * Storage Service Class.
     */
    Storage("1.2.840.10008.4.2", "Storage Service Class"),
    /**
     * Basic Film Session SOP Class.
     */
    BasicFilmSession("1.2.840.10008.5.1.1.1", "Basic Film Session SOP Class"),
    /**
     * Basic Film Box SOP Class.
     */
    BasicFilmBox("1.2.840.10008.5.1.1.2", "Basic Film Box SOP Class"),
    /**
     * Basic Grayscale Image Box SOP Class.
     */
    BasicGrayscaleImageBox("1.2.840.10008.5.1.1.4", "Basic Grayscale Image Box SOP Class"),
    /**
     * Basic Color Image Box SOP Class.
     */
    BasicColorImageBox("1.2.840.10008.5.1.1.4.1", "Basic Color Image Box SOP Class"),
    /**
     * Referenced Image Box SOP Class (Retired).
     */
    ReferencedImageBox("1.2.840.10008.5.1.1.4.2", "Referenced Image Box SOP Class (Retired)"),
    /**
     * Basic Grayscale Print Management Meta SOP Class.
     */
    BasicGrayscalePrintManagementMeta("1.2.840.10008.5.1.1.9", "Basic Grayscale Print Management Meta SOP Class"),
    /**
     * Referenced Grayscale Print Management Meta SOP Class (Retired).
     */
    ReferencedGrayscalePrintManagementMeta("1.2.840.10008.5.1.1.9.1",
            "Referenced Grayscale Print Management Meta SOP Class (Retired)"),
    /**
     * Print Job SOP Class.
     */
    PrintJob("1.2.840.10008.5.1.1.14", "Print Job SOP Class"),
    /**
     * Basic Annotation Box SOP Class.
     */
    BasicAnnotationBox("1.2.840.10008.5.1.1.15", "Basic Annotation Box SOP Class"),
    /**
     * Printer SOP Class.
     */
    Printer("1.2.840.10008.5.1.1.16", "Printer SOP Class"),
    /**
     * Printer Configuration Retrieval SOP Class.
     */
    PrinterConfigurationRetrieval("1.2.840.10008.5.1.1.16.376", "Printer Configuration Retrieval SOP Class"),
    /**
     * Printer SOP Instance.
     */
    PrinterInstance("1.2.840.10008.5.1.1.17", "Printer SOP Instance"),
    /**
     * Printer Configuration Retrieval SOP Instance.
     */
    PrinterConfigurationRetrievalInstance("1.2.840.10008.5.1.1.17.376", "Printer Configuration Retrieval SOP Instance"),
    /**
     * Basic Color Print Management Meta SOP Class.
     */
    BasicColorPrintManagementMeta("1.2.840.10008.5.1.1.18", "Basic Color Print Management Meta SOP Class"),
    /**
     * Referenced Color Print Management Meta SOP Class (Retired).
     */
    ReferencedColorPrintManagementMeta("1.2.840.10008.5.1.1.18.1",
            "Referenced Color Print Management Meta SOP Class (Retired)"),
    /**
     * VOI LUT Box SOP Class.
     */
    VOILUTBox("1.2.840.10008.5.1.1.22", "VOI LUT Box SOP Class"),
    /**
     * Presentation LUT SOP Class.
     */
    PresentationLUT("1.2.840.10008.5.1.1.23", "Presentation LUT SOP Class"),
    /**
     * Image Overlay Box SOP Class (Retired).
     */
    ImageOverlayBox("1.2.840.10008.5.1.1.24", "Image Overlay Box SOP Class (Retired)"),
    /**
     * Basic Print Image Overlay Box SOP Class (Retired).
     */
    BasicPrintImageOverlayBox("1.2.840.10008.5.1.1.24.1", "Basic Print Image Overlay Box SOP Class (Retired)"),
    /**
     * Print Queue SOP Instance (Retired).
     */
    PrintQueueInstance("1.2.840.10008.5.1.1.25", "Print Queue SOP Instance (Retired)"),
    /**
     * Print Queue Management SOP Class (Retired).
     */
    PrintQueueManagement("1.2.840.10008.5.1.1.26", "Print Queue Management SOP Class (Retired)"),
    /**
     * Stored Print Storage SOP Class (Retired).
     */
    StoredPrintStorage("1.2.840.10008.5.1.1.27", "Stored Print Storage SOP Class (Retired)"),
    /**
     * Hardcopy Grayscale Image Storage SOP Class (Retired).
     */
    HardcopyGrayscaleImageStorage("1.2.840.10008.5.1.1.29", "Hardcopy Grayscale Image Storage SOP Class (Retired)"),
    /**
     * Hardcopy Color Image Storage SOP Class (Retired).
     */
    HardcopyColorImageStorage("1.2.840.10008.5.1.1.30", "Hardcopy Color Image Storage SOP Class (Retired)"),
    /**
     * Pull Print Request SOP Class (Retired).
     */
    PullPrintRequest("1.2.840.10008.5.1.1.31", "Pull Print Request SOP Class (Retired)"),
    /**
     * Pull Stored Print Management Meta SOP Class (Retired).
     */
    PullStoredPrintManagementMeta("1.2.840.10008.5.1.1.32", "Pull Stored Print Management Meta SOP Class (Retired)"),
    /**
     * Media Creation Management SOP Class UID.
     */
    MediaCreationManagement("1.2.840.10008.5.1.1.33", "Media Creation Management SOP Class UID"),
    /**
     * Display System SOP Class.
     */
    DisplaySystem("1.2.840.10008.5.1.1.40", "Display System SOP Class"),
    /**
     * Display System SOP Instance.
     */
    DisplaySystemInstance("1.2.840.10008.5.1.1.40.1", "Display System SOP Instance"),
    /**
     * Computed Radiography Image Storage SOP Class.
     */
    ComputedRadiographyImageStorage("1.2.840.10008.5.1.4.1.1.1", "Computed Radiography Image Storage"),
    /**
     * Digital X-Ray Image Storage - For Presentation SOP Class.
     */
    DigitalXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.1.1",
            "Digital X-Ray Image Storage - For Presentation"),
    /**
     * Digital X-Ray Image Storage - For Processing SOP Class.
     */
    DigitalXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.1.1.1",
            "Digital X-Ray Image Storage - For Processing"),
    /**
     * Digital Mammography X-Ray Image Storage - For Presentation SOP Class.
     */
    DigitalMammographyXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.1.2",
            "Digital Mammography X-Ray Image Storage - For Presentation"),
    /**
     * Digital Mammography X-Ray Image Storage - For Processing SOP Class.
     */
    DigitalMammographyXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.1.2.1",
            "Digital Mammography X-Ray Image Storage - For Processing"),
    /**
     * Digital Intra-Oral X-Ray Image Storage - For Presentation SOP Class.
     */
    DigitalIntraOralXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.1.3",
            "Digital Intra-Oral X-Ray Image Storage - For Presentation"),
    /**
     * Digital Intra-Oral X-Ray Image Storage - For Processing SOP Class.
     */
    DigitalIntraOralXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.1.3.1",
            "Digital Intra-Oral X-Ray Image Storage - For Processing"),
    /**
     * CT Image Storage SOP Class.
     */
    CTImageStorage("1.2.840.10008.5.1.4.1.1.2", "CT Image Storage"),
    /**
     * Enhanced CT Image Storage SOP Class.
     */
    EnhancedCTImageStorage("1.2.840.10008.5.1.4.1.1.2.1", "Enhanced CT Image Storage"),
    /**
     * Legacy Converted Enhanced CT Image Storage SOP Class.
     */
    LegacyConvertedEnhancedCTImageStorage("1.2.840.10008.5.1.4.1.1.2.2", "Legacy Converted Enhanced CT Image Storage"),
    /**
     * Ultrasound Multi-frame Image Storage (Retired) SOP Class.
     */
    UltrasoundMultiFrameImageStorageRetired("1.2.840.10008.5.1.4.1.1.3",
            "Ultrasound Multi-frame Image Storage (Retired)"),
    /**
     * Ultrasound Multi-frame Image Storage SOP Class.
     */
    UltrasoundMultiFrameImageStorage("1.2.840.10008.5.1.4.1.1.3.1", "Ultrasound Multi-frame Image Storage"),
    /**
     * MR Image Storage SOP Class.
     */
    MRImageStorage("1.2.840.10008.5.1.4.1.1.4", "MR Image Storage"),
    /**
     * Enhanced MR Image Storage SOP Class.
     */
    EnhancedMRImageStorage("1.2.840.10008.5.1.4.1.1.4.1", "Enhanced MR Image Storage"),
    /**
     * MR Spectroscopy Storage SOP Class.
     */
    MRSpectroscopyStorage("1.2.840.10008.5.1.4.1.1.4.2", "MR Spectroscopy Storage"),
    /**
     * Enhanced MR Color Image Storage SOP Class.
     */
    EnhancedMRColorImageStorage("1.2.840.10008.5.1.4.1.1.4.3", "Enhanced MR Color Image Storage"),
    /**
     * Legacy Converted Enhanced MR Image Storage SOP Class.
     */
    LegacyConvertedEnhancedMRImageStorage("1.2.840.10008.5.1.4.1.1.4.4", "Legacy Converted Enhanced MR Image Storage"),
    /**
     * Nuclear Medicine Image Storage (Retired) SOP Class.
     */
    NuclearMedicineImageStorageRetired("1.2.840.10008.5.1.4.1.1.5", "Nuclear Medicine Image Storage (Retired)"),
    /**
     * Ultrasound Image Storage (Retired) SOP Class.
     */
    UltrasoundImageStorageRetired("1.2.840.10008.5.1.4.1.1.6", "Ultrasound Image Storage (Retired)"),
    /**
     * Ultrasound Image Storage SOP Class.
     */
    UltrasoundImageStorage("1.2.840.10008.5.1.4.1.1.6.1", "Ultrasound Image Storage"),
    /**
     * Enhanced US Volume Storage SOP Class.
     */
    EnhancedUSVolumeStorage("1.2.840.10008.5.1.4.1.1.6.2", "Enhanced US Volume Storage"),
    /**
     * Photoacoustic Image Storage SOP Class.
     */
    PhotoacousticImageStorage("1.2.840.10008.5.1.4.1.1.6.3", "Photoacoustic Image Storage"),
    /**
     * Secondary Capture Image Storage SOP Class.
     */
    SecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7", "Secondary Capture Image Storage"),
    /**
     * Multi-frame Single Bit Secondary Capture Image Storage SOP Class.
     */
    MultiFrameSingleBitSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.1",
            "Multi-frame Single Bit Secondary Capture Image Storage"),
    /**
     * Multi-frame Grayscale Byte Secondary Capture Image Storage SOP Class.
     */
    MultiFrameGrayscaleByteSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.2",
            "Multi-frame Grayscale Byte Secondary Capture Image Storage"),
    /**
     * Multi-frame Grayscale Word Secondary Capture Image Storage SOP Class.
     */
    MultiFrameGrayscaleWordSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.3",
            "Multi-frame Grayscale Word Secondary Capture Image Storage"),
    /**
     * Multi-frame True Color Secondary Capture Image Storage SOP Class.
     */
    MultiFrameTrueColorSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.4",
            "Multi-frame True Color Secondary Capture Image Storage"),
    /**
     * Standalone Overlay Storage (Retired) SOP Class.
     */
    StandaloneOverlayStorage("1.2.840.10008.5.1.4.1.1.8", "Standalone Overlay Storage (Retired)"),
    /**
     * Standalone Curve Storage (Retired) SOP Class.
     */
    StandaloneCurveStorage("1.2.840.10008.5.1.4.1.1.9", "Standalone Curve Storage (Retired)"),
    /**
     * Waveform Storage - Trial (Retired) SOP Class.
     */
    WaveformStorageTrial("1.2.840.10008.5.1.4.1.1.9.1", "Waveform Storage - Trial (Retired)"),
    /**
     * 12-lead ECG Waveform Storage SOP Class.
     */
    TwelveLeadECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.1", "12-lead ECG Waveform Storage"),
    /**
     * General ECG Waveform Storage SOP Class.
     */
    GeneralECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.2", "General ECG Waveform Storage"),
    /**
     * Ambulatory ECG Waveform Storage SOP Class.
     */
    AmbulatoryECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.3", "Ambulatory ECG Waveform Storage"),
    /**
     * General 32-bit ECG Waveform Storage SOP Class.
     */
    General32bitECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.4", "General 32-bit ECG Waveform Storage"),
    /**
     * Hemodynamic Waveform Storage SOP Class.
     */
    HemodynamicWaveformStorage("1.2.840.10008.5.1.4.1.1.9.2.1", "Hemodynamic Waveform Storage"),
    /**
     * Cardiac Electrophysiology Waveform Storage SOP Class.
     */
    CardiacElectrophysiologyWaveformStorage("1.2.840.10008.5.1.4.1.1.9.3.1",
            "Cardiac Electrophysiology Waveform Storage"),
    /**
     * Basic Voice Audio Waveform Storage SOP Class.
     */
    BasicVoiceAudioWaveformStorage("1.2.840.10008.5.1.4.1.1.9.4.1", "Basic Voice Audio Waveform Storage"),
    /**
     * General Audio Waveform Storage SOP Class.
     */
    GeneralAudioWaveformStorage("1.2.840.10008.5.1.4.1.1.9.4.2", "General Audio Waveform Storage"),
    /**
     * Arterial Pulse Waveform Storage SOP Class.
     */
    ArterialPulseWaveformStorage("1.2.840.10008.5.1.4.1.1.9.5.1", "Arterial Pulse Waveform Storage"),
    /**
     * Respiratory Waveform Storage SOP Class.
     */
    RespiratoryWaveformStorage("1.2.840.10008.5.1.4.1.1.9.6.1", "Respiratory Waveform Storage"),
    /**
     * Multi-channel Respiratory Waveform Storage SOP Class.
     */
    MultichannelRespiratoryWaveformStorage("1.2.840.10008.5.1.4.1.1.9.6.2",
            "Multi-channel Respiratory Waveform Storage"),
    /**
     * Routine Scalp Electroencephalogram Waveform Storage SOP Class.
     */
    RoutineScalpElectroencephalogramWaveformStorage("1.2.840.10008.5.1.4.1.1.9.7.1",
            "Routine Scalp Electroencephalogram Waveform Storage"),
    /**
     * Electromyogram Waveform Storage SOP Class.
     */
    ElectromyogramWaveformStorage("1.2.840.10008.5.1.4.1.1.9.7.2", "Electromyogram Waveform Storage"),
    /**
     * Electrooculogram Waveform Storage SOP Class.
     */
    ElectrooculogramWaveformStorage("1.2.840.10008.5.1.4.1.1.9.7.3", "Electrooculogram Waveform Storage"),
    /**
     * Sleep Electroencephalogram Waveform Storage SOP Class.
     */
    SleepElectroencephalogramWaveformStorage("1.2.840.10008.5.1.4.1.1.9.7.4",
            "Sleep Electroencephalogram Waveform Storage"),
    /**
     * Body Position Waveform Storage SOP Class.
     */
    BodyPositionWaveformStorage("1.2.840.10008.5.1.4.1.1.9.8.1", "Body Position Waveform Storage"),
    /**
     * Standalone Modality LUT Storage (Retired) SOP Class.
     */
    StandaloneModalityLUTStorage("1.2.840.10008.5.1.4.1.1.10", "Standalone Modality LUT Storage (Retired)"),
    /**
     * Standalone VOI LUT Storage (Retired) SOP Class.
     */
    StandaloneVOILUTStorage("1.2.840.10008.5.1.4.1.1.11", "Standalone VOI LUT Storage (Retired)"),
    /**
     * Grayscale Softcopy Presentation State Storage SOP Class.
     */
    GrayscaleSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.1",
            "Grayscale Softcopy Presentation State Storage"),
    /**
     * Color Softcopy Presentation State Storage SOP Class.
     */
    ColorSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.2", "Color Softcopy Presentation State Storage"),
    /**
     * Pseudo-Color Softcopy Presentation State Storage SOP Class.
     */
    PseudoColorSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.3",
            "Pseudo-Color Softcopy Presentation State Storage"),
    /**
     * Blending Softcopy Presentation State Storage SOP Class.
     */
    BlendingSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.4",
            "Blending Softcopy Presentation State Storage"),
    /**
     * XA/XRF Grayscale Softcopy Presentation State Storage SOP Class.
     */
    XAXRFGrayscaleSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.5",
            "XA/XRF Grayscale Softcopy Presentation State Storage"),
    /**
     * Grayscale Planar MPR Volumetric Presentation State Storage SOP Class.
     */
    GrayscalePlanarMPRVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.6",
            "Grayscale Planar MPR Volumetric Presentation State Storage"),
    /**
     * Compositing Planar MPR Volumetric Presentation State Storage SOP Class.
     */
    CompositingPlanarMPRVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.7",
            "Compositing Planar MPR Volumetric Presentation State Storage"),
    /**
     * Advanced Blending Presentation State Storage SOP Class.
     */
    AdvancedBlendingPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.8",
            "Advanced Blending Presentation State Storage"),
    /**
     * Volume Rendering Volumetric Presentation State Storage SOP Class.
     */
    VolumeRenderingVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.9",
            "Volume Rendering Volumetric Presentation State Storage"),
    /**
     * Segmented Volume Rendering Volumetric Presentation State Storage SOP Class.
     */
    SegmentedVolumeRenderingVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.10",
            "Segmented Volume Rendering Volumetric Presentation State Storage"),
    /**
     * Multiple Volume Rendering Volumetric Presentation State Storage SOP Class.
     */
    MultipleVolumeRenderingVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.11",
            "Multiple Volume Rendering Volumetric Presentation State Storage"),
    /**
     * Variable Modality LUT Softcopy Presentation State Storage SOP Class.
     */
    VariableModalityLUTSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.12",
            "Variable Modality LUT Softcopy Presentation State Storage"),
    /**
     * X-Ray Angiographic Image Storage SOP Class.
     */
    XRayAngiographicImageStorage("1.2.840.10008.5.1.4.1.1.12.1", "X-Ray Angiographic Image Storage"),
    /**
     * Enhanced XA Image Storage SOP Class.
     */
    EnhancedXAImageStorage("1.2.840.10008.5.1.4.1.1.12.1.1", "Enhanced XA Image Storage"),
    /**
     * X-Ray Radiofluoroscopic Image Storage SOP Class.
     */
    XRayRadiofluoroscopicImageStorage("1.2.840.10008.5.1.4.1.1.12.2", "X-Ray Radiofluoroscopic Image Storage"),
    /**
     * Enhanced XRF Image Storage SOP Class.
     */
    EnhancedXRFImageStorage("1.2.840.10008.5.1.4.1.1.12.2.1", "Enhanced XRF Image Storage"),
    /**
     * X-Ray Angiographic Bi-Plane Image Storage (Retired) SOP Class.
     */
    XRayAngiographicBiPlaneImageStorage("1.2.840.10008.5.1.4.1.1.12.3",
            "X-Ray Angiographic Bi-Plane Image Storage (Retired)"),
    /**
     * Zeiss OPT File (Retired).
     */
    ZeissOPTFile("1.2.840.10008.5.1.4.1.1.12.77", "Zeiss OPT File (Retired)"),
    /**
     * X-Ray 3D Angiographic Image Storage SOP Class.
     */
    XRay3DAngiographicImageStorage("1.2.840.10008.5.1.4.1.1.13.1.1", "X-Ray 3D Angiographic Image Storage"),
    /**
     * X-Ray 3D Craniofacial Image Storage SOP Class.
     */
    XRay3DCraniofacialImageStorage("1.2.840.10008.5.1.4.1.1.13.1.2", "X-Ray 3D Craniofacial Image Storage"),
    /**
     * Breast Tomosynthesis Image Storage SOP Class.
     */
    BreastTomosynthesisImageStorage("1.2.840.10008.5.1.4.1.1.13.1.3", "Breast Tomosynthesis Image Storage"),
    /**
     * Breast Projection X-Ray Image Storage - For Presentation SOP Class.
     */
    BreastProjectionXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.13.1.4",
            "Breast Projection X-Ray Image Storage - For Presentation"),
    /**
     * Breast Projection X-Ray Image Storage - For Processing SOP Class.
     */
    BreastProjectionXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.13.1.5",
            "Breast Projection X-Ray Image Storage - For Processing"),
    /**
     * Intravascular Optical Coherence Tomography Image Storage - For Presentation SOP Class.
     */
    IntravascularOpticalCoherenceTomographyImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.14.1",
            "Intravascular Optical Coherence Tomography Image Storage - For Presentation"),
    /**
     * Intravascular Optical Coherence Tomography Image Storage - For Processing SOP Class.
     */
    IntravascularOpticalCoherenceTomographyImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.14.2",
            "Intravascular Optical Coherence Tomography Image Storage - For Processing"),
    /**
     * Nuclear Medicine Image Storage SOP Class.
     */
    NuclearMedicineImageStorage("1.2.840.10008.5.1.4.1.1.20", "Nuclear Medicine Image Storage"),
    /**
     * Parametric Map Storage SOP Class.
     */
    ParametricMapStorage("1.2.840.10008.5.1.4.1.1.30", "Parametric Map Storage"),
    /**
     * MR Image Storage Zero Padded (Retired) SOP Class.
     */
    MRImageStorageZeroPadded("1.2.840.10008.5.1.4.1.1.40", "MR Image Storage Zero Padded (Retired)"),
    /**
     * Raw Data Storage SOP Class.
     */
    RawDataStorage("1.2.840.10008.5.1.4.1.1.66", "Raw Data Storage"),
    /**
     * Spatial Registration Storage SOP Class.
     */
    SpatialRegistrationStorage("1.2.840.10008.5.1.4.1.1.66.1", "Spatial Registration Storage"),
    /**
     * Spatial Fiducials Storage SOP Class.
     */
    SpatialFiducialsStorage("1.2.840.10008.5.1.4.1.1.66.2", "Spatial Fiducials Storage"),
    /**
     * Deformable Spatial Registration Storage SOP Class.
     */
    DeformableSpatialRegistrationStorage("1.2.840.10008.5.1.4.1.1.66.3", "Deformable Spatial Registration Storage"),
    /**
     * Segmentation Storage SOP Class.
     */
    SegmentationStorage("1.2.840.10008.5.1.4.1.1.66.4", "Segmentation Storage"),
    /**
     * Surface Segmentation Storage SOP Class.
     */
    SurfaceSegmentationStorage("1.2.840.10008.5.1.4.1.1.66.5", "Surface Segmentation Storage"),
    /**
     * Tractography Results Storage SOP Class.
     */
    TractographyResultsStorage("1.2.840.10008.5.1.4.1.1.66.6", "Tractography Results Storage"),
    /**
     * Real World Value Mapping Storage SOP Class.
     */
    RealWorldValueMappingStorage("1.2.840.10008.5.1.4.1.1.67", "Real World Value Mapping Storage"),
    /**
     * Surface Scan Mesh Storage SOP Class.
     */
    SurfaceScanMeshStorage("1.2.840.10008.5.1.4.1.1.68.1", "Surface Scan Mesh Storage"),
    /**
     * Surface Scan Point Cloud Storage SOP Class.
     */
    SurfaceScanPointCloudStorage("1.2.840.10008.5.1.4.1.1.68.2", "Surface Scan Point Cloud Storage"),
    /**
     * VL Image Storage - Trial (Retired) SOP Class.
     */
    VLImageStorageTrial("1.2.840.10008.5.1.4.1.1.77.1", "VL Image Storage - Trial (Retired)"),
    /**
     * VL Multi-frame Image Storage - Trial (Retired) SOP Class.
     */
    VLMultiFrameImageStorageTrial("1.2.840.10008.5.1.4.1.1.77.2", "VL Multi-frame Image Storage - Trial (Retired)"),
    /**
     * VL Endoscopic Image Storage SOP Class.
     */
    VLEndoscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.1", "VL Endoscopic Image Storage"),
    /**
     * Video Endoscopic Image Storage SOP Class.
     */
    VideoEndoscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.1.1", "Video Endoscopic Image Storage"),
    /**
     * VL Microscopic Image Storage SOP Class.
     */
    VLMicroscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.2", "VL Microscopic Image Storage"),
    /**
     * Video Microscopic Image Storage SOP Class.
     */
    VideoMicroscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.2.1", "Video Microscopic Image Storage"),
    /**
     * VL Slide-Coordinates Microscopic Image Storage SOP Class.
     */
    VLSlideCoordinatesMicroscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.3",
            "VL Slide-Coordinates Microscopic Image Storage"),
    /**
     * VL Photographic Image Storage SOP Class.
     */
    VLPhotographicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.4", "VL Photographic Image Storage"),
    /**
     * Video Photographic Image Storage SOP Class.
     */
    VideoPhotographicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.4.1", "Video Photographic Image Storage"),
    /**
     * Ophthalmic Photography 8 Bit Image Storage SOP Class.
     */
    OphthalmicPhotography8BitImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.1",
            "Ophthalmic Photography 8 Bit Image Storage"),
    /**
     * Ophthalmic Photography 16 Bit Image Storage SOP Class.
     */
    OphthalmicPhotography16BitImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.2",
            "Ophthalmic Photography 16 Bit Image Storage"),
    /**
     * Stereometric Relationship Storage SOP Class.
     */
    StereometricRelationshipStorage("1.2.840.10008.5.1.4.1.1.77.1.5.3", "Stereometric Relationship Storage"),
    /**
     * Ophthalmic Tomography Image Storage SOP Class.
     */
    OphthalmicTomographyImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.4", "Ophthalmic Tomography Image Storage"),
    /**
     * Wide Field Ophthalmic Photography Stereographic Projection Image Storage SOP Class.
     */
    WideFieldOphthalmicPhotographyStereographicProjectionImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.5",
            "Wide Field Ophthalmic Photography Stereographic Projection Image Storage"),
    /**
     * Wide Field Ophthalmic Photography 3D Coordinates Image Storage SOP Class.
     */
    WideFieldOphthalmicPhotography3DCoordinatesImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.6",
            "Wide Field Ophthalmic Photography 3D Coordinates Image Storage"),
    /**
     * Ophthalmic Optical Coherence Tomography En Face Image Storage SOP Class.
     */
    OphthalmicOpticalCoherenceTomographyEnFaceImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.7",
            "Ophthalmic Optical Coherence Tomography En Face Image Storage"),
    /**
     * Ophthalmic Optical Coherence Tomography B-scan Volume Analysis Storage SOP Class.
     */
    OphthalmicOpticalCoherenceTomographyBscanVolumeAnalysisStorage("1.2.840.10008.5.1.4.1.1.77.1.5.8",
            "Ophthalmic Optical Coherence Tomography B-scan Volume Analysis Storage"),
    /**
     * VL Whole Slide Microscopy Image Storage SOP Class.
     */
    VLWholeSlideMicroscopyImageStorage("1.2.840.10008.5.1.4.1.1.77.1.6", "VL Whole Slide Microscopy Image Storage"),
    /**
     * Dermoscopic Photography Image Storage SOP Class.
     */
    DermoscopicPhotographyImageStorage("1.2.840.10008.5.1.4.1.1.77.1.7", "Dermoscopic Photography Image Storage"),
    /**
     * Confocal Microscopy Image Storage SOP Class.
     */
    ConfocalMicroscopyImageStorage("1.2.840.10008.5.1.4.1.1.77.1.8", "Confocal Microscopy Image Storage"),
    /**
     * Confocal Microscopy Tiled Pyramidal Image Storage SOP Class.
     */
    ConfocalMicroscopyTiledPyramidalImageStorage("1.2.840.10008.5.1.4.1.1.77.1.9",
            "Confocal Microscopy Tiled Pyramidal Image Storage"),
    /**
     * Lensometry Measurements Storage SOP Class.
     */
    LensometryMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.1", "Lensometry Measurements Storage"),
    /**
     * Autorefraction Measurements Storage SOP Class.
     */
    AutorefractionMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.2", "Autorefraction Measurements Storage"),
    /**
     * Keratometry Measurements Storage SOP Class.
     */
    KeratometryMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.3", "Keratometry Measurements Storage"),
    /**
     * Subjective Refraction Measurements Storage SOP Class.
     */
    SubjectiveRefractionMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.4",
            "Subjective Refraction Measurements Storage"),
    /**
     * Visual Acuity Measurements Storage SOP Class.
     */
    VisualAcuityMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.5", "Visual Acuity Measurements Storage"),
    /**
     * Spectacle Prescription Report Storage SOP Class.
     */
    SpectaclePrescriptionReportStorage("1.2.840.10008.5.1.4.1.1.78.6", "Spectacle Prescription Report Storage"),
    /**
     * Ophthalmic Axial Measurements Storage SOP Class.
     */
    OphthalmicAxialMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.7", "Ophthalmic Axial Measurements Storage"),
    /**
     * Intraocular Lens Calculations Storage SOP Class.
     */
    IntraocularLensCalculationsStorage("1.2.840.10008.5.1.4.1.1.78.8", "Intraocular Lens Calculations Storage"),
    /**
     * Macular Grid Thickness and Volume Report Storage SOP Class.
     */
    MacularGridThicknessAndVolumeReportStorage("1.2.840.10008.5.1.4.1.1.79.1",
            "Macular Grid Thickness and Volume Report Storage"),
    /**
     * Ophthalmic Visual Field Static Perimetry Measurements Storage SOP Class.
     */
    OphthalmicVisualFieldStaticPerimetryMeasurementsStorage("1.2.840.10008.5.1.4.1.1.80.1",
            "Ophthalmic Visual Field Static Perimetry Measurements Storage"),
    /**
     * Ophthalmic Thickness Map Storage SOP Class.
     */
    OphthalmicThicknessMapStorage("1.2.840.10008.5.1.4.1.1.81.1", "Ophthalmic Thickness Map Storage"),
    /**
     * Corneal Topography Map Storage SOP Class.
     */
    CornealTopographyMapStorage("1.2.840.10008.5.1.4.1.1.82.1", "Corneal Topography Map Storage"),
    /**
     * Text SR Storage - Trial (Retired) SOP Class.
     */
    TextSRStorageTrial("1.2.840.10008.5.1.4.1.1.88.1", "Text SR Storage - Trial (Retired)"),
    /**
     * Audio SR Storage - Trial (Retired) SOP Class.
     */
    AudioSRStorageTrial("1.2.840.10008.5.1.4.1.1.88.2", "Audio SR Storage - Trial (Retired)"),
    /**
     * Detail SR Storage - Trial (Retired) SOP Class.
     */
    DetailSRStorageTrial("1.2.840.10008.5.1.4.1.1.88.3", "Detail SR Storage - Trial (Retired)"),
    /**
     * Comprehensive SR Storage - Trial (Retired) SOP Class.
     */
    ComprehensiveSRStorageTrial("1.2.840.10008.5.1.4.1.1.88.4", "Comprehensive SR Storage - Trial (Retired)"),
    /**
     * Basic Text SR Storage SOP Class.
     */
    BasicTextSRStorage("1.2.840.10008.5.1.4.1.1.88.11", "Basic Text SR Storage"),
    /**
     * Enhanced SR Storage SOP Class.
     */
    EnhancedSRStorage("1.2.840.10008.5.1.4.1.1.88.22", "Enhanced SR Storage"),
    /**
     * Comprehensive SR Storage SOP Class.
     */
    ComprehensiveSRStorage("1.2.840.10008.5.1.4.1.1.88.33", "Comprehensive SR Storage"),
    /**
     * Comprehensive 3D SR Storage SOP Class.
     */
    Comprehensive3DSRStorage("1.2.840.10008.5.1.4.1.1.88.34", "Comprehensive 3D SR Storage"),
    /**
     * Extensible SR Storage SOP Class.
     */
    ExtensibleSRStorage("1.2.840.10008.5.1.4.1.1.88.35", "Extensible SR Storage"),
    /**
     * Procedure Log Storage SOP Class.
     */
    ProcedureLogStorage("1.2.840.10008.5.1.4.1.1.88.40", "Procedure Log Storage"),
    /**
     * Mammography CAD SR Storage SOP Class.
     */
    MammographyCADSRStorage("1.2.840.10008.5.1.4.1.1.88.50", "Mammography CAD SR Storage"),
    /**
     * Key Object Selection Document Storage SOP Class.
     */
    KeyObjectSelectionDocumentStorage("1.2.840.10008.5.1.4.1.1.88.59", "Key Object Selection Document Storage"),
    /**
     * Chest CAD SR Storage SOP Class.
     */
    ChestCADSRStorage("1.2.840.10008.5.1.4.1.1.88.65", "Chest CAD SR Storage"),
    /**
     * X-Ray Radiation Dose SR Storage SOP Class.
     */
    XRayRadiationDoseSRStorage("1.2.840.10008.5.1.4.1.1.88.67", "X-Ray Radiation Dose SR Storage"),
    /**
     * Radiopharmaceutical Radiation Dose SR Storage SOP Class.
     */
    RadiopharmaceuticalRadiationDoseSRStorage("1.2.840.10008.5.1.4.1.1.88.68",
            "Radiopharmaceutical Radiation Dose SR Storage"),
    /**
     * Colon CAD SR Storage SOP Class.
     */
    ColonCADSRStorage("1.2.840.10008.5.1.4.1.1.88.69", "Colon CAD SR Storage"),
    /**
     * Implantation Plan SR Storage SOP Class.
     */
    ImplantationPlanSRStorage("1.2.840.10008.5.1.4.1.1.88.70", "Implantation Plan SR Storage"),
    /**
     * Acquisition Context SR Storage SOP Class.
     */
    AcquisitionContextSRStorage("1.2.840.10008.5.1.4.1.1.88.71", "Acquisition Context SR Storage"),
    /**
     * Simplified Adult Echo SR Storage SOP Class.
     */
    SimplifiedAdultEchoSRStorage("1.2.840.10008.5.1.4.1.1.88.72", "Simplified Adult Echo SR Storage"),
    /**
     * Patient Radiation Dose SR Storage SOP Class.
     */
    PatientRadiationDoseSRStorage("1.2.840.10008.5.1.4.1.1.88.73", "Patient Radiation Dose SR Storage"),
    /**
     * Planned Imaging Agent Administration SR Storage SOP Class.
     */
    PlannedImagingAgentAdministrationSRStorage("1.2.840.10008.5.1.4.1.1.88.74",
            "Planned Imaging Agent Administration SR Storage"),
    /**
     * Performed Imaging Agent Administration SR Storage SOP Class.
     */
    PerformedImagingAgentAdministrationSRStorage("1.2.840.10008.5.1.4.1.1.88.75",
            "Performed Imaging Agent Administration SR Storage"),
    /**
     * Enhanced X-Ray Radiation Dose SR Storage SOP Class.
     */
    EnhancedXRayRadiationDoseSRStorage("1.2.840.10008.5.1.4.1.1.88.76", "Enhanced X-Ray Radiation Dose SR Storage"),
    /**
     * Content Assessment Results Storage SOP Class.
     */
    ContentAssessmentResultsStorage("1.2.840.10008.5.1.4.1.1.90.1", "Content Assessment Results Storage"),
    /**
     * Microscopy Bulk Simple Annotations Storage SOP Class.
     */
    MicroscopyBulkSimpleAnnotationsStorage("1.2.840.10008.5.1.4.1.1.91.1",
            "Microscopy Bulk Simple Annotations Storage"),
    /**
     * Encapsulated PDF Storage SOP Class.
     */
    EncapsulatedPDFStorage("1.2.840.10008.5.1.4.1.1.104.1", "Encapsulated PDF Storage"),
    /**
     * Encapsulated CDA Storage SOP Class.
     */
    EncapsulatedCDAStorage("1.2.840.10008.5.1.4.1.1.104.2", "Encapsulated CDA Storage"),
    /**
     * Encapsulated STL Storage SOP Class.
     */
    EncapsulatedSTLStorage("1.2.840.10008.5.1.4.1.1.104.3", "Encapsulated STL Storage"),
    /**
     * Encapsulated OBJ Storage SOP Class.
     */
    EncapsulatedOBJStorage("1.2.840.10008.5.1.4.1.1.104.4", "Encapsulated OBJ Storage"),
    /**
     * Encapsulated MTL Storage SOP Class.
     */
    EncapsulatedMTLStorage("1.2.840.10008.5.1.4.1.1.104.5", "Encapsulated MTL Storage"),
    /**
     * Positron Emission Tomography Image Storage SOP Class.
     */
    PositronEmissionTomographyImageStorage("1.2.840.10008.5.1.4.1.1.128", "Positron Emission Tomography Image Storage"),
    /**
     * Legacy Converted Enhanced PET Image Storage SOP Class.
     */
    LegacyConvertedEnhancedPETImageStorage("1.2.840.10008.5.1.4.1.1.128.1",
            "Legacy Converted Enhanced PET Image Storage"),
    /**
     * Standalone PET Curve Storage (Retired) SOP Class.
     */
    StandalonePETCurveStorage("1.2.840.10008.5.1.4.1.1.129", "Standalone PET Curve Storage (Retired)"),
    /**
     * Enhanced PET Image Storage SOP Class.
     */
    EnhancedPETImageStorage("1.2.840.10008.5.1.4.1.1.130", "Enhanced PET Image Storage"),
    /**
     * Basic Structured Display Storage SOP Class.
     */
    BasicStructuredDisplayStorage("1.2.840.10008.5.1.4.1.1.131", "Basic Structured Display Storage"),
    /**
     * CT Defined Procedure Protocol Storage SOP Class.
     */
    CTDefinedProcedureProtocolStorage("1.2.840.10008.5.1.4.1.1.200.1", "CT Defined Procedure Protocol Storage"),
    /**
     * CT Performed Procedure Protocol Storage SOP Class.
     */
    CTPerformedProcedureProtocolStorage("1.2.840.10008.5.1.4.1.1.200.2", "CT Performed Procedure Protocol Storage"),
    /**
     * Protocol Approval Storage SOP Class.
     */
    ProtocolApprovalStorage("1.2.840.10008.5.1.4.1.1.200.3", "Protocol Approval Storage"),
    /**
     * Protocol Approval Information Model - FIND SOP Class.
     */
    ProtocolApprovalInformationModelFind("1.2.840.10008.5.1.4.1.1.200.4", "Protocol Approval Information Model - FIND"),
    /**
     * Protocol Approval Information Model - MOVE SOP Class.
     */
    ProtocolApprovalInformationModelMove("1.2.840.10008.5.1.4.1.1.200.5", "Protocol Approval Information Model - MOVE"),
    /**
     * Protocol Approval Information Model - GET SOP Class.
     */
    ProtocolApprovalInformationModelGet("1.2.840.10008.5.1.4.1.1.200.6", "Protocol Approval Information Model - GET"),
    /**
     * XA Defined Procedure Protocol Storage SOP Class.
     */
    XADefinedProcedureProtocolStorage("1.2.840.10008.5.1.4.1.1.200.7", "XA Defined Procedure Protocol Storage"),
    /**
     * XA Performed Procedure Protocol Storage SOP Class.
     */
    XAPerformedProcedureProtocolStorage("1.2.840.10008.5.1.4.1.1.200.8", "XA Performed Procedure Protocol Storage"),
    /**
     * Inventory Storage SOP Class.
     */
    InventoryStorage("1.2.840.10008.5.1.4.1.1.201.1", "Inventory Storage"),
    /**
     * Inventory - FIND SOP Class.
     */
    InventoryFind("1.2.840.10008.5.1.4.1.1.201.2", "Inventory - FIND"),
    /**
     * Inventory - MOVE SOP Class.
     */
    InventoryMove("1.2.840.10008.5.1.4.1.1.201.3", "Inventory - MOVE"),
    /**
     * Inventory - GET SOP Class.
     */
    InventoryGet("1.2.840.10008.5.1.4.1.1.201.4", "Inventory - GET"),
    /**
     * Inventory Creation SOP Class.
     */
    InventoryCreation("1.2.840.10008.5.1.4.1.1.201.5", "Inventory Creation"),
    /**
     * Repository Query SOP Class.
     */
    RepositoryQuery("1.2.840.10008.5.1.4.1.1.201.6", "Repository Query"),
    /**
     * Storage Management SOP Instance.
     */
    StorageManagementInstance("1.2.840.10008.5.1.4.1.1.201.1.1", "Storage Management SOP Instance"),
    /**
     * RT Image Storage SOP Class.
     */
    RTImageStorage("1.2.840.10008.5.1.4.1.1.481.1", "RT Image Storage"),
    /**
     * RT Dose Storage SOP Class.
     */
    RTDoseStorage("1.2.840.10008.5.1.4.1.1.481.2", "RT Dose Storage"),
    /**
     * RT Structure Set Storage SOP Class.
     */
    RTStructureSetStorage("1.2.840.10008.5.1.4.1.1.481.3", "RT Structure Set Storage"),
    /**
     * RT Beams Treatment Record Storage SOP Class.
     */
    RTBeamsTreatmentRecordStorage("1.2.840.10008.5.1.4.1.1.481.4", "RT Beams Treatment Record Storage"),
    /**
     * RT Plan Storage SOP Class.
     */
    RTPlanStorage("1.2.840.10008.5.1.4.1.1.481.5", "RT Plan Storage"),
    /**
     * RT Brachy Treatment Record Storage SOP Class.
     */
    RTBrachyTreatmentRecordStorage("1.2.840.10008.5.1.4.1.1.481.6", "RT Brachy Treatment Record Storage"),
    /**
     * RT Treatment Summary Record Storage SOP Class.
     */
    RTTreatmentSummaryRecordStorage("1.2.840.10008.5.1.4.1.1.481.7", "RT Treatment Summary Record Storage"),
    /**
     * RT Ion Plan Storage SOP Class.
     */
    RTIonPlanStorage("1.2.840.10008.5.1.4.1.1.481.8", "RT Ion Plan Storage"),
    /**
     * RT Ion Beams Treatment Record Storage SOP Class.
     */
    RTIonBeamsTreatmentRecordStorage("1.2.840.10008.5.1.4.1.1.481.9", "RT Ion Beams Treatment Record Storage"),
    /**
     * RT Physician Intent Storage SOP Class.
     */
    RTPhysicianIntentStorage("1.2.840.10008.5.1.4.1.1.481.10", "RT Physician Intent Storage"),
    /**
     * RT Segment Annotation Storage SOP Class.
     */
    RTSegmentAnnotationStorage("1.2.840.10008.5.1.4.1.1.481.11", "RT Segment Annotation Storage"),
    /**
     * RT Radiation Set Storage SOP Class.
     */
    RTRadiationSetStorage("1.2.840.10008.5.1.4.1.1.481.12", "RT Radiation Set Storage"),
    /**
     * C-Arm Photon-Electron Radiation Storage SOP Class.
     */
    CArmPhotonElectronRadiationStorage("1.2.840.10008.5.1.4.1.1.481.13", "C-Arm Photon-Electron Radiation Storage"),
    /**
     * Tomotherapeutic Radiation Storage SOP Class.
     */
    TomotherapeuticRadiationStorage("1.2.840.10008.5.1.4.1.1.481.14", "Tomotherapeutic Radiation Storage"),
    /**
     * Robotic-Arm Radiation Storage SOP Class.
     */
    RoboticArmRadiationStorage("1.2.840.10008.5.1.4.1.1.481.15", "Robotic-Arm Radiation Storage"),
    /**
     * RT Radiation Record Set Storage SOP Class.
     */
    RTRadiationRecordSetStorage("1.2.840.10008.5.1.4.1.1.481.16", "RT Radiation Record Set Storage"),
    /**
     * RT Radiation Salvage Record Storage SOP Class.
     */
    RTRadiationSalvageRecordStorage("1.2.840.10008.5.1.4.1.1.481.17", "RT Radiation Salvage Record Storage"),
    /**
     * Tomotherapeutic Radiation Record Storage SOP Class.
     */
    TomotherapeuticRadiationRecordStorage("1.2.840.10008.5.1.4.1.1.481.18", "Tomotherapeutic Radiation Record Storage"),
    /**
     * C-Arm Photon-Electron Radiation Record Storage SOP Class.
     */
    CArmPhotonElectronRadiationRecordStorage("1.2.840.10008.5.1.4.1.1.481.19",
            "C-Arm Photon-Electron Radiation Record Storage"),
    /**
     * Robotic Radiation Record Storage SOP Class.
     */
    RoboticRadiationRecordStorage("1.2.840.10008.5.1.4.1.1.481.20", "Robotic Radiation Record Storage"),
    /**
     * RT Radiation Set Delivery Instruction Storage SOP Class.
     */
    RTRadiationSetDeliveryInstructionStorage("1.2.840.10008.5.1.4.1.1.481.21",
            "RT Radiation Set Delivery Instruction Storage"),
    /**
     * RT Treatment Preparation Storage SOP Class.
     */
    RTTreatmentPreparationStorage("1.2.840.10008.5.1.4.1.1.481.22", "RT Treatment Preparation Storage"),
    /**
     * Enhanced RT Image Storage SOP Class.
     */
    EnhancedRTImageStorage("1.2.840.10008.5.1.4.1.1.481.23", "Enhanced RT Image Storage"),
    /**
     * Enhanced Continuous RT Image Storage SOP Class.
     */
    EnhancedContinuousRTImageStorage("1.2.840.10008.5.1.4.1.1.481.24", "Enhanced Continuous RT Image Storage"),
    /**
     * RT Patient Position Acquisition Instruction Storage SOP Class.
     */
    RTPatientPositionAcquisitionInstructionStorage("1.2.840.10008.5.1.4.1.1.481.25",
            "RT Patient Position Acquisition Instruction Storage"),
    /**
     * DICOS CT Image Storage SOP Class.
     */
    DICOSCTImageStorage("1.2.840.10008.5.1.4.1.1.501.1", "DICOS CT Image Storage"),
    /**
     * DICOS Digital X-Ray Image Storage - For Presentation SOP Class.
     */
    DICOSDigitalXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.501.2.1",
            "DICOS Digital X-Ray Image Storage - For Presentation"),
    /**
     * DICOS Digital X-Ray Image Storage - For Processing SOP Class.
     */
    DICOSDigitalXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.501.2.2",
            "DICOS Digital X-Ray Image Storage - For Processing"),
    /**
     * DICOS Threat Detection Report Storage SOP Class.
     */
    DICOSThreatDetectionReportStorage("1.2.840.10008.5.1.4.1.1.501.3", "DICOS Threat Detection Report Storage"),
    /**
     * DICOS 2D AIT Storage SOP Class.
     */
    DICOS2DAITStorage("1.2.840.10008.5.1.4.1.1.501.4", "DICOS 2D AIT Storage"),
    /**
     * DICOS 3D AIT Storage SOP Class.
     */
    DICOS3DAITStorage("1.2.840.10008.5.1.4.1.1.501.5", "DICOS 3D AIT Storage"),
    /**
     * DICOS Quadrupole Resonance (QR) Storage SOP Class.
     */
    DICOSQuadrupoleResonanceStorage("1.2.840.10008.5.1.4.1.1.501.6", "DICOS Quadrupole Resonance (QR) Storage"),
    /**
     * Eddy Current Image Storage SOP Class.
     */
    EddyCurrentImageStorage("1.2.840.10008.5.1.4.1.1.601.1", "Eddy Current Image Storage"),
    /**
     * Eddy Current Multi-frame Image Storage SOP Class.
     */
    EddyCurrentMultiFrameImageStorage("1.2.840.10008.5.1.4.1.1.601.2", "Eddy Current Multi-frame Image Storage"),
    /**
     * Patient Root Query/Retrieve Information Model - FIND SOP Class.
     */
    PatientRootQueryRetrieveInformationModelFind("1.2.840.10008.5.1.4.1.2.1.1",
            "Patient Root Query/Retrieve Information Model - FIND"),
    /**
     * Patient Root Query/Retrieve Information Model - MOVE SOP Class.
     */
    PatientRootQueryRetrieveInformationModelMove("1.2.840.10008.5.1.4.1.2.1.2",
            "Patient Root Query/Retrieve Information Model - MOVE"),
    /**
     * Patient Root Query/Retrieve Information Model - GET SOP Class.
     */
    PatientRootQueryRetrieveInformationModelGet("1.2.840.10008.5.1.4.1.2.1.3",
            "Patient Root Query/Retrieve Information Model - GET"),
    /**
     * Study Root Query/Retrieve Information Model - FIND SOP Class.
     */
    StudyRootQueryRetrieveInformationModelFind("1.2.840.10008.5.1.4.1.2.2.1",
            "Study Root Query/Retrieve Information Model - FIND"),
    /**
     * Study Root Query/Retrieve Information Model - MOVE SOP Class.
     */
    StudyRootQueryRetrieveInformationModelMove("1.2.840.10008.5.1.4.1.2.2.2",
            "Study Root Query/Retrieve Information Model - MOVE"),
    /**
     * Study Root Query/Retrieve Information Model - GET SOP Class.
     */
    StudyRootQueryRetrieveInformationModelGet("1.2.840.10008.5.1.4.1.2.2.3",
            "Study Root Query/Retrieve Information Model - GET"),
    /**
     * Patient/Study Only Query/Retrieve Information Model - FIND (Retired) SOP Class.
     */
    PatientStudyOnlyQueryRetrieveInformationModelFind("1.2.840.10008.5.1.4.1.2.3.1",
            "Patient/Study Only Query/Retrieve Information Model - FIND (Retired)"),
    /**
     * Patient/Study Only Query/Retrieve Information Model - MOVE (Retired) SOP Class.
     */
    PatientStudyOnlyQueryRetrieveInformationModelMove("1.2.840.10008.5.1.4.1.2.3.2",
            "Patient/Study Only Query/Retrieve Information Model - MOVE (Retired)"),
    /**
     * Patient/Study Only Query/Retrieve Information Model - GET (Retired) SOP Class.
     */
    PatientStudyOnlyQueryRetrieveInformationModelGet("1.2.840.10008.5.1.4.1.2.3.3",
            "Patient/Study Only Query/Retrieve Information Model - GET (Retired)"),
    /**
     * Composite Instance Root Retrieve - MOVE SOP Class.
     */
    CompositeInstanceRootRetrieveMove("1.2.840.10008.5.1.4.1.2.4.2", "Composite Instance Root Retrieve - MOVE"),
    /**
     * Composite Instance Root Retrieve - GET SOP Class.
     */
    CompositeInstanceRootRetrieveGet("1.2.840.10008.5.1.4.1.2.4.3", "Composite Instance Root Retrieve - GET"),
    /**
     * Composite Instance Retrieve Without Bulk Data - GET SOP Class.
     */
    CompositeInstanceRetrieveWithoutBulkDataGet("1.2.840.10008.5.1.4.1.2.5.3",
            "Composite Instance Retrieve Without Bulk Data - GET"),
    /**
     * Defined Procedure Protocol Information Model - FIND SOP Class.
     */
    DefinedProcedureProtocolInformationModelFind("1.2.840.10008.5.1.4.20.1",
            "Defined Procedure Protocol Information Model - FIND"),
    /**
     * Defined Procedure Protocol Information Model - MOVE SOP Class.
     */
    DefinedProcedureProtocolInformationModelMove("1.2.840.10008.5.1.4.20.2",
            "Defined Procedure Protocol Information Model - MOVE"),
    /**
     * Defined Procedure Protocol Information Model - GET SOP Class.
     */
    DefinedProcedureProtocolInformationModelGet("1.2.840.10008.5.1.4.20.3",
            "Defined Procedure Protocol Information Model - GET"),
    /**
     * Modality Worklist Information Model - FIND SOP Class.
     */
    ModalityWorklistInformationModelFind("1.2.840.10008.5.1.4.31", "Modality Worklist Information Model - FIND"),
    /**
     * General Purpose Worklist Management Meta SOP Class (Retired).
     */
    GeneralPurposeWorklistManagementMeta("1.2.840.10008.5.1.4.32",
            "General Purpose Worklist Management Meta SOP Class (Retired)"),
    /**
     * General Purpose Worklist Information Model - FIND (Retired) SOP Class.
     */
    GeneralPurposeWorklistInformationModelFind("1.2.840.10008.5.1.4.32.1",
            "General Purpose Worklist Information Model - FIND (Retired)"),
    /**
     * General Purpose Scheduled Procedure Step SOP Class (Retired).
     */
    GeneralPurposeScheduledProcedureStep("1.2.840.10008.5.1.4.32.2",
            "General Purpose Scheduled Procedure Step SOP Class (Retired)"),
    /**
     * General Purpose Performed Procedure Step SOP Class (Retired).
     */
    GeneralPurposePerformedProcedureStep("1.2.840.10008.5.1.4.32.3",
            "General Purpose Performed Procedure Step SOP Class (Retired)"),
    /**
     * Instance Availability Notification SOP Class.
     */
    InstanceAvailabilityNotification("1.2.840.10008.5.1.4.33", "Instance Availability Notification SOP Class"),
    /**
     * RT Beams Delivery Instruction Storage - Trial (Retired) SOP Class.
     */
    RTBeamsDeliveryInstructionStorageTrial("1.2.840.10008.5.1.4.34.1",
            "RT Beams Delivery Instruction Storage - Trial (Retired)"),
    /**
     * RT Conventional Machine Verification - Trial (Retired) SOP Class.
     */
    RTConventionalMachineVerificationTrial("1.2.840.10008.5.1.4.34.2",
            "RT Conventional Machine Verification - Trial (Retired)"),
    /**
     * RT Ion Machine Verification - Trial (Retired) SOP Class.
     */
    RTIonMachineVerificationTrial("1.2.840.10008.5.1.4.34.3", "RT Ion Machine Verification - Trial (Retired)"),
    /**
     * Unified Worklist and Procedure Step Service Class - Trial (Retired).
     */
    UnifiedWorklistAndProcedureStepTrial("1.2.840.10008.5.1.4.34.4",
            "Unified Worklist and Procedure Step Service Class - Trial (Retired)"),
    /**
     * Unified Procedure Step - Push SOP Class - Trial (Retired).
     */
    UnifiedProcedureStepPushTrial("1.2.840.10008.5.1.4.34.4.1",
            "Unified Procedure Step - Push SOP Class - Trial (Retired)"),
    /**
     * Unified Procedure Step - Watch SOP Class - Trial (Retired).
     */
    UnifiedProcedureStepWatchTrial("1.2.840.10008.5.1.4.34.4.2",
            "Unified Procedure Step - Watch SOP Class - Trial (Retired)"),
    /**
     * Unified Procedure Step - Pull SOP Class - Trial (Retired).
     */
    UnifiedProcedureStepPullTrial("1.2.840.10008.5.1.4.34.4.3",
            "Unified Procedure Step - Pull SOP Class - Trial (Retired)"),
    /**
     * Unified Procedure Step - Event SOP Class - Trial (Retired).
     */
    UnifiedProcedureStepEventTrial("1.2.840.10008.5.1.4.34.4.4",
            "Unified Procedure Step - Event SOP Class - Trial (Retired)"),
    /**
     * UPS Global Subscription SOP Instance.
     */
    UPSGlobalSubscriptionInstance("1.2.840.10008.5.1.4.34.5", "UPS Global Subscription SOP Instance"),
    /**
     * UPS Filtered Global Subscription SOP Instance.
     */
    UPSFilteredGlobalSubscriptionInstance("1.2.840.10008.5.1.4.34.5.1",
            "UPS Filtered Global Subscription SOP Instance"),
    /**
     * Unified Worklist and Procedure Step Service Class.
     */
    UnifiedWorklistAndProcedureStep("1.2.840.10008.5.1.4.34.6", "Unified Worklist and Procedure Step Service Class"),
    /**
     * Unified Procedure Step - Push SOP Class.
     */
    UnifiedProcedureStepPush("1.2.840.10008.5.1.4.34.6.1", "Unified Procedure Step - Push SOP Class"),
    /**
     * Unified Procedure Step - Watch SOP Class.
     */
    UnifiedProcedureStepWatch("1.2.840.10008.5.1.4.34.6.2", "Unified Procedure Step - Watch SOP Class"),
    /**
     * Unified Procedure Step - Pull SOP Class.
     */
    UnifiedProcedureStepPull("1.2.840.10008.5.1.4.34.6.3", "Unified Procedure Step - Pull SOP Class"),
    /**
     * Unified Procedure Step - Event SOP Class.
     */
    UnifiedProcedureStepEvent("1.2.840.10008.5.1.4.34.6.4", "Unified Procedure Step - Event SOP Class"),
    /**
     * Unified Procedure Step - Query SOP Class.
     */
    UnifiedProcedureStepQuery("1.2.840.10008.5.1.4.34.6.5", "Unified Procedure Step - Query SOP Class"),
    /**
     * RT Beams Delivery Instruction Storage SOP Class.
     */
    RTBeamsDeliveryInstructionStorage("1.2.840.10008.5.1.4.34.7", "RT Beams Delivery Instruction Storage"),
    /**
     * RT Conventional Machine Verification SOP Class.
     */
    RTConventionalMachineVerification("1.2.840.10008.5.1.4.34.8", "RT Conventional Machine Verification"),
    /**
     * RT Ion Machine Verification SOP Class.
     */
    RTIonMachineVerification("1.2.840.10008.5.1.4.34.9", "RT Ion Machine Verification"),
    /**
     * RT Brachy Application Setup Delivery Instruction Storage SOP Class.
     */
    RTBrachyApplicationSetupDeliveryInstructionStorage("1.2.840.10008.5.1.4.34.10",
            "RT Brachy Application Setup Delivery Instruction Storage"),
    /**
     * General Relevant Patient Information Query SOP Class.
     */
    GeneralRelevantPatientInformationQuery("1.2.840.10008.5.1.4.37.1", "General Relevant Patient Information Query"),
    /**
     * Breast Imaging Relevant Patient Information Query SOP Class.
     */
    BreastImagingRelevantPatientInformationQuery("1.2.840.10008.5.1.4.37.2",
            "Breast Imaging Relevant Patient Information Query"),
    /**
     * Cardiac Relevant Patient Information Query SOP Class.
     */
    CardiacRelevantPatientInformationQuery("1.2.840.10008.5.1.4.37.3", "Cardiac Relevant Patient Information Query"),
    /**
     * Hanging Protocol Storage SOP Class.
     */
    HangingProtocolStorage("1.2.840.10008.5.1.4.38.1", "Hanging Protocol Storage"),
    /**
     * Hanging Protocol Information Model - FIND SOP Class.
     */
    HangingProtocolInformationModelFind("1.2.840.10008.5.1.4.38.2", "Hanging Protocol Information Model - FIND"),
    /**
     * Hanging Protocol Information Model - MOVE SOP Class.
     */
    HangingProtocolInformationModelMove("1.2.840.10008.5.1.4.38.3", "Hanging Protocol Information Model - MOVE"),
    /**
     * Hanging Protocol Information Model - GET SOP Class.
     */
    HangingProtocolInformationModelGet("1.2.840.10008.5.1.4.38.4", "Hanging Protocol Information Model - GET"),
    /**
     * Color Palette Storage SOP Class.
     */
    ColorPaletteStorage("1.2.840.10008.5.1.4.39.1", "Color Palette Storage"),
    /**
     * Color Palette Query/Retrieve Information Model - FIND SOP Class.
     */
    ColorPaletteQueryRetrieveInformationModelFind("1.2.840.10008.5.1.4.39.2",
            "Color Palette Query/Retrieve Information Model - FIND"),
    /**
     * Color Palette Query/Retrieve Information Model - MOVE SOP Class.
     */
    ColorPaletteQueryRetrieveInformationModelMove("1.2.840.10008.5.1.4.39.3",
            "Color Palette Query/Retrieve Information Model - MOVE"),
    /**
     * Color Palette Query/Retrieve Information Model - GET SOP Class.
     */
    ColorPaletteQueryRetrieveInformationModelGet("1.2.840.10008.5.1.4.39.4",
            "Color Palette Query/Retrieve Information Model - GET"),
    /**
     * Product Characteristics Query SOP Class.
     */
    ProductCharacteristicsQuery("1.2.840.10008.5.1.4.41", "Product Characteristics Query SOP Class"),
    /**
     * Substance Approval Query SOP Class.
     */
    SubstanceApprovalQuery("1.2.840.10008.5.1.4.42", "Substance Approval Query SOP Class"),
    /**
     * Generic Implant Template Storage SOP Class.
     */
    GenericImplantTemplateStorage("1.2.840.10008.5.1.4.43.1", "Generic Implant Template Storage"),
    /**
     * Generic Implant Template Information Model - FIND SOP Class.
     */
    GenericImplantTemplateInformationModelFind("1.2.840.10008.5.1.4.43.2",
            "Generic Implant Template Information Model - FIND"),
    /**
     * Generic Implant Template Information Model - MOVE SOP Class.
     */
    GenericImplantTemplateInformationModelMove("1.2.840.10008.5.1.4.43.3",
            "Generic Implant Template Information Model - MOVE"),
    /**
     * Generic Implant Template Information Model - GET SOP Class.
     */
    GenericImplantTemplateInformationModelGet("1.2.840.10008.5.1.4.43.4",
            "Generic Implant Template Information Model - GET"),
    /**
     * Implant Assembly Template Storage SOP Class.
     */
    ImplantAssemblyTemplateStorage("1.2.840.10008.5.1.4.44.1", "Implant Assembly Template Storage"),
    /**
     * Implant Assembly Template Information Model - FIND SOP Class.
     */
    ImplantAssemblyTemplateInformationModelFind("1.2.840.10008.5.1.4.44.2",
            "Implant Assembly Template Information Model - FIND"),
    /**
     * Implant Assembly Template Information Model - MOVE SOP Class.
     */
    ImplantAssemblyTemplateInformationModelMove("1.2.840.10008.5.1.4.44.3",
            "Implant Assembly Template Information Model - MOVE"),
    /**
     * Implant Assembly Template Information Model - GET SOP Class.
     */
    ImplantAssemblyTemplateInformationModelGet("1.2.840.10008.5.1.4.44.4",
            "Implant Assembly Template Information Model - GET"),
    /**
     * Implant Template Group Storage SOP Class.
     */
    ImplantTemplateGroupStorage("1.2.840.10008.5.1.4.45.1", "Implant Template Group Storage"),
    /**
     * Implant Template Group Information Model - FIND SOP Class.
     */
    ImplantTemplateGroupInformationModelFind("1.2.840.10008.5.1.4.45.2",
            "Implant Template Group Information Model - FIND"),
    /**
     * Implant Template Group Information Model - MOVE SOP Class.
     */
    ImplantTemplateGroupInformationModelMove("1.2.840.10008.5.1.4.45.3",
            "Implant Template Group Information Model - MOVE"),
    /**
     * Implant Template Group Information Model - GET SOP Class.
     */
    ImplantTemplateGroupInformationModelGet("1.2.840.10008.5.1.4.45.4",
            "Implant Template Group Information Model - GET"),
    /**
     * Native DICOM Model.
     */
    NativeDICOMModel("1.2.840.10008.7.1.1", "Native DICOM Model"),
    /**
     * Abstract Multi-Dimensional Image Model.
     */
    AbstractMultiDimensionalImageModel("1.2.840.10008.7.1.2", "Abstract Multi-Dimensional Image Model"),
    /**
     * DICOM Content Mapping Resource.
     */
    DICOMContentMappingResource("1.2.840.10008.8.1.1", "DICOM Content Mapping Resource"),
    /**
     * Video Endoscopic Image Real-Time Communication.
     */
    VideoEndoscopicImageRealTimeCommunication("1.2.840.10008.10.1", "Video Endoscopic Image Real-Time Communication"),
    /**
     * Video Photographic Image Real-Time Communication.
     */
    VideoPhotographicImageRealTimeCommunication("1.2.840.10008.10.2",
            "Video Photographic Image Real-Time Communication"),
    /**
     * Audio Waveform Real-Time Communication.
     */
    AudioWaveformRealTimeCommunication("1.2.840.10008.10.3", "Audio Waveform Real-Time Communication"),
    /**
     * Rendition Selection Document Real-Time Communication.
     */
    RenditionSelectionDocumentRealTimeCommunication("1.2.840.10008.10.4",
            "Rendition Selection Document Real-Time Communication"),
    /**
     * DICOM Device Name.
     */
    dicomDeviceName("1.2.840.10008.15.0.3.1", "dicomDeviceName"),
    /**
     * DICOM Description.
     */
    dicomDescription("1.2.840.10008.15.0.3.2", "dicomDescription"),
    /**
     * DICOM Manufacturer.
     */
    dicomManufacturer("1.2.840.10008.15.0.3.3", "dicomManufacturer"),
    /**
     * DICOM Manufacturer Model Name.
     */
    dicomManufacturerModelName("1.2.840.10008.15.0.3.4", "dicomManufacturerModelName"),
    /**
     * DICOM Software Version.
     */
    dicomSoftwareVersion("1.2.840.10008.15.0.3.5", "dicomSoftwareVersion"),
    /**
     * DICOM Vendor Data.
     */
    dicomVendorData("1.2.840.10008.15.0.3.6", "dicomVendorData"),
    /**
     * DICOM AE Title.
     */
    dicomAETitle("1.2.840.10008.15.0.3.7", "dicomAETitle"),
    /**
     * DICOM Network Connection Reference.
     */
    dicomNetworkConnectionReference("1.2.840.10008.15.0.3.8", "dicomNetworkConnectionReference"),
    /**
     * DICOM Application Cluster.
     */
    dicomApplicationCluster("1.2.840.10008.15.0.3.9", "dicomApplicationCluster"),
    /**
     * DICOM Association Initiator.
     */
    dicomAssociationInitiator("1.2.840.10008.15.0.3.10", "dicomAssociationInitiator"),
    /**
     * DICOM Association Acceptor.
     */
    dicomAssociationAcceptor("1.2.840.10008.15.0.3.11", "dicomAssociationAcceptor"),
    /**
     * DICOM Hostname.
     */
    dicomHostname("1.2.840.10008.15.0.3.12", "dicomHostname"),
    /**
     * DICOM Port.
     */
    dicomPort("1.2.840.10008.15.0.3.13", "dicomPort"),
    /**
     * DICOM SOP Class.
     */
    dicomSOPClass("1.2.840.10008.15.0.3.14", "dicomSOPClass"),
    /**
     * DICOM Transfer Role.
     */
    dicomTransferRole("1.2.840.10008.15.0.3.15", "dicomTransferRole"),
    /**
     * DICOM Transfer Syntax.
     */
    dicomTransferSyntax("1.2.840.10008.15.0.3.16", "dicomTransferSyntax"),
    /**
     * DICOM Primary Device Type.
     */
    dicomPrimaryDeviceType("1.2.840.10008.15.0.3.17", "dicomPrimaryDeviceType"),
    /**
     * DICOM Related Device Reference.
     */
    dicomRelatedDeviceReference("1.2.840.10008.15.0.3.18", "dicomRelatedDeviceReference"),
    /**
     * DICOM Preferred Called AE Title.
     */
    dicomPreferredCalledAETitle("1.2.840.10008.15.0.3.19", "dicomPreferredCalledAETitle"),
    /**
     * DICOM TLS Cyphersuite.
     */
    dicomTLSCyphersuite("1.2.840.10008.15.0.3.20", "dicomTLSCyphersuite"),
    /**
     * DICOM Authorized Node Certificate Reference.
     */
    dicomAuthorizedNodeCertificateReference("1.2.840.10008.15.0.3.21", "dicomAuthorizedNodeCertificateReference"),
    /**
     * DICOM This Node Certificate Reference.
     */
    dicomThisNodeCertificateReference("1.2.840.10008.15.0.3.22", "dicomThisNodeCertificateReference"),
    /**
     * DICOM Installed.
     */
    dicomInstalled("1.2.840.10008.15.0.3.23", "dicomInstalled"),
    /**
     * DICOM Station Name.
     */
    dicomStationName("1.2.840.10008.15.0.3.24", "dicomStationName"),
    /**
     * DICOM Device Serial Number.
     */
    dicomDeviceSerialNumber("1.2.840.10008.15.0.3.25", "dicomDeviceSerialNumber"),
    /**
     * DICOM Institution Name.
     */
    dicomInstitutionName("1.2.840.10008.15.0.3.26", "dicomInstitutionName"),
    /**
     * DICOM Institution Address.
     */
    dicomInstitutionAddress("1.2.840.10008.15.0.3.27", "dicomInstitutionAddress"),
    /**
     * DICOM Institution Department Name.
     */
    dicomInstitutionDepartmentName("1.2.840.10008.15.0.3.28", "dicomInstitutionDepartmentName"),
    /**
     * DICOM Issuer of Patient ID.
     */
    dicomIssuerOfPatientID("1.2.840.10008.15.0.3.29", "dicomIssuerOfPatientID"),
    /**
     * DICOM Preferred Calling AE Title.
     */
    dicomPreferredCallingAETitle("1.2.840.10008.15.0.3.30", "dicomPreferredCallingAETitle"),
    /**
     * DICOM Supported Character Set.
     */
    dicomSupportedCharacterSet("1.2.840.10008.15.0.3.31", "dicomSupportedCharacterSet"),
    /**
     * DICOM Configuration Root.
     */
    dicomConfigurationRoot("1.2.840.10008.15.0.4.1", "dicomConfigurationRoot"),
    /**
     * DICOM Devices Root.
     */
    dicomDevicesRoot("1.2.840.10008.15.0.4.2", "dicomDevicesRoot"),
    /**
     * DICOM Unique AE Titles Registry Root.
     */
    dicomUniqueAETitlesRegistryRoot("1.2.840.10008.15.0.4.3", "dicomUniqueAETitlesRegistryRoot"),
    /**
     * DICOM Device.
     */
    dicomDevice("1.2.840.10008.15.0.4.4", "dicomDevice"),
    /**
     * DICOM Network AE.
     */
    dicomNetworkAE("1.2.840.10008.15.0.4.5", "dicomNetworkAE"),
    /**
     * DICOM Network Connection.
     */
    dicomNetworkConnection("1.2.840.10008.15.0.4.6", "dicomNetworkConnection"),
    /**
     * DICOM Unique AE Title.
     */
    dicomUniqueAETitle("1.2.840.10008.15.0.4.7", "dicomUniqueAETitle"),
    /**
     * DICOM Transfer Capability.
     */
    dicomTransferCapability("1.2.840.10008.15.0.4.8", "dicomTransferCapability"),
    /**
     * Universal Coordinated Time.
     */
    UTC("1.2.840.10008.15.1.1", "Universal Coordinated Time"),
    /**
     * Private Encapsulated Genozip Storage.
     */
    PrivateEncapsulatedGenozipStorage("1.2.40.0.13.1.5.1.4.1.1.104.1", "Private Encapsulated Genozip Storage"),
    /**
     * Private Encapsulated Bzip2 VCF Storage.
     */
    PrivateEncapsulatedBzip2VCFStorage("1.2.40.0.13.1.5.1.4.1.1.104.2", "Private Encapsulated Bzip2 VCF Storage"),
    /**
     * Private Encapsulated Bzip2 Document Storage.
     */
    PrivateEncapsulatedBzip2DocumentStorage("1.2.40.0.13.1.5.1.4.1.1.104.3",
            "Private Encapsulated Bzip2 Document Storage"),
    /**
     * Private Agfa Basic Attribute Presentation State.
     */
    PrivateAgfaBasicAttributePresentationState("1.2.124.113532.3500.7",
            "Private Agfa Basic Attribute Presentation State"),
    /**
     * Private Agfa Arrival Transaction.
     */
    PrivateAgfaArrivalTransaction("1.2.124.113532.3500.8.1", "Private Agfa Arrival Transaction"),
    /**
     * Private Agfa Dictation Transaction.
     */
    PrivateAgfaDictationTransaction("1.2.124.113532.3500.8.2", "Private Agfa Dictation Transaction"),
    /**
     * Private Agfa Report Transcription Transaction.
     */
    PrivateAgfaReportTranscriptionTransaction("1.2.124.113532.3500.8.3",
            "Private Agfa Report Transcription Transaction"),
    /**
     * Private Agfa Report Approval Transaction.
     */
    PrivateAgfaReportApprovalTransaction("1.2.124.113532.3500.8.4", "Private Agfa Report Approval Transaction"),
    /**
     * Private TomTec Annotation Storage.
     */
    PrivateTomTecAnnotationStorage("1.2.276.0.48.5.1.4.1.1.7", "Private TomTec Annotation Storage"),
    /**
     * Private Toshiba US Image Storage.
     */
    PrivateToshibaUSImageStorage("1.2.392.200036.9116.7.8.1.1.1", "Private Toshiba US Image Storage"),
    /**
     * Private Fuji CR Image Storage.
     */
    PrivateFujiCRImageStorage("1.2.392.200036.9125.1.1.2", "Private Fuji CR Image Storage"),
    /**
     * Private GE Collage Storage.
     */
    PrivateGECollageStorage("1.2.528.1.1001.5.1.1.1", "Private GE Collage Storage"),
    /**
     * Private ERAD Practice Builder Report Text Storage.
     */
    PrivateERADPracticeBuilderReportTextStorage("1.2.826.0.1.3680043.293.1.0.1",
            "Private ERAD Practice Builder Report Text Storage"),
    /**
     * Private ERAD Practice Builder Report Dictation Storage.
     */
    PrivateERADPracticeBuilderReportDictationStorage("1.2.826.0.1.3680043.293.1.0.2",
            "Private ERAD Practice Builder Report Dictation Storage"),
    /**
     * Private Philips HP Live 3D 01 Storage.
     */
    PrivatePhilipsHPLive3D01Storage("1.2.840.113543.6.6.1.3.10001", "Private Philips HP Live 3D 01 Storage"),
    /**
     * Private Philips HP Live 3D 02 Storage.
     */
    PrivatePhilipsHPLive3D02Storage("1.2.840.113543.6.6.1.3.10002", "Private Philips HP Live 3D 02 Storage"),
    /**
     * Private GE 3D Model Storage.
     */
    PrivateGE3DModelStorage("1.2.840.113619.4.26", "Private GE 3D Model Storage"),
    /**
     * Private GE Dicom CT Image Info Object.
     */
    PrivateGEDicomCTImageInfoObject("1.2.840.113619.4.3", "Private GE Dicom CT Image Info Object"),
    /**
     * Private GE Dicom Display Image Info Object.
     */
    PrivateGEDicomDisplayImageInfoObject("1.2.840.113619.4.4", "Private GE Dicom Display Image Info Object"),
    /**
     * Private GE Dicom MR Image Info Object.
     */
    PrivateGEDicomMRImageInfoObject("1.2.840.113619.4.2", "Private GE Dicom MR Image Info Object"),
    /**
     * Private GE eNTEGRA Protocol or NM Genie Storage.
     */
    PrivateGEeNTEGRAProtocolOrNMGenieStorage("1.2.840.113619.4.27", "Private GE eNTEGRA Protocol or NM Genie Storage"),
    /**
     * Private GE PET Raw Data Storage.
     */
    PrivateGEPETRawDataStorage("1.2.840.113619.4.30", "Private GE PET Raw Data Storage"),
    /**
     * Private GE RT Plan Storage.
     */
    PrivateGERTPlanStorage("1.2.840.113619.4.5.249", "Private GE RT Plan Storage"),
    /**
     * Private PixelMed Legacy Converted Enhanced CT Image Storage.
     */
    PrivatePixelMedLegacyConvertedEnhancedCTImageStorage("1.3.6.1.4.1.5962.301.1",
            "Private PixelMed Legacy Converted Enhanced CT Image Storage"),
    /**
     * Private PixelMed Legacy Converted Enhanced MR Image Storage.
     */
    PrivatePixelMedLegacyConvertedEnhancedMRImageStorage("1.3.6.1.4.1.5962.301.2",
            "Private PixelMed Legacy Converted Enhanced MR Image Storage"),
    /**
     * Private PixelMed Legacy Converted Enhanced PET Image Storage.
     */
    PrivatePixelMedLegacyConvertedEnhancedPETImageStorage("1.3.6.1.4.1.5962.301.3",
            "Private PixelMed Legacy Converted Enhanced PET Image Storage"),
    /**
     * Private PixelMed Floating Point Image Storage.
     */
    PrivatePixelMedFloatingPointImageStorage("1.3.6.1.4.1.5962.301.9", "Private PixelMed Floating Point Image Storage"),
    /**
     * Private Siemens CSA Non Image Storage.
     */
    PrivateSiemensCSANonImageStorage("1.3.12.2.1107.5.9.1", "Private Siemens CSA Non Image Storage"),
    /**
     * Private Siemens CT MR Volume Storage.
     */
    PrivateSiemensCTMRVolumeStorage("1.3.12.2.1107.5.99.3.10", "Private Siemens CT MR Volume Storage"),
    /**
     * Private Siemens AX Frame Sets Storage.
     */
    PrivateSiemensAXFrameSetsStorage("1.3.12.2.1107.5.99.3.11", "Private Siemens AX Frame Sets Storage"),
    /**
     * Private Philips Specialised XA Storage.
     */
    PrivatePhilipsSpecialisedXAStorage("1.3.46.670589.2.3.1.1", "Private Philips Specialised XA Storage"),
    /**
     * Private Philips CX Image Storage.
     */
    PrivatePhilipsCXImageStorage("1.3.46.670589.2.4.1.1", "Private Philips CX Image Storage"),
    /**
     * Private Philips 3D Presentation State Storage.
     */
    PrivatePhilips3DPresentationStateStorage("1.3.46.670589.2.5.1.1", "Private Philips 3D Presentation State Storage"),
    /**
     * Private Philips VRML Storage.
     */
    PrivatePhilipsVRMLStorage("1.3.46.670589.2.8.1.1", "Private Philips VRML Storage"),
    /**
     * Private Philips Volume Set Storage.
     */
    PrivatePhilipsVolumeSetStorage("1.3.46.670589.2.11.1.1", "Private Philips Volume Set Storage"),
    /**
     * Private Philips Volume Storage (Retired).
     */
    PrivatePhilipsVolumeStorageRetired("1.3.46.670589.5.0.1", "Private Philips Volume Storage (Retired)"),
    /**
     * Private Philips Volume Storage.
     */
    PrivatePhilipsVolumeStorage("1.3.46.670589.5.0.1.1", "Private Philips Volume Storage"),
    /**
     * Private Philips 3D Object Storage (Retired).
     */
    PrivatePhilips3DObjectStorageRetired("1.3.46.670589.5.0.2", "Private Philips 3D Object Storage (Retired)"),
    /**
     * Private Philips 3D Object Storage.
     */
    PrivatePhilips3DObjectStorage("1.3.46.670589.5.0.2.1", "Private Philips 3D Object Storage"),
    /**
     * Private Philips Surface Storage (Retired).
     */
    PrivatePhilipsSurfaceStorageRetired("1.3.46.670589.5.0.3", "Private Philips Surface Storage (Retired)"),
    /**
     * Private Philips Surface Storage.
     */
    PrivatePhilipsSurfaceStorage("1.3.46.670589.5.0.3.1", "Private Philips Surface Storage"),
    /**
     * Private Philips Composite Object Storage.
     */
    PrivatePhilipsCompositeObjectStorage("1.3.46.670589.5.0.4", "Private Philips Composite Object Storage"),
    /**
     * Private Philips MR Cardio Profile Storage.
     */
    PrivatePhilipsMRCardioProfileStorage("1.3.46.670589.5.0.7", "Private Philips MR Cardio Profile Storage"),
    /**
     * Private Philips MR Cardio Storage (Retired).
     */
    PrivatePhilipsMRCardioStorageRetired("1.3.46.670589.5.0.8", "Private Philips MR Cardio Storage (Retired)"),
    /**
     * Private Philips MR Cardio Storage.
     */
    PrivatePhilipsMRCardioStorage("1.3.46.670589.5.0.8.1", "Private Philips MR Cardio Storage"),
    /**
     * Private Philips CT Synthetic Image Storage.
     */
    PrivatePhilipsCTSyntheticImageStorage("1.3.46.670589.5.0.9", "Private Philips CT Synthetic Image Storage"),
    /**
     * Private Philips MR Synthetic Image Storage.
     */
    PrivatePhilipsMRSyntheticImageStorage("1.3.46.670589.5.0.10", "Private Philips MR Synthetic Image Storage"),
    /**
     * Private Philips MR Cardio Analysis Storage (Retired).
     */
    PrivatePhilipsMRCardioAnalysisStorageRetired("1.3.46.670589.5.0.11",
            "Private Philips MR Cardio Analysis Storage (Retired)"),
    /**
     * Private Philips MR Cardio Analysis Storage.
     */
    PrivatePhilipsMRCardioAnalysisStorage("1.3.46.670589.5.0.11.1", "Private Philips MR Cardio Analysis Storage"),
    /**
     * Private Philips CX Synthetic Image Storage.
     */
    PrivatePhilipsCXSyntheticImageStorage("1.3.46.670589.5.0.12", "Private Philips CX Synthetic Image Storage"),
    /**
     * Private Philips Perfusion Storage.
     */
    PrivatePhilipsPerfusionStorage("1.3.46.670589.5.0.13", "Private Philips Perfusion Storage"),
    /**
     * Private Philips Perfusion Image Storage.
     */
    PrivatePhilipsPerfusionImageStorage("1.3.46.670589.5.0.14", "Private Philips Perfusion Image Storage"),
    /**
     * Private Philips X-Ray MF Storage.
     */
    PrivatePhilipsXRayMFStorage("1.3.46.670589.7.8.1618510091", "Private Philips X-Ray MF Storage"),
    /**
     * Private Philips Live Run Storage.
     */
    PrivatePhilipsLiveRunStorage("1.3.46.670589.7.8.1618510092", "Private Philips Live Run Storage"),
    /**
     * Private Philips Run Storage.
     */
    PrivatePhilipsRunStorage("1.3.46.670589.7.8.16185100129", "Private Philips Run Storage"),
    /**
     * Private Philips Reconstruction Storage.
     */
    PrivatePhilipsReconstructionStorage("1.3.46.670589.7.8.16185100130", "Private Philips Reconstruction Storage"),
    /**
     * Private Philips MR Spectrum Storage.
     */
    PrivatePhilipsMRSpectrumStorage("1.3.46.670589.11.0.0.12.1", "Private Philips MR Spectrum Storage"),
    /**
     * Private Philips MR Series Data Storage.
     */
    PrivatePhilipsMRSeriesDataStorage("1.3.46.670589.11.0.0.12.2", "Private Philips MR Series Data Storage"),
    /**
     * Private Philips MR Color Image Storage.
     */
    PrivatePhilipsMRColorImageStorage("1.3.46.670589.11.0.0.12.3", "Private Philips MR Color Image Storage"),
    /**
     * Private Philips MR Examcard Storage.
     */
    PrivatePhilipsMRExamcardStorage("1.3.46.670589.11.0.0.12.4", "Private Philips MR Examcard Storage"),
    /**
     * Private PMOD Multi-frame Image Storage.
     */
    PrivatePMODMultiFrameImageStorage("2.16.840.1.114033.5.1.4.1.1.130", "Private PMOD Multi-frame Image Storage");

    /**
     * Regular expression pattern for validating UID format.
     */
    public static final Pattern PATTERN = Pattern.compile("[012]((\\.0)|(\\.[1-9]\\d*))+");
    /**
     * The US-ASCII charset.
     */
    public static final java.nio.charset.Charset ASCII = Charset.US_ASCII;

    /**
     * The root UID for generating Universally Unique Identifiers (UUIDs) based on ITU-T X.667 | ISO/IEC 9834-8.
     *
     * @see <a href="http://www.oid-info.com/get/2.25">OID repository {joint-iso-itu-t(2) uuid(25)}</a>
     */
    private static final String UUID_ROOT = "2.25";
    /**
     * The default root for UID generation, initialized to {@link #UUID_ROOT}.
     */
    public static String root = UUID_ROOT;

    /**
     * The UID string.
     */
    public final String uid;
    /**
     * The description of the UID.
     */
    public final String desc;

    /**
     * Constructs a UID enum constant.
     *
     * @param uid  The UID string.
     * @param desc The description of the UID.
     */
    UID(String uid, String desc) {
        this.uid = uid;
        this.desc = desc;
    }

    /**
     * Retrieves the UID enum constant corresponding to the given UID string.
     *
     * @param uid The UID string to look up.
     * @return The matching UID enum constant, or {@code null} if no match is found.
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
     * Gets the description for a given UID string.
     *
     * @param uid The UID string.
     * @return The description of the UID, or "?" if the UID is not found.
     */
    public static String nameOf(String uid) {
        for (UID u : UID.values()) {
            if (Objects.equals(u.uid, uid)) {
                return u.desc;
            }
        }
        return Symbol.QUESTION_MARK;
    }

    /**
     * Retrieves the UID string for a given keyword (enum constant name).
     *
     * @param keyword The name of the enum constant.
     * @return The corresponding UID string.
     * @throws IllegalArgumentException if the specified keyword does not exist.
     */
    public static String forName(String keyword) {
        try {
            return (String) UID.class.getField(keyword).get(null);
        } catch (Exception var2) {
            throw new IllegalArgumentException(keyword);
        }
    }

    /**
     * Converts a string to a standard UID format. The input can be a UID string, a keyword (enum constant name), or
     * "*".
     *
     * @param uid The input string to convert.
     * @return The converted UID string.
     */
    public static String toUID(String uid) {
        uid = uid.trim();
        return (uid.equals(Symbol.STAR) || Character.isDigit(uid.charAt(0))) ? uid : forName(uid);
    }

    /**
     * Converts a UUID into a DICOM UID string under a specified root.
     *
     * @param root The root for the new UID.
     * @param uuid The UUID to convert.
     * @return The generated UID string.
     */
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

    /**
     * Converts a comma-separated string of UIDs or keywords into a UID array.
     *
     * @param s The comma-separated string, or "*" for a wildcard.
     * @return An array of UID strings.
     */
    public static String[] toUIDs(String s) {
        if (Symbol.STAR.equals(s)) {
            return new String[] { Symbol.STAR };
        }
        String[] uids = StringKit.splitToArray(s, Symbol.COMMA);
        for (int i = 0; i < uids.length; i++) {
            uids[i] = toUID(uids[i]);
        }
        return uids;
    }

    /**
     * Finds UIDs whose keywords match the given regular expression.
     *
     * @param regex The regular expression to match against UID keywords (enum constant names).
     * @return An array of matching UID strings.
     */
    private static String[] findUIDs(String regex) {
        Pattern p = Pattern.compile(regex);
        Field[] fields = UID.class.getFields();
        String[] uids = new String[fields.length];
        int j = 0;
        for (Field field : fields) {
            if (p.matcher(field.getName()).matches())
                try {
                    uids[j++] = (String) field.get(null);
                } catch (Exception ignore) {
                }
        }
        return Arrays.copyOf(uids, j);
    }

    /**
     * Remaps a UID using the default root. A name-based UUID is generated from the original UID and appended to the
     * default root.
     *
     * @param uid The original UID to remap.
     * @return The remapped UID.
     */
    public static String remapUID(String uid) {
        return nameBasedUID(uid.getBytes(ASCII), root);
    }

    /**
     * Remaps a UID using a specified root. A name-based UUID is generated from the original UID and appended to the
     * specified root.
     *
     * @param uid  The original UID to remap.
     * @param root The root for the new UID.
     * @return The remapped UID.
     */
    public static String remapUID(String uid, String root) {
        checkRoot(root);
        return nameBasedUID(uid.getBytes(ASCII), root);
    }

    /**
     * Remaps UIDs within a DICOM {@link Attributes} object based on a given mapping.
     *
     * @param attrs  The DICOM attributes object.
     * @param uidMap A map where keys are original UIDs and values are the new UIDs.
     * @return The number of UIDs that were replaced.
     */
    public static int remapUID(Attributes attrs, Map<String, String> uidMap) {
        return remapUIDs(attrs, uidMap, null);
    }

    /**
     * Remaps UIDs within a DICOM {@link Attributes} object based on a given mapping, optionally recording the original
     * values of modified attributes.
     *
     * @param attrs    The DICOM attributes object in which UIDs will be replaced.
     * @param uidMap   A map containing the UID mappings.
     * @param modified An optional {@link Attributes} object to store the original values of overwritten attributes. Can
     *                 be {@code null}.
     * @return The number of UIDs that were replaced.
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

    /**
     * Generates a random UID using the specified root.
     *
     * @param root The root for the new UID.
     * @return A randomly generated UID string.
     */
    private static String randomUID(String root) {
        return toUID(root, UUID.randomUUID());
    }

    /**
     * Generates a name-based UID using the specified root.
     *
     * @param name The byte array from which to generate the UID.
     * @param root The root for the new UID.
     * @return A name-based UID string.
     */
    private static String nameBasedUID(byte[] name, String root) {
        return toUID(root, UUID.nameUUIDFromBytes(name));
    }

    /**
     * Appends a formatted UID string (UID and its description) to a {@link StringBuilder}.
     *
     * @param uid The UID string.
     * @param sb  The {@link StringBuilder} to append to.
     * @return The modified {@link StringBuilder}.
     */
    public static StringBuilder promptTo(String uid, StringBuilder sb) {
        return sb.append(uid).append(" - ").append(nameOf(uid));
    }

    /**
     * Creates a new random UID using the default root.
     *
     * @return A new UID string.
     */
    public static String createUID() {
        return randomUID(root);
    }

    /**
     * Creates a new random UID using the specified root.
     *
     * @param root The root for the new UID.
     * @return A new UID string.
     */
    public static String createUID(String root) {
        checkRoot(root);
        return randomUID(root);
    }

    /**
     * Creates a new name-based UID from a byte array using the default root.
     *
     * @param name The byte array from which to generate the UID.
     * @return A new name-based UID string.
     */
    public static String createNameBasedUID(byte[] name) {
        return nameBasedUID(name, root);
    }

    /**
     * Creates a new name-based UID from a byte array using the specified root.
     *
     * @param name The byte array from which to generate the UID.
     * @param root The root for the new UID.
     * @return A new name-based UID string.
     */
    private static String createNameBasedUID(byte[] name, String root) {
        checkRoot(root);
        return nameBasedUID(name, root);
    }

    /**
     * Returns the given UID if it is not null, otherwise creates a new random UID using the default root.
     *
     * @param uid The original UID, which can be {@code null}.
     * @return The original UID or a newly created one if the original was {@code null}.
     */
    public static String createUIDIfNull(String uid) {
        return uid == null ? randomUID(root) : uid;
    }

    /**
     * Returns the given UID if it is not null, otherwise creates a new random UID using the specified root.
     *
     * @param uid  The original UID, which can be {@code null}.
     * @param root The root to use for UID creation if the original is {@code null}.
     * @return The original UID or a newly created one.
     */
    private static String createUIDIfNull(String uid, String root) {
        checkRoot(root);
        return uid == null ? randomUID(root) : uid;
    }

    /**
     * Validates if the given string conforms to the DICOM UID format. A valid UID has a maximum length of 64 characters
     * and matches the defined pattern.
     *
     * @param uid The UID string to validate.
     * @return {@code true} if the UID format is valid, {@code false} otherwise.
     */
    private static boolean isValid1(String uid) {
        return uid.length() <= 64 && PATTERN.matcher(uid).matches();
    }

    /**
     * Gets the current default UID root.
     *
     * @return The current UID root string.
     */
    private static final String getRoot() {
        return root;
    }

    /**
     * Sets the default UID root.
     *
     * @param root The new UID root string.
     */
    private static final void setRoot(String root) {
        checkRoot(root);
        UID.root = root;
    }

    /**
     * Checks if the provided UID root is valid. A valid root must not exceed 24 characters and must conform to the UID
     * format.
     *
     * @param root The UID root to check.
     * @throws IllegalArgumentException if the UID root is invalid.
     */
    private static void checkRoot(String root) {
        if (root.length() > 24)
            throw new IllegalArgumentException("root length > 24");
        if (!isValid1(root))
            throw new IllegalArgumentException(root);
    }

    /**
     * Gets the UID string of this enum constant.
     *
     * @return The UID string.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Gets the description of this enum constant.
     *
     * @return The UID description.
     */
    public String getDesc() {
        return desc;
    }

}
