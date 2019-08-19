/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.spring.storage;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.storage.Provider;
import org.aoju.bus.storage.StorageProvider;
import org.aoju.bus.storage.UploadObject;
import org.aoju.bus.storage.UploadToken;
import org.aoju.bus.storage.provider.aliyun.AliyunOSSProvider;
import org.aoju.bus.storage.provider.qiniu.QiniuOSSProvider;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.1.0
 * @since JDK 1.8
 */
@EnableConfigurationProperties(value = {StorageProperties.class})
public class StorageConfiguration implements InitializingBean, DisposableBean {

    @Autowired
    StorageProperties properties;
    @Autowired
    StorageProvider storageProvider;

    @Override
    public void destroy() throws Exception {
        storageProvider.close();
    }

    @Override
    public void afterPropertiesSet() {
        if (Provider.QINIU_OSS.getValue().equals(storageProvider)) {
            Assert.notBlank(properties.accessKey, "[accessKey] not defined");
            Assert.notBlank(properties.secretKey, "[secretKey] not defined");
            storageProvider = new QiniuOSSProvider(properties.prefix, properties.bucket, properties.accessKey, properties.secretKey, properties.privated);
        } else if (Provider.ALI_OSS.getValue().equals(storageProvider)) {
            Assert.notBlank(properties.endpoint, "[endpoint] not defined");
            storageProvider = new AliyunOSSProvider(properties.prefix, properties.endpoint, properties.bucket, properties.accessKey, properties.secretKey, properties.internalUrl, properties.privated);
        } else {
            throw new RuntimeException("Provider[" + storageProvider + "] not support");
        }
    }

    public String upload(String fileName, File file) {
        return storageProvider.upload(new UploadObject(fileName, file));
    }

    public String upload(String fileName, InputStream in, String mimeType) {
        return storageProvider.upload(new UploadObject(fileName, in, mimeType));
    }

    public boolean delete(String fileName) {
        return storageProvider.delete(fileName);
    }

    public Map<String, Object> createUploadToken(UploadToken param) {
        return storageProvider.createUploadToken(param);
    }

}
