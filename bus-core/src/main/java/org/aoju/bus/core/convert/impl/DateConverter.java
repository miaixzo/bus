package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.date.DateTime;
import org.aoju.bus.core.utils.DateUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * 日期转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class DateConverter extends AbstractConverter<Date> {

    private Class<? extends Date> targetType;
    /**
     * 日期格式化
     */
    private String format;

    /**
     * 构造
     *
     * @param targetType 目标类型
     */
    public DateConverter(Class<? extends Date> targetType) {
        this.targetType = targetType;
    }

    /**
     * 构造
     *
     * @param targetType 目标类型
     * @param format     日期格式
     */
    public DateConverter(Class<? extends Date> targetType, String format) {
        this.targetType = targetType;
        this.format = format;
    }

    /**
     * 获取日期格式
     *
     * @return 设置日期格式
     */
    public String getFormat() {
        return format;
    }

    /**
     * 设置日期格式
     *
     * @param format 日期格式
     */
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    protected Date convertInternal(Object value) {
        long mills = -1;
        if (value instanceof Calendar) {
            // Handle Calendar
            mills = ((Calendar) value).getTimeInMillis();
        } else if (value instanceof Long) {
            // Handle Long
            // 此处使用自动拆装箱
            mills = (Long) value;
        } else {
            // 统一按照字符串处理
            final String valueStr = convertToStr(value);
            try {
                mills = StringUtils.isBlank(format) ? DateUtils.parse(valueStr).getTime() : DateUtils.parse(valueStr, format).getTime();
            } catch (Exception e) {
                // Ignore Exception
            }
        }

        if (mills < 0) {
            return null;
        }

        // 返回指定类型
        if (Date.class == targetType) {
            return new Date(mills);
        }
        if (DateTime.class == targetType) {
            return new DateTime(mills);
        } else if (java.sql.Date.class == targetType) {
            return new java.sql.Date(mills);
        } else if (java.sql.Time.class == targetType) {
            return new java.sql.Time(mills);
        } else if (java.sql.Timestamp.class == targetType) {
            return new java.sql.Timestamp(mills);
        }

        throw new UnsupportedOperationException(StringUtils.format("Unsupport Date type: {}", this.targetType.getName()));
    }

}
