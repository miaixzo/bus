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
import org.miaixz.bus.extra.ssh.provider.jsch.JschSftp;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.ErrorCode;
import org.miaixz.bus.storage.magic.Blob;

/**
 * Storage service provider for SFTP (SSH File Transfer Protocol). This provider allows interaction with SFTP servers
 * for file storage operations.
 *
 * @author Kimi Liu
 * @since Java 17+
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
     * Downloads a file from the default storage bucket.
     *
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation, including the file content stream or an error
     *         message.
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
     * @return A {@link Message} containing the result of the operation, including the file content stream or an error
     *         message.
     */
    @Override
    public Message download(String bucket, String fileName) {
        try {
            String objectKey = getAbsolutePath(bucket, Normal.EMPTY, fileName);
            InputStream inputStream = client.getFileStream(objectKey);
            if (inputStream == null) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("File not found").build();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(reader).build();
        } catch (InternalException e) {
            Logger.error("Failed to download file: {} from bucket: {}. Error: {}", fileName, bucket, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * Downloads a file from the default storage bucket and saves it to a local file.
     *
     * @param fileName The name of the file to download.
     * @param file     The target local file to save the downloaded content.
     * @return A {@link Message} containing the result of the operation, including success or error information.
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
     * @return A {@link Message} containing the result of the operation, including success or error information.
     */
    @Override
    public Message download(String bucket, String fileName, File file) {
        try {
            String objectKey = getAbsolutePath(bucket, Normal.EMPTY, fileName);
            client.download(objectKey, file);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (InternalException e) {
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
     * @return A {@link Message} containing the result of the operation, including a list of {@link Blob} objects or an
     *         error message.
     */
    @Override
    public Message list() {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            List<String> files = client.lsFiles(prefix);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(files.stream().map(fileName -> {
                        Map<String, Object> extend = new HashMap<>();
                        return Blob.builder().name(fileName).extend(extend).build();
                    }).collect(Collectors.toList())).build();
        } catch (InternalException e) {
            Logger.error("Failed to list files in path: {}. Error: {}", context.getPrefix(), e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * Renames a file in the default storage bucket.
     *
     * @param oldName The current name of the file.
     * @param newName The new name for the file.
     * @return A {@link Message} containing the result of the operation, including success or error information.
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
     * @return A {@link Message} containing the result of the operation, including success or error information.
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
     * @return A {@link Message} containing the result of the operation, including success or error information.
     */
    @Override
    public Message rename(String bucket, String path, String oldName, String newName) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String oldObjectKey = Builder.buildObjectKey(prefix, path, oldName);
            String newObjectKey = Builder.buildObjectKey(prefix, path, newName);
            if (!isExist(oldObjectKey)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("File not found").build();
            }
            client.rename(oldObjectKey, newObjectKey);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (InternalException e) {
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
            if (!client.isDir(dirPath)) {
                client.mkdir(dirPath);
            }
            client.put(content, objectKey, null, JschSftp.Mode.OVERWRITE);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Blob.builder().name(fileName).path(objectKey).build()).build();
        } catch (InternalException e) {
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
            if (isExist(objectKey)) {
                client.delFile(objectKey);
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (InternalException e) {
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
            Logger.warn("Invalid port in endpoint: {}. Using default port 22.", endpoint);
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
            Logger.error("Failed to check existence of file: {}. Error: {}", path, e.getMessage(), e);
            return false;
        }
    }

}
