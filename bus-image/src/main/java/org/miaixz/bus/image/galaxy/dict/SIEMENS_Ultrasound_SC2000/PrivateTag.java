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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_Ultrasound_SC2000;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS Ultrasound SC2000";

    /** (0019,xx2D) VR=US VM=1 B-mode Tint Index */
    public static final int BModeTintIndex = 0x0019002D;

    /** (0019,xx72) VR=US VM=1 Doppler Tint Index */
    public static final int DopplerTintIndex = 0x00190072;

    /** (0019,xx88) VR=US VM=1 M-mode Tint Index */
    public static final int MModeTintIndex = 0x00190088;

    /** (0019,xx89) VR=LO VM=1 ? */
    public static final int _0019_xx89_ = 0x00190089;

    /** (0119,xx00) VR=LO VM=1 Acoustic Meta Information Version */
    public static final int AcousticMetaInformationVersion = 0x01190000;

    /** (0119,xx01) VR=OB VM=1 Common Acoustic Meta Information */
    public static final int CommonAcousticMetaInformation = 0x01190001;

    /** (0119,xx02) VR=SQ VM=1 Multi Stream Sequence */
    public static final int MultiStreamSequence = 0x01190002;

    /** (0119,xx03) VR=SQ VM=1 Acoustic Data Sequence */
    public static final int AcousticDataSequence = 0x01190003;

    /** (0119,xx04) VR=OB VM=1 Per Transaction Acoustic Control Informatio */
    public static final int PerTransactionAcousticControlInformation = 0x01190004;

    /** (0119,xx05) VR=UL VM=1 Acoustic Data Offset */
    public static final int AcousticDataOffset = 0x01190005;

    /** (0119,xx06) VR=UL VM=1 Acoustic Data Length */
    public static final int AcousticDataLength = 0x01190006;

    /** (0119,xx07) VR=UL VM=1 Footer Offset */
    public static final int FooterOffset = 0x01190007;

    /** (0119,xx08) VR=UL VM=1 Footer Length */
    public static final int FooterLength = 0x01190008;

    /** (0119,xx09) VR=SS VM=1 Acoustic Stream Number */
    public static final int AcousticStreamNumber = 0x01190009;

    /** (0119,xx10) VR=SH VM=1 Acoustic Stream Type */
    public static final int AcousticStreamType = 0x01190010;

    /** (0119,xx11) VR=UN VM=1 Stage Timer Time */
    public static final int StageTimerTime = 0x01190011;

    /** (0119,xx12) VR=UN VM=1 Stop Watch Time */
    public static final int StopWatchTime = 0x01190012;

    /** (0119,xx13) VR=IS VM=1 Volume Rate */
    public static final int VolumeRate = 0x01190013;

    /** (0119,xx21) VR=SH VM=1 ? */
    public static final int _0119_xx21_ = 0x01190021;

    /** (0129,xx00) VR=SQ VM=1 MPR View Sequence */
    public static final int MPRViewSequence = 0x01290000;

    /** (0129,xx02) VR=UI VM=1 Bookmark UID */
    public static final int BookmarkUID = 0x01290002;

    /** (0129,xx03) VR=UN VM=1 Plane Origin Vector */
    public static final int PlaneOriginVector = 0x01290003;

    /** (0129,xx04) VR=UN VM=1 Row Vector */
    public static final int RowVector = 0x01290004;

    /** (0129,xx05) VR=UN VM=1 Column Vector */
    public static final int ColumnVector = 0x01290005;

    /** (0129,xx06) VR=SQ VM=1 Visualization Sequence */
    public static final int VisualizationSequence = 0x01290006;

    /** (0129,xx08) VR=OB VM=1 Visualization Informatio */
    public static final int VisualizationInformation = 0x01290008;

    /** (0129,xx09) VR=SQ VM=1 Application State Sequence */
    public static final int ApplicationStateSequence = 0x01290009;

    /** (0129,xx10) VR=OB VM=1 Application State Informatio */
    public static final int ApplicationStateInformation = 0x01290010;

    /** (0129,xx11) VR=SQ VM=1 Referenced Bookmark Sequence */
    public static final int ReferencedBookmarkSequence = 0x01290011;

    /** (0129,xx12) VR=UI VM=1 Referenced Bookmark UID */
    public static final int ReferencedBookmarkUID = 0x01290012;

    /** (0129,xx20) VR=SQ VM=1 Cine Parameters Sequence */
    public static final int CineParametersSequence = 0x01290020;

    /** (0129,xx21) VR=OB VM=1 Cine Parameters Schem */
    public static final int CineParametersSchema = 0x01290021;

    /** (0129,xx22) VR=OB VM=1 Values of Cine Parameters */
    public static final int ValuesOfCineParameters = 0x01290022;

    /** (0129,xx29) VR=OB VM=1 */
    public static final int _0129_xx29_ = 0x01290029;

    /** (0129,xx30) VR=CS VM=1 Raw Data Object Type */
    public static final int RawDataObjectType = 0x01290030;

    /** (0139,xx01) VR=SL VM=1 Physio Capture ROI */
    public static final int PhysioCaptureROI = 0x01390001;

    /** (0149,xx01) VR=FD VM=1-n Vector of BROI Points */
    public static final int VectorOfBROIPoints = 0x01490001;

    /** (0149,xx02) VR=FD VM=1-n Start/End Timestamps of Strip Stream */
    public static final int StartEndTimestampsOfStripStream = 0x01490002;

    /** (0149,xx03) VR=FD VM=1-n Timestamps of Visible R-waves */
    public static final int TimestampsOfVisibleRWaves = 0x01490003;

    /** (7FD1,xx01) VR=OB VM=1 Acoustic Image and Footer Data */
    public static final int AcousticImageAndFooterData = 0x7FD10001;

    /** (7FD1,xx09) VR=UI VM=1 Volume Version ID */
    public static final int VolumeVersionID = 0x7FD10009;

    /** (7FD1,xx10) VR=OB VM=1 Volume Payload */
    public static final int VolumePayload = 0x7FD10010;

    /** (7FD1,xx11) VR=OB VM=1 After Payload */
    public static final int AfterPayload = 0x7FD10011;

}
