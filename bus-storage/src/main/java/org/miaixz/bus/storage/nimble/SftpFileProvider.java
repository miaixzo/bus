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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.ftp.FtpEntry;
import org.miaixz.bus.extra.ssh.provider.jsch.JschSftp;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.Blob;
import org.miaixz.bus.storage.magic.ErrorCode;

/**
 * Storage service provider for SFTP (SSH File Transfer Protocol). This provider allows interaction with SFTP servers
 * for file storage operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SftpFileProvider extends AbstractProvider {

    /**
     * The SFTP client instance used for communication with the SFTP server.
     */
    private final JschSftp client;

    /**
     * Constructs an SFTP file provider with the given context. Initializes the SFTP client using the provided endpoint,
     * access key (username), and secret key (password).
     *
     * @param context The storage context, containing endpoint, bucket, access key, secret key, and other
     *                configurations.
     * @throws IllegalArgumentException If required context parameters are invalid or if SFTP client initialization
     *                                  fails.
     */
    public SftpFileProvider(Context context) {
        this.context = context;

        Assert.notBlank(this.context.getEndpoint(), "[endpoint] cannot be blank");
        Assert.notBlank(this.context.getBucket(), "[bucket] cannot be blank");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] cannot be blank");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] cannot be blank");

        // Parse host and port from the endpoint
        String host = parseHostFromEndpoint(context.getEndpoint());
        int port = parsePortFromEndpoint(context.getEndpoint());
        String username = context.getAccessKey();
        String password = context.getSecretKey();

        try {
            this.client = JschSftp.of(
                    host,
                    port != 0 ? port : 22, // Default SFTP port is 22
                    username,
                    password);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to initialize SFTP client: " + e.getMessage(), e);
        }
    }

    /**
     * Reads metadata for a file in the default SFTP location.
     *
     * @param fileName The file name to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message<Blob> stat(String fileName) {
        return stat(Normal.EMPTY, fileName);
    }

    /**
     * Reads metadata for a file using the provider's normal SFTP path-building rules.
     *
     * @param bucket   The logical bucket/path segment.
     * @param fileName The file name to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message<Blob> stat(String bucket, String fileName) {
        return statKey(this.context.getBucket(), getAbsolutePath(bucket, Normal.EMPTY, fileName));
    }

    /**
     * Reads metadata for an exact SFTP object path.
     *
     * @param bucket    The configured SFTP bucket/path segment.
     * @param objectKey The exact remote path.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message<Blob> statKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.<Blob>builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }

            FtpEntry entry = client.entry(objectKey);
            if (entry == null || entry.isDirectory()) {
                return Message.<Blob>builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }

            return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(
                            Blob.builder().bucket(this.context.getBucket()).key(objectKey).name(entry.getName())
                                    .path(objectKey).size(StringKit.toString(entry.getSize())).extend(toExtend(entry))
                                    .build())
                    .build();
        } catch (Exception e) {
            Errors error = ErrorCode._113012;
            if (e instanceof IllegalArgumentException) {
                error = ErrorCode._113008;
            } else if (StringKit.containsIgnoreCase(e.getMessage(), "No such file")
                    || StringKit.containsIgnoreCase(e.getMessage(), "does not exist")) {
                error = ErrorCode._113010;
            }
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
     * Opens a stream for a file in the default SFTP location.
     *
     * @param fileName The file name to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message<Blob> stream(String fileName) {
        return stream(Normal.EMPTY, fileName);
    }

    /**
     * Opens a stream for a file using the provider's normal SFTP path-building rules.
     *
     * @param bucket   The logical bucket/path segment.
     * @param fileName The file name to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message<Blob> stream(String bucket, String fileName) {
        return streamKey(this.context.getBucket(), getAbsolutePath(bucket, Normal.EMPTY, fileName));
    }

    /**
     * Opens a stream for an exact SFTP object path.
     *
     * @param bucket    The configured SFTP bucket/path segment.
     * @param objectKey The exact remote path.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message<Blob> streamKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.<Blob>builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }

            FtpEntry entry = client.entry(objectKey);
            if (entry == null || entry.isDirectory()) {
                return Message.<Blob>builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }

            InputStream inputStream = client.getFileStream(objectKey);
            if (inputStream == null) {
                return Message.<Blob>builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }

            return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(
                            Blob.builder().inputStream(inputStream).bucket(this.context.getBucket()).key(objectKey)
                                    .name(entry.getName()).path(objectKey).size(StringKit.toString(entry.getSize()))
                                    .extend(toExtend(entry)).build())
                    .build();
        } catch (Exception e) {
            Errors error = ErrorCode._113012;
            if (e instanceof IllegalArgumentException) {
                error = ErrorCode._113008;
            } else if (StringKit.containsIgnoreCase(e.getMessage(), "No such file")
                    || StringKit.containsIgnoreCase(e.getMessage(), "does not exist")) {
                error = ErrorCode._113010;
            }
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
     * Downloads a file from the default storage bucket and returns its content as a byte array.
     *
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation, including the file content as a byte array if
     *         successful.
     */
    @Override
    public Message<byte[]> download(String fileName) {
        return download(Normal.EMPTY, fileName);
    }

    /**
     * Downloads a file from the specified storage bucket and returns its content as a byte array.
     * <p>
     * This method reads the entire file content into memory as a byte array, making it suitable for images, PDFs, DOCX
     * files, and other binary files. The underlying input stream is automatically closed using try-with-resources to
     * prevent resource leaks.
     * </p>
     * <p>
     * <strong>Note:</strong> For large files (> 50MB), consider using {@link #download(String, String, File)} instead
     * to avoid excessive memory consumption.
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
            String objectKey = getAbsolutePath(bucket, Normal.EMPTY, fileName);
            InputStream inputStream = client.getFileStream(objectKey);
            if (inputStream == null) {
                return Message.<byte[]>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("File not found").build();
            }

            // Use try-with-resources to automatically close the InputStream and prevent resource leaks
            try (inputStream) {
                byte[] content = inputStream.readAllBytes();

                return Message.<byte[]>builder().errcode(ErrorCode._SUCCESS.getKey())
                        .errmsg(ErrorCode._SUCCESS.getValue()).data(content).build();
            }
        } catch (InternalException | IOException e) {
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
        return download(Normal.EMPTY, fileName, file);
    }

    /**
     * Downloads a file from the specified storage bucket and saves it directly to a local file.
     * <p>
     * This method uses the SFTP client's built-in download functionality, which is memory-efficient and suitable for
     * large files.
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
            String objectKey = getAbsolutePath(bucket, Normal.EMPTY, fileName);
            client.download(objectKey, file);
            return Message.<Void>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .build();
        } catch (InternalException e) {
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
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            List<String> files = client.lsFiles(prefix);
            return Message.<List<Blob>>builder().errcode(ErrorCode._SUCCESS.getKey())
                    .errmsg(ErrorCode._SUCCESS.getValue()).data(files.stream().map(fileName -> {
                        Map<String, Object> extend = new HashMap<>();
                        return Blob.builder().name(fileName).extend(extend).build();
                    }).collect(Collectors.toList())).build();
        } catch (InternalException e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage list failed; provider={}, bucket={}, prefix={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    this.context.getBucket(),
                    context.getPrefix(),
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
        return rename(Normal.EMPTY, oldName, newName);
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
        return rename(Normal.EMPTY, path, oldName, newName);
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
            if (!isExist(oldObjectKey)) {
                return Message.<Void>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("File not found").build();
            }
            client.rename(oldObjectKey, newObjectKey);
            return Message.<Void>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .build();
        } catch (InternalException e) {
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
        return upload(Normal.EMPTY, path, fileName, content);
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
        return upload(Normal.EMPTY, fileName, content);
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
        return upload(Normal.EMPTY, path, fileName, content);
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
            String objectKey = getAbsolutePath(bucket, path, fileName);
            String dirPath = objectKey.substring(0, objectKey.lastIndexOf(Symbol.SLASH));
            if (!client.isDir(dirPath)) {
                client.mkdir(dirPath);
            }
            client.put(content, objectKey, JschSftp.Mode.OVERWRITE);
            return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Blob.builder().name(fileName).path(objectKey).build()).build();
        } catch (InternalException e) {
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
        return remove(Normal.EMPTY, fileName);
    }

    /**
     * Removes a file from a specified path in the default storage bucket.
     *
     * @param path     The storage path where the file is located.
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Void> remove(String path, String fileName) {
        return remove(Normal.EMPTY, path, fileName);
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
            String objectKey = getAbsolutePath(bucket, path, fileName);
            if (isExist(objectKey)) {
                client.delFile(objectKey);
            }
            return Message.<Void>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .build();
        } catch (InternalException e) {
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

    /**
     * Releases SFTP client resources held by this provider.
     */
    @Override
    public void close() {
        try {
            this.client.close();
        } catch (Exception e) {
            // Ignore close-time failures.
        }
    }

    /**
     * Parses the host from the given endpoint string, ensuring no port information is included.
     *
     * @param endpoint The SFTP server address, e.g., {@code sftp://hostname:port} or {@code hostname}.
     * @return The hostname.
     */
    private String parseHostFromEndpoint(String endpoint) {
        if (StringKit.isBlank(endpoint)) {
            return "";
        }
        // Remove protocol header (e.g., sftp://, ssh://)
        String host = endpoint.replaceFirst("^(sftp|ssh)://", "");
        // Remove port and path
        int colonIndex = host.indexOf(':');
        int slashIndex = host.indexOf('/');
        if (colonIndex != -1) {
            host = host.substring(0, colonIndex);
        } else if (slashIndex != -1) {
            host = host.substring(0, slashIndex);
        }
        return host;
    }

    /**
     * Parses the port from the given endpoint string.
     *
     * @param endpoint The SFTP server address, e.g., {@code sftp://hostname:port} or {@code hostname}.
     * @return The port number, or 0 if no port is specified (indicating the default port 22 should be used).
     */
    private int parsePortFromEndpoint(String endpoint) {
        if (StringKit.isBlank(endpoint)) {
            return 0;
        }
        try {
            // Extract the port part
            String portStr = endpoint.replaceFirst("^(sftp|ssh)://[^:]+:?", "");
            int slashIndex = portStr.indexOf('/');
            if (slashIndex != -1) {
                portStr = portStr.substring(0, slashIndex); // Remove path part
            }
            if (StringKit.isNotBlank(portStr)) {
                return Integer.parseInt(portStr);
            }
        } catch (NumberFormatException e) {
            Logger.warn(
                    false,
                    "Storage",
                    "Storage endpoint port invalid; provider={}, defaultPort=22, endpointProvided={}, status=fallback",
                    this.getClass().getSimpleName(),
                    StringKit.isNotBlank(endpoint));
        }
        return 0; // Return 0 to indicate using the default port 22
    }

    /**
     * Constructs the absolute path for a file on the SFTP server.
     *
     * @param bucket   The name of the storage bucket, can be empty.
     * @param path     The path within the bucket, can be empty.
     * @param fileName The name of the file.
     * @return The normalized absolute path for the file.
     */
    private String getAbsolutePath(String bucket, String path, String fileName) {
        String prefix = StringKit.isBlank(bucket) ? Builder.buildNormalizedPrefix(context.getPrefix())
                : Builder.buildNormalizedPrefix(context.getPrefix() + bucket);
        return Builder.buildObjectKey(prefix, path, fileName);
    }

    /**
     * Checks if a file exists on the SFTP server.
     *
     * @param path The path to the file.
     * @return {@code true} if the file exists, {@code false} otherwise.
     */
    private boolean isExist(String path) {
        try {
            InputStream inputStream = client.getFileStream(path);
            if (inputStream != null) {
                inputStream.close();
                return true;
            }
            return false;
        } catch (InternalException | IOException e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage existence check failed; provider={}, pathProvided={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    StringKit.isNotBlank(path),
                    e.getMessage(),
                    e);
            return false;
        }
    }

    /**
     * Converts neutral FTP metadata to storage extension fields while preserving the existing SFTP response shape.
     *
     * @param entry The FTP metadata entry.
     * @return The storage extension fields.
     */
    private Map<String, Object> toExtend(FtpEntry entry) {
        Map<String, Object> extend = new HashMap<>();
        extend.put("uid", entry.getUid());
        extend.put("gid", entry.getGid());
        extend.put("permissions", entry.getPermissions());
        extend.put("permissionsText", entry.getPermissionsText());
        extend.put("accessTime", entry.getAccessTime());
        extend.put("modifiedTime", entry.getModifiedTime());
        extend.put("directory", entry.isDirectory());
        extend.put("regularFile", entry.isRegularFile());
        extend.put("link", entry.isLink());
        return extend;
    }

}
