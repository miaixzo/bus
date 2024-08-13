/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.starter.sensitive;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.stream.Collectors;

import org.miaixz.bus.base.advice.BaseAdvice;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.sensitive.Builder;
import org.miaixz.bus.sensitive.magic.annotation.Sensitive;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;

import jakarta.annotation.Resource;

/**
 * 请求请求处理类(目前仅仅对requestbody有效) 对加了@P的方法的数据进行解密密操作
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RequestBodyAdvice extends BaseAdvice
        implements org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice {

    @Resource
    SensitiveProperties properties;

    /**
     * 首次调用,以确定是否应用此拦截.
     *
     * @param parameter     方法参数
     * @param type          目标类型,不一定与方法相同 参数类型,例如 {@code HttpEntity<String>}.
     * @param converterType 转换器类型
     * @return true/false 是否应该调用此拦截
     */
    @Override
    public boolean supports(MethodParameter parameter, Type type,
            Class<? extends HttpMessageConverter<?>> converterType) {
        Annotation[] annotations = parameter.getDeclaringClass().getAnnotations();
        if (ArrayKit.isNotEmpty(annotations)) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Sensitive) {
                    return true;
                }
            }
        }
        return parameter.getMethod().isAnnotationPresent(Sensitive.class);
    }

    /**
     * 在读取和转换请求体之前调用.
     *
     * @param inputMessage  HTTP输入消息
     * @param parameter     方法参数
     * @param type          目标类型,不一定与方法相同 参数类型,例如 {@code HttpEntity<String>}.
     * @param converterType 转换器类型
     * @return 输入请求或新实例, 永远不会 {@code null}
     */
    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type type,
            Class<? extends HttpMessageConverter<?>> converterType) {
        if (ObjectKit.isNotEmpty(this.properties) && !this.properties.isDebug()) {
            try {
                final Sensitive sensitive = parameter.getMethod().getAnnotation(Sensitive.class);
                if (ObjectKit.isEmpty(sensitive)) {
                    return inputMessage;
                }

                // 数据解密
                if (Builder.ALL.equals(sensitive.value()) || Builder.SAFE.equals(sensitive.value())
                        && (Builder.ALL.equals(sensitive.stage()) || Builder.IN.equals(sensitive.stage()))) {
                    inputMessage = new InputMessage(inputMessage, this.properties.getDecrypt().getKey(),
                            this.properties.getDecrypt().getType(), Charset.DEFAULT_UTF_8);
                }
            } catch (Exception e) {
                Logger.error("Internal processing failure:" + e.getMessage());
            }
        }
        return inputMessage;
    }

    /**
     * 在请求体转换为对象之后调用第三个(也是最后一个).
     *
     * @param body          在调用第一个通知之前将其设置为转换器对象
     * @param inputMessage  HTTP输入消息
     * @param parameter     方法参数
     * @param type          目标类型,不一定与方法相同 参数类型,例如 {@code HttpEntity<String>}.
     * @param converterType 转换器类型
     * @return 相同的主体或新实例
     */
    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type type,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    /**
     * 如果主体为空,则调用第二个(也是最后一个).
     *
     * @param body          通常在调用第一个通知之前将其设置为{@code null}
     * @param inputMessage  HTTP输入消息
     * @param parameter     方法参数
     * @param type          目标类型,不一定与方法相同 参数类型,例如 {@code HttpEntity<String>}.
     * @param converterType 转换器类型
     * @return 要使用的值或{@code null},该值可能会引发{@code HttpMessageNotReadableException}.
     */
    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type type,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    class InputMessage implements HttpInputMessage {

        private HttpHeaders headers;
        private InputStream body;

        public InputMessage(HttpInputMessage inputMessage, String key, String type, String charset) throws Exception {
            if (StringKit.isEmpty(key)) {
                throw new NullPointerException("Please check the request.crypto.decrypt");
            }

            this.headers = inputMessage.getHeaders();

            String content = IoKit.toUtf8Reader(inputMessage.getBody()).lines()
                    .collect(Collectors.joining(System.lineSeparator()));

            String decryptBody;
            if (content.startsWith(Symbol.BRACE_LEFT)) {
                decryptBody = content;
            } else {
                StringBuilder json = new StringBuilder();
                content = content.replaceAll(Symbol.SPACE, Symbol.PLUS);

                if (!StringKit.isEmpty(content)) {
                    Logger.debug("Request data decryption enabled ...");
                    String[] contents = content.split("\\|");
                    for (int k = 0; k < contents.length; k++) {
                        json.append(org.miaixz.bus.crypto.Builder.decrypt(type, key, contents[k], Charset.UTF_8));
                    }
                }
                decryptBody = json.toString();
            }
            this.body = IoKit.toStream(decryptBody, Charset.parse(charset));
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
