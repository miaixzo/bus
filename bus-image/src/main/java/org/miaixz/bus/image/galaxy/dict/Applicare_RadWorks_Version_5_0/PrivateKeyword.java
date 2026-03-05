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
package org.miaixz.bus.image.galaxy.dict.Applicare_RadWorks_Version_5_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.WorklistFilename:
                return "WorklistFilename";

            case PrivateTag.NewSeenStatus:
                return "NewSeenStatus";

            case PrivateTag.DeleteLock:
                return "DeleteLock";

            case PrivateTag._3109_xx04_:
                return "_3109_xx04_";

            case PrivateTag._3109_xx05_:
                return "_3109_xx05_";

            case PrivateTag._3109_xx06_:
                return "_3109_xx06_";

            case PrivateTag._3109_xx07_:
                return "_3109_xx07_";

            case PrivateTag.ReceiveOrigin:
                return "ReceiveOrigin";

            case PrivateTag.Folder:
                return "Folder";

            case PrivateTag.ReceiveDate:
                return "ReceiveDate";

            case PrivateTag.ReceiveTime:
                return "ReceiveTime";

            case PrivateTag.Prior:
                return "Prior";

            case PrivateTag.StatStudy:
                return "StatStudy";

            case PrivateTag.Key:
                return "Key";

            case PrivateTag.LocalStudy:
                return "LocalStudy";

            case PrivateTag.ResultMessage:
                return "ResultMessage";

            case PrivateTag.CurrentUser:
                return "CurrentUser";

            case PrivateTag.SystemDate:
                return "SystemDate";

            case PrivateTag.SystemTime:
                return "SystemTime";

            case PrivateTag.WorklistName:
                return "WorklistName";

            case PrivateTag.WorklistUID:
                return "WorklistUID";

            case PrivateTag.Hostname:
                return "Hostname";

            case PrivateTag.DICOMAETitle:
                return "DICOMAETitle";

            case PrivateTag.DICOMPortNumber:
                return "DICOMPortNumber";

            case PrivateTag.DestinationName:
                return "DestinationName";

            case PrivateTag.OriginName:
                return "OriginName";

            case PrivateTag.ModalityStudyInstanceUID:
                return "ModalityStudyInstanceUID";

            case PrivateTag.ExamRouting:
                return "ExamRouting";

            case PrivateTag.NotificationComments:
                return "NotificationComments";

            case PrivateTag.TransactionComments:
                return "TransactionComments";

            case PrivateTag.SendFlag:
                return "SendFlag";

            case PrivateTag.PrintFlag:
                return "PrintFlag";

            case PrivateTag.ArchiveFlag:
                return "ArchiveFlag";

            case PrivateTag.RequestingFacilityName:
                return "RequestingFacilityName";

            case PrivateTag.RequestingProcedureName:
                return "RequestingProcedureName";

            case PrivateTag.RequestingProcedureCode:
                return "RequestingProcedureCode";

            case PrivateTag.RequestStorageCommitment:
                return "RequestStorageCommitment";

            case PrivateTag.RequestedCompression:
                return "RequestedCompression";

            case PrivateTag.StudySequence:
                return "StudySequence";

            case PrivateTag.ReplacedStudyUID:
                return "ReplacedStudyUID";

            case PrivateTag.TeachingACRCode:
                return "TeachingACRCode";

            case PrivateTag.TeachingSpecialInterestCode:
                return "TeachingSpecialInterestCode";

            case PrivateTag.NumberOfStudyRelatedImages:
                return "NumberOfStudyRelatedImages";

            case PrivateTag.StudyLocked:
                return "StudyLocked";

            case PrivateTag.WorkstationName:
                return "WorkstationName";

            case PrivateTag.ArchiveStatus:
                return "ArchiveStatus";

            case PrivateTag.InternalListUID:
                return "InternalListUID";

            case PrivateTag.Action:
                return "Action";
        }
        return "";
    }

}
