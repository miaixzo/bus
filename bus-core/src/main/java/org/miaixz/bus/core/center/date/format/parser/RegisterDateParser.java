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
package org.miaixz.bus.core.center.date.format.parser;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.xyz.ListKit;

/**
 * A registered date parser that iterates through a list of registered parsers to find a suitable one and parse the
 * date. A default singleton instance {@link #INSTANCE} can be used, or custom parser instances can be created via the
 * constructor.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegisterDateParser implements DateParser, Serializable {

    @Serial
    private static final long serialVersionUID = 2852256978098L;

    /**
     * Singleton instance of {@code RegisterDateParser}.
     */
    public static final RegisterDateParser INSTANCE = new RegisterDateParser();

    /**
     * List of date parsers.
     */
    private final List<PredicateDateParser> list;

    /**
     * Constructs a {@code RegisterDateParser} instance, initializing with a default list of parsers.
     */
    public RegisterDateParser() {
        list = ListKit.of(
                // HH:mm:ss or HH:mm time format parser
                TimeParser.INSTANCE,
                // Default regex parser
                NormalDateParser.INSTANCE);
    }

    /**
     * Parses a date string.
     *
     * @param source The date string to parse.
     * @return The parsed date object.
     * @throws DateException if parsing fails.
     */
    @Override
    public Date parse(final CharSequence source) throws DateException {
        return list.stream().filter(predicateDateParser -> predicateDateParser.test(source)).findFirst()
                .map(predicateDateParser -> predicateDateParser.parse(source)).orElse(null);
    }

    /**
     * Registers a custom date parser. Custom parsers have higher priority than default parsers.
     *
     * @param parser The custom date parser.
     * @return This instance.
     */
    public RegisterDateParser register(final PredicateDateParser parser) {
        this.list.add(0, parser);
        return this;
    }

}
