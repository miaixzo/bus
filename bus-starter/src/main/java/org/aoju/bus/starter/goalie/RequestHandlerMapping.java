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
package org.aoju.bus.starter.goalie;

import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.starter.goalie.annotation.ApiVersion;
import org.aoju.bus.starter.goalie.annotation.ClientVersion;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8+
 */
public class RequestHandlerMapping extends RequestMappingHandlerMapping {

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo mappinginfo = super.getMappingForMethod(method, handlerType);
        if (mappinginfo != null) {
            RequestMappingInfo apiVersionMappingInfo = getApiVersionMappingInfo(method, handlerType);
            return apiVersionMappingInfo == null ? mappinginfo : apiVersionMappingInfo.combine(mappinginfo);
        }
        return mappinginfo;
    }

    @Override
    protected RequestCondition getCustomTypeCondition(Class<?> handlerType) {
        ClientVersion clientVersion = AnnotatedElementUtils.findMergedAnnotation(handlerType, ClientVersion.class);
        return createRequestCondtion(clientVersion);
    }


    /**
     * 重新定义ClientVersion的条件匹配
     *
     * @param method 方法信息
     * @return 匹配信息
     */
    @Override
    protected RequestCondition getCustomMethodCondition(Method method) {
        ClientVersion clientVersion = AnnotatedElementUtils.findMergedAnnotation(method, ClientVersion.class);
        return createRequestCondtion(clientVersion);
    }

    private RequestCondition createRequestCondtion(ClientVersion clientVersion) {
        if (clientVersion == null) {
            return null;
        }
        if (clientVersion.value() != null && clientVersion.value().length > 0) {
            return new RequestConditions(clientVersion.value());
        }
        if (clientVersion.expression() != null && clientVersion.expression().length > 0) {
            return new RequestConditions(clientVersion.expression());
        }
        return null;
    }

    private RequestMappingInfo getApiVersionMappingInfo(Method method, Class<?> handlerType) {
        ApiVersion apiVersion = AnnotatedElementUtils.findMergedAnnotation(method, ApiVersion.class);
        if (apiVersion == null || StringKit.isBlank(apiVersion.value())) {
            apiVersion = AnnotatedElementUtils.findMergedAnnotation(handlerType, ApiVersion.class);
        }
        return apiVersion == null || StringKit.isBlank(apiVersion.value()) ? null : RequestMappingInfo
                .paths(apiVersion.value())
                .build();
    }

}
