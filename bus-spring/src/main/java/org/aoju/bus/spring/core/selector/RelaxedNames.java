/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.spring.core.selector;

import org.aoju.bus.core.utils.StringUtils;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author Kimi Liu
 * @version 3.5.1
 * @since JDK 1.8
 */
public final class RelaxedNames implements Iterable<String> {

    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("([^A-Z-])([A-Z])");

    private static final Pattern SEPARATED_TO_CAMEL_CASE_PATTERN = Pattern
            .compile("[_\\-.]");

    private final String name;

    private final Set<String> values = new LinkedHashSet<String>();

    /**
     * Create a new {@link RelaxedNames} instance.
     *
     * @param name the source name. For the maximum number of variations specify the name
     *             using dashed notation (e.g. {@literal my-property-name}
     */
    public RelaxedNames(String name) {
        this.name = (name != null ? name : "");
        initialize(RelaxedNames.this.name, this.values);
    }

    /**
     * Return a {@link RelaxedNames} for the given source camelCase source name.
     *
     * @param name the source name in camelCase
     * @return the relaxed names
     */
    public static RelaxedNames forCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        for (char c : name.toCharArray()) {
            result.append(Character.isUpperCase(c) && result.length() > 0
                    && result.charAt(result.length() - 1) != '-'
                    ? "-" + Character.toLowerCase(c) : c);
        }
        return new RelaxedNames(result.toString());
    }

    @Override
    public Iterator<String> iterator() {
        return this.values.iterator();
    }

    private void initialize(String name, Set<String> values) {
        if (values.contains(name)) {
            return;
        }
        for (Variation variation : Variation.values()) {
            for (Manipulation manipulation : Manipulation.values()) {
                String result = name;
                result = manipulation.apply(result);
                result = variation.apply(result);
                values.add(result);
                initialize(result, values);
            }
        }
    }

    /**
     * Name variations.
     */
    enum Variation {

        NONE {
            @Override
            public String apply(String value) {
                return value;
            }

        },

        LOWERCASE {
            @Override
            public String apply(String value) {
                return (value.isEmpty() ? value : value.toLowerCase(Locale.ENGLISH));
            }

        },

        UPPERCASE {
            @Override
            public String apply(String value) {
                return (value.isEmpty() ? value : value.toUpperCase(Locale.ENGLISH));
            }

        };

        public abstract String apply(String value);

    }

    /**
     * Name manipulations.
     */
    enum Manipulation {

        NONE {
            @Override
            public String apply(String value) {
                return value;
            }

        },

        HYPHEN_TO_UNDERSCORE {
            @Override
            public String apply(String value) {
                return (value.indexOf('-') != -1 ? value.replace('-', '_') : value);
            }

        },

        UNDERSCORE_TO_PERIOD {
            @Override
            public String apply(String value) {
                return (value.indexOf('_') != -1 ? value.replace('_', '.') : value);
            }

        },

        PERIOD_TO_UNDERSCORE {
            @Override
            public String apply(String value) {
                return (value.indexOf('.') != -1 ? value.replace('.', '_') : value);
            }

        },

        CAMELCASE_TO_UNDERSCORE {
            @Override
            public String apply(String value) {
                if (value.isEmpty()) {
                    return value;
                }
                Matcher matcher = CAMEL_CASE_PATTERN.matcher(value);
                if (!matcher.find()) {
                    return value;
                }
                matcher = matcher.reset();
                StringBuffer result = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(result, matcher.group(1) + '_'
                            + StringUtils.uncapitalize(matcher.group(2)));
                }
                matcher.appendTail(result);
                return result.toString();
            }

        },

        CAMELCASE_TO_HYPHEN {
            @Override
            public String apply(String value) {
                if (value.isEmpty()) {
                    return value;
                }
                Matcher matcher = CAMEL_CASE_PATTERN.matcher(value);
                if (!matcher.find()) {
                    return value;
                }
                matcher = matcher.reset();
                StringBuffer result = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(result, matcher.group(1) + '-'
                            + StringUtils.uncapitalize(matcher.group(2)));
                }
                matcher.appendTail(result);
                return result.toString();
            }

        },

        SEPARATED_TO_CAMELCASE {
            @Override
            public String apply(String value) {
                return separatedToCamelCase(value, false);
            }

        },

        CASE_INSENSITIVE_SEPARATED_TO_CAMELCASE {
            @Override
            public String apply(String value) {
                return separatedToCamelCase(value, true);
            }

        };

        private static final char[] SUFFIXES = new char[]{'_', '-', '.'};

        private static String separatedToCamelCase(String value,
                                                   boolean caseInsensitive) {
            if (value.isEmpty()) {
                return value;
            }
            StringBuilder builder = new StringBuilder();
            for (String field : SEPARATED_TO_CAMEL_CASE_PATTERN.split(value)) {
                field = (caseInsensitive ? field.toLowerCase(Locale.ENGLISH) : field);
                builder.append(
                        builder.length() != 0 ? StringUtils.capitalize(field) : field);
            }
            char lastChar = value.charAt(value.length() - 1);
            for (char suffix : SUFFIXES) {
                if (lastChar == suffix) {
                    builder.append(suffix);
                    break;
                }
            }
            return builder.toString();
        }

        public abstract String apply(String value);

    }

}
