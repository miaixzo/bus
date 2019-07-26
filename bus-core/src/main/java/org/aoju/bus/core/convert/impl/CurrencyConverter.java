package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;

import java.util.Currency;

/**
 * 货币{@link Currency} 转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class CurrencyConverter extends AbstractConverter<Currency> {

    @Override
    protected Currency convertInternal(Object value) {
        return Currency.getInstance(value.toString());
    }

}
