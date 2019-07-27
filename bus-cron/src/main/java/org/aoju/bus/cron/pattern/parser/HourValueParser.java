package org.aoju.bus.cron.pattern.parser;

/**
 * 小时值处理
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class HourValueParser extends SimpleValueParser {

    public HourValueParser() {
        super(0, 23);
    }

}
