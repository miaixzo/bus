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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_SOP_CLASS_PACKING;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS SYNGO SOP CLASS PACKING";

    /** (0031,xx10) VR=SQ VM=1 SOP Class Packing Sequence */
    public static final int SOPClassPackingSequence = 0x00310010;

    /** (0031,xx20) VR=CS VM=1 Packing Version */
    public static final int PackingVersion = 0x00310020;

    /** (0031,xx21) VR=CS VM=1 Packing Originator */
    public static final int PackingOriginator = 0x00310021;

    /** (0031,xx30) VR=UI VM=1 Original SOP Class UID */
    public static final int OriginalSOPClassUID = 0x00310030;

    /** (0031,xx31) VR=UI VM=1 Original Study Instance UID */
    public static final int OriginalStudyInstanceUID = 0x00310031;

    /** (0031,xx32) VR=UI VM=1 Original Series Instance UID */
    public static final int OriginalSeriesInstanceUID = 0x00310032;

    /** (0031,xx33) VR=UI VM=1 Original SOP Instance UID */
    public static final int OriginalSOPInstanceUID = 0x00310033;

    /** (0031,xx34) VR=UI VM=1 Original Transfer Syntax UID */
    public static final int OriginalTransferSyntaxUID = 0x00310034;

    /** (0031,xx40) VR=AT VM=1-n Attributes to Set to Zero Length */
    public static final int AttributesToSetToZeroLength = 0x00310040;

    /** (0031,xx41) VR=AT VM=1-n Attributes to Remove */
    public static final int AttributesToRemove = 0x00310041;

    /** (0031,xx50) VR=US VM=1 Original Rows */
    public static final int OriginalRows = 0x00310050;

    /** (0031,xx51) VR=US VM=1 Original Columns */
    public static final int OriginalColumns = 0x00310051;

    /** (0031,xx58) VR=CS VM=2-n Original Image Type */
    public static final int OriginalImageType = 0x00310058;

    /** (0031,xx60) VR=CS VM=1 Original Modality */
    public static final int OriginalModality = 0x00310060;

    /** (0031,xx70) VR=SQ VM=1 Sequence of Original StreamCchunks */
    public static final int SequenceOfOriginalStreamChunks = 0x00310070;

    /** (0031,xx71) VR=AT VM=1 Start Tag of a Stream Chunk */
    public static final int StartTagOfAStreamChunk = 0x00310071;

    /** (0031,xx72) VR=AT VM=1 End Tag of a Stream Chunk */
    public static final int EndTagOfAStreamChunk = 0x00310072;

    /** (0031,xx73) VR=CS VM=1 Stream Chunk is a Payload */
    public static final int StreamChunkIsAPayload = 0x00310073;

    /** (0031,xx80) VR=OB VM=1 Stream Chunk */
    public static final int StreamChunk = 0x00310080;

}
