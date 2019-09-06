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
package org.aoju.bus.spring.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aoju.bus.base.spring.BaseAdvice;
import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.crypto.CryptoUtils;
import org.aoju.bus.crypto.annotation.EncryptBody;
import org.aoju.bus.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.lang.annotation.Annotation;


/**
 * 请求响应处理类
 * 对加了@Encrypt的方法的数据进行加密操作
 *
 * @author Kimi Liu
 * @version 3.2.0
 * @since JDK 1.8
 */
public class ResponseBodyAdvice extends BaseAdvice
        implements org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice<Object> {

    @Autowired
    CryptoProperties cryptoProperties;

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        Annotation[] annotations = returnType.getDeclaringClass().getAnnotations();
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof EncryptBody) {
                    return true;
                }
            }
        }
        return returnType.getMethod().isAnnotationPresent(EncryptBody.class);
    }

    /**
     * 在选择{@code HttpMessageConverter}之后和之前调用 调用它的写方法
     *
     * @param body          需要操作的body
     * @param parameter     方法参数
     * @param mediaType     媒体类型
     * @param converterType 转换类型
     * @param request       当前 request
     * @param response      当前 response
     * @return 传入或修改(可能是新的)实例的主体
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter parameter, MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> converterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (!cryptoProperties.isDebug()) {
            try {
                final EncryptBody encrypt = parameter.getMethod().getAnnotation(EncryptBody.class);
                if (ObjectUtils.isNotNull(encrypt)) {
                    final String key = StringUtils.defaultString(encrypt.key(), cryptoProperties.getDecrypt().getKey());

                    if (!StringUtils.hasText(key)) {
                        throw new NullPointerException("请配置request.crypto.encrypt.key参数");
                    }
                    String content = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(body);
                    byte[] data = content.getBytes();
                    byte[] encodedData = CryptoUtils.encrypt(encrypt.type(), key, data);
                    return Base64.encode(encodedData);
                }
            } catch (Exception e) {
                Logger.error("加密数据异常:" + e.getMessage());
            }
        }
        return body;
    }

}
