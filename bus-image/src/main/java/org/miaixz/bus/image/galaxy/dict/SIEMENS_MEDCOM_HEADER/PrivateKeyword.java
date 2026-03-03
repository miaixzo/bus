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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.MedComHeaderType:
                return "MedComHeaderType";

            case PrivateTag.MedComHeaderVersion:
                return "MedComHeaderVersion";

            case PrivateTag.MedComHeaderInfo:
                return "MedComHeaderInfo";

            case PrivateTag.MedComHistoryInformation:
                return "MedComHistoryInformation";

            case PrivateTag.PMTFInformation1:
                return "PMTFInformation1";

            case PrivateTag.PMTFInformation2:
                return "PMTFInformation2";

            case PrivateTag.PMTFInformation3:
                return "PMTFInformation3";

            case PrivateTag.PMTFInformation4:
                return "PMTFInformation4";

            case PrivateTag.PMTFInformation5:
                return "PMTFInformation5";

            case PrivateTag.ApplicationHeaderSequence:
                return "ApplicationHeaderSequence";

            case PrivateTag.ApplicationHeaderType:
                return "ApplicationHeaderType";

            case PrivateTag.ApplicationHeaderID:
                return "ApplicationHeaderID";

            case PrivateTag.ApplicationHeaderVersion:
                return "ApplicationHeaderVersion";

            case PrivateTag.ApplicationHeaderInfo:
                return "ApplicationHeaderInfo";

            case PrivateTag.WorkflowControlFlags:
                return "WorkflowControlFlags";

            case PrivateTag.ArchiveManagementFlagKeepOnline:
                return "ArchiveManagementFlagKeepOnline";

            case PrivateTag.ArchiveManagementFlagDoNotArchive:
                return "ArchiveManagementFlagDoNotArchive";

            case PrivateTag.ImageLocationStatus:
                return "ImageLocationStatus";

            case PrivateTag.EstimatedRetrieveTime:
                return "EstimatedRetrieveTime";

            case PrivateTag.DataSizeOfRetrievedImages:
                return "DataSizeOfRetrievedImages";

            case PrivateTag.SiemensLinkSequence:
                return "SiemensLinkSequence";

            case PrivateTag.ReferencedTag:
                return "ReferencedTag";

            case PrivateTag.ReferencedTagType:
                return "ReferencedTagType";

            case PrivateTag.ReferencedValueLength:
                return "ReferencedValueLength";

            case PrivateTag.ReferencedObjectDeviceType:
                return "ReferencedObjectDeviceType";

            case PrivateTag.ReferencedObjectDeviceLocation:
                return "ReferencedObjectDeviceLocation";

            case PrivateTag.ReferencedObjectID:
                return "ReferencedObjectID";

            case PrivateTag.ReferencedObjectOffset:
                return "ReferencedObjectOffset";
        }
        return "";
    }

}
