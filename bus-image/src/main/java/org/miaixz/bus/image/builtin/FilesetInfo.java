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
