/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * A helper component for retrieving internationalized (i18n) messages from property files. This class simplifies access
 * to the Spring {@link MessageSource} by using the current locale from {@link LocaleContextHolder}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
public class I18nMessage {

    private final MessageSource messageSource;

    /**
     * Constructs a new {@code I18nMessage} with the given {@link MessageSource}.
     *
     * @param messageSource The Spring message source to be used for resolving messages.
     */
    public I18nMessage(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Retrieves a message for the given code, using the current locale.
     *
     * @param code The message code, corresponding to a key in the message properties file.
     * @return The resolved message as a {@code String}.
     */
    public String getMessage(String code) {
        return getMessage(code, null);
    }

    /**
     * Retrieves a message for the given code and arguments, using the current locale.
     *
     * @param code The message code, corresponding to a key in the message properties file.
     * @param args An array of arguments to be filled into the message template.
     * @return The resolved and formatted message as a {@code String}.
     */
    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, null);
    }

    /**
     * Retrieves a message for the given code and arguments, with a default message, using the current locale.
     *
     * @param code           The message code, corresponding to a key in the message properties file.
     * @param args           An array of arguments to be filled into the message template.
     * @param defaultMessage The default message to return if the specified code is not found.
     * @return The resolved and formatted message, or the default message if the code is not found.
     */
    public String getMessage(String code, Object[] args, String defaultMessage) {
        // Use a convenient method that does not rely on the request.
        return messageSource.getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }

}
