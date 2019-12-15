package org.aoju.bus.office;

import org.aoju.bus.office.builtin.OptionalSource;
import org.aoju.bus.office.magic.family.DocumentFormat;
import org.aoju.bus.office.magic.family.FormatRegistry;

import java.io.File;
import java.io.InputStream;

/**
 * 文档转换服务提供者.
 * 负责使用office管理器执行文档的转换.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface Provider {

    /**
     * 转换存储在本地文件系统上的源文件.
     *
     * @param source 转换输入作为一个文件.
     * @return 当前转换规范.
     */
    OptionalSource convert(File source);

    /**
     * 转换源流输入流.
     *
     * @param source 转换输入作为输入流.
     * @return 当前转换规范.
     */
    OptionalSource convert(InputStream source);

    /**
     * 转换源流输入流.
     *
     * @param source      转换输入作为输入流.
     * @param closeStream 是否在转换结束后关闭{@link InputStream}.
     * @return 当前转换规范.
     */
    OptionalSource convert(InputStream source, boolean closeStream);

    /**
     * 获取转换器支持的所有{@link DocumentFormat}.
     *
     * @return 包含支持格式的{@link FormatRegistry}.
     */
    FormatRegistry getFormatRegistry();

}
