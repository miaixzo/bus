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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.UIDVisitor;

/**
 * DICOM唯一标识符(UID)枚举类。
 *
 * <p>
 * 该枚举类定义了DICOM标准中使用的各种唯一标识符(UID)，包括SOP类、传输语法、信息模型等。 每个枚举常量包含一个UID字符串和对应的描述信息。
 * </p>
 *
 * <p>
 * 此外，该类还提供了用于创建、验证和转换UID的实用方法，包括基于UUID生成UID的方法， 以及将DICOM属性中的UID按照指定映射进行重映射的方法。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum UID {

    /**
     * 验证SOP类
     */
    Verification("1.2.840.10008.1.1", "Verification SOP Class"),
    /**
     * 隐式VR小端序传输语法
     */
    ImplicitVRLittleEndian("1.2.840.10008.1.2", "Implicit VR Little Endian"),
    /**
     * 显式VR小端序传输语法
     */
    ExplicitVRLittleEndian("1.2.840.10008.1.2.1", "Explicit VR Little Endian"),
    /**
     * 封装未压缩显式VR小端序传输语法
     */
    EncapsulatedUncompressedExplicitVRLittleEndian("1.2.840.10008.1.2.1.98",
            "Encapsulated Uncompressed Explicit VR Little Endian"),
    /**
     * 压缩显式VR小端序传输语法
     */
    DeflatedExplicitVRLittleEndian("1.2.840.10008.1.2.1.99", "Deflated Explicit VR Little Endian"),
    /**
     * 显式VR大端序传输语法（已弃用）
     */
    ExplicitVRBigEndian("1.2.840.10008.1.2.2", "Explicit VR Big Endian (Retired)"),
    /**
     * JPEG基线8位图像压缩
     */
    JPEGBaseline8Bit("1.2.840.10008.1.2.4.50", "JPEG Baseline (Process 1)"),
    /**
     * JPEG扩展12位图像压缩
     */
    JPEGExtended12Bit("1.2.840.10008.1.2.4.51", "JPEG Extended (Process 2 & 4)"),
    /**
     * JPEG扩展35图像压缩（已弃用）
     */
    JPEGExtended35("1.2.840.10008.1.2.4.52", "JPEG Extended (Process 3 & 5) (Retired)"),
    /**
     * JPEG频谱选择非分层68图像压缩
     */
    JPEGSpectralSelectionNonHierarchical68("1.2.840.10008.1.2.4.53", "JPEG Spectral Selection"),
    /**
     * JPEG频谱选择非分层79图像压缩
     */
    JPEGSpectralSelectionNonHierarchical79("1.2.840.10008.1.2.4.54", "JPEG Spectral Selection"),
    /**
     * JPEG全进程非分层1012图像压缩
     */
    JPEGFullProgressionNonHierarchical1012("1.2.840.10008.1.2.4.55", "JPEG Full Progression"),
    /**
     * JPEG全进程非分层1113图像压缩
     */
    JPEGFullProgressionNonHierarchical1113("1.2.840.10008.1.2.4.56", "JPEG Full Progression"),
    /**
     * JPEG无损压缩
     */
    JPEGLossless("1.2.840.10008.1.2.4.57", "JPEG Lossless"),
    /**
     * JPEG无损非分层15图像压缩
     */
    JPEGLosslessNonHierarchical15("1.2.840.10008.1.2.4.58", "JPEG Lossless"),
    /**
     * JPEG扩展分层1618图像压缩
     */
    JPEGExtendedHierarchical1618("1.2.840.10008.1.2.4.59", "JPEG Extended"),
    /**
     * JPEG扩展分层1719图像压缩
     */
    JPEGExtendedHierarchical1719("1.2.840.10008.1.2.4.60", "JPEG Extended"),
    /**
     * JPEG频谱选择分层2022图像压缩
     */
    JPEGSpectralSelectionHierarchical2022("1.2.840.10008.1.2.4.61", "JPEG Spectral Selection"),
    /**
     * JPEG频谱选择分层2123图像压缩
     */
    JPEGSpectralSelectionHierarchical2123("1.2.840.10008.1.2.4.62", "JPEG Spectral Selection"),
    /**
     * JPEG全进程分层2426图像压缩
     */
    JPEGFullProgressionHierarchical2426("1.2.840.10008.1.2.4.63", "JPEG Full Progression"),
    /**
     * JPEG全进程分层2527图像压缩
     */
    JPEGFullProgressionHierarchical2527("1.2.840.10008.1.2.4.64", "JPEG Full Progression"),
    /**
     * JPEG无损分层28图像压缩
     */
    JPEGLosslessHierarchical28("1.2.840.10008.1.2.4.65", "JPEG Lossless"),
    /**
     * JPEG无损分层29图像压缩
     */
    JPEGLosslessHierarchical29("1.2.840.10008.1.2.4.66", "JPEG Lossless"),
    /**
     * JPEG无损SV1压缩
     */
    JPEGLosslessSV1("1.2.840.10008.1.2.4.70", "JPEG Lossless"),
    /**
     * JPEG-LS无损图像压缩
     */
    JPEGLSLossless("1.2.840.10008.1.2.4.80", "JPEG-LS Lossless Image Compression"),
    /**
     * JPEG-LS有损（近无损）图像压缩
     */
    JPEGLSNearLossless("1.2.840.10008.1.2.4.81", "JPEG-LS Lossy (Near-Lossless) Image Compression"),
    /**
     * JPEG 2000无损图像压缩
     */
    JPEG2000Lossless("1.2.840.10008.1.2.4.90", "JPEG 2000 Image Compression (Lossless Only)"),
    /**
     * JPEG 2000图像压缩
     */
    JPEG2000("1.2.840.10008.1.2.4.91", "JPEG 2000 Image Compression"),
    /**
     * JPEG 2000第2部分多分量无损图像压缩
     */
    JPEG2000MCLossless("1.2.840.10008.1.2.4.92", "JPEG 2000 Part 2 Multi-component Image Compression (Lossless Only)"),
    /**
     * JPEG 2000第2部分多分量图像压缩
     */
    JPEG2000MC("1.2.840.10008.1.2.4.93", "JPEG 2000 Part 2 Multi-component Image Compression"),
    /**
     * JPIP引用压缩
     */
    JPIPReferenced("1.2.840.10008.1.2.4.94", "JPIP Referenced"),
    /**
     * JPIP引用压缩（Deflate）
     */
    JPIPReferencedDeflate("1.2.840.10008.1.2.4.95", "JPIP Referenced Deflate"),
    /**
     * MPEG2主配置/主级别
     */
    MPEG2MPML("1.2.840.10008.1.2.4.100", "MPEG2 Main Profile / Main Level"),
    /**
     * 可分段MPEG2主配置/主级别
     */
    MPEG2MPMLF("1.2.840.10008.1.2.4.100.1", "Fragmentable MPEG2 Main Profile / Main Level"),
    /**
     * MPEG2主配置/高级别
     */
    MPEG2MPHL("1.2.840.10008.1.2.4.101", "MPEG2 Main Profile / High Level"),
    /**
     * 可分段MPEG2主配置/高级别
     */
    MPEG2MPHLF("1.2.840.10008.1.2.4.101.1", "Fragmentable MPEG2 Main Profile / High Level"),
    /**
     * MPEG-4 AVC/H.264高级配置/级别4.1
     */
    MPEG4HP41("1.2.840.10008.1.2.4.102", "MPEG-4 AVC/H.264 High Profile / Level 4.1"),
    /**
     * 可分段MPEG-4 AVC/H.264高级配置/级别4.1
     */
    MPEG4HP41F("1.2.840.10008.1.2.4.102.1", "Fragmentable MPEG-4 AVC/H.264 High Profile / Level 4.1"),
    /**
     * MPEG-4 AVC/H.264 BD兼容高级配置/级别4.1
     */
    MPEG4HP41BD("1.2.840.10008.1.2.4.103", "MPEG-4 AVC/H.264 BD-compatible High Profile / Level 4.1"),
    /**
     * 可分段MPEG-4 AVC/H.264 BD兼容高级配置/级别4.1
     */
    MPEG4HP41BDF("1.2.840.10008.1.2.4.103.1", "Fragmentable MPEG-4 AVC/H.264 BD-compatible High Profile / Level 4.1"),
    /**
     * MPEG-4 AVC/H.264高级配置/级别4.2（2D视频）
     */
    MPEG4HP422D("1.2.840.10008.1.2.4.104", "MPEG-4 AVC/H.264 High Profile / Level 4.2 For 2D Video"),
    /**
     * 可分段MPEG-4 AVC/H.264高级配置/级别4.2（2D视频）
     */
    MPEG4HP422DF("1.2.840.10008.1.2.4.104.1", "Fragmentable MPEG-4 AVC/H.264 High Profile / Level 4.2 For 2D Video"),
    /**
     * MPEG-4 AVC/H.264高级配置/级别4.2（3D视频）
     */
    MPEG4HP423D("1.2.840.10008.1.2.4.105", "MPEG-4 AVC/H.264 High Profile / Level 4.2 For 3D Video"),
    /**
     * 可分段MPEG-4 AVC/H.264高级配置/级别4.2（3D视频）
     */
    MPEG4HP423DF("1.2.840.10008.1.2.4.105.1", "Fragmentable MPEG-4 AVC/H.264 High Profile / Level 4.2 For 3D Video"),
    /**
     * MPEG-4 AVC/H.264立体声高级配置/级别4.2
     */
    MPEG4HP42STEREO("1.2.840.10008.1.2.4.106", "MPEG-4 AVC/H.264 Stereo High Profile / Level 4.2"),
    /**
     * 可分段MPEG-4 AVC/H.264立体声高级配置/级别4.2
     */
    MPEG4HP42STEREOF("1.2.840.10008.1.2.4.106.1", "Fragmentable MPEG-4 AVC/H.264 Stereo High Profile / Level 4.2"),
    /**
     * HEVC/H.265主配置/级别5.1
     */
    HEVCMP51("1.2.840.10008.1.2.4.107", "HEVC/H.265 Main Profile / Level 5.1"),
    /**
     * HEVC/H.265主10配置/级别5.1
     */
    HEVCM10P51("1.2.840.10008.1.2.4.108", "HEVC/H.265 Main 10 Profile / Level 5.1"),
    /**
     * 高吞吐量JPEG 2000无损图像压缩
     */
    HTJ2KLossless("1.2.840.10008.1.2.4.201", "High-Throughput JPEG 2000 Image Compression (Lossless Only)"),
    /**
     * 带RPCL选项的高吞吐量JPEG 2000无损图像压缩
     */
    HTJ2KLosslessRPCL("1.2.840.10008.1.2.4.202",
            "High-Throughput JPEG 2000 with RPCL Options Image Compression (Lossless Only)"),
    /**
     * 高吞吐量JPEG 2000图像压缩
     */
    HTJ2K("1.2.840.10008.1.2.4.203", "High-Throughput JPEG 2000 Image Compression"),
    /**
     * JPIP HTJ2K引用压缩
     */
    JPIPHTJ2KReferenced("1.2.840.10008.1.2.4.204", "JPIP HTJ2K Referenced"),
    /**
     * JPIP HTJ2K引用压缩（Deflate）
     */
    JPIPHTJ2KReferencedDeflate("1.2.840.10008.1.2.4.205", "JPIP HTJ2K Referenced Deflate"),
    /**
     * RLE无损压缩
     */
    RLELossless("1.2.840.10008.1.2.5", "RLE Lossless"),
    /**
     * RFC 2557 MIME封装（已弃用）
     */
    RFC2557MIMEEncapsulation("1.2.840.10008.1.2.6.1", "RFC 2557 MIME encapsulation (Retired)"),
    /**
     * XML编码（已弃用）
     */
    XMLEncoding("1.2.840.10008.1.2.6.2", "XML Encoding (Retired)"),
    /**
     * SMPTE ST 2110-20未压缩渐进式活动视频
     */
    SMPTEST211020UncompressedProgressiveActiveVideo("1.2.840.10008.1.2.7.1",
            "SMPTE ST 2110-20 Uncompressed Progressive Active Video"),
    /**
     * SMPTE ST 2110-20未压缩隔行活动视频
     */
    SMPTEST211020UncompressedInterlacedActiveVideo("1.2.840.10008.1.2.7.2",
            "SMPTE ST 2110-20 Uncompressed Interlaced Active Video"),
    /**
     * SMPTE ST 2110-30 PCM数字音频
     */
    SMPTEST211030PCMDigitalAudio("1.2.840.10008.1.2.7.3", "SMPTE ST 2110-30 PCM Digital Audio"),
    /**
     * 媒体存储目录存储
     */
    MediaStorageDirectoryStorage("1.2.840.10008.1.3.10", "Media Storage Directory Storage"),
    /**
     * 热铁颜色调色板SOP实例
     */
    HotIronPalette("1.2.840.10008.1.5.1", "Hot Iron Color Palette SOP Instance"),
    /**
     * PET颜色调色板SOP实例
     */
    PETPalette("1.2.840.10008.1.5.2", "PET Color Palette SOP Instance"),
    /**
     * 热金属蓝颜色调色板SOP实例
     */
    HotMetalBluePalette("1.2.840.10008.1.5.3", "Hot Metal Blue Color Palette SOP Instance"),
    /**
     * PET 20步颜色调色板SOP实例
     */
    PET20StepPalette("1.2.840.10008.1.5.4", "PET 20 Step Color Palette SOP Instance"),
    /**
     * 春季颜色调色板SOP实例
     */
    SpringPalette("1.2.840.10008.1.5.5", "Spring Color Palette SOP Instance"),
    /**
     * 夏季颜色调色板SOP实例
     */
    SummerPalette("1.2.840.10008.1.5.6", "Summer Color Palette SOP Instance"),
    /**
     * 秋季颜色调色板SOP实例
     */
    FallPalette("1.2.840.10008.1.5.7", "Fall Color Palette SOP Instance"),
    /**
     * 冬季颜色调色板SOP实例
     */
    WinterPalette("1.2.840.10008.1.5.8", "Winter Color Palette SOP Instance"),
    /**
     * 基本研究内容通知SOP类（已弃用）
     */
    BasicStudyContentNotification("1.2.840.10008.1.9", "Basic Study Content Notification SOP Class (Retired)"),
    /**
     * Papyrus 3隐式VR小端序（已弃用）
     */
    Papyrus3ImplicitVRLittleEndian("1.2.840.10008.1.20", "Papyrus 3 Implicit VR Little Endian (Retired)"),
    /**
     * 存储承诺推送模型SOP类
     */
    StorageCommitmentPushModel("1.2.840.10008.1.20.1", "Storage Commitment Push Model SOP Class"),
    /**
     * 存储承诺推送模型SOP实例
     */
    StorageCommitmentPushModelInstance("1.2.840.10008.1.20.1.1", "Storage Commitment Push Model SOP Instance"),
    /**
     * 存储承诺拉取模型SOP类（已弃用）
     */
    StorageCommitmentPullModel("1.2.840.10008.1.20.2", "Storage Commitment Pull Model SOP Class (Retired)"),
    /**
     * 存储承诺拉取模型SOP实例（已弃用）
     */
    StorageCommitmentPullModelInstance("1.2.840.10008.1.20.2.1",
            "Storage Commitment Pull Model SOP Instance (Retired)"),
    /**
     * 程序事件记录SOP类
     */
    ProceduralEventLogging("1.2.840.10008.1.40", "Procedural Event Logging SOP Class"),
    /**
     * 程序事件记录SOP实例
     */
    ProceduralEventLoggingInstance("1.2.840.10008.1.40.1", "Procedural Event Logging SOP Instance"),
    /**
     * 物质管理记录SOP类
     */
    SubstanceAdministrationLogging("1.2.840.10008.1.42", "Substance Administration Logging SOP Class"),
    /**
     * 物质管理记录SOP实例
     */
    SubstanceAdministrationLoggingInstance("1.2.840.10008.1.42.1", "Substance Administration Logging SOP Instance"),
    /**
     * DICOM UID注册表
     */
    DCMUID("1.2.840.10008.2.6.1", "DICOM UID Registry"),
    /**
     * DICOM控制术语
     */
    DCM("1.2.840.10008.2.16.4", "DICOM Controlled Terminology"),
    /**
     * 成年小鼠解剖本体论
     */
    MA("1.2.840.10008.2.16.5", "Adult Mouse Anatomy Ontology"),
    /**
     * Uberon本体论
     */
    UBERON("1.2.840.10008.2.16.6", "Uberon Ontology"),
    /**
     * 综合分类信息系统(ITIS)分类序列号(TSN)
     */
    ITIS_TSN("1.2.840.10008.2.16.7", "Integrated Taxonomic Information System (ITIS) Taxonomic Serial Number (TSN)"),
    /**
     * 小鼠基因组计划(MGI)
     */
    MGI("1.2.840.10008.2.16.8", "Mouse Genome Initiative (MGI)"),
    /**
     * PubChem化合物CID
     */
    PUBCHEM_CID("1.2.840.10008.2.16.9", "PubChem Compound CID"),
    /**
     * 都柏林核心
     */
    DC("1.2.840.10008.2.16.10", "Dublin Core"),
    /**
     * 纽约大学黑色素瘤临床合作组
     */
    NYUMCCG("1.2.840.10008.2.16.11", "New York University Melanoma Clinical Cooperative Group"),
    /**
     * 梅奥诊所非放射学图像特定身体结构解剖表面区域指南
     */
    MAYONRISBSASRG("1.2.840.10008.2.16.12",
            "Mayo Clinic Non-radiological Images Specific Body Structure Anatomical Surface Region Guide"),
    /**
     * 图像生物标记标准化倡议
     */
    IBSI("1.2.840.10008.2.16.13", "Image Biomarker Standardisation Initiative"),
    /**
     * 放射组学本体论
     */
    RO("1.2.840.10008.2.16.14", "Radiomics Ontology"),
    /**
     * RadElement
     */
    RADELEMENT("1.2.840.10008.2.16.15", "RadElement"),
    /**
     * ICD-11
     */
    I11("1.2.840.10008.2.16.16", "ICD-11"),
    /**
     * 金属和合金统一编号系统(UNS)
     */
    UNS("1.2.840.10008.2.16.17", "Unified numbering system (UNS) for metals and alloys"),
    /**
     * 研究资源标识
     */
    RRID("1.2.840.10008.2.16.18", "Research Resource Identification"),
    /**
     * DICOM应用上下文名称
     */
    DICOMApplicationContext("1.2.840.10008.3.1.1.1", "DICOM Application Context Name"),
    /**
     * 分离患者管理SOP类（已弃用）
     */
    DetachedPatientManagement("1.2.840.10008.3.1.2.1.1", "Detached Patient Management SOP Class (Retired)"),
    /**
     * 分离患者管理元SOP类（已弃用）
     */
    DetachedPatientManagementMeta("1.2.840.10008.3.1.2.1.4", "Detached Patient Management Meta SOP Class (Retired)"),
    /**
     * 分离访问管理SOP类（已弃用）
     */
    DetachedVisitManagement("1.2.840.10008.3.1.2.2.1", "Detached Visit Management SOP Class (Retired)"),
    /**
     * 分离研究管理SOP类（已弃用）
     */
    DetachedStudyManagement("1.2.840.10008.3.1.2.3.1", "Detached Study Management SOP Class (Retired)"),
    /**
     * 研究组件管理SOP类（已弃用）
     */
    StudyComponentManagement("1.2.840.10008.3.1.2.3.2", "Study Component Management SOP Class (Retired)"),
    /**
     * 模态执行过程步骤SOP类
     */
    ModalityPerformedProcedureStep("1.2.840.10008.3.1.2.3.3", "Modality Performed Procedure Step SOP Class"),
    /**
     * 模态执行过程步骤检索SOP类
     */
    ModalityPerformedProcedureStepRetrieve("1.2.840.10008.3.1.2.3.4",
            "Modality Performed Procedure Step Retrieve SOP Class"),
    /**
     * 模态执行过程步骤通知SOP类
     */
    ModalityPerformedProcedureStepNotification("1.2.840.10008.3.1.2.3.5",
            "Modality Performed Procedure Step Notification SOP Class"),
    /**
     * 分离结果管理SOP类（已弃用）
     */
    DetachedResultsManagement("1.2.840.10008.3.1.2.5.1", "Detached Results Management SOP Class (Retired)"),
    /**
     * 分离结果管理元SOP类（已弃用）
     */
    DetachedResultsManagementMeta("1.2.840.10008.3.1.2.5.4", "Detached Results Management Meta SOP Class (Retired)"),
    /**
     * 分离研究管理元SOP类（已弃用）
     */
    DetachedStudyManagementMeta("1.2.840.10008.3.1.2.5.5", "Detached Study Management Meta SOP Class (Retired)"),
    /**
     * 分离解释管理SOP类（已弃用）
     */
    DetachedInterpretationManagement("1.2.840.10008.3.1.2.6.1",
            "Detached Interpretation Management SOP Class (Retired)"),
    /**
     * 存储服务类
     */
    Storage("1.2.840.10008.4.2", "Storage Service Class"),
    /**
     * 基本胶片会话SOP类
     */
    BasicFilmSession("1.2.840.10008.5.1.1.1", "Basic Film Session SOP Class"),
    /**
     * 基本胶片盒SOP类
     */
    BasicFilmBox("1.2.840.10008.5.1.1.2", "Basic Film Box SOP Class"),
    /**
     * 基本灰度图像框SOP类
     */
    BasicGrayscaleImageBox("1.2.840.10008.5.1.1.4", "Basic Grayscale Image Box SOP Class"),
    /**
     * 基本彩色图像框SOP类
     */
    BasicColorImageBox("1.2.840.10008.5.1.1.4.1", "Basic Color Image Box SOP Class"),
    /**
     * 引用图像框SOP类（已弃用）
     */
    ReferencedImageBox("1.2.840.10008.5.1.1.4.2", "Referenced Image Box SOP Class (Retired)"),
    /**
     * 基本灰度打印管理元SOP类
     */
    BasicGrayscalePrintManagementMeta("1.2.840.10008.5.1.1.9", "Basic Grayscale Print Management Meta SOP Class"),
    /**
     * 引用灰度打印管理元SOP类（已弃用）
     */
    ReferencedGrayscalePrintManagementMeta("1.2.840.10008.5.1.1.9.1",
            "Referenced Grayscale Print Management Meta SOP Class (Retired)"),
    /**
     * 打印作业SOP类
     */
    PrintJob("1.2.840.10008.5.1.1.14", "Print Job SOP Class"),
    /**
     * 基本注释框SOP类
     */
    BasicAnnotationBox("1.2.840.10008.5.1.1.15", "Basic Annotation Box SOP Class"),
    /**
     * 打印机SOP类
     */
    Printer("1.2.840.10008.5.1.1.16", "Printer SOP Class"),
    /**
     * 打印机配置检索SOP类
     */
    PrinterConfigurationRetrieval("1.2.840.10008.5.1.1.16.376", "Printer Configuration Retrieval SOP Class"),
    /**
     * 打印机SOP实例
     */
    PrinterInstance("1.2.840.10008.5.1.1.17", "Printer SOP Instance"),
    /**
     * 打印机配置检索SOP实例
     */
    PrinterConfigurationRetrievalInstance("1.2.840.10008.5.1.1.17.376", "Printer Configuration Retrieval SOP Instance"),
    /**
     * 基本彩色打印管理元SOP类
     */
    BasicColorPrintManagementMeta("1.2.840.10008.5.1.1.18", "Basic Color Print Management Meta SOP Class"),
    /**
     * 引用彩色打印管理元SOP类（已弃用）
     */
    ReferencedColorPrintManagementMeta("1.2.840.10008.5.1.1.18.1",
            "Referenced Color Print Management Meta SOP Class (Retired)"),
    /**
     * VOI LUT框SOP类
     */
    VOILUTBox("1.2.840.10008.5.1.1.22", "VOI LUT Box SOP Class"),
    /**
     * 表现LUT SOP类
     */
    PresentationLUT("1.2.840.10008.5.1.1.23", "Presentation LUT SOP Class"),
    /**
     * 图像覆盖框SOP类（已弃用）
     */
    ImageOverlayBox("1.2.840.10008.5.1.1.24", "Image Overlay Box SOP Class (Retired)"),
    /**
     * 基本打印图像覆盖框SOP类（已弃用）
     */
    BasicPrintImageOverlayBox("1.2.840.10008.5.1.1.24.1", "Basic Print Image Overlay Box SOP Class (Retired)"),
    /**
     * 打印队列SOP实例（已弃用）
     */
    PrintQueueInstance("1.2.840.10008.5.1.1.25", "Print Queue SOP Instance (Retired)"),
    /**
     * 打印队列管理SOP类（已弃用）
     */
    PrintQueueManagement("1.2.840.10008.5.1.1.26", "Print Queue Management SOP Class (Retired)"),
    /**
     * 存储打印存储SOP类（已弃用）
     */
    StoredPrintStorage("1.2.840.10008.5.1.1.27", "Stored Print Storage SOP Class (Retired)"),
    /**
     * 硬拷贝灰度图像存储SOP类（已弃用）
     */
    HardcopyGrayscaleImageStorage("1.2.840.10008.5.1.1.29", "Hardcopy Grayscale Image Storage SOP Class (Retired)"),
    /**
     * 硬拷贝彩色图像存储SOP类（已弃用）
     */
    HardcopyColorImageStorage("1.2.840.10008.5.1.1.30", "Hardcopy Color Image Storage SOP Class (Retired)"),
    /**
     * 拉取打印请求SOP类（已弃用）
     */
    PullPrintRequest("1.2.840.10008.5.1.1.31", "Pull Print Request SOP Class (Retired)"),
    /**
     * 拉取存储打印管理元SOP类（已弃用）
     */
    PullStoredPrintManagementMeta("1.2.840.10008.5.1.1.32", "Pull Stored Print Management Meta SOP Class (Retired)"),
    /**
     * 媒体创建管理SOP类UID
     */
    MediaCreationManagement("1.2.840.10008.5.1.1.33", "Media Creation Management SOP Class UID"),
    /**
     * 显示系统SOP类
     */
    DisplaySystem("1.2.840.10008.5.1.1.40", "Display System SOP Class"),
    /**
     * 显示系统SOP实例
     */
    DisplaySystemInstance("1.2.840.10008.5.1.1.40.1", "Display System SOP Instance"),
    /**
     * 计算放射摄影图像存储
     */
    ComputedRadiographyImageStorage("1.2.840.10008.5.1.4.1.1.1", "Computed Radiography Image Storage"),
    /**
     * 数字X射线图像存储 - 用于演示
     */
    DigitalXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.1.1",
            "Digital X-Ray Image Storage - For Presentation"),
    /**
     * 数字X射线图像存储 - 用于处理
     */
    DigitalXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.1.1.1",
            "Digital X-Ray Image Storage - For Processing"),
    /**
     * 数字乳腺X射线图像存储 - 用于演示
     */
    DigitalMammographyXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.1.2",
            "Digital Mammography X-Ray Image Storage - For Presentation"),
    /**
     * 数字乳腺X射线图像存储 - 用于处理
     */
    DigitalMammographyXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.1.2.1",
            "Digital Mammography X-Ray Image Storage - For Processing"),
    /**
     * 数字口腔内X射线图像存储 - 用于演示
     */
    DigitalIntraOralXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.1.3",
            "Digital Intra-Oral X-Ray Image Storage - For Presentation"),
    /**
     * 数字口腔内X射线图像存储 - 用于处理
     */
    DigitalIntraOralXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.1.3.1",
            "Digital Intra-Oral X-Ray Image Storage - For Processing"),
    /**
     * CT图像存储
     */
    CTImageStorage("1.2.840.10008.5.1.4.1.1.2", "CT Image Storage"),
    /**
     * 增强CT图像存储
     */
    EnhancedCTImageStorage("1.2.840.10008.5.1.4.1.1.2.1", "Enhanced CT Image Storage"),
    /**
     * 遗留转换增强CT图像存储
     */
    LegacyConvertedEnhancedCTImageStorage("1.2.840.10008.5.1.4.1.1.2.2", "Legacy Converted Enhanced CT Image Storage"),
    /**
     * 超声多帧图像存储（已弃用）
     */
    UltrasoundMultiFrameImageStorageRetired("1.2.840.10008.5.1.4.1.1.3",
            "Ultrasound Multi-frame Image Storage (Retired)"),
    /**
     * 超声多帧图像存储
     */
    UltrasoundMultiFrameImageStorage("1.2.840.10008.5.1.4.1.1.3.1", "Ultrasound Multi-frame Image Storage"),
    /**
     * MR图像存储
     */
    MRImageStorage("1.2.840.10008.5.1.4.1.1.4", "MR Image Storage"),
    /**
     * 增强MR图像存储
     */
    EnhancedMRImageStorage("1.2.840.10008.5.1.4.1.1.4.1", "Enhanced MR Image Storage"),
    /**
     * MR光谱存储
     */
    MRSpectroscopyStorage("1.2.840.10008.5.1.4.1.1.4.2", "MR Spectroscopy Storage"),
    /**
     * 增强MR彩色图像存储
     */
    EnhancedMRColorImageStorage("1.2.840.10008.5.1.4.1.1.4.3", "Enhanced MR Color Image Storage"),
    /**
     * 遗留转换增强MR图像存储
     */
    LegacyConvertedEnhancedMRImageStorage("1.2.840.10008.5.1.4.1.1.4.4", "Legacy Converted Enhanced MR Image Storage"),
    /**
     * 核医学图像存储（已弃用）
     */
    NuclearMedicineImageStorageRetired("1.2.840.10008.5.1.4.1.1.5", "Nuclear Medicine Image Storage (Retired)"),
    /**
     * 超声图像存储（已弃用）
     */
    UltrasoundImageStorageRetired("1.2.840.10008.5.1.4.1.1.6", "Ultrasound Image Storage (Retired)"),
    /**
     * 超声图像存储
     */
    UltrasoundImageStorage("1.2.840.10008.5.1.4.1.1.6.1", "Ultrasound Image Storage"),
    /**
     * 增强US体积存储
     */
    EnhancedUSVolumeStorage("1.2.840.10008.5.1.4.1.1.6.2", "Enhanced US Volume Storage"),
    /**
     * 光声图像存储
     */
    PhotoacousticImageStorage("1.2.840.10008.5.1.4.1.1.6.3", "Photoacoustic Image Storage"),
    /**
     * 二次捕获图像存储
     */
    SecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7", "Secondary Capture Image Storage"),
    /**
     * 多帧单比特二次捕获图像存储
     */
    MultiFrameSingleBitSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.1",
            "Multi-frame Single Bit Secondary Capture Image Storage"),
    /**
     * 多帧灰度字节二次捕获图像存储
     */
    MultiFrameGrayscaleByteSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.2",
            "Multi-frame Grayscale Byte Secondary Capture Image Storage"),
    /**
     * 多帧灰度字二次捕获图像存储
     */
    MultiFrameGrayscaleWordSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.3",
            "Multi-frame Grayscale Word Secondary Capture Image Storage"),
    /**
     * 多帧真彩二次捕获图像存储
     */
    MultiFrameTrueColorSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.4",
            "Multi-frame True Color Secondary Capture Image Storage"),
    /**
     * 独立覆盖存储（已弃用）
     */
    StandaloneOverlayStorage("1.2.840.10008.5.1.4.1.1.8", "Standalone Overlay Storage (Retired)"),
    /**
     * 独立曲线存储（已弃用）
     */
    StandaloneCurveStorage("1.2.840.10008.5.1.4.1.1.9", "Standalone Curve Storage (Retired)"),
    /**
     * 波形存储 - 试验（已弃用）
     */
    WaveformStorageTrial("1.2.840.10008.5.1.4.1.1.9.1", "Waveform Storage - Trial (Retired)"),
    /**
     * 12导联ECG波形存储
     */
    TwelveLeadECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.1", "12-lead ECG Waveform Storage"),
    /**
     * 通用ECG波形存储
     */
    GeneralECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.2", "General ECG Waveform Storage"),
    /**
     * 动态ECG波形存储
     */
    AmbulatoryECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.3", "Ambulatory ECG Waveform Storage"),
    /**
     * 通用32位ECG波形存储
     */
    General32bitECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.4", "General 32-bit ECG Waveform Storage"),
    /**
     * 血液动力学波形存储
     */
    HemodynamicWaveformStorage("1.2.840.10008.5.1.4.1.1.9.2.1", "Hemodynamic Waveform Storage"),
    /**
     * 心脏电生理波形存储
     */
    CardiacElectrophysiologyWaveformStorage("1.2.840.10008.5.1.4.1.1.9.3.1",
            "Cardiac Electrophysiology Waveform Storage"),
    /**
     * 基本语音音频波形存储
     */
    BasicVoiceAudioWaveformStorage("1.2.840.10008.5.1.4.1.1.9.4.1", "Basic Voice Audio Waveform Storage"),
    /**
     * 通用音频波形存储
     */
    GeneralAudioWaveformStorage("1.2.840.10008.5.1.4.1.1.9.4.2", "General Audio Waveform Storage"),
    /**
     * 动脉脉搏波形存储
     */
    ArterialPulseWaveformStorage("1.2.840.10008.5.1.4.1.1.9.5.1", "Arterial Pulse Waveform Storage"),
    /**
     * 呼吸波形存储
     */
    RespiratoryWaveformStorage("1.2.840.10008.5.1.4.1.1.9.6.1", "Respiratory Waveform Storage"),
    /**
     * 多通道呼吸波形存储
     */
    MultichannelRespiratoryWaveformStorage("1.2.840.10008.5.1.4.1.1.9.6.2",
            "Multi-channel Respiratory Waveform Storage"),
    /**
     * 常规头皮脑电图波形存储
     */
    RoutineScalpElectroencephalogramWaveformStorage("1.2.840.10008.5.1.4.1.1.9.7.1",
            "Routine Scalp Electroencephalogram Waveform Storage"),
    /**
     * 肌电图波形存储
     */
    ElectromyogramWaveformStorage("1.2.840.10008.5.1.4.1.1.9.7.2", "Electromyogram Waveform Storage"),
    /**
     * 眼动图波形存储
     */
    ElectrooculogramWaveformStorage("1.2.840.10008.5.1.4.1.1.9.7.3", "Electrooculogram Waveform Storage"),
    /**
     * 睡眠脑电图波形存储
     */
    SleepElectroencephalogramWaveformStorage("1.2.840.10008.5.1.4.1.1.9.7.4",
            "Sleep Electroencephalogram Waveform Storage"),
    /**
     * 身体位置波形存储
     */
    BodyPositionWaveformStorage("1.2.840.10008.5.1.4.1.1.9.8.1", "Body Position Waveform Storage"),
    /**
     * 独立模态LUT存储（已弃用）
     */
    StandaloneModalityLUTStorage("1.2.840.10008.5.1.4.1.1.10", "Standalone Modality LUT Storage (Retired)"),
    /**
     * 独立VOI LUT存储（已弃用）
     */
    StandaloneVOILUTStorage("1.2.840.10008.5.1.4.1.1.11", "Standalone VOI LUT Storage (Retired)"),
    /**
     * 灰度软拷贝表现状态存储
     */
    GrayscaleSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.1",
            "Grayscale Softcopy Presentation State Storage"),
    /**
     * 彩色软拷贝表现状态存储
     */
    ColorSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.2", "Color Softcopy Presentation State Storage"),
    /**
     * 伪彩色软拷贝表现状态存储
     */
    PseudoColorSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.3",
            "Pseudo-Color Softcopy Presentation State Storage"),
    /**
     * 混合软拷贝表现状态存储
     */
    BlendingSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.4",
            "Blending Softcopy Presentation State Storage"),
    /**
     * XA/XRF灰度软拷贝表现状态存储
     */
    XAXRFGrayscaleSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.5",
            "XA/XRF Grayscale Softcopy Presentation State Storage"),
    /**
     * 灰度平面MPR体积表现状态存储
     */
    GrayscalePlanarMPRVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.6",
            "Grayscale Planar MPR Volumetric Presentation State Storage"),
    /**
     * 合成平面MPR体积表现状态存储
     */
    CompositingPlanarMPRVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.7",
            "Compositing Planar MPR Volumetric Presentation State Storage"),
    /**
     * 高级混合表现状态存储
     */
    AdvancedBlendingPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.8",
            "Advanced Blending Presentation State Storage"),
    /**
     * 体积渲染体积表现状态存储
     */
    VolumeRenderingVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.9",
            "Volume Rendering Volumetric Presentation State Storage"),
    /**
     * 分段体积渲染体积表现状态存储
     */
    SegmentedVolumeRenderingVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.10",
            "Segmented Volume Rendering Volumetric Presentation State Storage"),
    /**
     * 多体积渲染体积表现状态存储
     */
    MultipleVolumeRenderingVolumetricPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.11",
            "Multiple Volume Rendering Volumetric Presentation State Storage"),
    /**
     * 可变模态LUT软拷贝表现状态存储
     */
    VariableModalityLUTSoftcopyPresentationStateStorage("1.2.840.10008.5.1.4.1.1.11.12",
            "Variable Modality LUT Softcopy Presentation State Storage"),
    /**
     * X射线血管造影图像存储
     */
    XRayAngiographicImageStorage("1.2.840.10008.5.1.4.1.1.12.1", "X-Ray Angiographic Image Storage"),
    /**
     * 增强XA图像存储
     */
    EnhancedXAImageStorage("1.2.840.10008.5.1.4.1.1.12.1.1", "Enhanced XA Image Storage"),
    /**
     * X射线透视图像存储
     */
    XRayRadiofluoroscopicImageStorage("1.2.840.10008.5.1.4.1.1.12.2", "X-Ray Radiofluoroscopic Image Storage"),
    /**
     * 增强XRF图像存储
     */
    EnhancedXRFImageStorage("1.2.840.10008.5.1.4.1.1.12.2.1", "Enhanced XRF Image Storage"),
    /**
     * X射线血管造影双平面图像存储（已弃用）
     */
    XRayAngiographicBiPlaneImageStorage("1.2.840.10008.5.1.4.1.1.12.3",
            "X-Ray Angiographic Bi-Plane Image Storage (Retired)"),
    /**
     * Zeiss OPT文件（已弃用）
     */
    ZeissOPTFile("1.2.840.10008.5.1.4.1.1.12.77", "Zeiss OPT File (Retired)"),
    /**
     * X射线3D血管造影图像存储
     */
    XRay3DAngiographicImageStorage("1.2.840.10008.5.1.4.1.1.13.1.1", "X-Ray 3D Angiographic Image Storage"),
    /**
     * X射线3D颅面图像存储
     */
    XRay3DCraniofacialImageStorage("1.2.840.10008.5.1.4.1.1.13.1.2", "X-Ray 3D Craniofacial Image Storage"),
    /**
     * 乳腺断层合成图像存储
     */
    BreastTomosynthesisImageStorage("1.2.840.10008.5.1.4.1.1.13.1.3", "Breast Tomosynthesis Image Storage"),
    /**
     * 乳腺投影X射线图像存储 - 用于演示
     */
    BreastProjectionXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.13.1.4",
            "Breast Projection X-Ray Image Storage - For Presentation"),
    /**
     * 乳腺投影X射线图像存储 - 用于处理
     */
    BreastProjectionXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.13.1.5",
            "Breast Projection X-Ray Image Storage - For Processing"),
    /**
     * 血管内光学相干断层扫描图像存储 - 用于演示
     */
    IntravascularOpticalCoherenceTomographyImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.14.1",
            "Intravascular Optical Coherence Tomography Image Storage - For Presentation"),
    /**
     * 血管内光学相干断层扫描图像存储 - 用于处理
     */
    IntravascularOpticalCoherenceTomographyImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.14.2",
            "Intravascular Optical Coherence Tomography Image Storage - For Processing"),
    /**
     * 核医学图像存储
     */
    NuclearMedicineImageStorage("1.2.840.10008.5.1.4.1.1.20", "Nuclear Medicine Image Storage"),
    /**
     * 参数图存储
     */
    ParametricMapStorage("1.2.840.10008.5.1.4.1.1.30", "Parametric Map Storage"),
    /**
     * MR图像存储零填充（已弃用）
     */
    MRImageStorageZeroPadded("1.2.840.10008.5.1.4.1.1.40", "MR Image Storage Zero Padded (Retired)"),
    /**
     * 原始数据存储
     */
    RawDataStorage("1.2.840.10008.5.1.4.1.1.66", "Raw Data Storage"),
    /**
     * 空间配准存储
     */
    SpatialRegistrationStorage("1.2.840.10008.5.1.4.1.1.66.1", "Spatial Registration Storage"),
    /**
     * 空间基准点存储
     */
    SpatialFiducialsStorage("1.2.840.10008.5.1.4.1.1.66.2", "Spatial Fiducials Storage"),
    /**
     * 可变形空间配准存储
     */
    DeformableSpatialRegistrationStorage("1.2.840.10008.5.1.4.1.1.66.3", "Deformable Spatial Registration Storage"),
    /**
     * 分割存储
     */
    SegmentationStorage("1.2.840.10008.5.1.4.1.1.66.4", "Segmentation Storage"),
    /**
     * 表面分割存储
     */
    SurfaceSegmentationStorage("1.2.840.10008.5.1.4.1.1.66.5", "Surface Segmentation Storage"),
    /**
     * 纤维束追踪结果存储
     */
    TractographyResultsStorage("1.2.840.10008.5.1.4.1.1.66.6", "Tractography Results Storage"),
    /**
     * 真实世界值映射存储
     */
    RealWorldValueMappingStorage("1.2.840.10008.5.1.4.1.1.67", "Real World Value Mapping Storage"),
    /**
     * 表面扫描网格存储
     */
    SurfaceScanMeshStorage("1.2.840.10008.5.1.4.1.1.68.1", "Surface Scan Mesh Storage"),
    /**
     * 表面扫描点云存储
     */
    SurfaceScanPointCloudStorage("1.2.840.10008.5.1.4.1.1.68.2", "Surface Scan Point Cloud Storage"),
    /**
     * VL图像存储 - 试验（已弃用）
     */
    VLImageStorageTrial("1.2.840.10008.5.1.4.1.1.77.1", "VL Image Storage - Trial (Retired)"),
    /**
     * VL多帧图像存储 - 试验（已弃用）
     */
    VLMultiFrameImageStorageTrial("1.2.840.10008.5.1.4.1.1.77.2", "VL Multi-frame Image Storage - Trial (Retired)"),
    /**
     * VL内窥镜图像存储
     */
    VLEndoscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.1", "VL Endoscopic Image Storage"),
    /**
     * 视频内窥镜图像存储
     */
    VideoEndoscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.1.1", "Video Endoscopic Image Storage"),
    /**
     * VL显微镜图像存储
     */
    VLMicroscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.2", "VL Microscopic Image Storage"),
    /**
     * 视频显微镜图像存储
     */
    VideoMicroscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.2.1", "Video Microscopic Image Storage"),
    /**
     * VL幻灯片坐标显微镜图像存储
     */
    VLSlideCoordinatesMicroscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.3",
            "VL Slide-Coordinates Microscopic Image Storage"),
    /**
     * VL摄影图像存储
     */
    VLPhotographicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.4", "VL Photographic Image Storage"),
    /**
     * 视频摄影图像存储
     */
    VideoPhotographicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.4.1", "Video Photographic Image Storage"),
    /**
     * 眼科摄影8位图像存储
     */
    OphthalmicPhotography8BitImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.1",
            "Ophthalmic Photography 8 Bit Image Storage"),
    /**
     * 眼科摄影16位图像存储
     */
    OphthalmicPhotography16BitImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.2",
            "Ophthalmic Photography 16 Bit Image Storage"),
    /**
     * 立体测量关系存储
     */
    StereometricRelationshipStorage("1.2.840.10008.5.1.4.1.1.77.1.5.3", "Stereometric Relationship Storage"),
    /**
     * 眼科断层扫描图像存储
     */
    OphthalmicTomographyImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.4", "Ophthalmic Tomography Image Storage"),
    /**
     * 广场眼科摄影立体投影图像存储
     */
    WideFieldOphthalmicPhotographyStereographicProjectionImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.5",
            "Wide Field Ophthalmic Photography Stereographic Projection Image Storage"),
    /**
     * 广场眼科摄影3D坐标图像存储
     */
    WideFieldOphthalmicPhotography3DCoordinatesImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.6",
            "Wide Field Ophthalmic Photography 3D Coordinates Image Storage"),
    /**
     * 眼科光学相干断层扫描正面图像存储
     */
    OphthalmicOpticalCoherenceTomographyEnFaceImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.7",
            "Ophthalmic Optical Coherence Tomography En Face Image Storage"),
    /**
     * 眼科光学相干断层扫描B扫描体积分析存储
     */
    OphthalmicOpticalCoherenceTomographyBscanVolumeAnalysisStorage("1.2.840.10008.5.1.4.1.1.77.1.5.8",
            "Ophthalmic Optical Coherence Tomography B-scan Volume Analysis Storage"),
    /**
     * VL全幻灯片显微镜图像存储
     */
    VLWholeSlideMicroscopyImageStorage("1.2.840.10008.5.1.4.1.1.77.1.6", "VL Whole Slide Microscopy Image Storage"),
    /**
     * 皮肤镜摄影图像存储
     */
    DermoscopicPhotographyImageStorage("1.2.840.10008.5.1.4.1.1.77.1.7", "Dermoscopic Photography Image Storage"),
    /**
     * 共聚焦显微镜图像存储
     */
    ConfocalMicroscopyImageStorage("1.2.840.10008.5.1.4.1.1.77.1.8", "Confocal Microscopy Image Storage"),
    /**
     * 共聚焦显微镜瓦片金字塔图像存储
     */
    ConfocalMicroscopyTiledPyramidalImageStorage("1.2.840.10008.5.1.4.1.1.77.1.9",
            "Confocal Microscopy Tiled Pyramidal Image Storage"),
    /**
     * 验光测量存储
     */
    LensometryMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.1", "Lensometry Measurements Storage"),
    /**
     * 自动验光测量存储
     */
    AutorefractionMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.2", "Autorefraction Measurements Storage"),
    /**
     * 角膜曲率测量存储
     */
    KeratometryMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.3", "Keratometry Measurements Storage"),
    /**
     * 主观验光测量存储
     */
    SubjectiveRefractionMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.4",
            "Subjective Refraction Measurements Storage"),
    /**
     * 视力测量存储
     */
    VisualAcuityMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.5", "Visual Acuity Measurements Storage"),
    /**
     * 眼镜处方报告存储
     */
    SpectaclePrescriptionReportStorage("1.2.840.10008.5.1.4.1.1.78.6", "Spectacle Prescription Report Storage"),
    /**
     * 眼科轴向测量存储
     */
    OphthalmicAxialMeasurementsStorage("1.2.840.10008.5.1.4.1.1.78.7", "Ophthalmic Axial Measurements Storage"),
    /**
     * 人工晶体计算存储
     */
    IntraocularLensCalculationsStorage("1.2.840.10008.5.1.4.1.1.78.8", "Intraocular Lens Calculations Storage"),
    /**
     * 黄斑网格厚度和体积报告存储
     */
    MacularGridThicknessAndVolumeReportStorage("1.2.840.10008.5.1.4.1.1.79.1",
            "Macular Grid Thickness and Volume Report Storage"),
    /**
     * 眼科视野静态视野测量存储
     */
    OphthalmicVisualFieldStaticPerimetryMeasurementsStorage("1.2.840.10008.5.1.4.1.1.80.1",
            "Ophthalmic Visual Field Static Perimetry Measurements Storage"),
    /**
     * 眼科厚度图存储
     */
    OphthalmicThicknessMapStorage("1.2.840.10008.5.1.4.1.1.81.1", "Ophthalmic Thickness Map Storage"),
    /**
     * 角膜地形图存储
     */
    CornealTopographyMapStorage("1.2.840.10008.5.1.4.1.1.82.1", "Corneal Topography Map Storage"),
    /**
     * 文本SR存储 - 试验（已弃用）
     */
    TextSRStorageTrial("1.2.840.10008.5.1.4.1.1.88.1", "Text SR Storage - Trial (Retired)"),
    /**
     * 音频SR存储 - 试验（已弃用）
     */
    AudioSRStorageTrial("1.2.840.10008.5.1.4.1.1.88.2", "Audio SR Storage - Trial (Retired)"),
    /**
     * 详细SR存储 - 试验（已弃用）
     */
    DetailSRStorageTrial("1.2.840.10008.5.1.4.1.1.88.3", "Detail SR Storage - Trial (Retired)"),
    /**
     * 综合SR存储 - 试验（已弃用）
     */
    ComprehensiveSRStorageTrial("1.2.840.10008.5.1.4.1.1.88.4", "Comprehensive SR Storage - Trial (Retired)"),
    /**
     * 基本文本SR存储
     */
    BasicTextSRStorage("1.2.840.10008.5.1.4.1.1.88.11", "Basic Text SR Storage"),
    /**
     * 增强SR存储
     */
    EnhancedSRStorage("1.2.840.10008.5.1.4.1.1.88.22", "Enhanced SR Storage"),
    /**
     * 综合SR存储
     */
    ComprehensiveSRStorage("1.2.840.10008.5.1.4.1.1.88.33", "Comprehensive SR Storage"),
    /**
     * 综合3D SR存储
     */
    Comprehensive3DSRStorage("1.2.840.10008.5.1.4.1.1.88.34", "Comprehensive 3D SR Storage"),
    /**
     * 可扩展SR存储
     */
    ExtensibleSRStorage("1.2.840.10008.5.1.4.1.1.88.35", "Extensible SR Storage"),
    /**
     * 程序日志存储
     */
    ProcedureLogStorage("1.2.840.10008.5.1.4.1.1.88.40", "Procedure Log Storage"),
    /**
     * 乳腺X光摄影CAD SR存储
     */
    MammographyCADSRStorage("1.2.840.10008.5.1.4.1.1.88.50", "Mammography CAD SR Storage"),
    /**
     * 关键对象选择文档存储
     */
    KeyObjectSelectionDocumentStorage("1.2.840.10008.5.1.4.1.1.88.59", "Key Object Selection Document Storage"),
    /**
     * 胸部CAD SR存储
     */
    ChestCADSRStorage("1.2.840.10008.5.1.4.1.1.88.65", "Chest CAD SR Storage"),
    /**
     * X射线辐射剂量SR存储
     */
    XRayRadiationDoseSRStorage("1.2.840.10008.5.1.4.1.1.88.67", "X-Ray Radiation Dose SR Storage"),
    /**
     * 放射性药物辐射剂量SR存储
     */
    RadiopharmaceuticalRadiationDoseSRStorage("1.2.840.10008.5.1.4.1.1.88.68",
            "Radiopharmaceutical Radiation Dose SR Storage"),
    /**
     * 结肠CAD SR存储
     */
    ColonCADSRStorage("1.2.840.10008.5.1.4.1.1.88.69", "Colon CAD SR Storage"),
    /**
     * 植入计划SR存储
     */
    ImplantationPlanSRStorage("1.2.840.10008.5.1.4.1.1.88.70", "Implantation Plan SR Storage"),
    /**
     * 获取上下文SR存储
     */
    AcquisitionContextSRStorage("1.2.840.10008.5.1.4.1.1.88.71", "Acquisition Context SR Storage"),
    /**
     * 简化成人回波SR存储
     */
    SimplifiedAdultEchoSRStorage("1.2.840.10008.5.1.4.1.1.88.72", "Simplified Adult Echo SR Storage"),
    /**
     * 患者辐射剂量SR存储
     */
    PatientRadiationDoseSRStorage("1.2.840.10008.5.1.4.1.1.88.73", "Patient Radiation Dose SR Storage"),
    /**
     * 计划成像剂管理SR存储
     */
    PlannedImagingAgentAdministrationSRStorage("1.2.840.10008.5.1.4.1.1.88.74",
            "Planned Imaging Agent Administration SR Storage"),
    /**
     * 执行成像剂管理SR存储
     */
    PerformedImagingAgentAdministrationSRStorage("1.2.840.10008.5.1.4.1.1.88.75",
            "Performed Imaging Agent Administration SR Storage"),
    /**
     * 增强X射线辐射剂量SR存储
     */
    EnhancedXRayRadiationDoseSRStorage("1.2.840.10008.5.1.4.1.1.88.76", "Enhanced X-Ray Radiation Dose SR Storage"),
    /**
     * 内容评估结果存储
     */
    ContentAssessmentResultsStorage("1.2.840.10008.5.1.4.1.1.90.1", "Content Assessment Results Storage"),
    /**
     * 显微镜批量简单注释存储
     */
    MicroscopyBulkSimpleAnnotationsStorage("1.2.840.10008.5.1.4.1.1.91.1",
            "Microscopy Bulk Simple Annotations Storage"),
    /**
     * 封装PDF存储
     */
    EncapsulatedPDFStorage("1.2.840.10008.5.1.4.1.1.104.1", "Encapsulated PDF Storage"),
    /**
     * 封装CDA存储
     */
    EncapsulatedCDAStorage("1.2.840.10008.5.1.4.1.1.104.2", "Encapsulated CDA Storage"),
    /**
     * 封装STL存储
     */
    EncapsulatedSTLStorage("1.2.840.10008.5.1.4.1.1.104.3", "Encapsulated STL Storage"),
    /**
     * 封装OBJ存储
     */
    EncapsulatedOBJStorage("1.2.840.10008.5.1.4.1.1.104.4", "Encapsulated OBJ Storage"),
    /**
     * 封装MTL存储
     */
    EncapsulatedMTLStorage("1.2.840.10008.5.1.4.1.1.104.5", "Encapsulated MTL Storage"),
    /**
     * 正电子发射断层扫描图像存储
     */
    PositronEmissionTomographyImageStorage("1.2.840.10008.5.1.4.1.1.128", "Positron Emission Tomography Image Storage"),
    /**
     * 遗留转换增强PET图像存储
     */
    LegacyConvertedEnhancedPETImageStorage("1.2.840.10008.5.1.4.1.1.128.1",
            "Legacy Converted Enhanced PET Image Storage"),
    /**
     * 独立PET曲线存储（已弃用）
     */
    StandalonePETCurveStorage("1.2.840.10008.5.1.4.1.1.129", "Standalone PET Curve Storage (Retired)"),
    /**
     * 增强PET图像存储
     */
    EnhancedPETImageStorage("1.2.840.10008.5.1.4.1.1.130", "Enhanced PET Image Storage"),
    /**
     * 基本结构化显示存储
     */
    BasicStructuredDisplayStorage("1.2.840.10008.5.1.4.1.1.131", "Basic Structured Display Storage"),
    /**
     * CT定义程序协议存储
     */
    CTDefinedProcedureProtocolStorage("1.2.840.10008.5.1.4.1.1.200.1", "CT Defined Procedure Protocol Storage"),
    /**
     * CT执行程序协议存储
     */
    CTPerformedProcedureProtocolStorage("1.2.840.10008.5.1.4.1.1.200.2", "CT Performed Procedure Protocol Storage"),
    /**
     * 协议批准存储
     */
    ProtocolApprovalStorage("1.2.840.10008.5.1.4.1.1.200.3", "Protocol Approval Storage"),
    /**
     * 协议批准信息模型 - FIND
     */
    ProtocolApprovalInformationModelFind("1.2.840.10008.5.1.4.1.1.200.4", "Protocol Approval Information Model - FIND"),
    /**
     * 协议批准信息模型 - MOVE
     */
    ProtocolApprovalInformationModelMove("1.2.840.10008.5.1.4.1.1.200.5", "Protocol Approval Information Model - MOVE"),
    /**
     * 协议批准信息模型 - GET
     */
    ProtocolApprovalInformationModelGet("1.2.840.10008.5.1.4.1.1.200.6", "Protocol Approval Information Model - GET"),
    /**
     * XA定义程序协议存储
     */
    XADefinedProcedureProtocolStorage("1.2.840.10008.5.1.4.1.1.200.7", "XA Defined Procedure Protocol Storage"),
    /**
     * XA执行程序协议存储
     */
    XAPerformedProcedureProtocolStorage("1.2.840.10008.5.1.4.1.1.200.8", "XA Performed Procedure Protocol Storage"),
    /**
     * 库存存储
     */
    InventoryStorage("1.2.840.10008.5.1.4.1.1.201.1", "Inventory Storage"),
    /**
     * 库存 - FIND
     */
    InventoryFind("1.2.840.10008.5.1.4.1.1.201.2", "Inventory - FIND"),
    /**
     * 库存 - MOVE
     */
    InventoryMove("1.2.840.10008.5.1.4.1.1.201.3", "Inventory - MOVE"),
    /**
     * 库存 - GET
     */
    InventoryGet("1.2.840.10008.5.1.4.1.1.201.4", "Inventory - GET"),
    /**
     * 库存创建
     */
    InventoryCreation("1.2.840.10008.5.1.4.1.1.201.5", "Inventory Creation"),
    /**
     * 存储库查询
     */
    RepositoryQuery("1.2.840.10008.5.1.4.1.1.201.6", "Repository Query"),
    /**
     * 存储管理SOP实例
     */
    StorageManagementInstance("1.2.840.10008.5.1.4.1.1.201.1.1", "Storage Management SOP Instance"),
    /**
     * RT图像存储
     */
    RTImageStorage("1.2.840.10008.5.1.4.1.1.481.1", "RT Image Storage"),
    /**
     * RT剂量存储
     */
    RTDoseStorage("1.2.840.10008.5.1.4.1.1.481.2", "RT Dose Storage"),
    /**
     * RT结构集存储
     */
    RTStructureSetStorage("1.2.840.10008.5.1.4.1.1.481.3", "RT Structure Set Storage"),
    /**
     * RT束治疗记录存储
     */
    RTBeamsTreatmentRecordStorage("1.2.840.10008.5.1.4.1.1.481.4", "RT Beams Treatment Record Storage"),
    /**
     * RT计划存储
     */
    RTPlanStorage("1.2.840.10008.5.1.4.1.1.481.5", "RT Plan Storage"),
    /**
     * RT近距离治疗记录存储
     */
    RTBrachyTreatmentRecordStorage("1.2.840.10008.5.1.4.1.1.481.6", "RT Brachy Treatment Record Storage"),
    /**
     * RT治疗摘要记录存储
     */
    RTTreatmentSummaryRecordStorage("1.2.840.10008.5.1.4.1.1.481.7", "RT Treatment Summary Record Storage"),
    /**
     * RT离子计划存储
     */
    RTIonPlanStorage("1.2.840.10008.5.1.4.1.1.481.8", "RT Ion Plan Storage"),
    /**
     * RT离子束治疗记录存储
     */
    RTIonBeamsTreatmentRecordStorage("1.2.840.10008.5.1.4.1.1.481.9", "RT Ion Beams Treatment Record Storage"),
    /**
     * RT医师意图存储
     */
    RTPhysicianIntentStorage("1.2.840.10008.5.1.4.1.1.481.10", "RT Physician Intent Storage"),
    /**
     * RT分段注释存储
     */
    RTSegmentAnnotationStorage("1.2.840.10008.5.1.4.1.1.481.11", "RT Segment Annotation Storage"),
    /**
     * RT辐射集存储
     */
    RTRadiationSetStorage("1.2.840.10008.5.1.4.1.1.481.12", "RT Radiation Set Storage"),
    /**
     * C臂光子-电子辐射存储
     */
    CArmPhotonElectronRadiationStorage("1.2.840.10008.5.1.4.1.1.481.13", "C-Arm Photon-Electron Radiation Storage"),
    /**
     * 断层治疗辐射存储
     */
    TomotherapeuticRadiationStorage("1.2.840.10008.5.1.4.1.1.481.14", "Tomotherapeutic Radiation Storage"),
    /**
     * 机械臂辐射存储
     */
    RoboticArmRadiationStorage("1.2.840.10008.5.1.4.1.1.481.15", "Robotic-Arm Radiation Storage"),
    /**
     * RT辐射记录集存储
     */
    RTRadiationRecordSetStorage("1.2.840.10008.5.1.4.1.1.481.16", "RT Radiation Record Set Storage"),
    /**
     * RT辐射抢救记录存储
     */
    RTRadiationSalvageRecordStorage("1.2.840.10008.5.1.4.1.1.481.17", "RT Radiation Salvage Record Storage"),
    /**
     * 断层治疗辐射记录存储
     */
    TomotherapeuticRadiationRecordStorage("1.2.840.10008.5.1.4.1.1.481.18", "Tomotherapeutic Radiation Record Storage"),
    /**
     * C臂光子-电子辐射记录存储
     */
    CArmPhotonElectronRadiationRecordStorage("1.2.840.10008.5.1.4.1.1.481.19",
            "C-Arm Photon-Electron Radiation Record Storage"),
    /**
     * 机械臂辐射记录存储
     */
    RoboticRadiationRecordStorage("1.2.840.10008.5.1.4.1.1.481.20", "Robotic Radiation Record Storage"),
    /**
     * RT辐射集传送指令存储
     */
    RTRadiationSetDeliveryInstructionStorage("1.2.840.10008.5.1.4.1.1.481.21",
            "RT Radiation Set Delivery Instruction Storage"),
    /**
     * RT治疗准备存储
     */
    RTTreatmentPreparationStorage("1.2.840.10008.5.1.4.1.1.481.22", "RT Treatment Preparation Storage"),
    /**
     * 增强RT图像存储
     */
    EnhancedRTImageStorage("1.2.840.10008.5.1.4.1.1.481.23", "Enhanced RT Image Storage"),
    /**
     * 增强连续RT图像存储
     */
    EnhancedContinuousRTImageStorage("1.2.840.10008.5.1.4.1.1.481.24", "Enhanced Continuous RT Image Storage"),
    /**
     * RT患者位置获取指令存储
     */
    RTPatientPositionAcquisitionInstructionStorage("1.2.840.10008.5.1.4.1.1.481.25",
            "RT Patient Position Acquisition Instruction Storage"),
    /**
     * DICOS CT图像存储
     */
    DICOSCTImageStorage("1.2.840.10008.5.1.4.1.1.501.1", "DICOS CT Image Storage"),
    /**
     * DICOS数字X射线图像存储 - 用于演示
     */
    DICOSDigitalXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.501.2.1",
            "DICOS Digital X-Ray Image Storage - For Presentation"),
    /**
     * DICOS数字X射线图像存储 - 用于处理
     */
    DICOSDigitalXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.501.2.2",
            "DICOS Digital X-Ray Image Storage - For Processing"),
    /**
     * DICOS威胁检测报告存储
     */
    DICOSThreatDetectionReportStorage("1.2.840.10008.5.1.4.1.1.501.3", "DICOS Threat Detection Report Storage"),
    /**
     * DICOS 2D AIT存储
     */
    DICOS2DAITStorage("1.2.840.10008.5.1.4.1.1.501.4", "DICOS 2D AIT Storage"),
    /**
     * DICOS 3D AIT存储
     */
    DICOS3DAITStorage("1.2.840.10008.5.1.4.1.1.501.5", "DICOS 3D AIT Storage"),
    /**
     * DICOS四极共振(QR)存储
     */
    DICOSQuadrupoleResonanceStorage("1.2.840.10008.5.1.4.1.1.501.6", "DICOS Quadrupole Resonance (QR) Storage"),
    /**
     * 涡流图像存储
     */
    EddyCurrentImageStorage("1.2.840.10008.5.1.4.1.1.601.1", "Eddy Current Image Storage"),
    /**
     * 涡流多帧图像存储
     */
    EddyCurrentMultiFrameImageStorage("1.2.840.10008.5.1.4.1.1.601.2", "Eddy Current Multi-frame Image Storage"),
    /**
     * 患者根查询/检索信息模型 - FIND
     */
    PatientRootQueryRetrieveInformationModelFind("1.2.840.10008.5.1.4.1.2.1.1",
            "Patient Root Query/Retrieve Information Model - FIND"),
    /**
     * 患者根查询/检索信息模型 - MOVE
     */
    PatientRootQueryRetrieveInformationModelMove("1.2.840.10008.5.1.4.1.2.1.2",
            "Patient Root Query/Retrieve Information Model - MOVE"),
    /**
     * 患者根查询/检索信息模型 - GET
     */
    PatientRootQueryRetrieveInformationModelGet("1.2.840.10008.5.1.4.1.2.1.3",
            "Patient Root Query/Retrieve Information Model - GET"),
    /**
     * 研究根查询/检索信息模型 - FIND
     */
    StudyRootQueryRetrieveInformationModelFind("1.2.840.10008.5.1.4.1.2.2.1",
            "Study Root Query/Retrieve Information Model - FIND"),
    /**
     * 研究根查询/检索信息模型 - MOVE
     */
    StudyRootQueryRetrieveInformationModelMove("1.2.840.10008.5.1.4.1.2.2.2",
            "Study Root Query/Retrieve Information Model - MOVE"),
    /**
     * 研究根查询/检索信息模型 - GET
     */
    StudyRootQueryRetrieveInformationModelGet("1.2.840.10008.5.1.4.1.2.2.3",
            "Study Root Query/Retrieve Information Model - GET"),
    /**
     * 患者/研究仅查询/检索信息模型 - FIND（已弃用）
     */
    PatientStudyOnlyQueryRetrieveInformationModelFind("1.2.840.10008.5.1.4.1.2.3.1",
            "Patient/Study Only Query/Retrieve Information Model - FIND (Retired)"),
    /**
     * 患者/研究仅查询/检索信息模型 - MOVE（已弃用）
     */
    PatientStudyOnlyQueryRetrieveInformationModelMove("1.2.840.10008.5.1.4.1.2.3.2",
            "Patient/Study Only Query/Retrieve Information Model - MOVE (Retired)"),
    /**
     * 患者/研究仅查询/检索信息模型 - GET（已弃用）
     */
    PatientStudyOnlyQueryRetrieveInformationModelGet("1.2.840.10008.5.1.4.1.2.3.3",
            "Patient/Study Only Query/Retrieve Information Model - GET (Retired)"),
    /**
     * 复合实例根检索 - MOVE
     */
    CompositeInstanceRootRetrieveMove("1.2.840.10008.5.1.4.1.2.4.2", "Composite Instance Root Retrieve - MOVE"),
    /**
     * 复合实例根检索 - GET
     */
    CompositeInstanceRootRetrieveGet("1.2.840.10008.5.1.4.1.2.4.3", "Composite Instance Root Retrieve - GET"),
    /**
     * 无批量数据的复合实例检索 - GET
     */
    CompositeInstanceRetrieveWithoutBulkDataGet("1.2.840.10008.5.1.4.1.2.5.3",
            "Composite Instance Retrieve Without Bulk Data - GET"),
    /**
     * 定义程序协议信息模型 - FIND
     */
    DefinedProcedureProtocolInformationModelFind("1.2.840.10008.5.1.4.20.1",
            "Defined Procedure Protocol Information Model - FIND"),
    /**
     * 定义程序协议信息模型 - MOVE
     */
    DefinedProcedureProtocolInformationModelMove("1.2.840.10008.5.1.4.20.2",
            "Defined Procedure Protocol Information Model - MOVE"),
    /**
     * 定义程序协议信息模型 - GET
     */
    DefinedProcedureProtocolInformationModelGet("1.2.840.10008.5.1.4.20.3",
            "Defined Procedure Protocol Information Model - GET"),
    /**
     * 模态工作列表信息模型 - FIND
     */
    ModalityWorklistInformationModelFind("1.2.840.10008.5.1.4.31", "Modality Worklist Information Model - FIND"),
    /**
     * 通用工作列表管理元SOP类（已弃用）
     */
    GeneralPurposeWorklistManagementMeta("1.2.840.10008.5.1.4.32",
            "General Purpose Worklist Management Meta SOP Class (Retired)"),
    /**
     * 通用工作列表信息模型 - FIND（已弃用）
     */
    GeneralPurposeWorklistInformationModelFind("1.2.840.10008.5.1.4.32.1",
            "General Purpose Worklist Information Model - FIND (Retired)"),
    /**
     * 通用计划程序步骤SOP类（已弃用）
     */
    GeneralPurposeScheduledProcedureStep("1.2.840.10008.5.1.4.32.2",
            "General Purpose Scheduled Procedure Step SOP Class (Retired)"),
    /**
     * 通用执行程序步骤SOP类（已弃用）
     */
    GeneralPurposePerformedProcedureStep("1.2.840.10008.5.1.4.32.3",
            "General Purpose Performed Procedure Step SOP Class (Retired)"),
    /**
     * 实例可用性通知SOP类
     */
    InstanceAvailabilityNotification("1.2.840.10008.5.1.4.33", "Instance Availability Notification SOP Class"),
    /**
     * RT束传送指令存储 - 试验（已弃用）
     */
    RTBeamsDeliveryInstructionStorageTrial("1.2.840.10008.5.1.4.34.1",
            "RT Beams Delivery Instruction Storage - Trial (Retired)"),
    /**
     * RT常规机器验证 - 试验（已弃用）
     */
    RTConventionalMachineVerificationTrial("1.2.840.10008.5.1.4.34.2",
            "RT Conventional Machine Verification - Trial (Retired)"),
    /**
     * RT离子机器验证 - 试验（已弃用）
     */
    RTIonMachineVerificationTrial("1.2.840.10008.5.1.4.34.3", "RT Ion Machine Verification - Trial (Retired)"),
    /**
     * 统一工作列表和程序步骤服务类 - 试验（已弃用）
     */
    UnifiedWorklistAndProcedureStepTrial("1.2.840.10008.5.1.4.34.4",
            "Unified Worklist and Procedure Step Service Class - Trial (Retired)"),
    /**
     * 统一程序步骤 - 推送SOP类 - 试验（已弃用）
     */
    UnifiedProcedureStepPushTrial("1.2.840.10008.5.1.4.34.4.1",
            "Unified Procedure Step - Push SOP Class - Trial (Retired)"),
    /**
     * 统一程序步骤 - 监视SOP类 - 试验（已弃用）
     */
    UnifiedProcedureStepWatchTrial("1.2.840.10008.5.1.4.34.4.2",
            "Unified Procedure Step - Watch SOP Class - Trial (Retired)"),
    /**
     * 统一程序步骤 - 拉取SOP类 - 试验（已弃用）
     */
    UnifiedProcedureStepPullTrial("1.2.840.10008.5.1.4.34.4.3",
            "Unified Procedure Step - Pull SOP Class - Trial (Retired)"),
    /**
     * 统一程序步骤 - 事件SOP类 - 试验（已弃用）
     */
    UnifiedProcedureStepEventTrial("1.2.840.10008.5.1.4.34.4.4",
            "Unified Procedure Step - Event SOP Class - Trial (Retired)"),
    /**
     * UPS全局订阅SOP实例
     */
    UPSGlobalSubscriptionInstance("1.2.840.10008.5.1.4.34.5", "UPS Global Subscription SOP Instance"),
    /**
     * UPS过滤全局订阅SOP实例
     */
    UPSFilteredGlobalSubscriptionInstance("1.2.840.10008.5.1.4.34.5.1",
            "UPS Filtered Global Subscription SOP Instance"),
    /**
     * 统一工作列表和程序步骤服务类
     */
    UnifiedWorklistAndProcedureStep("1.2.840.10008.5.1.4.34.6", "Unified Worklist and Procedure Step Service Class"),
    /**
     * 统一程序步骤 - 推送SOP类
     */
    UnifiedProcedureStepPush("1.2.840.10008.5.1.4.34.6.1", "Unified Procedure Step - Push SOP Class"),
    /**
     * 统一程序步骤 - 监视SOP类
     */
    UnifiedProcedureStepWatch("1.2.840.10008.5.1.4.34.6.2", "Unified Procedure Step - Watch SOP Class"),
    /**
     * 统一程序步骤 - 拉取SOP类
     */
    UnifiedProcedureStepPull("1.2.840.10008.5.1.4.34.6.3", "Unified Procedure Step - Pull SOP Class"),
    /**
     * 统一程序步骤 - 事件SOP类
     */
    UnifiedProcedureStepEvent("1.2.840.10008.5.1.4.34.6.4", "Unified Procedure Step - Event SOP Class"),
    /**
     * 统一程序步骤 - 查询SOP类
     */
    UnifiedProcedureStepQuery("1.2.840.10008.5.1.4.34.6.5", "Unified Procedure Step - Query SOP Class"),
    /**
     * RT束传送指令存储
     */
    RTBeamsDeliveryInstructionStorage("1.2.840.10008.5.1.4.34.7", "RT Beams Delivery Instruction Storage"),
    /**
     * RT常规机器验证
     */
    RTConventionalMachineVerification("1.2.840.10008.5.1.4.34.8", "RT Conventional Machine Verification"),
    /**
     * RT离子机器验证
     */
    RTIonMachineVerification("1.2.840.10008.5.1.4.34.9", "RT Ion Machine Verification"),
    /**
     * RT近距离应用设置传送指令存储
     */
    RTBrachyApplicationSetupDeliveryInstructionStorage("1.2.840.10008.5.1.4.34.10",
            "RT Brachy Application Setup Delivery Instruction Storage"),
    /**
     * 通用相关患者信息查询
     */
    GeneralRelevantPatientInformationQuery("1.2.840.10008.5.1.4.37.1", "General Relevant Patient Information Query"),
    /**
     * 乳腺成像相关患者信息查询
     */
    BreastImagingRelevantPatientInformationQuery("1.2.840.10008.5.1.4.37.2",
            "Breast Imaging Relevant Patient Information Query"),
    /**
     * 心脏相关患者信息查询
     */
    CardiacRelevantPatientInformationQuery("1.2.840.10008.5.1.4.37.3", "Cardiac Relevant Patient Information Query"),
    /**
     * 挂起协议存储
     */
    HangingProtocolStorage("1.2.840.10008.5.1.4.38.1", "Hanging Protocol Storage"),
    /**
     * 挂起协议信息模型 - FIND
     */
    HangingProtocolInformationModelFind("1.2.840.10008.5.1.4.38.2", "Hanging Protocol Information Model - FIND"),
    /**
     * 挂起协议信息模型 - MOVE
     */
    HangingProtocolInformationModelMove("1.2.840.10008.5.1.4.38.3", "Hanging Protocol Information Model - MOVE"),
    /**
     * 挂起协议信息模型 - GET
     */
    HangingProtocolInformationModelGet("1.2.840.10008.5.1.4.38.4", "Hanging Protocol Information Model - GET"),
    /**
     * 颜色调色板存储
     */
    ColorPaletteStorage("1.2.840.10008.5.1.4.39.1", "Color Palette Storage"),
    /**
     * 颜色调色板查询/检索信息模型 - FIND
     */
    ColorPaletteQueryRetrieveInformationModelFind("1.2.840.10008.5.1.4.39.2",
            "Color Palette Query/Retrieve Information Model - FIND"),
    /**
     * 颜色调色板查询/检索信息模型 - MOVE
     */
    ColorPaletteQueryRetrieveInformationModelMove("1.2.840.10008.5.1.4.39.3",
            "Color Palette Query/Retrieve Information Model - MOVE"),
    /**
     * 颜色调色板查询/检索信息模型 - GET
     */
    ColorPaletteQueryRetrieveInformationModelGet("1.2.840.10008.5.1.4.39.4",
            "Color Palette Query/Retrieve Information Model - GET"),
    /**
     * 产品特性查询SOP类
     */
    ProductCharacteristicsQuery("1.2.840.10008.5.1.4.41", "Product Characteristics Query SOP Class"),
    /**
     * 物质批准查询SOP类
     */
    SubstanceApprovalQuery("1.2.840.10008.5.1.4.42", "Substance Approval Query SOP Class"),
    /**
     * 通用植入模板存储
     */
    GenericImplantTemplateStorage("1.2.840.10008.5.1.4.43.1", "Generic Implant Template Storage"),
    /**
     * 通用植入模板信息模型 - FIND
     */
    GenericImplantTemplateInformationModelFind("1.2.840.10008.5.1.4.43.2",
            "Generic Implant Template Information Model - FIND"),
    /**
     * 通用植入模板信息模型 - MOVE
     */
    GenericImplantTemplateInformationModelMove("1.2.840.10008.5.1.4.43.3",
            "Generic Implant Template Information Model - MOVE"),
    /**
     * 通用植入模板信息模型 - GET
     */
    GenericImplantTemplateInformationModelGet("1.2.840.10008.5.1.4.43.4",
            "Generic Implant Template Information Model - GET"),
    /**
     * 植入组件模板存储
     */
    ImplantAssemblyTemplateStorage("1.2.840.10008.5.1.4.44.1", "Implant Assembly Template Storage"),
    /**
     * 植入组件模板信息模型 - FIND
     */
    ImplantAssemblyTemplateInformationModelFind("1.2.840.10008.5.1.4.44.2",
            "Implant Assembly Template Information Model - FIND"),
    /**
     * 植入组件模板信息模型 - MOVE
     */
    ImplantAssemblyTemplateInformationModelMove("1.2.840.10008.5.1.4.44.3",
            "Implant Assembly Template Information Model - MOVE"),
    /**
     * 植入组件模板信息模型 - GET
     */
    ImplantAssemblyTemplateInformationModelGet("1.2.840.10008.5.1.4.44.4",
            "Implant Assembly Template Information Model - GET"),
    /**
     * 植入模板组存储
     */
    ImplantTemplateGroupStorage("1.2.840.10008.5.1.4.45.1", "Implant Template Group Storage"),
    /**
     * 植入模板组信息模型 - FIND
     */
    ImplantTemplateGroupInformationModelFind("1.2.840.10008.5.1.4.45.2",
            "Implant Template Group Information Model - FIND"),
    /**
     * 植入模板组信息模型 - MOVE
     */
    ImplantTemplateGroupInformationModelMove("1.2.840.10008.5.1.4.45.3",
            "Implant Template Group Information Model - MOVE"),
    /**
     * 植入模板组信息模型 - GET
     */
    ImplantTemplateGroupInformationModelGet("1.2.840.10008.5.1.4.45.4",
            "Implant Template Group Information Model - GET"),
    /**
     * 本机DICOM模型
     */
    NativeDICOMModel("1.2.840.10008.7.1.1", "Native DICOM Model"),
    /**
     * 抽象多维图像模型
     */
    AbstractMultiDimensionalImageModel("1.2.840.10008.7.1.2", "Abstract Multi-Dimensional Image Model"),
    /**
     * DICOM内容映射资源
     */
    DICOMContentMappingResource("1.2.840.10008.8.1.1", "DICOM Content Mapping Resource"),
    /**
     * 视频内窥镜图像实时通信
     */
    VideoEndoscopicImageRealTimeCommunication("1.2.840.10008.10.1", "Video Endoscopic Image Real-Time Communication"),
    /**
     * 视频摄影图像实时通信
     */
    VideoPhotographicImageRealTimeCommunication("1.2.840.10008.10.2",
            "Video Photographic Image Real-Time Communication"),
    /**
     * 音频波形实时通信
     */
    AudioWaveformRealTimeCommunication("1.2.840.10008.10.3", "Audio Waveform Real-Time Communication"),
    /**
     * 渲染选择文档实时通信
     */
    RenditionSelectionDocumentRealTimeCommunication("1.2.840.10008.10.4",
            "Rendition Selection Document Real-Time Communication"),
    /**
     * DICOM设备名称
     */
    dicomDeviceName("1.2.840.10008.15.0.3.1", "dicomDeviceName"),
    /**
     * DICOM描述
     */
    dicomDescription("1.2.840.10008.15.0.3.2", "dicomDescription"),
    /**
     * DICOM制造商
     */
    dicomManufacturer("1.2.840.10008.15.0.3.3", "dicomManufacturer"),
    /**
     * DICOM制造商型号名称
     */
    dicomManufacturerModelName("1.2.840.10008.15.0.3.4", "dicomManufacturerModelName"),
    /**
     * DICOM软件版本
     */
    dicomSoftwareVersion("1.2.840.10008.15.0.3.5", "dicomSoftwareVersion"),
    /**
     * DICOM供应商数据
     */
    dicomVendorData("1.2.840.10008.15.0.3.6", "dicomVendorData"),
    /**
     * DICOM AE标题
     */
    dicomAETitle("1.2.840.10008.15.0.3.7", "dicomAETitle"),
    /**
     * DICOM网络连接引用
     */
    dicomNetworkConnectionReference("1.2.840.10008.15.0.3.8", "dicomNetworkConnectionReference"),
    /**
     * DICOM应用集群
     */
    dicomApplicationCluster("1.2.840.10008.15.0.3.9", "dicomApplicationCluster"),
    /**
     * DICOM关联发起方
     */
    dicomAssociationInitiator("1.2.840.10008.15.0.3.10", "dicomAssociationInitiator"),
    /**
     * DICOM关联接受方
     */
    dicomAssociationAcceptor("1.2.840.10008.15.0.3.11", "dicomAssociationAcceptor"),
    /**
     * DICOM主机名
     */
    dicomHostname("1.2.840.10008.15.0.3.12", "dicomHostname"),
    /**
     * DICOM端口
     */
    dicomPort("1.2.840.10008.15.0.3.13", "dicomPort"),
    /**
     * DICOM SOP类
     */
    dicomSOPClass("1.2.840.10008.15.0.3.14", "dicomSOPClass"),
    /**
     * DICOM传输角色
     */
    dicomTransferRole("1.2.840.10008.15.0.3.15", "dicomTransferRole"),
    /**
     * DICOM传输语法
     */
    dicomTransferSyntax("1.2.840.10008.15.0.3.16", "dicomTransferSyntax"),
    /**
     * DICOM主要设备类型
     */
    dicomPrimaryDeviceType("1.2.840.10008.15.0.3.17", "dicomPrimaryDeviceType"),
    /**
     * DICOM相关设备引用
     */
    dicomRelatedDeviceReference("1.2.840.10008.15.0.3.18", "dicomRelatedDeviceReference"),
    /**
     * DICOM首选调用AE标题
     */
    dicomPreferredCalledAETitle("1.2.840.10008.15.0.3.19", "dicomPreferredCalledAETitle"),
    /**
     * DICOM TLS密码套件
     */
    dicomTLSCyphersuite("1.2.840.10008.15.0.3.20", "dicomTLSCyphersuite"),
    /**
     * DICOM授权节点证书引用
     */
    dicomAuthorizedNodeCertificateReference("1.2.840.10008.15.0.3.21", "dicomAuthorizedNodeCertificateReference"),
    /**
     * DICOM此节点证书引用
     */
    dicomThisNodeCertificateReference("1.2.840.10008.15.0.3.22", "dicomThisNodeCertificateReference"),
    /**
     * DICOM已安装
     */
    dicomInstalled("1.2.840.10008.15.0.3.23", "dicomInstalled"),
    /**
     * DICOM工作站名称
     */
    dicomStationName("1.2.840.10008.15.0.3.24", "dicomStationName"),
    /**
     * DICOM设备序列号
     */
    dicomDeviceSerialNumber("1.2.840.10008.15.0.3.25", "dicomDeviceSerialNumber"),
    /**
     * DICOM机构名称
     */
    dicomInstitutionName("1.2.840.10008.15.0.3.26", "dicomInstitutionName"),
    /**
     * DICOM机构地址
     */
    dicomInstitutionAddress("1.2.840.10008.15.0.3.27", "dicomInstitutionAddress"),
    /**
     * DICOM机构部门名称
     */
    dicomInstitutionDepartmentName("1.2.840.10008.15.0.3.28", "dicomInstitutionDepartmentName"),
    /**
     * DICOM患者ID签发者
     */
    dicomIssuerOfPatientID("1.2.840.10008.15.0.3.29", "dicomIssuerOfPatientID"),
    /**
     * DICOM首选调用AE标题
     */
    dicomPreferredCallingAETitle("1.2.840.10008.15.0.3.30", "dicomPreferredCallingAETitle"),
    /**
     * DICOM支持的字符集
     */
    dicomSupportedCharacterSet("1.2.840.10008.15.0.3.31", "dicomSupportedCharacterSet"),
    /**
     * DICOM配置根
     */
    dicomConfigurationRoot("1.2.840.10008.15.0.4.1", "dicomConfigurationRoot"),
    /**
     * DICOM设备根
     */
    dicomDevicesRoot("1.2.840.10008.15.0.4.2", "dicomDevicesRoot"),
    /**
     * DICOM唯一AE标题注册表根
     */
    dicomUniqueAETitlesRegistryRoot("1.2.840.10008.15.0.4.3", "dicomUniqueAETitlesRegistryRoot"),
    /**
     * DICOM设备
     */
    dicomDevice("1.2.840.10008.15.0.4.4", "dicomDevice"),
    /**
     * DICOM网络AE
     */
    dicomNetworkAE("1.2.840.10008.15.0.4.5", "dicomNetworkAE"),
    /**
     * DICOM网络连接
     */
    dicomNetworkConnection("1.2.840.10008.15.0.4.6", "dicomNetworkConnection"),
    /**
     * DICOM唯一AE标题
     */
    dicomUniqueAETitle("1.2.840.10008.15.0.4.7", "dicomUniqueAETitle"),
    /**
     * DICOM传输能力
     */
    dicomTransferCapability("1.2.840.10008.15.0.4.8", "dicomTransferCapability"),
    /**
     * 协调世界时
     */
    UTC("1.2.840.10008.15.1.1", "Universal Coordinated Time"),
    /**
     * 私有封装Genozip存储
     */
    PrivateEncapsulatedGenozipStorage("1.2.40.0.13.1.5.1.4.1.1.104.1", "Private Encapsulated Genozip Storage"),
    /**
     * 私有封装Bzip2 VCF存储
     */
    PrivateEncapsulatedBzip2VCFStorage("1.2.40.0.13.1.5.1.4.1.1.104.2", "Private Encapsulated Bzip2 VCF Storage"),
    /**
     * 私有封装Bzip2文档存储
     */
    PrivateEncapsulatedBzip2DocumentStorage("1.2.40.0.13.1.5.1.4.1.1.104.3",
            "Private Encapsulated Bzip2 Document Storage"),
    /**
     * 私有Agfa基本属性表现状态
     */
    PrivateAgfaBasicAttributePresentationState("1.2.124.113532.3500.7",
            "Private Agfa Basic Attribute Presentation State"),
    /**
     * 私有Agfa到达事务
     */
    PrivateAgfaArrivalTransaction("1.2.124.113532.3500.8.1", "Private Agfa Arrival Transaction"),
    /**
     * 私有Agfa听写事务
     */
    PrivateAgfaDictationTransaction("1.2.124.113532.3500.8.2", "Private Agfa Dictation Transaction"),
    /**
     * 私有Agfa报告转录事务
     */
    PrivateAgfaReportTranscriptionTransaction("1.2.124.113532.3500.8.3",
            "Private Agfa Report Transcription Transaction"),
    /**
     * 私有Agfa报告批准事务
     */
    PrivateAgfaReportApprovalTransaction("1.2.124.113532.3500.8.4", "Private Agfa Report Approval Transaction"),
    /**
     * 私有TomTec注释存储
     */
    PrivateTomTecAnnotationStorage("1.2.276.0.48.5.1.4.1.1.7", "Private TomTec Annotation Storage"),
    /**
     * 私有东芝US图像存储
     */
    PrivateToshibaUSImageStorage("1.2.392.200036.9116.7.8.1.1.1", "Private Toshiba US Image Storage"),
    /**
     * 私有富士CR图像存储
     */
    PrivateFujiCRImageStorage("1.2.392.200036.9125.1.1.2", "Private Fuji CR Image Storage"),
    /**
     * 私有GE拼贴存储
     */
    PrivateGECollageStorage("1.2.528.1.1001.5.1.1.1", "Private GE Collage Storage"),
    /**
     * 私有ERAD实践构建器报告文本存储
     */
    PrivateERADPracticeBuilderReportTextStorage("1.2.826.0.1.3680043.293.1.0.1",
            "Private ERAD Practice Builder Report Text Storage"),
    /**
     * 私有ERAD实践构建器报告听写存储
     */
    PrivateERADPracticeBuilderReportDictationStorage("1.2.826.0.1.3680043.293.1.0.2",
            "Private ERAD Practice Builder Report Dictation Storage"),
    /**
     * 私有飞利浦HP Live 3D 01存储
     */
    PrivatePhilipsHPLive3D01Storage("1.2.840.113543.6.6.1.3.10001", "Private Philips HP Live 3D 01 Storage"),
    /**
     * 私有飞利浦HP Live 3D 02存储
     */
    PrivatePhilipsHPLive3D02Storage("1.2.840.113543.6.6.1.3.10002", "Private Philips HP Live 3D 02 Storage"),
    /**
     * 私有GE 3D模型存储
     */
    PrivateGE3DModelStorage("1.2.840.113619.4.26", "Private GE 3D Model Storage"),
    /**
     * 私有GE Dicom CT图像信息对象
     */
    PrivateGEDicomCTImageInfoObject("1.2.840.113619.4.3", "Private GE Dicom CT Image Info Object"),
    /**
     * 私有GE Dicom显示图像信息对象
     */
    PrivateGEDicomDisplayImageInfoObject("1.2.840.113619.4.4", "Private GE Dicom Display Image Info Object"),
    /**
     * 私有GE Dicom MR图像信息对象
     */
    PrivateGEDicomMRImageInfoObject("1.2.840.113619.4.2", "Private GE Dicom MR Image Info Object"),
    /**
     * 私有GE eNTEGRA协议或NM Genie存储
     */
    PrivateGEeNTEGRAProtocolOrNMGenieStorage("1.2.840.113619.4.27", "Private GE eNTEGRA Protocol or NM Genie Storage"),
    /**
     * 私有GE PET原始数据存储
     */
    PrivateGEPETRawDataStorage("1.2.840.113619.4.30", "Private GE PET Raw Data Storage"),
    /**
     * 私有GE RT计划存储
     */
    PrivateGERTPlanStorage("1.2.840.113619.4.5.249", "Private GE RT Plan Storage"),
    /**
     * 私有PixelMed遗留转换增强CT图像存储
     */
    PrivatePixelMedLegacyConvertedEnhancedCTImageStorage("1.3.6.1.4.1.5962.301.1",
            "Private PixelMed Legacy Converted Enhanced CT Image Storage"),
    /**
     * 私有PixelMed遗留转换增强MR图像存储
     */
    PrivatePixelMedLegacyConvertedEnhancedMRImageStorage("1.3.6.1.4.1.5962.301.2",
            "Private PixelMed Legacy Converted Enhanced MR Image Storage"),
    /**
     * 私有PixelMed遗留转换增强PET图像存储
     */
    PrivatePixelMedLegacyConvertedEnhancedPETImageStorage("1.3.6.1.4.1.5962.301.3",
            "Private PixelMed Legacy Converted Enhanced PET Image Storage"),
    /**
     * 私有PixelMed浮点图像存储
     */
    PrivatePixelMedFloatingPointImageStorage("1.3.6.1.4.1.5962.301.9", "Private PixelMed Floating Point Image Storage"),
    /**
     * 私有西门子CSA非图像存储
     */
    PrivateSiemensCSANonImageStorage("1.3.12.2.1107.5.9.1", "Private Siemens CSA Non Image Storage"),
    /**
     * 私有西门子CT MR体积存储
     */
    PrivateSiemensCTMRVolumeStorage("1.3.12.2.1107.5.99.3.10", "Private Siemens CT MR Volume Storage"),
    /**
     * 私有西门子AX帧集存储
     */
    PrivateSiemensAXFrameSetsStorage("1.3.12.2.1107.5.99.3.11", "Private Siemens AX Frame Sets Storage"),
    /**
     * 私有飞利浦专用XA存储
     */
    PrivatePhilipsSpecialisedXAStorage("1.3.46.670589.2.3.1.1", "Private Philips Specialised XA Storage"),
    /**
     * 私有飞利浦CX图像存储
     */
    PrivatePhilipsCXImageStorage("1.3.46.670589.2.4.1.1", "Private Philips CX Image Storage"),
    /**
     * 私有飞利浦3D表现状态存储
     */
    PrivatePhilips3DPresentationStateStorage("1.3.46.670589.2.5.1.1", "Private Philips 3D Presentation State Storage"),
    /**
     * 私有飞利浦VRML存储
     */
    PrivatePhilipsVRMLStorage("1.3.46.670589.2.8.1.1", "Private Philips VRML Storage"),
    /**
     * 私有飞利浦体积集存储
     */
    PrivatePhilipsVolumeSetStorage("1.3.46.670589.2.11.1.1", "Private Philips Volume Set Storage"),
    /**
     * 私有飞利浦体积存储（已弃用）
     */
    PrivatePhilipsVolumeStorageRetired("1.3.46.670589.5.0.1", "Private Philips Volume Storage (Retired)"),
    /**
     * 私有飞利浦体积存储
     */
    PrivatePhilipsVolumeStorage("1.3.46.670589.5.0.1.1", "Private Philips Volume Storage"),
    /**
     * 私有飞利浦3D对象存储（已弃用）
     */
    PrivatePhilips3DObjectStorageRetired("1.3.46.670589.5.0.2", "Private Philips 3D Object Storage (Retired)"),
    /**
     * 私有飞利浦3D对象存储
     */
    PrivatePhilips3DObjectStorage("1.3.46.670589.5.0.2.1", "Private Philips 3D Object Storage"),
    /**
     * 私有飞利浦表面存储（已弃用）
     */
    PrivatePhilipsSurfaceStorageRetired("1.3.46.670589.5.0.3", "Private Philips Surface Storage (Retired)"),
    /**
     * 私有飞利浦表面存储
     */
    PrivatePhilipsSurfaceStorage("1.3.46.670589.5.0.3.1", "Private Philips Surface Storage"),
    /**
     * 私有飞利浦复合对象存储
     */
    PrivatePhilipsCompositeObjectStorage("1.3.46.670589.5.0.4", "Private Philips Composite Object Storage"),
    /**
     * 私有飞利浦MR心血管配置文件存储
     */
    PrivatePhilipsMRCardioProfileStorage("1.3.46.670589.5.0.7", "Private Philips MR Cardio Profile Storage"),
    /**
     * 私有飞利浦MR心血管存储（已弃用）
     */
    PrivatePhilipsMRCardioStorageRetired("1.3.46.670589.5.0.8", "Private Philips MR Cardio Storage (Retired)"),
    /**
     * 私有飞利浦MR心血管存储
     */
    PrivatePhilipsMRCardioStorage("1.3.46.670589.5.0.8.1", "Private Philips MR Cardio Storage"),
    /**
     * 私有飞利浦CT合成图像存储
     */
    PrivatePhilipsCTSyntheticImageStorage("1.3.46.670589.5.0.9", "Private Philips CT Synthetic Image Storage"),
    /**
     * 私有飞利浦MR合成图像存储
     */
    PrivatePhilipsMRSyntheticImageStorage("1.3.46.670589.5.0.10", "Private Philips MR Synthetic Image Storage"),
    /**
     * 私有飞利浦MR心血管分析存储（已弃用）
     */
    PrivatePhilipsMRCardioAnalysisStorageRetired("1.3.46.670589.5.0.11",
            "Private Philips MR Cardio Analysis Storage (Retired)"),
    /**
     * 私有飞利浦MR心血管分析存储
     */
    PrivatePhilipsMRCardioAnalysisStorage("1.3.46.670589.5.0.11.1", "Private Philips MR Cardio Analysis Storage"),
    /**
     * 私有飞利浦CX合成图像存储
     */
    PrivatePhilipsCXSyntheticImageStorage("1.3.46.670589.5.0.12", "Private Philips CX Synthetic Image Storage"),
    /**
     * 私有飞利浦灌注存储
     */
    PrivatePhilipsPerfusionStorage("1.3.46.670589.5.0.13", "Private Philips Perfusion Storage"),
    /**
     * 私有飞利浦灌注图像存储
     */
    PrivatePhilipsPerfusionImageStorage("1.3.46.670589.5.0.14", "Private Philips Perfusion Image Storage"),
    /**
     * 私有飞利浦X射线MF存储
     */
    PrivatePhilipsXRayMFStorage("1.3.46.670589.7.8.1618510091", "Private Philips X-Ray MF Storage"),
    /**
     * 私有飞利浦实时运行存储
     */
    PrivatePhilipsLiveRunStorage("1.3.46.670589.7.8.1618510092", "Private Philips Live Run Storage"),
    /**
     * 私有飞利浦运行存储
     */
    PrivatePhilipsRunStorage("1.3.46.670589.7.8.16185100129", "Private Philips Run Storage"),
    /**
     * 私有飞利浦重建存储
     */
    PrivatePhilipsReconstructionStorage("1.3.46.670589.7.8.16185100130", "Private Philips Reconstruction Storage"),
    /**
     * 私有飞利浦MR光谱存储
     */
    PrivatePhilipsMRSpectrumStorage("1.3.46.670589.11.0.0.12.1", "Private Philips MR Spectrum Storage"),
    /**
     * 私有飞利浦MR系列数据存储
     */
    PrivatePhilipsMRSeriesDataStorage("1.3.46.670589.11.0.0.12.2", "Private Philips MR Series Data Storage"),
    /**
     * 私有飞利浦MR彩色图像存储
     */
    PrivatePhilipsMRColorImageStorage("1.3.46.670589.11.0.0.12.3", "Private Philips MR Color Image Storage"),
    /**
     * 私有飞利浦MR检查卡存储
     */
    PrivatePhilipsMRExamcardStorage("1.3.46.670589.11.0.0.12.4", "Private Philips MR Examcard Storage"),
    /**
     * 私有PMOD多帧图像存储
     */
    PrivatePMODMultiFrameImageStorage("2.16.840.1.114033.5.1.4.1.1.130", "Private PMOD Multi-frame Image Storage");

    /**
     * 用于验证UID格式的正则表达式模式
     */
    public static final Pattern PATTERN = Pattern.compile("[012]((\\.0)|(\\.[1-9]\\d*))+");
    /**
     * ASCII字符集
     */
    public static final Charset ASCII = StandardCharsets.US_ASCII;

    /**
     * 
     * UUID根UID，用于根据ITU-T X.667 | ISO/IEC 9834-8标准生成UUID(通用唯一标识符)。
     *
     * @see <a href="http://www.oid-info.com/get/2.25">OID repository {joint-iso-itu-t(2) uuid(25)}</a>
     */
    private static final String UUID_ROOT = "2.25";
    /**
     * UID根，默认为UUID_ROOT
     */
    public static String root = UUID_ROOT;

    /**
     * UID字符串
     */
    public final String uid;
    /**
     * UID描述
     */
    public final String desc;

    /**
     * 
     * 构造方法
     *
     * @param uid  UID编码
     * @param desc UID描述
     */
    UID(String uid, String desc) {
        this.uid = uid;
        this.desc = desc;
    }

    /**
     * 
     * 根据UID字符串获取对应的UID枚举常量
     *
     * @param uid UID字符串
     * @return 匹配的UID枚举常量，如果没有匹配则返回null
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
     * 
     * 获取指定UID的描述信息
     *
     * @param uid UID字符串
     * @return UID的描述信息，如果找不到匹配的UID则返回"?"
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
     * 根据关键字获取对应的UID字符串
     *
     * @param keyword UID关键字(枚举常量名)
     * @return 对应的UID字符串
     * @throws IllegalArgumentException 如果指定的关键字不存在
     */
    public static String forName(String keyword) {
        try {
            return (String) UID.class.getField(keyword).get(null);
        } catch (Exception var2) {
            throw new IllegalArgumentException(keyword);
        }
    }

    /**
     * 将字符串转换为UID格式
     *
     * @param uid 输入字符串，可以是"*"、UID字符串或关键字
     * @return 转换后的UID字符串
     */
    public static String toUID(String uid) {
        uid = uid.trim();
        return (uid.equals(Symbol.STAR) || Character.isDigit(uid.charAt(0))) ? uid : forName(uid);
    }

    /**
     * 将UUID转换为指定根下的UID字符串
     *
     * @param root UID根
     * @param uuid UUID对象
     * @return 转换后的UID字符串
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
     * 将逗号分隔的UID字符串转换为UID数组
     *
     * @param s 逗号分隔的UID字符串，可以是"*"
     * @return UID数组
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
     * 
     * 查找匹配指定正则表达式的UID
     *
     * @param regex 用于匹配UID关键字的正则表达式
     * @return 匹配的UID字符串数组
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
     * 使用默认根重新映射UID
     *
     * @param uid 原始UID
     * @return 重新映射后的UID
     */
    public static String remapUID(String uid) {
        return nameBasedUID(uid.getBytes(ASCII), root);
    }

    /**
     * 使用指定根重新映射UID
     *
     * @param uid  原始UID
     * @param root UID根
     * @return 重新映射后的UID
     */
    public static String remapUID(String uid, String root) {
        checkRoot(root);
        return nameBasedUID(uid.getBytes(ASCII), root);
    }

    /**
     * 
     * 根据指定的映射重新映射DICOM属性中的UID
     *
     * @param attrs  DICOM属性对象
     * @param uidMap UID映射表
     * @return 被替换的UID数量
     */
    public static int remapUID(Attributes attrs, Map<String, String> uidMap) {
        return remapUIDs(attrs, uidMap, null);
    }

    /**
     * 根据指定的映射重新映射DICOM属性中的UID
     *
     * @param attrs    DICOM属性对象，其中的UID将被替换
     * @param uidMap   指定的映射
     * @param modified 用于收集被覆盖的非空属性及其原始值的属性对象，可以为null
     * @return 被替换的UID数量
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
     * 生成随机UID
     *
     * @param root UID根
     * @return 随机生成的UID
     */
    private static String randomUID(String root) {
        return toUID(root, UUID.randomUUID());
    }

    /**
     * 基于名称生成UID
     *
     * @param name 名称字节数组
     * @param root UID根
     * @return 基于名称生成的UID
     */
    private static String nameBasedUID(byte[] name, String root) {
        return toUID(root, UUID.nameUUIDFromBytes(name));
    }

    /**
     * 将UID及其描述信息追加到字符串构建器
     *
     * @param uid UID字符串
     * @param sb  字符串构建器
     * @return 追加信息后的字符串构建器
     */
    public static StringBuilder promptTo(String uid, StringBuilder sb) {
        return sb.append(uid).append(" - ").append(nameOf(uid));
    }

    /**
     * 使用默认根创建新的UID
     *
     * @return 新创建的UID
     */
    public static String createUID() {
        return randomUID(root);
    }

    /**
     * 使用指定根创建新的UID
     *
     * @param root UID根
     * @return 新创建的UID
     */
    public static String createUID(String root) {
        checkRoot(root);
        return randomUID(root);
    }

    /**
     * 使用默认根基于名称创建UID
     *
     * @param name 名称字节数组
     * @return 基于名称创建的UID
     */
    public static String createNameBasedUID(byte[] name) {
        return nameBasedUID(name, root);
    }

    /**
     * 使用指定根基于名称创建UID
     *
     * @param name 名称字节数组
     * @param root UID根
     * @return 基于名称创建的UID
     */
    private static String createNameBasedUID(byte[] name, String root) {
        checkRoot(root);
        return nameBasedUID(name, root);
    }

    /**
     * 如果UID为null则使用默认根创建新的UID，否则返回原UID
     *
     * @param uid 原始UID，可以为null
     * @return 原UID或新创建的UID
     */
    public static String createUIDIfNull(String uid) {
        return uid == null ? randomUID(root) : uid;
    }

    /**
     * 如果UID为null则使用指定根创建新的UID，否则返回原UID
     *
     * @param uid  原始UID，可以为null
     * @param root UID根
     * @return 原UID或新创建的UID
     */
    private static String createUIDIfNull(String uid, String root) {
        checkRoot(root);
        return uid == null ? randomUID(root) : uid;
    }

    /**
     * 
     * 验证UID格式是否有效
     *
     * @param uid 要验证的UID字符串
     * @return 如果UID格式有效返回true，否则返回false
     */
    private static boolean isValid1(String uid) {
        return uid.length() <= 64 && PATTERN.matcher(uid).matches();
    }

    /**
     * 获取当前UID根
     *
     * @return 当前UID根
     */
    private static final String getRoot() {
        return root;
    }

    /**
     * 设置UID根
     *
     * @param root 新的UID根
     */
    private static final void setRoot(String root) {
        checkRoot(root);
        UID.root = root;
    }

    /**
     * 检查UID根是否有效
     *
     * @param root 要检查的UID根
     * @throws IllegalArgumentException 如果UID根无效
     */
    private static void checkRoot(String root) {
        if (root.length() > 24)
            throw new IllegalArgumentException("root length > 24");
        if (!isValid1(root))
            throw new IllegalArgumentException(root);
    }

    /**
     * 获取UID字符串
     *
     * @return UID字符串
     */
    public String getUid() {
        return uid;
    }

    /**
     * 获取UID描述
     *
     * @return UID描述
     */
    public String getDesc() {
        return desc;
    }

}