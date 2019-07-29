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
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
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
