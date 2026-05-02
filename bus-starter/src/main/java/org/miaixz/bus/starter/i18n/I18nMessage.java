/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.i18n;

import java.text.MessageFormat;
import java.util.Locale;

import org.miaixz.bus.core.lang.I18n;
import org.miaixz.bus.core.lang.Symbol;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.AbstractMessageSource;

/**
 * Spring {@link MessageSource} backed by the core {@link I18n} resolver, with convenience accessors for application
 * code.
 * <p>
 * This class deliberately combines the Spring integration point and the application helper API into one bean. Spring
 * MVC, validation, and regular application code therefore resolve messages through the same path:
 * {@code MessageSource -> I18nMessage -> org.miaixz.bus.core.lang.I18n}.
 * </p>
 * <p>
 * The core module remains Spring-free; this starter-side adapter is the only place that bridges Spring's
 * {@link MessageSource} contract to the core {@link I18n} enum and its {@code ResourceBundle} lookup.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class I18nMessage extends AbstractMessageSource {

    /**
     * Starter-side configuration used when this instance owns message resolution.
     */
    private final I18nProperties properties;

    /**
     * Creates an i18n message source backed by core {@link I18n}.
     *
     * @param properties i18n configuration properties
     */
    public I18nMessage(I18nProperties properties) {
        this.properties = properties;
    }

    /**
     * Retrieves a message for the given code, using the current locale.
     *
     * @param code the message code
     * @return the resolved message
     */
    public String getMessage(String code) {
        return getMessage(code, null);
    }

    /**
     * Retrieves a message for the given code and arguments, using the current locale.
     *
     * @param code the message code
     * @param args the message arguments
     * @return the resolved message
     */
    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, (String) null);
    }

    /**
     * Retrieves a message for the given code and arguments, with a default message, using the current locale.
     *
     * @param code           the message code
     * @param args           the message arguments
     * @param defaultMessage the default message
     * @return the resolved message
     */
    public String getMessage(String code, Object[] args, String defaultMessage) {
        return getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String message = resolveMessage(code, locale);
        return message == null ? null : new MessageFormat(message, locale == null ? Locale.getDefault() : locale);
    }

    /**
     * Resolves a raw message pattern. Spring's {@link AbstractMessageSource} performs argument formatting after this
     * method returns a {@link MessageFormat}.
     */
    private String resolveMessage(String code, Locale locale) {
        if (code == null) {
            return null;
        }
        I18n i18n = toI18n(locale);
        for (String baseName : baseNames()) {
            if (baseName == null || baseName.isBlank()) {
                continue;
            }
            String candidate = I18n.message(i18n, baseName, code);
            if (!code.equals(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Falls back to the conventional {@code messages} bundle so {@code messages*.properties} works without extra
     * configuration.
     */
    private String[] baseNames() {
        String[] baseNames = this.properties.getBaseNames();
        return baseNames == null || baseNames.length == 0 ? new String[] { "messages" } : baseNames;
    }

    /**
     * Converts Spring/JDK {@link Locale} values to the closest core {@link I18n} enum entry.
     */
    private I18n toI18n(Locale locale) {
        if (locale == null) {
            return I18n.AUTO_DETECT;
        }
        String localeName = locale.toString();
        String languageTag = locale.toLanguageTag().replace(Symbol.C_MINUS, Symbol.C_UNDERLINE);
        for (I18n i18n : I18n.values()) {
            if (i18n.lang().equalsIgnoreCase(localeName) || i18n.lang().equalsIgnoreCase(languageTag)) {
                return i18n;
            }
        }
        for (I18n i18n : I18n.values()) {
            if (i18n.lang().equalsIgnoreCase(locale.getLanguage())) {
                return i18n;
            }
        }
        return I18n.AUTO_DETECT;
    }

}
