package org.aoju.bus.office.builtin;

import org.aoju.bus.office.magic.family.DocumentFormat;

/**
 * 具有所需目标格式但尚未应用到转换器的转换作业.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface RequiredTarget {

    /**
     * 定义给定输入文档的目标文档格式.
     *
     * @param format 目标文档的文档格式.
     * @return 当前转换规范.
     */
    ConvertJob as(DocumentFormat format);

}
