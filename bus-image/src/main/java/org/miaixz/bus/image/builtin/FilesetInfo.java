/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.builtin;

import java.io.File;

/**
 * Represents information about a DICOM Fileset, including its UID, ID, descriptor file, and character set. This class
 * provides a structured way to store and access metadata related to a DICOM fileset.
 *
 * @author Kimi Liu
 * @since Java 17+
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
    private File descFile;
    /**
     * The character set used for the descriptor file.
     */
    private String descFileCharset;

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
    public final void setFilesetUID(String uid) {
        this.uid = uid;
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
    public final void setFilesetID(String id) {
        this.id = id;
    }

    /**
     * Retrieves the descriptor file associated with the fileset.
     *
     * @return The descriptor file.
     */
    public final File getDescriptorFile() {
        return descFile;
    }

    /**
     * Sets the descriptor file associated with the fileset.
     *
     * @param descFile The new descriptor file.
     */
    public final void setDescriptorFile(File descFile) {
        this.descFile = descFile;
    }

    /**
     * Retrieves the character set used for the descriptor file.
     *
     * @return The descriptor file character set.
     */
    public final String getDescriptorFileCharset() {
        return descFileCharset;
    }

    /**
     * Sets the character set used for the descriptor file.
     *
     * @param descFileCharset The new descriptor file character set.
     */
    public final void setDescriptorFileCharset(String descFileCharset) {
        this.descFileCharset = descFileCharset;
    }

}
