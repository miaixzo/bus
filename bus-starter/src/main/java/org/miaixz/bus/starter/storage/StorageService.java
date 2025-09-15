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
 * 存储服务提供类，用于管理和创建各种文件存储服务提供者实例。 该类维护了一个存储组件的缓存，支持通过配置或手动注册方式添加存储组件。
 *
 * <p>
 * 该类支持多种文件存储方式，包括但不限于：
 * </p>
 * <ul>
 * <li>云存储服务：阿里云OSS、腾讯云COS、华为云OBS、亚马逊S3、七牛云、又拍云等</li>
 * <li>本地存储：本地文件系统</li>
 * <li>网络存储：FTP、SFTP、WebDAV等</li>
 * <li>代码托管存储：GitLab</li>
 * </ul>
 *
 * <p>
 * 使用示例：
 * </p>
 * 
 * <pre>
 * // 创建配置
 * StorageProperties properties = new StorageProperties();
 * // 创建服务
 * StorageProviderService service = new StorageProviderService(properties);
 * // 获取阿里云OSS存储服务提供者
 * Provider ossProvider = service.require(Registry.ALIYUN);
 * // 上传文件
 * ossProvider.upload("文件路径", "文件内容");
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StorageService {

    /**
     * 存储组件缓存，用于存储已注册的存储组件。 使用ConcurrentHashMap保证线程安全。
     */
    private static Map<Registry, Context> CACHE = new ConcurrentHashMap<>();

    /**
     * 存储配置属性，包含各种存储组件的配置信息。
     */
    public StorageProperties properties;

    /**
     * 缓存接口，用于存储文件元数据等临时数据。
     */
    public CacheX CacheX;

    /**
     * 使用默认缓存创建存储服务提供者实例。
     *
     * @param properties 存储配置属性，不能为null
     */
    public StorageService(StorageProperties properties) {
        this(properties, StorageCache.INSTANCE);
    }

    /**
     * 使用指定缓存创建存储服务提供者实例。
     *
     * @param properties 存储配置属性，不能为null
     * @param CacheX     缓存实现，不能为null
     */
    public StorageService(StorageProperties properties, CacheX CacheX) {
        this.properties = properties;
        this.CacheX = CacheX;
    }

    /**
     * 注册存储组件到缓存中。 如果已存在相同类型的组件，则抛出异常。
     *
     * @param type    存储组件类型，不能为null
     * @param context 存储组件上下文，不能为null
     * @throws InternalException 如果已存在相同类型的组件
     */
    public static void register(Registry type, Context context) {
        if (CACHE.containsKey(type)) {
            throw new InternalException("重复注册同名称的组件：" + type.name());
        }
        CACHE.putIfAbsent(type, context);
    }

    /**
     * 根据存储组件类型获取对应的存储服务提供者实例。 首先从缓存中查找，如果不存在则从配置中获取。
     *
     * @param type 存储组件类型，不能为null
     * @return 对应的存储服务提供者实例
     * @throws InternalException 如果找不到对应的存储组件
     */
    public Provider require(Registry type) {
        // 从缓存中获取存储组件上下文
        Context context = CACHE.get(type);
        // 如果缓存中不存在，则从配置中获取
        if (ObjectKit.isEmpty(context)) {
            context = properties.getType().get(type);
        }

        // 根据不同的存储类型创建对应的存储服务提供者实例
        if (Registry.ALIYUN.equals(type)) {
            return new AliYunOssProvider(context);
        } else if (Registry.AMAZON.equals(type)) {
            return new AmazonS3Provider(context);
        } else if (Registry.BAIDU.equals(type)) {
            return new BaiduBosProvider(context);
        } else if (Registry.FTP.equals(type)) {
            return new FtpFileProvider(context);
        } else if (Registry.GITLAB.equals(type)) {
            return new GitlabFileProvider(context);
        } else if (Registry.GOOGLE.equals(type)) {
            return new GoogleCsProvider(context);
        } else if (Registry.HUAWEI.equals(type)) {
            return new HuaweiObsProvider(context);
        } else if (Registry.JD.equals(type)) {
            return new JdOssProvider(context);
        } else if (Registry.LOCAL.equals(type)) {
            return new LocalFileProvider(context);
        } else if (Registry.MINIO.equals(type)) {
            return new MinioOssProvider(context);
        } else if (Registry.QINIU.equals(type)) {
            return new QiniuOssProvider(context);
        } else if (Registry.TENCENT.equals(type)) {
            return new TencentCosProvider(context);
        } else if (Registry.SFTP.equals(type)) {
            return new SftpFileProvider(context);
        } else if (Registry.SMB.equals(type)) {
            return new SmbFileProvider(context);
        } else if (Registry.UPYUN.equals(type)) {
            return new UpyunOssProvider(context);
        } else if (Registry.WEBDAV.equals(type)) {
            return new WebDavProvider(context);
        }
        // 如果没有匹配的存储类型，抛出异常
        throw new InternalException(ErrorCode._100803.getValue());
    }

}