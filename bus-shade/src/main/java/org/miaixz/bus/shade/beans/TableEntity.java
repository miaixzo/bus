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
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.shade.beans;

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.core.lang.Symbol;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the basic information required for automatic code generation based on a database table. This class holds
 * metadata about the project, author, database connection, table structure, and naming conventions.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class TableEntity implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * The project name.
     */
    private String project;

    /**
     * The author of the generated code.
     */
    private String author;

    /**
     * The version of the generated code.
     */
    private String version;

    /**
     * The database connection URL.
     */
    private String url;

    /**
     * The database username.
     */
    private String user;

    /**
     * The database password.
     */
    private String password;

    /**
     * The database name.
     */
    private String database;

    /**
     * The table name.
     */
    private String table;

    /**
     * The generated entity class name.
     */
    private String entityName;

    /**
     * The generated object name (e.g., for service or controller).
     */
    private String objectName;

    /**
     * The comment for the entity class.
     */
    private String entityComment;

    /**
     * The creation time of the generated code.
     */
    private String createTime;

    /**
     * A string representing agile development related information, possibly a list of columns.
     */
    private String agile;

    /**
     * The package URL for the entity class.
     */
    private String entityUrl;

    /**
     * The package URL for the mapper interface.
     */
    private String mapperUrl;

    /**
     * The package URL for the mapper XML file.
     */
    private String mapperXmlUrl;

    /**
     * The package URL for the service interface.
     */
    private String serviceUrl;

    /**
     * The package URL for the service implementation class.
     */
    private String serviceImplUrl;

    /**
     * The package URL for the controller class.
     */
    private String controllerUrl;

    /**
     * The Java type of the ID column.
     */
    private String idType;

    /**
     * The JDBC type of the ID column.
     */
    private String idJdbcType;

    /**
     * A list of {@link PropertyInfo} objects representing the columns of the table.
     */
    private List<PropertyInfo> cis;

    /**
     * Indicates whether Swagger documentation should be generated.
     */
    private String isSwagger;

    /**
     * Indicates whether to use hump (camelCase) naming convention for fields.
     */
    private boolean isHump;

    /**
     * Indicates whether Dubbo-related code should be generated.
     */
    private String isDubbo;

    /**
     * Constructs a new TableEntity with the specified parameters.
     *
     * @param project        The project name.
     * @param author         The author of the generated code.
     * @param version        The version of the generated code.
     * @param url            The database connection URL template.
     * @param user           The database username.
     * @param password       The database password.
     * @param database       The database name.
     * @param table          The table name.
     * @param agile          Agile development related information.
     * @param entityUrl      The package URL for the entity class.
     * @param mapperUrl      The package URL for the mapper interface.
     * @param mapperXmlUrl   The package URL for the mapper XML file.
     * @param serviceUrl     The package URL for the service interface.
     * @param serviceImplUrl The package URL for the service implementation class.
     * @param controllerUrl  The package URL for the controller class.
     * @param isSwagger      Indicates whether Swagger documentation should be generated.
     * @param isDubbo        Indicates whether Dubbo-related code should be generated.
     * @param isHump         Indicates whether to use hump (camelCase) naming convention for fields.
     */
    public TableEntity(String project, String author, String version, String url, String user, String password,
            String database, String table, String agile, String entityUrl, String mapperUrl, String mapperXmlUrl,
            String serviceUrl, String serviceImplUrl, String controllerUrl, String isSwagger, String isDubbo,
            boolean isHump) {
        super();
        this.project = project;
        this.author = author;
        this.version = version;
        this.url = url.replace("database", database);
        this.user = user;
        this.password = password;
        this.database = database;
        this.table = table;
        this.agile = agile;
        this.entityUrl = entityUrl;
        this.mapperUrl = mapperUrl;
        this.mapperXmlUrl = mapperXmlUrl;
        this.serviceUrl = serviceUrl;
        this.serviceImplUrl = serviceImplUrl;
        this.controllerUrl = controllerUrl;
        this.isSwagger = isSwagger;
        this.isDubbo = isDubbo;
        this.isHump = isHump;
    }

    /**
     * Populates the {@link TableEntity} with column information retrieved from the database. It connects to the
     * database using the provided credentials and queries the table metadata.
     *
     * @param entity The {@link TableEntity} object to be populated with column details.
     * @return The populated {@link TableEntity} object.
     * @throws RuntimeException if there is an error connecting to the database, querying metadata, or if no columns are
     *                          found for the specified table.
     */
    public static TableEntity get(TableEntity entity) {
        List<PropertyInfo> columns = new ArrayList<>();
        // Database connection objects
        Connection con = null;
        PreparedStatement pstemt = null;
        ResultSet rs = null;
        // SQL query to retrieve column metadata
        String sql = "select column_name,data_type,column_comment from information_schema.columns where table_schema='"
                + entity.getDatabase() + "' and table_name='" + entity.getTable() + Symbol.SINGLE_QUOTE;
        try {
            // Establish database connection
            con = DriverManager.getConnection(entity.url, entity.user, entity.password);
            pstemt = con.prepareStatement(sql);
            rs = pstemt.executeQuery();
            // Process query results
            while (rs.next()) {
                String column = rs.getString(1);
                String jdbcType = rs.getString(2);
                String comment = rs.getString(3);
                PropertyInfo ci = new PropertyInfo();
                ci.setColumn(column);
                // Adjust JDBC type mapping for specific cases
                if (jdbcType.equalsIgnoreCase("int")) {
                    ci.setJdbcType("Integer");
                } else if (jdbcType.equalsIgnoreCase("datetime")) {
                    ci.setJdbcType("timestamp");
                } else {
                    ci.setJdbcType(jdbcType);
                }
                ci.setComment(comment);
                ci.setProperty(NamingRules.changeToJavaFiled(column, entity.isHump));
                ci.setJavaType(NamingRules.jdbcTypeToJavaType(jdbcType));
                // Set ID type if the column is 'id'
                if (column.equalsIgnoreCase("id")) {
                    entity.setIdType(ci.getJavaType());
                    entity.setIdJdbcType(ci.getJdbcType());
                }
                columns.add(ci);
            }
            entity.setCis(columns);
            // Close resources
            rs.close();
            pstemt.close();
            con.close();
            // Validate if columns were retrieved
            if (null == columns || columns.size() == 0) {
                throw new RuntimeException(
                        "Failed to read table or columns. Please check the connection URL, database account, password, and table name.");
            }
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating entity class: " + e.getMessage());
        } finally {
            // Ensure resources are closed in finally block
            try {
                if (null != rs)
                    rs.close();
            } catch (SQLException se2) {
                // Log or handle exception if closing fails
            }
            try {
                if (null != pstemt)
                    pstemt.close();
            } catch (SQLException se2) {
                // Log or handle exception if closing fails
            }
            try {
                if (null != con)
                    con.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

}
