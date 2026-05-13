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
package org.miaixz.bus.image.galaxy.media;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Patient-centric DICOM manifest model.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Manifest implements ManifestXml {

    /**
     * The tag document root value.
     */
    public static final String TAG_DOCUMENT_ROOT = "manifest";

    /**
     * The manifest uid value.
     */
    public static final String MANIFEST_UID = "uid";

    /**
     * The version value.
     */
    public static final String VERSION = "version";

    /**
     * The patients value.
     */
    private final Map<String, ManifestPatient> patients;

    /**
     * The uid value.
     */
    private String uid;

    /**
     * Creates a new instance.
     */
    public Manifest() {
        this(null);
    }

    /**
     * Creates a new instance.
     *
     * @param patients the patients.
     */
    public Manifest(Collection<ManifestPatient> patients) {
        this.patients = new ConcurrentHashMap<>();
        if (patients != null) {
            patients.forEach(this::addPatient);
        }
    }

    /**
     * Gets the uid.
     *
     * @return the uid.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the uid.
     *
     * @param uid the uid.
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Adds the patient.
     *
     * @param patient the patient.
     */
    public void addPatient(ManifestPatient patient) {
        if (patient != null) {
            patients.put(patient.getPseudoPatientUID(), patient);
        }
    }

    /**
     * Removes the patient.
     *
     * @param patientID         the patient id.
     * @param issuerOfPatientID the issuer of patient id.
     * @return the operation result.
     */
    public ManifestPatient removePatient(String patientID, String issuerOfPatientID) {
        String key = buildPatientKey(patientID, issuerOfPatientID);
        return key == null ? null : patients.remove(key);
    }

    /**
     * Gets the patient.
     *
     * @param patientID         the patient id.
     * @param issuerOfPatientID the issuer of patient id.
     * @return the patient.
     */
    public ManifestPatient getPatient(String patientID, String issuerOfPatientID) {
        String key = buildPatientKey(patientID, issuerOfPatientID);
        return key == null ? null : patients.get(key);
    }

    /**
     * Gets the patients.
     *
     * @return the patients.
     */
    public Map<String, ManifestPatient> getPatients() {
        return Collections.unmodifiableMap(patients);
    }

    /**
     * Gets the patient count.
     *
     * @return the patient count.
     */
    public int getPatientCount() {
        return patients.size();
    }

    /**
     * Determines whether patients.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean hasPatients() {
        return !patients.isEmpty();
    }

    /**
     * Executes the query result operation.
     *
     * @param wadoParameters the wado parameters.
     * @return the operation result.
     */
    public static ManifestQueryResult queryResult(WadoParameters wadoParameters) {
        return new ManifestQueryResult(wadoParameters);
    }

    /**
     * Converts this value to query result.
     *
     * @param wadoParameters the wado parameters.
     * @return the operation result.
     */
    public ManifestQueryResult toQueryResult(WadoParameters wadoParameters) {
        ManifestQueryResult result = new ManifestQueryResult(patients.values(), wadoParameters);
        result.setUid(uid);
        return result;
    }

    /**
     * Removes the patient id.
     *
     * @param patientIds     the patient ids.
     * @param containsIssuer the contains issuer.
     */
    public void removePatientId(Collection<String> patientIds, boolean containsIssuer) {
        if (patientIds == null || patientIds.isEmpty()) {
            return;
        }
        if (containsIssuer) {
            patientIds.forEach(patients::remove);
            return;
        }
        List<String> pseudoUidsToRemove = patientIds.stream().map(this::findPseudoUidForPatientId)
                .filter(Objects::nonNull).toList();
        pseudoUidsToRemove.forEach(patients::remove);
    }

    /**
     * Removes the study uid.
     *
     * @param studyUids the study uids.
     */
    public void removeStudyUid(Collection<String> studyUids) {
        if (studyUids == null || studyUids.isEmpty()) {
            return;
        }
        patients.values().forEach(patient -> studyUids.forEach(patient::removeStudy));
        removeItemsWithoutElements();
    }

    /**
     * Removes the series uid.
     *
     * @param seriesUids the series uids.
     */
    public void removeSeriesUid(Collection<String> seriesUids) {
        if (seriesUids == null || seriesUids.isEmpty()) {
            return;
        }
        patients.values()
                .forEach(patient -> patient.getStudies().forEach(study -> seriesUids.forEach(study::removeSeries)));
        removeItemsWithoutElements();
    }

    /**
     * Removes the accession number.
     *
     * @param accessionNumbers the accession numbers.
     */
    public void removeAccessionNumber(Collection<String> accessionNumbers) {
        if (accessionNumbers == null || accessionNumbers.isEmpty()) {
            return;
        }
        patients.values().forEach(
                patient -> patient.getEntrySet()
                        .removeIf(entry -> !accessionNumbers.contains(entry.getValue().getAccessionNumber())));
        removeItemsWithoutElements();
    }

    /**
     * Removes the items without elements.
     */
    public void removeItemsWithoutElements() {
        patients.values().forEach(patient -> patient.getEntrySet().removeIf(studyEntry -> {
            ManifestStudy study = studyEntry.getValue();
            study.getEntrySet().removeIf(seriesEntry -> seriesEntry.getValue().isEmpty());
            return study.isEmpty();
        }));
        patients.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Executes the xml manifest operation.
     *
     * @param version the version.
     * @return the operation result.
     */
    public String xmlManifest(String version) {
        try {
            StringWriter writer = new StringWriter();
            writeManifest(writer, version);
            return writer.toString();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write DICOM manifest", exception);
        }
    }

    /**
     * Writes the manifest.
     *
     * @param writer  the writer.
     * @param version the version.
     * @throws IOException if the operation cannot be completed.
     */
    public void writeManifest(Writer writer, String version) throws IOException {
        writer.append("<?xml version=\"1.0\" encoding=\"").append(getCharsetEncoding()).append("\"?>");
        toXml(writer, version);
    }

    /**
     * Executes the to xml operation.
     *
     * @param writer the writer.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void toXml(Writer writer) throws IOException {
        toXml(writer, null);
    }

    /**
     * Executes the to xml operation.
     *
     * @param writer  the writer.
     * @param version the version.
     * @throws IOException if the operation cannot be completed.
     */
    public void toXml(Writer writer, String version) throws IOException {
        writer.append("\n<").append(TAG_DOCUMENT_ROOT).append(" ");
        ManifestXml.addXmlAttribute(VERSION, version, writer);
        ManifestXml.addXmlAttribute(MANIFEST_UID, uid, writer);
        writer.append(">");

        ArrayList<ManifestPatient> sortedPatients = new ArrayList<>(patients.values());
        Collections.sort(sortedPatients);
        for (ManifestPatient patient : sortedPatients) {
            patient.toXml(writer);
        }
        writer.append("\n</").append(TAG_DOCUMENT_ROOT).append(">");
    }

    /**
     * Finds the pseudo uid for patient id.
     *
     * @param patientID the patient id.
     * @return the operation result.
     */
    private String findPseudoUidForPatientId(String patientID) {
        return patients.values().stream().filter(patient -> patientID.equals(patient.getPatientID()))
                .map(ManifestPatient::getPseudoPatientUID).findFirst().orElse(null);
    }

    /**
     * Builds the patient key.
     *
     * @param patientID         the patient id.
     * @param issuerOfPatientID the issuer of patient id.
     * @return the operation result.
     */
    private String buildPatientKey(String patientID, String issuerOfPatientID) {
        if (patientID == null) {
            return null;
        }
        return issuerOfPatientID != null ? patientID + issuerOfPatientID : patientID;
    }

}
