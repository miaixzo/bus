package org.aoju.bus.office.magic.family;

/**
 * 表示office支持的文档类型.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public enum FamilyType {

    /**
     * 文本文档 (odt, doc, docx, rtf, etc.)
     */
    TEXT,

    /**
     * 电子表格文件 (ods, xls, xlsx, csv, etc.)
     */
    SPREADSHEET,

    /**
     * 电子表格文件 (odp, ppt, pptx, etc.)
     */
    PRESENTATION,

    /**
     * 图像文件 (odg, png, svg, etc.)
     */
    DRAWING

}
