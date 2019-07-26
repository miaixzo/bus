package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;

import java.util.UUID;

/**
 * UUID对象转换器转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class UUIDConverter extends AbstractConverter<UUID> {

    @Override
    protected UUID convertInternal(Object value) {
        return UUID.fromString(convertToStr(value));
    }

}
