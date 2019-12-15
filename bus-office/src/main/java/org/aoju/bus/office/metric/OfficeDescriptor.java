package org.aoju.bus.office.metric;

import org.aoju.bus.logger.Logger;

import java.util.List;

/**
 * 包含有关正在使用的office的基本信息.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public final class OfficeDescriptor {

    private String product = "???";
    private String version = "???";
    private boolean useLongOptionNameGnuStyle;

    private OfficeDescriptor() {
    }

    /**
     * 使用help选项从命令行输出创建描述符.
     *
     * @param lines 执行的输出行.
     * @return 描述符.
     */
    public static OfficeDescriptor fromHelpOutput(final List<String> lines) {
        final OfficeDescriptor desc = new OfficeDescriptor();

        Logger.debug("Building {} from help output lines", OfficeDescriptor.class.getName());

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
            final String[] parts = productLine.split(" ");
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
    public static OfficeDescriptor fromExecutablePath(final String path) {
        final OfficeDescriptor desc = new OfficeDescriptor();

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
