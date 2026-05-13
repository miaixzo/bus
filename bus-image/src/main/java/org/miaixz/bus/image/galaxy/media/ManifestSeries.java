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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.miaixz.bus.image.Tag;

/**
 * Series entry in a DICOM manifest.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ManifestSeries implements ManifestXml, Comparable<ManifestSeries> {

    /**
     * The min compression value.
     */
    private static final int MIN_COMPRESSION = 0;

    /**
     * The max compression value.
     */
    private static final int MAX_COMPRESSION = 100;

    /**
     * The series instance uid value.
     */
    private final String seriesInstanceUID;

    /**
     * The instances value.
     */
    private final Map<String, ManifestInstance> instances;

    /**
     * The series description value.
     */
    private String seriesDescription;

    /**
     * The modality value.
     */
    private String modality;

    /**
     * The series number value.
     */
    private String seriesNumber;

    /**
     * The wado transfer syntax uid value.
     */
    private String wadoTransferSyntaxUID;

    /**
     * The wado compression value.
     */
    private int wadoCompression = MIN_COMPRESSION;

    /**
     * The thumbnail value.
     */
    private String thumbnail;

    /**
     * Creates a new instance.
     *
     * @param seriesInstanceUID the series instance uid.
     */
    public ManifestSeries(String seriesInstanceUID) {
        this.seriesInstanceUID = Objects.requireNonNull(seriesInstanceUID, "Series Instance UID cannot be null");
        this.instances = new HashMap<>();
    }

    /**
     * Gets the series instance uid.
     *
     * @return the series instance uid.
     */
    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    /**
     * Gets the series description.
     *
     * @return the series description.
     */
    public String getSeriesDescription() {
        return seriesDescription;
    }

    /**
     * Sets the series description.
     *
     * @param seriesDescription the series description.
     */
    public void setSeriesDescription(String seriesDescription) {
        this.seriesDescription = seriesDescription;
    }

    /**
     * Gets the modality.
     *
     * @return the modality.
     */
    public String getModality() {
        return modality;
    }

    /**
     * Sets the modality.
     *
     * @param modality the modality.
     */
    public void setModality(String modality) {
        this.modality = modality;
    }

    /**
     * Gets the series number.
     *
     * @return the series number.
     */
    public String getSeriesNumber() {
        return seriesNumber;
    }

    /**
     * Sets the series number.
     *
     * @param seriesNumber the series number.
     */
    public void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = hasText(seriesNumber) ? seriesNumber.trim() : null;
    }

    /**
     * Gets the wado transfer syntax uid.
     *
     * @return the wado transfer syntax uid.
     */
    public String getWadoTransferSyntaxUID() {
        return wadoTransferSyntaxUID;
    }

    /**
     * Sets the wado transfer syntax uid.
     *
     * @param wadoTransferSyntaxUID the wado transfer syntax uid.
     */
    public void setWadoTransferSyntaxUID(String wadoTransferSyntaxUID) {
        this.wadoTransferSyntaxUID = wadoTransferSyntaxUID;
    }

    /**
     * Gets the wado compression.
     *
     * @return the wado compression.
     */
    public int getWadoCompression() {
        return wadoCompression;
    }

    /**
     * Sets the wado compression.
     *
     * @param wadoCompression the wado compression.
     */
    public void setWadoCompression(int wadoCompression) {
        this.wadoCompression = Math.max(MIN_COMPRESSION, Math.min(MAX_COMPRESSION, wadoCompression));
    }

    /**
     * Sets the wado compression.
     *
     * @param wadoCompression the wado compression.
     */
    public void setWadoCompression(String wadoCompression) {
        if (hasText(wadoCompression)) {
            try {
                setWadoCompression(Integer.parseInt(wadoCompression.trim()));
            } catch (NumberFormatException ignored) {
                // Keep the previous value when the manifest contains an invalid compression value.
            }
        }
    }

    /**
     * Gets the thumbnail.
     *
     * @return the thumbnail.
     */
    public String getThumbnail() {
        return thumbnail;
    }

    /**
     * Sets the thumbnail.
     *
     * @param thumbnail the thumbnail.
     */
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    /**
     * Adds the instance.
     *
     * @param instance the instance.
     */
    public void addInstance(ManifestInstance instance) {
        ManifestInstance.addInstance(instances, instance);
    }

    /**
     * Removes the instance.
     *
     * @param sopUID         the sop uid.
     * @param instanceNumber the instance number.
     * @return the operation result.
     */
    public ManifestInstance removeInstance(String sopUID, Integer instanceNumber) {
        return ManifestInstance.removeInstance(instances, sopUID, instanceNumber);
    }

    /**
     * Gets the instance.
     *
     * @param sopUID         the sop uid.
     * @param instanceNumber the instance number.
     * @return the instance.
     */
    public ManifestInstance getInstance(String sopUID, Integer instanceNumber) {
        return ManifestInstance.getInstance(instances, sopUID, instanceNumber);
    }

    /**
     * Gets the instances.
     *
     * @return the instances.
     */
    public Collection<ManifestInstance> getInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    /**
     * Gets the entry set.
     *
     * @return the entry set.
     */
    public Set<Entry<String, ManifestInstance>> getEntrySet() {
        return instances.entrySet();
    }

    /**
     * Determines whether empty.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isEmpty() {
        return instances.isEmpty();
    }

    /**
     * Executes the to xml operation.
     *
     * @param writer the writer.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void toXml(Writer writer) throws IOException {
        writer.append("\n<").append(ManifestXml.Level.SERIES.getTagName()).append(" ");
        ManifestXml.addXmlAttribute(Tag.SeriesInstanceUID, seriesInstanceUID, writer);
        ManifestXml.addXmlAttribute(Tag.SeriesDescription, seriesDescription, writer);
        ManifestXml.addXmlAttribute(Tag.SeriesNumber, seriesNumber, writer);
        ManifestXml.addXmlAttribute(Tag.Modality, modality, writer);
        ManifestXml.addXmlAttribute("DirectDownloadThumbnail", thumbnail, writer);
        ManifestXml.addXmlAttribute("WadoTransferSyntaxUID", wadoTransferSyntaxUID, writer);
        ManifestXml.addXmlAttribute("WadoCompressionRate", getCompressionRateString(), writer);
        writer.append(">");

        ArrayList<ManifestInstance> sortedInstances = new ArrayList<>(instances.values());
        Collections.sort(sortedInstances);
        for (ManifestInstance instance : sortedInstances) {
            instance.toXml(writer);
        }
        writer.append("\n</").append(ManifestXml.Level.SERIES.getTagName()).append(">");
    }

    /**
     * Executes the compare to operation.
     *
     * @param other the other.
     * @return the operation result.
     */
    @Override
    public int compareTo(ManifestSeries other) {
        int numberComparison = compareSeriesNumbers(seriesNumber, other.seriesNumber);
        return numberComparison != 0 ? numberComparison : seriesInstanceUID.compareTo(other.seriesInstanceUID);
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
        if (!(object instanceof ManifestSeries other)) {
            return false;
        }
        return seriesInstanceUID.equals(other.seriesInstanceUID);
    }

    /**
     * Returns the hash code.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(seriesInstanceUID);
    }

    /**
     * Gets the compression rate string.
     *
     * @return the compression rate string.
     */
    private String getCompressionRateString() {
        return wadoCompression > MIN_COMPRESSION ? String.valueOf(wadoCompression) : null;
    }

    /**
     * Executes the compare series numbers operation.
     *
     * @param first  the first.
     * @param second the second.
     * @return the operation result.
     */
    private static int compareSeriesNumbers(String first, String second) {
        Integer firstValue = parseInteger(first);
        Integer secondValue = parseInteger(second);
        if (firstValue != null && secondValue != null) {
            return firstValue.compareTo(secondValue);
        }
        if (firstValue == null && secondValue != null) {
            return 1;
        }
        return firstValue != null ? -1 : 0;
    }

    /**
     * Parses the integer.
     *
     * @param value the value.
     * @return the operation result.
     */
    private static Integer parseInteger(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Determines whether text.
     *
     * @param value the value.
     * @return true if the condition is met; otherwise false.
     */
    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
