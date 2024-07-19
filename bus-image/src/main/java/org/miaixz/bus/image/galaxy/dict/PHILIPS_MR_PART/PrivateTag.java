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
public class PrivateTag {

    public static final String PrivateCreator = "PHILIPS MR/PART";

    /** (0019,xx00) VR=DS VM=1 Field of View */
    public static final int FieldOfView = 0x00190000;

    /** (0019,xx01) VR=IS VM=1-n Stack Type */
    public static final int StackType = 0x00190001;

    /** (0019,xx02) VR=IS VM=1-n ? */
    public static final int _0019_xx02_ = 0x00190002;

    /** (0019,xx03) VR=DS VM=1 ? */
    public static final int _0019_xx03_ = 0x00190003;

    /** (0019,xx05) VR=DS VM=1 CC Angulation */
    public static final int CCAngulation = 0x00190005;

    /** (0019,xx06) VR=DS VM=1 AP Angulation */
    public static final int APAngulation = 0x00190006;

    /** (0019,xx07) VR=DS VM=1 LR Angulation */
    public static final int LRAngulation = 0x00190007;

    /** (0019,xx08) VR=IS VM=1 Patient Orientation 1 */
    public static final int PatientOrientation1 = 0x00190008;

    /** (0019,xx09) VR=IS VM=1 Patient Orientation */
    public static final int PatientOrientation = 0x00190009;

    /** (0019,xx0a) VR=IS VM=1 Slice Orientation */
    public static final int SliceOrientation = 0x0019000a;

    /** (0019,xx0b) VR=DS VM=1 LR Offcenter */
    public static final int LROffcenter = 0x0019000b;

    /** (0019,xx0c) VR=DS VM=1 CC Offcenter */
    public static final int CCOffcenter = 0x0019000c;

    /** (0019,xx0d) VR=DS VM=1 AP Offcenter */
    public static final int APOffcenter = 0x0019000d;

    /** (0019,xx0e) VR=DS VM=1 ? */
    public static final int _0019_xx0e_ = 0x0019000e;

    /** (0019,xx0f) VR=IS VM=1 Number of Slices */
    public static final int NumberOfSlices = 0x0019000f;

    /** (0019,xx10) VR=DS VM=1 Slice Factor */
    public static final int SliceFactor = 0x00190010;

    /** (0019,xx11) VR=DS VM=1-n Echo Times */
    public static final int EchoTimes = 0x00190011;

    /** (0019,xx14) VR=CS VM=1 ? */
    public static final int _0019_xx14_ = 0x00190014;

    /** (0019,xx15) VR=IS VM=1 Dynamic Study */
    public static final int DynamicStudy = 0x00190015;

    /** (0019,xx18) VR=DS VM=1 Heartbeat Interval */
    public static final int HeartbeatInterval = 0x00190018;

    /** (0019,xx19) VR=DS VM=1 Repetition Time FFE */
    public static final int RepetitionTimeFFE = 0x00190019;

    /** (0019,xx1a) VR=DS VM=1 FFE Flip Angle */
    public static final int FFEFlipAngle = 0x0019001a;

    /** (0019,xx1b) VR=IS VM=1 Number of Scans */
    public static final int NumberOfScans = 0x0019001b;

    /** (0019,xx1c) VR=CS VM=1 ? */
    public static final int _0019_xx1c_ = 0x0019001c;

    /** (0019,xx1d) VR=CS VM=1 ? */
    public static final int _0019_xx1d_ = 0x0019001d;

    /** (0019,xx1e) VR=DS VM=1 ? */
    public static final int _0019_xx1e_ = 0x0019001e;

    /** (0019,xx21) VR=DS VM=1-n ? */
    public static final int _0019_xx21_ = 0x00190021;

    /** (0019,xx22) VR=DS VM=1 Dynamic Scan Time Begin */
    public static final int DynamicScanTimeBegin = 0x00190022;

    /** (0019,xx23) VR=DS VM=1-n ? */
    public static final int _0019_xx23_ = 0x00190023;

    /** (0019,xx24) VR=IS VM=1 ? */
    public static final int _0019_xx24_ = 0x00190024;

    /** (0019,xx25) VR=DS VM=1-n ? */
    public static final int _0019_xx25_ = 0x00190025;

    /** (0019,xx26) VR=DS VM=1-n ? */
    public static final int _0019_xx26_ = 0x00190026;

    /** (0019,xx27) VR=DS VM=1-n ? */
    public static final int _0019_xx27_ = 0x00190027;

    /** (0019,xx28) VR=CS VM=1 ? */
    public static final int _0019_xx28_ = 0x00190028;

    /** (0019,xx29) VR=DS VM=1-n ? */
    public static final int _0019_xx29_ = 0x00190029;

    /** (0019,xx30) VR=LO VM=1-n ? */
    public static final int _0019_xx30_ = 0x00190030;

    /** (0019,xx31) VR=DS VM=1-n ? */
    public static final int _0019_xx31_ = 0x00190031;

    /** (0019,xx40) VR=US VM=1 ? */
    public static final int _0019_xx40_ = 0x00190040;

    /** (0019,xx45) VR=IS VM=1 Reconstruction Resolution */
    public static final int ReconstructionResolution = 0x00190045;

    /** (0019,xx50) VR=IS VM=1 ? */
    public static final int _0019_xx50_ = 0x00190050;

    /** (0019,xx51) VR=DS VM=1 ? */
    public static final int _0019_xx51_ = 0x00190051;

    /** (0019,xx52) VR=DS VM=1 ? */
    public static final int _0019_xx52_ = 0x00190052;

    /** (0019,xx53) VR=IS VM=1 ? */
    public static final int _0019_xx53_ = 0x00190053;

    /** (0019,xx54) VR=DS VM=1 ? */
    public static final int _0019_xx54_ = 0x00190054;

    /** (0019,xx55) VR=DS VM=1 ? */
    public static final int _0019_xx55_ = 0x00190055;

    /** (0019,xx56) VR=DS VM=1 ? */
    public static final int _0019_xx56_ = 0x00190056;

    /** (0019,xx57) VR=US VM=1 ? */
    public static final int _0019_xx57_ = 0x00190057;

    /** (0019,xx58) VR=DS VM=1 ? */
    public static final int _0019_xx58_ = 0x00190058;

    /** (0019,xx59) VR=US VM=1 ? */
    public static final int _0019_xx59_ = 0x00190059;

    /** (0019,xx60) VR=DS VM=1 ? */
    public static final int _0019_xx60_ = 0x00190060;

    /** (0019,xx61) VR=DS VM=1 ? */
    public static final int _0019_xx61_ = 0x00190061;

    /** (0019,xx62) VR=DS VM=1 ? */
    public static final int _0019_xx62_ = 0x00190062;

    /** (0019,xx63) VR=DS VM=1 ? */
    public static final int _0019_xx63_ = 0x00190063;

    /** (0019,xx64) VR=DS VM=1 Repetition Time SE */
    public static final int RepetitionTimeSE = 0x00190064;

    /** (0019,xx65) VR=DS VM=1 Repetition Time IR */
    public static final int RepetitionTimeIR = 0x00190065;

    /** (0019,xx66) VR=US VM=1 ? */
    public static final int _0019_xx66_ = 0x00190066;

    /** (0019,xx67) VR=US VM=1 ? */
    public static final int _0019_xx67_ = 0x00190067;

    /** (0019,xx69) VR=IS VM=1 Number of Phases */
    public static final int NumberOfPhases = 0x00190069;

    /** (0019,xx6a) VR=IS VM=1 Cardiac Frequency */
    public static final int CardiacFrequency = 0x0019006a;

    /** (0019,xx6b) VR=DS VM=1 Inversion Delay */
    public static final int InversionDelay = 0x0019006b;

    /** (0019,xx6c) VR=DS VM=1 Gate Delay */
    public static final int GateDelay = 0x0019006c;

    /** (0019,xx6d) VR=DS VM=1 Gate Width */
    public static final int GateWidth = 0x0019006d;

    /** (0019,xx6e) VR=DS VM=1 Trigger Delay Time */
    public static final int TriggerDelayTime = 0x0019006e;

    /** (0019,xx70) VR=DS VM=1-n ? */
    public static final int _0019_xx70_ = 0x00190070;

    /** (0019,xx80) VR=IS VM=1 Number of Chemical Shifts */
    public static final int NumberOfChemicalShifts = 0x00190080;

    /** (0019,xx81) VR=DS VM=1 Chemical Shift */
    public static final int ChemicalShift = 0x00190081;

    /** (0019,xx84) VR=IS VM=1 Number of Rows */
    public static final int NumberOfRows = 0x00190084;

    /** (0019,xx85) VR=IS VM=1 Number of Samples */
    public static final int NumberOfSamples = 0x00190085;

    /** (0019,xx8A) VR=CS VM=1 ? */
    public static final int _0019_xx8A_ = 0x0019008A;

    /** (0019,xx8B) VR=CS VM=1 ? */
    public static final int _0019_xx8B_ = 0x0019008B;

    /** (0019,xx8C) VR=CS VM=1 ? */
    public static final int _0019_xx8C_ = 0x0019008C;

    /** (0019,xx8D) VR=CS VM=1 ? */
    public static final int _0019_xx8D_ = 0x0019008D;

    /** (0019,xx8E) VR=CS VM=1 ? */
    public static final int _0019_xx8E_ = 0x0019008E;

    /** (0019,xx8F) VR=CS VM=1 ? */
    public static final int _0019_xx8F_ = 0x0019008F;

    /** (0019,xx94) VR=LO VM=1 Magnetization Transfer Contrast */
    public static final int MagnetizationTransferContrast = 0x00190094;

    /** (0019,xx95) VR=LO VM=1 Spectral Presaturation With Inversion Recovery */
    public static final int SpectralPresaturationWithInversionRecovery = 0x00190095;

    /** (0019,xx96) VR=IS VM=1 ? */
    public static final int _0019_xx96_ = 0x00190096;

    /** (0019,xx97) VR=LO VM=1 ? */
    public static final int _0019_xx97_ = 0x00190097;

    /** (0019,xxB4) VR=DS VM=1 ? */
    public static final int _0019_xxB4_ = 0x001900B4;

    /** (0019,xxB5) VR=DS VM=1 ? */
    public static final int _0019_xxB5_ = 0x001900B5;

    /** (0019,xxB6) VR=DS VM=1 ? */
    public static final int _0019_xxB6_ = 0x001900B6;

    /** (0019,xxD1) VR=US VM=1 ? */
    public static final int _0019_xxD1_ = 0x001900D1;

    /** (0019,xxD3) VR=DS VM=1 ? */
    public static final int _0019_xxD3_ = 0x001900D3;

    /** (0019,xxF0) VR=IS VM=1 ? */
    public static final int _0019_xxF0_ = 0x001900F0;

    /** (0019,xxF6) VR=CS VM=1 ? */
    public static final int _0019_xxF6_ = 0x001900F6;

    /** (0019,xxF7) VR=CS VM=1 ? */
    public static final int _0019_xxF7_ = 0x001900F7;

    /** (0019,xxF8) VR=CS VM=1 ? */
    public static final int _0019_xxF8_ = 0x001900F8;

    /** (0019,xxF9) VR=CS VM=1 ? */
    public static final int _0019_xxF9_ = 0x001900F9;

    /** (0019,xxFA) VR=CS VM=1 ? */
    public static final int _0019_xxFA_ = 0x001900FA;

    /** (0019,xxFB) VR=CS VM=1 ? */
    public static final int _0019_xxFB_ = 0x001900FB;

    /** (0019,xxa0) VR=IS VM=1 ? */
    public static final int _0019_xxa0_ = 0x001900a0;

    /** (0019,xxa1) VR=DS VM=1 ? */
    public static final int _0019_xxa1_ = 0x001900a1;

    /** (0019,xxa3) VR=DS VM=1 ? */
    public static final int _0019_xxa3_ = 0x001900a3;

    /** (0019,xxa4) VR=CS VM=1 ? */
    public static final int _0019_xxa4_ = 0x001900a4;

    /** (0019,xxc0) VR=DS VM=1 Trigger Delay Times */
    public static final int TriggerDelayTimes = 0x001900c0;

    /** (0019,xxc8) VR=IS VM=1 ? */
    public static final int _0019_xxc8_ = 0x001900c8;

    /** (0019,xxc9) VR=IS VM=1 Foldover Direction Transverse */
    public static final int FoldoverDirectionTransverse = 0x001900c9;

    /** (0019,xxca) VR=IS VM=1 Foldover Direction Sagittal */
    public static final int FoldoverDirectionSagittal = 0x001900ca;

    /** (0019,xxcb) VR=IS VM=1 Foldover Direction Coronal */
    public static final int FoldoverDirectionCoronal = 0x001900cb;

    /** (0019,xxcc) VR=IS VM=1 ? */
    public static final int _0019_xxcc_ = 0x001900cc;

    /** (0019,xxcd) VR=IS VM=1 ? */
    public static final int _0019_xxcd_ = 0x001900cd;

    /** (0019,xxce) VR=IS VM=1 ? */
    public static final int _0019_xxce_ = 0x001900ce;

    /** (0019,xxcf) VR=IS VM=1 Number of Echoes */
    public static final int NumberOfEchoes = 0x001900cf;

    /** (0019,xxd0) VR=IS VM=1 Scan Resolution */
    public static final int ScanResolution = 0x001900d0;

    /** (0019,xxd2) VR=LO VM=2 Water Fat Shift */
    public static final int WaterFatShift = 0x001900d2;

    /** (0019,xxd4) VR=IS VM=1 Artifact Reduction */
    public static final int ArtifactReduction = 0x001900d4;

    /** (0019,xxd5) VR=IS VM=1 ? */
    public static final int _0019_xxd5_ = 0x001900d5;

    /** (0019,xxd6) VR=IS VM=1 ? */
    public static final int _0019_xxd6_ = 0x001900d6;

    /** (0019,xxd7) VR=DS VM=1 Scan Percentage */
    public static final int ScanPercentage = 0x001900d7;

    /** (0019,xxd8) VR=IS VM=1 Halfscan */
    public static final int Halfscan = 0x001900d8;

    /** (0019,xxd9) VR=IS VM=1 EPI Factor */
    public static final int EPIFactor = 0x001900d9;

    /** (0019,xxda) VR=IS VM=1 Turbo Factor */
    public static final int TurboFactor = 0x001900da;

    /** (0019,xxdb) VR=IS VM=1 ? */
    public static final int _0019_xxdb_ = 0x001900db;

    /** (0019,xxe0) VR=IS VM=1 Percentage of Scan Completed */
    public static final int PercentageOfScanCompleted = 0x001900e0;

    /** (0019,xxe1) VR=DS VM=1 Prepulse Delay */
    public static final int PrepulseDelay = 0x001900e1;

    /** (0019,xxe3) VR=DS VM=1 Phase Contrast Velocity */
    public static final int PhaseContrastVelocity = 0x001900e3;

    /** (0019,xxfc) VR=IS VM=1 Resonance Frequency */
    public static final int ResonanceFrequency = 0x001900fc;

    /** (0021,xx00) VR=IS VM=1 Reconstruction Number */
    public static final int ReconstructionNumber = 0x00210000;

    /** (0021,xx06) VR=LO VM=1 ? */
    public static final int _0021_xx06_ = 0x00210006;

    /** (0021,xx08) VR=LO VM=1 ? */
    public static final int _0021_xx08_ = 0x00210008;

    /** (0021,xx09) VR=CS VM=1 ? */
    public static final int _0021_xx09_ = 0x00210009;

    /** (0021,xx0a) VR=CS VM=1 ? */
    public static final int _0021_xx0a_ = 0x0021000a;

    /** (0021,xx0f) VR=LO VM=1 ? */
    public static final int _0021_xx0f_ = 0x0021000f;

    /** (0021,xx10) VR=IS VM=1 Image Type */
    public static final int ImageType = 0x00210010;

    /** (0021,xx13) VR=CS VM=1 ? */
    public static final int _0021_xx13_ = 0x00210013;

    /** (0021,xx15) VR=US VM=1 ? */
    public static final int _0021_xx15_ = 0x00210015;

    /** (0021,xx20) VR=IS VM=1 Slice Number */
    public static final int SliceNumber = 0x00210020;

    /** (0021,xx21) VR=IS VM=1 Slice Gap */
    public static final int SliceGap = 0x00210021;

    /** (0021,xx30) VR=IS VM=1 Echo Number */
    public static final int EchoNumber = 0x00210030;

    /** (0021,xx31) VR=DS VM=1 Patient Reference ID */
    public static final int PatientReferenceID = 0x00210031;

    /** (0021,xx35) VR=IS VM=1 Chemical Shift Number */
    public static final int ChemicalShiftNumber = 0x00210035;

    /** (0021,xx40) VR=IS VM=1 Phase Number */
    public static final int PhaseNumber = 0x00210040;

    /** (0021,xx50) VR=IS VM=1 Dynamic Scan Number */
    public static final int DynamicScanNumber = 0x00210050;

    /** (0021,xx60) VR=IS VM=1 Number of Rows In Object */
    public static final int NumberOfRowsInObject = 0x00210060;

    /** (0021,xx61) VR=IS VM=1-n Row Number */
    public static final int RowNumber = 0x00210061;

    /** (0021,xx62) VR=IS VM=1-n ? */
    public static final int _0021_xx62_ = 0x00210062;

    /** (0029,xx00) VR=DS VM=2 ? */
    public static final int _0029_xx00_ = 0x00290000;

    /** (0029,xx04) VR=US VM=1 ? */
    public static final int _0029_xx04_ = 0x00290004;

    /** (0029,xx10) VR=DS VM=1 ? */
    public static final int _0029_xx10_ = 0x00290010;

    /** (0029,xx11) VR=DS VM=1 ? */
    public static final int _0029_xx11_ = 0x00290011;

    /** (0029,xx20) VR=LO VM=1 ? */
    public static final int _0029_xx20_ = 0x00290020;

    /** (0029,xx31) VR=DS VM=2 ? */
    public static final int _0029_xx31_ = 0x00290031;

    /** (0029,xx32) VR=DS VM=2 ? */
    public static final int _0029_xx32_ = 0x00290032;

    /** (0029,xx50) VR=DS VM=1 ? */
    public static final int _0029_xx50_ = 0x00290050;

    /** (0029,xx51) VR=DS VM=1 ? */
    public static final int _0029_xx51_ = 0x00290051;

    /** (0029,xx52) VR=DS VM=1 ? */
    public static final int _0029_xx52_ = 0x00290052;

    /** (0029,xx53) VR=DS VM=1 ? */
    public static final int _0029_xx53_ = 0x00290053;

    /** (0029,xxd5) VR=LT VM=1 Slice Thickness */
    public static final int SliceThickness = 0x002900d5;

}
