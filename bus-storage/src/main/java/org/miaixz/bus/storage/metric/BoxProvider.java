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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.Blob;
import org.miaixz.bus.storage.magic.ErrorCode;

/**
 * Storage service provider for Box Enterprise. This provider integrates with Box using Box API v2 for enterprise
 * content management.
 * <p>
 * <strong>Supported Storage Locations:</strong>
 * <ul>
 * <li>Root Folder: bucket = "0" (default)</li>
 * <li>Specific Folder: bucket = "{folder-id}"</li>
 * </ul>
 * <p>
 * Configuration:
 * <ul>
 * <li>endpoint: Box API endpoint (default: https://api.box.com/2.0)</li>
 * <li>bucket: Folder ID (default: "0" for root)</li>
 * <li>accessKey: Box App client ID (not used for OAuth)</li>
 * <li>secretKey: Box App client secret (not used for OAuth)</li>
 * <li>extension: OAuth 2.0 access token (required)</li>
 * </ul>
 * <p>
 * <strong>Usage Example:</strong>
 * 
 * <pre>{@code
 * Context context = Context.builder().bucket("0").extension("your-oauth-access-token").build();
 *
 * BoxProvider provider = new BoxProvider(context);
 * provider.upload("document.pdf", fileBytes);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BoxProvider extends AbstractProvider {

    /**
     * The HTTP client instance used for making requests to the Box API.
     */
    private final Httpd client;

    /**
     * Box API base URL for metadata and file operations.
     */
    private static final String API_BASE = "https://api.box.com/2.0";

    /**
     * Box upload API base URL for file upload operations.
     */
    private static final String UPLOAD_BASE = "https://upload.box.com/api/2.0";

    /**
     * Constructs a Box Enterprise storage provider with the given context. Initializes the HTTP client and validates
     * the OAuth 2.0 access token.
     *
     * @param context The storage context, containing endpoint, bucket (folder ID), and OAuth access token.
     * @throws IllegalArgumentException If required context parameters are missing or invalid.
     */
    public BoxProvider(Context context) {
        this.context = context;
        Assert.notBlank(this.context.getExtension(), "[extension] (access token) cannot be blank");

        if (StringKit.isBlank(this.context.getEndpoint())) {
            this.context.setEndpoint(API_BASE);
        }
        if (StringKit.isBlank(this.context.getBucket())) {
            this.context.setBucket("0");
        }

        this.client = new Httpd();
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
     *
     * @param bucket   The folder ID in Box.
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation. If successful, the data field contains the file
     *         content as a byte array; otherwise, it contains error information.
     */
    @Override
    public Message download(String bucket, String fileName) {
        try {
            String fileId = findFileByName(fileName, bucket);
            if (fileId == null) {
                return Message.builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            String url = API_BASE + "/files/" + fileId + "/content";

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension()).get().build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Download failed: " + response.code());
                }

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
     * Downloads a file from the specified storage bucket and saves it directly to a local file using streaming.
     *
     * @param bucket   The folder ID in Box.
     * @param fileName The name of the file to download.
     * @param file     The target local file to save the downloaded content.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message download(String bucket, String fileName, File file) {
        try {
            String fileId = findFileByName(fileName, bucket);
            if (fileId == null) {
                return Message.builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            String url = API_BASE + "/files/" + fileId + "/content";

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension()).get().build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Download failed: " + response.code());
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
            String folderId = context.getBucket();
            String url = API_BASE + "/folders/" + folderId + "/items?fields=id,name,size,modified_at,type";

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension()).get().build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("List failed: " + response.code());
                }

                Map<String, Object> jsonMap = JsonKit.toMap(response.body().string());
                List<Map<String, Object>> entries = (List<Map<String, Object>>) jsonMap.get("entries");
                List<Blob> blobs = new ArrayList<>();

                if (entries != null) {
                    for (Map<String, Object> entry : entries) {
                        if ("file".equals(entry.get("type"))) {
                            Map<String, Object> extend = new HashMap<>();
                            extend.put("id", entry.get("id"));
                            extend.put("modified_at", entry.get("modified_at"));

                            String size = entry.get("size") != null ? entry.get("size").toString() : "0";
                            blobs.add(
                                    Blob.builder().name((String) entry.get("name")).size(size).extend(extend).build());
                        }
                    }
                }

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .data(blobs).build();
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
     * @param path    The path where the file is located (not used in Box).
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
     * @param bucket  The folder ID in Box.
     * @param path    The path where the file is located (not used in Box).
     * @param oldName The current name of the file.
     * @param newName The new name for the file.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message rename(String bucket, String path, String oldName, String newName) {
        try {
            String fileId = findFileByName(oldName, bucket);
            if (fileId == null) {
                return Message.builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            String url = API_BASE + "/files/" + fileId;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", newName);

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension())
                    .addHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .put(
                            RequestBody.of(
                                    MediaType.valueOf(MediaType.APPLICATION_JSON),
                                    JsonKit.toJsonString(requestBody)))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Rename failed: " + response.code());
                }

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            }
        } catch (Exception e) {
            Logger.error(
                    "Failed to rename file from: {} to: {} in bucket: {}, error: {}",
                    oldName,
                    newName,
                    bucket,
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
     * @param bucket   The folder ID in Box.
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String bucket, String fileName, byte[] content) {
        return upload(bucket, Normal.EMPTY, fileName, content);
    }

    /**
     * Uploads a byte array to the specified storage bucket. Uses multipart/form-data for file upload.
     *
     * @param bucket   The folder ID in Box.
     * @param path     The target path for the file (not used in Box).
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String bucket, String path, String fileName, byte[] content) {
        try {
            String url = UPLOAD_BASE + "/files/content";

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("name", fileName);
            attributes.put("parent", Map.of("id", bucket));

            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("--").append(boundary).append("\r\n");
            bodyBuilder.append("Content-Disposition: form-data; name=\"attributes\"\r\n\r\n");
            bodyBuilder.append(JsonKit.toJsonString(attributes)).append("\r\n");
            bodyBuilder.append("--").append(boundary).append("\r\n");
            bodyBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName)
                    .append("\"\r\n");
            bodyBuilder.append("Content-Type: application/octet-stream\r\n\r\n");

            byte[] header = bodyBuilder.toString().getBytes();
            byte[] footer = ("\r\n--" + boundary + "--\r\n").getBytes();
            byte[] body = new byte[header.length + content.length + footer.length];
            System.arraycopy(header, 0, body, 0, header.length);
            System.arraycopy(content, 0, body, header.length, content.length);
            System.arraycopy(footer, 0, body, header.length + content.length, footer.length);

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension())
                    .addHeader(HTTP.CONTENT_TYPE, "multipart/form-data; boundary=" + boundary)
                    .post(RequestBody.of(MediaType.valueOf("multipart/form-data"), body)).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Upload failed: " + response.code());
                }

                Map<String, Object> jsonMap = JsonKit.toMap(response.body().string());
                List<Map<String, Object>> entries = (List<Map<String, Object>>) jsonMap.get("entries");
                String fileId = entries != null && !entries.isEmpty() ? (String) entries.get(0).get("id") : null;

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .data(Blob.builder().name(fileName).path(fileId).build()).build();
            }
        } catch (Exception e) {
            Logger.error("Failed to upload file: {} to bucket: {}, error: {}", fileName, bucket, e.getMessage(), e);
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
     * @param path     The target path for the file (not used in Box).
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
     * @param bucket   The folder ID in Box.
     * @param path     The target path for the file (not used in Box).
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
            Logger.error("Failed to upload file: {} to bucket: {}, error: {}", fileName, bucket, e.getMessage(), e);
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
     * @param bucket   The folder ID in Box.
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
     * @param bucket   The folder ID in Box.
     * @param path     The storage path where the file is located (not used in Box).
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String bucket, String path, String fileName) {
        try {
            String fileId = findFileByName(fileName, bucket);
            if (fileId == null) {
                return Message.builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            String url = API_BASE + "/files/" + fileId;

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension()).delete().build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Delete failed: " + response.code());
                }

                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            }
        } catch (Exception e) {
            Logger.error("Failed to remove file: {} from bucket: {}, error: {}", fileName, bucket, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    /**
     * Removes a file from the specified storage bucket based on its path.
     *
     * @param bucket The folder ID in Box.
     * @param path   The target path of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString(), Normal.EMPTY);
    }

    /**
     * Searches for a file by name in the specified folder.
     *
     * @param fileName The file name to search for.
     * @param folderId The folder ID to search in.
     * @return The file ID if found, null otherwise.
     * @throws IOException If the search fails.
     */
    private String findFileByName(String fileName, String folderId) throws IOException {
        String url = API_BASE + "/folders/" + folderId + "/items?fields=id,name,type";

        Request request = new Request.Builder().url(url)
                .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension()).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Search failed: " + response.code());
            }

            Map<String, Object> jsonMap = JsonKit.toMap(response.body().string());
            List<Map<String, Object>> entries = (List<Map<String, Object>>) jsonMap.get("entries");

            if (entries != null) {
                for (Map<String, Object> entry : entries) {
                    if ("file".equals(entry.get("type")) && fileName.equals(entry.get("name"))) {
                        return (String) entry.get("id");
                    }
                }
            }

            return null;
        }
    }

}
