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
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.ClientX;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.Blob;
import org.miaixz.bus.storage.magic.ErrorCode;

import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * Storage service provider for generic S3-compatible object storage. This provider integrates with any S3-compatible
 * service for file storage operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class GenericS3Provider extends AbstractProvider {

    /**
     * S3 client instance for interacting with the S3-compatible service.
     */
    protected S3Client client;

    /**
     * S3 presigner for generating pre-signed URLs with limited validity.
     */
    protected S3Presigner presigner;

    /**
     * Constructs a generic S3 provider with the given context. Initializes the S3 client and presigner using the
     * provided credentials and endpoint configuration.
     *
     * @param context The storage context, containing endpoint, bucket, access key, secret key, region, and timeout
     *                configurations.
     * @throws IllegalArgumentException If required context parameters are missing or invalid.
     */
    public GenericS3Provider(Context context) {
        this.context = context;

        Assert.notBlank(this.context.getEndpoint(), "[endpoint] cannot be blank");
        Assert.notBlank(this.context.getBucket(), "[bucket] cannot be blank");
        if (!this.context.isAnonymous()) {
            Assert.notBlank(this.context.getAccessKey(), "[accessKey] cannot be blank");
            Assert.notBlank(this.context.getSecretKey(), "[secretKey] cannot be blank");
        }

        ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofSeconds(this.context.getWriteTimeout()))
                .apiCallAttemptTimeout(Duration.ofSeconds(this.context.getReadTimeout())).build();

        AwsCredentialsProvider credentialsProvider;
        if (this.context.isAnonymous()) {
            credentialsProvider = AnonymousCredentialsProvider.create();
        } else {
            AwsCredentials credentials = StringKit.isBlank(this.context.getAccessToken())
                    ? AwsBasicCredentials.create(this.context.getAccessKey(), this.context.getSecretKey())
                    : AwsSessionCredentials.create(
                            this.context.getAccessKey(),
                            this.context.getSecretKey(),
                            this.context.getAccessToken());
            credentialsProvider = StaticCredentialsProvider.create(credentials);
        }

        ClientX clientx = new ClientX.ClientBuilder()
                .connectTimeout(Duration.ofSeconds(this.context.getConnectTimeout()))
                .readTimeout(Duration.ofSeconds(this.context.getReadTimeout()))
                .writeTimeout(Duration.ofSeconds(this.context.getWriteTimeout())).addInterceptor(chain -> {
                    Request request = chain.request();
                    return chain.proceed(request);
                }).build();

        this.client = S3Client.builder().credentialsProvider(credentialsProvider).httpClient(clientx)
                .endpointOverride(URI.create(this.context.getEndpoint()))
                .region(Region.of(StringKit.isBlank(this.context.getRegion()) ? "us-east-1" : this.context.getRegion()))
                .overrideConfiguration(overrideConfig)
                .serviceConfiguration(s -> s.pathStyleAccessEnabled(this.context.isPathStyle())).build();

        this.presigner = S3Presigner.builder().credentialsProvider(credentialsProvider)
                .endpointOverride(URI.create(this.context.getEndpoint()))
                .region(Region.of(StringKit.isBlank(this.context.getRegion()) ? "us-east-1" : this.context.getRegion()))
                .serviceConfiguration(
                        S3Configuration.builder().pathStyleAccessEnabled(this.context.isPathStyle())
                                .chunkedEncodingEnabled(false).build())
                .build();
    }

    /**
     * Reads metadata for a file in the default bucket.
     *
     * @param fileName The file name to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message stat(String fileName) {
        return stat(this.context.getBucket(), fileName);
    }

    /**
     * Reads metadata for a file using the provider's normal key-building rules.
     *
     * @param bucket   The bucket name.
     * @param fileName The file name to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message stat(String bucket, String fileName) {
        String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
        return statKey(bucket, Builder.buildObjectKey(prefix, Normal.EMPTY, fileName));
    }

    /**
     * Reads metadata for an exact S3 object key.
     *
     * @param bucket    The bucket name.
     * @param objectKey The exact S3 object key.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message statKey(String bucket, String objectKey) {
        try {
            HeadObjectResponse response = client
                    .headObject(HeadObjectRequest.builder().bucket(bucket).key(objectKey).build());
            String name = objectKey;
            if (StringKit.isNotBlank(objectKey)) {
                int index = objectKey.lastIndexOf(Symbol.SLASH);
                name = index < 0 ? objectKey : objectKey.substring(index + 1);
            }
            Map<String, Object> extend = new HashMap<>();
            extend.put("storageClass", response.storageClassAsString());
            extend.put("lastModified", response.lastModified());
            if (response.metadata() != null && !response.metadata().isEmpty()) {
                extend.put("metadata", new HashMap<>(response.metadata()));
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(
                            Blob.builder().bucket(bucket).key(objectKey).name(name).path(objectKey)
                                    .size(StringKit.toString(response.contentLength())).type(response.contentType())
                                    .hash(response.eTag()).extend(extend).build())
                    .build();
        } catch (Exception e) {
            Errors error = ErrorCode._113012;
            if (e instanceof NoSuchBucketException) {
                error = ErrorCode._113011;
            } else if (e instanceof NoSuchKeyException) {
                error = ErrorCode._113010;
            } else if (e instanceof S3Exception s3) {
                if (s3.statusCode() == 401 || s3.statusCode() == 403) {
                    error = ErrorCode._113009;
                } else if (s3.statusCode() == 404) {
                    String code = s3.awsErrorDetails() == null ? null : s3.awsErrorDetails().errorCode();
                    error = StringKit.containsIgnoreCase(code, "bucket") ? ErrorCode._113011 : ErrorCode._113010;
                }
            } else if (e instanceof IllegalArgumentException) {
                error = ErrorCode._113008;
            }
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
     * Opens a stream for a file in the default bucket.
     *
     * @param fileName The file name to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message stream(String fileName) {
        return stream(this.context.getBucket(), fileName);
    }

    /**
     * Opens a stream for a file using the provider's normal key-building rules.
     *
     * @param bucket   The bucket name.
     * @param fileName The file name to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message stream(String bucket, String fileName) {
        String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
        return streamKey(bucket, Builder.buildObjectKey(prefix, Normal.EMPTY, fileName));
    }

    /**
     * Opens a stream for an exact S3 object key.
     *
     * @param bucket    The bucket name.
     * @param objectKey The exact S3 object key.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message streamKey(String bucket, String objectKey) {
        try {
            ResponseInputStream<GetObjectResponse> stream = client
                    .getObject(GetObjectRequest.builder().bucket(bucket).key(objectKey).build());
            GetObjectResponse response = stream.response();
            String name = objectKey;
            if (StringKit.isNotBlank(objectKey)) {
                int index = objectKey.lastIndexOf(Symbol.SLASH);
                name = index < 0 ? objectKey : objectKey.substring(index + 1);
            }
            Map<String, Object> extend = new HashMap<>();
            extend.put("storageClass", response.storageClassAsString());
            extend.put("lastModified", response.lastModified());
            if (response.metadata() != null && !response.metadata().isEmpty()) {
                extend.put("metadata", new HashMap<>(response.metadata()));
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(
                            Blob.builder().inputStream(stream).bucket(bucket).key(objectKey).name(name).path(objectKey)
                                    .size(StringKit.toString(response.contentLength())).type(response.contentType())
                                    .hash(response.eTag()).extend(extend).build())
                    .build();
        } catch (Exception e) {
            Errors error = ErrorCode._113012;
            if (e instanceof NoSuchBucketException) {
                error = ErrorCode._113011;
            } else if (e instanceof NoSuchKeyException) {
                error = ErrorCode._113010;
            } else if (e instanceof S3Exception s3) {
                if (s3.statusCode() == 401 || s3.statusCode() == 403) {
                    error = ErrorCode._113009;
                } else if (s3.statusCode() == 404) {
                    String code = s3.awsErrorDetails() == null ? null : s3.awsErrorDetails().errorCode();
                    error = StringKit.containsIgnoreCase(code, "bucket") ? ErrorCode._113011 : ErrorCode._113010;
                }
            } else if (e instanceof IllegalArgumentException) {
                error = ErrorCode._113008;
            }
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
     * files, and other binary files. The underlying input stream is automatically closed using try-with-resources to
     * prevent resource leaks.
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
    /**
     * Downloads a file from the specified storage bucket and returns its content as a byte array.
     * <p>
     * This method reads the entire file content into memory as a byte array, making it suitable for images, PDFs, DOCX
     * files, and other binary files. The underlying input stream is automatically closed using try-with-resources to
     * prevent resource leaks.
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
            GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(objectKey).build();

            // Use try-with-resources to automatically close the InputStream and prevent resource leaks
            try (InputStream inputStream = client.getObject(request)) {
                byte[] content = inputStream.readAllBytes();
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
            GetObjectRequest request = GetObjectRequest.builder().bucket(bucket).key(objectKey).build();

            // Use try-with-resources to automatically close both streams
            try (InputStream inputStream = client.getObject(request);
                    OutputStream outputStream = new FileOutputStream(file)) {
                IoKit.copy(inputStream, outputStream);
            }

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
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
            ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(this.context.getBucket())
                    .prefix(
                            StringKit.isBlank(context.getPrefix()) ? null
                                    : Builder.buildNormalizedPrefix(context.getPrefix()) + Symbol.SLASH)
                    .build();
            ListObjectsV2Response response = client.listObjectsV2(request);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(response.contents().stream().map(item -> {
                        Map<String, Object> extend = new HashMap<>();
                        extend.put("tag", item.eTag());
                        extend.put("storageClass", item.storageClassAsString());
                        extend.put("lastModified", item.lastModified());
                        return Blob.builder().name(item.key())
                                .owner(item.owner() != null ? item.owner().displayName() : null)
                                .size(StringKit.toString(item.size())).extend(extend).build();
                    }).collect(Collectors.toList())).build();
        } catch (SdkException e) {
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
        return rename(Normal.EMPTY, oldName, newName);
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
            boolean keyExists = true;
            try {
                HeadObjectRequest headRequest = HeadObjectRequest.builder().bucket(bucket).key(oldObjectKey).build();
                client.headObject(headRequest);
            } catch (Exception e) {
                keyExists = false;
            }
            if (keyExists) {
                CopyObjectRequest copyRequest = CopyObjectRequest.builder().sourceBucket(bucket).sourceKey(oldObjectKey)
                        .destinationBucket(bucket).destinationKey(newObjectKey).build();
                client.copyObject(copyRequest);
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder().bucket(bucket).key(oldObjectKey)
                        .build();
                client.deleteObject(deleteRequest);
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
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
        return upload(Normal.EMPTY, fileName, content);
    }

    /**
     * Uploads a byte array to a specified path in the default storage bucket.
     *
     * @param path     The target path for the file.
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String path, String fileName, byte[] content) {
        return upload(this.context.getBucket(), path, fileName, content);
    }

    /**
     * Uploads a byte array to the specified storage bucket and path.
     *
     * @param bucket   The name of the storage bucket.
     * @param path     The target path for the file.
     * @param fileName The name of the file to upload.
     * @param content  The file content as a byte array.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message upload(String bucket, String path, String fileName, byte[] content) {
        return upload(bucket, path, fileName, new ByteArrayInputStream(content));
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
        return upload(Normal.EMPTY, fileName, content);
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
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, path, fileName);
            PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(objectKey)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).build();
            client.putObject(request, RequestBody.fromInputStream(content, content.available()));

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofDays(7)).getObjectRequest(r -> r.bucket(bucket).key(objectKey))
                    .build();
            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Blob.builder().name(fileName).url(presignedUrl).path(objectKey).build()).build();
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
        return remove(Normal.EMPTY, fileName);
    }

    /**
     * Removes a file from a specified path in the default storage bucket.
     *
     * @param path     The storage path where the file is located.
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String path, String fileName) {
        return remove(this.context.getBucket(), path, fileName);
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
            DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(bucket).key(objectKey).build();
            client.deleteObject(request);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
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
     * Releases S3 client resources held by this provider.
     */
    @Override
    public void close() {
        if (this.presigner != null) {
            try {
                this.presigner.close();
            } catch (Exception e) {
                // Ignore close-time failures.
            }
        }
        if (this.client != null) {
            try {
                this.client.close();
            } catch (Exception e) {
                // Ignore close-time failures.
            }
        }
    }

}
