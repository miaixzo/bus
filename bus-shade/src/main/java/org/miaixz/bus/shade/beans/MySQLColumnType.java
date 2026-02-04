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
package org.miaixz.bus.shade.beans;

/**
 * Represents various column types for MySQL, including primitive types, wrapper types, SQL-specific types, Java 8
 * date/time types, and other common types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum MySQLColumnType implements ColumnType {

    // Primitive types
    /**
     * Represents the primitive byte type.
     */
    BASE_BYTE("byte", null),
    /**
     * Represents the primitive short type.
     */
    BASE_SHORT("short", null),
    /**
     * Represents the primitive char type.
     */
    BASE_CHAR("char", null),
    /**
     * Represents the primitive int type.
     */
    BASE_INT("int", null),
    /**
     * Represents the primitive long type.
     */
    BASE_LONG("long", null),
    /**
     * Represents the primitive float type.
     */
    BASE_FLOAT("float", null),
    /**
     * Represents the primitive double type.
     */
    BASE_DOUBLE("double", null),
    /**
     * Represents the primitive boolean type.
     */
    BASE_BOOLEAN("boolean", null),

    // Wrapper types
    /**
     * Represents the Byte wrapper type.
     */
    BYTE("Byte", null),
    /**
     * Represents the Short wrapper type.
     */
    SHORT("Short", null),
    /**
     * Represents the Character wrapper type.
     */
    CHARACTER("Character", null),
    /**
     * Represents the Integer wrapper type.
     */
    INTEGER("Integer", null),
    /**
     * Represents the Long wrapper type.
     */
    LONG("Long", null),
    /**
     * Represents the Float wrapper type.
     */
    FLOAT("Float", null),
    /**
     * Represents the Double wrapper type.
     */
    DOUBLE("Double", null),
    /**
     * Represents the Boolean wrapper type.
     */
    BOOLEAN("Boolean", null),
    /**
     * Represents the String type.
     */
    STRING("String", null),

    // SQL package data types
    /**
     * Represents {@code java.sql.Date}.
     */
    DATE_SQL("Date", "java.sql.Date"),
    /**
     * Represents {@code java.sql.Time}.
     */
    TIME("Time", "java.sql.Time"),
    /**
     * Represents {@code java.sql.Timestamp}.
     */
    TIMESTAMP("Timestamp", "java.sql.Timestamp"),
    /**
     * Represents {@code java.sql.Blob}.
     */
    BLOB("Blob", "java.sql.Blob"),
    /**
     * Represents {@code java.sql.Clob}.
     */
    CLOB("Clob", "java.sql.Clob"),

    // Java 8 new time types
    /**
     * Represents {@code java.time.LocalDate}.
     */
    LOCAL_DATE("LocalDate", "java.time.LocalDate"),
    /**
     * Represents {@code java.time.LocalTime}.
     */
    LOCAL_TIME("LocalTime", "java.time.LocalTime"),
    /**
     * Represents {@code java.time.Year}.
     */
    YEAR("Year", "java.time.Year"),
    /**
     * Represents {@code java.time.YearMonth}.
     */
    YEAR_MONTH("YearMonth", "java.time.YearMonth"),
    /**
     * Represents {@code java.time.LocalDateTime}.
     */
    LOCAL_DATE_TIME("LocalDateTime", "java.time.LocalDateTime"),

    // Other miscellaneous types
    /**
     * Represents a byte array.
     */
    BYTE_ARRAY("byte[]", null),
    /**
     * Represents the Object type.
     */
    OBJECT("Object", null),
    /**
     * Represents {@code java.util.Date}.
     */
    DATE("Date", "java.util.Date"),
    /**
     * Represents {@code java.math.BigInteger}.
     */
    BIG_INTEGER("BigInteger", "java.math.BigInteger"),
    /**
     * Represents {@code java.math.BigDecimal}.
     */
    BIG_DECIMAL("BigDecimal", "java.math.BigDecimal");

    /**
     * The simple name of the type.
     */
    private final String type;

    /**
     * The fully qualified package path of the type, or {@code null} if it's a primitive or common type.
     */
    private final String pkg;

    /**
     * Constructs a {@code MySQLColumnType} enum constant.
     *
     * @param type The simple name of the type.
     * @param pkg  The fully qualified package path of the type, or {@code null}.
     */
    MySQLColumnType(final String type, final String pkg) {
        this.type = type;
        this.pkg = pkg;
    }

    /**
     * Retrieves the simple name of the column type.
     *
     * @return The simple name of the type.
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Retrieves the fully qualified package path of the column type.
     *
     * @return The fully qualified package path, or {@code null} if not applicable.
     */
    @Override
    public String getPkg() {
        return pkg;
    }

}
