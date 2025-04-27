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
package org.miaixz.bus.image.galaxy.dict.SIENET;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIENET";

    /** (0009,xx01) VR=US VM=1 SIENET Command Field */
    public static final int SIENETCommandField = 0x00090001;

    /** (0009,xx14) VR=LO VM=1 Receiver PLA */
    public static final int ReceiverPLA = 0x00090014;

    /** (0009,xx16) VR=US VM=1 Transfer Priority */
    public static final int TransferPriority = 0x00090016;

    /** (0009,xx29) VR=LO VM=1 Actual User */
    public static final int ActualUser = 0x00090029;

    /** (0009,xx70) VR=DS VM=1 ? */
    public static final int _0009_xx70_ = 0x00090070;

    /** (0009,xx71) VR=DS VM=1 ? */
    public static final int _0009_xx71_ = 0x00090071;

    /** (0009,xx72) VR=LO VM=1 ? */
    public static final int _0009_xx72_ = 0x00090072;

    /** (0009,xx73) VR=LO VM=1 ? */
    public static final int _0009_xx73_ = 0x00090073;

    /** (0009,xx74) VR=LO VM=1 ? */
    public static final int _0009_xx74_ = 0x00090074;

    /** (0009,xx75) VR=LO VM=1 ? */
    public static final int _0009_xx75_ = 0x00090075;

    /** (0091,xx20) VR=PN VM=1 RIS Patient Name */
    public static final int RISPatientName = 0x00910020;

    /** (0093,xx02) VR=LO VM=1 ? */
    public static final int _0093_xx02_ = 0x00930002;

    /** (0095,xx01) VR=LO VM=1 Examination Folder ID */
    public static final int ExaminationFolderID = 0x00950001;

    /** (0095,xx04) VR=UL VM=1 Folder Reported Status */
    public static final int FolderReportedStatus = 0x00950004;

    /** (0095,xx05) VR=LO VM=1 Folder Reporting Radiologist */
    public static final int FolderReportingRadiologist = 0x00950005;

    /** (0095,xx07) VR=LO VM=1 SIENET ISA PLA */
    public static final int SIENETISAPLA = 0x00950007;

    /** (0095,xx0C) VR=SL VM=1 ? */
    public static final int _0095_xx0C_ = 0x0095000C;

    /** (0097,xx03) VR=SL VM=1 ? */
    public static final int _0097_xx03_ = 0x00970003;

    /** (0097,xx05) VR=LO VM=1 ? */
    public static final int _0097_xx05_ = 0x00970005;

    /** (0099,xx02) VR=SL VM=1 Data Object Attributes */
    public static final int DataObjectAttributes = 0x00990002;

    /** (0099,xx05) VR=SL VM=1 ? */
    public static final int _0099_xx05_ = 0x00990005;

    /** (00A5,xx05) VR=LO VM=1 ? */
    public static final int _00A5_xx05_ = 0x00A50005;

}
