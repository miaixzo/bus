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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.core.lang.Normal;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ArchiveKey {

    public static String valueOf(int tag) {
        switch (tag & 0xFFFF00FF) {
            case ArchiveTag.PatientCreateDateTime:
                return "PatientCreateDateTime";
            case ArchiveTag.PatientUpdateDateTime:
                return "PatientUpdateDateTime";
            case ArchiveTag.PatientVerificationDateTime:
                return "PatientVerificationDateTime";
            case ArchiveTag.PatientVerificationStatus:
                return "PatientVerificationStatus";
            case ArchiveTag.FailedVerificationsOfPatient:
                return "FailedVerificationsOfPatient";
            case ArchiveTag.StudyReceiveDateTime:
                return "StudyReceiveDateTime";
            case ArchiveTag.StudyUpdateDateTime:
                return "StudyUpdateDateTime";
            case ArchiveTag.StudyAccessDateTime:
                return "StudyAccessDateTime";
            case ArchiveTag.StudyExpirationDate:
                return "StudyExpirationDate";
            case ArchiveTag.StudyRejectionState:
                return "StudyRejectionState";
            case ArchiveTag.StudyCompleteness:
                return "StudyCompleteness";
            case ArchiveTag.FailedRetrievesOfStudy:
                return "FailedRetrievesOfStudy";
            case ArchiveTag.StudyAccessControlID:
                return "StudyAccessControlID";
            case ArchiveTag.StorageIDsOfStudy:
                return "StorageIDsOfStudy";
            case ArchiveTag.StudySizeInKB:
                return "StudySizeInKB";
            case ArchiveTag.StudySizeBytes:
                return "StudySizeBytes";
            case ArchiveTag.StudyExpirationState:
                return "StudyExpirationState";
            case ArchiveTag.StudyExpirationExporterID:
                return "StudyExpirationExporterID";
            case ArchiveTag.SeriesReceiveDateTime:
                return "SeriesReceiveDateTime";
            case ArchiveTag.SeriesUpdateDateTime:
                return "SeriesUpdateDateTime";
            case ArchiveTag.SeriesExpirationDate:
                return "SeriesExpirationDate";
            case ArchiveTag.SeriesRejectionState:
                return "SeriesRejectionState";
            case ArchiveTag.SeriesCompleteness:
                return "SeriesCompleteness";
            case ArchiveTag.FailedRetrievesOfSeries:
                return "FailedRetrievesOfSeries";
            case ArchiveTag.SendingApplicationEntityTitleOfSeries:
                return "SendingApplicationEntityTitleOfSeries";
            case ArchiveTag.ScheduledMetadataUpdateDateTimeOfSeries:
                return "ScheduledMetadataUpdateDateTimeOfSeries";
            case ArchiveTag.ScheduledInstanceRecordPurgeDateTimeOfSeries:
                return "ScheduledInstanceRecordPurgeDateTimeOfSeries";
            case ArchiveTag.InstanceRecordPurgeStateOfSeries:
                return "InstanceRecordPurgeStateOfSeries";
            case ArchiveTag.SeriesMetadataStorageID:
                return "SeriesMetadataStorageID";
            case ArchiveTag.SeriesMetadataStoragePath:
                return "SeriesMetadataStoragePath";
            case ArchiveTag.SeriesMetadataStorageObjectSize:
                return "SeriesMetadataStorageObjectSize";
            case ArchiveTag.SeriesMetadataStorageObjectDigest:
                return "SeriesMetadataStorageObjectDigest";
            case ArchiveTag.SeriesMetadataStorageObjectStatus:
                return "SeriesMetadataStorageObjectStatus";
            case ArchiveTag.InstanceReceiveDateTime:
                return "InstanceReceiveDateTime";
            case ArchiveTag.InstanceUpdateDateTime:
                return "InstanceUpdateDateTime";
            case ArchiveTag.RejectionCodeSequence:
                return "RejectionCodeSequence";
            case ArchiveTag.InstanceExternalRetrieveAETitle:
                return "InstanceExternalRetrieveAETitle";
            case ArchiveTag.StorageID:
                return "StorageID";
            case ArchiveTag.StoragePath:
                return "StoragePath";
            case ArchiveTag.StorageTransferSyntaxUID:
                return "StorageTransferSyntaxUID";
            case ArchiveTag.StorageObjectSize:
                return "StorageObjectSize";
            case ArchiveTag.StorageObjectDigest:
                return "StorageObjectDigest";
            case ArchiveTag.OtherStorageSequence:
                return "OtherStorageSequence";
            case ArchiveTag.StorageObjectStatus:
                return "StorageObjectStatus";
            case ArchiveTag.ScheduledStorageVerificationDateTimeOfSeries:
                return "ScheduledStorageVerificationDateTimeOfSeries";
            case ArchiveTag.FailuresOfLastStorageVerificationOfSeries:
                return "FailuresOfLastStorageVerificationOfSeries";
            case ArchiveTag.ScheduledCompressionDateTimeOfSeries:
                return "ScheduledCompressionDateTimeOfSeries";
            case ArchiveTag.FailuresOfLastCompressionOfSeries:
                return "FailuresOfLastCompressionOfSeries";
            case ArchiveTag.SeriesExpirationState:
                return "SeriesExpirationState";
            case ArchiveTag.SeriesExpirationExporterID:
                return "SeriesExpirationExporterID";
            case ArchiveTag.SeriesMetadataCreationDateTime:
                return "SeriesMetadataCreationDateTime";
            case ArchiveTag.SeriesMetadataUpdateFailures:
                return "SeriesMetadataUpdateFailures";
            case ArchiveTag.XRoadPersonStatus:
                return "XRoadPersonStatus";
            case ArchiveTag.XRoadDataStatus:
                return "XRoadDataStatus";
        }
        return Normal.EMPTY;
    }

}
