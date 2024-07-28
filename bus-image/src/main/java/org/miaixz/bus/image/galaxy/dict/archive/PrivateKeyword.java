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
