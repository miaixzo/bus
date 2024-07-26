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
