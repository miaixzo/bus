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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.miaixz.bus.image.Tag;

/**
 * Patient entry in a DICOM manifest.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ManifestPatient implements ManifestXml, Comparable<ManifestPatient> {

    /**
     * The patient id value.
     */
    private final String patientID;

    /**
     * The issuer of patient id value.
     */
    private final String issuerOfPatientID;

    /**
     * The studies value.
     */
    private final Map<String, ManifestStudy> studies;

    /**
     * The patient name value.
     */
    private String patientName = "";

    /**
     * The patient birth date value.
     */
    private String patientBirthDate;

    /**
     * The patient birth time value.
     */
    private String patientBirthTime;

    /**
     * The patient sex value.
     */
    private String patientSex;

    /**
     * Creates a new instance.
     *
     * @param patientID         the patient id.
     * @param issuerOfPatientID the issuer of patient id.
     */
    public ManifestPatient(String patientID, String issuerOfPatientID) {
        this.patientID = Objects.requireNonNull(patientID, "Patient ID cannot be null");
        this.issuerOfPatientID = issuerOfPatientID;
        this.studies = new HashMap<>();
    }

    /**
     * Gets the patient id.
     *
     * @return the patient id.
     */
    public String getPatientID() {
        return patientID;
    }

    /**
     * Gets the issuer of patient id.
     *
     * @return the issuer of patient id.
     */
    public String getIssuerOfPatientID() {
        return issuerOfPatientID;
    }

    /**
     * Gets the pseudo patient uid.
     *
     * @return the pseudo patient uid.
     */
    public String getPseudoPatientUID() {
        return issuerOfPatientID != null ? patientID + issuerOfPatientID : patientID;
    }

    /**
     * Gets the patient name.
     *
     * @return the patient name.
     */
    public String getPatientName() {
        return patientName;
    }

    /**
     * Sets the patient name.
     *
     * @param patientName the patient name.
     */
    public void setPatientName(String patientName) {
        this.patientName = patientName == null ? "" : patientName;
    }

    /**
     * Gets the patient birth date.
     *
     * @return the patient birth date.
     */
    public String getPatientBirthDate() {
        return patientBirthDate;
    }

    /**
     * Sets the patient birth date.
     *
     * @param patientBirthDate the patient birth date.
     */
    public void setPatientBirthDate(String patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
    }

    /**
     * Gets the patient birth time.
     *
     * @return the patient birth time.
     */
    public String getPatientBirthTime() {
        return patientBirthTime;
    }

    /**
     * Sets the patient birth time.
     *
     * @param patientBirthTime the patient birth time.
     */
    public void setPatientBirthTime(String patientBirthTime) {
        this.patientBirthTime = patientBirthTime;
    }

    /**
     * Gets the patient sex.
     *
     * @return the patient sex.
     */
    public String getPatientSex() {
        return patientSex;
    }

    /**
     * Sets the patient sex.
     *
     * @param patientSex the patient sex.
     */
    public void setPatientSex(String patientSex) {
        this.patientSex = normalizePatientSex(patientSex);
    }

    /**
     * Adds the study.
     *
     * @param study the study.
     */
    public void addStudy(ManifestStudy study) {
        if (study != null) {
            studies.put(study.getStudyInstanceUID(), study);
        }
    }

    /**
     * Removes the study.
     *
     * @param studyUID the study uid.
     * @return the operation result.
     */
    public ManifestStudy removeStudy(String studyUID) {
        return studies.remove(studyUID);
    }

    /**
     * Gets the study.
     *
     * @param studyUID the study uid.
     * @return the study.
     */
    public ManifestStudy getStudy(String studyUID) {
        return studies.get(studyUID);
    }

    /**
     * Gets the studies.
     *
     * @return the studies.
     */
    public Collection<ManifestStudy> getStudies() {
        return Collections.unmodifiableCollection(studies.values());
    }

    /**
     * Gets the entry set.
     *
     * @return the entry set.
     */
    public Set<Entry<String, ManifestStudy>> getEntrySet() {
        return studies.entrySet();
    }

    /**
     * Determines whether empty.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isEmpty() {
        return studies.values().stream().allMatch(ManifestStudy::isEmpty);
    }

    /**
     * Executes the to xml operation.
     *
     * @param writer the writer.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void toXml(Writer writer) throws IOException {
        writer.append("\n<").append(ManifestXml.Level.PATIENT.getTagName()).append(" ");
        ManifestXml.addXmlAttribute(Tag.PatientID, patientID, writer);
        ManifestXml.addXmlAttribute(Tag.IssuerOfPatientID, issuerOfPatientID, writer);
        ManifestXml.addXmlAttribute(Tag.PatientName, patientName, writer);
        ManifestXml.addXmlAttribute(Tag.PatientBirthDate, patientBirthDate, writer);
        ManifestXml.addXmlAttribute(Tag.PatientBirthTime, patientBirthTime, writer);
        ManifestXml.addXmlAttribute(Tag.PatientSex, patientSex, writer);
        writer.append(">");

        ArrayList<ManifestStudy> sortedStudies = new ArrayList<>(studies.values());
        Collections.sort(sortedStudies);
        for (ManifestStudy study : sortedStudies) {
            study.toXml(writer);
        }
        writer.append("\n</").append(ManifestXml.Level.PATIENT.getTagName()).append(">");
    }

    /**
     * Executes the compare to operation.
     *
     * @param other the other.
     * @return the operation result.
     */
    @Override
    public int compareTo(ManifestPatient other) {
        return patientName.compareTo(other.patientName);
    }

    /**
     * Compares this instance with another object for equality.
     *
     * @param object the object.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ManifestPatient other)) {
            return false;
        }
        return Objects.equals(patientID, other.patientID) && Objects.equals(issuerOfPatientID, other.issuerOfPatientID);
    }

    /**
     * Returns the hash code.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(patientID, issuerOfPatientID);
    }

    /**
     * Executes the normalize patient sex operation.
     *
     * @param sex the sex.
     * @return the operation result.
     */
    private static String normalizePatientSex(String sex) {
        if (sex == null) {
            return null;
        }
        String upperSex = sex.toUpperCase(Locale.getDefault());
        if (upperSex.startsWith("M")) {
            return "M";
        }
        if (upperSex.startsWith("F")) {
            return "F";
        }
        return "O";
    }

}
