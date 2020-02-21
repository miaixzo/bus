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
package org.aoju.bus.office.metric;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.logger.Logger;

import java.util.List;

/**
 * 包含有关正在使用的office的基本信息.
 *
 * @author Kimi Liu
 * @version 5.6.3
 * @since JDK 1.8+
 */
public final class OfficeOption {

    private String product = "???";
    private String version = "???";
    private boolean useLongOptionNameGnuStyle;

    private OfficeOption() {
    }

    /**
     * 使用help选项从命令行输出创建描述符.
     *
     * @param lines 执行的输出行.
     * @return 描述符.
     */
    public static OfficeOption fromHelpOutput(final List<String> lines) {
        final OfficeOption desc = new OfficeOption();

        Logger.debug("Building {} from help output lines", OfficeOption.class.getName());

        String productLine = null;
        for (final String line : lines) {
            if (line.contains("--help")) {
                desc.useLongOptionNameGnuStyle = true;
            } else {
                final String lowerLine = line.trim().toLowerCase();
                if (lowerLine.startsWith("openoffice") || lowerLine.startsWith("libreoffice")) {
                    productLine = line.trim();
                }
            }
        }

        if (productLine != null) {
            final String[] parts = productLine.split(Symbol.SPACE);
            if (parts.length > 0) {
                desc.product = parts[0];
            }
            if (parts.length > 1) {
                desc.version = parts[1];
            }
        }

        Logger.info("soffice info (from help output): {}", desc.toString());
        return desc;
    }

    /**
     * 从office安装路径创建描述符.
     *
     * @param path 安装路径.
     * @return 描述符.
     */
    public static OfficeOption fromExecutablePath(final String path) {
        final OfficeOption desc = new OfficeOption();

        if (path.toLowerCase().contains("openoffice")) {
            desc.product = "OpenOffice";
            desc.useLongOptionNameGnuStyle = false;
        }
        if (path.toLowerCase().contains("libreoffice")) {
            desc.product = "LibreOffice";
            desc.useLongOptionNameGnuStyle = true;
        }

        Logger.info("soffice info (from exec path): {}", desc.toString());
        return desc;
    }

    /**
     * 获取正在使用的office的产品名称.
     *
     * @return LibreOffice 或者 OpenOffice or ??? if 未知.
     */
    public String getProduct() {
        return product;
    }

    /**
     * 获取正在使用的office安装的版本.
     *
     * @return 版本信息或者未知.
     */
    public String getVersion() {
        return version;
    }

    /**
     * 获取在设置命令行选项以启动office实例时，是否必须使用单独的选项名GNU style(--)
     *
     * @return {@code true}使用单独的选项名GNU style，否则{@code false}.
     */
    public boolean useLongOptionNameGnuStyle() {
        return useLongOptionNameGnuStyle;
    }

    @Override
    public String toString() {
        return String.format(
                "Product: %s - Version: %s - useLongOptionNameGnuStyle: %s",
                getProduct(), getVersion(), useLongOptionNameGnuStyle());
    }

}
