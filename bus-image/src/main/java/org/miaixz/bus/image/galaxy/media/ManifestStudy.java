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
import java.text.Collator;
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
 * Study entry in a DICOM manifest.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ManifestStudy implements ManifestXml, Comparable<ManifestStudy> {

    /**
     * The study instance uid value.
     */
    private final String studyInstanceUID;

    /**
     * The series map value.
     */
    private final Map<String, ManifestSeries> seriesMap;

    /**
     * The study id value.
     */
    private String studyID;

    /**
     * The study description value.
     */
    private String studyDescription;

    /**
     * The study date value.
     */
    private String studyDate;

    /**
     * The study time value.
     */
    private String studyTime;

    /**
     * The accession number value.
     */
    private String accessionNumber;

    /**
     * The referring physician name value.
     */
    private String referringPhysicianName;

    /**
     * Creates a new instance.
     *
     * @param studyInstanceUID the study instance uid.
     */
    public ManifestStudy(String studyInstanceUID) {
        this.studyInstanceUID = Objects.requireNonNull(studyInstanceUID, "Study Instance UID cannot be null");
        this.seriesMap = new HashMap<>();
    }

    /**
     * Gets the study instance uid.
     *
     * @return the study instance uid.
     */
    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    /**
     * Gets the study id.
     *
     * @return the study id.
     */
    public String getStudyID() {
        return studyID;
    }

    /**
     * Sets the study id.
     *
     * @param studyID the study id.
     */
    public void setStudyID(String studyID) {
        this.studyID = studyID;
    }

    /**
     * Gets the study description.
     *
     * @return the study description.
     */
    public String getStudyDescription() {
        return studyDescription;
    }

    /**
     * Sets the study description.
     *
     * @param studyDescription the study description.
     */
    public void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
    }

    /**
     * Gets the study date.
     *
     * @return the study date.
     */
    public String getStudyDate() {
        return studyDate;
    }

    /**
     * Sets the study date.
     *
     * @param studyDate the study date.
     */
    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    /**
     * Gets the study time.
     *
     * @return the study time.
     */
    public String getStudyTime() {
        return studyTime;
    }

    /**
     * Sets the study time.
     *
     * @param studyTime the study time.
     */
    public void setStudyTime(String studyTime) {
        this.studyTime = studyTime;
    }

    /**
     * Gets the accession number.
     *
     * @return the accession number.
     */
    public String getAccessionNumber() {
        return accessionNumber;
    }

    /**
     * Sets the accession number.
     *
     * @param accessionNumber the accession number.
     */
    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    /**
     * Gets the referring physician name.
     *
     * @return the referring physician name.
     */
    public String getReferringPhysicianName() {
        return referringPhysicianName;
    }

    /**
     * Sets the referring physician name.
     *
     * @param referringPhysicianName the referring physician name.
     */
    public void setReferringPhysicianName(String referringPhysicianName) {
        this.referringPhysicianName = referringPhysicianName;
    }

    /**
     * Adds the series.
     *
     * @param series the series.
     */
    public void addSeries(ManifestSeries series) {
        if (series != null) {
            seriesMap.put(series.getSeriesInstanceUID(), series);
        }
    }

    /**
     * Removes the series.
     *
     * @param seriesUID the series uid.
     * @return the operation result.
     */
    public ManifestSeries removeSeries(String seriesUID) {
        return seriesMap.remove(seriesUID);
    }

    /**
     * Gets the series.
     *
     * @param seriesUID the series uid.
     * @return the series.
     */
    public ManifestSeries getSeries(String seriesUID) {
        return seriesMap.get(seriesUID);
    }

    /**
     * Gets the series.
     *
     * @return the series.
     */
    public Collection<ManifestSeries> getSeries() {
        return Collections.unmodifiableCollection(seriesMap.values());
    }

    /**
     * Gets the entry set.
     *
     * @return the entry set.
     */
    public Set<Entry<String, ManifestSeries>> getEntrySet() {
        return seriesMap.entrySet();
    }

    /**
     * Determines whether empty.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isEmpty() {
        return seriesMap.values().stream().allMatch(ManifestSeries::isEmpty);
    }

    /**
     * Executes the to xml operation.
     *
     * @param writer the writer.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void toXml(Writer writer) throws IOException {
        writer.append("\n<").append(ManifestXml.Level.STUDY.getTagName()).append(" ");
        ManifestXml.addXmlAttribute(Tag.StudyInstanceUID, studyInstanceUID, writer);
        ManifestXml.addXmlAttribute(Tag.StudyDescription, studyDescription, writer);
        ManifestXml.addXmlAttribute(Tag.StudyDate, studyDate, writer);
        ManifestXml.addXmlAttribute(Tag.StudyTime, studyTime, writer);
        ManifestXml.addXmlAttribute(Tag.AccessionNumber, accessionNumber, writer);
        ManifestXml.addXmlAttribute(Tag.StudyID, studyID, writer);
        ManifestXml.addXmlAttribute(Tag.ReferringPhysicianName, referringPhysicianName, writer);
        writer.append(">");

        ArrayList<ManifestSeries> sortedSeries = new ArrayList<>(seriesMap.values());
        Collections.sort(sortedSeries);
        for (ManifestSeries series : sortedSeries) {
            series.toXml(writer);
        }
        writer.append("\n</").append(ManifestXml.Level.STUDY.getTagName()).append(">");
    }

    /**
     * Executes the compare to operation.
     *
     * @param other the other.
     * @return the operation result.
     */
    @Override
    public int compareTo(ManifestStudy other) {
        int dateTimeComparison = compareStudyDateTimes(this, other);
        return dateTimeComparison != 0 ? dateTimeComparison
                : compareStudyDescriptions(studyDescription, other.studyDescription);
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
        if (!(object instanceof ManifestStudy other)) {
            return false;
        }
        return studyInstanceUID.equals(other.studyInstanceUID);
    }

    /**
     * Returns the hash code.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(studyInstanceUID);
    }

    /**
     * Executes the compare study date times operation.
     *
     * @param first  the first.
     * @param second the second.
     * @return the operation result.
     */
    private static int compareStudyDateTimes(ManifestStudy first, ManifestStudy second) {
        String firstDateTime = normalizeDateTime(first.studyDate, first.studyTime);
        String secondDateTime = normalizeDateTime(second.studyDate, second.studyTime);
        if (firstDateTime != null && secondDateTime != null) {
            return secondDateTime.compareTo(firstDateTime);
        }
        if (firstDateTime == null && secondDateTime != null) {
            return 1;
        }
        return firstDateTime != null ? -1 : 0;
    }

    /**
     * Executes the compare study descriptions operation.
     *
     * @param first  the first.
     * @param second the second.
     * @return the operation result.
     */
    private static int compareStudyDescriptions(String first, String second) {
        if (first != null && second != null) {
            return Collator.getInstance(Locale.getDefault()).compare(first, second);
        }
        if (first == null && second != null) {
            return 1;
        }
        return first != null ? -1 : 0;
    }

    /**
     * Executes the normalize date time operation.
     *
     * @param date the date.
     * @param time the time.
     * @return the operation result.
     */
    private static String normalizeDateTime(String date, String time) {
        String normalizedDate = digitsOnly(date);
        if (normalizedDate == null) {
            return null;
        }
        String normalizedTime = digitsOnly(time);
        return normalizedDate + (normalizedTime == null ? "" : normalizedTime);
    }

    /**
     * Executes the digits only operation.
     *
     * @param value the value.
     * @return the operation result.
     */
    private static String digitsOnly(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
    }

}
