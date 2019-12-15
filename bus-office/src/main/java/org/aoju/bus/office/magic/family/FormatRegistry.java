package org.aoju.bus.office.magic.family;

import java.util.Set;

/**
 * 实现此接口的类应该保留office支持的文档格式集合.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface FormatRegistry {

    /**
     * 获取指定扩展名的文档格式.
     *
     * @param extension 将返回其文档格式的扩展名.
     * @return 如果指定的扩展不存在文档格式，则使用找到的文档格式，或者使用{@code null}.
     */
    DocumentFormat getFormatByExtension(String extension);

    /**
     * 获取指定媒体类型的文档格式.
     *
     * @param mediaType 将返回其文档格式的媒体类型.
     * @return 如果指定的媒体类型不存在文档格式，则使用找到的文档格式，或者使用{@code null}.
     */
    DocumentFormat getFormatByMediaType(String mediaType);

    /**
     * @param family 将返回其文档格式的集合.
     * @return 包含指定系列的所有文档格式的集合.
     */
    Set<DocumentFormat> getOutputFormats(FamilyType family);

}
