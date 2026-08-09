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

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;

/**
 * JSON writer for DICOM manifest structures.
 *
 * @author Kimi Liu
 */
public class JsonManifestSerializer {

    /**
     * The writer value.
     */
    private final Writer writer;

    /**
     * Creates a new instance.
     *
     * @param writer the writer.
     */
    public JsonManifestSerializer(Writer writer) {
        this.writer = writer;
    }

    /**
     * Writes a manifest as JSON.
     *
     * @param manifest the manifest.
     * @param version  the version.
     * @throws IOException if the operation cannot be completed.
     */
    public void write(Manifest manifest, String version) throws IOException {
        writer.append('{');
        writeName(Manifest.TAG_DOCUMENT_ROOT);
        writer.append('{');

        boolean first = true;
        first = writeAttribute(first, Manifest.VERSION, version);
        first = writeAttribute(first, Manifest.MANIFEST_UID, manifest.getUid());
        if (manifest instanceof ManifestQueryResult queryResult) {
            first = writeArchiveQuery(first, queryResult);
        } else {
            first = writePatients(first, manifest.getPatients().values());
        }

        writer.append("}}");
    }

    /**
     * Writes an archive query array.
     *
     * @param first       whether this is the first property.
     * @param queryResult the query result.
     * @return false after a property has been written.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean writeArchiveQuery(boolean first, ManifestQueryResult queryResult) throws IOException {
        writeComma(first);
        writeName(ArchiveParameters.TAG_ARC_QUERY);
        writer.append("[{");

        WadoParameters wado = queryResult.getWadoParameters();
        boolean queryFirst = true;
        queryFirst = writeAttribute(queryFirst, ArchiveParameters.ARCHIVE_ID, wado.getArchiveID());
        queryFirst = writeAttribute(queryFirst, ArchiveParameters.BASE_URL, wado.getBaseURL());
        if (wado.getQueryMode() != QueryMode.DEFAULT) {
            queryFirst = writeAttribute(queryFirst, ArchiveParameters.QUERY_MODE, wado.getQueryMode().name());
        }
        queryFirst = writeAttribute(queryFirst, ArchiveParameters.WEB_LOGIN, wado.getWebLogin());
        queryFirst = writeBooleanAttribute(
                queryFirst,
                WadoParameters.WADO_ONLY_SOP_UID,
                wado.isRequireOnlySOPInstanceUID());
        queryFirst = writeAttribute(
                queryFirst,
                ArchiveParameters.ADDITIONAL_PARAMETERS,
                wado.getAdditionalParameters());
        queryFirst = writeAttribute(queryFirst, ArchiveParameters.OVERRIDE_TAGS, wado.getOverrideDicomTagsList());
        queryFirst = writeHttpTags(queryFirst, wado.getHttpTaglist());
        queryFirst = writeViewerMessages(queryFirst, queryResult.getViewerMessages());
        writePatients(queryFirst, queryResult.getPatients().values());

        writer.append("}]");
        return false;
    }

    /**
     * Writes HTTP tags.
     *
     * @param first whether this is the first property.
     * @param tags  the HTTP tags.
     * @return false after a property has been written.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean writeHttpTags(boolean first, Collection<HttpTag> tags) throws IOException {
        if (tags == null || tags.isEmpty()) {
            return first;
        }
        writeComma(first);
        writeName(ArchiveParameters.TAG_HTTP_TAG);
        writer.append('[');

        boolean itemFirst = true;
        for (HttpTag tag : tags) {
            writeComma(itemFirst);
            writer.append('{');
            boolean propertyFirst = true;
            propertyFirst = writeAttribute(propertyFirst, "key", tag.getKey());
            writeAttribute(propertyFirst, "value", tag.getValue());
            writer.append('}');
            itemFirst = false;
        }

        writer.append(']');
        return false;
    }

    /**
     * Writes viewer messages.
     *
     * @param first    whether this is the first property.
     * @param messages the viewer messages.
     * @return false after a property has been written.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean writeViewerMessages(boolean first, Collection<ViewerMessage> messages) throws IOException {
        if (messages == null || messages.isEmpty()) {
            return first;
        }
        writeComma(first);
        writeName(ViewerMessage.TAG_DOCUMENT_MSG);
        writer.append('[');

        boolean itemFirst = true;
        for (ViewerMessage message : messages) {
            writeComma(itemFirst);
            writer.append('{');
            boolean propertyFirst = true;
            propertyFirst = writeAttribute(propertyFirst, ViewerMessage.MSG_ATTRIBUTE_TITLE, message.title());
            propertyFirst = writeAttribute(propertyFirst, ViewerMessage.MSG_ATTRIBUTE_DESC, message.message());
            writeAttribute(propertyFirst, ViewerMessage.MSG_ATTRIBUTE_LEVEL, message.level().name());
            writer.append('}');
            itemFirst = false;
        }

        writer.append(']');
        return false;
    }

    /**
     * Writes patients.
     *
     * @param first    whether this is the first property.
     * @param patients the patients.
     * @return false after a property has been written.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean writePatients(boolean first, Collection<ManifestPatient> patients) throws IOException {
        if (patients == null || patients.isEmpty()) {
            return first;
        }
        writeComma(first);
        writeName(ManifestXml.Level.PATIENT.getTagName());
        writer.append('[');

        ArrayList<ManifestPatient> sortedPatients = new ArrayList<>(patients);
        Collections.sort(sortedPatients);
        boolean itemFirst = true;
        for (ManifestPatient patient : sortedPatients) {
            writeComma(itemFirst);
            writePatient(patient);
            itemFirst = false;
        }

        writer.append(']');
        return false;
    }

    /**
     * Writes a patient.
     *
     * @param patient the patient.
     * @throws IOException if the operation cannot be completed.
     */
    private void writePatient(ManifestPatient patient) throws IOException {
        writer.append('{');
        boolean first = true;
        first = writeAttribute(first, keyword(Tag.PatientID), patient.getPatientID());
        first = writeAttribute(first, keyword(Tag.IssuerOfPatientID), patient.getIssuerOfPatientID());
        first = writeAttribute(first, keyword(Tag.PatientName), patient.getPatientName());
        first = writeAttribute(first, keyword(Tag.PatientBirthDate), patient.getPatientBirthDate());
        first = writeAttribute(first, keyword(Tag.PatientBirthTime), patient.getPatientBirthTime());
        first = writeAttribute(first, keyword(Tag.PatientSex), patient.getPatientSex());
        writeStudies(first, patient.getStudies());
        writer.append('}');
    }

    /**
     * Writes studies.
     *
     * @param first   whether this is the first property.
     * @param studies the studies.
     * @return false after a property has been written.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean writeStudies(boolean first, Collection<ManifestStudy> studies) throws IOException {
        if (studies == null || studies.isEmpty()) {
            return first;
        }
        writeComma(first);
        writeName(ManifestXml.Level.STUDY.getTagName());
        writer.append('[');

        ArrayList<ManifestStudy> sortedStudies = new ArrayList<>(studies);
        Collections.sort(sortedStudies);
        boolean itemFirst = true;
        for (ManifestStudy study : sortedStudies) {
            writeComma(itemFirst);
            writeStudy(study);
            itemFirst = false;
        }

        writer.append(']');
        return false;
    }

    /**
     * Writes a study.
     *
     * @param study the study.
     * @throws IOException if the operation cannot be completed.
     */
    private void writeStudy(ManifestStudy study) throws IOException {
        writer.append('{');
        boolean first = true;
        first = writeAttribute(first, keyword(Tag.StudyInstanceUID), study.getStudyInstanceUID());
        first = writeAttribute(first, keyword(Tag.StudyDescription), study.getStudyDescription());
        first = writeAttribute(first, keyword(Tag.StudyDate), study.getStudyDate());
        first = writeAttribute(first, keyword(Tag.StudyTime), study.getStudyTime());
        first = writeAttribute(first, keyword(Tag.AccessionNumber), study.getAccessionNumber());
        first = writeAttribute(first, keyword(Tag.StudyID), study.getStudyID());
        first = writeAttribute(first, keyword(Tag.ReferringPhysicianName), study.getReferringPhysicianName());
        writeSeries(first, study.getSeries());
        writer.append('}');
    }

    /**
     * Writes series entries.
     *
     * @param first  whether this is the first property.
     * @param series the series entries.
     * @return false after a property has been written.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean writeSeries(boolean first, Collection<ManifestSeries> series) throws IOException {
        if (series == null || series.isEmpty()) {
            return first;
        }
        writeComma(first);
        writeName(ManifestXml.Level.SERIES.getTagName());
        writer.append('[');

        ArrayList<ManifestSeries> sortedSeries = new ArrayList<>(series);
        Collections.sort(sortedSeries);
        boolean itemFirst = true;
        for (ManifestSeries item : sortedSeries) {
            writeComma(itemFirst);
            writeSeries(item);
            itemFirst = false;
        }

        writer.append(']');
        return false;
    }

    /**
     * Writes a series.
     *
     * @param series the series.
     * @throws IOException if the operation cannot be completed.
     */
    private void writeSeries(ManifestSeries series) throws IOException {
        writer.append('{');
        boolean first = true;
        first = writeAttribute(first, keyword(Tag.SeriesInstanceUID), series.getSeriesInstanceUID());
        first = writeAttribute(first, keyword(Tag.SeriesDescription), series.getSeriesDescription());
        first = writeAttribute(first, keyword(Tag.SeriesNumber), series.getSeriesNumber());
        first = writeAttribute(first, keyword(Tag.Modality), series.getModality());
        first = writeAttribute(first, "DirectDownloadThumbnail", series.getThumbnail());
        first = writeAttribute(first, "WadoTransferSyntaxUID", series.getWadoTransferSyntaxUID());
        if (series.getWadoCompression() > 0) {
            first = writeAttribute(first, "WadoCompressionRate", Integer.toString(series.getWadoCompression()));
        }
        writeInstances(first, series.getInstances());
        writer.append('}');
    }

    /**
     * Writes instances.
     *
     * @param first     whether this is the first property.
     * @param instances the instances.
     * @return false after a property has been written.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean writeInstances(boolean first, Collection<ManifestInstance> instances) throws IOException {
        if (instances == null || instances.isEmpty()) {
            return first;
        }
        writeComma(first);
        writeName(ManifestXml.Level.INSTANCE.getTagName());
        writer.append('[');

        ArrayList<ManifestInstance> sortedInstances = new ArrayList<>(instances);
        Collections.sort(sortedInstances);
        boolean itemFirst = true;
        for (ManifestInstance instance : sortedInstances) {
            writeComma(itemFirst);
            writeInstance(instance);
            itemFirst = false;
        }

        writer.append(']');
        return false;
    }

    /**
     * Writes an instance.
     *
     * @param instance the instance.
     * @throws IOException if the operation cannot be completed.
     */
    private void writeInstance(ManifestInstance instance) throws IOException {
        writer.append('{');
        boolean first = true;
        first = writeAttribute(first, keyword(Tag.SOPInstanceUID), instance.getSopInstanceUID());
        first = writeAttribute(first, keyword(Tag.SOPClassUID), instance.getSopClassUID());
        first = writeAttribute(first, keyword(Tag.TransferSyntaxUID), instance.getTransferSyntaxUID());
        first = writeAttribute(first, keyword(Tag.ImageComments), instance.getImageComments());
        first = writeAttribute(first, keyword(Tag.InstanceNumber), instance.getStringInstanceNumber());
        writeAttribute(first, "DirectDownloadFile", instance.getDirectDownloadFile());
        writer.append('}');
    }

    /**
     * Writes a boolean attribute.
     *
     * @param first whether this is the first property.
     * @param name  the name.
     * @param value the value.
     * @return false after a property has been written.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean writeBooleanAttribute(boolean first, String name, boolean value) throws IOException {
        writeComma(first);
        writeName(name);
        writer.append(Boolean.toString(value));
        return false;
    }

    /**
     * Writes an attribute.
     *
     * @param first whether this is the first property.
     * @param name  the name.
     * @param value the value.
     * @return false after a property has been written.
     * @throws IOException if the operation cannot be completed.
     */
    private boolean writeAttribute(boolean first, String name, String value) throws IOException {
        if (name == null || value == null || value.trim().isEmpty()) {
            return first;
        }
        writeComma(first);
        writeName(name);
        writeString(value);
        return false;
    }

    /**
     * Writes a property name.
     *
     * @param name the name.
     * @throws IOException if the operation cannot be completed.
     */
    private void writeName(String name) throws IOException {
        writeString(name);
        writer.append(':');
    }

    /**
     * Writes a JSON string.
     *
     * @param value the value.
     * @throws IOException if the operation cannot be completed.
     */
    private void writeString(String value) throws IOException {
        writer.append('"');
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            switch (current) {
                case '"' -> writer.append("\\\"");
                case '\\' -> writer.append("\\\\");
                case '\b' -> writer.append("\\b");
                case '\f' -> writer.append("\\f");
                case '\n' -> writer.append("\\n");
                case '\r' -> writer.append("\\r");
                case '\t' -> writer.append("\\t");
                default -> {
                    if (current < 0x20) {
                        writer.append(String.format("\\u%04x", (int) current));
                    } else {
                        writer.append(current);
                    }
                }
            }
        }
        writer.append('"');
    }

    /**
     * Writes a comma when needed.
     *
     * @param first whether this is the first property.
     * @throws IOException if the operation cannot be completed.
     */
    private void writeComma(boolean first) throws IOException {
        if (!first) {
            writer.append(',');
        }
    }

    /**
     * Gets a DICOM keyword.
     *
     * @param tag the tag.
     * @return the keyword.
     */
    private static String keyword(int tag) {
        String keyword = ElementDictionary.getStandardElementDictionary().keywordOf(tag);
        return keyword == null || keyword.isBlank() ? Tag.toString(tag) : keyword;
    }

}
