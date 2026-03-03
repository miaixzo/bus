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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.PatientCreateDateTime:
                return "PatientCreateDateTime";

            case PrivateTag.PatientUpdateDateTime:
                return "PatientUpdateDateTime";

            case PrivateTag.PatientVerificationDateTime:
                return "PatientVerificationDateTime";

            case PrivateTag.PatientVerificationStatus:
                return "PatientVerificationStatus";

            case PrivateTag.FailedVerificationsOfPatient:
                return "FailedVerificationsOfPatient";

            case PrivateTag.DominantPatientSequence:
                return "DominantPatientSequence";

            case PrivateTag.LogicalPatientID:
                return "LogicalPatientID";

            case PrivateTag.StudyReceiveDateTime:
                return "StudyReceiveDateTime";

            case PrivateTag.StudyUpdateDateTime:
                return "StudyUpdateDateTime";

            case PrivateTag.StudyAccessDateTime:
                return "StudyAccessDateTime";

            case PrivateTag.StudyExpirationDate:
                return "StudyExpirationDate";

            case PrivateTag.StudyRejectionState:
                return "StudyRejectionState";

            case PrivateTag.StudyCompleteness:
                return "StudyCompleteness";

            case PrivateTag.FailedRetrievesOfStudy:
                return "FailedRetrievesOfStudy";

            case PrivateTag.StudyAccessControlID:
                return "StudyAccessControlID";

            case PrivateTag.StorageIDsOfStudy:
                return "StorageIDsOfStudy";

            case PrivateTag.StudySizeInKB:
                return "StudySizeInKB";

            case PrivateTag.StudySizeBytes:
                return "StudySizeBytes";

            case PrivateTag.StudyExpirationState:
                return "StudyExpirationState";

            case PrivateTag.StudyExpirationExporterID:
                return "StudyExpirationExporterID";

            case PrivateTag.StudyModifiedDateTime:
                return "StudyModifiedDateTime";

            case PrivateTag.SeriesReceiveDateTime:
                return "SeriesReceiveDateTime";

            case PrivateTag.SeriesUpdateDateTime:
                return "SeriesUpdateDateTime";

            case PrivateTag.SeriesExpirationDate:
                return "SeriesExpirationDate";

            case PrivateTag.SeriesRejectionState:
                return "SeriesRejectionState";

            case PrivateTag.SeriesCompleteness:
                return "SeriesCompleteness";

            case PrivateTag.FailedRetrievesOfSeries:
                return "FailedRetrievesOfSeries";

            case PrivateTag.SendingApplicationEntityTitleOfSeries:
                return "SendingApplicationEntityTitleOfSeries";

            case PrivateTag.ScheduledMetadataUpdateDateTimeOfSeries:
                return "ScheduledMetadataUpdateDateTimeOfSeries";

            case PrivateTag.ScheduledInstanceRecordPurgeDateTimeOfSeries:
                return "ScheduledInstanceRecordPurgeDateTimeOfSeries";

            case PrivateTag.InstanceRecordPurgeStateOfSeries:
                return "InstanceRecordPurgeStateOfSeries";

            case PrivateTag.SeriesMetadataStorageID:
                return "SeriesMetadataStorageID";

            case PrivateTag.SeriesMetadataStoragePath:
                return "SeriesMetadataStoragePath";

            case PrivateTag.SeriesMetadataStorageObjectSize:
                return "SeriesMetadataStorageObjectSize";

            case PrivateTag.SeriesMetadataStorageObjectDigest:
                return "SeriesMetadataStorageObjectDigest";

            case PrivateTag.SeriesMetadataStorageObjectStatus:
                return "SeriesMetadataStorageObjectStatus";

            case PrivateTag.InstanceReceiveDateTime:
                return "InstanceReceiveDateTime";

            case PrivateTag.InstanceUpdateDateTime:
                return "InstanceUpdateDateTime";

            case PrivateTag.RejectionCodeSequence:
                return "RejectionCodeSequence";

            case PrivateTag.InstanceExternalRetrieveAETitle:
                return "InstanceExternalRetrieveAETitle";

            case PrivateTag.SeriesExternalRetrieveAETitle:
                return "SeriesExternalRetrieveAETitle";

            case PrivateTag.StudyExternalRetrieveAETitle:
                return "StudyExternalRetrieveAETitle";

            case PrivateTag.SeriesAccessControlID:
                return "SeriesAccessControlID";

            case PrivateTag.StorageID:
                return "StorageID";

            case PrivateTag.StoragePath:
                return "StoragePath";

            case PrivateTag.StorageTransferSyntaxUID:
                return "StorageTransferSyntaxUID";

            case PrivateTag.StorageObjectSize:
                return "StorageObjectSize";

            case PrivateTag.StorageObjectDigest:
                return "StorageObjectDigest";

            case PrivateTag.OtherStorageSequence:
                return "OtherStorageSequence";

            case PrivateTag.StorageObjectStatus:
                return "StorageObjectStatus";

            case PrivateTag.StorageObjectMultiReference:
                return "StorageObjectMultiReference";

            case PrivateTag.ScheduledStorageVerificationDateTimeOfSeries:
                return "ScheduledStorageVerificationDateTimeOfSeries";

            case PrivateTag.FailuresOfLastStorageVerificationOfSeries:
                return "FailuresOfLastStorageVerificationOfSeries";

            case PrivateTag.ScheduledCompressionDateTimeOfSeries:
                return "ScheduledCompressionDateTimeOfSeries";

            case PrivateTag.FailuresOfLastCompressionOfSeries:
                return "FailuresOfLastCompressionOfSeries";

            case PrivateTag.SeriesExpirationState:
                return "SeriesExpirationState";

            case PrivateTag.SeriesExpirationExporterID:
                return "SeriesExpirationExporterID";

            case PrivateTag.SeriesMetadataCreationDateTime:
                return "SeriesMetadataCreationDateTime";

            case PrivateTag.SeriesMetadataUpdateFailures:
                return "SeriesMetadataUpdateFailures";

            case PrivateTag.ReceivingApplicationEntityTitleOfSeries:
                return "ReceivingApplicationEntityTitleOfSeries";

            case PrivateTag.SendingPresentationAddressOfSeries:
                return "SendingPresentationAddressOfSeries";

            case PrivateTag.ReceivingPresentationAddressOfSeries:
                return "ReceivingPresentationAddressOfSeries";

            case PrivateTag.SendingHL7ApplicationOfSeries:
                return "SendingHL7ApplicationOfSeries";

            case PrivateTag.SendingHL7FacilityOfSeries:
                return "SendingHL7FacilityOfSeries";

            case PrivateTag.ReceivingHL7ApplicationOfSeries:
                return "ReceivingHL7ApplicationOfSeries";

            case PrivateTag.ReceivingHL7FacilityOfSeries:
                return "ReceivingHL7FacilityOfSeries";

            case PrivateTag.SeriesModifiedDateTime:
                return "SeriesModifiedDateTime";

            case PrivateTag.MPPSCreateDateTime:
                return "MPPSCreateDateTime";

            case PrivateTag.MPPSUpdateDateTime:
                return "MPPSUpdateDateTime";

            case PrivateTag.XRoadPersonStatus:
                return "XRoadPersonStatus";

            case PrivateTag.XRoadDataStatus:
                return "XRoadDataStatus";
        }
        return "";
    }

}
