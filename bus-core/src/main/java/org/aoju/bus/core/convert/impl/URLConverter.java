package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * URL对象转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class URLConverter extends AbstractConverter<URL> {

    @Override
    protected URL convertInternal(Object value) {
        try {
            if (value instanceof File) {
                return ((File) value).toURI().toURL();
            }

            if (value instanceof URI) {
                return ((URI) value).toURL();
            }
            return new URL(convertToStr(value));
        } catch (Exception e) {
            // Ignore Exception
        }
        return null;
    }

}
