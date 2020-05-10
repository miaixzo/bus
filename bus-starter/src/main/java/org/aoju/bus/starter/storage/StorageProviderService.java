/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.starter.storage;

import org.aoju.bus.cache.metric.ExtendCache;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.storage.Builder;
import org.aoju.bus.storage.Context;
import org.aoju.bus.storage.Provider;
import org.aoju.bus.storage.Registry;
import org.aoju.bus.storage.metric.StorageCache;
import org.aoju.bus.storage.provider.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储服务提供
 *
 * @author Kimi Liu
 * @version 5.8.9
 * @since JDK 1.8+
 */
public class StorageProviderService {

    /**
     * 组件配置
     */
    private static Map<Registry, Context> CACHE = new ConcurrentHashMap<>();
    public StorageProperties properties;
    public ExtendCache extendCache;

    public StorageProviderService(StorageProperties properties) {
        this(properties, StorageCache.INSTANCE);
    }

    public StorageProviderService(StorageProperties properties, ExtendCache extendCache) {
        this.properties = properties;
        this.extendCache = extendCache;
    }

    /**
     * 注册组件
     *
     * @param type    组件名称
     * @param context 组件对象
     */
    public static void register(Registry type, Context context) {
        if (CACHE.containsKey(type)) {
            throw new InstrumentException("重复注册同名称的组件：" + type.name());
        }
        CACHE.putIfAbsent(type, context);
    }

    public Provider require(Registry type) {
        Context context = CACHE.get(type);
        if (ObjectUtils.isEmpty(context)) {
            context = properties.getType().get(type);
        }
        if (Registry.ALIYUN.equals(type)) {
            return new AliYunOssProvider(context);
        } else if (Registry.BAIDU.equals(type)) {
            return new BaiduYunBosProvider(context);
        } else if (Registry.HUAWEI.equals(type)) {
            return new HuaweiObsProvider(context);
        } else if (Registry.JD.equals(type)) {
            return new JdYunOssProvider(context);
        } else if (Registry.MINIO.equals(type)) {
            return new MinioOssProvider(context);
        } else if (Registry.QINIU.equals(type)) {
            return new QiniuYunOssProvider(context);
        } else if (Registry.TENCENT.equals(type)) {
            return new TencentCosProvider(context);
        } else if (Registry.UPYUN.equals(type)) {
            return new UpaiYunOssProvider(context);
        } else if (Registry.LOCAL.equals(type)) {
            return new LocalFileProvider(context);
        }
        throw new InstrumentException(Builder.ErrorCode.UNSUPPORTED.getMsg());
    }

}
