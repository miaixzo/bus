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
import org.miaixz.bus.storage.magic.Blob;
import org.miaixz.bus.storage.magic.ErrorCode;

/**
 * Storage service provider for Dropbox Business. This provider integrates with Dropbox using Dropbox API v2 for
 * enterprise file storage and collaboration.
 * <p>
 * <strong>Supported Storage Locations:</strong>
 * <ul>
 * <li>Personal Folder: bucket = "" or "/" (default)</li>
 * <li>Team Folder: bucket = "/team_folder_name"</li>
 * <li>Specific Folder: bucket = "/path/to/folder"</li>
 * </ul>
 * <p>
 * Configuration:
 * <ul>
 * <li>endpoint: Dropbox API endpoint (default: https://api.dropboxapi.com/2)</li>
 * <li>bucket: Folder path (default: root "/")</li>
 * <li>accessKey: Dropbox App key (not used for OAuth)</li>
 * <li>secretKey: Dropbox App secret (not used for OAuth)</li>
 * <li>extension: OAuth 2.0 access token (required)</li>
 * </ul>
 * <p>
 * <strong>Usage Example:</strong>
 * 
 * <pre>{@code
 * Context context = Context.builder().bucket("/").extension("your-oauth-access-token").build();
 *
 * DropboxProvider provider = new DropboxProvider(context);
 * provider.upload("document.pdf", fileBytes);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DropboxProvider extends AbstractProvider {

    /**
     * The HTTP client instance used for making requests to the Dropbox API.
     */
    private final Httpd client;

    /**
     * Dropbox API base URL for metadata operations.
     */
    private static final String API_BASE = "https://api.dropboxapi.com/2";

    /**
     * Dropbox content API base URL for file upload/download operations.
     */
    private static final String CONTENT_BASE = "https://content.dropboxapi.com/2";

    /**
     * Constructs a Dropbox Business storage provider with the given context. Initializes the HTTP client and validates
     * the OAuth 2.0 access token.
     *
     * @param context The storage context, containing endpoint, bucket (folder path), and OAuth access token.
     * @throws IllegalArgumentException If required context parameters are missing or invalid.
     */
    public DropboxProvider(Context context) {
        this.context = context;
        Assert.notBlank(this.context.getExtension(), "[extension] (access token) cannot be blank");

        if (StringKit.isBlank(this.context.getEndpoint())) {
            this.context.setEndpoint(API_BASE);
        }
        if (StringKit.isBlank(this.context.getBucket())) {
            this.context.setBucket("/");
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
     * @param bucket   The folder path in Dropbox.
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation. If successful, the data field contains the file
     *         content as a byte array; otherwise, it contains error information.
     */
    @Override
    public Message download(String bucket, String fileName) {
        try {
            String path = buildPath(bucket, fileName);
            String url = CONTENT_BASE + "/files/download";

            Map<String, Object> args = new HashMap<>();
            args.put("path", path);

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension())
                    .addHeader("Dropbox-API-Arg", JsonKit.toJsonString(args))
                    .post(RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM), new byte[0])).build();

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
     * @param bucket   The folder path in Dropbox.
     * @param fileName The name of the file to download.
     * @param file     The target local file to save the downloaded content.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message download(String bucket, String fileName, File file) {
        try {
            String path = buildPath(bucket, fileName);
            String url = CONTENT_BASE + "/files/download";

            Map<String, Object> args = new HashMap<>();
            args.put("path", path);

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension())
                    .addHeader("Dropbox-API-Arg", JsonKit.toJsonString(args))
                    .post(RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM), new byte[0])).build();

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
            String url = API_BASE + "/files/list_folder";
            String path = context.getBucket().equals("/") ? "" : context.getBucket();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("path", path);
            requestBody.put("recursive", false);
            requestBody.put("include_deleted", false);

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension())
                    .addHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .post(
                            RequestBody.of(
                                    MediaType.valueOf(MediaType.APPLICATION_JSON),
                                    JsonKit.toJsonString(requestBody)))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("List failed: " + response.code());
                }

                Map<String, Object> jsonMap = JsonKit.toMap(response.body().string());
                List<Map<String, Object>> entries = (List<Map<String, Object>>) jsonMap.get("entries");
                List<Blob> blobs = new ArrayList<>();

                if (entries != null) {
                    for (Map<String, Object> entry : entries) {
                        if ("file".equals(entry.get(".tag"))) {
                            Map<String, Object> extend = new HashMap<>();
                            extend.put("id", entry.get("id"));
                            extend.put("path_display", entry.get("path_display"));
                            extend.put("client_modified", entry.get("client_modified"));
                            extend.put("server_modified", entry.get("server_modified"));

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
     * @param path    The path where the file is located (not used in Dropbox).
     * @param oldName The current name of the file.
     * @param newName The new name for the file.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message rename(String path, String oldName, String newName) {
        return rename(this.context.getBucket(), path, oldName, newName);
    }

    /**
     * Renames a file within the specified bucket using Dropbox move API.
     *
     * @param bucket  The folder path in Dropbox.
     * @param path    The path where the file is located (not used in Dropbox).
     * @param oldName The current name of the file.
     * @param newName The new name for the file.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message rename(String bucket, String path, String oldName, String newName) {
        try {
            String fromPath = buildPath(bucket, oldName);
            String toPath = buildPath(bucket, newName);
            String url = API_BASE + "/files/move_v2";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from_path", fromPath);
            requestBody.put("to_path", toPath);
            requestBody.put("autorename", false);

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension())
                    .addHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .post(
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
     * @param bucket   The folder path in Dropbox.
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String bucket, String fileName, byte[] content) {
        return upload(bucket, Normal.EMPTY, fileName, content);
    }

    /**
     * Uploads a byte array to the specified storage bucket using Dropbox upload API.
     *
     * @param bucket   The folder path in Dropbox.
     * @param path     The target path for the file (not used in Dropbox).
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String bucket, String path, String fileName, byte[] content) {
        try {
            String filePath = buildPath(bucket, fileName);
            String url = CONTENT_BASE + "/files/upload";

            Map<String, Object> args = new HashMap<>();
            args.put("path", filePath);
            args.put("mode", "add");
            args.put("autorename", true);
            args.put("mute", false);

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension())
                    .addHeader("Dropbox-API-Arg", JsonKit.toJsonString(args))
                    .addHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                    .post(RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM), content)).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Upload failed: " + response.code());
                }

                Map<String, Object> jsonMap = JsonKit.toMap(response.body().string());
                String fileId = (String) jsonMap.get("id");

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
     * @param path     The target path for the file (not used in Dropbox).
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
     * @param bucket   The folder path in Dropbox.
     * @param path     The target path for the file (not used in Dropbox).
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
     * @param bucket   The folder path in Dropbox.
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String bucket, String fileName) {
        return remove(bucket, Normal.EMPTY, fileName);
    }

    /**
     * Removes a file from the specified storage bucket using Dropbox delete API.
     *
     * @param bucket   The folder path in Dropbox.
     * @param path     The storage path where the file is located (not used in Dropbox).
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String bucket, String path, String fileName) {
        try {
            String filePath = buildPath(bucket, fileName);
            String url = API_BASE + "/files/delete_v2";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("path", filePath);

            Request request = new Request.Builder().url(url)
                    .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension())
                    .addHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .post(
                            RequestBody.of(
                                    MediaType.valueOf(MediaType.APPLICATION_JSON),
                                    JsonKit.toJsonString(requestBody)))
                    .build();

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
     * @param bucket The folder path in Dropbox.
     * @param path   The target path of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString(), Normal.EMPTY);
    }

    /**
     * Builds the full file path by combining bucket and file name.
     *
     * @param bucket   The folder path in Dropbox.
     * @param fileName The file name.
     * @return The complete file path in Dropbox format.
     */
    private String buildPath(String bucket, String fileName) {
        if (StringKit.isBlank(bucket) || Symbol.SLASH.equals(bucket)) {
            return Symbol.SLASH + fileName;
        }
        String normalizedBucket = bucket.startsWith(Symbol.SLASH) ? bucket : Symbol.SLASH + bucket;
        return normalizedBucket + Symbol.SLASH + fileName;
    }

}
