package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.StringUtils;

import java.util.Locale;

/**
 * {@link Locale}对象转换器
 * 只提供String转换支持
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public class LocaleConverter extends AbstractConverter<Locale> {

    @Override
    protected Locale convertInternal(Object value) {
        try {
            String str = convertToStr(value);
            if (StringUtils.isEmpty(str)) {
                return null;
            }

            final String[] items = str.split(Symbol.UNDERLINE);
            if (items.length == 1) {
                return new Locale(items[0]);
            }
            if (items.length == 2) {
                return new Locale(items[0], items[1]);
            }
            return new Locale(items[0], items[1], items[2]);
        } catch (Exception e) {
            // Ignore Exception
        }
        return null;
    }

}
