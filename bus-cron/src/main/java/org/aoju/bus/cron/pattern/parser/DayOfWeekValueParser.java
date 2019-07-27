package org.aoju.bus.cron.pattern.parser;

import org.aoju.bus.core.lang.exception.CommonException;

/**
 * 星期值处理<br>
 * 1表示星期一，2表示星期二，依次类推，0和7都可以表示星期日
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class DayOfWeekValueParser extends SimpleValueParser {

    /**
     * Weeks aliases.
     */
    private static final String[] ALIASES = {"sun", "mon", "tue", "wed", "thu", "fri", "sat"};

    public DayOfWeekValueParser() {
        super(0, 7);
    }

    /**
     * 对于星期提供转换<br>
     * 1表示星期一，2表示星期二，依次类推，0和7都可以表示星期日
     */
    @Override
    public int parse(String value) throws CommonException {
        try {
            return super.parse(value) % 7;
        } catch (Exception e) {
            return parseAlias(value);
        }
    }

    /**
     * 解析别名
     *
     * @param value 别名值
     * @return 月份int值
     * @throws CommonException
     */
    private int parseAlias(String value) throws CommonException {
        if (value.equalsIgnoreCase("L")) {
            //最后一天为星期六
            return ALIASES.length - 1;
        }

        for (int i = 0; i < ALIASES.length; i++) {
            if (ALIASES[i].equalsIgnoreCase(value)) {
                return i;
            }
        }
        throw new CommonException("Invalid month alias: {}", value);
    }
}
