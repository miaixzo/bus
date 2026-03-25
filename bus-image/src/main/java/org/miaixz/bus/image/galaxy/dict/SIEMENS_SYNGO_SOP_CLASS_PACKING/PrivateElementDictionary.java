/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_SOP_CLASS_PACKING;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateElementDictionary extends ElementDictionary {

    public static final String PrivateCreator = "";

    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

            case PrivateTag.AttributesToSetToZeroLength:
            case PrivateTag.AttributesToRemove:
            case PrivateTag.StartTagOfAStreamChunk:
            case PrivateTag.EndTagOfAStreamChunk:
                return VR.AT;

            case PrivateTag.PackingVersion:
            case PrivateTag.PackingOriginator:
            case PrivateTag.OriginalImageType:
            case PrivateTag.OriginalModality:
            case PrivateTag.StreamChunkIsAPayload:
                return VR.CS;

            case PrivateTag.StreamChunk:
                return VR.OB;

            case PrivateTag.SOPClassPackingSequence:
            case PrivateTag.SequenceOfOriginalStreamChunks:
                return VR.SQ;

            case PrivateTag.OriginalSOPClassUID:
            case PrivateTag.OriginalStudyInstanceUID:
            case PrivateTag.OriginalSeriesInstanceUID:
            case PrivateTag.OriginalSOPInstanceUID:
            case PrivateTag.OriginalTransferSyntaxUID:
                return VR.UI;

            case PrivateTag.OriginalRows:
            case PrivateTag.OriginalColumns:
                return VR.US;
        }
        return VR.UN;
    }

}
