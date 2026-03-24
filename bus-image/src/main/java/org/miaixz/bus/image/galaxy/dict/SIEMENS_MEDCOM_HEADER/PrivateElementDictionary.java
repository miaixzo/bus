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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MEDCOM_HEADER;

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

            case PrivateTag.ReferencedTag:
                return VR.AT;

            case PrivateTag.MedComHeaderType:
            case PrivateTag.PMTFInformation4:
            case PrivateTag.ApplicationHeaderType:
            case PrivateTag.ArchiveManagementFlagKeepOnline:
            case PrivateTag.ArchiveManagementFlagDoNotArchive:
            case PrivateTag.ImageLocationStatus:
            case PrivateTag.ReferencedTagType:
            case PrivateTag.ReferencedObjectDeviceType:
                return VR.CS;

            case PrivateTag.EstimatedRetrieveTime:
            case PrivateTag.DataSizeOfRetrievedImages:
                return VR.DS;

            case PrivateTag.MedComHeaderVersion:
            case PrivateTag.PMTFInformation1:
            case PrivateTag.ApplicationHeaderID:
            case PrivateTag.ApplicationHeaderVersion:
            case PrivateTag.WorkflowControlFlags:
                return VR.LO;

            case PrivateTag.MedComHeaderInfo:
            case PrivateTag.MedComHistoryInformation:
            case PrivateTag.ApplicationHeaderInfo:
            case PrivateTag.ReferencedObjectDeviceLocation:
            case PrivateTag.ReferencedObjectID:
                return VR.OB;

            case PrivateTag.ApplicationHeaderSequence:
            case PrivateTag.SiemensLinkSequence:
                return VR.SQ;

            case PrivateTag.PMTFInformation2:
            case PrivateTag.PMTFInformation3:
            case PrivateTag.PMTFInformation5:
            case PrivateTag.ReferencedValueLength:
            case PrivateTag.ReferencedObjectOffset:
                return VR.UL;
        }
        return VR.UN;
    }

}
