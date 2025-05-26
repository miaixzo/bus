/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
public class PrivateTag {

    public static final String PrivateCreator = "Miaixz Archive 5";

    /** (7777,xx10) VR=DT VM=1 Patient Create Date Time */
    public static final int PatientCreateDateTime = 0x77770010;

    /** (7777,xx11) VR=DT VM=1 Patient Update Date Time */
    public static final int PatientUpdateDateTime = 0x77770011;

    /** (7777,xx12) VR=DT VM=1 Patient Verification Date Time */
    public static final int PatientVerificationDateTime = 0x77770012;

    /** (7777,xx13) VR=CS VM=1 Patient Verification Status */
    public static final int PatientVerificationStatus = 0x77770013;

    /** (7777,xx14) VR=US VM=1 Failed Verifications of Patient */
    public static final int FailedVerificationsOfPatient = 0x77770014;

    /** (7777,xx15) VR=SQ VM=1 Dominant Patient Sequence */
    public static final int DominantPatientSequence = 0x77770015;

    /** (7777,xx16) VR=LO VM=1 Logical Patient ID */
    public static final int LogicalPatientID = 0x77770016;

    /** (7777,xx20) VR=DT VM=1 Study Receive Date Time */
    public static final int StudyReceiveDateTime = 0x77770020;

    /** (7777,xx21) VR=DT VM=1 Study Update Date Time */
    public static final int StudyUpdateDateTime = 0x77770021;

    /** (7777,xx22) VR=DT VM=1 Study Access Date Time */
    public static final int StudyAccessDateTime = 0x77770022;

    /** (7777,xx23) VR=DA VM=1 Study Expiration Date */
    public static final int StudyExpirationDate = 0x77770023;

    /** (7777,xx24) VR=CS VM=1 Study Rejection State */
    public static final int StudyRejectionState = 0x77770024;

    /** (7777,xx25) VR=CS VM=1 Study Completeness */
    public static final int StudyCompleteness = 0x77770025;

    /** (7777,xx26) VR=US VM=1 Failed Retrieves of Study */
    public static final int FailedRetrievesOfStudy = 0x77770026;

    /** (7777,xx27) VR=LO VM=1 Study Access Control ID */
    public static final int StudyAccessControlID = 0x77770027;

    /** (7777,xx28) VR=LO VM=1-n Storage IDs of Study */
    public static final int StorageIDsOfStudy = 0x77770028;

    /** (7777,xx29) VR=UL VM=1 Study Size in KB */
    public static final int StudySizeInKB = 0x77770029;

    /** (7777,xx2A) VR=US VM=1 Study Size Bytes */
    public static final int StudySizeBytes = 0x7777002A;

    /** (7777,xx2B) VR=CS VM=1 Study Expiration State */
    public static final int StudyExpirationState = 0x7777002B;

    /** (7777,xx2C) VR=LO VM=1 Study Expiration Exporter ID */
    public static final int StudyExpirationExporterID = 0x7777002C;

    /** (7777,xx2D) VR=DT VM=1 Study Modified Date Time */
    public static final int StudyModifiedDateTime = 0x7777002D;

    /** (7777,xx30) VR=DT VM=1 Series Receive Date Time */
    public static final int SeriesReceiveDateTime = 0x77770030;

    /** (7777,xx31) VR=DT VM=1 Series Update Date Time */
    public static final int SeriesUpdateDateTime = 0x77770031;

    /** (7777,xx33) VR=DA VM=1 Series Expiration Date */
    public static final int SeriesExpirationDate = 0x77770033;

    /** (7777,xx34) VR=CS VM=1 Series Rejection State */
    public static final int SeriesRejectionState = 0x77770034;

    /** (7777,xx35) VR=CS VM=1 Series Completeness */
    public static final int SeriesCompleteness = 0x77770035;

    /** (7777,xx36) VR=US VM=1 Failed Retrieves of Series */
    public static final int FailedRetrievesOfSeries = 0x77770036;

    /** (7777,xx37) VR=AE VM=1 Sending Application Entity Title of Series */
    public static final int SendingApplicationEntityTitleOfSeries = 0x77770037;

    /** (7777,xx38) VR=DT VM=1 Scheduled Metadata Update Date Time of Series */
    public static final int ScheduledMetadataUpdateDateTimeOfSeries = 0x77770038;

    /** (7777,xx39) VR=DT VM=1 Scheduled Instance Record Purge Date Time of Series */
    public static final int ScheduledInstanceRecordPurgeDateTimeOfSeries = 0x77770039;

    /** (7777,xx3A) VR=CS VM=1 Instance Record Purge State of Series */
    public static final int InstanceRecordPurgeStateOfSeries = 0x7777003A;

    /** (7777,xx3B) VR=LO VM=1 Series Metadata Storage ID */
    public static final int SeriesMetadataStorageID = 0x7777003B;

    /** (7777,xx3C) VR=LO VM=1-n Series Metadata Storage Path */
    public static final int SeriesMetadataStoragePath = 0x7777003C;

    /** (7777,xx3D) VR=UL VM=1 Series Metadata Storage Object Size */
    public static final int SeriesMetadataStorageObjectSize = 0x7777003D;

    /** (7777,xx3E) VR=LO VM=1 Series Metadata Storage Object Digest */
    public static final int SeriesMetadataStorageObjectDigest = 0x7777003E;

    /** (7777,xx3F) VR=CS VM=1 Series Metadata Storage Object Status */
    public static final int SeriesMetadataStorageObjectStatus = 0x7777003F;

    /** (7777,xx40) VR=DT VM=1 Instance Receive Date Time */
    public static final int InstanceReceiveDateTime = 0x77770040;

    /** (7777,xx41) VR=DT VM=1 Instance Update Date Time */
    public static final int InstanceUpdateDateTime = 0x77770041;

    /** (7777,xx42) VR=SQ VM=1 Rejection Code Sequence */
    public static final int RejectionCodeSequence = 0x77770042;

    /** (7777,xx43) VR=AE VM=1 Instance External Retrieve AE Title */
    public static final int InstanceExternalRetrieveAETitle = 0x77770043;

    /** (7777,xx44) VR=AE VM=1 Series External Retrieve AE Title */
    public static final int SeriesExternalRetrieveAETitle = 0x77770044;

    /** (7777,xx45) VR=AE VM=1 Study External Retrieve AE Title */
    public static final int StudyExternalRetrieveAETitle = 0x77770045;

    /** (7777,xx46) VR=LO VM=1 Series Access Control ID */
    public static final int SeriesAccessControlID = 0x77770046;

    /** (7777,xx50) VR=LO VM=1 Storage ID */
    public static final int StorageID = 0x77770050;

    /** (7777,xx51) VR=LO VM=1-n Storage Path */
    public static final int StoragePath = 0x77770051;

    /** (7777,xx52) VR=UI VM=1 Storage Transfer Syntax UID */
    public static final int StorageTransferSyntaxUID = 0x77770052;

    /** (7777,xx53) VR=UL VM=1 Storage Object Size */
    public static final int StorageObjectSize = 0x77770053;

    /** (7777,xx54) VR=LO VM=1 Storage Object Digest */
    public static final int StorageObjectDigest = 0x77770054;

    /** (7777,xx55) VR=LO VM=1 Other Storage Sequence */
    public static final int OtherStorageSequence = 0x77770055;

    /** (7777,xx56) VR=CS VM=1 Storage Object Status */
    public static final int StorageObjectStatus = 0x77770056;

    /** (7777,xx57) VR=IS VM=1 Storage Object Multi-Reference */
    public static final int StorageObjectMultiReference = 0x77770057;

    /** (7777,xx60) VR=DT VM=1 Scheduled Storage Verification Date Time of Series */
    public static final int ScheduledStorageVerificationDateTimeOfSeries = 0x77770060;

    /** (7777,xx61) VR=US VM=1 Failures of last Storage Verification of Series */
    public static final int FailuresOfLastStorageVerificationOfSeries = 0x77770061;

    /** (7777,xx62) VR=DT VM=1 Scheduled Compression Date Time of Series */
    public static final int ScheduledCompressionDateTimeOfSeries = 0x77770062;

    /** (7777,xx63) VR=US VM=1 Failures of last Compression of Series */
    public static final int FailuresOfLastCompressionOfSeries = 0x77770063;

    /** (7777,xx64) VR=CS VM=1 Series Expiration State */
    public static final int SeriesExpirationState = 0x77770064;

    /** (7777,xx65) VR=LO VM=1 Series Expiration Exporter ID */
    public static final int SeriesExpirationExporterID = 0x77770065;

    /** (7777,xx66) VR=DT VM=1 Series Metadata Creation Date Time */
    public static final int SeriesMetadataCreationDateTime = 0x77770066;

    /** (7777,xx67) VR=US VM=1 Series Metadata Update Failures */
    public static final int SeriesMetadataUpdateFailures = 0x77770067;

    /** (7777,xx68) VR=AE VM=1 Receiving Application Entity Title of Series */
    public static final int ReceivingApplicationEntityTitleOfSeries = 0x77770068;

    /** (7777,xx69) VR=UR VM=1 Sending Presentation Address of Series */
    public static final int SendingPresentationAddressOfSeries = 0x77770069;

    /** (7777,xx6A) VR=UR VM=1 Receiving Presentation Address of Series */
    public static final int ReceivingPresentationAddressOfSeries = 0x7777006A;

    /** (7777,xx6B) VR=LO VM=1 Sending HL7 Application of Series */
    public static final int SendingHL7ApplicationOfSeries = 0x7777006B;

    /** (7777,xx6C) VR=LO VM=1 Sending HL7 Facility of Series */
    public static final int SendingHL7FacilityOfSeries = 0x7777006C;

    /** (7777,xx6D) VR=LO VM=1 Receiving HL7 Application of Series */
    public static final int ReceivingHL7ApplicationOfSeries = 0x7777006D;

    /** (7777,xx6E) VR=LO VM=1 Receiving HL7 Facility of Series */
    public static final int ReceivingHL7FacilityOfSeries = 0x7777006E;

    /** (7777,xx6F) VR=DT VM=1 Series Modified Date Time */
    public static final int SeriesModifiedDateTime = 0x7777006F;

    /** (7777,xx70) VR=DT VM=1 MPPS Create Date Time */
    public static final int MPPSCreateDateTime = 0x77770070;

    /** (7777,xx71) VR=DT VM=1 MPPS Update Date Time */
    public static final int MPPSUpdateDateTime = 0x77770071;

    /** (7777,xxE0) VR=CS VM=1 X-Road Person Status */
    public static final int XRoadPersonStatus = 0x777700E0;

    /** (7777,xxE1) VR=CS VM=1 X-Road Data Status */
    public static final int XRoadDataStatus = 0x777700E1;

}
