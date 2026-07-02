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
package org.miaixz.bus.extra.ftp;

import java.io.Serial;
import java.io.Serializable;

/**
 * Neutral metadata for a remote FTP/SFTP entry. Provider-specific file attributes should be converted to this object
 * before they cross the FTP abstraction boundary.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FtpEntry implements Serializable {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852292980088L;

    /**
     * The remote entry name.
     */
    private String name;

    /**
     * The remote entry path.
     */
    private String path;

    /**
     * The remote entry size.
     */
    private long size;

    /**
     * The remote owner user id.
     */
    private int uid;

    /**
     * The remote owner group id.
     */
    private int gid;

    /**
     * The raw permission value.
     */
    private int permissions;

    /**
     * The display permission text.
     */
    private String permissionsText;

    /**
     * The access time reported by the remote server.
     */
    private long accessTime;

    /**
     * The modified time reported by the remote server.
     */
    private long modifiedTime;

    /**
     * Whether the remote entry is a directory.
     */
    private boolean directory;

    /**
     * Whether the remote entry is a regular file.
     */
    private boolean regularFile;

    /**
     * Whether the remote entry is a symbolic link.
     */
    private boolean link;

    /**
     * Creates an empty FTP entry.
     */
    public FtpEntry() {
        // No initialization required.
    }

    /**
     * Creates an FTP entry for the given path.
     *
     * @param path The remote entry path.
     * @return A new {@code FtpEntry} instance.
     */
    public static FtpEntry of(final String path) {
        return new FtpEntry().setPath(path);
    }

    /**
     * Returns the remote entry name.
     *
     * @return The remote entry name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the remote entry name.
     *
     * @param name The remote entry name.
     * @return This {@code FtpEntry} instance.
     */
    public FtpEntry setName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the remote entry path.
     *
     * @return The remote entry path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the remote entry path.
     *
     * @param path The remote entry path.
     * @return This {@code FtpEntry} instance.
     */
    public FtpEntry setPath(final String path) {
        this.path = path;
        return this;
    }

    /**
     * Returns the remote entry size.
     *
     * @return The remote entry size.
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the remote entry size.
     *
     * @param size The remote entry size.
     * @return This {@code FtpEntry} instance.
     */
    public FtpEntry setSize(final long size) {
        this.size = size;
        return this;
    }

    /**
     * Returns the remote owner user id.
     *
     * @return The remote owner user id.
     */
    public int getUid() {
        return uid;
    }

    /**
     * Sets the remote owner user id.
     *
     * @param uid The remote owner user id.
     * @return This {@code FtpEntry} instance.
     */
    public FtpEntry setUid(final int uid) {
        this.uid = uid;
        return this;
    }

    /**
     * Returns the remote owner group id.
     *
     * @return The remote owner group id.
     */
    public int getGid() {
        return gid;
    }

    /**
     * Sets the remote owner group id.
     *
     * @param gid The remote owner group id.
     * @return This {@code FtpEntry} instance.
     */
    public FtpEntry setGid(final int gid) {
        this.gid = gid;
        return this;
    }

    /**
     * Returns the raw permission value.
     *
     * @return The raw permission value.
     */
    public int getPermissions() {
        return permissions;
    }

    /**
     * Sets the raw permission value.
     *
     * @param permissions The raw permission value.
     * @return This {@code FtpEntry} instance.
     */
    public FtpEntry setPermissions(final int permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * Returns the display permission text.
     *
     * @return The display permission text.
     */
    public String getPermissionsText() {
        return permissionsText;
    }

    /**
     * Sets the display permission text.
     *
     * @param permissionsText The display permission text.
     * @return This {@code FtpEntry} instance.
     */
    public FtpEntry setPermissionsText(final String permissionsText) {
        this.permissionsText = permissionsText;
        return this;
    }

    /**
     * Returns the access time reported by the remote server.
     *
     * @return The access time reported by the remote server.
     */
    public long getAccessTime() {
        return accessTime;
    }

    /**
     * Sets the access time reported by the remote server.
     *
     * @param accessTime The access time reported by the remote server.
     * @return This {@code FtpEntry} instance.
     */
    public FtpEntry setAccessTime(final long accessTime) {
        this.accessTime = accessTime;
        return this;
    }

    /**
     * Returns the modified time reported by the remote server.
     *
     * @return The modified time reported by the remote server.
     */
    public long getModifiedTime() {
        return modifiedTime;
    }

    /**
     * Sets the modified time reported by the remote server.
     *
     * @param modifiedTime The modified time reported by the remote server.
     * @return This {@code FtpEntry} instance.
     */
    public FtpEntry setModifiedTime(final long modifiedTime) {
        this.modifiedTime = modifiedTime;
        return this;
    }

    /**
     * Returns whether the remote entry is a directory.
     *
     * @return {@code true} if the remote entry is a directory.
     */
    public boolean isDirectory() {
        return directory;
    }

    /**
     * Sets whether the remote entry is a directory.
     *
     * @param directory Whether the remote entry is a directory.
     * @return This {@code FtpEntry} instance.
     */
    public FtpEntry setDirectory(final boolean directory) {
        this.directory = directory;
        return this;
    }

    /**
     * Returns whether the remote entry is a regular file.
     *
     * @return {@code true} if the remote entry is a regular file.
     */
    public boolean isRegularFile() {
        return regularFile;
    }

    /**
     * Sets whether the remote entry is a regular file.
     *
     * @param regularFile Whether the remote entry is a regular file.
     * @return This {@code FtpEntry} instance.
     */
    public FtpEntry setRegularFile(final boolean regularFile) {
        this.regularFile = regularFile;
        return this;
    }

    /**
     * Returns whether the remote entry is a symbolic link.
     *
     * @return {@code true} if the remote entry is a symbolic link.
     */
    public boolean isLink() {
        return link;
    }

    /**
     * Sets whether the remote entry is a symbolic link.
     *
     * @param link Whether the remote entry is a symbolic link.
     * @return This {@code FtpEntry} instance.
     */
    public FtpEntry setLink(final boolean link) {
        this.link = link;
        return this;
    }

}
