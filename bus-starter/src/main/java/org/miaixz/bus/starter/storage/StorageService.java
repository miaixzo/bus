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
package org.miaixz.bus.starter.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.storage.Context;
import org.miaixz.bus.storage.Provider;
import org.miaixz.bus.storage.Registry;
import org.miaixz.bus.storage.cache.StorageCache;
import org.miaixz.bus.storage.magic.ErrorCode;
import org.miaixz.bus.storage.metric.*;

/**
 * A service provider class for managing and creating various file storage provider instances. This class maintains a
 * cache of storage components and supports adding them through configuration or manual registration.
 * <p>
 * It supports a variety of file storage methods, including but not limited to:
 *
 * <ul>
 * <li>Cloud Storage Services: Aliyun OSS, Tencent COS, Huawei OBS, Amazon S3, Qiniu Cloud, Upyun, etc.</li>
 * <li>Local Storage: Local file system</li>
 * <li>Network Storage: FTP, SFTP, WebDAV, etc.</li>
 * <li>Code Hosting Storage: GitLab</li>
 * </ul>
 *
 * <p>
 * <strong>Usage Example:</strong>
 * 
 * <pre>{@code
 * // Create configuration
 * StorageProperties properties = new StorageProperties();
 * // Create the service
 * StorageService service = new StorageService(properties);
 * // Get the Aliyun OSS storage service provider
 * Provider ossProvider = service.require(Registry.ALIYUN);
 * // Upload a file
 * ossProvider.upload("filePath", "fileContent");
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StorageService {

    /**
     * Cache for storing registered storage component contexts. Uses {@link ConcurrentHashMap} for thread safety.
     */
    private static final Map<Registry, Context> CACHE = new ConcurrentHashMap<>();

    /**
     * Storage configuration properties, containing settings for various storage components.
     */
    public StorageProperties properties;

    /**
     * Cache interface for storing file metadata or other temporary data.
     */
    public CacheX cacheX;

    /**
     * Constructs a storage service provider instance with the default cache.
     *
     * @param properties The storage configuration properties (must not be null).
     */
    public StorageService(StorageProperties properties) {
        this(properties, StorageCache.INSTANCE);
    }

    /**
     * Constructs a storage service provider instance with a specified cache.
     *
     * @param properties The storage configuration properties (must not be null).
     * @param cacheX     The cache implementation to use (must not be null).
     */
    public StorageService(StorageProperties properties, CacheX cacheX) {
        this.properties = properties;
        this.cacheX = cacheX;
    }

    /**
     * Registers a storage component in the cache. Throws an exception if a component of the same type is already
     * registered.
     *
     * @param registry The type of the storage component (must not be null).
     * @param context  The context of the storage component (must not be null).
     * @throws InternalException if a component of the same type already exists.
     */
    public static void register(Registry registry, Context context) {
        if (CACHE.containsKey(registry)) {
            throw new InternalException("A component with the same name is already registered: " + registry.name());
        }
        CACHE.putIfAbsent(registry, context);
    }

    /**
     * Retrieves the corresponding storage service provider instance based on the component type. It first searches the
     * cache; if not found, it retrieves from the configuration.
     *
     * @param registry The type of the storage component (must not be null).
     * @return The corresponding storage service provider instance.
     * @throws InternalException if the corresponding storage component cannot be found.
     */
    public Provider require(Registry registry) {
        // Get the storage component context from the cache
        Context context = CACHE.get(registry);
        // If not in the cache, get it from the properties
        if (ObjectKit.isEmpty(context)) {
            context = properties.getType().get(registry);
        }

        // Create the corresponding provider instance based on the storage type
        switch (registry) {
            case ALIYUN:
                return new AliYunOssProvider(context);

            case AMAZON:
                return new AmazonS3Provider(context);

            case AZURE:
                return new AzureBsProvider(context);

            case BACKBLAZE_B2:
                return new BackblazeB2Provider(context);

            case BAIDU:
                return new BaiduBosProvider(context);

            case CLOUDFLARE_R2:
                return new CloudflareR2Provider(context);

            case FTP:
                return new FtpFileProvider(context);

            case S3:
                return new GenericS3Provider(context);

            case GITLAB:
                return new GitlabFileProvider(context);

            case GOOGLE:
                return new GoogleCsProvider(context);

            case HUAWEI:
                return new HuaweiObsProvider(context);

            case JD:
                return new JdOssProvider(context);

            case LOCAL:
                return new LocalFileProvider(context);

            case MINIO:
                return new MinioOssProvider(context);

            case QINIU:
                return new QiniuOssProvider(context);

            case TENCENT:
                return new TencentCosProvider(context);

            case SFTP:
                return new SftpFileProvider(context);

            case SMB:
                return new SmbFileProvider(context);

            case UPYUN:
                return new UpyunOssProvider(context);

            case WASABI:
                return new WasabiProvider(context);

            case WEBDAV:
                return new WebDavProvider(context);

            default:
                // If no matching storage type is found, throw an exception
                throw new InternalException(ErrorCode._100803.getValue());
        }
    }

}
