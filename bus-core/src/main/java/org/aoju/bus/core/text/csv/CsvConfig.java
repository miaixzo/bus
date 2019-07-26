package org.aoju.bus.core.text.csv;

import org.aoju.bus.core.consts.Symbol;

import java.io.Serializable;

/**
 * CSV基础配置项
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CsvConfig implements Serializable {

    /**
     * 字段分隔符，默认逗号','
     */
    protected char fieldSeparator = Symbol.C_COMMA;
    /**
     * 文本分隔符，文本包装符，默认双引号'"'
     */
    protected char textDelimiter = Symbol.C_DOUBLE_QUOTES;

    /**
     * 设置字段分隔符，默认逗号','
     *
     * @param fieldSeparator 字段分隔符，默认逗号','
     */
    public void setFieldSeparator(final char fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    /**
     * 设置 文本分隔符，文本包装符，默认双引号'"'
     *
     * @param textDelimiter 文本分隔符，文本包装符，默认双引号'"'
     */
    public void setTextDelimiter(char textDelimiter) {
        this.textDelimiter = textDelimiter;
    }

}
