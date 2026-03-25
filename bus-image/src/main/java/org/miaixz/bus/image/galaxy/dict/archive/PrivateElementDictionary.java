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
package org.miaixz.bus.image.galaxy.dict.archive;

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

            case PrivateTag.LogicalPatientID:
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
