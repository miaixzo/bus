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
package org.aoju.bus.office.magic;

import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.Builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 实用程序函数，使office信息更容易获取.
 *
 * @author Kimi Liu
 * @version 5.9.5
 * @since JDK 1.8+
 */
public final class Info {

    /**
     * 获取指定上下文是否用于OpenOffice安装.
     *
     * @param context 上下文.
     * @return 如果指定的上下文用于OpenOffice安装，则为{@code true}，否则为{@code false}.
     * @throws InstrumentException 如果发生UNO异常。UNO异常将导致 {@link InstrumentException}.
     */
    public static boolean isOpenOffice(final XComponentContext context) {
        return "openoffice".equalsIgnoreCase(getOfficeName(context));
    }

    /**
     * 获取指定上下文是否用于LibreOffice安装.
     *
     * @param context 上下文.
     * @return 如果指定的上下文用于LibreOffice安装，则为{@code true}，否则为{@code false}.
     * @throws InstrumentException 如果发生UNO异常。UNO异常将导致 {@link InstrumentException}.
     */
    public static boolean isLibreOffice(final XComponentContext context) {
        return "libreoffice".equalsIgnoreCase(getOfficeName(context));
    }

    /**
     * 获取给定上下文的office产品名称.
     *
     * @param context 上下文.
     * @return 如果无法检索office产品名称，则为{@code null}.
     * @throws InstrumentException 如果发生UNO异常。UNO异常将导致 {@link InstrumentException}.
     */
    public static String getOfficeName(final XComponentContext context) {
        return getConfig(context, "ooName").orElse(null);
    }

    /**
     * 获取给定上下文的office产品版本(长版本号)，例如e.g 6.1.0.3
     *
     * @param context 上下文.
     * @return office产品版本，如果无法检索，则为{@code null}.
     * @throws InstrumentException 如果发生UNO异常。UNO异常将导致 {@link InstrumentException}.
     */
    public static String getOfficeVersionLong(final XComponentContext context) {
        return getConfig(context, "ooSetupVersionAboutBox").orElse(null);
    }

    /**
     * 获取给定上下文的office产品版本(短版本), e.g 6.1
     *
     * @param context The 上下文.
     * @return ffice产品版本，如果无法检索，则为{@code null}.
     * @throws InstrumentException 如果发生UNO异常。UNO异常将导致 {@link InstrumentException}.
     */
    public static String getOfficeVersionShort(final XComponentContext context) {
        return getConfig(context, "ooSetupVersion").orElse(null);
    }

    /**
     * 比较两个版本的字符串 (ex. 1.6.1).
     *
     * @param version1 第一个比较的版本.
     * @param version2 第二版比较.
     * @param length   用于标准化的版本长度.
     * @return -1 if version1 &lt; version2, 1 if version1 &gt; version2, 0 if version1 = version2.
     */
    public static int compareVersions(
            final String version1, final String version2, final int length) {

        if (StringKit.isEmpty(version1) && StringKit.isEmpty(version2)) {
            return 0;
        } else if (StringKit.isEmpty(version1)) {
            return -1;
        } else if (StringKit.isEmpty(version2)) {
            return 1;
        }

        final String[] numbers1 = normalizeVersion(version1, length).split("\\.");
        final String[] numbers2 = normalizeVersion(version2, length).split("\\.");

        for (int i = 0; i < numbers1.length; i++) {
            if (Integer.valueOf(numbers1[i]) < Integer.valueOf(numbers2[i])) {
                return -1;
            } else if (Integer.valueOf(numbers1[i]) > Integer.valueOf(numbers2[i])) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * 将版本字符串规范化 使其具有由'.'分隔的版本号
     *
     * @param version 版本号
     * @param length  长度
     */
    private static String normalizeVersion(final String version, final int length) {
        final List<String> numbers = new ArrayList<>(Arrays.asList(version.split("\\.")));
        while (numbers.size() < length) {
            numbers.add(Symbol.ZERO);
        }
        return numbers.stream().collect(Collectors.joining(Symbol.DOT));
    }

    /**
     * 获取指定属性的配置值.
     *
     * @param context  上下文信息.
     * @param propName 要获取的属性值的属性名.
     * @return 包含属性值的可选属性.
     * @throws InstrumentException 如果发生UNO异常。UNO异常将导致 {@link InstrumentException}.
     */
    public static Optional<String> getConfig(final XComponentContext context, final String propName) {
        for (String nodePath : Builder.NODE_PATHS) {
            final Optional<Object> info = getConfig(context, nodePath, propName);
            if (info.isPresent()) {
                return info.map(String.class::cast);
            }
        }
        return Optional.empty();
    }

    /**
     * 获取指定路径的指定属性的配置值.
     *
     * @param context  上下文信息.
     * @param nodePath 属性获取的路径.
     * @param propName 要获取的属性值的属性名.
     * @return 包含属性值的可选属性.
     * @throws InstrumentException 如果发生UNO异常。UNO异常将导致 {@link InstrumentException}.
     */
    public static Optional<Object> getConfig(
            final XComponentContext context, final String nodePath, final String propName) {
        return getConfigProperties(context, nodePath)
                .map(props -> Props.getProperty(props, propName))
                .orElse(Optional.empty());
    }

    /**
     * 获取指定路径的配置属性.
     *
     * @param context  上下文信息.
     * @param nodePath 属性获取的路径.
     * @return 可选的{@link XPropertySet}，包含指定路径的配置属性.
     */
    public static Optional<XPropertySet> getConfigProperties(
            final XComponentContext context, final String nodePath) {
        final XMultiServiceFactory provider =
                Lo.createInstanceMCF(
                        context,
                        XMultiServiceFactory.class,
                        "com.sun.star.configuration.ConfigurationProvider");
        if (provider == null) {
            Logger.debug("Could not create configuration provider");
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(
                    Lo.qi(
                            XPropertySet.class,
                            provider.createInstanceWithArguments(
                                    "com.sun.star.configuration.ConfigurationAccess",
                                    Props.makeProperties("nodepath", nodePath))));
        } catch (Exception ex) {
            Logger.debug("Unable to access config properties for: " + nodePath, ex);
        }

        return Optional.empty();
    }

    /**
     * 获取给定文档是否属于给定文档类型.
     *
     * @param document     文档.
     * @param documentType 要检查的文档类型.
     * @return 如果文档是指定的类型，则为{@code true}，否则为{@code true}.
     */
    public static boolean isDocumentType(final XComponent document, final String documentType) {
        return Lo.qi(XServiceInfo.class, document).supportsService(documentType);
    }

}
