/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.storage.metric;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.gitlab.GitLabApi;
import org.miaixz.bus.gitlab.models.RepositoryFile;
import org.miaixz.bus.gitlab.models.TreeItem;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.storage.Builder;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.magic.ErrorCode;
import org.miaixz.bus.storage.magic.Blob;

/**
 * Storage service provider for GitLab. This provider integrates with GitLab for file storage operations, treating
 * GitLab repositories as storage buckets.
 *
 * @author Kimi Liu
 * @since Java 17+
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
     * Downloads a file from the default GitLab project (bucket).
     *
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation, including the file content stream if
     *         successful.
     */
    @Override
    public Message download(String fileName) {
        return download(this.context.getBucket(), fileName);
    }

    /**
     * Downloads a file from the specified GitLab project (bucket).
     *
     * @param bucket   The ID of the GitLab project (bucket).
     * @param fileName The name of the file to download.
     * @return A {@link Message} containing the result of the operation, including the file content stream if
     *         successful.
     */
    @Override
    public Message download(String bucket, String fileName) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, Normal.EMPTY, fileName);
            RepositoryFile file = client.getRepositoryFileApi().getFile(bucket, objectKey, "master");
            byte[] content = file.getContent().getBytes();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(content)));
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(bufferedReader).build();
        } catch (Exception e) {
            Logger.error("Failed to download file: {} from bucket: {}. Error: {}", fileName, bucket, e.getMessage(), e);
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
     * Downloads a file from the specified GitLab project (bucket) and saves it to a local file.
     *
     * @param bucket   The ID of the GitLab project (bucket).
     * @param fileName The name of the file to download.
     * @param file     The target local file to save the downloaded content.
     * @return A {@link Message} containing the result of the operation.
     */
    @Override
    public Message download(String bucket, String fileName, File file) {
        try {
            String prefix = Builder.buildNormalizedPrefix(context.getPrefix());
            String objectKey = Builder.buildObjectKey(prefix, Normal.EMPTY, fileName);
            RepositoryFile repositoryFile = client.getRepositoryFileApi().getFile(bucket, objectKey, "master");
            byte[] content = repositoryFile.getContent().getBytes();
            try (OutputStream outputStream = new FileOutputStream(file)) {
                IoKit.copy(new ByteArrayInputStream(content), outputStream);
            }
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue()).build();
        } catch (Exception e) {
            Logger.error(
                    "Failed to download file: {} from bucket: {} to local file: {}. Error: {}",
                    fileName,
                    bucket,
                    file.getAbsolutePath(),
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
            Logger.error("Failed to list files in bucket: {}. Error: {}", this.context.getBucket(), e.getMessage(), e);
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
                    "Failed to rename file: from {} to {} in bucket: {} path: {}. Error: {}",
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
            byte[] contentBytes = IoKit.readBytes(content);
            RepositoryFile file = new RepositoryFile();
            file.setFilePath(objectKey);
            file.setContent(new String(contentBytes));
            client.getRepositoryFileApi().createFile(bucket, file, "master", "upload");
            String url = client.getProjectApi().getProject(bucket).getWebUrl() + "/-/blob/master/" + objectKey;
            return Message.builder().errcode(ErrorCode._SUCCESS.getKey()).errmsg(ErrorCode._SUCCESS.getValue())
                    .data(Blob.builder().name(fileName).url(url).path(objectKey).build()).build();
        } catch (Exception e) {
            Logger.error(
                    "Failed to upload file: {} to bucket: {} path: {}. Error: {}",
                    fileName,
                    bucket,
                    path,
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
                    "Failed to delete file: {} from bucket: {} path: {}. Error: {}",
                    fileName,
                    bucket,
                    path,
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
