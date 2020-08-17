/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.metric.secure;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * appkey，secret文件管理，功能同CacheAppSecretManager，这个是将appKey，secret放在属性文件中
 * key为appKey，value为secret
 *
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8++
 */
public class FileAppSecretManager implements AppSecretManager {

    private String appSecretFile = "appSecret.properties";

    private Properties properties;

    @Override
    public void addAppSecret(Map<String, String> appSecretStore) {
        properties.putAll(appSecretStore);
    }

    @Override
    public String getSecret(String appKey) {
        if (properties == null) {
            try {
                // 默认加载class根目录的appSecret.properties文件
                DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
                Resource resource = resourceLoader.getResource(appSecretFile);
                properties = PropertiesLoaderUtils.loadProperties(resource);
            } catch (IOException e) {
                throw new RuntimeException("在类路径下找不到appSecret.properties的应用密钥的属性文件");
            }
        }

        return properties.getProperty(appKey);
    }

    public void setAppSecretFile(String appSecretFile) {
        this.appSecretFile = appSecretFile;
    }

    @Override
    public boolean isValidAppKey(String appKey) {
        if (appKey == null) {
            return false;
        }
        return getSecret(appKey) != null;
    }

}

