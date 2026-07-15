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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
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
 * @since Java 21+
 */
public class DropboxProvider extends AbstractProvider {

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
     * Reads metadata for a file in the default Dropbox folder.
     *
     * @param fileName The file name or Dropbox path to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message stat(String fileName) {
        return stat(this.context.getBucket(), fileName);
    }

    /**
     * Reads metadata for a file in the specified Dropbox folder.
     *
     * @param bucket   The folder path in Dropbox.
     * @param fileName The file name or Dropbox path to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message stat(String bucket, String fileName) {
        return statKey(bucket, normalizeObjectPath(bucket, fileName));
    }

    /**
     * Reads metadata for an exact Dropbox object path.
     *
     * @param bucket    The folder path in Dropbox.
     * @param objectKey The exact Dropbox path or file name.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message statKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }
            String path = normalizeObjectPath(bucket, objectKey);
            Map<String, Object> metadata = getMetadata(path);
            if (metadata == null || !"file".equals(metadata.get(".tag"))) {
                return Message.builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(toBlob(bucket, path, metadata, null)).build();
        } catch (Exception e) {
            Errors error = StringKit.containsIgnoreCase(e.getMessage(), "409") ? ErrorCode._113010 : ErrorCode._113012;
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
     * Opens a stream for a file in the default Dropbox folder.
     *
     * @param fileName The file name or Dropbox path to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message stream(String fileName) {
        return stream(this.context.getBucket(), fileName);
    }

    /**
     * Opens a stream for a file in the specified Dropbox folder.
     *
     * @param bucket   The folder path in Dropbox.
     * @param fileName The file name or Dropbox path to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message stream(String bucket, String fileName) {
        return streamKey(bucket, normalizeObjectPath(bucket, fileName));
    }

    /**
     * Opens a stream for an exact Dropbox object path.
     *
     * @param bucket    The folder path in Dropbox.
     * @param objectKey The exact Dropbox path or file name.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message streamKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }
            String path = normalizeObjectPath(bucket, objectKey);
            Map<String, Object> metadata = getMetadata(path);
            if (metadata == null || !"file".equals(metadata.get(".tag"))) {
                return Message.builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }

            String url = CONTENT_BASE + "/files/download";
            Map<String, Object> args = new HashMap<>();
            args.put("path", path);

            Response response = post(
                    url,
                    new byte[0],
                    MediaType.APPLICATION_OCTET_STREAM,
                    header(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension()),
                    header("Dropbox-API-Arg", JsonKit.toJsonString(args)));
            if (!response.successful()) {
                Errors error = toError(response.code());
                response.close();
                return Message.builder().errcode(error.getKey()).errmsg(error.getValue()).build();
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(toBlob(bucket, path, metadata, stream(response))).build();
        } catch (Exception e) {
            Errors error = StringKit.containsIgnoreCase(e.getMessage(), "409") ? ErrorCode._113010 : ErrorCode._113012;
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

            try (Response response = post(
                    url,
                    new byte[0],
                    MediaType.APPLICATION_OCTET_STREAM,
                    header(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension()),
                    header("Dropbox-API-Arg", JsonKit.toJsonString(args)))) {
                if (!response.successful()) {
                    throw new IOException("Download failed: " + response.code());
                }

                byte[] content = response.bytes();
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

            try (Response response = post(
                    url,
                    new byte[0],
                    MediaType.APPLICATION_OCTET_STREAM,
                    header(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension()),
                    header("Dropbox-API-Arg", JsonKit.toJsonString(args)))) {
                if (!response.successful()) {
                    throw new IOException("Download failed: " + response.code());
                }

                try (InputStream inputStream = stream(response);
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
            String url = API_BASE + "/files/list_folder";
            String path = context.getBucket().equals("/") ? "" : context.getBucket();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("path", path);
            requestBody.put("recursive", false);
            requestBody.put("include_deleted", false);

            try (Response response = post(
                    url,
                    JsonKit.toJsonString(requestBody),
                    MediaType.APPLICATION_JSON,
                    header(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension()))) {
                if (!response.successful()) {
                    throw new IOException("List failed: " + response.code());
                }

                Map<String, Object> jsonMap = JsonKit.toMap(response.text());
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

            try (Response response = post(
                    url,
                    JsonKit.toJsonString(requestBody),
                    MediaType.APPLICATION_JSON,
                    header(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension()))) {
                if (!response.successful()) {
                    throw new IOException("Rename failed: " + response.code());
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

            try (Response response = post(
                    url,
                    content,
                    MediaType.APPLICATION_OCTET_STREAM,
                    header(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension()),
                    header("Dropbox-API-Arg", JsonKit.toJsonString(args)))) {
                if (!response.successful()) {
                    throw new IOException("Upload failed: " + response.code());
                }

                Map<String, Object> jsonMap = JsonKit.toMap(response.text());
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

            try (Response response = post(
                    url,
                    JsonKit.toJsonString(requestBody),
                    MediaType.APPLICATION_JSON,
                    header(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension()))) {
                if (!response.successful()) {
                    throw new IOException("Delete failed: " + response.code());
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

    /**
     * Normalizes a Dropbox object key to an absolute Dropbox path.
     *
     * @param bucket    The folder path in Dropbox.
     * @param objectKey The object key or file name.
     * @return The absolute Dropbox path.
     */
    private String normalizeObjectPath(String bucket, String objectKey) {
        return objectKey.startsWith(Symbol.SLASH) ? objectKey : buildPath(bucket, objectKey);
    }

    /**
     * Reads Dropbox metadata for a path.
     *
     * @param path The Dropbox path.
     * @return The metadata, or {@code null} if not found.
     * @throws IOException If metadata lookup fails.
     */
    private Map<String, Object> getMetadata(String path) throws IOException {
        String url = API_BASE + "/files/get_metadata";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("path", path);
        requestBody.put("include_media_info", false);
        requestBody.put("include_deleted", false);

        try (Response response = post(
                url,
                JsonKit.toJsonString(requestBody),
                MediaType.APPLICATION_JSON,
                header(HTTP.AUTHORIZATION, HTTP.BEARER + context.getExtension()))) {
            if (response.code() == 409) {
                return null;
            }
            if (!response.successful()) {
                throw new IOException("Metadata failed: " + response.code());
            }
            return JsonKit.toMap(response.text());
        }
    }

    /**
     * Converts Dropbox metadata to a storage blob.
     *
     * @param bucket      The folder path in Dropbox.
     * @param path        The Dropbox path.
     * @param metadata    The Dropbox metadata.
     * @param inputStream The optional file stream.
     * @return The storage blob.
     */
    private Blob toBlob(String bucket, String path, Map<String, Object> metadata, InputStream inputStream) {
        Map<String, Object> extend = new HashMap<>();
        extend.put("id", metadata.get("id"));
        extend.put("path_display", metadata.get("path_display"));
        extend.put("client_modified", metadata.get("client_modified"));
        extend.put("server_modified", metadata.get("server_modified"));
        extend.put("rev", metadata.get("rev"));

        return Blob.builder().inputStream(inputStream).bucket(bucket).key(path).name((String) metadata.get("name"))
                .path(path).size(metadata.get("size") == null ? "0" : String.valueOf(metadata.get("size")))
                .hash((String) metadata.get("content_hash")).extend(extend).build();
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
        if (code == 404 || code == 409) {
            return ErrorCode._113010;
        }
        return ErrorCode._113012;
    }

}
