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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MEDCOM_HEADER;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MEDCOM HEADER";

    /** (0029,xx08) VR=CS VM=1 MedCom Header Type */
    public static final int MedComHeaderType = 0x00290008;

    /** (0029,xx09) VR=LO VM=1 MedCom Header Version */
    public static final int MedComHeaderVersion = 0x00290009;

    /** (0029,xx10) VR=OB VM=1 MedCom Header Info */
    public static final int MedComHeaderInfo = 0x00290010;

    /** (0029,xx20) VR=OB VM=1 MedCom History Information */
    public static final int MedComHistoryInformation = 0x00290020;

    /** (0029,xx31) VR=LO VM=1 PMTF Information 1 */
    public static final int PMTFInformation1 = 0x00290031;

    /** (0029,xx32) VR=UL VM=1 PMTF Information 2 */
    public static final int PMTFInformation2 = 0x00290032;

    /** (0029,xx33) VR=UL VM=1 PMTF Information 3 */
    public static final int PMTFInformation3 = 0x00290033;

    /** (0029,xx34) VR=CS VM=1 PMTF Information 4 */
    public static final int PMTFInformation4 = 0x00290034;

    /** (0029,xx35) VR=UL VM=1 PMTF Information 5 */
    public static final int PMTFInformation5 = 0x00290035;

    /** (0029,xx40) VR=SQ VM=1 Application Header Sequence */
    public static final int ApplicationHeaderSequence = 0x00290040;

    /** (0029,xx41) VR=CS VM=1 Application Header Type */
    public static final int ApplicationHeaderType = 0x00290041;

    /** (0029,xx42) VR=LO VM=1 Application Header ID */
    public static final int ApplicationHeaderID = 0x00290042;

    /** (0029,xx43) VR=LO VM=1 Application Header Version */
    public static final int ApplicationHeaderVersion = 0x00290043;

    /** (0029,xx44) VR=OB VM=1 Application Header Info */
    public static final int ApplicationHeaderInfo = 0x00290044;

    /** (0029,xx50) VR=LO VM=8 Workflow Control Flags */
    public static final int WorkflowControlFlags = 0x00290050;

    /** (0029,xx51) VR=CS VM=1 Archive Management Flag Keep Online */
    public static final int ArchiveManagementFlagKeepOnline = 0x00290051;

    /**
     * (0029,xx52) VR=CS VM=1 Archive Management Flag Do Not Archive
     */
    public static final int ArchiveManagementFlagDoNotArchive = 0x00290052;

    /** (0029,xx53) VR=CS VM=1 Image Location Status */
    public static final int ImageLocationStatus = 0x00290053;

    /** (0029,xx54) VR=DS VM=1 Estimated Retrieve Time */
    public static final int EstimatedRetrieveTime = 0x00290054;

    /** (0029,xx55) VR=DS VM=1 Data Size of Retrieved Images */
    public static final int DataSizeOfRetrievedImages = 0x00290055;

    /** (0029,xx70) VR=SQ VM=1 Siemens Link Sequenc */
    public static final int SiemensLinkSequence = 0x00290070;

    /** (0029,xx71) VR=AT VM=1 Referenced Ta */
    public static final int ReferencedTag = 0x00290071;

    /** (0029,xx72) VR=CS VM=1 Referenced Tag Typ */
    public static final int ReferencedTagType = 0x00290072;

    /** (0029,xx73) VR=UL VM=1 Referenced Value Lengt */
    public static final int ReferencedValueLength = 0x00290073;

    /** (0029,xx74) VR=CS VM=1 Referenced Object Device Typ */
    public static final int ReferencedObjectDeviceType = 0x00290074;

    /** (0029,xx75) VR=OB VM=1 Referenced Object Device Locatio */
    public static final int ReferencedObjectDeviceLocation = 0x00290075;

    /** (0029,xx76) VR=OB VM=1 Referenced Object I */
    public static final int ReferencedObjectID = 0x00290076;

    /** (0029,xx77) VR=UL VM=1 Referenced Object Offse */
    public static final int ReferencedObjectOffset = 0x00290077;

}
