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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.io.file.PathResolve;
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
 * Storage service provider for WebDAV. This provider allows interaction with WebDAV servers for file storage
 * operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class WebDavProvider extends AbstractProvider {

    /**
     * The Sardine client instance used for communication with the WebDAV server.
     */
    private final Sardine client;

    /**
     * Constructs a WebDAV storage provider with the given context. Initializes the Sardine client using the provided
     * access key (username) and secret key (password).
     *
     * @param context The storage context, containing endpoint, bucket, access key, secret key, and other
     *                configurations.
     * @throws IllegalArgumentException If required context parameters are missing or invalid.
     */
    public WebDavProvider(Context context) {
        this.context = context;

        Assert.notBlank(this.context.getEndpoint(), "[endpoint] cannot be blank");
        Assert.notBlank(this.context.getBucket(), "[bucket] cannot be blank");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] cannot be blank");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] cannot be blank");

        this.client = SardineFactory.begin(this.context.getAccessKey(), this.context.getSecretKey());
    }

    /**
     * Reads metadata for a file in the default WebDAV bucket.
     *
     * @param fileName The file name to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message<Blob> stat(String fileName) {
        return stat(this.context.getBucket(), fileName);
    }

    /**
     * Reads metadata for a file using the provider's normal WebDAV path-building rules.
     *
     * @param bucket   The WebDAV bucket/path segment.
     * @param fileName The file name to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message<Blob> stat(String bucket, String fileName) {
        String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
        return statKey(bucket, Builder.buildObjectKey(prefix, Normal.EMPTY, fileName));
    }

    /**
     * Reads metadata for an exact WebDAV object key.
     *
     * @param bucket    The WebDAV bucket/path segment.
     * @param objectKey The exact object key.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message<Blob> statKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.<Blob>builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }

            String url = getUrl(bucket + Symbol.SLASH + objectKey);
            if (!client.exists(url)) {
                return Message.<Blob>builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }

            List<DavResource> resources = client.list(url, 0);
            if (resources == null || resources.isEmpty() || resources.get(0).isDirectory()) {
                return Message.<Blob>builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }
            DavResource resource = resources.get(0);
            String name = StringKit.isBlank(resource.getName()) ? objectKey : resource.getName();
            Map<String, Object> extend = new HashMap<>();
            extend.put("tag", resource.getEtag());
            extend.put("creationTime", resource.getCreation());
            extend.put("lastModified", resource.getModified());
            extend.put("directory", resource.isDirectory());
            extend.put("contentLanguage", resource.getContentLanguage());
            extend.put("statusCode", resource.getStatusCode());
            extend.put("href", resource.getHref());

            return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(
                            Blob.builder().bucket(bucket).key(objectKey).name(name).path(objectKey)
                                    .size(StringKit.toString(resource.getContentLength()))
                                    .type(resource.getContentType()).hash(resource.getEtag()).extend(extend).build())
                    .build();
        } catch (Exception e) {
            Errors error = ErrorCode._113012;
            if (e instanceof IllegalArgumentException) {
                error = ErrorCode._113008;
            } else if (StringKit.containsIgnoreCase(e.getMessage(), "404")
                    || StringKit.containsIgnoreCase(e.getMessage(), "not found")) {
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
     * Opens a stream for a file in the default WebDAV bucket.
     *
     * @param fileName The file name to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message<Blob> stream(String fileName) {
        return stream(this.context.getBucket(), fileName);
    }

    /**
     * Opens a stream for a file using the provider's normal WebDAV path-building rules.
     *
     * @param bucket   The WebDAV bucket/path segment.
     * @param fileName The file name to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message<Blob> stream(String bucket, String fileName) {
        String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
        return streamKey(bucket, Builder.buildObjectKey(prefix, Normal.EMPTY, fileName));
    }

    /**
     * Opens a stream for an exact WebDAV object key.
     *
     * @param bucket    The WebDAV bucket/path segment.
     * @param objectKey The exact object key.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message<Blob> streamKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.<Blob>builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }

            String url = getUrl(bucket + Symbol.SLASH + objectKey);
            if (!client.exists(url)) {
                return Message.<Blob>builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }

            List<DavResource> resources = client.list(url, 0);
            if (resources == null || resources.isEmpty() || resources.get(0).isDirectory()) {
                return Message.<Blob>builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }
            DavResource resource = resources.get(0);
            String name = StringKit.isBlank(resource.getName()) ? objectKey : resource.getName();
            Map<String, Object> extend = new HashMap<>();
            extend.put("tag", resource.getEtag());
            extend.put("creationTime", resource.getCreation());
            extend.put("lastModified", resource.getModified());
            extend.put("directory", resource.isDirectory());
            extend.put("contentLanguage", resource.getContentLanguage());
            extend.put("statusCode", resource.getStatusCode());
            extend.put("href", resource.getHref());

            return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(
                            Blob.builder().inputStream(client.get(url)).bucket(bucket).key(objectKey).name(name)
                                    .path(objectKey).size(StringKit.toString(resource.getContentLength()))
                                    .type(resource.getContentType()).hash(resource.getEtag()).extend(extend).build())
                    .build();
        } catch (Exception e) {
            Errors error = ErrorCode._113012;
            if (e instanceof IllegalArgumentException) {
                error = ErrorCode._113008;
            } else if (StringKit.containsIgnoreCase(e.getMessage(), "404")
                    || StringKit.containsIgnoreCase(e.getMessage(), "not found")) {
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
            String url = getUrl(bucket + Symbol.SLASH + objectKey);

            // Use try-with-resources to automatically close the InputStream
            try (InputStream inputStream = client.get(url)) {
                byte[] content = inputStream.readAllBytes();

                return Message.<byte[]>builder().errcode(ErrorCode._SUCCESS.getKey())
                        .errmsg(ErrorCode._SUCCESS.getValue()).data(content).build();
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
    public Message<Void> download(String bucket, String fileName, File file) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, Normal.EMPTY, fileName);
            String url = getUrl(bucket + Symbol.SLASH + objectKey);

            // Use try-with-resources to automatically close both streams
            try (InputStream inputStream = client.get(url); OutputStream outputStream = new FileOutputStream(file)) {
                inputStream.transferTo(outputStream);
            }

            return Message.<Void>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .build();
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
            String url = getUrl(
                    this.context.getBucket() + Symbol.SLASH
                            + (StringKit.isBlank(prefix) ? Normal.EMPTY : prefix + Symbol.SLASH));
            return Message.<List<Blob>>builder().errcode(ErrorCode._SUCCESS.getKey())
                    .errmsg(ErrorCode._SUCCESS.getValue())
                    .data(client.list(url).stream().filter(resource -> !resource.isDirectory()).map(resource -> {
                        Map<String, Object> extend = new HashMap<>();
                        extend.put("tag", resource.getEtag());
                        extend.put("lastModified", resource.getModified());
                        return Blob.builder().name(resource.getName())
                                .size(StringKit.toString(resource.getContentLength())).extend(extend).build();
                    }).collect(Collectors.toList())).build();
        } catch (Exception e) {
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
            String sourceUrl = getUrl(bucket + Symbol.SLASH + oldObjectKey);
            String destinationUrl = getUrl(bucket + Symbol.SLASH + newObjectKey);
            client.move(sourceUrl, destinationUrl);
            return Message.<Void>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .build();
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
        return upload(this.context.getBucket(), Normal.EMPTY, fileName, content);
    }

    /**
     * Uploads a byte array to a specified path in the default storage bucket.
     *
     * @param bucket   The name of the storage bucket.
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message<Blob> upload(String bucket, String fileName, byte[] content) {
        return upload(bucket, Normal.EMPTY, fileName, content);
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
            String url = getUrl(bucket + Symbol.SLASH + objectKey);

            // Create parent directories if they do not exist
            String parentUrl = getUrl(
                    bucket + Symbol.SLASH + (StringKit.isBlank(prefix) ? Normal.EMPTY : prefix)
                            + (StringKit.isBlank(path) ? Normal.EMPTY : Symbol.SLASH + path));
            client.createDirectory(parentUrl);

            client.put(url, content);

            return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
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
            String url = getUrl(bucket + Symbol.SLASH + objectKey);
            client.delete(url);
            return Message.<Void>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .build();
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
     * Releases WebDAV client resources held by this provider.
     */
    @Override
    public void close() {
        try {
            this.client.shutdown();
        } catch (Exception e) {
            // Ignore close-time failures.
        }
    }

    /**
     * Constructs the full WebDAV URL for a given relative path.
     *
     * @param path The relative path to the resource.
     * @return The complete WebDAV URL.
     */
    private String getUrl(String path) {
        return PathResolve.of(context.getEndpoint(), path).toString();
    }

}
