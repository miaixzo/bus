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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.center.date.Formatter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.Blob;
import org.miaixz.bus.storage.magic.ErrorCode;

/**
 * Storage service provider for Upyun Object Storage Service. This provider integrates with Upyun OSS for file storage
 * operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class UpyunOssProvider extends AbstractProvider {

    /**
     * Constructs an Upyun OSS provider with the given context. Initializes the HTTP client using the provided
     * credentials and endpoint configuration.
     *
     * @param context The storage context, containing endpoint, bucket, access key, secret key, and other
     *                configurations.
     * @throws IllegalArgumentException If required context parameters are missing or invalid.
     */
    public UpyunOssProvider(Context context) {
        this.context = context;

        Assert.notBlank(this.context.getEndpoint(), "[endpoint] cannot be blank");
        Assert.notBlank(this.context.getBucket(), "[bucket] cannot be blank");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] cannot be blank");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] cannot be blank");
    }

    /**
     * Generates the Upyun REST API signature for authentication.
     *
     * @param method        The HTTP method (e.g., GET, PUT, DELETE).
     * @param path          The request path (e.g., /<bucket>/<path>).
     * @param date          The GMT formatted date string.
     * @param contentLength The length of the request body. Use 0 for GET and DELETE requests.
     * @return The generated signature string.
     */
    private String generateSignature(String method, String path, String date, long contentLength) {
        String signStr = String.format(
                "%s&%s&%s&%d&%s",
                method,
                path,
                date,
                contentLength,
                org.miaixz.bus.crypto.Builder.md5(this.context.getSecretKey()));
        return org.miaixz.bus.crypto.Builder.md5(signStr);
    }

    /**
     * Reads metadata for a file in the default Upyun bucket.
     *
     * @param fileName The file name to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message<Blob> stat(String fileName) {
        return stat(this.context.getBucket(), fileName);
    }

    /**
     * Reads metadata for a file using the provider's normal Upyun path-building rules.
     *
     * @param bucket   The bucket name.
     * @param fileName The file name to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message<Blob> stat(String bucket, String fileName) {
        String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
        return statKey(bucket, Builder.buildObjectKey(prefix, Normal.EMPTY, fileName));
    }

    /**
     * Reads metadata for an exact Upyun object key.
     *
     * @param bucket    The bucket name.
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

            String path = Symbol.SLASH + bucket + Symbol.SLASH + objectKey;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature(Http.Method.HEAD.value(), path, date, 0);

            try (Response response = head(
                    this.context.getEndpoint() + path,
                    header(Http.Header.AUTHORIZATION, "UPYUN " + context.getAccessKey() + ":" + signature),
                    header(Http.Header.DATE, date))) {
                if (!response.successful()) {
                    return Message.<Blob>builder().errcode(toError(response.code()).getKey())
                            .errmsg(toError(response.code()).getValue()).build();
                }

                String name = nameOf(objectKey);
                Map<String, Object> extend = new HashMap<>();
                extend.put("date", header(response, Http.Header.DATE));
                extend.put("lastModified", header(response, Http.Header.LAST_MODIFIED));

                return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey())
                        .errmsg(ErrorCode._SUCCESS.getValue())
                        .data(
                                Blob.builder().bucket(bucket).key(objectKey).name(name).path(objectKey)
                                        .size(header(response, Http.Header.CONTENT_LENGTH, "0"))
                                        .type(header(response, Http.Header.CONTENT_TYPE)).hash(header(response, Http.Header.ETAG))
                                        .extend(extend).build())
                        .build();
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage stat failed; provider={}, bucket={}, object={}, code={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    objectKey,
                    ErrorCode._113012.getKey(),
                    e.getMessage(),
                    e);
            return Message.<Blob>builder().errcode(ErrorCode._113012.getKey()).errmsg(ErrorCode._113012.getValue())
                    .build();
        }
    }

    /**
     * Opens a stream for a file in the default Upyun bucket.
     *
     * @param fileName The file name to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message<Blob> stream(String fileName) {
        return stream(this.context.getBucket(), fileName);
    }

    /**
     * Opens a stream for a file using the provider's normal Upyun path-building rules.
     *
     * @param bucket   The bucket name.
     * @param fileName The file name to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message<Blob> stream(String bucket, String fileName) {
        String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
        return streamKey(bucket, Builder.buildObjectKey(prefix, Normal.EMPTY, fileName));
    }

    /**
     * Opens a stream for an exact Upyun object key.
     *
     * @param bucket    The bucket name.
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

            String path = Symbol.SLASH + bucket + Symbol.SLASH + objectKey;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature(Http.Method.GET.value(), path, date, 0);

            Response response = get(
                    this.context.getEndpoint() + path,
                    header(Http.Header.AUTHORIZATION, "UPYUN " + context.getAccessKey() + ":" + signature),
                    header(Http.Header.DATE, date));
            if (!response.successful()) {
                Errors error = toError(response.code());
                response.close();
                return Message.<Blob>builder().errcode(error.getKey()).errmsg(error.getValue()).build();
            }

            String name = nameOf(objectKey);
            Map<String, Object> extend = new HashMap<>();
            extend.put("date", header(response, Http.Header.DATE));
            extend.put("lastModified", header(response, Http.Header.LAST_MODIFIED));

            return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(
                            Blob.builder().inputStream(stream(response)).bucket(bucket).key(objectKey).name(name)
                                    .path(objectKey).size(header(response, Http.Header.CONTENT_LENGTH, "0"))
                                    .type(header(response, Http.Header.CONTENT_TYPE)).hash(header(response, Http.Header.ETAG))
                                    .extend(extend).build())
                    .build();
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage stream failed; provider={}, bucket={}, object={}, code={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    objectKey,
                    ErrorCode._113012.getKey(),
                    e.getMessage(),
                    e);
            return Message.<Blob>builder().errcode(ErrorCode._113012.getKey()).errmsg(ErrorCode._113012.getValue())
                    .build();
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
        return download(this.context.getBucket(), fileName);
    }

    /**
     * Downloads a file from the specified storage bucket and returns its content as a byte array.
     * <p>
     * This method reads the entire file content into memory as a byte array, making it suitable for images, PDFs, DOCX
     * files, and other binary files. The HTTP response is automatically closed using try-with-resources to prevent
     * resource leaks.
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
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, Normal.EMPTY, fileName);
            String path = Symbol.SLASH + bucket + Symbol.SLASH + objectKey;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature("GET", path, date, 0);

            try (Response response = get(
                    this.context.getEndpoint() + path,
                    header(Http.Header.AUTHORIZATION, "UPYUN " + context.getAccessKey() + ":" + signature),
                    header(Http.Header.DATE, date))) {
                if (!response.successful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Read all bytes directly from the response body
                byte[] content = response.bytes();

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
            String path = Symbol.SLASH + bucket + Symbol.SLASH + objectKey;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature("GET", path, date, 0);

            try (Response response = get(
                    this.context.getEndpoint() + path,
                    header(Http.Header.AUTHORIZATION, "UPYUN " + context.getAccessKey() + ":" + signature),
                    header(Http.Header.DATE, date))) {
                if (!response.successful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Use try-with-resources to automatically close both streams
                try (InputStream inputStream = stream(response);
                        OutputStream outputStream = new FileOutputStream(file)) {
                    inputStream.transferTo(outputStream);
                }

                return Message.<Void>builder().errcode(ErrorCode._SUCCESS.getKey())
                        .errmsg(ErrorCode._SUCCESS.getValue()).build();
            }
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
            String path = Symbol.SLASH + context.getBucket() + Symbol.SLASH + prefix;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature("GET", path, date, 0);

            try (Response response = get(
                    this.context.getEndpoint() + path,
                    header(Http.Header.AUTHORIZATION, "UPYUN " + context.getAccessKey() + ":" + signature),
                    header(Http.Header.DATE, date),
                    header("x-upyun-list-limit", "100"))) {
                if (!response.successful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String responseBody = response.text();
                List<Blob> files = new ArrayList<>();
                String[] lines = responseBody.split("\n");
                for (String line : lines) {
                    String[] parts = line.split("\t");
                    if (parts.length == 4) {
                        Map<String, Object> extend = new HashMap<>();
                        extend.put("tag", parts[0]);
                        extend.put("type", parts[1]);
                        extend.put("size", parts[2]);
                        extend.put("lastModified", parts[3]);
                        files.add(Blob.builder().name(parts[0]).size(parts[2]).extend(extend).build());
                    }
                }
                return Message.<List<Blob>>builder().errcode(ErrorCode._SUCCESS.getKey())
                        .errmsg(ErrorCode._SUCCESS.getValue()).data(files).build();
            }
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
            String oldPath = Symbol.SLASH + bucket + Symbol.SLASH + oldObjectKey;
            String newPath = Symbol.SLASH + bucket + Symbol.SLASH + newObjectKey;

            // Download original file content
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String getSignature = generateSignature("GET", oldPath, date, 0);

            byte[] content;
            try (Response response = get(
                    this.context.getEndpoint() + oldPath,
                    header(Http.Header.AUTHORIZATION, "UPYUN " + context.getAccessKey() + ":" + getSignature),
                    header(Http.Header.DATE, date))) {
                if (!response.successful()) {
                    throw new IOException("Unexpected code " + response);
                }
                content = response.bytes();
            }

            // Upload to new path
            String putDate = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String putSignature = generateSignature("PUT", newPath, putDate, content.length);

            try (Response response = put(
                    this.context.getEndpoint() + newPath,
                    content,
                    MediaType.APPLICATION_OCTET_STREAM,
                    header(Http.Header.AUTHORIZATION, "UPYUN " + context.getAccessKey() + ":" + putSignature),
                    header(Http.Header.DATE, putDate),
                    header(Http.Header.CONTENT_LENGTH, String.valueOf(content.length)))) {
                if (!response.successful()) {
                    throw new IOException("Unexpected code " + response);
                }
            }

            // Delete original file
            String deleteDate = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String deleteSignature = generateSignature("DELETE", oldPath, deleteDate, 0);

            try (Response response = delete(
                    this.context.getEndpoint() + oldPath,
                    header(Http.Header.AUTHORIZATION, "UPYUN " + context.getAccessKey() + ":" + deleteSignature),
                    header(Http.Header.DATE, deleteDate))) {
                if (!response.successful()) {
                    throw new IOException("Unexpected code " + response);
                }
            }

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
     * @return A {@link Message} containing the result of the operation, including the uploaded file information if
     *         successful.
     */
    @Override
    public Message<Blob> upload(String bucket, String path, String fileName, byte[] content) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, path, fileName);
            String requestPath = Symbol.SLASH + bucket + Symbol.SLASH + objectKey;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature(Http.Method.PUT.value(), requestPath, date, content.length);

            try (Response response = put(
                    this.context.getEndpoint() + requestPath,
                    content,
                    MediaType.APPLICATION_OCTET_STREAM,
                    header(Http.Header.AUTHORIZATION, "UPYUN " + context.getAccessKey() + ":" + signature),
                    header(Http.Header.DATE, date),
                    header(Http.Header.CONTENT_LENGTH, String.valueOf(content.length)))) {
                if (!response.successful()) {
                    throw new IOException("Unexpected code " + response);
                }
                return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey())
                        .errmsg(ErrorCode._SUCCESS.getValue())
                        .data(Blob.builder().name(fileName).path(objectKey).build()).build();
            }
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
            // Read the content from the input stream
            byte[] contentBytes = content.readAllBytes();
            return upload(bucket, path, fileName, contentBytes);
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
            String requestPath = Symbol.SLASH + bucket + Symbol.SLASH + objectKey;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature("DELETE", requestPath, date, 0);

            try (Response response = delete(
                    this.context.getEndpoint() + requestPath,
                    header(Http.Header.AUTHORIZATION, "UPYUN " + context.getAccessKey() + ":" + signature),
                    header(Http.Header.DATE, date))) {
                if (!response.successful()) {
                    throw new IOException("Unexpected code " + response);
                }
                return Message.<Void>builder().errcode(ErrorCode._SUCCESS.getKey())
                        .errmsg(ErrorCode._SUCCESS.getValue()).build();
            }
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
     * Maps HTTP status codes to storage errors.
     *
     * @param code The HTTP status code.
     * @return The storage error.
     */
    private Errors toError(int code) {
        if (code == 401 || code == 403) {
            return ErrorCode._113009;
        }
        if (code == 404) {
            return ErrorCode._113010;
        }
        return ErrorCode._113012;
    }

    /**
     * Extracts the object name from an object key.
     *
     * @param objectKey The object key.
     * @return The object name.
     */
    private String nameOf(String objectKey) {
        int index = objectKey.lastIndexOf(Symbol.SLASH);
        return index < 0 ? objectKey : objectKey.substring(index + 1);
    }

}
