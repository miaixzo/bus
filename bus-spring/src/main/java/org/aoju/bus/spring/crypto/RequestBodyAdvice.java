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

import org.aoju.bus.base.spring.BaseAdvice;
import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.crypto.CryptoUtils;
import org.aoju.bus.crypto.annotation.DecryptBody;
import org.aoju.bus.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.HttpMessageConverter;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * 请求请求处理类（目前仅仅对requestbody有效）
 * 对加了@Decrypt的方法的数据进行解密密操作
 *
 * @author Kimi Liu
 * @version 3.1.2
 * @since JDK 1.8
 */
public class RequestBodyAdvice extends BaseAdvice
        implements org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice {

    @Autowired
    private CryptoProperties cryptoProperties;


    /**
     * Invoked first to determine if this intercept applies.
     *
     * @param parameter     the method parameter
     * @param type          the target type, not necessarily the same as the method
     *                      parameter type, e.g. for {@code HttpEntity<String>}.
     * @param converterType the selected converter type
     * @return whether this intercept should be invoked or not
     */
    @Override
    public boolean supports(MethodParameter parameter,
                            Type type,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        Annotation[] annotations = parameter.getDeclaringClass().getAnnotations();
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof DecryptBody) {
                    return true;
                }
            }
        }
        return parameter.getMethod().isAnnotationPresent(DecryptBody.class);
    }

    /**
     * Invoked second before the request body is read and converted.
     *
     * @param inputMessage  the request
     * @param parameter     the target method parameter
     * @param type          the target type, not necessarily the same as the method
     *                      parameter type, e.g. for {@code HttpEntity<String>}.
     * @param converterType the converter used to deserialize the body
     * @return the input request or a new instance, never {@code null}
     */
    @Override
    public org.springframework.http.HttpInputMessage beforeBodyRead(org.springframework.http.HttpInputMessage inputMessage,
                                                                    MethodParameter parameter,
                                                                    Type type,
                                                                    Class<? extends HttpMessageConverter<?>> converterType) {
        if (!cryptoProperties.isDebug()) {
            try {
                final DecryptBody decrypt = parameter.getMethod().getAnnotation(DecryptBody.class);
                if (ObjectUtils.isNotNull(decrypt)) {
                    final String key = StringUtils.defaultString(decrypt.key(), cryptoProperties.getDecrypt().getKey());
                    return new HttpInputMessage(inputMessage, key, decrypt.type(), Charset.DEFAULT_UTF_8);
                }
            } catch (Exception e) {
                Logger.error("数据解密失败", e);
            }
        }
        return inputMessage;
    }

    /**
     * Invoked third (and last) after the request body is converted to an Object.
     *
     * @param body          set to the converter Object before the first advice is called
     * @param inputMessage  the request
     * @param parameter     the target method parameter
     * @param targetType    the target type, not necessarily the same as the method
     *                      parameter type, e.g. for {@code HttpEntity<String>}.
     * @param converterType the converter used to deserialize the body
     * @return the same body or a new instance
     */
    @Override
    public Object afterBodyRead(Object body, org.springframework.http.HttpInputMessage inputMessage,
                                MethodParameter parameter,
                                Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    /**
     * Invoked second (and last) if the body is empty.
     *
     * @param body          usually set to {@code null} before the first advice is called
     * @param inputMessage  the request
     * @param parameter     the method parameter
     * @param type          the target type, not necessarily the same as the method
     *                      parameter type, e.g. for {@code HttpEntity<String>}.
     * @param converterType the selected converter type
     * @return the value to use or {@code null} which may then raise an
     * {@code HttpMessageNotReadableException} if the argument is required.
     */
    @Override
    public Object handleEmptyBody(Object body, org.springframework.http.HttpInputMessage inputMessage,
                                  MethodParameter parameter,
                                  Type type,
                                  Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    class HttpInputMessage implements org.springframework.http.HttpInputMessage {

        private HttpHeaders headers;
        private InputStream body;

        public HttpInputMessage(org.springframework.http.HttpInputMessage inputMessage,
                                String key,
                                String mode,
                                String charset) throws Exception {
            if (StringUtils.isEmpty(key)) {
                throw new NullPointerException("请配置spring.decrypt.key参数");
            }

            //获取请求内容
            this.headers = inputMessage.getHeaders();
            String content = IoUtils.toString(inputMessage.getBody(), charset);
            //未加密数据不进行解密操作
            String decryptBody;
            if (content.startsWith("{")) {
                decryptBody = content;
            } else {
                StringBuilder json = new StringBuilder();
                content = content.replaceAll(" ", "+");

                if (!StringUtils.isEmpty(content)) {
                    String[] contents = content.split("\\|");
                    for (int k = 0; k < contents.length; k++) {
                        String value = contents[k];
                        value = new String(CryptoUtils.decrypt(mode, key, Base64.decode(value)), charset);
                        json.append(value);
                    }
                }
                decryptBody = json.toString();
            }
            this.body = IoUtils.toInputStream(decryptBody, charset);
        }

        @Override
        public InputStream getBody() {
            return body;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }

}
