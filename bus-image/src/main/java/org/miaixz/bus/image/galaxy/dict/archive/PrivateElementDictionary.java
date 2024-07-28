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
package org.miaixz.bus.image.galaxy.dict.archive;

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

        case PrivateTag.SendingApplicationEntityTitleOfSeries:
        case PrivateTag.InstanceExternalRetrieveAETitle:
        case PrivateTag.SeriesExternalRetrieveAETitle:
        case PrivateTag.StudyExternalRetrieveAETitle:
        case PrivateTag.ReceivingApplicationEntityTitleOfSeries:
            return VR.AE;
        case PrivateTag.PatientVerificationStatus:
        case PrivateTag.StudyRejectionState:
        case PrivateTag.StudyCompleteness:
        case PrivateTag.StudyExpirationState:
        case PrivateTag.SeriesRejectionState:
        case PrivateTag.SeriesCompleteness:
        case PrivateTag.InstanceRecordPurgeStateOfSeries:
        case PrivateTag.SeriesMetadataStorageObjectStatus:
        case PrivateTag.StorageObjectStatus:
        case PrivateTag.SeriesExpirationState:
        case PrivateTag.XRoadPersonStatus:
        case PrivateTag.XRoadDataStatus:
            return VR.CS;
        case PrivateTag.StudyExpirationDate:
        case PrivateTag.SeriesExpirationDate:
            return VR.DA;
        case PrivateTag.PatientCreateDateTime:
        case PrivateTag.PatientUpdateDateTime:
        case PrivateTag.PatientVerificationDateTime:
        case PrivateTag.StudyReceiveDateTime:
        case PrivateTag.StudyUpdateDateTime:
        case PrivateTag.StudyAccessDateTime:
        case PrivateTag.StudyModifiedDateTime:
        case PrivateTag.SeriesReceiveDateTime:
        case PrivateTag.SeriesUpdateDateTime:
        case PrivateTag.ScheduledMetadataUpdateDateTimeOfSeries:
        case PrivateTag.ScheduledInstanceRecordPurgeDateTimeOfSeries:
        case PrivateTag.InstanceReceiveDateTime:
        case PrivateTag.InstanceUpdateDateTime:
        case PrivateTag.ScheduledStorageVerificationDateTimeOfSeries:
        case PrivateTag.ScheduledCompressionDateTimeOfSeries:
        case PrivateTag.SeriesMetadataCreationDateTime:
        case PrivateTag.SeriesModifiedDateTime:
        case PrivateTag.MPPSCreateDateTime:
        case PrivateTag.MPPSUpdateDateTime:
            return VR.DT;
        case PrivateTag.StorageObjectMultiReference:
            return VR.IS;
        case PrivateTag.StudyAccessControlID:
        case PrivateTag.StorageIDsOfStudy:
        case PrivateTag.StudyExpirationExporterID:
        case PrivateTag.SeriesMetadataStorageID:
        case PrivateTag.SeriesMetadataStoragePath:
        case PrivateTag.SeriesMetadataStorageObjectDigest:
        case PrivateTag.SeriesAccessControlID:
        case PrivateTag.StorageID:
        case PrivateTag.StoragePath:
        case PrivateTag.StorageObjectDigest:
        case PrivateTag.OtherStorageSequence:
        case PrivateTag.SeriesExpirationExporterID:
        case PrivateTag.SendingHL7ApplicationOfSeries:
        case PrivateTag.SendingHL7FacilityOfSeries:
        case PrivateTag.ReceivingHL7ApplicationOfSeries:
        case PrivateTag.ReceivingHL7FacilityOfSeries:
            return VR.LO;
        case PrivateTag.DominantPatientSequence:
        case PrivateTag.RejectionCodeSequence:
            return VR.SQ;
        case PrivateTag.StorageTransferSyntaxUID:
            return VR.UI;
        case PrivateTag.StudySizeInKB:
        case PrivateTag.SeriesMetadataStorageObjectSize:
        case PrivateTag.StorageObjectSize:
            return VR.UL;
        case PrivateTag.SendingPresentationAddressOfSeries:
        case PrivateTag.ReceivingPresentationAddressOfSeries:
            return VR.UR;
        case PrivateTag.FailedVerificationsOfPatient:
        case PrivateTag.FailedRetrievesOfStudy:
        case PrivateTag.StudySizeBytes:
        case PrivateTag.FailedRetrievesOfSeries:
        case PrivateTag.FailuresOfLastStorageVerificationOfSeries:
        case PrivateTag.FailuresOfLastCompressionOfSeries:
        case PrivateTag.SeriesMetadataUpdateFailures:
            return VR.US;
        }
        return VR.UN;
    }
}
