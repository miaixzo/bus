/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
