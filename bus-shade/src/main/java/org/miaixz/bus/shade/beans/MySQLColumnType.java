/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
