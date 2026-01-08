/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.storage;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.storage.magic.ErrorCode;

/**
 * Provides an interface for file storage operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Provider extends org.miaixz.bus.core.Provider {

    /**
     * Downloads a file as a stream.
     *
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation, including the file stream if successful.
     */
    Message download(String fileName);

    /**
     * Downloads a file from a specified bucket as a stream.
     *
     * @param bucket   The name of the storage bucket.
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation, including the file stream if successful.
     */
    Message download(String bucket, String fileName);

    /**
     * Downloads a file from a specified bucket to a local file.
     *
     * @param bucket   The name of the storage bucket.
     * @param fileName The name of the file to download.
     * @param file     The target local file to save the downloaded content.
     * @return A {@link Message} containing the result of the operation.
     */
    Message download(String bucket, String fileName, File file);

    /**
     * Downloads a file to a local file.
     *
     * @param fileName The name of the file to download.
     * @param file     The target local file to save the downloaded content.
     * @return A {@link Message} containing the result of the operation.
     */
    Message download(String fileName, File file);

    /**
     * Lists files in the default storage location.
     *
     * @return A {@link Message} containing the result of the operation. By default, returns a failure message.
     */
    default Message list() {
        return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
    }

    /**
     * Renames a file.
     *
     * @param oldName The current name of the file.
     * @param newName The new name for the file.
     * @return A {@link Message} containing the result of the operation.
     */
    Message rename(String oldName, String newName);

    /**
     * Renames a file within a specified path.
     *
     * @param path    The path where the file is located.
     * @param oldName The current name of the file.
     * @param newName The new name for the file.
     * @return A {@link Message} containing the result of the operation.
     */
    Message rename(String path, String oldName, String newName);

    /**
     * Renames a file within a specified bucket and path.
     *
     * @param bucket  The name of the storage bucket.
     * @param path    The path where the file is located.
     * @param oldName The current name of the file.
     * @param newName The new name for the file.
     * @return A {@link Message} containing the result of the operation.
     */
    Message rename(String bucket, String path, String oldName, String newName);

    /**
     * Uploads a file using a byte array.
     *
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    Message upload(String fileName, byte[] content);

    /**
     * Uploads a file to a specified path using a byte array.
     *
     * @param path     The target path for the file.
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    Message upload(String path, String fileName, byte[] content);

    /**
     * Uploads a file to a specified bucket and path using a byte array.
     *
     * @param bucket   The name of the storage bucket.
     * @param path     The target path for the file.
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    Message upload(String bucket, String path, String fileName, byte[] content);

    /**
     * Uploads a file using an {@link InputStream}.
     *
     * @param fileName The name of the file to upload.
     * @param content  The file content as an {@link InputStream}.
     * @return A {@link Message} containing the result of the operation.
     */
    Message upload(String fileName, InputStream content);

    /**
     * Uploads a file to a specified path using an {@link InputStream}.
     *
     * @param path     The target path for the file.
     * @param fileName The name of the file to upload.
     * @param content  The file content as an {@link InputStream}.
     * @return A {@link Message} containing the result of the operation.
     */
    Message upload(String path, String fileName, InputStream content);

    /**
     * Uploads a file to a specified bucket and path using an {@link InputStream}.
     *
     * @param bucket   The name of the storage bucket.
     * @param path     The target path for the file.
     * @param fileName The name of the file to upload.
     * @param content  The file content as an {@link InputStream}.
     * @return A {@link Message} containing the result of the operation.
     */
    Message upload(String bucket, String path, String fileName, InputStream content);

    /**
     * Removes a file.
     *
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    Message remove(String fileName);

    /**
     * Removes a file from a specified path.
     *
     * @param path     The storage path where the file is located.
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    Message remove(String path, String fileName);

    /**
     * Removes a file from a specified bucket and path.
     *
     * @param bucket   The name of the storage bucket.
     * @param path     The storage path where the file is located.
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    Message remove(String bucket, String path, String fileName);

    /**
     * Removes a file from a specified bucket and target path.
     *
     * @param bucket The name of the storage bucket.
     * @param path   The target path of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    Message remove(String bucket, Path path);

    /**
     * Returns the type of this provider.
     *
     * @return The provider type, which is {@link EnumValue.Povider#STORAGE}.
     */
    @Override
    default Object type() {
        return EnumValue.Povider.STORAGE;
    }

}
