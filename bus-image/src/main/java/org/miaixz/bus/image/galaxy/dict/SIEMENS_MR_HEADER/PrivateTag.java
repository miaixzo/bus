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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_HEADER;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MR HEADER";

    /** (0019,xx08) VR=CS VM=1 ? */
    public static final int _0019_xx08_ = 0x00190008;

    /** (0019,xx09) VR=LO VM=1 ? */
    public static final int _0019_xx09_ = 0x00190009;

    /** (0019,xx0A) VR=US VM=1 Number of Images in Mosaic */
    public static final int NumberOfImagesInMosaic = 0x0019000A;

    /** (0019,xx0B) VR=DS VM=1 Slice Measurement Duration */
    public static final int SliceMeasurementDuration = 0x0019000B;

    /** (0019,xx0C) VR=IS VM=1 B Value */
    public static final int BValue = 0x0019000C;

    /** (0019,xx0D) VR=CS VM=1 Diffusion Directionality */
    public static final int DiffusionDirectionality = 0x0019000D;

    /** (0019,xx0E) VR=FD VM=3 Diffusion Gradient Direction */
    public static final int DiffusionGradientDirection = 0x0019000E;

    /** (0019,xx0F) VR=SH VM=1 Gradient Mode */
    public static final int GradientMode = 0x0019000F;

    /** (0019,xx11) VR=SH VM=1 Flow Compensation */
    public static final int FlowCompensation = 0x00190011;

    /** (0019,xx12) VR=SL VM=3 Table Position Origin */
    public static final int TablePositionOrigin = 0x00190012;

    /** (0019,xx13) VR=SL VM=3 Ima Abs Table Position */
    public static final int ImaAbsTablePosition = 0x00190013;

    /** (0019,xx14) VR=IS VM=3 Ima Rel Table Position */
    public static final int ImaRelTablePosition = 0x00190014;

    /** (0019,xx15) VR=FD VM=3 Slice Position PCS */
    public static final int SlicePosition_PCS = 0x00190015;

    /** (0019,xx16) VR=DS VM=1 Time After Start */
    public static final int TimeAfterStart = 0x00190016;

    /** (0019,xx17) VR=DS VM=1 Slice Resolution */
    public static final int SliceResolution = 0x00190017;

    /** (0019,xx18) VR=IS VM=1 Real Dwell Time */
    public static final int RealDwellTime = 0x00190018;

    /** (0019,xx23) VR=IS VM=1 ? */
    public static final int _0019_xx23_ = 0x00190023;

    /** (0019,xx25) VR=FD VM=1-n ? */
    public static final int _0019_xx25_ = 0x00190025;

    /** (0019,xx26) VR=FD VM=1-n ? */
    public static final int _0019_xx26_ = 0x00190026;

    /** (0019,xx27) VR=FD VM=6 B Matrix */
    public static final int BMatrix = 0x00190027;

    /** (0019,xx28) VR=FD VM=1 Bandwidth per Pixel Phase Encode */
    public static final int BandwidthPerPixelPhaseEncode = 0x00190028;

    /** (0019,xx29) VR=FD VM=1-n Mosaic Ref Acq Times */
    public static final int MosaicRefAcqTimes = 0x00190029;

    /** (0051,xx08) VR=CS VM=1 CSA Image Header Type */
    public static final int CSAImageHeaderType = 0x00510008;

    /** (0051,xx09) VR=LO VM=1 CSA Image Header Version */
    public static final int CSAImageHeaderVersion = 0x00510009;

    /** (0051,xx0A) VR=LO VM=1 ? */
    public static final int _0051_xx0A_ = 0x0051000A;

    /** (0051,xx0B) VR=SH VM=1 Acquisition Matrix Text */
    public static final int AcquisitionMatrixText = 0x0051000B;

    /** (0051,xx0C) VR=LO VM=1 ? */
    public static final int _0051_xx0C_ = 0x0051000C;

    /** (0051,xx0D) VR=SH VM=1 ? */
    public static final int _0051_xx0D_ = 0x0051000D;

    /** (0051,xx0E) VR=LO VM=1 ? */
    public static final int _0051_xx0E_ = 0x0051000E;

    /** (0051,xx0F) VR=LO VM=1 Coil String */
    public static final int CoilString = 0x0051000F;

    /** (0051,xx11) VR=LO VM=1 ? */
    public static final int _0051_xx11_ = 0x00510011;

    /** (0051,xx12) VR=SH VM=1 ? */
    public static final int _0051_xx12_ = 0x00510012;

    /** (0051,xx13) VR=SH VM=1 Positive PCS Directions */
    public static final int PositivePCSDirections = 0x00510013;

    /** (0051,xx15) VR=SH VM=1 ? */
    public static final int _0051_xx15_ = 0x00510015;

    /** (0051,xx16) VR=LO VM=1 ? */
    public static final int _0051_xx16_ = 0x00510016;

    /** (0051,xx17) VR=SH VM=1 ? */
    public static final int _0051_xx17_ = 0x00510017;

    /** (0051,xx18) VR=SH VM=1 ? */
    public static final int _0051_xx18_ = 0x00510018;

    /** (0051,xx19) VR=LO VM=1 ? */
    public static final int _0051_xx19_ = 0x00510019;

}
