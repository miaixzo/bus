package org.aoju.bus.office.builtin;

import org.aoju.bus.office.magic.family.DocumentFormat;

/**
 * 具有尚未应用于转换器的可选源格式的转换作业.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface OptionalSource extends JobWithSource {

    /**
     * 定义给定输入文档的源文档格式.
     *
     * @param format 源文档的文档格式.
     * @return 当前转换规范.
     */
    JobWithSource as(DocumentFormat format);

}
