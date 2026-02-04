/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
