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
package org.aoju.bus.metric.magic;

import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.Config;
import org.aoju.bus.metric.Context;
import org.aoju.bus.metric.builtin.Errors;
import org.aoju.bus.metric.consts.MetricConsts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 负责结果输出
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8++
 */
public class ApiWriter implements Writer {

    private static Map<String, String> contentTypeMap = new HashMap<>();

    static {
        contentTypeMap.put(MetricConsts.FORMAT_JSON.toLowerCase(), MediaType.APPLICATION_JSON);
        contentTypeMap.put(MetricConsts.FORMAT_XML.toLowerCase(), MediaType.APPLICATION_XML);
    }

    private static String getContentType(String format) {
        return contentTypeMap.get(format.toLowerCase());
    }

    /**
     * 发送内容
     *
     * @param response    响应
     * @param contentType 媒体类型
     * @param text        内容
     */
    public static void doWriter(HttpServletResponse response, String contentType, String text) {
        response.setContentType(contentType);
        response.setCharacterEncoding(MetricConsts.UTF8);
        try {
            response.getWriter().write(text);
        } catch (IOException e) {
            Logger.error("doWriter", e);
        }
    }

    @Override
    public void write(HttpServletRequest request, HttpServletResponse response, Object result) {
        if (result == null) {
            return;
        }
        Config config = Context.getConfig();
        ApiParam param = Context.getApiParam();
        String format = param == null ? MetricConsts.FORMAT_JSON : param.fatchFormat();
        String returnText = "";

        // json格式输出
        if (MetricConsts.FORMAT_JSON.equalsIgnoreCase(format)) {
            returnText = config.getJsonResultSerializer().serialize(result);
        } else if (MetricConsts.FORMAT_XML.equalsIgnoreCase(format)) {
            // xml格式输出
            returnText = config.getXmlResultSerializer().serialize(result);
        } else {
            throw Errors.NO_FORMATTER.getException(format);
        }

        boolean isEncryptMode = Context.isEncryptMode() || Context.hasUseNewSSL(request);
        // 如果是加密模式,对结果加密
        if (isEncryptMode) {
            String randomKey = Context.getRandomKey();
            if (randomKey != null) {
                try {
                    String text = config.getSafety().aesEncryptToBase64String(returnText, randomKey);
                    this.writeText(response, text);
                } catch (Exception e) {
                    Logger.error(e.getMessage(), e);
                    this.writeText(response, e.getMessage());
                }
            } else {
                throw Errors.ERROR_SSL.getException();
            }
        } else {
            String contentType = getContentType(format);
            doWriter(response, contentType, returnText);
        }
    }

    public void writeText(HttpServletResponse response, String text) {
        doWriter(response, MediaType.TEXT_PLAIN, text);
    }

}
