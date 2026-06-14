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
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.image.Tag;

/**
 * SOP instance entry in a DICOM manifest.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ManifestInstance implements ManifestXml, Comparable<ManifestInstance> {

    /**
     * The key separator value.
     */
    private static final String KEY_SEPARATOR = "?";

    /**
     * The direct download file attribute.
     */
    private static final String ATTR_DIRECT_DOWNLOAD_FILE = "DirectDownloadFile";

    /**
     * The sop instance uid value.
     */
    private final String sopInstanceUID;

    /**
     * The sop class uid value.
     */
    private final String sopClassUID;

    /**
     * The instance number value.
     */
    private final Integer instanceNumber;

    /**
     * The image comments value.
     */
    private String imageComments;

    /**
     * The transfer syntax uid value.
     */
    private String transferSyntaxUID;

    /**
     * The direct download file value.
     */
    private String directDownloadFile;

    /**
     * The graphic model value.
     */
    private Object graphicModel;

    /**
     * Creates a new instance.
     *
     * @param sopInstanceUID the sop instance uid.
     * @param instanceNumber the instance number.
     */
    public ManifestInstance(String sopInstanceUID, Integer instanceNumber) {
        this(sopInstanceUID, null, instanceNumber);
    }

    /**
     * Creates a new instance.
     *
     * @param sopInstanceUID the sop instance uid.
     * @param sopClassUID    the sop class uid.
     * @param instanceNumber the instance number.
     */
    public ManifestInstance(String sopInstanceUID, String sopClassUID, Integer instanceNumber) {
        this.sopInstanceUID = Objects.requireNonNull(sopInstanceUID, "SOP Instance UID cannot be null");
        this.sopClassUID = sopClassUID;
        this.instanceNumber = instanceNumber;
    }

    /**
     * Gets the sop instance uid.
     *
     * @return the sop instance uid.
     */
    public String getSopInstanceUID() {
        return sopInstanceUID;
    }

    /**
     * Gets the sop class uid.
     *
     * @return the sop class uid.
     */
    public String getSopClassUID() {
        return sopClassUID;
    }

    /**
     * Gets the instance number.
     *
     * @return the instance number.
     */
    public Integer getInstanceNumber() {
        return instanceNumber;
    }

    /**
     * Gets the string instance number.
     *
     * @return the string instance number.
     */
    public String getStringInstanceNumber() {
        return instanceNumber != null ? instanceNumber.toString() : null;
    }

    /**
     * Gets the image comments.
     *
     * @return the image comments.
     */
    public String getImageComments() {
        return imageComments;
    }

    /**
     * Sets the image comments.
     *
     * @param imageComments the image comments.
     */
    public void setImageComments(String imageComments) {
        this.imageComments = imageComments;
    }

    /**
     * Gets the transfer syntax uid.
     *
     * @return the transfer syntax uid.
     */
    public String getTransferSyntaxUID() {
        return transferSyntaxUID;
    }

    /**
     * Sets the transfer syntax uid.
     *
     * @param transferSyntaxUID the transfer syntax uid.
     */
    public void setTransferSyntaxUID(String transferSyntaxUID) {
        this.transferSyntaxUID = hasText(transferSyntaxUID) ? transferSyntaxUID.trim() : null;
    }

    /**
     * Gets the direct download file.
     *
     * @return the direct download file.
     */
    public String getDirectDownloadFile() {
        return directDownloadFile;
    }

    /**
     * Sets the direct download file.
     *
     * @param directDownloadFile the direct download file.
     */
    public void setDirectDownloadFile(String directDownloadFile) {
        this.directDownloadFile = directDownloadFile;
    }

    /**
     * Gets the graphic model.
     *
     * @return the graphic model.
     */
    public Object getGraphicModel() {
        return graphicModel;
    }

    /**
     * Sets the graphic model.
     *
     * @param graphicModel the graphic model.
     */
    public void setGraphicModel(Object graphicModel) {
        this.graphicModel = graphicModel;
    }

    /**
     * Executes the to xml operation.
     *
     * @param writer the writer.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void toXml(Writer writer) throws IOException {
        writer.append("\n<").append(ManifestXml.Level.INSTANCE.getTagName()).append(" ");
        ManifestXml.addXmlAttribute(Tag.SOPInstanceUID, sopInstanceUID, writer);
        ManifestXml.addXmlAttribute(Tag.SOPClassUID, sopClassUID, writer);
        ManifestXml.addXmlAttribute(Tag.TransferSyntaxUID, transferSyntaxUID, writer);
        ManifestXml.addXmlAttribute(Tag.ImageComments, imageComments, writer);
        ManifestXml.addXmlAttribute(Tag.InstanceNumber, getStringInstanceNumber(), writer);
        ManifestXml.addXmlAttribute(ATTR_DIRECT_DOWNLOAD_FILE, directDownloadFile, writer);
        writer.append("/>");
    }

    /**
     * Executes the compare to operation.
     *
     * @param other the other.
     * @return the operation result.
     */
    @Override
    public int compareTo(ManifestInstance other) {
        int numberComparison = compareInstanceNumbers(instanceNumber, other.instanceNumber);
        return numberComparison != 0 ? numberComparison : compareNormalizedUIDs(sopInstanceUID, other.sopInstanceUID);
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
        if (!(object instanceof ManifestInstance other)) {
            return false;
        }
        return Objects.equals(instanceNumber, other.instanceNumber) && sopInstanceUID.equals(other.sopInstanceUID);
    }

    /**
     * Returns the hash code.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(sopInstanceUID, instanceNumber);
    }

    /**
     * Adds the instance.
     *
     * @param instances the instances.
     * @param instance  the instance.
     */
    public static void addInstance(Map<String, ManifestInstance> instances, ManifestInstance instance) {
        if (instances != null && instance != null) {
            instances.put(buildMapKey(instance.getSopInstanceUID(), instance.getInstanceNumber()), instance);
        }
    }

    /**
     * Gets the instance.
     *
     * @param instances      the instances.
     * @param sopUID         the sop uid.
     * @param instanceNumber the instance number.
     * @return the instance.
     */
    public static ManifestInstance getInstance(
            Map<String, ManifestInstance> instances,
            String sopUID,
            Integer instanceNumber) {
        return instances == null || sopUID == null ? null : instances.get(buildMapKey(sopUID, instanceNumber));
    }

    /**
     * Removes the instance.
     *
     * @param instances      the instances.
     * @param sopUID         the sop uid.
     * @param instanceNumber the instance number.
     * @return the operation result.
     */
    public static ManifestInstance removeInstance(
            Map<String, ManifestInstance> instances,
            String sopUID,
            Integer instanceNumber) {
        return instances == null || sopUID == null ? null : instances.remove(buildMapKey(sopUID, instanceNumber));
    }

    /**
     * Executes the compare instance numbers operation.
     *
     * @param first  the first.
     * @param second the second.
     * @return the operation result.
     */
    private static int compareInstanceNumbers(Integer first, Integer second) {
        if (first != null && second != null) {
            return first.compareTo(second);
        }
        if (first == null && second != null) {
            return 1;
        }
        return first != null ? -1 : 0;
    }

    /**
     * Executes the compare normalized ui ds operation.
     *
     * @param first  the first.
     * @param second the second.
     * @return the operation result.
     */
    private static int compareNormalizedUIDs(String first, String second) {
        int firstLength = first.length();
        int secondLength = second.length();
        if (firstLength < secondLength) {
            return normalizeUID(first, secondLength - firstLength).compareTo(second);
        }
        if (firstLength > secondLength) {
            return first.compareTo(normalizeUID(second, firstLength - secondLength));
        }
        return first.compareTo(second);
    }

    /**
     * Executes the normalize uid operation.
     *
     * @param uid           the uid.
     * @param paddingLength the padding length.
     * @return the operation result.
     */
    private static String normalizeUID(String uid, int paddingLength) {
        char[] padding = new char[paddingLength];
        Arrays.fill(padding, '0');
        int lastDotIndex = uid.lastIndexOf('.') + 1;
        return uid.substring(0, lastDotIndex) + new String(padding) + uid.substring(lastDotIndex);
    }

    /**
     * Builds the map key.
     *
     * @param sopUID         the sop uid.
     * @param instanceNumber the instance number.
     * @return the operation result.
     */
    private static String buildMapKey(String sopUID, Integer instanceNumber) {
        return instanceNumber != null ? sopUID + KEY_SEPARATOR + instanceNumber : sopUID;
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
