package org.aoju.bus.cron.pattern.parser;

import org.aoju.bus.core.lang.exception.CommonException;

/**
 * 每月的几号值处理<br>
 * 每月最多31天，32和“L”都表示最后一天
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class DayOfMonthValueParser extends SimpleValueParser {

    public DayOfMonthValueParser() {
        super(1, 31);
    }

    @Override
    public int parse(String value) throws CommonException {
        if (value.equalsIgnoreCase("L") || value.equals("32")) {//每月最后一天
            return 32;
        } else {
            return super.parse(value);
        }
    }
}
