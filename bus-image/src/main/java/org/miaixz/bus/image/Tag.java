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
package org.miaixz.bus.image;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;

/**
 * Utility class that defines constants for DICOM Tags and provides helper methods for tag manipulation. The constants
 * represent standard and some private DICOM tag identifiers. The integer values are constructed from the Group and
 * Element numbers as (Group &lt;&lt; 16) | Element.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Tag {

    /**
     * Constructs a new {@code Tag} instance.
     */
    public Tag() {
        // No initialization required.
    }

    /**
     * (0000,0000) UL Command Group Length
     */
    public static final int CommandGroupLength = 0;

    /**
     * (0000,0001) UL Command Length to End
     */
    public static final int CommandLengthToEnd = 1;

    /**
     * (0000,0002) UI Affected SOP Class UID
     */
    public static final int AffectedSOPClassUID = 2;

    /**
     * (0000,0003) UI Requested SOP Class UID
     */
    public static final int RequestedSOPClassUID = 3;

    /**
     * (0000,0010) SH Command Recognition Code
     */
    public static final int CommandRecognitionCode = 16;

    /**
     * (0000,0100) US Command Field
     */
    public static final int CommandField = 256;

    /**
     * (0000,0110) US Message ID
     */
    public static final int MessageID = 272;

    /**
     * (0000,0120) US Message ID Being Responded To
     */
    public static final int MessageIDBeingRespondedTo = 288;

    /**
     * (0000,0200) AE Initiator
     */
    public static final int Initiator = 512;

    /**
     * (0000,0300) AE Receiver
     */
    public static final int Receiver = 768;

    /**
     * (0000,0400) AE Find Location
     */
    public static final int FindLocation = 1024;

    /**
     * (0000,0600) AE Move Destination
     */
    public static final int MoveDestination = 1536;

    /**
     * (0000,0700) US Priority
     */
    public static final int Priority = 1792;

    /**
     * (0000,0800) US Command Data Set Type
     */
    public static final int CommandDataSetType = 2048;

    /**
     * (0000,0850) US Number of Matches
     */
    public static final int NumberOfMatches = 2128;

    /**
     * (0000,0860) US Response Sequence Number
     */
    public static final int ResponseSequenceNumber = 2144;

    /**
     * (0000,0900) US Status
     */
    public static final int Status = 2304;

    /**
     * (0000,0901) AT Offending Element
     */
    public static final int OffendingElement = 2305;

    /**
     * (0000,0902) LO Error Comment
     */
    public static final int ErrorComment = 2306;

    /**
     * (0000,0903) US Error ID
     */
    public static final int ErrorID = 2307;

    /**
     * (0000,1000) UI Affected SOP Instance UID
     */
    public static final int AffectedSOPInstanceUID = 4096;

    /**
     * (0000,1001) UI Requested SOP Instance UID
     */
    public static final int RequestedSOPInstanceUID = 4097;

    /**
     * (0000,1002) US Event Type ID
     */
    public static final int EventTypeID = 4098;

    /**
     * (0000,1005) AT Attribute Identifier List
     */
    public static final int AttributeIdentifierList = 4101;

    /**
     * (0000,1008) US Action Type ID
     */
    public static final int ActionTypeID = 4104;

    /**
     * (0000,1020) US Number of Remaining Sub-operations
     */
    public static final int NumberOfRemainingSuboperations = 4128;

    /**
     * (0000,1021) US Number of Completed Sub-operations
     */
    public static final int NumberOfCompletedSuboperations = 4129;

    /**
     * (0000,1022) US Number of Failed Sub-operations
     */
    public static final int NumberOfFailedSuboperations = 4130;

    /**
     * (0000,1023) US Number of Warning Sub-operations
     */
    public static final int NumberOfWarningSuboperations = 4131;

    /**
     * (0000,1030) AE Move Originator Application Entity Title
     */
    public static final int MoveOriginatorApplicationEntityTitle = 4144;

    /**
     * (0000,1031) US Move Originator Message ID
     */
    public static final int MoveOriginatorMessageID = 4145;

    /**
     * (0000,4000) AT Dialog Receiver
     */
    public static final int DialogReceiver = 16384;

    /**
     * (0000,4010) CS Terminal Type
     */
    public static final int TerminalType = 16400;

    /**
     * (0000,5010) SH Message Set ID
     */
    public static final int MessageSetID = 20496;

    /**
     * (0000,5020) SH End Message ID
     */
    public static final int EndMessageID = 20512;

    /**
     * (0000,5110) CS Display Format
     */
    public static final int DisplayFormat = 20752;

    /**
     * (0000,5120) CS Page Position ID
     */
    public static final int PagePositionID = 20768;

    /**
     * (0000,5130) CS Text Format ID
     */
    public static final int TextFormatID = 20784;

    /**
     * (0000,5140) CS Normal/Reverse
     */
    public static final int NormalReverse = 20800;

    /**
     * (0000,5150) CS Add Gray Scale
     */
    public static final int AddGrayScale = 20816;

    /**
     * (0000,5160) CS Borders
     */
    public static final int Borders = 20832;

    /**
     * (0000,5170) IS Copies
     */
    public static final int Copies = 20848;

    /**
     * (0000,5180) CS Command Magnification Type
     */
    public static final int CommandMagnificationType = 20864;

    /**
     * (0000,5190) CS Erase
     */
    public static final int Erase = 20880;

    /**
     * (0000,51A0) CS Print
     */
    public static final int Print = 20896;

    /**
     * (0000,51B0) US Overlays
     */
    public static final int Overlays = 20912;

    /**
     * (0002,0000) UL File Meta Information Group Length
     */
    public static final int FileMetaInformationGroupLength = 131072;

    /**
     * (0002,0001) OB File Meta Information Version
     */
    public static final int FileMetaInformationVersion = 131073;

    /**
     * (0002,0002) UI Media Storage SOP Class UID
     */
    public static final int MediaStorageSOPClassUID = 131074;

    /**
     * (0002,0003) UI Media Storage SOP Instance UID
     */
    public static final int MediaStorageSOPInstanceUID = 131075;

    /**
     * (0002,0010) UI Transfer Syntax UID
     */
    public static final int TransferSyntaxUID = 131088;

    /**
     * (0002,0012) UI Implementation Class UID
     */
    public static final int ImplementationClassUID = 131090;

    /**
     * (0002,0013) SH Implementation Version Name
     */
    public static final int ImplementationVersionName = 131091;

    /**
     * (0002,0016) AE Source Application Entity Title
     */
    public static final int SourceApplicationEntityTitle = 131094;

    /**
     * (0002,0017) AE Sending Application Entity Title
     */
    public static final int SendingApplicationEntityTitle = 131095;

    /**
     * (0002,0018) AE Receiving Application Entity Title
     */
    public static final int ReceivingApplicationEntityTitle = 131096;

    /**
     * (0002,0026) UR Source Presentation Address
     */
    public static final int SourcePresentationAddress = 131110;

    /**
     * (0002,0027) UR Sending Presentation Address
     */
    public static final int SendingPresentationAddress = 131111;

    /**
     * (0002,0028) UR Receiving Presentation Address
     */
    public static final int ReceivingPresentationAddress = 131112;

    /**
     * (0002,0031) OB RTV Meta Information Version
     */
    public static final int RTVMetaInformationVersion = 131121;

    /**
     * (0002,0032) UI RTV Communication SOP Class UID
     */
    public static final int RTVCommunicationSOPClassUID = 131122;

    /**
     * (0002,0033) UI RTV Communication SOP Instance UID
     */
    public static final int RTVCommunicationSOPInstanceUID = 131123;

    /**
     * (0002,0035) ST RTV Source Identifier
     */
    public static final int RTVSourceIdentifier = 131125;

    /**
     * (0002,0036) ST RTV Flow Identifier
     */
    public static final int RTVFlowIdentifier = 131126;

    /**
     * (0002,0037) UL RTV Flow RTP Sampling Rate
     */
    public static final int RTVFlowRTPSamplingRate = 131127;

    /**
     * (0002,0038) FD RTV Flow Actual Frame Duration
     */
    public static final int RTVFlowActualFrameDuration = 131128;

    /**
     * (0002,0100) UI Private Information Creator UID
     */
    public static final int PrivateInformationCreatorUID = 131328;

    /**
     * (0002,0102) OB Private Information
     */
    public static final int PrivateInformation = 131330;

    /**
     * (0004,1130) CS File-set ID
     */
    public static final int FileSetID = 266544;

    /**
     * (0004,1141) CS File-set Descriptor File ID
     */
    public static final int FileSetDescriptorFileID = 266561;

    /**
     * (0004,1142) CS Specific Character Set of File-set Descriptor File
     */
    public static final int SpecificCharacterSetOfFileSetDescriptorFile = 266562;

    /**
     * (0004,1200) UL Offset of the First Directory Record of the Root Directory Entity
     */
    public static final int OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity = 266752;

    /**
     * (0004,1202) UL Offset of the Last Directory Record of the Root Directory Entity
     */
    public static final int OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity = 266754;

    /**
     * (0004,1212) US File-set Consistency Flag
     */
    public static final int FileSetConsistencyFlag = 266770;

    /**
     * (0004,1220) SQ Directory Record Sequence
     */
    public static final int DirectoryRecordSequence = 266784;

    /**
     * (0004,1400) UL Offset of the Next Directory Record
     */
    public static final int OffsetOfTheNextDirectoryRecord = 267264;

    /**
     * (0004,1410) US Record In-use Flag
     */
    public static final int RecordInUseFlag = 267280;

    /**
     * (0004,1420) UL Offset of Referenced Lower-Level Directory Entity
     */
    public static final int OffsetOfReferencedLowerLevelDirectoryEntity = 267296;

    /**
     * (0004,1430) CS Directory Record Type
     */
    public static final int DirectoryRecordType = 267312;

    /**
     * (0004,1432) UI Private Record UID
     */
    public static final int PrivateRecordUID = 267314;

    /**
     * (0004,1500) CS Referenced File ID
     */
    public static final int ReferencedFileID = 267520;

    /**
     * (0004,1504) UL MRDR Directory Record Offset
     */
    public static final int MRDRDirectoryRecordOffset = 267524;

    /**
     * (0004,1510) UI Referenced SOP Class UID in File
     */
    public static final int ReferencedSOPClassUIDInFile = 267536;

    /**
     * (0004,1511) UI Referenced SOP Instance UID in File
     */
    public static final int ReferencedSOPInstanceUIDInFile = 267537;

    /**
     * (0004,1512) UI Referenced Transfer Syntax UID in File
     */
    public static final int ReferencedTransferSyntaxUIDInFile = 267538;

    /**
     * (0004,151A) UI Referenced Related General SOP Class UID in File
     */
    public static final int ReferencedRelatedGeneralSOPClassUIDInFile = 267546;

    /**
     * (0004,1600) UL Number of References
     */
    public static final int NumberOfReferences = 267776;

    /**
     * (0008,0001) UL Length to End
     */
    public static final int LengthToEnd = 524289;

    /**
     * (0008,0005) CS Specific Character Set
     */
    public static final int SpecificCharacterSet = 524293;

    /**
     * (0008,0006) SQ Language Code Sequence
     */
    public static final int LanguageCodeSequence = 524294;

    /**
     * (0008,0008) CS Image Type
     */
    public static final int ImageType = 524296;

    /**
     * (0008,0010) SH Recognition Code
     */
    public static final int RecognitionCode = 524304;

    /**
     * (0008,0012) DA Instance Creation Date
     */
    public static final int InstanceCreationDate = 524306;

    /**
     * (0008,0013) TM Instance Creation Time
     */
    public static final int InstanceCreationTime = 524307;

    /**
     * (0008,0014) UI Instance Creator UID
     */
    public static final int InstanceCreatorUID = 524308;

    /**
     * (0008,0015) DT Instance Coercion DateTime
     */
    public static final int InstanceCoercionDateTime = 524309;

    /**
     * (0008,0016) UI SOP Class UID
     */
    public static final int SOPClassUID = 524310;

    /**
     * (0008,0017) UI Acquisition UID
     */
    public static final int AcquisitionUID = 524311;

    /**
     * (0008,0018) UI SOP Instance UID
     */
    public static final int SOPInstanceUID = 524312;

    /**
     * (0008,0019) UI Pyramid UID
     */
    public static final int PyramidUID = 524313;

    /**
     * (0008,001A) UI Related General SOP Class UID
     */
    public static final int RelatedGeneralSOPClassUID = 524314;

    /**
     * (0008,001B) UI Original Specialized SOP Class UID
     */
    public static final int OriginalSpecializedSOPClassUID = 524315;

    /**
     * (0008,001C) CS Synthetic Data
     */
    public static final int SyntheticData = 524316;

    /**
     * (0008,0020) DA Study Date
     */
    public static final int StudyDate = 524320;

    /**
     * (0008,0021) DA Series Date
     */
    public static final int SeriesDate = 524321;

    /**
     * (0008,0022) DA Acquisition Date
     */
    public static final int AcquisitionDate = 524322;

    /**
     * (0008,0023) DA Content Date
     */
    public static final int ContentDate = 524323;

    /**
     * (0008,0024) DA Overlay Date
     */
    public static final int OverlayDate = 524324;

    /**
     * (0008,0025) DA Curve Date
     */
    public static final int CurveDate = 524325;

    /**
     * (0008,002A) DT Acquisition DateTime
     */
    public static final int AcquisitionDateTime = 524330;

    /**
     * (0008,0030) TM Study Time
     */
    public static final int StudyTime = 524336;

    /**
     * (0008,0031) TM Series Time
     */
    public static final int SeriesTime = 524337;

    /**
     * (0008,0032) TM Acquisition Time
     */
    public static final int AcquisitionTime = 524338;

    /**
     * (0008,0033) TM Content Time
     */
    public static final int ContentTime = 524339;

    /**
     * (0008,0034) TM Overlay Time
     */
    public static final int OverlayTime = 524340;

    /**
     * (0008,0035) TM Curve Time
     */
    public static final int CurveTime = 524341;

    /**
     * (0008,0040) US Data Set Type
     */
    public static final int DataSetType = 524352;

    /**
     * (0008,0041) LO Data Set Subtype
     */
    public static final int DataSetSubtype = 524353;

    /**
     * (0008,0042) CS Nuclear Medicine Series Type
     */
    public static final int NuclearMedicineSeriesType = 524354;

    /**
     * (0008,0050) SH Accession Number
     */
    public static final int AccessionNumber = 524368;

    /**
     * (0008,0051) SQ Issuer of Accession Number Sequence
     */
    public static final int IssuerOfAccessionNumberSequence = 524369;

    /**
     * (0008,0052) CS Query/Retrieve Level
     */
    public static final int QueryRetrieveLevel = 524370;

    /**
     * (0008,0053) CS Query/Retrieve View
     */
    public static final int QueryRetrieveView = 524371;

    /**
     * (0008,0054) AE Retrieve AE Title
     */
    public static final int RetrieveAETitle = 524372;

    /**
     * (0008,0055) AE Station AE Title
     */
    public static final int StationAETitle = 524373;

    /**
     * (0008,0056) CS Instance Availability
     */
    public static final int InstanceAvailability = 524374;

    /**
     * (0008,0058) UI Failed SOP Instance UID List
     */
    public static final int FailedSOPInstanceUIDList = 524376;

    /**
     * (0008,0060) CS Modality
     */
    public static final int Modality = 524384;

    /**
     * (0008,0061) CS Modalities in Study
     */
    public static final int ModalitiesInStudy = 524385;

    /**
     * (0008,0062) UI SOP Classes in Study
     */
    public static final int SOPClassesInStudy = 524386;

    /**
     * (0008,0063) SQ Anatomic Regions in Study Code Sequence
     */
    public static final int AnatomicRegionsInStudyCodeSequence = 524387;

    /**
     * (0008,0064) CS Conversion Type
     */
    public static final int ConversionType = 524388;

    /**
     * (0008,0068) CS Presentation Intent Type
     */
    public static final int PresentationIntentType = 524392;

    /**
     * (0008,0070) LO Manufacturer
     */
    public static final int Manufacturer = 524400;

    /**
     * (0008,0080) LO Institution Name
     */
    public static final int InstitutionName = 524416;

    /**
     * (0008,0081) ST Institution Address
     */
    public static final int InstitutionAddress = 524417;

    /**
     * (0008,0082) SQ Institution Code Sequence
     */
    public static final int InstitutionCodeSequence = 524418;

    /**
     * (0008,0090) PN Referring Physician's Name
     */
    public static final int ReferringPhysicianName = 524432;

    /**
     * (0008,0092) ST Referring Physician's Address
     */
    public static final int ReferringPhysicianAddress = 524434;

    /**
     * (0008,0094) SH Referring Physician's Telephone Numbers
     */
    public static final int ReferringPhysicianTelephoneNumbers = 524436;

    /**
     * (0008,0096) SQ Referring Physician Identification Sequence
     */
    public static final int ReferringPhysicianIdentificationSequence = 524438;

    /**
     * (0008,009C) PN Consulting Physician's Name
     */
    public static final int ConsultingPhysicianName = 524444;

    /**
     * (0008,009D) SQ Consulting Physician Identification Sequence
     */
    public static final int ConsultingPhysicianIdentificationSequence = 524445;

    /**
     * (0008,0100) SH Code Value
     */
    public static final int CodeValue = 524544;

    /**
     * (0008,0101) LO Extended Code Value
     */
    public static final int ExtendedCodeValue = 524545;

    /**
     * (0008,0102) SH Coding Scheme Designator
     */
    public static final int CodingSchemeDesignator = 524546;

    /**
     * (0008,0103) SH Coding Scheme Version
     */
    public static final int CodingSchemeVersion = 524547;

    /**
     * (0008,0104) LO Code Meaning
     */
    public static final int CodeMeaning = 524548;

    /**
     * (0008,0105) CS Mapping Resource
     */
    public static final int MappingResource = 524549;

    /**
     * (0008,0106) DT Context Group Version
     */
    public static final int ContextGroupVersion = 524550;

    /**
     * (0008,0107) DT Context Group Local Version
     */
    public static final int ContextGroupLocalVersion = 524551;

    /**
     * (0008,0108) LT Extended Code Meaning
     */
    public static final int ExtendedCodeMeaning = 524552;

    /**
     * (0008,0109) SQ Coding Scheme Resources Sequence
     */
    public static final int CodingSchemeResourcesSequence = 524553;

    /**
     * (0008,010A) CS Coding Scheme URL Type
     */
    public static final int CodingSchemeURLType = 524554;

    /**
     * (0008,010B) CS Context Group Extension Flag
     */
    public static final int ContextGroupExtensionFlag = 524555;

    /**
     * (0008,010C) UI Coding Scheme UID
     */
    public static final int CodingSchemeUID = 524556;

    /**
     * (0008,010D) UI Context Group Extension Creator UID
     */
    public static final int ContextGroupExtensionCreatorUID = 524557;

    /**
     * (0008,010E) UR Coding Scheme URL
     */
    public static final int CodingSchemeURL = 524558;

    /**
     * (0008,010F) CS Context Identifier
     */
    public static final int ContextIdentifier = 524559;

    /**
     * (0008,0110) SQ Coding Scheme Identification Sequence
     */
    public static final int CodingSchemeIdentificationSequence = 524560;

    /**
     * (0008,0112) LO Coding Scheme Registry
     */
    public static final int CodingSchemeRegistry = 524562;

    /**
     * (0008,0114) ST Coding Scheme External ID
     */
    public static final int CodingSchemeExternalID = 524564;

    /**
     * (0008,0115) ST Coding Scheme Name
     */
    public static final int CodingSchemeName = 524565;

    /**
     * (0008,0116) ST Coding Scheme Responsible Organization
     */
    public static final int CodingSchemeResponsibleOrganization = 524566;

    /**
     * (0008,0117) UI Context UID
     */
    public static final int ContextUID = 524567;

    /**
     * (0008,0118) UI Mapping Resource UID
     */
    public static final int MappingResourceUID = 524568;

    /**
     * (0008,0119) UC Long Code Value
     */
    public static final int LongCodeValue = 524569;

    /**
     * (0008,0120) UR URN Code Value
     */
    public static final int URNCodeValue = 524576;

    /**
     * (0008,0121) SQ Equivalent Code Sequence
     */
    public static final int EquivalentCodeSequence = 524577;

    /**
     * (0008,0122) LO Mapping Resource Name
     */
    public static final int MappingResourceName = 524578;

    /**
     * (0008,0123) SQ Context Group Identification Sequence
     */
    public static final int ContextGroupIdentificationSequence = 524579;

    /**
     * (0008,0124) SQ Mapping Resource Identification Sequence
     */
    public static final int MappingResourceIdentificationSequence = 524580;

    /**
     * (0008,0201) SH Timezone Offset From UTC
     */
    public static final int TimezoneOffsetFromUTC = 524801;

    /**
     * (0008,0220) SQ Responsible Group Code Sequence
     */
    public static final int ResponsibleGroupCodeSequence = 524832;

    /**
     * (0008,0221) CS Equipment Modality
     */
    public static final int EquipmentModality = 524833;

    /**
     * (0008,0222) LO Manufacturer's Related Model Group
     */
    public static final int ManufacturerRelatedModelGroup = 524834;

    /**
     * (0008,0300) SQ Private Data Element Characteristics Sequence
     */
    public static final int PrivateDataElementCharacteristicsSequence = 525056;

    /**
     * (0008,0301) US Private Group Reference
     */
    public static final int PrivateGroupReference = 525057;

    /**
     * (0008,0302) LO Private Creator Reference
     */
    public static final int PrivateCreatorReference = 525058;

    /**
     * (0008,0303) CS Block Identifying Information Status
     */
    public static final int BlockIdentifyingInformationStatus = 525059;

    /**
     * (0008,0304) US Nonidentifying Private Elements
     */
    public static final int NonidentifyingPrivateElements = 525060;

    /**
     * (0008,0306) US Identifying Private Elements
     */
    public static final int IdentifyingPrivateElements = 525062;

    /**
     * (0008,0305) SQ De-identification Action Sequence
     */
    public static final int DeidentificationActionSequence = 525061;

    /**
     * (0008,0307) CS De-identification Action
     */
    public static final int DeidentificationAction = 525063;

    /**
     * (0008,0308) US Private Data Element
     */
    public static final int PrivateDataElement = 525064;

    /**
     * (0008,0309) UL Private Data Element Value Multiplicity
     */
    public static final int PrivateDataElementValueMultiplicity = 525065;

    /**
     * (0008,030A) CS Private Data Element Value Representation
     */
    public static final int PrivateDataElementValueRepresentation = 525066;

    /**
     * (0008,030B) UL Private Data Element Number of Items
     */
    public static final int PrivateDataElementNumberOfItems = 525067;

    /**
     * (0008,030C) UC Private Data Element Name
     */
    public static final int PrivateDataElementName = 525068;

    /**
     * (0008,030D) UC Private Data Element Keyword
     */
    public static final int PrivateDataElementKeyword = 525069;

    /**
     * (0008,030E) UT Private Data Element Description
     */
    public static final int PrivateDataElementDescription = 525070;

    /**
     * (0008,030F) UT Private Data Element Encoding
     */
    public static final int PrivateDataElementEncoding = 525071;

    /**
     * (0008,0310) SQ Private Data Element Definition Sequence
     */
    public static final int PrivateDataElementDefinitionSequence = 525072;

    /**
     * (0008,0400) SQ Scope of Inventory Sequence
     */
    public static final int ScopeOfInventorySequence = 525312;

    /**
     * (0008,0401) CS Inventory Purpose
     */
    public static final int InventoryPurpose = 525313;

    /**
     * (0008,0402) LT Inventory Instance Description
     */
    public static final int InventoryInstanceDescription = 525314;

    /**
     * (0008,0403) CS Inventory Level
     */
    public static final int InventoryLevel = 525315;

    /**
     * (0008,0404) DT Item Inventory DateTime
     */
    public static final int ItemInventoryDateTime = 525316;

    /**
     * (0008,0405) CS Removed From Operational Use
     */
    public static final int RemovedFromOperationalUse = 525317;

    /**
     * (0008,0406) SQ Reason for Removal Code Sequence
     */
    public static final int ReasonForRemovalCodeSequence = 525318;

    /**
     * (0008,0407) UR Stored Instance Base URI
     */
    public static final int StoredInstanceBaseURI = 525319;

    /**
     * (0008,0408) UR Folder Access URI
     */
    public static final int FolderAccessURI = 525320;

    /**
     * (0008,0409) UR File Access URI
     */
    public static final int FileAccessURI = 525321;

    /**
     * (0008,040A) CS Container File Type
     */
    public static final int ContainerFileType = 525322;

    /**
     * (0008,040B) LO Filename in Container
     */
    public static final int FilenameInContainer = 525323;

    /**
     * (0008,040C) UL File Offset in Container
     */
    public static final int FileOffsetInContainer = 525324;

    /**
     * (0008,040D) UL File Length in Container
     */
    public static final int FileLengthInContainer = 525325;

    /**
     * (0008,040E) UI Stored Instance Transfer Syntax UID
     */
    public static final int StoredInstanceTransferSyntaxUID = 525326;

    /**
     * (0008,040F) CS Extended Matching Mechanisms
     */
    public static final int ExtendedMatchingMechanisms = 525327;

    /**
     * (0008,0410) SQ Range Matching Sequence
     */
    public static final int RangeMatchingSequence = 525328;

    /**
     * (0008,0411) SQ List of UID Matching Sequence
     */
    public static final int ListOfUIDMatchingSequence = 525329;

    /**
     * (0008,0412) SQ Empty Value Matching Sequence
     */
    public static final int EmptyValueMatchingSequence = 525330;

    /**
     * (0008,0413) SQ General Matching Sequence
     */
    public static final int GeneralMatchingSequence = 525331;

    /**
     * (0008,0414) US Requested Status Interval
     */
    public static final int RequestedStatusInterval = 525332;

    /**
     * (0008,0415) CS Retain Instances
     */
    public static final int RetainInstances = 525333;

    /**
     * (0008,0416) DT Expiration DateTime
     */
    public static final int ExpirationDateTime = 525334;

    /**
     * (0008,0417) CS Transaction Status
     */
    public static final int TransactionStatus = 525335;

    /**
     * (0008,0418) LT Transaction Status Comment
     */
    public static final int TransactionStatusComment = 525336;

    /**
     * (0008,0419) SQ File Set Access Sequence
     */
    public static final int FileSetAccessSequence = 525337;

    /**
     * (0008,041A) SQ File Access Sequence
     */
    public static final int FileAccessSequence = 525338;

    /**
     * (0008,041B) OB Record Key
     */
    public static final int RecordKey = 525339;

    /**
     * (0008,041C) OB Prior Record Key
     */
    public static final int PriorRecordKey = 525340;

    /**
     * (0008,041D) SQ Metadata Sequence
     */
    public static final int MetadataSequence = 525341;

    /**
     * (0008,041E) SQ Updated Metadata Sequence
     */
    public static final int UpdatedMetadataSequence = 525342;

    /**
     * (0008,041F) DT Study Update DateTime
     */
    public static final int StudyUpdateDateTime = 525343;

    /**
     * (0008,0420) SQ Inventory Access End Points Sequence
     */
    public static final int InventoryAccessEndPointsSequence = 525344;

    /**
     * (0008,0421) SQ Study Access End Points Sequence
     */
    public static final int StudyAccessEndPointsSequence = 525345;

    /**
     * (0008,0422) SQ Incorporated Inventory Instance Sequence
     */
    public static final int IncorporatedInventoryInstanceSequence = 525346;

    /**
     * (0008,0423) SQ Inventoried Studies Sequence
     */
    public static final int InventoriedStudiesSequence = 525347;

    /**
     * (0008,0424) SQ Inventoried Series Sequence
     */
    public static final int InventoriedSeriesSequence = 525348;

    /**
     * (0008,0425) SQ Inventoried Instances Sequence
     */
    public static final int InventoriedInstancesSequence = 525349;

    /**
     * (0008,0426) CS Inventory Completion Status
     */
    public static final int InventoryCompletionStatus = 525350;

    /**
     * (0008,0427) UL Number of Study Records in Instance
     */
    public static final int NumberOfStudyRecordsInInstance = 525351;

    /**
     * (0008,0428) UL Total Number of Study Records
     */
    public static final int TotalNumberOfStudyRecords = 525352;

    /**
     * (0008,0429) US Maximum Number of Records
     */
    public static final int MaximumNumberOfRecords = 525353;

    /**
     * (0008,1000) LO Network ID
     */
    public static final int NetworkID = 528384;

    /**
     * (0008,1010) SH Station Name
     */
    public static final int StationName = 528400;

    /**
     * (0008,1030) LO Study Description
     */
    public static final int StudyDescription = 528432;

    /**
     * (0008,1032) SQ Procedure Code Sequence
     */
    public static final int ProcedureCodeSequence = 528434;

    /**
     * (0008,103E) LO Series Description
     */
    public static final int SeriesDescription = 528446;

    /**
     * (0008,103F) SQ Series Description Code Sequence
     */
    public static final int SeriesDescriptionCodeSequence = 528447;

    /**
     * (0008,1040) LO Institutional Department Name
     */
    public static final int InstitutionalDepartmentName = 528448;

    /**
     * (0008,1041) SQ Institutional Department Type Code Sequence
     */
    public static final int InstitutionalDepartmentTypeCodeSequence = 528449;

    /**
     * (0008,1048) PN Physician(s) of Record
     */
    public static final int PhysiciansOfRecord = 528456;

    /**
     * (0008,1049) SQ Physician(s) of Record Identification Sequence
     */
    public static final int PhysiciansOfRecordIdentificationSequence = 528457;

    /**
     * (0008,1050) PN Performing Physician's Name
     */
    public static final int PerformingPhysicianName = 528464;

    /**
     * (0008,1052) SQ Performing Physician Identification Sequence
     */
    public static final int PerformingPhysicianIdentificationSequence = 528466;

    /**
     * (0008,1060) PN Name of Physician(s) Reading Study
     */
    public static final int NameOfPhysiciansReadingStudy = 528480;

    /**
     * (0008,1062) SQ Physician(s) Reading Study Identification Sequence
     */
    public static final int PhysiciansReadingStudyIdentificationSequence = 528482;

    /**
     * (0008,1070) PN Operator's Name
     */
    public static final int OperatorsName = 528496;

    /**
     * (0008,1072) SQ Operator Identification Sequence
     */
    public static final int OperatorIdentificationSequence = 528498;

    /**
     * (0008,1080) LO Admitting Diagnoses Description
     */
    public static final int AdmittingDiagnosesDescription = 528512;

    /**
     * (0008,1084) SQ Admitting Diagnoses Code Sequence
     */
    public static final int AdmittingDiagnosesCodeSequence = 528516;

    /**
     * (0008,1088) LO Pyramid Description
     */
    public static final int PyramidDescription = 528520;

    /**
     * (0008,1090) LO Manufacturer's Model Name
     */
    public static final int ManufacturerModelName = 528528;

    /**
     * (0008,1100) SQ Referenced Results Sequence
     */
    public static final int ReferencedResultsSequence = 528640;

    /**
     * (0008,1110) SQ Referenced Study Sequence
     */
    public static final int ReferencedStudySequence = 528656;

    /**
     * (0008,1111) SQ Referenced Performed Procedure Step Sequence
     */
    public static final int ReferencedPerformedProcedureStepSequence = 528657;

    /**
     * (0008,1112) SQ Referenced Instances by SOP Class Sequence
     */
    public static final int ReferencedInstancesBySOPClassSequence = 528658;

    /**
     * (0008,1115) SQ Referenced Series Sequence
     */
    public static final int ReferencedSeriesSequence = 528661;

    /**
     * (0008,1120) SQ Referenced Patient Sequence
     */
    public static final int ReferencedPatientSequence = 528672;

    /**
     * (0008,1125) SQ Referenced Visit Sequence
     */
    public static final int ReferencedVisitSequence = 528677;

    /**
     * (0008,1130) SQ Referenced Overlay Sequence
     */
    public static final int ReferencedOverlaySequence = 528688;

    /**
     * (0008,1134) SQ Referenced Stereometric Instance Sequence
     */
    public static final int ReferencedStereometricInstanceSequence = 528692;

    /**
     * (0008,113A) SQ Referenced Waveform Sequence
     */
    public static final int ReferencedWaveformSequence = 528698;

    /**
     * (0008,1140) SQ Referenced Image Sequence
     */
    public static final int ReferencedImageSequence = 528704;

    /**
     * (0008,1145) SQ Referenced Curve Sequence
     */
    public static final int ReferencedCurveSequence = 528709;

    /**
     * (0008,114A) SQ Referenced Instance Sequence
     */
    public static final int ReferencedInstanceSequence = 528714;

    /**
     * (0008,114B) SQ Referenced Real World Value Mapping Instance Sequence
     */
    public static final int ReferencedRealWorldValueMappingInstanceSequence = 528715;

    /**
     * (0008,1150) UI Referenced SOP Class UID
     */
    public static final int ReferencedSOPClassUID = 528720;

    /**
     * (0008,1155) UI Referenced SOP Instance UID
     */
    public static final int ReferencedSOPInstanceUID = 528725;

    /**
     * (0008,1156) SQ Definition Source Sequence
     */
    public static final int DefinitionSourceSequence = 528726;

    /**
     * (0008,115A) UI SOP Classes Supported
     */
    public static final int SOPClassesSupported = 528730;

    /**
     * (0008,1160) IS Referenced Frame Number
     */
    public static final int ReferencedFrameNumber = 528736;

    /**
     * (0008,1161) US Simple Frame List
     */
    public static final int SimpleFrameList = 528737;

    /**
     * (0008,1162) UL Calculated Frame List
     */
    public static final int CalculatedFrameList = 528738;

    /**
     * (0008,1163) FD Time Range
     */
    public static final int TimeRange = 528739;

    /**
     * (0008,1164) SQ Frame Extraction Sequence
     */
    public static final int FrameExtractionSequence = 528740;

    /**
     * (0008,1167) UI Multi-frame Source SOP Instance UID
     */
    public static final int MultiFrameSourceSOPInstanceUID = 528743;

    /**
     * (0008,1190) UR Retrieve URL
     */
    public static final int RetrieveURL = 528784;

    /**
     * (0008,1195) UI Transaction UID
     */
    public static final int TransactionUID = 528789;

    /**
     * (0008,1196) US Warning Reason
     */
    public static final int WarningReason = 528790;

    /**
     * (0008,1197) US Failure Reason
     */
    public static final int FailureReason = 528791;

    /**
     * (0008,1198) SQ Failed SOP Sequence
     */
    public static final int FailedSOPSequence = 528792;

    /**
     * (0008,1199) SQ Referenced SOP Sequence
     */
    public static final int ReferencedSOPSequence = 528793;

    /**
     * (0008,119A) SQ Other Failures Sequence
     */
    public static final int OtherFailuresSequence = 528794;

    /**
     * (0008,119B) SQ Failed Study Sequence
     */
    public static final int FailedStudySequence = 528795;

    /**
     * (0008,1250) SQ Studies Containing Other Referenced Instances Sequence
     */
    public static final int StudiesContainingOtherReferencedInstancesSequence = 528896;

    /**
     * (0008,1290) SQ Related Series Sequence
     */
    public static final int RelatedSeriesSequence = 528976;

    /**
     * (0008,2110) CS Lossy Image Compression (Retired)
     */
    public static final int LossyImageCompressionRetired = 532752;

    /**
     * (0008,2111) ST Derivation Description
     */
    public static final int DerivationDescription = 532753;

    /**
     * (0008,2112) SQ Source Image Sequence
     */
    public static final int SourceImageSequence = 532754;

    /**
     * (0008,2120) SH Stage Name
     */
    public static final int StageName = 532768;

    /**
     * (0008,2122) IS Stage Number
     */
    public static final int StageNumber = 532770;

    /**
     * (0008,2124) IS Number of Stages
     */
    public static final int NumberOfStages = 532772;

    /**
     * (0008,2127) SH View Name
     */
    public static final int ViewName = 532775;

    /**
     * (0008,2128) IS View Number
     */
    public static final int ViewNumber = 532776;

    /**
     * (0008,2129) IS Number of Event Timers
     */
    public static final int NumberOfEventTimers = 532777;

    /**
     * (0008,212A) IS Number of Views in Stage
     */
    public static final int NumberOfViewsInStage = 532778;

    /**
     * (0008,2130) DS Event Elapsed Time(s)
     */
    public static final int EventElapsedTimes = 532784;

    /**
     * (0008,2132) LO Event Timer Name(s)
     */
    public static final int EventTimerNames = 532786;

    /**
     * (0008,2133) SQ Event Timer Sequence
     */
    public static final int EventTimerSequence = 532787;

    /**
     * (0008,2134) FD Event Time Offset
     */
    public static final int EventTimeOffset = 532788;

    /**
     * (0008,2135) SQ Event Code Sequence
     */
    public static final int EventCodeSequence = 532789;

    /**
     * (0008,2142) IS Start Trim
     */
    public static final int StartTrim = 532802;

    /**
     * (0008,2143) IS Stop Trim
     */
    public static final int StopTrim = 532803;

    /**
     * (0008,2144) IS Recommended Display Frame Rate
     */
    public static final int RecommendedDisplayFrameRate = 532804;

    /**
     * (0008,2200) CS Transducer Position
     */
    public static final int TransducerPosition = 532992;

    /**
     * (0008,2204) CS Transducer Orientation
     */
    public static final int TransducerOrientation = 532996;

    /**
     * (0008,2208) CS Anatomic Structure
     */
    public static final int AnatomicStructure = 533000;

    /**
     * (0008,2218) SQ Anatomic Region Sequence
     */
    public static final int AnatomicRegionSequence = 533016;

    /**
     * (0008,2220) SQ Anatomic Region Modifier Sequence
     */
    public static final int AnatomicRegionModifierSequence = 533024;

    /**
     * (0008,2228) SQ Primary Anatomic Structure Sequence
     */
    public static final int PrimaryAnatomicStructureSequence = 533032;

    /**
     * (0008,2229) SQ Anatomic Structure, Space or Region Sequence
     */
    public static final int AnatomicStructureSpaceOrRegionSequence = 533033;

    /**
     * (0008,2230) SQ Primary Anatomic Structure Modifier Sequence
     */
    public static final int PrimaryAnatomicStructureModifierSequence = 533040;

    /**
     * (0008,2240) SQ Transducer Position Sequence
     */
    public static final int TransducerPositionSequence = 533056;

    /**
     * (0008,2242) SQ Transducer Position Modifier Sequence
     */
    public static final int TransducerPositionModifierSequence = 533058;

    /**
     * (0008,2244) SQ Transducer Orientation Sequence
     */
    public static final int TransducerOrientationSequence = 533060;

    /**
     * (0008,2246) SQ Transducer Orientation Modifier Sequence
     */
    public static final int TransducerOrientationModifierSequence = 533062;

    /**
     * (0008,2251) SQ Anatomic Structure Space or Region Code Sequence (Trial)
     */
    public static final int AnatomicStructureSpaceOrRegionCodeSequenceTrial = 533073;

    /**
     * (0008,2253) SQ Anatomic Portal of Entrance Code Sequence (Trial)
     */
    public static final int AnatomicPortalOfEntranceCodeSequenceTrial = 533075;

    /**
     * (0008,2255) SQ Anatomic Approach Direction Code Sequence (Trial)
     */
    public static final int AnatomicApproachDirectionCodeSequenceTrial = 533077;

    /**
     * (0008,2256) ST Anatomic Perspective Description (Trial)
     */
    public static final int AnatomicPerspectiveDescriptionTrial = 533078;

    /**
     * (0008,2257) SQ Anatomic Perspective Code Sequence (Trial)
     */
    public static final int AnatomicPerspectiveCodeSequenceTrial = 533079;

    /**
     * (0008,2258) ST Anatomic Location of Examining Instrument Description (Trial)
     */
    public static final int AnatomicLocationOfExaminingInstrumentDescriptionTrial = 533080;

    /**
     * (0008,2259) SQ Anatomic Location of Examining Instrument Code Sequence (Trial)
     */
    public static final int AnatomicLocationOfExaminingInstrumentCodeSequenceTrial = 533081;

    /**
     * (0008,225A) SQ Anatomic Structure Space or Region Modifier Code Sequence (Trial)
     */
    public static final int AnatomicStructureSpaceOrRegionModifierCodeSequenceTrial = 533082;

    /**
     * (0008,225C) SQ On Axis Background Anatomic Structure Code Sequence (Trial)
     */
    public static final int OnAxisBackgroundAnatomicStructureCodeSequenceTrial = 533084;

    /**
     * (0008,9001) SQ Alternate Representation Sequence
     */
    public static final int AlternateRepresentationSequence = 536577;

    /**
     * (0008,9002) UI Available Transfer Syntax UID
     */
    public static final int AvailableTransferSyntaxUID = 536578;

    /**
     * (0008,9010) UI Irradiation Event UID
     */
    public static final int IrradiationEventUID = 536592;

    /**
     * (0008,9011) SQ Source Irradiation Event Sequence
     */
    public static final int SourceIrradiationEventSequence = 536593;

    /**
     * (0008,9012) UI Radiopharmaceutical Administration Event UID
     */
    public static final int RadiopharmaceuticalAdministrationEventUID = 536594;

    /**
     * (0008,9200) ST Identifying Comments
     */
    public static final int IdentifyingComments = 540672;

    /**
     * (0008,9207) CS Frame Type
     */
    public static final int FrameType = 561159;

    /**
     * (0008,9292) SQ Referenced Image Evidence Sequence
     */
    public static final int ReferencedImageEvidenceSequence = 561298;

    /**
     * (0008,9401) SQ Referenced Raw Data Sequence
     */
    public static final int ReferencedRawDataSequence = 561441;

    /**
     * (0008,9403) UI Creator-Version UID
     */
    public static final int CreatorVersionUID = 561443;

    /**
     * (0008,9404) SQ Derivation Image Sequence
     */
    public static final int DerivationImageSequence = 561444;

    /**
     * (0008,9434) SQ Source Image Evidence Sequence
     */
    public static final int SourceImageEvidenceSequence = 561492;

    /**
     * (0008,9455) CS Pixel Presentation
     */
    public static final int PixelPresentation = 561669;

    /**
     * (0008,9456) CS Volumetric Properties
     */
    public static final int VolumetricProperties = 561670;

    /**
     * (0008,9457) CS Volume Based Calculation Technique
     */
    public static final int VolumeBasedCalculationTechnique = 561671;

    /**
     * (0008,9458) CS Complex Image Component
     */
    public static final int ComplexImageComponent = 561672;

    /**
     * (0008,9459) CS Acquisition Contrast
     */
    public static final int AcquisitionContrast = 561673;

    /**
     * (0008,9465) SQ Derivation Code Sequence
     */
    public static final int DerivationCodeSequence = 561685;

    /**
     * (0008,947F) SQ Referenced Presentation State Sequence
     */
    public static final int ReferencedPresentationStateSequence = 561719;

    /**
     * (0008,9610) SQ Referenced Other Plane Sequence
     */
    public static final int ReferencedOtherPlaneSequence = 562192;

    /**
     * (0008,9658) SQ Frame Display Sequence
     */
    public static final int FrameDisplaySequence = 562264;

    /**
     * (0008,9659) FL Recommended Display Frame Rate in Float
     */
    public static final int RecommendedDisplayFrameRateInFloat = 562265;

    /**
     * (0008,9660) CS Skip Frame Range Flag
     */
    public static final int SkipFrameRangeFlag = 562272;

    /**
     * (0010,0010) PN Patient's Name
     */
    public static final int PatientName = 1048592;

    /**
     * (0010,0020) LO Patient ID
     */
    public static final int PatientID = 1048608;

    /**
     * (0010,0021) LO Issuer of Patient ID
     */
    public static final int IssuerOfPatientID = 1048609;

    /**
     * (0010,0022) CS Type of Patient ID
     */
    public static final int TypeOfPatientID = 1048610;

    /**
     * (0010,0024) SQ Issuer of Patient ID Qualifiers Sequence
     */
    public static final int IssuerOfPatientIDQualifiersSequence = 1048612;

    /**
     * (0010,0026) SQ Source Patient Group Identification Sequence
     */
    public static final int SourcePatientGroupIdentificationSequence = 1048614;

    /**
     * (0010,0027) SQ Group of Patients Identification Sequence
     */
    public static final int GroupOfPatientsIdentificationSequence = 1048615;

    /**
     * (0010,0028) US Subject Relative Position in Image
     */
    public static final int SubjectRelativePositionInImage = 1048616;

    /**
     * (0010,0030) DA Patient's Birth Date
     */
    public static final int PatientBirthDate = 1048624;

    /**
     * (0010,0032) TM Patient's Birth Time
     */
    public static final int PatientBirthTime = 1048626;

    /**
     * (0010,0033) LO Patient's Birth Date in Alternative Calendar
     */
    public static final int PatientBirthDateInAlternativeCalendar = 1048627;

    /**
     * (0010,0034) LO Patient's Death Date in Alternative Calendar
     */
    public static final int PatientDeathDateInAlternativeCalendar = 1048628;

    /**
     * (0010,0035) CS Patient's Alternative Calendar
     */
    public static final int PatientAlternativeCalendar = 1048629;

    /**
     * (0010,0040) CS Patient's Sex
     */
    public static final int PatientSex = 1048640;

    /**
     * (0010,0050) SQ Patient's Insurance Plan Code Sequence
     */
    public static final int PatientInsurancePlanCodeSequence = 1048656;

    /**
     * (0010,0101) SQ Patient Primary Language Code Sequence
     */
    public static final int PatientPrimaryLanguageCodeSequence = 1048833;

    /**
     * (0010,0102) SQ Patient Primary Language Modifier Code Sequence
     */
    public static final int PatientPrimaryLanguageModifierCodeSequence = 1048834;

    /**
     * (0010,0200) CS Quality Control Subject
     */
    public static final int QualityControlSubject = 1049088;

    /**
     * (0010,0201) SQ Quality Control Subject Type Code Sequence
     */
    public static final int QualityControlSubjectTypeCodeSequence = 1049089;

    /**
     * (0010,0212) LO Strain Description
     */
    public static final int StrainDescription = 1049106;

    /**
     * (0010,0213) LO Strain Nomenclature
     */
    public static final int StrainNomenclature = 1049107;

    /**
     * (0010,0214) LO Strain Stock Number
     */
    public static final int StrainStockNumber = 1049108;

    /**
     * (0010,0215) SQ Strain Source Registry Code Sequence
     */
    public static final int StrainSourceRegistryCodeSequence = 1049109;

    /**
     * (0010,0216) SQ Strain Stock Sequence
     */
    public static final int StrainStockSequence = 1049110;

    /**
     * (0010,0217) LO Strain Source
     */
    public static final int StrainSource = 1049111;

    /**
     * (0010,0218) UT Strain Additional Information
     */
    public static final int StrainAdditionalInformation = 1049112;

    /**
     * (0010,0219) SQ Strain Code Sequence
     */
    public static final int StrainCodeSequence = 1049113;

    /**
     * (0010,0221) SQ Genetic Modifications Sequence
     */
    public static final int GeneticModificationsSequence = 1049121;

    /**
     * (0010,0222) LO Genetic Modifications Description
     */
    public static final int GeneticModificationsDescription = 1049122;

    /**
     * (0010,0223) LO Genetic Modifications Nomenclature
     */
    public static final int GeneticModificationsNomenclature = 1049123;

    /**
     * (0010,0229) SQ Genetic Modifications Code Sequence
     */
    public static final int GeneticModificationsCodeSequence = 1049129;

    /**
     * (0010,1000) LO Other Patient IDs
     */
    public static final int OtherPatientIDs = 1052672;

    /**
     * (0010,1001) PN Other Patient Names
     */
    public static final int OtherPatientNames = 1052673;

    /**
     * (0010,1002) SQ Other Patient IDs Sequence
     */
    public static final int OtherPatientIDsSequence = 1052674;

    /**
     * (0010,1005) PN Patient's Birth Name
     */
    public static final int PatientBirthName = 1052677;

    /**
     * (0010,1010) AS Patient's Age
     */
    public static final int PatientAge = 1052688;

    /**
     * (0010,1020) DS Patient's Size
     */
    public static final int PatientSize = 1052704;

    /**
     * (0010,1021) SQ Patient's Size Code Sequence
     */
    public static final int PatientSizeCodeSequence = 1052705;

    /**
     * (0010,1022) DS Patient's Body Mass Index
     */
    public static final int PatientBodyMassIndex = 1052706;

    /**
     * (0010,1023) DS Measured AP Dimension
     */
    public static final int MeasuredAPDimension = 1052707;

    /**
     * (0010,1024) DS Measured Lateral Dimension
     */
    public static final int MeasuredLateralDimension = 1052708;

    /**
     * (0010,1030) DS Patient's Weight
     */
    public static final int PatientWeight = 1052720;

    /**
     * (0010,1040) LO Patient's Address
     */
    public static final int PatientAddress = 1052736;

    /**
     * (0010,1050) LO Insurance Plan Identification
     */
    public static final int InsurancePlanIdentification = 1052752;

    /**
     * (0010,1060) PN Patient's Mother's Birth Name
     */
    public static final int PatientMotherBirthName = 1052768;

    /**
     * (0010,1080) LO Military Rank
     */
    public static final int MilitaryRank = 1052800;

    /**
     * (0010,1081) LO Branch of Service
     */
    public static final int BranchOfService = 1052801;

    /**
     * (0010,1090) LO Medical Record Locator
     */
    public static final int MedicalRecordLocator = 1052816;

    /**
     * (0010,1100) SQ Referenced Patient Photo Sequence
     */
    public static final int ReferencedPatientPhotoSequence = 1052928;

    /**
     * (0010,2000) LO Medical Alerts
     */
    public static final int MedicalAlerts = 1056768;

    /**
     * (0010,2110) LO Allergies
     */
    public static final int Allergies = 1057040;

    /**
     * (0010,2150) LO Country of Residence
     */
    public static final int CountryOfResidence = 1057104;

    /**
     * (0010,2152) LO Region of Residence
     */
    public static final int RegionOfResidence = 1057106;

    /**
     * (0010,2154) SH Patient's Telephone Numbers
     */
    public static final int PatientTelephoneNumbers = 1057108;

    /**
     * (0010,2155) LT Patient's Telecom Information
     */
    public static final int PatientTelecomInformation = 1057109;

    /**
     * (0010,2160) SH Ethnic Group
     */
    public static final int EthnicGroup = 1057120;

    /**
     * (0010,2180) SH Occupation
     */
    public static final int Occupation = 1057152;

    /**
     * (0010,21A0) CS Smoking Status
     */
    public static final int SmokingStatus = 1057184;

    /**
     * (0010,21B0) LT Additional Patient History
     */
    public static final int AdditionalPatientHistory = 1057200;

    /**
     * (0010,21C0) US Pregnancy Status
     */
    public static final int PregnancyStatus = 1057216;

    /**
     * (0010,21D0) DA Last Menstrual Date
     */
    public static final int LastMenstrualDate = 1057232;

    /**
     * (0010,21F0) LO Patient's Religious Preference
     */
    public static final int PatientReligiousPreference = 1057264;

    /**
     * (0010,2201) LO Patient Species Description
     */
    public static final int PatientSpeciesDescription = 1057281;

    /**
     * (0010,2202) SQ Patient Species Code Sequence
     */
    public static final int PatientSpeciesCodeSequence = 1057282;

    /**
     * (0010,2203) CS Patient's Sex Neutered
     */
    public static final int PatientSexNeutered = 1057283;

    /**
     * (0010,2210) CS Anatomical Orientation Type
     */
    public static final int AnatomicalOrientationType = 1057296;

    /**
     * (0010,2292) LO Patient Breed Description
     */
    public static final int PatientBreedDescription = 1057426;

    /**
     * (0010,2293) SQ Patient Breed Code Sequence
     */
    public static final int PatientBreedCodeSequence = 1057427;

    /**
     * (0010,2294) SQ Breed Registration Sequence
     */
    public static final int BreedRegistrationSequence = 1057428;

    /**
     * (0010,2295) LO Breed Registration Number
     */
    public static final int BreedRegistrationNumber = 1057429;

    /**
     * (0010,2296) SQ Breed Registry Code Sequence
     */
    public static final int BreedRegistryCodeSequence = 1057430;

    /**
     * (0010,2297) PN Responsible Person
     */
    public static final int ResponsiblePerson = 1057431;

    /**
     * (0010,2298) CS Responsible Person Role
     */
    public static final int ResponsiblePersonRole = 1057432;

    /**
     * (0010,2299) LO Responsible Organization
     */
    public static final int ResponsibleOrganization = 1057433;

    /**
     * (0010,4000) LT Patient Comments
     */
    public static final int PatientComments = 1064960;

    /**
     * (0010,9431) FL Examined Body Thickness
     */
    public static final int ExaminedBodyThickness = 1086513;

    /**
     * (0012,0010) LO Clinical Trial Sponsor Name
     */
    public static final int ClinicalTrialSponsorName = 1179664;

    /**
     * (0012,0020) LO Clinical Trial Protocol ID
     */
    public static final int ClinicalTrialProtocolID = 1179680;

    /**
     * (0012,0021) LO Clinical Trial Protocol Name
     */
    public static final int ClinicalTrialProtocolName = 1179681;

    /**
     * (0012,0022) LO Issuer of Clinical Trial Protocol ID
     */
    public static final int IssuerOfClinicalTrialProtocolID = 1179682;

    /**
     * (0012,0023) SQ Other Clinical Trial Protocol IDs Sequence
     */
    public static final int OtherClinicalTrialProtocolIDsSequence = 1179683;

    /**
     * (0012,0030) LO Clinical Trial Site ID
     */
    public static final int ClinicalTrialSiteID = 1179696;

    /**
     * (0012,0031) LO Clinical Trial Site Name
     */
    public static final int ClinicalTrialSiteName = 1179697;

    /**
     * (0012,0032) LO Issuer of Clinical Trial Site ID
     */
    public static final int IssuerOfClinicalTrialSiteID = 1179698;

    /**
     * (0012,0040) LO Clinical Trial Subject ID
     */
    public static final int ClinicalTrialSubjectID = 1179712;

    /**
     * (0012,0041) LO Issuer of Clinical Trial Subject ID
     */
    public static final int IssuerOfClinicalTrialSubjectID = 1179713;

    /**
     * (0012,0042) LO Clinical Trial Subject Reading ID
     */
    public static final int ClinicalTrialSubjectReadingID = 1179714;

    /**
     * (0012,0043) LO Issuer of Clinical Trial Subject Reading ID
     */
    public static final int IssuerOfClinicalTrialSubjectReadingID = 1179715;

    /**
     * (0012,0050) LO Clinical Trial Time Point ID
     */
    public static final int ClinicalTrialTimePointID = 1179728;

    /**
     * (0012,0051) ST Clinical Trial Time Point Description
     */
    public static final int ClinicalTrialTimePointDescription = 1179729;

    /**
     * (0012,0052) FD Longitudinal Temporal Offset from Event
     */
    public static final int LongitudinalTemporalOffsetFromEvent = 1179730;

    /**
     * (0012,0053) CS Longitudinal Temporal Event Type
     */
    public static final int LongitudinalTemporalEventType = 1179731;

    /**
     * (0012,0054) SQ Clinical Trial Time Point Type Code Sequence
     */
    public static final int ClinicalTrialTimePointTypeCodeSequence = 1179732;

    /**
     * (0012,0055) LO Issuer of Clinical Trial Time Point ID
     */
    public static final int IssuerOfClinicalTrialTimePointID = 1179733;

    /**
     * (0012,0060) LO Clinical Trial Coordinating Center Name
     */
    public static final int ClinicalTrialCoordinatingCenterName = 1179744;

    /**
     * (0012,0062) CS Patient Identity Removed
     */
    public static final int PatientIdentityRemoved = 1179746;

    /**
     * (0012,0063) LO De-identification Method
     */
    public static final int DeidentificationMethod = 1179747;

    /**
     * (0012,0064) SQ De-identification Method Code Sequence
     */
    public static final int DeidentificationMethodCodeSequence = 1179748;

    /**
     * (0012,0071) LO Clinical Trial Series ID
     */
    public static final int ClinicalTrialSeriesID = 1179761;

    /**
     * (0012,0072) LO Clinical Trial Series Description
     */
    public static final int ClinicalTrialSeriesDescription = 1179762;

    /**
     * (0012,0073) LO Issuer of Clinical Trial Series ID
     */
    public static final int IssuerOfClinicalTrialSeriesID = 1179763;

    /**
     * (0012,0081) LO Clinical Trial Protocol Ethics Committee Name
     */
    public static final int ClinicalTrialProtocolEthicsCommitteeName = 1179777;

    /**
     * (0012,0082) LO Clinical Trial Protocol Ethics Committee Approval Number
     */
    public static final int ClinicalTrialProtocolEthicsCommitteeApprovalNumber = 1179778;

    /**
     * (0012,0083) SQ Consent for Clinical Trial Use Sequence
     */
    public static final int ConsentForClinicalTrialUseSequence = 1179779;

    /**
     * (0012,0084) CS Distribution Type
     */
    public static final int DistributionType = 1179780;

    /**
     * (0012,0085) CS Consent for Distribution Flag
     */
    public static final int ConsentForDistributionFlag = 1179781;

    /**
     * (0012,0086) DA Ethics Committee Approval Effectiveness Start Date
     */
    public static final int EthicsCommitteeApprovalEffectivenessStartDate = 1179782;

    /**
     * (0012,0087) DA Ethics Committee Approval Effectiveness End Date
     */
    public static final int EthicsCommitteeApprovalEffectivenessEndDate = 1179783;

    /**
     * (0014,0023) ST CAD File Format
     */
    public static final int CADFileFormat = 1310755;

    /**
     * (0014,0024) ST Component Reference System
     */
    public static final int ComponentReferenceSystem = 1310756;

    /**
     * (0014,0025) ST Component Manufacturing Procedure
     */
    public static final int ComponentManufacturingProcedure = 1310757;

    /**
     * (0014,0028) ST Component Manufacturer
     */
    public static final int ComponentManufacturer = 1310760;

    /**
     * (0014,0030) DS Material Thickness
     */
    public static final int MaterialThickness = 1310768;

    /**
     * (0014,0032) DS Material Pipe Diameter
     */
    public static final int MaterialPipeDiameter = 1310770;

    /**
     * (0014,0034) DS Material Isolation Diameter
     */
    public static final int MaterialIsolationDiameter = 1310772;

    /**
     * (0014,0042) ST Material Grade
     */
    public static final int MaterialGrade = 1310786;

    /**
     * (0014,0044) ST Material Properties Description
     */
    public static final int MaterialPropertiesDescription = 1310788;

    /**
     * (0014,0045) ST Material Properties File Format (Retired)
     */
    public static final int MaterialPropertiesFileFormatRetired = 1310789;

    /**
     * (0014,0046) LT Material Notes
     */
    public static final int MaterialNotes = 1310790;

    /**
     * (0014,0050) CS Component Shape
     */
    public static final int ComponentShape = 1310800;

    /**
     * (0014,0052) CS Curvature Type
     */
    public static final int CurvatureType = 1310802;

    /**
     * (0014,0054) DS Outer Diameter
     */
    public static final int OuterDiameter = 1310804;

    /**
     * (0014,0056) DS Inner Diameter
     */
    public static final int InnerDiameter = 1310806;

    /**
     * (0014,0100) LO Component Welder IDs
     */
    public static final int ComponentWelderIDs = 1310976;

    /**
     * (0014,0101) CS Secondary Approval Status
     */
    public static final int SecondaryApprovalStatus = 1310977;

    /**
     * (0014,0102) DA Secondary Review Date
     */
    public static final int SecondaryReviewDate = 1310978;

    /**
     * (0014,0103) TM Secondary Review Time
     */
    public static final int SecondaryReviewTime = 1310979;

    /**
     * (0014,0104) PN Secondary Reviewer Name
     */
    public static final int SecondaryReviewerName = 1310980;

    /**
     * (0014,0105) ST Repair ID
     */
    public static final int RepairID = 1310981;

    /**
     * (0014,0106) SQ Multiple Component Approval Sequence
     */
    public static final int MultipleComponentApprovalSequence = 1310982;

    /**
     * (0014,0107) CS Other Approval Status
     */
    public static final int OtherApprovalStatus = 1310983;

    /**
     * (0014,0108) CS Other Secondary Approval Status
     */
    public static final int OtherSecondaryApprovalStatus = 1310984;

    /**
     * (0014,0200) SQ Data Element Label Sequence
     */
    public static final int DataElementLabelSequence = 1311232;

    /**
     * (0014,0201) SQ Data Element Label Item Sequence
     */
    public static final int DataElementLabelItemSequence = 1311233;

    /**
     * (0014,0202) AT Data Element
     */
    public static final int DataElement = 1311234;

    /**
     * (0014,0203) LO Data Element Name
     */
    public static final int DataElementName = 1311235;

    /**
     * (0014,0204) UT Data Element Description
     */
    public static final int DataElementDescription = 1311236;

    /**
     * (0014,0205) CS Data Element Conditionality
     */
    public static final int DataElementConditionality = 1311237;

    /**
     * (0014,0206) US Data Element Minimum Characters
     */
    public static final int DataElementMinimumCharacters = 1311238;

    /**
     * (0014,0207) US Data Element Maximum Characters
     */
    public static final int DataElementMaximumCharacters = 1311239;

    /**
     * (0014,1010) ST Actual Environmental Conditions
     */
    public static final int ActualEnvironmentalConditions = 1314832;

    /**
     * (0014,1020) DA Expiry Date
     */
    public static final int ExpiryDate = 1314848;

    /**
     * (0014,1040) ST Environmental Conditions
     */
    public static final int EnvironmentalConditions = 1314880;

    /**
     * (0014,2002) SQ Evaluator Sequence
     */
    public static final int EvaluatorSequence = 1318914;

    /**
     * (0014,2004) IS Evaluator Number
     */
    public static final int EvaluatorNumber = 1318916;

    /**
     * (0014,2006) PN Evaluator Name
     */
    public static final int EvaluatorName = 1318918;

    /**
     * (0014,2008) IS Evaluation Attempt
     */
    public static final int EvaluationAttempt = 1318920;

    /**
     * (0014,2012) SQ Indication Sequence
     */
    public static final int IndicationSequence = 1318930;

    /**
     * (0014,2014) IS Indication Number
     */
    public static final int IndicationNumber = 1318932;

    /**
     * (0014,2016) SH Indication Label
     */
    public static final int IndicationLabel = 1318934;

    /**
     * (0014,2018) ST Indication Description
     */
    public static final int IndicationDescription = 1318936;

    /**
     * (0014,201A) CS Indication Type
     */
    public static final int IndicationType = 1318938;

    /**
     * (0014,201C) CS Indication Disposition
     */
    public static final int IndicationDisposition = 1318940;

    /**
     * (0014,201E) SQ Indication ROI Sequence
     */
    public static final int IndicationROISequence = 1318942;

    /**
     * (0014,2030) SQ Indication Physical Property Sequence
     */
    public static final int IndicationPhysicalPropertySequence = 1318960;

    /**
     * (0014,2032) SH Property Label
     */
    public static final int PropertyLabel = 1318962;

    /**
     * (0014,2202) US Coordinate System Number of Axes
     */
    public static final int CoordinateSystemNumberOfAxes = 1319426;

    /**
     * (0014,2204) SQ Coordinate System Axes Sequence
     */
    public static final int CoordinateSystemAxesSequence = 1319428;

    /**
     * (0014,2206) ST Coordinate System Axis Description
     */
    public static final int CoordinateSystemAxisDescription = 1319430;

    /**
     * (0014,2208) CS Coordinate System Data Set Mapping
     */
    public static final int CoordinateSystemDataSetMapping = 1319432;

    /**
     * (0014,220A) US Coordinate System Axis Number
     */
    public static final int CoordinateSystemAxisNumber = 1319434;

    /**
     * (0014,220C) CS Coordinate System Axis Type
     */
    public static final int CoordinateSystemAxisType = 1319436;

    /**
     * (0014,220E) CS Coordinate System Axis Units
     */
    public static final int CoordinateSystemAxisUnits = 1319438;

    /**
     * (0014,2210) OB Coordinate System Axis Values
     */
    public static final int CoordinateSystemAxisValues = 1319440;

    /**
     * (0014,2220) SQ Coordinate System Transform Sequence
     */
    public static final int CoordinateSystemTransformSequence = 1319456;

    /**
     * (0014,2222) ST Transform Description
     */
    public static final int TransformDescription = 1319458;

    /**
     * (0014,2224) US Transform Number of Axes
     */
    public static final int TransformNumberOfAxes = 1319460;

    /**
     * (0014,2226) US Transform Order of Axes
     */
    public static final int TransformOrderOfAxes = 1319462;

    /**
     * (0014,2228) CS Transformed Axis Units
     */
    public static final int TransformedAxisUnits = 1319464;

    /**
     * (0014,222A) DS Coordinate System Transform Rotation and Scale Matrix
     */
    public static final int CoordinateSystemTransformRotationAndScaleMatrix = 1319466;

    /**
     * (0014,222C) DS Coordinate System Transform Translation Matrix
     */
    public static final int CoordinateSystemTransformTranslationMatrix = 1319468;

    /**
     * (0014,3011) DS Internal Detector Frame Time
     */
    public static final int InternalDetectorFrameTime = 1323025;

    /**
     * (0014,3012) DS Number of Frames Integrated
     */
    public static final int NumberOfFramesIntegrated = 1323026;

    /**
     * (0014,3020) SQ Detector Temperature Sequence
     */
    public static final int DetectorTemperatureSequence = 1323040;

    /**
     * (0014,3022) ST Sensor Name
     */
    public static final int SensorName = 1323042;

    /**
     * (0014,3024) DS Horizontal Offset of Sensor
     */
    public static final int HorizontalOffsetOfSensor = 1323044;

    /**
     * (0014,3026) DS Vertical Offset of Sensor
     */
    public static final int VerticalOffsetOfSensor = 1323046;

    /**
     * (0014,3028) DS Sensor Temperature
     */
    public static final int SensorTemperature = 1323048;

    /**
     * (0014,3040) SQ Dark Current Sequence
     */
    public static final int DarkCurrentSequence = 1323072;

    /**
     * (0014,3050) OB Dark Current Counts
     */
    public static final int DarkCurrentCounts = 1323088;

    /**
     * (0014,3060) SQ Gain Correction Reference Sequence
     */
    public static final int GainCorrectionReferenceSequence = 1323104;

    /**
     * (0014,3070) OB Air Counts
     */
    public static final int AirCounts = 1323120;

    /**
     * (0014,3071) DS KV Used in Gain Calibration
     */
    public static final int KVUsedInGainCalibration = 1323121;

    /**
     * (0014,3072) DS MA Used in Gain Calibration
     */
    public static final int MAUsedInGainCalibration = 1323122;

    /**
     * (0014,3073) DS Number of Frames Used for Integration
     */
    public static final int NumberOfFramesUsedForIntegration = 1323123;

    /**
     * (0014,3074) LO Filter Material Used in Gain Calibration
     */
    public static final int FilterMaterialUsedInGainCalibration = 1323124;

    /**
     * (0014,3075) DS Filter Thickness Used in Gain Calibration
     */
    public static final int FilterThicknessUsedInGainCalibration = 1323125;

    /**
     * (0014,3076) DA Date of Gain Calibration
     */
    public static final int DateOfGainCalibration = 1323126;

    /**
     * (0014,3077) TM Time of Gain Calibration
     */
    public static final int TimeOfGainCalibration = 1323127;

    /**
     * (0014,3080) OB Bad Pixel Image
     */
    public static final int BadPixelImage = 1323136;

    /**
     * (0014,3099) LT Calibration Notes
     */
    public static final int CalibrationNotes = 1323161;

    /**
     * (0014,3100) CS Linearity Correction Technique
     */
    public static final int LinearityCorrectionTechnique = 1323264;

    /**
     * (0014,3101) CS Beam Hardening Correction Technique
     */
    public static final int BeamHardeningCorrectionTechnique = 1323265;

    /**
     * (0014,4002) SQ Pulser Equipment Sequence
     */
    public static final int PulserEquipmentSequence = 1327106;

    /**
     * (0014,4004) CS Pulser Type
     */
    public static final int PulserType = 1327108;

    /**
     * (0014,4006) LT Pulser Notes
     */
    public static final int PulserNotes = 1327110;

    /**
     * (0014,4008) SQ Receiver Equipment Sequence
     */
    public static final int ReceiverEquipmentSequence = 1327112;

    /**
     * (0014,400A) CS Amplifier Type
     */
    public static final int AmplifierType = 1327114;

    /**
     * (0014,400C) LT Receiver Notes
     */
    public static final int ReceiverNotes = 1327116;

    /**
     * (0014,400E) SQ Pre-Amplifier Equipment Sequence
     */
    public static final int PreAmplifierEquipmentSequence = 1327118;

    /**
     * (0014,400F) LT Pre-Amplifier Notes
     */
    public static final int PreAmplifierNotes = 1327119;

    /**
     * (0014,4010) SQ Transmit Transducer Sequence
     */
    public static final int TransmitTransducerSequence = 1327120;

    /**
     * (0014,4011) SQ Receive Transducer Sequence
     */
    public static final int ReceiveTransducerSequence = 1327121;

    /**
     * (0014,4012) US Number of Elements
     */
    public static final int NumberOfElements = 1327122;

    /**
     * (0014,4013) CS Element Shape
     */
    public static final int ElementShape = 1327123;

    /**
     * (0014,4014) DS Element Dimension A
     */
    public static final int ElementDimensionA = 1327124;

    /**
     * (0014,4015) DS Element Dimension B
     */
    public static final int ElementDimensionB = 1327125;

    /**
     * (0014,4016) DS Element Pitch A
     */
    public static final int ElementPitchA = 1327126;

    /**
     * (0014,4017) DS Measured Beam Dimension A
     */
    public static final int MeasuredBeamDimensionA = 1327127;

    /**
     * (0014,4018) DS Measured Beam Dimension B
     */
    public static final int MeasuredBeamDimensionB = 1327128;

    /**
     * (0014,4019) DS Location of Measured Beam Diameter
     */
    public static final int LocationOfMeasuredBeamDiameter = 1327129;

    /**
     * (0014,401A) DS Nominal Frequency
     */
    public static final int NominalFrequency = 1327130;

    /**
     * (0014,401B) DS Measured Center Frequency
     */
    public static final int MeasuredCenterFrequency = 1327131;

    /**
     * (0014,401C) DS Measured Bandwidth
     */
    public static final int MeasuredBandwidth = 1327132;

    /**
     * (0014,401D) DS Element Pitch B
     */
    public static final int ElementPitchB = 1327133;

    /**
     * (0014,4020) SQ Pulser Settings Sequence
     */
    public static final int PulserSettingsSequence = 1327136;

    /**
     * (0014,4022) DS Pulse Width
     */
    public static final int PulseWidth = 1327138;

    /**
     * (0014,4024) DS Excitation Frequency
     */
    public static final int ExcitationFrequency = 1327140;

    /**
     * (0014,4026) CS Modulation Type
     */
    public static final int ModulationType = 1327142;

    /**
     * (0014,4028) DS Damping
     */
    public static final int Damping = 1327144;

    /**
     * (0014,4030) SQ Receiver Settings Sequence
     */
    public static final int ReceiverSettingsSequence = 1327152;

    /**
     * (0014,4031) DS Acquired Soundpath Length
     */
    public static final int AcquiredSoundpathLength = 1327153;

    /**
     * DICOM Tag for AcquisitionCompressionType.
     */
    public static final int AcquisitionCompressionType = 1327154;

    /**
     * DICOM Tag for AcquisitionSampleSize.
     */
    public static final int AcquisitionSampleSize = 1327155;

    /**
     * DICOM Tag for RectifierSmoothing.
     */
    public static final int RectifierSmoothing = 1327156;

    /**
     * DICOM Tag for DACSequence.
     */
    public static final int DACSequence = 1327157;

    /**
     * DICOM Tag for DACType.
     */
    public static final int DACType = 1327158;

    /**
     * DICOM Tag for DACGainPoints.
     */
    public static final int DACGainPoints = 1327160;

    /**
     * DICOM Tag for DACTimePoints.
     */
    public static final int DACTimePoints = 1327162;

    /**
     * DICOM Tag for DACAmplitude.
     */
    public static final int DACAmplitude = 1327164;

    /**
     * DICOM Tag for PreAmplifierSettingsSequence.
     */
    public static final int PreAmplifierSettingsSequence = 1327168;

    /**
     * DICOM Tag for TransmitTransducerSettingsSequence.
     */
    public static final int TransmitTransducerSettingsSequence = 1327184;

    /**
     * DICOM Tag for ReceiveTransducerSettingsSequence.
     */
    public static final int ReceiveTransducerSettingsSequence = 1327185;

    /**
     * DICOM Tag for IncidentAngle.
     */
    public static final int IncidentAngle = 1327186;

    /**
     * DICOM Tag for CouplingTechnique.
     */
    public static final int CouplingTechnique = 1327188;

    /**
     * DICOM Tag for CouplingMedium.
     */
    public static final int CouplingMedium = 1327190;

    /**
     * DICOM Tag for CouplingVelocity.
     */
    public static final int CouplingVelocity = 1327191;

    /**
     * DICOM Tag for ProbeCenterLocationX.
     */
    public static final int ProbeCenterLocationX = 1327192;

    /**
     * DICOM Tag for ProbeCenterLocationZ.
     */
    public static final int ProbeCenterLocationZ = 1327193;

    /**
     * DICOM Tag for SoundPathLength.
     */
    public static final int SoundPathLength = 1327194;

    /**
     * DICOM Tag for DelayLawIdentifier.
     */
    public static final int DelayLawIdentifier = 1327196;

    /**
     * DICOM Tag for GateSettingsSequence.
     */
    public static final int GateSettingsSequence = 1327200;

    /**
     * DICOM Tag for GateThreshold.
     */
    public static final int GateThreshold = 1327202;

    /**
     * DICOM Tag for VelocityOfSound.
     */
    public static final int VelocityOfSound = 1327204;

    /**
     * DICOM Tag for CalibrationSettingsSequence.
     */
    public static final int CalibrationSettingsSequence = 1327216;

    /**
     * DICOM Tag for CalibrationProcedure.
     */
    public static final int CalibrationProcedure = 1327218;

    /**
     * DICOM Tag for ProcedureVersion.
     */
    public static final int ProcedureVersion = 1327220;

    /**
     * DICOM Tag for ProcedureCreationDate.
     */
    public static final int ProcedureCreationDate = 1327222;

    /**
     * DICOM Tag for ProcedureExpirationDate.
     */
    public static final int ProcedureExpirationDate = 1327224;

    /**
     * DICOM Tag for ProcedureLastModifiedDate.
     */
    public static final int ProcedureLastModifiedDate = 1327226;

    /**
     * DICOM Tag for CalibrationTime.
     */
    public static final int CalibrationTime = 1327228;

    /**
     * DICOM Tag for CalibrationDate.
     */
    public static final int CalibrationDate = 1327230;

    /**
     * DICOM Tag for ProbeDriveEquipmentSequence.
     */
    public static final int ProbeDriveEquipmentSequence = 1327232;

    /**
     * DICOM Tag for DriveType.
     */
    public static final int DriveType = 1327233;

    /**
     * DICOM Tag for ProbeDriveNotes.
     */
    public static final int ProbeDriveNotes = 1327234;

    /**
     * DICOM Tag for DriveProbeSequence.
     */
    public static final int DriveProbeSequence = 1327235;

    /**
     * DICOM Tag for ProbeInductance.
     */
    public static final int ProbeInductance = 1327236;

    /**
     * DICOM Tag for ProbeResistance.
     */
    public static final int ProbeResistance = 1327237;

    /**
     * DICOM Tag for ReceiveProbeSequence.
     */
    public static final int ReceiveProbeSequence = 1327238;

    /**
     * DICOM Tag for ProbeDriveSettingsSequence.
     */
    public static final int ProbeDriveSettingsSequence = 1327239;

    /**
     * DICOM Tag for BridgeResistors.
     */
    public static final int BridgeResistors = 1327240;

    /**
     * DICOM Tag for ProbeOrientationAngle.
     */
    public static final int ProbeOrientationAngle = 1327241;

    /**
     * DICOM Tag for UserSelectedGainY.
     */
    public static final int UserSelectedGainY = 1327243;

    /**
     * DICOM Tag for UserSelectedPhase.
     */
    public static final int UserSelectedPhase = 1327244;

    /**
     * DICOM Tag for UserSelectedOffsetX.
     */
    public static final int UserSelectedOffsetX = 1327245;

    /**
     * DICOM Tag for UserSelectedOffsetY.
     */
    public static final int UserSelectedOffsetY = 1327246;

    /**
     * DICOM Tag for ChannelSettingsSequence.
     */
    public static final int ChannelSettingsSequence = 1327249;

    /**
     * DICOM Tag for ChannelThreshold.
     */
    public static final int ChannelThreshold = 1327250;

    /**
     * DICOM Tag for ScannerSettingsSequence.
     */
    public static final int ScannerSettingsSequence = 1327258;

    /**
     * DICOM Tag for ScanProcedure.
     */
    public static final int ScanProcedure = 1327259;

    /**
     * DICOM Tag for TranslationRateX.
     */
    public static final int TranslationRateX = 1327260;

    /**
     * DICOM Tag for TranslationRateY.
     */
    public static final int TranslationRateY = 1327261;

    /**
     * DICOM Tag for ChannelOverlap.
     */
    public static final int ChannelOverlap = 1327263;

    /**
     * DICOM Tag for ImageQualityIndicatorType.
     */
    public static final int ImageQualityIndicatorType = 1327264;

    /**
     * DICOM Tag for ImageQualityIndicatorMaterial.
     */
    public static final int ImageQualityIndicatorMaterial = 1327265;

    /**
     * DICOM Tag for ImageQualityIndicatorSize.
     */
    public static final int ImageQualityIndicatorSize = 1327266;

    /**
     * DICOM Tag for LINACEnergy.
     */
    public static final int LINACEnergy = 1331202;

    /**
     * DICOM Tag for LINACOutput.
     */
    public static final int LINACOutput = 1331204;

    /**
     * DICOM Tag for ActiveAperture.
     */
    public static final int ActiveAperture = 1331456;

    /**
     * DICOM Tag for TotalAperture.
     */
    public static final int TotalAperture = 1331457;

    /**
     * DICOM Tag for ApertureElevation.
     */
    public static final int ApertureElevation = 1331458;

    /**
     * DICOM Tag for MainLobeAngle.
     */
    public static final int MainLobeAngle = 1331459;

    /**
     * DICOM Tag for MainRoofAngle.
     */
    public static final int MainRoofAngle = 1331460;

    /**
     * DICOM Tag for ConnectorType.
     */
    public static final int ConnectorType = 1331461;

    /**
     * DICOM Tag for WedgeModelNumber.
     */
    public static final int WedgeModelNumber = 1331462;

    /**
     * DICOM Tag for WedgeAngleFloat.
     */
    public static final int WedgeAngleFloat = 1331463;

    /**
     * DICOM Tag for WedgeRoofAngle.
     */
    public static final int WedgeRoofAngle = 1331464;

    /**
     * DICOM Tag for WedgeElement1Position.
     */
    public static final int WedgeElement1Position = 1331465;

    /**
     * DICOM Tag for WedgeMaterialVelocity.
     */
    public static final int WedgeMaterialVelocity = 1331466;

    /**
     * DICOM Tag for WedgeMaterial.
     */
    public static final int WedgeMaterial = 1331467;

    /**
     * DICOM Tag for WedgeOffsetZ.
     */
    public static final int WedgeOffsetZ = 1331468;

    /**
     * DICOM Tag for WedgeOriginOffsetX.
     */
    public static final int WedgeOriginOffsetX = 1331469;

    /**
     * DICOM Tag for WedgeTimeDelay.
     */
    public static final int WedgeTimeDelay = 1331470;

    /**
     * DICOM Tag for WedgeName.
     */
    public static final int WedgeName = 1331471;

    /**
     * DICOM Tag for WedgeManufacturerName.
     */
    public static final int WedgeManufacturerName = 1331472;

    /**
     * DICOM Tag for WedgeDescription.
     */
    public static final int WedgeDescription = 1331473;

    /**
     * DICOM Tag for NominalBeamAngle.
     */
    public static final int NominalBeamAngle = 1331474;

    /**
     * DICOM Tag for WedgeOffsetX.
     */
    public static final int WedgeOffsetX = 1331475;

    /**
     * DICOM Tag for WedgeOffsetY.
     */
    public static final int WedgeOffsetY = 1331476;

    /**
     * DICOM Tag for WedgeTotalLength.
     */
    public static final int WedgeTotalLength = 1331477;

    /**
     * DICOM Tag for WedgeInContactLength.
     */
    public static final int WedgeInContactLength = 1331478;

    /**
     * DICOM Tag for WedgeFrontGap.
     */
    public static final int WedgeFrontGap = 1331479;

    /**
     * DICOM Tag for WedgeTotalHeight.
     */
    public static final int WedgeTotalHeight = 1331480;

    /**
     * DICOM Tag for WedgeFrontHeight.
     */
    public static final int WedgeFrontHeight = 1331481;

    /**
     * DICOM Tag for WedgeRearHeight.
     */
    public static final int WedgeRearHeight = 1331482;

    /**
     * DICOM Tag for WedgeTotalWidth.
     */
    public static final int WedgeTotalWidth = 1331483;

    /**
     * DICOM Tag for WedgeInContactWidth.
     */
    public static final int WedgeInContactWidth = 1331484;

    /**
     * DICOM Tag for WedgeChamferHeight.
     */
    public static final int WedgeChamferHeight = 1331485;

    /**
     * DICOM Tag for WedgeCurve.
     */
    public static final int WedgeCurve = 1331486;

    /**
     * DICOM Tag for RadiusAlongWedge.
     */
    public static final int RadiusAlongWedge = 1331487;

    /**
     * DICOM Tag for WhitePoint.
     */
    public static final int WhitePoint = 1441793;

    /**
     * DICOM Tag for PrimaryChromaticities.
     */
    public static final int PrimaryChromaticities = 1441794;

    /**
     * DICOM Tag for BatteryLevel.
     */
    public static final int BatteryLevel = 1441795;

    /**
     * DICOM Tag for ExposureTimeInSeconds.
     */
    public static final int ExposureTimeInSeconds = 1441796;

    /**
     * DICOM Tag for FNumber.
     */
    public static final int FNumber = 1441797;

    /**
     * DICOM Tag for OECFRows.
     */
    public static final int OECFRows = 1441798;

    /**
     * DICOM Tag for OECFColumns.
     */
    public static final int OECFColumns = 1441799;

    /**
     * DICOM Tag for OECFColumnNames.
     */
    public static final int OECFColumnNames = 1441800;

    /**
     * DICOM Tag for OECFValues.
     */
    public static final int OECFValues = 1441801;

    /**
     * DICOM Tag for SpatialFrequencyResponseRows.
     */
    public static final int SpatialFrequencyResponseRows = 1441802;

    /**
     * DICOM Tag for SpatialFrequencyResponseColumns.
     */
    public static final int SpatialFrequencyResponseColumns = 1441803;

    /**
     * DICOM Tag for SpatialFrequencyResponseColumnNames.
     */
    public static final int SpatialFrequencyResponseColumnNames = 1441804;

    /**
     * DICOM Tag for SpatialFrequencyResponseValues.
     */
    public static final int SpatialFrequencyResponseValues = 1441805;

    /**
     * DICOM Tag for ColorFilterArrayPatternRows.
     */
    public static final int ColorFilterArrayPatternRows = 1441806;

    /**
     * DICOM Tag for ColorFilterArrayPatternColumns.
     */
    public static final int ColorFilterArrayPatternColumns = 1441807;

    /**
     * DICOM Tag for ColorFilterArrayPatternValues.
     */
    public static final int ColorFilterArrayPatternValues = 1441808;

    /**
     * DICOM Tag for FlashFiringStatus.
     */
    public static final int FlashFiringStatus = 1441809;

    /**
     * DICOM Tag for FlashReturnStatus.
     */
    public static final int FlashReturnStatus = 1441810;

    /**
     * DICOM Tag for FlashMode.
     */
    public static final int FlashMode = 1441811;

    /**
     * DICOM Tag for FlashFunctionPresent.
     */
    public static final int FlashFunctionPresent = 1441812;

    /**
     * DICOM Tag for FlashRedEyeMode.
     */
    public static final int FlashRedEyeMode = 1441813;

    /**
     * DICOM Tag for ExposureProgram.
     */
    public static final int ExposureProgram = 1441814;

    /**
     * DICOM Tag for SpectralSensitivity.
     */
    public static final int SpectralSensitivity = 1441815;

    /**
     * DICOM Tag for PhotographicSensitivity.
     */
    public static final int PhotographicSensitivity = 1441816;

    /**
     * DICOM Tag for SelfTimerMode.
     */
    public static final int SelfTimerMode = 1441817;

    /**
     * DICOM Tag for SensitivityType.
     */
    public static final int SensitivityType = 1441818;

    /**
     * DICOM Tag for StandardOutputSensitivity.
     */
    public static final int StandardOutputSensitivity = 1441819;

    /**
     * DICOM Tag for RecommendedExposureIndex.
     */
    public static final int RecommendedExposureIndex = 1441820;

    /**
     * DICOM Tag for ISOSpeed.
     */
    public static final int ISOSpeed = 1441821;

    /**
     * DICOM Tag for ISOSpeedLatitudeyyy.
     */
    public static final int ISOSpeedLatitudeyyy = 1441822;

    /**
     * DICOM Tag for ISOSpeedLatitudezzz.
     */
    public static final int ISOSpeedLatitudezzz = 1441823;

    /**
     * DICOM Tag for EXIFVersion.
     */
    public static final int EXIFVersion = 1441824;

    /**
     * DICOM Tag for ShutterSpeedValue.
     */
    public static final int ShutterSpeedValue = 1441825;

    /**
     * DICOM Tag for ApertureValue.
     */
    public static final int ApertureValue = 1441826;

    /**
     * DICOM Tag for BrightnessValue.
     */
    public static final int BrightnessValue = 1441827;

    /**
     * DICOM Tag for ExposureBiasValue.
     */
    public static final int ExposureBiasValue = 1441828;

    /**
     * DICOM Tag for MaxApertureValue.
     */
    public static final int MaxApertureValue = 1441829;

    /**
     * DICOM Tag for SubjectDistance.
     */
    public static final int SubjectDistance = 1441830;

    /**
     * DICOM Tag for MeteringMode.
     */
    public static final int MeteringMode = 1441831;

    /**
     * DICOM Tag for LightSource.
     */
    public static final int LightSource = 1441832;

    /**
     * DICOM Tag for FocalLength.
     */
    public static final int FocalLength = 1441833;

    /**
     * DICOM Tag for SubjectArea.
     */
    public static final int SubjectArea = 1441834;

    /**
     * DICOM Tag for MakerNote.
     */
    public static final int MakerNote = 1441835;

    /**
     * DICOM Tag for Temperature.
     */
    public static final int Temperature = 1441840;

    /**
     * DICOM Tag for Humidity.
     */
    public static final int Humidity = 1441841;

    /**
     * DICOM Tag for Pressure.
     */
    public static final int Pressure = 1441842;

    /**
     * DICOM Tag for WaterDepth.
     */
    public static final int WaterDepth = 1441843;

    /**
     * DICOM Tag for Acceleration.
     */
    public static final int Acceleration = 1441844;

    /**
     * DICOM Tag for CameraElevationAngle.
     */
    public static final int CameraElevationAngle = 1441845;

    /**
     * DICOM Tag for FlashEnergy.
     */
    public static final int FlashEnergy = 1441846;

    /**
     * DICOM Tag for SubjectLocation.
     */
    public static final int SubjectLocation = 1441847;

    /**
     * DICOM Tag for PhotographicExposureIndex.
     */
    public static final int PhotographicExposureIndex = 1441848;

    /**
     * DICOM Tag for SensingMethod.
     */
    public static final int SensingMethod = 1441849;

    /**
     * DICOM Tag for FileSource.
     */
    public static final int FileSource = 1441850;

    /**
     * DICOM Tag for SceneType.
     */
    public static final int SceneType = 1441851;

    /**
     * DICOM Tag for CustomRendered.
     */
    public static final int CustomRendered = 1441857;

    /**
     * DICOM Tag for ExposureMode.
     */
    public static final int ExposureMode = 1441858;

    /**
     * DICOM Tag for WhiteBalance.
     */
    public static final int WhiteBalance = 1441859;

    /**
     * DICOM Tag for DigitalZoomRatio.
     */
    public static final int DigitalZoomRatio = 1441860;

    /**
     * DICOM Tag for FocalLengthIn35mmFilm.
     */
    public static final int FocalLengthIn35mmFilm = 1441861;

    /**
     * DICOM Tag for SceneCaptureType.
     */
    public static final int SceneCaptureType = 1441862;

    /**
     * DICOM Tag for GainControl.
     */
    public static final int GainControl = 1441863;

    /**
     * DICOM Tag for Contrast.
     */
    public static final int Contrast = 1441864;

    /**
     * DICOM Tag for Saturation.
     */
    public static final int Saturation = 1441865;

    /**
     * DICOM Tag for Sharpness.
     */
    public static final int Sharpness = 1441866;

    /**
     * DICOM Tag for DeviceSettingDescription.
     */
    public static final int DeviceSettingDescription = 1441867;

    /**
     * DICOM Tag for SubjectDistanceRange.
     */
    public static final int SubjectDistanceRange = 1441868;

    /**
     * DICOM Tag for CameraOwnerName.
     */
    public static final int CameraOwnerName = 1441869;

    /**
     * DICOM Tag for LensSpecification.
     */
    public static final int LensSpecification = 1441870;

    /**
     * DICOM Tag for LensMake.
     */
    public static final int LensMake = 1441871;

    /**
     * DICOM Tag for LensModel.
     */
    public static final int LensModel = 1441872;

    /**
     * DICOM Tag for LensSerialNumber.
     */
    public static final int LensSerialNumber = 1441873;

    /**
     * DICOM Tag for InteroperabilityIndex.
     */
    public static final int InteroperabilityIndex = 1441889;

    /**
     * DICOM Tag for InteroperabilityVersion.
     */
    public static final int InteroperabilityVersion = 1441890;

    /**
     * DICOM Tag for GPSVersionID.
     */
    public static final int GPSVersionID = 1441904;

    /**
     * DICOM Tag for GPSLatitudeRef.
     */
    public static final int GPSLatitudeRef = 1441905;

    /**
     * DICOM Tag for GPSLatitude.
     */
    public static final int GPSLatitude = 1441906;

    /**
     * DICOM Tag for GPSLongitudeRef.
     */
    public static final int GPSLongitudeRef = 1441907;

    /**
     * DICOM Tag for GPSLongitude.
     */
    public static final int GPSLongitude = 1441908;

    /**
     * DICOM Tag for GPSAltitudeRef.
     */
    public static final int GPSAltitudeRef = 1441909;

    /**
     * DICOM Tag for GPSAltitude.
     */
    public static final int GPSAltitude = 1441910;

    /**
     * DICOM Tag for GPSTimeStamp.
     */
    public static final int GPSTimeStamp = 1441911;

    /**
     * DICOM Tag for GPSSatellites.
     */
    public static final int GPSSatellites = 1441912;

    /**
     * DICOM Tag for GPSStatus.
     */
    public static final int GPSStatus = 1441913;

    /**
     * DICOM Tag for GPSMeasureMode.
     */
    public static final int GPSMeasureMode = 1441914;

    /**
     * DICOM Tag for GPSDOP.
     */
    public static final int GPSDOP = 1441915;

    /**
     * DICOM Tag for GPSSpeedRef.
     */
    public static final int GPSSpeedRef = 1441916;

    /**
     * DICOM Tag for GPSSpeed.
     */
    public static final int GPSSpeed = 1441917;

    /**
     * DICOM Tag for GPSTrackRef.
     */
    public static final int GPSTrackRef = 1441918;

    /**
     * DICOM Tag for GPSTrack.
     */
    public static final int GPSTrack = 1441919;

    /**
     * DICOM Tag for GPSImgDirectionRef.
     */
    public static final int GPSImgDirectionRef = 1441920;

    /**
     * DICOM Tag for GPSImgDirection.
     */
    public static final int GPSImgDirection = 1441921;

    /**
     * DICOM Tag for GPSMapDatum.
     */
    public static final int GPSMapDatum = 1441922;

    /**
     * DICOM Tag for GPSDestLatitudeRef.
     */
    public static final int GPSDestLatitudeRef = 1441923;

    /**
     * DICOM Tag for GPSDestLatitude.
     */
    public static final int GPSDestLatitude = 1441924;

    /**
     * DICOM Tag for GPSDestLongitudeRef.
     */
    public static final int GPSDestLongitudeRef = 1441925;

    /**
     * DICOM Tag for GPSDestLongitude.
     */
    public static final int GPSDestLongitude = 1441926;

    /**
     * DICOM Tag for GPSDestBearingRef.
     */
    public static final int GPSDestBearingRef = 1441927;

    /**
     * DICOM Tag for GPSDestBearing.
     */
    public static final int GPSDestBearing = 1441928;

    /**
     * DICOM Tag for GPSDestDistanceRef.
     */
    public static final int GPSDestDistanceRef = 1441929;

    /**
     * DICOM Tag for GPSDestDistance.
     */
    public static final int GPSDestDistance = 1441930;

    /**
     * DICOM Tag for GPSProcessingMethod.
     */
    public static final int GPSProcessingMethod = 1441931;

    /**
     * DICOM Tag for GPSAreaInformation.
     */
    public static final int GPSAreaInformation = 1441932;

    /**
     * DICOM Tag for GPSDateStamp.
     */
    public static final int GPSDateStamp = 1441933;

    /**
     * DICOM Tag for GPSDifferential.
     */
    public static final int GPSDifferential = 1441934;

    /**
     * DICOM Tag for LightSourcePolarization.
     */
    public static final int LightSourcePolarization = 1445889;

    /**
     * DICOM Tag for EmitterColorTemperature.
     */
    public static final int EmitterColorTemperature = 1445890;

    /**
     * DICOM Tag for ContactMethod.
     */
    public static final int ContactMethod = 1445891;

    /**
     * DICOM Tag for ImmersionMedia.
     */
    public static final int ImmersionMedia = 1445892;

    /**
     * DICOM Tag for OpticalMagnificationFactor.
     */
    public static final int OpticalMagnificationFactor = 1445893;

    /**
     * DICOM Tag for ContrastBolusAgent.
     */
    public static final int ContrastBolusAgent = 1572880;

    /**
     * DICOM Tag for ContrastBolusAgentSequence.
     */
    public static final int ContrastBolusAgentSequence = 1572882;

    /**
     * DICOM Tag for ContrastBolusT1Relaxivity.
     */
    public static final int ContrastBolusT1Relaxivity = 1572883;

    /**
     * DICOM Tag for ContrastBolusAdministrationRouteSequence.
     */
    public static final int ContrastBolusAdministrationRouteSequence = 1572884;

    /**
     * DICOM Tag for BodyPartExamined.
     */
    public static final int BodyPartExamined = 1572885;

    /**
     * DICOM Tag for ScanningSequence.
     */
    public static final int ScanningSequence = 1572896;

    /**
     * DICOM Tag for SequenceVariant.
     */
    public static final int SequenceVariant = 1572897;

    /**
     * DICOM Tag for ScanOptions.
     */
    public static final int ScanOptions = 1572898;

    /**
     * DICOM Tag for MRAcquisitionType.
     */
    public static final int MRAcquisitionType = 1572899;

    /**
     * DICOM Tag for SequenceName.
     */
    public static final int SequenceName = 1572900;

    /**
     * DICOM Tag for AngioFlag.
     */
    public static final int AngioFlag = 1572901;

    /**
     * DICOM Tag for InterventionDrugInformationSequence.
     */
    public static final int InterventionDrugInformationSequence = 1572902;

    /**
     * DICOM Tag for InterventionDrugStopTime.
     */
    public static final int InterventionDrugStopTime = 1572903;

    /**
     * DICOM Tag for InterventionDrugDose.
     */
    public static final int InterventionDrugDose = 1572904;

    /**
     * DICOM Tag for InterventionDrugCodeSequence.
     */
    public static final int InterventionDrugCodeSequence = 1572905;

    /**
     * DICOM Tag for AdditionalDrugSequence.
     */
    public static final int AdditionalDrugSequence = 1572906;

    /**
     * DICOM Tag for Radionuclide.
     */
    public static final int Radionuclide = 1572912;

    /**
     * DICOM Tag for Radiopharmaceutical.
     */
    public static final int Radiopharmaceutical = 1572913;

    /**
     * DICOM Tag for EnergyWindowCenterline.
     */
    public static final int EnergyWindowCenterline = 1572914;

    /**
     * DICOM Tag for EnergyWindowTotalWidth.
     */
    public static final int EnergyWindowTotalWidth = 1572915;

    /**
     * DICOM Tag for InterventionDrugName.
     */
    public static final int InterventionDrugName = 1572916;

    /**
     * DICOM Tag for InterventionDrugStartTime.
     */
    public static final int InterventionDrugStartTime = 1572917;

    /**
     * DICOM Tag for InterventionSequence.
     */
    public static final int InterventionSequence = 1572918;

    /**
     * DICOM Tag for TherapyType.
     */
    public static final int TherapyType = 1572919;

    /**
     * DICOM Tag for InterventionStatus.
     */
    public static final int InterventionStatus = 1572920;

    /**
     * DICOM Tag for TherapyDescription.
     */
    public static final int TherapyDescription = 1572921;

    /**
     * DICOM Tag for InterventionDescription.
     */
    public static final int InterventionDescription = 1572922;

    /**
     * DICOM Tag for CineRate.
     */
    public static final int CineRate = 1572928;

    /**
     * DICOM Tag for InitialCineRunState.
     */
    public static final int InitialCineRunState = 1572930;

    /**
     * DICOM Tag for SliceThickness.
     */
    public static final int SliceThickness = 1572944;

    /**
     * DICOM Tag for KVP.
     */
    public static final int KVP = 1572960;

    /**
     * DICOM Tag for CountsAccumulated.
     */
    public static final int CountsAccumulated = 1572976;

    /**
     * DICOM Tag for AcquisitionTerminationCondition.
     */
    public static final int AcquisitionTerminationCondition = 1572977;

    /**
     * DICOM Tag for EffectiveDuration.
     */
    public static final int EffectiveDuration = 1572978;

    /**
     * DICOM Tag for AcquisitionStartCondition.
     */
    public static final int AcquisitionStartCondition = 1572979;

    /**
     * DICOM Tag for AcquisitionStartConditionData.
     */
    public static final int AcquisitionStartConditionData = 1572980;

    /**
     * DICOM Tag for AcquisitionTerminationConditionData.
     */
    public static final int AcquisitionTerminationConditionData = 1572981;

    /**
     * DICOM Tag for RepetitionTime.
     */
    public static final int RepetitionTime = 1572992;

    /**
     * DICOM Tag for EchoTime.
     */
    public static final int EchoTime = 1572993;

    /**
     * DICOM Tag for InversionTime.
     */
    public static final int InversionTime = 1572994;

    /**
     * DICOM Tag for NumberOfAverages.
     */
    public static final int NumberOfAverages = 1572995;

    /**
     * DICOM Tag for ImagingFrequency.
     */
    public static final int ImagingFrequency = 1572996;

    /**
     * DICOM Tag for ImagedNucleus.
     */
    public static final int ImagedNucleus = 1572997;

    /**
     * DICOM Tag for EchoNumbers.
     */
    public static final int EchoNumbers = 1572998;

    /**
     * DICOM Tag for MagneticFieldStrength.
     */
    public static final int MagneticFieldStrength = 1572999;

    /**
     * DICOM Tag for SpacingBetweenSlices.
     */
    public static final int SpacingBetweenSlices = 1573000;

    /**
     * DICOM Tag for NumberOfPhaseEncodingSteps.
     */
    public static final int NumberOfPhaseEncodingSteps = 1573001;

    /**
     * DICOM Tag for DataCollectionDiameter.
     */
    public static final int DataCollectionDiameter = 1573008;

    /**
     * DICOM Tag for EchoTrainLength.
     */
    public static final int EchoTrainLength = 1573009;

    /**
     * DICOM Tag for PercentSampling.
     */
    public static final int PercentSampling = 1573011;

    /**
     * DICOM Tag for PercentPhaseFieldOfView.
     */
    public static final int PercentPhaseFieldOfView = 1573012;

    /**
     * DICOM Tag for PixelBandwidth.
     */
    public static final int PixelBandwidth = 1573013;

    /**
     * DICOM Tag for DeviceSerialNumber.
     */
    public static final int DeviceSerialNumber = 1576960;

    /**
     * DICOM Tag for DeviceUID.
     */
    public static final int DeviceUID = 1576962;

    /**
     * DICOM Tag for DeviceID.
     */
    public static final int DeviceID = 1576963;

    /**
     * DICOM Tag for PlateID.
     */
    public static final int PlateID = 1576964;

    /**
     * DICOM Tag for GeneratorID.
     */
    public static final int GeneratorID = 1576965;

    /**
     * DICOM Tag for GridID.
     */
    public static final int GridID = 1576966;

    /**
     * DICOM Tag for CassetteID.
     */
    public static final int CassetteID = 1576967;

    /**
     * DICOM Tag for GantryID.
     */
    public static final int GantryID = 1576968;

    /**
     * DICOM Tag for UniqueDeviceIdentifier.
     */
    public static final int UniqueDeviceIdentifier = 1576969;

    /**
     * DICOM Tag for UDISequence.
     */
    public static final int UDISequence = 1576970;

    /**
     * DICOM Tag for ManufacturerDeviceClassUID.
     */
    public static final int ManufacturerDeviceClassUID = 1576971;

    /**
     * DICOM Tag for SecondaryCaptureDeviceID.
     */
    public static final int SecondaryCaptureDeviceID = 1576976;

    /**
     * DICOM Tag for HardcopyCreationDeviceID.
     */
    public static final int HardcopyCreationDeviceID = 1576977;

    /**
     * DICOM Tag for DateOfSecondaryCapture.
     */
    public static final int DateOfSecondaryCapture = 1576978;

    /**
     * DICOM Tag for TimeOfSecondaryCapture.
     */
    public static final int TimeOfSecondaryCapture = 1576980;

    /**
     * DICOM Tag for SecondaryCaptureDeviceManufacturer.
     */
    public static final int SecondaryCaptureDeviceManufacturer = 1576982;

    /**
     * DICOM Tag for HardcopyDeviceManufacturer.
     */
    public static final int HardcopyDeviceManufacturer = 1576983;

    /**
     * DICOM Tag for SecondaryCaptureDeviceManufacturerModelName.
     */
    public static final int SecondaryCaptureDeviceManufacturerModelName = 1576984;

    /**
     * DICOM Tag for SecondaryCaptureDeviceSoftwareVersions.
     */
    public static final int SecondaryCaptureDeviceSoftwareVersions = 1576985;

    /**
     * DICOM Tag for HardcopyDeviceSoftwareVersion.
     */
    public static final int HardcopyDeviceSoftwareVersion = 1576986;

    /**
     * DICOM Tag for HardcopyDeviceManufacturerModelName.
     */
    public static final int HardcopyDeviceManufacturerModelName = 1576987;

    /**
     * DICOM Tag for SoftwareVersions.
     */
    public static final int SoftwareVersions = 1576992;

    /**
     * DICOM Tag for VideoImageFormatAcquired.
     */
    public static final int VideoImageFormatAcquired = 1576994;

    /**
     * DICOM Tag for DigitalImageFormatAcquired.
     */
    public static final int DigitalImageFormatAcquired = 1576995;

    /**
     * DICOM Tag for ProtocolName.
     */
    public static final int ProtocolName = 1577008;

    /**
     * DICOM Tag for ContrastBolusRoute.
     */
    public static final int ContrastBolusRoute = 1577024;

    /**
     * DICOM Tag for ContrastBolusVolume.
     */
    public static final int ContrastBolusVolume = 1577025;

    /**
     * DICOM Tag for ContrastBolusStartTime.
     */
    public static final int ContrastBolusStartTime = 1577026;

    /**
     * DICOM Tag for ContrastBolusStopTime.
     */
    public static final int ContrastBolusStopTime = 1577027;

    /**
     * DICOM Tag for ContrastBolusTotalDose.
     */
    public static final int ContrastBolusTotalDose = 1577028;

    /**
     * DICOM Tag for SyringeCounts.
     */
    public static final int SyringeCounts = 1577029;

    /**
     * DICOM Tag for ContrastFlowRate.
     */
    public static final int ContrastFlowRate = 1577030;

    /**
     * DICOM Tag for ContrastFlowDuration.
     */
    public static final int ContrastFlowDuration = 1577031;

    /**
     * DICOM Tag for ContrastBolusIngredient.
     */
    public static final int ContrastBolusIngredient = 1577032;

    /**
     * DICOM Tag for ContrastBolusIngredientConcentration.
     */
    public static final int ContrastBolusIngredientConcentration = 1577033;

    /**
     * DICOM Tag for SpatialResolution.
     */
    public static final int SpatialResolution = 1577040;

    /**
     * DICOM Tag for TriggerTime.
     */
    public static final int TriggerTime = 1577056;

    /**
     * DICOM Tag for TriggerSourceOrType.
     */
    public static final int TriggerSourceOrType = 1577057;

    /**
     * DICOM Tag for NominalInterval.
     */
    public static final int NominalInterval = 1577058;

    /**
     * DICOM Tag for FrameTime.
     */
    public static final int FrameTime = 1577059;

    /**
     * DICOM Tag for CardiacFramingType.
     */
    public static final int CardiacFramingType = 1577060;

    /**
     * DICOM Tag for FrameTimeVector.
     */
    public static final int FrameTimeVector = 1577061;

    /**
     * DICOM Tag for FrameDelay.
     */
    public static final int FrameDelay = 1577062;

    /**
     * DICOM Tag for ImageTriggerDelay.
     */
    public static final int ImageTriggerDelay = 1577063;

    /**
     * DICOM Tag for MultiplexGroupTimeOffset.
     */
    public static final int MultiplexGroupTimeOffset = 1577064;

    /**
     * DICOM Tag for TriggerTimeOffset.
     */
    public static final int TriggerTimeOffset = 1577065;

    /**
     * DICOM Tag for SynchronizationTrigger.
     */
    public static final int SynchronizationTrigger = 1577066;

    /**
     * DICOM Tag for SynchronizationChannel.
     */
    public static final int SynchronizationChannel = 1577068;

    /**
     * DICOM Tag for TriggerSamplePosition.
     */
    public static final int TriggerSamplePosition = 1577070;

    /**
     * DICOM Tag for RadiopharmaceuticalRoute.
     */
    public static final int RadiopharmaceuticalRoute = 1577072;

    /**
     * DICOM Tag for RadiopharmaceuticalVolume.
     */
    public static final int RadiopharmaceuticalVolume = 1577073;

    /**
     * DICOM Tag for RadiopharmaceuticalStartTime.
     */
    public static final int RadiopharmaceuticalStartTime = 1577074;

    /**
     * DICOM Tag for RadiopharmaceuticalStopTime.
     */
    public static final int RadiopharmaceuticalStopTime = 1577075;

    /**
     * DICOM Tag for RadionuclideTotalDose.
     */
    public static final int RadionuclideTotalDose = 1577076;

    /**
     * DICOM Tag for RadionuclideHalfLife.
     */
    public static final int RadionuclideHalfLife = 1577077;

    /**
     * DICOM Tag for RadionuclidePositronFraction.
     */
    public static final int RadionuclidePositronFraction = 1577078;

    /**
     * DICOM Tag for RadiopharmaceuticalSpecificActivity.
     */
    public static final int RadiopharmaceuticalSpecificActivity = 1577079;

    /**
     * DICOM Tag for RadiopharmaceuticalStartDateTime.
     */
    public static final int RadiopharmaceuticalStartDateTime = 1577080;

    /**
     * DICOM Tag for RadiopharmaceuticalStopDateTime.
     */
    public static final int RadiopharmaceuticalStopDateTime = 1577081;

    /**
     * DICOM Tag for BeatRejectionFlag.
     */
    public static final int BeatRejectionFlag = 1577088;

    /**
     * DICOM Tag for LowRRValue.
     */
    public static final int LowRRValue = 1577089;

    /**
     * DICOM Tag for HighRRValue.
     */
    public static final int HighRRValue = 1577090;

    /**
     * DICOM Tag for IntervalsAcquired.
     */
    public static final int IntervalsAcquired = 1577091;

    /**
     * DICOM Tag for IntervalsRejected.
     */
    public static final int IntervalsRejected = 1577092;

    /**
     * DICOM Tag for PVCRejection.
     */
    public static final int PVCRejection = 1577093;

    /**
     * DICOM Tag for SkipBeats.
     */
    public static final int SkipBeats = 1577094;

    /**
     * DICOM Tag for HeartRate.
     */
    public static final int HeartRate = 1577096;

    /**
     * DICOM Tag for CardiacNumberOfImages.
     */
    public static final int CardiacNumberOfImages = 1577104;

    /**
     * DICOM Tag for TriggerWindow.
     */
    public static final int TriggerWindow = 1577108;

    /**
     * DICOM Tag for ReconstructionDiameter.
     */
    public static final int ReconstructionDiameter = 1577216;

    /**
     * DICOM Tag for DistanceSourceToDetector.
     */
    public static final int DistanceSourceToDetector = 1577232;

    /**
     * DICOM Tag for DistanceSourceToPatient.
     */
    public static final int DistanceSourceToPatient = 1577233;

    /**
     * DICOM Tag for EstimatedRadiographicMagnificationFactor.
     */
    public static final int EstimatedRadiographicMagnificationFactor = 1577236;

    /**
     * DICOM Tag for GantryDetectorTilt.
     */
    public static final int GantryDetectorTilt = 1577248;

    /**
     * DICOM Tag for GantryDetectorSlew.
     */
    public static final int GantryDetectorSlew = 1577249;

    /**
     * DICOM Tag for TableHeight.
     */
    public static final int TableHeight = 1577264;

    /**
     * DICOM Tag for TableTraverse.
     */
    public static final int TableTraverse = 1577265;

    /**
     * DICOM Tag for TableMotion.
     */
    public static final int TableMotion = 1577268;

    /**
     * DICOM Tag for TableVerticalIncrement.
     */
    public static final int TableVerticalIncrement = 1577269;

    /**
     * DICOM Tag for TableLateralIncrement.
     */
    public static final int TableLateralIncrement = 1577270;

    /**
     * DICOM Tag for TableLongitudinalIncrement.
     */
    public static final int TableLongitudinalIncrement = 1577271;

    /**
     * DICOM Tag for TableAngle.
     */
    public static final int TableAngle = 1577272;

    /**
     * DICOM Tag for TableType.
     */
    public static final int TableType = 1577274;

    /**
     * DICOM Tag for RotationDirection.
     */
    public static final int RotationDirection = 1577280;

    /**
     * DICOM Tag for AngularPosition.
     */
    public static final int AngularPosition = 1577281;

    /**
     * DICOM Tag for RadialPosition.
     */
    public static final int RadialPosition = 1577282;

    /**
     * DICOM Tag for ScanArc.
     */
    public static final int ScanArc = 1577283;

    /**
     * DICOM Tag for AngularStep.
     */
    public static final int AngularStep = 1577284;

    /**
     * DICOM Tag for CenterOfRotationOffset.
     */
    public static final int CenterOfRotationOffset = 1577285;

    /**
     * DICOM Tag for RotationOffset.
     */
    public static final int RotationOffset = 1577286;

    /**
     * DICOM Tag for FieldOfViewShape.
     */
    public static final int FieldOfViewShape = 1577287;

    /**
     * DICOM Tag for FieldOfViewDimensions.
     */
    public static final int FieldOfViewDimensions = 1577289;

    /**
     * DICOM Tag for ExposureTime.
     */
    public static final int ExposureTime = 1577296;

    /**
     * DICOM Tag for XRayTubeCurrent.
     */
    public static final int XRayTubeCurrent = 1577297;

    /**
     * DICOM Tag for Exposure.
     */
    public static final int Exposure = 1577298;

    /**
     * DICOM Tag for ExposureInuAs.
     */
    public static final int ExposureInuAs = 1577299;

    /**
     * DICOM Tag for AveragePulseWidth.
     */
    public static final int AveragePulseWidth = 1577300;

    /**
     * DICOM Tag for RadiationSetting.
     */
    public static final int RadiationSetting = 1577301;

    /**
     * DICOM Tag for RectificationType.
     */
    public static final int RectificationType = 1577302;

    /**
     * DICOM Tag for RadiationMode.
     */
    public static final int RadiationMode = 1577306;

    /**
     * DICOM Tag for ImageAndFluoroscopyAreaDoseProduct.
     */
    public static final int ImageAndFluoroscopyAreaDoseProduct = 1577310;

    /**
     * DICOM Tag for FilterType.
     */
    public static final int FilterType = 1577312;

    /**
     * DICOM Tag for TypeOfFilters.
     */
    public static final int TypeOfFilters = 1577313;

    /**
     * DICOM Tag for IntensifierSize.
     */
    public static final int IntensifierSize = 1577314;

    /**
     * DICOM Tag for ImagerPixelSpacing.
     */
    public static final int ImagerPixelSpacing = 1577316;

    /**
     * DICOM Tag for Grid.
     */
    public static final int Grid = 1577318;

    /**
     * DICOM Tag for GeneratorPower.
     */
    public static final int GeneratorPower = 1577328;

    /**
     * DICOM Tag for CollimatorGridName.
     */
    public static final int CollimatorGridName = 1577344;

    /**
     * DICOM Tag for CollimatorType.
     */
    public static final int CollimatorType = 1577345;

    /**
     * DICOM Tag for FocalDistance.
     */
    public static final int FocalDistance = 1577346;

    /**
     * DICOM Tag for XFocusCenter.
     */
    public static final int XFocusCenter = 1577347;

    /**
     * DICOM Tag for YFocusCenter.
     */
    public static final int YFocusCenter = 1577348;

    /**
     * DICOM Tag for FocalSpots.
     */
    public static final int FocalSpots = 1577360;

    /**
     * DICOM Tag for AnodeTargetMaterial.
     */
    public static final int AnodeTargetMaterial = 1577361;

    /**
     * DICOM Tag for BodyPartThickness.
     */
    public static final int BodyPartThickness = 1577376;

    /**
     * DICOM Tag for CompressionForce.
     */
    public static final int CompressionForce = 1577378;

    /**
     * DICOM Tag for CompressionPressure.
     */
    public static final int CompressionPressure = 1577379;

    /**
     * DICOM Tag for PaddleDescription.
     */
    public static final int PaddleDescription = 1577380;

    /**
     * DICOM Tag for CompressionContactArea.
     */
    public static final int CompressionContactArea = 1577381;

    /**
     * DICOM Tag for AcquisitionMode.
     */
    public static final int AcquisitionMode = 1577392;

    /**
     * DICOM Tag for DoseModeName.
     */
    public static final int DoseModeName = 1577393;

    /**
     * DICOM Tag for AcquiredSubtractionMaskFlag.
     */
    public static final int AcquiredSubtractionMaskFlag = 1577394;

    /**
     * DICOM Tag for FluoroscopyPersistenceFlag.
     */
    public static final int FluoroscopyPersistenceFlag = 1577395;

    /**
     * DICOM Tag for FluoroscopyLastImageHoldPersistenceFlag.
     */
    public static final int FluoroscopyLastImageHoldPersistenceFlag = 1577396;

    /**
     * DICOM Tag for UpperLimitNumberOfPersistentFluoroscopyFrames.
     */
    public static final int UpperLimitNumberOfPersistentFluoroscopyFrames = 1577397;

    /**
     * DICOM Tag for ContrastBolusAutoInjectionTriggerFlag.
     */
    public static final int ContrastBolusAutoInjectionTriggerFlag = 1577398;

    /**
     * DICOM Tag for ContrastBolusInjectionDelay.
     */
    public static final int ContrastBolusInjectionDelay = 1577399;

    /**
     * DICOM Tag for XAAcquisitionPhaseDetailsSequence.
     */
    public static final int XAAcquisitionPhaseDetailsSequence = 1577400;

    /**
     * DICOM Tag for XAAcquisitionFrameRate.
     */
    public static final int XAAcquisitionFrameRate = 1577401;

    /**
     * DICOM Tag for XAPlaneDetailsSequence.
     */
    public static final int XAPlaneDetailsSequence = 1577402;

    /**
     * DICOM Tag for AcquisitionFieldOfViewLabel.
     */
    public static final int AcquisitionFieldOfViewLabel = 1577403;

    /**
     * DICOM Tag for XRayFilterDetailsSequence.
     */
    public static final int XRayFilterDetailsSequence = 1577404;

    /**
     * DICOM Tag for XAAcquisitionDuration.
     */
    public static final int XAAcquisitionDuration = 1577405;

    /**
     * DICOM Tag for ReconstructionPipelineType.
     */
    public static final int ReconstructionPipelineType = 1577406;

    /**
     * DICOM Tag for ImageFilterDetailsSequence.
     */
    public static final int ImageFilterDetailsSequence = 1577407;

    /**
     * DICOM Tag for AppliedMaskSubtractionFlag.
     */
    public static final int AppliedMaskSubtractionFlag = 1577408;

    /**
     * DICOM Tag for RequestedSeriesDescriptionCodeSequence.
     */
    public static final int RequestedSeriesDescriptionCodeSequence = 1577409;

    /**
     * DICOM Tag for DateOfLastCalibration.
     */
    public static final int DateOfLastCalibration = 1577472;

    /**
     * DICOM Tag for TimeOfLastCalibration.
     */
    public static final int TimeOfLastCalibration = 1577473;

    /**
     * DICOM Tag for DateTimeOfLastCalibration.
     */
    public static final int DateTimeOfLastCalibration = 1577474;

    /**
     * DICOM Tag for CalibrationDateTime.
     */
    public static final int CalibrationDateTime = 1577475;

    /**
     * DICOM Tag for DateOfManufacture.
     */
    public static final int DateOfManufacture = 1577476;

    /**
     * DICOM Tag for DateOfInstallation.
     */
    public static final int DateOfInstallation = 1577477;

    /**
     * DICOM Tag for ConvolutionKernel.
     */
    public static final int ConvolutionKernel = 1577488;

    /**
     * DICOM Tag for UpperLowerPixelValues.
     */
    public static final int UpperLowerPixelValues = 1577536;

    /**
     * DICOM Tag for ActualFrameDuration.
     */
    public static final int ActualFrameDuration = 1577538;

    /**
     * DICOM Tag for CountRate.
     */
    public static final int CountRate = 1577539;

    /**
     * DICOM Tag for PreferredPlaybackSequencing.
     */
    public static final int PreferredPlaybackSequencing = 1577540;

    /**
     * DICOM Tag for ReceiveCoilName.
     */
    public static final int ReceiveCoilName = 1577552;

    /**
     * DICOM Tag for TransmitCoilName.
     */
    public static final int TransmitCoilName = 1577553;

    /**
     * DICOM Tag for PlateType.
     */
    public static final int PlateType = 1577568;

    /**
     * DICOM Tag for PhosphorType.
     */
    public static final int PhosphorType = 1577569;

    /**
     * DICOM Tag for WaterEquivalentDiameter.
     */
    public static final int WaterEquivalentDiameter = 1577585;

    /**
     * DICOM Tag for WaterEquivalentDiameterCalculationMethodCodeSequence.
     */
    public static final int WaterEquivalentDiameterCalculationMethodCodeSequence = 1577586;

    /**
     * DICOM Tag for ScanVelocity.
     */
    public static final int ScanVelocity = 1577728;

    /**
     * DICOM Tag for WholeBodyTechnique.
     */
    public static final int WholeBodyTechnique = 1577729;

    /**
     * DICOM Tag for ScanLength.
     */
    public static final int ScanLength = 1577730;

    /**
     * DICOM Tag for AcquisitionMatrix.
     */
    public static final int AcquisitionMatrix = 1577744;

    /**
     * DICOM Tag for InPlanePhaseEncodingDirection.
     */
    public static final int InPlanePhaseEncodingDirection = 1577746;

    /**
     * DICOM Tag for FlipAngle.
     */
    public static final int FlipAngle = 1577748;

    /**
     * DICOM Tag for VariableFlipAngleFlag.
     */
    public static final int VariableFlipAngleFlag = 1577749;

    /**
     * DICOM Tag for SAR.
     */
    public static final int SAR = 1577750;

    /**
     * DICOM Tag for dBdt.
     */
    public static final int dBdt = 1577752;

    /**
     * DICOM Tag for B1rms.
     */
    public static final int B1rms = 1577760;

    /**
     * DICOM Tag for AcquisitionDeviceProcessingDescription.
     */
    public static final int AcquisitionDeviceProcessingDescription = 1577984;

    /**
     * DICOM Tag for AcquisitionDeviceProcessingCode.
     */
    public static final int AcquisitionDeviceProcessingCode = 1577985;

    /**
     * DICOM Tag for CassetteOrientation.
     */
    public static final int CassetteOrientation = 1577986;

    /**
     * DICOM Tag for CassetteSize.
     */
    public static final int CassetteSize = 1577987;

    /**
     * DICOM Tag for ExposuresOnPlate.
     */
    public static final int ExposuresOnPlate = 1577988;

    /**
     * DICOM Tag for RelativeXRayExposure.
     */
    public static final int RelativeXRayExposure = 1577989;

    /**
     * DICOM Tag for ExposureIndex.
     */
    public static final int ExposureIndex = 1578001;

    /**
     * DICOM Tag for TargetExposureIndex.
     */
    public static final int TargetExposureIndex = 1578002;

    /**
     * DICOM Tag for DeviationIndex.
     */
    public static final int DeviationIndex = 1578003;

    /**
     * DICOM Tag for ColumnAngulation.
     */
    public static final int ColumnAngulation = 1578064;

    /**
     * DICOM Tag for TomoLayerHeight.
     */
    public static final int TomoLayerHeight = 1578080;

    /**
     * DICOM Tag for TomoAngle.
     */
    public static final int TomoAngle = 1578096;

    /**
     * DICOM Tag for TomoTime.
     */
    public static final int TomoTime = 1578112;

    /**
     * DICOM Tag for TomoType.
     */
    public static final int TomoType = 1578128;

    /**
     * DICOM Tag for TomoClass.
     */
    public static final int TomoClass = 1578129;

    /**
     * DICOM Tag for NumberOfTomosynthesisSourceImages.
     */
    public static final int NumberOfTomosynthesisSourceImages = 1578133;

    /**
     * DICOM Tag for PositionerMotion.
     */
    public static final int PositionerMotion = 1578240;

    /**
     * DICOM Tag for PositionerType.
     */
    public static final int PositionerType = 1578248;

    /**
     * DICOM Tag for PositionerPrimaryAngle.
     */
    public static final int PositionerPrimaryAngle = 1578256;

    /**
     * DICOM Tag for PositionerSecondaryAngle.
     */
    public static final int PositionerSecondaryAngle = 1578257;

    /**
     * DICOM Tag for PositionerPrimaryAngleIncrement.
     */
    public static final int PositionerPrimaryAngleIncrement = 1578272;

    /**
     * DICOM Tag for PositionerSecondaryAngleIncrement.
     */
    public static final int PositionerSecondaryAngleIncrement = 1578273;

    /**
     * DICOM Tag for DetectorPrimaryAngle.
     */
    public static final int DetectorPrimaryAngle = 1578288;

    /**
     * DICOM Tag for DetectorSecondaryAngle.
     */
    public static final int DetectorSecondaryAngle = 1578289;

    /**
     * DICOM Tag for ShutterShape.
     */
    public static final int ShutterShape = 1578496;

    /**
     * DICOM Tag for ShutterLeftVerticalEdge.
     */
    public static final int ShutterLeftVerticalEdge = 1578498;

    /**
     * DICOM Tag for ShutterRightVerticalEdge.
     */
    public static final int ShutterRightVerticalEdge = 1578500;

    /**
     * DICOM Tag for ShutterUpperHorizontalEdge.
     */
    public static final int ShutterUpperHorizontalEdge = 1578502;

    /**
     * DICOM Tag for ShutterLowerHorizontalEdge.
     */
    public static final int ShutterLowerHorizontalEdge = 1578504;

    /**
     * DICOM Tag for CenterOfCircularShutter.
     */
    public static final int CenterOfCircularShutter = 1578512;

    /**
     * DICOM Tag for RadiusOfCircularShutter.
     */
    public static final int RadiusOfCircularShutter = 1578514;

    /**
     * DICOM Tag for VerticesOfThePolygonalShutter.
     */
    public static final int VerticesOfThePolygonalShutter = 1578528;

    /**
     * DICOM Tag for ShutterPresentationValue.
     */
    public static final int ShutterPresentationValue = 1578530;

    /**
     * DICOM Tag for ShutterOverlayGroup.
     */
    public static final int ShutterOverlayGroup = 1578531;

    /**
     * DICOM Tag for ShutterPresentationColorCIELabValue.
     */
    public static final int ShutterPresentationColorCIELabValue = 1578532;

    /**
     * DICOM Tag for OutlineShapeType.
     */
    public static final int OutlineShapeType = 1578544;

    /**
     * DICOM Tag for OutlineLeftVerticalEdge.
     */
    public static final int OutlineLeftVerticalEdge = 1578545;

    /**
     * DICOM Tag for OutlineRightVerticalEdge.
     */
    public static final int OutlineRightVerticalEdge = 1578546;

    /**
     * DICOM Tag for OutlineUpperHorizontalEdge.
     */
    public static final int OutlineUpperHorizontalEdge = 1578547;

    /**
     * DICOM Tag for OutlineLowerHorizontalEdge.
     */
    public static final int OutlineLowerHorizontalEdge = 1578548;

    /**
     * DICOM Tag for CenterOfCircularOutline.
     */
    public static final int CenterOfCircularOutline = 1578549;

    /**
     * DICOM Tag for DiameterOfCircularOutline.
     */
    public static final int DiameterOfCircularOutline = 1578550;

    /**
     * DICOM Tag for NumberOfPolygonalVertices.
     */
    public static final int NumberOfPolygonalVertices = 1578551;

    /**
     * DICOM Tag for VerticesOfThePolygonalOutline.
     */
    public static final int VerticesOfThePolygonalOutline = 1578552;

    /**
     * DICOM Tag for CollimatorShape.
     */
    public static final int CollimatorShape = 1578752;

    /**
     * DICOM Tag for CollimatorLeftVerticalEdge.
     */
    public static final int CollimatorLeftVerticalEdge = 1578754;

    /**
     * DICOM Tag for CollimatorRightVerticalEdge.
     */
    public static final int CollimatorRightVerticalEdge = 1578756;

    /**
     * DICOM Tag for CollimatorUpperHorizontalEdge.
     */
    public static final int CollimatorUpperHorizontalEdge = 1578758;

    /**
     * DICOM Tag for CollimatorLowerHorizontalEdge.
     */
    public static final int CollimatorLowerHorizontalEdge = 1578760;

    /**
     * DICOM Tag for CenterOfCircularCollimator.
     */
    public static final int CenterOfCircularCollimator = 1578768;

    /**
     * DICOM Tag for RadiusOfCircularCollimator.
     */
    public static final int RadiusOfCircularCollimator = 1578770;

    /**
     * DICOM Tag for VerticesOfThePolygonalCollimator.
     */
    public static final int VerticesOfThePolygonalCollimator = 1578784;

    /**
     * DICOM Tag for AcquisitionTimeSynchronized.
     */
    public static final int AcquisitionTimeSynchronized = 1579008;

    /**
     * DICOM Tag for TimeSource.
     */
    public static final int TimeSource = 1579009;

    /**
     * DICOM Tag for TimeDistributionProtocol.
     */
    public static final int TimeDistributionProtocol = 1579010;

    /**
     * DICOM Tag for NTPSourceAddress.
     */
    public static final int NTPSourceAddress = 1579011;

    /**
     * DICOM Tag for PageNumberVector.
     */
    public static final int PageNumberVector = 1581057;

    /**
     * DICOM Tag for FrameLabelVector.
     */
    public static final int FrameLabelVector = 1581058;

    /**
     * DICOM Tag for FramePrimaryAngleVector.
     */
    public static final int FramePrimaryAngleVector = 1581059;

    /**
     * DICOM Tag for FrameSecondaryAngleVector.
     */
    public static final int FrameSecondaryAngleVector = 1581060;

    /**
     * DICOM Tag for SliceLocationVector.
     */
    public static final int SliceLocationVector = 1581061;

    /**
     * DICOM Tag for DisplayWindowLabelVector.
     */
    public static final int DisplayWindowLabelVector = 1581062;

    /**
     * DICOM Tag for NominalScannedPixelSpacing.
     */
    public static final int NominalScannedPixelSpacing = 1581072;

    /**
     * DICOM Tag for DigitizingDeviceTransportDirection.
     */
    public static final int DigitizingDeviceTransportDirection = 1581088;

    /**
     * DICOM Tag for RotationOfScannedFilm.
     */
    public static final int RotationOfScannedFilm = 1581104;

    /**
     * DICOM Tag for BiopsyTargetSequence.
     */
    public static final int BiopsyTargetSequence = 1581121;

    /**
     * DICOM Tag for TargetUID.
     */
    public static final int TargetUID = 1581122;

    /**
     * DICOM Tag for LocalizingCursorPosition.
     */
    public static final int LocalizingCursorPosition = 1581123;

    /**
     * DICOM Tag for CalculatedTargetPosition.
     */
    public static final int CalculatedTargetPosition = 1581124;

    /**
     * DICOM Tag for TargetLabel.
     */
    public static final int TargetLabel = 1581125;

    /**
     * DICOM Tag for DisplayedZValue.
     */
    public static final int DisplayedZValue = 1581126;

    /**
     * DICOM Tag for IVUSAcquisition.
     */
    public static final int IVUSAcquisition = 1585408;

    /**
     * DICOM Tag for IVUSPullbackRate.
     */
    public static final int IVUSPullbackRate = 1585409;

    /**
     * DICOM Tag for IVUSGatedRate.
     */
    public static final int IVUSGatedRate = 1585410;

    /**
     * DICOM Tag for IVUSPullbackStartFrameNumber.
     */
    public static final int IVUSPullbackStartFrameNumber = 1585411;

    /**
     * DICOM Tag for IVUSPullbackStopFrameNumber.
     */
    public static final int IVUSPullbackStopFrameNumber = 1585412;

    /**
     * DICOM Tag for LesionNumber.
     */
    public static final int LesionNumber = 1585413;

    /**
     * DICOM Tag for AcquisitionComments.
     */
    public static final int AcquisitionComments = 1589248;

    /**
     * DICOM Tag for OutputPower.
     */
    public static final int OutputPower = 1593344;

    /**
     * DICOM Tag for TransducerData.
     */
    public static final int TransducerData = 1593360;

    /**
     * DICOM Tag for TransducerIdentificationSequence.
     */
    public static final int TransducerIdentificationSequence = 1593361;

    /**
     * DICOM Tag for FocusDepth.
     */
    public static final int FocusDepth = 1593362;

    /**
     * DICOM Tag for ProcessingFunction.
     */
    public static final int ProcessingFunction = 1593376;

    /**
     * DICOM Tag for PostprocessingFunction.
     */
    public static final int PostprocessingFunction = 1593377;

    /**
     * DICOM Tag for MechanicalIndex.
     */
    public static final int MechanicalIndex = 1593378;

    /**
     * DICOM Tag for BoneThermalIndex.
     */
    public static final int BoneThermalIndex = 1593380;

    /**
     * DICOM Tag for CranialThermalIndex.
     */
    public static final int CranialThermalIndex = 1593382;

    /**
     * DICOM Tag for SoftTissueThermalIndex.
     */
    public static final int SoftTissueThermalIndex = 1593383;

    /**
     * DICOM Tag for SoftTissueFocusThermalIndex.
     */
    public static final int SoftTissueFocusThermalIndex = 1593384;

    /**
     * DICOM Tag for SoftTissueSurfaceThermalIndex.
     */
    public static final int SoftTissueSurfaceThermalIndex = 1593385;

    /**
     * DICOM Tag for DynamicRange.
     */
    public static final int DynamicRange = 1593392;

    /**
     * DICOM Tag for TotalGain.
     */
    public static final int TotalGain = 1593408;

    /**
     * DICOM Tag for DepthOfScanField.
     */
    public static final int DepthOfScanField = 1593424;

    /**
     * DICOM Tag for PatientPosition.
     */
    public static final int PatientPosition = 1593600;

    /**
     * DICOM Tag for ViewPosition.
     */
    public static final int ViewPosition = 1593601;

    /**
     * DICOM Tag for ProjectionEponymousNameCodeSequence.
     */
    public static final int ProjectionEponymousNameCodeSequence = 1593604;

    /**
     * DICOM Tag for ImageTransformationMatrix.
     */
    public static final int ImageTransformationMatrix = 1593872;

    /**
     * DICOM Tag for ImageTranslationVector.
     */
    public static final int ImageTranslationVector = 1593874;

    /**
     * DICOM Tag for Sensitivity.
     */
    public static final int Sensitivity = 1597440;

    /**
     * DICOM Tag for SequenceOfUltrasoundRegions.
     */
    public static final int SequenceOfUltrasoundRegions = 1597457;

    /**
     * DICOM Tag for RegionSpatialFormat.
     */
    public static final int RegionSpatialFormat = 1597458;

    /**
     * DICOM Tag for RegionDataType.
     */
    public static final int RegionDataType = 1597460;

    /**
     * DICOM Tag for RegionFlags.
     */
    public static final int RegionFlags = 1597462;

    /**
     * DICOM Tag for RegionLocationMinX0.
     */
    public static final int RegionLocationMinX0 = 1597464;

    /**
     * DICOM Tag for RegionLocationMinY0.
     */
    public static final int RegionLocationMinY0 = 1597466;

    /**
     * DICOM Tag for RegionLocationMaxX1.
     */
    public static final int RegionLocationMaxX1 = 1597468;

    /**
     * DICOM Tag for RegionLocationMaxY1.
     */
    public static final int RegionLocationMaxY1 = 1597470;

    /**
     * DICOM Tag for ReferencePixelX0.
     */
    public static final int ReferencePixelX0 = 1597472;

    /**
     * DICOM Tag for ReferencePixelY0.
     */
    public static final int ReferencePixelY0 = 1597474;

    /**
     * DICOM Tag for PhysicalUnitsXDirection.
     */
    public static final int PhysicalUnitsXDirection = 1597476;

    /**
     * DICOM Tag for PhysicalUnitsYDirection.
     */
    public static final int PhysicalUnitsYDirection = 1597478;

    /**
     * DICOM Tag for ReferencePixelPhysicalValueX.
     */
    public static final int ReferencePixelPhysicalValueX = 1597480;

    /**
     * DICOM Tag for ReferencePixelPhysicalValueY.
     */
    public static final int ReferencePixelPhysicalValueY = 1597482;

    /**
     * DICOM Tag for PhysicalDeltaX.
     */
    public static final int PhysicalDeltaX = 1597484;

    /**
     * DICOM Tag for PhysicalDeltaY.
     */
    public static final int PhysicalDeltaY = 1597486;

    /**
     * DICOM Tag for TransducerFrequency.
     */
    public static final int TransducerFrequency = 1597488;

    /**
     * DICOM Tag for TransducerType.
     */
    public static final int TransducerType = 1597489;

    /**
     * DICOM Tag for PulseRepetitionFrequency.
     */
    public static final int PulseRepetitionFrequency = 1597490;

    /**
     * DICOM Tag for DopplerCorrectionAngle.
     */
    public static final int DopplerCorrectionAngle = 1597492;

    /**
     * DICOM Tag for SteeringAngle.
     */
    public static final int SteeringAngle = 1597494;

    /**
     * DICOM Tag for DopplerSampleVolumeXPositionRetired.
     */
    public static final int DopplerSampleVolumeXPositionRetired = 1597496;

    /**
     * DICOM Tag for DopplerSampleVolumeXPosition.
     */
    public static final int DopplerSampleVolumeXPosition = 1597497;

    /**
     * DICOM Tag for DopplerSampleVolumeYPositionRetired.
     */
    public static final int DopplerSampleVolumeYPositionRetired = 1597498;

    /**
     * DICOM Tag for DopplerSampleVolumeYPosition.
     */
    public static final int DopplerSampleVolumeYPosition = 1597499;

    /**
     * DICOM Tag for TMLinePositionX0Retired.
     */
    public static final int TMLinePositionX0Retired = 1597500;

    /**
     * DICOM Tag for TMLinePositionX0.
     */
    public static final int TMLinePositionX0 = 1597501;

    /**
     * DICOM Tag for TMLinePositionY0Retired.
     */
    public static final int TMLinePositionY0Retired = 1597502;

    /**
     * DICOM Tag for TMLinePositionY0.
     */
    public static final int TMLinePositionY0 = 1597503;

    /**
     * DICOM Tag for TMLinePositionX1Retired.
     */
    public static final int TMLinePositionX1Retired = 1597504;

    /**
     * DICOM Tag for TMLinePositionX1.
     */
    public static final int TMLinePositionX1 = 1597505;

    /**
     * DICOM Tag for TMLinePositionY1Retired.
     */
    public static final int TMLinePositionY1Retired = 1597506;

    /**
     * DICOM Tag for TMLinePositionY1.
     */
    public static final int TMLinePositionY1 = 1597507;

    /**
     * DICOM Tag for PixelComponentOrganization.
     */
    public static final int PixelComponentOrganization = 1597508;

    /**
     * DICOM Tag for PixelComponentMask.
     */
    public static final int PixelComponentMask = 1597510;

    /**
     * DICOM Tag for PixelComponentRangeStart.
     */
    public static final int PixelComponentRangeStart = 1597512;

    /**
     * DICOM Tag for PixelComponentRangeStop.
     */
    public static final int PixelComponentRangeStop = 1597514;

    /**
     * DICOM Tag for PixelComponentPhysicalUnits.
     */
    public static final int PixelComponentPhysicalUnits = 1597516;

    /**
     * DICOM Tag for PixelComponentDataType.
     */
    public static final int PixelComponentDataType = 1597518;

    /**
     * DICOM Tag for NumberOfTableBreakPoints.
     */
    public static final int NumberOfTableBreakPoints = 1597520;

    /**
     * DICOM Tag for TableOfXBreakPoints.
     */
    public static final int TableOfXBreakPoints = 1597522;

    /**
     * DICOM Tag for TableOfYBreakPoints.
     */
    public static final int TableOfYBreakPoints = 1597524;

    /**
     * DICOM Tag for NumberOfTableEntries.
     */
    public static final int NumberOfTableEntries = 1597526;

    /**
     * DICOM Tag for TableOfPixelValues.
     */
    public static final int TableOfPixelValues = 1597528;

    /**
     * DICOM Tag for TableOfParameterValues.
     */
    public static final int TableOfParameterValues = 1597530;

    /**
     * DICOM Tag for RWaveTimeVector.
     */
    public static final int RWaveTimeVector = 1597536;

    /**
     * DICOM Tag for ActiveImageAreaOverlayGroup.
     */
    public static final int ActiveImageAreaOverlayGroup = 1597552;

    /**
     * DICOM Tag for DetectorConditionsNominalFlag.
     */
    public static final int DetectorConditionsNominalFlag = 1601536;

    /**
     * DICOM Tag for DetectorTemperature.
     */
    public static final int DetectorTemperature = 1601537;

    /**
     * DICOM Tag for DetectorType.
     */
    public static final int DetectorType = 1601540;

    /**
     * DICOM Tag for DetectorConfiguration.
     */
    public static final int DetectorConfiguration = 1601541;

    /**
     * DICOM Tag for DetectorDescription.
     */
    public static final int DetectorDescription = 1601542;

    /**
     * DICOM Tag for DetectorMode.
     */
    public static final int DetectorMode = 1601544;

    /**
     * DICOM Tag for DetectorID.
     */
    public static final int DetectorID = 1601546;

    /**
     * DICOM Tag for DateOfLastDetectorCalibration.
     */
    public static final int DateOfLastDetectorCalibration = 1601548;

    /**
     * DICOM Tag for TimeOfLastDetectorCalibration.
     */
    public static final int TimeOfLastDetectorCalibration = 1601550;

    /**
     * DICOM Tag for ExposuresOnDetectorSinceLastCalibration.
     */
    public static final int ExposuresOnDetectorSinceLastCalibration = 1601552;

    /**
     * DICOM Tag for ExposuresOnDetectorSinceManufactured.
     */
    public static final int ExposuresOnDetectorSinceManufactured = 1601553;

    /**
     * DICOM Tag for DetectorTimeSinceLastExposure.
     */
    public static final int DetectorTimeSinceLastExposure = 1601554;

    /**
     * DICOM Tag for DetectorActiveTime.
     */
    public static final int DetectorActiveTime = 1601556;

    /**
     * DICOM Tag for DetectorActivationOffsetFromExposure.
     */
    public static final int DetectorActivationOffsetFromExposure = 1601558;

    /**
     * DICOM Tag for DetectorBinning.
     */
    public static final int DetectorBinning = 1601562;

    /**
     * DICOM Tag for DetectorElementPhysicalSize.
     */
    public static final int DetectorElementPhysicalSize = 1601568;

    /**
     * DICOM Tag for DetectorElementSpacing.
     */
    public static final int DetectorElementSpacing = 1601570;

    /**
     * DICOM Tag for DetectorActiveShape.
     */
    public static final int DetectorActiveShape = 1601572;

    /**
     * DICOM Tag for DetectorActiveDimensions.
     */
    public static final int DetectorActiveDimensions = 1601574;

    /**
     * DICOM Tag for DetectorActiveOrigin.
     */
    public static final int DetectorActiveOrigin = 1601576;

    /**
     * DICOM Tag for DetectorManufacturerName.
     */
    public static final int DetectorManufacturerName = 1601578;

    /**
     * DICOM Tag for DetectorManufacturerModelName.
     */
    public static final int DetectorManufacturerModelName = 1601579;

    /**
     * DICOM Tag for FieldOfViewOrigin.
     */
    public static final int FieldOfViewOrigin = 1601584;

    /**
     * DICOM Tag for FieldOfViewRotation.
     */
    public static final int FieldOfViewRotation = 1601586;

    /**
     * DICOM Tag for FieldOfViewHorizontalFlip.
     */
    public static final int FieldOfViewHorizontalFlip = 1601588;

    /**
     * DICOM Tag for PixelDataAreaOriginRelativeToFOV.
     */
    public static final int PixelDataAreaOriginRelativeToFOV = 1601590;

    /**
     * DICOM Tag for PixelDataAreaRotationAngleRelativeToFOV.
     */
    public static final int PixelDataAreaRotationAngleRelativeToFOV = 1601592;

    /**
     * DICOM Tag for GridAbsorbingMaterial.
     */
    public static final int GridAbsorbingMaterial = 1601600;

    /**
     * DICOM Tag for GridSpacingMaterial.
     */
    public static final int GridSpacingMaterial = 1601601;

    /**
     * DICOM Tag for GridThickness.
     */
    public static final int GridThickness = 1601602;

    /**
     * DICOM Tag for GridPitch.
     */
    public static final int GridPitch = 1601604;

    /**
     * DICOM Tag for GridAspectRatio.
     */
    public static final int GridAspectRatio = 1601606;

    /**
     * DICOM Tag for GridPeriod.
     */
    public static final int GridPeriod = 1601608;

    /**
     * DICOM Tag for GridFocalDistance.
     */
    public static final int GridFocalDistance = 1601612;

    /**
     * DICOM Tag for FilterMaterial.
     */
    public static final int FilterMaterial = 1601616;

    /**
     * DICOM Tag for FilterThicknessMinimum.
     */
    public static final int FilterThicknessMinimum = 1601618;

    /**
     * DICOM Tag for FilterThicknessMaximum.
     */
    public static final int FilterThicknessMaximum = 1601620;

    /**
     * DICOM Tag for FilterBeamPathLengthMinimum.
     */
    public static final int FilterBeamPathLengthMinimum = 1601622;

    /**
     * DICOM Tag for FilterBeamPathLengthMaximum.
     */
    public static final int FilterBeamPathLengthMaximum = 1601624;

    /**
     * DICOM Tag for ExposureControlMode.
     */
    public static final int ExposureControlMode = 1601632;

    /**
     * DICOM Tag for ExposureControlModeDescription.
     */
    public static final int ExposureControlModeDescription = 1601634;

    /**
     * DICOM Tag for ExposureStatus.
     */
    public static final int ExposureStatus = 1601636;

    /**
     * DICOM Tag for PhototimerSetting.
     */
    public static final int PhototimerSetting = 1601637;

    /**
     * DICOM Tag for ExposureTimeInuS.
     */
    public static final int ExposureTimeInuS = 1605968;

    /**
     * DICOM Tag for XRayTubeCurrentInuA.
     */
    public static final int XRayTubeCurrentInuA = 1605969;

    /**
     * DICOM Tag for ContentQualification.
     */
    public static final int ContentQualification = 1609732;

    /**
     * DICOM Tag for PulseSequenceName.
     */
    public static final int PulseSequenceName = 1609733;

    /**
     * DICOM Tag for MRImagingModifierSequence.
     */
    public static final int MRImagingModifierSequence = 1609734;

    /**
     * DICOM Tag for EchoPulseSequence.
     */
    public static final int EchoPulseSequence = 1609736;

    /**
     * DICOM Tag for InversionRecovery.
     */
    public static final int InversionRecovery = 1609737;

    /**
     * DICOM Tag for FlowCompensation.
     */
    public static final int FlowCompensation = 1609744;

    /**
     * DICOM Tag for MultipleSpinEcho.
     */
    public static final int MultipleSpinEcho = 1609745;

    /**
     * DICOM Tag for MultiPlanarExcitation.
     */
    public static final int MultiPlanarExcitation = 1609746;

    /**
     * DICOM Tag for PhaseContrast.
     */
    public static final int PhaseContrast = 1609748;

    /**
     * DICOM Tag for TimeOfFlightContrast.
     */
    public static final int TimeOfFlightContrast = 1609749;

    /**
     * DICOM Tag for Spoiling.
     */
    public static final int Spoiling = 1609750;

    /**
     * DICOM Tag for SteadyStatePulseSequence.
     */
    public static final int SteadyStatePulseSequence = 1609751;

    /**
     * DICOM Tag for EchoPlanarPulseSequence.
     */
    public static final int EchoPlanarPulseSequence = 1609752;

    /**
     * DICOM Tag for TagAngleFirstAxis.
     */
    public static final int TagAngleFirstAxis = 1609753;

    /**
     * DICOM Tag for MagnetizationTransfer.
     */
    public static final int MagnetizationTransfer = 1609760;

    /**
     * DICOM Tag for T2Preparation.
     */
    public static final int T2Preparation = 1609761;

    /**
     * DICOM Tag for BloodSignalNulling.
     */
    public static final int BloodSignalNulling = 1609762;

    /**
     * DICOM Tag for SaturationRecovery.
     */
    public static final int SaturationRecovery = 1609764;

    /**
     * DICOM Tag for SpectrallySelectedSuppression.
     */
    public static final int SpectrallySelectedSuppression = 1609765;

    /**
     * DICOM Tag for SpectrallySelectedExcitation.
     */
    public static final int SpectrallySelectedExcitation = 1609766;

    /**
     * DICOM Tag for SpatialPresaturation.
     */
    public static final int SpatialPresaturation = 1609767;

    /**
     * DICOM Tag for Tagging.
     */
    public static final int Tagging = 1609768;

    /**
     * DICOM Tag for OversamplingPhase.
     */
    public static final int OversamplingPhase = 1609769;

    /**
     * DICOM Tag for TagSpacingFirstDimension.
     */
    public static final int TagSpacingFirstDimension = 1609776;

    /**
     * DICOM Tag for GeometryOfKSpaceTraversal.
     */
    public static final int GeometryOfKSpaceTraversal = 1609778;

    /**
     * DICOM Tag for SegmentedKSpaceTraversal.
     */
    public static final int SegmentedKSpaceTraversal = 1609779;

    /**
     * DICOM Tag for RectilinearPhaseEncodeReordering.
     */
    public static final int RectilinearPhaseEncodeReordering = 1609780;

    /**
     * DICOM Tag for TagThickness.
     */
    public static final int TagThickness = 1609781;

    /**
     * DICOM Tag for PartialFourierDirection.
     */
    public static final int PartialFourierDirection = 1609782;

    /**
     * DICOM Tag for CardiacSynchronizationTechnique.
     */
    public static final int CardiacSynchronizationTechnique = 1609783;

    /**
     * DICOM Tag for ReceiveCoilManufacturerName.
     */
    public static final int ReceiveCoilManufacturerName = 1609793;

    /**
     * DICOM Tag for MRReceiveCoilSequence.
     */
    public static final int MRReceiveCoilSequence = 1609794;

    /**
     * DICOM Tag for ReceiveCoilType.
     */
    public static final int ReceiveCoilType = 1609795;

    /**
     * DICOM Tag for QuadratureReceiveCoil.
     */
    public static final int QuadratureReceiveCoil = 1609796;

    /**
     * DICOM Tag for MultiCoilDefinitionSequence.
     */
    public static final int MultiCoilDefinitionSequence = 1609797;

    /**
     * DICOM Tag for MultiCoilConfiguration.
     */
    public static final int MultiCoilConfiguration = 1609798;

    /**
     * DICOM Tag for MultiCoilElementName.
     */
    public static final int MultiCoilElementName = 1609799;

    /**
     * DICOM Tag for MultiCoilElementUsed.
     */
    public static final int MultiCoilElementUsed = 1609800;

    /**
     * DICOM Tag for MRTransmitCoilSequence.
     */
    public static final int MRTransmitCoilSequence = 1609801;

    /**
     * DICOM Tag for TransmitCoilManufacturerName.
     */
    public static final int TransmitCoilManufacturerName = 1609808;

    /**
     * DICOM Tag for TransmitCoilType.
     */
    public static final int TransmitCoilType = 1609809;

    /**
     * DICOM Tag for SpectralWidth.
     */
    public static final int SpectralWidth = 1609810;

    /**
     * DICOM Tag for ChemicalShiftReference.
     */
    public static final int ChemicalShiftReference = 1609811;

    /**
     * DICOM Tag for VolumeLocalizationTechnique.
     */
    public static final int VolumeLocalizationTechnique = 1609812;

    /**
     * DICOM Tag for MRAcquisitionFrequencyEncodingSteps.
     */
    public static final int MRAcquisitionFrequencyEncodingSteps = 1609816;

    /**
     * DICOM Tag for Decoupling.
     */
    public static final int Decoupling = 1609817;

    /**
     * DICOM Tag for DecoupledNucleus.
     */
    public static final int DecoupledNucleus = 1609824;

    /**
     * DICOM Tag for DecouplingFrequency.
     */
    public static final int DecouplingFrequency = 1609825;

    /**
     * DICOM Tag for DecouplingMethod.
     */
    public static final int DecouplingMethod = 1609826;

    /**
     * DICOM Tag for DecouplingChemicalShiftReference.
     */
    public static final int DecouplingChemicalShiftReference = 1609827;

    /**
     * DICOM Tag for KSpaceFiltering.
     */
    public static final int KSpaceFiltering = 1609828;

    /**
     * DICOM Tag for TimeDomainFiltering.
     */
    public static final int TimeDomainFiltering = 1609829;

    /**
     * DICOM Tag for NumberOfZeroFills.
     */
    public static final int NumberOfZeroFills = 1609830;

    /**
     * DICOM Tag for BaselineCorrection.
     */
    public static final int BaselineCorrection = 1609831;

    /**
     * DICOM Tag for ParallelReductionFactorInPlane.
     */
    public static final int ParallelReductionFactorInPlane = 1609833;

    /**
     * DICOM Tag for CardiacRRIntervalSpecified.
     */
    public static final int CardiacRRIntervalSpecified = 1609840;

    /**
     * DICOM Tag for AcquisitionDuration.
     */
    public static final int AcquisitionDuration = 1609843;

    /**
     * The frame acquisition date time value.
     */
    public static final int FrameAcquisitionDateTime = 1609844;

    /**
     * The diffusion directionality value.
     */
    public static final int DiffusionDirectionality = 1609845;

    /**
     * The diffusion gradient direction sequence value.
     */
    public static final int DiffusionGradientDirectionSequence = 1609846;

    /**
     * The parallel acquisition value.
     */
    public static final int ParallelAcquisition = 1609847;

    /**
     * The parallel acquisition technique value.
     */
    public static final int ParallelAcquisitionTechnique = 1609848;

    /**
     * The inversion times value.
     */
    public static final int InversionTimes = 1609849;

    /**
     * The metabolite map description value.
     */
    public static final int MetaboliteMapDescription = 1609856;

    /**
     * The partial fourier value.
     */
    public static final int PartialFourier = 1609857;

    /**
     * The effective echo time value.
     */
    public static final int EffectiveEchoTime = 1609858;

    /**
     * The metabolite map code sequence value.
     */
    public static final int MetaboliteMapCodeSequence = 1609859;

    /**
     * The chemical shift sequence value.
     */
    public static final int ChemicalShiftSequence = 1609860;

    /**
     * The cardiac signal source value.
     */
    public static final int CardiacSignalSource = 1609861;

    /**
     * The diffusion b value value.
     */
    public static final int DiffusionBValue = 1609863;

    /**
     * The diffusion gradient orientation value.
     */
    public static final int DiffusionGradientOrientation = 1609865;

    /**
     * The velocity encoding direction value.
     */
    public static final int VelocityEncodingDirection = 1609872;

    /**
     * The velocity encoding minimum value value.
     */
    public static final int VelocityEncodingMinimumValue = 1609873;

    /**
     * The velocity encoding acquisition sequence value.
     */
    public static final int VelocityEncodingAcquisitionSequence = 1609874;

    /**
     * The number of k space trajectories value.
     */
    public static final int NumberOfKSpaceTrajectories = 1609875;

    /**
     * The coverage of k space value.
     */
    public static final int CoverageOfKSpace = 1609876;

    /**
     * The spectroscopy acquisition phase rows value.
     */
    public static final int SpectroscopyAcquisitionPhaseRows = 1609877;

    /**
     * The parallel reduction factor in plane retired value.
     */
    public static final int ParallelReductionFactorInPlaneRetired = 1609878;

    /**
     * The transmitter frequency value.
     */
    public static final int TransmitterFrequency = 1609880;

    /**
     * The resonant nucleus value.
     */
    public static final int ResonantNucleus = 1609984;

    /**
     * The frequency correction value.
     */
    public static final int FrequencyCorrection = 1609985;

    /**
     * The mr spectroscopy fov geometry sequence value.
     */
    public static final int MRSpectroscopyFOVGeometrySequence = 1609987;

    /**
     * The slab thickness value.
     */
    public static final int SlabThickness = 1609988;

    /**
     * The slab orientation value.
     */
    public static final int SlabOrientation = 1609989;

    /**
     * The mid slab position value.
     */
    public static final int MidSlabPosition = 1609990;

    /**
     * The mr spatial saturation sequence value.
     */
    public static final int MRSpatialSaturationSequence = 1609991;

    /**
     * The mr timing and related parameters sequence value.
     */
    public static final int MRTimingAndRelatedParametersSequence = 1610002;

    /**
     * The mr echo sequence value.
     */
    public static final int MREchoSequence = 1610004;

    /**
     * The mr modifier sequence value.
     */
    public static final int MRModifierSequence = 1610005;

    /**
     * The mr diffusion sequence value.
     */
    public static final int MRDiffusionSequence = 1610007;

    /**
     * The cardiac synchronization sequence value.
     */
    public static final int CardiacSynchronizationSequence = 1610008;

    /**
     * The mr averages sequence value.
     */
    public static final int MRAveragesSequence = 1610009;

    /**
     * The mrfov geometry sequence value.
     */
    public static final int MRFOVGeometrySequence = 1610021;

    /**
     * The volume localization sequence value.
     */
    public static final int VolumeLocalizationSequence = 1610022;

    /**
     * The spectroscopy acquisition data columns value.
     */
    public static final int SpectroscopyAcquisitionDataColumns = 1610023;

    /**
     * The diffusion anisotropy type value.
     */
    public static final int DiffusionAnisotropyType = 1610055;

    /**
     * The frame reference date time value.
     */
    public static final int FrameReferenceDateTime = 1610065;

    /**
     * The mr metabolite map sequence value.
     */
    public static final int MRMetaboliteMapSequence = 1610066;

    /**
     * The parallel reduction factor out of plane value.
     */
    public static final int ParallelReductionFactorOutOfPlane = 1610069;

    /**
     * The spectroscopy acquisition out of plane phase steps value.
     */
    public static final int SpectroscopyAcquisitionOutOfPlanePhaseSteps = 1610073;

    /**
     * The bulk motion status value.
     */
    public static final int BulkMotionStatus = 1610086;

    /**
     * The parallel reduction factor second in plane value.
     */
    public static final int ParallelReductionFactorSecondInPlane = 1610088;

    /**
     * The cardiac beat rejection technique value.
     */
    public static final int CardiacBeatRejectionTechnique = 1610089;

    /**
     * The respiratory motion compensation technique value.
     */
    public static final int RespiratoryMotionCompensationTechnique = 1610096;

    /**
     * The respiratory signal source value.
     */
    public static final int RespiratorySignalSource = 1610097;

    /**
     * The bulk motion compensation technique value.
     */
    public static final int BulkMotionCompensationTechnique = 1610098;

    /**
     * The bulk motion signal source value.
     */
    public static final int BulkMotionSignalSource = 1610099;

    /**
     * The applicable safety standard agency value.
     */
    public static final int ApplicableSafetyStandardAgency = 1610100;

    /**
     * The applicable safety standard description value.
     */
    public static final int ApplicableSafetyStandardDescription = 1610101;

    /**
     * The operating mode sequence value.
     */
    public static final int OperatingModeSequence = 1610102;

    /**
     * The operating mode type value.
     */
    public static final int OperatingModeType = 1610103;

    /**
     * The operating mode value.
     */
    public static final int OperatingMode = 1610104;

    /**
     * The specific absorption rate definition value.
     */
    public static final int SpecificAbsorptionRateDefinition = 1610105;

    /**
     * The gradient output type value.
     */
    public static final int GradientOutputType = 1610112;

    /**
     * The specific absorption rate value value.
     */
    public static final int SpecificAbsorptionRateValue = 1610113;

    /**
     * The gradient output value.
     */
    public static final int GradientOutput = 1610114;

    /**
     * The flow compensation direction value.
     */
    public static final int FlowCompensationDirection = 1610115;

    /**
     * The tagging delay value.
     */
    public static final int TaggingDelay = 1610116;

    /**
     * The respiratory motion compensation technique description value.
     */
    public static final int RespiratoryMotionCompensationTechniqueDescription = 1610117;

    /**
     * The respiratory signal source id value.
     */
    public static final int RespiratorySignalSourceID = 1610118;

    /**
     * The chemical shift minimum integration limit in hz value.
     */
    public static final int ChemicalShiftMinimumIntegrationLimitInHz = 1610133;

    /**
     * The chemical shift maximum integration limit in hz value.
     */
    public static final int ChemicalShiftMaximumIntegrationLimitInHz = 1610134;

    /**
     * The mr velocity encoding sequence value.
     */
    public static final int MRVelocityEncodingSequence = 1610135;

    /**
     * The first order phase correction value.
     */
    public static final int FirstOrderPhaseCorrection = 1610136;

    /**
     * The water referenced phase correction value.
     */
    public static final int WaterReferencedPhaseCorrection = 1610137;

    /**
     * The mr spectroscopy acquisition type value.
     */
    public static final int MRSpectroscopyAcquisitionType = 1610240;

    /**
     * The respiratory cycle position value.
     */
    public static final int RespiratoryCyclePosition = 1610260;

    /**
     * The velocity encoding maximum value value.
     */
    public static final int VelocityEncodingMaximumValue = 1610263;

    /**
     * The tag spacing second dimension value.
     */
    public static final int TagSpacingSecondDimension = 1610264;

    /**
     * The tag angle second axis value.
     */
    public static final int TagAngleSecondAxis = 1610265;

    /**
     * The frame acquisition duration value.
     */
    public static final int FrameAcquisitionDuration = 1610272;

    /**
     * The mr image frame type sequence value.
     */
    public static final int MRImageFrameTypeSequence = 1610278;

    /**
     * The mr spectroscopy frame type sequence value.
     */
    public static final int MRSpectroscopyFrameTypeSequence = 1610279;

    /**
     * The mr acquisition phase encoding steps in plane value.
     */
    public static final int MRAcquisitionPhaseEncodingStepsInPlane = 1610289;

    /**
     * The mr acquisition phase encoding steps out of plane value.
     */
    public static final int MRAcquisitionPhaseEncodingStepsOutOfPlane = 1610290;

    /**
     * The spectroscopy acquisition phase columns value.
     */
    public static final int SpectroscopyAcquisitionPhaseColumns = 1610292;

    /**
     * The cardiac cycle position value.
     */
    public static final int CardiacCyclePosition = 1610294;

    /**
     * The specific absorption rate sequence value.
     */
    public static final int SpecificAbsorptionRateSequence = 1610297;

    /**
     * The rf echo train length value.
     */
    public static final int RFEchoTrainLength = 1610304;

    /**
     * The gradient echo train length value.
     */
    public static final int GradientEchoTrainLength = 1610305;

    /**
     * The arterial spin labeling contrast value.
     */
    public static final int ArterialSpinLabelingContrast = 1610320;

    /**
     * The mr arterial spin labeling sequence value.
     */
    public static final int MRArterialSpinLabelingSequence = 1610321;

    /**
     * The asl technique description value.
     */
    public static final int ASLTechniqueDescription = 1610322;

    /**
     * The asl slab number value.
     */
    public static final int ASLSlabNumber = 1610323;

    /**
     * The asl slab thickness value.
     */
    public static final int ASLSlabThickness = 1610324;

    /**
     * The asl slab orientation value.
     */
    public static final int ASLSlabOrientation = 1610325;

    /**
     * The asl mid slab position value.
     */
    public static final int ASLMidSlabPosition = 1610326;

    /**
     * The asl context value.
     */
    public static final int ASLContext = 1610327;

    /**
     * The asl pulse train duration value.
     */
    public static final int ASLPulseTrainDuration = 1610328;

    /**
     * The asl crusher flag value.
     */
    public static final int ASLCrusherFlag = 1610329;

    /**
     * The asl crusher flow limit value.
     */
    public static final int ASLCrusherFlowLimit = 1610330;

    /**
     * The asl crusher description value.
     */
    public static final int ASLCrusherDescription = 1610331;

    /**
     * The asl bolus cutoff flag value.
     */
    public static final int ASLBolusCutoffFlag = 1610332;

    /**
     * The asl bolus cutoff timing sequence value.
     */
    public static final int ASLBolusCutoffTimingSequence = 1610333;

    /**
     * The asl bolus cutoff technique value.
     */
    public static final int ASLBolusCutoffTechnique = 1610334;

    /**
     * The asl bolus cutoff delay time value.
     */
    public static final int ASLBolusCutoffDelayTime = 1610335;

    /**
     * The asl slab sequence value.
     */
    public static final int ASLSlabSequence = 1610336;

    /**
     * The chemical shift minimum integration limit inppm value.
     */
    public static final int ChemicalShiftMinimumIntegrationLimitInppm = 1610389;

    /**
     * The chemical shift maximum integration limit inppm value.
     */
    public static final int ChemicalShiftMaximumIntegrationLimitInppm = 1610390;

    /**
     * The water reference acquisition value.
     */
    public static final int WaterReferenceAcquisition = 1610391;

    /**
     * The echo peak position value.
     */
    public static final int EchoPeakPosition = 1610392;

    /**
     * The ct acquisition type sequence value.
     */
    public static final int CTAcquisitionTypeSequence = 1610497;

    /**
     * The acquisition type value.
     */
    public static final int AcquisitionType = 1610498;

    /**
     * The tube angle value.
     */
    public static final int TubeAngle = 1610499;

    /**
     * The ct acquisition details sequence value.
     */
    public static final int CTAcquisitionDetailsSequence = 1610500;

    /**
     * The revolution time value.
     */
    public static final int RevolutionTime = 1610501;

    /**
     * The single collimation width value.
     */
    public static final int SingleCollimationWidth = 1610502;

    /**
     * The total collimation width value.
     */
    public static final int TotalCollimationWidth = 1610503;

    /**
     * The ct table dynamics sequence value.
     */
    public static final int CTTableDynamicsSequence = 1610504;

    /**
     * The table speed value.
     */
    public static final int TableSpeed = 1610505;

    /**
     * The table feed per rotation value.
     */
    public static final int TableFeedPerRotation = 1610512;

    /**
     * The spiral pitch factor value.
     */
    public static final int SpiralPitchFactor = 1610513;

    /**
     * The ct geometry sequence value.
     */
    public static final int CTGeometrySequence = 1610514;

    /**
     * The data collection center patient value.
     */
    public static final int DataCollectionCenterPatient = 1610515;

    /**
     * The ct reconstruction sequence value.
     */
    public static final int CTReconstructionSequence = 1610516;

    /**
     * The reconstruction algorithm value.
     */
    public static final int ReconstructionAlgorithm = 1610517;

    /**
     * The convolution kernel group value.
     */
    public static final int ConvolutionKernelGroup = 1610518;

    /**
     * The reconstruction field of view value.
     */
    public static final int ReconstructionFieldOfView = 1610519;

    /**
     * The reconstruction target center patient value.
     */
    public static final int ReconstructionTargetCenterPatient = 1610520;

    /**
     * The reconstruction angle value.
     */
    public static final int ReconstructionAngle = 1610521;

    /**
     * The image filter value.
     */
    public static final int ImageFilter = 1610528;

    /**
     * The ct exposure sequence value.
     */
    public static final int CTExposureSequence = 1610529;

    /**
     * The reconstruction pixel spacing value.
     */
    public static final int ReconstructionPixelSpacing = 1610530;

    /**
     * The exposure modulation type value.
     */
    public static final int ExposureModulationType = 1610531;

    /**
     * The estimated dose saving value.
     */
    public static final int EstimatedDoseSaving = 1610532;

    /**
     * The ctx ray details sequence value.
     */
    public static final int CTXRayDetailsSequence = 1610533;

    /**
     * The ct position sequence value.
     */
    public static final int CTPositionSequence = 1610534;

    /**
     * The table position value.
     */
    public static final int TablePosition = 1610535;

    /**
     * The exposure time inms value.
     */
    public static final int ExposureTimeInms = 1610536;

    /**
     * The ct image frame type sequence value.
     */
    public static final int CTImageFrameTypeSequence = 1610537;

    /**
     * The x ray tube current inm a value.
     */
    public static final int XRayTubeCurrentInmA = 1610544;

    /**
     * The exposure inm as value.
     */
    public static final int ExposureInmAs = 1610546;

    /**
     * The constant volume flag value.
     */
    public static final int ConstantVolumeFlag = 1610547;

    /**
     * The fluoroscopy flag value.
     */
    public static final int FluoroscopyFlag = 1610548;

    /**
     * The distance source to data collection center value.
     */
    public static final int DistanceSourceToDataCollectionCenter = 1610549;

    /**
     * The contrast bolus agent number value.
     */
    public static final int ContrastBolusAgentNumber = 1610551;

    /**
     * The contrast bolus ingredient code sequence value.
     */
    public static final int ContrastBolusIngredientCodeSequence = 1610552;

    /**
     * The contrast administration profile sequence value.
     */
    public static final int ContrastAdministrationProfileSequence = 1610560;

    /**
     * The contrast bolus usage sequence value.
     */
    public static final int ContrastBolusUsageSequence = 1610561;

    /**
     * The contrast bolus agent administered value.
     */
    public static final int ContrastBolusAgentAdministered = 1610562;

    /**
     * The contrast bolus agent detected value.
     */
    public static final int ContrastBolusAgentDetected = 1610563;

    /**
     * The contrast bolus agent phase value.
     */
    public static final int ContrastBolusAgentPhase = 1610564;

    /**
     * The ctd ivol value.
     */
    public static final int CTDIvol = 1610565;

    /**
     * The ctdi phantom type code sequence value.
     */
    public static final int CTDIPhantomTypeCodeSequence = 1610566;

    /**
     * The calcium scoring mass factor patient value.
     */
    public static final int CalciumScoringMassFactorPatient = 1610577;

    /**
     * The calcium scoring mass factor device value.
     */
    public static final int CalciumScoringMassFactorDevice = 1610578;

    /**
     * The energy weighting factor value.
     */
    public static final int EnergyWeightingFactor = 1610579;

    /**
     * The ct additional x ray source sequence value.
     */
    public static final int CTAdditionalXRaySourceSequence = 1610592;

    /**
     * The multienergy ct acquisition value.
     */
    public static final int MultienergyCTAcquisition = 1610593;

    /**
     * The multienergy ct acquisition sequence value.
     */
    public static final int MultienergyCTAcquisitionSequence = 1610594;

    /**
     * The multienergy ct processing sequence value.
     */
    public static final int MultienergyCTProcessingSequence = 1610595;

    /**
     * The multienergy ct characteristics sequence value.
     */
    public static final int MultienergyCTCharacteristicsSequence = 1610596;

    /**
     * The multienergy ctx ray source sequence value.
     */
    public static final int MultienergyCTXRaySourceSequence = 1610597;

    /**
     * The x ray source index value.
     */
    public static final int XRaySourceIndex = 1610598;

    /**
     * The x ray source id value.
     */
    public static final int XRaySourceID = 1610599;

    /**
     * The multienergy source technique value.
     */
    public static final int MultienergySourceTechnique = 1610600;

    /**
     * The source start date time value.
     */
    public static final int SourceStartDateTime = 1610601;

    /**
     * The source end date time value.
     */
    public static final int SourceEndDateTime = 1610602;

    /**
     * The switching phase number value.
     */
    public static final int SwitchingPhaseNumber = 1610603;

    /**
     * The switching phase nominal duration value.
     */
    public static final int SwitchingPhaseNominalDuration = 1610604;

    /**
     * The switching phase transition duration value.
     */
    public static final int SwitchingPhaseTransitionDuration = 1610605;

    /**
     * The effective bin energy value.
     */
    public static final int EffectiveBinEnergy = 1610606;

    /**
     * The multienergy ctx ray detector sequence value.
     */
    public static final int MultienergyCTXRayDetectorSequence = 1610607;

    /**
     * The x ray detector index value.
     */
    public static final int XRayDetectorIndex = 1610608;

    /**
     * The x ray detector id value.
     */
    public static final int XRayDetectorID = 1610609;

    /**
     * The multienergy detector type value.
     */
    public static final int MultienergyDetectorType = 1610610;

    /**
     * The x ray detector label value.
     */
    public static final int XRayDetectorLabel = 1610611;

    /**
     * The nominal max energy value.
     */
    public static final int NominalMaxEnergy = 1610612;

    /**
     * The nominal min energy value.
     */
    public static final int NominalMinEnergy = 1610613;

    /**
     * The referenced x ray detector index value.
     */
    public static final int ReferencedXRayDetectorIndex = 1610614;

    /**
     * The referenced x ray source index value.
     */
    public static final int ReferencedXRaySourceIndex = 1610615;

    /**
     * The referenced path index value.
     */
    public static final int ReferencedPathIndex = 1610616;

    /**
     * The multienergy ct path sequence value.
     */
    public static final int MultienergyCTPathSequence = 1610617;

    /**
     * The multienergy ct path index value.
     */
    public static final int MultienergyCTPathIndex = 1610618;

    /**
     * The multienergy acquisition description value.
     */
    public static final int MultienergyAcquisitionDescription = 1610619;

    /**
     * The monoenergetic energy equivalent value.
     */
    public static final int MonoenergeticEnergyEquivalent = 1610620;

    /**
     * The material code sequence value.
     */
    public static final int MaterialCodeSequence = 1610621;

    /**
     * The decomposition method value.
     */
    public static final int DecompositionMethod = 1610622;

    /**
     * The decomposition description value.
     */
    public static final int DecompositionDescription = 1610623;

    /**
     * The decomposition algorithm identification sequence value.
     */
    public static final int DecompositionAlgorithmIdentificationSequence = 1610624;

    /**
     * The decomposition material sequence value.
     */
    public static final int DecompositionMaterialSequence = 1610625;

    /**
     * The material attenuation sequence value.
     */
    public static final int MaterialAttenuationSequence = 1610626;

    /**
     * The photon energy value.
     */
    public static final int PhotonEnergy = 1610627;

    /**
     * The x ray mass attenuation coefficient value.
     */
    public static final int XRayMassAttenuationCoefficient = 1610628;

    /**
     * The projection pixel calibration sequence value.
     */
    public static final int ProjectionPixelCalibrationSequence = 1610753;

    /**
     * The distance source to isocenter value.
     */
    public static final int DistanceSourceToIsocenter = 1610754;

    /**
     * The distance object to table top value.
     */
    public static final int DistanceObjectToTableTop = 1610755;

    /**
     * The object pixel spacing in center of beam value.
     */
    public static final int ObjectPixelSpacingInCenterOfBeam = 1610756;

    /**
     * The positioner position sequence value.
     */
    public static final int PositionerPositionSequence = 1610757;

    /**
     * The table position sequence value.
     */
    public static final int TablePositionSequence = 1610758;

    /**
     * The collimator shape sequence value.
     */
    public static final int CollimatorShapeSequence = 1610759;

    /**
     * The planes in acquisition value.
     */
    public static final int PlanesInAcquisition = 1610768;

    /**
     * The xaxrf frame characteristics sequence value.
     */
    public static final int XAXRFFrameCharacteristicsSequence = 1610770;

    /**
     * The frame acquisition sequence value.
     */
    public static final int FrameAcquisitionSequence = 1610775;

    /**
     * The x ray receptor type value.
     */
    public static final int XRayReceptorType = 1610784;

    /**
     * The acquisition protocol name value.
     */
    public static final int AcquisitionProtocolName = 1610787;

    /**
     * The acquisition protocol description value.
     */
    public static final int AcquisitionProtocolDescription = 1610788;

    /**
     * The contrast bolus ingredient opaque value.
     */
    public static final int ContrastBolusIngredientOpaque = 1610789;

    /**
     * The distance receptor plane to detector housing value.
     */
    public static final int DistanceReceptorPlaneToDetectorHousing = 1610790;

    /**
     * The intensifier active shape value.
     */
    public static final int IntensifierActiveShape = 1610791;

    /**
     * The intensifier active dimensions value.
     */
    public static final int IntensifierActiveDimensions = 1610792;

    /**
     * The physical detector size value.
     */
    public static final int PhysicalDetectorSize = 1610793;

    /**
     * The position of isocenter projection value.
     */
    public static final int PositionOfIsocenterProjection = 1610800;

    /**
     * The field of view sequence value.
     */
    public static final int FieldOfViewSequence = 1610802;

    /**
     * The field of view description value.
     */
    public static final int FieldOfViewDescription = 1610803;

    /**
     * The exposure control sensing regions sequence value.
     */
    public static final int ExposureControlSensingRegionsSequence = 1610804;

    /**
     * The exposure control sensing region shape value.
     */
    public static final int ExposureControlSensingRegionShape = 1610805;

    /**
     * The exposure control sensing region left vertical edge value.
     */
    public static final int ExposureControlSensingRegionLeftVerticalEdge = 1610806;

    /**
     * The exposure control sensing region right vertical edge value.
     */
    public static final int ExposureControlSensingRegionRightVerticalEdge = 1610807;

    /**
     * The exposure control sensing region upper horizontal edge value.
     */
    public static final int ExposureControlSensingRegionUpperHorizontalEdge = 1610808;

    /**
     * The exposure control sensing region lower horizontal edge value.
     */
    public static final int ExposureControlSensingRegionLowerHorizontalEdge = 1610809;

    /**
     * The center of circular exposure control sensing region value.
     */
    public static final int CenterOfCircularExposureControlSensingRegion = 1610816;

    /**
     * The radius of circular exposure control sensing region value.
     */
    public static final int RadiusOfCircularExposureControlSensingRegion = 1610817;

    /**
     * The vertices of the polygonal exposure control sensing region value.
     */
    public static final int VerticesOfThePolygonalExposureControlSensingRegion = 1610818;

    /**
     * The column angulation patient value.
     */
    public static final int ColumnAngulationPatient = 1610823;

    /**
     * The beam angle value.
     */
    public static final int BeamAngle = 1610825;

    /**
     * The frame detector parameters sequence value.
     */
    public static final int FrameDetectorParametersSequence = 1610833;

    /**
     * The calculated anatomy thickness value.
     */
    public static final int CalculatedAnatomyThickness = 1610834;

    /**
     * The calibration sequence value.
     */
    public static final int CalibrationSequence = 1610837;

    /**
     * The object thickness sequence value.
     */
    public static final int ObjectThicknessSequence = 1610838;

    /**
     * The plane identification value.
     */
    public static final int PlaneIdentification = 1610839;

    /**
     * The field of view dimensions in float value.
     */
    public static final int FieldOfViewDimensionsInFloat = 1610849;

    /**
     * The isocenter reference system sequence value.
     */
    public static final int IsocenterReferenceSystemSequence = 1610850;

    /**
     * The positioner isocenter primary angle value.
     */
    public static final int PositionerIsocenterPrimaryAngle = 1610851;

    /**
     * The positioner isocenter secondary angle value.
     */
    public static final int PositionerIsocenterSecondaryAngle = 1610852;

    /**
     * The positioner isocenter detector rotation angle value.
     */
    public static final int PositionerIsocenterDetectorRotationAngle = 1610853;

    /**
     * The table x position to isocenter value.
     */
    public static final int TableXPositionToIsocenter = 1610854;

    /**
     * The table y position to isocenter value.
     */
    public static final int TableYPositionToIsocenter = 1610855;

    /**
     * The table z position to isocenter value.
     */
    public static final int TableZPositionToIsocenter = 1610856;

    /**
     * The table horizontal rotation angle value.
     */
    public static final int TableHorizontalRotationAngle = 1610857;

    /**
     * The table head tilt angle value.
     */
    public static final int TableHeadTiltAngle = 1610864;

    /**
     * The table cradle tilt angle value.
     */
    public static final int TableCradleTiltAngle = 1610865;

    /**
     * The frame display shutter sequence value.
     */
    public static final int FrameDisplayShutterSequence = 1610866;

    /**
     * The acquired image area dose product value.
     */
    public static final int AcquiredImageAreaDoseProduct = 1610867;

    /**
     * The c arm positioner tabletop relationship value.
     */
    public static final int CArmPositionerTabletopRelationship = 1610868;

    /**
     * The x ray geometry sequence value.
     */
    public static final int XRayGeometrySequence = 1610870;

    /**
     * The irradiation event identification sequence value.
     */
    public static final int IrradiationEventIdentificationSequence = 1610871;

    /**
     * The x ray3 d frame type sequence value.
     */
    public static final int XRay3DFrameTypeSequence = 1611012;

    /**
     * The contributing sources sequence value.
     */
    public static final int ContributingSourcesSequence = 1611014;

    /**
     * The x ray3 d acquisition sequence value.
     */
    public static final int XRay3DAcquisitionSequence = 1611015;

    /**
     * The primary positioner scan arc value.
     */
    public static final int PrimaryPositionerScanArc = 1611016;

    /**
     * The secondary positioner scan arc value.
     */
    public static final int SecondaryPositionerScanArc = 1611017;

    /**
     * The primary positioner scan start angle value.
     */
    public static final int PrimaryPositionerScanStartAngle = 1611024;

    /**
     * The secondary positioner scan start angle value.
     */
    public static final int SecondaryPositionerScanStartAngle = 1611025;

    /**
     * The primary positioner increment value.
     */
    public static final int PrimaryPositionerIncrement = 1611028;

    /**
     * The secondary positioner increment value.
     */
    public static final int SecondaryPositionerIncrement = 1611029;

    /**
     * The start acquisition date time value.
     */
    public static final int StartAcquisitionDateTime = 1611030;

    /**
     * The end acquisition date time value.
     */
    public static final int EndAcquisitionDateTime = 1611031;

    /**
     * The primary positioner increment sign value.
     */
    public static final int PrimaryPositionerIncrementSign = 1611032;

    /**
     * The secondary positioner increment sign value.
     */
    public static final int SecondaryPositionerIncrementSign = 1611033;

    /**
     * The application name value.
     */
    public static final int ApplicationName = 1611044;

    /**
     * The application version value.
     */
    public static final int ApplicationVersion = 1611045;

    /**
     * The application manufacturer value.
     */
    public static final int ApplicationManufacturer = 1611046;

    /**
     * The algorithm type value.
     */
    public static final int AlgorithmType = 1611047;

    /**
     * The algorithm description value.
     */
    public static final int AlgorithmDescription = 1611048;

    /**
     * The x ray3 d reconstruction sequence value.
     */
    public static final int XRay3DReconstructionSequence = 1611056;

    /**
     * The reconstruction description value.
     */
    public static final int ReconstructionDescription = 1611057;

    /**
     * The per projection acquisition sequence value.
     */
    public static final int PerProjectionAcquisitionSequence = 1611064;

    /**
     * The detector position sequence value.
     */
    public static final int DetectorPositionSequence = 1611073;

    /**
     * The x ray acquisition dose sequence value.
     */
    public static final int XRayAcquisitionDoseSequence = 1611074;

    /**
     * The x ray source isocenter primary angle value.
     */
    public static final int XRaySourceIsocenterPrimaryAngle = 1611075;

    /**
     * The x ray source isocenter secondary angle value.
     */
    public static final int XRaySourceIsocenterSecondaryAngle = 1611076;

    /**
     * The breast support isocenter primary angle value.
     */
    public static final int BreastSupportIsocenterPrimaryAngle = 1611077;

    /**
     * The breast support isocenter secondary angle value.
     */
    public static final int BreastSupportIsocenterSecondaryAngle = 1611078;

    /**
     * The breast support x position to isocenter value.
     */
    public static final int BreastSupportXPositionToIsocenter = 1611079;

    /**
     * The breast support y position to isocenter value.
     */
    public static final int BreastSupportYPositionToIsocenter = 1611080;

    /**
     * The breast support z position to isocenter value.
     */
    public static final int BreastSupportZPositionToIsocenter = 1611081;

    /**
     * The detector isocenter primary angle value.
     */
    public static final int DetectorIsocenterPrimaryAngle = 1611088;

    /**
     * The detector isocenter secondary angle value.
     */
    public static final int DetectorIsocenterSecondaryAngle = 1611089;

    /**
     * The detector x position to isocenter value.
     */
    public static final int DetectorXPositionToIsocenter = 1611090;

    /**
     * The detector y position to isocenter value.
     */
    public static final int DetectorYPositionToIsocenter = 1611091;

    /**
     * The detector z position to isocenter value.
     */
    public static final int DetectorZPositionToIsocenter = 1611092;

    /**
     * The x ray grid sequence value.
     */
    public static final int XRayGridSequence = 1611093;

    /**
     * The x ray filter sequence value.
     */
    public static final int XRayFilterSequence = 1611094;

    /**
     * The detector active area tlhc position value.
     */
    public static final int DetectorActiveAreaTLHCPosition = 1611095;

    /**
     * The detector active area orientation value.
     */
    public static final int DetectorActiveAreaOrientation = 1611096;

    /**
     * The positioner primary angle direction value.
     */
    public static final int PositionerPrimaryAngleDirection = 1611097;

    /**
     * The diffusion b matrix sequence value.
     */
    public static final int DiffusionBMatrixSequence = 1611265;

    /**
     * The diffusion b value xx value.
     */
    public static final int DiffusionBValueXX = 1611266;

    /**
     * The diffusion b value xy value.
     */
    public static final int DiffusionBValueXY = 1611267;

    /**
     * The diffusion b value xz value.
     */
    public static final int DiffusionBValueXZ = 1611268;

    /**
     * The diffusion b value yy value.
     */
    public static final int DiffusionBValueYY = 1611269;

    /**
     * The diffusion b value yz value.
     */
    public static final int DiffusionBValueYZ = 1611270;

    /**
     * The diffusion b value zz value.
     */
    public static final int DiffusionBValueZZ = 1611271;

    /**
     * The functional mr sequence value.
     */
    public static final int FunctionalMRSequence = 1611297;

    /**
     * The functional settling phase frames present value.
     */
    public static final int FunctionalSettlingPhaseFramesPresent = 1611298;

    /**
     * The functional sync pulse value.
     */
    public static final int FunctionalSyncPulse = 1611299;

    /**
     * The settling phase frame value.
     */
    public static final int SettlingPhaseFrame = 1611300;

    /**
     * The decay correction date time value.
     */
    public static final int DecayCorrectionDateTime = 1611521;

    /**
     * The start density threshold value.
     */
    public static final int StartDensityThreshold = 1611541;

    /**
     * The start relative density difference threshold value.
     */
    public static final int StartRelativeDensityDifferenceThreshold = 1611542;

    /**
     * The start cardiac trigger count threshold value.
     */
    public static final int StartCardiacTriggerCountThreshold = 1611543;

    /**
     * The start respiratory trigger count threshold value.
     */
    public static final int StartRespiratoryTriggerCountThreshold = 1611544;

    /**
     * The termination counts threshold value.
     */
    public static final int TerminationCountsThreshold = 1611545;

    /**
     * The termination density threshold value.
     */
    public static final int TerminationDensityThreshold = 1611552;

    /**
     * The termination relative density threshold value.
     */
    public static final int TerminationRelativeDensityThreshold = 1611553;

    /**
     * The termination time threshold value.
     */
    public static final int TerminationTimeThreshold = 1611554;

    /**
     * The termination cardiac trigger count threshold value.
     */
    public static final int TerminationCardiacTriggerCountThreshold = 1611555;

    /**
     * The termination respiratory trigger count threshold value.
     */
    public static final int TerminationRespiratoryTriggerCountThreshold = 1611556;

    /**
     * The detector geometry value.
     */
    public static final int DetectorGeometry = 1611557;

    /**
     * The transverse detector separation value.
     */
    public static final int TransverseDetectorSeparation = 1611558;

    /**
     * The axial detector dimension value.
     */
    public static final int AxialDetectorDimension = 1611559;

    /**
     * The radiopharmaceutical agent number value.
     */
    public static final int RadiopharmaceuticalAgentNumber = 1611561;

    /**
     * The pet frame acquisition sequence value.
     */
    public static final int PETFrameAcquisitionSequence = 1611570;

    /**
     * The pet detector motion details sequence value.
     */
    public static final int PETDetectorMotionDetailsSequence = 1611571;

    /**
     * The pet table dynamics sequence value.
     */
    public static final int PETTableDynamicsSequence = 1611572;

    /**
     * The pet position sequence value.
     */
    public static final int PETPositionSequence = 1611573;

    /**
     * The pet frame correction factors sequence value.
     */
    public static final int PETFrameCorrectionFactorsSequence = 1611574;

    /**
     * The radiopharmaceutical usage sequence value.
     */
    public static final int RadiopharmaceuticalUsageSequence = 1611575;

    /**
     * The attenuation correction source value.
     */
    public static final int AttenuationCorrectionSource = 1611576;

    /**
     * The number of iterations value.
     */
    public static final int NumberOfIterations = 1611577;

    /**
     * The number of subsets value.
     */
    public static final int NumberOfSubsets = 1611584;

    /**
     * The pet reconstruction sequence value.
     */
    public static final int PETReconstructionSequence = 1611593;

    /**
     * The pet frame type sequence value.
     */
    public static final int PETFrameTypeSequence = 1611601;

    /**
     * The time of flight information used value.
     */
    public static final int TimeOfFlightInformationUsed = 1611605;

    /**
     * The reconstruction type value.
     */
    public static final int ReconstructionType = 1611606;

    /**
     * The decay corrected value.
     */
    public static final int DecayCorrected = 1611608;

    /**
     * The attenuation corrected value.
     */
    public static final int AttenuationCorrected = 1611609;

    /**
     * The scatter corrected value.
     */
    public static final int ScatterCorrected = 1611616;

    /**
     * The dead time corrected value.
     */
    public static final int DeadTimeCorrected = 1611617;

    /**
     * The gantry motion corrected value.
     */
    public static final int GantryMotionCorrected = 1611618;

    /**
     * The patient motion corrected value.
     */
    public static final int PatientMotionCorrected = 1611619;

    /**
     * The count loss normalization corrected value.
     */
    public static final int CountLossNormalizationCorrected = 1611620;

    /**
     * The randoms corrected value.
     */
    public static final int RandomsCorrected = 1611621;

    /**
     * The non uniform radial sampling corrected value.
     */
    public static final int NonUniformRadialSamplingCorrected = 1611622;

    /**
     * The sensitivity calibrated value.
     */
    public static final int SensitivityCalibrated = 1611623;

    /**
     * The detector normalization correction value.
     */
    public static final int DetectorNormalizationCorrection = 1611624;

    /**
     * The iterative reconstruction method value.
     */
    public static final int IterativeReconstructionMethod = 1611625;

    /**
     * The attenuation correction temporal relationship value.
     */
    public static final int AttenuationCorrectionTemporalRelationship = 1611632;

    /**
     * The patient physiological state sequence value.
     */
    public static final int PatientPhysiologicalStateSequence = 1611633;

    /**
     * The patient physiological state code sequence value.
     */
    public static final int PatientPhysiologicalStateCodeSequence = 1611634;

    /**
     * The depths of focus value.
     */
    public static final int DepthsOfFocus = 1611777;

    /**
     * The excluded intervals sequence value.
     */
    public static final int ExcludedIntervalsSequence = 1611779;

    /**
     * The exclusion start date time value.
     */
    public static final int ExclusionStartDateTime = 1611780;

    /**
     * The exclusion duration value.
     */
    public static final int ExclusionDuration = 1611781;

    /**
     * The us image description sequence value.
     */
    public static final int USImageDescriptionSequence = 1611782;

    /**
     * The image data type sequence value.
     */
    public static final int ImageDataTypeSequence = 1611783;

    /**
     * The data type value.
     */
    public static final int DataType = 1611784;

    /**
     * The transducer scan pattern code sequence value.
     */
    public static final int TransducerScanPatternCodeSequence = 1611785;

    /**
     * The aliased data type value.
     */
    public static final int AliasedDataType = 1611787;

    /**
     * The position measuring device used value.
     */
    public static final int PositionMeasuringDeviceUsed = 1611788;

    /**
     * The transducer geometry code sequence value.
     */
    public static final int TransducerGeometryCodeSequence = 1611789;

    /**
     * The transducer beam steering code sequence value.
     */
    public static final int TransducerBeamSteeringCodeSequence = 1611790;

    /**
     * The transducer application code sequence value.
     */
    public static final int TransducerApplicationCodeSequence = 1611791;

    /**
     * The zero velocity pixel value value.
     */
    public static final int ZeroVelocityPixelValue = 1611792;

    /**
     * The photoacoustic excitation characteristics sequence value.
     */
    public static final int PhotoacousticExcitationCharacteristicsSequence = 1611809;

    /**
     * The excitation spectral width value.
     */
    public static final int ExcitationSpectralWidth = 1611810;

    /**
     * The excitation energy value.
     */
    public static final int ExcitationEnergy = 1611811;

    /**
     * The excitation pulse duration value.
     */
    public static final int ExcitationPulseDuration = 1611812;

    /**
     * The excitation wavelength sequence value.
     */
    public static final int ExcitationWavelengthSequence = 1611813;

    /**
     * The excitation wavelength value.
     */
    public static final int ExcitationWavelength = 1611814;

    /**
     * The illumination translation flag value.
     */
    public static final int IlluminationTranslationFlag = 1611816;

    /**
     * The acoustic coupling medium flag value.
     */
    public static final int AcousticCouplingMediumFlag = 1611817;

    /**
     * The acoustic coupling medium code sequence value.
     */
    public static final int AcousticCouplingMediumCodeSequence = 1611818;

    /**
     * The acoustic coupling medium temperature value.
     */
    public static final int AcousticCouplingMediumTemperature = 1611819;

    /**
     * The transducer response sequence value.
     */
    public static final int TransducerResponseSequence = 1611820;

    /**
     * The center frequency value.
     */
    public static final int CenterFrequency = 1611821;

    /**
     * The fractional bandwidth value.
     */
    public static final int FractionalBandwidth = 1611822;

    /**
     * The lower cutoff frequency value.
     */
    public static final int LowerCutoffFrequency = 1611823;

    /**
     * The upper cutoff frequency value.
     */
    public static final int UpperCutoffFrequency = 1611824;

    /**
     * The transducer technology sequence value.
     */
    public static final int TransducerTechnologySequence = 1611825;

    /**
     * The sound speed correction mechanism code sequence value.
     */
    public static final int SoundSpeedCorrectionMechanismCodeSequence = 1611826;

    /**
     * The object sound speed value.
     */
    public static final int ObjectSoundSpeed = 1611827;

    /**
     * The acoustic coupling medium sound speed value.
     */
    public static final int AcousticCouplingMediumSoundSpeed = 1611828;

    /**
     * The photoacoustic image frame type sequence value.
     */
    public static final int PhotoacousticImageFrameTypeSequence = 1611829;

    /**
     * The image data type code sequence value.
     */
    public static final int ImageDataTypeCodeSequence = 1611830;

    /**
     * The reference location label value.
     */
    public static final int ReferenceLocationLabel = 1612032;

    /**
     * The reference location description value.
     */
    public static final int ReferenceLocationDescription = 1612033;

    /**
     * The reference basis code sequence value.
     */
    public static final int ReferenceBasisCodeSequence = 1612034;

    /**
     * The reference geometry code sequence value.
     */
    public static final int ReferenceGeometryCodeSequence = 1612035;

    /**
     * The offset distance value.
     */
    public static final int OffsetDistance = 1612036;

    /**
     * The offset direction value.
     */
    public static final int OffsetDirection = 1612037;

    /**
     * The potential scheduled protocol code sequence value.
     */
    public static final int PotentialScheduledProtocolCodeSequence = 1612038;

    /**
     * The potential requested procedure code sequence value.
     */
    public static final int PotentialRequestedProcedureCodeSequence = 1612039;

    /**
     * The potential reasons for procedure value.
     */
    public static final int PotentialReasonsForProcedure = 1612040;

    /**
     * The potential reasons for procedure code sequence value.
     */
    public static final int PotentialReasonsForProcedureCodeSequence = 1612041;

    /**
     * The potential diagnostic tasks value.
     */
    public static final int PotentialDiagnosticTasks = 1612042;

    /**
     * The contraindications code sequence value.
     */
    public static final int ContraindicationsCodeSequence = 1612043;

    /**
     * The referenced defined protocol sequence value.
     */
    public static final int ReferencedDefinedProtocolSequence = 1612044;

    /**
     * The referenced performed protocol sequence value.
     */
    public static final int ReferencedPerformedProtocolSequence = 1612045;

    /**
     * The predecessor protocol sequence value.
     */
    public static final int PredecessorProtocolSequence = 1612046;

    /**
     * The protocol planning information value.
     */
    public static final int ProtocolPlanningInformation = 1612047;

    /**
     * The protocol design rationale value.
     */
    public static final int ProtocolDesignRationale = 1612048;

    /**
     * The patient specification sequence value.
     */
    public static final int PatientSpecificationSequence = 1612049;

    /**
     * The model specification sequence value.
     */
    public static final int ModelSpecificationSequence = 1612050;

    /**
     * The parameters specification sequence value.
     */
    public static final int ParametersSpecificationSequence = 1612051;

    /**
     * The instruction sequence value.
     */
    public static final int InstructionSequence = 1612052;

    /**
     * The instruction index value.
     */
    public static final int InstructionIndex = 1612053;

    /**
     * The instruction text value.
     */
    public static final int InstructionText = 1612054;

    /**
     * The instruction description value.
     */
    public static final int InstructionDescription = 1612055;

    /**
     * The instruction performed flag value.
     */
    public static final int InstructionPerformedFlag = 1612056;

    /**
     * The instruction performed date time value.
     */
    public static final int InstructionPerformedDateTime = 1612057;

    /**
     * The instruction performance comment value.
     */
    public static final int InstructionPerformanceComment = 1612058;

    /**
     * The patient positioning instruction sequence value.
     */
    public static final int PatientPositioningInstructionSequence = 1612059;

    /**
     * The positioning method code sequence value.
     */
    public static final int PositioningMethodCodeSequence = 1612060;

    /**
     * The positioning landmark sequence value.
     */
    public static final int PositioningLandmarkSequence = 1612061;

    /**
     * The target frame of reference uid value.
     */
    public static final int TargetFrameOfReferenceUID = 1612062;

    /**
     * The acquisition protocol element specification sequence value.
     */
    public static final int AcquisitionProtocolElementSpecificationSequence = 1612063;

    /**
     * The acquisition protocol element sequence value.
     */
    public static final int AcquisitionProtocolElementSequence = 1612064;

    /**
     * The protocol element number value.
     */
    public static final int ProtocolElementNumber = 1612065;

    /**
     * The protocol element name value.
     */
    public static final int ProtocolElementName = 1612066;

    /**
     * The protocol element characteristics summary value.
     */
    public static final int ProtocolElementCharacteristicsSummary = 1612067;

    /**
     * The protocol element purpose value.
     */
    public static final int ProtocolElementPurpose = 1612068;

    /**
     * The acquisition motion value.
     */
    public static final int AcquisitionMotion = 1612080;

    /**
     * The acquisition start location sequence value.
     */
    public static final int AcquisitionStartLocationSequence = 1612081;

    /**
     * The acquisition end location sequence value.
     */
    public static final int AcquisitionEndLocationSequence = 1612082;

    /**
     * The reconstruction protocol element specification sequence value.
     */
    public static final int ReconstructionProtocolElementSpecificationSequence = 1612083;

    /**
     * The reconstruction protocol element sequence value.
     */
    public static final int ReconstructionProtocolElementSequence = 1612084;

    /**
     * The storage protocol element specification sequence value.
     */
    public static final int StorageProtocolElementSpecificationSequence = 1612085;

    /**
     * The storage protocol element sequence value.
     */
    public static final int StorageProtocolElementSequence = 1612086;

    /**
     * The requested series description value.
     */
    public static final int RequestedSeriesDescription = 1612087;

    /**
     * The source acquisition protocol element number value.
     */
    public static final int SourceAcquisitionProtocolElementNumber = 1612088;

    /**
     * The source acquisition beam number value.
     */
    public static final int SourceAcquisitionBeamNumber = 1612089;

    /**
     * The source reconstruction protocol element number value.
     */
    public static final int SourceReconstructionProtocolElementNumber = 1612090;

    /**
     * The reconstruction start location sequence value.
     */
    public static final int ReconstructionStartLocationSequence = 1612091;

    /**
     * The reconstruction end location sequence value.
     */
    public static final int ReconstructionEndLocationSequence = 1612092;

    /**
     * The reconstruction algorithm sequence value.
     */
    public static final int ReconstructionAlgorithmSequence = 1612093;

    /**
     * The reconstruction target center location sequence value.
     */
    public static final int ReconstructionTargetCenterLocationSequence = 1612094;

    /**
     * The image filter description value.
     */
    public static final int ImageFilterDescription = 1612097;

    /**
     * The ctd ivol notification trigger value.
     */
    public static final int CTDIvolNotificationTrigger = 1612098;

    /**
     * The dlp notification trigger value.
     */
    public static final int DLPNotificationTrigger = 1612099;

    /**
     * The auto kvp selection type value.
     */
    public static final int AutoKVPSelectionType = 1612100;

    /**
     * The auto kvp upper bound value.
     */
    public static final int AutoKVPUpperBound = 1612101;

    /**
     * The auto kvp lower bound value.
     */
    public static final int AutoKVPLowerBound = 1612102;

    /**
     * The protocol defined patient position value.
     */
    public static final int ProtocolDefinedPatientPosition = 1612103;

    /**
     * The contributing equipment sequence value.
     */
    public static final int ContributingEquipmentSequence = 1613825;

    /**
     * The contribution date time value.
     */
    public static final int ContributionDateTime = 1613826;

    /**
     * The contribution description value.
     */
    public static final int ContributionDescription = 1613827;

    /**
     * The study instance uid value.
     */
    public static final int StudyInstanceUID = 2097165;

    /**
     * The series instance uid value.
     */
    public static final int SeriesInstanceUID = 2097166;

    /**
     * The study id value.
     */
    public static final int StudyID = 2097168;

    /**
     * The series number value.
     */
    public static final int SeriesNumber = 2097169;

    /**
     * The acquisition number value.
     */
    public static final int AcquisitionNumber = 2097170;

    /**
     * The instance number value.
     */
    public static final int InstanceNumber = 2097171;

    /**
     * The isotope number value.
     */
    public static final int IsotopeNumber = 2097172;

    /**
     * The phase number value.
     */
    public static final int PhaseNumber = 2097173;

    /**
     * The interval number value.
     */
    public static final int IntervalNumber = 2097174;

    /**
     * The time slot number value.
     */
    public static final int TimeSlotNumber = 2097175;

    /**
     * The angle number value.
     */
    public static final int AngleNumber = 2097176;

    /**
     * The item number value.
     */
    public static final int ItemNumber = 2097177;

    /**
     * The patient orientation value.
     */
    public static final int PatientOrientation = 2097184;

    /**
     * The overlay number value.
     */
    public static final int OverlayNumber = 2097186;

    /**
     * The curve number value.
     */
    public static final int CurveNumber = 2097188;

    /**
     * The lut number value.
     */
    public static final int LUTNumber = 2097190;

    /**
     * The pyramid label value.
     */
    public static final int PyramidLabel = 2097191;

    /**
     * The image position value.
     */
    public static final int ImagePosition = 2097200;

    /**
     * The image position patient value.
     */
    public static final int ImagePositionPatient = 2097202;

    /**
     * The image orientation value.
     */
    public static final int ImageOrientation = 2097205;

    /**
     * The image orientation patient value.
     */
    public static final int ImageOrientationPatient = 2097207;

    /**
     * The location value.
     */
    public static final int Location = 2097232;

    /**
     * The frame of reference uid value.
     */
    public static final int FrameOfReferenceUID = 2097234;

    /**
     * The laterality value.
     */
    public static final int Laterality = 2097248;

    /**
     * The image laterality value.
     */
    public static final int ImageLaterality = 2097250;

    /**
     * The image geometry type value.
     */
    public static final int ImageGeometryType = 2097264;

    /**
     * The masking image value.
     */
    public static final int MaskingImage = 2097280;

    /**
     * The report number value.
     */
    public static final int ReportNumber = 2097322;

    /**
     * The temporal position identifier value.
     */
    public static final int TemporalPositionIdentifier = 2097408;

    /**
     * The number of temporal positions value.
     */
    public static final int NumberOfTemporalPositions = 2097413;

    /**
     * The temporal resolution value.
     */
    public static final int TemporalResolution = 2097424;

    /**
     * The synchronization frame of reference uid value.
     */
    public static final int SynchronizationFrameOfReferenceUID = 2097664;

    /**
     * The sop instance uid of concatenation source value.
     */
    public static final int SOPInstanceUIDOfConcatenationSource = 2097730;

    /**
     * The series in study value.
     */
    public static final int SeriesInStudy = 2101248;

    /**
     * The acquisitions in series value.
     */
    public static final int AcquisitionsInSeries = 2101249;

    /**
     * The images in acquisition value.
     */
    public static final int ImagesInAcquisition = 2101250;

    /**
     * The images in series value.
     */
    public static final int ImagesInSeries = 2101251;

    /**
     * The acquisitions in study value.
     */
    public static final int AcquisitionsInStudy = 2101252;

    /**
     * The images in study value.
     */
    public static final int ImagesInStudy = 2101253;

    /**
     * The reference value.
     */
    public static final int Reference = 2101280;

    /**
     * The target position reference indicator value.
     */
    public static final int TargetPositionReferenceIndicator = 2101311;

    /**
     * The position reference indicator value.
     */
    public static final int PositionReferenceIndicator = 2101312;

    /**
     * The slice location value.
     */
    public static final int SliceLocation = 2101313;

    /**
     * The other study numbers value.
     */
    public static final int OtherStudyNumbers = 2101360;

    /**
     * The number of patient related studies value.
     */
    public static final int NumberOfPatientRelatedStudies = 2101760;

    /**
     * The number of patient related series value.
     */
    public static final int NumberOfPatientRelatedSeries = 2101762;

    /**
     * The number of patient related instances value.
     */
    public static final int NumberOfPatientRelatedInstances = 2101764;

    /**
     * The number of study related series value.
     */
    public static final int NumberOfStudyRelatedSeries = 2101766;

    /**
     * The number of study related instances value.
     */
    public static final int NumberOfStudyRelatedInstances = 2101768;

    /**
     * The number of series related instances value.
     */
    public static final int NumberOfSeriesRelatedInstances = 2101769;

    /**
     * The source image i ds value.
     */
    public static final int SourceImageIDs = 2109696;

    /**
     * The modifying device id value.
     */
    public static final int ModifyingDeviceID = 2110465;

    /**
     * The modified image id value.
     */
    public static final int ModifiedImageID = 2110466;

    /**
     * The modified image date value.
     */
    public static final int ModifiedImageDate = 2110467;

    /**
     * The modifying device manufacturer value.
     */
    public static final int ModifyingDeviceManufacturer = 2110468;

    /**
     * The modified image time value.
     */
    public static final int ModifiedImageTime = 2110469;

    /**
     * The modified image description value.
     */
    public static final int ModifiedImageDescription = 2110470;

    /**
     * The image comments value.
     */
    public static final int ImageComments = 2113536;

    /**
     * The original image identification value.
     */
    public static final int OriginalImageIdentification = 2117632;

    /**
     * The original image identification nomenclature value.
     */
    public static final int OriginalImageIdentificationNomenclature = 2117634;

    /**
     * The stack id value.
     */
    public static final int StackID = 2134102;

    /**
     * The in stack position number value.
     */
    public static final int InStackPositionNumber = 2134103;

    /**
     * The frame anatomy sequence value.
     */
    public static final int FrameAnatomySequence = 2134129;

    /**
     * The frame laterality value.
     */
    public static final int FrameLaterality = 2134130;

    /**
     * The frame content sequence value.
     */
    public static final int FrameContentSequence = 2134289;

    /**
     * The plane position sequence value.
     */
    public static final int PlanePositionSequence = 2134291;

    /**
     * The plane orientation sequence value.
     */
    public static final int PlaneOrientationSequence = 2134294;

    /**
     * The temporal position index value.
     */
    public static final int TemporalPositionIndex = 2134312;

    /**
     * The nominal cardiac trigger delay time value.
     */
    public static final int NominalCardiacTriggerDelayTime = 2134355;

    /**
     * The nominal cardiac trigger time prior to r peak value.
     */
    public static final int NominalCardiacTriggerTimePriorToRPeak = 2134356;

    /**
     * The actual cardiac trigger time prior to r peak value.
     */
    public static final int ActualCardiacTriggerTimePriorToRPeak = 2134357;

    /**
     * The frame acquisition number value.
     */
    public static final int FrameAcquisitionNumber = 2134358;

    /**
     * The dimension index values value.
     */
    public static final int DimensionIndexValues = 2134359;

    /**
     * The frame comments value.
     */
    public static final int FrameComments = 2134360;

    /**
     * The concatenation uid value.
     */
    public static final int ConcatenationUID = 2134369;

    /**
     * The in concatenation number value.
     */
    public static final int InConcatenationNumber = 2134370;

    /**
     * The in concatenation total number value.
     */
    public static final int InConcatenationTotalNumber = 2134371;

    /**
     * The dimension organization uid value.
     */
    public static final int DimensionOrganizationUID = 2134372;

    /**
     * The dimension index pointer value.
     */
    public static final int DimensionIndexPointer = 2134373;

    /**
     * The functional group pointer value.
     */
    public static final int FunctionalGroupPointer = 2134375;

    /**
     * The unassigned shared converted attributes sequence value.
     */
    public static final int UnassignedSharedConvertedAttributesSequence = 2134384;

    /**
     * The unassigned per frame converted attributes sequence value.
     */
    public static final int UnassignedPerFrameConvertedAttributesSequence = 2134385;

    /**
     * The conversion source attributes sequence value.
     */
    public static final int ConversionSourceAttributesSequence = 2134386;

    /**
     * The dimension index private creator value.
     */
    public static final int DimensionIndexPrivateCreator = 2134547;

    /**
     * The dimension organization sequence value.
     */
    public static final int DimensionOrganizationSequence = 2134561;

    /**
     * The dimension index sequence value.
     */
    public static final int DimensionIndexSequence = 2134562;

    /**
     * The concatenation frame offset number value.
     */
    public static final int ConcatenationFrameOffsetNumber = 2134568;

    /**
     * The functional group private creator value.
     */
    public static final int FunctionalGroupPrivateCreator = 2134584;

    /**
     * The nominal percentage of cardiac phase value.
     */
    public static final int NominalPercentageOfCardiacPhase = 2134593;

    /**
     * The nominal percentage of respiratory phase value.
     */
    public static final int NominalPercentageOfRespiratoryPhase = 2134597;

    /**
     * The starting respiratory amplitude value.
     */
    public static final int StartingRespiratoryAmplitude = 2134598;

    /**
     * The starting respiratory phase value.
     */
    public static final int StartingRespiratoryPhase = 2134599;

    /**
     * The ending respiratory amplitude value.
     */
    public static final int EndingRespiratoryAmplitude = 2134600;

    /**
     * The ending respiratory phase value.
     */
    public static final int EndingRespiratoryPhase = 2134601;

    /**
     * The respiratory trigger type value.
     */
    public static final int RespiratoryTriggerType = 2134608;

    /**
     * The rr interval time nominal value.
     */
    public static final int RRIntervalTimeNominal = 2134609;

    /**
     * The actual cardiac trigger delay time value.
     */
    public static final int ActualCardiacTriggerDelayTime = 2134610;

    /**
     * The respiratory synchronization sequence value.
     */
    public static final int RespiratorySynchronizationSequence = 2134611;

    /**
     * The respiratory interval time value.
     */
    public static final int RespiratoryIntervalTime = 2134612;

    /**
     * The nominal respiratory trigger delay time value.
     */
    public static final int NominalRespiratoryTriggerDelayTime = 2134613;

    /**
     * The respiratory trigger delay threshold value.
     */
    public static final int RespiratoryTriggerDelayThreshold = 2134614;

    /**
     * The actual respiratory trigger delay time value.
     */
    public static final int ActualRespiratoryTriggerDelayTime = 2134615;

    /**
     * The image position volume value.
     */
    public static final int ImagePositionVolume = 2134785;

    /**
     * The image orientation volume value.
     */
    public static final int ImageOrientationVolume = 2134786;

    /**
     * The ultrasound acquisition geometry value.
     */
    public static final int UltrasoundAcquisitionGeometry = 2134791;

    /**
     * The apex position value.
     */
    public static final int ApexPosition = 2134792;

    /**
     * The volume to transducer mapping matrix value.
     */
    public static final int VolumeToTransducerMappingMatrix = 2134793;

    /**
     * The volume to table mapping matrix value.
     */
    public static final int VolumeToTableMappingMatrix = 2134794;

    /**
     * The volume to transducer relationship value.
     */
    public static final int VolumeToTransducerRelationship = 2134795;

    /**
     * The patient frame of reference source value.
     */
    public static final int PatientFrameOfReferenceSource = 2134796;

    /**
     * The temporal position time offset value.
     */
    public static final int TemporalPositionTimeOffset = 2134797;

    /**
     * The plane position volume sequence value.
     */
    public static final int PlanePositionVolumeSequence = 2134798;

    /**
     * The plane orientation volume sequence value.
     */
    public static final int PlaneOrientationVolumeSequence = 2134799;

    /**
     * The temporal position sequence value.
     */
    public static final int TemporalPositionSequence = 2134800;

    /**
     * The dimension organization type value.
     */
    public static final int DimensionOrganizationType = 2134801;

    /**
     * The volume frame of reference uid value.
     */
    public static final int VolumeFrameOfReferenceUID = 2134802;

    /**
     * The table frame of reference uid value.
     */
    public static final int TableFrameOfReferenceUID = 2134803;

    /**
     * The dimension description label value.
     */
    public static final int DimensionDescriptionLabel = 2135073;

    /**
     * The patient orientation in frame sequence value.
     */
    public static final int PatientOrientationInFrameSequence = 2135120;

    /**
     * The frame label value.
     */
    public static final int FrameLabel = 2135123;

    /**
     * The acquisition index value.
     */
    public static final int AcquisitionIndex = 2135320;

    /**
     * The contributing sop instances reference sequence value.
     */
    public static final int ContributingSOPInstancesReferenceSequence = 2135337;

    /**
     * The reconstruction index value.
     */
    public static final int ReconstructionIndex = 2135350;

    /**
     * The light path filter pass through wavelength value.
     */
    public static final int LightPathFilterPassThroughWavelength = 2228225;

    /**
     * The light path filter pass band value.
     */
    public static final int LightPathFilterPassBand = 2228226;

    /**
     * The image path filter pass through wavelength value.
     */
    public static final int ImagePathFilterPassThroughWavelength = 2228227;

    /**
     * The image path filter pass band value.
     */
    public static final int ImagePathFilterPassBand = 2228228;

    /**
     * The patient eye movement commanded value.
     */
    public static final int PatientEyeMovementCommanded = 2228229;

    /**
     * The patient eye movement command code sequence value.
     */
    public static final int PatientEyeMovementCommandCodeSequence = 2228230;

    /**
     * The spherical lens power value.
     */
    public static final int SphericalLensPower = 2228231;

    /**
     * The cylinder lens power value.
     */
    public static final int CylinderLensPower = 2228232;

    /**
     * The cylinder axis value.
     */
    public static final int CylinderAxis = 2228233;

    /**
     * The emmetropic magnification value.
     */
    public static final int EmmetropicMagnification = 2228234;

    /**
     * The intra ocular pressure value.
     */
    public static final int IntraOcularPressure = 2228235;

    /**
     * The horizontal field of view value.
     */
    public static final int HorizontalFieldOfView = 2228236;

    /**
     * The pupil dilated value.
     */
    public static final int PupilDilated = 2228237;

    /**
     * The degree of dilation value.
     */
    public static final int DegreeOfDilation = 2228238;

    /**
     * The vertex distance value.
     */
    public static final int VertexDistance = 2228239;

    /**
     * The stereo baseline angle value.
     */
    public static final int StereoBaselineAngle = 2228240;

    /**
     * The stereo baseline displacement value.
     */
    public static final int StereoBaselineDisplacement = 2228241;

    /**
     * The stereo horizontal pixel offset value.
     */
    public static final int StereoHorizontalPixelOffset = 2228242;

    /**
     * The stereo vertical pixel offset value.
     */
    public static final int StereoVerticalPixelOffset = 2228243;

    /**
     * The stereo rotation value.
     */
    public static final int StereoRotation = 2228244;

    /**
     * The acquisition device type code sequence value.
     */
    public static final int AcquisitionDeviceTypeCodeSequence = 2228245;

    /**
     * The illumination type code sequence value.
     */
    public static final int IlluminationTypeCodeSequence = 2228246;

    /**
     * The light path filter type stack code sequence value.
     */
    public static final int LightPathFilterTypeStackCodeSequence = 2228247;

    /**
     * The image path filter type stack code sequence value.
     */
    public static final int ImagePathFilterTypeStackCodeSequence = 2228248;

    /**
     * The lenses code sequence value.
     */
    public static final int LensesCodeSequence = 2228249;

    /**
     * The channel description code sequence value.
     */
    public static final int ChannelDescriptionCodeSequence = 2228250;

    /**
     * The refractive state sequence value.
     */
    public static final int RefractiveStateSequence = 2228251;

    /**
     * The mydriatic agent code sequence value.
     */
    public static final int MydriaticAgentCodeSequence = 2228252;

    /**
     * The relative image position code sequence value.
     */
    public static final int RelativeImagePositionCodeSequence = 2228253;

    /**
     * The camera angle of view value.
     */
    public static final int CameraAngleOfView = 2228254;

    /**
     * The stereo pairs sequence value.
     */
    public static final int StereoPairsSequence = 2228256;

    /**
     * The left image sequence value.
     */
    public static final int LeftImageSequence = 2228257;

    /**
     * The right image sequence value.
     */
    public static final int RightImageSequence = 2228258;

    /**
     * The stereo pairs present value.
     */
    public static final int StereoPairsPresent = 2228264;

    /**
     * The axial length of the eye value.
     */
    public static final int AxialLengthOfTheEye = 2228272;

    /**
     * The ophthalmic frame location sequence value.
     */
    public static final int OphthalmicFrameLocationSequence = 2228273;

    /**
     * The reference coordinates value.
     */
    public static final int ReferenceCoordinates = 2228274;

    /**
     * The depth spatial resolution value.
     */
    public static final int DepthSpatialResolution = 2228277;

    /**
     * The maximum depth distortion value.
     */
    public static final int MaximumDepthDistortion = 2228278;

    /**
     * The along scan spatial resolution value.
     */
    public static final int AlongScanSpatialResolution = 2228279;

    /**
     * The maximum along scan distortion value.
     */
    public static final int MaximumAlongScanDistortion = 2228280;

    /**
     * The ophthalmic image orientation value.
     */
    public static final int OphthalmicImageOrientation = 2228281;

    /**
     * The depth of transverse image value.
     */
    public static final int DepthOfTransverseImage = 2228289;

    /**
     * The mydriatic agent concentration units sequence value.
     */
    public static final int MydriaticAgentConcentrationUnitsSequence = 2228290;

    /**
     * The across scan spatial resolution value.
     */
    public static final int AcrossScanSpatialResolution = 2228296;

    /**
     * The maximum across scan distortion value.
     */
    public static final int MaximumAcrossScanDistortion = 2228297;

    /**
     * The mydriatic agent concentration value.
     */
    public static final int MydriaticAgentConcentration = 2228302;

    /**
     * The illumination wave length value.
     */
    public static final int IlluminationWaveLength = 2228309;

    /**
     * The illumination power value.
     */
    public static final int IlluminationPower = 2228310;

    /**
     * The illumination bandwidth value.
     */
    public static final int IlluminationBandwidth = 2228311;

    /**
     * The mydriatic agent sequence value.
     */
    public static final int MydriaticAgentSequence = 2228312;

    /**
     * The ophthalmic axial measurements right eye sequence value.
     */
    public static final int OphthalmicAxialMeasurementsRightEyeSequence = 2232327;

    /**
     * The ophthalmic axial measurements left eye sequence value.
     */
    public static final int OphthalmicAxialMeasurementsLeftEyeSequence = 2232328;

    /**
     * The ophthalmic axial measurements device type value.
     */
    public static final int OphthalmicAxialMeasurementsDeviceType = 2232329;

    /**
     * The ophthalmic axial length measurements type value.
     */
    public static final int OphthalmicAxialLengthMeasurementsType = 2232336;

    /**
     * The ophthalmic axial length sequence value.
     */
    public static final int OphthalmicAxialLengthSequence = 2232338;

    /**
     * The ophthalmic axial length value.
     */
    public static final int OphthalmicAxialLength = 2232345;

    /**
     * The lens status code sequence value.
     */
    public static final int LensStatusCodeSequence = 2232356;

    /**
     * The vitreous status code sequence value.
     */
    public static final int VitreousStatusCodeSequence = 2232357;

    /**
     * The iol formula code sequence value.
     */
    public static final int IOLFormulaCodeSequence = 2232360;

    /**
     * The iol formula detail value.
     */
    public static final int IOLFormulaDetail = 2232361;

    /**
     * The keratometer index value.
     */
    public static final int KeratometerIndex = 2232371;

    /**
     * The source of ophthalmic axial length code sequence value.
     */
    public static final int SourceOfOphthalmicAxialLengthCodeSequence = 2232373;

    /**
     * The source of corneal size data code sequence value.
     */
    public static final int SourceOfCornealSizeDataCodeSequence = 2232374;

    /**
     * The target refraction value.
     */
    public static final int TargetRefraction = 2232375;

    /**
     * The refractive procedure occurred value.
     */
    public static final int RefractiveProcedureOccurred = 2232377;

    /**
     * The refractive surgery type code sequence value.
     */
    public static final int RefractiveSurgeryTypeCodeSequence = 2232384;

    /**
     * The ophthalmic ultrasound method code sequence value.
     */
    public static final int OphthalmicUltrasoundMethodCodeSequence = 2232388;

    /**
     * The surgically induced astigmatism sequence value.
     */
    public static final int SurgicallyInducedAstigmatismSequence = 2232389;

    /**
     * The type of optical correction value.
     */
    public static final int TypeOfOpticalCorrection = 2232390;

    /**
     * The toric iol power sequence value.
     */
    public static final int ToricIOLPowerSequence = 2232391;

    /**
     * The predicted toric error sequence value.
     */
    public static final int PredictedToricErrorSequence = 2232392;

    /**
     * The pre selected for implantation value.
     */
    public static final int PreSelectedForImplantation = 2232393;

    /**
     * The toric iol power for exact emmetropia sequence value.
     */
    public static final int ToricIOLPowerForExactEmmetropiaSequence = 2232394;

    /**
     * The toric iol power for exact target refraction sequence value.
     */
    public static final int ToricIOLPowerForExactTargetRefractionSequence = 2232395;

    /**
     * The ophthalmic axial length measurements sequence value.
     */
    public static final int OphthalmicAxialLengthMeasurementsSequence = 2232400;

    /**
     * The iol power value.
     */
    public static final int IOLPower = 2232403;

    /**
     * The predicted refractive error value.
     */
    public static final int PredictedRefractiveError = 2232404;

    /**
     * The ophthalmic axial length velocity value.
     */
    public static final int OphthalmicAxialLengthVelocity = 2232409;

    /**
     * The lens status description value.
     */
    public static final int LensStatusDescription = 2232421;

    /**
     * The vitreous status description value.
     */
    public static final int VitreousStatusDescription = 2232422;

    /**
     * The iol power sequence value.
     */
    public static final int IOLPowerSequence = 2232464;

    /**
     * The lens constant sequence value.
     */
    public static final int LensConstantSequence = 2232466;

    /**
     * The iol manufacturer value.
     */
    public static final int IOLManufacturer = 2232467;

    /**
     * The lens constant description value.
     */
    public static final int LensConstantDescription = 2232468;

    /**
     * The implant name value.
     */
    public static final int ImplantName = 2232469;

    /**
     * The keratometry measurement type code sequence value.
     */
    public static final int KeratometryMeasurementTypeCodeSequence = 2232470;

    /**
     * The implant part number value.
     */
    public static final int ImplantPartNumber = 2232471;

    /**
     * The referenced ophthalmic axial measurements sequence value.
     */
    public static final int ReferencedOphthalmicAxialMeasurementsSequence = 2232576;

    /**
     * The ophthalmic axial length measurements segment name code sequence value.
     */
    public static final int OphthalmicAxialLengthMeasurementsSegmentNameCodeSequence = 2232577;

    /**
     * The refractive error before refractive surgery code sequence value.
     */
    public static final int RefractiveErrorBeforeRefractiveSurgeryCodeSequence = 2232579;

    /**
     * The iol power for exact emmetropia value.
     */
    public static final int IOLPowerForExactEmmetropia = 2232609;

    /**
     * The iol power for exact target refraction value.
     */
    public static final int IOLPowerForExactTargetRefraction = 2232610;

    /**
     * The anterior chamber depth definition code sequence value.
     */
    public static final int AnteriorChamberDepthDefinitionCodeSequence = 2232613;

    /**
     * The lens thickness sequence value.
     */
    public static final int LensThicknessSequence = 2232615;

    /**
     * The anterior chamber depth sequence value.
     */
    public static final int AnteriorChamberDepthSequence = 2232616;

    /**
     * The calculation comment sequence value.
     */
    public static final int CalculationCommentSequence = 2232618;

    /**
     * The calculation comment type value.
     */
    public static final int CalculationCommentType = 2232619;

    /**
     * The calculation comment value.
     */
    public static final int CalculationComment = 2232620;

    /**
     * The lens thickness value.
     */
    public static final int LensThickness = 2232624;

    /**
     * The anterior chamber depth value.
     */
    public static final int AnteriorChamberDepth = 2232625;

    /**
     * The source of lens thickness data code sequence value.
     */
    public static final int SourceOfLensThicknessDataCodeSequence = 2232626;

    /**
     * The source of anterior chamber depth data code sequence value.
     */
    public static final int SourceOfAnteriorChamberDepthDataCodeSequence = 2232627;

    /**
     * The source of refractive measurements sequence value.
     */
    public static final int SourceOfRefractiveMeasurementsSequence = 2232628;

    /**
     * The source of refractive measurements code sequence value.
     */
    public static final int SourceOfRefractiveMeasurementsCodeSequence = 2232629;

    /**
     * The ophthalmic axial length measurement modified value.
     */
    public static final int OphthalmicAxialLengthMeasurementModified = 2232640;

    /**
     * The ophthalmic axial length data source code sequence value.
     */
    public static final int OphthalmicAxialLengthDataSourceCodeSequence = 2232656;

    /**
     * The ophthalmic axial length acquisition method code sequence value.
     */
    public static final int OphthalmicAxialLengthAcquisitionMethodCodeSequence = 2232659;

    /**
     * The signal to noise ratio value.
     */
    public static final int SignalToNoiseRatio = 2232661;

    /**
     * The ophthalmic axial length data source description value.
     */
    public static final int OphthalmicAxialLengthDataSourceDescription = 2232665;

    /**
     * The ophthalmic axial length measurements total length sequence value.
     */
    public static final int OphthalmicAxialLengthMeasurementsTotalLengthSequence = 2232848;

    /**
     * The ophthalmic axial length measurements segmental length sequence value.
     */
    public static final int OphthalmicAxialLengthMeasurementsSegmentalLengthSequence = 2232849;

    /**
     * The ophthalmic axial length measurements length summation sequence value.
     */
    public static final int OphthalmicAxialLengthMeasurementsLengthSummationSequence = 2232850;

    /**
     * The ultrasound ophthalmic axial length measurements sequence value.
     */
    public static final int UltrasoundOphthalmicAxialLengthMeasurementsSequence = 2232864;

    /**
     * The optical ophthalmic axial length measurements sequence value.
     */
    public static final int OpticalOphthalmicAxialLengthMeasurementsSequence = 2232869;

    /**
     * The ultrasound selected ophthalmic axial length sequence value.
     */
    public static final int UltrasoundSelectedOphthalmicAxialLengthSequence = 2232880;

    /**
     * The ophthalmic axial length selection method code sequence value.
     */
    public static final int OphthalmicAxialLengthSelectionMethodCodeSequence = 2232912;

    /**
     * The optical selected ophthalmic axial length sequence value.
     */
    public static final int OpticalSelectedOphthalmicAxialLengthSequence = 2232917;

    /**
     * The selected segmental ophthalmic axial length sequence value.
     */
    public static final int SelectedSegmentalOphthalmicAxialLengthSequence = 2232919;

    /**
     * The selected total ophthalmic axial length sequence value.
     */
    public static final int SelectedTotalOphthalmicAxialLengthSequence = 2232928;

    /**
     * The ophthalmic axial length quality metric sequence value.
     */
    public static final int OphthalmicAxialLengthQualityMetricSequence = 2232930;

    /**
     * The ophthalmic axial length quality metric type code sequence value.
     */
    public static final int OphthalmicAxialLengthQualityMetricTypeCodeSequence = 2232933;

    /**
     * The ophthalmic axial length quality metric type description value.
     */
    public static final int OphthalmicAxialLengthQualityMetricTypeDescription = 2232947;

    /**
     * The intraocular lens calculations right eye sequence value.
     */
    public static final int IntraocularLensCalculationsRightEyeSequence = 2233088;

    /**
     * The intraocular lens calculations left eye sequence value.
     */
    public static final int IntraocularLensCalculationsLeftEyeSequence = 2233104;

    /**
     * The referenced ophthalmic axial length measurement qc image sequence value.
     */
    public static final int ReferencedOphthalmicAxialLengthMeasurementQCImageSequence = 2233136;

    /**
     * The ophthalmic mapping device type value.
     */
    public static final int OphthalmicMappingDeviceType = 2233365;

    /**
     * The acquisition method code sequence value.
     */
    public static final int AcquisitionMethodCodeSequence = 2233376;

    /**
     * The acquisition method algorithm sequence value.
     */
    public static final int AcquisitionMethodAlgorithmSequence = 2233379;

    /**
     * The ophthalmic thickness map type code sequence value.
     */
    public static final int OphthalmicThicknessMapTypeCodeSequence = 2233398;

    /**
     * The ophthalmic thickness mapping normals sequence value.
     */
    public static final int OphthalmicThicknessMappingNormalsSequence = 2233411;

    /**
     * The retinal thickness definition code sequence value.
     */
    public static final int RetinalThicknessDefinitionCodeSequence = 2233413;

    /**
     * The pixel value mapping to coded concept sequence value.
     */
    public static final int PixelValueMappingToCodedConceptSequence = 2233424;

    /**
     * The mapped pixel value value.
     */
    public static final int MappedPixelValue = 2233426;

    /**
     * The pixel value mapping explanation value.
     */
    public static final int PixelValueMappingExplanation = 2233428;

    /**
     * The ophthalmic thickness map quality threshold sequence value.
     */
    public static final int OphthalmicThicknessMapQualityThresholdSequence = 2233432;

    /**
     * The ophthalmic thickness map threshold quality rating value.
     */
    public static final int OphthalmicThicknessMapThresholdQualityRating = 2233440;

    /**
     * The anatomic structure reference point value.
     */
    public static final int AnatomicStructureReferencePoint = 2233443;

    /**
     * The registration to localizer sequence value.
     */
    public static final int RegistrationToLocalizerSequence = 2233445;

    /**
     * The registered localizer units value.
     */
    public static final int RegisteredLocalizerUnits = 2233446;

    /**
     * The registered localizer top left hand corner value.
     */
    public static final int RegisteredLocalizerTopLeftHandCorner = 2233447;

    /**
     * The registered localizer bottom right hand corner value.
     */
    public static final int RegisteredLocalizerBottomRightHandCorner = 2233448;

    /**
     * The ophthalmic thickness map quality rating sequence value.
     */
    public static final int OphthalmicThicknessMapQualityRatingSequence = 2233456;

    /**
     * The relevant opt attributes sequence value.
     */
    public static final int RelevantOPTAttributesSequence = 2233458;

    /**
     * The transformation method code sequence value.
     */
    public static final int TransformationMethodCodeSequence = 2233618;

    /**
     * The transformation algorithm sequence value.
     */
    public static final int TransformationAlgorithmSequence = 2233619;

    /**
     * The ophthalmic axial length method value.
     */
    public static final int OphthalmicAxialLengthMethod = 2233621;

    /**
     * The ophthalmic fov value.
     */
    public static final int OphthalmicFOV = 2233623;

    /**
     * The two dimensional to three dimensional map sequence value.
     */
    public static final int TwoDimensionalToThreeDimensionalMapSequence = 2233624;

    /**
     * The wide field ophthalmic photography quality rating sequence value.
     */
    public static final int WideFieldOphthalmicPhotographyQualityRatingSequence = 2233637;

    /**
     * The wide field ophthalmic photography quality threshold sequence value.
     */
    public static final int WideFieldOphthalmicPhotographyQualityThresholdSequence = 2233638;

    /**
     * The wide field ophthalmic photography threshold quality rating value.
     */
    public static final int WideFieldOphthalmicPhotographyThresholdQualityRating = 2233639;

    /**
     * The x coordinates center pixel view angle value.
     */
    public static final int XCoordinatesCenterPixelViewAngle = 2233640;

    /**
     * The y coordinates center pixel view angle value.
     */
    public static final int YCoordinatesCenterPixelViewAngle = 2233641;

    /**
     * The number of map points value.
     */
    public static final int NumberOfMapPoints = 2233648;

    /**
     * The two dimensional to three dimensional map data value.
     */
    public static final int TwoDimensionalToThreeDimensionalMapData = 2233649;

    /**
     * The derivation algorithm sequence value.
     */
    public static final int DerivationAlgorithmSequence = 2233874;

    /**
     * The ophthalmic image type code sequence value.
     */
    public static final int OphthalmicImageTypeCodeSequence = 2233877;

    /**
     * The ophthalmic image type description value.
     */
    public static final int OphthalmicImageTypeDescription = 2233878;

    /**
     * The scan pattern type code sequence value.
     */
    public static final int ScanPatternTypeCodeSequence = 2233880;

    /**
     * The referenced surface mesh identification sequence value.
     */
    public static final int ReferencedSurfaceMeshIdentificationSequence = 2233888;

    /**
     * The ophthalmic volumetric properties flag value.
     */
    public static final int OphthalmicVolumetricPropertiesFlag = 2233890;

    /**
     * The ophthalmic anatomic reference point x coordinate value.
     */
    public static final int OphthalmicAnatomicReferencePointXCoordinate = 2233892;

    /**
     * The ophthalmic anatomic reference point y coordinate value.
     */
    public static final int OphthalmicAnatomicReferencePointYCoordinate = 2233894;

    /**
     * The ophthalmic en face image quality rating sequence value.
     */
    public static final int OphthalmicEnFaceImageQualityRatingSequence = 2233896;

    /**
     * The quality threshold value.
     */
    public static final int QualityThreshold = 2233904;

    /**
     * The oct bscan analysis acquisition parameters sequence value.
     */
    public static final int OCTBscanAnalysisAcquisitionParametersSequence = 2233920;

    /**
     * The number of bscans per frame value.
     */
    public static final int NumberOfBscansPerFrame = 2233922;

    /**
     * The bscan slab thickness value.
     */
    public static final int BscanSlabThickness = 2233923;

    /**
     * The distance between bscan slabs value.
     */
    public static final int DistanceBetweenBscanSlabs = 2233924;

    /**
     * The bscan cycle time value.
     */
    public static final int BscanCycleTime = 2233925;

    /**
     * The bscan cycle time vector value.
     */
    public static final int BscanCycleTimeVector = 2233926;

    /**
     * The ascan rate value.
     */
    public static final int AscanRate = 2233929;

    /**
     * The bscan rate value.
     */
    public static final int BscanRate = 2233936;

    /**
     * The surface mesh z pixel offset value.
     */
    public static final int SurfaceMeshZPixelOffset = 2233944;

    /**
     * The visual field horizontal extent value.
     */
    public static final int VisualFieldHorizontalExtent = 2359312;

    /**
     * The visual field vertical extent value.
     */
    public static final int VisualFieldVerticalExtent = 2359313;

    /**
     * The visual field shape value.
     */
    public static final int VisualFieldShape = 2359314;

    /**
     * The screening test mode code sequence value.
     */
    public static final int ScreeningTestModeCodeSequence = 2359318;

    /**
     * The maximum stimulus luminance value.
     */
    public static final int MaximumStimulusLuminance = 2359320;

    /**
     * The background luminance value.
     */
    public static final int BackgroundLuminance = 2359328;

    /**
     * The stimulus color code sequence value.
     */
    public static final int StimulusColorCodeSequence = 2359329;

    /**
     * The background illumination color code sequence value.
     */
    public static final int BackgroundIlluminationColorCodeSequence = 2359332;

    /**
     * The stimulus area value.
     */
    public static final int StimulusArea = 2359333;

    /**
     * The stimulus presentation time value.
     */
    public static final int StimulusPresentationTime = 2359336;

    /**
     * The fixation sequence value.
     */
    public static final int FixationSequence = 2359346;

    /**
     * The fixation monitoring code sequence value.
     */
    public static final int FixationMonitoringCodeSequence = 2359347;

    /**
     * The visual field catch trial sequence value.
     */
    public static final int VisualFieldCatchTrialSequence = 2359348;

    /**
     * The fixation checked quantity value.
     */
    public static final int FixationCheckedQuantity = 2359349;

    /**
     * The patient not properly fixated quantity value.
     */
    public static final int PatientNotProperlyFixatedQuantity = 2359350;

    /**
     * The presented visual stimuli data flag value.
     */
    public static final int PresentedVisualStimuliDataFlag = 2359351;

    /**
     * The number of visual stimuli value.
     */
    public static final int NumberOfVisualStimuli = 2359352;

    /**
     * The excessive fixation losses data flag value.
     */
    public static final int ExcessiveFixationLossesDataFlag = 2359353;

    /**
     * The excessive fixation losses value.
     */
    public static final int ExcessiveFixationLosses = 2359360;

    /**
     * The stimuli retesting quantity value.
     */
    public static final int StimuliRetestingQuantity = 2359362;

    /**
     * The comments on patient performance of visual field value.
     */
    public static final int CommentsOnPatientPerformanceOfVisualField = 2359364;

    /**
     * The false negatives estimate flag value.
     */
    public static final int FalseNegativesEstimateFlag = 2359365;

    /**
     * The false negatives estimate value.
     */
    public static final int FalseNegativesEstimate = 2359366;

    /**
     * The negative catch trials quantity value.
     */
    public static final int NegativeCatchTrialsQuantity = 2359368;

    /**
     * The false negatives quantity value.
     */
    public static final int FalseNegativesQuantity = 2359376;

    /**
     * The excessive false negatives data flag value.
     */
    public static final int ExcessiveFalseNegativesDataFlag = 2359377;

    /**
     * The excessive false negatives value.
     */
    public static final int ExcessiveFalseNegatives = 2359378;

    /**
     * The false positives estimate flag value.
     */
    public static final int FalsePositivesEstimateFlag = 2359379;

    /**
     * The false positives estimate value.
     */
    public static final int FalsePositivesEstimate = 2359380;

    /**
     * The catch trials data flag value.
     */
    public static final int CatchTrialsDataFlag = 2359381;

    /**
     * The positive catch trials quantity value.
     */
    public static final int PositiveCatchTrialsQuantity = 2359382;

    /**
     * The test point normals data flag value.
     */
    public static final int TestPointNormalsDataFlag = 2359383;

    /**
     * The test point normals sequence value.
     */
    public static final int TestPointNormalsSequence = 2359384;

    /**
     * The global deviation probability normals flag value.
     */
    public static final int GlobalDeviationProbabilityNormalsFlag = 2359385;

    /**
     * The false positives quantity value.
     */
    public static final int FalsePositivesQuantity = 2359392;

    /**
     * The excessive false positives data flag value.
     */
    public static final int ExcessiveFalsePositivesDataFlag = 2359393;

    /**
     * The excessive false positives value.
     */
    public static final int ExcessiveFalsePositives = 2359394;

    /**
     * The visual field test normals flag value.
     */
    public static final int VisualFieldTestNormalsFlag = 2359395;

    /**
     * The results normals sequence value.
     */
    public static final int ResultsNormalsSequence = 2359396;

    /**
     * The age corrected sensitivity deviation algorithm sequence value.
     */
    public static final int AgeCorrectedSensitivityDeviationAlgorithmSequence = 2359397;

    /**
     * The global deviation from normal value.
     */
    public static final int GlobalDeviationFromNormal = 2359398;

    /**
     * The generalized defect sensitivity deviation algorithm sequence value.
     */
    public static final int GeneralizedDefectSensitivityDeviationAlgorithmSequence = 2359399;

    /**
     * The localized deviation from normal value.
     */
    public static final int LocalizedDeviationFromNormal = 2359400;

    /**
     * The patient reliability indicator value.
     */
    public static final int PatientReliabilityIndicator = 2359401;

    /**
     * The visual field mean sensitivity value.
     */
    public static final int VisualFieldMeanSensitivity = 2359408;

    /**
     * The global deviation probability value.
     */
    public static final int GlobalDeviationProbability = 2359409;

    /**
     * The local deviation probability normals flag value.
     */
    public static final int LocalDeviationProbabilityNormalsFlag = 2359410;

    /**
     * The localized deviation probability value.
     */
    public static final int LocalizedDeviationProbability = 2359411;

    /**
     * The short term fluctuation calculated value.
     */
    public static final int ShortTermFluctuationCalculated = 2359412;

    /**
     * The short term fluctuation value.
     */
    public static final int ShortTermFluctuation = 2359413;

    /**
     * The short term fluctuation probability calculated value.
     */
    public static final int ShortTermFluctuationProbabilityCalculated = 2359414;

    /**
     * The short term fluctuation probability value.
     */
    public static final int ShortTermFluctuationProbability = 2359415;

    /**
     * The corrected localized deviation from normal calculated value.
     */
    public static final int CorrectedLocalizedDeviationFromNormalCalculated = 2359416;

    /**
     * The corrected localized deviation from normal value.
     */
    public static final int CorrectedLocalizedDeviationFromNormal = 2359417;

    /**
     * The corrected localized deviation from normal probability calculated value.
     */
    public static final int CorrectedLocalizedDeviationFromNormalProbabilityCalculated = 2359424;

    /**
     * The corrected localized deviation from normal probability value.
     */
    public static final int CorrectedLocalizedDeviationFromNormalProbability = 2359425;

    /**
     * The global deviation probability sequence value.
     */
    public static final int GlobalDeviationProbabilitySequence = 2359427;

    /**
     * The localized deviation probability sequence value.
     */
    public static final int LocalizedDeviationProbabilitySequence = 2359429;

    /**
     * The foveal sensitivity measured value.
     */
    public static final int FovealSensitivityMeasured = 2359430;

    /**
     * The foveal sensitivity value.
     */
    public static final int FovealSensitivity = 2359431;

    /**
     * The visual field test duration value.
     */
    public static final int VisualFieldTestDuration = 2359432;

    /**
     * The visual field test point sequence value.
     */
    public static final int VisualFieldTestPointSequence = 2359433;

    /**
     * The visual field test point x coordinate value.
     */
    public static final int VisualFieldTestPointXCoordinate = 2359440;

    /**
     * The visual field test point y coordinate value.
     */
    public static final int VisualFieldTestPointYCoordinate = 2359441;

    /**
     * The age corrected sensitivity deviation value value.
     */
    public static final int AgeCorrectedSensitivityDeviationValue = 2359442;

    /**
     * The stimulus results value.
     */
    public static final int StimulusResults = 2359443;

    /**
     * The sensitivity value value.
     */
    public static final int SensitivityValue = 2359444;

    /**
     * The retest stimulus seen value.
     */
    public static final int RetestStimulusSeen = 2359445;

    /**
     * The retest sensitivity value value.
     */
    public static final int RetestSensitivityValue = 2359446;

    /**
     * The visual field test point normals sequence value.
     */
    public static final int VisualFieldTestPointNormalsSequence = 2359447;

    /**
     * The quantified defect value.
     */
    public static final int QuantifiedDefect = 2359448;

    /**
     * The age corrected sensitivity deviation probability value value.
     */
    public static final int AgeCorrectedSensitivityDeviationProbabilityValue = 2359552;

    /**
     * The generalized defect corrected sensitivity deviation flag value.
     */
    public static final int GeneralizedDefectCorrectedSensitivityDeviationFlag = 2359554;

    /**
     * The generalized defect corrected sensitivity deviation value value.
     */
    public static final int GeneralizedDefectCorrectedSensitivityDeviationValue = 2359555;

    /**
     * The generalized defect corrected sensitivity deviation probability value value.
     */
    public static final int GeneralizedDefectCorrectedSensitivityDeviationProbabilityValue = 2359556;

    /**
     * The minimum sensitivity value value.
     */
    public static final int MinimumSensitivityValue = 2359557;

    /**
     * The blind spot localized value.
     */
    public static final int BlindSpotLocalized = 2359558;

    /**
     * The blind spot x coordinate value.
     */
    public static final int BlindSpotXCoordinate = 2359559;

    /**
     * The blind spot y coordinate value.
     */
    public static final int BlindSpotYCoordinate = 2359560;

    /**
     * The visual acuity measurement sequence value.
     */
    public static final int VisualAcuityMeasurementSequence = 2359568;

    /**
     * The refractive parameters used on patient sequence value.
     */
    public static final int RefractiveParametersUsedOnPatientSequence = 2359570;

    /**
     * The measurement laterality value.
     */
    public static final int MeasurementLaterality = 2359571;

    /**
     * The ophthalmic patient clinical information left eye sequence value.
     */
    public static final int OphthalmicPatientClinicalInformationLeftEyeSequence = 2359572;

    /**
     * The ophthalmic patient clinical information right eye sequence value.
     */
    public static final int OphthalmicPatientClinicalInformationRightEyeSequence = 2359573;

    /**
     * The foveal point normative data flag value.
     */
    public static final int FovealPointNormativeDataFlag = 2359575;

    /**
     * The foveal point probability value value.
     */
    public static final int FovealPointProbabilityValue = 2359576;

    /**
     * The screening baseline measured value.
     */
    public static final int ScreeningBaselineMeasured = 2359584;

    /**
     * The screening baseline measured sequence value.
     */
    public static final int ScreeningBaselineMeasuredSequence = 2359586;

    /**
     * The screening baseline type value.
     */
    public static final int ScreeningBaselineType = 2359588;

    /**
     * The screening baseline value value.
     */
    public static final int ScreeningBaselineValue = 2359590;

    /**
     * The algorithm source value.
     */
    public static final int AlgorithmSource = 2359810;

    /**
     * The data set name value.
     */
    public static final int DataSetName = 2360070;

    /**
     * The data set version value.
     */
    public static final int DataSetVersion = 2360071;

    /**
     * The data set source value.
     */
    public static final int DataSetSource = 2360072;

    /**
     * The data set description value.
     */
    public static final int DataSetDescription = 2360073;

    /**
     * The visual field test reliability global index sequence value.
     */
    public static final int VisualFieldTestReliabilityGlobalIndexSequence = 2360087;

    /**
     * The visual field global results index sequence value.
     */
    public static final int VisualFieldGlobalResultsIndexSequence = 2360096;

    /**
     * The data observation sequence value.
     */
    public static final int DataObservationSequence = 2360101;

    /**
     * The index normals flag value.
     */
    public static final int IndexNormalsFlag = 2360120;

    /**
     * The index probability value.
     */
    public static final int IndexProbability = 2360129;

    /**
     * The index probability sequence value.
     */
    public static final int IndexProbabilitySequence = 2360132;

    /**
     * The samples per pixel value.
     */
    public static final int SamplesPerPixel = 2621442;

    /**
     * The samples per pixel used value.
     */
    public static final int SamplesPerPixelUsed = 2621443;

    /**
     * The photometric interpretation value.
     */
    public static final int PhotometricInterpretation = 2621444;

    /**
     * The image dimensions value.
     */
    public static final int ImageDimensions = 2621445;

    /**
     * The planar configuration value.
     */
    public static final int PlanarConfiguration = 2621446;

    /**
     * The number of frames value.
     */
    public static final int NumberOfFrames = 2621448;

    /**
     * The frame increment pointer value.
     */
    public static final int FrameIncrementPointer = 2621449;

    /**
     * The frame dimension pointer value.
     */
    public static final int FrameDimensionPointer = 2621450;

    /**
     * The rows value.
     */
    public static final int Rows = 2621456;

    /**
     * The columns value.
     */
    public static final int Columns = 2621457;

    /**
     * The planes value.
     */
    public static final int Planes = 2621458;

    /**
     * The ultrasound color data present value.
     */
    public static final int UltrasoundColorDataPresent = 2621460;

    /**
     * The pixel spacing value.
     */
    public static final int PixelSpacing = 2621488;

    /**
     * The zoom factor value.
     */
    public static final int ZoomFactor = 2621489;

    /**
     * The zoom center value.
     */
    public static final int ZoomCenter = 2621490;

    /**
     * The pixel aspect ratio value.
     */
    public static final int PixelAspectRatio = 2621492;

    /**
     * The image format value.
     */
    public static final int ImageFormat = 2621504;

    /**
     * The manipulated image value.
     */
    public static final int ManipulatedImage = 2621520;

    /**
     * The corrected image value.
     */
    public static final int CorrectedImage = 2621521;

    /**
     * The compression recognition code value.
     */
    public static final int CompressionRecognitionCode = 2621535;

    /**
     * The compression code value.
     */
    public static final int CompressionCode = 2621536;

    /**
     * The compression originator value.
     */
    public static final int CompressionOriginator = 2621537;

    /**
     * The compression label value.
     */
    public static final int CompressionLabel = 2621538;

    /**
     * The compression description value.
     */
    public static final int CompressionDescription = 2621539;

    /**
     * The compression sequence value.
     */
    public static final int CompressionSequence = 2621541;

    /**
     * The compression step pointers value.
     */
    public static final int CompressionStepPointers = 2621542;

    /**
     * The repeat interval value.
     */
    public static final int RepeatInterval = 2621544;

    /**
     * The bits grouped value.
     */
    public static final int BitsGrouped = 2621545;

    /**
     * The perimeter table value.
     */
    public static final int PerimeterTable = 2621552;

    /**
     * The perimeter value value.
     */
    public static final int PerimeterValue = 2621553;

    /**
     * The predictor rows value.
     */
    public static final int PredictorRows = 2621568;

    /**
     * The predictor columns value.
     */
    public static final int PredictorColumns = 2621569;

    /**
     * The predictor constants value.
     */
    public static final int PredictorConstants = 2621570;

    /**
     * The blocked pixels value.
     */
    public static final int BlockedPixels = 2621584;

    /**
     * The block rows value.
     */
    public static final int BlockRows = 2621585;

    /**
     * The block columns value.
     */
    public static final int BlockColumns = 2621586;

    /**
     * The row overlap value.
     */
    public static final int RowOverlap = 2621587;

    /**
     * The column overlap value.
     */
    public static final int ColumnOverlap = 2621588;

    /**
     * The bits allocated value.
     */
    public static final int BitsAllocated = 2621696;

    /**
     * The bits stored value.
     */
    public static final int BitsStored = 2621697;

    /**
     * The high bit value.
     */
    public static final int HighBit = 2621698;

    /**
     * The pixel representation value.
     */
    public static final int PixelRepresentation = 2621699;

    /**
     * The smallest valid pixel value value.
     */
    public static final int SmallestValidPixelValue = 2621700;

    /**
     * The largest valid pixel value value.
     */
    public static final int LargestValidPixelValue = 2621701;

    /**
     * The smallest image pixel value value.
     */
    public static final int SmallestImagePixelValue = 2621702;

    /**
     * The largest image pixel value value.
     */
    public static final int LargestImagePixelValue = 2621703;

    /**
     * The smallest pixel value in series value.
     */
    public static final int SmallestPixelValueInSeries = 2621704;

    /**
     * The largest pixel value in series value.
     */
    public static final int LargestPixelValueInSeries = 2621705;

    /**
     * The smallest image pixel value in plane value.
     */
    public static final int SmallestImagePixelValueInPlane = 2621712;

    /**
     * The largest image pixel value in plane value.
     */
    public static final int LargestImagePixelValueInPlane = 2621713;

    /**
     * The pixel padding value value.
     */
    public static final int PixelPaddingValue = 2621728;

    /**
     * The pixel padding range limit value.
     */
    public static final int PixelPaddingRangeLimit = 2621729;

    /**
     * The float pixel padding value value.
     */
    public static final int FloatPixelPaddingValue = 2621730;

    /**
     * The double float pixel padding value value.
     */
    public static final int DoubleFloatPixelPaddingValue = 2621731;

    /**
     * The float pixel padding range limit value.
     */
    public static final int FloatPixelPaddingRangeLimit = 2621732;

    /**
     * The double float pixel padding range limit value.
     */
    public static final int DoubleFloatPixelPaddingRangeLimit = 2621733;

    /**
     * The image location value.
     */
    public static final int ImageLocation = 2621952;

    /**
     * The quality control image value.
     */
    public static final int QualityControlImage = 2622208;

    /**
     * The burned in annotation value.
     */
    public static final int BurnedInAnnotation = 2622209;

    /**
     * The recognizable visual features value.
     */
    public static final int RecognizableVisualFeatures = 2622210;

    /**
     * The longitudinal temporal information modified value.
     */
    public static final int LongitudinalTemporalInformationModified = 2622211;

    /**
     * The referenced color palette instance uid value.
     */
    public static final int ReferencedColorPaletteInstanceUID = 2622212;

    /**
     * The transform label value.
     */
    public static final int TransformLabel = 2622464;

    /**
     * The transform version number value.
     */
    public static final int TransformVersionNumber = 2622465;

    /**
     * The number of transform steps value.
     */
    public static final int NumberOfTransformSteps = 2622466;

    /**
     * The sequence of compressed data value.
     */
    public static final int SequenceOfCompressedData = 2622467;

    /**
     * The details of coefficients value.
     */
    public static final int DetailsOfCoefficients = 2622468;

    /**
     * The rows for nth order coefficients value.
     */
    public static final int RowsForNthOrderCoefficients = 2622464;

    /**
     * The columns for nth order coefficients value.
     */
    public static final int ColumnsForNthOrderCoefficients = 2622465;

    /**
     * The coefficient coding value.
     */
    public static final int CoefficientCoding = 2622466;

    /**
     * The coefficient coding pointers value.
     */
    public static final int CoefficientCodingPointers = 2622467;

    /**
     * The dct label value.
     */
    public static final int DCTLabel = 2623232;

    /**
     * The data block description value.
     */
    public static final int DataBlockDescription = 2623233;

    /**
     * The data block value.
     */
    public static final int DataBlock = 2623234;

    /**
     * The normalization factor format value.
     */
    public static final int NormalizationFactorFormat = 2623248;

    /**
     * The zonal map number format value.
     */
    public static final int ZonalMapNumberFormat = 2623264;

    /**
     * The zonal map location value.
     */
    public static final int ZonalMapLocation = 2623265;

    /**
     * The zonal map format value.
     */
    public static final int ZonalMapFormat = 2623266;

    /**
     * The adaptive map format value.
     */
    public static final int AdaptiveMapFormat = 2623280;

    /**
     * The code number format value.
     */
    public static final int CodeNumberFormat = 2623296;

    /**
     * The code label value.
     */
    public static final int CodeLabel = 2623488;

    /**
     * The number of tables value.
     */
    public static final int NumberOfTables = 2623490;

    /**
     * The code table location value.
     */
    public static final int CodeTableLocation = 2623491;

    /**
     * The bits for code word value.
     */
    public static final int BitsForCodeWord = 2623492;

    /**
     * The image data location value.
     */
    public static final int ImageDataLocation = 2623496;

    /**
     * The pixel spacing calibration type value.
     */
    public static final int PixelSpacingCalibrationType = 2624002;

    /**
     * The pixel spacing calibration description value.
     */
    public static final int PixelSpacingCalibrationDescription = 2624004;

    /**
     * The pixel intensity relationship value.
     */
    public static final int PixelIntensityRelationship = 2625600;

    /**
     * The pixel intensity relationship sign value.
     */
    public static final int PixelIntensityRelationshipSign = 2625601;

    /**
     * The window center value.
     */
    public static final int WindowCenter = 2625616;

    /**
     * The window width value.
     */
    public static final int WindowWidth = 2625617;

    /**
     * The rescale intercept value.
     */
    public static final int RescaleIntercept = 2625618;

    /**
     * The rescale slope value.
     */
    public static final int RescaleSlope = 2625619;

    /**
     * The rescale type value.
     */
    public static final int RescaleType = 2625620;

    /**
     * The window center width explanation value.
     */
    public static final int WindowCenterWidthExplanation = 2625621;

    /**
     * The voilut function value.
     */
    public static final int VOILUTFunction = 2625622;

    /**
     * The gray scale value.
     */
    public static final int GrayScale = 2625664;

    /**
     * The recommended viewing mode value.
     */
    public static final int RecommendedViewingMode = 2625680;

    /**
     * The gray lookup table descriptor value.
     */
    public static final int GrayLookupTableDescriptor = 2625792;

    /**
     * The red palette color lookup table descriptor value.
     */
    public static final int RedPaletteColorLookupTableDescriptor = 2625793;

    /**
     * The green palette color lookup table descriptor value.
     */
    public static final int GreenPaletteColorLookupTableDescriptor = 2625794;

    /**
     * The blue palette color lookup table descriptor value.
     */
    public static final int BluePaletteColorLookupTableDescriptor = 2625795;

    /**
     * The alpha palette color lookup table descriptor value.
     */
    public static final int AlphaPaletteColorLookupTableDescriptor = 2625796;

    /**
     * The large red palette color lookup table descriptor value.
     */
    public static final int LargeRedPaletteColorLookupTableDescriptor = 2625809;

    /**
     * The large green palette color lookup table descriptor value.
     */
    public static final int LargeGreenPaletteColorLookupTableDescriptor = 2625810;

    /**
     * The large blue palette color lookup table descriptor value.
     */
    public static final int LargeBluePaletteColorLookupTableDescriptor = 2625811;

    /**
     * The palette color lookup table uid value.
     */
    public static final int PaletteColorLookupTableUID = 2625945;

    /**
     * The gray lookup table data value.
     */
    public static final int GrayLookupTableData = 2626048;

    /**
     * The red palette color lookup table data value.
     */
    public static final int RedPaletteColorLookupTableData = 2626049;

    /**
     * The green palette color lookup table data value.
     */
    public static final int GreenPaletteColorLookupTableData = 2626050;

    /**
     * The blue palette color lookup table data value.
     */
    public static final int BluePaletteColorLookupTableData = 2626051;

    /**
     * The alpha palette color lookup table data value.
     */
    public static final int AlphaPaletteColorLookupTableData = 2626052;

    /**
     * The large red palette color lookup table data value.
     */
    public static final int LargeRedPaletteColorLookupTableData = 2626065;

    /**
     * The large green palette color lookup table data value.
     */
    public static final int LargeGreenPaletteColorLookupTableData = 2626066;

    /**
     * The large blue palette color lookup table data value.
     */
    public static final int LargeBluePaletteColorLookupTableData = 2626067;

    /**
     * The large palette color lookup table uid value.
     */
    public static final int LargePaletteColorLookupTableUID = 2626068;

    /**
     * The segmented red palette color lookup table data value.
     */
    public static final int SegmentedRedPaletteColorLookupTableData = 2626081;

    /**
     * The segmented green palette color lookup table data value.
     */
    public static final int SegmentedGreenPaletteColorLookupTableData = 2626082;

    /**
     * The segmented blue palette color lookup table data value.
     */
    public static final int SegmentedBluePaletteColorLookupTableData = 2626083;

    /**
     * The segmented alpha palette color lookup table data value.
     */
    public static final int SegmentedAlphaPaletteColorLookupTableData = 2626084;

    /**
     * The stored value color range sequence value.
     */
    public static final int StoredValueColorRangeSequence = 2626096;

    /**
     * The minimum stored value mapped value.
     */
    public static final int MinimumStoredValueMapped = 2626097;

    /**
     * The maximum stored value mapped value.
     */
    public static final int MaximumStoredValueMapped = 2626098;

    /**
     * The breast implant present value.
     */
    public static final int BreastImplantPresent = 2626304;

    /**
     * The partial view value.
     */
    public static final int PartialView = 2626384;

    /**
     * The partial view description value.
     */
    public static final int PartialViewDescription = 2626385;

    /**
     * The partial view code sequence value.
     */
    public static final int PartialViewCodeSequence = 2626386;

    /**
     * The spatial locations preserved value.
     */
    public static final int SpatialLocationsPreserved = 2626394;

    /**
     * The data frame assignment sequence value.
     */
    public static final int DataFrameAssignmentSequence = 2626561;

    /**
     * The data path assignment value.
     */
    public static final int DataPathAssignment = 2626562;

    /**
     * The bits mapped to color lookup table value.
     */
    public static final int BitsMappedToColorLookupTable = 2626563;

    /**
     * The blending lut1 sequence value.
     */
    public static final int BlendingLUT1Sequence = 2626564;

    /**
     * The blending lut1 transfer function value.
     */
    public static final int BlendingLUT1TransferFunction = 2626565;

    /**
     * The blending weight constant value.
     */
    public static final int BlendingWeightConstant = 2626566;

    /**
     * The blending lookup table descriptor value.
     */
    public static final int BlendingLookupTableDescriptor = 2626567;

    /**
     * The blending lookup table data value.
     */
    public static final int BlendingLookupTableData = 2626568;

    /**
     * The enhanced palette color lookup table sequence value.
     */
    public static final int EnhancedPaletteColorLookupTableSequence = 2626571;

    /**
     * The blending lut2 sequence value.
     */
    public static final int BlendingLUT2Sequence = 2626572;

    /**
     * The blending lut2 transfer function value.
     */
    public static final int BlendingLUT2TransferFunction = 2626573;

    /**
     * The data path id value.
     */
    public static final int DataPathID = 2626574;

    /**
     * The rgblut transfer function value.
     */
    public static final int RGBLUTTransferFunction = 2626575;

    /**
     * The alpha lut transfer function value.
     */
    public static final int AlphaLUTTransferFunction = 2626576;

    /**
     * The icc profile value.
     */
    public static final int ICCProfile = 2629632;

    /**
     * The color space value.
     */
    public static final int ColorSpace = 2629634;

    /**
     * The lossy image compression value.
     */
    public static final int LossyImageCompression = 2629904;

    /**
     * The lossy image compression ratio value.
     */
    public static final int LossyImageCompressionRatio = 2629906;

    /**
     * The lossy image compression method value.
     */
    public static final int LossyImageCompressionMethod = 2629908;

    /**
     * The modality lut sequence value.
     */
    public static final int ModalityLUTSequence = 2633728;

    /**
     * The variable modality lut sequence value.
     */
    public static final int VariableModalityLUTSequence = 2633729;

    /**
     * The lut descriptor value.
     */
    public static final int LUTDescriptor = 2633730;

    /**
     * The lut explanation value.
     */
    public static final int LUTExplanation = 2633731;

    /**
     * The modality lut type value.
     */
    public static final int ModalityLUTType = 2633732;

    /**
     * The lut data value.
     */
    public static final int LUTData = 2633734;

    /**
     * The voilut sequence value.
     */
    public static final int VOILUTSequence = 2633744;

    /**
     * The softcopy voilut sequence value.
     */
    public static final int SoftcopyVOILUTSequence = 2634000;

    /**
     * The image presentation comments value.
     */
    public static final int ImagePresentationComments = 2637824;

    /**
     * The bi plane acquisition sequence value.
     */
    public static final int BiPlaneAcquisitionSequence = 2641920;

    /**
     * The representative frame number value.
     */
    public static final int RepresentativeFrameNumber = 2646032;

    /**
     * The frame numbers of interest value.
     */
    public static final int FrameNumbersOfInterest = 2646048;

    /**
     * The frame of interest description value.
     */
    public static final int FrameOfInterestDescription = 2646050;

    /**
     * The frame of interest type value.
     */
    public static final int FrameOfInterestType = 2646051;

    /**
     * The mask pointers value.
     */
    public static final int MaskPointers = 2646064;

    /**
     * The r wave pointer value.
     */
    public static final int RWavePointer = 2646080;

    /**
     * The mask subtraction sequence value.
     */
    public static final int MaskSubtractionSequence = 2646272;

    /**
     * The mask operation value.
     */
    public static final int MaskOperation = 2646273;

    /**
     * The applicable frame range value.
     */
    public static final int ApplicableFrameRange = 2646274;

    /**
     * The mask frame numbers value.
     */
    public static final int MaskFrameNumbers = 2646288;

    /**
     * The contrast frame averaging value.
     */
    public static final int ContrastFrameAveraging = 2646290;

    /**
     * The mask sub pixel shift value.
     */
    public static final int MaskSubPixelShift = 2646292;

    /**
     * The tid offset value.
     */
    public static final int TIDOffset = 2646304;

    /**
     * The mask operation explanation value.
     */
    public static final int MaskOperationExplanation = 2646416;

    /**
     * The equipment administrator sequence value.
     */
    public static final int EquipmentAdministratorSequence = 2650112;

    /**
     * The number of display subsystems value.
     */
    public static final int NumberOfDisplaySubsystems = 2650113;

    /**
     * The current configuration id value.
     */
    public static final int CurrentConfigurationID = 2650114;

    /**
     * The display subsystem id value.
     */
    public static final int DisplaySubsystemID = 2650115;

    /**
     * The display subsystem name value.
     */
    public static final int DisplaySubsystemName = 2650116;

    /**
     * The display subsystem description value.
     */
    public static final int DisplaySubsystemDescription = 2650117;

    /**
     * The system status value.
     */
    public static final int SystemStatus = 2650118;

    /**
     * The system status comment value.
     */
    public static final int SystemStatusComment = 2650119;

    /**
     * The target luminance characteristics sequence value.
     */
    public static final int TargetLuminanceCharacteristicsSequence = 2650120;

    /**
     * The luminance characteristics id value.
     */
    public static final int LuminanceCharacteristicsID = 2650121;

    /**
     * The display subsystem configuration sequence value.
     */
    public static final int DisplaySubsystemConfigurationSequence = 2650122;

    /**
     * The configuration id value.
     */
    public static final int ConfigurationID = 2650123;

    /**
     * The configuration name value.
     */
    public static final int ConfigurationName = 2650124;

    /**
     * The configuration description value.
     */
    public static final int ConfigurationDescription = 2650125;

    /**
     * The referenced target luminance characteristics id value.
     */
    public static final int ReferencedTargetLuminanceCharacteristicsID = 2650126;

    /**
     * The qa results sequence value.
     */
    public static final int QAResultsSequence = 2650127;

    /**
     * The display subsystem qa results sequence value.
     */
    public static final int DisplaySubsystemQAResultsSequence = 2650128;

    /**
     * The configuration qa results sequence value.
     */
    public static final int ConfigurationQAResultsSequence = 2650129;

    /**
     * The measurement equipment sequence value.
     */
    public static final int MeasurementEquipmentSequence = 2650130;

    /**
     * The measurement functions value.
     */
    public static final int MeasurementFunctions = 2650131;

    /**
     * The measurement equipment type value.
     */
    public static final int MeasurementEquipmentType = 2650132;

    /**
     * The visual evaluation result sequence value.
     */
    public static final int VisualEvaluationResultSequence = 2650133;

    /**
     * The display calibration result sequence value.
     */
    public static final int DisplayCalibrationResultSequence = 2650134;

    /**
     * The ddl value value.
     */
    public static final int DDLValue = 2650135;

    /**
     * The ci exy white point value.
     */
    public static final int CIExyWhitePoint = 2650136;

    /**
     * The display function type value.
     */
    public static final int DisplayFunctionType = 2650137;

    /**
     * The gamma value value.
     */
    public static final int GammaValue = 2650138;

    /**
     * The number of luminance points value.
     */
    public static final int NumberOfLuminancePoints = 2650139;

    /**
     * The luminance response sequence value.
     */
    public static final int LuminanceResponseSequence = 2650140;

    /**
     * The target minimum luminance value.
     */
    public static final int TargetMinimumLuminance = 2650141;

    /**
     * The target maximum luminance value.
     */
    public static final int TargetMaximumLuminance = 2650142;

    /**
     * The luminance value value.
     */
    public static final int LuminanceValue = 2650143;

    /**
     * The luminance response description value.
     */
    public static final int LuminanceResponseDescription = 2650144;

    /**
     * The white point flag value.
     */
    public static final int WhitePointFlag = 2650145;

    /**
     * The display device type code sequence value.
     */
    public static final int DisplayDeviceTypeCodeSequence = 2650146;

    /**
     * The display subsystem sequence value.
     */
    public static final int DisplaySubsystemSequence = 2650147;

    /**
     * The luminance result sequence value.
     */
    public static final int LuminanceResultSequence = 2650148;

    /**
     * The ambient light value source value.
     */
    public static final int AmbientLightValueSource = 2650149;

    /**
     * The measured characteristics value.
     */
    public static final int MeasuredCharacteristics = 2650150;

    /**
     * The luminance uniformity result sequence value.
     */
    public static final int LuminanceUniformityResultSequence = 2650151;

    /**
     * The visual evaluation test sequence value.
     */
    public static final int VisualEvaluationTestSequence = 2650152;

    /**
     * The test result value.
     */
    public static final int TestResult = 2650153;

    /**
     * The test result comment value.
     */
    public static final int TestResultComment = 2650154;

    /**
     * The test image validation value.
     */
    public static final int TestImageValidation = 2650155;

    /**
     * The test pattern code sequence value.
     */
    public static final int TestPatternCodeSequence = 2650156;

    /**
     * The measurement pattern code sequence value.
     */
    public static final int MeasurementPatternCodeSequence = 2650157;

    /**
     * The visual evaluation method code sequence value.
     */
    public static final int VisualEvaluationMethodCodeSequence = 2650158;

    /**
     * The pixel data provider url value.
     */
    public static final int PixelDataProviderURL = 2654176;

    /**
     * The data point rows value.
     */
    public static final int DataPointRows = 2658305;

    /**
     * The data point columns value.
     */
    public static final int DataPointColumns = 2658306;

    /**
     * The signal domain columns value.
     */
    public static final int SignalDomainColumns = 2658307;

    /**
     * The largest monochrome pixel value value.
     */
    public static final int LargestMonochromePixelValue = 2658457;

    /**
     * The data representation value.
     */
    public static final int DataRepresentation = 2658568;

    /**
     * The pixel measures sequence value.
     */
    public static final int PixelMeasuresSequence = 2658576;

    /**
     * The frame voilut sequence value.
     */
    public static final int FrameVOILUTSequence = 2658610;

    /**
     * The pixel value transformation sequence value.
     */
    public static final int PixelValueTransformationSequence = 2658629;

    /**
     * The signal domain rows value.
     */
    public static final int SignalDomainRows = 2658869;

    /**
     * The display filter percentage value.
     */
    public static final int DisplayFilterPercentage = 2659345;

    /**
     * The frame pixel shift sequence value.
     */
    public static final int FramePixelShiftSequence = 2659349;

    /**
     * The subtraction item id value.
     */
    public static final int SubtractionItemID = 2659350;

    /**
     * The pixel intensity relationship lut sequence value.
     */
    public static final int PixelIntensityRelationshipLUTSequence = 2659362;

    /**
     * The frame pixel data properties sequence value.
     */
    public static final int FramePixelDataPropertiesSequence = 2659395;

    /**
     * The geometrical properties value.
     */
    public static final int GeometricalProperties = 2659396;

    /**
     * The geometric maximum distortion value.
     */
    public static final int GeometricMaximumDistortion = 2659397;

    /**
     * The image processing applied value.
     */
    public static final int ImageProcessingApplied = 2659398;

    /**
     * The mask selection mode value.
     */
    public static final int MaskSelectionMode = 2659412;

    /**
     * The lut function value.
     */
    public static final int LUTFunction = 2659444;

    /**
     * The mask visibility percentage value.
     */
    public static final int MaskVisibilityPercentage = 2659448;

    /**
     * The pixel shift sequence value.
     */
    public static final int PixelShiftSequence = 2659585;

    /**
     * The region pixel shift sequence value.
     */
    public static final int RegionPixelShiftSequence = 2659586;

    /**
     * The vertices of the region value.
     */
    public static final int VerticesOfTheRegion = 2659587;

    /**
     * The multi frame presentation sequence value.
     */
    public static final int MultiFramePresentationSequence = 2659589;

    /**
     * The pixel shift frame range value.
     */
    public static final int PixelShiftFrameRange = 2659590;

    /**
     * The lut frame range value.
     */
    public static final int LUTFrameRange = 2659591;

    /**
     * The image to equipment mapping matrix value.
     */
    public static final int ImageToEquipmentMappingMatrix = 2659616;

    /**
     * The equipment coordinate system identification value.
     */
    public static final int EquipmentCoordinateSystemIdentification = 2659639;

    /**
     * The study status id value.
     */
    public static final int StudyStatusID = 3276810;

    /**
     * The study priority id value.
     */
    public static final int StudyPriorityID = 3276812;

    /**
     * The study id issuer value.
     */
    public static final int StudyIDIssuer = 3276818;

    /**
     * The study verified date value.
     */
    public static final int StudyVerifiedDate = 3276850;

    /**
     * The study verified time value.
     */
    public static final int StudyVerifiedTime = 3276851;

    /**
     * The study read date value.
     */
    public static final int StudyReadDate = 3276852;

    /**
     * The study read time value.
     */
    public static final int StudyReadTime = 3276853;

    /**
     * The scheduled study start date value.
     */
    public static final int ScheduledStudyStartDate = 3280896;

    /**
     * The scheduled study start time value.
     */
    public static final int ScheduledStudyStartTime = 3280897;

    /**
     * The scheduled study stop date value.
     */
    public static final int ScheduledStudyStopDate = 3280912;

    /**
     * The scheduled study stop time value.
     */
    public static final int ScheduledStudyStopTime = 3280913;

    /**
     * The scheduled study location value.
     */
    public static final int ScheduledStudyLocation = 3280928;

    /**
     * The scheduled study location ae title value.
     */
    public static final int ScheduledStudyLocationAETitle = 3280929;

    /**
     * The reason for study value.
     */
    public static final int ReasonForStudy = 3280944;

    /**
     * The requesting physician identification sequence value.
     */
    public static final int RequestingPhysicianIdentificationSequence = 3280945;

    /**
     * The requesting physician value.
     */
    public static final int RequestingPhysician = 3280946;

    /**
     * The requesting service value.
     */
    public static final int RequestingService = 3280947;

    /**
     * The requesting service code sequence value.
     */
    public static final int RequestingServiceCodeSequence = 3280948;

    /**
     * The study arrival date value.
     */
    public static final int StudyArrivalDate = 3280960;

    /**
     * The study arrival time value.
     */
    public static final int StudyArrivalTime = 3280961;

    /**
     * The study completion date value.
     */
    public static final int StudyCompletionDate = 3280976;

    /**
     * The study completion time value.
     */
    public static final int StudyCompletionTime = 3280977;

    /**
     * The study component status id value.
     */
    public static final int StudyComponentStatusID = 3280981;

    /**
     * The requested procedure description value.
     */
    public static final int RequestedProcedureDescription = 3280992;

    /**
     * The requested procedure code sequence value.
     */
    public static final int RequestedProcedureCodeSequence = 3280996;

    /**
     * The requested laterality code sequence value.
     */
    public static final int RequestedLateralityCodeSequence = 3280997;

    /**
     * The reason for visit value.
     */
    public static final int ReasonForVisit = 3280998;

    /**
     * The reason for visit code sequence value.
     */
    public static final int ReasonForVisitCodeSequence = 3280999;

    /**
     * The requested contrast agent value.
     */
    public static final int RequestedContrastAgent = 3281008;

    /**
     * The study comments value.
     */
    public static final int StudyComments = 3293184;

    /**
     * The flow identifier sequence value.
     */
    public static final int FlowIdentifierSequence = 3407873;

    /**
     * The flow identifier value.
     */
    public static final int FlowIdentifier = 3407874;

    /**
     * The flow transfer syntax uid value.
     */
    public static final int FlowTransferSyntaxUID = 3407875;

    /**
     * The flow rtp sampling rate value.
     */
    public static final int FlowRTPSamplingRate = 3407876;

    /**
     * The source identifier value.
     */
    public static final int SourceIdentifier = 3407877;

    /**
     * The frame origin timestamp value.
     */
    public static final int FrameOriginTimestamp = 3407879;

    /**
     * The includes imaging subject value.
     */
    public static final int IncludesImagingSubject = 3407880;

    /**
     * The frame usefulness group sequence value.
     */
    public static final int FrameUsefulnessGroupSequence = 3407881;

    /**
     * The real time bulk data flow sequence value.
     */
    public static final int RealTimeBulkDataFlowSequence = 3407882;

    /**
     * The camera position group sequence value.
     */
    public static final int CameraPositionGroupSequence = 3407883;

    /**
     * The includes information value.
     */
    public static final int IncludesInformation = 3407884;

    /**
     * The time of frame group sequence value.
     */
    public static final int TimeOfFrameGroupSequence = 3407885;

    /**
     * The referenced patient alias sequence value.
     */
    public static final int ReferencedPatientAliasSequence = 3670020;

    /**
     * The visit status id value.
     */
    public static final int VisitStatusID = 3670024;

    /**
     * The admission id value.
     */
    public static final int AdmissionID = 3670032;

    /**
     * The issuer of admission id value.
     */
    public static final int IssuerOfAdmissionID = 3670033;

    /**
     * The issuer of admission id sequence value.
     */
    public static final int IssuerOfAdmissionIDSequence = 3670036;

    /**
     * The route of admissions value.
     */
    public static final int RouteOfAdmissions = 3670038;

    /**
     * The scheduled admission date value.
     */
    public static final int ScheduledAdmissionDate = 3670042;

    /**
     * The scheduled admission time value.
     */
    public static final int ScheduledAdmissionTime = 3670043;

    /**
     * The scheduled discharge date value.
     */
    public static final int ScheduledDischargeDate = 3670044;

    /**
     * The scheduled discharge time value.
     */
    public static final int ScheduledDischargeTime = 3670045;

    /**
     * The scheduled patient institution residence value.
     */
    public static final int ScheduledPatientInstitutionResidence = 3670046;

    /**
     * The admitting date value.
     */
    public static final int AdmittingDate = 3670048;

    /**
     * The admitting time value.
     */
    public static final int AdmittingTime = 3670049;

    /**
     * The discharge date value.
     */
    public static final int DischargeDate = 3670064;

    /**
     * The discharge time value.
     */
    public static final int DischargeTime = 3670066;

    /**
     * The discharge diagnosis description value.
     */
    public static final int DischargeDiagnosisDescription = 3670080;

    /**
     * The discharge diagnosis code sequence value.
     */
    public static final int DischargeDiagnosisCodeSequence = 3670084;

    /**
     * The special needs value.
     */
    public static final int SpecialNeeds = 3670096;

    /**
     * The service episode id value.
     */
    public static final int ServiceEpisodeID = 3670112;

    /**
     * The issuer of service episode id value.
     */
    public static final int IssuerOfServiceEpisodeID = 3670113;

    /**
     * The service episode description value.
     */
    public static final int ServiceEpisodeDescription = 3670114;

    /**
     * The issuer of service episode id sequence value.
     */
    public static final int IssuerOfServiceEpisodeIDSequence = 3670116;

    /**
     * The pertinent documents sequence value.
     */
    public static final int PertinentDocumentsSequence = 3670272;

    /**
     * The pertinent resources sequence value.
     */
    public static final int PertinentResourcesSequence = 3670273;

    /**
     * The resource description value.
     */
    public static final int ResourceDescription = 3670274;

    /**
     * The current patient location value.
     */
    public static final int CurrentPatientLocation = 3670784;

    /**
     * The patient institution residence value.
     */
    public static final int PatientInstitutionResidence = 3671040;

    /**
     * The patient state value.
     */
    public static final int PatientState = 3671296;

    /**
     * The patient clinical trial participation sequence value.
     */
    public static final int PatientClinicalTrialParticipationSequence = 3671298;

    /**
     * The visit comments value.
     */
    public static final int VisitComments = 3686400;

    /**
     * The waveform originality value.
     */
    public static final int WaveformOriginality = 3801092;

    /**
     * The number of waveform channels value.
     */
    public static final int NumberOfWaveformChannels = 3801093;

    /**
     * The number of waveform samples value.
     */
    public static final int NumberOfWaveformSamples = 3801104;

    /**
     * The sampling frequency value.
     */
    public static final int SamplingFrequency = 3801114;

    /**
     * The multiplex group label value.
     */
    public static final int MultiplexGroupLabel = 3801120;

    /**
     * The channel definition sequence value.
     */
    public static final int ChannelDefinitionSequence = 3801600;

    /**
     * The waveform channel number value.
     */
    public static final int WaveformChannelNumber = 3801602;

    /**
     * The channel label value.
     */
    public static final int ChannelLabel = 3801603;

    /**
     * The channel status value.
     */
    public static final int ChannelStatus = 3801605;

    /**
     * The channel source sequence value.
     */
    public static final int ChannelSourceSequence = 3801608;

    /**
     * The channel source modifiers sequence value.
     */
    public static final int ChannelSourceModifiersSequence = 3801609;

    /**
     * The source waveform sequence value.
     */
    public static final int SourceWaveformSequence = 3801610;

    /**
     * The channel derivation description value.
     */
    public static final int ChannelDerivationDescription = 3801612;

    /**
     * The channel sensitivity value.
     */
    public static final int ChannelSensitivity = 3801616;

    /**
     * The channel sensitivity units sequence value.
     */
    public static final int ChannelSensitivityUnitsSequence = 3801617;

    /**
     * The channel sensitivity correction factor value.
     */
    public static final int ChannelSensitivityCorrectionFactor = 3801618;

    /**
     * The channel baseline value.
     */
    public static final int ChannelBaseline = 3801619;

    /**
     * The channel time skew value.
     */
    public static final int ChannelTimeSkew = 3801620;

    /**
     * The channel sample skew value.
     */
    public static final int ChannelSampleSkew = 3801621;

    /**
     * The channel offset value.
     */
    public static final int ChannelOffset = 3801624;

    /**
     * The waveform bits stored value.
     */
    public static final int WaveformBitsStored = 3801626;

    /**
     * The filter low frequency value.
     */
    public static final int FilterLowFrequency = 3801632;

    /**
     * The filter high frequency value.
     */
    public static final int FilterHighFrequency = 3801633;

    /**
     * The notch filter frequency value.
     */
    public static final int NotchFilterFrequency = 3801634;

    /**
     * The notch filter bandwidth value.
     */
    public static final int NotchFilterBandwidth = 3801635;

    /**
     * The waveform data display scale value.
     */
    public static final int WaveformDataDisplayScale = 3801648;

    /**
     * The waveform display background cie lab value value.
     */
    public static final int WaveformDisplayBackgroundCIELabValue = 3801649;

    /**
     * The waveform presentation group sequence value.
     */
    public static final int WaveformPresentationGroupSequence = 3801664;

    /**
     * The presentation group number value.
     */
    public static final int PresentationGroupNumber = 3801665;

    /**
     * The channel display sequence value.
     */
    public static final int ChannelDisplaySequence = 3801666;

    /**
     * The channel recommended display cie lab value value.
     */
    public static final int ChannelRecommendedDisplayCIELabValue = 3801668;

    /**
     * The channel position value.
     */
    public static final int ChannelPosition = 3801669;

    /**
     * The display shading flag value.
     */
    public static final int DisplayShadingFlag = 3801670;

    /**
     * The fractional channel display scale value.
     */
    public static final int FractionalChannelDisplayScale = 3801671;

    /**
     * The absolute channel display scale value.
     */
    public static final int AbsoluteChannelDisplayScale = 3801672;

    /**
     * The multiplexed audio channels description code sequence value.
     */
    public static final int MultiplexedAudioChannelsDescriptionCodeSequence = 3801856;

    /**
     * The channel identification code value.
     */
    public static final int ChannelIdentificationCode = 3801857;

    /**
     * The channel mode value.
     */
    public static final int ChannelMode = 3801858;

    /**
     * The multiplex group uid value.
     */
    public static final int MultiplexGroupUID = 3801872;

    /**
     * The powerline frequency value.
     */
    public static final int PowerlineFrequency = 3801873;

    /**
     * The channel impedance sequence value.
     */
    public static final int ChannelImpedanceSequence = 3801874;

    /**
     * The impedance value value.
     */
    public static final int ImpedanceValue = 3801875;

    /**
     * The impedance measurement date time value.
     */
    public static final int ImpedanceMeasurementDateTime = 3801876;

    /**
     * The impedance measurement frequency value.
     */
    public static final int ImpedanceMeasurementFrequency = 3801877;

    /**
     * The impedance measurement current type value.
     */
    public static final int ImpedanceMeasurementCurrentType = 3801878;

    /**
     * The waveform amplifier type value.
     */
    public static final int WaveformAmplifierType = 3801879;

    /**
     * The filter low frequency characteristics sequence value.
     */
    public static final int FilterLowFrequencyCharacteristicsSequence = 3801880;

    /**
     * The filter high frequency characteristics sequence value.
     */
    public static final int FilterHighFrequencyCharacteristicsSequence = 3801881;

    /**
     * The summarized filter lookup table value.
     */
    public static final int SummarizedFilterLookupTable = 3801888;

    /**
     * The notch filter characteristics sequence value.
     */
    public static final int NotchFilterCharacteristicsSequence = 3801889;

    /**
     * The waveform filter type value.
     */
    public static final int WaveformFilterType = 3801890;

    /**
     * The analog filter characteristics sequence value.
     */
    public static final int AnalogFilterCharacteristicsSequence = 3801891;

    /**
     * The analog filter roll off value.
     */
    public static final int AnalogFilterRollOff = 3801892;

    /**
     * The analog filter type value.
     */
    public static final int AnalogFilterType = 3801893;

    /**
     * The digital filter characteristics sequence value.
     */
    public static final int DigitalFilterCharacteristicsSequence = 3801894;

    /**
     * The digital filter order value.
     */
    public static final int DigitalFilterOrder = 3801895;

    /**
     * The digital filter type code sequence value.
     */
    public static final int DigitalFilterTypeCodeSequence = 3801896;

    /**
     * The waveform filter description value.
     */
    public static final int WaveformFilterDescription = 3801897;

    /**
     * The filter lookup table sequence value.
     */
    public static final int FilterLookupTableSequence = 3801898;

    /**
     * The filter lookup table description value.
     */
    public static final int FilterLookupTableDescription = 3801899;

    /**
     * The frequency encoding code sequence value.
     */
    public static final int FrequencyEncodingCodeSequence = 3801900;

    /**
     * The magnitude encoding code sequence value.
     */
    public static final int MagnitudeEncodingCodeSequence = 3801901;

    /**
     * The filter lookup table data value.
     */
    public static final int FilterLookupTableData = 3801902;

    /**
     * The scheduled station ae title value.
     */
    public static final int ScheduledStationAETitle = 4194305;

    /**
     * The scheduled procedure step start date value.
     */
    public static final int ScheduledProcedureStepStartDate = 4194306;

    /**
     * The scheduled procedure step start time value.
     */
    public static final int ScheduledProcedureStepStartTime = 4194307;

    /**
     * The scheduled procedure step end date value.
     */
    public static final int ScheduledProcedureStepEndDate = 4194308;

    /**
     * The scheduled procedure step end time value.
     */
    public static final int ScheduledProcedureStepEndTime = 4194309;

    /**
     * The scheduled performing physician name value.
     */
    public static final int ScheduledPerformingPhysicianName = 4194310;

    /**
     * The scheduled procedure step description value.
     */
    public static final int ScheduledProcedureStepDescription = 4194311;

    /**
     * The scheduled protocol code sequence value.
     */
    public static final int ScheduledProtocolCodeSequence = 4194312;

    /**
     * The scheduled procedure step id value.
     */
    public static final int ScheduledProcedureStepID = 4194313;

    /**
     * The stage code sequence value.
     */
    public static final int StageCodeSequence = 4194314;

    /**
     * The scheduled performing physician identification sequence value.
     */
    public static final int ScheduledPerformingPhysicianIdentificationSequence = 4194315;

    /**
     * The scheduled station name value.
     */
    public static final int ScheduledStationName = 4194320;

    /**
     * The scheduled procedure step location value.
     */
    public static final int ScheduledProcedureStepLocation = 4194321;

    /**
     * The pre medication value.
     */
    public static final int PreMedication = 4194322;

    /**
     * The scheduled procedure step status value.
     */
    public static final int ScheduledProcedureStepStatus = 4194336;

    /**
     * The order placer identifier sequence value.
     */
    public static final int OrderPlacerIdentifierSequence = 4194342;

    /**
     * The order filler identifier sequence value.
     */
    public static final int OrderFillerIdentifierSequence = 4194343;

    /**
     * The local namespace entity id value.
     */
    public static final int LocalNamespaceEntityID = 4194353;

    /**
     * The universal entity id value.
     */
    public static final int UniversalEntityID = 4194354;

    /**
     * The universal entity id type value.
     */
    public static final int UniversalEntityIDType = 4194355;

    /**
     * The identifier type code value.
     */
    public static final int IdentifierTypeCode = 4194357;

    /**
     * The assigning facility sequence value.
     */
    public static final int AssigningFacilitySequence = 4194358;

    /**
     * The assigning jurisdiction code sequence value.
     */
    public static final int AssigningJurisdictionCodeSequence = 4194361;

    /**
     * The assigning agency or department code sequence value.
     */
    public static final int AssigningAgencyOrDepartmentCodeSequence = 4194362;

    /**
     * The scheduled procedure step sequence value.
     */
    public static final int ScheduledProcedureStepSequence = 4194560;

    /**
     * The referenced non image composite sop instance sequence value.
     */
    public static final int ReferencedNonImageCompositeSOPInstanceSequence = 4194848;

    /**
     * The performed station ae title value.
     */
    public static final int PerformedStationAETitle = 4194881;

    /**
     * The performed station name value.
     */
    public static final int PerformedStationName = 4194882;

    /**
     * The performed location value.
     */
    public static final int PerformedLocation = 4194883;

    /**
     * The performed procedure step start date value.
     */
    public static final int PerformedProcedureStepStartDate = 4194884;

    /**
     * The performed procedure step start time value.
     */
    public static final int PerformedProcedureStepStartTime = 4194885;

    /**
     * The performed procedure step end date value.
     */
    public static final int PerformedProcedureStepEndDate = 4194896;

    /**
     * The performed procedure step end time value.
     */
    public static final int PerformedProcedureStepEndTime = 4194897;

    /**
     * The performed procedure step status value.
     */
    public static final int PerformedProcedureStepStatus = 4194898;

    /**
     * The performed procedure step id value.
     */
    public static final int PerformedProcedureStepID = 4194899;

    /**
     * The performed procedure step description value.
     */
    public static final int PerformedProcedureStepDescription = 4194900;

    /**
     * The performed procedure type description value.
     */
    public static final int PerformedProcedureTypeDescription = 4194901;

    /**
     * The performed protocol code sequence value.
     */
    public static final int PerformedProtocolCodeSequence = 4194912;

    /**
     * The performed protocol type value.
     */
    public static final int PerformedProtocolType = 4194913;

    /**
     * The scheduled step attributes sequence value.
     */
    public static final int ScheduledStepAttributesSequence = 4194928;

    /**
     * The request attributes sequence value.
     */
    public static final int RequestAttributesSequence = 4194933;

    /**
     * The comments on the performed procedure step value.
     */
    public static final int CommentsOnThePerformedProcedureStep = 4194944;

    /**
     * The performed procedure step discontinuation reason code sequence value.
     */
    public static final int PerformedProcedureStepDiscontinuationReasonCodeSequence = 4194945;

    /**
     * The quantity sequence value.
     */
    public static final int QuantitySequence = 4194963;

    /**
     * The quantity value.
     */
    public static final int Quantity = 4194964;

    /**
     * The measuring units sequence value.
     */
    public static final int MeasuringUnitsSequence = 4194965;

    /**
     * The billing item sequence value.
     */
    public static final int BillingItemSequence = 4194966;

    /**
     * The total time of fluoroscopy value.
     */
    public static final int TotalTimeOfFluoroscopy = 4195072;

    /**
     * The total number of exposures value.
     */
    public static final int TotalNumberOfExposures = 4195073;

    /**
     * The entrance dose value.
     */
    public static final int EntranceDose = 4195074;

    /**
     * The exposed area value.
     */
    public static final int ExposedArea = 4195075;

    /**
     * The distance source to entrance value.
     */
    public static final int DistanceSourceToEntrance = 4195078;

    /**
     * The distance source to support value.
     */
    public static final int DistanceSourceToSupport = 4195079;

    /**
     * The exposure dose sequence value.
     */
    public static final int ExposureDoseSequence = 4195086;

    /**
     * The comments on radiation dose value.
     */
    public static final int CommentsOnRadiationDose = 4195088;

    /**
     * The x ray output value.
     */
    public static final int XRayOutput = 4195090;

    /**
     * The half value layer value.
     */
    public static final int HalfValueLayer = 4195092;

    /**
     * The organ dose value.
     */
    public static final int OrganDose = 4195094;

    /**
     * The organ exposed value.
     */
    public static final int OrganExposed = 4195096;

    /**
     * The billing procedure step sequence value.
     */
    public static final int BillingProcedureStepSequence = 4195104;

    /**
     * The film consumption sequence value.
     */
    public static final int FilmConsumptionSequence = 4195105;

    /**
     * The billing supplies and devices sequence value.
     */
    public static final int BillingSuppliesAndDevicesSequence = 4195108;

    /**
     * The referenced procedure step sequence value.
     */
    public static final int ReferencedProcedureStepSequence = 4195120;

    /**
     * The performed series sequence value.
     */
    public static final int PerformedSeriesSequence = 4195136;

    /**
     * The comments on the scheduled procedure step value.
     */
    public static final int CommentsOnTheScheduledProcedureStep = 4195328;

    /**
     * The protocol context sequence value.
     */
    public static final int ProtocolContextSequence = 4195392;

    /**
     * The content item modifier sequence value.
     */
    public static final int ContentItemModifierSequence = 4195393;

    /**
     * The scheduled specimen sequence value.
     */
    public static final int ScheduledSpecimenSequence = 4195584;

    /**
     * The specimen accession number value.
     */
    public static final int SpecimenAccessionNumber = 4195594;

    /**
     * The container identifier value.
     */
    public static final int ContainerIdentifier = 4195602;

    /**
     * The issuer of the container identifier sequence value.
     */
    public static final int IssuerOfTheContainerIdentifierSequence = 4195603;

    /**
     * The alternate container identifier sequence value.
     */
    public static final int AlternateContainerIdentifierSequence = 4195605;

    /**
     * The container type code sequence value.
     */
    public static final int ContainerTypeCodeSequence = 4195608;

    /**
     * The container description value.
     */
    public static final int ContainerDescription = 4195610;

    /**
     * The container component sequence value.
     */
    public static final int ContainerComponentSequence = 4195616;

    /**
     * The specimen sequence value.
     */
    public static final int SpecimenSequence = 4195664;

    /**
     * The specimen identifier value.
     */
    public static final int SpecimenIdentifier = 4195665;

    /**
     * The specimen description sequence trial value.
     */
    public static final int SpecimenDescriptionSequenceTrial = 4195666;

    /**
     * The specimen description trial value.
     */
    public static final int SpecimenDescriptionTrial = 4195667;

    /**
     * The specimen uid value.
     */
    public static final int SpecimenUID = 4195668;

    /**
     * The acquisition context sequence value.
     */
    public static final int AcquisitionContextSequence = 4195669;

    /**
     * The acquisition context description value.
     */
    public static final int AcquisitionContextDescription = 4195670;

    /**
     * The specimen type code sequence value.
     */
    public static final int SpecimenTypeCodeSequence = 4195738;

    /**
     * The specimen description sequence value.
     */
    public static final int SpecimenDescriptionSequence = 4195680;

    /**
     * The issuer of the specimen identifier sequence value.
     */
    public static final int IssuerOfTheSpecimenIdentifierSequence = 4195682;

    /**
     * The specimen short description value.
     */
    public static final int SpecimenShortDescription = 4195840;

    /**
     * The specimen detailed description value.
     */
    public static final int SpecimenDetailedDescription = 4195842;

    /**
     * The specimen preparation sequence value.
     */
    public static final int SpecimenPreparationSequence = 4195856;

    /**
     * The specimen preparation step content item sequence value.
     */
    public static final int SpecimenPreparationStepContentItemSequence = 4195858;

    /**
     * The specimen localization content item sequence value.
     */
    public static final int SpecimenLocalizationContentItemSequence = 4195872;

    /**
     * The slide identifier value.
     */
    public static final int SlideIdentifier = 4196090;

    /**
     * The whole slide microscopy image frame type sequence value.
     */
    public static final int WholeSlideMicroscopyImageFrameTypeSequence = 4196112;

    /**
     * The image center point coordinates sequence value.
     */
    public static final int ImageCenterPointCoordinatesSequence = 4196122;

    /**
     * The x offset in slide coordinate system value.
     */
    public static final int XOffsetInSlideCoordinateSystem = 4196138;

    /**
     * The y offset in slide coordinate system value.
     */
    public static final int YOffsetInSlideCoordinateSystem = 4196154;

    /**
     * The z offset in slide coordinate system value.
     */
    public static final int ZOffsetInSlideCoordinateSystem = 4196170;

    /**
     * The pixel spacing sequence value.
     */
    public static final int PixelSpacingSequence = 4196568;

    /**
     * The coordinate system axis code sequence value.
     */
    public static final int CoordinateSystemAxisCodeSequence = 4196570;

    /**
     * The measurement units code sequence value.
     */
    public static final int MeasurementUnitsCodeSequence = 4196586;

    /**
     * The vital stain code sequence trial value.
     */
    public static final int VitalStainCodeSequenceTrial = 4196856;

    /**
     * The requested procedure id value.
     */
    public static final int RequestedProcedureID = 4198401;

    /**
     * The reason for the requested procedure value.
     */
    public static final int ReasonForTheRequestedProcedure = 4198402;

    /**
     * The requested procedure priority value.
     */
    public static final int RequestedProcedurePriority = 4198403;

    /**
     * The patient transport arrangements value.
     */
    public static final int PatientTransportArrangements = 4198404;

    /**
     * The requested procedure location value.
     */
    public static final int RequestedProcedureLocation = 4198405;

    /**
     * The placer order number procedure value.
     */
    public static final int PlacerOrderNumberProcedure = 4198406;

    /**
     * The filler order number procedure value.
     */
    public static final int FillerOrderNumberProcedure = 4198407;

    /**
     * The confidentiality code value.
     */
    public static final int ConfidentialityCode = 4198408;

    /**
     * The reporting priority value.
     */
    public static final int ReportingPriority = 4198409;

    /**
     * The reason for requested procedure code sequence value.
     */
    public static final int ReasonForRequestedProcedureCodeSequence = 4198410;

    /**
     * The names of intended recipients of results value.
     */
    public static final int NamesOfIntendedRecipientsOfResults = 4198416;

    /**
     * The intended recipients of results identification sequence value.
     */
    public static final int IntendedRecipientsOfResultsIdentificationSequence = 4198417;

    /**
     * The reason for performed procedure code sequence value.
     */
    public static final int ReasonForPerformedProcedureCodeSequence = 4198418;

    /**
     * The requested procedure description trial value.
     */
    public static final int RequestedProcedureDescriptionTrial = 4198496;

    /**
     * The person identification code sequence value.
     */
    public static final int PersonIdentificationCodeSequence = 4198657;

    /**
     * The person address value.
     */
    public static final int PersonAddress = 4198658;

    /**
     * The person telephone numbers value.
     */
    public static final int PersonTelephoneNumbers = 4198659;

    /**
     * The person telecom information value.
     */
    public static final int PersonTelecomInformation = 4198660;

    /**
     * The requested procedure comments value.
     */
    public static final int RequestedProcedureComments = 4199424;

    /**
     * The reason for the imaging service request value.
     */
    public static final int ReasonForTheImagingServiceRequest = 4202497;

    /**
     * The issue date of imaging service request value.
     */
    public static final int IssueDateOfImagingServiceRequest = 4202500;

    /**
     * The issue time of imaging service request value.
     */
    public static final int IssueTimeOfImagingServiceRequest = 4202501;

    /**
     * The placer order number imaging service request retired value.
     */
    public static final int PlacerOrderNumberImagingServiceRequestRetired = 4202502;

    /**
     * The filler order number imaging service request retired value.
     */
    public static final int FillerOrderNumberImagingServiceRequestRetired = 4202503;

    /**
     * The order entered by value.
     */
    public static final int OrderEnteredBy = 4202504;

    /**
     * The order enterer location value.
     */
    public static final int OrderEntererLocation = 4202505;

    /**
     * The order callback phone number value.
     */
    public static final int OrderCallbackPhoneNumber = 4202512;

    /**
     * The order callback telecom information value.
     */
    public static final int OrderCallbackTelecomInformation = 4202513;

    /**
     * The placer order number imaging service request value.
     */
    public static final int PlacerOrderNumberImagingServiceRequest = 4202518;

    /**
     * The filler order number imaging service request value.
     */
    public static final int FillerOrderNumberImagingServiceRequest = 4202519;

    /**
     * The imaging service request comments value.
     */
    public static final int ImagingServiceRequestComments = 4203520;

    /**
     * The confidentiality constraint on patient data description value.
     */
    public static final int ConfidentialityConstraintOnPatientDataDescription = 4206593;

    /**
     * The general purpose scheduled procedure step status value.
     */
    public static final int GeneralPurposeScheduledProcedureStepStatus = 4210689;

    /**
     * The general purpose performed procedure step status value.
     */
    public static final int GeneralPurposePerformedProcedureStepStatus = 4210690;

    /**
     * The general purpose scheduled procedure step priority value.
     */
    public static final int GeneralPurposeScheduledProcedureStepPriority = 4210691;

    /**
     * The scheduled processing applications code sequence value.
     */
    public static final int ScheduledProcessingApplicationsCodeSequence = 4210692;

    /**
     * The scheduled procedure step start date time value.
     */
    public static final int ScheduledProcedureStepStartDateTime = 4210693;

    /**
     * The multiple copies flag value.
     */
    public static final int MultipleCopiesFlag = 4210694;

    /**
     * The performed processing applications code sequence value.
     */
    public static final int PerformedProcessingApplicationsCodeSequence = 4210695;

    /**
     * The scheduled procedure step expiration date time value.
     */
    public static final int ScheduledProcedureStepExpirationDateTime = 4210696;

    /**
     * The human performer code sequence value.
     */
    public static final int HumanPerformerCodeSequence = 4210697;

    /**
     * The scheduled procedure step modification date time value.
     */
    public static final int ScheduledProcedureStepModificationDateTime = 4210704;

    /**
     * The expected completion date time value.
     */
    public static final int ExpectedCompletionDateTime = 4210705;

    /**
     * The resulting general purpose performed procedure steps sequence value.
     */
    public static final int ResultingGeneralPurposePerformedProcedureStepsSequence = 4210709;

    /**
     * The referenced general purpose scheduled procedure step sequence value.
     */
    public static final int ReferencedGeneralPurposeScheduledProcedureStepSequence = 4210710;

    /**
     * The scheduled workitem code sequence value.
     */
    public static final int ScheduledWorkitemCodeSequence = 4210712;

    /**
     * The performed workitem code sequence value.
     */
    public static final int PerformedWorkitemCodeSequence = 4210713;

    /**
     * The input availability flag value.
     */
    public static final int InputAvailabilityFlag = 4210720;

    /**
     * The input information sequence value.
     */
    public static final int InputInformationSequence = 4210721;

    /**
     * The relevant information sequence value.
     */
    public static final int RelevantInformationSequence = 4210722;

    /**
     * The referenced general purpose scheduled procedure step transaction uid value.
     */
    public static final int ReferencedGeneralPurposeScheduledProcedureStepTransactionUID = 4210723;

    /**
     * The scheduled station name code sequence value.
     */
    public static final int ScheduledStationNameCodeSequence = 4210725;

    /**
     * The scheduled station class code sequence value.
     */
    public static final int ScheduledStationClassCodeSequence = 4210726;

    /**
     * The scheduled station geographic location code sequence value.
     */
    public static final int ScheduledStationGeographicLocationCodeSequence = 4210727;

    /**
     * The performed station name code sequence value.
     */
    public static final int PerformedStationNameCodeSequence = 4210728;

    /**
     * The performed station class code sequence value.
     */
    public static final int PerformedStationClassCodeSequence = 4210729;

    /**
     * The performed station geographic location code sequence value.
     */
    public static final int PerformedStationGeographicLocationCodeSequence = 4210736;

    /**
     * The requested subsequent workitem code sequence value.
     */
    public static final int RequestedSubsequentWorkitemCodeSequence = 4210737;

    /**
     * The non dicom output code sequence value.
     */
    public static final int NonDICOMOutputCodeSequence = 4210738;

    /**
     * The output information sequence value.
     */
    public static final int OutputInformationSequence = 4210739;

    /**
     * The scheduled human performers sequence value.
     */
    public static final int ScheduledHumanPerformersSequence = 4210740;

    /**
     * The actual human performers sequence value.
     */
    public static final int ActualHumanPerformersSequence = 4210741;

    /**
     * The human performer organization value.
     */
    public static final int HumanPerformerOrganization = 4210742;

    /**
     * The human performer name value.
     */
    public static final int HumanPerformerName = 4210743;

    /**
     * The raw data handling value.
     */
    public static final int RawDataHandling = 4210752;

    /**
     * The input readiness state value.
     */
    public static final int InputReadinessState = 4210753;

    /**
     * The performed procedure step start date time value.
     */
    public static final int PerformedProcedureStepStartDateTime = 4210768;

    /**
     * The performed procedure step end date time value.
     */
    public static final int PerformedProcedureStepEndDateTime = 4210769;

    /**
     * The procedure step cancellation date time value.
     */
    public static final int ProcedureStepCancellationDateTime = 4210770;

    /**
     * The output destination sequence value.
     */
    public static final int OutputDestinationSequence = 4210800;

    /**
     * The dicom storage sequence value.
     */
    public static final int DICOMStorageSequence = 4210801;

    /**
     * The stowrs storage sequence value.
     */
    public static final int STOWRSStorageSequence = 4210802;

    /**
     * The storage url value.
     */
    public static final int StorageURL = 4210803;

    /**
     * The xds storage sequence value.
     */
    public static final int XDSStorageSequence = 4210804;

    /**
     * The entrance dose inm gy value.
     */
    public static final int EntranceDoseInmGy = 4227842;

    /**
     * The entrance dose derivation value.
     */
    public static final int EntranceDoseDerivation = 4227843;

    /**
     * The parametric map frame type sequence value.
     */
    public static final int ParametricMapFrameTypeSequence = 4231314;

    /**
     * The referenced image real world value mapping sequence value.
     */
    public static final int ReferencedImageRealWorldValueMappingSequence = 4231316;

    /**
     * The real world value mapping sequence value.
     */
    public static final int RealWorldValueMappingSequence = 4231318;

    /**
     * The pixel value mapping code sequence value.
     */
    public static final int PixelValueMappingCodeSequence = 4231320;

    /**
     * The lut label value.
     */
    public static final int LUTLabel = 4231696;

    /**
     * The real world value last value mapped value.
     */
    public static final int RealWorldValueLastValueMapped = 4231697;

    /**
     * The real world value lut data value.
     */
    public static final int RealWorldValueLUTData = 4231698;

    /**
     * The double float real world value last value mapped value.
     */
    public static final int DoubleFloatRealWorldValueLastValueMapped = 4231699;

    /**
     * The double float real world value first value mapped value.
     */
    public static final int DoubleFloatRealWorldValueFirstValueMapped = 4231700;

    /**
     * The real world value first value mapped value.
     */
    public static final int RealWorldValueFirstValueMapped = 4231702;

    /**
     * The quantity definition sequence value.
     */
    public static final int QuantityDefinitionSequence = 4231712;

    /**
     * The real world value intercept value.
     */
    public static final int RealWorldValueIntercept = 4231716;

    /**
     * The real world value slope value.
     */
    public static final int RealWorldValueSlope = 4231717;

    /**
     * The findings flag trial value.
     */
    public static final int FindingsFlagTrial = 4235271;

    /**
     * The relationship type value.
     */
    public static final int RelationshipType = 4235280;

    /**
     * The findings sequence trial value.
     */
    public static final int FindingsSequenceTrial = 4235296;

    /**
     * The findings group uid trial value.
     */
    public static final int FindingsGroupUIDTrial = 4235297;

    /**
     * The referenced findings group uid trial value.
     */
    public static final int ReferencedFindingsGroupUIDTrial = 4235298;

    /**
     * The findings group recording date trial value.
     */
    public static final int FindingsGroupRecordingDateTrial = 4235299;

    /**
     * The findings group recording time trial value.
     */
    public static final int FindingsGroupRecordingTimeTrial = 4235300;

    /**
     * The findings source category code sequence trial value.
     */
    public static final int FindingsSourceCategoryCodeSequenceTrial = 4235302;

    /**
     * The verifying organization value.
     */
    public static final int VerifyingOrganization = 4235303;

    /**
     * The documenting organization identifier code sequence trial value.
     */
    public static final int DocumentingOrganizationIdentifierCodeSequenceTrial = 4235304;

    /**
     * The verification date time value.
     */
    public static final int VerificationDateTime = 4235312;

    /**
     * The observation date time value.
     */
    public static final int ObservationDateTime = 4235314;

    /**
     * The observation start date time value.
     */
    public static final int ObservationStartDateTime = 4235315;

    /**
     * The value type value.
     */
    public static final int ValueType = 4235328;

    /**
     * The concept name code sequence value.
     */
    public static final int ConceptNameCodeSequence = 4235331;

    /**
     * The measurement precision description trial value.
     */
    public static final int MeasurementPrecisionDescriptionTrial = 4235335;

    /**
     * The continuity of content value.
     */
    public static final int ContinuityOfContent = 4235344;

    /**
     * The urgency or priority alerts trial value.
     */
    public static final int UrgencyOrPriorityAlertsTrial = 4235351;

    /**
     * The sequencing indicator trial value.
     */
    public static final int SequencingIndicatorTrial = 4235360;

    /**
     * The document identifier code sequence trial value.
     */
    public static final int DocumentIdentifierCodeSequenceTrial = 4235366;

    /**
     * The document author trial value.
     */
    public static final int DocumentAuthorTrial = 4235367;

    /**
     * The document author identifier code sequence trial value.
     */
    public static final int DocumentAuthorIdentifierCodeSequenceTrial = 4235368;

    /**
     * The identifier code sequence trial value.
     */
    public static final int IdentifierCodeSequenceTrial = 4235376;

    /**
     * The verifying observer sequence value.
     */
    public static final int VerifyingObserverSequence = 4235379;

    /**
     * The object binary identifier trial value.
     */
    public static final int ObjectBinaryIdentifierTrial = 4235380;

    /**
     * The verifying observer name value.
     */
    public static final int VerifyingObserverName = 4235381;

    /**
     * The documenting observer identifier code sequence trial value.
     */
    public static final int DocumentingObserverIdentifierCodeSequenceTrial = 4235382;

    /**
     * The author observer sequence value.
     */
    public static final int AuthorObserverSequence = 4235384;

    /**
     * The participant sequence value.
     */
    public static final int ParticipantSequence = 4235386;

    /**
     * The custodial organization sequence value.
     */
    public static final int CustodialOrganizationSequence = 4235388;

    /**
     * The participation type value.
     */
    public static final int ParticipationType = 4235392;

    /**
     * The participation date time value.
     */
    public static final int ParticipationDateTime = 4235394;

    /**
     * The observer type value.
     */
    public static final int ObserverType = 4235396;

    /**
     * The procedure identifier code sequence trial value.
     */
    public static final int ProcedureIdentifierCodeSequenceTrial = 4235397;

    /**
     * The verifying observer identification code sequence value.
     */
    public static final int VerifyingObserverIdentificationCodeSequence = 4235400;

    /**
     * The object directory binary identifier trial value.
     */
    public static final int ObjectDirectoryBinaryIdentifierTrial = 4235401;

    /**
     * The equivalent cda document sequence value.
     */
    public static final int EquivalentCDADocumentSequence = 4235408;

    /**
     * The referenced waveform channels value.
     */
    public static final int ReferencedWaveformChannels = 4235440;

    /**
     * The date of document or verbal transaction trial value.
     */
    public static final int DateOfDocumentOrVerbalTransactionTrial = 4235536;

    /**
     * The time of document creation or verbal transaction trial value.
     */
    public static final int TimeOfDocumentCreationOrVerbalTransactionTrial = 4235538;

    /**
     * The date time value.
     */
    public static final int DateTime = 4235552;

    /**
     * The date value.
     */
    public static final int Date = 4235553;

    /**
     * The time value.
     */
    public static final int Time = 4235554;

    /**
     * The person name value.
     */
    public static final int PersonName = 4235555;

    /**
     * The uid value.
     */
    public static final int UID = 4235556;

    /**
     * The report status id trial value.
     */
    public static final int ReportStatusIDTrial = 4235557;

    /**
     * The temporal range type value.
     */
    public static final int TemporalRangeType = 4235568;

    /**
     * The referenced sample positions value.
     */
    public static final int ReferencedSamplePositions = 4235570;

    /**
     * The referenced frame numbers value.
     */
    public static final int ReferencedFrameNumbers = 4235574;

    /**
     * The referenced time offsets value.
     */
    public static final int ReferencedTimeOffsets = 4235576;

    /**
     * The referenced date time value.
     */
    public static final int ReferencedDateTime = 4235578;

    /**
     * The text value value.
     */
    public static final int TextValue = 4235616;

    /**
     * The floating point value value.
     */
    public static final int FloatingPointValue = 4235617;

    /**
     * The rational numerator value value.
     */
    public static final int RationalNumeratorValue = 4235618;

    /**
     * The rational denominator value value.
     */
    public static final int RationalDenominatorValue = 4235619;

    /**
     * The observation category code sequence trial value.
     */
    public static final int ObservationCategoryCodeSequenceTrial = 4235623;

    /**
     * The concept code sequence value.
     */
    public static final int ConceptCodeSequence = 4235624;

    /**
     * The bibliographic citation trial value.
     */
    public static final int BibliographicCitationTrial = 4235626;

    /**
     * The purpose of reference code sequence value.
     */
    public static final int PurposeOfReferenceCodeSequence = 4235632;

    /**
     * The observation uid value.
     */
    public static final int ObservationUID = 4235633;

    /**
     * The referenced observation uid trial value.
     */
    public static final int ReferencedObservationUIDTrial = 4235634;

    /**
     * The referenced observation class trial value.
     */
    public static final int ReferencedObservationClassTrial = 4235635;

    /**
     * The referenced object observation class trial value.
     */
    public static final int ReferencedObjectObservationClassTrial = 4235636;

    /**
     * The annotation group number value.
     */
    public static final int AnnotationGroupNumber = 4235648;

    /**
     * The observation date trial value.
     */
    public static final int ObservationDateTrial = 4235666;

    /**
     * The observation time trial value.
     */
    public static final int ObservationTimeTrial = 4235667;

    /**
     * The measurement automation trial value.
     */
    public static final int MeasurementAutomationTrial = 4235668;

    /**
     * The modifier code sequence value.
     */
    public static final int ModifierCodeSequence = 4235669;

    /**
     * The identification description trial value.
     */
    public static final int IdentificationDescriptionTrial = 4235812;

    /**
     * The coordinates set geometric type trial value.
     */
    public static final int CoordinatesSetGeometricTypeTrial = 4235920;

    /**
     * The algorithm code sequence trial value.
     */
    public static final int AlgorithmCodeSequenceTrial = 4235926;

    /**
     * The algorithm description trial value.
     */
    public static final int AlgorithmDescriptionTrial = 4235927;

    /**
     * The pixel coordinates set trial value.
     */
    public static final int PixelCoordinatesSetTrial = 4235930;

    /**
     * The measured value sequence value.
     */
    public static final int MeasuredValueSequence = 4236032;

    /**
     * The numeric value qualifier code sequence value.
     */
    public static final int NumericValueQualifierCodeSequence = 4236033;

    /**
     * The current observer trial value.
     */
    public static final int CurrentObserverTrial = 4236039;

    /**
     * The numeric value value.
     */
    public static final int NumericValue = 4236042;

    /**
     * The referenced accession sequence trial value.
     */
    public static final int ReferencedAccessionSequenceTrial = 4236051;

    /**
     * The report status comment trial value.
     */
    public static final int ReportStatusCommentTrial = 4236090;

    /**
     * The procedure context sequence trial value.
     */
    public static final int ProcedureContextSequenceTrial = 4236096;

    /**
     * The verbal source trial value.
     */
    public static final int VerbalSourceTrial = 4236114;

    /**
     * The address trial value.
     */
    public static final int AddressTrial = 4236115;

    /**
     * The telephone number trial value.
     */
    public static final int TelephoneNumberTrial = 4236116;

    /**
     * The verbal source identifier code sequence trial value.
     */
    public static final int VerbalSourceIdentifierCodeSequenceTrial = 4236120;

    /**
     * The predecessor documents sequence value.
     */
    public static final int PredecessorDocumentsSequence = 4236128;

    /**
     * The referenced request sequence value.
     */
    public static final int ReferencedRequestSequence = 4236144;

    /**
     * The performed procedure code sequence value.
     */
    public static final int PerformedProcedureCodeSequence = 4236146;

    /**
     * The current requested procedure evidence sequence value.
     */
    public static final int CurrentRequestedProcedureEvidenceSequence = 4236149;

    /**
     * The report detail sequence trial value.
     */
    public static final int ReportDetailSequenceTrial = 4236160;

    /**
     * The pertinent other evidence sequence value.
     */
    public static final int PertinentOtherEvidenceSequence = 4236165;

    /**
     * The hl7 structured document reference sequence value.
     */
    public static final int HL7StructuredDocumentReferenceSequence = 4236176;

    /**
     * The observation subject uid trial value.
     */
    public static final int ObservationSubjectUIDTrial = 4236290;

    /**
     * The observation subject class trial value.
     */
    public static final int ObservationSubjectClassTrial = 4236291;

    /**
     * The observation subject type code sequence trial value.
     */
    public static final int ObservationSubjectTypeCodeSequenceTrial = 4236292;

    /**
     * The completion flag value.
     */
    public static final int CompletionFlag = 4236433;

    /**
     * The completion flag description value.
     */
    public static final int CompletionFlagDescription = 4236434;

    /**
     * The verification flag value.
     */
    public static final int VerificationFlag = 4236435;

    /**
     * The archive requested value.
     */
    public static final int ArchiveRequested = 4236436;

    /**
     * The preliminary flag value.
     */
    public static final int PreliminaryFlag = 4236438;

    /**
     * The content template sequence value.
     */
    public static final int ContentTemplateSequence = 4236548;

    /**
     * The identical documents sequence value.
     */
    public static final int IdenticalDocumentsSequence = 4236581;

    /**
     * The observation subject context flag trial value.
     */
    public static final int ObservationSubjectContextFlagTrial = 4236800;

    /**
     * The observer context flag trial value.
     */
    public static final int ObserverContextFlagTrial = 4236801;

    /**
     * The procedure context flag trial value.
     */
    public static final int ProcedureContextFlagTrial = 4236803;

    /**
     * The content sequence value.
     */
    public static final int ContentSequence = 4237104;

    /**
     * The relationship sequence trial value.
     */
    public static final int RelationshipSequenceTrial = 4237105;

    /**
     * The relationship type code sequence trial value.
     */
    public static final int RelationshipTypeCodeSequenceTrial = 4237106;

    /**
     * The language code sequence trial value.
     */
    public static final int LanguageCodeSequenceTrial = 4237124;

    /**
     * The tabulated values sequence value.
     */
    public static final int TabulatedValuesSequence = 4237313;

    /**
     * The number of table rows value.
     */
    public static final int NumberOfTableRows = 4237314;

    /**
     * The number of table columns value.
     */
    public static final int NumberOfTableColumns = 4237315;

    /**
     * The table row number value.
     */
    public static final int TableRowNumber = 4237316;

    /**
     * The table column number value.
     */
    public static final int TableColumnNumber = 4237317;

    /**
     * The table row definition sequence value.
     */
    public static final int TableRowDefinitionSequence = 4237318;

    /**
     * The table column definition sequence value.
     */
    public static final int TableColumnDefinitionSequence = 4237319;

    /**
     * The cell values sequence value.
     */
    public static final int CellValuesSequence = 4237320;

    /**
     * The uniform resource locator trial value.
     */
    public static final int UniformResourceLocatorTrial = 4237714;

    /**
     * The waveform annotation sequence value.
     */
    public static final int WaveformAnnotationSequence = 4239392;

    /**
     * The template identifier value.
     */
    public static final int TemplateIdentifier = 4250368;

    /**
     * The template version value.
     */
    public static final int TemplateVersion = 4250374;

    /**
     * The template local version value.
     */
    public static final int TemplateLocalVersion = 4250375;

    /**
     * The template extension flag value.
     */
    public static final int TemplateExtensionFlag = 4250379;

    /**
     * The template extension organization uid value.
     */
    public static final int TemplateExtensionOrganizationUID = 4250380;

    /**
     * The template extension creator uid value.
     */
    public static final int TemplateExtensionCreatorUID = 4250381;

    /**
     * The referenced content item identifier value.
     */
    public static final int ReferencedContentItemIdentifier = 4250483;

    /**
     * The hl7 instance identifier value.
     */
    public static final int HL7InstanceIdentifier = 4251649;

    /**
     * The hl7 document effective time value.
     */
    public static final int HL7DocumentEffectiveTime = 4251652;

    /**
     * The hl7 document type code sequence value.
     */
    public static final int HL7DocumentTypeCodeSequence = 4251654;

    /**
     * The document class code sequence value.
     */
    public static final int DocumentClassCodeSequence = 4251656;

    /**
     * The retrieve uri value.
     */
    public static final int RetrieveURI = 4251664;

    /**
     * The retrieve location uid value.
     */
    public static final int RetrieveLocationUID = 4251665;

    /**
     * The type of instances value.
     */
    public static final int TypeOfInstances = 4251680;

    /**
     * The dicom retrieval sequence value.
     */
    public static final int DICOMRetrievalSequence = 4251681;

    /**
     * The dicom media retrieval sequence value.
     */
    public static final int DICOMMediaRetrievalSequence = 4251682;

    /**
     * The wado retrieval sequence value.
     */
    public static final int WADORetrievalSequence = 4251683;

    /**
     * The xds retrieval sequence value.
     */
    public static final int XDSRetrievalSequence = 4251684;

    /**
     * The wadors retrieval sequence value.
     */
    public static final int WADORSRetrievalSequence = 4251685;

    /**
     * The repository unique id value.
     */
    public static final int RepositoryUniqueID = 4251696;

    /**
     * The home community id value.
     */
    public static final int HomeCommunityID = 4251697;

    /**
     * The document title value.
     */
    public static final int DocumentTitle = 4325392;

    /**
     * The encapsulated document value.
     */
    public static final int EncapsulatedDocument = 4325393;

    /**
     * The mime type of encapsulated document value.
     */
    public static final int MIMETypeOfEncapsulatedDocument = 4325394;

    /**
     * The source instance sequence value.
     */
    public static final int SourceInstanceSequence = 4325395;

    /**
     * The list of mime types value.
     */
    public static final int ListOfMIMETypes = 4325396;

    /**
     * The encapsulated document length value.
     */
    public static final int EncapsulatedDocumentLength = 4325397;

    /**
     * The product package identifier value.
     */
    public static final int ProductPackageIdentifier = 4456449;

    /**
     * The substance administration approval value.
     */
    public static final int SubstanceAdministrationApproval = 4456450;

    /**
     * The approval status further description value.
     */
    public static final int ApprovalStatusFurtherDescription = 4456451;

    /**
     * The approval status date time value.
     */
    public static final int ApprovalStatusDateTime = 4456452;

    /**
     * The product type code sequence value.
     */
    public static final int ProductTypeCodeSequence = 4456455;

    /**
     * The product name value.
     */
    public static final int ProductName = 4456456;

    /**
     * The product description value.
     */
    public static final int ProductDescription = 4456457;

    /**
     * The product lot identifier value.
     */
    public static final int ProductLotIdentifier = 4456458;

    /**
     * The product expiration date time value.
     */
    public static final int ProductExpirationDateTime = 4456459;

    /**
     * The substance administration date time value.
     */
    public static final int SubstanceAdministrationDateTime = 4456464;

    /**
     * The substance administration notes value.
     */
    public static final int SubstanceAdministrationNotes = 4456465;

    /**
     * The substance administration device id value.
     */
    public static final int SubstanceAdministrationDeviceID = 4456466;

    /**
     * The product parameter sequence value.
     */
    public static final int ProductParameterSequence = 4456467;

    /**
     * The substance administration parameter sequence value.
     */
    public static final int SubstanceAdministrationParameterSequence = 4456473;

    /**
     * The approval sequence value.
     */
    public static final int ApprovalSequence = 4456704;

    /**
     * The assertion code sequence value.
     */
    public static final int AssertionCodeSequence = 4456705;

    /**
     * The assertion uid value.
     */
    public static final int AssertionUID = 4456706;

    /**
     * The asserter identification sequence value.
     */
    public static final int AsserterIdentificationSequence = 4456707;

    /**
     * The assertion date time value.
     */
    public static final int AssertionDateTime = 4456708;

    /**
     * The assertion expiration date time value.
     */
    public static final int AssertionExpirationDateTime = 4456709;

    /**
     * The assertion comments value.
     */
    public static final int AssertionComments = 4456710;

    /**
     * The related assertion sequence value.
     */
    public static final int RelatedAssertionSequence = 4456711;

    /**
     * The referenced assertion uid value.
     */
    public static final int ReferencedAssertionUID = 4456712;

    /**
     * The approval subject sequence value.
     */
    public static final int ApprovalSubjectSequence = 4456713;

    /**
     * The organizational role code sequence value.
     */
    public static final int OrganizationalRoleCodeSequence = 4456714;

    /**
     * The lens description value.
     */
    public static final int LensDescription = 4587538;

    /**
     * The right lens sequence value.
     */
    public static final int RightLensSequence = 4587540;

    /**
     * The left lens sequence value.
     */
    public static final int LeftLensSequence = 4587541;

    /**
     * The unspecified laterality lens sequence value.
     */
    public static final int UnspecifiedLateralityLensSequence = 4587542;

    /**
     * The cylinder sequence value.
     */
    public static final int CylinderSequence = 4587544;

    /**
     * The prism sequence value.
     */
    public static final int PrismSequence = 4587560;

    /**
     * The horizontal prism power value.
     */
    public static final int HorizontalPrismPower = 4587568;

    /**
     * The horizontal prism base value.
     */
    public static final int HorizontalPrismBase = 4587570;

    /**
     * The vertical prism power value.
     */
    public static final int VerticalPrismPower = 4587572;

    /**
     * The vertical prism base value.
     */
    public static final int VerticalPrismBase = 4587574;

    /**
     * The lens segment type value.
     */
    public static final int LensSegmentType = 4587576;

    /**
     * The optical transmittance value.
     */
    public static final int OpticalTransmittance = 4587584;

    /**
     * The channel width value.
     */
    public static final int ChannelWidth = 4587586;

    /**
     * The pupil size value.
     */
    public static final int PupilSize = 4587588;

    /**
     * The corneal size value.
     */
    public static final int CornealSize = 4587590;

    /**
     * The corneal size sequence value.
     */
    public static final int CornealSizeSequence = 4587591;

    /**
     * The autorefraction right eye sequence value.
     */
    public static final int AutorefractionRightEyeSequence = 4587600;

    /**
     * The autorefraction left eye sequence value.
     */
    public static final int AutorefractionLeftEyeSequence = 4587602;

    /**
     * The distance pupillary distance value.
     */
    public static final int DistancePupillaryDistance = 4587616;

    /**
     * The near pupillary distance value.
     */
    public static final int NearPupillaryDistance = 4587618;

    /**
     * The intermediate pupillary distance value.
     */
    public static final int IntermediatePupillaryDistance = 4587619;

    /**
     * The other pupillary distance value.
     */
    public static final int OtherPupillaryDistance = 4587620;

    /**
     * The keratometry right eye sequence value.
     */
    public static final int KeratometryRightEyeSequence = 4587632;

    /**
     * The keratometry left eye sequence value.
     */
    public static final int KeratometryLeftEyeSequence = 4587633;

    /**
     * The steep keratometric axis sequence value.
     */
    public static final int SteepKeratometricAxisSequence = 4587636;

    /**
     * The radius of curvature value.
     */
    public static final int RadiusOfCurvature = 4587637;

    /**
     * The keratometric power value.
     */
    public static final int KeratometricPower = 4587638;

    /**
     * The keratometric axis value.
     */
    public static final int KeratometricAxis = 4587639;

    /**
     * The flat keratometric axis sequence value.
     */
    public static final int FlatKeratometricAxisSequence = 4587648;

    /**
     * The background color value.
     */
    public static final int BackgroundColor = 4587666;

    /**
     * The optotype value.
     */
    public static final int Optotype = 4587668;

    /**
     * The optotype presentation value.
     */
    public static final int OptotypePresentation = 4587669;

    /**
     * The subjective refraction right eye sequence value.
     */
    public static final int SubjectiveRefractionRightEyeSequence = 4587671;

    /**
     * The subjective refraction left eye sequence value.
     */
    public static final int SubjectiveRefractionLeftEyeSequence = 4587672;

    /**
     * The add near sequence value.
     */
    public static final int AddNearSequence = 4587776;

    /**
     * The add intermediate sequence value.
     */
    public static final int AddIntermediateSequence = 4587777;

    /**
     * The add other sequence value.
     */
    public static final int AddOtherSequence = 4587778;

    /**
     * The add power value.
     */
    public static final int AddPower = 4587780;

    /**
     * The viewing distance value.
     */
    public static final int ViewingDistance = 4587782;

    /**
     * The cornea measurements sequence value.
     */
    public static final int CorneaMeasurementsSequence = 4587792;

    /**
     * The source of cornea measurement data code sequence value.
     */
    public static final int SourceOfCorneaMeasurementDataCodeSequence = 4587793;

    /**
     * The steep corneal axis sequence value.
     */
    public static final int SteepCornealAxisSequence = 4587794;

    /**
     * The flat corneal axis sequence value.
     */
    public static final int FlatCornealAxisSequence = 4587795;

    /**
     * The corneal power value.
     */
    public static final int CornealPower = 4587796;

    /**
     * The corneal axis value.
     */
    public static final int CornealAxis = 4587797;

    /**
     * The cornea measurement method code sequence value.
     */
    public static final int CorneaMeasurementMethodCodeSequence = 4587798;

    /**
     * The refractive index of cornea value.
     */
    public static final int RefractiveIndexOfCornea = 4587799;

    /**
     * The refractive index of aqueous humor value.
     */
    public static final int RefractiveIndexOfAqueousHumor = 4587800;

    /**
     * The visual acuity type code sequence value.
     */
    public static final int VisualAcuityTypeCodeSequence = 4587809;

    /**
     * The visual acuity right eye sequence value.
     */
    public static final int VisualAcuityRightEyeSequence = 4587810;

    /**
     * The visual acuity left eye sequence value.
     */
    public static final int VisualAcuityLeftEyeSequence = 4587811;

    /**
     * The visual acuity both eyes open sequence value.
     */
    public static final int VisualAcuityBothEyesOpenSequence = 4587812;

    /**
     * The viewing distance type value.
     */
    public static final int ViewingDistanceType = 4587813;

    /**
     * The visual acuity modifiers value.
     */
    public static final int VisualAcuityModifiers = 4587829;

    /**
     * The decimal visual acuity value.
     */
    public static final int DecimalVisualAcuity = 4587831;

    /**
     * The optotype detailed definition value.
     */
    public static final int OptotypeDetailedDefinition = 4587833;

    /**
     * The referenced refractive measurements sequence value.
     */
    public static final int ReferencedRefractiveMeasurementsSequence = 4587845;

    /**
     * The sphere power value.
     */
    public static final int SpherePower = 4587846;

    /**
     * The cylinder power value.
     */
    public static final int CylinderPower = 4587847;

    /**
     * The corneal topography surface value.
     */
    public static final int CornealTopographySurface = 4588033;

    /**
     * The corneal vertex location value.
     */
    public static final int CornealVertexLocation = 4588034;

    /**
     * The pupil centroid x coordinate value.
     */
    public static final int PupilCentroidXCoordinate = 4588035;

    /**
     * The pupil centroid y coordinate value.
     */
    public static final int PupilCentroidYCoordinate = 4588036;

    /**
     * The equivalent pupil radius value.
     */
    public static final int EquivalentPupilRadius = 4588037;

    /**
     * The corneal topography map type code sequence value.
     */
    public static final int CornealTopographyMapTypeCodeSequence = 4588039;

    /**
     * The vertices of the outline of pupil value.
     */
    public static final int VerticesOfTheOutlineOfPupil = 4588040;

    /**
     * The corneal topography mapping normals sequence value.
     */
    public static final int CornealTopographyMappingNormalsSequence = 4588048;

    /**
     * The maximum corneal curvature sequence value.
     */
    public static final int MaximumCornealCurvatureSequence = 4588049;

    /**
     * The maximum corneal curvature value.
     */
    public static final int MaximumCornealCurvature = 4588050;

    /**
     * The maximum corneal curvature location value.
     */
    public static final int MaximumCornealCurvatureLocation = 4588051;

    /**
     * The minimum keratometric sequence value.
     */
    public static final int MinimumKeratometricSequence = 4588053;

    /**
     * The simulated keratometric cylinder sequence value.
     */
    public static final int SimulatedKeratometricCylinderSequence = 4588056;

    /**
     * The average corneal power value.
     */
    public static final int AverageCornealPower = 4588064;

    /**
     * The corneal is value value.
     */
    public static final int CornealISValue = 4588068;

    /**
     * The analyzed area value.
     */
    public static final int AnalyzedArea = 4588071;

    /**
     * The surface regularity index value.
     */
    public static final int SurfaceRegularityIndex = 4588080;

    /**
     * The surface asymmetry index value.
     */
    public static final int SurfaceAsymmetryIndex = 4588082;

    /**
     * The corneal eccentricity index value.
     */
    public static final int CornealEccentricityIndex = 4588084;

    /**
     * The keratoconus prediction index value.
     */
    public static final int KeratoconusPredictionIndex = 4588086;

    /**
     * The decimal potential visual acuity value.
     */
    public static final int DecimalPotentialVisualAcuity = 4588088;

    /**
     * The corneal topography map quality evaluation value.
     */
    public static final int CornealTopographyMapQualityEvaluation = 4588098;

    /**
     * The source image corneal processed data sequence value.
     */
    public static final int SourceImageCornealProcessedDataSequence = 4588100;

    /**
     * The corneal point location value.
     */
    public static final int CornealPointLocation = 4588103;

    /**
     * The corneal point estimated value.
     */
    public static final int CornealPointEstimated = 4588104;

    /**
     * The axial power value.
     */
    public static final int AxialPower = 4588105;

    /**
     * The tangential power value.
     */
    public static final int TangentialPower = 4588112;

    /**
     * The refractive power value.
     */
    public static final int RefractivePower = 4588113;

    /**
     * The relative elevation value.
     */
    public static final int RelativeElevation = 4588114;

    /**
     * The corneal wavefront value.
     */
    public static final int CornealWavefront = 4588115;

    /**
     * The imaged volume width value.
     */
    public static final int ImagedVolumeWidth = 4718593;

    /**
     * The imaged volume height value.
     */
    public static final int ImagedVolumeHeight = 4718594;

    /**
     * The imaged volume depth value.
     */
    public static final int ImagedVolumeDepth = 4718595;

    /**
     * The total pixel matrix columns value.
     */
    public static final int TotalPixelMatrixColumns = 4718598;

    /**
     * The total pixel matrix rows value.
     */
    public static final int TotalPixelMatrixRows = 4718599;

    /**
     * The total pixel matrix origin sequence value.
     */
    public static final int TotalPixelMatrixOriginSequence = 4718600;

    /**
     * The specimen label in image value.
     */
    public static final int SpecimenLabelInImage = 4718608;

    /**
     * The focus method value.
     */
    public static final int FocusMethod = 4718609;

    /**
     * The extended depth of field value.
     */
    public static final int ExtendedDepthOfField = 4718610;

    /**
     * The number of focal planes value.
     */
    public static final int NumberOfFocalPlanes = 4718611;

    /**
     * The distance between focal planes value.
     */
    public static final int DistanceBetweenFocalPlanes = 4718612;

    /**
     * The recommended absent pixel cie lab value value.
     */
    public static final int RecommendedAbsentPixelCIELabValue = 4718613;

    /**
     * The illuminator type code sequence value.
     */
    public static final int IlluminatorTypeCodeSequence = 4718848;

    /**
     * The image orientation slide value.
     */
    public static final int ImageOrientationSlide = 4718850;

    /**
     * The optical path sequence value.
     */
    public static final int OpticalPathSequence = 4718853;

    /**
     * The optical path identifier value.
     */
    public static final int OpticalPathIdentifier = 4718854;

    /**
     * The optical path description value.
     */
    public static final int OpticalPathDescription = 4718855;

    /**
     * The illumination color code sequence value.
     */
    public static final int IlluminationColorCodeSequence = 4718856;

    /**
     * The specimen reference sequence value.
     */
    public static final int SpecimenReferenceSequence = 4718864;

    /**
     * The condenser lens power value.
     */
    public static final int CondenserLensPower = 4718865;

    /**
     * The objective lens power value.
     */
    public static final int ObjectiveLensPower = 4718866;

    /**
     * The objective lens numerical aperture value.
     */
    public static final int ObjectiveLensNumericalAperture = 4718867;

    /**
     * The confocal mode value.
     */
    public static final int ConfocalMode = 4718868;

    /**
     * The tissue location value.
     */
    public static final int TissueLocation = 4718869;

    /**
     * The confocal microscopy image frame type sequence value.
     */
    public static final int ConfocalMicroscopyImageFrameTypeSequence = 4718870;

    /**
     * The image acquisition depth value.
     */
    public static final int ImageAcquisitionDepth = 4718871;

    /**
     * The palette color lookup table sequence value.
     */
    public static final int PaletteColorLookupTableSequence = 4718880;

    /**
     * The referenced image navigation sequence value.
     */
    public static final int ReferencedImageNavigationSequence = 4719104;

    /**
     * The top left hand corner of localizer area value.
     */
    public static final int TopLeftHandCornerOfLocalizerArea = 4719105;

    /**
     * The bottom right hand corner of localizer area value.
     */
    public static final int BottomRightHandCornerOfLocalizerArea = 4719106;

    /**
     * The optical path identification sequence value.
     */
    public static final int OpticalPathIdentificationSequence = 4719111;

    /**
     * The plane position slide sequence value.
     */
    public static final int PlanePositionSlideSequence = 4719130;

    /**
     * The column position in total image pixel matrix value.
     */
    public static final int ColumnPositionInTotalImagePixelMatrix = 4719134;

    /**
     * The row position in total image pixel matrix value.
     */
    public static final int RowPositionInTotalImagePixelMatrix = 4719135;

    /**
     * The pixel origin interpretation value.
     */
    public static final int PixelOriginInterpretation = 4719361;

    /**
     * The number of optical paths value.
     */
    public static final int NumberOfOpticalPaths = 4719362;

    /**
     * The total pixel matrix focal planes value.
     */
    public static final int TotalPixelMatrixFocalPlanes = 4719363;

    /**
     * The calibration image value.
     */
    public static final int CalibrationImage = 5242884;

    /**
     * The device sequence value.
     */
    public static final int DeviceSequence = 5242896;

    /**
     * The container component type code sequence value.
     */
    public static final int ContainerComponentTypeCodeSequence = 5242898;

    /**
     * The container component thickness value.
     */
    public static final int ContainerComponentThickness = 5242899;

    /**
     * The device length value.
     */
    public static final int DeviceLength = 5242900;

    /**
     * The container component width value.
     */
    public static final int ContainerComponentWidth = 5242901;

    /**
     * The device diameter value.
     */
    public static final int DeviceDiameter = 5242902;

    /**
     * The device diameter units value.
     */
    public static final int DeviceDiameterUnits = 5242903;

    /**
     * The device volume value.
     */
    public static final int DeviceVolume = 5242904;

    /**
     * The inter marker distance value.
     */
    public static final int InterMarkerDistance = 5242905;

    /**
     * The container component material value.
     */
    public static final int ContainerComponentMaterial = 5242906;

    /**
     * The container component id value.
     */
    public static final int ContainerComponentID = 5242907;

    /**
     * The container component length value.
     */
    public static final int ContainerComponentLength = 5242908;

    /**
     * The container component diameter value.
     */
    public static final int ContainerComponentDiameter = 5242909;

    /**
     * The container component description value.
     */
    public static final int ContainerComponentDescription = 5242910;

    /**
     * The device description value.
     */
    public static final int DeviceDescription = 5242912;

    /**
     * The long device description value.
     */
    public static final int LongDeviceDescription = 5242913;

    /**
     * The contrast bolus ingredient percent by volume value.
     */
    public static final int ContrastBolusIngredientPercentByVolume = 5373953;

    /**
     * The oct focal distance value.
     */
    public static final int OCTFocalDistance = 5373954;

    /**
     * The beam spot size value.
     */
    public static final int BeamSpotSize = 5373955;

    /**
     * The effective refractive index value.
     */
    public static final int EffectiveRefractiveIndex = 5373956;

    /**
     * The oct acquisition domain value.
     */
    public static final int OCTAcquisitionDomain = 5373958;

    /**
     * The oct optical center wavelength value.
     */
    public static final int OCTOpticalCenterWavelength = 5373959;

    /**
     * The axial resolution value.
     */
    public static final int AxialResolution = 5373960;

    /**
     * The ranging depth value.
     */
    public static final int RangingDepth = 5373961;

    /**
     * The a line rate value.
     */
    public static final int ALineRate = 5373969;

    /**
     * The a lines per frame value.
     */
    public static final int ALinesPerFrame = 5373970;

    /**
     * The catheter rotational rate value.
     */
    public static final int CatheterRotationalRate = 5373971;

    /**
     * The a line pixel spacing value.
     */
    public static final int ALinePixelSpacing = 5373972;

    /**
     * The mode of percutaneous access sequence value.
     */
    public static final int ModeOfPercutaneousAccessSequence = 5373974;

    /**
     * The intravascular oct frame type sequence value.
     */
    public static final int IntravascularOCTFrameTypeSequence = 5373989;

    /**
     * The octz offset applied value.
     */
    public static final int OCTZOffsetApplied = 5373990;

    /**
     * The intravascular frame content sequence value.
     */
    public static final int IntravascularFrameContentSequence = 5373991;

    /**
     * The intravascular longitudinal distance value.
     */
    public static final int IntravascularLongitudinalDistance = 5373992;

    /**
     * The intravascular oct frame content sequence value.
     */
    public static final int IntravascularOCTFrameContentSequence = 5373993;

    /**
     * The octz offset correction value.
     */
    public static final int OCTZOffsetCorrection = 5374000;

    /**
     * The catheter direction of rotation value.
     */
    public static final int CatheterDirectionOfRotation = 5374001;

    /**
     * The seam line location value.
     */
    public static final int SeamLineLocation = 5374003;

    /**
     * The first a line location value.
     */
    public static final int FirstALineLocation = 5374004;

    /**
     * The seam line index value.
     */
    public static final int SeamLineIndex = 5374006;

    /**
     * The number of padded a lines value.
     */
    public static final int NumberOfPaddedALines = 5374008;

    /**
     * The interpolation type value.
     */
    public static final int InterpolationType = 5374009;

    /**
     * The refractive index applied value.
     */
    public static final int RefractiveIndexApplied = 5374010;

    /**
     * The energy window vector value.
     */
    public static final int EnergyWindowVector = 5505040;

    /**
     * The number of energy windows value.
     */
    public static final int NumberOfEnergyWindows = 5505041;

    /**
     * The energy window information sequence value.
     */
    public static final int EnergyWindowInformationSequence = 5505042;

    /**
     * The energy window range sequence value.
     */
    public static final int EnergyWindowRangeSequence = 5505043;

    /**
     * The energy window lower limit value.
     */
    public static final int EnergyWindowLowerLimit = 5505044;

    /**
     * The energy window upper limit value.
     */
    public static final int EnergyWindowUpperLimit = 5505045;

    /**
     * The radiopharmaceutical information sequence value.
     */
    public static final int RadiopharmaceuticalInformationSequence = 5505046;

    /**
     * The residual syringe counts value.
     */
    public static final int ResidualSyringeCounts = 5505047;

    /**
     * The energy window name value.
     */
    public static final int EnergyWindowName = 5505048;

    /**
     * The detector vector value.
     */
    public static final int DetectorVector = 5505056;

    /**
     * The number of detectors value.
     */
    public static final int NumberOfDetectors = 5505057;

    /**
     * The detector information sequence value.
     */
    public static final int DetectorInformationSequence = 5505058;

    /**
     * The phase vector value.
     */
    public static final int PhaseVector = 5505072;

    /**
     * The number of phases value.
     */
    public static final int NumberOfPhases = 5505073;

    /**
     * The phase information sequence value.
     */
    public static final int PhaseInformationSequence = 5505074;

    /**
     * The number of frames in phase value.
     */
    public static final int NumberOfFramesInPhase = 5505075;

    /**
     * The phase delay value.
     */
    public static final int PhaseDelay = 5505078;

    /**
     * The pause between frames value.
     */
    public static final int PauseBetweenFrames = 5505080;

    /**
     * The phase description value.
     */
    public static final int PhaseDescription = 5505081;

    /**
     * The rotation vector value.
     */
    public static final int RotationVector = 5505104;

    /**
     * The number of rotations value.
     */
    public static final int NumberOfRotations = 5505105;

    /**
     * The rotation information sequence value.
     */
    public static final int RotationInformationSequence = 5505106;

    /**
     * The number of frames in rotation value.
     */
    public static final int NumberOfFramesInRotation = 5505107;

    /**
     * The rr interval vector value.
     */
    public static final int RRIntervalVector = 5505120;

    /**
     * The number of rr intervals value.
     */
    public static final int NumberOfRRIntervals = 5505121;

    /**
     * The gated information sequence value.
     */
    public static final int GatedInformationSequence = 5505122;

    /**
     * The data information sequence value.
     */
    public static final int DataInformationSequence = 5505123;

    /**
     * The time slot vector value.
     */
    public static final int TimeSlotVector = 5505136;

    /**
     * The number of time slots value.
     */
    public static final int NumberOfTimeSlots = 5505137;

    /**
     * The time slot information sequence value.
     */
    public static final int TimeSlotInformationSequence = 5505138;

    /**
     * The time slot time value.
     */
    public static final int TimeSlotTime = 5505139;

    /**
     * The slice vector value.
     */
    public static final int SliceVector = 5505152;

    /**
     * The number of slices value.
     */
    public static final int NumberOfSlices = 5505153;

    /**
     * The angular view vector value.
     */
    public static final int AngularViewVector = 5505168;

    /**
     * The time slice vector value.
     */
    public static final int TimeSliceVector = 5505280;

    /**
     * The number of time slices value.
     */
    public static final int NumberOfTimeSlices = 5505281;

    /**
     * The start angle value.
     */
    public static final int StartAngle = 5505536;

    /**
     * The type of detector motion value.
     */
    public static final int TypeOfDetectorMotion = 5505538;

    /**
     * The trigger vector value.
     */
    public static final int TriggerVector = 5505552;

    /**
     * The number of triggers in phase value.
     */
    public static final int NumberOfTriggersInPhase = 5505553;

    /**
     * The view code sequence value.
     */
    public static final int ViewCodeSequence = 5505568;

    /**
     * The view modifier code sequence value.
     */
    public static final int ViewModifierCodeSequence = 5505570;

    /**
     * The radionuclide code sequence value.
     */
    public static final int RadionuclideCodeSequence = 5505792;

    /**
     * The administration route code sequence value.
     */
    public static final int AdministrationRouteCodeSequence = 5505794;

    /**
     * The radiopharmaceutical code sequence value.
     */
    public static final int RadiopharmaceuticalCodeSequence = 5505796;

    /**
     * The calibration data sequence value.
     */
    public static final int CalibrationDataSequence = 5505798;

    /**
     * The energy window number value.
     */
    public static final int EnergyWindowNumber = 5505800;

    /**
     * The image id value.
     */
    public static final int ImageID = 5506048;

    /**
     * The patient orientation code sequence value.
     */
    public static final int PatientOrientationCodeSequence = 5506064;

    /**
     * The patient orientation modifier code sequence value.
     */
    public static final int PatientOrientationModifierCodeSequence = 5506066;

    /**
     * The patient gantry relationship code sequence value.
     */
    public static final int PatientGantryRelationshipCodeSequence = 5506068;

    /**
     * The slice progression direction value.
     */
    public static final int SliceProgressionDirection = 5506304;

    /**
     * The scan progression direction value.
     */
    public static final int ScanProgressionDirection = 5506305;

    /**
     * The series type value.
     */
    public static final int SeriesType = 5509120;

    /**
     * The units value.
     */
    public static final int Units = 5509121;

    /**
     * The counts source value.
     */
    public static final int CountsSource = 5509122;

    /**
     * The reprojection method value.
     */
    public static final int ReprojectionMethod = 5509124;

    /**
     * The suv type value.
     */
    public static final int SUVType = 5509126;

    /**
     * The randoms correction method value.
     */
    public static final int RandomsCorrectionMethod = 5509376;

    /**
     * The attenuation correction method value.
     */
    public static final int AttenuationCorrectionMethod = 5509377;

    /**
     * The decay correction value.
     */
    public static final int DecayCorrection = 5509378;

    /**
     * The reconstruction method value.
     */
    public static final int ReconstructionMethod = 5509379;

    /**
     * The detector lines of response used value.
     */
    public static final int DetectorLinesOfResponseUsed = 5509380;

    /**
     * The scatter correction method value.
     */
    public static final int ScatterCorrectionMethod = 5509381;

    /**
     * The axial acceptance value.
     */
    public static final int AxialAcceptance = 5509632;

    /**
     * The axial mash value.
     */
    public static final int AxialMash = 5509633;

    /**
     * The transverse mash value.
     */
    public static final int TransverseMash = 5509634;

    /**
     * The detector element size value.
     */
    public static final int DetectorElementSize = 5509635;

    /**
     * The coincidence window width value.
     */
    public static final int CoincidenceWindowWidth = 5509648;

    /**
     * The secondary counts type value.
     */
    public static final int SecondaryCountsType = 5509664;

    /**
     * The frame reference time value.
     */
    public static final int FrameReferenceTime = 5509888;

    /**
     * The primary prompts counts accumulated value.
     */
    public static final int PrimaryPromptsCountsAccumulated = 5509904;

    /**
     * The secondary counts accumulated value.
     */
    public static final int SecondaryCountsAccumulated = 5509905;

    /**
     * The slice sensitivity factor value.
     */
    public static final int SliceSensitivityFactor = 5509920;

    /**
     * The decay factor value.
     */
    public static final int DecayFactor = 5509921;

    /**
     * The dose calibration factor value.
     */
    public static final int DoseCalibrationFactor = 5509922;

    /**
     * The scatter fraction factor value.
     */
    public static final int ScatterFractionFactor = 5509923;

    /**
     * The dead time factor value.
     */
    public static final int DeadTimeFactor = 5509924;

    /**
     * The image index value.
     */
    public static final int ImageIndex = 5509936;

    /**
     * The counts included value.
     */
    public static final int CountsIncluded = 5510144;

    /**
     * The dead time correction flag value.
     */
    public static final int DeadTimeCorrectionFlag = 5510145;

    /**
     * The histogram sequence value.
     */
    public static final int HistogramSequence = 6303744;

    /**
     * The histogram number of bins value.
     */
    public static final int HistogramNumberOfBins = 6303746;

    /**
     * The histogram first bin value value.
     */
    public static final int HistogramFirstBinValue = 6303748;

    /**
     * The histogram last bin value value.
     */
    public static final int HistogramLastBinValue = 6303750;

    /**
     * The histogram bin width value.
     */
    public static final int HistogramBinWidth = 6303752;

    /**
     * The histogram explanation value.
     */
    public static final int HistogramExplanation = 6303760;

    /**
     * The histogram data value.
     */
    public static final int HistogramData = 6303776;

    /**
     * The segmentation type value.
     */
    public static final int SegmentationType = 6422529;

    /**
     * The segment sequence value.
     */
    public static final int SegmentSequence = 6422530;

    /**
     * The segmented property category code sequence value.
     */
    public static final int SegmentedPropertyCategoryCodeSequence = 6422531;

    /**
     * The segment number value.
     */
    public static final int SegmentNumber = 6422532;

    /**
     * The segment label value.
     */
    public static final int SegmentLabel = 6422533;

    /**
     * The segment description value.
     */
    public static final int SegmentDescription = 6422534;

    /**
     * The segmentation algorithm identification sequence value.
     */
    public static final int SegmentationAlgorithmIdentificationSequence = 6422535;

    /**
     * The segment algorithm type value.
     */
    public static final int SegmentAlgorithmType = 6422536;

    /**
     * The segment algorithm name value.
     */
    public static final int SegmentAlgorithmName = 6422537;

    /**
     * The segment identification sequence value.
     */
    public static final int SegmentIdentificationSequence = 6422538;

    /**
     * The referenced segment number value.
     */
    public static final int ReferencedSegmentNumber = 6422539;

    /**
     * The recommended display grayscale value value.
     */
    public static final int RecommendedDisplayGrayscaleValue = 6422540;

    /**
     * The recommended display cie lab value value.
     */
    public static final int RecommendedDisplayCIELabValue = 6422541;

    /**
     * The maximum fractional value value.
     */
    public static final int MaximumFractionalValue = 6422542;

    /**
     * The segmented property type code sequence value.
     */
    public static final int SegmentedPropertyTypeCodeSequence = 6422543;

    /**
     * The segmentation fractional type value.
     */
    public static final int SegmentationFractionalType = 6422544;

    /**
     * The segmented property type modifier code sequence value.
     */
    public static final int SegmentedPropertyTypeModifierCodeSequence = 6422545;

    /**
     * The used segments sequence value.
     */
    public static final int UsedSegmentsSequence = 6422546;

    /**
     * The segments overlap value.
     */
    public static final int SegmentsOverlap = 6422547;

    /**
     * The tracking id value.
     */
    public static final int TrackingID = 6422560;

    /**
     * The tracking uid value.
     */
    public static final int TrackingUID = 6422561;

    /**
     * The deformable registration sequence value.
     */
    public static final int DeformableRegistrationSequence = 6553602;

    /**
     * The source frame of reference uid value.
     */
    public static final int SourceFrameOfReferenceUID = 6553603;

    /**
     * The deformable registration grid sequence value.
     */
    public static final int DeformableRegistrationGridSequence = 6553605;

    /**
     * The grid dimensions value.
     */
    public static final int GridDimensions = 6553607;

    /**
     * The grid resolution value.
     */
    public static final int GridResolution = 6553608;

    /**
     * The vector grid data value.
     */
    public static final int VectorGridData = 6553609;

    /**
     * The pre deformation matrix registration sequence value.
     */
    public static final int PreDeformationMatrixRegistrationSequence = 6553615;

    /**
     * The post deformation matrix registration sequence value.
     */
    public static final int PostDeformationMatrixRegistrationSequence = 6553616;

    /**
     * The number of surfaces value.
     */
    public static final int NumberOfSurfaces = 6684673;

    /**
     * The surface sequence value.
     */
    public static final int SurfaceSequence = 6684674;

    /**
     * The surface number value.
     */
    public static final int SurfaceNumber = 6684675;

    /**
     * The surface comments value.
     */
    public static final int SurfaceComments = 6684676;

    /**
     * The surface processing value.
     */
    public static final int SurfaceProcessing = 6684681;

    /**
     * The surface processing ratio value.
     */
    public static final int SurfaceProcessingRatio = 6684682;

    /**
     * The surface processing description value.
     */
    public static final int SurfaceProcessingDescription = 6684683;

    /**
     * The recommended presentation opacity value.
     */
    public static final int RecommendedPresentationOpacity = 6684684;

    /**
     * The recommended presentation type value.
     */
    public static final int RecommendedPresentationType = 6684685;

    /**
     * The finite volume value.
     */
    public static final int FiniteVolume = 6684686;

    /**
     * The manifold value.
     */
    public static final int Manifold = 6684688;

    /**
     * The surface points sequence value.
     */
    public static final int SurfacePointsSequence = 6684689;

    /**
     * The surface points normals sequence value.
     */
    public static final int SurfacePointsNormalsSequence = 6684690;

    /**
     * The surface mesh primitives sequence value.
     */
    public static final int SurfaceMeshPrimitivesSequence = 6684691;

    /**
     * The number of surface points value.
     */
    public static final int NumberOfSurfacePoints = 6684693;

    /**
     * The point coordinates data value.
     */
    public static final int PointCoordinatesData = 6684694;

    /**
     * The point position accuracy value.
     */
    public static final int PointPositionAccuracy = 6684695;

    /**
     * The mean point distance value.
     */
    public static final int MeanPointDistance = 6684696;

    /**
     * The maximum point distance value.
     */
    public static final int MaximumPointDistance = 6684697;

    /**
     * The points bounding box coordinates value.
     */
    public static final int PointsBoundingBoxCoordinates = 6684698;

    /**
     * The axis of rotation value.
     */
    public static final int AxisOfRotation = 6684699;

    /**
     * The center of rotation value.
     */
    public static final int CenterOfRotation = 6684700;

    /**
     * The number of vectors value.
     */
    public static final int NumberOfVectors = 6684702;

    /**
     * The vector dimensionality value.
     */
    public static final int VectorDimensionality = 6684703;

    /**
     * The vector accuracy value.
     */
    public static final int VectorAccuracy = 6684704;

    /**
     * The vector coordinate data value.
     */
    public static final int VectorCoordinateData = 6684705;

    /**
     * The double point coordinates data value.
     */
    public static final int DoublePointCoordinatesData = 6684706;

    /**
     * The triangle point index list value.
     */
    public static final int TrianglePointIndexList = 6684707;

    /**
     * The edge point index list value.
     */
    public static final int EdgePointIndexList = 6684708;

    /**
     * The vertex point index list value.
     */
    public static final int VertexPointIndexList = 6684709;

    /**
     * The triangle strip sequence value.
     */
    public static final int TriangleStripSequence = 6684710;

    /**
     * The triangle fan sequence value.
     */
    public static final int TriangleFanSequence = 6684711;

    /**
     * The line sequence value.
     */
    public static final int LineSequence = 6684712;

    /**
     * The primitive point index list value.
     */
    public static final int PrimitivePointIndexList = 6684713;

    /**
     * The surface count value.
     */
    public static final int SurfaceCount = 6684714;

    /**
     * The referenced surface sequence value.
     */
    public static final int ReferencedSurfaceSequence = 6684715;

    /**
     * The referenced surface number value.
     */
    public static final int ReferencedSurfaceNumber = 6684716;

    /**
     * The segment surface generation algorithm identification sequence value.
     */
    public static final int SegmentSurfaceGenerationAlgorithmIdentificationSequence = 6684717;

    /**
     * The segment surface source instance sequence value.
     */
    public static final int SegmentSurfaceSourceInstanceSequence = 6684718;

    /**
     * The algorithm family code sequence value.
     */
    public static final int AlgorithmFamilyCodeSequence = 6684719;

    /**
     * The algorithm name code sequence value.
     */
    public static final int AlgorithmNameCodeSequence = 6684720;

    /**
     * The algorithm version value.
     */
    public static final int AlgorithmVersion = 6684721;

    /**
     * The algorithm parameters value.
     */
    public static final int AlgorithmParameters = 6684722;

    /**
     * The facet sequence value.
     */
    public static final int FacetSequence = 6684724;

    /**
     * The surface processing algorithm identification sequence value.
     */
    public static final int SurfaceProcessingAlgorithmIdentificationSequence = 6684725;

    /**
     * The algorithm name value.
     */
    public static final int AlgorithmName = 6684726;

    /**
     * The recommended point radius value.
     */
    public static final int RecommendedPointRadius = 6684727;

    /**
     * The recommended line thickness value.
     */
    public static final int RecommendedLineThickness = 6684728;

    /**
     * The long primitive point index list value.
     */
    public static final int LongPrimitivePointIndexList = 6684736;

    /**
     * The long triangle point index list value.
     */
    public static final int LongTrianglePointIndexList = 6684737;

    /**
     * The long edge point index list value.
     */
    public static final int LongEdgePointIndexList = 6684738;

    /**
     * The long vertex point index list value.
     */
    public static final int LongVertexPointIndexList = 6684739;

    /**
     * The track set sequence value.
     */
    public static final int TrackSetSequence = 6684929;

    /**
     * The track sequence value.
     */
    public static final int TrackSequence = 6684930;

    /**
     * The recommended display cie lab value list value.
     */
    public static final int RecommendedDisplayCIELabValueList = 6684931;

    /**
     * The tracking algorithm identification sequence value.
     */
    public static final int TrackingAlgorithmIdentificationSequence = 6684932;

    /**
     * The track set number value.
     */
    public static final int TrackSetNumber = 6684933;

    /**
     * The track set label value.
     */
    public static final int TrackSetLabel = 6684934;

    /**
     * The track set description value.
     */
    public static final int TrackSetDescription = 6684935;

    /**
     * The track set anatomical type code sequence value.
     */
    public static final int TrackSetAnatomicalTypeCodeSequence = 6684936;

    /**
     * The measurements sequence value.
     */
    public static final int MeasurementsSequence = 6684961;

    /**
     * The track set statistics sequence value.
     */
    public static final int TrackSetStatisticsSequence = 6684964;

    /**
     * The floating point values value.
     */
    public static final int FloatingPointValues = 6684965;

    /**
     * The track point index list value.
     */
    public static final int TrackPointIndexList = 6684969;

    /**
     * The track statistics sequence value.
     */
    public static final int TrackStatisticsSequence = 6684976;

    /**
     * The measurement values sequence value.
     */
    public static final int MeasurementValuesSequence = 6684978;

    /**
     * The diffusion acquisition code sequence value.
     */
    public static final int DiffusionAcquisitionCodeSequence = 6684979;

    /**
     * The diffusion model code sequence value.
     */
    public static final int DiffusionModelCodeSequence = 6684980;

    /**
     * The implant size value.
     */
    public static final int ImplantSize = 6840848;

    /**
     * The implant template version value.
     */
    public static final int ImplantTemplateVersion = 6840865;

    /**
     * The replaced implant template sequence value.
     */
    public static final int ReplacedImplantTemplateSequence = 6840866;

    /**
     * The implant type value.
     */
    public static final int ImplantType = 6840867;

    /**
     * The derivation implant template sequence value.
     */
    public static final int DerivationImplantTemplateSequence = 6840868;

    /**
     * The original implant template sequence value.
     */
    public static final int OriginalImplantTemplateSequence = 6840869;

    /**
     * The effective date time value.
     */
    public static final int EffectiveDateTime = 6840870;

    /**
     * The implant target anatomy sequence value.
     */
    public static final int ImplantTargetAnatomySequence = 6840880;

    /**
     * The information from manufacturer sequence value.
     */
    public static final int InformationFromManufacturerSequence = 6840928;

    /**
     * The notification from manufacturer sequence value.
     */
    public static final int NotificationFromManufacturerSequence = 6840933;

    /**
     * The information issue date time value.
     */
    public static final int InformationIssueDateTime = 6840944;

    /**
     * The information summary value.
     */
    public static final int InformationSummary = 6840960;

    /**
     * The implant regulatory disapproval code sequence value.
     */
    public static final int ImplantRegulatoryDisapprovalCodeSequence = 6840992;

    /**
     * The overall template spatial tolerance value.
     */
    public static final int OverallTemplateSpatialTolerance = 6840997;

    /**
     * The hpgl document sequence value.
     */
    public static final int HPGLDocumentSequence = 6841024;

    /**
     * The hpgl document id value.
     */
    public static final int HPGLDocumentID = 6841040;

    /**
     * The hpgl document label value.
     */
    public static final int HPGLDocumentLabel = 6841045;

    /**
     * The view orientation code sequence value.
     */
    public static final int ViewOrientationCodeSequence = 6841056;

    /**
     * The view orientation modifier code sequence value.
     */
    public static final int ViewOrientationModifierCodeSequence = 6841072;

    /**
     * The hpgl document scaling value.
     */
    public static final int HPGLDocumentScaling = 6841074;

    /**
     * The hpgl document value.
     */
    public static final int HPGLDocument = 6841088;

    /**
     * The hpgl contour pen number value.
     */
    public static final int HPGLContourPenNumber = 6841104;

    /**
     * The hpgl pen sequence value.
     */
    public static final int HPGLPenSequence = 6841120;

    /**
     * The hpgl pen number value.
     */
    public static final int HPGLPenNumber = 6841136;

    /**
     * The hpgl pen label value.
     */
    public static final int HPGLPenLabel = 6841152;

    /**
     * The hpgl pen description value.
     */
    public static final int HPGLPenDescription = 6841157;

    /**
     * The recommended rotation point value.
     */
    public static final int RecommendedRotationPoint = 6841158;

    /**
     * The bounding rectangle value.
     */
    public static final int BoundingRectangle = 6841159;

    /**
     * The implant template3 d model surface number value.
     */
    public static final int ImplantTemplate3DModelSurfaceNumber = 6841168;

    /**
     * The surface model description sequence value.
     */
    public static final int SurfaceModelDescriptionSequence = 6841184;

    /**
     * The surface model label value.
     */
    public static final int SurfaceModelLabel = 6841216;

    /**
     * The surface model scaling factor value.
     */
    public static final int SurfaceModelScalingFactor = 6841232;

    /**
     * The materials code sequence value.
     */
    public static final int MaterialsCodeSequence = 6841248;

    /**
     * The coating materials code sequence value.
     */
    public static final int CoatingMaterialsCodeSequence = 6841252;

    /**
     * The implant type code sequence value.
     */
    public static final int ImplantTypeCodeSequence = 6841256;

    /**
     * The fixation method code sequence value.
     */
    public static final int FixationMethodCodeSequence = 6841260;

    /**
     * The mating feature sets sequence value.
     */
    public static final int MatingFeatureSetsSequence = 6841264;

    /**
     * The mating feature set id value.
     */
    public static final int MatingFeatureSetID = 6841280;

    /**
     * The mating feature set label value.
     */
    public static final int MatingFeatureSetLabel = 6841296;

    /**
     * The mating feature sequence value.
     */
    public static final int MatingFeatureSequence = 6841312;

    /**
     * The mating feature id value.
     */
    public static final int MatingFeatureID = 6841328;

    /**
     * The mating feature degree of freedom sequence value.
     */
    public static final int MatingFeatureDegreeOfFreedomSequence = 6841344;

    /**
     * The degree of freedom id value.
     */
    public static final int DegreeOfFreedomID = 6841360;

    /**
     * The degree of freedom type value.
     */
    public static final int DegreeOfFreedomType = 6841376;

    /**
     * The two d mating feature coordinates sequence value.
     */
    public static final int TwoDMatingFeatureCoordinatesSequence = 6841392;

    /**
     * The referenced hpgl document id value.
     */
    public static final int ReferencedHPGLDocumentID = 6841408;

    /**
     * The two d mating point value.
     */
    public static final int TwoDMatingPoint = 6841424;

    /**
     * The two d mating axes value.
     */
    public static final int TwoDMatingAxes = 6841440;

    /**
     * The two d degree of freedom sequence value.
     */
    public static final int TwoDDegreeOfFreedomSequence = 6841456;

    /**
     * The three d degree of freedom axis value.
     */
    public static final int ThreeDDegreeOfFreedomAxis = 6841488;

    /**
     * The range of freedom value.
     */
    public static final int RangeOfFreedom = 6841504;

    /**
     * The three d mating point value.
     */
    public static final int ThreeDMatingPoint = 6841536;

    /**
     * The three d mating axes value.
     */
    public static final int ThreeDMatingAxes = 6841552;

    /**
     * The two d degree of freedom axis value.
     */
    public static final int TwoDDegreeOfFreedomAxis = 6841584;

    /**
     * The planning landmark point sequence value.
     */
    public static final int PlanningLandmarkPointSequence = 6841600;

    /**
     * The planning landmark line sequence value.
     */
    public static final int PlanningLandmarkLineSequence = 6841616;

    /**
     * The planning landmark plane sequence value.
     */
    public static final int PlanningLandmarkPlaneSequence = 6841632;

    /**
     * The planning landmark id value.
     */
    public static final int PlanningLandmarkID = 6841648;

    /**
     * The planning landmark description value.
     */
    public static final int PlanningLandmarkDescription = 6841664;

    /**
     * The planning landmark identification code sequence value.
     */
    public static final int PlanningLandmarkIdentificationCodeSequence = 6841669;

    /**
     * The two d point coordinates sequence value.
     */
    public static final int TwoDPointCoordinatesSequence = 6841680;

    /**
     * The two d point coordinates value.
     */
    public static final int TwoDPointCoordinates = 6841696;

    /**
     * The three d point coordinates value.
     */
    public static final int ThreeDPointCoordinates = 6841744;

    /**
     * The two d line coordinates sequence value.
     */
    public static final int TwoDLineCoordinatesSequence = 6841760;

    /**
     * The two d line coordinates value.
     */
    public static final int TwoDLineCoordinates = 6841776;

    /**
     * The three d line coordinates value.
     */
    public static final int ThreeDLineCoordinates = 6841808;

    /**
     * The two d plane coordinates sequence value.
     */
    public static final int TwoDPlaneCoordinatesSequence = 6841824;

    /**
     * The two d plane intersection value.
     */
    public static final int TwoDPlaneIntersection = 6841840;

    /**
     * The three d plane origin value.
     */
    public static final int ThreeDPlaneOrigin = 6841872;

    /**
     * The three d plane normal value.
     */
    public static final int ThreeDPlaneNormal = 6841888;

    /**
     * The model modification value.
     */
    public static final int ModelModification = 6844417;

    /**
     * The model mirroring value.
     */
    public static final int ModelMirroring = 6844418;

    /**
     * The model usage code sequence value.
     */
    public static final int ModelUsageCodeSequence = 6844419;

    /**
     * The model group uid value.
     */
    public static final int ModelGroupUID = 6844420;

    /**
     * The relative uri reference within encapsulated document value.
     */
    public static final int RelativeURIReferenceWithinEncapsulatedDocument = 6844421;

    /**
     * The annotation coordinate type value.
     */
    public static final int AnnotationCoordinateType = 6946817;

    /**
     * The annotation group sequence value.
     */
    public static final int AnnotationGroupSequence = 6946818;

    /**
     * The annotation group uid value.
     */
    public static final int AnnotationGroupUID = 6946819;

    /**
     * The annotation group label value.
     */
    public static final int AnnotationGroupLabel = 6946821;

    /**
     * The annotation group description value.
     */
    public static final int AnnotationGroupDescription = 6946822;

    /**
     * The annotation group generation type value.
     */
    public static final int AnnotationGroupGenerationType = 6946823;

    /**
     * The annotation group algorithm identification sequence value.
     */
    public static final int AnnotationGroupAlgorithmIdentificationSequence = 6946824;

    /**
     * The annotation property category code sequence value.
     */
    public static final int AnnotationPropertyCategoryCodeSequence = 6946825;

    /**
     * The annotation property type code sequence value.
     */
    public static final int AnnotationPropertyTypeCodeSequence = 6946826;

    /**
     * The annotation property type modifier code sequence value.
     */
    public static final int AnnotationPropertyTypeModifierCodeSequence = 6946827;

    /**
     * The number of annotations value.
     */
    public static final int NumberOfAnnotations = 6946828;

    /**
     * The annotation applies to all optical paths value.
     */
    public static final int AnnotationAppliesToAllOpticalPaths = 6946829;

    /**
     * The referenced optical path identifier value.
     */
    public static final int ReferencedOpticalPathIdentifier = 6946830;

    /**
     * The annotation applies to all z planes value.
     */
    public static final int AnnotationAppliesToAllZPlanes = 6946831;

    /**
     * The common z coordinate value value.
     */
    public static final int CommonZCoordinateValue = 6946832;

    /**
     * The annotation index list value.
     */
    public static final int AnnotationIndexList = 6946833;

    /**
     * The graphic annotation sequence value.
     */
    public static final int GraphicAnnotationSequence = 7340033;

    /**
     * The graphic layer value.
     */
    public static final int GraphicLayer = 7340034;

    /**
     * The bounding box annotation units value.
     */
    public static final int BoundingBoxAnnotationUnits = 7340035;

    /**
     * The anchor point annotation units value.
     */
    public static final int AnchorPointAnnotationUnits = 7340036;

    /**
     * The graphic annotation units value.
     */
    public static final int GraphicAnnotationUnits = 7340037;

    /**
     * The unformatted text value value.
     */
    public static final int UnformattedTextValue = 7340038;

    /**
     * The text object sequence value.
     */
    public static final int TextObjectSequence = 7340040;

    /**
     * The graphic object sequence value.
     */
    public static final int GraphicObjectSequence = 7340041;

    /**
     * The bounding box top left hand corner value.
     */
    public static final int BoundingBoxTopLeftHandCorner = 7340048;

    /**
     * The bounding box bottom right hand corner value.
     */
    public static final int BoundingBoxBottomRightHandCorner = 7340049;

    /**
     * The bounding box text horizontal justification value.
     */
    public static final int BoundingBoxTextHorizontalJustification = 7340050;

    /**
     * The anchor point value.
     */
    public static final int AnchorPoint = 7340052;

    /**
     * The anchor point visibility value.
     */
    public static final int AnchorPointVisibility = 7340053;

    /**
     * The graphic dimensions value.
     */
    public static final int GraphicDimensions = 7340064;

    /**
     * The number of graphic points value.
     */
    public static final int NumberOfGraphicPoints = 7340065;

    /**
     * The graphic data value.
     */
    public static final int GraphicData = 7340066;

    /**
     * The graphic type value.
     */
    public static final int GraphicType = 7340067;

    /**
     * The graphic filled value.
     */
    public static final int GraphicFilled = 7340068;

    /**
     * The image rotation retired value.
     */
    public static final int ImageRotationRetired = 7340096;

    /**
     * The image horizontal flip value.
     */
    public static final int ImageHorizontalFlip = 7340097;

    /**
     * The image rotation value.
     */
    public static final int ImageRotation = 7340098;

    /**
     * The displayed area top left hand corner trial value.
     */
    public static final int DisplayedAreaTopLeftHandCornerTrial = 7340112;

    /**
     * The displayed area bottom right hand corner trial value.
     */
    public static final int DisplayedAreaBottomRightHandCornerTrial = 7340113;

    /**
     * The displayed area top left hand corner value.
     */
    public static final int DisplayedAreaTopLeftHandCorner = 7340114;

    /**
     * The displayed area bottom right hand corner value.
     */
    public static final int DisplayedAreaBottomRightHandCorner = 7340115;

    /**
     * The displayed area selection sequence value.
     */
    public static final int DisplayedAreaSelectionSequence = 7340122;

    /**
     * The graphic layer sequence value.
     */
    public static final int GraphicLayerSequence = 7340128;

    /**
     * The graphic layer order value.
     */
    public static final int GraphicLayerOrder = 7340130;

    /**
     * The graphic layer recommended display grayscale value value.
     */
    public static final int GraphicLayerRecommendedDisplayGrayscaleValue = 7340134;

    /**
     * The graphic layer recommended display rgb value value.
     */
    public static final int GraphicLayerRecommendedDisplayRGBValue = 7340135;

    /**
     * The graphic layer description value.
     */
    public static final int GraphicLayerDescription = 7340136;

    /**
     * The content label value.
     */
    public static final int ContentLabel = 7340160;

    /**
     * The content description value.
     */
    public static final int ContentDescription = 7340161;

    /**
     * The presentation creation date value.
     */
    public static final int PresentationCreationDate = 7340162;

    /**
     * The presentation creation time value.
     */
    public static final int PresentationCreationTime = 7340163;

    /**
     * The content creator name value.
     */
    public static final int ContentCreatorName = 7340164;

    /**
     * The content creator identification code sequence value.
     */
    public static final int ContentCreatorIdentificationCodeSequence = 7340166;

    /**
     * The alternate content description sequence value.
     */
    public static final int AlternateContentDescriptionSequence = 7340167;

    /**
     * The presentation size mode value.
     */
    public static final int PresentationSizeMode = 7340288;

    /**
     * The presentation pixel spacing value.
     */
    public static final int PresentationPixelSpacing = 7340289;

    /**
     * The presentation pixel aspect ratio value.
     */
    public static final int PresentationPixelAspectRatio = 7340290;

    /**
     * The presentation pixel magnification ratio value.
     */
    public static final int PresentationPixelMagnificationRatio = 7340291;

    /**
     * The graphic group label value.
     */
    public static final int GraphicGroupLabel = 7340551;

    /**
     * The graphic group description value.
     */
    public static final int GraphicGroupDescription = 7340552;

    /**
     * The compound graphic sequence value.
     */
    public static final int CompoundGraphicSequence = 7340553;

    /**
     * The compound graphic instance id value.
     */
    public static final int CompoundGraphicInstanceID = 7340582;

    /**
     * The font name value.
     */
    public static final int FontName = 7340583;

    /**
     * The font name type value.
     */
    public static final int FontNameType = 7340584;

    /**
     * The css font name value.
     */
    public static final int CSSFontName = 7340585;

    /**
     * The rotation angle value.
     */
    public static final int RotationAngle = 7340592;

    /**
     * The text style sequence value.
     */
    public static final int TextStyleSequence = 7340593;

    /**
     * The line style sequence value.
     */
    public static final int LineStyleSequence = 7340594;

    /**
     * The fill style sequence value.
     */
    public static final int FillStyleSequence = 7340595;

    /**
     * The graphic group sequence value.
     */
    public static final int GraphicGroupSequence = 7340596;

    /**
     * The text color cie lab value value.
     */
    public static final int TextColorCIELabValue = 7340609;

    /**
     * The horizontal alignment value.
     */
    public static final int HorizontalAlignment = 7340610;

    /**
     * The vertical alignment value.
     */
    public static final int VerticalAlignment = 7340611;

    /**
     * The shadow style value.
     */
    public static final int ShadowStyle = 7340612;

    /**
     * The shadow offset x value.
     */
    public static final int ShadowOffsetX = 7340613;

    /**
     * The shadow offset y value.
     */
    public static final int ShadowOffsetY = 7340614;

    /**
     * The shadow color cie lab value value.
     */
    public static final int ShadowColorCIELabValue = 7340615;

    /**
     * The underlined value.
     */
    public static final int Underlined = 7340616;

    /**
     * The bold value.
     */
    public static final int Bold = 7340617;

    /**
     * The italic value.
     */
    public static final int Italic = 7340624;

    /**
     * The pattern on color cie lab value value.
     */
    public static final int PatternOnColorCIELabValue = 7340625;

    /**
     * The pattern off color cie lab value value.
     */
    public static final int PatternOffColorCIELabValue = 7340626;

    /**
     * The line thickness value.
     */
    public static final int LineThickness = 7340627;

    /**
     * The line dashing style value.
     */
    public static final int LineDashingStyle = 7340628;

    /**
     * The line pattern value.
     */
    public static final int LinePattern = 7340629;

    /**
     * The fill pattern value.
     */
    public static final int FillPattern = 7340630;

    /**
     * The fill mode value.
     */
    public static final int FillMode = 7340631;

    /**
     * The shadow opacity value.
     */
    public static final int ShadowOpacity = 7340632;

    /**
     * The gap length value.
     */
    public static final int GapLength = 7340641;

    /**
     * The diameter of visibility value.
     */
    public static final int DiameterOfVisibility = 7340642;

    /**
     * The rotation point value.
     */
    public static final int RotationPoint = 7340659;

    /**
     * The tick alignment value.
     */
    public static final int TickAlignment = 7340660;

    /**
     * The show tick label value.
     */
    public static final int ShowTickLabel = 7340664;

    /**
     * The tick label alignment value.
     */
    public static final int TickLabelAlignment = 7340665;

    /**
     * The compound graphic units value.
     */
    public static final int CompoundGraphicUnits = 7340674;

    /**
     * The pattern on opacity value.
     */
    public static final int PatternOnOpacity = 7340676;

    /**
     * The pattern off opacity value.
     */
    public static final int PatternOffOpacity = 7340677;

    /**
     * The major ticks sequence value.
     */
    public static final int MajorTicksSequence = 7340679;

    /**
     * The tick position value.
     */
    public static final int TickPosition = 7340680;

    /**
     * The tick label value.
     */
    public static final int TickLabel = 7340681;

    /**
     * The compound graphic type value.
     */
    public static final int CompoundGraphicType = 7340692;

    /**
     * The graphic group id value.
     */
    public static final int GraphicGroupID = 7340693;

    /**
     * The shape type value.
     */
    public static final int ShapeType = 7340806;

    /**
     * The registration sequence value.
     */
    public static final int RegistrationSequence = 7340808;

    /**
     * The matrix registration sequence value.
     */
    public static final int MatrixRegistrationSequence = 7340809;

    /**
     * The matrix sequence value.
     */
    public static final int MatrixSequence = 7340810;

    /**
     * The frame of reference to displayed coordinate system transformation matrix value.
     */
    public static final int FrameOfReferenceToDisplayedCoordinateSystemTransformationMatrix = 7340811;

    /**
     * The frame of reference transformation matrix type value.
     */
    public static final int FrameOfReferenceTransformationMatrixType = 7340812;

    /**
     * The registration type code sequence value.
     */
    public static final int RegistrationTypeCodeSequence = 7340813;

    /**
     * The fiducial description value.
     */
    public static final int FiducialDescription = 7340815;

    /**
     * The fiducial identifier value.
     */
    public static final int FiducialIdentifier = 7340816;

    /**
     * The fiducial identifier code sequence value.
     */
    public static final int FiducialIdentifierCodeSequence = 7340817;

    /**
     * The contour uncertainty radius value.
     */
    public static final int ContourUncertaintyRadius = 7340818;

    /**
     * The used fiducials sequence value.
     */
    public static final int UsedFiducialsSequence = 7340820;

    /**
     * The used rt structure set roi sequence value.
     */
    public static final int UsedRTStructureSetROISequence = 7340821;

    /**
     * The graphic coordinates data sequence value.
     */
    public static final int GraphicCoordinatesDataSequence = 7340824;

    /**
     * The fiducial uid value.
     */
    public static final int FiducialUID = 7340826;

    /**
     * The referenced fiducial uid value.
     */
    public static final int ReferencedFiducialUID = 7340827;

    /**
     * The fiducial set sequence value.
     */
    public static final int FiducialSetSequence = 7340828;

    /**
     * The fiducial sequence value.
     */
    public static final int FiducialSequence = 7340830;

    /**
     * The fiducials property category code sequence value.
     */
    public static final int FiducialsPropertyCategoryCodeSequence = 7340831;

    /**
     * The graphic layer recommended display cie lab value value.
     */
    public static final int GraphicLayerRecommendedDisplayCIELabValue = 7341057;

    /**
     * The blending sequence value.
     */
    public static final int BlendingSequence = 7341058;

    /**
     * The relative opacity value.
     */
    public static final int RelativeOpacity = 7341059;

    /**
     * The referenced spatial registration sequence value.
     */
    public static final int ReferencedSpatialRegistrationSequence = 7341060;

    /**
     * The blending position value.
     */
    public static final int BlendingPosition = 7341061;

    /**
     * The presentation display collection uid value.
     */
    public static final int PresentationDisplayCollectionUID = 7344385;

    /**
     * The presentation sequence collection uid value.
     */
    public static final int PresentationSequenceCollectionUID = 7344386;

    /**
     * The presentation sequence position index value.
     */
    public static final int PresentationSequencePositionIndex = 7344387;

    /**
     * The rendered image reference sequence value.
     */
    public static final int RenderedImageReferenceSequence = 7344388;

    /**
     * The volumetric presentation state input sequence value.
     */
    public static final int VolumetricPresentationStateInputSequence = 7344641;

    /**
     * The presentation input type value.
     */
    public static final int PresentationInputType = 7344642;

    /**
     * The input sequence position index value.
     */
    public static final int InputSequencePositionIndex = 7344643;

    /**
     * The crop value.
     */
    public static final int Crop = 7344644;

    /**
     * The cropping specification index value.
     */
    public static final int CroppingSpecificationIndex = 7344645;

    /**
     * The compositing method value.
     */
    public static final int CompositingMethod = 7344646;

    /**
     * The volumetric presentation input number value.
     */
    public static final int VolumetricPresentationInputNumber = 7344647;

    /**
     * The image volume geometry value.
     */
    public static final int ImageVolumeGeometry = 7344648;

    /**
     * The volumetric presentation input set uid value.
     */
    public static final int VolumetricPresentationInputSetUID = 7344649;

    /**
     * The volumetric presentation input set sequence value.
     */
    public static final int VolumetricPresentationInputSetSequence = 7344650;

    /**
     * The global crop value.
     */
    public static final int GlobalCrop = 7344651;

    /**
     * The global cropping specification index value.
     */
    public static final int GlobalCroppingSpecificationIndex = 7344652;

    /**
     * The rendering method value.
     */
    public static final int RenderingMethod = 7344653;

    /**
     * The volume cropping sequence value.
     */
    public static final int VolumeCroppingSequence = 7344897;

    /**
     * The volume cropping method value.
     */
    public static final int VolumeCroppingMethod = 7344898;

    /**
     * The bounding box crop value.
     */
    public static final int BoundingBoxCrop = 7344899;

    /**
     * The oblique cropping plane sequence value.
     */
    public static final int ObliqueCroppingPlaneSequence = 7344900;

    /**
     * The plane value.
     */
    public static final int Plane = 7344901;

    /**
     * The plane normal value.
     */
    public static final int PlaneNormal = 7344902;

    /**
     * The cropping specification number value.
     */
    public static final int CroppingSpecificationNumber = 7344905;

    /**
     * The multi planar reconstruction style value.
     */
    public static final int MultiPlanarReconstructionStyle = 7345409;

    /**
     * The mpr thickness type value.
     */
    public static final int MPRThicknessType = 7345410;

    /**
     * The mpr slab thickness value.
     */
    public static final int MPRSlabThickness = 7345411;

    /**
     * The mpr top left hand corner value.
     */
    public static final int MPRTopLeftHandCorner = 7345413;

    /**
     * The mpr view width direction value.
     */
    public static final int MPRViewWidthDirection = 7345415;

    /**
     * The mpr view width value.
     */
    public static final int MPRViewWidth = 7345416;

    /**
     * The number of volumetric curve points value.
     */
    public static final int NumberOfVolumetricCurvePoints = 7345420;

    /**
     * The volumetric curve points value.
     */
    public static final int VolumetricCurvePoints = 7345421;

    /**
     * The mpr view height direction value.
     */
    public static final int MPRViewHeightDirection = 7345425;

    /**
     * The mpr view height value.
     */
    public static final int MPRViewHeight = 7345426;

    /**
     * The render projection value.
     */
    public static final int RenderProjection = 7345666;

    /**
     * The viewpoint position value.
     */
    public static final int ViewpointPosition = 7345667;

    /**
     * The viewpoint look at point value.
     */
    public static final int ViewpointLookAtPoint = 7345668;

    /**
     * The viewpoint up direction value.
     */
    public static final int ViewpointUpDirection = 7345669;

    /**
     * The render field of view value.
     */
    public static final int RenderFieldOfView = 7345670;

    /**
     * The sampling step size value.
     */
    public static final int SamplingStepSize = 7345671;

    /**
     * The shading style value.
     */
    public static final int ShadingStyle = 7345921;

    /**
     * The ambient reflection intensity value.
     */
    public static final int AmbientReflectionIntensity = 7345922;

    /**
     * The light direction value.
     */
    public static final int LightDirection = 7345923;

    /**
     * The diffuse reflection intensity value.
     */
    public static final int DiffuseReflectionIntensity = 7345924;

    /**
     * The specular reflection intensity value.
     */
    public static final int SpecularReflectionIntensity = 7345925;

    /**
     * The shininess value.
     */
    public static final int Shininess = 7345926;

    /**
     * The presentation state classification component sequence value.
     */
    public static final int PresentationStateClassificationComponentSequence = 7346177;

    /**
     * The component type value.
     */
    public static final int ComponentType = 7346178;

    /**
     * The component input sequence value.
     */
    public static final int ComponentInputSequence = 7346179;

    /**
     * The volumetric presentation input index value.
     */
    public static final int VolumetricPresentationInputIndex = 7346180;

    /**
     * The presentation state compositor component sequence value.
     */
    public static final int PresentationStateCompositorComponentSequence = 7346181;

    /**
     * The weighting transfer function sequence value.
     */
    public static final int WeightingTransferFunctionSequence = 7346182;

    /**
     * The weighting lookup table descriptor value.
     */
    public static final int WeightingLookupTableDescriptor = 7346183;

    /**
     * The weighting lookup table data value.
     */
    public static final int WeightingLookupTableData = 7346184;

    /**
     * The volumetric annotation sequence value.
     */
    public static final int VolumetricAnnotationSequence = 7346433;

    /**
     * The referenced structured context sequence value.
     */
    public static final int ReferencedStructuredContextSequence = 7346435;

    /**
     * The referenced content item value.
     */
    public static final int ReferencedContentItem = 7346436;

    /**
     * The volumetric presentation input annotation sequence value.
     */
    public static final int VolumetricPresentationInputAnnotationSequence = 7346437;

    /**
     * The annotation clipping value.
     */
    public static final int AnnotationClipping = 7346439;

    /**
     * The presentation animation style value.
     */
    public static final int PresentationAnimationStyle = 7346689;

    /**
     * The recommended animation rate value.
     */
    public static final int RecommendedAnimationRate = 7346691;

    /**
     * The animation curve sequence value.
     */
    public static final int AnimationCurveSequence = 7346692;

    /**
     * The animation step size value.
     */
    public static final int AnimationStepSize = 7346693;

    /**
     * The swivel range value.
     */
    public static final int SwivelRange = 7346694;

    /**
     * The volumetric curve up directions value.
     */
    public static final int VolumetricCurveUpDirections = 7346695;

    /**
     * The volume stream sequence value.
     */
    public static final int VolumeStreamSequence = 7346696;

    /**
     * The rgba transfer function description value.
     */
    public static final int RGBATransferFunctionDescription = 7346697;

    /**
     * The advanced blending sequence value.
     */
    public static final int AdvancedBlendingSequence = 7346945;

    /**
     * The blending input number value.
     */
    public static final int BlendingInputNumber = 7346946;

    /**
     * The blending display input sequence value.
     */
    public static final int BlendingDisplayInputSequence = 7346947;

    /**
     * The blending display sequence value.
     */
    public static final int BlendingDisplaySequence = 7346948;

    /**
     * The blending mode value.
     */
    public static final int BlendingMode = 7346950;

    /**
     * The time series blending value.
     */
    public static final int TimeSeriesBlending = 7346951;

    /**
     * The geometry for display value.
     */
    public static final int GeometryForDisplay = 7346952;

    /**
     * The threshold sequence value.
     */
    public static final int ThresholdSequence = 7346961;

    /**
     * The threshold value sequence value.
     */
    public static final int ThresholdValueSequence = 7346962;

    /**
     * The threshold type value.
     */
    public static final int ThresholdType = 7346963;

    /**
     * The threshold value value.
     */
    public static final int ThresholdValue = 7346964;

    /**
     * The hanging protocol name value.
     */
    public static final int HangingProtocolName = 7471106;

    /**
     * The hanging protocol description value.
     */
    public static final int HangingProtocolDescription = 7471108;

    /**
     * The hanging protocol level value.
     */
    public static final int HangingProtocolLevel = 7471110;

    /**
     * The hanging protocol creator value.
     */
    public static final int HangingProtocolCreator = 7471112;

    /**
     * The hanging protocol creation date time value.
     */
    public static final int HangingProtocolCreationDateTime = 7471114;

    /**
     * The hanging protocol definition sequence value.
     */
    public static final int HangingProtocolDefinitionSequence = 7471116;

    /**
     * The hanging protocol user identification code sequence value.
     */
    public static final int HangingProtocolUserIdentificationCodeSequence = 7471118;

    /**
     * The hanging protocol user group name value.
     */
    public static final int HangingProtocolUserGroupName = 7471120;

    /**
     * The source hanging protocol sequence value.
     */
    public static final int SourceHangingProtocolSequence = 7471122;

    /**
     * The number of priors referenced value.
     */
    public static final int NumberOfPriorsReferenced = 7471124;

    /**
     * The image sets sequence value.
     */
    public static final int ImageSetsSequence = 7471136;

    /**
     * The image set selector sequence value.
     */
    public static final int ImageSetSelectorSequence = 7471138;

    /**
     * The image set selector usage flag value.
     */
    public static final int ImageSetSelectorUsageFlag = 7471140;

    /**
     * The selector attribute value.
     */
    public static final int SelectorAttribute = 7471142;

    /**
     * The selector value number value.
     */
    public static final int SelectorValueNumber = 7471144;

    /**
     * The time based image sets sequence value.
     */
    public static final int TimeBasedImageSetsSequence = 7471152;

    /**
     * The image set number value.
     */
    public static final int ImageSetNumber = 7471154;

    /**
     * The image set selector category value.
     */
    public static final int ImageSetSelectorCategory = 7471156;

    /**
     * The relative time value.
     */
    public static final int RelativeTime = 7471160;

    /**
     * The relative time units value.
     */
    public static final int RelativeTimeUnits = 7471162;

    /**
     * The abstract prior value value.
     */
    public static final int AbstractPriorValue = 7471164;

    /**
     * The abstract prior code sequence value.
     */
    public static final int AbstractPriorCodeSequence = 7471166;

    /**
     * The image set label value.
     */
    public static final int ImageSetLabel = 7471168;

    /**
     * The selector attribute vr value.
     */
    public static final int SelectorAttributeVR = 7471184;

    /**
     * The selector sequence pointer value.
     */
    public static final int SelectorSequencePointer = 7471186;

    /**
     * The selector sequence pointer private creator value.
     */
    public static final int SelectorSequencePointerPrivateCreator = 7471188;

    /**
     * The selector attribute private creator value.
     */
    public static final int SelectorAttributePrivateCreator = 7471190;

    /**
     * The selector ae value value.
     */
    public static final int SelectorAEValue = 7471198;

    /**
     * The selector as value value.
     */
    public static final int SelectorASValue = 7471199;

    /**
     * The selector at value value.
     */
    public static final int SelectorATValue = 7471200;

    /**
     * The selector da value value.
     */
    public static final int SelectorDAValue = 7471201;

    /**
     * The selector cs value value.
     */
    public static final int SelectorCSValue = 7471202;

    /**
     * The selector dt value value.
     */
    public static final int SelectorDTValue = 7471203;

    /**
     * The selector is value value.
     */
    public static final int SelectorISValue = 7471204;

    /**
     * The selector ob value value.
     */
    public static final int SelectorOBValue = 7471205;

    /**
     * The selector lo value value.
     */
    public static final int SelectorLOValue = 7471206;

    /**
     * The selector of value value.
     */
    public static final int SelectorOFValue = 7471207;

    /**
     * The selector lt value value.
     */
    public static final int SelectorLTValue = 7471208;

    /**
     * The selector ow value value.
     */
    public static final int SelectorOWValue = 7471209;

    /**
     * The selector pn value value.
     */
    public static final int SelectorPNValue = 7471210;

    /**
     * The selector tm value value.
     */
    public static final int SelectorTMValue = 7471211;

    /**
     * The selector sh value value.
     */
    public static final int SelectorSHValue = 7471212;

    /**
     * The selector un value value.
     */
    public static final int SelectorUNValue = 7471213;

    /**
     * The selector st value value.
     */
    public static final int SelectorSTValue = 7471214;

    /**
     * The selector uc value value.
     */
    public static final int SelectorUCValue = 7471215;

    /**
     * The selector ut value value.
     */
    public static final int SelectorUTValue = 7471216;

    /**
     * The selector ur value value.
     */
    public static final int SelectorURValue = 7471217;

    /**
     * The selector ds value value.
     */
    public static final int SelectorDSValue = 7471218;

    /**
     * The selector od value value.
     */
    public static final int SelectorODValue = 7471219;

    /**
     * The selector fd value value.
     */
    public static final int SelectorFDValue = 7471220;

    /**
     * The selector ol value value.
     */
    public static final int SelectorOLValue = 7471221;

    /**
     * The selector fl value value.
     */
    public static final int SelectorFLValue = 7471222;

    /**
     * The selector ul value value.
     */
    public static final int SelectorULValue = 7471224;

    /**
     * The selector us value value.
     */
    public static final int SelectorUSValue = 7471226;

    /**
     * The selector sl value value.
     */
    public static final int SelectorSLValue = 7471228;

    /**
     * The selector ss value value.
     */
    public static final int SelectorSSValue = 7471230;

    /**
     * The selector ui value value.
     */
    public static final int SelectorUIValue = 7471231;

    /**
     * The selector code sequence value value.
     */
    public static final int SelectorCodeSequenceValue = 7471232;

    /**
     * The selector ov value value.
     */
    public static final int SelectorOVValue = 7471233;

    /**
     * The selector sv value value.
     */
    public static final int SelectorSVValue = 7471234;

    /**
     * The selector uv value value.
     */
    public static final int SelectorUVValue = 7471235;

    /**
     * The number of screens value.
     */
    public static final int NumberOfScreens = 7471360;

    /**
     * The nominal screen definition sequence value.
     */
    public static final int NominalScreenDefinitionSequence = 7471362;

    /**
     * The number of vertical pixels value.
     */
    public static final int NumberOfVerticalPixels = 7471364;

    /**
     * The number of horizontal pixels value.
     */
    public static final int NumberOfHorizontalPixels = 7471366;

    /**
     * The display environment spatial position value.
     */
    public static final int DisplayEnvironmentSpatialPosition = 7471368;

    /**
     * The screen minimum grayscale bit depth value.
     */
    public static final int ScreenMinimumGrayscaleBitDepth = 7471370;

    /**
     * The screen minimum color bit depth value.
     */
    public static final int ScreenMinimumColorBitDepth = 7471372;

    /**
     * The application maximum repaint time value.
     */
    public static final int ApplicationMaximumRepaintTime = 7471374;

    /**
     * The display sets sequence value.
     */
    public static final int DisplaySetsSequence = 7471616;

    /**
     * The display set number value.
     */
    public static final int DisplaySetNumber = 7471618;

    /**
     * The display set label value.
     */
    public static final int DisplaySetLabel = 7471619;

    /**
     * The display set presentation group value.
     */
    public static final int DisplaySetPresentationGroup = 7471620;

    /**
     * The display set presentation group description value.
     */
    public static final int DisplaySetPresentationGroupDescription = 7471622;

    /**
     * The partial data display handling value.
     */
    public static final int PartialDataDisplayHandling = 7471624;

    /**
     * The synchronized scrolling sequence value.
     */
    public static final int SynchronizedScrollingSequence = 7471632;

    /**
     * The display set scrolling group value.
     */
    public static final int DisplaySetScrollingGroup = 7471634;

    /**
     * The navigation indicator sequence value.
     */
    public static final int NavigationIndicatorSequence = 7471636;

    /**
     * The navigation display set value.
     */
    public static final int NavigationDisplaySet = 7471638;

    /**
     * The reference display sets value.
     */
    public static final int ReferenceDisplaySets = 7471640;

    /**
     * The image boxes sequence value.
     */
    public static final int ImageBoxesSequence = 7471872;

    /**
     * The image box number value.
     */
    public static final int ImageBoxNumber = 7471874;

    /**
     * The image box layout type value.
     */
    public static final int ImageBoxLayoutType = 7471876;

    /**
     * The image box tile horizontal dimension value.
     */
    public static final int ImageBoxTileHorizontalDimension = 7471878;

    /**
     * The image box tile vertical dimension value.
     */
    public static final int ImageBoxTileVerticalDimension = 7471880;

    /**
     * The image box scroll direction value.
     */
    public static final int ImageBoxScrollDirection = 7471888;

    /**
     * The image box small scroll type value.
     */
    public static final int ImageBoxSmallScrollType = 7471890;

    /**
     * The image box small scroll amount value.
     */
    public static final int ImageBoxSmallScrollAmount = 7471892;

    /**
     * The image box large scroll type value.
     */
    public static final int ImageBoxLargeScrollType = 7471894;

    /**
     * The image box large scroll amount value.
     */
    public static final int ImageBoxLargeScrollAmount = 7471896;

    /**
     * The image box overlap priority value.
     */
    public static final int ImageBoxOverlapPriority = 7471904;

    /**
     * The cine relative to real time value.
     */
    public static final int CineRelativeToRealTime = 7471920;

    /**
     * The filter operations sequence value.
     */
    public static final int FilterOperationsSequence = 7472128;

    /**
     * The filter by category value.
     */
    public static final int FilterByCategory = 7472130;

    /**
     * The filter by attribute presence value.
     */
    public static final int FilterByAttributePresence = 7472132;

    /**
     * The filter by operator value.
     */
    public static final int FilterByOperator = 7472134;

    /**
     * The structured display background cie lab value value.
     */
    public static final int StructuredDisplayBackgroundCIELabValue = 7472160;

    /**
     * The empty image box cie lab value value.
     */
    public static final int EmptyImageBoxCIELabValue = 7472161;

    /**
     * The structured display image box sequence value.
     */
    public static final int StructuredDisplayImageBoxSequence = 7472162;

    /**
     * The structured display text box sequence value.
     */
    public static final int StructuredDisplayTextBoxSequence = 7472164;

    /**
     * The referenced first frame sequence value.
     */
    public static final int ReferencedFirstFrameSequence = 7472167;

    /**
     * The image box synchronization sequence value.
     */
    public static final int ImageBoxSynchronizationSequence = 7472176;

    /**
     * The synchronized image box list value.
     */
    public static final int SynchronizedImageBoxList = 7472178;

    /**
     * The type of synchronization value.
     */
    public static final int TypeOfSynchronization = 7472180;

    /**
     * The blending operation type value.
     */
    public static final int BlendingOperationType = 7472384;

    /**
     * The reformatting operation type value.
     */
    public static final int ReformattingOperationType = 7472400;

    /**
     * The reformatting thickness value.
     */
    public static final int ReformattingThickness = 7472402;

    /**
     * The reformatting interval value.
     */
    public static final int ReformattingInterval = 7472404;

    /**
     * The reformatting operation initial view direction value.
     */
    public static final int ReformattingOperationInitialViewDirection = 7472406;

    /**
     * The three d rendering type value.
     */
    public static final int ThreeDRenderingType = 7472416;

    /**
     * The sorting operations sequence value.
     */
    public static final int SortingOperationsSequence = 7472640;

    /**
     * The sort by category value.
     */
    public static final int SortByCategory = 7472642;

    /**
     * The sorting direction value.
     */
    public static final int SortingDirection = 7472644;

    /**
     * The display set patient orientation value.
     */
    public static final int DisplaySetPatientOrientation = 7472896;

    /**
     * The voi type value.
     */
    public static final int VOIType = 7472898;

    /**
     * The pseudo color type value.
     */
    public static final int PseudoColorType = 7472900;

    /**
     * The pseudo color palette instance reference sequence value.
     */
    public static final int PseudoColorPaletteInstanceReferenceSequence = 7472901;

    /**
     * The show grayscale inverted value.
     */
    public static final int ShowGrayscaleInverted = 7472902;

    /**
     * The show image true size flag value.
     */
    public static final int ShowImageTrueSizeFlag = 7472912;

    /**
     * The show graphic annotation flag value.
     */
    public static final int ShowGraphicAnnotationFlag = 7472914;

    /**
     * The show patient demographics flag value.
     */
    public static final int ShowPatientDemographicsFlag = 7472916;

    /**
     * The show acquisition techniques flag value.
     */
    public static final int ShowAcquisitionTechniquesFlag = 7472918;

    /**
     * The display set horizontal justification value.
     */
    public static final int DisplaySetHorizontalJustification = 7472919;

    /**
     * The display set vertical justification value.
     */
    public static final int DisplaySetVerticalJustification = 7472920;

    /**
     * The continuation start meterset value.
     */
    public static final int ContinuationStartMeterset = 7602464;

    /**
     * The continuation end meterset value.
     */
    public static final int ContinuationEndMeterset = 7602465;

    /**
     * The procedure step state value.
     */
    public static final int ProcedureStepState = 7606272;

    /**
     * The procedure step progress information sequence value.
     */
    public static final int ProcedureStepProgressInformationSequence = 7606274;

    /**
     * The procedure step progress value.
     */
    public static final int ProcedureStepProgress = 7606276;

    /**
     * The procedure step progress description value.
     */
    public static final int ProcedureStepProgressDescription = 7606278;

    /**
     * The procedure step progress parameters sequence value.
     */
    public static final int ProcedureStepProgressParametersSequence = 7606279;

    /**
     * The procedure step communications uri sequence value.
     */
    public static final int ProcedureStepCommunicationsURISequence = 7606280;

    /**
     * The contact uri value.
     */
    public static final int ContactURI = 7606282;

    /**
     * The contact display name value.
     */
    public static final int ContactDisplayName = 7606284;

    /**
     * The procedure step discontinuation reason code sequence value.
     */
    public static final int ProcedureStepDiscontinuationReasonCodeSequence = 7606286;

    /**
     * The beam task sequence value.
     */
    public static final int BeamTaskSequence = 7606304;

    /**
     * The beam task type value.
     */
    public static final int BeamTaskType = 7606306;

    /**
     * The beam order index trial value.
     */
    public static final int BeamOrderIndexTrial = 7606308;

    /**
     * The autosequence flag value.
     */
    public static final int AutosequenceFlag = 7606309;

    /**
     * The table top vertical adjusted position value.
     */
    public static final int TableTopVerticalAdjustedPosition = 7606310;

    /**
     * The table top longitudinal adjusted position value.
     */
    public static final int TableTopLongitudinalAdjustedPosition = 7606311;

    /**
     * The table top lateral adjusted position value.
     */
    public static final int TableTopLateralAdjustedPosition = 7606312;

    /**
     * The patient support adjusted angle value.
     */
    public static final int PatientSupportAdjustedAngle = 7606314;

    /**
     * The table top eccentric adjusted angle value.
     */
    public static final int TableTopEccentricAdjustedAngle = 7606315;

    /**
     * The table top pitch adjusted angle value.
     */
    public static final int TableTopPitchAdjustedAngle = 7606316;

    /**
     * The table top roll adjusted angle value.
     */
    public static final int TableTopRollAdjustedAngle = 7606317;

    /**
     * The delivery verification image sequence value.
     */
    public static final int DeliveryVerificationImageSequence = 7606320;

    /**
     * The verification image timing value.
     */
    public static final int VerificationImageTiming = 7606322;

    /**
     * The double exposure flag value.
     */
    public static final int DoubleExposureFlag = 7606324;

    /**
     * The double exposure ordering value.
     */
    public static final int DoubleExposureOrdering = 7606326;

    /**
     * The double exposure meterset trial value.
     */
    public static final int DoubleExposureMetersetTrial = 7606328;

    /**
     * The double exposure field delta trial value.
     */
    public static final int DoubleExposureFieldDeltaTrial = 7606330;

    /**
     * The related reference rt image sequence value.
     */
    public static final int RelatedReferenceRTImageSequence = 7606336;

    /**
     * The general machine verification sequence value.
     */
    public static final int GeneralMachineVerificationSequence = 7606338;

    /**
     * The conventional machine verification sequence value.
     */
    public static final int ConventionalMachineVerificationSequence = 7606340;

    /**
     * The ion machine verification sequence value.
     */
    public static final int IonMachineVerificationSequence = 7606342;

    /**
     * The failed attributes sequence value.
     */
    public static final int FailedAttributesSequence = 7606344;

    /**
     * The overridden attributes sequence value.
     */
    public static final int OverriddenAttributesSequence = 7606346;

    /**
     * The conventional control point verification sequence value.
     */
    public static final int ConventionalControlPointVerificationSequence = 7606348;

    /**
     * The ion control point verification sequence value.
     */
    public static final int IonControlPointVerificationSequence = 7606350;

    /**
     * The attribute occurrence sequence value.
     */
    public static final int AttributeOccurrenceSequence = 7606352;

    /**
     * The attribute occurrence pointer value.
     */
    public static final int AttributeOccurrencePointer = 7606354;

    /**
     * The attribute item selector value.
     */
    public static final int AttributeItemSelector = 7606356;

    /**
     * The attribute occurrence private creator value.
     */
    public static final int AttributeOccurrencePrivateCreator = 7606358;

    /**
     * The selector sequence pointer items value.
     */
    public static final int SelectorSequencePointerItems = 7606359;

    /**
     * The scheduled procedure step priority value.
     */
    public static final int ScheduledProcedureStepPriority = 7606784;

    /**
     * The worklist label value.
     */
    public static final int WorklistLabel = 7606786;

    /**
     * The procedure step label value.
     */
    public static final int ProcedureStepLabel = 7606788;

    /**
     * The scheduled processing parameters sequence value.
     */
    public static final int ScheduledProcessingParametersSequence = 7606800;

    /**
     * The performed processing parameters sequence value.
     */
    public static final int PerformedProcessingParametersSequence = 7606802;

    /**
     * The unified procedure step performed procedure sequence value.
     */
    public static final int UnifiedProcedureStepPerformedProcedureSequence = 7606806;

    /**
     * The related procedure step sequence value.
     */
    public static final int RelatedProcedureStepSequence = 7606816;

    /**
     * The procedure step relationship type value.
     */
    public static final int ProcedureStepRelationshipType = 7606818;

    /**
     * The replaced procedure step sequence value.
     */
    public static final int ReplacedProcedureStepSequence = 7606820;

    /**
     * The deletion lock value.
     */
    public static final int DeletionLock = 7606832;

    /**
     * The receiving ae value.
     */
    public static final int ReceivingAE = 7606836;

    /**
     * The requesting ae value.
     */
    public static final int RequestingAE = 7606838;

    /**
     * The reason for cancellation value.
     */
    public static final int ReasonForCancellation = 7606840;

    /**
     * The scp status value.
     */
    public static final int SCPStatus = 7606850;

    /**
     * The subscription list status value.
     */
    public static final int SubscriptionListStatus = 7606852;

    /**
     * The unified procedure step list status value.
     */
    public static final int UnifiedProcedureStepListStatus = 7606854;

    /**
     * The beam order index value.
     */
    public static final int BeamOrderIndex = 7607076;

    /**
     * The double exposure meterset value.
     */
    public static final int DoubleExposureMeterset = 7607096;

    /**
     * The double exposure field delta value.
     */
    public static final int DoubleExposureFieldDelta = 7607098;

    /**
     * The brachy task sequence value.
     */
    public static final int BrachyTaskSequence = 7607297;

    /**
     * The continuation start total reference air kerma value.
     */
    public static final int ContinuationStartTotalReferenceAirKerma = 7607298;

    /**
     * The continuation end total reference air kerma value.
     */
    public static final int ContinuationEndTotalReferenceAirKerma = 7607299;

    /**
     * The continuation pulse number value.
     */
    public static final int ContinuationPulseNumber = 7607300;

    /**
     * The channel delivery order sequence value.
     */
    public static final int ChannelDeliveryOrderSequence = 7607301;

    /**
     * The referenced channel number value.
     */
    public static final int ReferencedChannelNumber = 7607302;

    /**
     * The start cumulative time weight value.
     */
    public static final int StartCumulativeTimeWeight = 7607303;

    /**
     * The end cumulative time weight value.
     */
    public static final int EndCumulativeTimeWeight = 7607304;

    /**
     * The omitted channel sequence value.
     */
    public static final int OmittedChannelSequence = 7607305;

    /**
     * The reason for channel omission value.
     */
    public static final int ReasonForChannelOmission = 7607306;

    /**
     * The reason for channel omission description value.
     */
    public static final int ReasonForChannelOmissionDescription = 7607307;

    /**
     * The channel delivery order index value.
     */
    public static final int ChannelDeliveryOrderIndex = 7607308;

    /**
     * The channel delivery continuation sequence value.
     */
    public static final int ChannelDeliveryContinuationSequence = 7607309;

    /**
     * The omitted application setup sequence value.
     */
    public static final int OmittedApplicationSetupSequence = 7607310;

    /**
     * The implant assembly template name value.
     */
    public static final int ImplantAssemblyTemplateName = 7733249;

    /**
     * The implant assembly template issuer value.
     */
    public static final int ImplantAssemblyTemplateIssuer = 7733251;

    /**
     * The implant assembly template version value.
     */
    public static final int ImplantAssemblyTemplateVersion = 7733254;

    /**
     * The replaced implant assembly template sequence value.
     */
    public static final int ReplacedImplantAssemblyTemplateSequence = 7733256;

    /**
     * The implant assembly template type value.
     */
    public static final int ImplantAssemblyTemplateType = 7733258;

    /**
     * The original implant assembly template sequence value.
     */
    public static final int OriginalImplantAssemblyTemplateSequence = 7733260;

    /**
     * The derivation implant assembly template sequence value.
     */
    public static final int DerivationImplantAssemblyTemplateSequence = 7733262;

    /**
     * The implant assembly template target anatomy sequence value.
     */
    public static final int ImplantAssemblyTemplateTargetAnatomySequence = 7733264;

    /**
     * The procedure type code sequence value.
     */
    public static final int ProcedureTypeCodeSequence = 7733280;

    /**
     * The surgical technique value.
     */
    public static final int SurgicalTechnique = 7733296;

    /**
     * The component types sequence value.
     */
    public static final int ComponentTypesSequence = 7733298;

    /**
     * The component type code sequence value.
     */
    public static final int ComponentTypeCodeSequence = 7733300;

    /**
     * The exclusive component type value.
     */
    public static final int ExclusiveComponentType = 7733302;

    /**
     * The mandatory component type value.
     */
    public static final int MandatoryComponentType = 7733304;

    /**
     * The component sequence value.
     */
    public static final int ComponentSequence = 7733312;

    /**
     * The component id value.
     */
    public static final int ComponentID = 7733333;

    /**
     * The component assembly sequence value.
     */
    public static final int ComponentAssemblySequence = 7733344;

    /**
     * The component1 referenced id value.
     */
    public static final int Component1ReferencedID = 7733360;

    /**
     * The component1 referenced mating feature set id value.
     */
    public static final int Component1ReferencedMatingFeatureSetID = 7733376;

    /**
     * The component1 referenced mating feature id value.
     */
    public static final int Component1ReferencedMatingFeatureID = 7733392;

    /**
     * The component2 referenced id value.
     */
    public static final int Component2ReferencedID = 7733408;

    /**
     * The component2 referenced mating feature set id value.
     */
    public static final int Component2ReferencedMatingFeatureSetID = 7733424;

    /**
     * The component2 referenced mating feature id value.
     */
    public static final int Component2ReferencedMatingFeatureID = 7733440;

    /**
     * The implant template group name value.
     */
    public static final int ImplantTemplateGroupName = 7864321;

    /**
     * The implant template group description value.
     */
    public static final int ImplantTemplateGroupDescription = 7864336;

    /**
     * The implant template group issuer value.
     */
    public static final int ImplantTemplateGroupIssuer = 7864352;

    /**
     * The implant template group version value.
     */
    public static final int ImplantTemplateGroupVersion = 7864356;

    /**
     * The replaced implant template group sequence value.
     */
    public static final int ReplacedImplantTemplateGroupSequence = 7864358;

    /**
     * The implant template group target anatomy sequence value.
     */
    public static final int ImplantTemplateGroupTargetAnatomySequence = 7864360;

    /**
     * The implant template group members sequence value.
     */
    public static final int ImplantTemplateGroupMembersSequence = 7864362;

    /**
     * The implant template group member id value.
     */
    public static final int ImplantTemplateGroupMemberID = 7864366;

    /**
     * The three d implant template group member matching point value.
     */
    public static final int ThreeDImplantTemplateGroupMemberMatchingPoint = 7864400;

    /**
     * The three d implant template group member matching axes value.
     */
    public static final int ThreeDImplantTemplateGroupMemberMatchingAxes = 7864416;

    /**
     * The implant template group member matching2 d coordinates sequence value.
     */
    public static final int ImplantTemplateGroupMemberMatching2DCoordinatesSequence = 7864432;

    /**
     * The two d implant template group member matching point value.
     */
    public static final int TwoDImplantTemplateGroupMemberMatchingPoint = 7864464;

    /**
     * The two d implant template group member matching axes value.
     */
    public static final int TwoDImplantTemplateGroupMemberMatchingAxes = 7864480;

    /**
     * The implant template group variation dimension sequence value.
     */
    public static final int ImplantTemplateGroupVariationDimensionSequence = 7864496;

    /**
     * The implant template group variation dimension name value.
     */
    public static final int ImplantTemplateGroupVariationDimensionName = 7864498;

    /**
     * The implant template group variation dimension rank sequence value.
     */
    public static final int ImplantTemplateGroupVariationDimensionRankSequence = 7864500;

    /**
     * The referenced implant template group member id value.
     */
    public static final int ReferencedImplantTemplateGroupMemberID = 7864502;

    /**
     * The implant template group variation dimension rank value.
     */
    public static final int ImplantTemplateGroupVariationDimensionRank = 7864504;

    /**
     * The surface scan acquisition type code sequence value.
     */
    public static final int SurfaceScanAcquisitionTypeCodeSequence = 8388609;

    /**
     * The surface scan mode code sequence value.
     */
    public static final int SurfaceScanModeCodeSequence = 8388610;

    /**
     * The registration method code sequence value.
     */
    public static final int RegistrationMethodCodeSequence = 8388611;

    /**
     * The shot duration time value.
     */
    public static final int ShotDurationTime = 8388612;

    /**
     * The shot offset time value.
     */
    public static final int ShotOffsetTime = 8388613;

    /**
     * The surface point presentation value data value.
     */
    public static final int SurfacePointPresentationValueData = 8388614;

    /**
     * The surface point color cie lab value data value.
     */
    public static final int SurfacePointColorCIELabValueData = 8388615;

    /**
     * The uv mapping sequence value.
     */
    public static final int UVMappingSequence = 8388616;

    /**
     * The texture label value.
     */
    public static final int TextureLabel = 8388617;

    /**
     * The u value data value.
     */
    public static final int UValueData = 8388624;

    /**
     * The v value data value.
     */
    public static final int VValueData = 8388625;

    /**
     * The referenced texture sequence value.
     */
    public static final int ReferencedTextureSequence = 8388626;

    /**
     * The referenced surface data sequence value.
     */
    public static final int ReferencedSurfaceDataSequence = 8388627;

    /**
     * The assessment summary value.
     */
    public static final int AssessmentSummary = 8519681;

    /**
     * The assessment summary description value.
     */
    public static final int AssessmentSummaryDescription = 8519683;

    /**
     * The assessed sop instance sequence value.
     */
    public static final int AssessedSOPInstanceSequence = 8519684;

    /**
     * The referenced comparison sop instance sequence value.
     */
    public static final int ReferencedComparisonSOPInstanceSequence = 8519685;

    /**
     * The number of assessment observations value.
     */
    public static final int NumberOfAssessmentObservations = 8519686;

    /**
     * The assessment observations sequence value.
     */
    public static final int AssessmentObservationsSequence = 8519687;

    /**
     * The observation significance value.
     */
    public static final int ObservationSignificance = 8519688;

    /**
     * The observation description value.
     */
    public static final int ObservationDescription = 8519690;

    /**
     * The structured constraint observation sequence value.
     */
    public static final int StructuredConstraintObservationSequence = 8519692;

    /**
     * The assessed attribute value sequence value.
     */
    public static final int AssessedAttributeValueSequence = 8519696;

    /**
     * The assessment set id value.
     */
    public static final int AssessmentSetID = 8519702;

    /**
     * The assessment requester sequence value.
     */
    public static final int AssessmentRequesterSequence = 8519703;

    /**
     * The selector attribute name value.
     */
    public static final int SelectorAttributeName = 8519704;

    /**
     * The selector attribute keyword value.
     */
    public static final int SelectorAttributeKeyword = 8519705;

    /**
     * The assessment type code sequence value.
     */
    public static final int AssessmentTypeCodeSequence = 8519713;

    /**
     * The observation basis code sequence value.
     */
    public static final int ObservationBasisCodeSequence = 8519714;

    /**
     * The assessment label value.
     */
    public static final int AssessmentLabel = 8519715;

    /**
     * The constraint type value.
     */
    public static final int ConstraintType = 8519730;

    /**
     * The specification selection guidance value.
     */
    public static final int SpecificationSelectionGuidance = 8519731;

    /**
     * The constraint value sequence value.
     */
    public static final int ConstraintValueSequence = 8519732;

    /**
     * The recommended default value sequence value.
     */
    public static final int RecommendedDefaultValueSequence = 8519733;

    /**
     * The constraint violation significance value.
     */
    public static final int ConstraintViolationSignificance = 8519734;

    /**
     * The constraint violation condition value.
     */
    public static final int ConstraintViolationCondition = 8519735;

    /**
     * The modifiable constraint flag value.
     */
    public static final int ModifiableConstraintFlag = 8519736;

    /**
     * The storage media file set id value.
     */
    public static final int StorageMediaFileSetID = 8913200;

    /**
     * The storage media file set uid value.
     */
    public static final int StorageMediaFileSetUID = 8913216;

    /**
     * The icon image sequence value.
     */
    public static final int IconImageSequence = 8913408;

    /**
     * The topic title value.
     */
    public static final int TopicTitle = 8915204;

    /**
     * The topic subject value.
     */
    public static final int TopicSubject = 8915206;

    /**
     * The topic author value.
     */
    public static final int TopicAuthor = 8915216;

    /**
     * The topic keywords value.
     */
    public static final int TopicKeywords = 8915218;

    /**
     * The sop instance status value.
     */
    public static final int SOPInstanceStatus = 16778256;

    /**
     * The sop authorization date time value.
     */
    public static final int SOPAuthorizationDateTime = 16778272;

    /**
     * The sop authorization comment value.
     */
    public static final int SOPAuthorizationComment = 16778276;

    /**
     * The authorization equipment certification number value.
     */
    public static final int AuthorizationEquipmentCertificationNumber = 16778278;

    /**
     * The macid number value.
     */
    public static final int MACIDNumber = 67108869;

    /**
     * The mac calculation transfer syntax uid value.
     */
    public static final int MACCalculationTransferSyntaxUID = 67108880;

    /**
     * The mac algorithm value.
     */
    public static final int MACAlgorithm = 67108885;

    /**
     * The data elements signed value.
     */
    public static final int DataElementsSigned = 67108896;

    /**
     * The digital signature uid value.
     */
    public static final int DigitalSignatureUID = 67109120;

    /**
     * The digital signature date time value.
     */
    public static final int DigitalSignatureDateTime = 67109125;

    /**
     * The certificate type value.
     */
    public static final int CertificateType = 67109136;

    /**
     * The certificate of signer value.
     */
    public static final int CertificateOfSigner = 67109141;

    /**
     * The signature value.
     */
    public static final int Signature = 67109152;

    /**
     * The certified timestamp type value.
     */
    public static final int CertifiedTimestampType = 67109637;

    /**
     * The certified timestamp value.
     */
    public static final int CertifiedTimestamp = 67109648;

    /**
     * The digital signature purpose code sequence value.
     */
    public static final int DigitalSignaturePurposeCodeSequence = 67109889;

    /**
     * The referenced digital signature sequence value.
     */
    public static final int ReferencedDigitalSignatureSequence = 67109890;

    /**
     * The referenced sop instance mac sequence value.
     */
    public static final int ReferencedSOPInstanceMACSequence = 67109891;

    /**
     * The mac value.
     */
    public static final int MAC = 67109892;

    /**
     * The encrypted attributes sequence value.
     */
    public static final int EncryptedAttributesSequence = 67110144;

    /**
     * The encrypted content transfer syntax uid value.
     */
    public static final int EncryptedContentTransferSyntaxUID = 67110160;

    /**
     * The encrypted content value.
     */
    public static final int EncryptedContent = 67110176;

    /**
     * The modified attributes sequence value.
     */
    public static final int ModifiedAttributesSequence = 67110224;

    /**
     * The nonconforming modified attributes sequence value.
     */
    public static final int NonconformingModifiedAttributesSequence = 67110225;

    /**
     * The nonconforming data element value value.
     */
    public static final int NonconformingDataElementValue = 67110226;

    /**
     * The original attributes sequence value.
     */
    public static final int OriginalAttributesSequence = 67110241;

    /**
     * The attribute modification date time value.
     */
    public static final int AttributeModificationDateTime = 67110242;

    /**
     * The modifying system value.
     */
    public static final int ModifyingSystem = 67110243;

    /**
     * The source of previous values value.
     */
    public static final int SourceOfPreviousValues = 67110244;

    /**
     * The reason for the attribute modification value.
     */
    public static final int ReasonForTheAttributeModification = 67110245;

    /**
     * The instance origin status value.
     */
    public static final int InstanceOriginStatus = 67110400;

    /**
     * The escape triplet value.
     */
    public static final int EscapeTriplet = 268435456;

    /**
     * The run length triplet value.
     */
    public static final int RunLengthTriplet = 268435457;

    /**
     * The huffman table size value.
     */
    public static final int HuffmanTableSize = 268435458;

    /**
     * The huffman table triplet value.
     */
    public static final int HuffmanTableTriplet = 268435459;

    /**
     * The shift table size value.
     */
    public static final int ShiftTableSize = 268435460;

    /**
     * The shift table triplet value.
     */
    public static final int ShiftTableTriplet = 268435461;

    /**
     * The zonal map value.
     */
    public static final int ZonalMap = 269484032;

    /**
     * The number of copies value.
     */
    public static final int NumberOfCopies = 536870928;

    /**
     * The printer configuration sequence value.
     */
    public static final int PrinterConfigurationSequence = 536870942;

    /**
     * The print priority value.
     */
    public static final int PrintPriority = 536870944;

    /**
     * The medium type value.
     */
    public static final int MediumType = 536870960;

    /**
     * The film destination value.
     */
    public static final int FilmDestination = 536870976;

    /**
     * The film session label value.
     */
    public static final int FilmSessionLabel = 536870992;

    /**
     * The memory allocation value.
     */
    public static final int MemoryAllocation = 536871008;

    /**
     * The maximum memory allocation value.
     */
    public static final int MaximumMemoryAllocation = 536871009;

    /**
     * The color image printing flag value.
     */
    public static final int ColorImagePrintingFlag = 536871010;

    /**
     * The collation flag value.
     */
    public static final int CollationFlag = 536871011;

    /**
     * The annotation flag value.
     */
    public static final int AnnotationFlag = 536871013;

    /**
     * The image overlay flag value.
     */
    public static final int ImageOverlayFlag = 536871015;

    /**
     * The presentation lut flag value.
     */
    public static final int PresentationLUTFlag = 536871017;

    /**
     * The image box presentation lut flag value.
     */
    public static final int ImageBoxPresentationLUTFlag = 536871018;

    /**
     * The memory bit depth value.
     */
    public static final int MemoryBitDepth = 536871072;

    /**
     * The printing bit depth value.
     */
    public static final int PrintingBitDepth = 536871073;

    /**
     * The media installed sequence value.
     */
    public static final int MediaInstalledSequence = 536871074;

    /**
     * The other media available sequence value.
     */
    public static final int OtherMediaAvailableSequence = 536871076;

    /**
     * The supported image display formats sequence value.
     */
    public static final int SupportedImageDisplayFormatsSequence = 536871080;

    /**
     * The referenced film box sequence value.
     */
    public static final int ReferencedFilmBoxSequence = 536872192;

    /**
     * The referenced stored print sequence value.
     */
    public static final int ReferencedStoredPrintSequence = 536872208;

    /**
     * The image display format value.
     */
    public static final int ImageDisplayFormat = 537919504;

    /**
     * The annotation display format id value.
     */
    public static final int AnnotationDisplayFormatID = 537919536;

    /**
     * The film orientation value.
     */
    public static final int FilmOrientation = 537919552;

    /**
     * The film size id value.
     */
    public static final int FilmSizeID = 537919568;

    /**
     * The printer resolution id value.
     */
    public static final int PrinterResolutionID = 537919570;

    /**
     * The default printer resolution id value.
     */
    public static final int DefaultPrinterResolutionID = 537919572;

    /**
     * The magnification type value.
     */
    public static final int MagnificationType = 537919584;

    /**
     * The smoothing type value.
     */
    public static final int SmoothingType = 537919616;

    /**
     * The default magnification type value.
     */
    public static final int DefaultMagnificationType = 537919654;

    /**
     * The other magnification types available value.
     */
    public static final int OtherMagnificationTypesAvailable = 537919655;

    /**
     * The default smoothing type value.
     */
    public static final int DefaultSmoothingType = 537919656;

    /**
     * The other smoothing types available value.
     */
    public static final int OtherSmoothingTypesAvailable = 537919657;

    /**
     * The border density value.
     */
    public static final int BorderDensity = 537919744;

    /**
     * The empty image density value.
     */
    public static final int EmptyImageDensity = 537919760;

    /**
     * The min density value.
     */
    public static final int MinDensity = 537919776;

    /**
     * The max density value.
     */
    public static final int MaxDensity = 537919792;

    /**
     * The trim value.
     */
    public static final int Trim = 537919808;

    /**
     * The configuration information value.
     */
    public static final int ConfigurationInformation = 537919824;

    /**
     * The configuration information description value.
     */
    public static final int ConfigurationInformationDescription = 537919826;

    /**
     * The maximum collated films value.
     */
    public static final int MaximumCollatedFilms = 537919828;

    /**
     * The illumination value.
     */
    public static final int Illumination = 537919838;

    /**
     * The reflected ambient light value.
     */
    public static final int ReflectedAmbientLight = 537919840;

    /**
     * The printer pixel spacing value.
     */
    public static final int PrinterPixelSpacing = 537920374;

    /**
     * The referenced film session sequence value.
     */
    public static final int ReferencedFilmSessionSequence = 537920768;

    /**
     * The referenced image box sequence value.
     */
    public static final int ReferencedImageBoxSequence = 537920784;

    /**
     * The referenced basic annotation box sequence value.
     */
    public static final int ReferencedBasicAnnotationBoxSequence = 537920800;

    /**
     * The image box position value.
     */
    public static final int ImageBoxPosition = 538968080;

    /**
     * The polarity value.
     */
    public static final int Polarity = 538968096;

    /**
     * The requested image size value.
     */
    public static final int RequestedImageSize = 538968112;

    /**
     * The requested decimate crop behavior value.
     */
    public static final int RequestedDecimateCropBehavior = 538968128;

    /**
     * The requested resolution id value.
     */
    public static final int RequestedResolutionID = 538968144;

    /**
     * The requested image size flag value.
     */
    public static final int RequestedImageSizeFlag = 538968224;

    /**
     * The decimate crop result value.
     */
    public static final int DecimateCropResult = 538968226;

    /**
     * The basic grayscale image sequence value.
     */
    public static final int BasicGrayscaleImageSequence = 538968336;

    /**
     * The basic color image sequence value.
     */
    public static final int BasicColorImageSequence = 538968337;

    /**
     * The referenced image overlay box sequence value.
     */
    public static final int ReferencedImageOverlayBoxSequence = 538968368;

    /**
     * The referenced voilut box sequence value.
     */
    public static final int ReferencedVOILUTBoxSequence = 538968384;

    /**
     * The annotation position value.
     */
    public static final int AnnotationPosition = 540016656;

    /**
     * The text string value.
     */
    public static final int TextString = 540016672;

    /**
     * The referenced overlay plane sequence value.
     */
    public static final int ReferencedOverlayPlaneSequence = 541065232;

    /**
     * The referenced overlay plane groups value.
     */
    public static final int ReferencedOverlayPlaneGroups = 541065233;

    /**
     * The overlay pixel data sequence value.
     */
    public static final int OverlayPixelDataSequence = 541065248;

    /**
     * The overlay magnification type value.
     */
    public static final int OverlayMagnificationType = 541065312;

    /**
     * The overlay smoothing type value.
     */
    public static final int OverlaySmoothingType = 541065328;

    /**
     * The overlay or image magnification value.
     */
    public static final int OverlayOrImageMagnification = 541065330;

    /**
     * The magnify to number of columns value.
     */
    public static final int MagnifyToNumberOfColumns = 541065332;

    /**
     * The overlay foreground density value.
     */
    public static final int OverlayForegroundDensity = 541065344;

    /**
     * The overlay background density value.
     */
    public static final int OverlayBackgroundDensity = 541065346;

    /**
     * The overlay mode value.
     */
    public static final int OverlayMode = 541065360;

    /**
     * The threshold density value.
     */
    public static final int ThresholdDensity = 541065472;

    /**
     * The referenced image box sequence retired value.
     */
    public static final int ReferencedImageBoxSequenceRetired = 541066496;

    /**
     * The presentation lut sequence value.
     */
    public static final int PresentationLUTSequence = 542113808;

    /**
     * The presentation lut shape value.
     */
    public static final int PresentationLUTShape = 542113824;

    /**
     * The referenced presentation lut sequence value.
     */
    public static final int ReferencedPresentationLUTSequence = 542115072;

    /**
     * The print job id value.
     */
    public static final int PrintJobID = 553648144;

    /**
     * The execution status value.
     */
    public static final int ExecutionStatus = 553648160;

    /**
     * The execution status info value.
     */
    public static final int ExecutionStatusInfo = 553648176;

    /**
     * The creation date value.
     */
    public static final int CreationDate = 553648192;

    /**
     * The creation time value.
     */
    public static final int CreationTime = 553648208;

    /**
     * The originator value.
     */
    public static final int Originator = 553648240;

    /**
     * The destination ae value.
     */
    public static final int DestinationAE = 553648448;

    /**
     * The owner id value.
     */
    public static final int OwnerID = 553648480;

    /**
     * The number of films value.
     */
    public static final int NumberOfFilms = 553648496;

    /**
     * The referenced print job sequence pull stored print value.
     */
    public static final int ReferencedPrintJobSequencePullStoredPrint = 553649408;

    /**
     * The printer status value.
     */
    public static final int PrinterStatus = 554696720;

    /**
     * The printer status info value.
     */
    public static final int PrinterStatusInfo = 554696736;

    /**
     * The printer name value.
     */
    public static final int PrinterName = 554696752;

    /**
     * The print queue id value.
     */
    public static final int PrintQueueID = 554696857;

    /**
     * The queue status value.
     */
    public static final int QueueStatus = 555745296;

    /**
     * The print job description sequence value.
     */
    public static final int PrintJobDescriptionSequence = 555745360;

    /**
     * The referenced print job sequence value.
     */
    public static final int ReferencedPrintJobSequence = 555745392;

    /**
     * The print management capabilities sequence value.
     */
    public static final int PrintManagementCapabilitiesSequence = 556793872;

    /**
     * The printer characteristics sequence value.
     */
    public static final int PrinterCharacteristicsSequence = 556793877;

    /**
     * The film box content sequence value.
     */
    public static final int FilmBoxContentSequence = 556793904;

    /**
     * The image box content sequence value.
     */
    public static final int ImageBoxContentSequence = 556793920;

    /**
     * The annotation content sequence value.
     */
    public static final int AnnotationContentSequence = 556793936;

    /**
     * The image overlay box content sequence value.
     */
    public static final int ImageOverlayBoxContentSequence = 556793952;

    /**
     * The presentation lut content sequence value.
     */
    public static final int PresentationLUTContentSequence = 556793984;

    /**
     * The proposed study sequence value.
     */
    public static final int ProposedStudySequence = 556794016;

    /**
     * The original image sequence value.
     */
    public static final int OriginalImageSequence = 556794048;

    /**
     * The label using information extracted from instances value.
     */
    public static final int LabelUsingInformationExtractedFromInstances = 570425345;

    /**
     * The label text value.
     */
    public static final int LabelText = 570425346;

    /**
     * The label style selection value.
     */
    public static final int LabelStyleSelection = 570425347;

    /**
     * The media disposition value.
     */
    public static final int MediaDisposition = 570425348;

    /**
     * The barcode value value.
     */
    public static final int BarcodeValue = 570425349;

    /**
     * The barcode symbology value.
     */
    public static final int BarcodeSymbology = 570425350;

    /**
     * The allow media splitting value.
     */
    public static final int AllowMediaSplitting = 570425351;

    /**
     * The include non dicom objects value.
     */
    public static final int IncludeNonDICOMObjects = 570425352;

    /**
     * The include display application value.
     */
    public static final int IncludeDisplayApplication = 570425353;

    /**
     * The preserve composite instances after media creation value.
     */
    public static final int PreserveCompositeInstancesAfterMediaCreation = 570425354;

    /**
     * The total number of pieces of media created value.
     */
    public static final int TotalNumberOfPiecesOfMediaCreated = 570425355;

    /**
     * The requested media application profile value.
     */
    public static final int RequestedMediaApplicationProfile = 570425356;

    /**
     * The referenced storage media sequence value.
     */
    public static final int ReferencedStorageMediaSequence = 570425357;

    /**
     * The failure attributes value.
     */
    public static final int FailureAttributes = 570425358;

    /**
     * The allow lossy compression value.
     */
    public static final int AllowLossyCompression = 570425359;

    /**
     * The request priority value.
     */
    public static final int RequestPriority = 570425376;

    /**
     * The rt image label value.
     */
    public static final int RTImageLabel = 805437442;

    /**
     * The rt image name value.
     */
    public static final int RTImageName = 805437443;

    /**
     * The rt image description value.
     */
    public static final int RTImageDescription = 805437444;

    /**
     * The reported values origin value.
     */
    public static final int ReportedValuesOrigin = 805437450;

    /**
     * The rt image plane value.
     */
    public static final int RTImagePlane = 805437452;

    /**
     * The x ray image receptor translation value.
     */
    public static final int XRayImageReceptorTranslation = 805437453;

    /**
     * The x ray image receptor angle value.
     */
    public static final int XRayImageReceptorAngle = 805437454;

    /**
     * The rt image orientation value.
     */
    public static final int RTImageOrientation = 805437456;

    /**
     * The image plane pixel spacing value.
     */
    public static final int ImagePlanePixelSpacing = 805437457;

    /**
     * The rt image position value.
     */
    public static final int RTImagePosition = 805437458;

    /**
     * The radiation machine name value.
     */
    public static final int RadiationMachineName = 805437472;

    /**
     * The radiation machine sad value.
     */
    public static final int RadiationMachineSAD = 805437474;

    /**
     * The radiation machine ssd value.
     */
    public static final int RadiationMachineSSD = 805437476;

    /**
     * The rt image sid value.
     */
    public static final int RTImageSID = 805437478;

    /**
     * The source to reference object distance value.
     */
    public static final int SourceToReferenceObjectDistance = 805437480;

    /**
     * The fraction number value.
     */
    public static final int FractionNumber = 805437481;

    /**
     * The exposure sequence value.
     */
    public static final int ExposureSequence = 805437488;

    /**
     * The meterset exposure value.
     */
    public static final int MetersetExposure = 805437490;

    /**
     * The diaphragm position value.
     */
    public static final int DiaphragmPosition = 805437492;

    /**
     * The fluence map sequence value.
     */
    public static final int FluenceMapSequence = 805437504;

    /**
     * The fluence data source value.
     */
    public static final int FluenceDataSource = 805437505;

    /**
     * The fluence data scale value.
     */
    public static final int FluenceDataScale = 805437506;

    /**
     * The primary fluence mode sequence value.
     */
    public static final int PrimaryFluenceModeSequence = 805437520;

    /**
     * The fluence mode value.
     */
    public static final int FluenceMode = 805437521;

    /**
     * The fluence mode id value.
     */
    public static final int FluenceModeID = 805437522;

    /**
     * The selected frame number value.
     */
    public static final int SelectedFrameNumber = 805437696;

    /**
     * The selected frame functional groups sequence value.
     */
    public static final int SelectedFrameFunctionalGroupsSequence = 805437697;

    /**
     * The rt image frame general content sequence value.
     */
    public static final int RTImageFrameGeneralContentSequence = 805437698;

    /**
     * The rt image frame context sequence value.
     */
    public static final int RTImageFrameContextSequence = 805437699;

    /**
     * The rt image scope sequence value.
     */
    public static final int RTImageScopeSequence = 805437700;

    /**
     * The beam modifier coordinates presence flag value.
     */
    public static final int BeamModifierCoordinatesPresenceFlag = 805437701;

    /**
     * The start cumulative meterset value.
     */
    public static final int StartCumulativeMeterset = 805437702;

    /**
     * The stop cumulative meterset value.
     */
    public static final int StopCumulativeMeterset = 805437703;

    /**
     * The rt acquisition patient position sequence value.
     */
    public static final int RTAcquisitionPatientPositionSequence = 805437704;

    /**
     * The rt image frame imaging device position sequence value.
     */
    public static final int RTImageFrameImagingDevicePositionSequence = 805437705;

    /**
     * The rt image framek v radiation acquisition sequence value.
     */
    public static final int RTImageFramekVRadiationAcquisitionSequence = 805437706;

    /**
     * The rt image frame mv radiation acquisition sequence value.
     */
    public static final int RTImageFrameMVRadiationAcquisitionSequence = 805437707;

    /**
     * The rt image frame radiation acquisition sequence value.
     */
    public static final int RTImageFrameRadiationAcquisitionSequence = 805437708;

    /**
     * The imaging source position sequence value.
     */
    public static final int ImagingSourcePositionSequence = 805437709;

    /**
     * The image receptor position sequence value.
     */
    public static final int ImageReceptorPositionSequence = 805437710;

    /**
     * The device position to equipment mapping matrix value.
     */
    public static final int DevicePositionToEquipmentMappingMatrix = 805437711;

    /**
     * The device position parameter sequence value.
     */
    public static final int DevicePositionParameterSequence = 805437712;

    /**
     * The imaging source location specification type value.
     */
    public static final int ImagingSourceLocationSpecificationType = 805437713;

    /**
     * The imaging device location matrix sequence value.
     */
    public static final int ImagingDeviceLocationMatrixSequence = 805437714;

    /**
     * The imaging device location parameter sequence value.
     */
    public static final int ImagingDeviceLocationParameterSequence = 805437715;

    /**
     * The imaging aperture sequence value.
     */
    public static final int ImagingApertureSequence = 805437716;

    /**
     * The imaging aperture specification type value.
     */
    public static final int ImagingApertureSpecificationType = 805437717;

    /**
     * The number of acquisition devices value.
     */
    public static final int NumberOfAcquisitionDevices = 805437718;

    /**
     * The acquisition device sequence value.
     */
    public static final int AcquisitionDeviceSequence = 805437719;

    /**
     * The acquisition task sequence value.
     */
    public static final int AcquisitionTaskSequence = 805437720;

    /**
     * The acquisition task workitem code sequence value.
     */
    public static final int AcquisitionTaskWorkitemCodeSequence = 805437721;

    /**
     * The acquisition subtask sequence value.
     */
    public static final int AcquisitionSubtaskSequence = 805437722;

    /**
     * The subtask workitem code sequence value.
     */
    public static final int SubtaskWorkitemCodeSequence = 805437723;

    /**
     * The acquisition task index value.
     */
    public static final int AcquisitionTaskIndex = 805437724;

    /**
     * The acquisition subtask index value.
     */
    public static final int AcquisitionSubtaskIndex = 805437725;

    /**
     * The referenced baseline parameters rt radiation instance sequence value.
     */
    public static final int ReferencedBaselineParametersRTRadiationInstanceSequence = 805437726;

    /**
     * The position acquisition template identification sequence value.
     */
    public static final int PositionAcquisitionTemplateIdentificationSequence = 805437727;

    /**
     * The position acquisition template id value.
     */
    public static final int PositionAcquisitionTemplateID = 805437728;

    /**
     * The position acquisition template name value.
     */
    public static final int PositionAcquisitionTemplateName = 805437729;

    /**
     * The position acquisition template code sequence value.
     */
    public static final int PositionAcquisitionTemplateCodeSequence = 805437730;

    /**
     * The position acquisition template description value.
     */
    public static final int PositionAcquisitionTemplateDescription = 805437731;

    /**
     * The acquisition task applicability sequence value.
     */
    public static final int AcquisitionTaskApplicabilitySequence = 805437732;

    /**
     * The projection imaging acquisition parameter sequence value.
     */
    public static final int ProjectionImagingAcquisitionParameterSequence = 805437733;

    /**
     * The ct imaging acquisition parameter sequence value.
     */
    public static final int CTImagingAcquisitionParameterSequence = 805437734;

    /**
     * The kv imaging generation parameters sequence value.
     */
    public static final int KVImagingGenerationParametersSequence = 805437735;

    /**
     * The mv imaging generation parameters sequence value.
     */
    public static final int MVImagingGenerationParametersSequence = 805437736;

    /**
     * The acquisition signal type value.
     */
    public static final int AcquisitionSignalType = 805437737;

    /**
     * The acquisition method value.
     */
    public static final int AcquisitionMethod = 805437738;

    /**
     * The scan start position sequence value.
     */
    public static final int ScanStartPositionSequence = 805437739;

    /**
     * The scan stop position sequence value.
     */
    public static final int ScanStopPositionSequence = 805437740;

    /**
     * The imaging source to beam modifier definition plane distance value.
     */
    public static final int ImagingSourceToBeamModifierDefinitionPlaneDistance = 805437741;

    /**
     * The scan arc type value.
     */
    public static final int ScanArcType = 805437742;

    /**
     * The detector positioning type value.
     */
    public static final int DetectorPositioningType = 805437743;

    /**
     * The additional rt accessory device sequence value.
     */
    public static final int AdditionalRTAccessoryDeviceSequence = 805437744;

    /**
     * The device specific acquisition parameter sequence value.
     */
    public static final int DeviceSpecificAcquisitionParameterSequence = 805437745;

    /**
     * The referenced position reference instance sequence value.
     */
    public static final int ReferencedPositionReferenceInstanceSequence = 805437746;

    /**
     * The energy derivation code sequence value.
     */
    public static final int EnergyDerivationCodeSequence = 805437747;

    /**
     * The maximum cumulative meterset exposure value.
     */
    public static final int MaximumCumulativeMetersetExposure = 805437748;

    /**
     * The acquisition initiation sequence value.
     */
    public static final int AcquisitionInitiationSequence = 805437749;

    /**
     * The dvh type value.
     */
    public static final int DVHType = 805568513;

    /**
     * The dose units value.
     */
    public static final int DoseUnits = 805568514;

    /**
     * The dose type value.
     */
    public static final int DoseType = 805568516;

    /**
     * The spatial transform of dose value.
     */
    public static final int SpatialTransformOfDose = 805568517;

    /**
     * The dose comment value.
     */
    public static final int DoseComment = 805568518;

    /**
     * The normalization point value.
     */
    public static final int NormalizationPoint = 805568520;

    /**
     * The dose summation type value.
     */
    public static final int DoseSummationType = 805568522;

    /**
     * The grid frame offset vector value.
     */
    public static final int GridFrameOffsetVector = 805568524;

    /**
     * The dose grid scaling value.
     */
    public static final int DoseGridScaling = 805568526;

    /**
     * The rt dose roi sequence value.
     */
    public static final int RTDoseROISequence = 805568528;

    /**
     * The dose value value.
     */
    public static final int DoseValue = 805568530;

    /**
     * The tissue heterogeneity correction value.
     */
    public static final int TissueHeterogeneityCorrection = 805568532;

    /**
     * The dvh normalization point value.
     */
    public static final int DVHNormalizationPoint = 805568576;

    /**
     * The dvh normalization dose value value.
     */
    public static final int DVHNormalizationDoseValue = 805568578;

    /**
     * The dvh sequence value.
     */
    public static final int DVHSequence = 805568592;

    /**
     * The dvh dose scaling value.
     */
    public static final int DVHDoseScaling = 805568594;

    /**
     * The dvh volume units value.
     */
    public static final int DVHVolumeUnits = 805568596;

    /**
     * The dvh number of bins value.
     */
    public static final int DVHNumberOfBins = 805568598;

    /**
     * The dvh data value.
     */
    public static final int DVHData = 805568600;

    /**
     * The dvh referenced roi sequence value.
     */
    public static final int DVHReferencedROISequence = 805568608;

    /**
     * The dvhroi contribution type value.
     */
    public static final int DVHROIContributionType = 805568610;

    /**
     * The dvh minimum dose value.
     */
    public static final int DVHMinimumDose = 805568624;

    /**
     * The dvh maximum dose value.
     */
    public static final int DVHMaximumDose = 805568626;

    /**
     * The dvh mean dose value.
     */
    public static final int DVHMeanDose = 805568628;

    /**
     * The structure set label value.
     */
    public static final int StructureSetLabel = 805699586;

    /**
     * The structure set name value.
     */
    public static final int StructureSetName = 805699588;

    /**
     * The structure set description value.
     */
    public static final int StructureSetDescription = 805699590;

    /**
     * The structure set date value.
     */
    public static final int StructureSetDate = 805699592;

    /**
     * The structure set time value.
     */
    public static final int StructureSetTime = 805699593;

    /**
     * The referenced frame of reference sequence value.
     */
    public static final int ReferencedFrameOfReferenceSequence = 805699600;

    /**
     * The rt referenced study sequence value.
     */
    public static final int RTReferencedStudySequence = 805699602;

    /**
     * The rt referenced series sequence value.
     */
    public static final int RTReferencedSeriesSequence = 805699604;

    /**
     * The contour image sequence value.
     */
    public static final int ContourImageSequence = 805699606;

    /**
     * The predecessor structure set sequence value.
     */
    public static final int PredecessorStructureSetSequence = 805699608;

    /**
     * The structure set roi sequence value.
     */
    public static final int StructureSetROISequence = 805699616;

    /**
     * The roi number value.
     */
    public static final int ROINumber = 805699618;

    /**
     * The referenced frame of reference uid value.
     */
    public static final int ReferencedFrameOfReferenceUID = 805699620;

    /**
     * The roi name value.
     */
    public static final int ROIName = 805699622;

    /**
     * The roi description value.
     */
    public static final int ROIDescription = 805699624;

    /**
     * The roi display color value.
     */
    public static final int ROIDisplayColor = 805699626;

    /**
     * The roi volume value.
     */
    public static final int ROIVolume = 805699628;

    /**
     * The roi date time value.
     */
    public static final int ROIDateTime = 805699629;

    /**
     * The roi observation date time value.
     */
    public static final int ROIObservationDateTime = 805699630;

    /**
     * The rt related roi sequence value.
     */
    public static final int RTRelatedROISequence = 805699632;

    /**
     * The rtroi relationship value.
     */
    public static final int RTROIRelationship = 805699635;

    /**
     * The roi generation algorithm value.
     */
    public static final int ROIGenerationAlgorithm = 805699638;

    /**
     * The roi derivation algorithm identification sequence value.
     */
    public static final int ROIDerivationAlgorithmIdentificationSequence = 805699639;

    /**
     * The roi generation description value.
     */
    public static final int ROIGenerationDescription = 805699640;

    /**
     * The roi contour sequence value.
     */
    public static final int ROIContourSequence = 805699641;

    /**
     * The contour sequence value.
     */
    public static final int ContourSequence = 805699648;

    /**
     * The contour geometric type value.
     */
    public static final int ContourGeometricType = 805699650;

    /**
     * The contour slab thickness value.
     */
    public static final int ContourSlabThickness = 805699652;

    /**
     * The contour offset vector value.
     */
    public static final int ContourOffsetVector = 805699653;

    /**
     * The number of contour points value.
     */
    public static final int NumberOfContourPoints = 805699654;

    /**
     * The contour number value.
     */
    public static final int ContourNumber = 805699656;

    /**
     * The attached contours value.
     */
    public static final int AttachedContours = 805699657;

    /**
     * The source pixel planes characteristics sequence value.
     */
    public static final int SourcePixelPlanesCharacteristicsSequence = 805699658;

    /**
     * The source series sequence value.
     */
    public static final int SourceSeriesSequence = 805699659;

    /**
     * The source series information sequence value.
     */
    public static final int SourceSeriesInformationSequence = 805699660;

    /**
     * The roi creator sequence value.
     */
    public static final int ROICreatorSequence = 805699661;

    /**
     * The roi interpreter sequence value.
     */
    public static final int ROIInterpreterSequence = 805699662;

    /**
     * The roi observation context code sequence value.
     */
    public static final int ROIObservationContextCodeSequence = 805699663;

    /**
     * The contour data value.
     */
    public static final int ContourData = 805699664;

    /**
     * The rtroi observations sequence value.
     */
    public static final int RTROIObservationsSequence = 805699712;

    /**
     * The observation number value.
     */
    public static final int ObservationNumber = 805699714;

    /**
     * The referenced roi number value.
     */
    public static final int ReferencedROINumber = 805699716;

    /**
     * The roi observation label value.
     */
    public static final int ROIObservationLabel = 805699717;

    /**
     * The rtroi identification code sequence value.
     */
    public static final int RTROIIdentificationCodeSequence = 805699718;

    /**
     * The roi observation description value.
     */
    public static final int ROIObservationDescription = 805699720;

    /**
     * The related rtroi observations sequence value.
     */
    public static final int RelatedRTROIObservationsSequence = 805699744;

    /**
     * The rtroi interpreted type value.
     */
    public static final int RTROIInterpretedType = 805699748;

    /**
     * The roi interpreter value.
     */
    public static final int ROIInterpreter = 805699750;

    /**
     * The roi physical properties sequence value.
     */
    public static final int ROIPhysicalPropertiesSequence = 805699760;

    /**
     * The roi physical property value.
     */
    public static final int ROIPhysicalProperty = 805699762;

    /**
     * The roi physical property value value.
     */
    public static final int ROIPhysicalPropertyValue = 805699764;

    /**
     * The roi elemental composition sequence value.
     */
    public static final int ROIElementalCompositionSequence = 805699766;

    /**
     * The roi elemental composition atomic number value.
     */
    public static final int ROIElementalCompositionAtomicNumber = 805699767;

    /**
     * The roi elemental composition atomic mass fraction value.
     */
    public static final int ROIElementalCompositionAtomicMassFraction = 805699768;

    /**
     * The additional rtroi identification code sequence value.
     */
    public static final int AdditionalRTROIIdentificationCodeSequence = 805699769;

    /**
     * The frame of reference relationship sequence value.
     */
    public static final int FrameOfReferenceRelationshipSequence = 805699776;

    /**
     * The related frame of reference uid value.
     */
    public static final int RelatedFrameOfReferenceUID = 805699778;

    /**
     * The frame of reference transformation type value.
     */
    public static final int FrameOfReferenceTransformationType = 805699780;

    /**
     * The frame of reference transformation matrix value.
     */
    public static final int FrameOfReferenceTransformationMatrix = 805699782;

    /**
     * The frame of reference transformation comment value.
     */
    public static final int FrameOfReferenceTransformationComment = 805699784;

    /**
     * The patient location coordinates sequence value.
     */
    public static final int PatientLocationCoordinatesSequence = 805699785;

    /**
     * The patient location coordinates code sequence value.
     */
    public static final int PatientLocationCoordinatesCodeSequence = 805699786;

    /**
     * The patient support position sequence value.
     */
    public static final int PatientSupportPositionSequence = 805699787;

    /**
     * The measured dose reference sequence value.
     */
    public static final int MeasuredDoseReferenceSequence = 805830672;

    /**
     * The measured dose description value.
     */
    public static final int MeasuredDoseDescription = 805830674;

    /**
     * The measured dose type value.
     */
    public static final int MeasuredDoseType = 805830676;

    /**
     * The measured dose value value.
     */
    public static final int MeasuredDoseValue = 805830678;

    /**
     * The treatment session beam sequence value.
     */
    public static final int TreatmentSessionBeamSequence = 805830688;

    /**
     * The treatment session ion beam sequence value.
     */
    public static final int TreatmentSessionIonBeamSequence = 805830689;

    /**
     * The current fraction number value.
     */
    public static final int CurrentFractionNumber = 805830690;

    /**
     * The treatment control point date value.
     */
    public static final int TreatmentControlPointDate = 805830692;

    /**
     * The treatment control point time value.
     */
    public static final int TreatmentControlPointTime = 805830693;

    /**
     * The treatment termination status value.
     */
    public static final int TreatmentTerminationStatus = 805830698;

    /**
     * The treatment termination code value.
     */
    public static final int TreatmentTerminationCode = 805830699;

    /**
     * The treatment verification status value.
     */
    public static final int TreatmentVerificationStatus = 805830700;

    /**
     * The referenced treatment record sequence value.
     */
    public static final int ReferencedTreatmentRecordSequence = 805830704;

    /**
     * The specified primary meterset value.
     */
    public static final int SpecifiedPrimaryMeterset = 805830706;

    /**
     * The specified secondary meterset value.
     */
    public static final int SpecifiedSecondaryMeterset = 805830707;

    /**
     * The delivered primary meterset value.
     */
    public static final int DeliveredPrimaryMeterset = 805830710;

    /**
     * The delivered secondary meterset value.
     */
    public static final int DeliveredSecondaryMeterset = 805830711;

    /**
     * The specified treatment time value.
     */
    public static final int SpecifiedTreatmentTime = 805830714;

    /**
     * The delivered treatment time value.
     */
    public static final int DeliveredTreatmentTime = 805830715;

    /**
     * The control point delivery sequence value.
     */
    public static final int ControlPointDeliverySequence = 805830720;

    /**
     * The ion control point delivery sequence value.
     */
    public static final int IonControlPointDeliverySequence = 805830721;

    /**
     * The specified meterset value.
     */
    public static final int SpecifiedMeterset = 805830722;

    /**
     * The delivered meterset value.
     */
    public static final int DeliveredMeterset = 805830724;

    /**
     * The meterset rate set value.
     */
    public static final int MetersetRateSet = 805830725;

    /**
     * The meterset rate delivered value.
     */
    public static final int MetersetRateDelivered = 805830726;

    /**
     * The scan spot metersets delivered value.
     */
    public static final int ScanSpotMetersetsDelivered = 805830727;

    /**
     * The dose rate delivered value.
     */
    public static final int DoseRateDelivered = 805830728;

    /**
     * The treatment summary calculated dose reference sequence value.
     */
    public static final int TreatmentSummaryCalculatedDoseReferenceSequence = 805830736;

    /**
     * The cumulative dose to dose reference value.
     */
    public static final int CumulativeDoseToDoseReference = 805830738;

    /**
     * The first treatment date value.
     */
    public static final int FirstTreatmentDate = 805830740;

    /**
     * The most recent treatment date value.
     */
    public static final int MostRecentTreatmentDate = 805830742;

    /**
     * The number of fractions delivered value.
     */
    public static final int NumberOfFractionsDelivered = 805830746;

    /**
     * The override sequence value.
     */
    public static final int OverrideSequence = 805830752;

    /**
     * The parameter sequence pointer value.
     */
    public static final int ParameterSequencePointer = 805830753;

    /**
     * The override parameter pointer value.
     */
    public static final int OverrideParameterPointer = 805830754;

    /**
     * The parameter item index value.
     */
    public static final int ParameterItemIndex = 805830755;

    /**
     * The measured dose reference number value.
     */
    public static final int MeasuredDoseReferenceNumber = 805830756;

    /**
     * The parameter pointer value.
     */
    public static final int ParameterPointer = 805830757;

    /**
     * The override reason value.
     */
    public static final int OverrideReason = 805830758;

    /**
     * The parameter value number value.
     */
    public static final int ParameterValueNumber = 805830759;

    /**
     * The corrected parameter sequence value.
     */
    public static final int CorrectedParameterSequence = 805830760;

    /**
     * The correction value value.
     */
    public static final int CorrectionValue = 805830762;

    /**
     * The calculated dose reference sequence value.
     */
    public static final int CalculatedDoseReferenceSequence = 805830768;

    /**
     * The calculated dose reference number value.
     */
    public static final int CalculatedDoseReferenceNumber = 805830770;

    /**
     * The calculated dose reference description value.
     */
    public static final int CalculatedDoseReferenceDescription = 805830772;

    /**
     * The calculated dose reference dose value value.
     */
    public static final int CalculatedDoseReferenceDoseValue = 805830774;

    /**
     * The start meterset value.
     */
    public static final int StartMeterset = 805830776;

    /**
     * The end meterset value.
     */
    public static final int EndMeterset = 805830778;

    /**
     * The referenced measured dose reference sequence value.
     */
    public static final int ReferencedMeasuredDoseReferenceSequence = 805830784;

    /**
     * The referenced measured dose reference number value.
     */
    public static final int ReferencedMeasuredDoseReferenceNumber = 805830786;

    /**
     * The referenced calculated dose reference sequence value.
     */
    public static final int ReferencedCalculatedDoseReferenceSequence = 805830800;

    /**
     * The referenced calculated dose reference number value.
     */
    public static final int ReferencedCalculatedDoseReferenceNumber = 805830802;

    /**
     * The beam limiting device leaf pairs sequence value.
     */
    public static final int BeamLimitingDeviceLeafPairsSequence = 805830816;

    /**
     * The enhanced rt beam limiting device sequence value.
     */
    public static final int EnhancedRTBeamLimitingDeviceSequence = 805830817;

    /**
     * The enhanced rt beam limiting opening sequence value.
     */
    public static final int EnhancedRTBeamLimitingOpeningSequence = 805830818;

    /**
     * The enhanced rt beam limiting device definition flag value.
     */
    public static final int EnhancedRTBeamLimitingDeviceDefinitionFlag = 805830819;

    /**
     * The parallel rt beam delimiter opening extents value.
     */
    public static final int ParallelRTBeamDelimiterOpeningExtents = 805830820;

    /**
     * The recorded wedge sequence value.
     */
    public static final int RecordedWedgeSequence = 805830832;

    /**
     * The recorded compensator sequence value.
     */
    public static final int RecordedCompensatorSequence = 805830848;

    /**
     * The recorded block sequence value.
     */
    public static final int RecordedBlockSequence = 805830864;

    /**
     * The recorded block slab sequence value.
     */
    public static final int RecordedBlockSlabSequence = 805830865;

    /**
     * The treatment summary measured dose reference sequence value.
     */
    public static final int TreatmentSummaryMeasuredDoseReferenceSequence = 805830880;

    /**
     * The recorded snout sequence value.
     */
    public static final int RecordedSnoutSequence = 805830896;

    /**
     * The recorded range shifter sequence value.
     */
    public static final int RecordedRangeShifterSequence = 805830898;

    /**
     * The recorded lateral spreading device sequence value.
     */
    public static final int RecordedLateralSpreadingDeviceSequence = 805830900;

    /**
     * The recorded range modulator sequence value.
     */
    public static final int RecordedRangeModulatorSequence = 805830902;

    /**
     * The recorded source sequence value.
     */
    public static final int RecordedSourceSequence = 805830912;

    /**
     * The source serial number value.
     */
    public static final int SourceSerialNumber = 805830917;

    /**
     * The treatment session application setup sequence value.
     */
    public static final int TreatmentSessionApplicationSetupSequence = 805830928;

    /**
     * The application setup check value.
     */
    public static final int ApplicationSetupCheck = 805830934;

    /**
     * The recorded brachy accessory device sequence value.
     */
    public static final int RecordedBrachyAccessoryDeviceSequence = 805830944;

    /**
     * The referenced brachy accessory device number value.
     */
    public static final int ReferencedBrachyAccessoryDeviceNumber = 805830946;

    /**
     * The recorded channel sequence value.
     */
    public static final int RecordedChannelSequence = 805830960;

    /**
     * The specified channel total time value.
     */
    public static final int SpecifiedChannelTotalTime = 805830962;

    /**
     * The delivered channel total time value.
     */
    public static final int DeliveredChannelTotalTime = 805830964;

    /**
     * The specified number of pulses value.
     */
    public static final int SpecifiedNumberOfPulses = 805830966;

    /**
     * The delivered number of pulses value.
     */
    public static final int DeliveredNumberOfPulses = 805830968;

    /**
     * The specified pulse repetition interval value.
     */
    public static final int SpecifiedPulseRepetitionInterval = 805830970;

    /**
     * The delivered pulse repetition interval value.
     */
    public static final int DeliveredPulseRepetitionInterval = 805830972;

    /**
     * The recorded source applicator sequence value.
     */
    public static final int RecordedSourceApplicatorSequence = 805830976;

    /**
     * The referenced source applicator number value.
     */
    public static final int ReferencedSourceApplicatorNumber = 805830978;

    /**
     * The recorded channel shield sequence value.
     */
    public static final int RecordedChannelShieldSequence = 805830992;

    /**
     * The referenced channel shield number value.
     */
    public static final int ReferencedChannelShieldNumber = 805830994;

    /**
     * The brachy control point delivered sequence value.
     */
    public static final int BrachyControlPointDeliveredSequence = 805831008;

    /**
     * The safe position exit date value.
     */
    public static final int SafePositionExitDate = 805831010;

    /**
     * The safe position exit time value.
     */
    public static final int SafePositionExitTime = 805831012;

    /**
     * The safe position return date value.
     */
    public static final int SafePositionReturnDate = 805831014;

    /**
     * The safe position return time value.
     */
    public static final int SafePositionReturnTime = 805831016;

    /**
     * The pulse specific brachy control point delivered sequence value.
     */
    public static final int PulseSpecificBrachyControlPointDeliveredSequence = 805831025;

    /**
     * The pulse number value.
     */
    public static final int PulseNumber = 805831026;

    /**
     * The brachy pulse control point delivered sequence value.
     */
    public static final int BrachyPulseControlPointDeliveredSequence = 805831027;

    /**
     * The current treatment status value.
     */
    public static final int CurrentTreatmentStatus = 805831168;

    /**
     * The treatment status comment value.
     */
    public static final int TreatmentStatusComment = 805831170;

    /**
     * The fraction group summary sequence value.
     */
    public static final int FractionGroupSummarySequence = 805831200;

    /**
     * The referenced fraction number value.
     */
    public static final int ReferencedFractionNumber = 805831203;

    /**
     * The fraction group type value.
     */
    public static final int FractionGroupType = 805831204;

    /**
     * The beam stopper position value.
     */
    public static final int BeamStopperPosition = 805831216;

    /**
     * The fraction status summary sequence value.
     */
    public static final int FractionStatusSummarySequence = 805831232;

    /**
     * The treatment date value.
     */
    public static final int TreatmentDate = 805831248;

    /**
     * The treatment time value.
     */
    public static final int TreatmentTime = 805831249;

    /**
     * The rt plan label value.
     */
    public static final int RTPlanLabel = 805961730;

    /**
     * The rt plan name value.
     */
    public static final int RTPlanName = 805961731;

    /**
     * The rt plan description value.
     */
    public static final int RTPlanDescription = 805961732;

    /**
     * The rt plan date value.
     */
    public static final int RTPlanDate = 805961734;

    /**
     * The rt plan time value.
     */
    public static final int RTPlanTime = 805961735;

    /**
     * The treatment protocols value.
     */
    public static final int TreatmentProtocols = 805961737;

    /**
     * The plan intent value.
     */
    public static final int PlanIntent = 805961738;

    /**
     * The treatment sites value.
     */
    public static final int TreatmentSites = 805961739;

    /**
     * The rt plan geometry value.
     */
    public static final int RTPlanGeometry = 805961740;

    /**
     * The prescription description value.
     */
    public static final int PrescriptionDescription = 805961742;

    /**
     * The dose reference sequence value.
     */
    public static final int DoseReferenceSequence = 805961744;

    /**
     * The dose reference number value.
     */
    public static final int DoseReferenceNumber = 805961746;

    /**
     * The dose reference uid value.
     */
    public static final int DoseReferenceUID = 805961747;

    /**
     * The dose reference structure type value.
     */
    public static final int DoseReferenceStructureType = 805961748;

    /**
     * The nominal beam energy unit value.
     */
    public static final int NominalBeamEnergyUnit = 805961749;

    /**
     * The dose reference description value.
     */
    public static final int DoseReferenceDescription = 805961750;

    /**
     * The dose reference point coordinates value.
     */
    public static final int DoseReferencePointCoordinates = 805961752;

    /**
     * The nominal prior dose value.
     */
    public static final int NominalPriorDose = 805961754;

    /**
     * The dose reference type value.
     */
    public static final int DoseReferenceType = 805961760;

    /**
     * The constraint weight value.
     */
    public static final int ConstraintWeight = 805961761;

    /**
     * The delivery warning dose value.
     */
    public static final int DeliveryWarningDose = 805961762;

    /**
     * The delivery maximum dose value.
     */
    public static final int DeliveryMaximumDose = 805961763;

    /**
     * The target minimum dose value.
     */
    public static final int TargetMinimumDose = 805961765;

    /**
     * The target prescription dose value.
     */
    public static final int TargetPrescriptionDose = 805961766;

    /**
     * The target maximum dose value.
     */
    public static final int TargetMaximumDose = 805961767;

    /**
     * The target underdose volume fraction value.
     */
    public static final int TargetUnderdoseVolumeFraction = 805961768;

    /**
     * The organ at risk full volume dose value.
     */
    public static final int OrganAtRiskFullVolumeDose = 805961770;

    /**
     * The organ at risk limit dose value.
     */
    public static final int OrganAtRiskLimitDose = 805961771;

    /**
     * The organ at risk maximum dose value.
     */
    public static final int OrganAtRiskMaximumDose = 805961772;

    /**
     * The organ at risk overdose volume fraction value.
     */
    public static final int OrganAtRiskOverdoseVolumeFraction = 805961773;

    /**
     * The tolerance table sequence value.
     */
    public static final int ToleranceTableSequence = 805961792;

    /**
     * The tolerance table number value.
     */
    public static final int ToleranceTableNumber = 805961794;

    /**
     * The tolerance table label value.
     */
    public static final int ToleranceTableLabel = 805961795;

    /**
     * The gantry angle tolerance value.
     */
    public static final int GantryAngleTolerance = 805961796;

    /**
     * The beam limiting device angle tolerance value.
     */
    public static final int BeamLimitingDeviceAngleTolerance = 805961798;

    /**
     * The beam limiting device tolerance sequence value.
     */
    public static final int BeamLimitingDeviceToleranceSequence = 805961800;

    /**
     * The beam limiting device position tolerance value.
     */
    public static final int BeamLimitingDevicePositionTolerance = 805961802;

    /**
     * The snout position tolerance value.
     */
    public static final int SnoutPositionTolerance = 805961803;

    /**
     * The patient support angle tolerance value.
     */
    public static final int PatientSupportAngleTolerance = 805961804;

    /**
     * The table top eccentric angle tolerance value.
     */
    public static final int TableTopEccentricAngleTolerance = 805961806;

    /**
     * The table top pitch angle tolerance value.
     */
    public static final int TableTopPitchAngleTolerance = 805961807;

    /**
     * The table top roll angle tolerance value.
     */
    public static final int TableTopRollAngleTolerance = 805961808;

    /**
     * The table top vertical position tolerance value.
     */
    public static final int TableTopVerticalPositionTolerance = 805961809;

    /**
     * The table top longitudinal position tolerance value.
     */
    public static final int TableTopLongitudinalPositionTolerance = 805961810;

    /**
     * The table top lateral position tolerance value.
     */
    public static final int TableTopLateralPositionTolerance = 805961811;

    /**
     * The rt plan relationship value.
     */
    public static final int RTPlanRelationship = 805961813;

    /**
     * The fraction group sequence value.
     */
    public static final int FractionGroupSequence = 805961840;

    /**
     * The fraction group number value.
     */
    public static final int FractionGroupNumber = 805961841;

    /**
     * The fraction group description value.
     */
    public static final int FractionGroupDescription = 805961842;

    /**
     * The number of fractions planned value.
     */
    public static final int NumberOfFractionsPlanned = 805961848;

    /**
     * The number of fraction pattern digits per day value.
     */
    public static final int NumberOfFractionPatternDigitsPerDay = 805961849;

    /**
     * The repeat fraction cycle length value.
     */
    public static final int RepeatFractionCycleLength = 805961850;

    /**
     * The fraction pattern value.
     */
    public static final int FractionPattern = 805961851;

    /**
     * The number of beams value.
     */
    public static final int NumberOfBeams = 805961856;

    /**
     * The beam dose specification point value.
     */
    public static final int BeamDoseSpecificationPoint = 805961858;

    /**
     * The referenced dose reference uid value.
     */
    public static final int ReferencedDoseReferenceUID = 805961859;

    /**
     * The beam dose value.
     */
    public static final int BeamDose = 805961860;

    /**
     * The beam meterset value.
     */
    public static final int BeamMeterset = 805961862;

    /**
     * The beam dose point depth value.
     */
    public static final int BeamDosePointDepth = 805961864;

    /**
     * The beam dose point equivalent depth value.
     */
    public static final int BeamDosePointEquivalentDepth = 805961865;

    /**
     * The beam dose point ssd value.
     */
    public static final int BeamDosePointSSD = 805961866;

    /**
     * The beam dose meaning value.
     */
    public static final int BeamDoseMeaning = 805961867;

    /**
     * The beam dose verification control point sequence value.
     */
    public static final int BeamDoseVerificationControlPointSequence = 805961868;

    /**
     * The average beam dose point depth value.
     */
    public static final int AverageBeamDosePointDepth = 805961869;

    /**
     * The average beam dose point equivalent depth value.
     */
    public static final int AverageBeamDosePointEquivalentDepth = 805961870;

    /**
     * The average beam dose point ssd value.
     */
    public static final int AverageBeamDosePointSSD = 805961871;

    /**
     * The beam dose type value.
     */
    public static final int BeamDoseType = 805961872;

    /**
     * The alternate beam dose value.
     */
    public static final int AlternateBeamDose = 805961873;

    /**
     * The alternate beam dose type value.
     */
    public static final int AlternateBeamDoseType = 805961874;

    /**
     * The depth value averaging flag value.
     */
    public static final int DepthValueAveragingFlag = 805961875;

    /**
     * The beam dose point source to external contour distance value.
     */
    public static final int BeamDosePointSourceToExternalContourDistance = 805961876;

    /**
     * The number of brachy application setups value.
     */
    public static final int NumberOfBrachyApplicationSetups = 805961888;

    /**
     * The brachy application setup dose specification point value.
     */
    public static final int BrachyApplicationSetupDoseSpecificationPoint = 805961890;

    /**
     * The brachy application setup dose value.
     */
    public static final int BrachyApplicationSetupDose = 805961892;

    /**
     * The beam sequence value.
     */
    public static final int BeamSequence = 805961904;

    /**
     * The treatment machine name value.
     */
    public static final int TreatmentMachineName = 805961906;

    /**
     * The primary dosimeter unit value.
     */
    public static final int PrimaryDosimeterUnit = 805961907;

    /**
     * The source axis distance value.
     */
    public static final int SourceAxisDistance = 805961908;

    /**
     * The beam limiting device sequence value.
     */
    public static final int BeamLimitingDeviceSequence = 805961910;

    /**
     * The rt beam limiting device type value.
     */
    public static final int RTBeamLimitingDeviceType = 805961912;

    /**
     * The source to beam limiting device distance value.
     */
    public static final int SourceToBeamLimitingDeviceDistance = 805961914;

    /**
     * The isocenter to beam limiting device distance value.
     */
    public static final int IsocenterToBeamLimitingDeviceDistance = 805961915;

    /**
     * The number of leaf jaw pairs value.
     */
    public static final int NumberOfLeafJawPairs = 805961916;

    /**
     * The leaf position boundaries value.
     */
    public static final int LeafPositionBoundaries = 805961918;

    /**
     * The beam number value.
     */
    public static final int BeamNumber = 805961920;

    /**
     * The beam name value.
     */
    public static final int BeamName = 805961922;

    /**
     * The beam description value.
     */
    public static final int BeamDescription = 805961923;

    /**
     * The beam type value.
     */
    public static final int BeamType = 805961924;

    /**
     * The beam delivery duration limit value.
     */
    public static final int BeamDeliveryDurationLimit = 805961925;

    /**
     * The radiation type value.
     */
    public static final int RadiationType = 805961926;

    /**
     * The high dose technique type value.
     */
    public static final int HighDoseTechniqueType = 805961927;

    /**
     * The reference image number value.
     */
    public static final int ReferenceImageNumber = 805961928;

    /**
     * The planned verification image sequence value.
     */
    public static final int PlannedVerificationImageSequence = 805961930;

    /**
     * The imaging device specific acquisition parameters value.
     */
    public static final int ImagingDeviceSpecificAcquisitionParameters = 805961932;

    /**
     * The treatment delivery type value.
     */
    public static final int TreatmentDeliveryType = 805961934;

    /**
     * The number of wedges value.
     */
    public static final int NumberOfWedges = 805961936;

    /**
     * The wedge sequence value.
     */
    public static final int WedgeSequence = 805961937;

    /**
     * The wedge number value.
     */
    public static final int WedgeNumber = 805961938;

    /**
     * The wedge type value.
     */
    public static final int WedgeType = 805961939;

    /**
     * The wedge id value.
     */
    public static final int WedgeID = 805961940;

    /**
     * The wedge angle value.
     */
    public static final int WedgeAngle = 805961941;

    /**
     * The wedge factor value.
     */
    public static final int WedgeFactor = 805961942;

    /**
     * The total wedge tray water equivalent thickness value.
     */
    public static final int TotalWedgeTrayWaterEquivalentThickness = 805961943;

    /**
     * The wedge orientation value.
     */
    public static final int WedgeOrientation = 805961944;

    /**
     * The isocenter to wedge tray distance value.
     */
    public static final int IsocenterToWedgeTrayDistance = 805961945;

    /**
     * The source to wedge tray distance value.
     */
    public static final int SourceToWedgeTrayDistance = 805961946;

    /**
     * The wedge thin edge position value.
     */
    public static final int WedgeThinEdgePosition = 805961947;

    /**
     * The bolus id value.
     */
    public static final int BolusID = 805961948;

    /**
     * The bolus description value.
     */
    public static final int BolusDescription = 805961949;

    /**
     * The effective wedge angle value.
     */
    public static final int EffectiveWedgeAngle = 805961950;

    /**
     * The number of compensators value.
     */
    public static final int NumberOfCompensators = 805961952;

    /**
     * The material id value.
     */
    public static final int MaterialID = 805961953;

    /**
     * The total compensator tray factor value.
     */
    public static final int TotalCompensatorTrayFactor = 805961954;

    /**
     * The compensator sequence value.
     */
    public static final int CompensatorSequence = 805961955;

    /**
     * The compensator number value.
     */
    public static final int CompensatorNumber = 805961956;

    /**
     * The compensator id value.
     */
    public static final int CompensatorID = 805961957;

    /**
     * The source to compensator tray distance value.
     */
    public static final int SourceToCompensatorTrayDistance = 805961958;

    /**
     * The compensator rows value.
     */
    public static final int CompensatorRows = 805961959;

    /**
     * The compensator columns value.
     */
    public static final int CompensatorColumns = 805961960;

    /**
     * The compensator pixel spacing value.
     */
    public static final int CompensatorPixelSpacing = 805961961;

    /**
     * The compensator position value.
     */
    public static final int CompensatorPosition = 805961962;

    /**
     * The compensator transmission data value.
     */
    public static final int CompensatorTransmissionData = 805961963;

    /**
     * The compensator thickness data value.
     */
    public static final int CompensatorThicknessData = 805961964;

    /**
     * The number of boli value.
     */
    public static final int NumberOfBoli = 805961965;

    /**
     * The compensator type value.
     */
    public static final int CompensatorType = 805961966;

    /**
     * The compensator tray id value.
     */
    public static final int CompensatorTrayID = 805961967;

    /**
     * The number of blocks value.
     */
    public static final int NumberOfBlocks = 805961968;

    /**
     * The total block tray factor value.
     */
    public static final int TotalBlockTrayFactor = 805961970;

    /**
     * The total block tray water equivalent thickness value.
     */
    public static final int TotalBlockTrayWaterEquivalentThickness = 805961971;

    /**
     * The block sequence value.
     */
    public static final int BlockSequence = 805961972;

    /**
     * The block tray id value.
     */
    public static final int BlockTrayID = 805961973;

    /**
     * The source to block tray distance value.
     */
    public static final int SourceToBlockTrayDistance = 805961974;

    /**
     * The isocenter to block tray distance value.
     */
    public static final int IsocenterToBlockTrayDistance = 805961975;

    /**
     * The block type value.
     */
    public static final int BlockType = 805961976;

    /**
     * The accessory code value.
     */
    public static final int AccessoryCode = 805961977;

    /**
     * The block divergence value.
     */
    public static final int BlockDivergence = 805961978;

    /**
     * The block mounting position value.
     */
    public static final int BlockMountingPosition = 805961979;

    /**
     * The block number value.
     */
    public static final int BlockNumber = 805961980;

    /**
     * The block name value.
     */
    public static final int BlockName = 805961982;

    /**
     * The block thickness value.
     */
    public static final int BlockThickness = 805961984;

    /**
     * The block transmission value.
     */
    public static final int BlockTransmission = 805961986;

    /**
     * The block number of points value.
     */
    public static final int BlockNumberOfPoints = 805961988;

    /**
     * The block data value.
     */
    public static final int BlockData = 805961990;

    /**
     * The applicator sequence value.
     */
    public static final int ApplicatorSequence = 805961991;

    /**
     * The applicator id value.
     */
    public static final int ApplicatorID = 805961992;

    /**
     * The applicator type value.
     */
    public static final int ApplicatorType = 805961993;

    /**
     * The applicator description value.
     */
    public static final int ApplicatorDescription = 805961994;

    /**
     * The cumulative dose reference coefficient value.
     */
    public static final int CumulativeDoseReferenceCoefficient = 805961996;

    /**
     * The final cumulative meterset weight value.
     */
    public static final int FinalCumulativeMetersetWeight = 805961998;

    /**
     * The number of control points value.
     */
    public static final int NumberOfControlPoints = 805962000;

    /**
     * The control point sequence value.
     */
    public static final int ControlPointSequence = 805962001;

    /**
     * The control point index value.
     */
    public static final int ControlPointIndex = 805962002;

    /**
     * The nominal beam energy value.
     */
    public static final int NominalBeamEnergy = 805962004;

    /**
     * The dose rate set value.
     */
    public static final int DoseRateSet = 805962005;

    /**
     * The wedge position sequence value.
     */
    public static final int WedgePositionSequence = 805962006;

    /**
     * The wedge position value.
     */
    public static final int WedgePosition = 805962008;

    /**
     * The beam limiting device position sequence value.
     */
    public static final int BeamLimitingDevicePositionSequence = 805962010;

    /**
     * The leaf jaw positions value.
     */
    public static final int LeafJawPositions = 805962012;

    /**
     * The gantry angle value.
     */
    public static final int GantryAngle = 805962014;

    /**
     * The gantry rotation direction value.
     */
    public static final int GantryRotationDirection = 805962015;

    /**
     * The beam limiting device angle value.
     */
    public static final int BeamLimitingDeviceAngle = 805962016;

    /**
     * The beam limiting device rotation direction value.
     */
    public static final int BeamLimitingDeviceRotationDirection = 805962017;

    /**
     * The patient support angle value.
     */
    public static final int PatientSupportAngle = 805962018;

    /**
     * The patient support rotation direction value.
     */
    public static final int PatientSupportRotationDirection = 805962019;

    /**
     * The table top eccentric axis distance value.
     */
    public static final int TableTopEccentricAxisDistance = 805962020;

    /**
     * The table top eccentric angle value.
     */
    public static final int TableTopEccentricAngle = 805962021;

    /**
     * The table top eccentric rotation direction value.
     */
    public static final int TableTopEccentricRotationDirection = 805962022;

    /**
     * The table top vertical position value.
     */
    public static final int TableTopVerticalPosition = 805962024;

    /**
     * The table top longitudinal position value.
     */
    public static final int TableTopLongitudinalPosition = 805962025;

    /**
     * The table top lateral position value.
     */
    public static final int TableTopLateralPosition = 805962026;

    /**
     * The isocenter position value.
     */
    public static final int IsocenterPosition = 805962028;

    /**
     * The surface entry point value.
     */
    public static final int SurfaceEntryPoint = 805962030;

    /**
     * The source to surface distance value.
     */
    public static final int SourceToSurfaceDistance = 805962032;

    /**
     * The average beam dose point source to external contour distance value.
     */
    public static final int AverageBeamDosePointSourceToExternalContourDistance = 805962033;

    /**
     * The source to external contour distance value.
     */
    public static final int SourceToExternalContourDistance = 805962034;

    /**
     * The external contour entry point value.
     */
    public static final int ExternalContourEntryPoint = 805962035;

    /**
     * The cumulative meterset weight value.
     */
    public static final int CumulativeMetersetWeight = 805962036;

    /**
     * The table top pitch angle value.
     */
    public static final int TableTopPitchAngle = 805962048;

    /**
     * The table top pitch rotation direction value.
     */
    public static final int TableTopPitchRotationDirection = 805962050;

    /**
     * The table top roll angle value.
     */
    public static final int TableTopRollAngle = 805962052;

    /**
     * The table top roll rotation direction value.
     */
    public static final int TableTopRollRotationDirection = 805962054;

    /**
     * The head fixation angle value.
     */
    public static final int HeadFixationAngle = 805962056;

    /**
     * The gantry pitch angle value.
     */
    public static final int GantryPitchAngle = 805962058;

    /**
     * The gantry pitch rotation direction value.
     */
    public static final int GantryPitchRotationDirection = 805962060;

    /**
     * The gantry pitch angle tolerance value.
     */
    public static final int GantryPitchAngleTolerance = 805962062;

    /**
     * The fixation eye value.
     */
    public static final int FixationEye = 805962064;

    /**
     * The chair head frame position value.
     */
    public static final int ChairHeadFramePosition = 805962065;

    /**
     * The head fixation angle tolerance value.
     */
    public static final int HeadFixationAngleTolerance = 805962066;

    /**
     * The chair head frame position tolerance value.
     */
    public static final int ChairHeadFramePositionTolerance = 805962067;

    /**
     * The fixation light azimuthal angle tolerance value.
     */
    public static final int FixationLightAzimuthalAngleTolerance = 805962068;

    /**
     * The fixation light polar angle tolerance value.
     */
    public static final int FixationLightPolarAngleTolerance = 805962069;

    /**
     * The patient setup sequence value.
     */
    public static final int PatientSetupSequence = 805962112;

    /**
     * The patient setup number value.
     */
    public static final int PatientSetupNumber = 805962114;

    /**
     * The patient setup label value.
     */
    public static final int PatientSetupLabel = 805962115;

    /**
     * The patient additional position value.
     */
    public static final int PatientAdditionalPosition = 805962116;

    /**
     * The fixation device sequence value.
     */
    public static final int FixationDeviceSequence = 805962128;

    /**
     * The fixation device type value.
     */
    public static final int FixationDeviceType = 805962130;

    /**
     * The fixation device label value.
     */
    public static final int FixationDeviceLabel = 805962132;

    /**
     * The fixation device description value.
     */
    public static final int FixationDeviceDescription = 805962134;

    /**
     * The fixation device position value.
     */
    public static final int FixationDevicePosition = 805962136;

    /**
     * The fixation device pitch angle value.
     */
    public static final int FixationDevicePitchAngle = 805962137;

    /**
     * The fixation device roll angle value.
     */
    public static final int FixationDeviceRollAngle = 805962138;

    /**
     * The shielding device sequence value.
     */
    public static final int ShieldingDeviceSequence = 805962144;

    /**
     * The shielding device type value.
     */
    public static final int ShieldingDeviceType = 805962146;

    /**
     * The shielding device label value.
     */
    public static final int ShieldingDeviceLabel = 805962148;

    /**
     * The shielding device description value.
     */
    public static final int ShieldingDeviceDescription = 805962150;

    /**
     * The shielding device position value.
     */
    public static final int ShieldingDevicePosition = 805962152;

    /**
     * The setup technique value.
     */
    public static final int SetupTechnique = 805962160;

    /**
     * The setup technique description value.
     */
    public static final int SetupTechniqueDescription = 805962162;

    /**
     * The setup device sequence value.
     */
    public static final int SetupDeviceSequence = 805962164;

    /**
     * The setup device type value.
     */
    public static final int SetupDeviceType = 805962166;

    /**
     * The setup device label value.
     */
    public static final int SetupDeviceLabel = 805962168;

    /**
     * The setup device description value.
     */
    public static final int SetupDeviceDescription = 805962170;

    /**
     * The setup device parameter value.
     */
    public static final int SetupDeviceParameter = 805962172;

    /**
     * The setup reference description value.
     */
    public static final int SetupReferenceDescription = 805962192;

    /**
     * The table top vertical setup displacement value.
     */
    public static final int TableTopVerticalSetupDisplacement = 805962194;

    /**
     * The table top longitudinal setup displacement value.
     */
    public static final int TableTopLongitudinalSetupDisplacement = 805962196;

    /**
     * The table top lateral setup displacement value.
     */
    public static final int TableTopLateralSetupDisplacement = 805962198;

    /**
     * The brachy treatment technique value.
     */
    public static final int BrachyTreatmentTechnique = 805962240;

    /**
     * The brachy treatment type value.
     */
    public static final int BrachyTreatmentType = 805962242;

    /**
     * The treatment machine sequence value.
     */
    public static final int TreatmentMachineSequence = 805962246;

    /**
     * The source sequence value.
     */
    public static final int SourceSequence = 805962256;

    /**
     * The source number value.
     */
    public static final int SourceNumber = 805962258;

    /**
     * The source type value.
     */
    public static final int SourceType = 805962260;

    /**
     * The source manufacturer value.
     */
    public static final int SourceManufacturer = 805962262;

    /**
     * The active source diameter value.
     */
    public static final int ActiveSourceDiameter = 805962264;

    /**
     * The active source length value.
     */
    public static final int ActiveSourceLength = 805962266;

    /**
     * The source model id value.
     */
    public static final int SourceModelID = 805962267;

    /**
     * The source description value.
     */
    public static final int SourceDescription = 805962268;

    /**
     * The source encapsulation nominal thickness value.
     */
    public static final int SourceEncapsulationNominalThickness = 805962274;

    /**
     * The source encapsulation nominal transmission value.
     */
    public static final int SourceEncapsulationNominalTransmission = 805962276;

    /**
     * The source isotope name value.
     */
    public static final int SourceIsotopeName = 805962278;

    /**
     * The source isotope half life value.
     */
    public static final int SourceIsotopeHalfLife = 805962280;

    /**
     * The source strength units value.
     */
    public static final int SourceStrengthUnits = 805962281;

    /**
     * The reference air kerma rate value.
     */
    public static final int ReferenceAirKermaRate = 805962282;

    /**
     * The source strength value.
     */
    public static final int SourceStrength = 805962283;

    /**
     * The source strength reference date value.
     */
    public static final int SourceStrengthReferenceDate = 805962284;

    /**
     * The source strength reference time value.
     */
    public static final int SourceStrengthReferenceTime = 805962286;

    /**
     * The application setup sequence value.
     */
    public static final int ApplicationSetupSequence = 805962288;

    /**
     * The application setup type value.
     */
    public static final int ApplicationSetupType = 805962290;

    /**
     * The application setup number value.
     */
    public static final int ApplicationSetupNumber = 805962292;

    /**
     * The application setup name value.
     */
    public static final int ApplicationSetupName = 805962294;

    /**
     * The application setup manufacturer value.
     */
    public static final int ApplicationSetupManufacturer = 805962296;

    /**
     * The template number value.
     */
    public static final int TemplateNumber = 805962304;

    /**
     * The template type value.
     */
    public static final int TemplateType = 805962306;

    /**
     * The template name value.
     */
    public static final int TemplateName = 805962308;

    /**
     * The total reference air kerma value.
     */
    public static final int TotalReferenceAirKerma = 805962320;

    /**
     * The brachy accessory device sequence value.
     */
    public static final int BrachyAccessoryDeviceSequence = 805962336;

    /**
     * The brachy accessory device number value.
     */
    public static final int BrachyAccessoryDeviceNumber = 805962338;

    /**
     * The brachy accessory device id value.
     */
    public static final int BrachyAccessoryDeviceID = 805962339;

    /**
     * The brachy accessory device type value.
     */
    public static final int BrachyAccessoryDeviceType = 805962340;

    /**
     * The brachy accessory device name value.
     */
    public static final int BrachyAccessoryDeviceName = 805962342;

    /**
     * The brachy accessory device nominal thickness value.
     */
    public static final int BrachyAccessoryDeviceNominalThickness = 805962346;

    /**
     * The brachy accessory device nominal transmission value.
     */
    public static final int BrachyAccessoryDeviceNominalTransmission = 805962348;

    /**
     * The channel effective length value.
     */
    public static final int ChannelEffectiveLength = 805962353;

    /**
     * The channel inner length value.
     */
    public static final int ChannelInnerLength = 805962354;

    /**
     * The afterloader channel id value.
     */
    public static final int AfterloaderChannelID = 805962355;

    /**
     * The source applicator tip length value.
     */
    public static final int SourceApplicatorTipLength = 805962356;

    /**
     * The channel sequence value.
     */
    public static final int ChannelSequence = 805962368;

    /**
     * The channel number value.
     */
    public static final int ChannelNumber = 805962370;

    /**
     * The channel length value.
     */
    public static final int ChannelLength = 805962372;

    /**
     * The channel total time value.
     */
    public static final int ChannelTotalTime = 805962374;

    /**
     * The source movement type value.
     */
    public static final int SourceMovementType = 805962376;

    /**
     * The number of pulses value.
     */
    public static final int NumberOfPulses = 805962378;

    /**
     * The pulse repetition interval value.
     */
    public static final int PulseRepetitionInterval = 805962380;

    /**
     * The source applicator number value.
     */
    public static final int SourceApplicatorNumber = 805962384;

    /**
     * The source applicator id value.
     */
    public static final int SourceApplicatorID = 805962385;

    /**
     * The source applicator type value.
     */
    public static final int SourceApplicatorType = 805962386;

    /**
     * The source applicator name value.
     */
    public static final int SourceApplicatorName = 805962388;

    /**
     * The source applicator length value.
     */
    public static final int SourceApplicatorLength = 805962390;

    /**
     * The source applicator manufacturer value.
     */
    public static final int SourceApplicatorManufacturer = 805962392;

    /**
     * The source applicator wall nominal thickness value.
     */
    public static final int SourceApplicatorWallNominalThickness = 805962396;

    /**
     * The source applicator wall nominal transmission value.
     */
    public static final int SourceApplicatorWallNominalTransmission = 805962398;

    /**
     * The source applicator step size value.
     */
    public static final int SourceApplicatorStepSize = 805962400;

    /**
     * The applicator shape referenced roi number value.
     */
    public static final int ApplicatorShapeReferencedROINumber = 805962401;

    /**
     * The transfer tube number value.
     */
    public static final int TransferTubeNumber = 805962402;

    /**
     * The transfer tube length value.
     */
    public static final int TransferTubeLength = 805962404;

    /**
     * The channel shield sequence value.
     */
    public static final int ChannelShieldSequence = 805962416;

    /**
     * The channel shield number value.
     */
    public static final int ChannelShieldNumber = 805962418;

    /**
     * The channel shield id value.
     */
    public static final int ChannelShieldID = 805962419;

    /**
     * The channel shield name value.
     */
    public static final int ChannelShieldName = 805962420;

    /**
     * The channel shield nominal thickness value.
     */
    public static final int ChannelShieldNominalThickness = 805962424;

    /**
     * The channel shield nominal transmission value.
     */
    public static final int ChannelShieldNominalTransmission = 805962426;

    /**
     * The final cumulative time weight value.
     */
    public static final int FinalCumulativeTimeWeight = 805962440;

    /**
     * The brachy control point sequence value.
     */
    public static final int BrachyControlPointSequence = 805962448;

    /**
     * The control point relative position value.
     */
    public static final int ControlPointRelativePosition = 805962450;

    /**
     * The control point3 d position value.
     */
    public static final int ControlPoint3DPosition = 805962452;

    /**
     * The cumulative time weight value.
     */
    public static final int CumulativeTimeWeight = 805962454;

    /**
     * The compensator divergence value.
     */
    public static final int CompensatorDivergence = 805962464;

    /**
     * The compensator mounting position value.
     */
    public static final int CompensatorMountingPosition = 805962465;

    /**
     * The source to compensator distance value.
     */
    public static final int SourceToCompensatorDistance = 805962466;

    /**
     * The total compensator tray water equivalent thickness value.
     */
    public static final int TotalCompensatorTrayWaterEquivalentThickness = 805962467;

    /**
     * The isocenter to compensator tray distance value.
     */
    public static final int IsocenterToCompensatorTrayDistance = 805962468;

    /**
     * The compensator column offset value.
     */
    public static final int CompensatorColumnOffset = 805962469;

    /**
     * The isocenter to compensator distances value.
     */
    public static final int IsocenterToCompensatorDistances = 805962470;

    /**
     * The compensator relative stopping power ratio value.
     */
    public static final int CompensatorRelativeStoppingPowerRatio = 805962471;

    /**
     * The compensator milling tool diameter value.
     */
    public static final int CompensatorMillingToolDiameter = 805962472;

    /**
     * The ion range compensator sequence value.
     */
    public static final int IonRangeCompensatorSequence = 805962474;

    /**
     * The compensator description value.
     */
    public static final int CompensatorDescription = 805962475;

    /**
     * The radiation mass number value.
     */
    public static final int RadiationMassNumber = 805962498;

    /**
     * The radiation atomic number value.
     */
    public static final int RadiationAtomicNumber = 805962500;

    /**
     * The radiation charge state value.
     */
    public static final int RadiationChargeState = 805962502;

    /**
     * The scan mode value.
     */
    public static final int ScanMode = 805962504;

    /**
     * The modulated scan mode type value.
     */
    public static final int ModulatedScanModeType = 805962505;

    /**
     * The virtual source axis distances value.
     */
    public static final int VirtualSourceAxisDistances = 805962506;

    /**
     * The snout sequence value.
     */
    public static final int SnoutSequence = 805962508;

    /**
     * The snout position value.
     */
    public static final int SnoutPosition = 805962509;

    /**
     * The snout id value.
     */
    public static final int SnoutID = 805962511;

    /**
     * The number of range shifters value.
     */
    public static final int NumberOfRangeShifters = 805962514;

    /**
     * The range shifter sequence value.
     */
    public static final int RangeShifterSequence = 805962516;

    /**
     * The range shifter number value.
     */
    public static final int RangeShifterNumber = 805962518;

    /**
     * The range shifter id value.
     */
    public static final int RangeShifterID = 805962520;

    /**
     * The range shifter type value.
     */
    public static final int RangeShifterType = 805962528;

    /**
     * The range shifter description value.
     */
    public static final int RangeShifterDescription = 805962530;

    /**
     * The number of lateral spreading devices value.
     */
    public static final int NumberOfLateralSpreadingDevices = 805962544;

    /**
     * The lateral spreading device sequence value.
     */
    public static final int LateralSpreadingDeviceSequence = 805962546;

    /**
     * The lateral spreading device number value.
     */
    public static final int LateralSpreadingDeviceNumber = 805962548;

    /**
     * The lateral spreading device id value.
     */
    public static final int LateralSpreadingDeviceID = 805962550;

    /**
     * The lateral spreading device type value.
     */
    public static final int LateralSpreadingDeviceType = 805962552;

    /**
     * The lateral spreading device description value.
     */
    public static final int LateralSpreadingDeviceDescription = 805962554;

    /**
     * The lateral spreading device water equivalent thickness value.
     */
    public static final int LateralSpreadingDeviceWaterEquivalentThickness = 805962556;

    /**
     * The number of range modulators value.
     */
    public static final int NumberOfRangeModulators = 805962560;

    /**
     * The range modulator sequence value.
     */
    public static final int RangeModulatorSequence = 805962562;

    /**
     * The range modulator number value.
     */
    public static final int RangeModulatorNumber = 805962564;

    /**
     * The range modulator id value.
     */
    public static final int RangeModulatorID = 805962566;

    /**
     * The range modulator type value.
     */
    public static final int RangeModulatorType = 805962568;

    /**
     * The range modulator description value.
     */
    public static final int RangeModulatorDescription = 805962570;

    /**
     * The beam current modulation id value.
     */
    public static final int BeamCurrentModulationID = 805962572;

    /**
     * The patient support type value.
     */
    public static final int PatientSupportType = 805962576;

    /**
     * The patient support id value.
     */
    public static final int PatientSupportID = 805962578;

    /**
     * The patient support accessory code value.
     */
    public static final int PatientSupportAccessoryCode = 805962580;

    /**
     * The tray accessory code value.
     */
    public static final int TrayAccessoryCode = 805962581;

    /**
     * The fixation light azimuthal angle value.
     */
    public static final int FixationLightAzimuthalAngle = 805962582;

    /**
     * The fixation light polar angle value.
     */
    public static final int FixationLightPolarAngle = 805962584;

    /**
     * The meterset rate value.
     */
    public static final int MetersetRate = 805962586;

    /**
     * The range shifter settings sequence value.
     */
    public static final int RangeShifterSettingsSequence = 805962592;

    /**
     * The range shifter setting value.
     */
    public static final int RangeShifterSetting = 805962594;

    /**
     * The isocenter to range shifter distance value.
     */
    public static final int IsocenterToRangeShifterDistance = 805962596;

    /**
     * The range shifter water equivalent thickness value.
     */
    public static final int RangeShifterWaterEquivalentThickness = 805962598;

    /**
     * The lateral spreading device settings sequence value.
     */
    public static final int LateralSpreadingDeviceSettingsSequence = 805962608;

    /**
     * The lateral spreading device setting value.
     */
    public static final int LateralSpreadingDeviceSetting = 805962610;

    /**
     * The isocenter to lateral spreading device distance value.
     */
    public static final int IsocenterToLateralSpreadingDeviceDistance = 805962612;

    /**
     * The range modulator settings sequence value.
     */
    public static final int RangeModulatorSettingsSequence = 805962624;

    /**
     * The range modulator gating start value value.
     */
    public static final int RangeModulatorGatingStartValue = 805962626;

    /**
     * The range modulator gating stop value value.
     */
    public static final int RangeModulatorGatingStopValue = 805962628;

    /**
     * The range modulator gating start water equivalent thickness value.
     */
    public static final int RangeModulatorGatingStartWaterEquivalentThickness = 805962630;

    /**
     * The range modulator gating stop water equivalent thickness value.
     */
    public static final int RangeModulatorGatingStopWaterEquivalentThickness = 805962632;

    /**
     * The isocenter to range modulator distance value.
     */
    public static final int IsocenterToRangeModulatorDistance = 805962634;

    /**
     * The scan spot time offset value.
     */
    public static final int ScanSpotTimeOffset = 805962639;

    /**
     * The scan spot tune id value.
     */
    public static final int ScanSpotTuneID = 805962640;

    /**
     * The scan spot prescribed indices value.
     */
    public static final int ScanSpotPrescribedIndices = 805962641;

    /**
     * The number of scan spot positions value.
     */
    public static final int NumberOfScanSpotPositions = 805962642;

    /**
     * The scan spot reordered value.
     */
    public static final int ScanSpotReordered = 805962643;

    /**
     * The scan spot position map value.
     */
    public static final int ScanSpotPositionMap = 805962644;

    /**
     * The scan spot reordering allowed value.
     */
    public static final int ScanSpotReorderingAllowed = 805962645;

    /**
     * The scan spot meterset weights value.
     */
    public static final int ScanSpotMetersetWeights = 805962646;

    /**
     * The scanning spot size value.
     */
    public static final int ScanningSpotSize = 805962648;

    /**
     * The scan spot sizes delivered value.
     */
    public static final int ScanSpotSizesDelivered = 805962649;

    /**
     * The number of paintings value.
     */
    public static final int NumberOfPaintings = 805962650;

    /**
     * The ion tolerance table sequence value.
     */
    public static final int IonToleranceTableSequence = 805962656;

    /**
     * The ion beam sequence value.
     */
    public static final int IonBeamSequence = 805962658;

    /**
     * The ion beam limiting device sequence value.
     */
    public static final int IonBeamLimitingDeviceSequence = 805962660;

    /**
     * The ion block sequence value.
     */
    public static final int IonBlockSequence = 805962662;

    /**
     * The ion control point sequence value.
     */
    public static final int IonControlPointSequence = 805962664;

    /**
     * The ion wedge sequence value.
     */
    public static final int IonWedgeSequence = 805962666;

    /**
     * The ion wedge position sequence value.
     */
    public static final int IonWedgePositionSequence = 805962668;

    /**
     * The referenced setup image sequence value.
     */
    public static final int ReferencedSetupImageSequence = 805962753;

    /**
     * The setup image comment value.
     */
    public static final int SetupImageComment = 805962754;

    /**
     * The motion synchronization sequence value.
     */
    public static final int MotionSynchronizationSequence = 805962768;

    /**
     * The control point orientation value.
     */
    public static final int ControlPointOrientation = 805962770;

    /**
     * The general accessory sequence value.
     */
    public static final int GeneralAccessorySequence = 805962784;

    /**
     * The general accessory id value.
     */
    public static final int GeneralAccessoryID = 805962785;

    /**
     * The general accessory description value.
     */
    public static final int GeneralAccessoryDescription = 805962786;

    /**
     * The general accessory type value.
     */
    public static final int GeneralAccessoryType = 805962787;

    /**
     * The general accessory number value.
     */
    public static final int GeneralAccessoryNumber = 805962788;

    /**
     * The source to general accessory distance value.
     */
    public static final int SourceToGeneralAccessoryDistance = 805962789;

    /**
     * The isocenter to general accessory distance value.
     */
    public static final int IsocenterToGeneralAccessoryDistance = 805962790;

    /**
     * The applicator geometry sequence value.
     */
    public static final int ApplicatorGeometrySequence = 805962801;

    /**
     * The applicator aperture shape value.
     */
    public static final int ApplicatorApertureShape = 805962802;

    /**
     * The applicator opening value.
     */
    public static final int ApplicatorOpening = 805962803;

    /**
     * The applicator opening x value.
     */
    public static final int ApplicatorOpeningX = 805962804;

    /**
     * The applicator opening y value.
     */
    public static final int ApplicatorOpeningY = 805962805;

    /**
     * The source to applicator mounting position distance value.
     */
    public static final int SourceToApplicatorMountingPositionDistance = 805962806;

    /**
     * The number of block slab items value.
     */
    public static final int NumberOfBlockSlabItems = 805962816;

    /**
     * The block slab sequence value.
     */
    public static final int BlockSlabSequence = 805962817;

    /**
     * The block slab thickness value.
     */
    public static final int BlockSlabThickness = 805962818;

    /**
     * The block slab number value.
     */
    public static final int BlockSlabNumber = 805962819;

    /**
     * The device motion control sequence value.
     */
    public static final int DeviceMotionControlSequence = 805962832;

    /**
     * The device motion execution mode value.
     */
    public static final int DeviceMotionExecutionMode = 805962833;

    /**
     * The device motion observation mode value.
     */
    public static final int DeviceMotionObservationMode = 805962834;

    /**
     * The device motion parameter code sequence value.
     */
    public static final int DeviceMotionParameterCodeSequence = 805962835;

    /**
     * The distal depth fraction value.
     */
    public static final int DistalDepthFraction = 805963009;

    /**
     * The distal depth value.
     */
    public static final int DistalDepth = 805963010;

    /**
     * The nominal range modulation fractions value.
     */
    public static final int NominalRangeModulationFractions = 805963011;

    /**
     * The nominal range modulated region depths value.
     */
    public static final int NominalRangeModulatedRegionDepths = 805963012;

    /**
     * The depth dose parameters sequence value.
     */
    public static final int DepthDoseParametersSequence = 805963013;

    /**
     * The delivered depth dose parameters sequence value.
     */
    public static final int DeliveredDepthDoseParametersSequence = 805963014;

    /**
     * The delivered distal depth fraction value.
     */
    public static final int DeliveredDistalDepthFraction = 805963015;

    /**
     * The delivered distal depth value.
     */
    public static final int DeliveredDistalDepth = 805963016;

    /**
     * The delivered nominal range modulation fractions value.
     */
    public static final int DeliveredNominalRangeModulationFractions = 805963017;

    /**
     * The delivered nominal range modulated region depths value.
     */
    public static final int DeliveredNominalRangeModulatedRegionDepths = 805963024;

    /**
     * The delivered reference dose definition value.
     */
    public static final int DeliveredReferenceDoseDefinition = 805963025;

    /**
     * The reference dose definition value.
     */
    public static final int ReferenceDoseDefinition = 805963026;

    /**
     * The rt control point index value.
     */
    public static final int RTControlPointIndex = 805963264;

    /**
     * The radiation generation mode index value.
     */
    public static final int RadiationGenerationModeIndex = 805963265;

    /**
     * The referenced defined device index value.
     */
    public static final int ReferencedDefinedDeviceIndex = 805963266;

    /**
     * The radiation dose identification index value.
     */
    public static final int RadiationDoseIdentificationIndex = 805963267;

    /**
     * The number of rt control points value.
     */
    public static final int NumberOfRTControlPoints = 805963268;

    /**
     * The referenced radiation generation mode index value.
     */
    public static final int ReferencedRadiationGenerationModeIndex = 805963269;

    /**
     * The treatment position index value.
     */
    public static final int TreatmentPositionIndex = 805963270;

    /**
     * The referenced device index value.
     */
    public static final int ReferencedDeviceIndex = 805963271;

    /**
     * The treatment position group label value.
     */
    public static final int TreatmentPositionGroupLabel = 805963272;

    /**
     * The treatment position group uid value.
     */
    public static final int TreatmentPositionGroupUID = 805963273;

    /**
     * The treatment position group sequence value.
     */
    public static final int TreatmentPositionGroupSequence = 805963274;

    /**
     * The referenced treatment position index value.
     */
    public static final int ReferencedTreatmentPositionIndex = 805963275;

    /**
     * The referenced radiation dose identification index value.
     */
    public static final int ReferencedRadiationDoseIdentificationIndex = 805963276;

    /**
     * The rt accessory holder water equivalent thickness value.
     */
    public static final int RTAccessoryHolderWaterEquivalentThickness = 805963277;

    /**
     * The referenced rt accessory holder device index value.
     */
    public static final int ReferencedRTAccessoryHolderDeviceIndex = 805963278;

    /**
     * The rt accessory holder slot existence flag value.
     */
    public static final int RTAccessoryHolderSlotExistenceFlag = 805963279;

    /**
     * The rt accessory holder slot sequence value.
     */
    public static final int RTAccessoryHolderSlotSequence = 805963280;

    /**
     * The rt accessory holder slot id value.
     */
    public static final int RTAccessoryHolderSlotID = 805963281;

    /**
     * The rt accessory holder slot distance value.
     */
    public static final int RTAccessoryHolderSlotDistance = 805963282;

    /**
     * The rt accessory slot distance value.
     */
    public static final int RTAccessorySlotDistance = 805963283;

    /**
     * The rt accessory holder definition sequence value.
     */
    public static final int RTAccessoryHolderDefinitionSequence = 805963284;

    /**
     * The rt accessory device slot id value.
     */
    public static final int RTAccessoryDeviceSlotID = 805963285;

    /**
     * The rt radiation sequence value.
     */
    public static final int RTRadiationSequence = 805963286;

    /**
     * The radiation dose sequence value.
     */
    public static final int RadiationDoseSequence = 805963287;

    /**
     * The radiation dose identification sequence value.
     */
    public static final int RadiationDoseIdentificationSequence = 805963288;

    /**
     * The radiation dose identification label value.
     */
    public static final int RadiationDoseIdentificationLabel = 805963289;

    /**
     * The reference dose type value.
     */
    public static final int ReferenceDoseType = 805963290;

    /**
     * The primary dose value indicator value.
     */
    public static final int PrimaryDoseValueIndicator = 805963291;

    /**
     * The dose values sequence value.
     */
    public static final int DoseValuesSequence = 805963292;

    /**
     * The dose value purpose value.
     */
    public static final int DoseValuePurpose = 805963293;

    /**
     * The reference dose point coordinates value.
     */
    public static final int ReferenceDosePointCoordinates = 805963294;

    /**
     * The radiation dose values parameters sequence value.
     */
    public static final int RadiationDoseValuesParametersSequence = 805963295;

    /**
     * The meterset to dose mapping sequence value.
     */
    public static final int MetersetToDoseMappingSequence = 805963296;

    /**
     * The expected in vivo measurement values sequence value.
     */
    public static final int ExpectedInVivoMeasurementValuesSequence = 805963297;

    /**
     * The expected in vivo measurement value index value.
     */
    public static final int ExpectedInVivoMeasurementValueIndex = 805963298;

    /**
     * The radiation dose in vivo measurement label value.
     */
    public static final int RadiationDoseInVivoMeasurementLabel = 805963299;

    /**
     * The radiation dose central axis displacement value.
     */
    public static final int RadiationDoseCentralAxisDisplacement = 805963300;

    /**
     * The radiation dose value value.
     */
    public static final int RadiationDoseValue = 805963301;

    /**
     * The radiation dose source to skin distance value.
     */
    public static final int RadiationDoseSourceToSkinDistance = 805963302;

    /**
     * The radiation dose measurement point coordinates value.
     */
    public static final int RadiationDoseMeasurementPointCoordinates = 805963303;

    /**
     * The radiation dose source to external contour distance value.
     */
    public static final int RadiationDoseSourceToExternalContourDistance = 805963304;

    /**
     * The rt tolerance set sequence value.
     */
    public static final int RTToleranceSetSequence = 805963305;

    /**
     * The rt tolerance set label value.
     */
    public static final int RTToleranceSetLabel = 805963306;

    /**
     * The attribute tolerance values sequence value.
     */
    public static final int AttributeToleranceValuesSequence = 805963307;

    /**
     * The tolerance value value.
     */
    public static final int ToleranceValue = 805963308;

    /**
     * The patient support position tolerance sequence value.
     */
    public static final int PatientSupportPositionToleranceSequence = 805963309;

    /**
     * The treatment time limit value.
     */
    public static final int TreatmentTimeLimit = 805963310;

    /**
     * The c arm photon electron control point sequence value.
     */
    public static final int CArmPhotonElectronControlPointSequence = 805963311;

    /**
     * The referenced rt radiation sequence value.
     */
    public static final int ReferencedRTRadiationSequence = 805963312;

    /**
     * The referenced rt instance sequence value.
     */
    public static final int ReferencedRTInstanceSequence = 805963313;

    /**
     * The referenced rt patient setup sequence value.
     */
    public static final int ReferencedRTPatientSetupSequence = 805963314;

    /**
     * The source to patient surface distance value.
     */
    public static final int SourceToPatientSurfaceDistance = 805963316;

    /**
     * The treatment machine special mode code sequence value.
     */
    public static final int TreatmentMachineSpecialModeCodeSequence = 805963317;

    /**
     * The intended number of fractions value.
     */
    public static final int IntendedNumberOfFractions = 805963318;

    /**
     * The rt radiation set intent value.
     */
    public static final int RTRadiationSetIntent = 805963319;

    /**
     * The rt radiation physical and geometric content detail flag value.
     */
    public static final int RTRadiationPhysicalAndGeometricContentDetailFlag = 805963320;

    /**
     * The rt record flag value.
     */
    public static final int RTRecordFlag = 805963321;

    /**
     * The treatment device identification sequence value.
     */
    public static final int TreatmentDeviceIdentificationSequence = 805963322;

    /**
     * The referenced rt physician intent sequence value.
     */
    public static final int ReferencedRTPhysicianIntentSequence = 805963323;

    /**
     * The cumulative meterset value.
     */
    public static final int CumulativeMeterset = 805963324;

    /**
     * The delivery rate value.
     */
    public static final int DeliveryRate = 805963325;

    /**
     * The delivery rate unit sequence value.
     */
    public static final int DeliveryRateUnitSequence = 805963326;

    /**
     * The treatment position sequence value.
     */
    public static final int TreatmentPositionSequence = 805963327;

    /**
     * The radiation source axis distance value.
     */
    public static final int RadiationSourceAxisDistance = 805963328;

    /**
     * The number of rt beam limiting devices value.
     */
    public static final int NumberOfRTBeamLimitingDevices = 805963329;

    /**
     * The rt beam limiting device proximal distance value.
     */
    public static final int RTBeamLimitingDeviceProximalDistance = 805963330;

    /**
     * The rt beam limiting device distal distance value.
     */
    public static final int RTBeamLimitingDeviceDistalDistance = 805963331;

    /**
     * The parallel rt beam delimiter device orientation label code sequence value.
     */
    public static final int ParallelRTBeamDelimiterDeviceOrientationLabelCodeSequence = 805963332;

    /**
     * The beam modifier orientation angle value.
     */
    public static final int BeamModifierOrientationAngle = 805963333;

    /**
     * The fixed rt beam delimiter device sequence value.
     */
    public static final int FixedRTBeamDelimiterDeviceSequence = 805963334;

    /**
     * The parallel rt beam delimiter device sequence value.
     */
    public static final int ParallelRTBeamDelimiterDeviceSequence = 805963335;

    /**
     * The number of parallel rt beam delimiters value.
     */
    public static final int NumberOfParallelRTBeamDelimiters = 805963336;

    /**
     * The parallel rt beam delimiter boundaries value.
     */
    public static final int ParallelRTBeamDelimiterBoundaries = 805963337;

    /**
     * The parallel rt beam delimiter positions value.
     */
    public static final int ParallelRTBeamDelimiterPositions = 805963338;

    /**
     * The rt beam limiting device offset value.
     */
    public static final int RTBeamLimitingDeviceOffset = 805963339;

    /**
     * The rt beam delimiter geometry sequence value.
     */
    public static final int RTBeamDelimiterGeometrySequence = 805963340;

    /**
     * The rt beam limiting device definition sequence value.
     */
    public static final int RTBeamLimitingDeviceDefinitionSequence = 805963341;

    /**
     * The parallel rt beam delimiter opening mode value.
     */
    public static final int ParallelRTBeamDelimiterOpeningMode = 805963342;

    /**
     * The parallel rt beam delimiter leaf mounting side value.
     */
    public static final int ParallelRTBeamDelimiterLeafMountingSide = 805963343;

    /**
     * The patient setup uid value.
     */
    public static final int PatientSetupUID = 805963344;

    /**
     * The wedge definition sequence value.
     */
    public static final int WedgeDefinitionSequence = 805963345;

    /**
     * The radiation beam wedge angle value.
     */
    public static final int RadiationBeamWedgeAngle = 805963346;

    /**
     * The radiation beam wedge thin edge distance value.
     */
    public static final int RadiationBeamWedgeThinEdgeDistance = 805963347;

    /**
     * The radiation beam effective wedge angle value.
     */
    public static final int RadiationBeamEffectiveWedgeAngle = 805963348;

    /**
     * The number of wedge positions value.
     */
    public static final int NumberOfWedgePositions = 805963349;

    /**
     * The rt beam limiting device opening sequence value.
     */
    public static final int RTBeamLimitingDeviceOpeningSequence = 805963350;

    /**
     * The number of rt beam limiting device openings value.
     */
    public static final int NumberOfRTBeamLimitingDeviceOpenings = 805963351;

    /**
     * The radiation dosimeter unit sequence value.
     */
    public static final int RadiationDosimeterUnitSequence = 805963352;

    /**
     * The rt device distance reference location code sequence value.
     */
    public static final int RTDeviceDistanceReferenceLocationCodeSequence = 805963353;

    /**
     * The radiation device configuration and commissioning key sequence value.
     */
    public static final int RadiationDeviceConfigurationAndCommissioningKeySequence = 805963354;

    /**
     * The patient support position parameter sequence value.
     */
    public static final int PatientSupportPositionParameterSequence = 805963355;

    /**
     * The patient support position specification method value.
     */
    public static final int PatientSupportPositionSpecificationMethod = 805963356;

    /**
     * The patient support position device parameter sequence value.
     */
    public static final int PatientSupportPositionDeviceParameterSequence = 805963357;

    /**
     * The device order index value.
     */
    public static final int DeviceOrderIndex = 805963358;

    /**
     * The patient support position parameter order index value.
     */
    public static final int PatientSupportPositionParameterOrderIndex = 805963359;

    /**
     * The patient support position device tolerance sequence value.
     */
    public static final int PatientSupportPositionDeviceToleranceSequence = 805963360;

    /**
     * The patient support position tolerance order index value.
     */
    public static final int PatientSupportPositionToleranceOrderIndex = 805963361;

    /**
     * The compensator definition sequence value.
     */
    public static final int CompensatorDefinitionSequence = 805963362;

    /**
     * The compensator map orientation value.
     */
    public static final int CompensatorMapOrientation = 805963363;

    /**
     * The compensator proximal thickness map value.
     */
    public static final int CompensatorProximalThicknessMap = 805963364;

    /**
     * The compensator distal thickness map value.
     */
    public static final int CompensatorDistalThicknessMap = 805963365;

    /**
     * The compensator base plane offset value.
     */
    public static final int CompensatorBasePlaneOffset = 805963366;

    /**
     * The compensator shape fabrication code sequence value.
     */
    public static final int CompensatorShapeFabricationCodeSequence = 805963367;

    /**
     * The compensator shape sequence value.
     */
    public static final int CompensatorShapeSequence = 805963368;

    /**
     * The radiation beam compensator milling tool diameter value.
     */
    public static final int RadiationBeamCompensatorMillingToolDiameter = 805963369;

    /**
     * The block definition sequence value.
     */
    public static final int BlockDefinitionSequence = 805963370;

    /**
     * The block edge data value.
     */
    public static final int BlockEdgeData = 805963371;

    /**
     * The block orientation value.
     */
    public static final int BlockOrientation = 805963372;

    /**
     * The radiation beam block thickness value.
     */
    public static final int RadiationBeamBlockThickness = 805963373;

    /**
     * The radiation beam block slab thickness value.
     */
    public static final int RadiationBeamBlockSlabThickness = 805963374;

    /**
     * The block edge data sequence value.
     */
    public static final int BlockEdgeDataSequence = 805963375;

    /**
     * The number of rt accessory holders value.
     */
    public static final int NumberOfRTAccessoryHolders = 805963376;

    /**
     * The general accessory definition sequence value.
     */
    public static final int GeneralAccessoryDefinitionSequence = 805963377;

    /**
     * The number of general accessories value.
     */
    public static final int NumberOfGeneralAccessories = 805963378;

    /**
     * The bolus definition sequence value.
     */
    public static final int BolusDefinitionSequence = 805963379;

    /**
     * The number of boluses value.
     */
    public static final int NumberOfBoluses = 805963380;

    /**
     * The equipment frame of reference uid value.
     */
    public static final int EquipmentFrameOfReferenceUID = 805963381;

    /**
     * The equipment frame of reference description value.
     */
    public static final int EquipmentFrameOfReferenceDescription = 805963382;

    /**
     * The equipment reference point coordinates sequence value.
     */
    public static final int EquipmentReferencePointCoordinatesSequence = 805963383;

    /**
     * The equipment reference point code sequence value.
     */
    public static final int EquipmentReferencePointCodeSequence = 805963384;

    /**
     * The rt beam limiting device angle value.
     */
    public static final int RTBeamLimitingDeviceAngle = 805963385;

    /**
     * The source roll angle value.
     */
    public static final int SourceRollAngle = 805963386;

    /**
     * The radiation generation mode sequence value.
     */
    public static final int RadiationGenerationModeSequence = 805963387;

    /**
     * The radiation generation mode label value.
     */
    public static final int RadiationGenerationModeLabel = 805963388;

    /**
     * The radiation generation mode description value.
     */
    public static final int RadiationGenerationModeDescription = 805963389;

    /**
     * The radiation generation mode machine code sequence value.
     */
    public static final int RadiationGenerationModeMachineCodeSequence = 805963390;

    /**
     * The radiation type code sequence value.
     */
    public static final int RadiationTypeCodeSequence = 805963391;

    /**
     * The nominal energy value.
     */
    public static final int NominalEnergy = 805963392;

    /**
     * The minimum nominal energy value.
     */
    public static final int MinimumNominalEnergy = 805963393;

    /**
     * The maximum nominal energy value.
     */
    public static final int MaximumNominalEnergy = 805963394;

    /**
     * The radiation fluence modifier code sequence value.
     */
    public static final int RadiationFluenceModifierCodeSequence = 805963395;

    /**
     * The energy unit code sequence value.
     */
    public static final int EnergyUnitCodeSequence = 805963396;

    /**
     * The number of radiation generation modes value.
     */
    public static final int NumberOfRadiationGenerationModes = 805963397;

    /**
     * The patient support devices sequence value.
     */
    public static final int PatientSupportDevicesSequence = 805963398;

    /**
     * The number of patient support devices value.
     */
    public static final int NumberOfPatientSupportDevices = 805963399;

    /**
     * The rt beam modifier definition distance value.
     */
    public static final int RTBeamModifierDefinitionDistance = 805963400;

    /**
     * The beam area limit sequence value.
     */
    public static final int BeamAreaLimitSequence = 805963401;

    /**
     * The referenced rt prescription sequence value.
     */
    public static final int ReferencedRTPrescriptionSequence = 805963402;

    /**
     * The dose value interpretation value.
     */
    public static final int DoseValueInterpretation = 805963403;

    /**
     * The treatment session uid value.
     */
    public static final int TreatmentSessionUID = 805963520;

    /**
     * The rt radiation usage value.
     */
    public static final int RTRadiationUsage = 805963521;

    /**
     * The referenced rt radiation set sequence value.
     */
    public static final int ReferencedRTRadiationSetSequence = 805963522;

    /**
     * The referenced rt radiation record sequence value.
     */
    public static final int ReferencedRTRadiationRecordSequence = 805963523;

    /**
     * The rt radiation set delivery number value.
     */
    public static final int RTRadiationSetDeliveryNumber = 805963524;

    /**
     * The clinical fraction number value.
     */
    public static final int ClinicalFractionNumber = 805963525;

    /**
     * The rt treatment fraction completion status value.
     */
    public static final int RTTreatmentFractionCompletionStatus = 805963526;

    /**
     * The rt radiation set usage value.
     */
    public static final int RTRadiationSetUsage = 805963527;

    /**
     * The treatment delivery continuation flag value.
     */
    public static final int TreatmentDeliveryContinuationFlag = 805963528;

    /**
     * The treatment record content origin value.
     */
    public static final int TreatmentRecordContentOrigin = 805963529;

    /**
     * The rt treatment termination status value.
     */
    public static final int RTTreatmentTerminationStatus = 805963540;

    /**
     * The rt treatment termination reason code sequence value.
     */
    public static final int RTTreatmentTerminationReasonCodeSequence = 805963541;

    /**
     * The machine specific treatment termination code sequence value.
     */
    public static final int MachineSpecificTreatmentTerminationCodeSequence = 805963542;

    /**
     * The rt radiation salvage record control point sequence value.
     */
    public static final int RTRadiationSalvageRecordControlPointSequence = 805963554;

    /**
     * The starting meterset value known flag value.
     */
    public static final int StartingMetersetValueKnownFlag = 805963555;

    /**
     * The treatment termination description value.
     */
    public static final int TreatmentTerminationDescription = 805963568;

    /**
     * The treatment tolerance violation sequence value.
     */
    public static final int TreatmentToleranceViolationSequence = 805963569;

    /**
     * The treatment tolerance violation category value.
     */
    public static final int TreatmentToleranceViolationCategory = 805963570;

    /**
     * The treatment tolerance violation attribute sequence value.
     */
    public static final int TreatmentToleranceViolationAttributeSequence = 805963571;

    /**
     * The treatment tolerance violation description value.
     */
    public static final int TreatmentToleranceViolationDescription = 805963572;

    /**
     * The treatment tolerance violation identification value.
     */
    public static final int TreatmentToleranceViolationIdentification = 805963573;

    /**
     * The treatment tolerance violation date time value.
     */
    public static final int TreatmentToleranceViolationDateTime = 805963574;

    /**
     * The recorded rt control point date time value.
     */
    public static final int RecordedRTControlPointDateTime = 805963578;

    /**
     * The referenced radiation rt control point index value.
     */
    public static final int ReferencedRadiationRTControlPointIndex = 805963579;

    /**
     * The alternate value sequence value.
     */
    public static final int AlternateValueSequence = 805963582;

    /**
     * The confirmation sequence value.
     */
    public static final int ConfirmationSequence = 805963583;

    /**
     * The interlock sequence value.
     */
    public static final int InterlockSequence = 805963584;

    /**
     * The interlock date time value.
     */
    public static final int InterlockDateTime = 805963585;

    /**
     * The interlock description value.
     */
    public static final int InterlockDescription = 805963586;

    /**
     * The interlock originating device sequence value.
     */
    public static final int InterlockOriginatingDeviceSequence = 805963587;

    /**
     * The interlock code sequence value.
     */
    public static final int InterlockCodeSequence = 805963588;

    /**
     * The interlock resolution code sequence value.
     */
    public static final int InterlockResolutionCodeSequence = 805963589;

    /**
     * The interlock resolution user sequence value.
     */
    public static final int InterlockResolutionUserSequence = 805963590;

    /**
     * The override date time value.
     */
    public static final int OverrideDateTime = 805963616;

    /**
     * The treatment tolerance violation type code sequence value.
     */
    public static final int TreatmentToleranceViolationTypeCodeSequence = 805963617;

    /**
     * The treatment tolerance violation cause code sequence value.
     */
    public static final int TreatmentToleranceViolationCauseCodeSequence = 805963618;

    /**
     * The measured meterset to dose mapping sequence value.
     */
    public static final int MeasuredMetersetToDoseMappingSequence = 805963634;

    /**
     * The referenced expected in vivo measurement value index value.
     */
    public static final int ReferencedExpectedInVivoMeasurementValueIndex = 805963635;

    /**
     * The dose measurement device code sequence value.
     */
    public static final int DoseMeasurementDeviceCodeSequence = 805963636;

    /**
     * The additional parameter recording instance sequence value.
     */
    public static final int AdditionalParameterRecordingInstanceSequence = 805963648;

    /**
     * The interlock origin description value.
     */
    public static final int InterlockOriginDescription = 805963651;

    /**
     * The rt patient position scope sequence value.
     */
    public static final int RTPatientPositionScopeSequence = 805963652;

    /**
     * The referenced treatment position group uid value.
     */
    public static final int ReferencedTreatmentPositionGroupUID = 805963653;

    /**
     * The radiation order index value.
     */
    public static final int RadiationOrderIndex = 805963654;

    /**
     * The omitted radiation sequence value.
     */
    public static final int OmittedRadiationSequence = 805963655;

    /**
     * The reason for omission code sequence value.
     */
    public static final int ReasonForOmissionCodeSequence = 805963656;

    /**
     * The rt delivery start patient position sequence value.
     */
    public static final int RTDeliveryStartPatientPositionSequence = 805963657;

    /**
     * The rt treatment preparation patient position sequence value.
     */
    public static final int RTTreatmentPreparationPatientPositionSequence = 805963658;

    /**
     * The referenced rt treatment preparation sequence value.
     */
    public static final int ReferencedRTTreatmentPreparationSequence = 805963659;

    /**
     * The referenced patient setup photo sequence value.
     */
    public static final int ReferencedPatientSetupPhotoSequence = 805963660;

    /**
     * The patient treatment preparation method code sequence value.
     */
    public static final int PatientTreatmentPreparationMethodCodeSequence = 805963661;

    /**
     * The patient treatment preparation procedure parameter description value.
     */
    public static final int PatientTreatmentPreparationProcedureParameterDescription = 805963662;

    /**
     * The patient treatment preparation device sequence value.
     */
    public static final int PatientTreatmentPreparationDeviceSequence = 805963663;

    /**
     * The patient treatment preparation procedure sequence value.
     */
    public static final int PatientTreatmentPreparationProcedureSequence = 805963664;

    /**
     * The patient treatment preparation procedure code sequence value.
     */
    public static final int PatientTreatmentPreparationProcedureCodeSequence = 805963665;

    /**
     * The patient treatment preparation method description value.
     */
    public static final int PatientTreatmentPreparationMethodDescription = 805963666;

    /**
     * The patient treatment preparation procedure parameter sequence value.
     */
    public static final int PatientTreatmentPreparationProcedureParameterSequence = 805963667;

    /**
     * The patient setup photo description value.
     */
    public static final int PatientSetupPhotoDescription = 805963668;

    /**
     * The patient treatment preparation procedure index value.
     */
    public static final int PatientTreatmentPreparationProcedureIndex = 805963669;

    /**
     * The referenced patient setup procedure index value.
     */
    public static final int ReferencedPatientSetupProcedureIndex = 805963670;

    /**
     * The rt radiation task sequence value.
     */
    public static final int RTRadiationTaskSequence = 805963671;

    /**
     * The rt patient position displacement sequence value.
     */
    public static final int RTPatientPositionDisplacementSequence = 805963672;

    /**
     * The rt patient position sequence value.
     */
    public static final int RTPatientPositionSequence = 805963673;

    /**
     * The displacement reference label value.
     */
    public static final int DisplacementReferenceLabel = 805963674;

    /**
     * The displacement matrix value.
     */
    public static final int DisplacementMatrix = 805963675;

    /**
     * The patient support displacement sequence value.
     */
    public static final int PatientSupportDisplacementSequence = 805963676;

    /**
     * The displacement reference location code sequence value.
     */
    public static final int DisplacementReferenceLocationCodeSequence = 805963677;

    /**
     * The rt radiation set delivery usage value.
     */
    public static final int RTRadiationSetDeliveryUsage = 805963678;

    /**
     * The referenced rt plan sequence value.
     */
    public static final int ReferencedRTPlanSequence = 806092802;

    /**
     * The referenced beam sequence value.
     */
    public static final int ReferencedBeamSequence = 806092804;

    /**
     * The referenced beam number value.
     */
    public static final int ReferencedBeamNumber = 806092806;

    /**
     * The referenced reference image number value.
     */
    public static final int ReferencedReferenceImageNumber = 806092807;

    /**
     * The start cumulative meterset weight value.
     */
    public static final int StartCumulativeMetersetWeight = 806092808;

    /**
     * The end cumulative meterset weight value.
     */
    public static final int EndCumulativeMetersetWeight = 806092809;

    /**
     * The referenced brachy application setup sequence value.
     */
    public static final int ReferencedBrachyApplicationSetupSequence = 806092810;

    /**
     * The referenced brachy application setup number value.
     */
    public static final int ReferencedBrachyApplicationSetupNumber = 806092812;

    /**
     * The referenced source number value.
     */
    public static final int ReferencedSourceNumber = 806092814;

    /**
     * The referenced fraction group sequence value.
     */
    public static final int ReferencedFractionGroupSequence = 806092832;

    /**
     * The referenced fraction group number value.
     */
    public static final int ReferencedFractionGroupNumber = 806092834;

    /**
     * The referenced verification image sequence value.
     */
    public static final int ReferencedVerificationImageSequence = 806092864;

    /**
     * The referenced reference image sequence value.
     */
    public static final int ReferencedReferenceImageSequence = 806092866;

    /**
     * The referenced dose reference sequence value.
     */
    public static final int ReferencedDoseReferenceSequence = 806092880;

    /**
     * The referenced dose reference number value.
     */
    public static final int ReferencedDoseReferenceNumber = 806092881;

    /**
     * The brachy referenced dose reference sequence value.
     */
    public static final int BrachyReferencedDoseReferenceSequence = 806092885;

    /**
     * The referenced structure set sequence value.
     */
    public static final int ReferencedStructureSetSequence = 806092896;

    /**
     * The referenced patient setup number value.
     */
    public static final int ReferencedPatientSetupNumber = 806092906;

    /**
     * The referenced dose sequence value.
     */
    public static final int ReferencedDoseSequence = 806092928;

    /**
     * The referenced tolerance table number value.
     */
    public static final int ReferencedToleranceTableNumber = 806092960;

    /**
     * The referenced bolus sequence value.
     */
    public static final int ReferencedBolusSequence = 806092976;

    /**
     * The referenced wedge number value.
     */
    public static final int ReferencedWedgeNumber = 806092992;

    /**
     * The referenced compensator number value.
     */
    public static final int ReferencedCompensatorNumber = 806093008;

    /**
     * The referenced block number value.
     */
    public static final int ReferencedBlockNumber = 806093024;

    /**
     * The referenced control point index value.
     */
    public static final int ReferencedControlPointIndex = 806093040;

    /**
     * The referenced control point sequence value.
     */
    public static final int ReferencedControlPointSequence = 806093042;

    /**
     * The referenced start control point index value.
     */
    public static final int ReferencedStartControlPointIndex = 806093044;

    /**
     * The referenced stop control point index value.
     */
    public static final int ReferencedStopControlPointIndex = 806093046;

    /**
     * The referenced range shifter number value.
     */
    public static final int ReferencedRangeShifterNumber = 806093056;

    /**
     * The referenced lateral spreading device number value.
     */
    public static final int ReferencedLateralSpreadingDeviceNumber = 806093058;

    /**
     * The referenced range modulator number value.
     */
    public static final int ReferencedRangeModulatorNumber = 806093060;

    /**
     * The omitted beam task sequence value.
     */
    public static final int OmittedBeamTaskSequence = 806093073;

    /**
     * The reason for omission value.
     */
    public static final int ReasonForOmission = 806093074;

    /**
     * The reason for omission description value.
     */
    public static final int ReasonForOmissionDescription = 806093075;

    /**
     * The prescription overview sequence value.
     */
    public static final int PrescriptionOverviewSequence = 806093076;

    /**
     * The total prescription dose value.
     */
    public static final int TotalPrescriptionDose = 806093077;

    /**
     * The plan overview sequence value.
     */
    public static final int PlanOverviewSequence = 806093078;

    /**
     * The plan overview index value.
     */
    public static final int PlanOverviewIndex = 806093079;

    /**
     * The referenced plan overview index value.
     */
    public static final int ReferencedPlanOverviewIndex = 806093080;

    /**
     * The number of fractions included value.
     */
    public static final int NumberOfFractionsIncluded = 806093081;

    /**
     * The dose calibration conditions sequence value.
     */
    public static final int DoseCalibrationConditionsSequence = 806093088;

    /**
     * The absorbed dose to meterset ratio value.
     */
    public static final int AbsorbedDoseToMetersetRatio = 806093089;

    /**
     * The delineated radiation field size value.
     */
    public static final int DelineatedRadiationFieldSize = 806093090;

    /**
     * The dose calibration conditions verified flag value.
     */
    public static final int DoseCalibrationConditionsVerifiedFlag = 806093091;

    /**
     * The calibration reference point depth value.
     */
    public static final int CalibrationReferencePointDepth = 806093092;

    /**
     * The gating beam hold transition sequence value.
     */
    public static final int GatingBeamHoldTransitionSequence = 806093093;

    /**
     * The beam hold transition value.
     */
    public static final int BeamHoldTransition = 806093094;

    /**
     * The beam hold transition date time value.
     */
    public static final int BeamHoldTransitionDateTime = 806093095;

    /**
     * The beam hold originating device sequence value.
     */
    public static final int BeamHoldOriginatingDeviceSequence = 806093096;

    /**
     * The beam hold transition trigger source value.
     */
    public static final int BeamHoldTransitionTriggerSource = 806093097;

    /**
     * The approval status value.
     */
    public static final int ApprovalStatus = 806223874;

    /**
     * The review date value.
     */
    public static final int ReviewDate = 806223876;

    /**
     * The review time value.
     */
    public static final int ReviewTime = 806223877;

    /**
     * The reviewer name value.
     */
    public static final int ReviewerName = 806223880;

    /**
     * The radiobiological dose effect sequence value.
     */
    public static final int RadiobiologicalDoseEffectSequence = 806354945;

    /**
     * The radiobiological dose effect flag value.
     */
    public static final int RadiobiologicalDoseEffectFlag = 806354946;

    /**
     * The effective dose calculation method category code sequence value.
     */
    public static final int EffectiveDoseCalculationMethodCategoryCodeSequence = 806354947;

    /**
     * The effective dose calculation method code sequence value.
     */
    public static final int EffectiveDoseCalculationMethodCodeSequence = 806354948;

    /**
     * The effective dose calculation method description value.
     */
    public static final int EffectiveDoseCalculationMethodDescription = 806354949;

    /**
     * The conceptual volume uid value.
     */
    public static final int ConceptualVolumeUID = 806354950;

    /**
     * The originating sop instance reference sequence value.
     */
    public static final int OriginatingSOPInstanceReferenceSequence = 806354951;

    /**
     * The conceptual volume constituent sequence value.
     */
    public static final int ConceptualVolumeConstituentSequence = 806354952;

    /**
     * The equivalent conceptual volume instance reference sequence value.
     */
    public static final int EquivalentConceptualVolumeInstanceReferenceSequence = 806354953;

    /**
     * The equivalent conceptual volumes sequence value.
     */
    public static final int EquivalentConceptualVolumesSequence = 806354954;

    /**
     * The referenced conceptual volume uid value.
     */
    public static final int ReferencedConceptualVolumeUID = 806354955;

    /**
     * The conceptual volume combination expression value.
     */
    public static final int ConceptualVolumeCombinationExpression = 806354956;

    /**
     * The conceptual volume constituent index value.
     */
    public static final int ConceptualVolumeConstituentIndex = 806354957;

    /**
     * The conceptual volume combination flag value.
     */
    public static final int ConceptualVolumeCombinationFlag = 806354958;

    /**
     * The conceptual volume combination description value.
     */
    public static final int ConceptualVolumeCombinationDescription = 806354959;

    /**
     * The conceptual volume segmentation defined flag value.
     */
    public static final int ConceptualVolumeSegmentationDefinedFlag = 806354960;

    /**
     * The conceptual volume segmentation reference sequence value.
     */
    public static final int ConceptualVolumeSegmentationReferenceSequence = 806354961;

    /**
     * The conceptual volume constituent segmentation reference sequence value.
     */
    public static final int ConceptualVolumeConstituentSegmentationReferenceSequence = 806354962;

    /**
     * The constituent conceptual volume uid value.
     */
    public static final int ConstituentConceptualVolumeUID = 806354963;

    /**
     * The derivation conceptual volume sequence value.
     */
    public static final int DerivationConceptualVolumeSequence = 806354964;

    /**
     * The source conceptual volume uid value.
     */
    public static final int SourceConceptualVolumeUID = 806354965;

    /**
     * The conceptual volume derivation algorithm sequence value.
     */
    public static final int ConceptualVolumeDerivationAlgorithmSequence = 806354966;

    /**
     * The conceptual volume description value.
     */
    public static final int ConceptualVolumeDescription = 806354967;

    /**
     * The source conceptual volume sequence value.
     */
    public static final int SourceConceptualVolumeSequence = 806354968;

    /**
     * The author identification sequence value.
     */
    public static final int AuthorIdentificationSequence = 806354969;

    /**
     * The manufacturer model version value.
     */
    public static final int ManufacturerModelVersion = 806354970;

    /**
     * The device alternate identifier value.
     */
    public static final int DeviceAlternateIdentifier = 806354971;

    /**
     * The device alternate identifier type value.
     */
    public static final int DeviceAlternateIdentifierType = 806354972;

    /**
     * The device alternate identifier format value.
     */
    public static final int DeviceAlternateIdentifierFormat = 806354973;

    /**
     * The segmentation creation template label value.
     */
    public static final int SegmentationCreationTemplateLabel = 806354974;

    /**
     * The segmentation template uid value.
     */
    public static final int SegmentationTemplateUID = 806354975;

    /**
     * The referenced segment reference index value.
     */
    public static final int ReferencedSegmentReferenceIndex = 806354976;

    /**
     * The segment reference sequence value.
     */
    public static final int SegmentReferenceSequence = 806354977;

    /**
     * The segment reference index value.
     */
    public static final int SegmentReferenceIndex = 806354978;

    /**
     * The direct segment reference sequence value.
     */
    public static final int DirectSegmentReferenceSequence = 806354979;

    /**
     * The combination segment reference sequence value.
     */
    public static final int CombinationSegmentReferenceSequence = 806354980;

    /**
     * The conceptual volume sequence value.
     */
    public static final int ConceptualVolumeSequence = 806354981;

    /**
     * The segmented rt accessory device sequence value.
     */
    public static final int SegmentedRTAccessoryDeviceSequence = 806354982;

    /**
     * The segment characteristics sequence value.
     */
    public static final int SegmentCharacteristicsSequence = 806354983;

    /**
     * The related segment characteristics sequence value.
     */
    public static final int RelatedSegmentCharacteristicsSequence = 806354984;

    /**
     * The segment characteristics precedence value.
     */
    public static final int SegmentCharacteristicsPrecedence = 806354985;

    /**
     * The rt segment annotation sequence value.
     */
    public static final int RTSegmentAnnotationSequence = 806354986;

    /**
     * The segment annotation category code sequence value.
     */
    public static final int SegmentAnnotationCategoryCodeSequence = 806354987;

    /**
     * The segment annotation type code sequence value.
     */
    public static final int SegmentAnnotationTypeCodeSequence = 806354988;

    /**
     * The device label value.
     */
    public static final int DeviceLabel = 806354989;

    /**
     * The device type code sequence value.
     */
    public static final int DeviceTypeCodeSequence = 806354990;

    /**
     * The segment annotation type modifier code sequence value.
     */
    public static final int SegmentAnnotationTypeModifierCodeSequence = 806354991;

    /**
     * The patient equipment relationship code sequence value.
     */
    public static final int PatientEquipmentRelationshipCodeSequence = 806354992;

    /**
     * The referenced fiducials uid value.
     */
    public static final int ReferencedFiducialsUID = 806354993;

    /**
     * The patient treatment orientation sequence value.
     */
    public static final int PatientTreatmentOrientationSequence = 806354994;

    /**
     * The user content label value.
     */
    public static final int UserContentLabel = 806354995;

    /**
     * The user content long label value.
     */
    public static final int UserContentLongLabel = 806354996;

    /**
     * The entity label value.
     */
    public static final int EntityLabel = 806354997;

    /**
     * The entity name value.
     */
    public static final int EntityName = 806354998;

    /**
     * The entity description value.
     */
    public static final int EntityDescription = 806354999;

    /**
     * The entity long label value.
     */
    public static final int EntityLongLabel = 806355000;

    /**
     * The device index value.
     */
    public static final int DeviceIndex = 806355001;

    /**
     * The rt treatment phase index value.
     */
    public static final int RTTreatmentPhaseIndex = 806355002;

    /**
     * The rt treatment phase uid value.
     */
    public static final int RTTreatmentPhaseUID = 806355003;

    /**
     * The rt prescription index value.
     */
    public static final int RTPrescriptionIndex = 806355004;

    /**
     * The rt segment annotation index value.
     */
    public static final int RTSegmentAnnotationIndex = 806355005;

    /**
     * The basis rt treatment phase index value.
     */
    public static final int BasisRTTreatmentPhaseIndex = 806355006;

    /**
     * The related rt treatment phase index value.
     */
    public static final int RelatedRTTreatmentPhaseIndex = 806355007;

    /**
     * The referenced rt treatment phase index value.
     */
    public static final int ReferencedRTTreatmentPhaseIndex = 806355008;

    /**
     * The referenced rt prescription index value.
     */
    public static final int ReferencedRTPrescriptionIndex = 806355009;

    /**
     * The referenced parent rt prescription index value.
     */
    public static final int ReferencedParentRTPrescriptionIndex = 806355010;

    /**
     * The manufacturer device identifier value.
     */
    public static final int ManufacturerDeviceIdentifier = 806355011;

    /**
     * The instance level referenced performed procedure step sequence value.
     */
    public static final int InstanceLevelReferencedPerformedProcedureStepSequence = 806355012;

    /**
     * The rt treatment phase intent presence flag value.
     */
    public static final int RTTreatmentPhaseIntentPresenceFlag = 806355013;

    /**
     * The radiotherapy treatment type value.
     */
    public static final int RadiotherapyTreatmentType = 806355014;

    /**
     * The teletherapy radiation type value.
     */
    public static final int TeletherapyRadiationType = 806355015;

    /**
     * The brachytherapy source type value.
     */
    public static final int BrachytherapySourceType = 806355016;

    /**
     * The referenced rt treatment phase sequence value.
     */
    public static final int ReferencedRTTreatmentPhaseSequence = 806355017;

    /**
     * The referenced direct segment instance sequence value.
     */
    public static final int ReferencedDirectSegmentInstanceSequence = 806355018;

    /**
     * The intended rt treatment phase sequence value.
     */
    public static final int IntendedRTTreatmentPhaseSequence = 806355019;

    /**
     * The intended phase start date value.
     */
    public static final int IntendedPhaseStartDate = 806355020;

    /**
     * The intended phase end date value.
     */
    public static final int IntendedPhaseEndDate = 806355021;

    /**
     * The rt treatment phase interval sequence value.
     */
    public static final int RTTreatmentPhaseIntervalSequence = 806355022;

    /**
     * The temporal relationship interval anchor value.
     */
    public static final int TemporalRelationshipIntervalAnchor = 806355023;

    /**
     * The minimum number of interval days value.
     */
    public static final int MinimumNumberOfIntervalDays = 806355024;

    /**
     * The maximum number of interval days value.
     */
    public static final int MaximumNumberOfIntervalDays = 806355025;

    /**
     * The pertinent sop classes in study value.
     */
    public static final int PertinentSOPClassesInStudy = 806355026;

    /**
     * The pertinent sop classes in series value.
     */
    public static final int PertinentSOPClassesInSeries = 806355027;

    /**
     * The rt prescription label value.
     */
    public static final int RTPrescriptionLabel = 806355028;

    /**
     * The rt physician intent predecessor sequence value.
     */
    public static final int RTPhysicianIntentPredecessorSequence = 806355029;

    /**
     * The rt treatment approach label value.
     */
    public static final int RTTreatmentApproachLabel = 806355030;

    /**
     * The rt physician intent sequence value.
     */
    public static final int RTPhysicianIntentSequence = 806355031;

    /**
     * The rt physician intent index value.
     */
    public static final int RTPhysicianIntentIndex = 806355032;

    /**
     * The rt treatment intent type value.
     */
    public static final int RTTreatmentIntentType = 806355033;

    /**
     * The rt physician intent narrative value.
     */
    public static final int RTPhysicianIntentNarrative = 806355034;

    /**
     * The rt protocol code sequence value.
     */
    public static final int RTProtocolCodeSequence = 806355035;

    /**
     * The reason for superseding value.
     */
    public static final int ReasonForSuperseding = 806355036;

    /**
     * The rt diagnosis code sequence value.
     */
    public static final int RTDiagnosisCodeSequence = 806355037;

    /**
     * The referenced rt physician intent index value.
     */
    public static final int ReferencedRTPhysicianIntentIndex = 806355038;

    /**
     * The rt physician intent input instance sequence value.
     */
    public static final int RTPhysicianIntentInputInstanceSequence = 806355039;

    /**
     * The rt anatomic prescription sequence value.
     */
    public static final int RTAnatomicPrescriptionSequence = 806355040;

    /**
     * The prior treatment dose description value.
     */
    public static final int PriorTreatmentDoseDescription = 806355041;

    /**
     * The prior treatment reference sequence value.
     */
    public static final int PriorTreatmentReferenceSequence = 806355042;

    /**
     * The dosimetric objective evaluation scope value.
     */
    public static final int DosimetricObjectiveEvaluationScope = 806355043;

    /**
     * The therapeutic role category code sequence value.
     */
    public static final int TherapeuticRoleCategoryCodeSequence = 806355044;

    /**
     * The therapeutic role type code sequence value.
     */
    public static final int TherapeuticRoleTypeCodeSequence = 806355045;

    /**
     * The conceptual volume optimization precedence value.
     */
    public static final int ConceptualVolumeOptimizationPrecedence = 806355046;

    /**
     * The conceptual volume category code sequence value.
     */
    public static final int ConceptualVolumeCategoryCodeSequence = 806355047;

    /**
     * The conceptual volume blocking constraint value.
     */
    public static final int ConceptualVolumeBlockingConstraint = 806355048;

    /**
     * The conceptual volume type code sequence value.
     */
    public static final int ConceptualVolumeTypeCodeSequence = 806355049;

    /**
     * The conceptual volume type modifier code sequence value.
     */
    public static final int ConceptualVolumeTypeModifierCodeSequence = 806355050;

    /**
     * The rt prescription sequence value.
     */
    public static final int RTPrescriptionSequence = 806355051;

    /**
     * The dosimetric objective sequence value.
     */
    public static final int DosimetricObjectiveSequence = 806355052;

    /**
     * The dosimetric objective type code sequence value.
     */
    public static final int DosimetricObjectiveTypeCodeSequence = 806355053;

    /**
     * The dosimetric objective uid value.
     */
    public static final int DosimetricObjectiveUID = 806355054;

    /**
     * The referenced dosimetric objective uid value.
     */
    public static final int ReferencedDosimetricObjectiveUID = 806355055;

    /**
     * The dosimetric objective parameter sequence value.
     */
    public static final int DosimetricObjectiveParameterSequence = 806355056;

    /**
     * The referenced dosimetric objectives sequence value.
     */
    public static final int ReferencedDosimetricObjectivesSequence = 806355057;

    /**
     * The absolute dosimetric objective flag value.
     */
    public static final int AbsoluteDosimetricObjectiveFlag = 806355059;

    /**
     * The dosimetric objective weight value.
     */
    public static final int DosimetricObjectiveWeight = 806355060;

    /**
     * The dosimetric objective purpose value.
     */
    public static final int DosimetricObjectivePurpose = 806355061;

    /**
     * The planning input information sequence value.
     */
    public static final int PlanningInputInformationSequence = 806355062;

    /**
     * The treatment site value.
     */
    public static final int TreatmentSite = 806355063;

    /**
     * The treatment site code sequence value.
     */
    public static final int TreatmentSiteCodeSequence = 806355064;

    /**
     * The fraction pattern sequence value.
     */
    public static final int FractionPatternSequence = 806355065;

    /**
     * The treatment technique notes value.
     */
    public static final int TreatmentTechniqueNotes = 806355066;

    /**
     * The prescription notes value.
     */
    public static final int PrescriptionNotes = 806355067;

    /**
     * The number of interval fractions value.
     */
    public static final int NumberOfIntervalFractions = 806355068;

    /**
     * The number of fractions value.
     */
    public static final int NumberOfFractions = 806355069;

    /**
     * The intended delivery duration value.
     */
    public static final int IntendedDeliveryDuration = 806355070;

    /**
     * The fractionation notes value.
     */
    public static final int FractionationNotes = 806355071;

    /**
     * The rt treatment technique code sequence value.
     */
    public static final int RTTreatmentTechniqueCodeSequence = 806355072;

    /**
     * The prescription notes sequence value.
     */
    public static final int PrescriptionNotesSequence = 806355073;

    /**
     * The fraction based relationship sequence value.
     */
    public static final int FractionBasedRelationshipSequence = 806355074;

    /**
     * The fraction based relationship interval anchor value.
     */
    public static final int FractionBasedRelationshipIntervalAnchor = 806355075;

    /**
     * The minimum hours between fractions value.
     */
    public static final int MinimumHoursBetweenFractions = 806355076;

    /**
     * The intended fraction start time value.
     */
    public static final int IntendedFractionStartTime = 806355077;

    /**
     * The intended start day of week value.
     */
    public static final int IntendedStartDayOfWeek = 806355078;

    /**
     * The weekday fraction pattern sequence value.
     */
    public static final int WeekdayFractionPatternSequence = 806355079;

    /**
     * The delivery time structure code sequence value.
     */
    public static final int DeliveryTimeStructureCodeSequence = 806355080;

    /**
     * The treatment site modifier code sequence value.
     */
    public static final int TreatmentSiteModifierCodeSequence = 806355081;

    /**
     * The robotic base location indicator value.
     */
    public static final int RoboticBaseLocationIndicator = 806355088;

    /**
     * The robotic path node set code sequence value.
     */
    public static final int RoboticPathNodeSetCodeSequence = 806355089;

    /**
     * The robotic node identifier value.
     */
    public static final int RoboticNodeIdentifier = 806355090;

    /**
     * The rt treatment source coordinates value.
     */
    public static final int RTTreatmentSourceCoordinates = 806355091;

    /**
     * The radiation source coordinate system yaw angle value.
     */
    public static final int RadiationSourceCoordinateSystemYawAngle = 806355092;

    /**
     * The radiation source coordinate system roll angle value.
     */
    public static final int RadiationSourceCoordinateSystemRollAngle = 806355093;

    /**
     * The radiation source coordinate system pitch angle value.
     */
    public static final int RadiationSourceCoordinateSystemPitchAngle = 806355094;

    /**
     * The robotic path control point sequence value.
     */
    public static final int RoboticPathControlPointSequence = 806355095;

    /**
     * The tomotherapeutic control point sequence value.
     */
    public static final int TomotherapeuticControlPointSequence = 806355096;

    /**
     * The tomotherapeutic leaf open durations value.
     */
    public static final int TomotherapeuticLeafOpenDurations = 806355097;

    /**
     * The tomotherapeutic leaf initial closed durations value.
     */
    public static final int TomotherapeuticLeafInitialClosedDurations = 806355098;

    /**
     * The conceptual volume identification sequence value.
     */
    public static final int ConceptualVolumeIdentificationSequence = 806355104;

    /**
     * The arbitrary value.
     */
    public static final int Arbitrary = 1073741840;

    /**
     * The text comments value.
     */
    public static final int TextComments = 1073758208;

    /**
     * The results id value.
     */
    public static final int ResultsID = 1074266176;

    /**
     * The results id issuer value.
     */
    public static final int ResultsIDIssuer = 1074266178;

    /**
     * The referenced interpretation sequence value.
     */
    public static final int ReferencedInterpretationSequence = 1074266192;

    /**
     * The report production status trial value.
     */
    public static final int ReportProductionStatusTrial = 1074266367;

    /**
     * The interpretation recorded date value.
     */
    public static final int InterpretationRecordedDate = 1074266368;

    /**
     * The interpretation recorded time value.
     */
    public static final int InterpretationRecordedTime = 1074266369;

    /**
     * The interpretation recorder value.
     */
    public static final int InterpretationRecorder = 1074266370;

    /**
     * The reference to recorded sound value.
     */
    public static final int ReferenceToRecordedSound = 1074266371;

    /**
     * The interpretation transcription date value.
     */
    public static final int InterpretationTranscriptionDate = 1074266376;

    /**
     * The interpretation transcription time value.
     */
    public static final int InterpretationTranscriptionTime = 1074266377;

    /**
     * The interpretation transcriber value.
     */
    public static final int InterpretationTranscriber = 1074266378;

    /**
     * The interpretation text value.
     */
    public static final int InterpretationText = 1074266379;

    /**
     * The interpretation author value.
     */
    public static final int InterpretationAuthor = 1074266380;

    /**
     * The interpretation approver sequence value.
     */
    public static final int InterpretationApproverSequence = 1074266385;

    /**
     * The interpretation approval date value.
     */
    public static final int InterpretationApprovalDate = 1074266386;

    /**
     * The interpretation approval time value.
     */
    public static final int InterpretationApprovalTime = 1074266387;

    /**
     * The physician approving interpretation value.
     */
    public static final int PhysicianApprovingInterpretation = 1074266388;

    /**
     * The interpretation diagnosis description value.
     */
    public static final int InterpretationDiagnosisDescription = 1074266389;

    /**
     * The interpretation diagnosis code sequence value.
     */
    public static final int InterpretationDiagnosisCodeSequence = 1074266391;

    /**
     * The results distribution list sequence value.
     */
    public static final int ResultsDistributionListSequence = 1074266392;

    /**
     * The distribution name value.
     */
    public static final int DistributionName = 1074266393;

    /**
     * The distribution address value.
     */
    public static final int DistributionAddress = 1074266394;

    /**
     * The interpretation id value.
     */
    public static final int InterpretationID = 1074266624;

    /**
     * The interpretation id issuer value.
     */
    public static final int InterpretationIDIssuer = 1074266626;

    /**
     * The interpretation type id value.
     */
    public static final int InterpretationTypeID = 1074266640;

    /**
     * The interpretation status id value.
     */
    public static final int InterpretationStatusID = 1074266642;

    /**
     * The impressions value.
     */
    public static final int Impressions = 1074266880;

    /**
     * The results comments value.
     */
    public static final int ResultsComments = 1074282496;

    /**
     * The low energy detectors value.
     */
    public static final int LowEnergyDetectors = 1074790401;

    /**
     * The high energy detectors value.
     */
    public static final int HighEnergyDetectors = 1074790402;

    /**
     * The detector geometry sequence value.
     */
    public static final int DetectorGeometrySequence = 1074790404;

    /**
     * The threat roi voxel sequence value.
     */
    public static final int ThreatROIVoxelSequence = 1074794497;

    /**
     * The threat roi base value.
     */
    public static final int ThreatROIBase = 1074794500;

    /**
     * The threat roi extents value.
     */
    public static final int ThreatROIExtents = 1074794501;

    /**
     * The threat roi bitmap value.
     */
    public static final int ThreatROIBitmap = 1074794502;

    /**
     * The route segment id value.
     */
    public static final int RouteSegmentID = 1074794503;

    /**
     * The gantry type value.
     */
    public static final int GantryType = 1074794504;

    /**
     * The ooi owner type value.
     */
    public static final int OOIOwnerType = 1074794505;

    /**
     * The route segment sequence value.
     */
    public static final int RouteSegmentSequence = 1074794506;

    /**
     * The potential threat object id value.
     */
    public static final int PotentialThreatObjectID = 1074794512;

    /**
     * The threat sequence value.
     */
    public static final int ThreatSequence = 1074794513;

    /**
     * The threat category value.
     */
    public static final int ThreatCategory = 1074794514;

    /**
     * The threat category description value.
     */
    public static final int ThreatCategoryDescription = 1074794515;

    /**
     * The atd ability assessment value.
     */
    public static final int ATDAbilityAssessment = 1074794516;

    /**
     * The atd assessment flag value.
     */
    public static final int ATDAssessmentFlag = 1074794517;

    /**
     * The atd assessment probability value.
     */
    public static final int ATDAssessmentProbability = 1074794518;

    /**
     * The mass value.
     */
    public static final int Mass = 1074794519;

    /**
     * The density value.
     */
    public static final int Density = 1074794520;

    /**
     * The z effective value.
     */
    public static final int ZEffective = 1074794521;

    /**
     * The boarding pass id value.
     */
    public static final int BoardingPassID = 1074794522;

    /**
     * The center of mass value.
     */
    public static final int CenterOfMass = 1074794523;

    /**
     * The center of pto value.
     */
    public static final int CenterOfPTO = 1074794524;

    /**
     * The bounding polygon value.
     */
    public static final int BoundingPolygon = 1074794525;

    /**
     * The route segment start location id value.
     */
    public static final int RouteSegmentStartLocationID = 1074794526;

    /**
     * The route segment end location id value.
     */
    public static final int RouteSegmentEndLocationID = 1074794527;

    /**
     * The route segment location id type value.
     */
    public static final int RouteSegmentLocationIDType = 1074794528;

    /**
     * The abort reason value.
     */
    public static final int AbortReason = 1074794529;

    /**
     * The volume of pto value.
     */
    public static final int VolumeOfPTO = 1074794531;

    /**
     * The abort flag value.
     */
    public static final int AbortFlag = 1074794532;

    /**
     * The route segment start time value.
     */
    public static final int RouteSegmentStartTime = 1074794533;

    /**
     * The route segment end time value.
     */
    public static final int RouteSegmentEndTime = 1074794534;

    /**
     * The tdr type value.
     */
    public static final int TDRType = 1074794535;

    /**
     * The international route segment value.
     */
    public static final int InternationalRouteSegment = 1074794536;

    /**
     * The threat detection algorithm and version value.
     */
    public static final int ThreatDetectionAlgorithmAndVersion = 1074794537;

    /**
     * The assigned location value.
     */
    public static final int AssignedLocation = 1074794538;

    /**
     * The alarm decision time value.
     */
    public static final int AlarmDecisionTime = 1074794539;

    /**
     * The alarm decision value.
     */
    public static final int AlarmDecision = 1074794545;

    /**
     * The number of total objects value.
     */
    public static final int NumberOfTotalObjects = 1074794547;

    /**
     * The number of alarm objects value.
     */
    public static final int NumberOfAlarmObjects = 1074794548;

    /**
     * The pto representation sequence value.
     */
    public static final int PTORepresentationSequence = 1074794551;

    /**
     * The atd assessment sequence value.
     */
    public static final int ATDAssessmentSequence = 1074794552;

    /**
     * The tip type value.
     */
    public static final int TIPType = 1074794553;

    /**
     * The dicos version value.
     */
    public static final int DICOSVersion = 1074794554;

    /**
     * The ooi owner creation time value.
     */
    public static final int OOIOwnerCreationTime = 1074794561;

    /**
     * The ooi type value.
     */
    public static final int OOIType = 1074794562;

    /**
     * The ooi size value.
     */
    public static final int OOISize = 1074794563;

    /**
     * The acquisition status value.
     */
    public static final int AcquisitionStatus = 1074794564;

    /**
     * The basis materials code sequence value.
     */
    public static final int BasisMaterialsCodeSequence = 1074794565;

    /**
     * The phantom type value.
     */
    public static final int PhantomType = 1074794566;

    /**
     * The ooi owner sequence value.
     */
    public static final int OOIOwnerSequence = 1074794567;

    /**
     * The scan type value.
     */
    public static final int ScanType = 1074794568;

    /**
     * The itinerary id value.
     */
    public static final int ItineraryID = 1074794577;

    /**
     * The itinerary id type value.
     */
    public static final int ItineraryIDType = 1074794578;

    /**
     * The itinerary id assigning authority value.
     */
    public static final int ItineraryIDAssigningAuthority = 1074794579;

    /**
     * The route id value.
     */
    public static final int RouteID = 1074794580;

    /**
     * The route id assigning authority value.
     */
    public static final int RouteIDAssigningAuthority = 1074794581;

    /**
     * The inbound arrival type value.
     */
    public static final int InboundArrivalType = 1074794582;

    /**
     * The carrier id value.
     */
    public static final int CarrierID = 1074794584;

    /**
     * The carrier id assigning authority value.
     */
    public static final int CarrierIDAssigningAuthority = 1074794585;

    /**
     * The source orientation value.
     */
    public static final int SourceOrientation = 1074794592;

    /**
     * The source position value.
     */
    public static final int SourcePosition = 1074794593;

    /**
     * The belt height value.
     */
    public static final int BeltHeight = 1074794594;

    /**
     * The algorithm routing code sequence value.
     */
    public static final int AlgorithmRoutingCodeSequence = 1074794596;

    /**
     * The transport classification value.
     */
    public static final int TransportClassification = 1074794599;

    /**
     * The ooi type descriptor value.
     */
    public static final int OOITypeDescriptor = 1074794600;

    /**
     * The total processing time value.
     */
    public static final int TotalProcessingTime = 1074794601;

    /**
     * The detector calibration data value.
     */
    public static final int DetectorCalibrationData = 1074794604;

    /**
     * The additional screening performed value.
     */
    public static final int AdditionalScreeningPerformed = 1074794605;

    /**
     * The additional inspection selection criteria value.
     */
    public static final int AdditionalInspectionSelectionCriteria = 1074794606;

    /**
     * The additional inspection method sequence value.
     */
    public static final int AdditionalInspectionMethodSequence = 1074794607;

    /**
     * The ait device type value.
     */
    public static final int AITDeviceType = 1074794608;

    /**
     * The qr measurements sequence value.
     */
    public static final int QRMeasurementsSequence = 1074794609;

    /**
     * The target material sequence value.
     */
    public static final int TargetMaterialSequence = 1074794610;

    /**
     * The snr threshold value.
     */
    public static final int SNRThreshold = 1074794611;

    /**
     * The image scale representation value.
     */
    public static final int ImageScaleRepresentation = 1074794613;

    /**
     * The referenced pto sequence value.
     */
    public static final int ReferencedPTOSequence = 1074794614;

    /**
     * The referenced tdr instance sequence value.
     */
    public static final int ReferencedTDRInstanceSequence = 1074794615;

    /**
     * The pto location description value.
     */
    public static final int PTOLocationDescription = 1074794616;

    /**
     * The anomaly locator indicator sequence value.
     */
    public static final int AnomalyLocatorIndicatorSequence = 1074794617;

    /**
     * The anomaly locator indicator value.
     */
    public static final int AnomalyLocatorIndicator = 1074794618;

    /**
     * The pto region sequence value.
     */
    public static final int PTORegionSequence = 1074794619;

    /**
     * The inspection selection criteria value.
     */
    public static final int InspectionSelectionCriteria = 1074794620;

    /**
     * The secondary inspection method sequence value.
     */
    public static final int SecondaryInspectionMethodSequence = 1074794621;

    /**
     * The prcs to rcs orientation value.
     */
    public static final int PRCSToRCSOrientation = 1074794622;

    /**
     * The mac parameters sequence value.
     */
    public static final int MACParametersSequence = 1342046209;

    /**
     * The curve dimensions value.
     */
    public static final int CurveDimensions = 1342177285;

    /**
     * The number of points value.
     */
    public static final int NumberOfPoints = 1342177296;

    /**
     * The type of data value.
     */
    public static final int TypeOfData = 1342177312;

    /**
     * The curve description value.
     */
    public static final int CurveDescription = 1342177314;

    /**
     * The axis units value.
     */
    public static final int AxisUnits = 1342177328;

    /**
     * The axis labels value.
     */
    public static final int AxisLabels = 1342177344;

    /**
     * The data value representation value.
     */
    public static final int DataValueRepresentation = 1342177539;

    /**
     * The minimum coordinate value value.
     */
    public static final int MinimumCoordinateValue = 1342177540;

    /**
     * The maximum coordinate value value.
     */
    public static final int MaximumCoordinateValue = 1342177541;

    /**
     * The curve range value.
     */
    public static final int CurveRange = 1342177542;

    /**
     * The curve data descriptor value.
     */
    public static final int CurveDataDescriptor = 1342177552;

    /**
     * The coordinate start value value.
     */
    public static final int CoordinateStartValue = 1342177554;

    /**
     * The coordinate step value value.
     */
    public static final int CoordinateStepValue = 1342177556;

    /**
     * The curve activation layer value.
     */
    public static final int CurveActivationLayer = 1342181377;

    /**
     * The audio type value.
     */
    public static final int AudioType = 1342185472;

    /**
     * The audio sample format value.
     */
    public static final int AudioSampleFormat = 1342185474;

    /**
     * The number of channels value.
     */
    public static final int NumberOfChannels = 1342185476;

    /**
     * The number of samples value.
     */
    public static final int NumberOfSamples = 1342185478;

    /**
     * The sample rate value.
     */
    public static final int SampleRate = 1342185480;

    /**
     * The total time value.
     */
    public static final int TotalTime = 1342185482;

    /**
     * The audio sample data value.
     */
    public static final int AudioSampleData = 1342185484;

    /**
     * The audio comments value.
     */
    public static final int AudioComments = 1342185486;

    /**
     * The curve label value.
     */
    public static final int CurveLabel = 1342186752;

    /**
     * The curve referenced overlay sequence value.
     */
    public static final int CurveReferencedOverlaySequence = 1342187008;

    /**
     * The curve referenced overlay group value.
     */
    public static final int CurveReferencedOverlayGroup = 1342187024;

    /**
     * The curve data value.
     */
    public static final int CurveData = 1342189568;

    /**
     * The shared functional groups sequence value.
     */
    public static final int SharedFunctionalGroupsSequence = 1375769129;

    /**
     * The per frame functional groups sequence value.
     */
    public static final int PerFrameFunctionalGroupsSequence = 1375769136;

    /**
     * The waveform sequence value.
     */
    public static final int WaveformSequence = 1409286400;

    /**
     * The channel minimum value value.
     */
    public static final int ChannelMinimumValue = 1409286416;

    /**
     * The channel maximum value value.
     */
    public static final int ChannelMaximumValue = 1409286418;

    /**
     * The waveform bits allocated value.
     */
    public static final int WaveformBitsAllocated = 1409290244;

    /**
     * The waveform sample interpretation value.
     */
    public static final int WaveformSampleInterpretation = 1409290246;

    /**
     * The waveform padding value value.
     */
    public static final int WaveformPaddingValue = 1409290250;

    /**
     * The waveform data value.
     */
    public static final int WaveformData = 1409290256;

    /**
     * The first order phase correction angle value.
     */
    public static final int FirstOrderPhaseCorrectionAngle = 1442840592;

    /**
     * The spectroscopy data value.
     */
    public static final int SpectroscopyData = 1442840608;

    /**
     * The overlay rows value.
     */
    public static final int OverlayRows = 1610612752;

    /**
     * The overlay columns value.
     */
    public static final int OverlayColumns = 1610612753;

    /**
     * The overlay planes value.
     */
    public static final int OverlayPlanes = 1610612754;

    /**
     * The number of frames in overlay value.
     */
    public static final int NumberOfFramesInOverlay = 1610612757;

    /**
     * The overlay description value.
     */
    public static final int OverlayDescription = 1610612770;

    /**
     * The overlay type value.
     */
    public static final int OverlayType = 1610612800;

    /**
     * The overlay subtype value.
     */
    public static final int OverlaySubtype = 1610612805;

    /**
     * The overlay origin value.
     */
    public static final int OverlayOrigin = 1610612816;

    /**
     * The image frame origin value.
     */
    public static final int ImageFrameOrigin = 1610612817;

    /**
     * The overlay plane origin value.
     */
    public static final int OverlayPlaneOrigin = 1610612818;

    /**
     * The overlay compression code value.
     */
    public static final int OverlayCompressionCode = 1610612832;

    /**
     * The overlay compression originator value.
     */
    public static final int OverlayCompressionOriginator = 1610612833;

    /**
     * The overlay compression label value.
     */
    public static final int OverlayCompressionLabel = 1610612834;

    /**
     * The overlay compression description value.
     */
    public static final int OverlayCompressionDescription = 1610612835;

    /**
     * The overlay compression step pointers value.
     */
    public static final int OverlayCompressionStepPointers = 1610612838;

    /**
     * The overlay repeat interval value.
     */
    public static final int OverlayRepeatInterval = 1610612840;

    /**
     * The overlay bits grouped value.
     */
    public static final int OverlayBitsGrouped = 1610612841;

    /**
     * The overlay bits allocated value.
     */
    public static final int OverlayBitsAllocated = 1610612992;

    /**
     * The overlay bit position value.
     */
    public static final int OverlayBitPosition = 1610612994;

    /**
     * The overlay format value.
     */
    public static final int OverlayFormat = 1610613008;

    /**
     * The overlay location value.
     */
    public static final int OverlayLocation = 1610613248;

    /**
     * The overlay code label value.
     */
    public static final int OverlayCodeLabel = 1610614784;

    /**
     * The overlay number of tables value.
     */
    public static final int OverlayNumberOfTables = 1610614786;

    /**
     * The overlay code table location value.
     */
    public static final int OverlayCodeTableLocation = 1610614787;

    /**
     * The overlay bits for code word value.
     */
    public static final int OverlayBitsForCodeWord = 1610614788;

    /**
     * The overlay activation layer value.
     */
    public static final int OverlayActivationLayer = 1610616833;

    /**
     * The overlay descriptor gray value.
     */
    public static final int OverlayDescriptorGray = 1610617088;

    /**
     * The overlay descriptor red value.
     */
    public static final int OverlayDescriptorRed = 1610617089;

    /**
     * The overlay descriptor green value.
     */
    public static final int OverlayDescriptorGreen = 1610617090;

    /**
     * The overlay descriptor blue value.
     */
    public static final int OverlayDescriptorBlue = 1610617091;

    /**
     * The overlays gray value.
     */
    public static final int OverlaysGray = 1610617344;

    /**
     * The overlays red value.
     */
    public static final int OverlaysRed = 1610617345;

    /**
     * The overlays green value.
     */
    public static final int OverlaysGreen = 1610617346;

    /**
     * The overlays blue value.
     */
    public static final int OverlaysBlue = 1610617347;

    /**
     * The roi area value.
     */
    public static final int ROIArea = 1610617601;

    /**
     * The roi mean value.
     */
    public static final int ROIMean = 1610617602;

    /**
     * The roi standard deviation value.
     */
    public static final int ROIStandardDeviation = 1610617603;

    /**
     * The overlay label value.
     */
    public static final int OverlayLabel = 1610618112;

    /**
     * The overlay data value.
     */
    public static final int OverlayData = 1610625024;

    /**
     * The overlay comments value.
     */
    public static final int OverlayComments = 1610629120;

    /**
     * The extended offset table value.
     */
    public static final int ExtendedOffsetTable = 2145386497;

    /**
     * The extended offset table lengths value.
     */
    public static final int ExtendedOffsetTableLengths = 2145386498;

    /**
     * The encapsulated pixel data value total length value.
     */
    public static final int EncapsulatedPixelDataValueTotalLength = 2145386499;

    /**
     * The float pixel data value.
     */
    public static final int FloatPixelData = 2145386504;

    /**
     * The double float pixel data value.
     */
    public static final int DoubleFloatPixelData = 2145386505;

    /**
     * The pixel data value.
     */
    public static final int PixelData = 2145386512;

    /**
     * The coefficients sdvn value.
     */
    public static final int CoefficientsSDVN = 2145386528;

    /**
     * The coefficients sdhn value.
     */
    public static final int CoefficientsSDHN = 2145386544;

    /**
     * The coefficients sddn value.
     */
    public static final int CoefficientsSDDN = 2145386560;

    /**
     * The variable pixel data value.
     */
    public static final int VariablePixelData = 2130706448;

    /**
     * The variable next data group value.
     */
    public static final int VariableNextDataGroup = 2130706449;

    /**
     * The variable coefficients sdvn value.
     */
    public static final int VariableCoefficientsSDVN = 2130706464;

    /**
     * The variable coefficients sdhn value.
     */
    public static final int VariableCoefficientsSDHN = 2130706480;

    /**
     * The variable coefficients sddn value.
     */
    public static final int VariableCoefficientsSDDN = 2130706496;

    /**
     * The digital signatures sequence value.
     */
    public static final int DigitalSignaturesSequence = -327686;

    /**
     * The data set trailing padding value.
     */
    public static final int DataSetTrailingPadding = -196612;

    /**
     * The item value.
     */
    public static final int Item = -73728;

    /**
     * The item delimitation item value.
     */
    public static final int ItemDelimitationItem = -73715;

    /**
     * The sequence delimitation item value.
     */
    public static final int SequenceDelimitationItem = -73507;

    /**
     * The instance creation date and time value.
     */
    public static final long InstanceCreationDateAndTime = 2251877123620883L;

    /**
     * The study date and time value.
     */
    public static final long StudyDateAndTime = 2251937253163056L;

    /**
     * The series date and time value.
     */
    public static final long SeriesDateAndTime = 2251941548130353L;

    /**
     * The acquisition date and time value.
     */
    public static final long AcquisitionDateAndTime = 2251945843097650L;

    /**
     * The content date and time value.
     */
    public static final long ContentDateAndTime = 2251950138064947L;

    /**
     * The overlay date and time value.
     */
    public static final long OverlayDateAndTime = 2251954433032244L;

    /**
     * The curve date and time value.
     */
    public static final long CurveDateAndTime = 2251958727999541L;

    /**
     * The patient birth date and time value.
     */
    public static final long PatientBirthDateAndTime = 4503805786849330L;

    /**
     * The date and time of secondary capture value.
     */
    public static final long DateAndTimeOfSecondaryCapture = 6773068938088468L;

    /**
     * The date and time of last calibration value.
     */
    public static final long DateAndTimeOfLastCalibration = 6775190651933185L;

    /**
     * The date and time of last detector calibration value.
     */
    public static final long DateAndTimeOfLastDetectorCalibration = 6878596284575758L;

    /**
     * The modified image date and time value.
     */
    public static final long ModifiedImageDateAndTime = 9064386746397701L;

    /**
     * The study verified date and time value.
     */
    public static final long StudyVerifiedDateAndTime = 14073963587174451L;

    /**
     * The study read date and time value.
     */
    public static final long StudyReadDateAndTime = 14073972177109045L;

    /**
     * The scheduled study start date and time value.
     */
    public static final long ScheduledStudyStartDateAndTime = 14091341024858113L;

    /**
     * The scheduled study stop date and time value.
     */
    public static final long ScheduledStudyStopDateAndTime = 14091409744334865L;

    /**
     * The study arrival date and time value.
     */
    public static final long StudyArrivalDateAndTime = 14091615902765121L;

    /**
     * The study completion date and time value.
     */
    public static final long StudyCompletionDateAndTime = 14091684622241873L;

    /**
     * The scheduled admission date and time value.
     */
    public static final long ScheduledAdmissionDateAndTime = 15762710368616475L;

    /**
     * The scheduled discharge date and time value.
     */
    public static final long ScheduledDischargeDateAndTime = 15762718958551069L;

    /**
     * The admitting date and time value.
     */
    public static final long AdmittingDateAndTime = 15762736138420257L;

    /**
     * The discharge date and time value.
     */
    public static final long DischargeDateAndTime = 15762804857897010L;

    /**
     * The scheduled procedure step start date and time value.
     */
    public static final long ScheduledProcedureStepStartDateAndTime = 18014407103610883L;

    /**
     * The scheduled procedure step end date and time value.
     */
    public static final long ScheduledProcedureStepEndDateAndTime = 18014415693545477L;

    /**
     * The performed procedure step start date and time value.
     */
    public static final long PerformedProcedureStepStartDateAndTime = 18016889594708549L;

    /**
     * The performed procedure step end date and time value.
     */
    public static final long PerformedProcedureStepEndDateAndTime = 18016941134316113L;

    /**
     * The issue date and time of imaging service request value.
     */
    public static final long IssueDateAndTimeOfImagingServiceRequest = 18049600065642501L;

    /**
     * The date and time value.
     */
    public static final long DateAndTime = 18191561619710242L;

    /**
     * The presentation creation date and time value.
     */
    public static final long PresentationCreationDateAndTime = 31525755744682115L;

    /**
     * The creation date and time value.
     */
    public static final long CreationDateAndTime = 2377900878683177040L;

    /**
     * The structure set date and time value.
     */
    public static final long StructureSetDateAndTime = 3460453398846242825L;

    /**
     * The treatment control point date and time value.
     */
    public static final long TreatmentControlPointDateAndTime = 3461016469058879525L;

    /**
     * The safe position exit date and time value.
     */
    public static final long SafePositionExitDateAndTime = 3461017834858479972L;

    /**
     * The safe position return date and time value.
     */
    public static final long SafePositionReturnDateAndTime = 3461017852038349160L;

    /**
     * The treatment date and time value.
     */
    public static final long TreatmentDateAndTime = 3461018857060696657L;

    /**
     * The rt plan date and time value.
     */
    public static final long RTPlanDateAndTime = 3461579290163412999L;

    /**
     * The source strength reference date and time value.
     */
    public static final long SourceStrengthReferenceDateAndTime = 3461581652395426350L;

    /**
     * The review date and time value.
     */
    public static final long ReviewDateAndTime = 3462705181480583173L;

    /**
     * The interpretation recorded date and time value.
     */
    public static final long InterpretationRecordedDateAndTime = 4613938918826967297L;

    /**
     * The interpretation transcription date and time value.
     */
    public static final long InterpretationTranscriptionDateAndTime = 4613938953186705673L;

    /**
     * The interpretation approval date and time value.
     */
    public static final long InterpretationApprovalDateAndTime = 4613938996136378643L;

    /**
     * The inv hex digits value.
     */
    private static final byte[] INV_HEX_DIGITS = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12,
            13, 14, 15 };

    /**
     * The hex digits value.
     */
    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F' };

    /**
     * Executes the short to hex string operation.
     *
     * @param n the n.
     * @return the operation result.
     */
    public static String shortToHexString(int n) {
        char[] s = { HEX_DIGITS[(n >>> 12) & 0xF], HEX_DIGITS[(n >>> 8) & 0xF], HEX_DIGITS[(n >>> 4) & 0xF],
                HEX_DIGITS[(n >>> 0) & 0xF] };
        return new String(s);
    }

    /**
     * Converts this value to hex string.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    public static String toHexString(int tag) {
        char[] s = { HEX_DIGITS[(tag >>> 28)], HEX_DIGITS[(tag >>> 24) & 0xF], HEX_DIGITS[(tag >>> 20) & 0xF],
                HEX_DIGITS[(tag >>> 16) & 0xF], HEX_DIGITS[(tag >>> 12) & 0xF], HEX_DIGITS[(tag >>> 8) & 0xF],
                HEX_DIGITS[(tag >>> 4) & 0xF], HEX_DIGITS[(tag >>> 0) & 0xF] };
        return new String(s);
    }

    /**
     * Converts this value to hex strings.
     *
     * @param vals the vals.
     * @return the operation result.
     */
    public static String[] toHexStrings(int[] vals) {
        int n = vals.length;
        String[] ss = new String[n];
        for (int i = 0; i < n; i++)
            ss[i] = toHexString(vals[i]);

        return ss;
    }

    /**
     * Converts this value to hex string.
     *
     * @param b the b.
     * @return the operation result.
     */
    public static String toHexString(byte[] b) {
        char[] s = new char[b.length << 1];
        for (int i = 0, j = 0; i < b.length; i++) {
            s[j++] = HEX_DIGITS[(b[i] >>> 4) & 0xF];
            s[j++] = HEX_DIGITS[b[i] & 0xF];
        }
        return new String(s);
    }

    /**
     * Executes the from hex string operation.
     *
     * @param s the s.
     * @return the operation result.
     */
    public static byte[] fromHexString(String s) {
        char[] chars = s.toCharArray();
        byte[] b = new byte[chars.length / 2];
        try {
            for (int i = 0, j = 0; i < b.length; i++)
                b[i] = (byte) ((INV_HEX_DIGITS[chars[j++]] << 4) | INV_HEX_DIGITS[chars[j++]]);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(s);
        }
        return b;
    }

    /**
     * Executes the int from hex string operation.
     *
     * @param s the s.
     * @return the operation result.
     */
    public static int intFromHexString(String s) {
        char[] chars = s.toCharArray();
        int val = 0;
        for (int i = 0; i < chars.length; i++)
            val = (val << 4) | INV_HEX_DIGITS[chars[i]];

        return val;
    }

    /**
     * Executes the from hex strings operation.
     *
     * @param ss the ss.
     * @return the operation result.
     */
    public static int[] fromHexStrings(String[] ss) {
        int n = ss.length;
        int[] vals = new int[n];
        for (int i = 0; i < n; i++)
            vals[i] = intFromHexString(ss[i]);

        return vals;
    }

    /**
     * Returns the string representation.
     *
     * @param tag the tag.
     * @return the string representation.
     */
    public static String toString(int tag) {
        char[] s = { '(', HEX_DIGITS[(tag >>> 28)], HEX_DIGITS[(tag >>> 24) & 0xF], HEX_DIGITS[(tag >>> 20) & 0xF],
                HEX_DIGITS[(tag >>> 16) & 0xF], ',', HEX_DIGITS[(tag >>> 12) & 0xF], HEX_DIGITS[(tag >>> 8) & 0xF],
                HEX_DIGITS[(tag >>> 4) & 0xF], HEX_DIGITS[(tag >>> 0) & 0xF], ')' };
        return new String(s);
    }

    /**
     * Executes the group number operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    public static int groupNumber(int tag) {
        return tag >>> 16;
    }

    /**
     * Executes the element number operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    public static int elementNumber(int tag) {
        return tag & 0xFFFF;
    }

    /**
     * Determines whether group length.
     *
     * @param tag the tag.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean isGroupLength(int tag) {
        return elementNumber(tag) == 0;
    }

    /**
     * Determines whether private creator.
     *
     * @param tag the tag.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean isPrivateCreator(int tag) {
        return (tag & 0x00010000) != 0 && (tag & 0x0000FF00) == 0 && (tag & 0x000000F0) != 0;
    }

    /**
     * Determines whether private group.
     *
     * @param tag the tag.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean isPrivateGroup(int tag) {
        return (tag & 0x00010000) != 0;
    }

    /**
     * Determines whether private tag.
     *
     * @param tag the tag.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean isPrivateTag(int tag) {
        return (tag & 0x00010000) != 0 && (tag & 0x0000FF00) != 0;
    }

    /**
     * Converts this value to tag.
     *
     * @param groupNumber   the group number.
     * @param elementNumber the element number.
     * @return the operation result.
     */
    public static int toTag(int groupNumber, int elementNumber) {
        return groupNumber << 16 | elementNumber;
    }

    /**
     * Converts this value to private tag.
     *
     * @param creatorTag    the creator tag.
     * @param elementNumber the element number.
     * @return the operation result.
     */
    public static int toPrivateTag(int creatorTag, int elementNumber) {
        return (creatorTag & 0xffff0000) | ((creatorTag & 0xff) << 8 | (elementNumber & 0xff));
    }

    /**
     * Executes the creator tag of operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    public static int creatorTagOf(int tag) {
        return (tag & 0xffff0000) | ((tag >>> 8) & 0xff);
    }

    /**
     * Executes the group length tag of operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    public static int groupLengthTagOf(int tag) {
        return tag & 0xffff0000;
    }

    /**
     * Determines whether item.
     *
     * @param tag the tag.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean isItem(int tag) {
        return tag == Item || tag == ItemDelimitationItem || tag == SequenceDelimitationItem;
    }

    /**
     * Determines whether file meta information.
     *
     * @param tag the tag.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean isFileMetaInformation(int tag) {
        return (tag & 0xffff0000) == 0x00020000;
    }

    /**
     * Executes the normalize repeating group operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    public static int normalizeRepeatingGroup(int tag) {
        int gg000000 = tag & 0xffe00000;
        return (gg000000 == 0x50000000 || gg000000 == 0x60000000) ? tag & 0xffe0ffff : tag;
    }

    /**
     * Executes the for name operation.
     *
     * @param name the name.
     * @return the operation result.
     */
    public static int forName(String name) {
        try {
            return Integer.parseInt(name, 16);
        } catch (NumberFormatException nfe) {
            try {
                return Tag.class.getField(name).getInt(null);
            } catch (Exception e) {
                return -1;
            }
        }
    }

    /**
     * Parses the tag path.
     *
     * @param tagPath the tag path.
     * @return the operation result.
     */
    public static int[] parseTagPath(String tagPath) {
        String[] names = Builder.split(tagPath, '.');
        int[] tags = new int[names.length];
        for (int i = 0; i < tags.length; i++)
            if ((tags[i] = forName(names[i])) == -1)
                throw new IllegalArgumentException("tagPath: " + tagPath);
        return tags;
    }

    /**
     * Converts this value to tags.
     *
     * @param tagOrKeywords the tag or keywords.
     * @return the operation result.
     */
    public static int[] toTags(String[] tagOrKeywords) {
        int[] tags = new int[tagOrKeywords.length];
        for (int i = 0; i < tags.length; i++) {
            tags[i] = toTag(tagOrKeywords[i]);
        }
        return tags;
    }

    /**
     * Converts this value to tag.
     *
     * @param tagOrKeyword the tag or keyword.
     * @return the operation result.
     */
    public static int toTag(String tagOrKeyword) {
        try {
            return Integer.parseInt(tagOrKeyword, 16);
        } catch (IllegalArgumentException e) {
            int tag = ElementDictionary.tagForKeyword(tagOrKeyword, null);
            if (tag == -1) {
                throw new IllegalArgumentException(tagOrKeyword);
            }
            return tag;
        }
    }

    /**
     * Defines the Type values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Type {

        /**
         * Constant for the standard value.
         */
        STANDARD,
        /**
         * Constant for the private value.
         */
        PRIVATE,
        /**
         * Constant for the private creator value.
         */
        PRIVATE_CREATOR;

        /**
         * Executes the type of operation.
         *
         * @param tag the tag.
         * @return the operation result.
         */
        public static Type typeOf(int tag) {
            return (tag & 0x00010000) != 0 ? (tag & 0x0000FF00) != 0 ? PRIVATE : PRIVATE_CREATOR : STANDARD;
        }

    }

}
