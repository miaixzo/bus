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

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.ibatis.type.JdbcType;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * SQL column type descriptor.
 *
 * @author Kimi Liu
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
     * Capacity unit supplied by the mapping or metadata reader; UNKNOWN never authorizes byte/character conversion.
     */
    private LengthUnit lengthUnit = LengthUnit.UNKNOWN;

    /**
     * Constructs a new SqlTypeDescriptor instance.
     */
    public SqlTypeDescriptor() {
        // No initialization required.
    }

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
        return normalized.contains("DECIMAL") || normalized.contains("NUMERIC") || "NUMBER".equals(normalized);
    }

    /**
     * Normalizes database-specific type aliases.
     *
     * @param value the source SQL type name
     * @return the normalized SQL type name
     */
    public static String normalizeTypeName(String value) {
        if (value == null || value.isBlank()) {
            return Normal.EMPTY;
        }
        String type = StringKit.toUpperCase(value.trim());
        return switch (type) {
            case "INT4" -> "INTEGER";
            case "INT8" -> "BIGINT";
            case "BOOL" -> "BOOLEAN";
            case "CHARACTER VARYING", "VARCHAR2" -> "VARCHAR";
            case "NUMBER", "DECIMAL" -> "NUMERIC";
            case "CHARACTER" -> "CHAR";
            case "CHARACTER LARGE OBJECT" -> "CLOB";
            case "BINARY LARGE OBJECT" -> "BLOB";
            case "DOUBLE PRECISION" -> "DOUBLE";
            default -> type;
        };
    }

    /**
     * Builds the native SQL type definition without using comparison aliases. An explicit native definition takes
     * precedence; otherwise declared length units, precision, and a zero scale are retained where supported.
     *
     * @return the SQL type definition
     */
    public String definition() {
        if (nativeDefinition != null && !nativeDefinition.isBlank()) {
            return nativeDefinition;
        }
        String normalized = typeName == null ? Normal.EMPTY : typeName.trim();
        if (length != null && length > 0 && supportsLength(normalized)) {
            String unit = ("VARCHAR2".equalsIgnoreCase(normalized) || "CHAR".equalsIgnoreCase(normalized))
                    ? lengthUnit == LengthUnit.CHARACTER ? " CHAR"
                            : lengthUnit == LengthUnit.BYTE ? " BYTE" : Normal.EMPTY
                    : Normal.EMPTY;
            return normalized + Symbol.PARENTHESE_LEFT + length + unit + Symbol.PARENTHESE_RIGHT;
        }
        if (precision != null && precision > 0 && supportsPrecision(normalized)) {
            if (scale != null && scale >= 0) {
                return normalized + Symbol.PARENTHESE_LEFT + precision + Symbol.COMMA + scale + Symbol.PARENTHESE_RIGHT;
            }
            return normalized + Symbol.PARENTHESE_LEFT + precision + Symbol.PARENTHESE_RIGHT;
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
     * Tests whether this descriptor has matching normalized type, capacity, precision, and scale values. A capacity
     * with an unknown unit cannot establish equivalence; dialect-specific comparison belongs to SchemaBehavior.
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
        return Objects.equals(length, other.length)
                && (length == null || lengthUnit != LengthUnit.UNKNOWN && lengthUnit == other.lengthUnit)
                && Objects.equals(precision, other.precision) && Objects.equals(scale, other.scale);
    }

    /**
     * Unit used to interpret a declared or observed capacity, rather than a database column default.
     */
    public enum LengthUnit {
        /**
         * The capacity unit is unavailable or cannot be established safely.
         */
        UNKNOWN,

        /**
         * Capacity counts characters.
         */
        CHARACTER,

        /**
         * Capacity counts bytes.
         */
        BYTE
    }

    /**
     * Comparison result that keeps insufficient evidence distinct from a confirmed difference.
     */
    public enum Comparison {
        /**
         * Available facts establish equivalence for the compared dimension.
         */
        SAME,

        /**
         * Available facts establish a difference for the compared dimension.
         */
        DIFFERENT,

        /**
         * Available facts do not establish either equivalence or a difference.
         */
        UNKNOWN
    }

}
