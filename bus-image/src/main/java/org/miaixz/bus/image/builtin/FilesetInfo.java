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
package org.miaixz.bus.image.builtin;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents information about a DICOM Fileset, including its UID, ID, descriptor file, and character set. This class
 * provides a structured way to store and access metadata related to a DICOM fileset.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FilesetInfo {

    /**
     * The Unique Identifier (UID) of the fileset.
     */
    private String uid;

    /**
     * The identifier of the fileset.
     */
    private String id;

    /**
     * The descriptor file associated with the fileset.
     */
    private Path descriptorFile;

    /**
     * The character set used for the descriptor file.
     */
    private String descriptorFileCharset;

    /**
     * Constructs a new FilesetInfo instance.
     */
    public FilesetInfo() {
        // No initialization required.
    }

    /**
     * Constructs a new FilesetInfo instance.
     *
     * @param uid the uid.
     * @param id  the id.
     */
    public FilesetInfo(String uid, String id) {
        this.uid = uid;
        this.id = id;
    }

    /**
     * Constructs a new FilesetInfo instance.
     *
     * @param uid                   the uid.
     * @param id                    the id.
     * @param descriptorFile        the descriptor file.
     * @param descriptorFileCharset the descriptor file charset.
     */
    public FilesetInfo(String uid, String id, Path descriptorFile, String descriptorFileCharset) {
        this.uid = uid;
        this.id = id;
        this.descriptorFile = descriptorFile;
        this.descriptorFileCharset = descriptorFileCharset;
    }

    /**
     * Retrieves the Unique Identifier (UID) of the fileset.
     *
     * @return The fileset UID.
     */
    public final String getFilesetUID() {
        return uid;
    }

    /**
     * Sets the Unique Identifier (UID) of the fileset.
     *
     * @param uid The new fileset UID.
     */
    public final FilesetInfo setFilesetUID(String uid) {
        this.uid = uid;
        return this;
    }

    /**
     * Retrieves the identifier of the fileset.
     *
     * @return The fileset ID.
     */
    public final String getFilesetID() {
        return id;
    }

    /**
     * Sets the identifier of the fileset.
     *
     * @param id The new fileset ID.
     */
    public final FilesetInfo setFilesetID(String id) {
        this.id = id;
        return this;
    }

    /**
     * Retrieves the descriptor file associated with the fileset.
     *
     * @return The descriptor file.
     */
    public final Optional<Path> getDescriptorFile() {
        return Optional.ofNullable(descriptorFile);
    }

    /**
     * Sets the descriptor file associated with the fileset.
     *
     * @param descriptorFile The new descriptor file.
     */
    public final FilesetInfo setDescriptorFile(Path descriptorFile) {
        this.descriptorFile = descriptorFile;
        return this;
    }

    /**
     * Retrieves the character set used for the descriptor file.
     *
     * @return The descriptor file character set.
     */
    public final Optional<String> getDescriptorFileCharset() {
        return Optional.ofNullable(descriptorFileCharset);
    }

    /**
     * Sets the character set used for the descriptor file.
     *
     * @param descriptorFileCharset the descriptor file character set.
     */
    public final FilesetInfo setDescriptorFileCharset(String descriptorFileCharset) {
        this.descriptorFileCharset = descriptorFileCharset;
        return this;
    }

    /**
     * Determines whether this fileset information is complete.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isComplete() {
        return hasValidIdentifiers() && descriptorFile != null;
    }

    /**
     * Determines whether this fileset information has valid identifiers.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean hasValidIdentifiers() {
        return uid != null && !uid.trim().isEmpty() && id != null && !id.trim().isEmpty();
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
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        FilesetInfo that = (FilesetInfo) object;
        return Objects.equals(uid, that.uid) && Objects.equals(id, that.id)
                && Objects.equals(descriptorFile, that.descriptorFile)
                && Objects.equals(descriptorFileCharset, that.descriptorFileCharset);
    }

    /**
     * Returns the hash code.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(uid, id, descriptorFile, descriptorFileCharset);
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "FilesetInfo{" + "uid='" + uid + '\'' + ", id='" + id + '\'' + ", descriptorFile=" + descriptorFile
                + ", descriptorFileCharset='" + descriptorFileCharset + '\'' + '}';
    }

}
