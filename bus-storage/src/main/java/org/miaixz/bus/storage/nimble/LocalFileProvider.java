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
package org.miaixz.bus.storage.nimble;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.Blob;
import org.miaixz.bus.storage.magic.ErrorCode;

/**
 * Local file storage service provider.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class LocalFileProvider extends AbstractProvider {

    /**
     * Constructs a local file storage provider, initializing the storage context.
     *
     * @param context The storage context, containing the storage path (region), bucket, and other configurations.
     * @throws IllegalArgumentException If required context parameters are missing or invalid.
     */
    public LocalFileProvider(Context context) {
        this.context = context;

        Assert.notBlank(this.context.getBucket(), "[bucket] cannot be blank");
        Assert.notBlank(this.context.getRegion(), "[region] cannot be blank");
    }

    /**
     * Reads metadata for a file in the default local bucket.
     *
     * @param fileName The file name to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message<Blob> stat(String fileName) {
        return stat(this.context.getBucket(), fileName);
    }

    /**
     * Reads metadata for a file using the provider's normal local path-building rules.
     *
     * @param bucket   The local bucket directory.
     * @param fileName The file name to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message<Blob> stat(String bucket, String fileName) {
        String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
        return statKey(bucket, Builder.buildObjectKey(prefix, Normal.EMPTY, fileName));
    }

    /**
     * Reads metadata for an exact local object key.
     *
     * @param bucket    The local bucket directory.
     * @param objectKey The exact object key inside the bucket.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message<Blob> statKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.<Blob>builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }

            Path filePath = Paths.get(context.getRegion(), bucket, objectKey);
            if (!Files.isRegularFile(filePath)) {
                return Message.<Blob>builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }

            BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
            String name = objectKey;
            int index = objectKey.lastIndexOf(Symbol.SLASH);
            if (index >= 0) {
                name = objectKey.substring(index + 1);
            }
            Map<String, Object> extend = new HashMap<>();
            extend.put("creationTime", attributes.creationTime().toInstant());
            extend.put("lastAccessTime", attributes.lastAccessTime().toInstant());
            extend.put("lastModified", attributes.lastModifiedTime().toInstant());
            extend.put("directory", attributes.isDirectory());
            extend.put("regularFile", attributes.isRegularFile());
            extend.put("symbolicLink", attributes.isSymbolicLink());
            extend.put("other", attributes.isOther());
            extend.put("fileKey", attributes.fileKey());

            return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(
                            Blob.builder().bucket(bucket).key(objectKey).name(name).path(objectKey)
                                    .size(StringKit.toString(attributes.size())).type(Files.probeContentType(filePath))
                                    .extend(extend).build())
                    .build();
        } catch (Exception e) {
            Errors error = e instanceof IllegalArgumentException ? ErrorCode._113008 : ErrorCode._113012;
            Logger.error(
                    false,
                    "Storage",
                    "Storage stat failed; provider={}, bucket={}, object={}, code={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    objectKey,
                    error.getKey(),
                    e.getMessage(),
                    e);
            return Message.<Blob>builder().errcode(error.getKey()).errmsg(error.getValue()).build();
        }
    }

    /**
     * Opens a stream for a file in the default local bucket.
     *
     * @param fileName The file name to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message<Blob> stream(String fileName) {
        return stream(this.context.getBucket(), fileName);
    }

    /**
     * Opens a stream for a file using the provider's normal local path-building rules.
     *
     * @param bucket   The local bucket directory.
     * @param fileName The file name to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message<Blob> stream(String bucket, String fileName) {
        String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
        return streamKey(bucket, Builder.buildObjectKey(prefix, Normal.EMPTY, fileName));
    }

    /**
     * Opens a stream for an exact local object key.
     *
     * @param bucket    The local bucket directory.
     * @param objectKey The exact object key inside the bucket.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message<Blob> streamKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.<Blob>builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }

            Path filePath = Paths.get(context.getRegion(), bucket, objectKey);
            if (!Files.isRegularFile(filePath)) {
                return Message.<Blob>builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }

            BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
            String name = objectKey;
            int index = objectKey.lastIndexOf(Symbol.SLASH);
            if (index >= 0) {
                name = objectKey.substring(index + 1);
            }
            Map<String, Object> extend = new HashMap<>();
            extend.put("creationTime", attributes.creationTime().toInstant());
            extend.put("lastAccessTime", attributes.lastAccessTime().toInstant());
            extend.put("lastModified", attributes.lastModifiedTime().toInstant());
            extend.put("directory", attributes.isDirectory());
            extend.put("regularFile", attributes.isRegularFile());
            extend.put("symbolicLink", attributes.isSymbolicLink());
            extend.put("other", attributes.isOther());
            extend.put("fileKey", attributes.fileKey());

            return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(
                            Blob.builder().inputStream(Files.newInputStream(filePath)).bucket(bucket).key(objectKey)
                                    .name(name).path(objectKey).size(StringKit.toString(attributes.size()))
                                    .type(Files.probeContentType(filePath)).extend(extend).build())
                    .build();
        } catch (Exception e) {
            Errors error = e instanceof IllegalArgumentException ? ErrorCode._113008 : ErrorCode._113012;
            Logger.error(
                    false,
                    "Storage",
                    "Storage stream failed; provider={}, bucket={}, object={}, code={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    objectKey,
                    error.getKey(),
                    e.getMessage(),
                    e);
            return Message.<Blob>builder().errcode(error.getKey()).errmsg(error.getValue()).build();
        }
    }

    /**
     * Downloads a file from the default storage bucket.
     *
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation, including the file content as a byte array if
     *         successful.
     */
    @Override
    public Message<byte[]> download(String fileName) {
        return download(this.context.getBucket(), fileName);
    }

    /**
     * Downloads a file from the specified storage bucket and returns its content as a byte array.
     * <p>
     * This method reads the entire file content into memory as a byte array, making it suitable for images, PDFs, DOCX
     * files, and other binary files. The underlying input stream is automatically closed using try-with-resources to
     * prevent resource leaks.
     * </p>
     *
     * @param bucket   The name of the storage bucket.
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation. If successful, the data field contains the file
     *         content as a byte array; otherwise, it contains error information.
     */
    @Override
    public Message<byte[]> download(String bucket, String fileName) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, Normal.EMPTY, fileName);
            Path filePath = Paths.get(context.getRegion(), bucket, objectKey);

            if (!Files.exists(filePath)) {
                throw new IOException("File does not exist: " + filePath);
            }

            // Read all bytes directly from the file
            byte[] content = Files.readAllBytes(filePath);

            return Message.<byte[]>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(content).build();
        } catch (IOException e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage download failed; provider={}, bucket={}, object={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    fileName,
                    e.getMessage(),
                    e);
            return Message.<byte[]>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue())
                    .build();
        }
    }

    /**
     * Downloads a file from the default storage bucket and saves it to a local file.
     *
     * @param fileName The name of the file to download.
     * @param file     The target local file to save the downloaded content.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Void> download(String fileName, File file) {
        return download(this.context.getBucket(), fileName, file);
    }

    /**
     * Downloads a file from the specified storage bucket and saves it directly to a local file.
     * <p>
     * This method uses file system copy operations, making it memory-efficient and suitable for large files.
     * </p>
     * <p>
     * <strong>Recommended for:</strong> Large files, videos, archives, or any scenario where you need to persist the
     * file locally without loading it entirely into memory.
     * </p>
     *
     * @param bucket   The name of the storage bucket.
     * @param fileName The name of the file to download.
     * @param file     The target local file to save the downloaded content.
     * @return A {@link Message} containing the result of the operation. If successful, the file is saved to the
     *         specified location; otherwise, error information is returned.
     */
    @Override
    public Message<Void> download(String bucket, String fileName, File file) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, Normal.EMPTY, fileName);
            Path sourcePath = Paths.get(context.getRegion(), bucket, objectKey);

            if (!Files.exists(sourcePath)) {
                throw new IOException("File does not exist: " + sourcePath);
            }

            Files.copy(sourcePath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return Message.<Void>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .build();
        } catch (IOException e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage download-to-local failed; provider={}, bucket={}, object={}, targetProvided={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    fileName,
                    file != null,
                    e.getMessage(),
                    e);
            return Message.<Void>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue())
                    .build();
        }
    }

    /**
     * Lists files in the default storage bucket.
     *
     * @return A {@link Message} containing the result of the operation, including a list of {@link Blob} objects if
     *         successful.
     */
    @Override
    public Message<List<Blob>> list() {
        try {
            Path bucketPath = Paths.get(context.getRegion(), this.context.getBucket());
            if (!Files.exists(bucketPath)) {
                throw new IOException("Bucket directory does not exist: " + bucketPath);
            }
            return Message.<List<Blob>>builder().errcode(ErrorCode._SUCCESS.getKey())
                    .errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Files.walk(bucketPath).filter(Files::isRegularFile).map(path -> {
                        String relativePath = bucketPath.relativize(path).toString();
                        Map<String, Object> extend = new HashMap<>();
                        try {
                            extend.put("lastModified", Files.getLastModifiedTime(path).toInstant());
                        } catch (IOException e) {
                            extend.put("lastModified", null);
                        }
                        return Blob.builder().name(relativePath).size(StringKit.toString(path.toFile().length()))
                                .extend(extend).build();
                    }).collect(Collectors.toList())).build();
        } catch (IOException e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage list failed; provider={}, bucket={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    this.context.getBucket(),
                    e.getMessage(),
                    e);
            return Message.<List<Blob>>builder().errcode(ErrorCode._FAILURE.getKey())
                    .errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * Renames a file in the default storage bucket.
     *
     * @param oldName The current name of the file.
     * @param newName The new name for the file.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Void> rename(String oldName, String newName) {
        return rename(this.context.getBucket(), Normal.EMPTY, oldName, newName);
    }

    /**
     * Renames a file within a specified path in the default storage bucket.
     *
     * @param path    The path where the file is located.
     * @param oldName The current name of the file.
     * @param newName The new name for the file.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Void> rename(String path, String oldName, String newName) {
        return rename(this.context.getBucket(), path, oldName, newName);
    }

    /**
     * Renames a file within the specified bucket and path.
     *
     * @param bucket  The name of the storage bucket.
     * @param path    The path where the file is located.
     * @param oldName The current name of the file.
     * @param newName The new name for the file.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Void> rename(String bucket, String path, String oldName, String newName) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String oldObjectKey = Builder.buildObjectKey(prefix, path, oldName);
            String newObjectKey = Builder.buildObjectKey(prefix, path, newName);
            Path oldPath = Paths.get(context.getRegion(), bucket, oldObjectKey);
            Path newPath = Paths.get(context.getRegion(), bucket, newObjectKey);

            if (!Files.exists(oldPath)) {
                throw new IOException("Source file does not exist: " + oldPath);
            }

            Files.createDirectories(newPath.getParent());
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);

            return Message.<Void>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .build();
        } catch (IOException e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage rename failed; provider={}, bucket={}, path={}, sourceObject={}, targetObject={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    path,
                    oldName,
                    newName,
                    e.getMessage(),
                    e);
            return Message.<Void>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue())
                    .build();
        }
    }

    /**
     * Uploads a byte array to the default storage bucket.
     *
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Blob> upload(String fileName, byte[] content) {
        return upload(Normal.EMPTY, fileName, content);
    }

    /**
     * Uploads a byte array to a specified path in the default storage bucket.
     *
     * @param path     The target path for the file.
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Blob> upload(String path, String fileName, byte[] content) {
        return upload(this.context.getBucket(), path, fileName, content);
    }

    /**
     * Uploads a byte array to the specified storage bucket and path.
     *
     * @param bucket   The name of the storage bucket.
     * @param path     The target path for the file.
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Blob> upload(String bucket, String path, String fileName, byte[] content) {
        return upload(bucket, path, fileName, new ByteArrayInputStream(content));
    }

    /**
     * Uploads an input stream to the default storage bucket.
     *
     * @param fileName The name of the file to upload.
     * @param content  The file content as an {@link InputStream}.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Blob> upload(String fileName, InputStream content) {
        return upload(this.context.getBucket(), Normal.EMPTY, fileName, content);
    }

    /**
     * Uploads an input stream to a specified path in the default storage bucket.
     *
     * @param path     The target path for the file.
     * @param fileName The name of the file to upload.
     * @param content  The file content as an {@link InputStream}.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Blob> upload(String path, String fileName, InputStream content) {
        return upload(this.context.getBucket(), path, fileName, content);
    }

    /**
     * Uploads an input stream to the specified storage bucket and path.
     *
     * @param bucket   The name of the storage bucket.
     * @param path     The target path for the file.
     * @param fileName The name of the file to upload.
     * @param content  The file content as an {@link InputStream}.
     * @return A {@link Message} containing the result of the operation, including blob details if successful.
     */
    @Override
    public Message<Blob> upload(String bucket, String path, String fileName, InputStream content) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, path, fileName);
            Path destPath = Paths.get(context.getRegion(), bucket, objectKey);

            Files.createDirectories(destPath.getParent());

            // Use Files.copy to write the InputStream directly to the file
            Files.copy(content, destPath, StandardCopyOption.REPLACE_EXISTING);

            return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Blob.builder().name(fileName).path(objectKey).build()).build();
        } catch (IOException e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage upload failed; provider={}, bucket={}, path={}, object={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    path,
                    fileName,
                    e.getMessage(),
                    e);
            return Message.<Blob>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue())
                    .build();
        }
    }

    /**
     * Removes a file from the default storage bucket.
     *
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Void> remove(String fileName) {
        return remove(this.context.getBucket(), Normal.EMPTY, fileName);
    }

    /**
     * Removes a file from the specified storage bucket.
     *
     * @param bucket   The name of the storage bucket.
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Void> remove(String bucket, String fileName) {
        return remove(bucket, Normal.EMPTY, fileName);
    }

    /**
     * Removes a file from the specified storage bucket and path.
     *
     * @param bucket   The name of the storage bucket.
     * @param path     The storage path where the file is located.
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Void> remove(String bucket, String path, String fileName) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, path, fileName);
            Path filePath = Paths.get(context.getRegion(), bucket, objectKey);

            Files.deleteIfExists(filePath);

            return Message.<Void>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .build();
        } catch (IOException e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage remove failed; provider={}, bucket={}, path={}, object={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    path,
                    fileName,
                    e.getMessage(),
                    e);
            return Message.<Void>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue())
                    .build();
        }
    }

    /**
     * Removes a file from the specified storage bucket based on its path.
     *
     * @param bucket The name of the storage bucket.
     * @param path   The target path of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Void> remove(String bucket, Path path) {
        return remove(bucket, path.toString(), Normal.EMPTY);
    }

}
