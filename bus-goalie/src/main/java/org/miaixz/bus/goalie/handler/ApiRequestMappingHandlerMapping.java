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
package org.miaixz.bus.goalie.handler;

import java.lang.reflect.Method;

import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.goalie.annotation.ApiVersion;
import org.miaixz.bus.goalie.annotation.ClientVersion;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 自定义 API 请求映射处理器，扩展 Spring 的 RequestMappingHandlerMapping，支持 ApiVersion 和 ClientVersion 注解
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ApiRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    /**
     * 重写方法映射逻辑，添加对 ApiVersion 注解的支持
     *
     * @param method      请求处理方法
     * @param handlerType 处理器类型（通常为控制器类）
     * @return 合并后的 RequestMappingInfo，若无 ApiVersion 注解则返回原始映射信息
     */
    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo mappingInfo = super.getMappingForMethod(method, handlerType);
        if (null != mappingInfo) {
            RequestMappingInfo apiVersionMappingInfo = getApiVersionMappingInfo(method, handlerType);
            return null == apiVersionMappingInfo ? mappingInfo : apiVersionMappingInfo.combine(mappingInfo);
        }
        return mappingInfo;
    }

    /**
     * 获取类级别的自定义条件，基于 ClientVersion 注解
     *
     * @param handlerType 处理器类型
     * @return 基于 ClientVersion 的 RequestCondition，若无注解返回 null
     */
    @Override
    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
        ClientVersion clientVersion = AnnoKit.getAnnotation(handlerType, ClientVersion.class);
        return createRequestCondtion(clientVersion);
    }

    /**
     * 获取方法级别的自定义条件，基于 ClientVersion 注解
     *
     * @param method 请求处理方法
     * @return 基于 ClientVersion 的 RequestCondition，若无注解返回 null
     */
    @Override
    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        ClientVersion clientVersion = AnnoKit.getAnnotation(method, ClientVersion.class);
        return createRequestCondtion(clientVersion);
    }

    /**
     * 创建基于 ClientVersion 注解的请求条件
     *
     * @param clientVersion ClientVersion 注解实例
     * @return ApiVersionRequestCondition 实例，若注解为空或无有效值返回 null
     */
    private RequestCondition<?> createRequestCondtion(ClientVersion clientVersion) {
        if (null == clientVersion) {
            return null;
        }
        if (null != clientVersion.value() && clientVersion.value().length > 0) {
            return new ApiVersionRequestCondition(clientVersion.value());
        }
        if (null != clientVersion.expression() && clientVersion.expression().length > 0) {
            return new ApiVersionRequestCondition(clientVersion.expression());
        }
        return null;
    }

    /**
     * 获取 ApiVersion 注解的映射信息
     *
     * @param method      请求处理方法
     * @param handlerType 处理器类型
     * @return 包含 ApiVersion 路径的 RequestMappingInfo，若无有效注解返回 null
     */
    private RequestMappingInfo getApiVersionMappingInfo(Method method, Class<?> handlerType) {
        // 优先检查方法上的 ApiVersion 注解
        ApiVersion apiVersion = AnnoKit.getAnnotation(method, ApiVersion.class);
        if (null == apiVersion || StringKit.isBlank(apiVersion.value())) {
            // 若方法无有效注解，检查类上的 ApiVersion 注解
            apiVersion = AnnoKit.getAnnotation(handlerType, ApiVersion.class);
        }
        // 若仍无有效注解或值为空，返回 null，否则构建路径映射
        return null == apiVersion || StringKit.isBlank(apiVersion.value()) ? null
                : RequestMappingInfo.paths(apiVersion.value()).build();
    }

}