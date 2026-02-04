/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.storage.metric;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.IoKit;
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
 * Storage service provider for Mega encrypted cloud storage. Mega is a privacy-focused cloud storage service based in
 * New Zealand, offering end-to-end encryption for all stored data.
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>End-to-end encryption (E2EE) for all files</li>
 * <li>Zero-knowledge encryption - Mega cannot access your data</li>
 * <li>Generous free storage (20GB)</li>
 * <li>GDPR compliant with strong privacy protection</li>
 * <li>Global CDN for fast downloads</li>
 * <li>File versioning and recovery</li>
 * </ul>
 * <p>
 * <strong>Supported Storage Locations:</strong>
 * <ul>
 * <li>Root Folder: bucket = "/" (default)</li>
 * <li>Specific Folder: bucket = "/folder_name"</li>
 * </ul>
 * <p>
 * <strong>Configuration:</strong>
 * <ul>
 * <li>endpoint: Mega API endpoint (default: https://g.api.mega.co.nz)</li>
 * <li>bucket: Folder path (default: root "/")</li>
 * <li>accessKey: Mega account email (required)</li>
 * <li>secretKey: Mega account password (required)</li>
 * <li>extension: Session ID (optional, for reusing existing sessions)</li>
 * </ul>
 * <p>
 * <strong>Usage Example:</strong>
 *
 * <pre>{@code
 * Context context = Context.builder().endpoint("https://g.api.mega.co.nz").bucket("/")
 *         .accessKey("your-email@example.com").secretKey("your-password").build();
 *
 * MegaProvider provider = new MegaProvider(context);
 * provider.upload("document.pdf", fileBytes);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MegaProvider extends AbstractProvider {

    /**
     * Mega API base URL.
     */
    private static final String API_BASE = "https://g.api.mega.co.nz";

    /**
     * HTTP client for making API requests.
     */
    private final Httpd client;

    /**
     * Mega session identifier for authenticated requests.
     */
    private String sessionId;

    /**
     * Root folder handle for file operations.
     */
    private String rootHandle;

    /**
     * Sequence number for API requests.
     */
    private int sequenceNumber = 0;

    /**
     * Constructs a new Mega provider with the specified context.
     *
     * @param context The storage context containing connection configuration.
     */
    public MegaProvider(Context context) {
        this.context = context;
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] (email) cannot be blank");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] (password) cannot be blank");

        if (StringKit.isBlank(this.context.getEndpoint())) {
            this.context.setEndpoint(API_BASE);
        }
        if (StringKit.isBlank(this.context.getBucket())) {
            this.context.setBucket("/");
        }

        this.client = new Httpd();

        // Check if session ID is provided in extension
        if (StringKit.isNotBlank(this.context.getExtension())) {
            this.sessionId = this.context.getExtension();
            Logger.info("Using provided session ID for Mega authentication");
        } else {
            // Perform login to get session ID
            login();
        }
    }

    /**
     * Authenticates with Mega and obtains a session ID.
     */
    private void login() {
        try {
            // Note: Full Mega authentication requires complex cryptographic operations
            // including RSA key derivation, AES encryption, and challenge-response
            // This is a simplified placeholder that demonstrates the API structure
            Logger.warn(
                    "Mega authentication requires complex cryptographic operations. "
                            + "Please provide a valid session ID in the 'extension' field of the context.");

            // Placeholder for actual authentication
            // Real implementation would need:
            // 1. Derive password key using PBKDF2
            // 2. Prepare user credentials
            // 3. Send login request
            // 4. Process encrypted session data
            // 5. Store session ID and master key

            this.sessionId = null;
            this.rootHandle = null;
        } catch (Exception e) {
            Logger.error("Failed to authenticate with Mega. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Mega authentication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Makes an API request to Mega.
     *
     * @param command The API command to execute.
     * @return The response as a Map.
     * @throws IOException If the request fails.
     */
    private Map<String, Object> makeApiRequest(List<Map<String, Object>> command) throws IOException {
        String url = this.context.getEndpoint() + "/cs?id=" + (sequenceNumber++);
        if (StringKit.isNotBlank(sessionId)) {
            url += "&sid=" + sessionId;
        }

        String jsonBody = JsonKit.toJsonString(command);
        RequestBody body = RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_JSON), jsonBody);
        Request request = new Request.Builder().url(url).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API request failed: " + response.code());
            }

            String responseBody = response.body().string();
            // Mega returns an array, we take the first element
            List<Object> resultList = JsonKit.toPojo(responseBody, List.class);
            if (resultList != null && !resultList.isEmpty()) {
                Object firstResult = resultList.get(0);
                if (firstResult instanceof Map) {
                    return (Map<String, Object>) firstResult;
                } else if (firstResult instanceof Number) {
                    // Error code returned
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("error", firstResult);
                    return errorMap;
                }
            }
            return new HashMap<>();
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
     * Downloads a file from the specified storage bucket and returns its content as a byte array.
     *
     * @param bucket   The folder path in Mega.
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation. If successful, the data field contains the file
     *         content as a byte array; otherwise, it contains error information.
     */
    @Override
    public Message download(String bucket, String fileName) {
        try {
            if (StringKit.isBlank(sessionId)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey())
                        .errmsg("Not authenticated. Please provide a valid session ID.").build();
            }

            // Find file by name
            String fileHandle = findFileByName(fileName, bucket);
            if (fileHandle == null) {
                return Message.builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            // Get download URL
            List<Map<String, Object>> command = new ArrayList<>();
            Map<String, Object> getLink = new HashMap<>();
            getLink.put("a", "g");
            getLink.put("g", 1);
            getLink.put("n", fileHandle);
            command.add(getLink);

            Map<String, Object> result = makeApiRequest(command);
            if (result.containsKey("error")) {
                throw new IOException("Failed to get download URL: " + result.get("error"));
            }

            String downloadUrl = (String) result.get("g");
            if (StringKit.isBlank(downloadUrl)) {
                throw new IOException("Download URL not found in response");
            }

            // Download file content
            Request request = new Request.Builder().url(downloadUrl).get().build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Download failed: " + response.code());
                }

                byte[] content = response.body().bytes();
                // Note: Content is encrypted and would need to be decrypted with file key
                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .data(content).build();
            }
        } catch (Exception e) {
            Logger.error("Failed to download file: {} from bucket: {}. Error: {}", fileName, bucket, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(e.getMessage()).build();
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
     *
     * @param bucket   The folder path in Mega.
     * @param fileName The name of the file to download.
     * @param file     The target local file to save the downloaded content.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message download(String bucket, String fileName, File file) {
        Message result = download(bucket, fileName);
        if (ErrorCode._SUCCESS.getKey().equals(result.getErrcode())) {
            try {
                byte[] content = (byte[]) result.getData();
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(content);
                }
                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            } catch (IOException e) {
                Logger.error("Failed to write file: {}. Error: {}", file.getPath(), e.getMessage(), e);
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(e.getMessage()).build();
            }
        }
        return result;
    }

    /**
     * Lists files in the default storage bucket.
     *
     * @return A {@link Message} containing the result of the operation, including a list of {@link Blob} objects if
     *         successful.
     */
    @Override
    public Message list() {
        return list(this.context.getBucket());
    }

    /**
     * Lists files in the specified storage bucket.
     *
     * @param bucket The folder path in Mega.
     * @return A {@link Message} containing the result of the operation, including a list of {@link Blob} objects if
     *         successful.
     */
    @Override
    public Message list(String bucket) {
        try {
            if (StringKit.isBlank(sessionId)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey())
                        .errmsg("Not authenticated. Please provide a valid session ID.").build();
            }

            // Get file list
            List<Map<String, Object>> command = new ArrayList<>();
            Map<String, Object> getFiles = new HashMap<>();
            getFiles.put("a", "f");
            getFiles.put("c", 1);
            command.add(getFiles);

            Map<String, Object> result = makeApiRequest(command);
            if (result.containsKey("error")) {
                throw new IOException("Failed to list files: " + result.get("error"));
            }

            List<Map<String, Object>> files = (List<Map<String, Object>>) result.get("f");
            List<Blob> blobs = new ArrayList<>();

            if (files != null) {
                for (Map<String, Object> file : files) {
                    // Type 0 = file, 1 = folder
                    Object typeObj = file.get("t");
                    if (typeObj != null && typeObj.equals(0)) {
                        String name = (String) file.get("a");
                        Object sizeObj = file.get("s");
                        String size = sizeObj != null ? sizeObj.toString() : "0";

                        Map<String, Object> extend = new HashMap<>();
                        extend.put("h", file.get("h")); // File handle
                        extend.put("ts", file.get("ts")); // Timestamp

                        blobs.add(Blob.builder().name(name).size(size).extend(extend).build());
                    }
                }
            }

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(blobs).build();
        } catch (Exception e) {
            Logger.error("Failed to list files in bucket: {}. Error: {}", bucket, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(e.getMessage()).build();
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
     * @param path    The path where the file is located (not used in Mega).
     * @param oldName The current name of the file.
     * @param newName The new name for the file.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message rename(String path, String oldName, String newName) {
        return rename(this.context.getBucket(), path, oldName, newName);
    }

    /**
     * Renames a file within the specified bucket using Mega's file attribute update API.
     *
     * @param bucket  The folder path in Mega.
     * @param path    The path where the file is located (not used in Mega).
     * @param oldName The current name of the file.
     * @param newName The new name for the file.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message rename(String bucket, String path, String oldName, String newName) {
        try {
            if (StringKit.isBlank(sessionId)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey())
                        .errmsg("Not authenticated. Please provide a valid session ID.").build();
            }

            String fileHandle = findFileByName(oldName, bucket);
            if (fileHandle == null) {
                return Message.builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            // Rename file
            List<Map<String, Object>> command = new ArrayList<>();
            Map<String, Object> renameCmd = new HashMap<>();
            renameCmd.put("a", "a");
            renameCmd.put("n", fileHandle);
            renameCmd.put("attr", Map.of("n", newName));
            command.add(renameCmd);

            Map<String, Object> result = makeApiRequest(command);
            if (result.containsKey("error")) {
                throw new IOException("Failed to rename file: " + result.get("error"));
            }

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (Exception e) {
            Logger.error("Failed to rename file: {} to {}. Error: {}", oldName, newName, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(e.getMessage()).build();
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
     * @param path     The target path for the file (not used in Mega).
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String path, String fileName, byte[] content) {
        return upload(this.context.getBucket(), path, fileName, content);
    }

    /**
     * Uploads a byte array to the specified storage bucket using Mega's upload API. The upload process involves three
     * steps: 1) Request an upload URL, 2) Upload the encrypted file content, 3) Create a file node to complete the
     * upload.
     *
     * @param bucket   The folder path in Mega.
     * @param path     The target path for the file (not used in Mega).
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String bucket, String path, String fileName, byte[] content) {
        try {
            if (StringKit.isBlank(sessionId)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey())
                        .errmsg("Not authenticated. Please provide a valid session ID.").build();
            }

            // Get upload URL
            List<Map<String, Object>> command = new ArrayList<>();
            Map<String, Object> getUploadUrl = new HashMap<>();
            getUploadUrl.put("a", "u");
            getUploadUrl.put("s", content.length);
            command.add(getUploadUrl);

            Map<String, Object> result = makeApiRequest(command);
            if (result.containsKey("error")) {
                throw new IOException("Failed to get upload URL: " + result.get("error"));
            }

            String uploadUrl = (String) result.get("p");
            if (StringKit.isBlank(uploadUrl)) {
                throw new IOException("Upload URL not found in response");
            }

            // Upload file content (encrypted)
            // Note: Content should be encrypted before upload in real implementation
            Request request = new Request.Builder().url(uploadUrl)
                    .post(RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM), content)).build();

            String completionHandle;
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Upload failed: " + response.code());
                }
                completionHandle = response.body().string();
            }

            // Complete upload by creating file node
            List<Map<String, Object>> completeCommand = new ArrayList<>();
            Map<String, Object> createNode = new HashMap<>();
            createNode.put("a", "p");
            createNode.put("t", rootHandle != null ? rootHandle : "");
            createNode.put(
                    "n",
                    Arrays.asList(Map.of("h", completionHandle, "t", 0, "a", Map.of("n", fileName), "k", "")));
            completeCommand.add(createNode);

            Map<String, Object> completeResult = makeApiRequest(completeCommand);
            if (completeResult.containsKey("error")) {
                throw new IOException("Failed to complete upload: " + completeResult.get("error"));
            }

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Blob.builder().name(fileName).build()).build();
        } catch (Exception e) {
            Logger.error("Failed to upload file: {} to bucket: {}. Error: {}", fileName, bucket, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(e.getMessage()).build();
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
     * @param path     The target path for the file (not used in Mega).
     * @param fileName The name of the file to upload.
     * @param content  The file content as an {@link InputStream}.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String path, String fileName, InputStream content) {
        return upload(this.context.getBucket(), path, fileName, content);
    }

    /**
     * Uploads an input stream to the specified storage bucket. The stream is first read into a byte array before
     * uploading.
     *
     * @param bucket   The folder path in Mega.
     * @param path     The target path for the file (not used in Mega).
     * @param fileName The name of the file to upload.
     * @param content  The file content as an {@link InputStream}.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String bucket, String path, String fileName, InputStream content) {
        try {
            byte[] bytes = IoKit.readBytes(content);
            return upload(bucket, path, fileName, bytes);
        } catch (Exception e) {
            Logger.error("Failed to read input stream. Error: {}", e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(e.getMessage()).build();
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
     * Removes a file from the specified path in the default storage bucket.
     *
     * @param path     The storage path where the file is located (not used in Mega).
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String path, String fileName) {
        return remove(this.context.getBucket(), path, fileName);
    }

    /**
     * Removes a file from the specified storage bucket using Mega's delete API.
     *
     * @param bucket   The folder path in Mega.
     * @param path     The storage path where the file is located (not used in Mega).
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String bucket, String path, String fileName) {
        try {
            if (StringKit.isBlank(sessionId)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey())
                        .errmsg("Not authenticated. Please provide a valid session ID.").build();
            }

            String fileHandle = findFileByName(fileName, bucket);
            if (fileHandle == null) {
                return Message.builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            // Delete file
            List<Map<String, Object>> command = new ArrayList<>();
            Map<String, Object> deleteCmd = new HashMap<>();
            deleteCmd.put("a", "d");
            deleteCmd.put("n", fileHandle);
            command.add(deleteCmd);

            Map<String, Object> result = makeApiRequest(command);
            if (result.containsKey("error")) {
                throw new IOException("Failed to delete file: " + result.get("error"));
            }

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (Exception e) {
            Logger.error("Failed to remove file: {}. Error: {}", fileName, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(e.getMessage()).build();
        }
    }

    /**
     * Removes a file from the specified storage bucket based on its path. This operation is not supported for Mega.
     *
     * @param bucket The folder path in Mega.
     * @param path   The target path of the file to remove.
     * @return A {@link Message} containing an error indicating this operation is not supported.
     */
    @Override
    public Message remove(String bucket, Path path) {
        return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("Remove by Path not supported for Mega")
                .build();
    }

    /**
     * Finds a file by name in the specified bucket.
     *
     * @param fileName The file name to search for.
     * @param bucket   The bucket (folder) to search in.
     * @return The file handle if found, null otherwise.
     * @throws IOException If the search fails.
     */
    private String findFileByName(String fileName, String bucket) throws IOException {
        List<Map<String, Object>> command = new ArrayList<>();
        Map<String, Object> getFiles = new HashMap<>();
        getFiles.put("a", "f");
        getFiles.put("c", 1);
        command.add(getFiles);

        Map<String, Object> result = makeApiRequest(command);
        if (result.containsKey("error")) {
            throw new IOException("Failed to list files: " + result.get("error"));
        }

        List<Map<String, Object>> files = (List<Map<String, Object>>) result.get("f");
        if (files != null) {
            for (Map<String, Object> file : files) {
                Object typeObj = file.get("t");
                if (typeObj != null && typeObj.equals(0)) {
                    String name = (String) file.get("a");
                    if (fileName.equals(name)) {
                        return (String) file.get("h");
                    }
                }
            }
        }

        return null;
    }

}
