package org.aoju.bus.mapper.criteria;


/**
 * 主键处理
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public enum Identity {

    DB2("VALUES IDENTITY_VAL_LOCAL()"),
    MYSQL("SELECT LAST_INSERT_ID()"),
    SQLSERVER("SELECT SCOPE_IDENTITY()"),
    CLOUDSCAPE("VALUES IDENTITY_VAL_LOCAL()"),
    DERBY("VALUES IDENTITY_VAL_LOCAL()"),
    HSQLDB("CALL IDENTITY()"),
    SYBASE("SELECT @@IDENTITY"),
    DB2_MF("SELECT IDENTITY_VAL_LOCAL() FROM SYSIBM.SYSDUMMY1"),
    INFORMIX("select dbinfo('sqlca.sqlerrd1') from systables where tabid=1");

    private String identityRetrievalStatement;

    private Identity(String identityRetrievalStatement) {
        this.identityRetrievalStatement = identityRetrievalStatement;
    }

    public static Identity getDatabaseDialect(String database) {
        Identity returnValue = null;
        if ("DB2".equalsIgnoreCase(database)) {
            returnValue = DB2;
        } else if ("MySQL".equalsIgnoreCase(database)) {
            returnValue = MYSQL;
        } else if ("SqlServer".equalsIgnoreCase(database)) {
            returnValue = SQLSERVER;
        } else if ("Cloudscape".equalsIgnoreCase(database)) {
            returnValue = CLOUDSCAPE;
        } else if ("Derby".equalsIgnoreCase(database)) {
            returnValue = DERBY;
        } else if ("HSQLDB".equalsIgnoreCase(database)) {
            returnValue = HSQLDB;
        } else if ("SYBASE".equalsIgnoreCase(database)) {
            returnValue = SYBASE;
        } else if ("DB2_MF".equalsIgnoreCase(database)) {
            returnValue = DB2_MF;
        } else if ("Informix".equalsIgnoreCase(database)) {
            returnValue = INFORMIX;
        }
        return returnValue;
    }

    public String getIdentityRetrievalStatement() {
        return identityRetrievalStatement;
    }
}
