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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
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
 * @since Java 21+
 */
public class ICloudDriveProvider extends AbstractProvider {

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
        Logger.info(
                false,
                "Storage",
                "Storage provider initialized; provider={}, environment={}, containerConfigured={}",
                this.getClass().getSimpleName(),
                environment,
                StringKit.isNotBlank(containerIdentifier));
    }

    @Override
    public Message<byte[]> download(String fileName) {
        return download(this.context.getBucket(), fileName);
    }

    /**
     * Reads metadata for a file in the default iCloud Drive folder.
     *
     * @param fileName The file name or iCloud path to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message<Blob> stat(String fileName) {
        return stat(this.context.getBucket(), fileName);
    }

    /**
     * Reads metadata for a file in the specified iCloud Drive folder.
     *
     * @param bucket   The folder path in iCloud Drive.
     * @param fileName The file name or iCloud path to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message<Blob> stat(String bucket, String fileName) {
        return statKey(bucket, normalizeObjectPath(bucket, fileName));
    }

    /**
     * Reads metadata for an exact iCloud Drive object path.
     *
     * @param bucket    The folder path in iCloud Drive.
     * @param objectKey The exact iCloud path or file name.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message<Blob> statKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.<Blob>builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }
            String path = normalizeObjectPath(bucket, objectKey);
            Map<String, Object> record = queryFileRecord(path);
            if (record == null) {
                return Message.<Blob>builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }
            return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(toBlob(bucket, path, record, null)).build();
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage stat failed; provider={}, bucket={}, object={}, code={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    objectKey,
                    ErrorCode._113012.getKey(),
                    e.getClass().getSimpleName());
            return Message.<Blob>builder().errcode(ErrorCode._113012.getKey()).errmsg(ErrorCode._113012.getValue())
                    .build();
        }
    }

    /**
     * Opens a stream for a file in the default iCloud Drive folder.
     *
     * @param fileName The file name or iCloud path to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message<Blob> stream(String fileName) {
        return stream(this.context.getBucket(), fileName);
    }

    /**
     * Opens a stream for a file in the specified iCloud Drive folder.
     *
     * @param bucket   The folder path in iCloud Drive.
     * @param fileName The file name or iCloud path to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message<Blob> stream(String bucket, String fileName) {
        return streamKey(bucket, normalizeObjectPath(bucket, fileName));
    }

    /**
     * Opens a stream for an exact iCloud Drive object path.
     *
     * @param bucket    The folder path in iCloud Drive.
     * @param objectKey The exact iCloud path or file name.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message<Blob> streamKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.<Blob>builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }
            String path = normalizeObjectPath(bucket, objectKey);
            Map<String, Object> record = queryFileRecord(path);
            if (record == null) {
                return Message.<Blob>builder().errcode(ErrorCode._113010.getKey()).errmsg(ErrorCode._113010.getValue())
                        .build();
            }

            String downloadUrl = getAssetDownloadUrl(record);
            if (StringKit.isBlank(downloadUrl)) {
                return Message.<Blob>builder().errcode(ErrorCode._113012.getKey()).errmsg(ErrorCode._113012.getValue())
                        .build();
            }

            Response response = get(downloadUrl);
            if (!response.successful()) {
                Errors error = toError(response.code());
                response.close();
                return Message.<Blob>builder().errcode(error.getKey()).errmsg(error.getValue()).build();
            }
            return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(toBlob(bucket, path, record, stream(response))).build();
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage stream failed; provider={}, bucket={}, object={}, code={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    objectKey,
                    ErrorCode._113012.getKey(),
                    e.getClass().getSimpleName());
            return Message.<Blob>builder().errcode(ErrorCode._113012.getKey()).errmsg(ErrorCode._113012.getValue())
                    .build();
        }
    }

    @Override
    public Message<byte[]> download(String bucket, String fileName) {
        try {
            String path = buildPath(bucket, fileName);
            Map<String, Object> record = queryFileRecord(path);
            if (record == null) {
                return Message.<byte[]>builder().errcode(ErrorCode._113003.getKey())
                        .errmsg(ErrorCode._113003.getValue()).build();
            }

            String downloadUrl = getAssetDownloadUrl(record);
            if (StringKit.isBlank(downloadUrl)) {
                return Message.<byte[]>builder().errcode(ErrorCode._FAILURE.getKey())
                        .errmsg("Failed to get download URL").build();
            }

            try (Response response = get(downloadUrl)) {
                if (!response.successful()) {
                    throw new IOException("Download failed: " + response.code());
                }
                byte[] content = response.bytes();
                return Message.<byte[]>builder().errcode(ErrorCode._SUCCESS.getKey())
                        .errmsg(ErrorCode._SUCCESS.getValue()).data(content).build();
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage download failed; provider={}, bucket={}, object={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    fileName,
                    e.getClass().getSimpleName());
            return Message.<byte[]>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue())
                    .build();
        }
    }

    @Override
    public Message<Void> download(String fileName, File file) {
        return download(this.context.getBucket(), fileName, file);
    }

    @Override
    public Message<Void> download(String bucket, String fileName, File file) {
        Message<byte[]> result = download(bucket, fileName);
        if (ErrorCode._SUCCESS.getKey().equals(result.getErrcode())) {
            try {
                byte[] content = result.getData();
                Files.write(file.toPath(), content);
                return Message.<Void>builder().errcode(ErrorCode._SUCCESS.getKey())
                        .errmsg(ErrorCode._SUCCESS.getValue()).build();
            } catch (IOException e) {
                Logger.error(
                        false,
                        "Storage",
                        e,
                        "Storage download-to-local write failed; provider={}, bucket={}, object={}, targetProvided={}, status=failure, exception={}",
                        this.getClass().getSimpleName(),
                        bucket,
                        fileName,
                        file != null,
                        e.getClass().getSimpleName());
                return Message.<Void>builder().errcode(ErrorCode._FAILURE.getKey())
                        .errmsg(ErrorCode._FAILURE.getValue()).build();
            }
        }
        return Message.<Void>builder().errcode(result.getErrcode()).errmsg(result.getErrmsg()).build();
    }

    @Override
    public Message<List<Blob>> list() {
        try {
            List<Map<String, Object>> records = queryFolderRecords(this.context.getBucket());
            List<Blob> blobs = new ArrayList<>();
            for (Map<String, Object> record : records) {
                Blob blob = recordToBlob(record);
                if (blob != null) {
                    blobs.add(blob);
                }
            }
            return Message.<List<Blob>>builder().errcode(ErrorCode._SUCCESS.getKey())
                    .errmsg(ErrorCode._SUCCESS.getValue()).data(blobs).build();
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage list failed; provider={}, bucket={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    this.context.getBucket(),
                    e.getClass().getSimpleName());
            return Message.<List<Blob>>builder().errcode(ErrorCode._FAILURE.getKey())
                    .errmsg(ErrorCode._FAILURE.getValue()).build();
        }
    }

    @Override
    public Message<Void> rename(String oldName, String newName) {
        return rename(this.context.getBucket(), Normal.EMPTY, oldName, newName);
    }

    @Override
    public Message<Void> rename(String path, String oldName, String newName) {
        return rename(this.context.getBucket(), path, oldName, newName);
    }

    @Override
    public Message<Void> rename(String bucket, String path, String oldName, String newName) {
        try {
            String oldPath = buildPath(bucket, oldName);
            String newPath = buildPath(bucket, newName);

            Map<String, Object> record = queryFileRecord(oldPath);
            if (record == null) {
                return Message.<Void>builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            boolean success = updateFileRecord(record, newPath);
            if (success) {
                return Message.<Void>builder().errcode(ErrorCode._SUCCESS.getKey())
                        .errmsg(ErrorCode._SUCCESS.getValue()).build();
            } else {
                return Message.<Void>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("Failed to rename file")
                        .build();
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage rename failed; provider={}, bucket={}, sourceObject={}, targetObject={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    oldName,
                    newName,
                    e.getClass().getSimpleName());
            return Message.<Void>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue())
                    .build();
        }
    }

    @Override
    public Message<Blob> upload(String fileName, byte[] content) {
        return upload(this.context.getBucket(), Normal.EMPTY, fileName, content);
    }

    @Override
    public Message<Blob> upload(String path, String fileName, byte[] content) {
        return upload(this.context.getBucket(), path, fileName, content);
    }

    @Override
    public Message<Blob> upload(String bucket, String path, String fileName, byte[] content) {
        try {
            String filePath = buildPath(bucket, fileName);

            String uploadUrl = requestAssetUpload(content.length);
            if (StringKit.isBlank(uploadUrl)) {
                return Message.<Blob>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("Failed to get upload URL")
                        .build();
            }

            boolean uploaded = uploadAsset(uploadUrl, content);
            if (!uploaded) {
                return Message.<Blob>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("Failed to upload asset")
                        .build();
            }

            boolean created = createFileRecord(filePath, uploadUrl, content.length);
            if (created) {
                return Message.<Blob>builder().errcode(ErrorCode._SUCCESS.getKey())
                        .errmsg(ErrorCode._SUCCESS.getValue()).build();
            } else {
                return Message.<Blob>builder().errcode(ErrorCode._FAILURE.getKey())
                        .errmsg("Failed to create file record").build();
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage upload failed; provider={}, bucket={}, object={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    fileName,
                    e.getClass().getSimpleName());
            return Message.<Blob>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue())
                    .build();
        }
    }

    @Override
    public Message<Blob> upload(String fileName, InputStream content) {
        return upload(this.context.getBucket(), Normal.EMPTY, fileName, content);
    }

    @Override
    public Message<Blob> upload(String path, String fileName, InputStream content) {
        return upload(this.context.getBucket(), path, fileName, content);
    }

    @Override
    public Message<Blob> upload(String bucket, String path, String fileName, InputStream content) {
        try {
            byte[] bytes = IoKit.readBytes(content);
            return upload(bucket, path, fileName, bytes);
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage upload stream read failed; provider={}, bucket={}, object={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    fileName,
                    e.getClass().getSimpleName());
            return Message.<Blob>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue())
                    .build();
        }
    }

    @Override
    public Message<Void> remove(String fileName) {
        return remove(this.context.getBucket(), Normal.EMPTY, fileName);
    }

    @Override
    public Message<Void> remove(String path, String fileName) {
        return remove(this.context.getBucket(), path, fileName);
    }

    @Override
    public Message<Void> remove(String bucket, String path, String fileName) {
        try {
            String filePath = buildPath(bucket, fileName);
            Map<String, Object> record = queryFileRecord(filePath);
            if (record == null) {
                return Message.<Void>builder().errcode(ErrorCode._113003.getKey()).errmsg(ErrorCode._113003.getValue())
                        .build();
            }

            boolean deleted = deleteFileRecord(record);
            if (deleted) {
                return Message.<Void>builder().errcode(ErrorCode._SUCCESS.getKey())
                        .errmsg(ErrorCode._SUCCESS.getValue()).build();
            } else {
                return Message.<Void>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("Failed to delete file")
                        .build();
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage remove failed; provider={}, bucket={}, object={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    bucket,
                    fileName,
                    e.getClass().getSimpleName());
            return Message.<Void>builder().errcode(ErrorCode._FAILURE.getKey()).errmsg(ErrorCode._FAILURE.getValue())
                    .build();
        }
    }

    @Override
    public Message<Void> remove(String bucket, Path path) {
        return Message.<Void>builder().errcode(ErrorCode._FAILURE.getKey())
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

            try (Response response = post(
                    url,
                    jsonBody,
                    MediaType.APPLICATION_JSON,
                    header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + apiToken))) {
                if (response.successful()) {
                    String responseBody = response.text();
                    Map<String, Object> result = JsonKit.toPojo(responseBody, Map.class);
                    List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
                    return (records != null && !records.isEmpty()) ? records.get(0) : null;
                }
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage file record query failed; provider={}, pathProvided={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    StringKit.isNotBlank(path),
                    e.getClass().getSimpleName());
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

            try (Response response = post(
                    url,
                    jsonBody,
                    MediaType.APPLICATION_JSON,
                    header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + apiToken))) {
                if (response.successful()) {
                    String responseBody = response.text();
                    Map<String, Object> result = JsonKit.toPojo(responseBody, Map.class);
                    List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
                    return records != null ? records : new ArrayList<>();
                }
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage folder records query failed; provider={}, bucket={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    folder,
                    e.getClass().getSimpleName());
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
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage asset link extraction failed; provider={}, recordProvided={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    record != null,
                    e.getClass().getSimpleName());
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

            try (Response response = post(
                    url,
                    jsonBody,
                    MediaType.APPLICATION_JSON,
                    header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + apiToken))) {
                if (response.successful()) {
                    String responseBody = response.text();
                    Map<String, Object> result = JsonKit.toPojo(responseBody, Map.class);
                    List<Map<String, Object>> tokensList = (List<Map<String, Object>>) result.get("tokens");
                    if (tokensList != null && !tokensList.isEmpty()) {
                        return (String) tokensList.get(0).get("url");
                    }
                }
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage asset upload request failed; provider={}, fileSize={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    fileSize,
                    e.getClass().getSimpleName());
        }
        return null;
    }

    private boolean uploadAsset(String uploadUrl, byte[] content) {
        try {
            try (Response response = post(uploadUrl, content, MediaType.APPLICATION_OCTET_STREAM)) {
                return response.successful();
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage asset upload failed; provider={}, payloadBytes={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    content == null ? 0 : content.length,
                    e.getClass().getSimpleName());
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

            try (Response response = post(
                    url,
                    jsonBody,
                    MediaType.APPLICATION_JSON,
                    header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + apiToken))) {
                return response.successful();
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage file record create failed; provider={}, pathProvided={}, fileSize={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    StringKit.isNotBlank(path),
                    fileSize,
                    e.getClass().getSimpleName());
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

            try (Response response = post(
                    url,
                    jsonBody,
                    MediaType.APPLICATION_JSON,
                    header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + apiToken))) {
                return response.successful();
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage file record update failed; provider={}, pathProvided={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    StringKit.isNotBlank(newPath),
                    e.getClass().getSimpleName());
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

            try (Response response = post(
                    url,
                    jsonBody,
                    MediaType.APPLICATION_JSON,
                    header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + apiToken))) {
                return response.successful();
            }
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage file record delete failed; provider={}, recordProvided={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    record != null,
                    e.getClass().getSimpleName());
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
            Logger.error(
                    false,
                    "Storage",
                    e,
                    "Storage file record conversion failed; provider={}, recordProvided={}, status=failure, exception={}",
                    this.getClass().getSimpleName(),
                    record != null,
                    e.getClass().getSimpleName());
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
        if (StringKit.isBlank(bucket) || Symbol.SLASH.equals(bucket)) {
            return Symbol.SLASH + fileName;
        }
        return bucket.endsWith(Symbol.SLASH) ? bucket + fileName : bucket + Symbol.SLASH + fileName;
    }

    /**
     * Normalizes an iCloud object key to an absolute path.
     *
     * @param bucket    The folder path in iCloud Drive.
     * @param objectKey The object key or file name.
     * @return The absolute iCloud path.
     */
    private String normalizeObjectPath(String bucket, String objectKey) {
        return objectKey.startsWith(Symbol.SLASH) ? objectKey : buildPath(bucket, objectKey);
    }

    /**
     * Converts an iCloud record to a storage blob.
     *
     * @param bucket      The folder path in iCloud Drive.
     * @param path        The absolute iCloud path.
     * @param record      The iCloud record.
     * @param inputStream The optional file stream.
     * @return The storage blob.
     */
    private Blob toBlob(String bucket, String path, Map<String, Object> record, InputStream inputStream) {
        Map<String, Object> fields = (Map<String, Object>) record.get("fields");
        Object sizeObject = fieldValue(fields, "size");

        Map<String, Object> extend = new HashMap<>();
        extend.put("recordName", record.get("recordName"));
        extend.put("recordChangeTag", record.get("recordChangeTag"));

        return Blob.builder().inputStream(inputStream).bucket(bucket).key(path)
                .name(path.substring(path.lastIndexOf(Symbol.SLASH) + 1)).path(path)
                .size(sizeObject == null ? "0" : String.valueOf(sizeObject)).extend(extend).build();
    }

    /**
     * Reads a CloudKit field value.
     *
     * @param fields The fields map.
     * @param name   The field name.
     * @return The field value.
     */
    private Object fieldValue(Map<String, Object> fields, String name) {
        if (fields == null || !(fields.get(name) instanceof Map<?, ?> field)) {
            return null;
        }
        return field.get("value");
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

}
