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
package org.miaixz.bus.image.galaxy.dict.QUASAR_INTERNAL_USE;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "QUASAR_INTERNAL_USE";

    /** (0009,xx01) VR=UL VM=1-n Rate Vector */
    public static final int RateVector = 0x00090001;

    /** (0009,xx02) VR=UL VM=1-n Count Vector */
    public static final int CountVector = 0x00090002;

    /** (0009,xx03) VR=UL VM=1-n Time Vector */
    public static final int TimeVector = 0x00090003;

    /** (0009,xx07) VR=UL VM=1-n Angle Vector */
    public static final int AngleVector = 0x00090007;

    /** (0009,xx08) VR=US VM=1 Camera Shape */
    public static final int CameraShape = 0x00090008;

    /** (0009,xx10) VR=US VM=1 Whole Body Spots */
    public static final int WholeBodySpots = 0x00090010;

    /** (0009,xx11) VR=US VM=1 Worklist Flag */
    public static final int WorklistFlag = 0x00090011;

    /** (0009,xx12) VR=LO VM=1 ? */
    public static final int _0009_xx12_ = 0x00090012;

    /** (0009,xx13) VR=ST VM=1 Sequence Type */
    public static final int SequenceType = 0x00090013;

    /** (0009,xx14) VR=ST VM=1 Sequence Name */
    public static final int SequenceName = 0x00090014;

    /** (0009,xx15) VR=UL VM=1-n Average RR Time Vector */
    public static final int AverageRRTimeVector = 0x00090015;

    /** (0009,xx16) VR=UL VM=1-n Low Limit Vector */
    public static final int LowLimitVector = 0x00090016;

    /** (0009,xx17) VR=UL VM=1-n High Limit Vector */
    public static final int HighLimitVector = 0x00090017;

    /** (0009,xx18) VR=UL VM=1-n Begin Index Vector */
    public static final int BeginIndexVector = 0x00090018;

    /** (0009,xx19) VR=UL VM=1-n End Index Vector */
    public static final int EndIndexVector = 0x00090019;

    /** (0009,xx1A) VR=UL VM=1-n Raw Time Vector */
    public static final int RawTimeVector = 0x0009001A;

    /** (0009,xx1B) VR=LO VM=1 Image Type String */
    public static final int ImageTypeString = 0x0009001B;

    /** (0009,xx1D) VR=US VM=1 ? */
    public static final int _0009_xx1D_ = 0x0009001D;

    /** (0009,xx1E) VR=ST VM=1 ? */
    public static final int _0009_xx1E_ = 0x0009001E;

    /** (0009,xx22) VR=FL VM=1 ? */
    public static final int _0009_xx22_ = 0x00090022;

    /** (0009,xx23) VR=US VM=1 ? */
    public static final int _0009_xx23_ = 0x00090023;

    /** (0009,xx39) VR=UI VM=1 ? */
    public static final int _0009_xx39_ = 0x00090039;

    /** (0009,xx40) VR=DA VM=1 ? */
    public static final int _0009_xx40_ = 0x00090040;

    /** (0009,xx41) VR=TM VM=1 ? */
    public static final int _0009_xx41_ = 0x00090041;

    /** (0009,xx42) VR=LO VM=1 ? */
    public static final int _0009_xx42_ = 0x00090042;

    /** (0009,xx44) VR=SH VM=1 ? */
    public static final int _0009_xx44_ = 0x00090044;

    /** (0009,xx45) VR=LO VM=1 ? */
    public static final int _0009_xx45_ = 0x00090045;

    /** (0009,xx48) VR=LO VM=1 ? */
    public static final int _0009_xx48_ = 0x00090048;

    /** (0037,xx10) VR=SQ VM=1 ? */
    public static final int _0037_xx10_ = 0x00370010;

    /** (0037,xx1B) VR=LO VM=1 ? */
    public static final int _0037_xx1B_ = 0x0037001B;

    /** (0037,xx30) VR=LO VM=1 ? */
    public static final int _0037_xx30_ = 0x00370030;

    /** (0037,xx40) VR=LO VM=1 ? */
    public static final int _0037_xx40_ = 0x00370040;

    /** (0037,xx50) VR=LO VM=1 ? */
    public static final int _0037_xx50_ = 0x00370050;

    /** (0037,xx60) VR=LO VM=1 ? */
    public static final int _0037_xx60_ = 0x00370060;

    /** (0037,xx70) VR=LO VM=1 ? */
    public static final int _0037_xx70_ = 0x00370070;

    /** (0037,xx71) VR=FD VM=1 ? */
    public static final int _0037_xx71_ = 0x00370071;

    /** (0037,xx72) VR=SH VM=1 ? */
    public static final int _0037_xx72_ = 0x00370072;

    /** (0037,xx73) VR=FD VM=1 ? */
    public static final int _0037_xx73_ = 0x00370073;

    /** (0037,xx78) VR=FD VM=1 ? */
    public static final int _0037_xx78_ = 0x00370078;

    /** (0037,xx90) VR=IS VM=1 ? */
    public static final int _0037_xx90_ = 0x00370090;

    /** (0037,xx92) VR=DS VM=1 ? */
    public static final int _0037_xx92_ = 0x00370092;

    /** (0041,xx01) VR=UT VM=1 ? */
    public static final int _0041_xx01_ = 0x00410001;

}
