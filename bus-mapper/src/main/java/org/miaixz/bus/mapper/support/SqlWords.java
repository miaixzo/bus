/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.mapper.support;

import java.util.HashSet;
import java.util.Set;

/**
 * 关键字处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlWords {

    private static Set<String> RESERVED_WORDS;

    static {
        String[] words = { "A", "ABORT", "ABS", "ABSOLUTE", "ACCESS", "ACTION", "ADA", "ADD", // DB2
                "ADMIN", "AFTER", // DB2
                "AGGREGATE", "ALIAS", // DB2
                "ALL", // DB2
                "ALLOCATE", // DB2
                "ALLOW", // DB2
                "ALSO", "ALTER", // DB2
                "ALWAYS", "ANALYSE", "ANALYZE", "AND", // DB2
                "ANY", // DB2
                "APPLICATION", // DB2
                "ARE", "ARRAY", "AS", // DB2
                "ASC", "ASENSITIVE", "ASSERTION", "ASSIGNMENT", "ASSOCIATE", // DB2
                "ASUTIME", // DB2
                "ASYMMETRIC", "AT", "ATOMIC", "ATTRIBUTE", "ATTRIBUTES", "AUDIT", // DB2
                "AUTHORIZATION", // DB2
                "AUTO_INCREMENT", "AUX", // DB2
                "AUXILIARY", // DB2
                "AVG", "AVG_ROW_LENGTH", "BACKUP", "BACKWARD", "BEFORE", // DB2
                "BEGIN", // DB2
                "BERNOULLI", "BETWEEN", // DB2
                "BIGINT", "BINARY", // DB2
                "BIT", "BIT_LENGTH", "BITVAR", "BLOB", "BOOL", "BOOLEAN", "BOTH", "BREADTH", "BREAK", "BROWSE",
                "BUFFERPOOL", // DB2
                "BULK", "BY", // DB2
                "C", "CACHE", // DB2
                "CALL", // DB2
                "CALLED", // DB2
                "CAPTURE", // DB2
                "CARDINALITY", // DB2
                "CASCADE", "CASCADED", // DB2
                "CASE", // DB2
                "CAST", // DB2
                "CATALOG", "CATALOG_NAME", "CCSID", // DB2
                "CEIL", "CEILING", "CHAIN", "CHANGE", "CHAR", // DB2
                "CHAR_LENGTH", "CHARACTER", // DB2
                "CHARACTER_LENGTH", "CHARACTER_SET_CATALOG", "CHARACTER_SET_NAME", "CHARACTER_SET_SCHEMA",
                "CHARACTERISTICS", "CHARACTERS", "CHECK", // DB2
                "CHECKED", "CHECKPOINT", "CHECKSUM", "CLASS", "CLASS_ORIGIN", "CLOB", "CLOSE", // DB2
                "CLUSTER", // DB2
                "CLUSTERED", "COALESCE", "COBOL", "COLLATE", "COLLATION", "COLLATION_CATALOG", "COLLATION_NAME",
                "COLLATION_SCHEMA", "COLLECT", "COLLECTION", // DB2
                "COLLID", // DB2
                "COLUMN", // DB2
                "COLUMN_NAME", "COLUMNS", "COMMAND_FUNCTION", "COMMAND_FUNCTION_CODE", "COMMENT", // DB2
                "COMMIT", // DB2
                "COMMITTED", "COMPLETION", "COMPRESS", "COMPUTE", "CONCAT", // DB2
                "CONDITION", // DB2
                "CONDITION_NUMBER", "CONNECT", // DB2
                "CONNECTION", // DB2
                "CONNECTION_NAME", "CONSTRAINT", // DB2
                "CONSTRAINT_CATALOG", "CONSTRAINT_NAME", "CONSTRAINT_SCHEMA", "CONSTRAINTS", "CONSTRUCTOR", "CONTAINS", // DB2
                "CONTAINSTABLE", "CONTINUE", // DB2
                "CONVERSION", "CONVERT", "COPY", "CORR", "CORRESPONDING", "COUNT", // DB2
                "COUNT_BIG", // DB2
                "COVAR_POP", "COVAR_SAMP", "CREATE", // DB2
                "CREATEDB", "CREATEROLE", "CREATEUSER", "CROSS", // DB2
                "CSV", "CUBE", "CUME_DIST", "CURRENT", // DB2
                "CURRENT_DATE", // DB2
                "CURRENT_DEFAULT_TRANSFORM_GROUP", "CURRENT_LC_CTYPE", // DB2
                "CURRENT_PATH", // DB2
                "CURRENT_ROLE", "CURRENT_SERVER", // DB2
                "CURRENT_TIME", // DB2
                "CURRENT_TIMESTAMP", // DB2
                "CURRENT_TIMEZONE", // DB2
                "CURRENT_TRANSFORM_GROUP_FOR_TYPE", "CURRENT_USER", // DB2
                "CURSOR", // DB2
                "CURSOR_NAME", "CYCLE", // DB2
                "DATA", // DB2
                "DATABASE", // DB2
                "DATABASES", "DATE", "DATETIME", "DATETIME_INTERVAL_CODE", "DATETIME_INTERVAL_PRECISION", "DAY", // DB2
                "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DAYOFMONTH", "DAYOFWEEK", "DAYOFYEAR",
                "DAYS", // DB2
                "DB2GENERAL", // DB2
                "DB2GNRL", // DB2
                "DB2SQL", // DB2
                "DBCC", "DBINFO", // DB2
                "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", // DB2
                "DEFAULT", // DB2
                "DEFAULTS", // DB2
                "DEFERRABLE", "DEFERRED", "DEFINED", "DEFINER", "DEFINITION", // DB2
                "DEGREE", "DELAY_KEY_WRITE", "DELAYED", "DELETE", // DB2
                "DELIMITER", "DELIMITERS", "DENSE_RANK", "DENY", "DEPTH", "DEREF", "DERIVED", "DESC", "DESCRIBE",
                "DESCRIPTOR", // DB2
                "DESTROY", "DESTRUCTOR", "DETERMINISTIC", // DB2
                "DIAGNOSTICS", "DICTIONARY", "DISABLE", "DISALLOW", // DB2
                "DISCONNECT", // DB2
                "DISK", "DISPATCH", "DISTINCT", // DB2
                "DISTINCTROW", "DISTRIBUTED", "DIV", "DO", // DB2
                "DOMAIN", "DOUBLE", // DB2
                "DROP", // DB2
                "DSNHATTR", // DB2
                "DSSIZE", // DB2
                "DUAL", "DUMMY", "DUMP", "DYNAMIC", // DB2
                "DYNAMIC_FUNCTION", "DYNAMIC_FUNCTION_CODE", "EACH", // DB2
                "EDITPROC", // DB2
                "ELEMENT", "ELSE", // DB2
                "ELSEIF", // DB2
                "ENABLE", "ENCLOSED", "ENCODING", // DB2
                "ENCRYPTED", "END", // DB2
                "END-EXEC", // DB2
                "END-EXEC1", // DB2
                "ENUM", "EQUALS", "ERASE", // DB2
                "ERRLVL", "ESCAPE", // DB2
                "ESCAPED", "EVERY", "EXCEPT", // DB2
                "EXCEPTION", // DB2
                "EXCLUDE", "EXCLUDING", // DB2
                "EXCLUSIVE", "EXEC", "EXECUTE", // DB2
                "EXISTING", "EXISTS", // DB2
                "EXIT", // DB2
                "EXP", "EXPLAIN", "EXTERNAL", // DB2
                "EXTRACT", "FALSE", "FENCED", // DB2
                "FETCH", // DB2
                "FIELDPROC", // DB2
                "FIELDS", "FILE", // DB2
                "FILLFACTOR", "FILTER", "FINAL", // DB2
                "FIRST", "FLOAT", "FLOAT4", "FLOAT8", "FLOOR", "FLUSH", "FOLLOWING", "FOR", // DB2
                "FORCE", "FOREIGN", // DB2
                "FORTRAN", "FORWARD", "FOUND", "FREE", // DB2
                "FREETEXT", "FREETEXTTABLE", "FREEZE", "FROM", // DB2
                "FULL", // DB2
                "FULLTEXT", "FUNCTION", // DB2
                "FUSION", "G", "GENERAL", // DB2
                "GENERATED", // DB2
                "GET", // DB2
                "GLOBAL", // DB2
                "GO", // DB2
                "GOTO", // DB2
                "GRANT", // DB2
                "GRANTED", "GRANTS", "GRAPHIC", // DB2
                "GREATEST", "GROUP", // DB2
                "GROUPING", "HANDLER", // DB2
                "HAVING", // DB2
                "HEADER", "HEAP", "HIERARCHY", "HIGH_PRIORITY", "HOLD", // DB2
                "HOLDLOCK", "HOST", "HOSTS", "HOUR", // DB2
                "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", "HOURS", // DB2
                "IDENTIFIED", "IDENTITY", // DB2
                "IDENTITY_INSERT", "IDENTITYCOL", "IF", // DB2
                "IGNORE", "ILIKE", "IMMEDIATE", // DB2
                "IMMUTABLE", "IMPLEMENTATION", "IMPLICIT", "IN", // DB2
                "INCLUDE", "INCLUDING", // DB2
                "INCREMENT", // DB2
                "INDEX", // DB2
                "INDICATOR", // DB2
                "INFILE", "INFIX", "INHERIT", // DB2
                "INHERITS", "INITIAL", "INITIALIZE", "INITIALLY", "INNER", // DB2
                "INOUT", // DB2
                "INPUT", "INSENSITIVE", // DB2
                "INSERT", // DB2
                "INSERT_ID", "INSTANCE", "INSTANTIABLE", "INSTEAD", "INT", "INT1", "INT2", "INT3", "INT4", "INT8",
                "INTEGER", "INTEGRITY", // DB2
                "INTERSECT", "INTERSECTION", "INTERVAL", "INTO", // DB2
                "INVOKER", "IS", // DB2
                "ISAM", "ISNULL", "ISOBID", // DB2
                "ISOLATION", // DB2
                "ITERATE", // DB2
                "JAR", // DB2
                "JAVA", // DB2
                "JOIN", // DB2
                "K", "KEY", // DB2
                "KEY_MEMBER", "KEY_TYPE", "KEYS", "KILL", "LABEL", // DB2
                "LANCOMPILER", "LANGUAGE", // DB2
                "LARGE", "LAST", "LAST_INSERT_ID", "LATERAL", "LC_CTYPE", // DB2
                "LEADING", "LEAST", "LEAVE", // DB2
                "LEFT", // DB2
                "LENGTH", "LESS", "LEVEL", "LIKE", // DB2
                "LIMIT", "LINENO", "LINES", "LINKTYPE", // DB2
                "LISTEN", "LN", "LOAD", "LOCAL", // DB2
                "LOCALE", // DB2
                "LOCALTIME", "LOCALTIMESTAMP", "LOCATION", "LOCATOR", // DB2
                "LOCATORS", // DB2
                "LOCK", // DB2
                "LOCKMAX", // DB2
                "LOCKSIZE", // DB2
                "LOGIN", "LOGS", "LONG", // DB2
                "LONGBLOB", "LONGTEXT", "LOOP", // DB2
                "LOW_PRIORITY", "LOWER", "M", "MAP", "MATCH", "MATCHED", "MAX", "MAX_ROWS", "MAXEXTENTS", "MAXVALUE", // DB2
                "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MEMBER", "MERGE", "MESSAGE_LENGTH", "MESSAGE_OCTET_LENGTH",
                "MESSAGE_TEXT", "METHOD", "MICROSECOND", // DB2
                "MICROSECONDS", // DB2
                "MIDDLEINT", "MIN", "MIN_ROWS", "MINUS", "MINUTE", // DB2
                "MINUTE_MICROSECOND", "MINUTE_SECOND", "MINUTES", // DB2
                "MINVALUE", // DB2
                "MLSLABEL", "MOD", "MODE", // DB2
                "MODIFIES", // DB2
                "MODIFY", "MODULE", "MONTH", // DB2
                "MONTHNAME", "MONTHS", // DB2
                "MORE", "MOVE", "MULTISET", "MUMPS", "MYISAM", "NAME", "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NCLOB",
                "NESTING", "NEW", // DB2
                "NEW_TABLE", // DB2
                "NEXT", "NO", // DB2
                "NO_WRITE_TO_BINLOG", "NOAUDIT", "NOCACHE", // DB2
                "NOCHECK", "NOCOMPRESS", "NOCREATEDB", "NOCREATEROLE", "NOCREATEUSER", "NOCYCLE", // DB2
                "NODENAME", // DB2
                "NODENUMBER", // DB2
                "NOINHERIT", "NOLOGIN", "NOMAXVALUE", // DB2
                "NOMINVALUE", // DB2
                "NONCLUSTERED", "NONE", "NOORDER", // DB2
                "NORMALIZE", "NORMALIZED", "NOSUPERUSER", "NOT", // DB2
                "NOTHING", "NOTIFY", "NOTNULL", "NOWAIT", "NULL", // DB2
                "NULLABLE", "NULLIF", "NULLS", // DB2
                "NUMBER", "NUMERIC", "NUMPARTS", // DB2
                "OBID", // DB2
                "OBJECT", "OCTET_LENGTH", "OCTETS", "OF", // DB2
                "OFF", "OFFLINE", "OFFSET", "OFFSETS", "OIDS", "OLD", // DB2
                "OLD_TABLE", // DB2
                "ON", // DB2
                "ONLINE", "ONLY", "OPEN", // DB2
                "OPENDATASOURCE", "OPENQUERY", "OPENROWSET", "OPENXML", "OPERATION", "OPERATOR", "OPTIMIZATION", // DB2
                "OPTIMIZE", // DB2
                "OPTION", // DB2
                "OPTIONALLY", "OPTIONS", "OR", // DB2
                "ORDER", // DB2
                "ORDERING", "ORDINALITY", "OTHERS", "OUT", // DB2
                "OUTER", // DB2
                "OUTFILE", "OUTPUT", "OVER", "OVERLAPS", "OVERLAY", "OVERRIDING", // DB2
                "OWNER", "PACK_KEYS", "PACKAGE", // DB2
                "PAD", "PARAMETER", // DB2
                "PARAMETER_MODE", "PARAMETER_NAME", "PARAMETER_ORDINAL_POSITION", "PARAMETER_SPECIFIC_CATALOG",
                "PARAMETER_SPECIFIC_NAME", "PARAMETER_SPECIFIC_SCHEMA", "PARAMETERS", "PART", // DB2
                "PARTIAL", "PARTITION", // DB2
                "PASCAL", "PASSWORD", "PATH", // DB2
                "PCTFREE", "PERCENT", "PERCENT_RANK", "PERCENTILE_CONT", "PERCENTILE_DISC", "PIECESIZE", // DB2
                "PLACING", "PLAN", // DB2
                "PLI", "POSITION", // DB2
                "POSTFIX", "POWER", "PRECEDING", "PRECISION", // DB2
                "PREFIX", "PREORDER", "PREPARE", // DB2
                "PREPARED", "PRESERVE", "PRIMARY", // DB2
                "PRINT", "PRIOR", "PRIQTY", // DB2
                "PRIVILEGES", // DB2
                "PROC", "PROCEDURAL", "PROCEDURE", // DB2
                "PROCESS", "PROCESSLIST", "PROGRAM", // DB2
                "PSID", // DB2
                "PUBLIC", "PURGE", "QUERYNO", // DB2
                "QUOTE", "RAID0", "RAISERROR", "RANGE", "RANK", "RAW", "READ", // DB2
                "READS", // DB2
                "READTEXT", "REAL", "RECHECK", "RECONFIGURE", "RECOVERY", // DB2
                "RECURSIVE", "REF", "REFERENCES", // DB2
                "REFERENCING", // DB2
                "REGEXP", "REGR_AVGX", "REGR_AVGY", "REGR_COUNT", "REGR_INTERCEPT", "REGR_R2", "REGR_SLOPE", "REGR_SXX",
                "REGR_SXY", "REGR_SYY", "REINDEX", "RELATIVE", "RELEASE", // DB2
                "RELOAD", "RENAME", // DB2
                "REPEAT", // DB2
                "REPEATABLE", "REPLACE", "REPLICATION", "REQUIRE", "RESET", // DB2
                "RESIGNAL", // DB2
                "RESOURCE", "RESTART", // DB2
                "RESTORE", "RESTRICT", // DB2
                "RESULT", // DB2
                "RESULT_SET_LOCATOR", // DB2
                "RETURN", // DB2
                "RETURNED_CARDINALITY", "RETURNED_LENGTH", "RETURNED_OCTET_LENGTH", "RETURNED_SQLSTATE", "RETURNS", // DB2
                "REVOKE", // DB2
                "RIGHT", // DB2
                "RLIKE", "ROLE", "ROLLBACK", // DB2
                "ROLLUP", "ROUTINE", // DB2
                "ROUTINE_CATALOG", "ROUTINE_NAME", "ROUTINE_SCHEMA", "ROW", // DB2
                "ROW_COUNT", "ROW_NUMBER", "ROWCOUNT", "ROWGUIDCOL", "ROWID", "ROWNUM", "ROWS", // DB2
                "RRN", // DB2
                "RULE", "RUN", // DB2
                "SAVE", "SAVEPOINT", // DB2
                "SCALE", "SCHEMA", // DB2
                "SCHEMA_NAME", "SCHEMAS", "SCOPE", "SCOPE_CATALOG", "SCOPE_NAME", "SCOPE_SCHEMA", "SCRATCHPAD", // DB2
                "SCROLL", "SEARCH", "SECOND", // DB2
                "SECOND_MICROSECOND", "SECONDS", // DB2
                "SECQTY", // DB2
                "SECTION", "SECURITY", // DB2
                "SELECT", // DB2
                "SELF", "SENSITIVE", // DB2
                "SEPARATOR", "SEQUENCE", "SERIALIZABLE", "SERVER_NAME", "SESSION", "SESSION_USER", "SET", // DB2
                "SETOF", "SETS", "SETUSER", "SHARE", "SHOW", "SHUTDOWN", "SIGNAL", // DB2
                "SIMILAR", "SIMPLE", // DB2
                "SIZE", "SMALLINT", "SOME", // DB2
                "SONAME", "SOURCE", // DB2
                "SPACE", "SPATIAL", "SPECIFIC", // DB2
                "SPECIFIC_NAME", "SPECIFICTYPE", "SQL", // DB2
                "SQL_BIG_RESULT", "SQL_BIG_SELECTS", "SQL_BIG_TABLES", "SQL_CALC_FOUND_ROWS", "SQL_LOG_OFF",
                "SQL_LOG_UPDATE", "SQL_LOW_PRIORITY_UPDATES", "SQL_SELECT_LIMIT", "SQL_SMALL_RESULT", "SQL_WARNINGS",
                "SQLCA", "SQLCODE", "SQLERROR", "SQLEXCEPTION", "SQLID", // DB2
                "SQLSTATE", "SQLWARNING", "SQRT", "SSL", "STABLE", "STANDARD", // DB2
                "START", // DB2
                "STARTING", "STATE", "STATEMENT", "STATIC", // DB2
                "STATISTICS", "STATUS", "STAY", // DB2
                "STDDEV_POP", "STDDEV_SAMP", "STDIN", "STDOUT", "STOGROUP", // DB2
                "STORAGE", "STORES", // DB2
                "STRAIGHT_JOIN", "STRICT", "STRING", "STRUCTURE", "STYLE", // DB2
                "SUBCLASS_ORIGIN", "SUBLIST", "SUBMULTISET", "SUBPAGES", // DB2
                "SUBSTRING", // DB2
                "SUCCESSFUL", "SUM", "SUPERUSER", "SYMMETRIC", "SYNONYM", // DB2
                "SYSDATE", "SYSFUN", // DB2
                "SYSIBM", // DB2
                "SYSID", "SYSPROC", // DB2
                "SYSTEM", // DB2
                "SYSTEM_USER", "TABLE", // DB2
                "TABLE_NAME", "TABLES", "TABLESAMPLE", "TABLESPACE", // DB2
                "TEMP", "TEMPLATE", "TEMPORARY", "TERMINATE", "TERMINATED", "TEXT", "TEXTSIZE", "THAN", "THEN", // DB2
                "TIES", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TINYBLOB", "TINYINT", "TINYTEXT",
                "TO", // DB2
                "TOAST", "TOP", "TOP_LEVEL_COUNT", "TRAILING", "TRAN", "TRANSACTION", // DB2
                "TRANSACTION_ACTIVE", "TRANSACTIONS_COMMITTED", "TRANSACTIONS_ROLLED_BACK", "TRANSFORM", "TRANSFORMS",
                "TRANSLATE", "TRANSLATION", "TREAT", "TRIGGER", // DB2
                "TRIGGER_CATALOG", "TRIGGER_NAME", "TRIGGER_SCHEMA", "TRIM", // DB2
                "TRUE", "TRUNCATE", "TRUSTED", "TSEQUAL", "TYPE", // DB2
                "UESCAPE", "UID", "UNBOUNDED", "UNCOMMITTED", "UNDER", "UNDO", // DB2
                "UNENCRYPTED", "UNION", // DB2
                "UNIQUE", // DB2
                "UNKNOWN", "UNLISTEN", "UNLOCK", "UNNAMED", "UNNEST", "UNSIGNED", "UNTIL", // DB2
                "UPDATE", // DB2
                "UPDATETEXT", "UPPER", "USAGE", // DB2
                "USE", "USER", // DB2
                "USER_DEFINED_TYPE_CATALOG", "USER_DEFINED_TYPE_CODE", "USER_DEFINED_TYPE_NAME",
                "USER_DEFINED_TYPE_SCHEMA", "USING", // DB2
                "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VACUUM", "VALID", "VALIDATE", "VALIDATOR", "VALIDPROC", // DB2
                "VALUE", "VALUES", // DB2
                "VAR_POP", "VAR_SAMP", "VARBINARY", "VARCHAR", "VARCHAR2", "VARCHARACTER", "VARIABLE", // DB2
                "VARIABLES", "VARIANT", // DB2
                "VARYING", "VCAT", // DB2
                "VERBOSE", "VIEW", // DB2
                "VIRTUAL", // MySQL #272
                "VOLATILE", "VOLUMES", // DB2
                "WAITFOR", "WHEN", // DB2
                "WHENEVER", "WHERE", // DB2
                "WHILE", // DB2
                "WIDTH_BUCKET", "WINDOW", "WITH", // DB2
                "WITHIN", "WITHOUT", "WLM", // DB2
                "WORK", "WRITE", // DB2
                "WRITETEXT", "X509", "XOR", "YEAR", // DB2
                "YEAR_MONTH", "YEARS", // DB2
                "ZEROFILL", "ZONE" };

        RESERVED_WORDS = new HashSet<>(words.length);

        for (String word : words) {
            RESERVED_WORDS.add(word);
        }
    }

    public static boolean containsWord(String word) {
        boolean rc;

        if (word == null) {
            rc = false;
        } else {
            rc = RESERVED_WORDS.contains(word.toUpperCase());
        }

        return rc;
    }

}
