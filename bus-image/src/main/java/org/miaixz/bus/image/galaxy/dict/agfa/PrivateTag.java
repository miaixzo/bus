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
package org.miaixz.bus.image.galaxy.dict.agfa;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "AGFA";

    /** (0009,xx10) VR=LO VM=1 ? */
    public static final int _0009_xx10_ = 0x00090010;

    /** (0009,xx11) VR=LO VM=1 ? */
    public static final int _0009_xx11_ = 0x00090011;

    /** (0009,xx13) VR=LO VM=1 ? */
    public static final int _0009_xx13_ = 0x00090013;

    /** (0009,xx14) VR=LO VM=1 ? */
    public static final int _0009_xx14_ = 0x00090014;

    /** (0009,xx15) VR=LO VM=1 ? */
    public static final int _0009_xx15_ = 0x00090015;

    /** (0019,xx05) VR=ST VM=1 Cassette Data Stream */
    public static final int CassetteDataStream = 0x00190005;

    /** (0019,xx10) VR=ST VM=1 Image Processing Parameters */
    public static final int ImageProcessingParameters = 0x00190010;

    /** (0019,xx11) VR=LO VM=1 Identification Data */
    public static final int IdentificationData = 0x00190011;

    /** (0019,xx13) VR=LO VM=1 Sensitometry Name */
    public static final int SensitometryName = 0x00190013;

    /** (0019,xx14) VR=ST VM=1 Window Level List */
    public static final int WindowLevelList = 0x00190014;

    /** (0019,xx15) VR=LO VM=1 Dose Monitoring */
    public static final int DoseMonitoring = 0x00190015;

    /** (0019,xx16) VR=LO VM=1 Other Info */
    public static final int OtherInfo = 0x00190016;

    /** (0019,xx1A) VR=LO VM=1 Clipped Exposure Deviation */
    public static final int ClippedExposureDeviation = 0x0019001A;

    /** (0019,xx1B) VR=LO VM=1 Logarithmic PLT Full Scale */
    public static final int LogarithmicPLTFullScale = 0x0019001B;

    /** (0019,xx60) VR=US VM=1 Total Number Series */
    public static final int TotalNumberSeries = 0x00190060;

    /** (0019,xx61) VR=SH VM=1 Session Number */
    public static final int SessionNumber = 0x00190061;

    /** (0019,xx62) VR=SH VM=1 ID Station Name */
    public static final int IDStationName = 0x00190062;

    /** (0019,xx65) VR=US VM=1 Number of Images in Study to be Transmitted */
    public static final int NumberOfImagesInStudyToBeTransmitted = 0x00190065;

    /** (0019,xx70) VR=US VM=1 Total Number Images */
    public static final int TotalNumberImages = 0x00190070;

    /** (0019,xx80) VR=ST VM=1 Geometrical Transformations */
    public static final int GeometricalTransformations = 0x00190080;

    /** (0019,xx81) VR=ST VM=1 Roam Origin */
    public static final int RoamOrigin = 0x00190081;

    /** (0019,xx82) VR=US VM=1 Zoom Factor */
    public static final int ZoomFactor = 0x00190082;

    /** (0019,xx93) VR=CS VM=1 Status */
    public static final int Status = 0x00190093;

}
