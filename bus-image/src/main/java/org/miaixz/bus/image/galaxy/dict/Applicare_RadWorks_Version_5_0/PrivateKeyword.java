/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
