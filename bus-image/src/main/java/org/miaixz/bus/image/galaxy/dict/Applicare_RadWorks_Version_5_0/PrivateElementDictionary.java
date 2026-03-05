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

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
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

            case PrivateTag.DICOMAETitle:
                return VR.AE;

            case PrivateTag.DeleteLock:
            case PrivateTag._3109_xx04_:
            case PrivateTag._3109_xx05_:
            case PrivateTag._3109_xx06_:
            case PrivateTag.Prior:
            case PrivateTag.StatStudy:
            case PrivateTag.Key:
            case PrivateTag.LocalStudy:
            case PrivateTag.Hostname:
            case PrivateTag.RequestingProcedureName:
            case PrivateTag.RequestingProcedureCode:
            case PrivateTag.RequestStorageCommitment:
            case PrivateTag.RequestedCompression:
            case PrivateTag.StudyLocked:
            case PrivateTag.WorkstationName:
            case PrivateTag.ArchiveStatus:
            case PrivateTag.Action:
                return VR.CS;

            case PrivateTag.ReceiveDate:
            case PrivateTag.SystemDate:
                return VR.DA;

            case PrivateTag.NumberOfStudyRelatedImages:
                return VR.IS;

            case PrivateTag.ReceiveOrigin:
            case PrivateTag.Folder:
            case PrivateTag.ResultMessage:
            case PrivateTag.CurrentUser:
            case PrivateTag.WorklistName:
            case PrivateTag.DestinationName:
            case PrivateTag.OriginName:
            case PrivateTag.NotificationComments:
            case PrivateTag.TransactionComments:
            case PrivateTag.SendFlag:
            case PrivateTag.PrintFlag:
            case PrivateTag.ArchiveFlag:
            case PrivateTag.RequestingFacilityName:
                return VR.LO;

            case PrivateTag.NewSeenStatus:
            case PrivateTag.TeachingACRCode:
            case PrivateTag.TeachingSpecialInterestCode:
                return VR.SH;

            case PrivateTag.ExamRouting:
            case PrivateTag.StudySequence:
                return VR.SQ;

            case PrivateTag.WorklistFilename:
                return VR.ST;

            case PrivateTag.ReceiveTime:
            case PrivateTag.SystemTime:
                return VR.TM;

            case PrivateTag.WorklistUID:
            case PrivateTag.ModalityStudyInstanceUID:
            case PrivateTag.ReplacedStudyUID:
            case PrivateTag.InternalListUID:
                return VR.UI;

            case PrivateTag._3109_xx07_:
                return VR.UL;

            case PrivateTag.DICOMPortNumber:
                return VR.US;
        }
        return VR.UN;
    }

}
