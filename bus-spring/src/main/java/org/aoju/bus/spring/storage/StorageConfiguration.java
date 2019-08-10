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

import org.aoju.bus.storage.StorageProvider;
import org.aoju.bus.storage.UploadObject;
import org.aoju.bus.storage.UploadToken;
import org.aoju.bus.storage.provider.fdfs.FdfsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author Kimi Liu
 * @version 3.0.0
 * @since JDK 1.8
 */
@EnableConfigurationProperties(value = {StorageProperties.class})
public class StorageConfiguration {

    @Autowired
    StorageProperties properties;
    @Autowired
    StorageProvider storageProvider;

    @Bean
    public void afterPropertiesSet() {
        if (FdfsProvider.NAME.equals(this.properties.getProvider())) {
            Properties properties = new Properties();
            storageProvider = new FdfsProvider(this.properties.getGroupName(), properties);
        } else {
            throw new RuntimeException("Provider[" + this.properties.getProvider() + "] not support");
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
