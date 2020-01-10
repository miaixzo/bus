/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.metric.builtin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.Context;
import org.aoju.bus.metric.consts.MetricConsts;
import org.aoju.bus.metric.magic.Secret;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * appkey，secret文件管理
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8++
 */
public class LocalAppSecretManager implements AppSecretManager, ManagerInitializer {

    private static Map<String, Secret> secretMap = new ConcurrentHashMap<>(64);

    @Override
    public void loadLocal() {
        Logger.info("开始读取本地秘钥配置文件");
        String localFile = Context.getConfig().getLocalSecretConfigFile();
        String configJson = FileUtils.readString(new File(localFile), MetricConsts.UTF8);
        this.loadSecretCache(configJson);
        Logger.info("本地秘钥配置文件读取成功，路径：{}", localFile);
    }

    public void loadSecretCache(String json) {
        if (StringUtils.isEmpty(json)) {
            json = "[]";
        }
        secretMap.clear();
        JSONArray arr = JSON.parseArray(json);
        for (int i = 0; i < arr.size(); i++) {
            JSONObject jsonObj = arr.getJSONObject(i);
            Secret secret = jsonObj.toJavaObject(Secret.class);
            secretMap.put(secret.getAppKey(), secret);
        }
    }

    @Override
    public void addAppSecret(Map<String, String> appSecretStore) {
        throw new UnsupportedOperationException("无效操作， appSecretStore)");
    }

    @Override
    public String getSecret(String appKey) {
        if (secretMap.isEmpty()) {
            throw new RuntimeException("服务端尚未初始化秘钥");
        }
        Secret secret = secretMap.get(appKey);
        if (secret == null) {
            return null;
        }
        return secret.getSecret();
    }

    public Secret getSecretInfo(String appKey) {
        if (StringUtils.isEmpty(appKey)) {
            return null;
        }
        if (secretMap.isEmpty()) {
            throw new RuntimeException("服务端尚未初始化秘钥");
        }
        return secretMap.get(appKey);
    }

    @Override
    public boolean isValidAppKey(String appKey) {
        if (appKey == null) {
            return false;
        }
        return secretMap.containsKey(appKey);
    }

}
