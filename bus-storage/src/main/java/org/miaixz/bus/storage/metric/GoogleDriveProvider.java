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
package org.miaixz.bus.storage.metric;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.builtin.ResponseBodyInputStream;
import org.miaixz.bus.storage.magic.Blob;
import org.miaixz.bus.storage.magic.ErrorCode;

/**
 * Storage service provider for Google Drive and Google Workspace. This provider integrates with Google Drive using
 * Google Drive API v3 for enterprise file storage and collaboration.
 * <p>
 * <strong>Supported Storage Locations:</strong>
 * <ul>
 * <li>My Drive: bucket = "root" or "me" (default)</li>
 * <li>Shared Drive (Team Drive): bucket = "drive:{drive-id}"</li>
 * <li>Specific Folder: bucket = "{folder-id}"</li>
 * </ul>
 * <p>
 * Configuration:
 * <ul>
 * <li>endpoint: Google Drive API endpoint (default: https://www.googleapis.com/drive/v3)</li>
 * <li>bucket: Storage location identifier (see above for formats)</li>
 * <li>accessKey: Google Cloud application client ID</li>
 * <li>secretKey: Google Cloud application client secret</li>
 * <li>region: Not used for Google Drive (can be empty)</li>
 * <li>extension: Optional refresh token for user-delegated access</li>
 * </ul>
 * <p>
 * <strong>Usage Examples:</strong>
 *
 * <pre>{@code
 *
 * // My Drive
 * Context context = Context.builder().bucket("root").accessKey("client-id").secretKey("client-secret").build();
 *
 * // Shared Drive
 * Context context = Context.builder().bucket("drive:0ABcDeFgHiJkLmNoPqRsTuVwXyZ").accessKey("client-id")
 *         .secretKey("client-secret").build();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class GoogleDriveProvider extends AbstractProvider {

    /**
     * The HTTP client instance used for making requests to the Google Drive API.
     */
    private final Httpd client;

    /**
     * OAuth 2.0 access token for authentication.
     */
    private String accessToken;

    /**
     * Token expiration time in milliseconds.
     */
    private long tokenExpireTime;

    /**
     * Google Drive API base URL.
     */
    private static final String DRIVE_API_BASE = "https://www.googleapis.com/drive/v3";

    /**
     * Google Drive upload API base URL.
     */
    private static final String UPLOAD_API_BASE = "https://www.googleapis.com/upload/drive/v3";

    /**
     * OAuth 2.0 token endpoint.
     */
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    /**
     * Constructs a Google Drive storage provider with the given context. Initializes the HTTP client and obtains an
     * access token using OAuth 2.0 client credentials flow.
     *
     * @param context The storage context, containing endpoint, bucket, access key (client ID), secret key (client
     *                secret), and other configurations.
     * @throws IllegalArgumentException If required context parameters are missing or invalid.
     */
    public GoogleDriveProvider(Context context) {
        this.context = context;

        Assert.notBlank(this.context.getAccessKey(), "[accessKey] (client ID) cannot be blank");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] (client secret) cannot be blank");

        if (StringKit.isBlank(this.context.getEndpoint())) {
            this.context.setEndpoint(DRIVE_API_BASE);
        }

        if (StringKit.isBlank(this.context.getBucket())) {
            this.context.setBucket("root");
        }

        this.client = new Httpd();
        this.accessToken = null;
        this.tokenExpireTime = 0;
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
     * Reads metadata for a file in the default Google Drive folder.
     *
     * @param fileName The file name or Google Drive file ID to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message stat(String fileName) {
        return stat(this.context.getBucket(), fileName);
    }

    /**
     * Reads metadata for a file in the specified Google Drive folder.
     *
     * @param bucket   The parent folder ID.
     * @param fileName The file name or Google Drive file ID to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message stat(String bucket, String fileName) {
        return statKey(bucket, fileName);
    }

    /**
     * Reads metadata for an exact Google Drive object key. The key may be a file ID or a file name in the folder.
     *
     * @param bucket    The parent folder ID.
     * @param objectKey The Google Drive file ID or file name.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message statKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }
            Map<String, Object> metadata = resolveFileMetadata(bucket, objectKey);
            if (metadata == null) {
                return Message.builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(toBlob(bucket, objectKey, metadata, null)).build();
        } catch (Exception e) {
            Errors error = StringKit.containsIgnoreCase(e.getMessage(), "404") ? ErrorCode._113010 : ErrorCode._113012;
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
     * Opens a stream for a file in the default Google Drive folder.
     *
     * @param fileName The file name or Google Drive file ID to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message stream(String fileName) {
        return stream(this.context.getBucket(), fileName);
    }

    /**
     * Opens a stream for a file in the specified Google Drive folder.
     *
     * @param bucket   The parent folder ID.
     * @param fileName The file name or Google Drive file ID to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message stream(String bucket, String fileName) {
        return streamKey(bucket, fileName);
    }

    /**
     * Opens a stream for an exact Google Drive object key. The key may be a file ID or a file name in the folder.
     *
     * @param bucket    The parent folder ID.
     * @param objectKey The Google Drive file ID or file name.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message streamKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }
            Map<String, Object> metadata = resolveFileMetadata(bucket, objectKey);
            if (metadata == null) {
                return Message.builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }
            String fileId = (String) metadata.get("id");
            String url = context.getEndpoint() + "/files/" + fileId + "?alt=media";
            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + getAccessToken()).get().build();
            Response response = client.newCall(request).execute();
            if (response.code() == 401) {
                response.close();
                refreshAccessToken();
                return streamKey(bucket, objectKey);
            }
            if (!response.isSuccessful()) {
                Errors error = toError(response.code());
                response.close();
                return Message.builder().errcode(error.getKey()).errmsg(error.getValue()).build();
            }
            if (response.body() == null) {
                response.close();
                return Message.builder().errcode(ErrorCode._113012.getKey()).errmsg(ErrorCode._113012.getValue())
                        .build();
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(toBlob(bucket, objectKey, metadata, new ResponseBodyInputStream(response))).build();
        } catch (Exception e) {
            Errors error = StringKit.containsIgnoreCase(e.getMessage(), "404") ? ErrorCode._113010 : ErrorCode._113012;
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
        }
    }

    /**
     * Downloads a file from the specified storage bucket and returns its content as a byte array.
     *
     * @param bucket   The name of the storage bucket (parent folder ID).
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation. If successful, the data field contains the file
     *         content as a byte array; otherwise, it contains error information.
     */
    @Override
    public Message download(String bucket, String fileName) {
        try {
            String parentId = resolveParentId(bucket);
            String fileId = findFileByName(fileName, parentId);

            if (fileId == null) {
                return Message.builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            String url = context.getEndpoint() + "/files/" + fileId + "?alt=media";

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + getAccessToken()).get().build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    if (response.code() == 401) {
                        refreshAccessToken();
                        return download(bucket, fileName);
                    }
                    throw new IOException("Unexpected code " + response);
                }

                byte[] content = response.body().bytes();

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
     * Downloads a file from the specified storage bucket and saves it directly to a local file using streaming.
     *
     * @param bucket   The name of the storage bucket (parent folder ID).
     * @param fileName The name of the file to download.
     * @param file     The target local file to save the downloaded content.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message download(String bucket, String fileName, File file) {
        try {
            String parentId = resolveParentId(bucket);
            String fileId = findFileByName(fileName, parentId);

            if (fileId == null) {
                return Message.builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            String url = context.getEndpoint() + "/files/" + fileId + "?alt=media";

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + getAccessToken()).get().build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    if (response.code() == 401) {
                        refreshAccessToken();
                        return download(bucket, fileName, file);
                    }
                    throw new IOException("Unexpected code " + response);
                }

                try (InputStream inputStream = response.body().byteStream();
                        OutputStream outputStream = new FileOutputStream(file)) {
                    inputStream.transferTo(outputStream);
                }

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
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
            String parentId = resolveParentId(context.getBucket());
            String query = String.format("'%s' in parents and trashed=false", parentId);

            String url = context.getEndpoint() + "/files?q=" + URLEncoder.encode(query, Charset.UTF_8)
                    + "&fields=files(id,name,size,mimeType,modifiedTime)&pageSize=100";

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + getAccessToken()).get().build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    if (response.code() == 401) {
                        refreshAccessToken();
                        return list();
                    }
                    throw new IOException("Unexpected code " + response);
                }

                Map<String, Object> jsonMap = JsonKit.toMap(response.body().string());
                List<Map<String, Object>> files = (List<Map<String, Object>>) jsonMap.get("files");
                List<Blob> blobs = new ArrayList<>();

                if (files != null) {
                    for (Map<String, Object> file : files) {
                        Map<String, Object> extend = new HashMap<>();
                        extend.put("id", file.get("id"));
                        extend.put("mimeType", file.get("mimeType"));
                        extend.put("modifiedTime", file.get("modifiedTime"));

                        String size = file.get("size") != null ? file.get("size").toString() : "0";

                        blobs.add(Blob.builder().name((String) file.get("name")).size(size).extend(extend).build());
                    }
                }

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .data(blobs).build();
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
     * @param path    The path where the file is located (not used in Google Drive).
     * @param oldName The current name of the file.
     * @param newName The new name for the file.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message rename(String path, String oldName, String newName) {
        return rename(this.context.getBucket(), path, oldName, newName);
    }

    /**
     * Renames a file within the specified bucket.
     *
     * @param bucket  The name of the storage bucket (parent folder ID).
     * @param path    The path where the file is located (not used in Google Drive).
     * @param oldName The current name of the file.
     * @param newName The new name for the file.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message rename(String bucket, String path, String oldName, String newName) {
        try {
            String parentId = resolveParentId(bucket);
            String fileId = findFileByName(oldName, parentId);

            if (fileId == null) {
                return Message.builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            String url = context.getEndpoint() + "/files/" + fileId;
            String requestBody = String.format("{\"name\":\"%s\"}", newName);

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + getAccessToken())
                    .addHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .patch(RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_JSON), requestBody)).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    if (response.code() == 401) {
                        refreshAccessToken();
                        return rename(bucket, path, oldName, newName);
                    }
                    throw new IOException("Unexpected code " + response);
                }

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage rename failed; provider={}, bucket={}, sourceObject={}, targetObject={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    bucket,
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
        return upload(this.context.getBucket(), Normal.EMPTY, fileName, content);
    }

    /**
     * Uploads a byte array to a specified bucket.
     *
     * @param bucket   The name of the storage bucket (parent folder ID).
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String bucket, String fileName, byte[] content) {
        return upload(bucket, Normal.EMPTY, fileName, content);
    }

    /**
     * Uploads a byte array to the specified storage bucket. Uses resumable upload for reliable file transfer.
     *
     * @param bucket   The name of the storage bucket (parent folder ID).
     * @param path     The target path for the file (not used in Google Drive).
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String bucket, String path, String fileName, byte[] content) {
        try {
            String parentId = resolveParentId(bucket);

            // Create file metadata
            String metadata = String.format("{\"name\":\"%s\",\"parents\":[\"%s\"]}", fileName, parentId);

            // Create resumable upload session
            String url = UPLOAD_API_BASE + "/files?uploadType=resumable";

            Request createSessionRequest = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + getAccessToken())
                    .addHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .addHeader("X-Upload-Content-Length", String.valueOf(content.length))
                    .post(RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_JSON), metadata)).build();

            String uploadUrl;
            try (Response response = client.newCall(createSessionRequest).execute()) {
                if (!response.isSuccessful()) {
                    if (response.code() == 401) {
                        refreshAccessToken();
                        return upload(bucket, path, fileName, content);
                    }
                    throw new IOException("Failed to create upload session: " + response.code());
                }

                uploadUrl = response.header(HTTP.LOCATION);
            }

            // Upload file content
            Request uploadRequest = new Request.Builder().url(uploadUrl)
                    .addHeader(HTTP.CONTENT_LENGTH, String.valueOf(content.length))
                    .put(RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM), content)).build();

            try (Response response = client.newCall(uploadRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Upload failed: " + response.code());
                }

                Map<String, Object> jsonMap = JsonKit.toMap(response.body().string());
                String fileId = (String) jsonMap.get("id");

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .data(Blob.builder().name(fileName).path(fileId).build()).build();
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage upload failed; provider={}, bucket={}, object={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    fileName,
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
     * @param path     The target path for the file (not used in Google Drive).
     * @param fileName The name of the file to upload.
     * @param content  The file content as an {@link InputStream}.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String path, String fileName, InputStream content) {
        return upload(this.context.getBucket(), path, fileName, content);
    }

    /**
     * Uploads an input stream to the specified storage bucket.
     *
     * @param bucket   The name of the storage bucket (parent folder ID).
     * @param path     The target path for the file (not used in Google Drive).
     * @param fileName The name of the file to upload.
     * @param content  The file content as an {@link InputStream}.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String bucket, String path, String fileName, InputStream content) {
        try {
            byte[] contentBytes = content.readAllBytes();
            return upload(bucket, path, fileName, contentBytes);
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage upload failed; provider={}, bucket={}, object={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    fileName,
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
     * @param bucket   The name of the storage bucket (parent folder ID).
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String bucket, String fileName) {
        return remove(bucket, Normal.EMPTY, fileName);
    }

    /**
     * Removes a file from the specified storage bucket.
     *
     * @param bucket   The name of the storage bucket (parent folder ID).
     * @param path     The storage path where the file is located (not used in Google Drive).
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String bucket, String path, String fileName) {
        try {
            String parentId = resolveParentId(bucket);
            String fileId = findFileByName(fileName, parentId);

            if (fileId == null) {
                return Message.builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            String url = context.getEndpoint() + "/files/" + fileId;

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + getAccessToken()).delete().build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    if (response.code() == 401) {
                        refreshAccessToken();
                        return remove(bucket, path, fileName);
                    }
                    throw new IOException("Unexpected code " + response);
                }

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    "Storage remove failed; provider={}, bucket={}, object={}, status=failure, error={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    fileName,
                    e.getMessage(),
                    e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * Removes a file from the specified storage bucket based on its path.
     *
     * @param bucket The name of the storage bucket (parent folder ID).
     * @param path   The target path of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString(), Normal.EMPTY);
    }

    /**
     * Obtains a valid access token, refreshing it if necessary.
     *
     * @return The current valid access token.
     * @throws IOException If token acquisition fails.
     */
    private String getAccessToken() throws IOException {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpireTime) {
            refreshAccessToken();
        }
        return accessToken;
    }

    /**
     * Refreshes the OAuth 2.0 access token using client credentials flow or refresh token.
     *
     * @throws IOException If token refresh fails.
     */
    private void refreshAccessToken() throws IOException {
        String requestBody;

        // Use refresh token if available, otherwise use client credentials
        if (StringKit.isNotBlank(context.getExtension())) {
            requestBody = String.format(
                    "client_id=%s&client_secret=%s&refresh_token=%s&grant_type=refresh_token",
                    URLEncoder.encode(context.getAccessKey(), Charset.UTF_8),
                    URLEncoder.encode(context.getSecretKey(), Charset.UTF_8),
                    URLEncoder.encode(context.getExtension(), Charset.UTF_8));
        } else {
            requestBody = String.format(
                    "client_id=%s&client_secret=%s&scope=%s&grant_type=client_credentials",
                    URLEncoder.encode(context.getAccessKey(), Charset.UTF_8),
                    URLEncoder.encode(context.getSecretKey(), Charset.UTF_8),
                    URLEncoder.encode("https://www.googleapis.com/auth/drive", Charset.UTF_8));
        }

        Request request = new Request.Builder().url(TOKEN_ENDPOINT)
                .addHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED), requestBody)).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to refresh access token: " + response.code());
            }

            Map<String, Object> jsonMap = JsonKit.toMap(response.body().string());
            this.accessToken = (String) jsonMap.get("access_token");
            int expiresIn = ((Number) jsonMap.get("expires_in")).intValue();
            this.tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;
        }
    }

    /**
     * Resolves the parent folder ID based on the bucket configuration.
     *
     * @param bucket The bucket identifier.
     * @return The parent folder ID.
     */
    private String resolveParentId(String bucket) {
        if ("root".equals(bucket) || "me".equals(bucket) || StringKit.isBlank(bucket)) {
            return "root";
        }

        // Shared Drive: drive:{drive-id}
        if (bucket.startsWith("drive:")) {
            return bucket.substring(6);
        }

        // Folder ID
        return bucket;
    }

    /**
     * Searches for a file by name in the specified parent folder.
     *
     * @param fileName The file name to search for.
     * @param parentId The parent folder ID.
     * @return The file ID if found, null otherwise.
     * @throws IOException If the search fails.
     */
    private String findFileByName(String fileName, String parentId) throws IOException {
        String query = String
                .format("name='%s' and '%s' in parents and trashed=false", fileName.replace("'", "\\'"), parentId);

        String url = context.getEndpoint() + "/files?q=" + URLEncoder.encode(query, Charset.UTF_8)
                + "&fields=files(id,name)";

        Request request = new Request.Builder().url(url).addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + getAccessToken())
                .get().build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 401) {
                    refreshAccessToken();
                    return findFileByName(fileName, parentId);
                }
                throw new IOException("Failed to search file: " + response.code());
            }

            Map<String, Object> jsonMap = JsonKit.toMap(response.body().string());
            List<Map<String, Object>> files = (List<Map<String, Object>>) jsonMap.get("files");

            if (files != null && !files.isEmpty()) {
                return (String) files.get(0).get("id");
            }

            return null;
        }
    }

    /**
     * Resolves file metadata by treating the key as a file ID first, then as a file name in the parent folder.
     *
     * @param bucket    The parent folder ID.
     * @param objectKey The Google Drive file ID or file name.
     * @return The file metadata, or {@code null} if not found.
     * @throws IOException If metadata lookup fails.
     */
    private Map<String, Object> resolveFileMetadata(String bucket, String objectKey) throws IOException {
        Map<String, Object> metadata = getFileMetadata(objectKey, true);
        if (metadata != null) {
            return metadata;
        }
        String fileId = findFileByName(nameOf(objectKey), resolveParentId(bucket));
        return fileId == null ? null : getFileMetadata(fileId, true);
    }

    /**
     * Reads Google Drive file metadata by file ID.
     *
     * @param fileId The Google Drive file ID.
     * @param retry  Whether to refresh token and retry on authentication failure.
     * @return The metadata, or {@code null} when the file does not exist.
     * @throws IOException If metadata lookup fails.
     */
    private Map<String, Object> getFileMetadata(String fileId, boolean retry) throws IOException {
        String fields = "id,name,size,mimeType,modifiedTime,md5Checksum,sha256Checksum";
        String url = context.getEndpoint() + "/files/" + fileId + "?fields=" + fields;
        Request request = new Request.Builder().url(url).addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + getAccessToken())
                .get().build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 401 && retry) {
                refreshAccessToken();
                return getFileMetadata(fileId, false);
            }
            if (response.code() == 404) {
                return null;
            }
            if (!response.isSuccessful()) {
                throw new IOException("Metadata failed: " + response.code());
            }
            return JsonKit.toMap(response.body().string());
        }
    }

    /**
     * Converts Google Drive metadata to a storage blob.
     *
     * @param bucket      The parent folder ID.
     * @param objectKey   The requested object key.
     * @param metadata    The Google Drive metadata.
     * @param inputStream The optional file stream.
     * @return The storage blob.
     */
    private Blob toBlob(String bucket, String objectKey, Map<String, Object> metadata, InputStream inputStream) {
        Map<String, Object> extend = new HashMap<>();
        extend.put("id", metadata.get("id"));
        extend.put("mimeType", metadata.get("mimeType"));
        extend.put("modifiedTime", metadata.get("modifiedTime"));
        extend.put("sha256Checksum", metadata.get("sha256Checksum"));

        String hash = metadata.get("md5Checksum") == null ? (String) metadata.get("sha256Checksum")
                : (String) metadata.get("md5Checksum");
        return Blob.builder().inputStream(inputStream).bucket(bucket).key(objectKey).name((String) metadata.get("name"))
                .path(objectKey).size(metadata.get("size") == null ? "0" : String.valueOf(metadata.get("size")))
                .type((String) metadata.get("mimeType")).hash(hash).extend(extend).build();
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
     * Extracts a file name from a key or path.
     *
     * @param objectKey The object key.
     * @return The file name.
     */
    private String nameOf(String objectKey) {
        int index = objectKey.lastIndexOf(Symbol.SLASH);
        return index < 0 ? objectKey : objectKey.substring(index + 1);
    }

}
