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
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.provider;

import org.miaixz.bus.core.Context;
import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.parsing.FieldMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Naming provider class based on {@link EnumValue.Naming}.
 *
 * <p>
 * This class provides naming conversion functionality directly using the core module's {@link EnumValue.Naming}
 * enumeration, eliminating code duplication and providing a single source of truth for naming conventions across the
 * framework.
 * </p>
 *
 * <p>
 * Supported naming strategies (from {@link EnumValue.Naming}):
 * </p>
 * <ul>
 * <li>NORMAL (0): No conversion</li>
 * <li>LOWER_CASE (5): Convert to lowercase</li>
 * <li>UPPER_CASE (4): Convert to uppercase</li>
 * <li>CAMEL (6): Convert to camelCase</li>
 * <li>CAMEL_UNDERLINE_LOWER_CASE (8): Convert to snake_case</li>
 * <li>CAMEL_UNDERLINE_UPPER_CASE (7): Convert to UPPER_SNAKE_CASE</li>
 * </ul>
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre>{@code
 * 
 * // Get naming provider instance
 * NamingProvider provider = NamingProvider.type("camel_underline_lower_case");
 *
 * // Transform names
 * String columnName = provider.transform("userName"); // "user_name"
 * String tableName = provider.tableName(UserEntity.class);
 * String column = provider.columnName(tableMeta, fieldMeta);
 *
 * // Access underlying enumeration
 * EnumValue.Naming naming = provider.getNaming();
 * long code = provider.getCode();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NamingProvider implements Provider {

    private final EnumValue.Naming naming;

    /**
     * Private constructor using specific naming convention.
     *
     * @param naming the naming convention to use
     */
    private NamingProvider(EnumValue.Naming naming) {
        this.naming = naming != null ? naming : EnumValue.Naming.getDefault();
    }

    /**
     * Gets the default naming style handler instance.
     *
     * @return The default naming style implementation.
     */
    public static NamingProvider getDefaultStyle() {
        return type(null);
    }

    /**
     * Gets a naming style handler instance by style name.
     *
     * @param style The name of the style. If null or empty, the global configuration or default style will be used.
     * @return The naming style implementation.
     * @throws IllegalArgumentException if the style name is invalid.
     */
    public static NamingProvider type(String style) {
        if (style == null || style.isEmpty()) {
            style = Context.INSTANCE.getProperty(
                    Args.PROVIDER_KEY + Symbol.DOT + Args.NAMING_KEY,
                    EnumValue.Naming.LOWER_SNAKE_CASE.name());
        }
        if (style == null || style.isEmpty()) {
            style = EnumValue.Naming.LOWER_SNAKE_CASE.name();
        }

        try {
            EnumValue.Naming naming = EnumValue.Naming.fromString(style);
            return new NamingProvider(naming);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("illegal style：" + style);
        }
    }

    /**
     * Creates a naming provider for a specific naming style.
     *
     * @param styleName the style name (e.g., "normal", "camel", "camel_underline_lower_case")
     * @return the naming provider instance
     */
    public static NamingProvider forStyle(String styleName) {
        EnumValue.Naming naming = EnumValue.Naming.fromString(styleName);
        return new NamingProvider(naming);
    }

    /**
     * Gets all available naming style types.
     *
     * @return set of available naming style names
     */
    public static java.util.Set<String> getAvailableStyles() {
        java.util.Set<String> styles = java.util.HashSet.newHashSet(EnumValue.Naming.values().length);
        for (EnumValue.Naming naming : EnumValue.Naming.values()) {
            styles.add(naming.name().toLowerCase());
        }
        return java.util.Collections.unmodifiableSet(styles);
    }

    /**
     * Transforms a name according to the naming convention.
     *
     * @param input the input name
     * @return the transformed name
     */
    public String transform(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return switch (naming) {
            case LOWER_CASE -> input.toLowerCase();
            case UPPER_CASE -> input.toUpperCase();
            case CAMEL_CASE -> StringKit.toCamelCase(input);
            case LOWER_SNAKE_CASE -> StringKit.toUnderlineCase(input);
            case UPPER_SNAKE_CASE -> StringKit.toUnderlineCase(input).toUpperCase();
            default -> input; // NORMAL, BOLD, FAINT, ITALIC - no transformation
        };
    }

    @Override
    public String type() {
        return naming.name().toLowerCase();
    }

    /**
     * Converts an entity class to a table name.
     *
     * @param entityClass The entity class.
     * @return The corresponding table name.
     */
    public String tableName(Class<?> entityClass) {
        if (entityClass == null) {
            return null;
        }
        return transform(entityClass.getSimpleName());
    }

    /**
     * Converts a field to a column name.
     *
     * @param entityTable The entity table information.
     * @param field       The entity field information.
     * @return The corresponding column name.
     */
    public String columnName(TableMeta entityTable, FieldMeta field) {
        if (field == null) {
            return null;
        }
        return transform(field.getName());
    }

    /**
     * Gets the underlying naming enumeration.
     *
     * @return the {@link EnumValue.Naming} instance
     */
    public EnumValue.Naming getNaming() {
        return naming;
    }

    /**
     * Gets the code value from the naming enumeration.
     *
     * @return the code value
     */
    public long getCode() {
        return naming.getCode();
    }

    /**
     * Gets the display name from the naming enumeration.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return naming.getName();
    }

}
