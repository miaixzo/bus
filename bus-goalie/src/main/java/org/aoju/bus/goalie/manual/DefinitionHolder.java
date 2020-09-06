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
package org.aoju.bus.goalie.manual;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.goalie.ApiContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 负责存储定义好的接口信息
 *
 * @author Kimi Liu
 * @version 6.0.9
 * @since JDK 1.8++
 */
public class DefinitionHolder {

    /**
     * key:nameversion
     */
    private static Map<String, ApiDefinition> apiDefinitionMap = new ConcurrentHashMap<>(64);

    private static volatile String defaultVersion = null;

    public static void addApiDefinition(ApiDefinition apiDefinition) throws InstrumentException {
        String key = getKey(apiDefinition);
        boolean hasApi = apiDefinitionMap.containsKey(key);
        if (hasApi) {
            throw new InstrumentException("重复申明接口,name:" + apiDefinition.getName() + " ,version:"
                    + apiDefinition.getVersion() + ",method:" + apiDefinition.getMethod().getName());

        }
        apiDefinitionMap.put(key, apiDefinition);
    }

    /**
     * 获取全部接口
     *
     * @return 返回全部解开
     */
    public static List<Api> listAllApi() {
        Collection<ApiDefinition> allApi = apiDefinitionMap.values();
        List<Api> ret = new ArrayList<>(allApi.size());
        for (ApiDefinition apiDefinition : allApi) {
            Api api = new Api();
            api.setName(apiDefinition.getName());
            api.setVersion(apiDefinition.getVersion());
            api.setDescription(apiDefinition.getDescription());
            api.setModuleName(apiDefinition.getModuleName());
            api.setOrderIndex(apiDefinition.getOrderIndex());
            ret.add(api);
        }
        return ret;
    }

    public static Map<String, ApiDefinition> getApiDefinitionMap() {
        return apiDefinitionMap;
    }

    public static ApiDefinition getByParam(ApiParam param) {
        String key = getKey(param.fatchName(), param.fatchVersion());
        return apiDefinitionMap.get(key);
    }

    public static String getKey(ApiDefinition apiDefinition) {
        return getKey(apiDefinition.getName(), apiDefinition.getVersion());
    }

    public static String getKey(String name, String version) {
        if (version == null) {
            version = getDefaultVersion();
        }
        return name + version;
    }

    private static String getDefaultVersion() {
        if (defaultVersion == null) {
            synchronized (DefinitionHolder.class) {
                if (defaultVersion == null) {
                    defaultVersion = ApiContext.getConfig().getVersion();
                }
            }
        }
        return defaultVersion;
    }

    public static void setApiInfo(Api api) {
        ApiDefinition apiDefinition = apiDefinitionMap.get(api.getName() + api.getVersion());
        if (apiDefinition != null) {
            apiDefinition.setDescription(api.getDescription());
            apiDefinition.setModuleName(api.getModuleName());
            apiDefinition.setOrderIndex(api.getOrderIndex());
        }
    }

    public static void clear() {
        apiDefinitionMap.clear();
        defaultVersion = null;
    }

}
