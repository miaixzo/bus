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
package org.miaixz.bus.storage.metric;

import java.io.*;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.ErrorCode;
import org.miaixz.bus.storage.magic.Material;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
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

/**
 * Storage service provider for SMB (Server Message Block) shared files. This provider allows interaction with SMB/CIFS
 * shares for file storage operations.
 *
 * @author Kimi Liu
 * @since Java 17+
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
                Logger.error("Error while closing SMB resources: {}", ex.getMessage(), ex);
            }
            throw new IllegalArgumentException("Failed to initialize SMB client: " + e.getMessage(), e);
        }
    }

    /**
     * Downloads a file from the default storage bucket.
     *
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation, including the file content stream if
     *         successful.
     */
    @Override
    public Message download(String fileName) {
        return download(Normal.EMPTY, fileName);
    }

    /**
     * Downloads a file from the specified storage bucket.
     *
     * @param bucket   The name of the storage bucket.
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation, including the file content stream if
     *         successful.
     */
    @Override
    public Message download(String bucket, String fileName) {
        try {
            String objectKey = getAbsolutePath(bucket, Normal.EMPTY, fileName);
            if (!share.fileExists(objectKey)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("File not found").build();
            }

            com.hierynomus.smbj.share.File smbFile = share.openFile(
                    objectKey,
                    EnumSet.of(AccessMask.GENERIC_READ),
                    null,
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN,
                    null);

            InputStream inputStream = smbFile.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(reader).build();
        } catch (Exception e) {
            Logger.error("Failed to download file: {} from bucket: {}. Error: {}", fileName, bucket, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
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
     * Downloads a file from the specified storage bucket and saves it to a local file.
     *
     * @param bucket   The name of the storage bucket.
     * @param fileName The name of the file to download.
     * @param file     The target local file to save the downloaded content.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message download(String bucket, String fileName, File file) {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            String objectKey = getAbsolutePath(bucket, Normal.EMPTY, fileName);
            if (!share.fileExists(objectKey)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("File not found").build();
            }

            com.hierynomus.smbj.share.File smbFile = share.openFile(
                    objectKey,
                    EnumSet.of(AccessMask.GENERIC_READ),
                    null,
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN,
                    null);

            try (InputStream inputStream = smbFile.getInputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (Exception e) {
            Logger.error(
                    "Failed to download file: {} from bucket: {} to local file: {}. Error: {}",
                    fileName,
                    bucket,
                    file.getAbsolutePath(),
                    e.getMessage(),
                    e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * Lists files in the default storage bucket.
     *
     * @return A {@link Message} containing the result of the operation, including a list of {@link Material} objects if
     *         successful.
     */
    @Override
    public Message list() {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            List<String> files = share.list(prefix).stream()
                    .filter(fileInfo -> !fileInfo.getFileName().equals(".") && !fileInfo.getFileName().equals(".."))
                    .map(fileInfo -> fileInfo.getFileName()).collect(Collectors.toList());

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(files.stream().map(fileName -> {
                        Map<String, Object> extend = new HashMap<>();
                        return Material.builder().name(fileName).extend(extend).build();
                    }).collect(Collectors.toList())).build();
        } catch (Exception e) {
            Logger.error("Failed to list files in path: {}. Error: {}", context.getPrefix(), e.getMessage(), e);
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
                    "Failed to rename file from {} to {} in bucket: {} path: {}. Error: {}",
                    oldName,
                    newName,
                    bucket,
                    path,
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
     * @return A {@link Message} containing the result of the operation, including the uploaded file information or an
     *         error message.
     */
    @Override
    public Message upload(String fileName, byte[] content) {
        return upload(Normal.EMPTY, fileName, content);
    }

    /**
     * Uploads a byte array to a specified path in the default storage bucket.
     *
     * @param path     The path to upload the file to.
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation, including the uploaded file information or an
     *         error message.
     */
    @Override
    public Message upload(String path, String fileName, byte[] content) {
        return upload(Normal.EMPTY, path, fileName, content);
    }

    /**
     * Uploads a byte array to the specified storage bucket and path.
     *
     * @param bucket   The name of the storage bucket.
     * @param path     The path to upload the file to.
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation, including the uploaded file information or an
     *         error message.
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
     * @return A {@link Message} containing the result of the operation, including the uploaded file information or an
     *         error message.
     */
    @Override
    public Message upload(String fileName, InputStream content) {
        return upload(Normal.EMPTY, fileName, content);
    }

    /**
     * Uploads an input stream to a specified path in the default storage bucket.
     *
     * @param path     The path to upload the file to.
     * @param fileName The name of the file to upload.
     * @param content  The file content as an {@link InputStream}.
     * @return A {@link Message} containing the result of the operation, including the uploaded file information or an
     *         error message.
     */
    @Override
    public Message upload(String path, String fileName, InputStream content) {
        return upload(Normal.EMPTY, path, fileName, content);
    }

    /**
     * Uploads an input stream to the specified storage bucket and path.
     *
     * @param bucket   The name of the storage bucket.
     * @param path     The path to upload the file to.
     * @param fileName The name of the file to upload.
     * @param content  The file content as an {@link InputStream}.
     * @return A {@link Message} containing the result of the operation, including the uploaded file information or an
     *         error message.
     */
    @Override
    public Message upload(String bucket, String path, String fileName, InputStream content) {
        try {
            String objectKey = getAbsolutePath(bucket, path, fileName);
            String dirPath = objectKey.substring(0, objectKey.lastIndexOf(Symbol.SLASH));

            // Ensure directory exists
            ensureDirectoryExists(dirPath);

            com.hierynomus.smbj.share.File smbFile = share.openFile(
                    objectKey,
                    EnumSet.of(AccessMask.GENERIC_WRITE),
                    null,
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OVERWRITE_IF,
                    null);

            try (OutputStream outputStream = smbFile.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = content.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Material.builder().name(fileName).path(objectKey).build()).build();
        } catch (Exception e) {
            Logger.error(
                    "Failed to upload file: {} to bucket: {} path: {}. Error: {}",
                    fileName,
                    bucket,
                    path,
                    e.getMessage(),
                    e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * Removes a file from the default storage bucket.
     *
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation, including success or error information.
     */
    @Override
    public Message remove(String fileName) {
        return remove(Normal.EMPTY, fileName);
    }

    /**
     * Removes a file from a specified path in the default storage bucket.
     *
     * @param path     The path where the file is located.
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation, including success or error information.
     */
    @Override
    public Message remove(String path, String fileName) {
        return remove(Normal.EMPTY, path, fileName);
    }

    /**
     * Removes a file from the specified storage bucket and path.
     *
     * @param bucket   The name of the storage bucket.
     * @param path     The path where the file is located.
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation, including success or error information.
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
                    "Failed to remove file: {} from bucket: {} path: {}. Error: {}",
                    fileName,
                    bucket,
                    path,
                    e.getMessage(),
                    e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * Removes a file from the specified storage bucket based on its path.
     *
     * @param bucket The name of the storage bucket.
     * @param path   The path of the file to remove.
     * @return A {@link Message} containing the result of the operation, including success or error information.
     */
    @Override
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString(), Normal.EMPTY);
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
            Logger.warn("Invalid port in endpoint: {}. Using default port 445.", endpoint);
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
            Logger.error("Failed to ensure directory exists: {}. Error: {}", dirPath, e.getMessage(), e);
            throw new InternalException("Failed to create directory: " + dirPath, e);
        }
    }

    /**
     * Checks if a file exists on the SMB share.
     *
     * @param path The path to the file.
     * @return {@code true} if the file exists, {@code false} otherwise.
     */
    private boolean isExist(String path) {
        try {
            return share.fileExists(path);
        } catch (Exception e) {
            Logger.error("Failed to check existence of file: {}. Error: {}", path, e.getMessage(), e);
            return false;
        }
    }

}
