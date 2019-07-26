package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;

import java.util.TimeZone;

/**
 * TimeZone转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class TimeZoneConverter extends AbstractConverter<TimeZone> {

    @Override
    protected TimeZone convertInternal(Object value) {
        return TimeZone.getTimeZone(convertToStr(value));
    }

}
