package org.aoju.bus.spring.crypto.advice;

import org.aoju.bus.base.spring.BaseAdvice;
import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.crypto.CryptoUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.spring.crypto.CryptoProperties;
import org.aoju.bus.spring.crypto.annotation.CryptoE;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;


/**
 * 请求响应处理类
 * 对加了@Encrypt的方法的数据进行加密操作
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ResponseBodyAdvice extends BaseAdvice
        implements org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice<Object> {

    @Autowired
    private CryptoProperties cryptoProperties;

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * Invoked after an {@code HttpMessageConverter} is selected and just before
     * its write method is invoked.
     *
     * @param body                  the body to be written
     * @param returnType            the return type of the controller method
     * @param selectedContentType   the content type selected through content negotiation
     * @param selectedConverterType the converter type selected to write to the response
     * @param request               the current request
     * @param response              the current response
     * @return the body that was passed in or a modified (possibly new) instance
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter parameter, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (!cryptoProperties.isDebug()) {
            try {
                final CryptoE encrypt = parameter.getMethod().getAnnotation(CryptoE.class);
                if (ObjectUtils.isNotNull(encrypt)) {
                    final String key = StringUtils.defaultString(encrypt.key(), cryptoProperties.getDecrypt().getKey());

                    if (!StringUtils.hasText(key)) {
                        throw new NullPointerException("请配置spring.encrypt.key参数");
                    }
                    String content = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(body);
                    byte[] data = content.getBytes();
                    byte[] encodedData = CryptoUtils.encrypt(encrypt.type(), key, data);
                    return Base64.encode(encodedData);
                }
            } catch (Exception e) {
                Logger.error("加密数据异常", e.getMessage());
            }
        }
        return body;
    }

}
