/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.image.metric.internal.xdsi;

/**
 * @author Kimi Liu
 * @version 6.2.2
 * @since JDK 1.8+
 */
public interface XDSConstants {

    String XDS_ERR_MISSING_DOCUMENT = "XDSMissingDocument";
    String XDS_ERR_MISSING_DOCUMENT_METADATA = "XDSMissingDocumentMetadata";
    String XDS_ERR_REG_NOT_AVAIL = "XDSRegistryNotAvailable";
    String XDS_ERR_REGISTRY_ERROR = "XDSRegistryError";
    String XDS_ERR_REPOSITORY_ERROR = "XDSRepositoryError";
    String XDS_ERR_REGISTRY_DUPLICATE_UNIQUE_ID_IN_MSG = "XDSRegistryDuplicateUniqueIdInMessage";
    String XDS_ERR_REPOSITORY_DUPLICATE_UNIQUE_ID_IN_MSG = "XDSRepositoryDuplicateUniqueIdInMessage";
    String XDS_ERR_DUPLICATE_UNIQUE_ID_IN_REGISTRY = "XDSDuplicateUniqueIdInRegistry";
    String XDS_ERR_NON_IDENTICAL_HASH = "XDSNonIdenticalHash";
    String XDS_ERR_NON_IDENTICAL_SIZE = "XDSNonIdenticalSize";
    String XDS_ERR_REGISTRY_BUSY = "XDSRegistryBusy";
    String XDS_ERR_REPOSITORY_BUSY = "XDSRepositoryBusy";
    String XDS_ERR_REGISTRY_OUT_OF_RESOURCES = "XDSRegistryOutOfResources";
    String XDS_ERR_REPOSITORY_OUT_OF_RESOURCES = "XDSRepositoryOutOfResources";
    String XDS_ERR_REGISTRY_METADATA_ERROR = "XDSRegistryMetadataError";
    String XDS_ERR_REPOSITORY_METADATA_ERROR = "XDSRepositoryMetadataError";
    String XDS_ERR_TOO_MANY_RESULTS = "XDSTooManyResults";
    String XDS_ERR_EXTRA_METADATA_NOT_SAVED = "XDSExtraMetadataNotSaved";
    String XDS_ERR_UNKNOWN_PATID = "XDSUnknownPatientId";
    String XDS_ERR_PATID_DOESNOT_MATCH = "XDSPatientIdDoesNotMatch";
    String XDS_ERR_UNKNOWN_STORED_QUERY_ID = "XDSUnknownStoredQuery";
    String XDS_ERR_STORED_QUERY_MISSING_PARAM = "XDSStoredQueryMissingParam";
    String XDS_ERR_STORED_QUERY_PARAM_NUMBER = "XDSStoredQueryParamNumber";
    String XDS_ERR_REGISTRY_DEPRECATED_DOC_ERROR = "XDSRegistryDeprecatedDocumentError";
    String XDS_ERR_UNKNOWN_REPOSITORY_ID = "XDSUnknownRepositoryId";
    String XDS_ERR_DOCUMENT_UNIQUE_ID_ERROR = "XDSDocumentUniqueIdError";
    String XDS_ERR_RESULT_NOT_SINGLE_PATIENT = "XDSResultNotSinglePatient";
    String XDS_ERR_PARTIAL_FOLDER_CONTENT_NOT_PROCESSED = "PartialFolderContentNotProcessed";
    String XDS_ERR_PARTIAL_REPLACE_CONTENT_NOT_PROCESSED = "PartialReplaceContentNotProcessed";
    String XDS_ERR_UNKNOWN_COMMUNITY = "XDSUnknownCommunity";
    String XDS_ERR_MISSING_HOME_COMMUNITY_ID = "XDSMissingHomeCommunityId";
    String XDS_ERR_UNAVAILABLE_COMMUNITY = "XDSUnavailableCommunity";
    String HAS_MEMBER = "urn:oasis:names:tc:ebxml-regrep:AssociationType:HasMember";
    String RELATED_TO = "urn:oasis:names:tc:ebxml-regrep:AssociationType:relatedTo";
    String RPLC = "urn:ihe:iti:2007:AssociationType:RPLC";
    String APND = "urn:ihe:iti:2007:AssociationType:APND";
    String XFRM = "urn:ihe:iti:2007:AssociationType:XFRM";
    String XFRM_RPLC = "urn:ihe:iti:2007:AssociationType:XFRM_RPLC";
    String SIGNS = "urn:ihe:iti:2007:AssociationType:signs";
    String XDS_STATUS_SUCCESS = "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success";
    String XDS_STATUS_FAILURE = "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Failure";
    String XDS_STATUS_PARTIAL_SUCCESS = "urn:ihe:iti:2007:ResponseStatusType:PartialSuccess";
    String XDS_ERR_SEVERITY_WARNING = "urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Warning";
    String XDS_ERR_SEVERITY_ERROR = "urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Error";
    String CLASSIFICATION = "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification";
    String EXTERNAL_IDENTIFIER = "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier";
    String ASSOCIATION = "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association";
    String STATUS_SUBMITTED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted";
    String STATUS_APPROVED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Approved";
    String STATUS_DEPRECATED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Deprecated";
    String UUID_XDSSubmissionSet = "urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd";
    String UUID_XDSSubmissionSet_autor = "urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d";
    String UUID_XDSSubmissionSet_contentTypeCode = "urn:uuid:aa543740-bdda-424e-8c96-df4873be8500";
    String UUID_XDSSubmissionSet_patientId = "urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446";
    String UUID_XDSSubmissionSet_sourceId = "urn:uuid:554ac39e-e3fe-47fe-b233-965d2a147832";
    String UUID_XDSSubmissionSet_uniqueId = "urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8";
    String UUID_XDSSubmissionSet_limitedMetadata = "urn:uuid:5003a9db-8d8d-49e6-bf0c-990e34ac7707";
    String UUID_XDSDocumentEntry = "urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1";
    String UUID_XDSDocumentEntry_author = "urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d";
    String UUID_XDSDocumentEntry_classCode = "urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a";
    String UUID_XDSDocumentEntry_confidentialityCode = "urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f";
    String UUID_XDSDocumentEntry_eventCodeList = "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4";
    String UUID_XDSDocumentEntry_formatCode = "urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d";
    String UUID_XDSDocumentEntry_healthCareFacilityTypeCode = "urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1";
    String UUID_XDSDocumentEntry_patientId = "urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427";
    String UUID_XDSDocumentEntry_practiceSettingCode = "urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead";
    String UUID_XDSDocumentEntry_typeCode = "urn:uuid:f0306f51-975f-434e-a61c-c59651d33983";
    String UUID_XDSDocumentEntry_uniqueId = "urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab";
    String UUID_XDSDocumentEntry_limitedMetadata = "urn:uuid:ab9b591b-83ab-4d03-8f5d-f93b1fb92e85";
    String UUID_XDSFolder = "urn:uuid:d9d542f3-6cc4-48b6-8870-ea235fbc94c2";
    String UUID_XDSFolder_codeList = "urn:uuid:1ba97051-7806-41a8-a48b-8fce7af683c5";
    String UUID_XDSFolder_patientId = "urn:uuid:f64ffdf0-4b97-4e06-b79f-a52b38ec2f8a";
    String UUID_XDSFolder_uniqueId = "urn:uuid:75df8f67-9973-4fbe-a900-df66cefecc5a";
    String UUID_XDSFolder_limitedMetadata = "urn:uuid:2c144a76-29a9-4b7c-af54-b25409fe7d03";
    String UUID_XDSDocumentEntryStub = "urn:uuid:10aa1a4b-715a-4120-bfd0-9760414112c8";
    String UUID_Association_Documentation = "urn:uuid:abd807a3-4432-4053-87b4-fd82c643d1f3";
    String XDS_FindDocuments = "urn:uuid:14d4debf-8f97-4251-9a74-a90016b0af0d";
    String XDS_FindDocumentsByReferenceId = "urn:uuid:12941a89-e02e-4be5-967cce4bfc8fe492";
    String XDS_FindSubmissionSets = "urn:uuid:f26abbcb-ac74-4422-8a30-edb644bbc1a9";
    String XDS_FindFolders = "urn:uuid:958f3006-baad-4929-a4de-ff1114824431";
    String XDS_GetAll = "urn:uuid:10b545ea-725c-446d-9b95-8aeb444eddf3";
    String XDS_GetDocuments = "urn:uuid:5c4f972b-d56b-40ac-a5fc-c8ca9b40b9d4";
    String XDS_GetFolders = "urn:uuid:5737b14c-8a1a-4539-b659-e03a34a5e1e4";
    String XDS_GetAssociations = "urn:uuid:a7ae438b-4bc2-4642-93e9-be891f7bb155";
    String XDS_GetDocumentsAndAssociations = "urn:uuid:bab9529a-4a10-40b3-a01f-f68a615d247a";
    String XDS_GetSubmissionSets = "urn:uuid:51224314-5390-4169-9b91-b1980040715a";
    String XDS_GetSubmissionSetAndContents = "urn:uuid:e8e3cb2c-e39c-46b9-99e4-c12f57260b83";
    String XDS_GetFolderAndContents = "urn:uuid:b909a503-523d-4517-8acf-8e5834dfc4c7";
    String XDS_GetFoldersForDocument = "urn:uuid:10cae35a-c7f9-4cf5-b61e-fc3278ffb578";
    String XDS_GetRelatedDocuments = "urn:uuid:d90e5407-b356-4d91-a89f-873917b4b0e6";
    String QRY_DOCUMENT_ENTRY_PATIENT_ID = "$XDSDocumentEntryPatientId";
    String QRY_DOCUMENT_ENTRY_CLASS_CODE = "$XDSDocumentEntryClassCode";
    String QRY_DOCUMENT_ENTRY_TYPE_CODE = "$XDSDocumentEntryTypeCode";
    String QRY_DOCUMENT_ENTRY_PRACTICE_SETTING_CODE = "$XDSDocumentEntryPracticeSettingCode";
    String QRY_DOCUMENT_ENTRY_CREATION_TIME_FROM = "$XDSDocumentEntryCreationTimeFrom";
    String QRY_DOCUMENT_ENTRY_CREATION_TIME_TO = "$XDSDocumentEntryCreationTimeTo";
    String QRY_DOCUMENT_ENTRY_SERVICE_START_TIME_FROM = "$XDSDocumentEntryServiceStartTimeFrom";
    String QRY_DOCUMENT_ENTRY_SERVICE_START_TIME_TO = "$XDSDocumentEntryServiceStartTimeTo";
    String QRY_DOCUMENT_ENTRY_SERVICE_STOP_TIME_FROM = "$XDSDocumentEntryServiceStopTimeFrom";
    String QRY_DOCUMENT_ENTRY_SERVICE_STOP_TIME_TO = "$XDSDocumentEntryServiceStopTimeTo";
    String QRY_DOCUMENT_ENTRY_HEALTHCARE_FACILITY_TYPE_CODE = "$XDSDocumentEntryHealthcareFacilityTypeCode";
    String QRY_DOCUMENT_ENTRY_EVENT_CODE_LIST = "$XDSDocumentEntryEventCodeList";
    String QRY_DOCUMENT_ENTRY_CONFIDENTIALITY_CODE = "$XDSDocumentEntryConfidentialityCode";
    String QRY_DOCUMENT_ENTRY_AUTHOR_PERSON = "$XDSDocumentEntryAuthorPerson";
    String QRY_DOCUMENT_ENTRY_FORMAT_CODE = "$XDSDocumentEntryFormatCode";
    String QRY_DOCUMENT_ENTRY_STATUS = "$XDSDocumentEntryStatus";
    String QRY_DOCUMENT_ENTRY_REFERENCED_ID_LIST = "$XDSDocumentEntryReferenceIdList";
    String QRY_SUBMISSIONSET_PATIENT_ID = "$XDSSubmissionSetPatientId";
    String QRY_SUBMISSIONSET_SOURCE_ID = "$XDSSubmissionSetSourceId";
    String QRY_SUBMISSIONSET_SUBMISSION_TIME_FROM = "$XDSSubmissionSetSubmissionTimeFrom";
    String QRY_SUBMISSIONSET_SUBMISSION_TIME_TO = "$XDSSubmissionSetSubmissionTimeTo";
    String QRY_SUBMISSIONSET_AUTHOR_PERSON = "$XDSSubmissionSetAuthorPerson";
    String QRY_SUBMISSIONSET_CONTENT_TYPE = "$XDSSubmissionSetContentType";
    String QRY_SUBMISSIONSET_STATUS = "$XDSSubmissionSetStatus";
    String QRY_FOLDER_PATIENT_ID = "$XDSFolderPatientId";
    String QRY_FOLDER_LAST_UPDATE_TIME_FROM = "$XDSFolderLastUpdateTimeFrom";
    String QRY_FOLDER_LAST_UPDATE_TIME_TO = "$XDSFolderLastUpdateTimeTo";
    String QRY_FOLDER_CODE_LIST = "$XDSFolderCodeList";
    String QRY_FOLDER_STATUS = "$XDSFolderStatus";
    String QRY_SUBMISSIONSET_UNIQUE_ID = "$XDSSubmissionSetUniqueId";
    String QRY_SUBMISSIONSET_ENTRY_UUID = "$XDSSubmissionSetEntryUUID";
    String QRY_DOCUMENT_ENTRY_UUID = "$XDSDocumentEntryEntryUUID";
    String QRY_DOCUMENT_UNIQUE_ID = "$XDSDocumentEntryUniqueId";
    String QRY_FOLDER_ENTRY_UUID = "$XDSFolderEntryUUID";
    String QRY_FOLDER_UNIQUE_ID = "$XDSFolderUniqueId";
    String QRY_UUID = "$uuid";
    String QRY_PATIENT_ID = "$patientId";
    String QRY_ASSOCIATION_TYPES = "$AssociationTypes";
    String QRY_HOME_COMMUNITY_ID = "$homeCommunityId";
    String QUERY_RETURN_TYPE_LEAF = "LeafClass";
    String QUERY_RETURN_TYPE_OBJREF = "ObjectRef";
    String SLOT_NAME_AUTHOR_PERSON = "authorPerson";
    String SLOT_NAME_CREATION_TIME = "creationTime";
    String SLOT_NAME_SERVICE_START_TIME = "serviceStartTime";
    String SLOT_NAME_SERVICE_STOP_TIME = "serviceStopTime";
    String SLOT_NAME_SUBMISSION_TIME = "submissionTime";
    String SLOT_NAME_SUBMISSIONSET_STATUS = "SubmissionSetStatus";
    String SLOT_NAME_LAST_UPDATE_TIME = "lastUpdateTime";
    String SLOT_NAME_REPOSITORY_UNIQUE_ID = "repositoryUniqueId";
    String SLOT_NAME_SIZE = "size";
    String SLOT_NAME_HASH = "hash";
    String SLOT_NAME_SOURCE_PATIENT_ID = "sourcePatientId";
    String SLOT_NAME_SOURCE_PATIENT_INFO = "sourcePatientInfo";
    String SLOT_NAME_LANGUAGE_CODE = "languageCode";
    String SLOT_NAME_INTENDED_RECIPIENT = "intendedRecipient";
    String SLOT_NAME_LEGAL_AUTHENTICATOR = "legalAuthenticator";
    String SLOT_NAME_REFERENCE_ID_LIST = "urn:ihe:iti:xds:2013:referenceIdList";
    String WS_ADDRESSING_NS = "http://www.w3.org/2005/08/addressing";
    String WS_ADDRESSING_ANONYMOUS = "http://www.w3.org/2005/08/addressing/anonymous";
    String CXI_TYPE_UNIQUQ_ID = "urn:ihe:iti:xds:2013:uniqueId";
    String CXI_TYPE_ACCESSION = "urn:ihe:iti:xds:2013:accession";
    String CXI_TYPE_REFERRAL = "urn:ihe:iti:xds:2013:referral";
    String CXI_TYPE_ORDER = "urn:ihe:iti:xds:2013:order";
    String CXI_TYPE_WORKFLOW_INSTANCE_ID = "urn:ihe:iti:xdw:2013:workflowInstanceId";
    String CXI_TYPE_STUDY_INSTANCE_UID = "urn:ihe:iti:xds:2016:studyInstanceUID";
    String CXI_TYPE_ENCOUNTER_ID = "urn:ihe:iti:xds:2015:encounterId";

}

