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
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
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
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.Blob;
import org.miaixz.bus.storage.magic.ErrorCode;

/**
 * Storage service provider for Microsoft SharePoint and OneDrive for Business. This provider integrates with SharePoint
 * Online document libraries and OneDrive for Business using Microsoft Graph API for enterprise file storage operations.
 * <p>
 * <strong>Supported Storage Locations:</strong>
 * <ul>
 * <li>SharePoint Site (Default Document Library): bucket = "site:{site-id}"</li>
 * <li>SharePoint Document Library (Specific): bucket = "site:{site-id}:drive:{drive-id}"</li>
 * <li>OneDrive for Business (User): bucket = "user:{user-id}" or "root"</li>
 * <li>OneDrive for Business (Current User): bucket = "me" or "root"</li>
 * </ul>
 * <p>
 * <strong>Configuration:</strong>
 * <ul>
 * <li>endpoint: Microsoft Graph API endpoint (default: https://graph.microsoft.com/v1.0)</li>
 * <li>bucket: Storage location identifier (see above for formats)</li>
 * <li>accessKey: Azure AD application client ID</li>
 * <li>secretKey: Azure AD application client secret</li>
 * <li>region: Azure AD tenant ID</li>
 * <li>prefix: Optional path prefix for all operations</li>
 * </ul>
 * <p>
 * <strong>Usage Examples:</strong>
 * 
 * <pre>{@code
 * 
 * // SharePoint Site (Primary Use Case)
 * Context context = Context.builder().bucket("site:contoso.sharepoint.com,abc123,def456").accessKey("client-id")
 *         .secretKey("client-secret").region("tenant-id").build();
 *
 * // SharePoint Specific Document Library
 * Context context = Context.builder().bucket("site:contoso.sharepoint.com,abc123,def456:drive:b!xyz789")
 *         .accessKey("client-id").secretKey("client-secret").region("tenant-id").build();
 *
 * // OneDrive for Business (Enterprise User Storage)
 * Context context = Context.builder().bucket("user:john@contoso.com").accessKey("client-id").secretKey("client-secret")
 *         .region("tenant-id").build();
 * }</pre>
 * <p>
 * <strong>Note:</strong> This provider is designed for enterprise scenarios. For personal OneDrive consumer accounts,
 * use appropriate consumer-facing authentication flows.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SharePointProvider extends AbstractProvider {

    /**
     * The HTTP client instance used for making requests to the Microsoft Graph API.
     */
    private final Httpd client;

    /**
     * OAuth 2.0 access token for API authentication.
     */
    private String accessToken;

    /**
     * Token expiration time in milliseconds.
     */
    private long tokenExpireTime;

    /**
     * Microsoft Graph API base URL.
     */
    private static final String GRAPH_API_BASE = "https://graph.microsoft.com/v1.0";

    /**
     * OAuth 2.0 token endpoint template.
     */
    private static final String TOKEN_ENDPOINT = "https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token";

    /**
     * Constructs a SharePoint storage provider with the given context. Initializes the HTTP client and obtains an
     * access token using OAuth 2.0 client credentials flow for enterprise authentication.
     *
     * @param context The storage context, containing endpoint, bucket, access key (client ID), secret key (client
     *                secret), region (tenant ID), and other configurations.
     * @throws IllegalArgumentException If required context parameters are missing or invalid.
     */
    public SharePointProvider(Context context) {
        this.context = context;

        Assert.notBlank(this.context.getAccessKey(), "[accessKey] (client ID) cannot be blank");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] (client secret) cannot be blank");
        Assert.notBlank(this.context.getRegion(), "[region] (tenant ID) cannot be blank");

        if (StringKit.isBlank(this.context.getEndpoint())) {
            this.context.setEndpoint(GRAPH_API_BASE);
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
     * Downloads a file from the specified storage bucket and returns its content as a byte array.
     * <p>
     * <strong>Note:</strong> This method loads the entire file into memory. For large files, consider using
     * {@link #download(String, String, File)} to stream directly to disk.
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
            String apiPath = buildApiPath(bucket, objectKey);
            String url = context.getEndpoint() + apiPath + ":/content";

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
     * <p>
     * <strong>Recommended for large files:</strong> This method uses streaming to transfer file content, making it
     * memory-efficient and suitable for files of any size. The file is written directly to disk without loading the
     * entire content into memory.
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
            String apiPath = buildApiPath(bucket, objectKey);
            String url = context.getEndpoint() + apiPath + ":/content";

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
            String apiPath = buildApiPath(context.getBucket(), prefix);
            String url = context.getEndpoint() + apiPath + ":/children";

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
                List<Map<String, Object>> items = (List<Map<String, Object>>) jsonMap.get("value");
                List<Blob> files = new ArrayList<>();

                if (items != null) {
                    for (Map<String, Object> item : items) {
                        if (!item.containsKey("folder")) {
                            Map<String, Object> extend = new HashMap<>();
                            extend.put("id", item.get("id"));
                            if (item.containsKey("lastModifiedDateTime")) {
                                extend.put("lastModified", item.get("lastModifiedDateTime"));
                            }
                            if (item.containsKey("file")) {
                                Map<String, Object> fileNode = (Map<String, Object>) item.get("file");
                                if (fileNode.containsKey("mimeType")) {
                                    extend.put("mimeType", fileNode.get("mimeType"));
                                }
                            }

                            files.add(
                                    Blob.builder().name((String) item.get("name"))
                                            .size(item.containsKey("size") ? String.valueOf(item.get("size")) : "0")
                                            .extend(extend).build());
                        }
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
            String apiPath = buildApiPath(bucket, oldObjectKey);
            String url = context.getEndpoint() + apiPath;

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
     * Uploads a byte array to the specified storage bucket and path using chunked upload session.
     * <p>
     * <strong>Note:</strong> This method supports files of any size. All uploads use the chunked upload strategy for
     * consistency and reliability. Microsoft Graph API handles the optimization internally.
     * </p>
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
            return chunkedUpload(bucket, objectKey, fileName, content);
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
            String apiPath = buildApiPath(bucket, objectKey);
            String url = context.getEndpoint() + apiPath;

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
     * Refreshes the OAuth 2.0 access token using client credentials flow.
     *
     * @throws IOException If token refresh fails.
     */
    private void refreshAccessToken() throws IOException {
        String tokenUrl = TOKEN_ENDPOINT.replace("{tenant}", context.getRegion());

        String requestBody = String.format(
                "client_id=%s&client_secret=%s&scope=%s&grant_type=client_credentials",
                URLEncoder.encode(context.getAccessKey(), Charset.UTF_8),
                URLEncoder.encode(context.getSecretKey(), Charset.UTF_8),
                URLEncoder.encode("https://graph.microsoft.com/.default", Charset.UTF_8));

        Request request = new Request.Builder().url(tokenUrl)
                .addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .post(RequestBody.of(MediaType.valueOf("application/x-www-form-urlencoded"), requestBody)).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to obtain access token: " + response.code());
            }

            Map<String, Object> jsonMap = JsonKit.toMap(response.body().string());
            this.accessToken = (String) jsonMap.get("access_token");
            int expiresIn = ((Number) jsonMap.get("expires_in")).intValue();
            this.tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;
        }
    }

    /**
     * Builds the Microsoft Graph API path for a file operation. Supports SharePoint sites, document libraries, and
     * OneDrive for Business.
     * <p>
     * <strong>Supported bucket formats (in priority order):</strong>
     * <ul>
     * <li><strong>SharePoint Site (Primary):</strong> "site:{site-id}" → /sites/{site-id}/drive/root:/path</li>
     * <li><strong>SharePoint Document Library:</strong> "site:{site-id}:drive:{drive-id}" →
     * /sites/{site-id}/drives/{drive-id}/root:/path</li>
     * <li><strong>OneDrive for Business User:</strong> "user:{user-id}" → /users/{user-id}/drive/root:/path</li>
     * <li><strong>OneDrive for Business Current:</strong> "root" or "me" → /me/drive/root:/path</li>
     * <li><strong>Folder/Item ID:</strong> Other values → /me/drive/items/{bucket}:/path</li>
     * </ul>
     *
     * @param bucket   The bucket (storage location) identifier.
     * @param filePath The file path relative to the bucket.
     * @return The complete Microsoft Graph API path.
     */
    private String buildApiPath(String bucket, String filePath) {
        // SharePoint Site with specific document library: site:{site-id}:drive:{drive-id}
        if (bucket.startsWith("site:") && bucket.contains(":drive:")) {
            String[] parts = bucket.split(":drive:");
            String siteId = parts[0].substring(5); // Remove "site:" prefix
            String driveId = parts[1];
            return "/sites/" + siteId + "/drives/" + driveId + "/root:/" + filePath;
        }

        // SharePoint Site default document library: site:{site-id}
        if (bucket.startsWith("site:")) {
            String siteId = bucket.substring(5);
            return "/sites/" + siteId + "/drive/root:/" + filePath;
        }

        // OneDrive for Business - specific user: user:{user-id}
        if (bucket.startsWith("user:")) {
            String userId = bucket.substring(5);
            return "/users/" + userId + "/drive/root:/" + filePath;
        }

        // OneDrive for Business - current user or default
        if ("root".equals(bucket) || "me".equals(bucket)) {
            return "/me/drive/root:/" + filePath;
        }

        // Folder ID or item ID (fallback)
        return "/me/drive/items/" + bucket + ":/" + filePath;
    }

    /**
     * Performs a chunked upload using Microsoft Graph API upload session. This method creates an upload session and
     * uploads the file in chunks, supporting files of any size with resumable upload capability.
     * <p>
     * The upload process:
     * <ol>
     * <li>Create an upload session with Microsoft Graph API</li>
     * <li>Split the file into chunks (configurable, default 10MB)</li>
     * <li>Upload each chunk sequentially with proper Content-Range headers</li>
     * <li>Microsoft Graph API automatically assembles the chunks</li>
     * </ol>
     * </p>
     *
     * @param bucket    The bucket name.
     * @param objectKey The object key.
     * @param fileName  The file name.
     * @param content   The file content (any size supported).
     * @return A {@link Message} containing the result of the operation.
     * @throws IOException If upload fails.
     */
    private Message chunkedUpload(String bucket, String objectKey, String fileName, byte[] content) throws IOException {
        String apiPath = buildApiPath(bucket, objectKey);
        String createSessionUrl = context.getEndpoint() + apiPath + ":/createUploadSession";

        String sessionBody = "{\"item\":{\"@microsoft.graph.conflictBehavior\":\"replace\"}}";
        Request createSessionRequest = new Request.Builder().url(createSessionUrl)
                .addHeader(HTTP.AUTHORIZATION, HTTP.BEARER + getAccessToken())
                .addHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_JSON), sessionBody)).build();

        String uploadUrl;
        try (Response response = client.newCall(createSessionRequest).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 401) {
                    refreshAccessToken();
                    return chunkedUpload(bucket, objectKey, fileName, content);
                }
                throw new IOException("Failed to create upload session: " + response.code());
            }

            Map<String, Object> jsonMap = JsonKit.toMap(response.body().string());
            uploadUrl = (String) jsonMap.get("uploadUrl");
        }

        // Upload in chunks - Microsoft Graph API recommends multiples of 320 KiB
        // Using 10MB chunks for optimal performance
        int chunkSize = 10 * 1024 * 1024; // 10MB
        int totalSize = content.length;
        int offset = 0;

        while (offset < totalSize) {
            int currentChunkSize = Math.min(chunkSize, totalSize - offset);
            byte[] chunk = new byte[currentChunkSize];
            System.arraycopy(content, offset, chunk, 0, currentChunkSize);

            String contentRange = String.format("bytes %d-%d/%d", offset, offset + currentChunkSize - 1, totalSize);

            Request uploadRequest = new Request.Builder().url(uploadUrl)
                    .addHeader(HTTP.CONTENT_LENGTH, String.valueOf(currentChunkSize))
                    .addHeader(HTTP.CONTENT_RANGE, contentRange)
                    .put(RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM), chunk)).build();

            try (Response response = client.newCall(uploadRequest).execute()) {
                if (!response.isSuccessful() && response.code() != 202) {
                    throw new IOException("Chunk upload failed: " + response.code());
                }
            }

            offset += currentChunkSize;
        }

        return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                .data(Blob.builder().name(fileName).path(objectKey).build()).build();
    }

}
