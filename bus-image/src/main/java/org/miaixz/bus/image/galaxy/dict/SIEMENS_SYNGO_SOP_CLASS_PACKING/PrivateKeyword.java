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
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.SOPClassPackingSequence:
            return "SOPClassPackingSequence";
        case PrivateTag.PackingVersion:
            return "PackingVersion";
        case PrivateTag.PackingOriginator:
            return "PackingOriginator";
        case PrivateTag.OriginalSOPClassUID:
            return "OriginalSOPClassUID";
        case PrivateTag.OriginalStudyInstanceUID:
            return "OriginalStudyInstanceUID";
        case PrivateTag.OriginalSeriesInstanceUID:
            return "OriginalSeriesInstanceUID";
        case PrivateTag.OriginalSOPInstanceUID:
            return "OriginalSOPInstanceUID";
        case PrivateTag.OriginalTransferSyntaxUID:
            return "OriginalTransferSyntaxUID";
        case PrivateTag.AttributesToSetToZeroLength:
            return "AttributesToSetToZeroLength";
        case PrivateTag.AttributesToRemove:
            return "AttributesToRemove";
        case PrivateTag.OriginalRows:
            return "OriginalRows";
        case PrivateTag.OriginalColumns:
            return "OriginalColumns";
        case PrivateTag.OriginalImageType:
            return "OriginalImageType";
        case PrivateTag.OriginalModality:
            return "OriginalModality";
        case PrivateTag.SequenceOfOriginalStreamChunks:
            return "SequenceOfOriginalStreamChunks";
        case PrivateTag.StartTagOfAStreamChunk:
            return "StartTagOfAStreamChunk";
        case PrivateTag.EndTagOfAStreamChunk:
            return "EndTagOfAStreamChunk";
        case PrivateTag.StreamChunkIsAPayload:
            return "StreamChunkIsAPayload";
        case PrivateTag.StreamChunk:
            return "StreamChunk";
        }
        return "";
    }

}
