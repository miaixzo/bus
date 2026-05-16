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
package org.miaixz.bus.mapper.feature.schema;

import java.util.Locale;
import java.util.Objects;

import org.apache.ibatis.type.JdbcType;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * SQL column type descriptor.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@Accessors(fluent = true)
public class SqlTypeDescriptor {

    /**
     * JDBC type associated with the SQL type.
     */
    private JdbcType jdbcType;

    /**
     * Database SQL type name.
     */
    private String typeName;

    /**
     * Character or binary length.
     */
    private Integer length;

    /**
     * Numeric precision.
     */
    private Integer precision;

    /**
     * Numeric scale.
     */
    private Integer scale;

    /**
     * Native SQL type definition supplied by entity metadata.
     */
    private String nativeDefinition;

    /**
     * Creates a SQL type descriptor for a type name.
     *
     * @param typeName the SQL type name
     * @return the SQL type descriptor
     */
    public static SqlTypeDescriptor of(String typeName) {
        return new SqlTypeDescriptor().typeName(typeName);
    }

    /**
     * Builds the normalized SQL type definition.
     *
     * @return the SQL type definition
     */
    public String definition() {
        if (nativeDefinition != null && !nativeDefinition.isBlank()) {
            return nativeDefinition;
        }
        String normalized = normalizeTypeName(typeName);
        if (length != null && length > 0 && supportsLength(normalized)) {
            return normalized + "(" + length + ")";
        }
        if (precision != null && precision > 0 && supportsPrecision(normalized)) {
            if (scale != null && scale >= 0) {
                return normalized + "(" + precision + "," + scale + ")";
            }
            return normalized + "(" + precision + ")";
        }
        return normalized;
    }

    /**
     * Gets the normalized SQL type name.
     *
     * @return the normalized SQL type name
     */
    public String normalizedTypeName() {
        return normalizeTypeName(typeName);
    }

    /**
     * Tests whether this descriptor is equivalent to another descriptor.
     *
     * @param other the descriptor to compare
     * @return {@code true} when the descriptors are equivalent
     */
    public boolean equivalent(SqlTypeDescriptor other) {
        if (other == null) {
            return false;
        }
        if (!Objects.equals(normalizedTypeName(), other.normalizedTypeName())) {
            return false;
        }
        return Objects.equals(length, other.length) && Objects.equals(precision, other.precision)
                && Objects.equals(scale, other.scale);
    }

    /**
     * Tests whether a SQL type supports a length parameter.
     *
     * @param type the normalized SQL type name
     * @return {@code true} when length is supported
     */
    public static boolean supportsLength(String type) {
        String normalized = normalizeTypeName(type);
        return normalized.contains("CHAR") || "VARCHAR".equals(normalized) || "CHAR".equals(normalized);
    }

    /**
     * Tests whether a SQL type supports precision and scale parameters.
     *
     * @param type the normalized SQL type name
     * @return {@code true} when precision is supported
     */
    public static boolean supportsPrecision(String type) {
        String normalized = normalizeTypeName(type);
        return normalized.contains("DECIMAL") || normalized.contains("NUMERIC");
    }

    /**
     * Normalizes database-specific type aliases.
     *
     * @param value the source SQL type name
     * @return the normalized SQL type name
     */
    public static String normalizeTypeName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String type = value.trim().toUpperCase(Locale.ROOT);
        return switch (type) {
            case "INT4" -> "INTEGER";
            case "INT8" -> "BIGINT";
            case "BOOL" -> "BOOLEAN";
            case "CHARACTER VARYING", "VARCHAR2" -> "VARCHAR";
            case "CHARACTER" -> "CHAR";
            case "DOUBLE PRECISION" -> "DOUBLE";
            default -> type;
        };
    }

}
