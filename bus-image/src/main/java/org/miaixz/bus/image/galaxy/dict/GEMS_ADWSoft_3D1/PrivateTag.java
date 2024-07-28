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
package org.miaixz.bus.image.galaxy.dict.GEMS_ADWSoft_3D1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_ADWSoft_3D1";

    /** (0047,xx01) VR=SQ VM=1 Reconstruction Parameters Sequence */
    public static final int ReconstructionParametersSequence = 0x00470001;

    /** (0047,xx50) VR=UL VM=1 Volume Voxel Count */
    public static final int VolumeVoxelCount = 0x00470050;

    /** (0047,xx51) VR=UL VM=1 Volume Segment Count */
    public static final int VolumeSegmentCount = 0x00470051;

    /** (0047,xx53) VR=US VM=1 Volume Slice Size */
    public static final int VolumeSliceSize = 0x00470053;

    /** (0047,xx54) VR=US VM=1 Volume Slice Count */
    public static final int VolumeSliceCount = 0x00470054;

    /** (0047,xx55) VR=SL VM=1 Volume Threshold Value */
    public static final int VolumeThresholdValue = 0x00470055;

    /** (0047,xx57) VR=DS VM=1 Volume Voxel Ratio */
    public static final int VolumeVoxelRatio = 0x00470057;

    /** (0047,xx58) VR=DS VM=1 Volume Voxel Size */
    public static final int VolumeVoxelSize = 0x00470058;

    /** (0047,xx59) VR=US VM=1 Volume Z Position Size */
    public static final int VolumeZPositionSize = 0x00470059;

    /** (0047,xx60) VR=DS VM=9 Volume Base Line */
    public static final int VolumeBaseLine = 0x00470060;

    /** (0047,xx61) VR=DS VM=3 Volume Center Point */
    public static final int VolumeCenterPoint = 0x00470061;

    /** (0047,xx63) VR=SL VM=1 Volume Skew Base */
    public static final int VolumeSkewBase = 0x00470063;

    /** (0047,xx64) VR=DS VM=9 Volume Registration Transform Rotation Matrix */
    public static final int VolumeRegistrationTransformRotationMatrix = 0x00470064;

    /** (0047,xx65) VR=DS VM=3 Volume Registration Transform Translation Vector */
    public static final int VolumeRegistrationTransformTranslationVector = 0x00470065;

    /** (0047,xx70) VR=DS VM=1-n KVP List */
    public static final int KVPList = 0x00470070;

    /** (0047,xx71) VR=IS VM=1-n XRay Tube Current List */
    public static final int XRayTubeCurrentList = 0x00470071;

    /** (0047,xx72) VR=IS VM=1-n Exposure List */
    public static final int ExposureList = 0x00470072;

    /** (0047,xx80) VR=LO VM=1 Acquisition DLX Identifier */
    public static final int AcquisitionDLXIdentifier = 0x00470080;

    /** (0047,xx85) VR=SQ VM=1 Acquisition DLX 2D Series Sequence */
    public static final int AcquisitionDLX2DSeriesSequence = 0x00470085;

    /** (0047,xx89) VR=DS VM=1-n Contrast Agent Volume List */
    public static final int ContrastAgentVolumeList = 0x00470089;

    /** (0047,xx8A) VR=US VM=1 Number Of Injections */
    public static final int NumberOfInjections = 0x0047008A;

    /** (0047,xx8B) VR=US VM=1 Frame Count */
    public static final int FrameCount = 0x0047008B;

    /** (0047,xx91) VR=LO VM=1 XA 3D Reconstruction Algorithm Name */
    public static final int XA3DReconstructionAlgorithmName = 0x00470091;

    /** (0047,xx92) VR=CS VM=1 XA 3D Reconstruction Algorithm Version */
    public static final int XA3DReconstructionAlgorithmVersion = 0x00470092;

    /** (0047,xx93) VR=DA VM=1 DLX Calibration Date */
    public static final int DLXCalibrationDate = 0x00470093;

    /** (0047,xx94) VR=TM VM=1 DLX Calibration Time */
    public static final int DLXCalibrationTime = 0x00470094;

    /** (0047,xx95) VR=CS VM=1 DLX Calibration Status */
    public static final int DLXCalibrationStatus = 0x00470095;

    /** (0047,xx96) VR=IS VM=1-n Used Frames */
    public static final int UsedFrames = 0x00470096;

    /** (0047,xx98) VR=US VM=1 Transform Count */
    public static final int TransformCount = 0x00470098;

    /** (0047,xx99) VR=SQ VM=1 Transform Sequence */
    public static final int TransformSequence = 0x00470099;

    /** (0047,xx9A) VR=DS VM=9 Transform Rotation Matrix */
    public static final int TransformRotationMatrix = 0x0047009A;

    /** (0047,xx9B) VR=DS VM=3 Transform Translation Vector */
    public static final int TransformTranslationVector = 0x0047009B;

    /** (0047,xx9C) VR=LO VM=1 Transform Label */
    public static final int TransformLabel = 0x0047009C;

    /** (0047,xxB0) VR=SQ VM=1 Wireframe List */
    public static final int WireframeList = 0x004700B0;

    /** (0047,xxB1) VR=US VM=1 Wireframe Count */
    public static final int WireframeCount = 0x004700B1;

    /** (0047,xxB2) VR=US VM=1 Location System */
    public static final int LocationSystem = 0x004700B2;

    /** (0047,xxB5) VR=LO VM=1 Wireframe Name */
    public static final int WireframeName = 0x004700B5;

    /** (0047,xxB6) VR=LO VM=1 Wireframe Group Name */
    public static final int WireframeGroupName = 0x004700B6;

    /** (0047,xxB7) VR=LO VM=1 Wireframe Color */
    public static final int WireframeColor = 0x004700B7;

    /** (0047,xxB8) VR=SL VM=1 Wireframe Attributes */
    public static final int WireframeAttributes = 0x004700B8;

    /** (0047,xxB9) VR=SL VM=1 Wireframe Point Count */
    public static final int WireframePointCount = 0x004700B9;

    /** (0047,xxBA) VR=SL VM=1 Wireframe Timestamp */
    public static final int WireframeTimestamp = 0x004700BA;

    /** (0047,xxBB) VR=SQ VM=1 Wireframe Point List */
    public static final int WireframePointList = 0x004700BB;

    /** (0047,xxBC) VR=DS VM=3 Wireframe Points Coordinates */
    public static final int WireframePointsCoordinates = 0x004700BC;

    /** (0047,xxC0) VR=DS VM=3 Volume Upper Left High Corner RAS */
    public static final int VolumeUpperLeftHighCornerRAS = 0x004700C0;

    /** (0047,xxC1) VR=DS VM=9 Volume Slice To RAS Rotation Matrix */
    public static final int VolumeSliceToRASRotationMatrix = 0x004700C1;

    /** (0047,xxC2) VR=DS VM=1 Volume Upper Left High Corner TLOC */
    public static final int VolumeUpperLeftHighCornerTLOC = 0x004700C2;

    /** (0047,xxD1) VR=OB VM=1 Volume Segment List */
    public static final int VolumeSegmentList = 0x004700D1;

    /** (0047,xxD2) VR=OB VM=1 Volume Gradient List */
    public static final int VolumeGradientList = 0x004700D2;

    /** (0047,xxD3) VR=OB VM=1 Volume Density List */
    public static final int VolumeDensityList = 0x004700D3;

    /** (0047,xxD4) VR=OB VM=1 Volume Z Position List */
    public static final int VolumeZPositionList = 0x004700D4;

    /** (0047,xxD5) VR=OB VM=1 Volume Original Index List */
    public static final int VolumeOriginalIndexList = 0x004700D5;

}
