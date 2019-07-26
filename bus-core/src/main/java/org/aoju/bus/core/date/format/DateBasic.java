package org.aoju.bus.core.date.format;

import java.util.Locale;
import java.util.TimeZone;

/**
 * 日期基本信息获取接口
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface DateBasic {

    /**
     * 获得日期格式化或者转换的格式
     *
     * @return {@link java.text.SimpleDateFormat}兼容的格式
     */
    String getPattern();

    /**
     * 获得时区
     *
     * @return {@link TimeZone}
     */
    TimeZone getTimeZone();

    /**
     * 获得 日期地理位置
     *
     * @return {@link Locale}
     */
    Locale getLocale();

}
