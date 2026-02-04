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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.net.HTTP;
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
 * Storage service provider for Apple iCloud Drive. This provider integrates with iCloud Drive using Apple's CloudKit
 * Web Services API for personal cloud storage operations.
 * <p>
 * <strong>Key Features:</strong>
 * <ul>
 * <li>Seamless integration with Apple ecosystem (iOS, macOS, iPadOS)</li>
 * <li>Automatic sync across all Apple devices</li>
 * <li>End-to-end encryption for data security</li>
 * <li>5GB free storage (upgradable to 50GB, 200GB, 2TB, 6TB, 12TB)</li>
 * <li>File versioning and recovery</li>
 * <li>Shared folders and collaboration</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 *
 * <pre>{@code
 * 
 * Context context = Context.builder().endpoint("https://api.apple-cloudkit.com").bucket("/Documents")
 *         .accessKey("your-cloudkit-api-token").secretKey("iCloud.com.example.app").region("production").build();
 *
 * ICloudDriveProvider provider = new ICloudDriveProvider(context);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ICloudDriveProvider extends AbstractProvider {

    /**
     * HTTP client for making API requests.
     */
    private final Httpd client;
    /**
     * CloudKit API authentication token.
     */
    private final String apiToken;
    /**
     * CloudKit container identifier.
     */
    private final String containerIdentifier;
    /**
     * CloudKit environment (production or development).
     */
    private final String environment;

    private static final String API_BASE = "https://api.apple-cloudkit.com";
    private static final String DATABASE_TYPE = "private";
    private static final String RECORD_TYPE = "DriveFile";

    /**
     * Constructs a new iCloud Drive provider with the specified context.
     *
     * @param context The storage context containing connection configuration.
     */
    public ICloudDriveProvider(Context context) {
        this.context = context;
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] (CloudKit API token) cannot be blank");
        Assert.notBlank(this.context.getSecretKey(), "[secretKey] (container identifier) cannot be blank");

        if (StringKit.isBlank(this.context.getEndpoint())) {
            this.context.setEndpoint(API_BASE);
        }
        if (StringKit.isBlank(this.context.getBucket())) {
            this.context.setBucket("/");
        }

        this.apiToken = this.context.getAccessKey();
        this.containerIdentifier = this.context.getSecretKey();
        this.environment = StringKit.isBlank(this.context.getRegion()) ? "production" : this.context.getRegion();
        this.client = new Httpd();

        Logger.info(
                "iCloud Drive provider initialized with container: {}, environment: {}",
                containerIdentifier,
                environment);
    }

    @Override
    public Message download(String fileName) {
        return download(this.context.getBucket(), fileName);
    }

    @Override
    public Message download(String bucket, String fileName) {
        try {
            String path = buildPath(bucket, fileName);
            Map<String, Object> record = queryFileRecord(path);
            if (record == null) {
                return Message.builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            String downloadUrl = getAssetDownloadUrl(record);
            if (StringKit.isBlank(downloadUrl)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("Failed to get download URL")
                        .build();
            }

            Request request = new Request.Builder().url(downloadUrl).get().build();
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

    @Override
    public Message download(String fileName, File file) {
        return download(this.context.getBucket(), fileName, file);
    }

    @Override
    public Message download(String bucket, String fileName, File file) {
        Message result = download(bucket, fileName);
        if (ErrorCode._SUCCESS.getKey().equals(result.getErrcode())) {
            try {
                byte[] content = (byte[]) result.getData();
                Files.write(file.toPath(), content);
                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            } catch (IOException e) {
                Logger.error("Failed to write file: {}. Error: {}", file.getPath(), e.getMessage(), e);
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue())
                        .build();
            }
        }
        return result;
    }

    @Override
    public Message list() {
        try {
            List<Map<String, Object>> records = queryFolderRecords(this.context.getBucket());
            List<Blob> blobs = new ArrayList<>();
            for (Map<String, Object> record : records) {
                Blob blob = recordToBlob(record);
                if (blob != null) {
                    blobs.add(blob);
                }
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(blobs).build();
        } catch (Exception e) {
            Logger.error("Failed to list files. Error: {}", e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    @Override
    public Message rename(String oldName, String newName) {
        return rename(this.context.getBucket(), Normal.EMPTY, oldName, newName);
    }

    @Override
    public Message rename(String path, String oldName, String newName) {
        return rename(this.context.getBucket(), path, oldName, newName);
    }

    @Override
    public Message rename(String bucket, String path, String oldName, String newName) {
        try {
            String oldPath = buildPath(bucket, oldName);
            String newPath = buildPath(bucket, newName);

            Map<String, Object> record = queryFileRecord(oldPath);
            if (record == null) {
                return Message.builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            boolean success = updateFileRecord(record, newPath);
            if (success) {
                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            } else {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("Failed to rename file").build();
            }
        } catch (Exception e) {
            Logger.error("Failed to rename file: {} to {}. Error: {}", oldName, newName, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    @Override
    public Message upload(String fileName, byte[] content) {
        return upload(this.context.getBucket(), Normal.EMPTY, fileName, content);
    }

    @Override
    public Message upload(String path, String fileName, byte[] content) {
        return upload(this.context.getBucket(), path, fileName, content);
    }

    @Override
    public Message upload(String bucket, String path, String fileName, byte[] content) {
        try {
            String filePath = buildPath(bucket, fileName);

            String uploadUrl = requestAssetUpload(content.length);
            if (StringKit.isBlank(uploadUrl)) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("Failed to get upload URL")
                        .build();
            }

            boolean uploaded = uploadAsset(uploadUrl, content);
            if (!uploaded) {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("Failed to upload asset").build();
            }

            boolean created = createFileRecord(filePath, uploadUrl, content.length);
            if (created) {
                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            } else {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("Failed to create file record")
                        .build();
            }
        } catch (Exception e) {
            Logger.error("Failed to upload file: {} to bucket: {}. Error: {}", fileName, bucket, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    @Override
    public Message upload(String fileName, InputStream content) {
        return upload(this.context.getBucket(), Normal.EMPTY, fileName, content);
    }

    @Override
    public Message upload(String path, String fileName, InputStream content) {
        return upload(this.context.getBucket(), path, fileName, content);
    }

    @Override
    public Message upload(String bucket, String path, String fileName, InputStream content) {
        try {
            byte[] bytes = IoKit.readBytes(content);
            return upload(bucket, path, fileName, bytes);
        } catch (Exception e) {
            Logger.error("Failed to read input stream. Error: {}", e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    @Override
    public Message remove(String fileName) {
        return remove(this.context.getBucket(), Normal.EMPTY, fileName);
    }

    @Override
    public Message remove(String path, String fileName) {
        return remove(this.context.getBucket(), path, fileName);
    }

    @Override
    public Message remove(String bucket, String path, String fileName) {
        try {
            String filePath = buildPath(bucket, fileName);
            Map<String, Object> record = queryFileRecord(filePath);
            if (record == null) {
                return Message.builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            boolean deleted = deleteFileRecord(record);
            if (deleted) {
                return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                        .build();
            } else {
                return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("Failed to delete file").build();
            }
        } catch (Exception e) {
            Logger.error("Failed to remove file: {}. Error: {}", fileName, e.getMessage(), e);
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    @Override
    public Message remove(String bucket, Path path) {
        return Message.builder().errcode(ErrorCode._FAILURE.getKey())
                .errmsg("Remove by Path not supported for iCloud Drive").build();
    }

    private Map<String, Object> queryFileRecord(String path) {
        try {
            String url = buildApiUrl("/records/query");
            Map<String, Object> query = new HashMap<>();
            query.put("recordType", RECORD_TYPE);
            query.put(
                    "filterBy",
                    Arrays.asList(
                            Map.of(
                                    "fieldName",
                                    "path",
                                    "comparator",
                                    "EQUALS",
                                    "fieldValue",
                                    Map.of("value", path, "type", "STRING"))));

            String jsonBody = JsonKit.toJsonString(query);
            RequestBody body = RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_JSON), jsonBody);
            Request request = new Request.Builder().url(url).post(body)
                    .addHeader(HTTP.AUTHORIZATION, "Bearer " + apiToken).build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Map<String, Object> result = JsonKit.toPojo(responseBody, Map.class);
                    List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
                    return (records != null && !records.isEmpty()) ? records.get(0) : null;
                }
            }
        } catch (Exception e) {
            Logger.error("Failed to query file record. Error: {}", e.getMessage(), e);
        }
        return null;
    }

    private List<Map<String, Object>> queryFolderRecords(String folder) {
        try {
            String url = buildApiUrl("/records/query");
            Map<String, Object> query = new HashMap<>();
            query.put("recordType", RECORD_TYPE);

            List<Map<String, Object>> filters = new ArrayList<>();
            Map<String, Object> filter = new HashMap<>();
            filter.put("fieldName", "path");
            filter.put("comparator", "BEGINS_WITH");
            Map<String, Object> fieldValue = new HashMap<>();
            fieldValue.put("value", folder.endsWith("/") ? folder : folder + "/");
            fieldValue.put("type", "STRING");
            filter.put("fieldValue", fieldValue);
            filters.add(filter);
            query.put("filterBy", filters);

            String jsonBody = JsonKit.toJsonString(query);
            RequestBody body = RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_JSON), jsonBody);
            Request request = new Request.Builder().url(url).post(body)
                    .addHeader(HTTP.AUTHORIZATION, "Bearer " + apiToken).build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Map<String, Object> result = JsonKit.toPojo(responseBody, Map.class);
                    List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
                    return records != null ? records : new ArrayList<>();
                }
            }
        } catch (Exception e) {
            Logger.error("Failed to query folder records. Error: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    private String getAssetDownloadUrl(Map<String, Object> record) {
        try {
            Map<String, Object> fields = (Map<String, Object>) record.get("fields");
            Map<String, Object> asset = (Map<String, Object>) fields.get("asset");
            Map<String, Object> value = (Map<String, Object>) asset.get("value");
            return (String) value.get("downloadURL");
        } catch (Exception e) {
            Logger.error("Failed to extract download URL. Error: {}", e.getMessage(), e);
            return null;
        }
    }

    private String requestAssetUpload(long fileSize) {
        try {
            String url = buildApiUrl("/assets/upload");
            Map<String, Object> request = new HashMap<>();
            List<Map<String, Object>> tokens = new ArrayList<>();
            Map<String, Object> token = new HashMap<>();
            token.put("recordName", UUID.randomUUID().toString());
            token.put("recordType", RECORD_TYPE);
            token.put("fieldName", "asset");
            tokens.add(token);
            request.put("tokens", tokens);

            String jsonBody = JsonKit.toJsonString(request);
            RequestBody body = RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_JSON), jsonBody);
            Request httpRequest = new Request.Builder().url(url).post(body)
                    .addHeader(HTTP.AUTHORIZATION, "Bearer " + apiToken).build();

            try (Response response = client.newCall(httpRequest).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Map<String, Object> result = JsonKit.toPojo(responseBody, Map.class);
                    List<Map<String, Object>> tokensList = (List<Map<String, Object>>) result.get("tokens");
                    if (tokensList != null && !tokensList.isEmpty()) {
                        return (String) tokensList.get(0).get("url");
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("Failed to request asset upload. Error: {}", e.getMessage(), e);
        }
        return null;
    }

    private boolean uploadAsset(String uploadUrl, byte[] content) {
        try {
            RequestBody body = RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM), content);
            Request request = new Request.Builder().url(uploadUrl).post(body).build();
            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            Logger.error("Failed to upload asset. Error: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean createFileRecord(String path, String assetUrl, long fileSize) {
        try {
            String url = buildApiUrl("/records/modify");
            Map<String, Object> request = new HashMap<>();
            List<Map<String, Object>> operations = new ArrayList<>();
            Map<String, Object> operation = new HashMap<>();
            operation.put("operationType", "create");

            Map<String, Object> record = new HashMap<>();
            record.put("recordType", RECORD_TYPE);
            record.put("recordName", UUID.randomUUID().toString());

            Map<String, Object> fields = new HashMap<>();
            fields.put("path", Map.of("value", path, "type", "STRING"));
            fields.put("size", Map.of("value", fileSize, "type", "INT64"));
            fields.put(
                    "asset",
                    Map.of(
                            "value",
                            Map.of(
                                    "fileChecksum",
                                    Normal.EMPTY,
                                    "size",
                                    fileSize,
                                    "wrappingKey",
                                    Normal.EMPTY,
                                    "referenceChecksum",
                                    Normal.EMPTY,
                                    "downloadURL",
                                    assetUrl),
                            "type",
                            "ASSETID"));

            record.put("fields", fields);
            operation.put("record", record);
            operations.add(operation);
            request.put("operations", operations);

            String jsonBody = JsonKit.toJsonString(request);
            RequestBody body = RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_JSON), jsonBody);
            Request httpRequest = new Request.Builder().url(url).post(body)
                    .addHeader(HTTP.AUTHORIZATION, "Bearer " + apiToken).build();

            try (Response response = client.newCall(httpRequest).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            Logger.error("Failed to create file record. Error: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean updateFileRecord(Map<String, Object> record, String newPath) {
        try {
            String url = buildApiUrl("/records/modify");
            String recordName = (String) record.get("recordName");
            String recordChangeTag = (String) record.get("recordChangeTag");

            Map<String, Object> request = new HashMap<>();
            List<Map<String, Object>> operations = new ArrayList<>();
            Map<String, Object> operation = new HashMap<>();
            operation.put("operationType", "update");

            Map<String, Object> recordData = new HashMap<>();
            recordData.put("recordType", RECORD_TYPE);
            recordData.put("recordName", recordName);
            recordData.put("recordChangeTag", recordChangeTag);
            recordData.put("fields", Map.of("path", Map.of("value", newPath, "type", "STRING")));

            operation.put("record", recordData);
            operations.add(operation);
            request.put("operations", operations);

            String jsonBody = JsonKit.toJsonString(request);
            RequestBody body = RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_JSON), jsonBody);
            Request httpRequest = new Request.Builder().url(url).post(body)
                    .addHeader(HTTP.AUTHORIZATION, "Bearer " + apiToken).build();

            try (Response response = client.newCall(httpRequest).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            Logger.error("Failed to update file record. Error: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean deleteFileRecord(Map<String, Object> record) {
        try {
            String url = buildApiUrl("/records/modify");
            String recordName = (String) record.get("recordName");

            Map<String, Object> request = new HashMap<>();
            List<Map<String, Object>> operations = new ArrayList<>();
            Map<String, Object> operation = new HashMap<>();
            operation.put("operationType", "delete");
            operation.put("record", Map.of("recordName", recordName));
            operations.add(operation);
            request.put("operations", operations);

            String jsonBody = JsonKit.toJsonString(request);
            RequestBody body = RequestBody.of(MediaType.valueOf(MediaType.APPLICATION_JSON), jsonBody);
            Request httpRequest = new Request.Builder().url(url).post(body)
                    .addHeader(HTTP.AUTHORIZATION, "Bearer " + apiToken).build();

            try (Response response = client.newCall(httpRequest).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            Logger.error("Failed to delete file record. Error: {}", e.getMessage(), e);
            return false;
        }
    }

    private Blob recordToBlob(Map<String, Object> record) {
        try {
            Map<String, Object> fields = (Map<String, Object>) record.get("fields");
            String path = (String) ((Map<String, Object>) fields.get("path")).get("value");
            Object sizeObj = ((Map<String, Object>) fields.get("size")).get("value");
            String sizeStr = sizeObj != null ? String.valueOf(sizeObj) : "0";

            return Blob.builder().name(path.substring(path.lastIndexOf('/') + 1)).size(sizeStr).build();
        } catch (Exception e) {
            Logger.error("Failed to convert record to blob. Error: {}", e.getMessage(), e);
            return null;
        }
    }

    private String buildApiUrl(String endpoint) {
        return String.format(
                "%s/database/1/%s/%s/%s%s",
                this.context.getEndpoint(),
                containerIdentifier,
                environment,
                DATABASE_TYPE,
                endpoint);
    }

    private String buildPath(String bucket, String fileName) {
        if (StringKit.isBlank(bucket) || "/".equals(bucket)) {
            return "/" + fileName;
        }
        return bucket.endsWith("/") ? bucket + fileName : bucket + "/" + fileName;
    }

}
