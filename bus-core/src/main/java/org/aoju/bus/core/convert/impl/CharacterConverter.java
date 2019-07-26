package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.utils.BooleanUtils;
import org.aoju.bus.core.utils.StringUtils;

/**
 * 字符转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CharacterConverter extends AbstractConverter<Character> {

    @Override
    protected Character convertInternal(Object value) {
        if (char.class == value.getClass()) {
            return Character.valueOf((char) value);
        } else if (value instanceof Boolean) {
            return BooleanUtils.toCharacter((Boolean) value);
        } else if (boolean.class == value.getClass()) {
            return BooleanUtils.toCharacter((boolean) value);
        } else {
            final String valueStr = convertToStr(value);
            if (StringUtils.isNotBlank(valueStr)) {
                return Character.valueOf(valueStr.charAt(0));
            }
        }
        return null;
    }

}
