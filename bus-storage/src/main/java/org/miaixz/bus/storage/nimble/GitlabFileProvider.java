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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.gitlab.GitLabApi;
import org.miaixz.bus.gitlab.models.RepositoryFile;
import org.miaixz.bus.gitlab.models.TreeItem;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.Blob;
import org.miaixz.bus.storage.magic.ErrorCode;

/**
 * Storage service provider for GitLab. This provider integrates with GitLab for file storage operations, treating
 * GitLab repositories as storage buckets.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class GitlabFileProvider extends AbstractProvider {

    /**
     * The GitLab API client instance for interacting with the GitLab server.
     */
    private final GitLabApi client;

    /**
     * Constructs a GitLab file provider with the given context. Initializes the GitLab API client using the provided
     * endpoint and access token.
     *
     * @param context The storage context, containing the GitLab API endpoint, project ID (as bucket), and private token
     *                (as access key).
     * @throws IllegalArgumentException If required context parameters (endpoint, bucket, accessKey) are missing or
     *                                  invalid.
     */
    public GitlabFileProvider(Context context) {
        this.context = context;

        Assert.notBlank(this.context.getEndpoint(), "[endpoint] cannot be blank");
        Assert.notBlank(this.context.getBucket(), "[bucket] cannot be blank");
        Assert.notBlank(this.context.getAccessKey(), "[accessKey] cannot be blank");

        this.client = new GitLabApi(this.context.getEndpoint(), this.context.getAccessKey());
    }

    /**
     * Reads metadata for a file in the default GitLab project.
     *
     * @param fileName The file name to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message stat(String fileName) {
        return stat(this.context.getBucket(), fileName);
    }

    /**
     * Reads metadata for a file using the provider's normal GitLab path-building rules.
     *
     * @param bucket   The GitLab project ID or path.
     * @param fileName The file name to read.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message stat(String bucket, String fileName) {
        String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
        return statKey(bucket, Builder.buildObjectKey(prefix, Normal.EMPTY, fileName));
    }

    /**
     * Reads metadata for an exact GitLab repository file path.
     *
     * @param bucket    The GitLab project ID or path.
     * @param objectKey The exact repository file path.
     * @return A {@link Message} containing storage metadata.
     */
    @Override
    public Message statKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }

            RepositoryFile file = client.getRepositoryFileApi().getFile(bucket, objectKey, "master", false);
            String name = StringKit.isBlank(file.getFileName()) ? objectKey : file.getFileName();
            Map<String, Object> extend = new HashMap<>();
            extend.put("filePath", file.getFilePath());
            extend.put("encoding", file.getEncoding());
            extend.put("ref", file.getRef());
            extend.put("blobId", file.getBlobId());
            extend.put("commitId", file.getCommitId());
            extend.put("lastCommitId", file.getLastCommitId());

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(
                            Blob.builder().bucket(bucket).key(objectKey).name(name).path(objectKey)
                                    .size(StringKit.toString(file.getSize())).hash(file.getContentSha256())
                                    .extend(extend).build())
                    .build();
        } catch (Exception e) {
            Errors error = ErrorCode._113012;
            if (e instanceof IllegalArgumentException) {
                error = ErrorCode._113008;
            } else if (StringKit.containsIgnoreCase(e.getMessage(), "404")
                    || StringKit.containsIgnoreCase(e.getMessage(), "not found")) {
                error = ErrorCode._113010;
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
     * Opens a raw stream for a file in the default GitLab project.
     *
     * @param fileName The file name to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message stream(String fileName) {
        return stream(this.context.getBucket(), fileName);
    }

    /**
     * Opens a raw stream for a file using the provider's normal GitLab path-building rules.
     *
     * @param bucket   The GitLab project ID or path.
     * @param fileName The file name to read.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message stream(String bucket, String fileName) {
        String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
        return streamKey(bucket, Builder.buildObjectKey(prefix, Normal.EMPTY, fileName));
    }

    /**
     * Opens a raw stream for an exact GitLab repository file path.
     *
     * @param bucket    The GitLab project ID or path.
     * @param objectKey The exact repository file path.
     * @return A {@link Message} containing a storage resource.
     */
    @Override
    public Message streamKey(String bucket, String objectKey) {
        try {
            if (StringKit.isBlank(objectKey)) {
                return Message.builder().errcode(ErrorCode._113008.getKey()).errmsg(ErrorCode._113008.getValue())
                        .build();
            }

            RepositoryFile file = client.getRepositoryFileApi().getFile(bucket, objectKey, "master", false);
            InputStream inputStream = client.getRepositoryFileApi().getRawFile(bucket, "master", objectKey);
            String name = StringKit.isBlank(file.getFileName()) ? objectKey : file.getFileName();
            Map<String, Object> extend = new HashMap<>();
            extend.put("filePath", file.getFilePath());
            extend.put("encoding", file.getEncoding());
            extend.put("ref", file.getRef());
            extend.put("blobId", file.getBlobId());
            extend.put("commitId", file.getCommitId());
            extend.put("lastCommitId", file.getLastCommitId());

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(
                            Blob.builder().inputStream(inputStream).bucket(bucket).key(objectKey).name(name)
                                    .path(objectKey).size(StringKit.toString(file.getSize()))
                                    .hash(file.getContentSha256()).extend(extend).build())
                    .build();
        } catch (Exception e) {
            Errors error = ErrorCode._113012;
            if (e instanceof IllegalArgumentException) {
                error = ErrorCode._113008;
            } else if (StringKit.containsIgnoreCase(e.getMessage(), "404")
                    || StringKit.containsIgnoreCase(e.getMessage(), "not found")) {
                error = ErrorCode._113010;
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
     * Downloads a file from the default GitLab project (bucket) and returns its content as a byte array.
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
     * Downloads a file from the specified GitLab project (bucket) and returns its content as a byte array.
     * <p>
     * This method retrieves the file content from GitLab and returns it as a byte array, making it suitable for images,
     * PDFs, DOCX files, and other binary files.
     * </p>
     * <p>
     * <strong>Note:</strong> For large files (> 50MB), consider using {@link #download(String, String, File)} instead
     * to avoid excessive memory consumption.
     * </p>
     *
     * @param bucket   The ID of the GitLab project (bucket).
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation. If successful, the data field contains the file
     *         content as a byte array; otherwise, it contains error information.
     */
    @Override
    public Message download(String bucket, String fileName) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, Normal.EMPTY, fileName);
            RepositoryFile file = client.getRepositoryFileApi().getFile(bucket, objectKey, "master");
            byte[] content = file.getContent().getBytes();

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(content).build();
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
     * Downloads a file from the default GitLab project (bucket) and saves it to a local file.
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
     * Downloads a file from the specified GitLab project (bucket) and saves it directly to a local file.
     * <p>
     * This method retrieves file content from GitLab and writes it to the specified local file. The output stream is
     * automatically closed using try-with-resources to ensure proper resource management.
     * </p>
     * <p>
     * <strong>Recommended for:</strong> Large files, or any scenario where you need to persist the file locally without
     * holding it entirely in memory.
     * </p>
     *
     * @param bucket   The ID of the GitLab project (bucket).
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
            RepositoryFile repositoryFile = client.getRepositoryFileApi().getFile(bucket, objectKey, "master");
            byte[] content = repositoryFile.getContent().getBytes();

            // Use try-with-resources to automatically close the output stream
            try (OutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write(content);
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
            return Message.builder().errcode(ErrorCode._FAILURE.getKey()).errmsg("Failed to download file").build();
        }
    }

    /**
     * Lists files in the default GitLab project (bucket).
     *
     * @return A {@link Message} containing the result of the operation, including a list of {@link Blob} objects if
     *         successful.
     */
    @Override
    public Message list() {
        try {
            String prefix = StringKit.isBlank(context.getPrefix()) ? null
                    : Builder.buildNormalizedPrefix(context.getPrefix()) + "/";
            // Use getTree method to retrieve the repository tree, with "master" as the branch.
            List<TreeItem> treeItems = client.getRepositoryApi()
                    .getTree(this.context.getBucket(), prefix, "master", true);
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(
                            treeItems.stream()
                                    // Filter for file types (assuming TreeItem has a getType method, where "blob"
                                    // indicates a file).
                                    .filter(item -> "blob".equals(item.getType())).map(item -> {
                                        Map<String, Object> extend = new HashMap<>();
                                        // As getCommitId does not exist, lastModified is temporarily set to null.
                                        // If the last commit ID is needed, it can be obtained via RepositoryFileApi or
                                        // CommitsApi.
                                        extend.put("lastModified", null);
                                        return Blob.builder().name(item.getPath()).size("0").extend(extend).build();
                                    }).collect(Collectors.toList()))
                    .build();
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
     * Renames a file in the default GitLab project (bucket).
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
     * Renames a file within a specified path in the default GitLab project (bucket).
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
     * Renames a file within the specified GitLab project (bucket) and path.
     *
     * @param bucket  The ID of the GitLab project (bucket).
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
            RepositoryFile file = client.getRepositoryFileApi().getFile(bucket, oldObjectKey, "master");
            if (file != null) {
                client.getRepositoryFileApi().deleteFile(bucket, oldObjectKey, "master", "delete");
                RepositoryFile newFile = new RepositoryFile();
                newFile.setFilePath(newObjectKey);
                newFile.setContent(file.getContent());
                client.getRepositoryFileApi().createFile(bucket, newFile, "master", "create");
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
     * Uploads a byte array to the default GitLab project (bucket).
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
     * Uploads a byte array to a specified path in the default GitLab project (bucket).
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
     * Uploads a byte array to the specified GitLab project (bucket) and path.
     *
     * @param bucket   The ID of the GitLab project (bucket).
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
     * Uploads an input stream to the default GitLab project (bucket).
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
     * Uploads an input stream to a specified path in the default GitLab project (bucket).
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
     * Uploads an input stream to the specified GitLab project (bucket) and path.
     *
     * @param bucket   The ID of the GitLab project (bucket).
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

            // Read the content from the input stream
            byte[] contentBytes = content.readAllBytes();

            RepositoryFile file = new RepositoryFile();
            file.setFilePath(objectKey);
            file.setContent(new String(contentBytes));
            client.getRepositoryFileApi().createFile(bucket, file, "master", "upload");

            String url = client.getProjectApi().getProject(bucket).getWebUrl() + "/-/blob/master/" + objectKey;

            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Blob.builder().name(fileName).url(url).path(objectKey).build()).build();
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
     * Removes a file from the default GitLab project (bucket).
     *
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String fileName) {
        return remove(this.context.getBucket(), Normal.EMPTY, fileName);
    }

    /**
     * Removes a file from a specified path in the default GitLab project (bucket).
     *
     * @param path     The storage path where the file is located.
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String path, String fileName) {
        return remove(path, Normal.EMPTY, fileName);
    }

    /**
     * Removes a file from the specified GitLab project (bucket) and path.
     *
     * @param bucket   The ID of the GitLab project (bucket).
     * @param path     The storage path where the file is located.
     * @param fileName The name of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String bucket, String path, String fileName) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, path, fileName);
            client.getRepositoryFileApi().deleteFile(bucket, objectKey, "master", "delete");
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
     * Removes a file from the specified GitLab project (bucket) based on its path.
     *
     * @param bucket The ID of the GitLab project (bucket).
     * @param path   The target path of the file to remove.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message remove(String bucket, Path path) {
        return remove(bucket, path.toString(), Normal.EMPTY);
    }

}
