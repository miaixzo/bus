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
import java.util.List;
import java.util.Objects;

import org.miaixz.bus.core.lang.Symbol;

/**
 * Manifest query result enriched with WADO retrieval parameters and viewer message metadata.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ManifestQueryResult extends Manifest {

    /**
     * The wado parameters value.
     */
    private final WadoParameters wadoParameters;

    /**
     * The viewer messages value.
     */
    private final List<ViewerMessage> viewerMessages = new ArrayList<>();

    /**
     * Creates a new instance.
     *
     * @param wadoParameters the wado parameters.
     */
    public ManifestQueryResult(WadoParameters wadoParameters) {
        this(null, wadoParameters);
    }

    /**
     * Creates a new instance.
     *
     * @param patients       the patients.
     * @param wadoParameters the wado parameters.
     */
    public ManifestQueryResult(Collection<ManifestPatient> patients, WadoParameters wadoParameters) {
        super(patients);
        this.wadoParameters = Objects.requireNonNull(wadoParameters, "WADO parameters cannot be null");
    }

    /**
     * Gets the wado parameters.
     *
     * @return the wado parameters.
     */
    public WadoParameters getWadoParameters() {
        return wadoParameters;
    }

    /**
     * Gets the viewer message.
     *
     * @return the viewer message.
     */
    public ViewerMessage getViewerMessage() {
        return viewerMessages.isEmpty() ? null : viewerMessages.get(0);
    }

    /**
     * Gets the viewer messages.
     *
     * @return the viewer messages.
     */
    public List<ViewerMessage> getViewerMessages() {
        return Collections.unmodifiableList(viewerMessages);
    }

    /**
     * Adds the viewer message.
     *
     * @param viewerMessage the viewer message.
     */
    public void addViewerMessage(ViewerMessage viewerMessage) {
        if (viewerMessage != null) {
            viewerMessages.add(viewerMessage);
        }
    }

    /**
     * Sets the viewer message.
     *
     * @param viewerMessage the viewer message.
     */
    public void setViewerMessage(ViewerMessage viewerMessage) {
        viewerMessages.clear();
        addViewerMessage(viewerMessage);
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
    @Override
    public void toXml(Writer writer, String version) throws IOException {
        writer.append(Symbol.LF).append(Symbol.LT).append(TAG_DOCUMENT_ROOT).append(Symbol.SPACE);
        ManifestXml.addXmlAttribute(VERSION, version, writer);
        ManifestXml.addXmlAttribute(MANIFEST_UID, getUid(), writer);
        writer.append(ArchiveParameters.SCHEMA).append(Symbol.GT);

        writeArchiveQuery(writer);
        writer.append("\n</").append(TAG_DOCUMENT_ROOT).append(Symbol.GT);
    }

    /**
     * Writes the archive query.
     *
     * @param writer the writer.
     * @throws IOException if the operation cannot be completed.
     */
    private void writeArchiveQuery(Writer writer) throws IOException {
        writer.append(Symbol.LF).append(Symbol.LT).append(ArchiveParameters.TAG_ARC_QUERY).append(Symbol.SPACE);
        ManifestXml.addXmlAttribute(ArchiveParameters.ARCHIVE_ID, wadoParameters.getArchiveID(), writer);
        ManifestXml.addXmlAttribute(ArchiveParameters.BASE_URL, wadoParameters.getBaseURL(), writer);
        if (wadoParameters.getQueryMode() != QueryMode.DEFAULT) {
            ManifestXml.addXmlAttribute(ArchiveParameters.QUERY_MODE, wadoParameters.getQueryMode().name(), writer);
        }
        ManifestXml.addXmlAttribute(ArchiveParameters.WEB_LOGIN, wadoParameters.getWebLogin(), writer);
        ManifestXml.addXmlAttribute(
                WadoParameters.WADO_ONLY_SOP_UID,
                wadoParameters.isRequireOnlySOPInstanceUID(),
                writer);
        ManifestXml.addXmlAttribute(
                ArchiveParameters.ADDITIONAL_PARAMETERS,
                wadoParameters.getAdditionalParameters(),
                writer);
        ManifestXml.addXmlAttribute(ArchiveParameters.OVERRIDE_TAGS, wadoParameters.getOverrideDicomTagsList(), writer);
        writer.append(Symbol.GT);

        for (HttpTag tag : wadoParameters.getHttpTaglist()) {
            writeHttpTag(writer, tag);
        }
        for (ViewerMessage message : viewerMessages) {
            writeViewerMessage(writer, message);
        }
        ArrayList<ManifestPatient> sortedPatients = new ArrayList<>(getPatients().values());
        Collections.sort(sortedPatients);
        for (ManifestPatient patient : sortedPatients) {
            patient.toXml(writer);
        }
        writer.append("\n</").append(ArchiveParameters.TAG_ARC_QUERY).append(Symbol.GT);
    }

    /**
     * Writes an HTTP tag.
     *
     * @param writer the writer.
     * @param tag    the HTTP tag.
     * @throws IOException if the operation cannot be completed.
     */
    private static void writeHttpTag(Writer writer, HttpTag tag) throws IOException {
        writer.append(Symbol.LF).append(Symbol.LT).append(ArchiveParameters.TAG_HTTP_TAG).append(Symbol.SPACE);
        ManifestXml.addXmlAttribute("key", tag.getKey(), writer);
        ManifestXml.addXmlAttribute("value", tag.getValue(), writer);
        writer.append("/>");
    }

    /**
     * Writes a viewer message.
     *
     * @param writer  the writer.
     * @param message the message.
     * @throws IOException if the operation cannot be completed.
     */
    private static void writeViewerMessage(Writer writer, ViewerMessage message) throws IOException {
        writer.append(Symbol.LF).append(Symbol.LT).append(ViewerMessage.TAG_DOCUMENT_MSG).append(Symbol.SPACE);
        ManifestXml.addXmlAttribute(ViewerMessage.MSG_ATTRIBUTE_TITLE, message.title(), writer);
        ManifestXml.addXmlAttribute(ViewerMessage.MSG_ATTRIBUTE_DESC, message.message(), writer);
        ManifestXml.addXmlAttribute(ViewerMessage.MSG_ATTRIBUTE_LEVEL, message.level().name(), writer);
        writer.append("/>");
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "ManifestQueryResult{" + "patientCount=" + getPatientCount() + ", wadoParameters=" + wadoParameters
                + ", viewerMessageCount=" + viewerMessages.size() + '}';
    }

}
