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
package org.miaixz.bus.image.builtin;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.*;

/**
 * This class provides functionality to de-identify DICOM attributes based on specified options. It implements various
 * de-identification methods as defined in DICOM Part 15, Annex E. The de-identification process involves removing,
 * replacing, or remapping certain DICOM tags to protect patient privacy.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DeIdentifier {

    /**
     * DICOM tags that are always removed (X.1.1.1) unless explicitly retained by an option.
     */
    private static final int[] X = { Tag.AcquisitionComments, Tag.AcquisitionContextSequence,
            Tag.AcquisitionProtocolDescription, Tag.ActualHumanPerformersSequence, Tag.AdditionalPatientHistory,
            Tag.AddressTrial, Tag.AdmissionID, Tag.AdmittingDiagnosesCodeSequence, Tag.AdmittingDiagnosesDescription,
            Tag.Allergies, Tag.Arbitrary, Tag.AuthorObserverSequence, Tag.BranchOfService,
            Tag.CommentsOnThePerformedProcedureStep, Tag.ConfidentialityConstraintOnPatientDataDescription,
            Tag.ConsultingPhysicianIdentificationSequence, Tag.ContentCreatorIdentificationCodeSequence,
            Tag.ContentSequence, Tag.ContributionDescription, Tag.CountryOfResidence, Tag.CurrentObserverTrial,
            Tag.CurrentPatientLocation, Tag.CustodialOrganizationSequence, Tag.Date, // Content Item Attribute
            Tag.DateTime, // Content Item Attribute
            Tag.DataSetTrailingPadding, Tag.DerivationDescription, Tag.DigitalSignatureUID,
            Tag.DigitalSignaturesSequence, Tag.DischargeDiagnosisCodeSequence, Tag.DischargeDiagnosisDescription,
            Tag.DistributionAddress, Tag.DistributionName, Tag.EthnicGroup, Tag.FrameComments,
            Tag.GraphicAnnotationSequence, Tag.HumanPerformerCodeSequence, // missing in Part 15
            Tag.HumanPerformerName, Tag.HumanPerformerOrganization, Tag.IconImageSequence, Tag.IdentifyingComments,
            Tag.ImageComments, Tag.ImagePresentationComments, Tag.ImagingServiceRequestComments, Tag.Impressions,
            Tag.InsurancePlanIdentification, Tag.IntendedRecipientsOfResultsIdentificationSequence,
            Tag.InterpretationApproverSequence, Tag.InterpretationAuthor, Tag.InterpretationDiagnosisDescription,
            Tag.InterpretationIDIssuer, Tag.InterpretationRecorder, Tag.InterpretationText,
            Tag.InterpretationTranscriber, Tag.IssuerOfAccessionNumberSequence, // missing in Part 15
            Tag.IssuerOfAdmissionID, Tag.IssuerOfAdmissionIDSequence, // missing in Part 15
            Tag.IssuerOfPatientID, Tag.IssuerOfPatientIDQualifiersSequence, // missing in Part 15
            Tag.IssuerOfServiceEpisodeID, Tag.MAC, Tag.MedicalAlerts, Tag.MedicalRecordLocator, Tag.MilitaryRank,
            Tag.ModifiedAttributesSequence, Tag.ModifiedImageDescription, Tag.ModifyingDeviceID,
            Tag.NameOfPhysiciansReadingStudy, Tag.NamesOfIntendedRecipientsOfResults, Tag.Occupation,
            Tag.OperatorIdentificationSequence, Tag.OrderCallbackPhoneNumber, Tag.OrderCallbackTelecomInformation,
            Tag.OrderEnteredBy, Tag.OrderEntererLocation, Tag.OriginalAttributesSequence, Tag.OtherPatientIDs,
            Tag.OtherPatientIDsSequence, Tag.OtherPatientNames, Tag.ParticipantSequence, Tag.PatientAddress,
            Tag.PatientComments, Tag.PatientState, Tag.PatientTransportArrangements, Tag.PatientAge,
            Tag.PatientBirthName, Tag.PatientBirthTime, Tag.PatientInstitutionResidence,
            Tag.PatientInsurancePlanCodeSequence, Tag.PatientMotherBirthName, Tag.PatientPrimaryLanguageCodeSequence,
            Tag.PatientPrimaryLanguageModifierCodeSequence, Tag.PatientReligiousPreference, Tag.PatientSize,
            Tag.PatientSizeCodeSequence, // missing in Part 15
            Tag.PatientTelecomInformation, Tag.PatientTelephoneNumbers, Tag.PatientWeight, Tag.PerformedLocation,
            Tag.PerformedProcedureStepDescription, Tag.PerformedProcedureStepID,
            Tag.PerformingPhysicianIdentificationSequence, Tag.PerformingPhysicianName, Tag.PersonAddress,
            Tag.PersonIdentificationCodeSequence, Tag.PersonName, // Content Item Attribute
            Tag.PersonTelecomInformation, Tag.PersonTelephoneNumbers, Tag.PhysicianApprovingInterpretation,
            Tag.PhysiciansReadingStudyIdentificationSequence, Tag.PhysiciansOfRecord,
            Tag.PhysiciansOfRecordIdentificationSequence, Tag.PreMedication, Tag.PregnancyStatus,
            Tag.ReasonForOmissionDescription, Tag.ReasonForTheImagingServiceRequest, Tag.ReasonForStudy,
            Tag.ReferencedDigitalSignatureSequence, Tag.ReferencedPatientAliasSequence,
            Tag.ReferencedPatientPhotoSequence, Tag.ReferencedPatientSequence, Tag.ReferencedSOPInstanceMACSequence,
            Tag.ReferringPhysicianAddress, Tag.ReferringPhysicianIdentificationSequence,
            Tag.ReferringPhysicianTelephoneNumbers, Tag.RegionOfResidence, Tag.RequestAttributesSequence,
            Tag.RequestedContrastAgent, Tag.RequestedProcedureComments, Tag.RequestedProcedureID,
            Tag.RequestedProcedureLocation, Tag.RequestingPhysician, Tag.RequestingPhysicianIdentificationSequence, // missing
                                                                                                                    // in
                                                                                                                    // Part
                                                                                                                    // 15
            Tag.RequestingService, Tag.RequestingServiceCodeSequence, // missing in Part 15
            Tag.ResponsibleOrganization, Tag.ResponsiblePerson, Tag.ResultsComments,
            Tag.ResultsDistributionListSequence, Tag.ResultsIDIssuer, Tag.ScheduledHumanPerformersSequence,
            Tag.ScheduledPatientInstitutionResidence, Tag.ScheduledPerformingPhysicianIdentificationSequence,
            Tag.ScheduledPerformingPhysicianName, Tag.ScheduledProcedureStepDescription, Tag.SeriesDescription,
            Tag.SeriesDescriptionCodeSequence, // missing in Part 15
            Tag.ServiceEpisodeDescription, Tag.ServiceEpisodeID, Tag.SmokingStatus, Tag.SpecialNeeds, Tag.StudyComments,
            Tag.StudyDescription, Tag.StudyIDIssuer, Tag.TelephoneNumberTrial, Tag.TextComments, Tag.TextString,
            Tag.TextValue, // Content Item Attribute
            Tag.Time, // Content Item Attribute
            Tag.TopicAuthor, Tag.TopicKeywords, Tag.TopicSubject, Tag.TopicTitle, Tag.VerbalSourceTrial,
            Tag.VerbalSourceIdentifierCodeSequenceTrial, Tag.VisitComments };

    /**
     * DICOM tags related to institution identity that are removed if {@link Option#RetainInstitutionIdentityOption} is
     * not set.
     */
    private static final int[] X_INSTITUTION = { Tag.InstitutionAddress, Tag.InstitutionalDepartmentName,
            Tag.InstitutionalDepartmentTypeCodeSequence };

    /**
     * DICOM tags related to device identity that are removed if {@link Option#RetainDeviceIdentityOption} is not set.
     */
    private static final int[] X_DEVICE = { Tag.CassetteID, Tag.GantryID, Tag.GeneratorID, Tag.PerformedStationAETitle,
            Tag.PerformedStationGeographicLocationCodeSequence, Tag.PerformedStationName,
            Tag.PerformedStationNameCodeSequence, Tag.PlateID, Tag.ScheduledProcedureStepLocation,
            Tag.ScheduledStationAETitle, Tag.ScheduledStationGeographicLocationCodeSequence, Tag.ScheduledStationName,
            Tag.ScheduledStationNameCodeSequence, Tag.ScheduledStudyLocation, Tag.ScheduledStudyLocationAETitle,
            Tag.SourceSerialNumber, };

    /**
     * DICOM tags related to dates that are removed if
     * {@link Option#RetainLongitudinalTemporalInformationFullDatesOption} is not set.
     */
    private static final int[] X_DATES = { Tag.CurveDate, Tag.CurveTime, Tag.ExpectedCompletionDateTime,
            Tag.InstanceCoercionDateTime, Tag.InstanceCreationDate, // missing in Part 15
            Tag.InstanceCreationTime, // missing in Part 15
            Tag.LastMenstrualDate, Tag.ObservationDateTime, Tag.ObservationDateTrial, Tag.ObservationTimeTrial,
            Tag.OverlayDate, Tag.OverlayTime, Tag.PerformedProcedureStepEndDate, Tag.PerformedProcedureStepEndDateTime,
            Tag.PerformedProcedureStepEndTime, Tag.PerformedProcedureStepStartDate,
            Tag.PerformedProcedureStepStartDateTime, Tag.PerformedProcedureStepStartTime,
            Tag.ProcedureStepCancellationDateTime, Tag.ScheduledProcedureStepEndDate, Tag.ScheduledProcedureStepEndTime,
            Tag.ScheduledProcedureStepModificationDateTime, Tag.ScheduledProcedureStepStartDate,
            Tag.ScheduledProcedureStepStartDateTime, Tag.ScheduledProcedureStepStartTime, Tag.TimezoneOffsetFromUTC, };

    /**
     * DICOM tags that are zeroed out (Z.1.1.1) unless explicitly retained by an option.
     */
    private static final int[] Z = { Tag.AccessionNumber, Tag.ConsultingPhysicianName, Tag.ContentCreatorName,
            Tag.FillerOrderNumberImagingServiceRequest, Tag.PatientID, Tag.PatientSexNeutered, Tag.PatientBirthDate,
            Tag.PatientName, Tag.PatientSex, Tag.PlacerOrderNumberImagingServiceRequest, Tag.ReferringPhysicianName,
            Tag.RequestedProcedureDescription, Tag.ReviewerName, Tag.StudyID,
            Tag.VerifyingObserverIdentificationCodeSequence, };

    /**
     * DICOM tags related to institution identity that are zeroed out if {@link Option#RetainInstitutionIdentityOption}
     * is not set.
     */
    private static final int[] Z_INSTITUTION = { Tag.InstitutionCodeSequence, };

    /**
     * DICOM tags related to dates that are zeroed out if
     * {@link Option#RetainLongitudinalTemporalInformationFullDatesOption} is not set.
     */
    private static final int[] Z_DATES = { Tag.AcquisitionDate, Tag.AcquisitionTime, Tag.AdmittingDate,
            Tag.AdmittingTime, Tag.SeriesDate, Tag.SeriesTime, Tag.StudyDate, Tag.StudyTime, };

    /**
     * DICOM tags related to UIDs that are zeroed out if {@link Option#RetainUIDsOption} is not set.
     */
    private static final int[] Z_UID = { Tag.ReferencedPerformedProcedureStepSequence, Tag.ReferencedStudySequence };

    /**
     * DICOM tags that are replaced with dummy values (D.1.1.1) unless explicitly retained by an option.
     */
    private static final int[] D = { Tag.AcquisitionDeviceProcessingDescription, Tag.ContrastBolusAgent,
            Tag.DoseReferenceUID, Tag.OperatorsName, Tag.PersonName, Tag.ProtocolName, Tag.VerifyingObserverName,
            Tag.VerifyingOrganization };

    /**
     * DICOM tags related to device identity that are replaced with dummy values if
     * {@link Option#RetainDeviceIdentityOption} is not set.
     */
    private static final int[] D_DEVICE = { Tag.DetectorID, Tag.DeviceSerialNumber, Tag.StationName, };

    /**
     * DICOM tags related to institution identity that are replaced with dummy values if
     * {@link Option#RetainInstitutionIdentityOption} is not set.
     */
    private static final int[] D_INSTITUTION = { Tag.InstitutionName, };

    /**
     * DICOM tags related to dates that are replaced with dummy values if
     * {@link Option#RetainLongitudinalTemporalInformationFullDatesOption} is not set.
     */
    private static final int[] D_DATES = { Tag.AcquisitionDateTime, Tag.ContentDate, Tag.ContentTime,
            Tag.EndAcquisitionDateTime, Tag.StartAcquisitionDateTime, Tag.VerificationDateTime // missing in Part 15
    };

    /**
     * DICOM tags that are remapped (U.1.1.1) unless explicitly retained by an option.
     */
    private static final int[] U = { Tag.AffectedSOPInstanceUID, Tag.ConcatenationUID, Tag.DimensionOrganizationUID,
            Tag.FailedSOPInstanceUIDList, Tag.FiducialUID, Tag.FrameOfReferenceUID, Tag.InstanceCreatorUID,
            Tag.IrradiationEventUID, Tag.LargePaletteColorLookupTableUID, Tag.MediaStorageSOPInstanceUID,
            Tag.ObservationSubjectUIDTrial, Tag.ObservationUID, Tag.PaletteColorLookupTableUID,
            Tag.PresentationDisplayCollectionUID, Tag.PresentationSequenceCollectionUID,
            Tag.ReferencedFrameOfReferenceUID, Tag.ReferencedGeneralPurposeScheduledProcedureStepTransactionUID,
            Tag.ReferencedObservationUIDTrial, Tag.ReferencedSOPInstanceUID, Tag.ReferencedSOPInstanceUIDInFile,
            Tag.RelatedFrameOfReferenceUID, Tag.RequestedSOPInstanceUID, Tag.SeriesInstanceUID, Tag.SOPInstanceUID,
            Tag.StorageMediaFileSetUID, Tag.StudyInstanceUID, Tag.SynchronizationFrameOfReferenceUID, Tag.TargetUID,
            Tag.TemplateExtensionCreatorUID, Tag.TemplateExtensionOrganizationUID, Tag.TrackingUID, Tag.TransactionUID,
            Tag.UID, };

    /**
     * DICOM tags related to device UIDs that are remapped if {@link Option#RetainDeviceIdentityOption} is not set.
     */
    private static final int[] U_DEVICE = { Tag.DeviceUID, };

    /**
     * Constant string indicating that longitudinal temporal information is unmodified.
     */
    private static final String UNMODIFIED = "UNMODIFIED";
    /**
     * Constant string indicating that longitudinal temporal information is removed.
     */
    private static final String REMOVED = "REMOVED";
    /**
     * Constant string indicating a positive affirmation, used for PatientIdentityRemoved tag.
     */
    private static final String YES = "YES";

    /**
     * The set of de-identification options applied by this de-identifier.
     */
    private final EnumSet<Option> options;
    /**
     * Attributes containing dummy values used for replacement during de-identification.
     */
    private final Attributes dummyValues = new Attributes();
    /**
     * Combined array of tags to be zeroed out or replaced with dummy values.
     */
    private final int[] o;
    /**
     * Array of tags to be removed.
     */
    private int[] x = X;
    /**
     * Array of UID tags to be remapped.
     */
    private int[] u = U;

    /**
     * Constructs a {@code DeIdentifier} instance with the specified de-identification options. The
     * {@link Option#BasicApplicationConfidentialityProfile} is always included. Based on the provided options, the sets
     * of tags to be removed (X), zeroed/replaced (Z/D), and remapped (U) are dynamically adjusted.
     *
     * @param options An array of {@link Option} specifying the de-identification methods to apply.
     */
    public DeIdentifier(Option... options) {
        this.options = EnumSet.of(Option.BasicApplicationConfidentialityProfile, options);
        int[] z = Z;
        int[] d = D;
        if (!this.options.contains(Option.RetainDeviceIdentityOption)) {
            x = cat(x, X_DEVICE);
            d = cat(d, D_DEVICE);
            u = cat(u, U_DEVICE);
        }
        if (!this.options.contains(Option.RetainInstitutionIdentityOption)) {
            x = cat(x, X_INSTITUTION);
            z = cat(z, Z_INSTITUTION);
            d = cat(d, D_INSTITUTION);
        }
        if (!this.options.contains(Option.RetainLongitudinalTemporalInformationFullDatesOption)) {
            x = cat(x, X_DATES);
            z = cat(z, Z_DATES);
            d = cat(d, D_DATES);
        }
        if (!this.options.contains(Option.RetainUIDsOption)) {
            z = cat(z, Z_UID);
        }
        o = cat(z, d);
        Arrays.sort(x);
        Arrays.sort(u);
        Arrays.sort(o);
        initDummyValues(d);
    }

    /**
     * Generates a UUID hash from a given {@link IDWithIssuer}.
     *
     * @param pid The patient ID with issuer to hash.
     * @return A UUID string representing the hash of the patient ID.
     */
    private static String hash(IDWithIssuer pid) {
        return UUID.nameUUIDFromBytes(pid.toString().getBytes(Charset.UTF_8)).toString();
    }

    /**
     * Concatenates two integer arrays into a new array.
     *
     * @param a The first integer array.
     * @param b The second integer array.
     * @return A new array containing all elements from both input arrays.
     */
    private static int[] cat(int[] a, int[] b) {
        int[] dest = new int[a.length + b.length];
        System.arraycopy(a, 0, dest, 0, a.length);
        System.arraycopy(b, 0, dest, a.length, b.length);
        return dest;
    }

    /**
     * Provides a dummy string value for a given {@link VR} (Value Representation).
     *
     * @param vr The Value Representation for which to get a dummy value.
     * @return A dummy string value appropriate for the given VR.
     */
    private static String dummyValueFor(VR vr) {
        switch (vr) {
            case DA:
                return "19991111";

            case DT:
                return "19991111111111";

            case TM:
                return "111111";

            case IS:
            case DS:
                return "0";
        }
        return "REMOVED";
    }

    /**
     * Sets a specific dummy value for a given DICOM tag and Value Representation. This value will be used when
     * replacing the original attribute during de-identification.
     *
     * @param tag The DICOM tag to set the dummy value for.
     * @param vr  The Value Representation of the tag.
     * @param s   The dummy string value to set.
     */
    public void setDummyValue(int tag, VR vr, String s) {
        dummyValues.setString(tag, vr, s);
    }

    /**
     * De-identifies the given DICOM attributes based on the configured options. This method removes private attributes,
     * curve data, overlay data, and then applies removal, replacement with dummy values, and UID remapping as per the
     * options.
     *
     * @param attrs The {@link Attributes} object containing the DICOM dataset to de-identify.
     */
    public void deidentify(Attributes attrs) {
        IDWithIssuer pid = options.contains(Option.RetainPatientIDHashOption) ? IDWithIssuer.pidOf(attrs) : null;
        deidentifyItem(attrs);
        correct(attrs);
        if (pid != null)
            attrs.setString(Tag.PatientID, VR.LO, hash(pid));
        attrs.setString(Tag.PatientIdentityRemoved, VR.CS, YES);
        attrs.setString(
                Tag.LongitudinalTemporalInformationModified,
                VR.CS,
                options.contains(Option.RetainLongitudinalTemporalInformationFullDatesOption) ? UNMODIFIED : REMOVED);
        Sequence sq = attrs.ensureSequence(Tag.DeidentificationMethodCodeSequence, options.size());
        for (Option option : options) {
            sq.add(option.code.toItem());
        }
    }

    /**
     * Remaps a given UID if the {@link Option#RetainUIDsOption} is not set. Otherwise, the original UID is returned.
     *
     * @param uid The UID string to remap.
     * @return The remapped UID or the original UID if retained.
     */
    public String remapUID(String uid) {
        return options.contains(Option.RetainUIDsOption) ? uid : UID.remapUID(uid);
    }

    /**
     * Checks if the current de-identification options are equal to the specified options. The
     * {@link Option#BasicApplicationConfidentialityProfile} is implicitly included in the comparison.
     *
     * @param options An array of {@link Option} to compare against.
     * @return {@code true} if the options are equal, {@code false} otherwise.
     */
    public boolean equalOptions(Option... options) {
        return EnumSet.of(Option.BasicApplicationConfidentialityProfile, options).equals(this.options);
    }

    /**
     * Initializes the {@link #dummyValues} map with default dummy values for tags that are to be replaced. This
     * includes specific date/time tags if not retained.
     *
     * @param d The array of tags for which dummy values should be initialized.
     */
    private void initDummyValues(int[] d) {
        ElementDictionary dict = ElementDictionary.getStandardElementDictionary();
        for (int tag : d)
            initDummyValue(dict.vrOf(tag), tag);
        initDummyValue(VR.DA, Tag.SeriesDate);
        initDummyValue(VR.TM, Tag.SeriesTime);
    }

    /**
     * Initializes a single dummy value for a given VR and tag in the {@link #dummyValues} map.
     *
     * @param vr  The Value Representation of the tag.
     * @param tag The DICOM tag.
     * @return The object representing the dummy value set.
     */
    private Object initDummyValue(VR vr, int tag) {
        return dummyValues.setString(tag, vr, dummyValueFor(vr));
    }

    /**
     * Corrects specific DICOM attributes after de-identification, particularly for PET images if longitudinal temporal
     * information is not retained.
     *
     * @param attrs The {@link Attributes} object to correct.
     */
    private void correct(Attributes attrs) {
        if (!options.contains(Option.RetainLongitudinalTemporalInformationFullDatesOption)
                && UID.PositronEmissionTomographyImageStorage.equals(attrs.getString(Tag.SOPClassUID))) {
            attrs.setString(Tag.SeriesDate, VR.DA, dummyValues.getString(Tag.SeriesDate));
            attrs.setString(Tag.SeriesTime, VR.TM, dummyValues.getString(Tag.SeriesTime));
        }
    }

    /**
     * Recursively de-identifies attributes within a single DICOM item (or the root dataset). This method handles
     * private attributes, curve data, overlay data, and applies the configured removal, replacement, and UID remapping
     * rules.
     *
     * @param attrs The {@link Attributes} object representing the DICOM item to de-identify.
     */
    private void deidentifyItem(Attributes attrs) {
        attrs.removePrivateAttributes();
        attrs.removeCurveData();
        attrs.removeOverlayData();
        attrs.removeSelected(x);
        attrs.replaceSelected(dummyValues, o);
        if (!options.contains(Option.RetainUIDsOption))
            attrs.replaceUIDSelected(u);

        try {
            attrs.accept((attrs1, tag, vr, value) -> {
                if (value instanceof Sequence)
                    for (Attributes item : (Sequence) value)
                        deidentifyItem(item);
                return true;
            }, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Enumeration of de-identification options, corresponding to DICOM PS3.15 Annex E. Each option is associated with a
     * {@link Code} from {@link DeIdentificationMethod}.
     */
    public enum Option {

        /**
         * Basic Application Confidentiality Profile option.
         */
        BasicApplicationConfidentialityProfile(DeIdentificationMethod.BasicApplicationConfidentialityProfile),
        // CleanPixelDataOption(DeIdentificationMethod.CleanPixelDataOption),
        // CleanRecognizableVisualFeaturesOption(DeIdentificationMethod.CleanRecognizableVisualFeaturesOption),
        // CleanGraphicsOption(DeIdentificationMethod.CleanGraphicsOption),
        // CleanStructuredContentOption(DeIdentificationMethod.CleanStructuredContentOption),
        // CleanDescriptorsOption(DeIdentificationMethod.CleanDescriptorsOption),
        /**
         * Retain Longitudinal Temporal Information Full Dates Option.
         */
        RetainLongitudinalTemporalInformationFullDatesOption(
                DeIdentificationMethod.RetainLongitudinalTemporalInformationFullDatesOption),
        // RetainLongitudinalTemporalInformationModifiedDatesOption(
        // DeIdentificationMethod.RetainLongitudinalTemporalInformationModifiedDatesOption),
        // RetainPatientCharacteristicsOption(DeIdentificationMethod.RetainPatientCharacteristicsOption),
        /**
         * Retain Device Identity Option.
         */
        RetainDeviceIdentityOption(DeIdentificationMethod.RetainDeviceIdentityOption),
        /**
         * Retain Institution Identity Option.
         */
        RetainInstitutionIdentityOption(DeIdentificationMethod.RetainInstitutionIdentityOption),
        /**
         * Retain UIDs Option.
         */
        RetainUIDsOption(DeIdentificationMethod.RetainUIDsOption),
        // RetainSafePrivateOption(DeIdentificationMethod.RetainSafePrivateOption),
        /**
         * Retain Patient ID Hash Option.
         */
        RetainPatientIDHashOption(DeIdentificationMethod.RetainPatientIDHashOption);

        /**
         * The DICOM code associated with this de-identification option.
         */
        private final Code code;

        /**
         * Constructs an {@code Option} with the specified DICOM code.
         *
         * @param code The {@link Code} representing the de-identification method.
         */
        Option(Code code) {
            this.code = code;
        }
    }

}
