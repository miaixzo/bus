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

import java.io.*;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileRenameInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskEntry;
import com.hierynomus.smbj.share.DiskShare;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.Blob;
import org.miaixz.bus.storage.magic.ErrorCode;

/**
 * Storage service provider for SMB (Server Message Block) shared files. This provider allows interaction with SMB/CIFS
 * shares for file storage operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SmbFileProvider extends AbstractProvider {

    /**
     * The SMB client instance for establishing connections.
     */
    private final SMBClient client;

    /**
     * The SMB connection to the server.
     */
    private Connection connection;

    /**
     * The SMB session established with the server.
     */
    private Session session;

    /**
     * The connected disk share.
     */
    private DiskShare share;

    /**
     * Constructs an SMB file provider with the given context. Initializes the SMB client and establishes a connection,
     * session, and disk share.
     *
     * @param context The storage context, containing endpoint, bucket (share name), access key (username), secret key
     *                (password), and domain information.
     * @throws IllegalArgumentException If required context parameters are missing or invalid, or if SMB client
     *                                  initialization fails.
     */
    public SmbFileProvider(Context context) {
        this.context = context;
        Assert.notBlank(this.context.getEndpoint(), "[endpoint] cannot be blank");
        Assert.notBlank(this.context.getBucket(), "[bucket] cannot be blank");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] cannot be blank");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] cannot be blank");

        // Initialize SMB client
        this.client = new SMBClient();

        // Parse server address, port, share name, username, password, and domain from the endpoint
        String server = parseServerFromEndpoint(context.getEndpoint());
        int port = parsePortFromEndpoint(context.getEndpoint());
        String shareName = context.getBucket();
        String username = context.getAccessKey();
        String password = context.getSecretKey();
        String domain = parseDomainFromEndpoint(context.getEndpoint());

        try {
            // Establish connection
            this.connection = client.connect(server, port);
            // Create authentication context
            AuthenticationContext ac = new AuthenticationContext(username, password.toCharArray(), domain);
            // Establish session
            this.session = connection.authenticate(ac);
            // Connect to the share
            this.share = (DiskShare) session.connectShare(shareName);
        } catch (Exception e) {
            try {
                if (share != null) {
                    share.close();
                }
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception ex) {
                Logger.error(
                        false,
                        "Storage",
                        "Storage resource close failed; provider={}, resource=smb-client, status=failure, error={}",
                        this.getClass().getSimpleName(),
                        ex.getMessage(),
                        ex);
            }
            throw new IllegalArgumentException("Failed to initialize SMB client: " + e.getMessage(), e);
        }
    }

    /**
     * Reads metadata for a file in the default SMB share.
     *
     * @param fileName The file name to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message stat(String fileName) {
        return stat(Normal.EMPTY, fileName);
    }

    /**
     * Reads metadata for a file using the provider's normal SMB path-building rules.
     *
     * @param bucket   The logical bucket/path segment.
     * @param fileName The file name to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message stat(String bucket, String fileName) {
        return statKey(this.context.getBucket(), getAbsolutePath(bucket, Normal.EMPTY, fileName));
    }

    /**
     * Reads metadata for an exact SMB object path inside the connected share.
     *
     * @param bucket    The SMB share name.
     * @param objectKey The exact path inside the SMB share.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message statKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }
            if (!share.fileExists(objectKey)) {
                return Message.builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }

            FileAllInformation information = share.getFileInformation(objectKey);
            String name = objectKey;
            int index = objectKey.lastIndexOf(Symbol.SLASH);
            if (index >= 0) {
                name = objectKey.substring(index + 1);
            }

            Map<String, Object> extend = new HashMap<>();
            extend.put("creationTime", information.getBasicInformation().getCreationTime().toEpochMillis());
            extend.put("lastAccessTime", information.getBasicInformation().getLastAccessTime().toEpochMillis());
            extend.put("lastWriteTime", information.getBasicInformation().getLastWriteTime().toEpochMillis());
            extend.put("changeTime", information.getBasicInformation().getChangeTime().toEpochMillis());
            extend.put("fileAttributes", information.getBasicInformation().getFileAttributes());
            extend.put("allocationSize", information.getStandardInformation().getAllocationSize());
            extend.put("numberOfLinks", information.getStandardInformation().getNumberOfLinks());
            extend.put("deletePending", information.getStandardInformation().isDeletePending());
            extend.put("directory", information.getStandardInformation().isDirectory());
            extend.put("indexNumber", information.getInternalInformation().getIndexNumber());

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(
                            Blob.builder().bucket(this.context.getBucket()).key(objectKey).name(name).path(objectKey)
                                    .size(StringKit.toString(information.getStandardInformation().getEndOfFile()))
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
            return Message.builder().errcode(error.getKey()).errmsg(error.getValue()).build();
        }
    }

    /**
     * Opens a stream for a file in the default SMB share.
     *
     * @param fileName The file name to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message stream(String fileName) {
        return stream(Normal.EMPTY, fileName);
    }

    /**
     * Opens a stream for a file using the provider's normal SMB path-building rules.
     *
     * @param bucket   The logical bucket/path segment.
     * @param fileName The file name to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message stream(String bucket, String fileName) {
        return streamKey(this.context.getBucket(), getAbsolutePath(bucket, Normal.EMPTY, fileName));
    }

    /**
     * Opens a stream for an exact SMB object path inside the connected share.
     *
     * @param bucket    The SMB share name.
     * @param objectKey The exact path inside the SMB share.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message streamKey(String bucket, String objectKey) {
        com.hierynomus.smbj.share.File smbFile = null;
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }
            if (!share.fileExists(objectKey)) {
                return Message.builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }

            smbFile = share.openFile(
                    objectKey,
                    EnumSet.of(AccessMask.GENERIC_READ),
                    null,
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN,
                    null);

            FileAllInformation information = smbFile.getFileInformation();
            String name = objectKey;
            int index = objectKey.lastIndexOf(Symbol.SLASH);
            if (index >= 0) {
                name = objectKey.substring(index + 1);
            }

            Map<String, Object> extend = new HashMap<>();
            extend.put("creationTime", information.getBasicInformation().getCreationTime().toEpochMillis());
            extend.put("lastAccessTime", information.getBasicInformation().getLastAccessTime().toEpochMillis());
            extend.put("lastWriteTime", information.getBasicInformation().getLastWriteTime().toEpochMillis());
            extend.put("changeTime", information.getBasicInformation().getChangeTime().toEpochMillis());
            extend.put("fileAttributes", information.getBasicInformation().getFileAttributes());
            extend.put("allocationSize", information.getStandardInformation().getAllocationSize());
            extend.put("numberOfLinks", information.getStandardInformation().getNumberOfLinks());
            extend.put("deletePending", information.getStandardInformation().isDeletePending());
            extend.put("directory", information.getStandardInformation().isDirectory());
            extend.put("indexNumber", information.getInternalInformation().getIndexNumber());

            InputStream inputStream = new SmbFileInputStream(smbFile.getInputStream(), smbFile);
            smbFile = null;
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(
                            Blob.builder().inputStream(inputStream).bucket(this.context.getBucket()).key(objectKey)
                                    .name(name).path(objectKey)
                                    .size(StringKit.toString(information.getStandardInformation().getEndOfFile()))
                                    .extend(extend).build())
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
            return Message.builder().errcode(error.getKey()).errmsg(error.getValue()).build();
        } finally {
            if (smbFile != null) {
                try {
                    smbFile.close();
                } catch (Exception e) {
                    Logger.warn(
                            false,
                            "Storage",
                            "Storage resource close failed; provider={}, resource=smb-file, status=failure, error={}",
                            this.getClass().getSimpleName(),
                            e.getMessage());
                }
            }
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
    public Message download(String fileName) {
        return download(Normal.EMPTY, fileName);
    }

    /**
     * Downloads a file from the specified storage bucket and returns its content as a byte array.
     * <p>
     * This method reads the entire file content into memory as a byte array, making it suitable for images, PDFs, DOCX
     * files, and other binary files. The SMB file and input stream are automatically closed using try-with-resources to
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
    public Message download(String bucket, String fileName) {
        com.hierynomus.smbj.share.File smbFile = null;
        try {
            String objectKey = getAbsolutePath(bucket, Normal.EMPTY, fileName);
            if (!share.fileExists(objectKey)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("File not found").build();
            }

            smbFile = share.openFile(
                    objectKey,
                    EnumSet.of(AccessMask.GENERIC_READ),
                    null,
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN,
                    null);

            // Use try-with-resources to automatically close the InputStream
            try (InputStream inputStream = smbFile.getInputStream()) {
                // Read all bytes - supports images, PDFs, DOCX, and all other binary file types
                byte[] content = inputStream.readAllBytes();

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .data(content).build();
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage download failed; provider={}, bucket={}, object={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    fileName,
                    e.getMessage(),
                    e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        } finally {
            // Close SMB file resource
            if (smbFile != null) {
                try {
                    smbFile.close();
                } catch (Exception e) {
                    Logger.warn(
                            false,
                            "Storage",
                            "Storage resource close failed; provider={}, resource=smb-file, status=failure, error={}",
                            this.getClass().getSimpleName(),
                            e.getMessage());
                }
            }
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
    public Message download(String fileName, File file) {
        return download(Normal.EMPTY, fileName, file);
    }

    /**
     * Downloads a file from the specified storage bucket and saves it directly to a local file.
     * <p>
     * This method uses streaming to transfer file content, making it memory-efficient and suitable for large files.
     * Both the input stream and output stream are automatically closed using try-with-resources to ensure proper
     * resource management.
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
    public Message download(String bucket, String fileName, File file) {
        com.hierynomus.smbj.share.File smbFile = null;
        try {
            String objectKey = getAbsolutePath(bucket, Normal.EMPTY, fileName);
            if (!share.fileExists(objectKey)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("File not found").build();
            }

            smbFile = share.openFile(
                    objectKey,
                    EnumSet.of(AccessMask.GENERIC_READ),
                    null,
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN,
                    null);

            // Use try-with-resources to automatically close both streams
            try (InputStream inputStream = smbFile.getInputStream();
                    OutputStream outputStream = new FileOutputStream(file)) {
                inputStream.transferTo(outputStream);
            }

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (Exception e) {
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
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        } finally {
            // Close SMB file resource
            if (smbFile != null) {
                try {
                    smbFile.close();
                } catch (Exception e) {
                    Logger.warn(
                            false,
                            "Storage",
                            "Storage resource close failed; provider={}, resource=smb-file, status=failure, error={}",
                            this.getClass().getSimpleName(),
                            e.getMessage());
                }
            }
        }
    }

    /**
     * Lists files in the default storage bucket.
     *
     * @return A {@link Message} containing the result of the operation, including a list of {@link Blob} objects if
     *         successful.
     */
    @Override
    public Message list() {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            List<String> files = share.list(prefix).stream()
                    .filter(
                            fileInfo -> !fileInfo.getFileName().equals(Symbol.DOT)
                                    && !fileInfo.getFileName().equals(Symbol.DOUBLE_DOT))
                    .map(fileInfo -> fileInfo.getFileName()).collect(Collectors.toList());

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(files.stream().map(fileName -> {
                        Map<String, Object> extend = new HashMap<>();
                        return Blob.builder().name(fileName).extend(extend).build();
                    }).collect(Collectors.toList())).build();
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage list failed; provider={}, bucket={}, prefix={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    this.context.getBucket(),
                    context.getPrefix(),
                    e.getMessage(),
                    e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
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
    public Message rename(String oldName, String newName) {
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
    public Message rename(String path, String oldName, String newName) {
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
    public Message rename(String bucket, String path, String oldName, String newName) {
        try {
            String oldObjectKey = getAbsolutePath(bucket, path, oldName);
            String newObjectKey = getAbsolutePath(bucket, path, newName);

            if (!share.fileExists(oldObjectKey)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("File not found").build();
            }

            // Use smbj's correct way to rename a file
            // 1. Open the file
            DiskEntry diskEntry = share.open(
                    oldObjectKey,
                    EnumSet.of(AccessMask.GENERIC_WRITE, AccessMask.DELETE),
                    EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN,
                    EnumSet.of(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE));

            try {
                // 2. Set rename information - correct constructor parameter order
                FileRenameInformation renameInfo = new FileRenameInformation(false, // replaceIfExists - do not replace
                        // target file (if exists)
                        0L, // rootDirectory - root directory file ID (usually 0)
                        newObjectKey // fileName - new file name
                );

                // 3. Apply rename
                diskEntry.setFileInformation(renameInfo);

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            } finally {
                diskEntry.close();
            }
        } catch (Exception e) {
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
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
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
    public Message upload(String fileName, byte[] content) {
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
    public Message upload(String path, String fileName, byte[] content) {
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
    public Message upload(String bucket, String path, String fileName, byte[] content) {
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
    public Message upload(String fileName, InputStream content) {
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
    public Message upload(String path, String fileName, InputStream content) {
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
    public Message upload(String bucket, String path, String fileName, InputStream content) {
        com.hierynomus.smbj.share.File smbFile = null;
        try {
            String objectKey = getAbsolutePath(bucket, path, fileName);
            String dirPath = objectKey.substring(0, objectKey.lastIndexOf(Symbol.SLASH));

            // Ensure directory exists
            ensureDirectoryExists(dirPath);

            smbFile = share.openFile(
                    objectKey,
                    EnumSet.of(AccessMask.GENERIC_WRITE),
                    null,
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OVERWRITE_IF,
                    null);

            try (OutputStream outputStream = smbFile.getOutputStream()) {
                content.transferTo(outputStream);
            }

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Blob.builder().name(fileName).path(objectKey).build()).build();
        } catch (Exception e) {
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
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        } finally {
            // Close SMB file resource
            if (smbFile != null) {
                try {
                    smbFile.close();
                } catch (Exception e) {
                    Logger.warn(
                            false,
                            "Storage",
                            "Storage resource close failed; provider={}, resource=smb-file, status=failure, error={}",
                            this.getClass().getSimpleName(),
                            e.getMessage());
                }
            }
        }
    }

    /**
     * Removes a file from the default storage bucket.
     *
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String fileName) {
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
    public Message remove(String path, String fileName) {
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
    public Message remove(String bucket, String path, String fileName) {
        try {
            String objectKey = getAbsolutePath(bucket, path, fileName);
            if (share.fileExists(objectKey)) {
                share.rm(objectKey);
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (Exception e) {
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
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
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
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString(), Normal.EMPTY);
    }

    /**
     * Releases SMB resources held by this provider.
     */
    @Override
    public void close() {
        if (this.share != null) {
            try {
                this.share.close();
            } catch (Exception e) {
                // Ignore close-time failures.
            }
        }
        if (this.session != null) {
            try {
                this.session.close();
            } catch (Exception e) {
                // Ignore close-time failures.
            }
        }
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (Exception e) {
                // Ignore close-time failures.
            }
        }
        try {
            this.client.close();
        } catch (Exception e) {
            // Ignore close-time failures.
        }
    }

    /**
     * Closes the SMB file handle together with the returned stream.
     */
    private static class SmbFileInputStream extends FilterInputStream {

        private final com.hierynomus.smbj.share.File file;

        private SmbFileInputStream(InputStream inputStream, com.hierynomus.smbj.share.File file) {
            super(inputStream);
            this.file = file;
        }

        @Override
        public void close() throws IOException {
            IOException failure = null;
            try {
                super.close();
            } catch (IOException e) {
                failure = e;
            }
            try {
                file.close();
            } catch (Exception e) {
                if (failure == null) {
                    failure = new IOException(e);
                } else {
                    failure.addSuppressed(e);
                }
            }
            if (failure != null) {
                throw failure;
            }
        }

    }

    /**
     * Parses the server address from the given endpoint string, ensuring no port information is included.
     *
     * @param endpoint The SMB server address, e.g., {@code smb://hostname:port/share} or {@code hostname/share}.
     * @return The server address.
     */
    private String parseServerFromEndpoint(String endpoint) {
        if (StringKit.isBlank(endpoint)) {
            return "";
        }
        // Remove protocol header (e.g., smb://, cifs://)
        String server = endpoint.replaceFirst("^(smb|cifs)://", "");
        // Remove share name and path
        int slashIndex = server.indexOf('/');
        if (slashIndex != -1) {
            server = server.substring(0, slashIndex);
        }
        // Remove port information
        int colonIndex = server.indexOf(':');
        if (colonIndex != -1) {
            server = server.substring(0, colonIndex);
        }
        return server;
    }

    /**
     * Parses the port from the given endpoint string.
     *
     * @param endpoint The SMB server address, e.g., {@code smb://hostname:port/share} or {@code hostname/share}.
     * @return The port number, defaulting to 445 if not specified or invalid.
     */
    private int parsePortFromEndpoint(String endpoint) {
        if (StringKit.isBlank(endpoint)) {
            return 445; // Default SMB port
        }
        try {
            // Remove protocol header (e.g., smb://, cifs://)
            String server = endpoint.replaceFirst("^(smb|cifs)://", "");
            // Extract the port part
            int colonIndex = server.indexOf(':');
            int slashIndex = server.indexOf('/');

            if (colonIndex != -1) {
                String portStr = server.substring(colonIndex + 1, slashIndex != -1 ? slashIndex : server.length());
                return Integer.parseInt(portStr);
            }
        } catch (NumberFormatException e) {
            Logger.warn(
                    false,
                    "Storage",
                    "Storage endpoint port invalid; provider={}, defaultPort=445, endpointProvided={}, status=fallback",
                    this.getClass().getSimpleName(),
                    StringKit.isNotBlank(endpoint));
        }
        return 445; // Default SMB port
    }

    /**
     * Parses the domain from the given endpoint string.
     *
     * @param endpoint The SMB server address, e.g., {@code smb://domain;hostname/share} or {@code hostname/share}.
     * @return The domain, or an empty string if not specified.
     */
    private String parseDomainFromEndpoint(String endpoint) {
        if (StringKit.isBlank(endpoint)) {
            return "";
        }
        // Remove protocol header (e.g., smb://, cifs://)
        String server = endpoint.replaceFirst("^(smb|cifs)://", "");
        // Check if domain is present
        int semicolonIndex = server.indexOf(';');
        if (semicolonIndex != -1) {
            return server.substring(0, semicolonIndex);
        }
        return "";
    }

    /**
     * Constructs the absolute path for a file on the SMB share.
     *
     * @param bucket   The name of the storage bucket (share), can be empty.
     * @param path     The path within the share, can be empty.
     * @param fileName The name of the file.
     * @return The normalized absolute path for the file.
     */
    private String getAbsolutePath(String bucket, String path, String fileName) {
        String prefix = StringKit.isBlank(bucket) ? Builder.buildNormalizedPrefix(context.getPrefix())
                : Builder.buildNormalizedPrefix(context.getPrefix() + bucket);
        return Builder.buildObjectKey(prefix, path, fileName);
    }

    /**
     * Ensures that the specified directory path exists on the SMB share, creating it if necessary.
     *
     * @param dirPath The directory path to ensure existence.
     * @throws InternalException If there is an error creating the directory.
     */
    private void ensureDirectoryExists(String dirPath) {
        if (StringKit.isBlank(dirPath) || dirPath.equals(Symbol.SLASH)) {
            return; // Root directory does not need to be created
        }

        try {
            if (!share.folderExists(dirPath)) {
                // Create parent directories recursively
                String parentPath = dirPath.substring(0, dirPath.lastIndexOf(Symbol.SLASH));
                if (!StringKit.isBlank(parentPath) && !parentPath.equals(Symbol.SLASH)) {
                    ensureDirectoryExists(parentPath);
                }
                // Create the current directory
                share.mkdir(dirPath);
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage directory preparation failed; provider={}, bucket={}, directoryProvided={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    this.context.getBucket(),
                    StringKit.isNotBlank(dirPath),
                    e.getMessage(),
                    e);
            throw new InternalException("Failed to create directory: " + dirPath, e);
        }
    }

}
