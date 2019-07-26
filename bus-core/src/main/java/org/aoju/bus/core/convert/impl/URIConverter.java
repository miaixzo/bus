package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * URI对象转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class URIConverter extends AbstractConverter<URI> {

    @Override
    protected URI convertInternal(Object value) {
        try {
            if (value instanceof File) {
                return ((File) value).toURI();
            }

            if (value instanceof URL) {
                return ((URL) value).toURI();
            }
            return new URI(convertToStr(value));
        } catch (Exception e) {
            // Ignore Exception
        }
        return null;
    }

}
