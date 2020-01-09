/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.metric.oauth2;

import com.alibaba.fastjson.JSON;
import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.Context;
import org.aoju.bus.metric.consts.MetricConsts;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 权限管理
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8++
 */
public class ApiPermissionManager implements PermissionManager {

    /**
     * key:appKey， value:name+version
     */
    private static Map<String, List<String>> appKeyApiMap = new ConcurrentHashMap<>(64);

    @Override
    public synchronized boolean canVisit(String appKey, String name, String version) {
        // 能够访问的接口，里面是name+version
        List<String> clientApis = this.listClientApi(appKey);
        return clientApis.contains(name + version);
    }

    @Override
    public void loadLocal() {
        Logger.info("开始读取本地权限配置文件");
        String localFile = Context.getConfig().getLocalPermissionConfigFile();
        String configJson = FileUtils.readString(new File(localFile), MetricConsts.UTF8);
        this.loadPermissionCache(configJson);
        Logger.info("本地权限配置文件读取成功，路径：{}", localFile);
    }

    @Override
    public void loadPermissionConfig() {
    }

    @Override
    public synchronized void loadPermissionCache(String configJson) {
        if (StringUtils.isEmpty(configJson)) {
            configJson = "[]";
        }
        List<ApiInfo> list = JSON.parseArray(configJson, ApiInfo.class);
        appKeyApiMap.clear();
        for (ApiInfo apiInfo : list) {
            String appKey = apiInfo.getApp_key();
            List<String> nameVersionList = appKeyApiMap.get(appKey);
            if (nameVersionList == null) {
                nameVersionList = new ArrayList<>();
                appKeyApiMap.put(appKey, nameVersionList);
            }
            nameVersionList.add(apiInfo.getName() + apiInfo.getVersion());
        }
    }

    @Override
    public List<String> listClientApi(String appKey) {
        List<String> list = appKeyApiMap.get(appKey);
        if (list == null) {
            list = Collections.emptyList();
        }
        return list;
    }


}
