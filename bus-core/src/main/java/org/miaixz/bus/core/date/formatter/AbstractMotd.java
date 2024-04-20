package org.miaixz.bus.core.date.formatter;

import java.io.Serializable;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 抽象日期基本信息类，包括日期格式、时区、本地化等信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractMotd implements DateMotd, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 匹配规则
     */
    protected final String pattern;
    /**
     * 时区
     */
    protected final TimeZone timeZone;
    /**
     * 语言环境
     */
    protected final Locale locale;

    /**
     * 构造，内部使用
     *
     * @param pattern  使用{@link java.text.SimpleDateFormat} 相同的日期格式
     * @param timeZone 非空时区{@link TimeZone}
     * @param locale   非空{@link Locale} 日期地理位置
     */
    protected AbstractMotd(final String pattern, final TimeZone timeZone, final Locale locale) {
        this.pattern = pattern;
        this.timeZone = timeZone;
        this.locale = locale;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof FastDatePrinter)) {
            return false;
        }
        final AbstractMotd other = (AbstractMotd) object;
        return pattern.equals(other.pattern) && timeZone.equals(other.timeZone) && locale.equals(other.locale);
    }

    @Override
    public int hashCode() {
        return pattern.hashCode() + 13 * (timeZone.hashCode() + 13 * locale.hashCode());
    }

    @Override
    public String toString() {
        return "AbstractMotd[" + pattern + "," + locale + "," + timeZone.getID() + "]";
    }

}
