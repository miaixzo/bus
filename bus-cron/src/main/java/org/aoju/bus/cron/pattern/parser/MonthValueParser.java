package org.aoju.bus.cron.pattern.parser;

import org.aoju.bus.core.lang.exception.CommonException;

/**
 * 月份值处理
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class MonthValueParser extends SimpleValueParser {

    /**
     * Months aliases.
     */
    private static final String[] ALIASES = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

    public MonthValueParser() {
        super(1, 12);
    }

    @Override
    public int parse(String value) throws CommonException {
        try {
            return super.parse(value);
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
        for (int i = 0; i < ALIASES.length; i++) {
            if (ALIASES[i].equalsIgnoreCase(value)) {
                return i + 1;
            }
        }
        throw new CommonException("Invalid month alias: {}", value);
    }

}
