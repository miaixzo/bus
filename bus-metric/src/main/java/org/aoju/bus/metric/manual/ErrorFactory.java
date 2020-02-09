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
package org.aoju.bus.metric.manual;

import org.aoju.bus.logger.Logger;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 负责构建错误消息
 *
 * @author Kimi Liu
 * @version 5.5.6
 * @since JDK 1.8++
 */
public class ErrorFactory {

    private static final String I18N_OPEN_ERROR = "i18n/open/error";
    private static Set<String> noModuleCache = new HashSet<>();
    /**
     * 错误信息的国际化信息
     */
    private static MessageSourceAccessor errorMessageSourceAccessor;

    public static void initMessageSource(List<String> isvModules) {
        HashSet<String> baseNamesSet = new HashSet<>();
        baseNamesSet.add(I18N_OPEN_ERROR);

        if (!isvModules.isEmpty()) {
            baseNamesSet.addAll(isvModules);
        }

        String[] totalBaseNames = baseNamesSet.toArray(new String[0]);

        Logger.info("加载错误码国际化资源：{}", StringUtils.arrayToCommaDelimitedString(totalBaseNames));

        ResourceBundleMessageSource bundleMessageSource = new ResourceBundleMessageSource();
        bundleMessageSource.setBasenames(totalBaseNames);
        MessageSourceAccessor messageSourceAccessor = new MessageSourceAccessor(bundleMessageSource);
        setErrorMessageSourceAccessor(messageSourceAccessor);
    }

    /**
     * 通过ErrorMeta，Locale，params构建国际化错误消息
     *
     * @param errorMeta 错误信息
     * @param locale    本地化
     * @param params    参数
     * @return 如果没有配置国际化消息，则直接返回errorMeta中的信息
     */
    public static Error<String> getError(ErrorMeta errorMeta, Locale locale, Object... params) {
        Assert.notNull(locale, "未设置Locale");
        final String code = errorMeta.getCode();
        String errorMessage = getErrorMessage(errorMeta.getIsvModule() + code, locale, params);
        if (StringUtils.isEmpty(errorMessage)) {
            errorMessage = errorMeta.getMsg();
        }
        final String errorMsg = errorMessage;
        return new Error<String>() {
            @Override
            public String getMsg() {
                return errorMsg;
            }

            @Override
            public String getCode() {
                return code;
            }
        };
    }


    public static void setErrorMessageSourceAccessor(MessageSourceAccessor errorMessageSourceAccessor) {
        ErrorFactory.errorMessageSourceAccessor = errorMessageSourceAccessor;
    }

    /**
     * 返回本地化信息
     *
     * @param module 错误模块
     * @param locale 本地化
     * @param params 参数
     * @return 返回信息
     */
    public static String getErrorMessage(String module, Locale locale, Object... params) {
        if (noModuleCache.contains(module)) {
            return null;
        }
        try {
            return errorMessageSourceAccessor.getMessage(module, params, locale);
        } catch (Exception e) {
            noModuleCache.add(module);
            return null;
        }
    }

}
