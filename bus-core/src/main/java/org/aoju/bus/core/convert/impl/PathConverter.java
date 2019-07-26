package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 字符串转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class PathConverter extends AbstractConverter<Path> {

    @Override
    protected Path convertInternal(Object value) {
        try {
            if (value instanceof URI) {
                return Paths.get((URI) value);
            }

            if (value instanceof URL) {
                return Paths.get(((URL) value).toURI());
            }

            if (value instanceof File) {
                return ((File) value).toPath();
            }

            return Paths.get(convertToStr(value));
        } catch (Exception e) {
            // Ignore Exception
        }
        return null;
    }

}
