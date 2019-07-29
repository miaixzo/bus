package org.aoju.bus.cron.pattern.parser;

/**
 * 年值处理
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class YearValueParser extends SimpleValueParser {

    public YearValueParser() {
        super(1970, 2099);
    }

}
