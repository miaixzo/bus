/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.builtin;

import org.aoju.bus.image.galaxy.data.ElementDictionary;
import org.aoju.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ArchiveElementDictionary extends ElementDictionary {

    public ArchiveElementDictionary() {
        super(ArchiveTag.PrivateCreator, ArchiveTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return ArchiveKey.valueOf(tag);
    }

    @Override
    public VR vrOf(int tag) {
        switch (tag & 0xFFFF00FF) {
            case ArchiveTag.SendingApplicationEntityTitleOfSeries:
            case ArchiveTag.InstanceExternalRetrieveAETitle:
                return VR.AE;
            case ArchiveTag.PatientVerificationStatus:
            case ArchiveTag.StudyRejectionState:
            case ArchiveTag.StudyCompleteness:
            case ArchiveTag.StudyExpirationState:
            case ArchiveTag.SeriesRejectionState:
            case ArchiveTag.SeriesCompleteness:
            case ArchiveTag.InstanceRecordPurgeStateOfSeries:
            case ArchiveTag.SeriesMetadataStorageObjectStatus:
            case ArchiveTag.StorageObjectStatus:
            case ArchiveTag.SeriesExpirationState:
            case ArchiveTag.XRoadPersonStatus:
            case ArchiveTag.XRoadDataStatus:
                return VR.CS;
            case ArchiveTag.StudyExpirationDate:
            case ArchiveTag.SeriesExpirationDate:
                return VR.DA;
            case ArchiveTag.PatientCreateDateTime:
            case ArchiveTag.PatientUpdateDateTime:
            case ArchiveTag.PatientVerificationDateTime:
            case ArchiveTag.StudyReceiveDateTime:
            case ArchiveTag.StudyUpdateDateTime:
            case ArchiveTag.StudyAccessDateTime:
            case ArchiveTag.SeriesReceiveDateTime:
            case ArchiveTag.SeriesUpdateDateTime:
            case ArchiveTag.ScheduledMetadataUpdateDateTimeOfSeries:
            case ArchiveTag.ScheduledInstanceRecordPurgeDateTimeOfSeries:
            case ArchiveTag.InstanceReceiveDateTime:
            case ArchiveTag.InstanceUpdateDateTime:
            case ArchiveTag.ScheduledStorageVerificationDateTimeOfSeries:
            case ArchiveTag.ScheduledCompressionDateTimeOfSeries:
            case ArchiveTag.SeriesMetadataCreationDateTime:
                return VR.DT;
            case ArchiveTag.StudyAccessControlID:
            case ArchiveTag.StorageIDsOfStudy:
            case ArchiveTag.StudyExpirationExporterID:
            case ArchiveTag.SeriesMetadataStorageID:
            case ArchiveTag.SeriesMetadataStoragePath:
            case ArchiveTag.SeriesMetadataStorageObjectDigest:
            case ArchiveTag.StorageID:
            case ArchiveTag.StoragePath:
            case ArchiveTag.StorageObjectDigest:
            case ArchiveTag.OtherStorageSequence:
            case ArchiveTag.SeriesExpirationExporterID:
                return VR.LO;
            case ArchiveTag.RejectionCodeSequence:
                return VR.SQ;
            case ArchiveTag.StorageTransferSyntaxUID:
                return VR.UI;
            case ArchiveTag.StudySizeInKB:
            case ArchiveTag.SeriesMetadataStorageObjectSize:
            case ArchiveTag.StorageObjectSize:
                return VR.UL;
            case ArchiveTag.FailedVerificationsOfPatient:
            case ArchiveTag.FailedRetrievesOfStudy:
            case ArchiveTag.StudySizeBytes:
            case ArchiveTag.FailedRetrievesOfSeries:
            case ArchiveTag.FailuresOfLastStorageVerificationOfSeries:
            case ArchiveTag.FailuresOfLastCompressionOfSeries:
            case ArchiveTag.SeriesMetadataUpdateFailures:
                return VR.US;
        }
        return VR.UN;
    }

}
