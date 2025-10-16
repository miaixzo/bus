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
package org.miaixz.bus.starter.sensitive;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.stream.Collectors;

import org.miaixz.bus.base.advice.BaseAdvice;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
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
 * A {@link org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice} implementation that handles the
 * decryption of incoming request bodies for methods or classes annotated with {@link Sensitive}. This advice is
 * specifically effective for {@code @RequestBody} annotated parameters.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RequestBodyAdvice extends BaseAdvice
        implements org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice {

    @Resource
    private SensitiveProperties properties;

    /**
     * Determines if this advice should be applied to the given method parameter.
     *
     * @param parameter     The method parameter.
     * @param targetType    The target type, not necessarily the same as the method parameter type.
     * @param converterType The selected converter type.
     * @return {@code true} if this advice should be applied, {@code false} otherwise.
     */
    @Override
    public boolean supports(
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        // Check for @Sensitive annotation on the class
        if (parameter.getDeclaringClass().isAnnotationPresent(Sensitive.class)) {
            return true;
        }
        // Check for @Sensitive annotation on the method
        return parameter.getMethod().isAnnotationPresent(Sensitive.class);
    }

    /**
     * Invoked before the request body is read and converted.
     * <p>
     * If decryption is enabled via the {@link Sensitive} annotation, this method wraps the original
     * {@link HttpInputMessage} with a custom implementation that decrypts the body on the fly.
     * </p>
     *
     * @param inputMessage  The HTTP input message.
     * @param parameter     The method parameter.
     * @param targetType    The target type for the conversion.
     * @param converterType The selected converter type.
     * @return The original or a new {@link HttpInputMessage} instance; never {@code null}.
     */
    @Override
    public HttpInputMessage beforeBodyRead(
            HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        if (ObjectKit.isNotEmpty(this.properties) && !this.properties.isDebug()) {
            try {
                final Sensitive sensitive = parameter.getMethodAnnotation(Sensitive.class);
                if (ObjectKit.isEmpty(sensitive)) {
                    return inputMessage;
                }

                // Decrypt the data if the stage is 'ALL' or 'IN'
                if ((Builder.ALL.equals(sensitive.value()) || Builder.SAFE.equals(sensitive.value()))
                        && (Builder.ALL.equals(sensitive.stage()) || Builder.IN.equals(sensitive.stage()))) {
                    return new InputMessage(inputMessage, this.properties.getDecrypt().getKey(),
                            this.properties.getDecrypt().getType(), Charset.DEFAULT_UTF_8);
                }
            } catch (Exception e) {
                Logger.error("Internal processing failure during request body decryption", e);
            }
        }
        return inputMessage;
    }

    /**
     * Invoked after the request body has been converted to an object.
     *
     * @param body          The converted object.
     * @param inputMessage  The HTTP input message.
     * @param parameter     The method parameter.
     * @param targetType    The target type for the conversion.
     * @param converterType The selected converter type.
     * @return The same body instance or a new one.
     */
    @Override
    public Object afterBodyRead(
            Object body,
            HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    /**
     * Invoked when the request body is empty.
     *
     * @param body          Usually {@code null} before this call.
     * @param inputMessage  The HTTP input message.
     * @param parameter     The method parameter.
     * @param targetType    The target type for the conversion.
     * @param converterType The selected converter type.
     * @return The value to use, or {@code null}, which may result in an {@code HttpMessageNotReadableException}.
     */
    @Override
    public Object handleEmptyBody(
            Object body,
            HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    /**
     * A custom {@link HttpInputMessage} that wraps the original message and provides a decrypted body stream.
     */
    class InputMessage implements HttpInputMessage {

        private final HttpHeaders headers;
        private final InputStream body;

        /**
         * Constructs a new InputMessage.
         *
         * @param inputMessage The original HTTP input message.
         * @param key          The decryption key.
         * @param type         The decryption algorithm type.
         * @param charset      The character set of the body.
         * @throws Exception if an error occurs during decryption.
         */
        public InputMessage(HttpInputMessage inputMessage, String key, String type, String charset) throws Exception {
            if (StringKit.isEmpty(key)) {
                throw new IllegalArgumentException(
                        "Decryption key is missing. Please check the 'bus.sensitive.decrypt.key' property.");
            }

            this.headers = inputMessage.getHeaders();

            String content = IoKit.toUtf8Reader(inputMessage.getBody()).lines()
                    .collect(Collectors.joining(System.lineSeparator()));

            String decryptBody;
            if (content.startsWith(Symbol.BRACE_LEFT)) {
                // Assume it's a plain JSON object, not encrypted.
                decryptBody = content;
            } else {
                // The content is expected to be encrypted.
                StringBuilder json = new StringBuilder();
                content = content.replaceAll(Symbol.SPACE, Symbol.PLUS);

                if (StringKit.isNotEmpty(content)) {
                    Logger.debug("Request data decryption enabled...");
                    // The content might be split by a delimiter if it was encrypted in chunks.
                    String[] contents = content.split("\\|");
                    for (String encryptedPart : contents) {
                        json.append(org.miaixz.bus.crypto.Builder.decrypt(type, key, encryptedPart, Charset.UTF_8));
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
