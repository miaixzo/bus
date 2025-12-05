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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.center.date.Formatter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.ErrorCode;
import org.miaixz.bus.storage.magic.Blob;

/**
 * Storage service provider for Upyun Object Storage Service. This provider integrates with Upyun OSS for file storage
 * operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UpyunOssProvider extends AbstractProvider {

    /**
     * The HTTP client instance used for making requests to the Upyun API.
     */
    private final Httpd client;

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

        this.client = new Httpd();
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
     * Downloads a file from the default storage bucket.
     *
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation, including the file content as a byte array if
     *         successful.
     */
    @Override
    public Message download(String fileName) {
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
    public Message download(String bucket, String fileName) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, Normal.EMPTY, fileName);
            String path = "/" + bucket + "/" + objectKey;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature("GET", path, date, 0);

            Request request = new Request.Builder().url(this.context.getEndpoint() + path)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + signature)
                    .addHeader("Date", date).get().build();

            try (Response response = this.client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Read all bytes directly from the response body
                byte[] content = response.body().bytes();

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .data(content).build();
            }
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
    public Message download(String bucket, String fileName, File file) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, Normal.EMPTY, fileName);
            String path = "/" + bucket + "/" + objectKey;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature("GET", path, date, 0);

            Request request = new Request.Builder().url(this.context.getEndpoint() + path)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + signature)
                    .addHeader("Date", date).get().build();

            try (Response response = this.client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Use try-with-resources to automatically close both streams
                try (InputStream inputStream = response.body().byteStream();
                        OutputStream outputStream = new FileOutputStream(file)) {
                    inputStream.transferTo(outputStream);
                }

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            }
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
     * @return A {@link Message} containing the result of the operation, including a list of {@link Blob} objects if
     *         successful.
     */
    @Override
    public Message list() {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String path = "/" + context.getBucket() + "/" + prefix;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature("GET", path, date, 0);

            Request request = new Request.Builder().url(this.context.getEndpoint() + path)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + signature)
                    .addHeader("Date", date).addHeader("x-upyun-list-limit", "100").get().build();

            try (Response response = this.client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String responseBody = response.body().string();
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
                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .data(files).build();
            }
        } catch (Exception e) {
            Logger.error(
                    "Failed to list objects in bucket: {}. Error: {}",
                    this.context.getBucket(),
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
    public Message rename(String path, String oldName, String newName) {
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
    public Message rename(String bucket, String path, String oldName, String newName) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String oldObjectKey = Builder.buildObjectKey(prefix, path, oldName);
            String newObjectKey = Builder.buildObjectKey(prefix, path, newName);
            String oldPath = "/" + bucket + "/" + oldObjectKey;
            String newPath = "/" + bucket + "/" + newObjectKey;

            // Download original file content
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String getSignature = generateSignature("GET", oldPath, date, 0);
            Request getRequest = new Request.Builder().url(this.context.getEndpoint() + oldPath)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + getSignature)
                    .addHeader("Date", date).get().build();

            byte[] content;
            try (Response response = this.client.newCall(getRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                content = response.body().bytes();
            }

            // Upload to new path
            String putDate = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String putSignature = generateSignature("PUT", newPath, putDate, content.length);
            Request putRequest = new Request.Builder().url(this.context.getEndpoint() + newPath)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + putSignature)
                    .addHeader("Date", putDate).addHeader("Content-Length", String.valueOf(content.length))
                    .addHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
                    .put(RequestBody.create(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM), content)).build();

            try (Response response = this.client.newCall(putRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
            }

            // Delete original file
            String deleteDate = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String deleteSignature = generateSignature("DELETE", oldPath, deleteDate, 0);
            Request deleteRequest = new Request.Builder().url(this.context.getEndpoint() + oldPath)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + deleteSignature)
                    .addHeader("Date", deleteDate).delete().build();

            try (Response response = this.client.newCall(deleteRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
            }

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (Exception e) {
            Logger.error(
                    "Failed to rename file from: {} to: {} in bucket: {} with path: {}, error: {}",
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
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String fileName, byte[] content) {
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
    public Message upload(String bucket, String fileName, byte[] content) {
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
    public Message upload(String bucket, String path, String fileName, byte[] content) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, path, fileName);
            String requestPath = "/" + bucket + "/" + objectKey;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature("PUT", requestPath, date, content.length);

            Request request = new Request.Builder().url(this.context.getEndpoint() + requestPath)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + signature)
                    .addHeader("Date", date).addHeader("Content-Length", String.valueOf(content.length))
                    .addHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
                    .put(RequestBody.create(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM), content)).build();

            try (Response response = this.client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .data(Blob.builder().name(fileName).path(objectKey).build()).build();
            }
        } catch (Exception e) {
            Logger.error(
                    "Failed to upload file: {} to bucket: {} with path: {}, error: {}",
                    fileName,
                    bucket,
                    path,
                    e.getMessage(),
                    e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
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
    public Message upload(String fileName, InputStream content) {
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
    public Message upload(String path, String fileName, InputStream content) {
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
    public Message upload(String bucket, String path, String fileName, InputStream content) {
        try {
            // Read the content from the input stream
            byte[] contentBytes = content.readAllBytes();
            return upload(bucket, path, fileName, contentBytes);
        } catch (Exception e) {
            Logger.error(
                    "Failed to upload file: {} to bucket: {} with path: {}, error: {}",
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
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String fileName) {
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
    public Message remove(String bucket, String fileName) {
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
    public Message remove(String bucket, String path, String fileName) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, path, fileName);
            String requestPath = "/" + bucket + "/" + objectKey;
            String date = Formatter.HTTP_DATETIME_FORMAT_GMT.format(ZonedDateTime.now());
            String signature = generateSignature("DELETE", requestPath, date, 0);

            Request request = new Request.Builder().url(this.context.getEndpoint() + requestPath)
                    .addHeader("Authorization", "UPYUN " + context.getAccessKey() + ":" + signature)
                    .addHeader("Date", date).delete().build();

            try (Response response = this.client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            }
        } catch (Exception e) {
            Logger.error(
                    "Failed to remove file: {} from bucket: {} with path: {}, error: {}",
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
     * @param path   The target path of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString(), Normal.EMPTY);
    }

}
